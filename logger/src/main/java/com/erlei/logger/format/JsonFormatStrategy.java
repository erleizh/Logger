package com.erlei.logger.format;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.erlei.logger.LogLine;
import com.erlei.logger.printer.DiskPrintStrategy;
import com.erlei.logger.printer.PrintStrategy;
import com.erlei.logger.writer.LogWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Created by lll on 2019/8/21
 * Email : erleizh@gmail.com
 * Describe : write log to json file
 */
public class JsonFormatStrategy implements FormatStrategy {

    private final Builder mBuilder;

    private JsonFormatStrategy(Builder builder) {
        mBuilder = builder;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void log(@NonNull LogLine logLine) {
        logLine.setModuleName(this.mBuilder.tag);
        if (mBuilder.date != null) logLine.setTime(mBuilder.date.getTime());
        logLine.setMessage(mBuilder.mSerializer.toJson(logLine));
        mBuilder.mPrintStrategy.print(logLine);
    }


    public static final class Builder {

        Date date;
        PrintStrategy mPrintStrategy;
        String tag;
        private ObjectSerializer mSerializer;

        private Builder() {
        }

        @NonNull
        public Builder date(@Nullable Date val) {
            date = val;
            return this;
        }

        @NonNull
        public Builder logStrategy(@Nullable PrintStrategy val) {
            mPrintStrategy = val;
            return this;
        }

        @NonNull
        public Builder serializer(@Nullable ObjectSerializer serializer) {
            mSerializer = serializer;
            return this;
        }

        @NonNull
        public Builder tag(@Nullable String tag) {
            this.tag = tag;
            return this;
        }

        @NonNull
        public JsonFormatStrategy build() {
            if (date == null) {
                date = new Date();
            }
            if (mPrintStrategy == null) {
                mPrintStrategy = DiskPrintStrategy.newBuilder()
                        .fileListener(new JsonFileListener())
                        .suffix(".json").build();
            }
            if (mSerializer == null) {
                mSerializer = new DefaultObjectSerializer();
            }
            return new JsonFormatStrategy(this);
        }
    }

    public interface ObjectSerializer {
        String toJson(LogLine logLine);
    }


    public static class DefaultObjectSerializer implements ObjectSerializer {


        @Override
        public String toJson(LogLine logLine) {
            JSONStringer stringer = new JSONStringer();
            try {
                stringer.object()
                        .key("level").value(logLine.getLevel())
                        .key("time").value(logLine.getTime())
                        .key("tag").value(logLine.getFulTag());

                Map<String, String> extra = logLine.getExtra();
                if (!extra.isEmpty()) {
                    stringer.key("extra").value(new JSONObject(extra));
                }
                Throwable throwable = logLine.getThrowable();
                if (throwable != null) {
                    stringer.key("throwable").value(Log.getStackTraceString(throwable));
                    //json文件，throwable 不用出现在 message 里面
                    logLine.setThrowable(null);
                }
                stringer.key("msg").value(logLine.getMessage());
                return stringer.endObject().toString().concat(",");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }
    }


    /**
     * {
     * "header":{}
     * "logs":[]
     * }
     */
    public static class JsonFileListener extends DiskPrintStrategy.FileHeader {

        public JsonFileListener(@Nullable Map<String, String> header) {
            super(header);
        }

        public JsonFileListener() {
            super(null);
        }

        @Override
        public void afterCreate(File file, LogWriter writer) {
            try {
                writer.write("{");
                if (mHeader != null && !mHeader.isEmpty()) {
                    writer.write("\"header\":");
                    writer.write(new JSONObject(mHeader).toString());
                    writer.write(",");
                }
                writer.write("\"logs\":");
                writer.write("[");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void beforeClose(File file, LogWriter writer) {

        }

        @Override
        public void afterClose(File file) {
            try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
                accessFile.seek(file.length() - ",".getBytes().length);
                accessFile.write("]".getBytes(StandardCharsets.UTF_8));
                accessFile.write("}".getBytes(StandardCharsets.UTF_8));
            } catch (FileNotFoundException ignored) {
            } catch (IOException ignored) {
            }
        }

        @Override
        public void beforeCreate(File file) {

        }
    }
}