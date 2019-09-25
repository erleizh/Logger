package com.erlei.logger;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import com.erlei.logger.adapter.LogAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.erlei.logger.Logger.*;


@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class LoggerPrinter implements Printer {

    /**
     * Provides one-time used tag for the log message
     */
    private final ThreadLocal<String> localTag = new ThreadLocal<>();

    private final List<LogAdapter> logAdapters = new ArrayList<>();

    @Override
    public Printer t(String tag) {
        if (tag != null) {
            localTag.set(tag);
        }
        return this;
    }

    @Override
    public void d(@NonNull String message, @Nullable Object... args) {
        log(DEBUG, null, message, args);
    }


    @Override
    public void e(@NonNull String message, @Nullable Object... args) {
        e(null, message, args);
    }

    @Override
    public void e(@Nullable Throwable throwable, @NonNull String message, @Nullable Object... args) {
        log(ERROR, throwable, message, args);
    }

    @Override
    public void w(@NonNull String message, @Nullable Object... args) {
        log(WARN, null, message, args);
    }

    @Override
    public void i(@NonNull String message, @Nullable Object... args) {
        log(INFO, null, message, args);
    }

    @Override
    public void v(@NonNull String message, @Nullable Object... args) {
        log(VERBOSE, null, message, args);
    }

    @Override
    public void wtf(@NonNull String message, @Nullable Object... args) {
        log(ASSERT, null, message, args);
    }

    @Override
    public void json(@Nullable String json) {
        LogLine obtain = LogLine.obtain(DEBUG, getTag());
        obtain.setJson(json == null ? "" : json);
        log(obtain);
    }

    @Override
    public void xml(@Nullable String xml) {
        LogLine obtain = LogLine.obtain(DEBUG, getTag());
        obtain.setXml(xml == null ? "" : xml);
        log(obtain);
    }

    @Override
    public void log(@Level int level,
                    @Nullable String tag,
                    @Nullable String message,
                    @Nullable Throwable throwable) {
        log(LogLine.obtain(level, tag, message, throwable));
    }

    @Override
    public synchronized void log(@NonNull LogLine line) {
        boolean consumed = false;
        for (LogAdapter adapter : logAdapters) {
            if (adapter.isLoggable(line)) {
                adapter.log(consumed ? line.clone() : line);
                consumed = true;
            }
        }
        if (!consumed) line.recycle();
    }

    @Override
    public void clearLogAdapters() {
        logAdapters.clear();
    }

    @Override
    public void args(Object... args) {
        LogLine obtain = LogLine.obtain(DEBUG, getTag());
        obtain.setArgs(args);
        log(obtain);
    }

    @Override
    public void addAdapter(@NonNull LogAdapter adapter) {
        logAdapters.add(Objects.requireNonNull(adapter));
    }

    /**
     * This method is synchronized in order to avoid messy of logs' order.
     */
    private synchronized void log(int level,
                                  @Nullable Throwable throwable,
                                  @NonNull String message,
                                  @Nullable Object... args) {
        Objects.requireNonNull(message);
        LogLine obtain = LogLine.obtain(level, getTag());
        obtain.setThrowable(throwable);
        obtain.setFormat(message);
        obtain.setArgs(args);
        log(obtain);
    }

    /**
     * @return the appropriate tag based on local or global
     */
    @Nullable
    private String getTag() {
        String tag = localTag.get();
        if (tag != null) {
            localTag.remove();
            return tag;
        }
        return null;
    }
}
