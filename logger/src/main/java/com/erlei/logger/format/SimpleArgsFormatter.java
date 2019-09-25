package com.erlei.logger.format;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SimpleArgsFormatter implements ArgsFormatter {

    private static final SimpleArgsFormatter sFormatter = new SimpleArgsFormatter();
    private String separator;
    private static final Map<Class, ObjectConverter> sConverters = new HashMap<>();

    static {
        sConverters.put(int[].class, (ObjectConverter<int[]>) Arrays::toString);
        sConverters.put(float[].class, (ObjectConverter<float[]>) Arrays::toString);
        sConverters.put(double[].class, (ObjectConverter<double[]>) Arrays::toString);
        sConverters.put(byte[].class, (ObjectConverter<byte[]>) Arrays::toString);
        sConverters.put(short[].class, (ObjectConverter<short[]>) Arrays::toString);
        sConverters.put(long[].class, (ObjectConverter<long[]>) Arrays::toString);
        sConverters.put(char[].class, (ObjectConverter<char[]>) Arrays::toString);
        sConverters.put(boolean[].class, (ObjectConverter<boolean[]>) Arrays::toString);
    }


    public SimpleArgsFormatter() {

    }

    public SimpleArgsFormatter(@NonNull String separator) {
        this.separator = Objects.requireNonNull(separator);
    }


    public static SimpleArgsFormatter getInstance() {
        return sFormatter;
    }

    private final StringBuilder sb = new StringBuilder(50);


    @NonNull
    @Override
    public String format(@Nullable Object... args) {
        if (args == null || args.length <= 0) return "Empty/NULL args message";
        sb.delete(0, sb.length());
        for (int i = 0; i < args.length; i++) {
            sb.append(convert(args[i]));
            if (separator != null && i < args.length - 1) sb.append(separator);
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    protected String convert(@Nullable Object arg) {
        if (arg == null) return null;
        if (arg.getClass().isArray()) {
            ObjectConverter converter = sConverters.get(arg.getClass());
            if (converter != null) {
                return converter.convert(arg);
            }
            return Arrays.deepToString((Object[]) arg);
        }
        if (arg.getClass().isAssignableFrom(Throwable.class)) {
            return Log.getStackTraceString((Throwable) arg);
        }
        return Objects.toString(arg);
    }

    public interface ObjectConverter<T> {

        String convert(T obj);
    }
}
