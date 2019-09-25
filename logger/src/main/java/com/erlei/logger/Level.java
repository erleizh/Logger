package com.erlei.logger;

import android.support.annotation.IntDef;


@IntDef({Logger.VERBOSE, Logger.DEBUG, Logger.INFO, Logger.WARN, Logger.ERROR, Logger.ASSERT})
public @interface Level {
}
