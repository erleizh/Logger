package com.erlei.logger.format;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.erlei.logger.LogLine;
import com.erlei.logger.printer.DiskPrintStrategy;
import com.erlei.logger.printer.PrintStrategy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


/**
 * Created by lll on 2019/8/21
 * Email : erleizh@gmail.com
 * Describe : write log to txt file
 */
public class TextFormatStrategy implements FormatStrategy {

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String SEPARATOR = ",";
    private final Builder builder;
    private StringBuffer sb = new StringBuffer(32);

    public TextFormatStrategy(Builder builder) {
        this.builder = builder;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    @Override
    public void log(@NonNull LogLine logLine) {
        Objects.requireNonNull(logLine);
        logLine.setModuleName(builder.tag);
        if (builder.date != null) logLine.setTime(builder.date.getTime());
        sb.delete(0, sb.length());
        //date
        sb.append(builder.dateFormat.format(logLine.getTime()));
        sb.append(builder.separator);
        //level
        sb.append(logLine.getLevelString());

        // tag
        sb.append(builder.separator);
        sb.append(logLine.getFulTag());

        sb.append(builder.separator);
        sb.append(logLine.getMessage());

        sb.append(NEW_LINE);
        logLine.setMessage(sb.toString());
        builder.mPrintStrategy.print(logLine);
    }


    public static final class Builder {

        Date date;
        SimpleDateFormat dateFormat;
        PrintStrategy mPrintStrategy;
        String tag = "";
        private String separator;

        public Builder() {
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
        public Builder separator(@Nullable String val) {
            separator = separator;
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
        public TextFormatStrategy build() {
            if (date == null) {
                date = new Date();
            }
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.getDefault());
            }
            if (separator == null) {
                separator = SEPARATOR;
            }
            if (mPrintStrategy == null) {
                mPrintStrategy = DiskPrintStrategy.newBuilder().suffix(".log").build();
            }
            return new TextFormatStrategy(this);
        }
    }
}
