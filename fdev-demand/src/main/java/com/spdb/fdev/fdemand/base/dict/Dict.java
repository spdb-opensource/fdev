package com.spdb.fdev.fdemand.base.dict;

/**
 * fdev系统——公共字典文件
 *
 * @author: Auto Generated
 */
public class Dict {
    /**
     * 计数
     */
    public static final String COUNT = "count";
    /**
     * 获取组信息，发task接口
     */
    public static final String GETGROUPS = "getGroups";

    /**
     * 组名称
     */
    public static final String GROUPNAME = "groupName";
    /**
     * 标签
     */
    public static final String LABEL = "label";
    /**
     * 名称
     */
    public static final String NAME = "name";
    /**
     * group信息的父group_id
     */
    public static final String PARENT_ID = "parent_id";

    public static final String FDEMAND = "fdemand";
    /**
     * 请求接口状态码封装
     */
    public static final String REST_CODE = "REST_CODE";

    /**
     * 用户模块查询人员信息
     */
    public static final String QUERYBYUSERCOREDATA = "queryByUserCoreData";

    public static final String QUERYUSERCOREDATA = "queryUserCoreData";

    public static final String QUERYUSER = "queryUser";

    public static final String QUERYCHILDGROUPBYID = "queryChildGroupById";

    public static final String QUERYGROUP = "queryGroup";


    /**
     * 任务模块相关字典
     */

    /**
     * 任务撤销和恢复接口
     */
    public static final String TASKRECOVER = "taskRecover";

    /**
     * 判断任务是否可以暂缓和撤销
     */
    public static final String DEFER_OR_DELETE = "deferOrDelete";

    /**
     * 修改任务，任务状态字段
     */
    public static final String TASK_SPECTIAL_STATUS = "taskSpectialStatus";

    /**
     * 查询需求是否可以撤销
     */
    public static final String IS_DELETE = "isDelete";

    /**
     * 查询需求是否可以暂缓
     */
    public static final String IS_DEFER = "isDefer";

    /**
     * 邮件发送相关
     */
    public static final String SEND_EAMIL = "sendEmail";

    public static final String EMAIL = "email";

    public static final String DemandInfoNotifyImpl = "DemandInfoNotifyImpl";

    public static final String DemandUpdateNotifyImpl = "DemandUpdateNotifyImpl";

    public static final String DemandDeferNotifyImpl = "DemandDeferNotifyImpl";

    public static final String DemandRecoverNotifyImpl = "DemandRecoverNotifyImpl";

    public static final String UiUploadedImpl = "UiUploadedImpl";

    public static final String UiAuditWaitImpl = "UiAuditWaitImpl";

    public static final String UiAuditPassImpl = "UiAuditPassImpl";

    public static final String UiAuditPassNotImpl = "UiAuditPassNotImpl";

    /**
     * 需求暂缓邮件模版
     */
    public static final String FDEMAND_DEFER_FTL = "fdemand_defer.ftl";

    /**
     * 需求录入邮件模版
     */
    public static final String FDEMAND_INFO_FTL = "fdemand_info.ftl";

    /**
     * 需求更新邮件模版
     */
    public static final String FDEMAND_UPDATE_FTL = "fdemand_update.ftl";

    /**
     * 需求恢复邮件模版
     */
    public static final String FDEMAND_RECOVER_FTL = "fdemand_recover.ftl";

    /**
     * Ui设计稿已上传邮件模版
     */
    public static final String FDEMAND_UIUPLOADED_FTL = "fdemand_uiUploaded.ftl";

    /**
     * Ui设计稿待审核邮件模版
     */
    public static final String FDEMAND_UIAUDITWAIT_FTL = "fdemand_uiAuditWait.ftl";

    /**
     * Ui设计稿审核通过邮件模版
     */
    public static final String FDEMAND_UIAUDITPASS_FTL = "fdemand_uiAuditPass.ftl";

    /**
     * Ui设计稿审核未通过邮件模版
     */
    public static final String FDEMAND_UIAUDITPASSNOT_FTL = "fdemand_uiAuditPassNot.ftl";

    /**
     * 邮件发送，延期原因
     */
    public static final String DEFER_REASON = "defer_reason";

    /**
     * 邮件内容
     */
    public static final String EMAIL_CONTENT = "email_content";

    /**
     * 需求地址
     */
    public static final String HISTORY_DEMAND_URL = "history_demand_url";

    /**
     * UI设计稿审核地址
     */
    public static final String DEMAND_UI_URL = "demand_ui_url";

    /**
     * git账号ID
     */
    public static final String GIT_USER_ID = "git_user_id";

    /**
     * ObjectId类型的_id
     */
    public static final String OBJECTID = "_id";


    /**
     * session中获取user
     */
    public static final String _USER = "_USER";

    /**
     * 中文名称
     */
    public static final String NAME_ZH = "name_zh";

    /**
     * 中文名称
     */
    public static final String NAME_CN = "name_cn";

    /**
     * 英文名称
     */
    public static final String NAME_EN = "name_en";

    /**
     * 用户英文名称
     */
    public static final String USER_NAME_EN = "user_name_en";

    /**
     * 用户中文名称
     */
    public static final String USER_NAME_CN = "user_name_cn";

    /**
     * id
     */
    public static final String ID = "id";

    /**
     * id
     */
    public static final String IDS = "ids";

    /**
     * 分页查询每页查询数
     */
    public static final String SIZE = "size";

    /**
     * 分页查询页码
     */
    public static final String INDEX = "index";

    /**
     * 分页模糊搜索匹配字段
     */
    public static final String KEYWORD = "keyword";

    /**
     * 需求基础表
     */
    public static final String DEMAND_BASEINFO = "demand_baseinfo";

    /**
     * 分页查询内容
     */
    public static final String DATA = "data";

    /**
     * 需求说明书名称
     */
    public static final String DEMAND_INSTRUCTION = "demand_instruction";

    /**
     * 需求计划编号
     */
    public static final String DEMAND_PLAN_NO = "demand_plan_no";

    /**
     * 实施单元表
     */
    public static final String FDEV_IMPLEMENT_UNIT = "fdev_implement_unit";

    /**
     * 需求文档表
     */
    public static final String DEMAND_DOC = "demand_doc";

    /**
     * 需求文档类型
     */
    public static final String DEMAND_DOC_TYPE = "doc_type";


    /**
     * 需求种类：主要区分是否是暂缓邮件
     */
    public static final String DEMAND_KIND = "demand_kind";

    /**
     * 任务集表
     */
    public static final String IPMP_TASK = "ipmp_task";

    /**
     * 需求id
     */
    public static final String DEMAND_ID = "demand_id";

    /**
     * 需求类型，业务:business、科技:tech
     */
    public static final String DEMAND_TYPE = "demand_type";

    /**
     * 需求说明
     */
    public static final String DEMAND_DESC = "demand_desc";

    /**
     * 业务需求
     */
    public static final String BUSINESS = "business";

    /**
     * 科技需求
     */
    public static final String TECH = "tech";

    /**
     * 批次编号
     */
    public static final String BATCH_NO = "batch_no";

    /**
     * 任务集编号
     */
    public static final String TASK_NO = "task_no";

    /**
     * 任务集名称
     */
    public static final String TASK_NAME = "task_name";

    /**
     * IPMP实施单元编号
     */
    public static final String IPMP_IMPLEMENT_UNIT_NO = "ipmp_implement_unit_no";

    /**
     * 研发单元内容
     */
    public static final String IMPLEMENT_UNIT_CONTENT = "implement_unit_content";

    /**
     * 实施牵头单位
     */
    public static final String IMPLEMENT_LEAD_DEPT = "implement_lead_dept";

    /**
     * 实施牵头团队
     */
    public static final String IMPLEMENT_LEAD_TEAM = "implement_lead_team";

    /**
     * 所属小组id
     */
    public static final String GROUP = "group";

    /**
     * 所属小组中文
     */
    public static final String GROUP_CN = "group_cn";

    /**
     * 研发单元牵头人姓名
     */
    public static final String IMPLEMENT_LEADER = "implement_leader";

    /**
     * 研发单元牵头人姓名信息
     */
    public static final String IMPLEMENT_LEADER_ALL = "implement_leader_all";

    /**
     * 研发单元牵头人域账号
     */
    public static final String IMPLEMENT_LEADER_ACCOUNT = "implement_leader_account";

    /**
     * 拟纳入项目名称
     */
    public static final String PROJECT_NAME = "project_name";

    /**
     * 业务需求：项目编号，科技需求任务集编号
     */
    public static final String PROJECT_NO = "project_no";

    /**
     * 计划启动开发日期
     */
    public static final String PLAN_START_DATE = "plan_start_date";

    /**
     * 计划提交内测日期
     */
    public static final String PLAN_INNER_TEST_DATE = "plan_inner_test_date";

    /**
     * 计划提交用户测试日期
     */
    public static final String PLAN_TEST_DATE = "plan_test_date";

    /**
     * 计划用户测试完成日期
     */
    public static final String PLAN_TEST_FINISH_DATE = "plan_test_finish_date";

    /**
     * 计划投产日期
     */
    public static final String PLAN_PRODUCT_DATE = "plan_product_date";

    /**
     * 涉及系统名称
     */
    public static final String RELATE_SYSTEM_NAME = "relate_system_name";

    /**
     * 预期我部人员工作量
     */
    public static final String DEPT_WORKLOAD = "dept_workload";

    /**
     * 预期公司人员工作量
     */
    public static final String COMPANY_WORKLOAD = "company_workload";

    /**
     * 备注
     */
    public static final String REMARK = "remark";

    /**
     * 研发单元创建人
     */
    public static final String CREATE_USER = "create_user";

    /**
     * 是否撤销
     */
    public static final String IS_CANCELED = "is_canceled";

    /*
     * OA联系单
     */
    public static final String OA_CONTACT_NAME = "oa_contact_name";
    /*
     * 需求编号
     */
    public static final String OA_CONTACT_NO = "oa_contact_no";
    /*
     *OA联系单编号
     */
    public static final String OA_REAL_NO = "oa_real_no";
    /*
     * OA收件日期
     */
    public static final String OA_RECEIVE_DATE = "oa_receive_date";
    /*
     * 需求提出部门
     */
    public static final String PROPOSE_DEMAND_DEPT = "propose_demand_dept";
    /*
     * 需求联系人
     */
    public static final String PROPOSE_DEMAND_USER = "propose_demand_user";
    /*
     * 需求计划名称
     */
    public static final String DEMAND_PLAN_NAME = "demand_plan_name";
    /*
     * 需求背景
     */
    public static final String DEMAND_BACKGROUND = "demand_background";
    /*
     * 前期沟通情况
     */
    public static final String FORMER_COMMUNICATION = "former_communication";
    /*
     * 需求方期望投产日期
     */
    public static final String RESPECT_PRODUCT_DATE = "respect_product_date";
    /*
     * 需求是否可行
     */
    public static final String DEMAND_AVAILABLE = "demand_available";
    /*
     * 需求评估方式
     */
    public static final String DEMAND_ASSESS_WAY = "demand_assess_way";
    /*
     * 未来是否纳入后评估
     */
    public static final String FUTURE_ASSESS = "future_assess";
    /*
     * 需求可行性评估意见
     */
    public static final String AVAILABLE_ASSESS_IDEA = "available_assess_idea";
    /*
     * 实施团队可行性评估补充意见
     */
    public static final String EXTRA_IDEA = "extra_idea";

    public static final String IPMP_IMPLELMENT_UNIT_NO = "ipmpImplementUnitNo";

    public static final String IPMP_IMPLELMENT_UNIT_CONTENT = "implementUnitContent";

    public static final String GROUP_ID = "groupid";


    /**
     * 创建时间
     */
    public static final String CREATE_TIME = "create_time";

    /**
     * 正常实施单元状态
     */
    public static final String IMPLEMENT_UNIT_STATUS_NORMAL = "implement_unit_status_normal";

    /**
     * 特殊实施单元状态
     */
    public static final String IMPLEMENT_UNIT_STATUS_SPECIAL = "implement_unit_status_special";

    /**
     * 实施单元信息
     */
    public static final String IMPLEMENT_UNIT_INFO = "implement_unit_info";

    /**
     * 任务修改实施单元参数阶段
     */
    public static final String STAGE = "stage";

    /**
     * 实际开始时间
     */
    public static final String REAL_START_DATE = "real_start_date";


    /**
     * 实际提交内测日期
     */
    public static final String REAL_INNER_TEST_DATE = "real_inner_test_date";

    /**
     * 实际提交业测日期
     */
    public static final String REAL_TEST_DATE = "real_test_date";

    /**
     * 实际用户测试完成日期
     */
    public static final String REAL_TEST_FINISH_DATE = "real_test_finish_date";

    /**
     * 实际投产日期
     */
    public static final String REAL_PRODUCT_DATE = "real_product_date";

    /**
     * fdev实施单元编号
     */
    public static final String FDEV_IMPLEMENT_UNIT_NO = "fdev_implement_unit_no";

    /**
     * 暂缓时间
     */
    public static final String DEFER_TIME = "defer_time";

    /**
     * 恢复时间
     */
    public static final String RECOVER_TIME = "recover_time";

    /**
     * 撤销时间
     */
    public static final String DELETE_TIME = "delete_time";

    /**
     * 录入基本信息
     */
    public static final String TASK_STAGE_CREATE_INFO = "create-info";
    /**
     * 录入应用信息
     */
    public static final String TASK_STAGE_CREATE_APP = "create-app";
    /**
     * 录入分支信息
     */
    public static final String TASK_STAGE_CREATE_FEATURE = "create-feature";

    /**
     * 任务开发
     */
    public static final String TASK_STAGE_DEVELOP = "develop";

    /**
     * 任务开发SIT
     */
    public static final String TASK_STAGE_SIT = "sit";

    /**
     * 任务开发UAT
     */
    public static final String TASK_STAGE_UAT = "uat";

    /**
     * 任务开发REL
     */
    public static final String TASK_STAGE_REL = "rel";

    /**
     * 任务开发投产
     */
    public static final String TASK_STAGE_PRODUCTION = "production";

    /**
     * 任务归档
     */
    public static final String TASK_STAGE_FILE = "file";

    /**
     * 小组名称
     */
    public static final String GROUP_NAME = "name";

    /**
     * 玉衡工单入参-fdev实施单元编号
     */
    public static final String UNIT_NO = "unitNo";

    /**
     * 玉衡工单入参-计划开始Sit时间
     */
    public static final String INTERNAL_TEST_START = "internalTestStart";

    /**
     * 玉衡工单入参-计划开始Uat时间
     */
    public static final String INTERNAL_TEST_END = "internalTestEnd";

    /**
     * 玉衡工单入参-计划pro投产时间
     */
    public static final String EXPECTED_PRODUCT_DATE = "expectedProductDate";

    /**
     * 玉衡工单入参-工单备注
     */
    public static final String REQUIRE_REMARK = "requireRemark";

    /**
     * 玉衡工单入参-小组名id
     */
    public static final String GROUP_ID_YH = "group_id";

    /**
     * 玉衡工单入参-小组中文名
     */
    public static final String GROUP_NAME_YUHENG = "group_name";


    /**
     * 规划联系人
     */
    public static final String PLAN_USER = "plan_user";

    /**
     * 需求备案编号
     */
    public static final String DEMAND_RECORD_NO = "demand_record_no";

    /**
     * 评审人
     */
    public static final String REVIEW_USER = "review_user";

    /**
     * 需求总牵头小组
     */
    public static final String DEMAND_LEADER_GROUP = "demand_leader_group";

    /**
     * 需求总牵头小组中文名
     */
    public static final String DEMAND_LEADER_GROUP_CN = "demand_leader_group_cn";

    /**
     * 需求总牵头负责人
     */
    public static final String DEMAND_LEADER = "demand_leader";

    /**
     * 需求总牵头负责人信息
     */
    public static final String DEMAND_LEADER_ALL = "demand_leader_all";
    /**
     * 优先级
     */
    public static final String PRIORITY = "priority";
    /**
     * 受理日期
     */
    public static final String ACCEPT_DATE = "accept_date";

    /**
     * 涉及板块
     */
    public static final String RELATE_PART = "relate_part";
    /**
     * 涉及板块详情
     */
    public static final String RELATE_PART_DETAIL = "relate_part_detail";

    /**
     * 需求属性
     */
    public static final String DEMAND_PROPERTY = "demand_property";

    /**
     * 是否涉及UI审核
     */
    public static final String UI_VERIFY = "ui_verify";

    /**
     * 查询用户ID
     */
    public static final String USERID = "userid";
    /**
     * 需求状态
     */
    public static final String STATES = "states";
    /**
     * 状态天数
     */
    public static final String STATENUM = "stateNum";
    /**
     * 预进行阶段类型
     */
    public static final String FEATURETYPE = "featureType";
    /**
     * 预进行阶段天数
     */
    public static final String FEATURENUM = "featureNum";
    //与我相关
    public static final String RELEVENT = "relevant";
    //小组id
    public static final String GROUPID = "groupid";
    //小组状态
    public static final String GROUPSTATE = "groupState";
    //延期类型
    public static final String DATETYPE = "datetype";
    //延期时间
    public static final String DELAYNUM = "delayNum";
    //排序方式
    public static final String SORTBY = "sortBy";
    //降序
    public static final String DESCENDING = "descending";
    //实际日期类型
    public static final String RELDATETYPE = "relDateType";
    //实际起始日期
    public static final String RELSTARTDATE = "relStartDate";
    //实际结束日期
    public static final String RELENDDATE = "relEndDate";
    //设计稿状态
    public static final String DESIGNSTATE = "designState";
    //设计稿状态
    public static final String DESIGN_STATUS = "design_status";
    //大于日期
    public static final String GTEDATE = "gteDate";
    //小于日期
    public static final String LTEDATE = "lteDate";
    //需求类型
    public static final String DEMANDTYPE = "demandType";
    /**
     * 需求创建人信息
     */
    public static final String DEMAND_CREATE_USER_ALL = "demand_create_user_all";

    /**
     * 组ids数组
     */
    public static final String GROUPIDS = "groupIds";
    /**
     * 是否关联子组
     */
    public static final String ISPARENT = "isParent";
    /**
     * Redis存储组信息key
     */
    public static final String REDISGETGROUPS = "demand.getGroups";
    /**
     * 获取开发信息
     */
    public static final String QUERYDEVRESOURCE = "queryDevResource";

    /**
     * 特殊需求状态
     */
    public static final String DEMAND_STATUS_SPECIAL = "demand_status_special";
    /**
     * 特殊需求状态DEMAND_STATUS_NORMAL
     */
    public static final String DEMAND_STATUS_NORMAL = "demand_status_normal";

    /**
     * 即将到期需求提醒通知邮件模版
     */
    public static final String FDEMAND_EXPIREDMSG = "fdemand_expiredMsg";
    /**
     * 仍然延期的需求告警通知邮件模版
     */
    public static final String FDEMAND_OVERDUEMSG = "fdemand_overdueMsg";
    /**
     * 仍然延期的需求告警通知邮件模版
     */
    public static final String FRQRMNT_OVERDUEMSG_FTL = "fdemand_overdueMsg.ftl";
    /**
     * 开始日期
     */
    public static final String START_DATE = "startDate";
    /**
     * 结束日期
     */
    public static final String END_DATE = "endDate";
    /**
     * 计划开始时间
     */
    public static final String INITSTRING = "initString";
    /**
     * 计划开始时间
     */
    public static final String SITSTRING = "sitString";
    /**
     * 开发即将到期
     */
    public static final String EXPIRED_DEV = "expired-dev";
    /**
     * 提测即将到期
     */
    public static final String EXPIRED_SIT = "expired-sit";
    /**
     * 开发已延期
     */
    public static final String OVERDUE_DEV = "overdue-dev";
    /**
     * 提测已延期
     */
    public static final String OVERDUE_SIT = "overdue-sit";
    /**
     * 需求编号
     */
    public static final String DEMAND_NO = "demandNo";
    /**
     * 需求名称
     */
    public static final String DEMAND_NAME = "demandName";
    /**
     * 拼接用户中文名
     */
    public static final String APPENDUSERCN = "appendUserCn";
    /**
     * 天数
     */
    public static final String COUNTDAY = "countDay";
    /**
     * 时间
     */
    public static final String DATE = "date";
    /**
     * 小组固定接收人
     */
    public static final String GROUPRECEIVER = "groupReceiver";

    /**
     * 需求id,
     */
    public static final String RQRMNTID = "rqrmntId";
    /**
     * 不涉及UI审核
     */
    public static final String NORRLATE = "noRelate";
    /**
     * UI设计稿状态：审核完成
     */
    public static final String COMPLETEDAUDIT = "completedAudit";
    /**
     * UI设计稿状态：异常关闭
     */
    public static final String ABNORMALSHUTDOWN = "abnormalShutdown";
    /**
     * ui设计稿审核人
     */
    public static final String UIDESIGNREPORTER = "uiDesignReporter";
    /**
     * 权限id
     */
    public static final String ROLEID = "role_id";
    /**
     * ui设计稿审核中状态
     */
    public static final String AUDITIN = "auditIn";
    /**
     * 用户英文名
     */
    public static final String USERNAMEEN = "user_name_en";
    /**
     * confluence文档路径
     */
    public static final String DOC_LINK = "doc_link";

    /**
     * 任务模块接收参数
     */
    public static final String DEMANDID = "demandId";

    /**
     * 任务集名称
     */
    public static final String IPMP_IMPLELMENT_NAME = "ipmpImplelmentName";
    /**
     * 日期时间转换
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 状态
     */
    public static final String PARTSTATUS = "partStatus";

    /**
     * 最小阶段
     */
    public static final String MINSTAGE = "minStage";
    /**
     * 开始时间
     */
    public static final String STARTTIME = "startTime";

    /**
     * 结束时间
     */
    public static final String ENDTIME = "endTime";
    /**
     * 互联网条线实施中需求
     */
    public static final String INTIMPINGDEMAND = "intImpingDemand";
    /**
     * 总数
     */
    public static final String TOTAL = "total";

    /**
     * 总数占比
     */
    public static final String TOTALPROP = "totalProp";

    /**
     * 预评估
     */
    public static final String PREEVALUAT = "preEvaluat";
    /**
     * 待实施
     */
    public static final String TOIMP = "toImp";

    /**
     * 开发中
     */
    public static final String DEVELOPING = "developing";
    /**
     * 业测
     */
    public static final String BUSITEST = "busiTest";
    /**
     * 准生产
     */
    public static final String STAGING = "staging";

    /**
     * 需求数
     */
    public static final String DEMANDAMT = "demandAmt";

    /**
     * 紧急需求数
     */
    public static final String URGAMT = "urgAmt";
    /**
     * 开发人员数量
     */
    public static final String CODERAMT = "coderAmt";
    /**
     * 人员负荷
     */
    public static final String STAFFLOAD = "staffLoad";

    /**
     * 需求占比
     */
    public static final String DEMANDPROP = "demandProp";

    /**
     * 业务预评估需求
     */
    public static final String BUSIPREEVALUATEDEMAND = "busiPreEvaluateDemand";
    /**
     * 业务待实施需求
     */
    public static final String BUSITOIMPDEMAND = "busiToImpDemand";

    /**
     * 业务开发中需求
     */
    public static final String BUSIDEVELOPINGDEMAND = "busiDevelopingDemand";
    /**
     * 业务业测需求
     */
    public static final String BUSIBUSIDEMAND = "busiBusiDemand";
    /**
     * 业务准生产需求
     */
    public static final String BUSISTAGINGDEMAND = "busiStagingDemand";
    /**
     * 科技预评估需求
     */
    public static final String KJPREEVALUATEDEMAND = "kjPreEvaluateDemand";

    /**
     * 科技待实施需求
     */
    public static final String KJDTOIMPDEMAND = "kjDToImpDemand";
    /**
     * 科技开发中需求
     */
    public static final String KJDEVELOPINGDEMAND = "kjDevelopingDemand";
    /**
     * 科技业测需求
     */
    public static final String KJBUSITESTDEMAND = "kjBusiTestDemand";

    /**
     * 科技准生产需求
     */
    public static final String KJSTAGINGDEMAND = "kjStagingDemand";

    /**
     * 行内开发
     */
    public static final String INTDEV = "intDev";
    /**
     * 行内开发占比
     */
    public static final String INTDEVPROP = "intDevProp";

    /**
     * 行内测试
     */
    public static final String INTTEST = "intTest";
    /**
     * 行内测试占比
     */
    public static final String INTTESTPROP = "intTestProp";
    /**
     * 行外开发
     */
    public static final String EXTDEV = "extDev";
    /**
     * 行外开发占比
     */
    public static final String EXTDEVPROP = "extDevProp";
    /**
     * 行外测试
     */
    public static final String EXTTEST = "extTest";
    /**
     * 行外测试占比
     */
    public static final String EXTTESTPROP = "extTestProp";
    /**
     * 各组实施需求
     */
    public static final String PREGROUPIMPDEMAND = "preGroupImpDemand";
    /**
     * 零售组实施需求
     */
    public static final String RETAILIMPDEMAND = "retailImpDemand";
    /**
     * 角色实体
     */
    public static final String ROLE = "role";

    /**
     * 查询大组的需求
     */
    public static final String QUERY_GROUP_DEMAND = "queryGroupDemand";

    /**
     * 查询板块的需求
     */
    public static final String QUERY_PART_DEMAND = "queryPartDemand";

    /**
     * 小组id
     */
    public static final String GROUP_ID_TF = "groupId";
    /**
     * 用户状态
     */
    public static final String STATUS = "status";
    /**
     * 零售组各组Ids
     */
    public static final String PARTIDS = "partIds";

    /**
     * ipmp任务集的组id
     */
    public static final String IPMP_GROUP_ID = "ipmpGroupId";

    /**
     * 实施单元补充标识，0为新增，大于0为补充
     */
    public static final String ADD_FLAG = "add_flag";

    /**
     * 板块id
     */
    public static final String PART_ID = "part_id";

    /**
     * 板块名
     */
    public static final String PART_NAME = "part_name";

    /**
     * 板块状态
     */
    public static final String ASSESS_STATUS = "assess_status";

    /**
     * 板块评估人
     */
    public static final String ASSESS_USER = "assess_user";

    /**
     * 板块评估人详情
     */
    public static final String ASSESS_USER_ALL = "assess_user_all";

    /**
     * 需求评估时间
     */
    public static final String DEMAND_ASSESS_DATE = "demand_assess_date";

    /**
     * 可发送邮件标识，yes为可发送邮件，no为不可发送邮件
     */
    public static final String ASSESS_FLAG = "assess_flag";
    /**
     * 消费方系统编号
     */
    public static final String APPNO = "appno";
    /**
     * 请求接口名
     */
    public static final String METHOD = "method";
    public static final String APPKEY = "appKey";
    public static final String ISAUTH = "isAuth";
    public static final String VERSION = "version";
    public static final String COMMON = "common";
    public static final String REQUEST = "request";
    /**
     * 项目/任务集编号
     */
    public static final String PROJECTNO = "projectNo";
    /**
     * 项目/任务集名称
     */
    public static final String PROJECTNAME = "projectName";
    /**
     * 项目/任务集状态
     */
    public static final String PROJECTSTATUSNAME = "projectStatusName";
    /**
     * 创建时间
     */
    public static final String CREATETIME = "createTime";
    /**
     * 新实施单元表
     */
    public static final String IPMP_UNIT = "ipmp_unit";
    /**
     * 需求编号
     */
    public static final String INFORMATIONNUM = "informationNum";
    /**
     * 实施单元编号
     */
    public static final String IMPLUNITNUM = "implUnitNum";
    /**
     * 实施单元编号
     */
    public static final String IPMP_UNIT_NO = "ipmp_unit_no";
    /**
     * 牵头人标识
     */
    public static final String LEADERFLAG = "leaderFlag";
    /**
     * 实施牵头人域账号
     */
    public static final String IMPLLEADER = "implLeader";
    /**
     * 实施牵头团队
     */
    public static final String HEADERTEAMNAME = "headerTeamName";
    /**
     * 测试牵头人中文姓名
     */
    public static final String TESTLEADERNAME = "testLeaderName";
    /**
     * 测试牵头人域账号
     */
    public static final String TESTLEADER = "testLeader";
    /**
     * 测试牵头人邮箱
     */
    public static final String TESTLEADEREMAIL = "testLeaderEmail";
    /**
     * 项目编号
     */
    public static final String PRJNUM = "prjNum";
    /**
     * 预期行内人员工作量
     */
    public static final String EXPECTOWNWORKLOAD = "expectOwnWorkload";
    /**
     * 预期公司人员工作量
     */
    public static final String EXPECTOUTWORKLOAD = "expectOutWorkload";
    /**
     * 实施延期原因分类
     */
    public static final String IMPLDELAYTYPENAME = "implDelayTypeName";
    /**
     * 实施延期原因
     */
    public static final String IMPLDELAYREASON = "implDelayReason";
    /**
     * 牵头小组
     */
    public static final String LEADERGROUP = "leaderGroup";
    /**
     * 计划提交内测时间
     */
    public static final String PLANINNERTESTDATE = "planInnerTestDate";
    /**
     * 是否同步IPMP 1 为同步
     */
    public static final String SYNCFLAG = "syncFlag";
    /**
     * 任务状态
     */
    public static final String TASKSTATUS = "taskStatus";
    /**
     * 实际启动开发日期
     */
    public static final String ACTURALDEVELOPDATE = "acturalDevelopDate";
    /**
     * 实际提交用户测试日期
     */
    public static final String ACTURALTESTSTARTDATE = "acturalTestStartDate";
    /**
     * 实际用户测试完成日期
     */
    public static final String ACTURALTESTFINISHDATE = "acturalTestFinishDate";
    /**
     * 实际投产日期
     */
    public static final String ACTURALPRODUCTDATE = "acturalProductDate";
    /**
     * 用户英文名集合
     */
    public static final String USERNAMEENS = "usernameEns";
    /**
     * 实施单元内容
     */
    public static final String IMPLCONTENT = "implContent";
    /**
     * 实施单元状态
     */
    public static final String IMPLSTATUSNAME = "implStatusName";
    /**
     * 实施单元同步时间
     */
    public static final String IPMPUNIT_UPDATE = "ipmpUnit_update";
    /**
     * 定时同步ipmp实施单元入参1
     */
    public static final String UPDATEDATE = "updateDate";
    /**
     * 定时同步ipmp实施单元入参2
     */
    public static final String UPDATETEXTDATE = "updateTextDate";
    /**
     * result
     */
    public static final Object RESULT = "result";
    /**
     * param
     */
    public static final String PARAM = "param";
    /**
     * 研发单元编号
     */
    public static final String UNITNO = "unitNo";
    /**
     * 暂缓状态，0-正常，1-暂缓
     */
    public static final String TASKSPECTIALSTATUS = "taskSpectialStatus";
    /**
     * 流水号
     */
    public static final String SEQUENCE = "sequence";
    /**
     * 实施延期原因分类
     */
    public static final String IMPLDELAYTYPE = "implDelayType";
    /**
     * 牵头单位
     */
    public static final String HEADERUNITNAME = "headerUnitName";
    /**
     * 实施牵头人中文姓名
     */
    public static final String IMPLLEADERNAME = "implLeaderName";
    /**
     * 拟纳入项目名称
     */
    public static final String PLANPRJNAME = "planPrjName";
    /**
     * 实施牵头团队
     */
    public static final Object HEADERTEAM = "headerTeam";
    /**
     * 用户ID
     */
    public static final String IPMPUSERID = "userId";
    /**
     * 实施单元编号列表
     */
    public static final String IMPLUNITNUMLIST = "implUnitNumList";
    /**
     * 是否是科技需求
     */
    public static final String ISTECH = "isTech";
    /**
     * 厂商或行内的标志
     */
    public static final String USERTYPE = "userType";
    /**
     * 紧急需求标识 空 存量需求 0 紧急需求 1 正常同步
     */
    public static final String DEMAND_FLAG = "demand_flag";
    /**
     * 实施单元日志信息表
     */
    public static final String IPMP_UNIT_ENTITY_LOG = "ipmp_unit_entity_log";
    /**
     * IPMP接口操作日志表
     */
    public static final String IPMP_UNIT_OPERATE_LOG = "ipmp_unit_operate_log";
    /**
     * 返回信息
     */
    public static final String MESSAGE = "message";
    /**
     * 错误编码
     */
    public static final String ERRORCODE = "errorCode";
    /**
     * 研发模式
     */
    public static final String UNITDEVMODE = "unitDevMode";
    /**
     * url
     */
    public static final String URL = "url";
    /**
     * 是否需功能点核算标识 true 需要核算
     */
    public static final String NEEDUFPFLAG = "needUfpFlag";
    /**
     * 是否校验核算
     */
    public static final String ISCHECK = "isCheck";
    /**
     * ipmpurl
     */
    public static final String IPMPUNITURL = "ipmpUnitUrl";
    /**
     * 更新时间
     */
    public static final String UPDATETIME = "updateTime";
    /**
     * 更新接口
     */
    public static final String INTERFACENAME = "interfaceName";
    /**
     * 日常需求
     */
    public static final String DAILY = "daily";
    /**
     * 其他需求任务
     */
    public static final String OTHER_DEMAND_TASK = "other_demand_task";
    /**
     * 任务编号
     */
    public static final String TASKNUM = "taskNum";
    /**
     * 任务名称
     */
    public static final String TASKNAME = "taskName";
    /**
     * 任务类型
     */
    public static final String TASKTYPE = "taskType";
    /**
     * 任务分类
     */
    public static final String TASKCLASSIFY = "taskClassify";
    /**
     * 牵头小组中文名
     */
    public static final String LEADERGROUPNAME = "leaderGroupName";
    /**
     * 任务负责人ID
     */
    public static final String TASKLEADERID = "taskLeaderId";
    /**
     * 任务负责人中文姓名
     */
    public static final String TASKLEADERNAME = "taskLeaderName";
    /**
     * 厂商负责人ID
     */
    public static final String FIRMLEADERID = "firmLeaderId";
    /**
     * 厂商负责人中文姓名
     */
    public static final String FIRMLEADERNAME = "firmLeaderName";
    /**
     * 计划启动日期
     */
    public static final String PLANSTARTDATE = "planStartDate";
    /**
     * 计划完成日期
     */
    public static final String PLANDONEDATE = "planDoneDate";
    /**
     * 实际启动日期
     */
    public static final String ACTUALSTARTDATE = "actualStartDate";
    /**
     * 实际完成日期
     */
    public static final String ACTUALDONEDATE = "actualDoneDate";
    /**
     * 实施单元是否必填
     */
    public static final String DISABLEDTIP = "disabledTip";
    /**
     * 其他需求任务
     */
    public static final String OTHER_DEMAND_TASK_NUM = "other_demand_task_num";
    /**
     * notStart=未开始
     */
    public static final String NOTSTART = "notStart";
    /**
     * going=进行中
     */
    public static final String GOING = "going";
    /**
     * done=已完成
     */
    public static final String DONE = "done";
    /**
     * delete=删除
     */
    public static final String DELETE = "delete";

    /**
     * 需求评估表
     */
    public static final String DEMAND_ASSESS = "demand_assess";


    /**
     * 需求起始评估日期
     */
    public static final String START_ASSESS_DATE = "start_assess_date";

    /**
     * 评估需求状态
     */
    public static final String DEMAND_STATUS = "demand_status";

    /**
     * 评估需求状态
     */
    public static final String CONF_STATE = "conf_state";

    /**
     * 评估需求状态
     */
    public static final String FINAL_DATE = "final_date";


    /**
     * 评估需求 - 超期分类
     */
    public static final String OVERDUE_TYPE = "overdue_type";

    /**
     * 评估需求 - 评估天数
     */
    public static final String ASSESS_DAYS = "assess_days";

    /**
     * 评估需求 - 评估现状
     */
    public static final String ASSESS_PRESENT = "assess_present";

    /**
     * 评估需求状态
     */
    public static final String CONF_URL = "conf_url";

    /**
     * 字典
     */
    public static final String CODE = "code";

    public static final String TYPE = "type";

    /**
     * 评估需求地址
     */
    public static final String ASSESS_DEMAND_URL = "assess_demand_url";

    /**
     * 研发单元编号集合
     */
    public static final String UNITNOS = "unitNos";
    public static final String FINISHED = "finished";
    public static final String NOPASS = "nopass";
    public static final String UNINVOLVED = "uninvolved";
    public static final String REFUSEUNINVOLVED = "refuseUninvolved";
    /**
     * 时间
     */
    public static final String TIME = "time";
    /**
     *
     */
    public static final String FIXING = "fixing";
    public static final String WAIT_ALLOT = "wait_allot";
    public static final String UPLOADED = "uploaded";
    public static final String IRRELEVANT = "irrelevant";
    /**
     * 审核人
     */
    public static final String REVIEWER = "reviewer";
    /**
     * 开始日期
     */
    public static final String STARTDATE = "startDate";
    /**
     * 结束日期
     */
    public static final String ENDDATE = "endDate";
    /**
     * 下载表格的列及文字说明对于关系
     */
    public static final String COLUMNMAP = "columnMap";
    /**
     * 互联网应用的子组id
     */
    public static final String INTERNETCHILDGROUPID = "internetChildGroupId";
    /**
     * 审核通过任务集合
     */
    public static final String FINISHEDLIST = "finishedList";
    /**
     * 未审核通过任务集合
     */
    public static final String UNFINISHEDLIST = "unfinishedList";
    /**
     * 数量
     */
    public static final String NUM = "num";
    /**
     * 第一个日期
     */
    public static final String FIRSTDATE = "firstDate";
    /**
     * 最后一个日期
     */
    public static final String LASTDATE = "lastDate";
    /** 代码审核工单编号 */
    public static final String CODE_ORDER_NO = "code_order_no";
    /** 是否提交代码审核 */
    public static final String ISCODECHECK = "isCodeCheck";
    /**
     * 需求信息单标题
     */
    public static final String INFORMATIONTITLE = "informationTitle";
    /**
     * 需求名称/编号
     */
    public static final String DEMANDKEY = "demandKey";
    /**
     * 实施单元类型 tech--科技；business--业务
     */
    public static final String IMPLUNITTYPE = "implUnitType";
    /**
     * 延期类型
     */
    public static final String DATETYPE1 = "dateType";
    /**
     * 延期类型：计划
     */
    public static final String DATETYPEPLAN = "dateTypePlan";
    /**
     * 延期类型：实际
     */
    public static final String DATETYPEREAL = "dateTypeReal";
    /**
     * 实施单元编号
     */
    public static final String IPMPUNITNO = "ipmpUnitNo";
    /**
     * 其他需求任务编号
     */
    public static final String OTHERDEMANDTASKNUM = "otherDemandTaskNum";
    /**
     * 牵头人
     */
    public static final String USERIDS = "userIds";
    /** 是否查询子组 0 本组 1 本组子组 */
    public static final String GROUPQUERYTYPE = "groupQueryType";
    public static final String DEMANDIDS = "demandIds";
    /** 是否提交代码审核  */
    public static final String ISSUBMITCODEREVIEW = "isSubmitCodeReview";
    /**
     * 流水线标识 ZH-0748=FDEV  stockUnit=存量实施单元不可编辑
     */
    public static final String USEDSYSCODE = "usedSysCode";
    /**
     * 返回数据
     */
    public static final String CONTENT = "content";
    /**
     * 错误信息
     */
    public static final String MSG = "msg";
    /**
     * 实施单元编号
     */
    public static final String IMPLEMENTUNITNO = "implementUnitNo";
    /**
     * 实际提交用户测试日期
     */
    public static final String ACTUALSUBMITTESTDATE = "actualSubmitTestDate";
    /**
     * 实际用户测试完成日期
     */
    public static final String ACTUALTESTFINISHDATE = "actualTestFinishDate";
    /**
     * 实施单元日志信息表
     */
    public static final String XTEST_IPMP_UNIT = "xtest_ipmp_unit";
    /**
     * 同步时间
     */
    public static final String SYNCTIME = "syncTime";
    /** 项目类型 */
    public static final String PROJECTTYPE = "projectType";
    /** 是否上云 implunit.cloud.flag.02 否 implunit.cloud.flag.03 待定 implunit.cloud.flag.01 是 implunit.cloud.flag.04 审核中 */
    public static final String CLOUDFLAG = "cloudFlag";
    /** 是否上云转译 */
    public static final String CLOUDFLAGNAME = "cloudFlagName";
    /** 技术方案编号id */
    public static final String TECHSCHEMEKEY = "techSchemeKey";
    /** 技术方案编号 */
    public static final String TECHSCHEMENO = "techSchemeNo";
    /** 审核人 */
    public static final String CHECKERUSERIDS = "checkerUserIds";
    /** 审核人姓名 */
    public static final String CHECKERUSERNAMES = "checkerUserNames";
    /** 是否上云  */
    public static final String PLANCLOUDFLAG = "planCloudFlag";
    /** 技术方案编号  */
    public static final String PLANTECHSCHEMEKEY = "planTechSchemeKey";
    /** 审核人  */
    public static final String LINECHARGERS = "lineChargers";
    /** 判断计划开始日期超期时间  */
    public static final String STARTOVERDUEDATE = "startOverdueDate";
    /** 判断计划业测日期超期时间  */
    public static final String TESTOVERDUEDATE = "testOverdueDate";
    /** 完成评估时间 为空不校验  */
    public static final String ASSESSDATE = "assessDate";
    /** 研发单元审批表  */
    public static final String FDEV_UNIT_APPROVE = "fdev_unit_approve";
    /** 条线  */
    public static final String SECTION = "section";
    /** 页码 送空查全部  */
    public static final String PAGENUM = "pageNum";
    /** 每页长度  */
    public static final String PAGESIZE = "pageSize";
    /** 条线ID  */
    public static final String SECTIONID = "sectionId";
    /** 审批状态  */
    public static final String APPROVESTATE = "approveState";
    /** 审批列表  */
    public static final String APPROVELIST = "approveList";
    /** 条线中文名  */
    public static final String SECTIONNAMECN = "sectionNameCn";
    /** 审批人ID  */
    public static final String APPROVERID = "approverId";
    /** 审批说明  */
    public static final String APPROVEREASON = "approveReason";
    /** 研发单元编号/内容  */
    public static final String FDEVUNITKEY = "fdevUnitKey";
    /** 牵头人（多选）  */
    public static final String FDEVUNITLEADERIDS = "fdevUnitLeaderIds";
    /** 审批状态（多选） wait=待审批，pass=通过，  reject=拒绝  */
    public static final String APPROVESTATES = "approveStates";
    /** 审批人（多选）  */
    public static final String APPROVERIDS = "approverIds";
    /** 研发单元编号  */
    public static final String FDEVUNITNO = "fdevUnitNo";
    /** 申请审批时间  */
    public static final String APPLYTIME = "applyTime";
    /** 需求编号  */
    public static final String DEMANDNO = "demandNo";
    /** 需求名称  */
    public static final String DEMANDNAME = "demandName";
    /** 研发单元内容  */
    public static final String FDEVUNITNAME = "fdevUnitName";
    /** 拒绝原因  */
    public static final String APPROVEREJECTREASONKEY = "approveRejectReasonKey";
    /** 我的审批Url  */
    public static final String MYAPPROVEURL = "myApproveUrl";
    /** 超期类别  */
    public static final String OVERDUETYPE = "overdueType";
    /** 超期原因  */
    public static final String OVERDUEREASON = "overdueReason";
    /** 审批时间  */
    public static final String APPROVETIME = "approveTime";
    /** 审批拒绝原因  */
    public static final String APPROVEREJECTREASON = "approveRejectReason";
    /** 待审批总数  */
    public static final String WAITCOUNT = "waitCount";
    /** 已完成总数  */
    public static final String DONECOUNT = "doneCount";
    /** 小组层级  */
    public static final String LEVEL = "level";
    /** 开发延期  */
    public static final String DEVELOPDELAY = "developDelay";
    /** 提交用户测试延期  */
    public static final String TESTSTARTDELAY = "testStartDelay";
    /** 用户测试结束延期  */
    public static final String TESTFINISHDELAY = "testFinishDelay";
    /** 投产延期  */
    public static final String PRODUCTDELAY = "productDelay";

    /** 提测单实体  */
    public static final String TEST_ORDER = "test_order";
    /** 提测单附件  */
    public static final String TEST_ORDER_FILE = "test_order_file";
    /** 撤销时间  */
    public static final String DELETED_TIME = "deleted_time";
    /** 修改人员ID  */
    public static final String UPDATE_USER_ID = "update_user_id";
    /** 提交时间  */
    public static final String SUBMIT_TIME = "submit_time";
    /** 修改时间  */
    public static final String UPDATE_TIME = "update_time";
    /** 内测通过情况  */
    public static final String INNER_TEST_RESULT = "inner_test_result";
    /** 是否涉及交易接口改动  */
    public static final String TRANS_INTERFACE_CHANGE = "trans_interface_change";
    /** 是否涉及数据库改动  */
    public static final String DATABASE_CHANGE = "database_change";
    /** 是否涉及回归测试  */
    public static final String REGRESS_TEST = "regress_test";
    /** 是否涉及客户端更新  */
    public static final String CLIENT_CHANGE = "client_change";
    /** 测试环境  */
    public static final String TEST_ENVIRONMENT = "test_environment";
    /** 任务开始UAT时间  */
    public static final String START_UAT_TEST_TIME = "start_uat_test_time";
    /** 需规文件列表 */
    public static final String REQUIREMENTSPECIFICATION = "requirementSpecification";
    /** 需求说明书文件列表 */
    public static final String DEMANDINSTRUCTIONBOOK = "demandInstructionBook";
    /** 其他文件 */
    public static final String OTHERFILES = "otherFiles";
    /** 研发单元列表 */
    public static final String FDEVUNITNOS ="fdevUnitNos";
    /** 任务编号 */
    public static final String TASKNO = "taskNo";
    /** 工单类型 function-功能测试工单，security-安全测试工单，默认查功能测试工单 */
    public static final String ORDERTYPE = "orderType";
    /** 未内测、部分内测通过、内测通过、内业测并行 */
    public static final String INNERTESTTAB = "innerTestTab";
    /** 为空可被选择 ，不为空不可被选中，展示话术 */
    public static final String ISOPTION = "isOption";
    /** 工单状态 */
    public static final String STAGESTATUS = "stageStatus";
    /** 提测单实体 */
    public static final String TESTORDER = "testOrder";
    /** 测试日报抄送 */
    public static final String DAILY_CC_USER_NAME = "daily_cc_user_name";
    /** 测试日报抄送 */
    public static final String DAILY_CC_USER_INFO = "daily_cc_user_info";
    /** 实施单元列表 */
    public static final String IMPL_UNIT_NUM = "impl_unit_num";
    /** 归档时间 */
    public static final String FILE_TIME = "file_time";
    /** 文档 */
    public static final String DOC = "doc";
    /** 路径 */
    public static final String PATH = "path";
    /** 需求编号 */
    public static final String RIM_REQ_NO = "rim_req_no";
    /** 需求测试信息 */
    public static final String TECH_REQ_TEST_INFO = "tech_req_test_info";
    /** 云测试平台测试完成时间 */
    public static final String RIM_FILL_ACTUAL_TEST_FINISH_DATE = "rim_fill_actual_test_finish_date";
    /** 工单编号 */
    public static final String WORKNO = "workNo";
    /** 云测试平台链接 */
    public static final String X_TEST_URL = "x_test_url";
    /** 需求标签 */
    public static final String DEMAND_LABEL = "demand_label";
    /** 需求标签 */
    public static final String DEMANDLABEL = "demandLabel";
    /** flag */
    public static final String FLAG = "flag";
    /** 颜色 */
    public static final String COLOR = "color";
    /** 审批类型 */
    public static final String APPROVETYPE = "approveType";
    /** 实施单元状态列表 */
    public static final String IMPLSTATUSNAMELIST = "implStatusNameList";
    /** 应用id */
    public static final String PROJECT_ID = "project_id";
    /** 应用所属系统 */
    public static final String SYSTEM = "system";
    /** 应用所属系统名称 */
    public static final String SYSTEMNAME = "systemName";
    /** 测试内容 */
    public static final String TEST_CONTENT = "test_content";

    /**
     * 定稿日期修改审核表
     */
    public static final String FDEV_FINAL_DATE_APPROVE = "fdev_final_date_approve";

    public static final String ACCESS_ID = "access_id";

    public static final String APPLY_REASON = "apply_reason";

    public static final String APPLY_USER_ID = "apply_user_id";

    public static final String SECTION_ID = "section_id";

    public static final String APPLY_UPDATE_TIME = "apply_update_time";

    public static final String OPERATE_USER_ID = "operate_user_id";

    public static final String OPERATE_TIME = "operate_time";

    public static final String OPERATE_STATUS = "operate_status";

    public static final String STATE = "state";

    public static final String UNDETERMINED = "undetermined"; // 待审批

    public static final String AGREE = "agree";  // 同意

    public static final String DISAGREE = "disagree"; // 拒绝
    /** 客户端下载地址 */
    public static final String CLIENT_DOWNLOAD = "client_download";
    /** 提测用户 */
    public static final String SUBMIT_USER = "submit_user";
    /** 提测用户信息 */
    public static final String TEST_USER_INFO = "test_user_info";

    /**  gitlab_user_token  */
    public static final String PRIVATE_TOKEN = "Private-Token";
    /**  projects  */
    public static final String PROJECTS = "projects";
    /**  分支  */
    public static final String BRANCH = "branch";
    /**  提交说明  */
    public static final String COMMIT_MESSAGE = "commit_message";
    /**  action  */
    public static final String ACTION = "action";
    /**  文件路径  */
    public static final String FILE_PATH = "file_path";
    /**  编码  */
    public static final String ENCODING = "encoding";
    /**  actions  */
    public static final String ACTIONS = "actions";
    /**  gitlab_user_token_low  */
    public static final String PRIVATE_TOKEN_L="private_token";
    public static final String IMPLUNITRELATSPFLAG = "implUnitRelatSpFlag";
}
