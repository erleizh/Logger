package com.erlei.logger.writer;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 异步写日志，已废弃
 *
 * @see com.erlei.logger.adapter.AsyncLogAdapter
 */
@Deprecated
public class AsyncLogWriter extends LogWriter implements Handler.Callback {

    private static final int MSG_WRITE = 1;
    private static final int MSG_CLOSE = 2;
    private static final int MSG_FLUSH = 3;
    private final LogWriter mWriter;
    private final LinkedBlockingQueue<Message> mQueue = new LinkedBlockingQueue<>();
    private final Thread mThread;

    public AsyncLogWriter(@NonNull LogWriter writer) {
        mWriter = writer;
        mWrittenBytes = mWriter.getWrittenBytes();
        mThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Message take = mQueue.take();
                    if (take != null) {
                        handleMessage(take);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        mThread.setName(getClass().getSimpleName());
        mThread.start();
    }


    @Override
    public long getWrittenBytes() {
        return mWrittenBytes;
    }

    @Override
    public void write(byte[] buffer, int off, int len) {
        mWrittenBytes += len;
        try {
            mQueue.put(Message.obtain(null, MSG_WRITE, off, len, buffer));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            mQueue.put(Message.obtain(null, MSG_CLOSE));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void flush() {
        try {
            mQueue.put(Message.obtain(null, MSG_FLUSH));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_WRITE:
                try {
                    mWriter.write((byte[]) msg.obj, msg.arg1, msg.arg2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case MSG_FLUSH:
                try {
                    mWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case MSG_CLOSE:
                try {
                    mWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mThread.interrupt();
                }
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + ":" + mWriter.toString();
    }
}
