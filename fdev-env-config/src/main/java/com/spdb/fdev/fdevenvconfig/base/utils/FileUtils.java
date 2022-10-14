package com.spdb.fdev.fdevenvconfig.base.utils;

import com.spdb.fdev.fdevenvconfig.base.CommonUtils;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    public static File createFile(String path) throws IOException {
        return createFile(path, false);
    }

    public static File createFile(String path, Boolean isDir) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            createFile(file.getParent(), true);
            if (isDir) {
                file.mkdir();
            } else {
                file.createNewFile();
            }
        }
        return file;
    }

    public static File createFile(File file) throws IOException {
        return createFile(file.getCanonicalPath(), false);
    }

    public static File createFile(File file, Boolean isDir) throws IOException {
        return createFile(file.getCanonicalPath(), isDir);
    }

    public static boolean deleteFile(File file) throws IOException {
        Boolean flag = true;
        if (file.isDirectory()) {
            File[] subfiles = file.listFiles();
            if (!CommonUtils.isNullOrEmpty(subfiles)) {
                for (File subfile : subfiles) {
                    flag = flag && deleteFile(subfile);
                }
            }
            if (CommonUtils.isNullOrEmpty(file.listFiles())) {
                flag = flag && file.delete();
                return flag;
            } else {
                return false;
            }
        } else {
            if (!file.delete()) {
                System.out.println(file.getAbsolutePath());
                return false;
            }
            return true;
        }

    }
}
