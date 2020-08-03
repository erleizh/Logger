package com.erlei.logger.writer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.erlei.logger.LogLine;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Create by erlei on 2019-11-04
 *
 * Email : erleizh@gmail.com
 *
 * Describe : 之所以不直接使用writer是因为 write(byte[] buffer, int off, int len) 方法签名不一样
 */
public abstract class LogWriter implements Closeable, Flushable {

    /**
     * the object used to synchronize operations on this stream.  for
     * efficiency, a character-stream object may use an object other than
     * itself to protect critical sections.  a subclass should therefore use
     * the object in this field rather than <tt>this</tt> or a synchronized
     * method.
     */
    protected final Object lock;
    protected long mWrittenBytes;

    /**
     * Creates a new LogWriter whose critical sections will
     * synchronize on the writer itself.
     */
    protected LogWriter() {
        this.lock = this;
    }

    /**
     * Creates a new LogWriter whose critical sections will
     * synchronize on the given object.
     *
     * @param lock Object to synchronize on
     */
    protected LogWriter(Object lock) {
        if (lock == null) {
            throw new NullPointerException();
        }
        this.lock = lock;
    }

    /**
     * Writes an array of bytes.
     *
     * @param buffer Array of bytes to be written
     * @throws IOException If an I/O error occurs
     */
    public void write(@NonNull byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    /**
     * Writes a string.
     *
     * @param str String to be written
     * @throws IOException If an I/O error occurs
     */
    public void write(@Nullable String str) throws IOException {
        String s = (str == null ? "null" : str);
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        write(bytes, 0, bytes.length);
    }


    public void write(@NonNull LogLine log) throws IOException {
        write(log.getMessage());
    }

    public void write(@NonNull List<LogLine> logs) throws IOException {
        if (logs.isEmpty()) return;
        synchronized (lock) {
            for (LogLine log : logs) if (log != null) write(log);
        }
    }

    public abstract long getWrittenBytes();

    @NonNull
    public LogWriter append(@Nullable CharSequence csq) throws IOException {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.toString());
        return this;
    }

    @NonNull
    public LogWriter append(@Nullable CharSequence csq, int start, int end) throws IOException {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    /**
     * Writes a portion of an array of byte.
     *
     * @param buffer Array of byte
     * @param off    Offset from which to start writing byte
     * @param len    Number of byte to write
     * @throws IOException If an I/O error occurs
     */
    abstract public void write(byte[] buffer, int off, int len) throws IOException;

}
