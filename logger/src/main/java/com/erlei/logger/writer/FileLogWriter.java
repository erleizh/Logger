package com.erlei.logger.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by lll on 2019/9/12
 * Email : erleizh@gmail.com
 * Describe : 使用FileOutputStream将日志写入文件
 */
public class FileLogWriter extends LogWriter {

    private FileOutputStream mWriter;
    private File mFile;

    public FileLogWriter(File file) {
        mFile = file;
        try {
            mWrittenBytes = mFile.length();
            mWriter = new FileOutputStream(mFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getWrittenBytes() {
        return mWrittenBytes;
    }

    @Override
    public void write(byte[] buffer, int off, int len) throws IOException {
        mWrittenBytes += len;
        mWriter.write(buffer, off, len);
    }

    @Override
    public void close() throws IOException {
        mWriter.close();
    }

    @Override
    public void flush() throws IOException {
        mWriter.flush();
    }
}
