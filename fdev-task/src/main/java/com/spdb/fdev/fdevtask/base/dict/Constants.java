package com.spdb.fdev.fdevtask.base.dict;

public class Constants {

    //交易成功返回码
    public static final String I_SUCCESS = "AAAAAAA";

    //交易成功响应信息
    public static final String M_SUCCESS = "交易执行成功";

    //投产负责人角色ID
    public static final String ROLE_RELEASE = "5c41796ca3178a3eb4b52005";
    
    public static final String ROLE_SPDB_MANAGER="5caee97a357e190007af7250";
    
    public static final String CODE_200="200";
    
    public static final String CODE_201="201";
    
    public static final String CODE_204="204";
    //厂商项目负责人角色ID
    public static final String ROLE_CSII = "5c41796ca3178a3eb4b52007";
    //行内项目负责人角色ID
    public static final String ROLE_SPDB = "5cb72f1d9b1dd800076b4ad1";
    //版本管理员角色ID
    public static final String ROLE_VERSION = "5c8b610fa3178a3b68bb6ce5";
    //数据库管理员角色ID
    public static final String ROLE_DATA = "5c41796ca3178a3eb4b52008";

    //邮件提醒-用户完成首次登录
    public static final String EMAIL_ONECELOGIN = "email.user.oncelogin";
    //邮件提醒-任务新建完成
    public static final String EMAIL_ADDTASK = "email.task.addtask";
    //忘记密码-邮件发验证码
    public static final String EMAIL_FORGETPW = "email.user.forgetPw";
    //邮件提醒-任务更新
    public static final String EMAIL_UPDATETASK = "email.task.updatetask";
    //邮件提醒-任务回退
    public static final String EMAIL_ROLLBACKTASK = "email.task.rollbacktask";
    //邮件提醒-进入sit
    public static final String EMAIL_INTOSIT = "email.task.intosit";
    //邮件提醒-进入任务投产
    public static final String EMAIL_PRODUCTTASK = "email.task.producetask";
    //邮件提醒-进入uat
    public static final String EMAIL_INTOUAT = "email.task.intouat";
    //邮件提醒-进入uat
    public static final String EMAIL_INTOREL = "email.task.intorel";
    //邮件提醒-进入uat
    public static final String EMAIL_INTOPRO = "email.task.intopro";
    //邮件提醒-进入file
    public static final String EMAIL_INTOFILE = "email.task.intofile";
    //邮件提醒-进入abort
    public static final String EMAIL_INTOABORT = "email.task.intoabort";

	// 任务关联项未审核
	public static final String EMAIL_TASKREVIEWS = "email.release.taskreviews";
	// 进入UAT提醒确认
	public static final String EMAIL_RELEASEADDTASK = "email.release.addtask";
	// 进入UAT确认完成
	public static final String EMAIL_RELEASEAUDITADD = "email.release.auditadd";
	// 进入UAT确认完成
	public static final String EMAIL_RELEASETAGBUILD = "email.release.tagbuild";
	// 创建投产窗口
	public static final String EMAIL_RELEASECREATENODE = "email.release.createnode";
    //修改投产窗口
	public static final String EMAIL_RELEASEUPDATENODE="email.release.updatenode";
	//更新审核状态
	public static final String EMAIL_REVIEWUPDATE = "email.task.reviewupdate";
	//设计还原审核指派审核人
	public static final String EMAIL_CHOOSEREVIEWER = "email.task.choosereviewer";
	//设计还原审核任务提醒
	public static final String EMAIL_NOTIFYREVIEWER = "email.task.notifyreviewer";
	//UI团队负责人
	public static final String UI_TEAM_MANAGER = "UI团队负责人";
	//UI团队设计师
	public static final String UI_TEAM_DESIGNER = "UI团队设计师";
    //无代码变更
    public static final String EMAIL_ADDNOCODETASK = "email.task.nocode" ;
    //业务需求
    public static final String TASK_BUS_RQR = "业务需求" ;
    //科技内部需求
    public static final String TASK_SCI_TEC_INNER_RQR = "科技内部需求" ;
    //任务模块module
    public static final String TASK_MODULE = "任务模块";
    //key=
    public static final String KEY = "key=";
    //转换码
    public static final String TRANSITIONS = "/transitions";

    //需求列表标签-处对处
    public static final String RQRMNT_TAG_CHIEF = "2";

    //需求列表标签-总对总
    public static final String RQRMNT_TAG_LENGTH = "3";

    //子任务
    public static final String SUB_TASK = "10101";

    //待处理
    public static final String WAIT_HANDLE = "11";
    //处理中
    public static final String IN_HAND = "21";
    //测试中
    public static final String TESTING = "41";
    //完成
    public static final String DONE = "31";
    public static final String APPTYPE_VUE = "Vue应用";
    public static final String APPTYPE_ANDROID = "Android应用";
    public static final String APPTYPE_IOS = "IOS应用";
    public static final String DEMANDTYPE_BUSINESS = "business";//业务需求
    public static final String DEMANDTYPE_TECH = "tech";//科技需求
    public static final String DEMANDTYPE_DAILY = "daily";//日常需求
    public static final String TASKTYPE_DAILY = "daily";//日常任务
    public static final String TASKTYPE_DEVELOP = "develop";//开发任务
    public static final String TASKTYPE_NOCODE = "nocode";//无代码任务

    /** 审核轮数 */
    public static final String SEARCHKEY_CHECKCOUNT = "checkCount";
    /** 审核反馈->开发上传 */
    public static final String SEARCHKEY_RETURNTOUPLOAD = "returnToUpload";
    /** 开发上传->审核反馈 */
    public static final String SEARCHKEY_UPLOADTORETURN = "uploadToReturn";
    /** 卡点状态 */
    public static final String SEARCHKEY_POSITIONSTATUS = "positionStatus";
    /** 完成情况 */
    public static final String SEARCHKEY_FINSHFLAG = "finshFlag";
    /** 当前阶段 */
    public static final String SEARCHKEY_CURRENTSTAGE = "currentStage";
    /** ui审核阶段-审核未通过，上传修改意见稿 */
    public static final String STAGE_LOADNOPASS = "load_nopass";
    /** ui审核阶段-开发上传 */
    public static final String STAGE_LOADUPLOAD = "load_upload";
    /** 关系运算符-小于 */
    public static final String RELATIONALOPERATOR_LT = "lt";
    /** 关系运算符-等于 */
    public static final String RELATIONALOPERATOR_EQ = "eq";
    /** 关系运算符-大于 */
    public static final String RELATIONALOPERATOR_GT = "gt";
    /** 卡点状态-正常 */
    public static final String POSITIONSTATUS_OK = "ok";
    /** 卡点状态-失败 */
    public static final String POSITIONSTATUS_FAIL = "fail";
    /** 完成情况-通过 */
    public static final String FINSHFLAG_PASS = "pass";
    /** 完成情况-未通过 */
    public static final String FINSHFLAG_NOPASS = "noPass";
    /** 审核状态-待分配 */
    public static final String REVIEWSTATUS_WAITALLOT = "wait_allot";
    /** 审核状态-申请跳过 */
    public static final String REVIEWSTATUS_UNINVOLVED = "uninvolved";
    /** 审核状态-分配了审核人 */
    public static final String REVIEWSTATUS_FIXING = "fixing";
    /** 审核状态-未通过 */
    public static final String REVIEWSTATUS_NOPASS = "nopass";
    /** 审核状态-拒绝跳过 */
    public static final String REVIEWSTATUS_REFUSEUNINVOLVED = "refuseUninvolved";
    /** 审核状态-完成 */
    public static final String REVIEWSTATUS_FINISHED = "finished";
    /** 当前审核阶段-分配中 */
    public static final String REVIEWSTAGE_ALLOTING = "alloting";
    /** 当前审核阶段-审核中 */
    public static final String REVIEWSTAGE_CHECKING = "checking";
    /** 当前审核阶段-修改中 */
    public static final String REVIEWSTAGE_UPDATEING = "updateing";
    /** 当前审核阶段-完成 */
    public static final String REVIEWSTAGE_FINISH = "finish";
    /** 逻辑运算符-或 */
    public static final String LOGICALOPERATOR_OR = "or";
    /** 安全测试文件类型-接口说明 */
    public static final String FILETYPE_INTERFACE = "interface";
    /** 安全测试文件类型-交易清单 */
    public static final String FILETYPE_TRANS = "trans";
    /** 交易清单模板文件 */
    public static final String TRANS_TEMPLATE_XLSX = "files/transTemplate.xlsx";
    /** 交易清单文件名 */
    public static final String TRANS_XLSX_NAME = "安全测试交易清单";
    /** 应用名称 */
    public static final String APPLICATION_NAME = "fdev-task";
    /** 应用类型-java微服务 */
    public static final String APPLICATION_JAVA = "appJava";
    /** 应用类型-Vue应用 */
    public static final String APPLICATION_VUE = "appVue";
    /** 应用类型-IOS应用 */
    public static final String APPLICATION_IOS = "appIos";
    /** 应用类型-Android应用 */
    public static final String APPLICATION_ANDROID = "appAndroid";
    /** 应用类型-容器化项目 */
    public static final String APPLICATION_DOCKER = "appDocker";
    /** 应用类型-老版服务 */
    public static final String APPLICATION_OLDSERVICE = "appOldService";
    /** 应用类型-前端组件 */
    public static final String COMPONENT_WEB = "componentWeb";
    /** 应用类型-后端组件 */
    public static final String COMPONENT_SERVER = "componentServer";
    /** 应用类型-前端骨架 */
    public static final String ARCHETYPE_WEB = "archetypeWeb";
    /** 应用类型-后端骨架 */
    public static final String ARCHETYPE_SERVER = "archetypeServer";
    /** 应用类型-镜像 */
    public static final String IMAGE = "image";
    /** 测试包类型-beta */
    public static final String PACKAGETYPE_BETA = "beta";
    /** 测试包类型-alpha */
    public static final String PACKAGETYPE_ALPHA = "alpha";
    /** 查询前端组件详情接口 */
    public static final String QUERYMPASSCOMPONENTDETAIL = "queryMpassComponentDetail";
    /** 查询后端组件详情接口 */
    public static final String QUERYCOMPONENTDETAIL = "queryComponentDetail";
    /** 查询前端骨架详情接口 */
    public static final String QUERYMPASSARCHETYPEDETAIL = "queryMpassArchetypeDetail";
    /** 查询后端骨架详情接口 */
    public static final String QUERYARCHETYPEDETAIL = "queryArchetypeDetail";
    /** 查询镜像详情接口 */
    public static final String QUERYBASEIMAGEDETAIL = "queryBaseImageDetail";
    /** 保存预设版本号 */
    public static final String SAVEPREDICTVERSION = "savePredictVersion";
    /** 保存目标版本号 */
    public static final String SAVETARGETVERSION = "saveTargetVersion";
    /** 保存合并请求信息 */
    public static final String SAVEMERGEREQUEST = "saveMergeRequest";
    /** 组件测试包 */
    public static final String TESTPACKAGE = "testPackage";
    /** 校验预设版本号 */
    public static final String JUDGEPREDICTVERSION = "judgePredictVersion";
    /** 校验目标版本号 */
    public static final String JUDGETARGETVERSION = "judgeTargetVersion";
    /** SIT分支 */
    public static final String BRANCH_SIT = "SIT";
    /** master分支 */
    public static final String BRANCH_MASTER = "master";
    /** 前端 */
    public static final String MPASS = "mpass";
    /** 后端 */
    public static final String BACK = "back";

}
