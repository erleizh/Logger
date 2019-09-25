package com.erlei.logger.writer;

import android.util.Log;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;

public abstract class Tester {

    protected LogWriter mWriter = null;
    protected File mFile;

    public Tester(File file, LogWriter writer) {
        mFile = file;
        this.mWriter = writer;
    }

    public Tester(String path) {
        this(new File(path), 4);
    }

    public LogWriter getWriter() {
        return mWriter;
    }

    public Tester(File file, int pageCount) {
        mFile = file;
        try {
            this.mWriter = new MMAPLogWriter(file, pageCount);
        } catch (IOException e) {
            Assert.fail(Log.getStackTraceString(e));
        }
    }


    public Tester(File file) {
        this(file, 1);
    }

    public void start() {
        before();
        try {
            write();
        } catch (IOException e) {
            Assert.fail(Log.getStackTraceString(e));
        } finally {
            if (mWriter != null) {
                try {
                    mWriter.close();
                } catch (IOException e) {
                    Assert.fail(Log.getStackTraceString(e));
                }
            }
            after();
        }
    }

    protected void after() {

    }


    protected void before() {

    }

    abstract void write() throws IOException;


}
