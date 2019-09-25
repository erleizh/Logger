package com.erlei.logger.format;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface ArgsFormatter {
    @NonNull
    String format(@Nullable Object... args);
}
