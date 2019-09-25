package com.erlei.logger.writer;

import android.Manifest;
import android.os.SystemClock;
import android.support.test.filters.LargeTest;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import com.erlei.logger.LogLine;
import com.erlei.logger.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static com.erlei.logger.FileUtil.getNewFile;

@RunWith(AndroidJUnit4.class)
public class LogWriterTest {


    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE);


    @LargeTest
    @Test
    public void testMMAPWrite() throws IOException {
        File file = getNewFile(getTargetContext(), "performance_mmap.log");
        new PerformanceTester(file, new MMAPLogWriter(file, 4)).start();
    }

    @LargeTest
    @Test
    public void testFileWrite() {
        File file = getNewFile(getTargetContext(), "performance_file.log");
        new PerformanceTester(file, new FileLogWriter(file)).start();
    }


    @LargeTest
    @Test
    public void testAsyncMMAPWrite() throws IOException {
        File file = getNewFile(getTargetContext(), "performance_async_mmap.log");
        new PerformanceTester(file, new AsyncLogWriter(new MMAPLogWriter(file, 4))).start();
    }

    @LargeTest
    @Test
    public void testAsyncFileWrite() throws IOException {
        File file = getNewFile(getTargetContext(), "performance_async_file.log");
        new PerformanceTester(file, new AsyncLogWriter(new FileLogWriter(file))).start();
    }


    public class PerformanceTester extends Tester {

        private long mStartTime;
        private long LOG_LINE = 300000;
        long totalBytes = 0;
        byte[] bytes;


        public PerformanceTester(File file, LogWriter writer) {
            super(file, writer);
        }

        @Override
        void write() throws IOException {
            for (int i = 0; i < LOG_LINE; i++) {
                LogLine logLine = LogLine.obtain(Logger.ASSERT, "sequence string", System.currentTimeMillis() + "\n");
                mWriter.write(logLine);
                byte[] bytes = logLine.getMessage().getBytes();
                if (i == LOG_LINE - 1) this.bytes = bytes;
                totalBytes += bytes.length;
            }
        }

        @Override
        protected void after() {
            if (mWriter instanceof AsyncLogWriter) {
                SystemClock.sleep(1000 * 5);
            }
            long l = System.currentTimeMillis() - mStartTime;
            println("cost %-10d %-10.3f %s", l, (float) LOG_LINE / l, mWriter.toString());
            FileAssert.assertFileLength(mFile, totalBytes);
            FileAssert.assertEquals(mFile, totalBytes - bytes.length, bytes);
        }

        @Override
        protected void before() {
            mStartTime = System.currentTimeMillis();
        }
    }


    public void println(String msg, Object... args) {
        Log.d("PerformanceTester", String.format(msg, args));
    }
}
