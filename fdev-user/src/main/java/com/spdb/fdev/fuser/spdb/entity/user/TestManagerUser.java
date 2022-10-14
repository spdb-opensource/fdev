package com.spdb.fdev.fuser.spdb.entity.user;

import com.spdb.fdev.fuser.base.dict.Constants;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = Constants.TEST_MANAGER_USER)
public class TestManagerUser {
    @Id
    private ObjectId _id;
    /**
     * 域账号
     */
    @Field("user_name_en")
    @Indexed
    private String user_name_en;
    /**
     * 用户姓名
     */
    @Field("user_name_cn")
    private String user_name_cn;
    /**
     * 邮箱
     */
    @Field("user_email")
    private String user_email;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getUser_name_en() {
        return user_name_en;
    }

    public void setUser_name_en(String user_name_en) {
        this.user_name_en = user_name_en;
    }

    public String getUser_name_cn() {
        return user_name_cn;
    }

    public void setUser_name_cn(String user_name_cn) {
        this.user_name_cn = user_name_cn;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

}
