package com.erlei.logger.format;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * format args as string
 * https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#syntax
 */
public class AlignArgsFormatter implements ArgsFormatter {


    protected String divider = "|";
    protected StringBuilder sb = new StringBuilder();
    protected HashMap<Class, String> map = new HashMap<>();
    protected int width = 8;
    private boolean alignLeft = false;

    public AlignArgsFormatter() {
    }

    public AlignArgsFormatter(int width, boolean alignLeft) {
        this("", width, alignLeft);
    }

    public AlignArgsFormatter(String divider, int width, boolean alignLeft) {
        this.divider = divider;
        this.width = width;
        this.alignLeft = alignLeft;
    }

    {
        map.put(Integer.class, "d");
        map.put(Float.class, "e");
        map.put(Double.class, "e");
        map.put(Character.class, "c");
        map.put(String.class, "s");
        map.put(BigDecimal.class, "e");
        map.put(Boolean.class, "b");
        map.put(Short.class, "c");
        map.put(Byte.class, "c");
        map.put(BigInteger.class, "e");
    }

    @Override
    @NonNull
    public String format(@Nullable Object... args) {
        if (args == null || args.length == 0) {
            return "Empty/NULL args message";
        }
        Object[] objects = getFormatArgs(args);
        return String.format(Locale.getDefault(), genFormatString(objects), objects);
//        return format(genFormatString(args), args);
    }

    @NonNull
    protected Object[] getFormatArgs(@NonNull Object... args) {
        Object[] objects = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            objects[i] = convert(args[i]);
        }
        return objects;
    }

    protected Object convert(Object object) {
        if (object == null) {
            return "null";
        }
        if (object.getClass().isArray()) {
            if (object instanceof boolean[]) {
                return Arrays.toString((boolean[]) object);
            }
            if (object instanceof byte[]) {
                return Arrays.toString((byte[]) object);
            }
            if (object instanceof char[]) {
                return Arrays.toString((char[]) object);
            }
            if (object instanceof short[]) {
                return Arrays.toString((short[]) object);
            }
            if (object instanceof int[]) {
                return Arrays.toString((int[]) object);
            }
            if (object instanceof long[]) {
                return Arrays.toString((long[]) object);
            }
            if (object instanceof float[]) {
                return Arrays.toString((float[]) object);
            }
            if (object instanceof double[]) {
                return Arrays.toString((double[]) object);
            }
            if (object instanceof Object[]) {
                return Arrays.deepToString((Object[]) object);
            }
        }
        if (object instanceof Throwable) {
            return Log.getStackTraceString((Throwable) object);
        }

        return object;
    }

    @NonNull
    public String genFormatString(@NonNull Object... args) {
        sb.delete(0, sb.length());
        for (int i = 0; i < args.length; i++) {
            sb.append("%");
            sb.append(i + 1).append("$");
            if (alignLeft) sb.append("-");
            if (width > 0) sb.append(width);
            sb.append(getFlag(args[i]));
            if (divider != null && i < args.length - 1) sb.append(divider);
        }
        return sb.toString();
    }

    protected String getFlag(@Nullable Object arg) {
        if (arg == null) return "s";
        String s = map.get(arg.getClass());
        if (s == null) return "s";
        return s;
    }
}
