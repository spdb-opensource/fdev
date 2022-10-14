package com.gotest.dict;

public class Constants {

    //交易失败返回码
    public static final String I_FAILED = "00000";
    //expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
    public static final String YYYYMMDD = "yyyyMMdd";

    public static final String YYYYMMDD_1 = "yyyy/MM/dd";

    public static final String YYYYMMDD_2 = "yyyy-MM-dd";
    //默认页面大小 0
    public static final Integer PAGESIZE_DEF = 0;
    //默认当前页 1
    public static final Integer CURRENTPAGE_DEF = 1;
    //消息id默认值
    public static final String MESSAGE_ID_DEF = "-1";
    // 组员们
    // get 失败 默认值
    public static final String FAIL_GET = "";
    //邮件 更新工单 key
    public static final String UPDATETASK = "email.task.updatetask";

    public static final String NUMBER_0 = "0";

    public static final String NUMBER_1 = "1";
    //工单状态默认
    public static final String DEFAULTSTAGE = "0,1,2,3,5,6,7,8,9,10";

    public static final String PROPORTION= "proportion";

    public static final String VALID_COUNT_MANTIS= "validCountMantis";

    public static final String GROUP_ID= "group_id";

    public static final String COUNT_MANTIS= "countMantis";

    public static final String RQRMNTNAME= "rqrmntName";

    public static final String SUMMANTIS = "sumMantis";//工单下的缺陷总数

    public static final String CLOSEDMANTIS = "closedMantis";//工单下已关闭的缺陷总数

    public static final String REJECTEDMANTIS = "rejectedMantis";//工单下已确认拒绝的缺陷总数

    public static final String UNHANDLEDMANTIS = "unhandledMantis";//工单下未处理的缺陷总数

    public static final String COUNT = "count";//计数

    public static final String CASENOEXECUTE = "caseNoExecute";//未执行案例数

    public static final String ORDERNAMEORNO = "orderNameOrNo";//工单名或编号

    public static final String SUMPASS = "sumPass";// 案例通过数

    public static final String SUMUSELESS = "sumUseless";// 无效案例数

    public static final String PARENTID = "parent_id";//父组id

    public static final String FTMSGROUPID = "ftmsGroupId";//玉衡组id

    public static final String FDEVGROUPID = "fdevGroupId";//fdev组id
    
    public static final String STAGECNNAME = "stageCnName";//计数

    public static final String RISKDESCRIPTION = "riskDescription";//风险描述

    public static final String REASON = "reason";//原因

    public static final String TYPE = "type";//消息类型
    
    public static final String TARGET = "target";//通知对象集合
    
    public static final String WORKMANAGEREN = "workManagerEn";//分配工单管理员英文名
    
    public static final String GROUPLEADEREN = "groupLeaderEn";//分配到的组长英文名

    public static final String TESTERSEN = "testersEn";//分配到的组长人员英文名

    public static final String CHILDGROUPFLAG = "childGroupFlag";//是否包含子组标志

    public static final String ROLLBACKOPR = "rollbackOpr";//测试打回执行人

    public static final String DETAIL = "detail";//详情

    public static final String FSTSITDATE = "fstSitDate";//首次提测时间

    public static final String FNLROLLBACKDATE = "fnlRollbackDate";//最新打回时间

    public static final String ROLLBACKNUM = "rollbackNum";//打回次数

    public static final String DEVELOPERNAMEEN = "developerNameEn";//开发人员英文名
    public static final Integer DEFAULT_0 = 0;
    public static final Integer DEFAULT_1 = 1;
    //工单UAT提测状态

    public static final String TESTERNAME = "testerName";

    public static final String TESTCASENAME = "testcaseName";//案例名称

    public static final String EXPECTEDRESULT = "expectedResult";//案例预期结果

    public static final String TESTCASES = "testcases";//任务下的案例集合

    public static final String FROMTESTCASENO = "fromTestcaseNo";//案例迁移的案例来源标号

    public static final String FROMPLANID = "fromPlanId";//计划迁移的计划来源标号

    public static final String TOWORKNO = "toWorkNo";//计划迁移的目标工单编号

    public static final String TOPLANID = "toPlanId";//案例迁移的目标计划编号

    public static final String MOVETYPE = "moveType";//工单迁移类型：1为计划迁移,否则就是案例迁移
    /**
     * 打回原因映射值
     */
    public static final String REASON_1 = "文档不规范";
    public static final String REASON_2 = "文档缺失";
    public static final String REASON_3 = "冒烟测试不通过";
    //dateFormat yyyy-mm-dd HH:mm:ss
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**  用户验证  */
    public static final String AUTHORIZATION="Authorization";
    /**  自定义字段  */
    public static final String CUSTOM_FIELDS="custom_fields";
    /**  参数  */
    public static final String FIELD="field";
    /** 测试原因 */
    public static final String TEST_REASON_1="正常";
    public static final String TEST_REASON_2="缺陷";
    public static final String TEST_REASON_3="需求变更";
    //属于开发问题的缺陷来源
    public static final String DEVELOPMANTIS = "需规问题,功能实现不完整,功能实现错误";
    //生产问题开发相关
    public static final String DEVPROISSUE = "开发";
    //fdev任务需求类文档
    public static final String RQRMNTTYPE = "需求类";
    //fdev需求需求类文档
    public static final String RQRMNTFILE = "需求说明书,需求规格说明书";
    //生产问题内测相关
    public static final String SITPROISSUE = "内测";
    //需求说明书
    public static final String RQRMNTDOC = "需求说明书";
    //需求规格说明书
    public static final String RQRMNTRULE = "需求规格说明书";
    //其他相关材料
    public static final String OTHERDOC = "其他相关材料";
    //正常实施单元的状态为sit
    public static final Integer UNIT_IN_SIT = 4;
    //严重缺陷编号
    public static final String severeMantisCode = "60,70,80";
    //优先级高编号
    public static final String priorityMantisCode = "40,50,60";
    /** 工单类型-功能测试工单 */
    public static final String ORDERTYPE_FUNCTION = "function";
    /** 工单类型-安全测试工单 */
    public static final String ORDERTYPE_SECURITY = "security";
    /** 工单类型-全部 */
    public static final String ORDERTYPE_ALL = "all";
    /** 工单类型-功能测试工单 */
    public static final String ORDERTYPE_FUNCTION_CH = "功能测试";
    /** 工单类型-安全测试工单 */
    public static final String ORDERTYPE_SECURITY_CH = "安全测试";
    /** 应用名称-工单模块 */
    public static final String APPLICATION_NAME_ORDER = "testmanage-order";
    /** back */
    public static final String BACK = "back";
    /** ContentType_multipart/form-data */
    public static final String CONTENTTYPE_FORMDATA = "multipart/form-data";
    /** 交易码-filesUpload */
    public static final String RESTCODE_FILESUPLOAD = "filesUpload";
    /** 交易码-filesDownload */
    public static final String RESTCODE_FILESDOWNLOAD = "filesDownload";
    /** 报表类型-提测准时率 */
    public static final String REPORTTYPE_TIMELYRATE = "timelyRate";
    /** 报表类型-冒烟测试通过率 */
    public static final String REPORTTYPE_SMOKERATE = "smokeRate";
    /** 报表类型-缺陷reopen率 */
    public static final String REPORTTYPE_REOPENRATE = "reopenRate";
    /** 报表类型-缺陷密度 */
    public static final String REPORTTYPE_MANTISRATE = "mantisRate";
    /** 报表类型-缺陷平均解决时长 */
    public static final String REPORTTYPE_NORMALAVGTIME = "normalAvgTime";
    /** 报表类型-严重缺陷解决时长 */
    public static final String REPORTTYPE_SEVAVGTIME = "sevAvgTime";

}
