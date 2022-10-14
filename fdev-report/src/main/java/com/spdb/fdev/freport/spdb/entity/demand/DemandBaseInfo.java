package com.spdb.fdev.freport.spdb.entity.demand;

import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.spdb.entity.BaseEntity;
import com.spdb.fdev.freport.spdb.entity.user.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Component
@Document(collection = EntityDict.DEMAND_BASEINFO)
@Data
public class DemandBaseInfo extends BaseEntity {

    /**
     * 需求类型
     */
    @Field("demand_type")
    private String demandType;

    /**
     * 正常需求状态
     */
    @Field("demand_status_normal")
    private Integer demandStatusNormal;

    /**
     * 特殊需求状态
     */
    @Field("demand_status_special")
    private Integer demandStatusSpecial;

    /**
     * 需求编号
     */
    @Field("oa_contact_no")
    private String oaContactNo;

    /**
     * OA联系单编号
     */
    @Field("oa_real_no")
    private String oaRealNo;

    /**
     * OA联系单名称
     */
    @Field("oa_contact_name")
    private String oaContactName;

    /**
     * OA收件日期
     */
    @Field("oa_receive_date")
    private String oaReceiveDate;

    /**
     * 需求提出部门
     */
    @Field("propose_demand_dept")
    private String proposeDemandDept;

    /**
     * 需求联系人
     */
    @Field("propose_demand_user")
    private String proposeDemandUser;

    /**
     * 规划联系人
     */
    @Field("plan_user")
    private String planUser;

    /**
     * 需求说明书名称
     * 对应科技需求的需求名称
     */
    @Field("demand_instruction")
    private String demandInstruction;

    /**
     * 需求计划编号
     * 对应科技需求的计划编号
     */
    @Field("demand_plan_no")
    private String demandPlanNo;

    /**
     * 需求计划名称
     */
    @Field("demand_plan_name")
    private String demandPlanName;

    /**
     * 需求背景
     */
    @Field("demand_background")
    private String demandBackground;

    /**
     * 前期沟通情况
     */
    @Field("former_communication")
    private String formerCommunication;

    /**
     * 需求是否可行
     */
    @Field("demand_available")
    private String demandAvailable;

    /**
     * 需求评估方式
     */
    @Field("demand_assess_way")
    private String demandAssessWay;

    /**
     * 需求备案编号
     */
    @Field("demand_record_no")
    private String demandRecordNo;

    /**
     * 未来是否纳入后评估
     */
    @Field("future_assess")
    private String futureAssess;

    /**
     * 评审人
     */
    @Field("review_user")
    private String reviewUser;

    /**
     * 需求方期望投产日期
     */
    @Field("respect_product_date")
    private String respectProductDate;

    /**
     * 需求可行性评估意见
     */
    @Field("available_assess_idea")
    private String availableAssessIdea;

    /**
     * 是否涉及UI审核
     */
    @Field("ui_verify")
    private Boolean uiVerify;

    /**
     * 是否通过审核
     */
    @Field("is_verify")
    private Boolean isVerify;

    /**
     * 设计稿审核人
     */
    @Field("ui_verify_user")
    private String uiVerifyUser;

    /**
     * 设计稿审核状态,不涉及：noRelate、未上传：uploadNot、已上传：uploaded、待审核：auditWait、审核中：auditIn、审核通过：auditPass、审核不通过：auditPassNot、审核完成：completedAudit
     */
    @Field("design_status")
    private String designStatus;

    /**
     * 需求说明
     */
    @Field("demand_desc")
    private String demandDesc;

    /**
     * 实施团队可行性评估补充意见
     */
    @Field("extra_idea")
    private String extraIdea;

    /**
     * 需求创建人
     */
    @Field("demand_create_user")
    private String demandCreateUser;

    /**
     * 需求创建人全部信息
     */
    @Field("demand_create_user_all")
    private User demandCreateUserAll;

    /**
     * 需求创建时间
     */
    @Field("demand_create_time")
    private String demandCreateTime;

    /**
     * 需求评估时间
     */
    @Field("demand_assess_date")
    private String demandAssessDate;

    /**
     * 需求删除时间
     */
    @Field("demand_delete_time")
    private String demandDeleteTime;

    /**
     * 需求暂缓时间
     */
    @Field("demand_defer_time")
    private String demandDeferTime;

    /**
     * 需求恢复时间
     */
    @Field("demand_recover_time")
    private String demandRecoverTime;

    /**
     * 需求总牵头
     */
    @Field("demand_leader_group")
    private String demandLeaderGroup;
    /**
     * 需求总牵头小组中文名
     */
    @Field("demand_leader_group_cn")
    private String demandLeaderGroupCn;

    /**
     * 需求总牵头负责人
     */
    @Field("demand_leader")
    private Set<String> demandLeader;

    /**
     * 需求总牵头负责人信息
     */
    @Field("demand_leader_all")
    private List<User> demandLeaderAll;

    /**
     * 优先级
     */
    @Field("priority")
    private String priority;

    /**
     * 受理日期
     */
    @Field("accept_date")
    private String acceptDate;

    /**
     * 计划启动日期
     */
    @Field("plan_start_date")
    private String planStartDate;

    /**
     * 计划提交内测日期
     */
    @Field("plan_inner_test_date")
    private String planInnerTestDate;

    /**
     * 计划提交业测日期
     */
    @Field("plan_test_date")
    private String planTestDate;

    /**
     * 计划用户测试完成日期
     */
    @Field("plan_test_finish_date")
    private String planTestFinishDate;

    /**
     * 计划投产日期
     */
    @Field("plan_product_date")
    private String planProductDate;

    /**
     * 实际启动日期
     */
    @Field("real_start_date")
    private String realStartDate;

    /**
     * 实际提交内测日期
     */
    @Field("real_inner_test_date")
    private String realInnerTestDate;

    /**
     * 实际提交业测日期
     */
    @Field("real_test_date")
    private String realTestDate;

    /**
     * 实际用户测试完成日期
     */
    @Field("real_test_finish_date")
    private String realTestFinishDate;

    /**
     * 实际投产日期
     */
    @Field("real_product_date")
    private String realProductDate;

    /**
     * 涉及板块
     */
    @Field("relate_part")
    private Set<String> relatePart;

    /**
     * 是否撤销
     */
    @Field("is_canceled")
    private Boolean isCanceled;

    /**
     * 设计还原审核存储信息
     */
    @Field("designMap")
    private Map<String, List<Map<String, String>>> designMap;
    /**
     * UI上传者
     */
    @Field("uploader")
    private String uploader;

    /**
     * 第一次UI上传者
     */
    @Field("firstUploader")
    private String firstUploader;

    /**
     * 审核意见
     */
    @Field("designRemark")
    private String designRemark;

    /**
     * 是否原需求模块迁移数据，老数据设置为1，新数据不存
     */
    @Field("isTransferRqrmnt")
    private String isTransferRqrmnt;

    /**
     * 可发送邮件标识，yes为可发送邮件，no为不可发送邮件
     */
    @Field("assess_flag")
    private String assessFlag;

    /**
     * 预期我部人员工作
     */
    @Field("dept_workload")
    private Double deptWorkload;

    /**
     * 预期公司人员工作量
     */
    @Field("company_workload")
    private Double companyWorkload;

}