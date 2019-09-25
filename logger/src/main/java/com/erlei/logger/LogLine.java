package com.erlei.logger;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.erlei.logger.format.ArgsFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.erlei.logger.Logger.*;

public class LogLine implements Serializable, Cloneable, Parcelable {

    private static final Pools.SynchronizedPool<LogLine> sPool = new Pools.SynchronizedPool<>(100);

    @NonNull
    public static LogLine obtain(int level,
                                 @Nullable String tag,
                                 long time,
                                 @Nullable String module,
                                 @Nullable String format,
                                 @Nullable String json,
                                 @Nullable String xml,
                                 @Nullable Throwable throwable,
                                 @Nullable HashMap<String, String> extra,
                                 @Nullable ArgsFormatter argsFormatter,
                                 @Nullable Object... args) {
        LogLine instance = sPool.acquire();
        if (instance == null) {
            instance = new LogLine();
        }
        instance.level = level;
        instance.time = time;
        instance.extra = extra;
        instance.module = module;
        instance.throwable = throwable;
        instance.args = args;
        instance.json = json;
        instance.xml = xml;
        instance.format = format;
        if (argsFormatter != null) {
            instance.argsFormatter = argsFormatter;
        } else {
            instance.argsFormatter = LoggerFactory.sArgsFormatter;
        }
        instance.tag = tag;
        return instance;
    }

    @NonNull
    public static LogLine obtain(@Level int level, String tag, String format, Throwable throwable) {
        return obtain(level, tag, System.currentTimeMillis(), null, format,
                null, null, throwable, null, null);
    }

    @NonNull
    public static LogLine obtain(@NonNull ArgsFormatter formatter, Object... args) {
        return obtain(DEBUG, null, System.currentTimeMillis(),
                null, null, null, null,
                null, null, formatter, args);
    }


    @NonNull
    public static LogLine obtain(int level, String tag, String message) {
        return obtain(level, tag, System.currentTimeMillis(), null, message,
                null, null, null, null, null);
    }


    @NonNull
    public static LogLine obtain(int level, String tag) {
        return obtain(level, tag, System.currentTimeMillis(), null, null,
                null, null, null, null, null);
    }


    private int level;
    private String fulTag;
    private String message;
    private long time;
    private HashMap<String, String> extra;
    private String module;
    private String tag;
    private Throwable throwable;
    private Object[] args;
    private transient String format;
    private String xml;
    private String json;
    private transient ArgsFormatter argsFormatter;

    private LogLine() {
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @NonNull
    public Map<String, String> getExtra() {
        if (extra == null) extra = new HashMap<>();
        return extra;
    }

    @Nullable
    public Throwable getThrowable() {
        return throwable;
    }

    @Level
    public int getLevel() {
        return level;
    }

    public String getModule() {
        return module;
    }

    public String getTag() {
        return tag;
    }


    @NonNull
    public String getFulTag() {
        if (fulTag != null) return fulTag;
        StringBuilder sb = new StringBuilder(20);
        if (!isEmpty(module) && !isEmpty(tag) && !Objects.equals(tag, module)) {
            sb.append(module).append("-").append(tag);
        }
        if (!isEmpty(module) && isEmpty(tag)) {
            sb.append(module);
        }
        if (!isEmpty(tag) && isEmpty(module)) {
            sb.append(tag);
        }
        return fulTag = sb.toString();
    }

    private static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLevelString() {
        switch (level) {
            case VERBOSE:
                return "VERBOSE";
            case DEBUG:
                return "DEBUG";
            case INFO:
                return "INFO";
            case WARN:
                return "WARN";
            case ERROR:
                return "ERROR";
            case ASSERT:
                return "ASSERT";
            default:
                return "UNKNOWN";
        }
    }

    @NonNull
    public String getMessage() {
        if (message != null) return message;
        if (!TextUtils.isEmpty(format)) {
            //如果指定了format
            message = format();
        } else if (args != null && args.length > 0) {
            //自动格式化
            message = autoFormat();
        } else {
            if (xml != null) {
                message = formatXml();
            }
            if (json != null) {
                message = formatJson();
            }
            if (TextUtils.isEmpty(message)) {
                message = "Empty/NULL message content";
            }
        }
        return message;
    }


    public void setModuleName(String tag) {
        this.fulTag = null;
        module = tag;
    }

    public void setArgs(Object[] args) {
        this.message = null;
        this.args = args;
    }

    public void setFormat(String format) {
        this.message = null;
        this.format = format;
    }

    public void setThrowable(Throwable throwable) {
        this.message = null;
        this.throwable = throwable;
    }

    public void setXml(String xml) {
        this.message = null;
        this.xml = xml;
    }

    public void setJson(String json) {
        this.message = null;
        this.json = json;
    }


    @Override
    public String toString() {
        return "LogLine{" +
                "level=" + level +
                ", fulTag='" + fulTag + '\'' +
                ", message='" + message + '\'' +
                ", time=" + time +
                ", extra=" + extra +
                ", module='" + module + '\'' +
                ", throwable=" + throwable +
                ", args=" + Arrays.toString(args) +
                ", format='" + format + '\'' +
                ", xml='" + xml + '\'' +
                ", json='" + json + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        LogLine logLine = (LogLine) object;

        if (level != logLine.level) return false;
        if (time != logLine.time) return false;
        if (!Objects.equals(fulTag, logLine.fulTag)) return false;
        return Objects.equals(message, logLine.message);
    }

    @Override
    public int hashCode() {
        int result = level;
        result = 31 * result + (fulTag != null ? fulTag.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }

    @NonNull
    private String autoFormat() {
        return argsFormatter.format(args);
    }

    /**
     * It is used for json pretty print
     */
    public static final int JSON_INDENT = 2;

    @NonNull
    private String formatJson() {
        if (TextUtils.isEmpty(json)) {
            return "Empty/Null json content";
        }
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject.toString(JSON_INDENT);
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                return jsonArray.toString(JSON_INDENT);
            }
            return "Invalid Json";
        } catch (JSONException e) {
            return "Invalid Json";
        }
    }

    @NonNull
    private String formatXml() {
        if (TextUtils.isEmpty(xml)) {
            return "Empty/Null xml content";
        }
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString().replaceFirst(">", ">\n");
        } catch (TransformerException e) {
            return "Invalid xml";
        }
    }

    @NonNull
    private String format() {
        String msg;
        if (args == null || args.length == 0) {
            msg = format;
        } else {
            msg = String.format(format, args);
        }
        if (throwable != null && msg != null) {
            msg += (" : " + Log.getStackTraceString(throwable));
        }
        if (msg == null && throwable != null) {
            msg = Log.getStackTraceString(throwable);
        }
        if (TextUtils.isEmpty(msg)) {
            msg = "Empty/NULL log message";
        }
        assert msg != null;
        return msg;
    }


    @SuppressWarnings("unchecked")
    @Override
    protected LogLine clone() {
        HashMap<String, String> map = (extra == null || extra.isEmpty()) ? null : (HashMap<String, String>) extra.clone();
        LogLine obtain = obtain(level, tag, time, module, format, json, xml, throwable, map, argsFormatter, args);
        obtain.message = message;
        obtain.fulTag = fulTag;
        return obtain;
    }

    public void recycle() {
        level = 0;
        time = 0;
        if (extra != null) extra.clear();
        fulTag = null;
        message = null;
        module = null;
        throwable = null;
        args = null;
        format = null;
        xml = null;
        json = null;
        argsFormatter = null;
        sPool.release(this);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.level);
        dest.writeString(this.fulTag);
        dest.writeString(this.message);
        dest.writeLong(this.time);
        dest.writeSerializable(this.extra);
        dest.writeString(this.module);
        dest.writeString(this.tag);
        dest.writeSerializable(this.throwable);
        dest.writeString(this.xml);
        dest.writeString(this.json);
    }

    private LogLine(Parcel in) {
        this.level = in.readInt();
        this.fulTag = in.readString();
        this.message = in.readString();
        this.time = in.readLong();
        this.extra = (HashMap<String, String>) in.readSerializable();
        this.module = in.readString();
        this.tag = in.readString();
        this.throwable = (Throwable) in.readSerializable();
        this.xml = in.readString();
        this.json = in.readString();
    }

    public static final Creator<LogLine> CREATOR = new Creator<LogLine>() {
        @Override
        public LogLine createFromParcel(Parcel source) {
            return new LogLine(source);
        }

        @Override
        public LogLine[] newArray(int size) {
            return new LogLine[size];
        }
    };
}