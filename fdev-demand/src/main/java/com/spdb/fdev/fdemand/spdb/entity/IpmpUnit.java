package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Document(collection = Dict.IPMP_UNIT)
public class IpmpUnit {


    @Id
    private ObjectId _id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    @Field("id")
    private String id;

    /** 需求信息单标题 */
    private String informationTitle;

    /** 需求书编号 */
    private String bookNum;

    /** 需求书名称 */
    private String bookName;

    /** 需求提出部门 */
    private String infoSubmitDept;

    /** 实施单元跟踪人 */
    private String unitTrackerName;

    /** 需求信息单我部收件日期 */
    private String receiveDate;

    /** 需求计划名称 */
    private String dpName;

    /** 对应需求计划编号 */
    private String dpNum;

    /** 需求信息单编号 */
    @Indexed(unique = false)
    private String informationNum;

    /** IPMP实施单元编号 */
    @Indexed(unique = true)
    private String implUnitNum;

    /** 实施状态 */
    private String implStatusName;

    /** 实施单元内容 */
    private String implContent;

    /** 实施牵头人中文姓名 */
    private String implLeaderName;

    /** 实施牵头人域账号 */
    private String implLeader;

    /** 实施牵头人信息 */
    @Transient
    private List<UserInfo> implLeaderInfo;

    /** 牵头单位ID */
    private String headerUnit;

    /** 牵头单位 */
    private String headerUnitName;

    /** 牵头团队ID */
    private String headerTeam;

    /** 牵头团队 */
    private String headerTeamName;

    /** 计划启动开发日期 */
    private String planDevelopDate;

    /** 计划提交用户测试日期 */
    private String planTestStartDate;

    /** 计划用户测试完成日期 */
    private String planTestFinishDate;

    /** 计划投产日期 */
    private String planProductDate;

    /** 涉及系统名称 */
    private String relateSysName;

    /** 涉及系统编号 */
    private String relateSysCode;

    /** 行内人员实际工作量（非功能性）（人月） */
    private Double actualOwnUfworkload;

    /** 公司人员实际工作量（非功能性）（人月） */
    private Double actualOutUfworkload;

    /** 行内人员实际工作量（功能性）（人月） */
    private Double actualOwnFworkload;

    /** 公司人员实际工作量（功能性）（人月） */
    private Double actualOutFworkload;

    /** 实际启动开发日期 */
    private String acturalDevelopDate;

    /** 实际提交用户测试日期 */
    private String acturalTestStartDate;

    /** 实际用户测试完成日期 */
    private String acturalTestFinishDate;

    /** 实际投产日期 */
    private String acturalProductDate;

    /** 预期行内人员工作量 */
    private Double expectOwnWorkload;

    /** 预期公司人员工作量 */
    private Double expectOutWorkload;

    /** 拟纳入项目名称 */
    private String planPrjName;

    /** 项目编号 */
    private String prjNum;

    /** 测试牵头人中文姓名 */
    private String testLeaderName;

    /** 测试牵头人域账号 */
    private String testLeader;

    /** 测试牵头人邮箱 */
    private String testLeaderEmail;

    /** 实施延期原因分类Type */
    private String implDelayType;

    /** 实施延期原因分类 */
    private String implDelayTypeName;

    /** 实施延期原因 */
    private String implDelayReason;

    /** 实际启动开发延期邮件 是否有发送过邮件 1 = 已发送 */
    private String developDelayEmail;

    /** 实际提交用户测试延期邮件 */
    private String testStartDelayEmail;

    /** 实际用户测试完成延期邮件 */
    private String testFinishDelayEmail;

    /** 实际投产延期邮件 */
    private String productDelayEmail;

    /** 计划提交内测日期 */
    private String planInnerTestDate;

    /** 实际提交内测日期 */
    private String actualInnerTestDate;

    /** 牵头小组 */
    private String leaderGroup;

    /** 牵头小组中文名 */
    @Transient
    private String leaderGroupName;

    /** 牵头人标识 1 都是FDEV 2 部分是FDEV 3 不是FDEV */
    private String leaderFlag;

    /** 是否已同步IPMP 1为同步 */
    private String syncFlag;

    /** 流水线标识 为空或等于 ZH-0748 可编辑  stockUnit=存量实施单元不可编辑 */
    private String usedSysCode;

    /** implunit.dev.mode.01  敏态；implunit.dev.mode.02  稳态 */
    private String unitDevMode;

    /** 是否展示向IPMP同步按钮 true展示*/
    @Transient
    private Boolean isShowSync;

    /** 实施单元是否可选 */
    @Transient
    private String unitText;

    /** 测试实施部门 ​1-测试服务中心  2-业务部门（含分行）3-开发项目组  4- 无需测试  5-业务部门（辅助资源） */
    private String testImplDeptDemdKey;

    /** 测试实施部门名称 */
    private String testImplDeptName;

    /** 需求id */
    @Transient
    private String demandId;

    /** 实施单元提交时间（需求平台同步字段） */
    private String unitSubmitDate;

    /** 实施单元首次同步时间*/
    private String createTime;

    /** 实施单元同步时间 */
    private String updateTextDate;

    /** 计划启动开发日期（调） */
    private String planDevelopDateAdjust;

    /** 计划提交用户测试日期（调） */
    private String planTestStartDateAdjust;

    /** 计划用户测试完成日期（调） */
    private String planTestFinishDateAdjust;

    /** 计划投产日期（调） */
    private String planProductDateAdjust;

    /** fdev实施计划变更原因分类 */
    private List<String> implChangeType;

    /** fdev实施计划变更原因分类 （中文值）*/
    @Transient
    private List<String> implChangeTypeName;

    /** fdev实施计划变更原因 */
    private String implChangeReason;

    /** 确认延期阶段 */
    //developDelay开发延期、testStartDelay提交用户测试延期、testFinishDelay用户测试结束延期、productDelay投产延期
    private List<String> confirmDelayStage;

    /** 页面确认延期展示选项 */
    //developDelay开发延期、testStartDelay提交用户测试延期、testFinishDelay用户测试结束延期、productDelay投产延期
    @Transient
    private List<String> confirmDelayItem;

    /** 业务确认邮件名称 */
    private List<Map<String, String>> businessEmail;//包含业务确认邮件名称businessEmailName和业务确认邮件地址businessEmailPath

    /** 调整排期按钮，0亮，1置灰（仅本实施单元牵头人可操作） */
    private String adjustDateButton;

    /** 确认延期按钮，0亮，1置灰（仅本实施单元牵头人可操作），2置灰（本实施单元暂未申请调整排期） */
    private String confirmDelayButton;

    public String getHeaderUnit() {
        return headerUnit;
    }

    public void setHeaderUnit(String headerUnit) {
        this.headerUnit = headerUnit;
    }

    public String getHeaderTeam() {
        return headerTeam;
    }

    public void setHeaderTeam(String headerTeam) {
        this.headerTeam = headerTeam;
    }

    public String getUnitSubmitDate() {
        return unitSubmitDate;
    }

    public void setUnitSubmitDate(String unitSubmitDate) {
        this.unitSubmitDate = unitSubmitDate;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTextDate() {
        return updateTextDate;
    }

    public void setUpdateTextDate(String updateTextDate) {
        this.updateTextDate = updateTextDate;
    }

    public String getTestImplDeptName() {
        return testImplDeptName;
    }

    public void setTestImplDeptName(String testImplDeptName) {
        this.testImplDeptName = testImplDeptName;
    }

    public String getDemandId() {
        return demandId;
    }

    public void setDemandId(String demandId) {
        this.demandId = demandId;
    }

    public String getTestImplDeptDemdKey() {
        return testImplDeptDemdKey;
    }

    public void setTestImplDeptDemdKey(String testImplDeptDemdKey) {
        this.testImplDeptDemdKey = testImplDeptDemdKey;
    }

    /** 是否上云 implunit.cloud.flag.01 是 implunit.cloud.flag.02 否
     *          implunit.cloud.flag.03 待定 implunit.cloud.flag.04 审核中 */
    private String cloudFlag;

    /** 是否上云转译 */
    private String cloudFlagName;

    /** 技术方案编号id */
    private String techSchemeKey;

    /** 技术方案编号 */
    private String techSchemeNo;

    /** 项目类型 */
    private String projectType;

    /** 审核人（待定） */
    private String checkerUserIds;

    /** 审核人姓名 */
    private String checkerUserNames;

    public String getCloudFlag() {
        return cloudFlag;
    }

    public void setCloudFlag(String cloudFlag) {
        this.cloudFlag = cloudFlag;
    }

    public String getCloudFlagName() {
        return cloudFlagName;
    }

    public void setCloudFlagName(String cloudFlagName) {
        this.cloudFlagName = cloudFlagName;
    }

    public String getTechSchemeKey() {
        return techSchemeKey;
    }

    public void setTechSchemeKey(String techSchemeKey) {
        this.techSchemeKey = techSchemeKey;
    }

    public String getTechSchemeNo() {
        return techSchemeNo;
    }

    public void setTechSchemeNo(String techSchemeNo) {
        this.techSchemeNo = techSchemeNo;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getCheckerUserIds() {
        return checkerUserIds;
    }

    public void setCheckerUserIds(String checkerUserIds) {
        this.checkerUserIds = checkerUserIds;
    }

    public String getCheckerUserNames() {
        return checkerUserNames;
    }

    public void setCheckerUserNames(String checkerUserNames) {
        this.checkerUserNames = checkerUserNames;
    }

    public String getUnitText() {
        return unitText;
    }

    public void setUnitText(String unitText) {
        this.unitText = unitText;
    }

    public Boolean getIsShowSync() {
        return isShowSync;
    }

    public void setIsShowSync(Boolean isShowSync) {
        this.isShowSync = isShowSync;
    }

    public String getUnitDevMode() {
        return unitDevMode;
    }

    public void setUnitDevMode(String unitDevMode) {
        this.unitDevMode = unitDevMode;
    }

    public String getDevelopDelayEmail() {
        return developDelayEmail;
    }

    public void setDevelopDelayEmail(String developDelayEmail) {
        this.developDelayEmail = developDelayEmail;
    }

    public String getTestStartDelayEmail() {
        return testStartDelayEmail;
    }

    public void setTestStartDelayEmail(String testStartDelayEmail) {
        this.testStartDelayEmail = testStartDelayEmail;
    }

    public String getTestFinishDelayEmail() {
        return testFinishDelayEmail;
    }

    public void setTestFinishDelayEmail(String testFinishDelayEmail) {
        this.testFinishDelayEmail = testFinishDelayEmail;
    }

    public String getProductDelayEmail() {
        return productDelayEmail;
    }

    public void setProductDelayEmail(String productDelayEmail) {
        this.productDelayEmail = productDelayEmail;
    }

    public String getImplDelayType() {
        return implDelayType;
    }

    public void setImplDelayType(String implDelayType) {
        this.implDelayType = implDelayType;
    }

    public String getLeaderGroupName() {
        return leaderGroupName;
    }

    public void setLeaderGroupName(String leaderGroupName) {
        this.leaderGroupName = leaderGroupName;
    }

    public String getUsedSysCode() {
        return usedSysCode;
    }

    public void setUsedSysCode(String usedSysCode) {
        this.usedSysCode = usedSysCode;
    }

    public List<UserInfo> getImplLeaderInfo() {
        return implLeaderInfo;
    }

    public void setImplLeaderInfo(List<UserInfo> implLeaderInfo) {
        this.implLeaderInfo = implLeaderInfo;
    }

    public String getInformationTitle() {
        return informationTitle;
    }

    public void setInformationTitle(String informationTitle) {
        this.informationTitle = informationTitle;
    }

    public String getBookNum() {
        return bookNum;
    }

    public void setBookNum(String bookNum) {
        this.bookNum = bookNum;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getInfoSubmitDept() {
        return infoSubmitDept;
    }

    public void setInfoSubmitDept(String infoSubmitDept) {
        this.infoSubmitDept = infoSubmitDept;
    }

    public String getUnitTrackerName() {
        return unitTrackerName;
    }

    public void setUnitTrackerName(String unitTrackerName) {
        this.unitTrackerName = unitTrackerName;
    }

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }

    public String getDpName() {
        return dpName;
    }

    public void setDpName(String dpName) {
        this.dpName = dpName;
    }

    public String getDpNum() {
        return dpNum;
    }

    public void setDpNum(String dpNum) {
        this.dpNum = dpNum;
    }

    public String getInformationNum() {
        return informationNum;
    }

    public void setInformationNum(String informationNum) {
        this.informationNum = informationNum;
    }

    public String getImplUnitNum() {
        return implUnitNum;
    }

    public void setImplUnitNum(String implUnitNum) {
        this.implUnitNum = implUnitNum;
    }

    public String getImplStatusName() {
        return implStatusName;
    }

    public void setImplStatusName(String implStatusName) {
        this.implStatusName = implStatusName;
    }

    public String getImplContent() {
        return implContent;
    }

    public void setImplContent(String implContent) {
        this.implContent = implContent;
    }

    public String getImplLeaderName() {
        return implLeaderName;
    }

    public void setImplLeaderName(String implLeaderName) {
        this.implLeaderName = implLeaderName;
    }

    public String getImplLeader() {
        return implLeader;
    }

    public void setImplLeader(String implLeader) {
        this.implLeader = implLeader;
    }

    public String getHeaderUnitName() {
        return headerUnitName;
    }

    public void setHeaderUnitName(String headerUnitName) {
        this.headerUnitName = headerUnitName;
    }

    public String getHeaderTeamName() {
        return headerTeamName;
    }

    public void setHeaderTeamName(String headerTeamName) {
        this.headerTeamName = headerTeamName;
    }

    public String getPlanDevelopDate() {
        return planDevelopDate;
    }

    public void setPlanDevelopDate(String planDevelopDate) {
        this.planDevelopDate = planDevelopDate;
    }

    public String getPlanTestStartDate() {
        return planTestStartDate;
    }

    public void setPlanTestStartDate(String planTestStartDate) {
        this.planTestStartDate = planTestStartDate;
    }

    public String getPlanTestFinishDate() {
        return planTestFinishDate;
    }

    public void setPlanTestFinishDate(String planTestFinishDate) {
        this.planTestFinishDate = planTestFinishDate;
    }

    public String getPlanProductDate() {
        return planProductDate;
    }

    public void setPlanProductDate(String planProductDate) {
        this.planProductDate = planProductDate;
    }

    public String getRelateSysName() {
        return relateSysName;
    }

    public void setRelateSysName(String relateSysName) {
        this.relateSysName = relateSysName;
    }

    public String getRelateSysCode() {
        return relateSysCode;
    }

    public void setRelateSysCode(String relateSysCode) {
        this.relateSysCode = relateSysCode;
    }

    public Double getActualOwnUfworkload() {
        return actualOwnUfworkload;
    }

    public void setActualOwnUfworkload(Double actualOwnUfworkload) {
        this.actualOwnUfworkload = actualOwnUfworkload;
    }

    public Double getActualOutUfworkload() {
        return actualOutUfworkload;
    }

    public void setActualOutUfworkload(Double actualOutUfworkload) {
        this.actualOutUfworkload = actualOutUfworkload;
    }

    public Double getActualOwnFworkload() {
        return actualOwnFworkload;
    }

    public void setActualOwnFworkload(Double actualOwnFworkload) {
        this.actualOwnFworkload = actualOwnFworkload;
    }

    public Double getActualOutFworkload() {
        return actualOutFworkload;
    }

    public void setActualOutFworkload(Double actualOutFworkload) {
        this.actualOutFworkload = actualOutFworkload;
    }

    public String getActuralDevelopDate() {
        return acturalDevelopDate;
    }

    public void setActuralDevelopDate(String acturalDevelopDate) {
        this.acturalDevelopDate = acturalDevelopDate;
    }

    public String getActuralTestStartDate() {
        return acturalTestStartDate;
    }

    public void setActuralTestStartDate(String acturalTestStartDate) {
        this.acturalTestStartDate = acturalTestStartDate;
    }

    public String getActuralTestFinishDate() {
        return acturalTestFinishDate;
    }

    public void setActuralTestFinishDate(String acturalTestFinishDate) {
        this.acturalTestFinishDate = acturalTestFinishDate;
    }

    public String getActuralProductDate() {
        return acturalProductDate;
    }

    public void setActuralProductDate(String acturalProductDate) {
        this.acturalProductDate = acturalProductDate;
    }

    public Double getExpectOwnWorkload() {
        return expectOwnWorkload;
    }

    public void setExpectOwnWorkload(Double expectOwnWorkload) {
        this.expectOwnWorkload = expectOwnWorkload;
    }

    public Double getExpectOutWorkload() {
        return expectOutWorkload;
    }

    public void setExpectOutWorkload(Double expectOutWorkload) {
        this.expectOutWorkload = expectOutWorkload;
    }

    public String getPlanPrjName() {
        return planPrjName;
    }

    public void setPlanPrjName(String planPrjName) {
        this.planPrjName = planPrjName;
    }

    public String getPrjNum() {
        return prjNum;
    }

    public void setPrjNum(String prjNum) {
        this.prjNum = prjNum;
    }

    public String getTestLeaderName() {
        return testLeaderName;
    }

    public void setTestLeaderName(String testLeaderName) {
        this.testLeaderName = testLeaderName;
    }

    public String getTestLeader() {
        return testLeader;
    }

    public void setTestLeader(String testLeader) {
        this.testLeader = testLeader;
    }

    public String getTestLeaderEmail() {
        return testLeaderEmail;
    }

    public void setTestLeaderEmail(String testLeaderEmail) {
        this.testLeaderEmail = testLeaderEmail;
    }

    public String getImplDelayTypeName() {
        return implDelayTypeName;
    }

    public void setImplDelayTypeName(String implDelayTypeName) {
        this.implDelayTypeName = implDelayTypeName;
    }

    public String getImplDelayReason() {
        return implDelayReason;
    }

    public void setImplDelayReason(String implDelayReason) {
        this.implDelayReason = implDelayReason;
    }

    public String getPlanInnerTestDate() {
        return planInnerTestDate;
    }

    public void setPlanInnerTestDate(String planInnerTestDate) {
        this.planInnerTestDate = planInnerTestDate;
    }

    public String getActualInnerTestDate() {
        return actualInnerTestDate;
    }

    public void setActualInnerTestDate(String actualInnerTestDate) {
        this.actualInnerTestDate = actualInnerTestDate;
    }

    public String getLeaderGroup() {
        return leaderGroup;
    }

    public void setLeaderGroup(String leaderGroup) {
        this.leaderGroup = leaderGroup;
    }

    public String getLeaderFlag() {
        return leaderFlag;
    }

    public void setLeaderFlag(String leaderFlag) {
        this.leaderFlag = leaderFlag;
    }

    public String getSyncFlag() {
        return syncFlag;
    }

    public void setSyncFlag(String syncFlag) {
        this.syncFlag = syncFlag;
    }

    public String getPlanDevelopDateAdjust() {
        return planDevelopDateAdjust;
    }

    public void setPlanDevelopDateAdjust(String planDevelopDateAdjust) {
        this.planDevelopDateAdjust = planDevelopDateAdjust;
    }

    public String getPlanTestStartDateAdjust() {
        return planTestStartDateAdjust;
    }

    public void setPlanTestStartDateAdjust(String planTestStartDateAdjust) {
        this.planTestStartDateAdjust = planTestStartDateAdjust;
    }

    public String getPlanTestFinishDateAdjust() {
        return planTestFinishDateAdjust;
    }

    public void setPlanTestFinishDateAdjust(String planTestFinishDateAdjust) {
        this.planTestFinishDateAdjust = planTestFinishDateAdjust;
    }

    public String getPlanProductDateAdjust() {
        return planProductDateAdjust;
    }

    public void setPlanProductDateAdjust(String planProductDateAdjust) {
        this.planProductDateAdjust = planProductDateAdjust;
    }


    public String getImplChangeReason() {
        return implChangeReason;
    }

    public void setImplChangeReason(String implChangeReason) {
        this.implChangeReason = implChangeReason;
    }

    public List<String> getConfirmDelayStage() {
        return confirmDelayStage;
    }

    public void setConfirmDelayStage(List<String> confirmDelayStage) {
        this.confirmDelayStage = confirmDelayStage;
    }

    public List<String> getConfirmDelayItem() {
        return confirmDelayItem;
    }

    public void setConfirmDelayItem(List<String> confirmDelayItem) {
        this.confirmDelayItem = confirmDelayItem;
    }

    public List<Map<String, String>> getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(List<Map<String, String>> businessEmail) {
        this.businessEmail = businessEmail;
    }

    public String getAdjustDateButton() {
        return adjustDateButton;
    }

    public void setAdjustDateButton(String adjustDateButton) {
        this.adjustDateButton = adjustDateButton;
    }

    public String getConfirmDelayButton() {
        return confirmDelayButton;
    }

    public void setConfirmDelayButton(String confirmDelayButton) {
        this.confirmDelayButton = confirmDelayButton;
    }

    public List<String> getImplChangeType() {
        return implChangeType;
    }

    public void setImplChangeType(List<String> implChangeType) {
        this.implChangeType = implChangeType;
    }

    public List<String> getImplChangeTypeName() {
        return implChangeTypeName;
    }

    public void setImplChangeTypeName(List<String> implChangeTypeName) {
        this.implChangeTypeName = implChangeTypeName;
    }
}
