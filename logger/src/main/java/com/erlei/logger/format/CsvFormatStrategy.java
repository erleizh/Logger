package com.erlei.logger.format;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.erlei.logger.LogLine;
import com.erlei.logger.printer.DiskPrintStrategy;
import com.erlei.logger.printer.PrintStrategy;
import com.erlei.logger.writer.LogWriter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


/**
 * CSV formatted file logging for Android.
 * Writes to CSV the following data:
 * epoch timestamp, ISO8601 timestamp (human-readable), log Level, tag, log message.
 */
public class CsvFormatStrategy implements FormatStrategy {

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String NEW_LINE_REPLACEMENT = " <br> ";
    private static final String SEPARATOR = ",";

    @NonNull
    private final Date date;
    @NonNull
    private final SimpleDateFormat dateFormat;
    @NonNull
    private final PrintStrategy mPrintStrategy;
    @Nullable
    private final String globalTag;

    private StringBuilder builder = new StringBuilder(32);

    private CsvFormatStrategy(@NonNull Builder builder) {
        date = builder.date;
        dateFormat = builder.dateFormat;
        mPrintStrategy = builder.mPrintStrategy;
        globalTag = builder.tag;
    }

    @NonNull
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void log(@NonNull LogLine logLine) {
        Objects.requireNonNull(logLine);

        logLine.setModuleName(this.globalTag);
        date.setTime(System.currentTimeMillis());
        builder.delete(0, builder.length());
        // machine-readable date/time
        builder.append(date.getTime());

        // human-readable date/time
        builder.append(SEPARATOR);
        builder.append(dateFormat.format(date));

        // Level
        builder.append(SEPARATOR);
        builder.append(logLine.getLevelString());

        // tag
        builder.append(SEPARATOR);
        builder.append(logLine.getFulTag());

        // message
        String message = logLine.getMessage();
        if (message.contains(NEW_LINE)) {
            // a new line would break the CSV format, so we replace it here
            message = message.replaceAll(NEW_LINE, NEW_LINE_REPLACEMENT);
        }
        builder.append(SEPARATOR);
        builder.append(message);

        // new line
        builder.append(NEW_LINE);
        logLine.setMessage(builder.toString());
        mPrintStrategy.print(logLine);
    }


    public static final class Builder {

        Date date;
        SimpleDateFormat dateFormat;
        PrintStrategy mPrintStrategy;
        String tag = "PRETTY_LOGGER";

        private Builder() {
        }

        @NonNull
        public Builder date(@Nullable Date val) {
            date = val;
            return this;
        }

        @NonNull
        public Builder dateFormat(@Nullable SimpleDateFormat val) {
            dateFormat = val;
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
        public CsvFormatStrategy build() {
            if (date == null) {
                date = new Date();
            }
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.getDefault());
            }
            if (mPrintStrategy == null) {
                mPrintStrategy = DiskPrintStrategy.newBuilder()
                        // csv 就不要写文件头了， 估计会破坏csv 的文件格式，反正没试过
                        // TODO: 2019/9/13 支持csv文件添加头信息，
                        .fileListener(new DiskPrintStrategy.FileListener() {
                            @Override
                            public void afterCreate(File file, LogWriter writer) {

                            }

                            @Override
                            public void beforeClose(File file, LogWriter writer) {

                            }

                            @Override
                            public void afterClose(File file) {

                            }

                            @Override
                            public void beforeCreate(File file) {

                            }
                        }).suffix(".cvs").build();
            }
            return new CsvFormatStrategy(this);
        }
    }
}
