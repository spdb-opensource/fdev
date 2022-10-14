package com.fdev.docmanage.util;

import com.fdev.docmanage.dict.Dict;
import com.fdev.docmanage.dict.ErrorConstants;
import com.fdev.docmanage.entity.Constants;
import com.spdb.fdev.common.exception.FdevException;
import io.minio.*;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RefreshScope
public class MinIoUtils implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(MinIoUtils.class);

    private MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;
    @Value("${minio.bucketName}")
    private String bucketName;

    public void init() {
        try {
            if (!CommonUtils.isNullOrEmpty(endpoint) && !CommonUtils.isNullOrEmpty(accessKey) && !CommonUtils.isNullOrEmpty(secretKey)) {
                minioClient = MinioClient.builder()
                        .endpoint(endpoint)
                        .credentials(accessKey, secretKey)
                        .build();
            } else {
                throw new Exception(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("restClient.close occur error");
        }
    }

    /***
     * 上传文件至minio
     * @param path   文件存储路径
     * @param files  上传文件
     * @return
     * @throws Exception
     */
    public boolean minioUploadFiles(String[] path, MultipartFile[] files) throws Exception {

        boolean isExit = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!isExit) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{bucketName, "存储桶不存在"});
        }
        if (!CommonUtils.isNullOrEmpty(files)) {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                try {
                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(bucketName).object(path[i]).stream(
                                    file.getInputStream(), file.getInputStream().available(), -1)
                            .build());
                    logger.info("文件上传成功");
                } catch (Exception e) {
                    logger.error("在上传文件至minio时失败", e);
                    throw new FdevException(ErrorConstants.FILE_UPLOAD_MINIO_FAILAD);
                } finally {
                    file.getInputStream().close();
                }
            }
            return true;
        } else {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.FILES, " 上传文件数量为空"});
        }
    }

    /***
     * minio文件删除
     * @param path   文件存储路径
     * @return
     * @throws Exception
     */
    public boolean minioDeleteFiles(String[] path) throws Exception {

        boolean isExit = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!isExit) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{bucketName, "存储桶不存在"});
        }
        for (int i = 0; i < path.length; i++) {
            try {
                //查询文件是否存在
                minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(path[i]).build());
                //文件删除
                minioClient.removeObject(RemoveObjectArgs.
                        builder().bucket(bucketName).object(path[i]).build());
                logger.info("文件删除成功");
                return true;
            } catch (Exception e) {
                logger.error("在删除minio上文件时失败", e);
                throw new FdevException(ErrorConstants.FILE_DELETE_MINIO_FAILAD + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 下载文件
     *
     * @param response 响应报文
     * @param path     下载文件路径
     * @return
     * @throws Exception
     */
    public void minioDowloadFiles(HttpServletResponse response, String path) throws Exception {
        boolean isExit = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!isExit) {
            throw new FdevException(
                    ErrorConstants.PARAM_ERROR, new String[]{bucketName, "存储桶不存在"});
        }
        InputStream in = null;
        OutputStream os = null;
        String path2 = URLDecoder.decode(path, "UTF-8");
        if(path2.contains("+")){
            path = path2;
        }
        try {
            //查询文件是否存在
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(path).build());
            int index = path.lastIndexOf("/");
            String filename = path.substring(index + 1);
            //文件下载
            in = minioClient.getObject(GetObjectArgs.
                    builder().bucket(bucketName).object(path).build());
            //中文为两个字节，设置为utf-8编码为3个字节，因此需要设置字节数为3的倍数
            byte[] bytes = new byte[3072];
            response.reset();
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(new String(filename.getBytes()), "UTF-8"));
            //下载任意文件
            response.setContentType("application/form-data");
            response.setHeader("Access-Control-Expose-Headers", "content-disposition");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Access-Control-Allow-Origin", "*");
            os = new BufferedOutputStream(response.getOutputStream());
            int count = -1;
            while ((count = in.read(bytes)) != -1) {
                os.write(bytes,0,count);
                os.flush();
            }
        } catch (Exception e) {
            logger.error("在下载minio上文件时失败", e);
            throw new FdevException(ErrorConstants.FILE_DOWLOAD_MINIO_FAILAD);
        } finally {
            in.close();
            os.close();
        }
    }

    /**
     * 查询文件列表
     *
     * @param path
     * @return
     * @throws Exception
     */
    public ArrayList<String> minioListFiles(String path) throws Exception {

        boolean isExit = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!isExit) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{bucketName, "存储桶不存在"});
        }
        Iterable<Result<Item>> results = null;
        try {
            //文件列表查询
            results = minioClient.listObjects(ListObjectsArgs.builder().
                    bucket(bucketName).recursive(true).build());
        } catch (Exception e) {
            logger.error("获取文件列表失败", e);

        }
        ArrayList<String> arrayList = new ArrayList<>();
        for (Result<Item> itemResult : results) {
            Item item = itemResult.get();
            String filePath = URLDecoder.decode(item.objectName(), "utf-8");
            if (!CommonUtils.isNullOrEmpty(filePath) && filePath.indexOf(path) >= 0) {
                arrayList.add(filePath);
                continue;
            }
        }
        return arrayList;

    }

    @Override
    public void afterPropertiesSet() {
        init();
    }

    public void fdevMinio() {
        try {
            boolean isExit = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExit) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{bucketName, "存储桶不存在"});
            }
        } catch (Exception e) {
            logger.error("在下载minio上文件时失败", e);
            throw new FdevException(ErrorConstants.FILE_DOWLOAD_MINIO_FAILAD);
        }
    }

    public void minioDowloadFilesAll2(List<Map<String, Object>> fileList, String pathName) throws IOException {
        //创建目录
        File directory = new File(pathName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (!CommonUtils.isNullOrEmpty(fileList)) {
            for (Map<String, Object> fileInfo : fileList) {
                String type = (String) fileInfo.get(Dict.FILETYPE);
                if (Dict.FILE.equals(type)) {
                    //生成文件到本地
                    String path = (String) fileInfo.get(Dict.PATH);
                    //查询文件是否存在
                    try {
                        minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(path).build());
                    } catch (Exception e) {
                        logger.info("文件不存在", "");
                        continue;
                    }
                    FileOutputStream os = null;
                    try {
                        InputStream in = minioClient.getObject(GetObjectArgs.
                                builder().bucket(bucketName).object(path).build());
                        byte[] buffer = new byte[3072];
                        int count = -1;
                        os = new FileOutputStream(pathName + fileInfo.get(Dict.FILENAME));
                        while ((count = in.read(buffer)) != -1) {
                            os.write(buffer, 0, count);
                            os.flush();
                        }
                    } catch (IOException e) {
                        logger.info("{}文件生成失败", "");
                    } catch (Exception e) {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e1) {
                                logger.info("关闭流异常");
                            }
                        }
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e1) {
                                logger.info("关闭流异常");
                            }
                        }
                    }
                } else if ("folder".equals(type)) {
                    minioDowloadFilesAll2((List<Map<String, Object>>) fileInfo.get(Dict.FOLDER), (String) fileInfo.get(Dict.PATHNAME));
                }
            }
        }
    }

    public void downloadFileZip(HttpServletResponse response, String path, String zipName) throws IOException {
        InputStream in = null;
        OutputStream os = null;
        byte[] buffer;
        File zip = new File(path);
        try {
            in = new BufferedInputStream(new FileInputStream(zip));
            buffer = new byte[in.available()];
            in.read(buffer);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        response.reset();
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(new String(zipName.getBytes()), "UTF-8"));
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Length", String.valueOf(zip.length()));
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        try {
            os = new BufferedOutputStream(response.getOutputStream());
            os.write(buffer);
            os.flush();
        } catch (IOException e) {
            logger.info("{}文件生成失败", "");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e1) {
                    logger.info("关闭流异常");
                }
            }
        }
    }
}


