package com.erlei.logger;

import android.support.annotation.NonNull;
import com.erlei.logger.writer.LogWriter;

import java.io.File;

public interface LogWriterFactory {
    @NonNull
    LogWriter create(File file);
}
