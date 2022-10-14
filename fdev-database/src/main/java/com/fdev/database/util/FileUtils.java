package com.fdev.database.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class FileUtils {
    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);// 控制台日志打印

    //文件夹拷贝
    public static void copy(String sourcepath, String targetpath) {
        File file = new File(sourcepath);
        File[] files = file.listFiles();
        File file1 = new File(targetpath);
        if (!file1.exists() && file1.isDirectory()) {
            file1.mkdirs();
        }
        if (file.isFile()) {
            fileCopy(file.getPath(), targetpath);
        } else if (file.isDirectory()) {
            for (File f : files) {
                copy(f.getPath(), targetpath);
            }
        }
    }

    //文件拷贝
    public static void fileCopy(String sourcepath, String targetpath) {
        BufferedReader br = null;
        PrintStream ps = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(sourcepath)));
            ps = new PrintStream(new FileOutputStream(targetpath));
            String s = null;
            while ((s = br.readLine()) != null) {
                ps.println(s);
                ps.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ps.close();
        }
    }

    /**
     * 删除文件夹及其下面的子文件夹和文件
     *
     * @param file
     */
    public static void deleteFile(File file) {
        try {
            if (file.isFile()) {
                file.delete();
            } else {
                File[] listFiles = file.listFiles();
                if (listFiles != null && listFiles.length > 0) {
                    for (File subFile : listFiles) {
                        deleteFile(subFile);
                    }
                }
                file.delete();
            }
        } catch (Exception e) {
//            logger.error("删除失败，请人工删除，{}", file.getAbsolutePath());
        }
    }
    
    //写入文件到指定路径下
    public static void writerFile(String content, String path){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hhmmss");
        File file = new File(path + dateFormat.format(date) + ".txt");
        try (Writer out = new FileWriter(file)) {
            out.write(content);
        } catch (IOException e) {
            logger.error("写入文件到{}失败", path, e);
        }
    }

}
