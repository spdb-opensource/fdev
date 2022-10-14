package com.spdb.fdev.base.dict;

public class Constants {

    /**
     * 组件类型-单模块组件、1-多模块组件父级模块、2-多模块组件子模块
     */
    public static final String SINGLE_COMPONENT = "0";

    /**
     * 组件类型-多模块组件父级模块
     */
    public static final String PARENT_COMPONENT = "1";

    /**
     * 组件类型-多模块组件子模块
     */
    public static final String CHILD_COMPONENT = "2";

    /**
     * 组件类型-多模块组件
     */
    public static final String MULTI_COMPONENT = "3";


    public static final String REDISSON_LOCK_NAMESPACE = "lock";

    /**
     * 组件历史版本类型,正式版本
     */
    public static final String RECORD_OFFICIAL_TYPE = "0";

    /**
     * 组件历史版本类型,推荐版本
     */
    public static final String RECORD_RECOMMEND_TYPE = "1";

    /**
     * 组件历史版本类型,废弃版本
     */
    public static final String RECORD_DISCARD_TYPE = "2";

    /**
     * 组件历史版本类型,测试版本
     */
    public static final String RECORD_TEST_TYPE = "3";

    /**
     * 组件历史版本，最新版本
     */
    public static final String LATEST = "0";


    /**
     * 组件历史版本，非最新版本
     */
    public static final String NOTLATEST = "1";

    /**
     * 组件优化需求状态，已发布
     */
    public static final String STAGE_PASSED = "4";


    /**
     * 骨架优化需求状态，已发布
     */
    public static final String ARCHETYPE_STAGE_PASSED = "3";

    /**
     * python 版本 2
     */
    public static final String PYTHON_VERSION_TWO = "2";

    /**
     * python 版本 3
     */
    public static final String PYTHON_VERSION_THREE = "3";

    /**
     * SNAPSHOT
     */
    public static final String SNAPSHOT = "SNAPSHOT";

    /**
     * snapshot
     */
    public static final String SNAPSHOT_LOWCASE = "snapshot";

    /**
     * RC
     */
    public static final String RC = "RC";

    /**
     * rc
     */
    public static final String RC_LOWCASE = "rc";

    /**
     * RELEASE
     */
    public static final String RELEASE = "RELEASE";


    /**
     * RELEASE
     */
    public static final String _RELEASE = "-RELEASE";

    /**
     * release
     */
    public static final String RELEASE_LOWCASE = "release";

    /**
     * PREPACKAGE
     * 在打包时就存储为历史记录，但是标记为提前打包
     */
    public static final String PREPACKAGE = "prepackage";

    /**
     * PIPEPACKAGE
     * Pipeline回调时设置状态，表示为Pipe成功后的打包
     */
    public static final String PIPEPACKAGE = "pipepackage";

    /**
     * source 组件来源 组内维护
     */
    public static final String INNER_SOURCE = "0";

    /**
     * source 组件来源 第三方
     */
    public static final String THIRD_PARTY_SOURCE = "1";

    public static final String SUBJECT = "【组件更新通知】";

    public static final String SUBJECT_DESTORY = "【组件废弃通知】";

    /**
     * 组件模块 组件
     */
    public static final String COMPONENT_COMPONENT = "component";

    /**
     * 组件模块 骨架
     */
    public static final String COMPONENT_ARCHETYPE = "archetype";

    /**
     * 组件模块 镜像
     */
    public static final String COMPONENT_IMAGE = "image";

    /**
     * 组件模块 mpass组件
     */
    public static final String COMPONENT_MPASS_COMPONENT = "mpass_component";

    /**
     * 骨架优化需求状态，开发
     */
    public static final String ARCHETYPE_STAGE_SNAPSHOT = "0";

    /**
     * 骨架优化需求状态，内测
     */
    public static final String ARCHETYPE_STAGE_ALPHA = "1";

    /**
     * 骨架优化需求状态，正式发布
     */
    public static final String ARCHETYPE_STAGE_RELEASE = "2";

    /**
     * 镜像版本变量
     */
    public static final String FDEV_CAAS_BASE_IMAGE_VERSION = "FDEV_CAAS_BASE_IMAGE_VERSION";

    /**
     * gitlab分支
     */
    public static final String TAG = "tag";

    /**
     * alpha mpass组件版本标识
     */
    public static final String ALPHA = "alpha";

    /**
     * -alpha
     */
    public static final String _ALPHA = "-alpha";

    /**
     * beta
     */
    public static final String BETA = "beta";

    /**
     * -beta
     */
    public static final String _BETA = "-beta";

    /**
     * -rc
     */
    public static final String _RCLOWCASE = "-rc";

    /**
     * vue项目
     */
    public static final String VUE = "Vue";


    public static final String VUE_LOWCASE = "vue";

    /**
     * webhook返回项目地址
     */
    public static final String WEB_URL = "web_url";

    /**
     * mpass新增优化需求标识
     */
    public static final String MPASS_RELEASE = "mpass_release";

    /**
     * mpass新增需求标志
     */
    public static final String MPASS_DEV = "mpass_dev";

    /**
     * tag标识
     */
    public static final String VERSION_TAG = "v";

    /**
     * 扫描Java项目标识
     */

    public static final String JAVA_APP = "JAVA_APP";

    /**
     * 扫描Vue项目标识
     */
    public static final String VUE_APP = "VUE_APP";

    /**
     * 扫描应用使用组件标识
     */
    public static final String SCAN_APP_COMPONENT = "ScanAppComponent";

    /**
     * 扫描应用使用镜像标识
     */
    public static final String SCAN_APP_IMAGE = "ScanAppImage";

    /**
     * 扫描应用使用Vue组件情况
     */
    public static final String Scan_App_MpassComponent = "ScanAppMpassComponent";

    /**
     * 开发阶段
     */
    public static final String DEV = "开发中";

    /**
     * 拉取tag阶段
     */
    public static final String PUSH_TAG = "拉取tag";

    /**
     * 新增需求阶段
     */
    public static final String INCREASE = "新增";

    /**
     * 已发布
     */
    public static final String DEV_END = "已发布";

    /**
     * 开发测试阶段
     */
    public static final String DEV_TEST = "开发测试";
    /**
     * 测试发布阶段
     */
    public static final String TEST_END = "测试发布";
    /**
     * 已发布状态
     */
    public static final String FOUR = "4";
    /**
     * 后端组件
     */
    public static final String COMPONENT_TYPE = "0";
    /**
     * 前端组件
     */
    public static final String MPASS_COMPONENT_TYPE = "1";
    /**
     * 后端骨架
     */
    public static final String ARCHETYPE_TYPE = "2";
    /**
     * 前端骨架
     */
    public static final String MPASS_ARCHETYPE_TYPE = "3";
    /**
     * 基础镜像
     */
    public static final String IMAGE_TYPE = "4";

}