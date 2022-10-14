/**
 * ErrorConstants.java Created on Tue Oct 19 16:44:15 CST 2021
 *
 * Copyright 2004 Client Server International, Inc. All rights reserved.
 * CSII PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.spdb.fdev.fdemand.base.dict;
/**
 * FDEV系统错误字典文件
 * @author: Auto Generated
 */
public class ErrorConstants {
/**  该其他需求任务下存在研发单元无法删除！  */
    public static final String DAILY_ERROR_NOT_DELETE="DAILY001";
/**  研发单元的计划启动、计划完成测试日期不得晚于其他需求任务对应计划日期！  */
    public static final String DAILY_ERROR_PLAN_DATE="DAILY002";
/**  数据不存在!{0}  */
    public static final String DATA_NOT_EXIST="DEMA0171";
/**  数据查询失败！  */
    public static final String DATA_QUERY_ERROR="DEMA0050";
/**   需求评估未完成  */
    public static final String DEMAND_ASSESS_ERROR="DEMA0080";
/**  该需求{0}已分析完成，无法操作  */
    public static final String DEMAND_ASSESS_NOT_OPERATE="DEMA0030";
/**  该需求{0}不是分析中状态，无法确认完成 */
    public static final String DEMAND_ASSESS_NOT_CONFIRM = "DEMA0091";
/**  该需求{0}处于暂缓状态，请更新研发单元内对应计划时间!  */
    public static final String DEMAND_CANNOT_DEFER_ERROR="DEMA0057";
/**  该需求{0}未进入已投产状态，无法归档!  */
    public static final String DEMAND_CANNOT_PRODUCT_ERROR="DEMA0011";
/**  {0},该需求不可撤销  */
    public static final String DEMAND_CANNOT_REPEAL="DEMA0170";
/**  该需求已无法修改!  */
    public static final String DEMAND_CANNOT_UPDATE_ERROR="DEMA0013";
/**  需求无法暂缓，{0}  */
    public static final String DEMAND_DEFER_FAIL="DEMA0173";
/**  需求无法撤销，{0}  */
    public static final String DEMAND_DELETE_FAIL="DEMA0174";
/**  当前需求编号{0}已被使用，请重新申请！  */
    public static final String DEMAND_INFO_ERROR="DEMA0053";
/**  该需求{0}不存在!  */
    public static final String DEMAND_NULL_ERROR="DEMA0007";
/**  {0}不能为空，请联系需求管理员和需求牵头人，在需求编辑页面进行补填！  */
    public static final String DEMAND_PLAN_NO_CANNOT_BE_EMPTY="DEMA0058";
/**  该需求未处于待实施状态前，不允许新增或修改研发单元!  */
    public static final String DEMAND_PREIMPLEMENT_STATUS_ERROR="DEMA0006";
/**  需求无法恢复，{0}  */
    public static final String DEMAND_RECOVER_FAIL="DEMA0176";
/**  该需求处于特殊状态中，不允许新增或修改研发单元!  */
    public static final String DEMAND_SPECIAL_STATUS_ERROR="DEMA0005";
/**  该需求类型-{0}不存在!  */
    public static final String DEMAND_TYPE_ERROR="DEMA0004";
/**  计划提交内测日期不得大于计划提交业测日期  */
    public static final String DEMAN_INNER_TEST_DATE_EMPTY="DEMA0065";
    /**  开始日期不得大于结束日期  */
    public static final String DATE_range_ERROR="DEMA0092";
/**  计划启动日期不得大于计划提交内测日期  */
    public static final String DEMAN_START_DATE_EMPTY="DEMA0064";
/**  计划提交业测日期不得大于计划用户测试完成日期  */
    public static final String DEMAN_TEST_DATE_EMPTY="DEMA0066";
/**  计划用户测试完成日期不得大于计划投产日期   */
    public static final String DEMAN_TEST_FINISH_DATE_EMPTY="DEMA0067";
/**  上传文件失败,请重试!  */
    public static final String DOC_ERROR="DEMA0054";
/**  文档删除失败！  */
    public static final String DOC_ERROR_DELETE="DEMA0052";
/**  文档上传失败！  */
    public static final String DOC_ERROR_UPLOAD="DEMA0051";
/**  研发单元预期公司人员工作量超出实施单元预期公司工作量，请修改后提交！最多可填{0}  */
    public static final String EXPECT_OUT_WORK_ERROE="IPMP0006";
/**  预期公司人员工作量保留两位小数，请修改后提交！  */
    public static final String EXPECT_OUT_WORK_ERROE2="IPMP0009";
/**  研发单元预期行内人员工作量超出实施单元预期行内工作量，请修改后提交！最多可填{0}  */
    public static final String EXPECT_OWN_WORK_ERROR="IPMP0005";
/**  预期行内人员工作量保留两位小数，请修改后提交！  */
    public static final String EXPECT_OWN_WORK_ERROR2="IPMP0008";
/**  研发单元预期行内人员工作量已等于实施单元预期行内工作量,无法新增!  */
    public static final String EXPECT_OWN_WORK_ERROR3="IPMP0010";
/**  实施单元{0}下的研发单元预期工作量之和将超出实施单元对应工作量，请修改研发单元工作量后提交！  */
    public static final String EXPECT_WORK_ERROE="IPMP0007";
/**  暂缓/恢复中需求不允许导出评估表  */
    public static final String EXPORT_ASSESSEXPORT_ERROR="DEMA0082";
/**  调用用户模块失败！  */
    public static final String FAILED_TO_INVOKE_USER_MODULE="DEMA0056";
/**  上传文件超过10M!  */
    public static final String FILE_INFO_SIZE_FILE="DEMA0055";
/**  文件超过5M  */
    public static final String FILE_SIZE_FILE="DEMA0081";
/**  查询 ftask 数据失败!  */
    public static final String FTASK_QUERY_ERROR="FSK0001";
/**  查询 fuser 数据失败!  */
    public static final String FUSER_QUERY_ERROR="FUSER0002";
/**  fuser 用户查询异常，无此用户信息  */
    public static final String FUSER_QUERY_NOTFOUND="FUSER0004";
/**  当前用户权限不足!需要具有 {0} 权限操作  */
    public static final String FUSER_ROLE_ERROR="FUSER0003";
/**  gitlab服务异常！{0}  */
    public static final String GITLAB_SERVER_ERROR="DEMA0002";
/**  研发单元下存在任务无法删除  */
    public static final String IMPLEMENTUNIT_CANNOT_DELETE_ERROR_TOKEN="DEMA0017";
/**  该小组未完成评估，无法补充研发单元!  */
    public static final String IMPLEMENTUNIT_CANNOT_SUPPLY_ERROR="DEMA0020";
/**  该需求处于暂缓中，请先恢复该需求!  */
    public static final String IMPL_DEFER_ERROR="DEMA0015";
/**  对应需求计划编号格式不正确，请重新输入！  */
    public static final String INCORRECT_DATA_FORMAT="DEMA0062";
/**  实施单元状态为已撤销、暂缓、暂存，不允许修改!  */
    public static final String IPMP_ERROR_CANNOT_UPDATE="IPMP0003";
/**  实施单元已延期，请填写实施延期原因分类、实施延期原因。  */
    public static final String IPMP_ERROR_DELAY="IPMP0001";
/**  请重新选择实施单元!  */
    public static final String IPMP_ERROR_NOT_NULL="IPMP0012";
/**  该实施单元非FDEV平台,不允许修改！  */
    public static final String IPMP_ERROR_NOT_UPDATE="IPMP0004";
/**  需向IPMP同步，请填写以下字段实施牵头人、实施牵头团队、项目编号、预期行内人员工作量、预期公司人员工作量。  */
    public static final String IPMP_ERROR_SYNC_NULL="IPMP0002";
/**  牵头人数不可超过10人!  */
    public static final String IPMP_ERROR_LEADER="IPMP0011";
/**  该研发单元已添加到需求，无法删除  */
    public static final String IPMP_TASK_IMPLEMENTUNIT_CANNOT_DELETE="DEMA0200";
/**  该小组已完成评估，无法新建研发单元!  */
    public static final String IMPLEMENTUNIT_CANNOT_CREATE_ERROR="DEMA0012";
/**  该研发单元不允许删除!  */
    public static final String IMPLEMENTUNIT_CANNOT_DELETE_ERROR="DEMA0009";
/**  该研发单元不允许更新!  */
    public static final String IMPLEMENTUNIT_CANNOT_UPDATE_ERROR="DEMA0008";
/**  该小组已完成评估，无法修改研发单元!  */
    public static final String IMPLEMENTUNIT_CANNOT_UPDATE_ERROR2="DEMA0018";
/**  已评估完成的研发单元下有任务，不允许删除！  */
    public static final String IMPLEMENTUNIT_HAVE_TASK_CANNOT_DELETE="DEMA0023";
/**  已挂载且研发单元状态不为评估中的研发单元不允许再挂载！  */
    public static final String IMPLEMENTUNIT_MOUNT_EROOR1="DEMA0024";
/**  已投产、暂缓、暂存、撤销状态的实施单元不允许被挂载！  */
    public static final String IMPLEMENTUNIT_MOUNT_EROOR2="DEMA0025";
/**  实施单元牵头人不是fdev用户，不允许被挂载！  */
    public static final String IMPLEMENTUNIT_MOUNT_EROOR3="DEMA0026";
/**  暂缓中的研发单元，不允许挂载！  */
    public static final String IMPLEMENTUNIT_MOUNT_EROOR4="DEMA0027";
/**  已撤销的研发单元，不允许挂载！  */
    public static final String IMPLEMENTUNIT_MOUNT_EROOR5="DEMA0028";
/**  该研发单元不存在!  */
    public static final String IMPLEMENTUNIT_NULL_ERROR="DEMA0010";
/**  研发单元的计划日期不得晚于实施单元对应计划日期！  */
    public static final String IMPLEMENTUNIT_PLAN_DATE_EROOR="DEMA0029";
/**  所属实施单元状态为已投产、已撤销、暂缓、暂存，不允许修改！  */
    public static final String IMPLEMENTUNIT_SPECIAL_STATUS_CANNOT_UPDATE="DEMA0022";
/**  所选实施单元状态不可为已投产、已撤销、暂缓、暂存状态！  */
    public static final String IMPLEMENTUNIT_SPECIAL_STATUS_ERROR="DEMA0019";
/**  {0}已存在，不允许进行修改！  */
    public static final String NOT_ALLOW_UPDATE="DEMA0068";
/**  请求参数{0}不能同时为空!  */
    public static final String PARAM_CANNOT_BE_BOTH_EMPTY="COMM003";
/**  请求参数{0}不能为空!  */
    public static final String PARAM_CANNOT_BE_EMPTY="COMM002";
/**  请求参数{0}异常!{1}  */
    public static final String PARAM_ERROR="COMM004";
/**  前一个阶段的日期不能晚于后一个阶段的日期  */
    public static final String PLAN_DATE_ERROR="DEMA0021";
/**  该阶段不能进行预评估！  */
    public static final String PRE_APPRAISAL="DEMA0063";
/**  调用任务模块失败，{0}  */
    public static final String QUERY_TASK_FAIL="DEMA0175";
/**  {0} 该板块无法删除  */
    public static final String RELATE_DEL_ERROR="DEMA0180";
/**  该用户{0}无权限执行此操作!  */
    public static final String ROLE_ERROR="DEMA0003";
/**  需求超期邮件主题为空!  */
    public static final String RQRMNT_EMAIL_SUBJECT_BE_EMPTY="DEMA0014";
/**  服务器错误！{0}  */
    public static final String SERVER_ERROR="COMM0000";
/**  开始日期不可比结束日期晚！  */
    public static final String START_END_DATE_ERROR="DEMA0016";
/**  任务修改研发单元失败，{0}  */
    public static final String TASK_UPDATE_IMPLEMENTUNIT_FAIL="DEMA0172";
/**  其他后台服务异常  */
    public static final String THIRD_SERVER_ERROR="COMM001";
/**  {0}不允许其中一个为空！  */
    public static final String TIME_CANNOT_BE_EMPTY="DEMA0059";
/**  用户权限不足!{0}  */
    public static final String UI_VERIFY_USER_ERROR="DEMA0060";
/**  {0}不能为空!请联系需求模块管理员！  */
    public static final String UPLOADER_CANNOT_BE_EMPTY="DEMA0061";
/**  用户认证失败  */
    public static final String USR_AUTH_FAIL="DEMA0001";
/**  上传文件不能超过20M!{0}  */
    public static final String FILE_INFO_SIZE_ERROR="FILE0001";
/**  以下研发单元需要补充申请原因后，提交审批，方能评估完成。谢谢!{0}  */
    public static final String FDEV_UNIT_ERROR="UNIT0001";
    /**  该定稿日期修改审批{0}不存在!  */
    public static final String FINAL_DATE_APPROVE_NULL="APPR0001";
    /**  存在定稿日期申请已审批，禁止修改状态!  */
    public static final String FINAL_DATE_APPROVE_FORBID="APPR0002";
    /**  缺少{0}，请在文档库中上传  */
    public static final String DEMAND_FILE_NOT_EXIST="DEMA0100";
    /**  同一提测单下上传文件大小总和不能超过10M!  */
    public static final String FILE_INFO_SIZE_ERROR_10="FILE0002";

}
