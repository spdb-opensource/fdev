package com.spdb.fdev.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/9/29-14:41
 * @Description：caas需要修改的信息
 */
@Document(collection = Dict.CAASMODIFYKEY)
public class CaasModifyKey implements Serializable {
    private static final long serialVersionUID = 8637314009320661723L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("type")
    private String type;

    @Field("modifykey")
    private Map<String, Object> modifykey;

    public CaasModifyKey(){

    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getModifykey() {
        return modifykey;
    }

    public void setModifykey(Map<String, Object> modifykey) {
        this.modifykey = modifykey;
    }

    @Override
    public String toString() {
        return "CaasModifyKey{" +
                "_id=" + _id +
                ", type='" + type + '\'' +
                ", modifykey=" + modifykey +
                '}';
    }
}
