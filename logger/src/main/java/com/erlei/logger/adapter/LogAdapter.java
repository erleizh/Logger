package com.erlei.logger.adapter;

import android.support.annotation.NonNull;
import com.erlei.logger.LogLine;

/**
 * Provides a common interface to emits logs through. This is a required contract for Logger.
 *
 * @see AndroidLogAdapter
 * @see DiskLogAdapter
 * @see AsyncLogAdapter
 */
public interface LogAdapter {

    /**
     * Used to determine whether log should be printed out or not.
     *
     * @param logLine log info
     * @return is used to determine if log should printed.
     * If it is true, it will be printed, otherwise it'll be ignored.
     */
    boolean isLoggable(@NonNull LogLine logLine);

    /**
     * Each log will use this pipeline
     *
     * @param logLine log info
     */
    void log(@NonNull LogLine logLine);
}