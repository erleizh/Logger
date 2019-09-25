package com.erlei.logger;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import com.erlei.logger.printer.DiskPrintStrategy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LogFileManager {

    private static final String TAG = "LogFileManager";

    private static File sDir;
    private static final Map<String, String> sFileHeaders = new HashMap<>();
    private static long sMaxFileLength = 1024 * 400;
    private static DiskPrintStrategy.LogFileProvider sDefaultLogFileProvider;

    public static File getLogFileDir() {
        if (sDir == null) {
            throw new IllegalStateException("The LogFileManager is not initialized，please call LogFileManager.init(Context context)");
        }
        return sDir;
    }

    /**
     * @param length 设置单个文件的最大大小
     */
    public static void setFileMaxLength(@IntRange(from = 1024) long length) {
        sMaxFileLength = length;
    }

    public static void init(@NonNull File dir) {
        if (dir.isFile()) dir = dir.getParentFile();
        sDir = dir;
        sDefaultLogFileProvider = new DiskPrintStrategy.DefaultLogFileProvider(sDir);
    }

    public static DiskPrintStrategy.LogFileProvider getDefaultLogFileProvider() {
        if (sDefaultLogFileProvider == null)
            throw new IllegalStateException("The LogFileManager is not initialized，please call LogFileManager.init(Context context)");
        return sDefaultLogFileProvider;
    }

    /**
     * 获取每个日志文件都需要携带的参数map，
     */
    @NonNull
    public static Map<String, String> getFileHeaders() {
        return sFileHeaders;
    }

    public static void setLogFileProvider(@NonNull DiskPrintStrategy.LogFileProvider logFileProvider) {
        sDefaultLogFileProvider = Objects.requireNonNull(logFileProvider);
    }

    public static void init(@NonNull Context context) {
        init(context.getDir("log", Context.MODE_PRIVATE));
    }

    /**
     * @return 单个文件的最大大小
     */
    public static long getFileMaxLength() {
        return sMaxFileLength;
    }


}
