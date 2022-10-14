package com.spdb.fdev.release.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Document(collection = "prod_asset")
@ApiModel(value = "变更文件列表")
@CompoundIndexes({
        @CompoundIndex(name = "prod_asset_namex", def = "{'prod_id': 1, 'asset_catalog_name': 1, 'runtime_env' : 1, 'filename' : 1, 'bucket_name' : 1, 'bucket_path' : 1, 'source_application':1 , 'child_catalog' : 1 }", unique = true)
//		@CompoundIndex(name = "prod_asset_sqlnox", def = "{'prod_id': 1, 'asset_catalog_name': 1, 'seq_no' : 1}", unique = true)
})
public class ProdAsset implements Serializable {

    private static final long serialVersionUID = 8266994712527533661L;

    @Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;

    @Field("id")
    @Indexed(unique = true)
    private String id;

    @Field("upload_username_cn")
    @ApiModelProperty(value = "上传人中文名")
    private String upload_username_cn;

    @Field("release_node_name")
    @ApiModelProperty(value = "投产窗口名称")
    private String release_node_name;

    @Field("prod_id")
    @NotEmpty(message = "变更文件id不能为空")
    @ApiModelProperty(value = "变更id")
    private String prod_id;

    @Field("filename")
    @ApiModelProperty(value = "上传文件名")
    private String fileName;


    @Field("child_catalog")
    @ApiModelProperty(value = "子目录名")
    private String child_catalog;

    @Field("file_giturl")
    @ApiModelProperty(value = "文件gitlab路径")
    private String file_giturl;

    @Field("file_encoding")
    @ApiModelProperty(value = "文件编码")
    private String file_encoding;


    @Field("asset_catalog_name")
    @NotEmpty(message = "介质目录不能为空")
    @ApiModelProperty(value = "介质目录")
    private String asset_catalog_name;

    @Field("source_application")
    @ApiModelProperty(value = "来源应用id")
    private String source_application;

    @Field("source_application_name")
    @ApiModelProperty(value = "来源应用名称")
    private String source_application_name;

    @Field("runtime_env")
    @ApiModelProperty(value = "运行环境")
    private String runtime_env;

    @Field("file_type")
    @NotEmpty(message = "文件类型不能为空")
    @ApiModelProperty(value = "文件类型")
    private String file_type;

    @Field("source")
    @NotEmpty(message = "文件来源类型不能为空")
    @ApiModelProperty(value = "文件来源类型:1-上传,2-gitlab选择")
    private String source;

    @Field("seq_no")
    @ApiModelProperty(value = "执行序号")
    private String seq_no;

    @Field("upload_user")
    @ApiModelProperty(value = "上传用户id")
    private String upload_user;

    @Field("upload_time")
    @ApiModelProperty(value = "上传时间")
    private String upload_time;

    @Field("prod_commitid")
    @ApiModelProperty(value = "变更文件投产介质版本")
    private String prod_commitid;

    @Field("projectId")
    @ApiModelProperty(value = "项目Id")
    private String projectId;

    @Field("update_time")
    @ApiModelProperty(value = "修改时间")
    private String update_time;

    @Field("cfg_type")
    @ApiModelProperty(value = "cfg类型")
    private String cfg_type;

    @Field("isFirst")
    @ApiModelProperty(value = "是否为首次投产")
    private boolean isFirst;

    @Field("bucket_name")
    @ApiModelProperty(value = "桶名称")
    private String bucket_name;

    @Field("bucket_path")
    @ApiModelProperty(value = "桶路径")
    private String bucket_path;

    @Field("write_flag")
    @ApiModelProperty(value = "写入标识")
    private String write_flag;

    @Field("sid")
    @ApiModelProperty(value = "应用sid")
    private String sid;

    @Field("aws_type")
    @ApiModelProperty(value = "AWS的文件来源标识 0-流水线传过来1-文件上传2-文件夹上传")
    private String aws_type;

    public ProdAsset() {
        super();
    }

    public ProdAsset(String file_giturl, String fileName, String release_node_name, String asset_catalog_name,
                     String source, String upload_user, String prod_id, String source_application,
                     String source_application_name, String projectId) {
        this.file_giturl = file_giturl;
        this.fileName = fileName;
        this.release_node_name = release_node_name;
        this.asset_catalog_name = asset_catalog_name;
        this.source = source;
        this.upload_user = upload_user;
        this.prod_id = prod_id;
        this.source_application = source_application;
        this.source_application_name = source_application_name;
        this.projectId = projectId;
    }


    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpload_username_cn() {
        return upload_username_cn;
    }

    public void setUpload_username_cn(String upload_username_cn) {
        this.upload_username_cn = upload_username_cn;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFile_giturl() {
        return file_giturl;
    }

    public void setFile_giturl(String file_giturl) {
        this.file_giturl = file_giturl;
    }

    public String getFile_encoding() {
        return file_encoding;
    }

    public void setFile_encoding(String file_encoding) {
        this.file_encoding = file_encoding;
    }

    public String getSource_application() {
        return source_application;
    }

    public void setSource_application(String source_application) {
        this.source_application = source_application;
    }

    public String getSource_application_name() {
        return source_application_name;
    }

    public void setSource_application_name(String source_application_name) {
        this.source_application_name = source_application_name;
    }


    public String getRuntime_env() {
        return runtime_env;
    }

    public void setRuntime_env(String runtime_env) {
        this.runtime_env = runtime_env;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSeq_no() {
        return seq_no;
    }

    public void setSeq_no(String seq_no) {
        this.seq_no = seq_no;
    }

    public String getUpload_user() {
        return upload_user;
    }

    public void setUpload_user(String upload_user) {
        this.upload_user = upload_user;
    }

    public String getUpload_time() {
        return upload_time;
    }

    public void setUpload_time(String upload_time) {
        this.upload_time = upload_time;
    }

    public String getProd_id() {
        return prod_id;
    }

    public void setProd_id(String prod_id) {
        this.prod_id = prod_id;
    }

    public String getChild_catalog() {
        return child_catalog;
    }

    public void setChild_catalog(String child_catalog) {
        this.child_catalog = child_catalog;
    }

    public String getAsset_catalog_name() {
        return asset_catalog_name;
    }

    public void setAsset_catalog_name(String asset_catalog_name) {
        this.asset_catalog_name = asset_catalog_name;
    }

    public String getProd_commitid() {
        return prod_commitid;
    }

    public void setProd_commitid(String prod_commitid) {
        this.prod_commitid = prod_commitid;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getCfg_type() {
        return cfg_type;
    }

    public void setCfg_type(String cfg_type) {
        this.cfg_type = cfg_type;
    }

    public boolean isFirst() {
        return this.isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public String getBucket_name() {
        return bucket_name;
    }

    public void setBucket_name(String bucket_name) {
        this.bucket_name = bucket_name;
    }

    public String getBucket_path() {
        return bucket_path;
    }

    public void setBucket_path(String bucket_path) {
        this.bucket_path = bucket_path;
    }

    public String getWrite_flag() {
        return write_flag;
    }

    public void setWrite_flag(String write_flag) {
        this.write_flag = write_flag;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getAws_type() {
        return aws_type;
    }

    public void setAws_type(String aws_type) {
        this.aws_type = aws_type;
    }
}
