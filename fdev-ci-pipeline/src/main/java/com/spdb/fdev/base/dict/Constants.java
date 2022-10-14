package com.spdb.fdev.base.dict;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    private Constants() {
    }

    /**
     * 数字1
     */
    public static final String ONE = "1";
    /**
     * 数字0
     */
    public static final String ZERO = "0";
    /**
     * 失效
     */
    public static final String STATUS_CLOSE = "0";
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
     * up version
     */
    public static final String VERSION = "version";
    /**
     * pipeline 执行缓存
     */
    public static final String PIPELINE_EXE_REDIS_KEY_PROFIX = "pipeline.variables.";

    /**
     * pipeline 执行缓存
     */
    public static final String PIPELINE_JOE_EXE_LOG_KEY_PROFIX = "pipeline.job.log.";

    /**
     * job实例队列缓存
     */
    public static final String JOB_EXE_QUEUE_REDIS_KEY_PROFIX = "job.queue";

    /**
     * 应用负责人列表缓存
     */
    public static final String PROJECT_MANAGER_REDIS_KEY_PROFIX = "project.manager.";

    public static final String UP_CHANGE_VERSION = "1";
    /**
     * up version
     */
    public static final String VERSION_FORMAT = "0.0";
    /**
     * 环境配置管理员roleId
     * */
    public static final String ENVIRONMENT_ADMIN_ID = "5d72123ca68d0e00099d222e";
    /**
     * 默认版本号1.0.0
     * */
    public static final String DEFAULT_VERSION = "1.0.0";
    /**
     * 默认给的文件名称
     * */
    public static final String SCRIPT_NAME = "app.sh";
    /**
     * golang语言
     * */
    public static final String LANGUAGE = "golang";
    /**
     * 插件类型：0-系统提供1-用户自定义
     * */
    public static final String TYPE_0 = "0";

    public static final String TYPE_1 = "1";
    /**
     * 是否收藏：0-收藏1-取消收藏
     * */
    public static final String state_0 = "0";
    public static final String state_1 = "1";
    /**
     * 插件查询条件：0-全部 1-我的
     * */
    public static final String pluginType_0 = "0";
    public static final String pluginType_1 = "1";

    /**
     * 插件的分类类型
     * */
    public static final String CATEGORY_OTHER = "其他";

    /**
     * 基础镜像
     * */
    public static final String IMAGE_BASE = "基础镜像";
    /**
     * 自定义插件
     * */
    public static final String CATEGORY_DEFINE = "自定义";
    /**
     * runner的revision
     * */
    public static final String RUNNER_REVISION_FIRST = "001";
    /**
     * runner的revision
     * */
    public static final String RUNNER_REVISION = "revision";
    /**
     * decimalFormat的patten  适用于最大六位数
     *
     * */
    public static final String DC_PATTEN = "###000";

    /**
     * 脚本标签
     * */
    public static final String SCRIPT_LABEL = "脚本";

    /**
     * 脚本在minio目录前缀
     */

    public static final String SCRIPT_PREFIX = "script/";

    /**
     *成功
     */
    public static final String SUCCESS = "SUCCESS";
    /**
     * 失败
     */
    public static final String FAIL = "FAIL";
    /*
    * 蓝鲸插件和sonnar扫描插件下的分类的id
    * */
    public static final String[] pluginCategoryList = {"60388de1e18171d090404fb5", "60388d27e18171d09040100a"};
    /**
     * 组类型、处中心1
     */
    public static final String GROUPTYPEONE = "1";

    /**
     * 组类型、二级处2
     */
    public static final String GROUPTYPETWO = "2";

    /**
     * 组类型、条线3
     */
    public static final String GROUPTYPETHREE = "3";
    /**
     * 平台：linux
     */
    public static final String PLATFORM_LINUX = "linux";
    /**
     * 平台：windows
     */
    public static final String PLATFORM_WINDOWS = "windows";
    /**
     * 平台：darwin
     */
    public static final String PLATFORM_DARWIN = "darwin";
}