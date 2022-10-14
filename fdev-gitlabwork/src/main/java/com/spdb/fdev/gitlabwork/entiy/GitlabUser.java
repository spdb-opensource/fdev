package com.spdb.fdev.gitlabwork.entiy;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * GitlabUser
 *
 * @blame Android Team
 */
@Document(collection = "gitlabUserInfo")
public class GitlabUser implements Serializable {
    @Field("user_id")
    @ApiModelProperty(value="人员id")
    private String user_id;

    @Field("name")
    @ApiModelProperty(value="人员中文名")
    private String name;

    @Field("username")
    @ApiModelProperty(value="人员英文名")
    private String username;

    @Field("groupid")
    @ApiModelProperty(value="小组id")
    private String groupid;

    @Field("groupname")
    @ApiModelProperty(value="小组名")
    private String groupname;

    @Field("gituser")
    @ApiModelProperty(value="人员gitlab名称")
    private String gituser;

    @Field("companyid")
    @ApiModelProperty(value="公司id")
    private String companyid;

    @Field("companyname")
    @ApiModelProperty(value="公司名称")
    private String companyname;

    @Field("rolename")
    @ApiModelProperty(value="角色名称")
    private String rolename;

    @Field("configname")
    @ApiModelProperty(value="人员本地git配置名称")
    private String configname;

    @Field("sign")
    @ApiModelProperty(value="人员标识，0：新增 1：已同步")
    private int sign;

    @Field("status")
    @ApiModelProperty(value="是否在职")
    private String status;

    @Field("areaid")
    @ApiModelProperty(value="地区id")
    private String areaid;

    @Field("areaname")
    @ApiModelProperty(value="地区名")
    private String areaname;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getGituser() {
        return gituser;
    }

    public void setGituser(String gituser) {
        this.gituser = gituser;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getRolename() {
        return rolename;
    }

    public void setRolename(String rolename) {
        this.rolename = rolename;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public String getConfigname() {
        return configname;
    }

    public void setConfigname(String configname) {
        this.configname = configname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAreaid() {
        return areaid;
    }

    public void setAreaid(String areaid) {
        this.areaid = areaid;
    }

    public String getAreaname() {
        return areaname;
    }

    public void setAreaname(String areaname) {
        this.areaname = areaname;
    }
}
