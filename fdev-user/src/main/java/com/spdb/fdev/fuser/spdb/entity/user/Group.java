package com.spdb.fdev.fuser.spdb.entity.user;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Document(collection = "group")
@ApiModel(value = "小组")
public class Group implements Serializable {

    private static final long serialVersionUID = 7528659211794720516L;
    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "小组ID")
    private String id;

    @Field("name")
    @ApiModelProperty(value = "小组名称")
    private String name;

    @Field("full_name")
    @ApiModelProperty(value = "小组全名称")
    private String fullName;

    @ApiModelProperty(value = "小组人数,包括下级小组")
    private Integer count;

    @ApiModelProperty(value = "仅本小组人数")
    private Integer current_count;

    @Field("parent_id")
    @ApiModelProperty(value = "父id")
    private String parent_id;

    @Field("level")
    @ApiModelProperty(value = "小组级别")
    private Integer level;//1级为大组，2为团队，3及以后为小组

    @Field("status")
	@ApiModelProperty(value="小组状态, 0是废弃, 1是使用")
	private String status;

    @Field("sortNum")
    @ApiModelProperty(value="小组排序序号")
    private String sortNum;

    @ApiModelProperty(value = "子组")
    private List<Group> childGroup;

    public Group() {
    }

    public Group(String id, String name, Integer count, String parent_id, String status, Integer level, String sortNum) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.parent_id = parent_id;
        this.status = status;
        this.level = level;
        this.sortNum = sortNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getCurrent_count() {
        return current_count;
    }

    public void setCurrent_count(Integer current_count) {
        this.current_count = current_count;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId get_id() {
        return _id;
    }

    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

    public String getSortNum() {
        return sortNum;
    }

    public void setSortNum(String sortNum) {
        this.sortNum = sortNum;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Group> getChildGroup() {
        return childGroup;
    }

    public void setChildGroup(List<Group> childGroup) {
        this.childGroup = childGroup;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    //存数据时，初始化_id和id
    public void initId() {
        ObjectId temp = new ObjectId();
        this._id = temp;
        this.id = temp.toString();
    }

}
