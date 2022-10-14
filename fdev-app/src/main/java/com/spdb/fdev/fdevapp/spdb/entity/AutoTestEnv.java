package com.spdb.fdev.fdevapp.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document(collection = "auto-test-env")
@ApiModel(value = "自动测试平台")
public class AutoTestEnv implements Serializable {

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;

    @Field("gitlab_project_id")
    @Indexed(unique = true)
    @ApiModelProperty(value = "gitlab项目ID")
    private String gitlab_project_id;

//    @Field("sitAutoTestEnvId")
//    @ApiModelProperty(value = "内测自动化测试环境id")
//    private String sitAutoTestEnvId;
//    @Field("sitAutoTestEnvName")
//    @ApiModelProperty(value = "内测自动化测试环境name")
//    private String sitAutoTestEnvName;
    @Field("sitProjectDeploySwitchInfo")
    @ApiModelProperty(value = "内测自动化测试环境部署详情")
    private String sitProjectDeploySwitchInfo;

    @Field("sitAutoTest")
    @ApiModelProperty(value = "sit自动化测试环境数据")
    private List<Map<String, String>> sitAutoTest;



//    @Field("uatAutoTestEnvId")
//    @ApiModelProperty(value = "UAT自动化测试环境id")
//    private String uatAutoTestEnvId;
//    @Field("uatAutoTestEnvName")
//    @ApiModelProperty(value = "UAT自动化测试环境name")
//    private String uatAutoTestEnvName;
    @Field("uatProjectDeploySwitchInfo")
    @ApiModelProperty(value = "UAT自动化测试环境部署详情")
    private String uatProjectDeploySwitchInfo;

    @Field("uatAutoTest")
    @ApiModelProperty(value = "uat自动化测试环境数据")
    private List<Map<String, String>> uatAutoTest;

//    @Field("relAutoTestEnvId")
//    @ApiModelProperty(value = "准生产自动化测试环境id")
//    private String relAutoTestEnvId;
//    @Field("relAutoTestEnvName")
//    @ApiModelProperty(value = "准生产自动化测试环境name")
//    private String relAutoTestEnvName;
    @Field("relProjectDeploySwitchInfo")
    @ApiModelProperty(value = "准生产自动化测试环境部署详情")
    private String relProjectDeploySwitchInfo;

    @Field("relAutoTest")
    @ApiModelProperty(value = "rel自动化测试环境数据")
    private List<Map<String, String>> relAutoTest;


    @Field("lastUpdateDate")
    @ApiModelProperty(value = "最后更新时间")
    private String lastUpdateDate;

    @Field("lastUpdateUserId")
    @ApiModelProperty(value = "最后更新时间")
    private String lastUpdateUserId;

    @Field("createDate")
    @ApiModelProperty(value = "创建时间")
    private String createDate;




    public AutoTestEnv() {
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getGitlab_project_id() {
        return gitlab_project_id;
    }

    public void setGitlab_project_id(String gitlab_project_id) {
        this.gitlab_project_id = gitlab_project_id;
    }

    public List<Map<String, String>> getSitAutoTest() {
        return sitAutoTest;
    }

    public void setSitAutoTest(List<Map<String, String>> sitAutoTest) {
        this.sitAutoTest = sitAutoTest;
    }

    public List<Map<String, String>> getUatAutoTest() {
        return uatAutoTest;
    }

    public void setUatAutoTest(List<Map<String, String>> uatAutoTest) {
        this.uatAutoTest = uatAutoTest;
    }

    public List<Map<String, String>> getRelAutoTest() {
        return relAutoTest;
    }

    public void setRelAutoTest(List<Map<String, String>> relAutoTest) {
        this.relAutoTest = relAutoTest;
    }

    public String getSitProjectDeploySwitchInfo() {
        return sitProjectDeploySwitchInfo;
    }

    public void setSitProjectDeploySwitchInfo(String sitProjectDeploySwitchInfo) {
        this.sitProjectDeploySwitchInfo = sitProjectDeploySwitchInfo;
    }

    public String getUatProjectDeploySwitchInfo() {
        return uatProjectDeploySwitchInfo;
    }

    public void setUatProjectDeploySwitchInfo(String uatProjectDeploySwitchInfo) {
        this.uatProjectDeploySwitchInfo = uatProjectDeploySwitchInfo;
    }

    public String getRelProjectDeploySwitchInfo() {
        return relProjectDeploySwitchInfo;
    }

    public void setRelProjectDeploySwitchInfo(String relProjectDeploySwitchInfo) {
        this.relProjectDeploySwitchInfo = relProjectDeploySwitchInfo;
    }

    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getLastUpdateUserId() {
        return lastUpdateUserId;
    }

    public void setLastUpdateUserId(String lastUpdateUserId) {
        this.lastUpdateUserId = lastUpdateUserId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "AutoTests{" +
                "_id=" + _id +
                ", gitlab_project_id='" + gitlab_project_id + '\'' +
                ", sitProjectDeploySwitchInfo='" + sitProjectDeploySwitchInfo + '\'' +
                ", sitAutoTest=" + sitAutoTest +
                ", uatProjectDeploySwitchInfo='" + uatProjectDeploySwitchInfo + '\'' +
                ", uatAutoTest=" + uatAutoTest +
                ", relProjectDeploySwitchInfo='" + relProjectDeploySwitchInfo + '\'' +
                ", relAutoTest=" + relAutoTest +
                ", lastUpdateDate='" + lastUpdateDate + '\'' +
                ", lastUpdateUserId='" + lastUpdateUserId + '\'' +
                ", createDate='" + createDate + '\'' +
                '}';
    }
}
