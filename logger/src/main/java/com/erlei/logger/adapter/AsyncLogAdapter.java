package com.erlei.logger.adapter;

import android.support.annotation.NonNull;
import com.erlei.logger.LogLine;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by lll on 2019/9/12
 * Email : erleizh@gmail.com
 * Describe : 异步的LogAdapter
 */
public class AsyncLogAdapter implements LogAdapter {

    private final LogAdapter mLogAdapter;
    private final LinkedBlockingQueue<LogLine> log = new LinkedBlockingQueue<>();
    private final Thread mThread;

    public AsyncLogAdapter(@NonNull LogAdapter adapter) {
        mLogAdapter = adapter;
        mThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                LogLine logLine;
                try {
                    logLine = log.take();
                    if (logLine != null) {
                        mLogAdapter.log(logLine);
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
    public boolean isLoggable(@NonNull LogLine logLine) {
        return mLogAdapter.isLoggable(logLine);
    }

    @Override
    public void log(@NonNull LogLine logLine) {
        try {
            log.put(logLine);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
