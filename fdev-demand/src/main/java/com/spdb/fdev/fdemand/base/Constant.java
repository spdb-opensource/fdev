package com.spdb.fdev.fdemand.base;

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


}
