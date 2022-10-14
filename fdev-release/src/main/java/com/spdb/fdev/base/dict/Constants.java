package com.spdb.fdev.base.dict;

/**
 * Created by xxx on 上午10:21.
 */
public class Constants {

    /**
     * 窗口已创建
     */
    public static final String NODE_CREATED = "1";

    /**
     * 窗口已废弃
     */
    public static final String NODE_DESERTED = "0";

    /**
     * DEVOPS状态-初始状态
     */
    public static final String DEVOPS_INIT = "0";

    /**
     * DEVOPS状态-已发起RELEASE至MASTER merge request
     */
    public static final String DEVOPS_MERGEREQ_CREATED = "1";

    /**
     * DEVOPS状态-RELEASE至MASTER merge request发起失败
     */
    public static final String DEVOPS_MERGEREQ_FAILED = "2";

    /**
     * DEVOPS状态-RELEASE至MASTER merge request已合并 待拉取Product tag
     */
    public static final String DEVOPS_MERGEREQ_MERGED = "3";

    /**
     * DEVOPS状态-RELEASE至MASTER merge request拒绝合并
     */
    public static final String DEVOPS_MERGEREQ_CLOSED = "4";

    /**
     * DEVOPS状态-Product tag已拉取
     */
    public static final String DEVOPS_PRODTAG_CREATED = "5";

    /**
     * DEVOPS状态-Product tag已打包
     */
    public static final String DEVOPS_PRODTAG_PACKAGED = "6";

    /**
     * 任务状态-0-投产待审核
     */
    public static final String TASK_RLS_NEED_AUDIT = "0";

    /**
     * 任务状态-1-已确认投产
     */
    public static final String TASK_RLS_CONFIRMED = "1";

    /**
     * 任务状态-2-拒绝投产
     */
    public static final String TASK_RLS_REJECTED = "2";

    /**
     * 任务状态-3-取消投产待审核
     */
    public static final String TASK_RLS_CANCEL_NEED_AUDIT = "3";

    /**
     * 任务状态-4-已取消投产
     */
    public static final String TASK_RLS_CANCELED = "4";

    /**
     * 任务状态-5-更换投产点待审核
     */
    public static final String TASK_RLS_CHANGE_NODE_NEED_AUDIT = "5";
    
    /**
     * 任务状态-6-已投产
     */
    public static final String TASK_RLS_PRODUCTION= "6";
    
    /**
     * 任务审核-0-发起
     */
    public static final String OPERATION_TYPR_BEGIN = "0";
    
    /**
     * 任务审核-1-同意
     */
    public static final String OPERATION_TYPR_ACCESS = "1";

    /**
     * 任务审核-2-拒绝
     */
    public static final String OPERATION_TYPR_REFUSE = "2";



    //====================文件上传相关=============================//

    public static final String M_SUCCESS = "成功";
    public static final String I_SUCCESS = "AAAAAA";

    public static final String M_SERVER_ERROR = "服务器异常";
    public static final String I_SERVER_ERROR = "200001";


    public static final String M_GITLAB_ERROR = "gitlab处理异常";
    public static final String I_GITLAB_ERROR = "300001";

    public static final String M_PARAM_ERROR = "参数异常";
    public static final String I_PARAM_ERROR = "400002";

    public static final String M_SAVE_ERROR = "保存文件异常";
    public static final String I_SAVE_ERROR = "400003";

    public static final String M_REQUIRE_ERROR = "请求远程服务异常";
    public static final String I_REQUIRE_ERROR = "400004";

    public static final String M_DATA_ERROR = "请求数据异常";
    public static final String I_DATA_ERROR = "300004";



    public static final String M_VALIDAYTION_USER_ERROR = "gitlab用户异常";
    public static final String I_VALIDAYTION_USER_ERROR = "300002";

    public static final String M_VALIDAYTION_PROJECT_ERROR = "gitlab项目异常";
    public static final String I_VALIDAYTION_PROJECT_ERROR = "300003";


    public static final Integer Guest=10;
    public static final Integer Reporter=20;
    public static final Integer Developer=30;
    public static final Integer Maintainer=40;
    public static final Integer Owner=50;

    /**
     * 保存文件的 本地临时目录的前缀
     */
    public static final String TEMP_DIR = "temp-";
    /**
     * gitlab 存放文件的项目名字
     */
    public static final String GITLAB_PROJECT_NAME = "fdev-resource";

    /**
     * 空字符串
     */
    public static final String NULL_STRING = "";
    /**
     * 分割符
     */
    public static final String SPLIT_STRING = "/";
    
    public static final String ASSTETYPE_MS="2";
    
    
    public static final String RELEASESTATUS_0="0";
    //1-常规应用包更新，2-微服务应用更新，3-数据库更新，4-配置文件更新	
    public static final String CATALOG_TYPE_NORMAL="1";
    
    public static final String CATALOG_TYPE_MICROSERIVICE="2";
    
    public static final String CATALOG_TYPE_DATA="3";
    
    public static final String CATALOG_TYPE_COMFIG="4";

    public static final String REDISSON_LOCK_NAMESPACE = "lock";

    //0-未准备，1-已发起，2-已取消，3-准备完毕，4-失败
    public static final String PROD_RECORD_STSTUS_INIT = "0";

    //0-未准备，1-已发起，2-已取消，3-准备完毕，4-失败
    public static final String PROD_RECORD_STSTUS_STARED = "1";

    //0-未准备，1-已发起，2-已取消，3-准备完毕，4-失败
    public static final String PROD_RECORD_STSTUS_CANCELED = "2";

    //0-未准备，1-已发起，2-已取消，3-准备完毕，4-失败
    public static final String PROD_RECORD_STSTUS_READY = "3";

    //0-未准备，1-已发起，2-已取消，3-准备完毕，4-失败
    public static final String PROD_RECORD_STSTUS_FAILED = "4";
    //1 代表忽略已归档窗口
    public static final String IGNORE_ARCHEIVED = "1";
    //直接修改投产窗口 1 需要重新审核 0
    public static final String CHANEG_TASK_NEED_ADD = "0";

    // 不在前端展示
    public static final String HIDE = "0";

    // 前端展示
    public static final String SHOW = "1";

    public static final String ROLE_RELEASE = "5c41796ca3178a3eb4b52005";
    
    public static final String ROLE_CSII = "5c41796ca3178a3eb4b52007";
    
    public static final String ROLE_SPDB = "5cb72f1d9b1dd800076b4ad1";
    //模版状态 0-正常 1-废弃
    public static final String TEMPLATE_STATUS_NORMAL = "0";
    
    public static final String TEMPLATE_STATUS_CANCEL = "1";

    // 文件来源3:-fdev
    public static final String SOURCE_FDEV = "3";

    // 分支已合并
    public static final String MERGED = "1";

    // 试运行分支
    public static final String TESTRUN = "testrun";

    // 变更应用类型   1：微服务镜像异步更新
    public static final String APPLICATION_DOCKER = "1";

    // 变更应用类型   2：K1、K2镜像同步更新 ==> 更新为单集群停止
    public static final String APPLICATION_DOCKER_ALL = "2";

    // 变更应用类型   3：微服务停止后重启
    public static final String APPLICATION_DOCKER_STOP_START_ALL = "3";

    // 变更应用类型   4：服务重启
    public static final String APPLICATION_DOCKER_RESTART = "4";
    
    // 变更应用类型   1：微服务镜像异步更新
    public static final String SERVICE_ASYN_UPDATE = "1";

    // 变更应用类型   2：K1、K2镜像同步更新 = >更改为 单集群停止
    public static final String SERVICE_SYN_UPDATE = "2";

    // 变更应用类型   3：微服务停止后重启
    public static final String SERVICE_STOP_AND_START = "3";

    // 变更应用类型   4：服务重启
    public static final String SERVICE_RESTART = "4";
    
    // 投产窗口任务过期处理
    public static final int DATE_THREE_DAYS = 3;
    public static final int DATE_TWO_DAYS = 2;
    public static final int DATE_ONE_DAYS = 1;

    // 新版自动化发布（镜像直推）
    public static final String IMAGE_DELIVER_TYPE_NEW = "1";

    // 投产变更应用配置文件变化发送通知内容
    public static final String CONFIG_CHECKOUT_NOTIFY = "投产应用涉及配置文件变化，请前往处理!";

    // 进入REL错误提示
    public static final String RELEASE_REL_ERROR = "任务进入REL出现异常";

    // 查询任务详情错误提示
    public static final String QUERY_TASK_ERROR = "查询任务详情出现异常";
    //业务需求
    public static final String BUSINESS_RQRMNT="0";
    // 任务上传文档，科技需求
    public static final String RELEASE_RQRMNT_TASK_FILE = "1";
    // 投产需求上传文档
    public static final String RELEASE_RQRMNT_FILE = "2";

    // 配置文件更新消息类型
    public static final String CONFIG_FILE_REFRESH = "配置文件更新待确认";

    public static final String CONFIRM_RELEASE = "任务投产待确认";

    public static final String CONFIRM_TESTRUN ="试运行待确认";

    public static final String SYSTEM_ERROR = "系统内部错误";

    // 需要拉取fake分支
    public static final String PULL_FAKE_TAG = "1";

    //minio任务模块文件
    public static final String TASK_MODULE = "fdev-task";

    //需求列表标签-日常流程
    public static final String RQRMNT_TAG_DAILY = "1";

    //需求列表标签-处对处
    public static final String RQRMNT_TAG_CHIEF = "2";

    //需求列表标签-总对总
    public static final String RQRMNT_TAG_LENGTH = "3";

    //需求列表标签-不予投产
    public static final String RQRMNT_TAG_NOT_ALLOW = "4";

    //需求列表标签-日常流程
    public static final String RQRMNT_TAG_DAILY_CN = "日常流程";

    //需求列表标签-处对处
    public static final String RQRMNT_TAG_CHIEF_CN = "处对处";

    //需求列表标签-总对总
    public static final String RQRMNT_TAG_LENGTH_CN = "总对总";

    //需求列表标签-条线审批
    public static final String RQRMNT_TAG_CHIEF_TEC_CN = "条线审批";

    //需求列表标签-老总审批
    public static final String RQRMNT_TAG_LENGTH_TEC_CN = "老总审批";

    //需求列表标签-不予投产
    public static final String RQRMNT_TAG_NOT_ALLOW_CN = "不予投产";

    //提测重点需求列表查询
    public static final String RQRMNT_POINT_QUERY = "1";

    //安全测试需求列表查询
    public static final String INVOLVE_SECURITY_TEST = "1";

    //业务需求
    public static final String RQRMNT_TYPE_BUSINESS = "业务需求";

    //科技内部需求
    public static final String RQRMNT_TYPE_INNER = "科技内部需求";
    // 日常需求
    public static final String RQRMNT_TYPE_DAILY = "日常需求";

    //科技内部需求
    public static final String RQRMNT_TYPE_INNER_EN = "tech";

    //科技内部需求
    public static final String RQRMNT_TYPE_BUSINESS_EN = "business";

    //需求信息投产确认书打开
    public static final String RELEASE_COMFIRM_FILE_OPEN = "已到达";

    //需求信息投产确认书关闭
    public static final String RELEASE_COMFIRM_FILE_CLOSE = "未到达";

    //需求信息投产确认书关闭
    public static final String RELEASE_COMFIRM_FILE_NOT_INVOLVE = "不涉及";

    //任务对应工单状态为uat
    public static final String TASK_ORDER_STATUS_UAT = "3";

    //任务对应工单状态为uat
    public static final String TASK_ORDER_STATUS_SUBPACKAGE = "9";

    //投产大窗口的类型
    public static final String RELEASE_BIG_NODE_TYPE="0";

    //上线确认书关闭状态
    public static final String CONFIRM_BTN_CLOSED="0";

    //上线确认书打开状态
    public static final String CONFIRM_BTN_OPENED="1";

    //投产周期T
    public static final String T="T";

    //投产周期T-1
    public static final String T_1="T-1";

    //投产周期T-2
    public static final String T_2="T-2";

    //投产周期T-3
    public static final String T_3="T-3";

    //投产周期T-4
    public static final String T_4="T-4";

    //投产周期T-5
    public static final String T_5="T-5";

    //投产周期T-6
    public static final String T_6="T-6";

    //投产周期T-7
    public static final String T_7="T-7";

    //投产周期T-7
    public static final String T_8="T-8";

    //关联项包含包含
    public static final String REVIEW_TRUE="是";

    // 可以正常提交发布
    public static final String RELEASE_CONFIRM_NORMAL = "0";

    // 不满足提交发布
    public static final String RELEASE_CONFIRM_ERROR = "1";

    public static final String INVOLVE_SECURITY_TEST_WORD = "涉及整包提请安全测试";

    public static final String INTERNET_GROUP = "互联网";

    //投产任务拒绝
    public static final String RELEASE_TASK_STATUS_REFUSED = "2";
    
    public static final String SCC_DOCUMENT_NAME = "变更管理_个人手机银行系统_SCCDCE_Vnew20210707.xls";

    // release.json文件里的变量名
    public static final String RELEASE_NAME = "$RELEASE_NAME";
    public static final String RELEASE_COMMAND = "$RELEASE_COMMAND";
    public static final String RELEASE_IMAGE_NAME = "$RELEASE_IMAGE_NAME";
    public static final String TIMESTAMP = "\"$TIMESTAMP\"";
    public static final String CI_ENVIRONMENT_SLUG = "$CI_ENVIRONMENT_SLUG";
    public static final String CI_PROJECT_URL = "$CI_PROJECT_URL";
    public static final String CI_PROJECT_NAME = "$CI_PROJECT_NAME";
    public static final String CI_PROJECT_BRANCH = "$CI_PROJECT_BRANCH";
    public static final String CI_PROJECT_DIR = "$CI_PROJECT_DIR";
    public static final String SYSTEM_ROUTE_DIR = "$SYSTEM_ROUTE_DIR";
    public static final String CI_API_V4_URL = "$CI_API_V4_URL";
    public static final String CI_PROJECT_ID = "$CI_PROJECT_ID";
    
    //批量任务变量名
    public static final String TYPE = "\"type\"";
    public static final String JOBGROUP = "\"jobGroup\"";
    public static final String EXECUTORID = "\"executorId\"";
    public static final String TRANSNAME = "\"transName\"";
    public static final String DESCRIPTION = "\"description\"";
    public static final String CRONEXPRESSION = "\"cronExpression\"";
    public static final String MISFIREINSTR = "\"misfireInstr\"";
    public static final String FIRETIME = "\"fireTime\"";
    public static final String APOSTROPHE = "\"";//单引号
    

}