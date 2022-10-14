package com.spdb.fdev.fuser.spdb.entity.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "holiday-vacation")
@ApiModel(value="节假日")
public class Vacation {

    @Id
    private ObjectId _id;

    @Field("time")
    @ApiModelProperty(value="日期")
    private String time;


    @Field("is_vacation")
    @ApiModelProperty(value="是否为节假日")           //0代表为工作日，1代表为节假日（包括周六、周日）
    private Integer is_vacation;


    public Vacation() {
    }

    public Vacation(String time, Integer is_vacation) {
        this.time = time;
        this.is_vacation = is_vacation;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getIs_vacation() {
        return is_vacation;
    }

    public void setIs_vacation(Integer is_vacation) {
        this.is_vacation = is_vacation;
    }

    @Override
    public String toString() {
        return "Vacation{" +
                "_id=" + _id +
                ", time='" + time + '\'' +
                ", is_vacation=" + is_vacation +
                '}';
    }
}
