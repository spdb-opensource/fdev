package com.spdb.fdev.freport.spdb.entity.task;

import com.spdb.fdev.freport.spdb.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "task")
@ApiModel(value = "任务表")
@EqualsAndHashCode(callSuper = true)
@Data
public class Task extends BaseEntity {

    @Field("group")
    private String group;

    @Field("name")
    private String name;

    @Field("desc")
    private String desc;

    @Field("start_time")
    private String startTime;

    @Field("start_inner_test_time")
    private String startInnerTestTime;

    @Field("start_uat_test_time")
    private String startUatTestTime;

    @Field("start_rel_test_time")
    private String startRelTestTime;

    @Field("fire_time")
    private String fireTime;

    @Field("rqrmnt_no")
    private String rqrmntNo;

    @Field("master")
    private List<String> master;

    @Field("spdb_master")
    private List<String> spdbMaster;

    @Field("developer")
    private List<String> developer;

    @Field("tester")
    private List<String> tester;

    @Field("stage")
    private String stage;

    @Field("fdev_implement_unit_no")
    private String fdevImplementUnitNo;

    @Field("difficulty_desc")
    private String difficultyDesc;

    @Field("project_id")
    private String projectId;

    @Field("taskType")
    private Integer taskType;//任务类型 0:开发任务;1:无代码任务 2:日常任务


}
