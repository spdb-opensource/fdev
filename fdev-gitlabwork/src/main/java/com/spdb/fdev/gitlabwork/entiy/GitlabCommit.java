package com.spdb.fdev.gitlabwork.entiy;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitlabCommit
 *
 * @blame Android Team
 */
@Document(collection = "gitlabCommitInfo")
public class GitlabCommit implements Serializable {

    @Field("short_id")
    @ApiModelProperty(value="commit短id")
    private String short_id;

    @Field("groupName")
    @ApiModelProperty(value="小组名称")
    private String groupName;

    @Field("committer_email")
    @ApiModelProperty(value="提交人邮件")
    private String committer_email;

    @Field("committed_date")
    @ApiModelProperty(value="提交日期")
    private String committed_date;

    @Field("stats")
    @ApiModelProperty(value="行数统计情况")
    private HashMap stats;

    @Field("branchName")
    @ApiModelProperty(value="分支名称")
    private String branchName;

    @Field("projectName")
    @ApiModelProperty(value="项目名称")
    private String projectName;

    @Field("committer_name")
    @ApiModelProperty(value="提交人名称")
    private String committer_name;

    @Field("fileNameList")
    @ApiModelProperty(value="提交文件")
    private List fileNameList;

    @Field("fileDiffUrl")
    @ApiModelProperty(value="提交记录的git地址")
    private String fileDiffUrl;

    public String getShort_id() {
        return short_id;
    }

    public void setShort_id(String short_id) {
        this.short_id = short_id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCommitter_email() {
        return committer_email;
    }

    public void setCommitter_email(String committer_email) {
        this.committer_email = committer_email;
    }

    public String getCommitted_date() {
        return committed_date;
    }

    public void setCommitted_date(String committed_date) {
        this.committed_date = committed_date;
    }

    public HashMap getStats() {
        return stats;
    }

    public void setStats(HashMap stats) {
        this.stats = stats;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCommitter_name() {
        return committer_name;
    }

    public void setCommitter_name(String committer_name) {
        this.committer_name = committer_name;
    }

    public List getFileNameList() {
        return fileNameList;
    }

    public void setFileNameList(List fileNameList) {
        this.fileNameList = fileNameList;
    }

    public String getFileDiffUrl() {
        return fileDiffUrl;
    }

    public void setFileDiffUrl(String fileDiffUrl) {
        this.fileDiffUrl = fileDiffUrl;
    }
}
