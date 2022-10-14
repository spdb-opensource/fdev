package com.spdb.fdev.fdemand.base.dict;

/**
 * Created by xxx on 上午10:21.
 */
public class Constants {
    /**
     * 需求暂缓邮件模版
     */
    public static final String FDEMAND_DEFER_FTL = "fdemand_defer.ftl";

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
    public static final String HISTORY_DEMAND_URL="history_demand_url";
    
    /**
     * 科技需求
     */
    public static final String TECH="tech";
    
    /**
     * 业务需求
     */
    public static final String BUSINESS="business";

    /**
     * UI团队负责人
     */
    public static final String UI_MANAGER = "UI团队负责人";
    public static final String COUNT = "count";

    //投产阶段科技的数量
    public static final String PROTECNUM = "proTecNum";
    //投产阶段业务的数量
    public static final String PROBUSNUM = "proBusNum";
    //rel阶段科技的数量
    public static final String RELTECNUM = "relTecNum";
    //rel阶段业务的数量
    public static final String RELBUSNUM = "relBusNum";
    //sit阶段科技的数量
    public static final String SITTECNUM = "sitTecNum";
    //sit阶段业务的数量
    public static final String SITBUSNUM = "sitBusNum";
    //uat阶段科技的数量
    public static final String UATTECNUM = "uatTecNum";
    //uat阶段业务的数量
    public static final String UATBUSNUM = "uatBusNum";
    //待实施阶段科技的数量
    public static final String TODOTECNUM = "todoTecNum";
    //待实施科技排队的数量
    public static final String TODOTECWAITNUM = "todoTecWaitNum";
    //待实施业务排队的数量
    public static final String TODOBUSWAITNUM = "todoBusWaitNum";
    //待实施业务的数量
    public static final String TODOBUSNUM = "todoBusNum";
    //开发科技（牵头）的数量
    public static final String DEVTECNUM = "devTecNum";
    //开发平均负荷
    public static final String DEVAVGLOAD = "devAvgLoad";
    //开发业务（牵头）的数量
    public static final String DEVBUSNUM = "devBusNum";
    //开发科技（参与）的数量
    public static final String DEVTECJOINNUM = "devTecJoinNum";
    //开发业务（参与）的数量
    public static final String DEVBUSJOINNUM = "devBusJoinNum";
    //科技总数
    public static final String TOTALTECNUM = "totalTecNum";
    //业务总数
    public static final String TOTALBUSNUM = "totalBusNum";
    //业务需求人均负荷
    public static final String BUSRQRAVGLOAD = "busRqrAvgLoad";
    //暂缓阶段业务的数量
    public static final String WAITBUSNUM = "waitBusNum";
    //暂缓阶段科技的数量
    public static final String WAITTECNUM = "waitTecNum";
    /**  Redis存储组信息key  */
    public static final String REDISGETGROUPS="frqrmnt.getGroups";
    /**  暂缓状态  */
    public static final Integer IMPLUNITSTOP=1;
    /**  小组id  */
    public static final String GROUPID = "groupid";
    /**  测试人员  */
    public static final String TEST = "测试人员";
    /**  开发人员  */
    public static final String DEVELOP = "开发人员";
    /**  UI审核未上传  */
    public static final String UPLOADNOT = "uploadNot";
    /**  UI审核不涉及  */
    public static final String NORELATE = "noRelate";
    /**  yes  */
    public static final String YES = "yes";
    /**  1  */
    public static final String ONE = "1";
    /**  2  */
    public static final String TWO = "2";

    /**  ipmp项目/任务集  */
    public static final String IPMP_PROJECT = "ipmp_project";
    public static final String IPMP_VERSION = "1.0";
    /** ipmp接口查询全量项目任务集信息 */
    public static final String IPMP_METHOD_GETALLPROJECTS = "/project/getAllProjects";

    /**  科技类型 */
    public static final String TECH_TYPE = "tech_type";

    /**  字典 */
    public static final String DICT = "dict";

    /**  存量实施单元 */
    public static final String STOCKUNIT = "stockUnit";

    /**  未开始  */
    public static final String NOTSTART = "notStart";
    /**  进行中 */
    public static final String GOING = "going";
    /** 已完成 */
    public static final String DONE = "done";
    /**  删除 */
    public static final String DELETE = "delete";
    /** ui审核阶段-审核未通过，上传修改意见稿 */
    public static final String STAGE_LOADNOPASS = "load_nopass";
    /** 审核状态-待分配 */
    public static final String REVIEWSTATUS_WAITALLOT = "wait_allot";
    /** 当前审核阶段-分配中 */
    public static final String REVIEWSTAGE_ALLOTING = "alloting";
    /** 当前审核阶段-审核中 */
    public static final String REVIEWSTAGE_CHECKING = "checking";
    /** 当前审核阶段-修改中 */
    public static final String REVIEWSTAGE_UPDATEING = "updateing";
    /** 当前审核阶段-完成 */
    public static final String REVIEWSTAGE_FINISH = "finish";
    /** 审核状态-分配了审核人 */
    public static final String REVIEWSTATUS_FIXING = "fixing";
    /** 审核状态-未通过 */
    public static final String REVIEWSTATUS_NOPASS = "nopass";
    /** ui审核阶段-开发上传 */
    public static final String STAGE_LOADUPLOAD = "load_upload";
    /** 审核状态-申请跳过 */
    public static final String REVIEWSTATUS_UNINVOLVED = "uninvolved";
    /** 审核状态-拒绝跳过 */
    public static final String REVIEWSTATUS_REFUSEUNINVOLVED = "refuseUninvolved";
    /** 审核状态-完成 */
    public static final String REVIEWSTATUS_FINISHED = "finished";
    /** 完成情况-通过 */
    public static final String FINSHFLAG_PASS = "pass";
    /** 完成情况-未通过 */
    public static final String FINSHFLAG_NOPASS = "noPass";
    /** 卡点状态 */
    public static final String SEARCHKEY_POSITIONSTATUS = "positionStatus";
    /** 完成情况 */
    public static final String SEARCHKEY_FINSHFLAG = "finshFlag";
    /** 卡点状态-正常 */
    public static final String POSITIONSTATUS_OK = "ok";
    /** 当前阶段 */
    public static final String SEARCHKEY_CURRENTSTAGE = "currentStage";
    /** 审核轮数 */
    public static final String SEARCHKEY_CHECKCOUNT = "checkCount";
    /** 待审批 */
    public static final String WAIT = "wait";
    /** 通过 */
    public static final String PASS = "pass";
    /** 拒绝 */
    public static final String REJECT = "reject";
    /** 未提交 */
    public static final String NOSUBMIT = "noSubmit";
    /** 是否判断大于 */
    public static final String GT = "gt";
    /** 业测 */
    public static final String FDEV_TEST = "test";
    /** 开始 */
    public static final String START = "start";
    /** 已撤销 */
    public static final String DELETED = "deleted";
    /** 已归档 */
    public static final String FILE = "file";
    /** 创建 */
    public static final String CREATE = "create";
    /** 开发审批 */
    public static final String DEVAPPROVE = "devApprove";
    /** 超期审批 */
    public static final String OVERDUEAPPROVE = "overdueApprove";
    /** 开发审批&超期审批 */
    public static final String DEVOVERDUE = "dev&overdue";
}