package com.erlei.logger.format;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.erlei.logger.LogLine;
import com.erlei.logger.Logger;
import com.erlei.logger.LoggerPrinter;
import com.erlei.logger.printer.LogcatPrintStrategy;
import com.erlei.logger.printer.PrintStrategy;

import java.util.Objects;

/**
 * Draws borders around the given log message along with additional information such as :
 *
 * <ul>
 * <li>Thread information</li>
 * <li>Method stack trace</li>
 * </ul>
 *
 * <pre>
 *  ┌──────────────────────────
 *  │ Method stack history
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ Thread information
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ LogLine message
 *  └──────────────────────────
 * </pre>
 *
 * <h3>Customize</h3>
 * <pre><code>
 *   FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
 *       .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
 *       .methodCount(0)         // (Optional) How many method line to show. Default 2
 *       .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
 *       .mPrintStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
 *       .tag("My custom tag")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
 *       .build();
 * </code></pre>
 */
public class PrettyFormatStrategy implements FormatStrategy {

    /**
     * Android's max limit for a log entry is ~4076 bytes,
     * so 4000 bytes is used as chunk size since default charset
     * is UTF-8
     */
    private static final int CHUNK_SIZE = 4000;

    /**
     * The minimum stack trace index, starts at this class after two native calls.
     */
    private static final int MIN_STACK_OFFSET = 5;

    /**
     * Drawing toolbox
     */
    private static final char TOP_LEFT_CORNER = '┌';
    private static final char BOTTOM_LEFT_CORNER = '└';
    private static final char MIDDLE_CORNER = '├';
    private static final char HORIZONTAL_LINE = '│';
    private static final String DOUBLE_DIVIDER = "────────────────────────────────────────────────────────";
    private static final String SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;

    private final int methodCount;
    private final int methodOffset;
    private final boolean showThreadInfo;
    @NonNull
    private final PrintStrategy mPrintStrategy;
    @Nullable
    private final String tag;

    private PrettyFormatStrategy(@NonNull Builder builder) {
        Objects.requireNonNull(builder);

        methodCount = builder.methodCount;
        methodOffset = builder.methodOffset;
        showThreadInfo = builder.showThreadInfo;
        mPrintStrategy = Objects.requireNonNull(builder.mPrintStrategy);
        tag = builder.tag;
    }

    @NonNull
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void log(@NonNull LogLine logLine) {
        Objects.requireNonNull(logLine);

        logLine.setModuleName(tag);
        String tag = logLine.getFulTag();

        logTopBorder(logLine.getLevel(), tag);
        logHeaderContent(logLine.getLevel(), tag, methodCount);

        //get bytes of message with system's default charset (which is UTF-8 for Android)
        byte[] bytes = logLine.getMessage().getBytes();
        int length = bytes.length;
        if (length <= CHUNK_SIZE) {
            if (methodCount > 0) {
                logDivider(logLine.getLevel(), tag);
            }
            logContent(logLine.getLevel(), tag, logLine.getMessage());
            logBottomBorder(logLine.getLevel(), tag);
            return;
        }
        if (methodCount > 0) {
            logDivider(logLine.getLevel(), tag);
        }
        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int count = Math.min(length - i, CHUNK_SIZE);
            //create a new String with system's default charset (which is UTF-8 for Android)
            logContent(logLine.getLevel(), tag, new String(bytes, i, count));
        }
        logBottomBorder(logLine.getLevel(), tag);
    }

    private void logTopBorder(int logType, @Nullable String tag) {
        logChunk(logType, tag, TOP_BORDER);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private void logHeaderContent(int logType, @Nullable String tag, int methodCount) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (showThreadInfo) {
            logChunk(logType, tag, HORIZONTAL_LINE + " Thread: " + Thread.currentThread().getName());
            logDivider(logType, tag);
        }
        String level = "";

        int stackOffset = getStackOffset(trace) + methodOffset;

        //corresponding method count with the current stack may exceeds the stack trace. Trims the count
        if (methodCount + stackOffset > trace.length) {
            methodCount = trace.length - stackOffset - 1;
        }

        for (int i = methodCount; i > 0; i--) {
            int stackIndex = i + stackOffset;
            if (stackIndex >= trace.length) {
                continue;
            }
            StringBuilder builder = new StringBuilder();
            builder.append(HORIZONTAL_LINE)
                    .append(' ')
                    .append(level)
                    .append(getSimpleClassName(trace[stackIndex].getClassName()))
                    .append(".")
                    .append(trace[stackIndex].getMethodName())
                    .append(" ")
                    .append(" (")
                    .append(trace[stackIndex].getFileName())
                    .append(":")
                    .append(trace[stackIndex].getLineNumber())
                    .append(")");
            level += "   ";
            logChunk(logType, tag, builder.toString());
        }
    }

    private void logBottomBorder(int logType, @Nullable String tag) {
        logChunk(logType, tag, BOTTOM_BORDER);
    }

    private void logDivider(int logType, @Nullable String tag) {
        logChunk(logType, tag, MIDDLE_BORDER);
    }

    private void logContent(int logType, @Nullable String tag, @NonNull String chunk) {
        Objects.requireNonNull(chunk);

        String[] lines = chunk.split(Objects.requireNonNull(System.getProperty("line.separator")));
        for (String line : lines) {
            logChunk(logType, tag, HORIZONTAL_LINE + " " + line);
        }
    }

    private void logChunk(int level, @Nullable String tag, @NonNull String chunk) {
        Objects.requireNonNull(chunk);
        mPrintStrategy.print(LogLine.obtain(level, tag, chunk));
    }

    private String getSimpleClassName(@NonNull String name) {
        Objects.requireNonNull(name);

        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

    /**
     * Determines the starting index of the stack trace, after method calls made by this class.
     *
     * @param trace the stack trace
     * @return the stack offset
     */
    private int getStackOffset(@NonNull StackTraceElement[] trace) {
        Objects.requireNonNull(trace);

        for (int i = MIN_STACK_OFFSET; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (!name.equals(LoggerPrinter.class.getName()) && !name.equals(Logger.class.getName())) {
                return --i;
            }
        }
        return -1;
    }

    public static class Builder {
        int methodCount = 2;
        int methodOffset = 0;
        boolean showThreadInfo = true;
        @Nullable
        PrintStrategy mPrintStrategy;
        @Nullable
        String tag = "PRETTY_LOGGER";

        private Builder() {
        }

        @NonNull
        public Builder methodCount(int val) {
            methodCount = val;
            return this;
        }

        @NonNull
        public Builder methodOffset(int val) {
            methodOffset = val;
            return this;
        }

        @NonNull
        public Builder showThreadInfo(boolean val) {
            showThreadInfo = val;
            return this;
        }

        @NonNull
        public Builder logStrategy(@Nullable PrintStrategy val) {
            mPrintStrategy = val;
            return this;
        }

        @NonNull
        public Builder tag(@Nullable String tag) {
            this.tag = tag;
            return this;
        }

        @NonNull
        public PrettyFormatStrategy build() {
            if (mPrintStrategy == null) {
                mPrintStrategy = new LogcatPrintStrategy();
            }
            return new PrettyFormatStrategy(this);
        }
    }

}
