package com.erlei.logger;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.test.filters.LargeTest;
import android.util.Log;
import com.dianping.logan.Logan;
import com.dianping.logan.LoganConfig;
import com.erlei.logger.adapter.AsyncLogAdapter;
import com.erlei.logger.adapter.DiskLogAdapter;
import com.erlei.logger.format.JsonFormatStrategy;
import com.erlei.logger.format.TextFormatStrategy;
import com.erlei.logger.printer.DiskPrintStrategy;
import com.erlei.logger.printer.PrintStrategy;
import com.erlei.logger.writer.FileLogWriter;
import com.erlei.logger.writer.LogWriter;
import com.erlei.logger.writer.MMAPLogWriter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static com.erlei.logger.FileUtil.deleteFolderFile;
import static com.erlei.logger.FileUtil.getFolderSize;
import static com.erlei.logger.Logger.ERROR;

public class PerformanceTest {
    private static final int LOG_LINE = 300000;

    @BeforeClass
    public static void setup() {
        deleteFolderFile(getLogFilesDir("").getParentFile().getAbsolutePath(), true);
    }


    @LargeTest
    @Test
    public void txtMMap() {
        String name = "txt_mmap";
        LogFileManager.init(getLogFilesDir(name));
        Logger logger = LoggerFactory.create(name);
        logger.clearLogAdapters();

        DiskPrintStrategy diskLogStrategy = DiskPrintStrategy.newBuilder()
                .writerFactory(file1 -> {
                    try {
                        return new MMAPLogWriter(file1, 400);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .build();
        MockPrintStrategy strategy = MockPrintStrategy.warp(diskLogStrategy);
        logger.addLogAdapter(new DiskLogAdapter(TextFormatStrategy.newBuilder()
                .logStrategy(strategy)
                .build()));
        new PerformanceTester(name, logger::log).start();
        Logger.replaceFileNullChar(LogFileManager.getLogFileDir(), "");
        Assert.assertEquals(strategy.getWrittenBytes(), getFolderSize(LogFileManager.getLogFileDir()));
        Assert.assertEquals(LOG_LINE, strategy.getLogCount());
    }

    private static File getLogFilesDir(String name) {
        return getTargetContext().getExternalFilesDir(name);
    }

    @LargeTest
    @Test
    public void txtFile() {
        String name = "txt_file";
        LogFileManager.init(getLogFilesDir(name));
        Logger logger = LoggerFactory.create(name);
        logger.clearLogAdapters();


        DiskPrintStrategy diskLogStrategy = DiskPrintStrategy.newBuilder()
                .writerFactory(FileLogWriter::new)
                .build();
        MockPrintStrategy strategy = MockPrintStrategy.warp(diskLogStrategy);
        logger.addLogAdapter(new DiskLogAdapter(TextFormatStrategy.newBuilder()
                .logStrategy(strategy)
                .build()));
        new PerformanceTester(name, logger::log).start();

        Assert.assertEquals(LOG_LINE, strategy.getLogCount());
        Assert.assertEquals(strategy.getWrittenBytes(), getFolderSize(LogFileManager.getLogFileDir()));
    }

    @LargeTest
    @Test
    public void jsonMMap() {
        String name = "json_mmap";
        LogFileManager.init(getLogFilesDir(name));
        Logger logger = LoggerFactory.create(name);
        logger.clearLogAdapters();


        DiskPrintStrategy diskLogStrategy = DiskPrintStrategy.newBuilder()
                .writerFactory(file1 -> {
                    try {
                        return new MMAPLogWriter(file1, 400);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .fileListener(new JsonFormatStrategy.JsonFileListener())
                .suffix(".json")
                .build();
        MockPrintStrategy strategy = MockPrintStrategy.warp(diskLogStrategy);
        logger.addLogAdapter(new DiskLogAdapter(JsonFormatStrategy.newBuilder()
                .logStrategy(strategy)
                .build()));
        new PerformanceTester(name, logger::log).start();
        Logger.replaceFileNullChar(LogFileManager.getLogFileDir(), "");
        Assert.assertEquals(strategy.getWrittenBytes(), getFolderSize(LogFileManager.getLogFileDir()), 2);
    }

    @LargeTest
    @Test
    public void jsonFile() {
        String name = "json_file";
        LogFileManager.init(getLogFilesDir(name));
        Logger logger = LoggerFactory.create(name);
        logger.clearLogAdapters();


        DiskPrintStrategy diskLogStrategy = DiskPrintStrategy.newBuilder()
                .writerFactory(FileLogWriter::new)
                .suffix(".json")
                .fileListener(new JsonFormatStrategy.JsonFileListener())
                .build();
        MockPrintStrategy strategy = MockPrintStrategy.warp(diskLogStrategy);
        logger.addLogAdapter(new DiskLogAdapter(JsonFormatStrategy.newBuilder()
                .logStrategy(strategy)
                .build()));
        new PerformanceTester(name, logger::log).start();
        Assert.assertEquals(strategy.getWrittenBytes(), getFolderSize(LogFileManager.getLogFileDir()));

    }


    @LargeTest
    @Test
    public void asyncJsonMMap() {
        String name = "async_json_mmap";
        LogFileManager.init(getLogFilesDir(name));
        Logger logger = LoggerFactory.create(name);
        logger.clearLogAdapters();


        DiskPrintStrategy diskLogStrategy = DiskPrintStrategy.newBuilder()
                .writerFactory(file1 -> {
                    try {
                        return new MMAPLogWriter(file1, 400);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .fileListener(new JsonFormatStrategy.JsonFileListener())
                .suffix(".json")
                .build();
        MockPrintStrategy strategy = MockPrintStrategy.warp(diskLogStrategy);
        logger.addLogAdapter(new AsyncLogAdapter(new DiskLogAdapter(JsonFormatStrategy.newBuilder()
                .logStrategy(strategy)
                .build())));
        new PerformanceTester(name, logger::log).start();
        SystemClock.sleep(1000 * 10);
        Logger.replaceFileNullChar(LogFileManager.getLogFileDir(), "");
        Assert.assertEquals(strategy.getWrittenBytes(), getFolderSize(LogFileManager.getLogFileDir()));
    }

    @LargeTest
    @Test
    public void asyncJsonFile() {
        String name = "async_json_file";
        LogFileManager.init(getLogFilesDir(name));
        Logger logger = LoggerFactory.create(name);
        logger.clearLogAdapters();


        DiskPrintStrategy diskLogStrategy = DiskPrintStrategy.newBuilder()
                .writerFactory(FileLogWriter::new)
                .suffix(".json")
                .fileListener(new JsonFormatStrategy.JsonFileListener())
                .build();
        MockPrintStrategy strategy = MockPrintStrategy.warp(diskLogStrategy);
        logger.addLogAdapter(new AsyncLogAdapter(new DiskLogAdapter(JsonFormatStrategy.newBuilder()
                .logStrategy(strategy)
                .build())));
        new PerformanceTester(name, logger::log).start();
        SystemClock.sleep(1000 * 10);
        Assert.assertEquals(strategy.getWrittenBytes(), getFolderSize(LogFileManager.getLogFileDir()));
    }


    @LargeTest
    @Test
    public void testLogan() {
        String name = "logan";
        LoganConfig config = new LoganConfig.Builder()
                .setCachePath(getTargetContext().getFilesDir().getAbsolutePath())
                .setPath(getLogFilesDir(name).getAbsolutePath()
                        + File.separator + name)
                .setEncryptKey16("0123456789012345".getBytes())
                .setEncryptIV16("0123456789012345".getBytes())
                .build();
        Logan.init(config);

        new PerformanceTester(name, logLine -> Logan.w(logLine.getMessage(), logLine.getLevel())).start();

    }


    /**
     * 记录总共写了多少 bytes ，最后跟文件大小进行对比
     */
    public static class MockPrintStrategy implements PrintStrategy {
        private long mTotalBytes;
        private int mLogCount;

        static MockPrintStrategy warp(PrintStrategy strategy) {
            return new MockPrintStrategy(strategy);
        }

        private final PrintStrategy mStrategy;

        public MockPrintStrategy(PrintStrategy strategy) {
            mStrategy = strategy;
        }

        @Override
        public void print(@NonNull LogLine line) {
            mLogCount++;
            mTotalBytes += line.getMessage().getBytes().length;
            mStrategy.print(line);
        }

        public int getLogCount() {
            return mLogCount;
        }

        public long getWrittenBytes() {
            return mTotalBytes;
        }
    }


    public class PerformanceTester {

        private final String mName;
        private final Tester mTester;
        private long mStartTime;


        public PerformanceTester(String name, Tester tester) {
            mName = name;
            mTester = tester;
        }

        void test() {
            for (int i = 0; i < LOG_LINE; i++) {
                LogLine line = LogLine.obtain(ERROR, "sequence string", System.currentTimeMillis() + "\t\t" + i);
                mTester.test(line);
            }
        }

        public void start() {
            before();
            test();
            after();
        }

        protected void after() {
            long l = System.currentTimeMillis() - mStartTime;
            //当前分配的总内存
            float totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0 / (1024 * 1024));
            println("cost %dms \t%d/s \tmemory %.1fM \t%s", l, LOG_LINE / l, totalMemory, mName);
            println("-----------------------------------------------------");

        }

        protected void before() {
            mStartTime = System.currentTimeMillis();
        }
    }

    public interface Tester {

        void test(LogLine logLine);
    }

    public void println(String msg, Object... args) {
        String format = String.format(msg, args);
        Log.d("PerformanceTester", format);
    }


    private class MockFileLogWriter extends LogWriter {
        private int writeCount;
        private final LogWriter mWriter;

        public MockFileLogWriter(File file) {
            mWriter = new FileLogWriter(file);
        }

        @Override
        public long getWrittenBytes() {
            return mWriter.getWrittenBytes();
        }

        @Override
        public void write(byte[] buffer, int off, int len) throws IOException {
            writeCount++;
            mWriter.write(buffer, off, len);
        }

        public int getWriteCount() {
            return writeCount;
        }

        @Override
        public void close() throws IOException {
            mWriter.close();
        }

        @Override
        public void flush() throws IOException {
            mWriter.flush();
        }
    }
}
