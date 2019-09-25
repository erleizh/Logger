package com.erlei.logger.format;

import android.support.annotation.NonNull;
import com.erlei.logger.LogLine;

/**
 * Used to determine how messages should be printed or saved.
 *
 * @see PrettyFormatStrategy
 * @see CsvFormatStrategy
 * @see TextFormatStrategy
 */
public interface FormatStrategy {

    void log(@NonNull LogLine logLine);
}
