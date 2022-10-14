package com.spdb.fdev.base.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spdb.fdev.base.utils.CommonUtil;

public class FileInfoUtil {

    private FileInfoUtil() {
    }

    private static Logger logger = LoggerFactory.getLogger(FileInfoUtil.class);
    
    /**
     * 文件压缩
     * @param directory_name
     * @param zip_name
     */
    public static void pressToZip(String directory_name, String zip_name) {
        ZipOutputStream zos = null;
        FileOutputStream fos =null;
        try {
            fos = new FileOutputStream(zip_name);
            zos = new ZipOutputStream(fos);
            File sourceFile = new File(directory_name);
            compress(sourceFile, zos, sourceFile.getName());
        } catch (Exception e) {
            logger.error("异常:{}", e);
        } finally {
            if(zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    logger.error("关闭流异常");
                }
            }
            if(fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error("关闭流异常");
                }
            }
        }
    }


    private static void compress(File sourceFile, ZipOutputStream zos, String name) throws IOException {
        byte[] buf = new byte[2048];
        if(sourceFile.isFile()) {
            zos.putNextEntry(new ZipEntry(name));
            int len;
            FileInputStream in = null;
            try {
                in = new FileInputStream(sourceFile);
                while((len = in.read(buf)) != -1) {
                    zos.write(buf, 0 , len);
                }
                zos.closeEntry();
            } finally {
                if(in!=null) {
                    in.close();
                }
            }
        } else {
            File[] listFiles = sourceFile.listFiles();
            if(listFiles == null || listFiles.length == 0) {
                zos.putNextEntry(new ZipEntry(name + "/"));
                zos.closeEntry();
            } else {
                for(File file : listFiles) {
                    compress(file, zos, name + "/" + file.getName());
                }
            }
        }
    } 
    //删除文件夹
    public static boolean deleteDirectory(File project, String localProjectPath) {
        if(CommonUtil.isNullOrEmpty(project) || !project.exists()) {
            logger.info("本地目录不正确，请检查{}", localProjectPath);
            return false;
        }
        File[] files = project.listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                deleteDirectory(file, null);
            } else {
                file.delete();
            }
        }
        project.delete();
        return true;
    }

    //删除文件夹
    public static boolean deleteFile(File file) {
        if(file.exists() && file.isFile()){
            if(file.delete()){
                return true;
            }else {
                return false;
            }
        }
        return false;
    }

    // 判断文件夹名称合法性,只能时英文数字和汉字，合法返回true
    public static boolean checkName(String name) {
        String patter = "[^(a-zA-Z0-9\\u4e00-\\u9fa5)]+$";
        for (char a : name.toCharArray()) {
            if (Character.toString(a).matches(patter)) {
                return false;
            }
        }
        return true;
    }
}
