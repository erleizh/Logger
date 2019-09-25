package com.erlei.logger;

import android.Manifest;
import android.support.test.filters.LargeTest;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import com.erlei.logger.adapter.DiskLogAdapter;
import com.erlei.logger.format.TextFormatStrategy;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;

@RunWith(AndroidJUnit4.class)
public class TextFormatStrategyTest {

    private static Logger logger;

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @BeforeClass
    public static void before() {
        LogFileManager.init(getTargetContext());
        TextFormatStrategy strategy = new TextFormatStrategy.Builder().build();
        LoggerFactory.addLogAdapter(new DiskLogAdapter(strategy));
        logger = LoggerFactory.create("Logger");
    }

    @LargeTest
    @Test
    public void testBatch() {
        for (int i = 0; i < 10000; i++) {
            logger.args(System.currentTimeMillis(), i);
        }
    }

}
