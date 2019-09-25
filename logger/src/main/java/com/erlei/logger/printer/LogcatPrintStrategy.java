package com.erlei.logger.printer;

import android.support.annotation.NonNull;
import android.util.Log;
import com.erlei.logger.LogLine;

import java.util.Objects;


/**
 * LogCat implementation for {@link PrintStrategy}
 * <p>
 * This simply prints out all logs to Logcat by using standard {@link Log} class.
 */
public class LogcatPrintStrategy implements PrintStrategy {

    @Override
    public void print(@NonNull LogLine logLine) {
        Objects.requireNonNull(logLine);
        Log.println(logLine.getLevel(), logLine.getFulTag(), logLine.getMessage());
        logLine.recycle();
    }
}
