package com.spdb.fdev.fdemand.spdb.entity;


/**
 * 设计还原文档
 */
public class DesignDoc {
    //路径
   private String minioPath;
   //文件类型
   private String docType;
   //文件名字
   private String fileName;
   //上传阶段
   private String uploadStage;

    public String getMinioPath() {
        return minioPath;
    }

    public void setMinioPath(String minioPath) {
        this.minioPath = minioPath;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploadStage() {
        return uploadStage;
    }

    public void setUploadStage(String uploadStage) {
        this.uploadStage = uploadStage;
    }

    @Override
    public String toString() {
        return "DesignDoc{" +
                "minioPath='" + minioPath + '\'' +
                ", docType='" + docType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", uploadStage='" + uploadStage + '\'' +
                '}';
    }
}
