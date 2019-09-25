package com.erlei.logger.printer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.erlei.logger.LogFileManager;
import com.erlei.logger.LogLine;
import com.erlei.logger.LogWriterFactory;
import com.erlei.logger.writer.FileLogWriter;
import com.erlei.logger.writer.LogWriter;
import com.erlei.logger.writer.MMAPLogWriter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Abstract class that takes care of background threading the file log operation on Android.
 * implementing classes are free to directly perform I/O operations there.
 */
public class DiskPrintStrategy implements PrintStrategy {

    private final Builder mBuilder;
    private File mFile = null;
    private LogWriter mWriter;

    private DiskPrintStrategy(Builder builder) {
        mBuilder = builder;
    }

    public static Builder newBuilder() {
        return new Builder()
                .maxLength(LogFileManager.getFileMaxLength());
    }

    @Override
    public void print(@NonNull LogLine logLine) {
        LogWriter writer = getWriter();
        try {
            writer.write(logLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logLine.recycle();
    }

    @NonNull
    protected synchronized LogWriter getWriter() {
        if (mWriter == null || mWriter.getWrittenBytes() >= mBuilder.maxLength) {

            if (mWriter != null) {

                notifyFileListener(fileListener -> fileListener.beforeClose(mFile, mWriter));
                try {
                    mWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                notifyFileListener(fileListener -> fileListener.afterClose(mFile));
            }
            File file = mBuilder.fileProvider.getLogFile(mBuilder.maxLength, mBuilder.suffix);
            if (!Objects.equals(file, mFile)) {
                notifyFileListener(fileListener -> fileListener.beforeCreate(file));
            }
            LogWriter writer = mBuilder.factory.create(file);
            if (!Objects.equals(file, mFile)) {
                notifyFileListener(fileListener -> fileListener.afterCreate(file, writer));
            }
            mFile = file;
            return mWriter = writer;
        }
        return mWriter;
    }

    protected void notifyFileListener(Customer customer) {
        for (FileListener fileListener : mBuilder.mFileListeners) {
            customer.run(fileListener);
        }
    }

    private interface Customer {

        void run(FileListener fileListener);
    }

    public static class Builder {
        private String suffix = ".log";
        private LogWriterFactory factory;
        private List<FileListener> mFileListeners = new ArrayList<>();
        private Map<String, String> header;
        private long maxLength = 1024 * 500;
        private LogFileProvider fileProvider;

        private Builder() {
        }

        public Builder suffix(@NonNull String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder writerFactory(@NonNull LogWriterFactory writerFactory) {
            this.factory = writerFactory;
            return this;
        }

        public Builder fileListener(FileListener fileEvent) {
            mFileListeners.add(fileEvent);
            return this;
        }

        public Builder maxLength(long maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder fileHeader(@Nullable Map<String, String> header) {
            this.header = header;
            return this;
        }

        public Builder fileProvider(@Nullable LogFileProvider fileProvider) {
            this.fileProvider = fileProvider;
            return this;
        }


        public DiskPrintStrategy build() {
            if (factory == null) {
                factory = file -> {
                    try {
                        return new MMAPLogWriter(file, 400);
                    } catch (IOException e) {
                        return new FileLogWriter(file);
                    }
                };
            }

            if (header != null && !header.isEmpty()) {
                mFileListeners.add(0, new FileHeader(header));
            }

            if (fileProvider == null) {
                fileProvider = LogFileManager.getDefaultLogFileProvider();
            }
            return new DiskPrintStrategy(this);
        }
    }

    public interface FileListener {

        /**
         * 不能使用除了LogWriter之外的其他Writer进行写入
         */
        void afterCreate(File file, LogWriter writer);

        void beforeClose(File file, LogWriter writer);

        void afterClose(File file);

        void beforeCreate(File file);
    }

    /**
     * 提供全局文件头写入
     */
    public static class FileHeader implements DiskPrintStrategy.FileListener {

        protected final Map<String, String> mHeader;

        public FileHeader(@Nullable Map<String, String> header) {
            mHeader = header;
        }

        @Override
        public void afterCreate(File file, LogWriter writer) {
            if (mHeader != null && !mHeader.isEmpty()) {
                try {
                    writer.write("\n\n");
                    writer.write(mHeader.toString());
                    writer.write("\n\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void beforeClose(File file, LogWriter writer) {

        }

        @Override
        public void afterClose(File file) {

        }

        @Override
        public void beforeCreate(File file) {

        }
    }


    public interface LogFileProvider {

        /**
         * @param maxLength 单个文件最大长度
         * @param suffix    文件后缀
         * @return 获取一个可用于写入日志的文件
         */
        File getLogFile(long maxLength, String suffix);

    }

    public static class DefaultLogFileProvider implements LogFileProvider {

        private final File dir;

        public DefaultLogFileProvider(@NonNull File dir) {
            this.dir = dir;
        }

        private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault());

        @Override
        public File getLogFile(long maxLength, String suffix) {
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            String[] logs = dir.list((dir1, name) -> name.startsWith("log") && name.endsWith(suffix));
            int fileCount = 0;
            if (logs != null) fileCount = logs.length;
            if (fileCount > 0) fileCount--;
            File file = new File(dir, String.format("%s_%s_%s%s", "log", fileCount, mDateFormat.format(new Date()), suffix));
            if (file.length() >= maxLength) {
                fileCount++;
                file = new File(dir, String.format("%s_%s_%s%s", "log", fileCount, mDateFormat.format(new Date()), suffix));
            }
            return file;
        }
    }
}
