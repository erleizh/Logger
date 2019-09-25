package com.erlei.logger.writer;

import android.util.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static android.support.test.InstrumentationRegistry.getTargetContext;

public class MMAPLogWriterTest {

    @Before
    public void setup() {
        //noinspection ResultOfMethodCallIgnored
        getFile().delete();
    }

    private File getFile() {
        return new File(getTargetContext().getExternalFilesDir("log"), "mmap_writer_test.txt");
    }

    @Test
    public void write() throws IOException {
        MMAPLogWriter writer = new MMAPLogWriter(getFile(), 4);
        writer.write(System.currentTimeMillis() + "");

        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            byte[] bytes = new byte[i];
            random.nextBytes(bytes);
            writer.write(bytes, 0, bytes.length);
        }

        for (int i = 0; i < 1000; i++) {
            byte[] bytes = new byte[i];
            random.nextBytes(bytes);
            writer.write(new String(bytes));
        }
        writer.close();
    }

    @Test
    public void testFileLock() throws IOException {
        new MMAPLogWriter(getFile(), 4);
        try {
            new MMAPLogWriter(getFile(), 4);
            Assert.fail("同一个文件进行两次映射不报错");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Bad file descriptor"));
        }
    }

    @Test
    public void testPerformance() throws IOException {
        byte[] bytes = new byte[1024 * 1024 * 100];
        new Random().nextBytes(bytes);
        MMAPLogWriter writer = new MMAPLogWriter(getFile(), 1024 * 100);
        long millis = System.currentTimeMillis();
        writer.write(bytes, 0, bytes.length);
        println("mmap write cost %s", (System.currentTimeMillis() - millis));

        FileLogWriter logWriter = new FileLogWriter(getFile());
        millis = System.currentTimeMillis();
        logWriter.write(bytes, 0, bytes.length);
        println("file write cost %s", (System.currentTimeMillis() - millis));


        byte[] bytes1 = new byte[bytes.length];
        millis = System.currentTimeMillis();
        System.arraycopy(bytes, 0, bytes1, 0, bytes.length);
        println("mem write cost %s", (System.currentTimeMillis() - millis));
    }


    @Test
    public void getWrittenBytes() {
    }

    @Test
    public void testUnlockFile() throws IOException {
        write();
        write();
    }

    public void println(String msg, Object... args) {
        String format = String.format(msg, args);
        Log.d("MMAPLogWriterTest", format);
    }

}