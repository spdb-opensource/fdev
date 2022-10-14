package com.spdb.fdev.fdevenvconfig.base.dict;

public class Constants {

    private Constants() {
    }

    /**
     * 字符串必须是大小写字母数字和'_'组成；用于实体模板英文名、实体模板属性英文名
     */
    public static final String PATTERN_MODEL_TEMPLATE_NAME_EN = "^[a-zA-Z0-9\\_]+$";
    /**
     * 字符串必须是汉字大小写字母数字和'_'组成，至少一个中文字符；用于实体模板中文名、
     */
    public static final String PATTERN_MODEL_TEMPLATE_NAME_CN = "^[\u4E00-\u9FA5A-Za-z0-9_]*[\u4E00-\u9FA5]{1,}[\u4E00-\u9FA5A-Za-z0-9_]*$";



    /**
     * 字符串必须是小写字母和数字组成
     */
    public static final String PATTERN_MODEL_FILTER = "^[a-zA-Z0-9]+$";
    /**
     * 字符串必须是小写字母数字和'_'组成
     */
    public static final String PATTERN_MODEL_NAME_EN = "^[a-zA-Z0-9\\_]+$";
    /**
     * 字符串必须是小写字母数字和'-'组成
     */
    public static final String PATTERN_SUFFIX_NAME = "^[a-zA-Z0-9\\-]+$";
    /**
     * 字符串必须是大小写字母数字和特殊字符._-组成,且第一字符必须的字母
     */
    public static final String PATTERN_ENV_NAME_EN = "^[a-zA-Z]{1}[a-zA-Z0-9\\.\\-\\_]*$";

    /**
     * 配置模板预览 请求参数 环境英文名
     */
    public static final String ENV_NAME = "env_name";
    /**
     * 配置文件依赖搜索 请求参数 实体英文名
     */
    public static final String MODEL_NAME_EN = "model_name_en";
    /**
     * 配置文件 依赖搜索 请求参数 实体属性字段 key
     */
    public static final String FIELD_NAME_EN = "field_name_en";
    /**
     * 配置文件 依赖搜索 请求参数 搜索范围
     */
    public static final String RANGE = "range";
    /**
     * 更新配置模板文件提交信息
     */
    public static final String COMMIT_MESSAGE = "update config file";

    /**
     * 实体 的状态表示字段 1代表正常,0代表删除 -1代表未启用
     */
    public static final String ONE = "1";

    /**
     * 用于过滤序列化id
     */
    public static final String SERIALVERSIONUID = "serialVersionUID";
    /**
     * 失效
     */
    public static final String STATUS_LOSE = "0";
    /**
     * 正常
     */
    public static final String STATUS_OPEN = "1";
    /**
     * AND
     */
    public static final String AND = "and";
    /**
     * OR
     */
    public static final String OR = "or";
    /**
     * NULL
     */
    public static final String NULL = "null";
    /**
     * id
     */
    public static final String ID = "id";
    /**
     * ObjectId类型的_id
     */
    public static final String OBJECTID = "_id";
    /**
     * 描述信息
     */
    public static final String DESC = "desc";
    /**
     * 中文名称
     */
    public static final String NAME_CN = "name_cn";
    /**
     * 英文名称
     */
    public static final String NAME_EN = "name_en";
    /**
     * 作用域
     */
    public static final String SCOPE = "scope";
    /**
     * 状态
     */
    public static final String STATUS = "status";
    /**
     * 模糊条件的参数
     */
    public static final String TERM = "term";
    /**
     * 类型
     */
    public static final String TYPE = "type";
    /**
     * 参数value
     */
    public static final String VALUE = "value";
    /**
     * 参数列表
     */
    public static final String VARIABLES = "variables";
    /**
     * env_id
     */
    public static final String ENV_ID = "env_id";
    /**
     * model_id
     */
    public static final String MODEL_ID = "model_id";
    /**
     * variables.id
     */
    public static final String VARIABLES_ID = "variables.id";
    /**
     * env
     */
    public static final String ENV = "env";
    /**
     * version
     */
    public static final String VERSION = "version";
    /**
     * 一级分类
     */
    public static final String FIRST_CATEGORY = "first_category";
    /**
     * 二级分类
     */
    public static final String SECOND_CATEGORY = "second_category";
    /**
     * 后缀名
     */
    public static final String SUFFIX_NAME = "suffix_name";
    /**
     * category
     */
    public static final String CATEGORY = "category";
    /**
     * 创建时间
     */
    public static final String CTIME = "ctime";
    /**
     * 更新时间
     */
    public static final String UTIME = "utime";
    /**
     * 操作员
     */
    public static final String OPNO = "opno";
    /**
     * _USER
     */
    public static final String _USER = "_USER";
    /**
     * model-env
     */
    public static final String MODEL_ENV = "model-env";
    /**
     * env_name_en
     */
    public static final String ENV_NAME_EN = "env_name_en";
    /**
     * env_desc
     */
    public static final String ENV_DESC = "env_desc";
    /**
     * model
     */
    public static final String MODEL = "model";
    /**
     * model_version
     */
    public static final String MODEL_VERSION = "model_version";
    /**
     * model_desc
     */
    public static final String MODEL_DESC = "model_desc";
    /**
     * require
     */
    public static final String REQUIRE = "require";
    /**
     * name
     */
    public static final String NAME = "name";
    /**
     * gitlab_project_id
     */
    public static final String GITLAB_PROJECT_ID = "gitlab_project_id";
    /**
     * env_key
     */
    public static final String ENV_KEY = "env_key";
    /**
     * env_key.name_en
     */
    public static final String ENV_KEY_NAME_EN = "env_key.name_en";
    /**
     * env_key.name_cn
     */
    public static final String ENV_KEY_NAME_CN = "env_key.name_cn";
    /**
     * env_key.require
     */
    public static final String ENV_KEY_REQUIRE = "env_key.require";
    /**
     * env_key.type
     */
    public static final String ENV_KEY_TYPE = "env_key.type";
    /**
     * environment
     */
    public static final String ENVIRONMENT = "environment";
    /**
     * 分支
     */
    public static final String BRANCH = "branch";
    /**
     * 用户英文名key/用户idkey
     */
    public static final String keys = "keys";
    /**
     * 数据data
     */
    public static final String DATA = "data";
    /**
     * Ids/usernameEns
     */
    public static final String KEY = "key";
    /**
     * 项目英文名
     */
    public static final String NAME_ZH = "name_zh";
    /**
     * 多个用户查询信息点的key
     */
    public static final String USERNAMERNS = "usernameEns";
    /**
     * 外部参数配置模版应用存储的应用key
     **/
    public static final String APPKEY = "appkey";
    /**
     * 生产分支
     */
    public static final String PRO = "pro";
    /**
     * 投产验证环境标签
     */
    public static final String TCYZ = "tcyz";
    /**
     * 标签
     */
    public static final String LABELS = "labels";
    /**
     * rel
     */
    public static final String REL = "rel";
    /**
     * 0
     */
    public static final String ZERO = "0";
    /**
     * 外部参数配置表
     */
    public static final String OUT_SIDE_TEMPLATE = "out_side_template";

    /**
     * 生成的配置文件前缀
     */
    public static final String CONFIG_FILE_PRE = "配置文件依赖";

    /**
     * fenvconfig
     */
    public static final String FENVCONFIG = "fenvconfig";

    public static final String COMM = "comm";
    /**
     * env_variables
     */
    public static final String ENV_VARIABLES = "env_variables";
    /**
     * var_list
     */
    public static final String VAR_LIST = "var_list";
    /**
     * ci
     */
    public static final String CI = "ci";
    /**
     * 部署时，大key-小key映射需要的gitlabId，是gitlab上的项目id
     */
    public static final String GITLAB_ID = "gitlabId";
    /**
     * 部署时的大key：项目需要的属性名
     */
    public static final String CI_KEY = "ci_key";
    /**
     * 部署时的小key：实体.属性
     */
    public static final String MODEL_KEY = "model_key";
    /**
     * master分支
     */
    public static final String MASTER = "master";

    public static final String DEPLOY = "deploy";
    public static final String APP_ID = "app_id";

    public static final String FILEDIR = "filedir";
    public static final String YAML_FILE_DIR = "yaml_file_dir";

    public static final String CONTENT = "content";
    public static final String DCE = "dce";
    public static final String FDEV_CAAS_SERVICE_REGISTRY = "fdev_caas_service_registry";

    /**
     * 生成配置文件第一行内容
     */
    public static final String CONFIG_FILE_FIRST_LINE = "# gen to env ";

    /**
     * 邮件主题
     */
    public static final String EMAIL_SUBJECT_MODEL_DELETE = "【开发协作服务】【请关注】环境配置模块实体删除";
    public static final String EMAIL_SUBJECT_MODEL_UPDATE = "【开发协作服务】【请关注】环境配置模块实体属性字段删除";
    public static final String EMAIL_SUBJECT_MODEL_ENV_DELETE = "【开发协作服务】【请关注】环境配置模块实体与环境映射删除";
    public static final String EMAIL_SUBJECT_MODEL_ENV_UPDATE = "【开发协作服务】【请关注】环境配置模块实体与环境映射更新";
    public static final String EMAIL_MODEL_ENV_UPDATE_APP = "【开发协作服务】【请5天内核对确认】环境配置模块 实体-环境映射值变动单";

    /**
     * 邮件模板
     */
    public static final String TEMPLATE_SUBJECT_MODEL_UPDATE = "subjectmodel_update";
    public static final String TEMPLATE_SUBJECT_MODEL_DELETE = "subjectmodel_delete";
    public static final String TEMPLATE_SUBJECT_MODEL_ENV_UPDATE = "subjectmodelenv_update";
    public static final String TEMPLATE_SUBJECT_MODEL_ENV_DELETE = "subjectmodelenv_delete";
    public static final String TEMPLATE_MODEL_ENV_UPDATE_APP = "modelenvupdate_app";

    public static final String UP_VERSION = "0.1";
    public static final String ERROR = "#ERROR#";


    public static final String PARAM_CAN_NOT_NULL = "必传参数不能为空";
    public static final String FDEV_APPLICATION = "gitlab-ci/fdev-application.properties";

    public static final String JSON_SCHEMA_ID = "json_schema_id";


    public static final String APP_ENV_MAP = "app-env-map";
    public static final String FDEV_CAAS_USER = "fdev_caas_user";

    public static final String FDEV_CAAS_REGISTRY_PD_UP = "FDEV_CAAS_REGISTRY_PASSWORD";
    public static final String FDEV_CAAS_REGISTRY_USER_UP = "FDEV_CAAS_REGISTRY_USER";
    public static final String DOCKERSERVICE_USER = "dockerservice_user";
    public static final String DOCKERSERVICE_PASSWD= "dockerservice_passwd";

    /**
     * Tags表信息
     */
    public static final String TAGS = "tags";
    /**
     * app_deploy_map表信息
     */
    public static final String APP_DEPLOY_MAP = "app-deploy-map";

    // properties文件注释前缀
    public static final String NOTE_PLACEHOLDER = "#";

    //caas标签
    public static final String CAAS = "caas";

    //scc标签
    public static final String SCC = "scc";

}