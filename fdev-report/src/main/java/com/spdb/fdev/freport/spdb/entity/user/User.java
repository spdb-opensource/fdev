package com.spdb.fdev.freport.spdb.entity.user;

import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.spdb.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Document(collection = EntityDict.USER)
@Data
public class User extends BaseEntity {

    /**
     * 组id
     */
    @Field("group_id")
    private String groupId;

    /**
     * 用户中文名
     */
    @Field("user_name_cn")
    private String userNameCn;

    /**
     * 用户英文名
     */
    @Field("user_name_en")
    private String userNameEn;

    /**
     * git用户名
     */
    @Field("git_user")
    private String gitUser;

    /**
     * 是否在职 0在职 1离职
     */
    @Field("status")
    private String status;

    /**
     * 公司id
     */
    @Field("company_id")
    private String companyId;

    /**
     * email
     */
    @Field("email")
    private String email;

    @Field("labels")
    private List<String> labels;

    @Field("role_id")
    private List<String> roleId;

    @Field("area_id")
    private String areaId;

    private Set<String> groupIds;

}
