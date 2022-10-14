package com.spdb.fdev.base.utils;

import com.spdb.fdev.base.dict.Dict;

import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import io.minio.*;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
@RefreshScope
public class MinioUtils implements InitializingBean {

    MinioClient minioClient;

    @Value("${minio.endpoint}")
    String endpoint;
    @Value("${minio.firstKey}")
    String firstKey;
    @Value("${minio.secondKey}")
    String secondKey;

    private static Logger logger = LoggerFactory.getLogger(MinioUtils.class);

    /***
     * 上传文件至minio
     * @param path   文件存储路径
     * @param file  上传文件
     * @return
     * @throws Exception
     */
    public boolean minioUploadFiles(String buckt, String path, MultipartFile file) throws Exception {

        boolean isExit = minioClient.bucketExists(BucketExistsArgs.builder().bucket(buckt).build());
        if (!isExit) {
            throw new FdevException(ErrorConstants.PARAMS_ERROR, new String[]{buckt, "存储桶不存在"});
        }
        if (!CommonUtils.isNullOrEmpty(file)) {
            try {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(buckt).object(path).stream(
                                file.getInputStream(), file.getInputStream().available(), -1).build());
                logger.info("文件上传成功");
            } catch (Exception e) {
//                logger.error("在上传文件至minio时失败", e);
                throw e;
            } finally {
                file.getInputStream().close();
            }
        return true;
        } else {
            throw new FdevException(ErrorConstants.PARAMS_ERROR, new String[]{Dict.FILES, " 上传文件数量为空"});
        }
    }

    public String minioDowloadFiles(String bucket, String path) throws Exception {
        boolean isExit = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!isExit) {
            throw new FdevException(ErrorConstants.PARAMS_ERROR, new String[]{bucket, "存储桶不存在"});
        }
        InputStream in = null;
        StringBuilder file_content = new StringBuilder();
        try {
            //查询文件是否存在
            minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(path).build());
            //文件下载
            in = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(path).build());
            byte[] buff = new byte[1024];
            int len;
            while (-1 != (len = in.read(buff))) {
                file_content.append(new String(buff, 0, len));
            }
        } catch (Exception e) {
            throw new FdevException("文件下载失败");
        } finally {
            if(in != null) {
                try {
                    in.close();
                }catch (Exception e) {
                    logger.error("文件流关闭异常");
                }
            }
        }
        return file_content.toString();
    }

    public InputStream downloadFileStream(String bucket, String minioPath) throws Exception{
        boolean isExit = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!isExit) {
            throw new FdevException(ErrorConstants.PARAMS_ERROR, new String[]{bucket, "存储桶不存在"});
        }
        InputStream in = null;
        StringBuilder file_content = new StringBuilder();
        try {
            //查询文件是否存在
            minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(minioPath).build());
            //文件下载
            in = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(minioPath).build());
            return in;
        } catch (Exception e) {
            throw new FdevException("文件下载失败");
        }
    }

    /**
     * 获取文件的md5
     *
     * @param bucket
     * @param object
     * @return
     * @throws Exception
     */
    public String getMd5(String bucket, String object) throws Exception {
        ObjectStat objectStat = null;
        try {
            objectStat = minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(object).build());
            return objectStat.etag();
        } catch (Exception e) {
            logger.error(" Minio get file md5 failed !!!!!");
            return "";
        }
    }

    /**
     * 获取objectStat
     *
     * @param bucket
     * @param object
     * @return
     */
    public ObjectStat getObjectStat(String bucket, String object) {
        ObjectStat objectStat = null;
        try {
            objectStat = minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(object).build());
            return objectStat;
        } catch (Exception e) {
            logger.error(" Minio get file objectStat failed !!!!!");
            return null;
        }
    }

    private void init() {
        try {
            if (!CommonUtils.isNullOrEmpty(endpoint) && !CommonUtils.isNullOrEmpty(firstKey) && !CommonUtils.isNullOrEmpty(secondKey)) {
                minioClient = MinioClient.builder()
                        .endpoint(endpoint)
                        .credentials(firstKey, secondKey)
                        .build();
            } else {
                throw new Exception(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
            }
        } catch (Exception e) {
            logger.error("restClient.close occur error");
        }
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }
}
