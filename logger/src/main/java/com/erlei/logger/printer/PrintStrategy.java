package com.erlei.logger.printer;

import android.support.annotation.NonNull;
import com.erlei.logger.LogLine;

/**
 * Determines print target for the logs such as Disk, Logcat etc.
 *
 * @see LogcatPrintStrategy
 * @see DiskPrintStrategy
 */
public interface PrintStrategy {

    /**
     * This is invoked by Logger each time a log message is processed.
     * Interpret this method as last destination of the log in whole pipeline.
     *
     * @param logLine log info
     */
    void print(@NonNull LogLine logLine);
}
