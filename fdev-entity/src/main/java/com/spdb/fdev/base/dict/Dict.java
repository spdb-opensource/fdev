
package com.spdb.fdev.base.dict;

public class Dict {

    private Dict() {
    }

    public static final String ID = "id";
    public static final String PAGE = "page";
    /** 用户中文名*/
    public static final String USER_NAME_CN = "user_name_cn";
    /** 用户英文名*/
    public static final String USER_NAME_EN = "user_name_en";
    /**  远程调用交易码  */
    public static final String REST_CODE="REST_CODE";
    /** 查询内容*/
    public static final String DATA = "data";
    /**计数*/
    public static final String COUNT = "count";
    /** ObjectId类型的_id*/
    public static final String OBJECTID = "_id";
    public static final String GET = "get";
    /** 文件类型*/
    public static final String TYPE = "type";
    /**  projects  */
    public static final String PROJECTS = "projects";
    /** 实体表名*/
    public static final String ENTITY = "entity";
    /** 所属范围 */
    public static final String SCOPE = "scope";
    /** 逻辑删除标记 */
    public static final String DELETE = "delete";
    /** 所属范围id */
    public static final String SCOPEID = "scopeId";
    /** 复制的实体id */
    public static final String  COPYID= "copyId";
    /** 实体创建人 */
    public static final String CREATEUSERID = "createUserId";
    /** 实体英文名 */
    public static final String NAMEEN = "nameEn";
    /** 实体中文名 */
    public static final String NAMECN = "nameCn";
    /** 实体模板 */
    public static final String TEMPLATEID = "templateId";
    /** 每页条数 */
    public static final String PERPAGE = "perPage";
    /** 实体字段值 */
    public static final String PROPERTIESVALUE = "propertiesValue";
    /** 环境 */
    public static final String ENV = "env";
    /** 实体列表 */
    public static final String ENTITYMODELLIST = "entityModelList";
    /** 最新修改人 */
    public static final String UPDATEUSERID = "updateUserId";
    /** 环境 */
	public static final String ENVTYPE = "envType";
	/** 环境名称 */
	public static final String ENVNAME = "envName";
    /**  queryUser  */
    public static final String QUERYUSER="queryUser";
    /**  标识  */
	public static final String FLAG = "flag";
    /**  序列化版本号  */
    public static final String SERIALVERSIONUID="serialVersionUID";
    /** 模糊字段 */
    public static final String LIKEKEY="likeKey";

    /** queryApp */
    public static final String QUERYAPP="queryApp";
    /** queryGroup */
    public static final String QUERYGROUP="queryGroup";
    /** 系统编号 */
	public static final String SYSCODE = "sysCode";
	/** 系统名称 */
	public static final String SYSNAME = "sysName";
	/** 名称 */
	public static final String NAME = "name";
	/** 应用中文名 */
    public static final String SERVICENAMECN = "nameCN";
	/** 实体字段数组 */
	public static final String VARIABLESKEY = "variablesKey";
	/** 更新时间 */
	public static final String UPDATETIME = "updateTime";
	/** 英文名是否重复 */
	public static final String NAMEENFLAG = "nameEnFlag";
	/** 中文名是否重复 */
	public static final String NAMECNFLAG = "nameCnFlag";
	/** 是否必输*/
	public static final String REQUIRED = "required";
	/** 实体id集合*/
	public static final String ENTITYIDLIST = "entityIdList";
	/** 环境英文名*/
	public static final String ENVNAMEEN = "envNameEn";
    /** 外部配置模板*/
    public static final String OUT_SIDE_TEMPLATE = "out_side_template";
    /** 应用id*/
    public static final String PROJECT_ID = "project_id";
    //异常参数
    public static final String FORMATERROR = "formatError";
    public static final String MODELERRORLIST = "modelErrorList";
    public static final String FILEDERRORLIST = "FiledErrorList";
    /** 模板*/
    public static final String MODELS = "models";
    public static final String MODEL_FIELD = "modelField";
    /** 更新配置模板文件提交信息*/
    public static final String COMMIT_MESSAGE = "commit_message";

	/** 配置模板内容*/
	public static final String CONTENT = "content";
	/** 实体.属性参数*/
	public static final String MODELPARAM = "modelParam";
	/** 优先生效参数*/
	public static final String OUTSIDEPARAM = "outSideParam";
	/** 应用英文名 */
    public static final String NAMEENN = "nameEN";
    /** 配置中心ID*/
	public static final String FDEVCONFIGHOSTIP = "fdevConfigHostIp";
	/** 配置中心用户名*/
	public static final String FDEVCONFIGUSER = "fdevConfigUser";
	/** 配置中心用户密码*/
	public static final String FDEVCONFIGPASSWORD = "fdevConfigPassword";
	/** 配置中心文件夹*/
	public static final String FDEVCONFIGDIR = "fdevConfigDir";
	/** 项目名 */
	public static final String PROJECTNAME = "projectName";


    /** 应用分支*/
    public static final String FEATUREBRANCH = "featureBranch";
    /** 应用id*/
    public static final String PROJECTID = "projectId";
    /** gitlab项目ID*/
    public static final String GITLABID = "gitlabId";
    public static final String PRIVATE_TOKEN = "Private-Token";
    public static final String REF = "ref";

    public static final String GITLAB_PROJECT_ID = "gitlab_project_id";
    public static final String PRIVATE_TOKEN_L = "private_token";
    public static final  String BRANCH="branch";
    /**  字段Key  */
	public static final String APPKEY = "appkey";
	/**  环境名  */
	public static final String ENV_NAME = "env_name";
	/**  字段值 */
	public static final String VALUE = "value";
	/**  字段列表 */
	public static final String PROPERTIES = "properties";
	/**  小组 */
	public static final String GROUP = "group";
	/**  应用列表 */
	public static final String SERVICEAPPLIST = "serviceAppList";
	/**  queryApps */
	public static final String QUERYAPPS = "queryApps";
	/**  系统 */
	public static final String SYSTEM = "system";
	/**  状态 */
	public static final String STATUS = "status";
    /**  master */
    public static final String MASTER = "master";
    public static final String COMMITS = "commits";
    public static final String MODIFIED = "modified";
    public static final String BEFORE = "before";
    public static final String ZEROS = "0000000000000000000000000000000000000000";
    /** 应用与实体关系表 service-config*/
    public static final String SERVICE_CONFIG = "service_config";
    /** 历史日志信息表 history-details */
    public static final String  HISTORY_DETAILS = "history_details";
    /** 配置文件 */
    public static final String  APPLICATION = "application";
    /** serviceGitId */
	public static final String SERVICEGITID = "serviceGitId";
	/** 修改字段列表 */
	public static final String FIELDS = "fields";
	/** 实体英文名 */
	public static final String ENTITYNAMEEN = "entityNameEn";
	/** 应用使用到的实体属性信息 */
	public static final String ENTITYFIELD = "entityField";
	/** 应用gitlabIds*/
	public static final String GITLABIDS = "gitlabIds";
	/** 应用所属小组*/
	public static final String GROUPNAME = "groupName";
	/** 应用列表*/
	public static final String SERVICELIST = "serviceList";
	/** 实体id*/
	public static final String ENTITYID = "entityId";
	/** 环境英文名*/
	public static final String ENVNAMES = "envNames";
	/** 日志列表*/
	public static final String HISTORYLIST = "historyList";
	/** 实体中文名 */
	public static final String ENTITYNAMECN = "EntityNameCn";
	/** 操作类型，0-编辑，1-新增，2-删除 */
	public static final String OPERATETYPE = "operateType";
	/** 描述 */
	public static final String DESC = "desc";
	/** 修改后的数据 */
	public static final String AFTER = "after";
	/** 行内应用负责人 */
	public static final String SPDBMANAGERS = "spdb_managers";
	/** 厂商应用负责人 */
	public static final String DEVMANAGERS = "dev_managers";
    /** 实体id集合 */
    public static final String ENTITYIDS = "entityIds";
    /** 页码 */
    public static final String PAGENUM = "pageNum";
    /** 页面大小 */
    public static final String PAGESIZE = "pageSize";
    /** 流水线id */
    public static final String PIPELINEID = "pipelineId";
    /** 流水线名称 */
    public static final String PIPELINENAME = "pipelineName";
    /** 流水线名称id */
    public static final String PIPELINENAMEID = "pipelineNameId";
    /** 流水线名称id */
    public static final String NAMEID = "nameId";
    /** 正常数据 */
	public static final String NORMAL = "normal";
	/** 异常数据 */
	public static final String ERROR = "error";

    /** 实体字段列表  */
    public static final String PROPERTIESLIST = "propertiesList";
    /** 原字段英文名  */
    public static final String OLDNAMEEN = "oldNameEn";
    /** 原字段中文名  */
    public static final String OLDNAMECN = "oldNameCn";
    /** 原是否必输 true=是  */
    public static final String OLDREQUIRED = "oldRequired";
    /** 新字段英文名  */
    public static final String NEWNAMEEN = "newNameEn";
    /** 新字段中文名  */
    public static final String NEWNAMECN = "newNameCn";
    /** 新是否必输 true=是  */
    public static final String NEWREQUIRED = "newRequired";
    /** 实体日志列表  */
    public static final String ENTITYLOGLIST = "entityLogList";
    /** 历史日志信息表 history-details */
    public static final String  ENTITY_LOG = "entity_log";

}

