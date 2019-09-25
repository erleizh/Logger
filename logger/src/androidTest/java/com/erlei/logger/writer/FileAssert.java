package com.erlei.logger.writer;

import android.util.Log;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileAssert {

    /**
     * 断言文件大小是否与给定的一致
     *
     * @param file  file
     * @param bytes bytes
     */
    public static void assertFileLength(File file, long bytes) {
        Assert.assertEquals(file.length(), bytes);
    }

    /**
     * 断言文件的指定位置是否与给定的 bytes 一致
     *
     * @param file   file
     * @param offset offset
     * @param bytes  bytes
     */
    protected static void assertEquals(File file, long offset, byte[] bytes) {
        if (file == null) Assert.fail("file == null");
        InputStream reader = null;
        try {
            reader = new FileInputStream(file);
            //noinspection ResultOfMethodCallIgnored
            reader.skip(offset);
            byte[] actual = new byte[bytes.length];
            //noinspection ResultOfMethodCallIgnored
            reader.read(actual);
            Assert.assertArrayEquals(bytes, actual);
        } catch (IOException e) {
            Assert.fail(Log.getStackTraceString(e));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
