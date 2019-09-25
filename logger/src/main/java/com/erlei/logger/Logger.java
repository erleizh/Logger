package com.erlei.logger;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.erlei.logger.adapter.LogAdapter;
import com.erlei.logger.format.FormatStrategy;
import com.erlei.logger.printer.PrintStrategy;
import com.erlei.logger.writer.MMAPLogWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.erlei.logger.LoggerFactory.sPrinter;


/**
 * <pre>
 *  ┌────────────────────────────────────────────
 *  │ LOGGER
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ Standard logging mechanism
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ But more pretty, simple and powerful
 *  └────────────────────────────────────────────
 * </pre>
 *
 * <h3>How to use it</h3>
 * Initialize it first
 * <pre><code>
 *   Logger.addLogAdapter(new AndroidLogAdapter());
 * </code></pre>
 * <p>
 * And use the appropriate static Logger methods.
 *
 * <pre><code>
 *   Logger.d("debug");
 *   Logger.e("error");
 *   Logger.w("warning");
 *   Logger.v("verbose");
 *   Logger.i("information");
 *   Logger.wtf("What a Terrible Failure");
 * </code></pre>
 *
 * <h3>String format arguments are supported</h3>
 * <pre><code>
 *   Logger.d("hello %s", "world");
 * </code></pre>
 *
 * <h3>Collections are support ed(only available for debug logs)</h3>
 * <pre><code>
 *   Logger.d(MAP);
 *   Logger.d(SET);
 *   Logger.d(LIST);
 *   Logger.d(ARRAY);
 * </code></pre>
 *
 * <h3>Json and Xml support (output will be in debug Level)</h3>
 * <pre><code>
 *   Logger.json(JSON_CONTENT);
 *   Logger.xml(XML_CONTENT);
 * </code></pre>
 *
 * <h3>Customize Logger</h3>
 * Based on your needs, you can change the following settings:
 * <ul>
 * <li>Different {@link LogAdapter}</li>
 * <li>Different {@link FormatStrategy}</li>
 * <li>Different {@link PrintStrategy}</li>
 * </ul>
 *
 * @see LogAdapter
 * @see FormatStrategy
 * @see PrintStrategy
 */
public class Logger {
    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    public static final int ASSERT = Log.ASSERT;

    private final String mTag;

    /**
     * Given mTag will be used as mTag only once for this method call regardless of the mTag that's been
     * set during initialization. After this invocation, the general mTag that's been set will
     * be used for the subsequent log calls
     */
    public static Printer t(@Nullable String tag) {
        return sPrinter.t(tag);
    }

    /**
     * General log function that accepts all configurations as parameter
     */
    public static void log(@Level int level, @Nullable String tag, @Nullable String message, @Nullable Throwable throwable) {
        sPrinter.log(level, tag, message, throwable);
    }

    public static void d(@Nullable String tag, @NonNull String message, @Nullable Object... args) {
        t(tag).d(message, args);
    }

    public static void e(@Nullable String tag, @NonNull String message, @Nullable Object... args) {
        t(tag).e(null, message, args);
    }

    public static void e(@Nullable String tag, @Nullable Throwable throwable, @NonNull String message, @Nullable Object... args) {
        t(tag).e(throwable, message, args);
    }

    public static void i(@Nullable String tag, @NonNull String message, @Nullable Object... args) {
        t(tag).i(message, args);
    }

    public static void v(@Nullable String tag, @NonNull String message, @Nullable Object... args) {
        t(tag).v(message, args);
    }

    public static void w(@Nullable String tag, @NonNull String message, @Nullable Object... args) {
        t(tag).w(message, args);
    }

    /**
     * Tip: Use this for exceptional situations to log
     * ie: Unexpected errors etc
     */
    public static void wtf(@Nullable String tag, @NonNull String message, @Nullable Object... args) {
        t(tag).wtf(message, args);
    }

    /**
     * Formats the given json content and print it
     */
    public static void json(@Nullable String tag, @Nullable String json) {
        t(tag).json(json);
    }

    /**
     * Formats the given xml content and print it
     */
    public static void xml(@Nullable String tag, @Nullable String xml) {
        t(tag).xml(xml);
    }

    /**
     * Formats the given args and print it
     */
    public static void args(@Nullable String tag, @Nullable Object... args) {
        t(tag).args(args);
    }

    /**
     * Formats the given args and print it
     */
    public static void args(@Nullable String tag, @Level int level, @Nullable Object... args) {
        t(tag).args(level, args);
    }

    private final Printer mPrinter;

    private Logger(String tag) {
        this.mTag = tag;
        this.mPrinter = sPrinter;
    }

    private Logger(String tag, @Nullable Printer printer) {
        this.mTag = tag;
        this.mPrinter = printer == null ? sPrinter : printer;
    }


    public void addLogAdapter(@NonNull LogAdapter adapter) {
        mPrinter.addAdapter(Objects.requireNonNull(adapter));
    }

    public void clearLogAdapters() {
        mPrinter.clearLogAdapters();
    }


    public void d(@NonNull String message, @Nullable Object... args) {
        mPrinter.t(mTag).d(message, args);
    }

    public void e(@NonNull String message, @Nullable Object... args) {
        mPrinter.t(mTag).e(message, args);
    }

    public void e(@Nullable Throwable throwable, @NonNull String message, @Nullable Object... args) {
        mPrinter.t(mTag).e(throwable, message, args);
    }

    public void i(@NonNull String message, @Nullable Object... args) {
        mPrinter.t(mTag).i(message, args);
    }

    public void v(@NonNull String message, @Nullable Object... args) {
        mPrinter.t(mTag).v(message, args);
    }

    public void w(@NonNull String message, @Nullable Object... args) {
        mPrinter.t(mTag).w(message, args);
    }

    /**
     * Formats the given json content and print it
     */
    public void json(@Nullable String json) {
        mPrinter.t(mTag).json(json);
    }

    /**
     * Tip: Use this for exceptional situations to log
     * ie: Unexpected errors etc
     */
    public void wtf(@NonNull String message, @Nullable Object... args) {
        mPrinter.t(mTag).wtf(message, args);
    }

    /**
     * Formats the given xml content and print it
     */
    public void xml(@Nullable String xml) {
        mPrinter.t(mTag).xml(xml);
    }

    /**
     * Formats the given args and print it
     */
    public void args(@Nullable Object... args) {
        mPrinter.t(mTag).args(args);
    }

    public void log(LogLine logLine) {
        mPrinter.log(logLine);
    }


    static class Builder {

        private Printer printer = new LoggerPrinter();
        private final String tag;
        private List<LogAdapter> adapters = new ArrayList<>();

        Builder(@Nullable String tag) {
            this.tag = tag;
        }

        @NonNull
        public Builder setPrinter(@NonNull Printer printer) {
            this.printer = Objects.requireNonNull(printer);
            return this;
        }

        @NonNull
        public Logger build() {
            Logger logger = new Logger(tag, printer);
            for (LogAdapter adapter : adapters) {
                logger.addLogAdapter(adapter);
            }
            return logger;
        }

        @NonNull
        public Builder addLogAdapter(@NonNull LogAdapter adapter) {
            adapters.add(adapter);
            return this;
        }
    }


    /**
     * 替换文件的NULL 字符
     *
     * @param file        文件
     * @param replacement replacement
     */
    public static void replaceFileNullChar(@NonNull File file, @Nullable String replacement) {
        Objects.requireNonNull(file);
        replacement = replacement == null ? "" : replacement;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                replaceFileNullChar(f, replacement);
            }
        } else {
            MMAPLogWriter.replaceLastNulChar(file, replacement);
        }
    }
}
