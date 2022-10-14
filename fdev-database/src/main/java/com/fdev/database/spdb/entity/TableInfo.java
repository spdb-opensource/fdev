package com.fdev.database.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document(collection = "tableInfo")
@ApiModel(value="库表信息")
public class TableInfo implements Serializable {
    private static final long serialVersionUID = -3454544698799042176L;

    @Id
    private ObjectId _id;

    @Field("tableid")
    @ApiModelProperty(value="库表ID")
    private String tableid;

    @Field("database_type")
    @ApiModelProperty(value="数据库类型")
    private String database_type;

    @Field("database_name")
    @ApiModelProperty(value="库名")
    private String database_name;

    @Field("table_name")
    @ApiModelProperty(value="表名")
    private String table_name;

    @Field("columns")
    @ApiModelProperty(value="表中的字段及类型")
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

    public String getTableid() {
        return tableid;
    }

    public void setTableid(String tableid) {
        this.tableid = tableid;
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

    public List<Map> getColumns() {
        return columns;
    }

    public void setColumns(List<Map> columns) {
        this.columns = columns;
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public TableInfo() {

    }

    public TableInfo(String tableid, String database_type, String database_name, String table_name, List<Map> columns, List<String> primaryKey, List<Map> uniqueIndex, List<Map> index) {
        this.tableid = tableid;
        this.database_type = database_type;
        this.database_name = database_name;
        this.table_name = table_name;
        this.columns = columns;
        this.primaryKey = primaryKey;
        this.uniqueIndex = uniqueIndex;
        this.index = index;
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "tableid='" + tableid + '\'' +
                ", database_type='" + database_type + '\'' +
                ", database_name='" + database_name + '\'' +
                ", table_name='" + table_name + '\'' +
                ", columns=" + columns +
                ", primaryKey=" + primaryKey +
                ", uniqueIndex=" + uniqueIndex +
                ", index=" + index +
                '}';
    }

    public void initId() {
        ObjectId temp = new ObjectId();
        this.tableid=temp.toString();
        this._id=temp;
    }
}
