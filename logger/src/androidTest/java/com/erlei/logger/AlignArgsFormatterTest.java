package com.erlei.logger;

import android.util.Log;
import com.erlei.logger.format.AlignArgsFormatter;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class AlignArgsFormatterTest {


    @Test
    public void format() {
        AlignArgsFormatter formatter = new AlignArgsFormatter("|", 8, false);
        long l = System.currentTimeMillis();
        String result = formatter.format(Integer.MIN_VALUE, 0.1, "As", true, Long.MAX_VALUE, Integer.MAX_VALUE, Math.E, Math.PI, new BigDecimal(Math.PI), new Size(), null);
        Log.d("AlignArgsFormatterTest", "cost :" + (System.currentTimeMillis() - l));

        Assert.assertEquals("-2147483648|1.000000e-01|      As|    true|9223372036854775807|2147483647|2.718282e+00|3.141593e+00|3.141593e+00|Size{width=0, height=0}|    null", result);
    }

    @Test
    public void formatString() {
        AlignArgsFormatter formatter = new AlignArgsFormatter("|", 20, false);
        String format = formatter.genFormatString(0.1, "As", true, Long.MAX_VALUE, Integer.MAX_VALUE);
        Assert.assertEquals("%1$20e|%2$20s|%3$20b|%4$20s|%5$20d", format);
    }

    public class Size {
        private int width = 0;
        private int height = 0;

        @Override
        public String toString() {
            return "Size{" +
                    "width=" + width +
                    ", height=" + height +
                    '}';
        }
    }
}
