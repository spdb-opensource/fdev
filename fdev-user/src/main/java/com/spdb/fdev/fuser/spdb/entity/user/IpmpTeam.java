package com.spdb.fdev.fuser.spdb.entity.user;

import com.spdb.fdev.fuser.base.dict.Constants;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = Constants.IPMP_TEAM)
public class IpmpTeam {
    @Id
    private ObjectId _id;
    /**
     * 单位id
     */
    @Indexed
    @Field("dept_id")
    private String dept_id;
    /**
     * 单位名称
     */
    @Field("dept_name")
    private String dept_name;
    /**
     * 团队id
     */
    @Field("team_id")
    private String team_id;
    /**
     * 团队名称
     */
    @Field("team_name")
    private String team_name;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getDept_id() {
        return dept_id;
    }

    public void setDept_id(String dept_id) {
        this.dept_id = dept_id;
    }

    public String getDept_name() {
        return dept_name;
    }

    public void setDept_name(String dept_name) {
        this.dept_name = dept_name;
    }

    public String getTeam_id() {
        return team_id;
    }

    public void setTeam_id(String team_id) {
        this.team_id = team_id;
    }

    public String getTeam_name() {
        return team_name;
    }

    public void setTeam_name(String team_name) {
        this.team_name = team_name;
    }
}
