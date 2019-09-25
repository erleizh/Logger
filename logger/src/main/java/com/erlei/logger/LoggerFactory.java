package com.erlei.logger;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.erlei.logger.adapter.LogAdapter;
import com.erlei.logger.format.ArgsFormatter;
import com.erlei.logger.format.SimpleArgsFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class LoggerFactory {
    private static final String TAG = "LoggerFactory";
    private static List<LogAdapter> adapters = new ArrayList<>();

    @NonNull
    static Printer sPrinter = new LoggerPrinter();
    @NonNull
    static ArgsFormatter sArgsFormatter = new SimpleArgsFormatter("\t");

    public static void printer(@NonNull Printer printer) {
        sPrinter = Objects.requireNonNull(printer);
    }

    public static void argsFormatter(@NonNull ArgsFormatter argsFormatter) {
        sArgsFormatter = Objects.requireNonNull(argsFormatter);
    }

    public static void addLogAdapter(@NonNull LogAdapter adapter) {
        adapters.add(Objects.requireNonNull(adapter));
    }

    public static void clearLogAdapters() {
        adapters.clear();
    }

    /**
     * @return create a default Logger
     */
    @NonNull
    public static Logger create() {
        return create((String) null);
    }

    /**
     * Use obj class name as tag
     *
     * @param obj obj
     * @return Logger
     */
    @NonNull
    public static Logger create(@Nullable Object obj) {
        String tag = null;
        if (obj != null) tag = obj.getClass().getSimpleName();
        return create(tag);
    }

    /**
     * Use obj class name as tag
     *
     * @param clazz clazz
     * @return Logger
     */
    @NonNull
    public static Logger create(@Nullable Class clazz) {
        String tag = null;
        if (clazz != null) tag = clazz.getSimpleName();
        return create(tag);
    }


    @NonNull
    public static Logger create(@Nullable String tag) {
        Logger.Builder builder = new Logger.Builder(tag);
        for (LogAdapter adapter : adapters) {
            builder.addLogAdapter(adapter);
        }
        return builder.build();
    }
}
