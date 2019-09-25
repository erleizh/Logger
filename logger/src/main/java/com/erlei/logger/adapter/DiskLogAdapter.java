package com.erlei.logger.adapter;

import android.support.annotation.NonNull;
import com.erlei.logger.LogLine;
import com.erlei.logger.format.FormatStrategy;
import com.erlei.logger.format.TextFormatStrategy;

import java.util.Objects;

import static com.erlei.logger.Logger.DEBUG;


/**
 * This is used to saves log messages to the disk.
 * By default it uses {@link TextFormatStrategy} to translates text message into txt format.
 */
public class DiskLogAdapter implements LogAdapter {

    @NonNull
    private final FormatStrategy formatStrategy;

    public DiskLogAdapter() {
        formatStrategy = TextFormatStrategy.newBuilder().build();
    }

    public DiskLogAdapter(@NonNull FormatStrategy formatStrategy) {
        this.formatStrategy = Objects.requireNonNull(formatStrategy);
    }

    @Override
    public boolean isLoggable(@NonNull LogLine logLine) {
        return logLine.getLevel() > DEBUG;
    }

    @Override
    public void log(@NonNull LogLine logLine) {
        formatStrategy.log(logLine);
    }

}
