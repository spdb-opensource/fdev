package com.spdb.fdev.fdevapp.base.dict;

/**
 * Created with IntelliJ IDEA.
 * User: luotao
 * Date: 2019/2/27
 * Time: 下午4:52
 * To change this template use File | Settings | File Templates.
 */
public class Constant {
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

    //vipChannel.json的变量命名
    public static final String VC_NAME = "$NAME";
    public static final String VC_TIMESTAMP = "\"$TIMESTAMP\"";
    public static final String VC_COMMAND = "$COMMAND";
    public static final String VC_IMAGE = "$IMAGE";
    public static final String VC_PROJECT_NAME = "$PROJECT_NAME";
    public static final String VC_PROJECT_BRANCH = "$PROJECT_BRANCH";
    public static final String VC_PROJECT_URL = "$PROJECT_URL";
    public static final String VC_BUILD_STATE = "$BUILD_STATE";
    public static final String VC_PROJECTS_NAMESPACE = "$PROJECTS_NAMESPACE";
    public static final String VC_PROJECT_ID = "\"$PROJECT_ID\"";
    public static final String VC_METADATA = "metadata";
    public static final String CI_BUILD_STATE = "CI_BUILD_STATE";
    public static final String VC_TRIGGER_TIME = "TRIGGER_TIME";
    public static final String CI_COMMIT_REF_NAME = "CI_COMMIT_REF_NAME";
    public static final String CI_COMMIT_TAG = "CI_COMMIT_TAG";
    public static final String CI_PROJECT_DIR = "CI_PROJECT_DIR";
    public static final String CI_PIPELINE_ID = "CI_PIPELINE_ID";
    public static final String CI_API_V4_URL = "CI_API_V4_URL";
    public static final String CI_COMMIT_SHORT_SHA = "CI_COMMIT_SHORT_SHA";

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

    /**
     * 异步创建应用
     */
    public static final String SAVEBYASYNC = "saveByAsync";

    /**
     * 创建 应用
     */
    public static final String SAVE = "save";


    /**
     * 多模块maven骨架
     */
    public static final String MAVEN_MULTI_MODULE = "maven-multi-module";

    /**
     * 单模块maven骨架
     */
    public static final String MAVEN_SINGLE_MODULE = "maven-single-module";

    /**
     * vue骨架
     */
    public static final String VUE = "vue";

    /**
     * vue骨架映射持续集成
     */
    public static final String MOBCLI_VUE = "mobcli-vue";

    /**
     * Java容器化项目
     */
    public static final String CONTAINER_PROJECT = "容器化项目";

    /**
     * Java老版服务
     */
    public static final String OLD_VERSION_SERVICE = "老版服务";

    /**
     * 持续集成环境信息 sit
     */
    public static final String ENV_SIT = "sit";

    /**
     * 持续集成环境信息 rel
     */
    public static final String ENV_REL = "rel";

    /**
     * 骨架编码格式
     */
    public static final String GBK = "GBK";

    /**
     * 编码
     */
    public static final String ENCODING="encoding";

    /**
     * 镜像最新版本
     */
    public static final String FDEV_CAAS_BASE_IMAGE_VERSION = "FDEV_CAAS_BASE_IMAGE_VERSION";
    public static final String IMAGE = "image";
    public static final String GITLAB_ID = "gitlabId";

}
