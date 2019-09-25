package com.erlei.logger.adapter;

import android.support.annotation.NonNull;
import com.erlei.logger.LogLine;
import com.erlei.logger.format.FormatStrategy;
import com.erlei.logger.format.TextFormatStrategy;
import com.erlei.logger.printer.LogcatPrintStrategy;

import java.util.Objects;


/**
 * Android terminal log output implementation for {@link LogAdapter}.
 */
public class AndroidLogAdapter implements LogAdapter {

    @NonNull
    private final FormatStrategy formatStrategy;

    public AndroidLogAdapter() {
        this.formatStrategy = TextFormatStrategy.newBuilder().logStrategy(new LogcatPrintStrategy()).build();
    }

    public AndroidLogAdapter(@NonNull FormatStrategy formatStrategy) {
        this.formatStrategy = Objects.requireNonNull(formatStrategy);
    }

    @Override
    public boolean isLoggable(@NonNull LogLine logLine) {
        return true;
    }

    @Override
    public void log(@NonNull LogLine logLine) {
        formatStrategy.log(logLine);
    }
}
