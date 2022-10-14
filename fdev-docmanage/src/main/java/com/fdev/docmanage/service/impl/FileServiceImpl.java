package com.fdev.docmanage.service.impl;

import com.fdev.docmanage.dict.Dict;
import com.fdev.docmanage.entity.Constants;
import com.fdev.docmanage.service.UserInfoService;
import com.fdev.docmanage.service.IFileService;
import com.fdev.docmanage.util.CommonUtils;
import com.fdev.docmanage.util.MinIoUtils;
import com.spdb.fdev.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileServiceImpl implements IFileService {
//    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    @Autowired
    private MinIoUtils minIoUtils;

    private String loadPath="/home/ebank/spdb/fdev/load/";

    @Override
    public void fileUpload(String[] path,MultipartFile[] files) throws Exception {

        minIoUtils.minioUploadFiles(path, files);
        logger.info("文件上传成功！");
    }

    @Override
    public List<String> getFiles(String path) throws Exception {
        List<String> list = minIoUtils.minioListFiles(path);

        return list ;
    }

    @Override
    public void fileDownload(HttpServletResponse response, String path) throws Exception {
        //文件下载
        minIoUtils.minioDowloadFiles(response,path);
    }

    @Override
    public void fileDelete(String[] path) throws Exception {
        //文件删除
        minIoUtils.minioDeleteFiles(path);
    }
    @Override
    public void minioDowloadFolder(HttpServletResponse response, Map<String, Object> fileMap) throws Exception {
        String zipName = (String)fileMap.get(Dict.ZIPNAME);
        /** 将文件下载本地 */
        String path = (String) fileMap.get(Dict.PATHNAME);
        minIoUtils.fdevMinio();//判断文件桶是否存在
        minIoUtils.minioDowloadFilesAll2((List<Map<String, Object>>) fileMap.get(Dict.FOLDER), path);
        //文件打包
        pressToZip(loadPath + zipName, loadPath + zipName + Dict._ZIP);
        //文件下载
        minIoUtils.downloadFileZip(response, loadPath + zipName + Dict._ZIP, zipName + Dict._ZIP);
        deleteDirectory(new File(path), path);
        deleteFile(new File(loadPath + zipName + Dict._ZIP));
    }

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

    //删除文件夹
    public static boolean deleteDirectory(File project, String localProjectPath) {
        if(CommonUtils.isNullOrEmpty(project) || !project.exists()) {
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

}
