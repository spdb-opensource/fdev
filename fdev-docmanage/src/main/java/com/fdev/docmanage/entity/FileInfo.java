package com.fdev.docmanage.entity;

import io.swagger.annotations.ApiModel;

import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fileInfo")
@ApiModel(value="文件")
public class FileInfo {
	
	 //主键 id
    @Id
    private ObjectId _id;
    //文件夹名称（需求编号）
    private String name;
    //卷id
    private String volume_id;
    //修改时间
    private String updated_at;
    //父文件夹标识
    private String parent_id;
    //修改者
    private Map updated_by;
    //创建时间
    private String created_at;
    //文件夹id
    private String id;
    //文件类型
    private String type;
    //创建者
    private Map created_by;

    
    
    public String get_id() {
        return _id.toString();
    }

    public void set_id(Object _id) {
        if (_id instanceof ObjectId) {
            this._id = (ObjectId) _id;
        } else if (_id instanceof String) {
            this._id = new ObjectId((String) _id);
        }
    }

	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getVolume_id() {
		return volume_id;
	}



	public void setVolume_id(String volume_id) {
		this.volume_id = volume_id;
	}



	public String getUpdated_at() {
		return updated_at;
	}



	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}



	public String getParent_id() {
		return parent_id;
	}



	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}



	public Map<String, Map> getUpdated_by() {
		return updated_by;
	}



	public void setUpdated_by(Map<String, Map> updated_by) {
		this.updated_by = updated_by;
	}



	public String getCreated_at() {
		return created_at;
	}



	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}



	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
	}



	public String getType() {
		return type;
	}



	public void setType(String type) {
		this.type = type;
	}



	public Map<String, Map> getCreated_by() {
		return created_by;
	}



	public void setCreated_by(Map<String, Map> created_by) {
		this.created_by = created_by;
	}

	@Override
    public String toString() {
        return "File{" +
                "_id=" + _id +
                ", name='" + name + '\'' +
                ", volume_id='" + volume_id + '\'' +
                ", updated_at='" + updated_at + '\'' +
                ", parent_id='" + parent_id + '\'' +
                ", updated_by='" + updated_by + '\'' +
                ", created_at='" + created_at + '\'' +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", created_by=" + created_by +
                '}';
    }

	 //存数据时，初始化_id
    public void initId() {
        ObjectId temp = new ObjectId();
        this.set_id(temp);
    }
}
