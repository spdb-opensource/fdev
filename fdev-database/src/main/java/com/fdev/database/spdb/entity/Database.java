package com.fdev.database.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.*;


@Document(collection = "database")
@ApiModel(value="应用与数据库关联信息")
public class Database implements Serializable {
	private static final long serialVersionUID = -6785268527663229814L;

	@Id
	private ObjectId _id;

	@Field("id")
	@ApiModelProperty(value="数据库信息ID")
	private String id;

	@Field("database_type")
	@ApiModelProperty(value="数据库类型")
	private String database_type;

	@Field("database_name")
	@ApiModelProperty(value="库名")
	private String database_name;
	
	@Field("table_name")
	@ApiModelProperty(value="表名")
	private String table_name;

	@Field("appid")
	@ApiModelProperty(value="应用id")
	private String appid;

	@Field("tbNum")
	@ApiModelProperty(value="该应用下表名出现次数")
	private Integer tbNum;

	@Field("autoflag")
	@ApiModelProperty(value="维护标识 0 自动扫描(上传) ,1 手动维护（新增、修改），2 自动扫描(合并代码)")
	private String autoflag;

	@Field("status")
	@ApiModelProperty(value="状态  0 待确认，1 确认")
	private String status;

	@Field("columns")
	@ApiModelProperty(value="表中的字段")
	private List<Map> columns;

	@Field("primaryKey")
	@ApiModelProperty(value="主键")
	private List<String> primaryKey;

	@Field("uniqueIndex")
	@ApiModelProperty(value="唯一索引")
	private List<Map> uniqueIndex;

	@Field("index")
	@ApiModelProperty(value="索引")
	private List<Map> index;

    @Field("createTime")
    @ApiModelProperty(value="创建时间")
    private String createTime;

	public Database() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDatabase_type() {
		return database_type;
	}

	public void setDatabase_type(String database_type) {
		this.database_type = database_type;
	}

	public String getDatabase_name() {
		return database_name;
	}

	public void setDatabase_name(String database_name) {
		this.database_name = database_name;
	}

	public String getTable_name() {
		return table_name;
	}

	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public Integer getTbNum() {
		return tbNum;
	}

	public void setTbNum(Integer tbNum) {
		this.tbNum = tbNum;
	}

	public String getAutoflag() {
		return autoflag;
	}

	public void setAutoflag(String autoflag) {
		this.autoflag = autoflag;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Map> getColumns() {
		return columns;
	}

    public void setColumns(List<Map> columns) {
		this.columns = columns;
	}

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

	public List<String> getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(List<String> primaryKey) {
		this.primaryKey = primaryKey;
	}

	public List<Map> getUniqueIndex() {
		return uniqueIndex;
	}

	public void setUniqueIndex(List<Map> uniqueIndex) {
		this.uniqueIndex = uniqueIndex;
	}

	public List<Map> getIndex() {
		return index;
	}

	public void setIndex(List<Map> index) {
		this.index = index;
	}

	public void initId() {
		ObjectId temp = new ObjectId();
		this.id=temp.toString();
		this._id=temp;
	}

    public Database(String id, String database_type, String database_name, String table_name, String appid, Integer tbNum, String autoflag, String status, List<Map> columns, String createTime) {
        this.id = id;
        this.database_type = database_type;
        this.database_name = database_name;
        this.table_name = table_name;
        this.appid = appid;
        this.tbNum = tbNum;
        this.autoflag = autoflag;
        this.status = status;
        this.columns = columns;
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Database{" +
                "id='" + id + '\'' +
                ", database_type='" + database_type + '\'' +
                ", database_name='" + database_name + '\'' +
                ", table_name='" + table_name + '\'' +
                ", appid='" + appid + '\'' +
                ", tbNum=" + tbNum +
                ", autoflag='" + autoflag + '\'' +
                ", status='" + status + '\'' +
                ", columns=" + columns +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
