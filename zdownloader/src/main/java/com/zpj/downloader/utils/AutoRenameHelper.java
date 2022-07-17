package com.zpj.downloader.utils;

import java.io.File;

/**
 * 自动重命名冲突的任务。例如：file.xxx重命名为file(1).xxx
 * @author Z-P-J
 */
public class AutoRenameHelper {

    private static final String FORMAT_NAME= "%s(%s)%s";


    public static File renameFile(File file) {
        if (file.isFile()) {
            String filePath = file.getAbsolutePath();

            String name;
            String suffix;
            int index = filePath.lastIndexOf('.');
            if (index >= 0) {
                name = filePath.substring(0, index);
                suffix = filePath.substring(index);
            } else {
                name = filePath;
                suffix = "";
            }
            return renameFile(name, suffix, 0);
        } else {
            return file;
        }
    }

    private static File renameFile(String fileName, String suffix, int num) {
        File file;
        if (num > 0) {
            file = new File(String.format(FORMAT_NAME, fileName, num, suffix));
        } else {
            file = new File(fileName + suffix);
        }
        if (file.isFile()) {
            file = renameFile(fileName, suffix, ++num);
        }
        return file;
    }

}
