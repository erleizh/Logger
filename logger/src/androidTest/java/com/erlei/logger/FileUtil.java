package com.erlei.logger;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;

public class FileUtil {

    @NonNull
    public static File getNewFile(Context context, String path) {
        File file = new File(context.getExternalCacheDir(), path);
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parentFile.mkdirs();
        }
        deleteFolderFile(file.getAbsolutePath(), true);
        return file;
    }

    /**
     * 删除指定目录下文件及目录
     *
     * @param deleteThisPath
     * @param filePath
     * @return
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 处理目录
                    File[] files = file.listFiles();
                    for (File file1 : files) {
                        deleteFolderFile(file1.getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {// 如果是文件，删除
                        file.delete();
                    } else {// 目录
                        if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                            file.delete();
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


    /**
     * 获取文件夹大小(递归)
     *
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(java.io.File file) {
        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (File file1 : fileList) {
                if (file1.isDirectory()) {
                    size = size + getFolderSize(file1);
                } else {
                    size = size + file1.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }


}
