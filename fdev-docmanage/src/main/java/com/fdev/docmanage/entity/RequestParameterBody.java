package com.fdev.docmanage.entity;

import java.util.List;
import java.util.Map;

public class RequestParameterBody {

    private String fileName;

    private String fileType;

    private String fileNameConflictBehavior;

    private boolean parents;

    private Long fileSize;

    private Map<String,String> fileHashes;

    private String uploadMethod;

    private String feedback;

    private String status;

    private String fileExtension;

    private String fileContent;

    private String location;

    private List<Operate> operates;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileNameConflictBehavior() {
        return fileNameConflictBehavior;
    }

    public void setFileNameConflictBehavior(String fileNameConflictBehavior) {
        this.fileNameConflictBehavior = fileNameConflictBehavior;
    }

    public boolean isParents() {
        return parents;
    }

    public void setParents(boolean parents) {
        this.parents = parents;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUploadMethod() {
        return uploadMethod;
    }

    public void setUploadMethod(String uploadMethod) {
        this.uploadMethod = uploadMethod;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getFileHashes() {
        return fileHashes;
    }

    public void setFileHashes(Map<String, String> fileHashes) {
        this.fileHashes = fileHashes;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Operate> getOperates() {
        return operates;
    }

    public void setOperates(List<Operate> operates) {
        this.operates = operates;
    }

    public class Operate{
        private String operate;

        private Map<String,Object> args;

        public String getOperate() {
            return operate;
        }

        public void setOperate(String operate) {
            this.operate = operate;
        }

        public Map<String, Object> getArgs() {
            return args;
        }

        public void setArgs(Map<String, Object> args) {
            this.args = args;
        }
    }

    public Operate newOperate(String operateStr){
        Operate operate = new Operate();
        operate.setOperate(operateStr);
        return operate;
    }
}
