package com.erlei.logger.writer;

import android.support.annotation.NonNull;
import com.erlei.logger.LogLine;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by lll on 2019/8/30
 * Email : erleizh@gmail.com
 * Describe : 使用 mmap技术 将日志写入文件
 * <p>
 */
public class MMAPLogWriter extends LogWriter {

    static {
        System.loadLibrary("mmap-writer");
    }

    private int mPageCount = 5;

    private File file;
    protected long mNativeWriter;

    public MMAPLogWriter(File file, int pageCount) throws IOException {
        this.file = file;
        if (pageCount > 0) this.mPageCount = pageCount;
        replaceLastNulChar(file, "");
        mWrittenBytes = file.length();// must be before init,
        mNativeWriter = nInit(file.getAbsolutePath(), this.mPageCount);
        // init 之后 length() = pageCount * 1024(page_size)
    }

    @Override
    public void write(@NonNull LogLine log) throws IOException {
        synchronized (lock) {
            write(log.getMessage());
        }
    }

    @Override
    public void write(@NonNull List<LogLine> logs) throws IOException {
        if (logs.isEmpty()) return;
        synchronized (lock) {
            for (LogLine log : logs) {
                write(log);
            }
        }
    }

    @Override
    public long getWrittenBytes() {
        return mWrittenBytes;
//        return nGetWrittenBytes(mNativeWriter);
    }

    @Override
    public void write(byte[] buffer, int off, int len) throws IOException {
        synchronized (lock) {
            mWrittenBytes += len;
            nWrite(mNativeWriter, buffer, off, len);
        }
    }


    /**
     * Didn't do anything
     */
    @Override
    public void flush() {
    }

    @Override
    public void close() throws IOException {
        synchronized (lock) {
            nClose(mNativeWriter);
            mNativeWriter = 0;
        }
    }

    /**
     * 替换因异常情况导致文件末尾产生的 NUL 字符
     * 已知没有调用close会导致文件末尾产生NUL字符
     *
     * @param file        文件
     * @param replacement 要替换NUL的文字
     */
    public static void replaceLastNulChar(@NonNull File file, @NonNull String replacement) {
        if (file.exists() && file.length() > 0) {
            try {
                nReplaceLastNulChar(file.getAbsolutePath(), replacement);
            } catch (IOException ignored) {
            }
        }
    }

    private static native void nReplaceLastNulChar(@NonNull String path, @NonNull String replacement) throws IOException;

    public native long nInit(@NonNull String path, int pageCount) throws IOException;

    public native long nGetWrittenBytes(long nativeWriter);

    public native void nClose(long nativeWriter) throws IOException;

    public native void nWrite(long nativeWriter, @NonNull byte[] buffer, int off, int len) throws IOException;


}
