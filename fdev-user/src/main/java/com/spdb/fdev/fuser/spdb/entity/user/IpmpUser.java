package com.spdb.fdev.fuser.spdb.entity.user;

import com.spdb.fdev.fuser.base.dict.Constants;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = Constants.IPMP_USER)
public class IpmpUser {
    @Id
    private ObjectId _id;
    /**
     * 域账号
     */
    @Field("user_name_en")
    @Indexed
    private String user_name_en;
    /**
     * 工号
     */
    @Field("staff_no")
    private String staff_no;
    /**
     * 用户姓名
     */
    @Field("user_name_cn")
    private String user_name_cn;
    /**
     * 邮箱
     */
    @Field("email")
    private String email;

    /**
     * 是否为行内人员
     */
    @Field("is_spdb")
    private Boolean is_spdb;

    /**
     * 机构
     */
    @Field("dept_name")
    private String dept_name;

    /**
     * 公司（浦发，科蓝等）
     */
    @Field("company_full_name")
    private String company_full_name;

    /**
     * 用户状态（在职0,离职1）
     */
    @Field("status")
    private String status;

    /**
     * 创建日期
     */
    @Field("create_date")
    private String create_date;

    /**
     * 修改日期
     */
    @Field("update_date")
    private String update_date;

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

    public String getStaff_no() {
        return staff_no;
    }

    public void setStaff_no(String staff_no) {
        this.staff_no = staff_no;
    }

    public String getUser_name_cn() {
        return user_name_cn;
    }

    public void setUser_name_cn(String user_name_cn) {
        this.user_name_cn = user_name_cn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIs_spdb() {
        return is_spdb;
    }

    public void setIs_spdb(Boolean is_spdb) {
        this.is_spdb = is_spdb;
    }

    public String getDept_name() {
        return dept_name;
    }

    public void setDept_name(String dept_name) {
        this.dept_name = dept_name;
    }

    public String getCompany_full_name() {
        return company_full_name;
    }

    public void setCompany_full_name(String company_full_name) {
        this.company_full_name = company_full_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getUpdate_date() {
        return update_date;
    }

    public void setUpdate_date(String update_date) {
        this.update_date = update_date;
    }
}
