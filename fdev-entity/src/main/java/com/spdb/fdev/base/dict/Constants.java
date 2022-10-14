package com.spdb.fdev.base.dict;

public class Constants {

    private Constants() {
    }

    // sonar.json文件里的变量名
    public static final String SONAR_NAME = "$SONAR_NAME";
    public static final String SONAR_COMMAND = "$SONAR_COMMAND";
    public static final String SONAR_IMAGE_NAME = "$SONAR_IMAGE_NAME";
    public static final String TIMESTAMP = "\"$TIMESTAMP\"";
    public static final String PROIECT_ID = "\"$PROIECT_ID\"";
    public static final String PROJECT_NAME = "$PROJECT_NAME";
    public static final String BRANCH = "$BRANCH";
    public static final String SONAR_ENVIRONMENT_SLUG = "$SONAR_ENVIRONMENT_SLUG";


    // sonar扫描返回状态
    public static final String SUCCESS = "SUCCESS";
    public static final String PENDING = "PENDING";
    public static final String RETRY = "RETRY";

    /**  状态-未反馈 */
    public static final String N= "N";
    /**  状态-已反馈 */
    public static final String Y = "Y";

	/**  未完成 */
    public static final String UNFINISHED = "未完成";


    /**  获取当前组及其子组信息 */
    public static final String CHILDGROUP = "childGroup";
    /**  系统  */
    public static final String SYSTEM = "system";
    /**  应用 */
    public static final String SERVICE = "service";
    /**  string */
    public static final String STRING = "string";
    /**  createUser.userId */
    public static final String CREATEUSERUSERID = "createUser.userId";
    /**  已删除 */
    public static final String YES = "yes";
    /**   en */
    public static final String EN = "en";
    /**   0 */
	public static final String ZERO = "0";
	/**  properties文件注释前缀 */
    public static final String NOTE_PLACEHOLDER = "#";
    /** ERROR */
    public static final String ERROR = "#ERROR#";
    /**
     * 生成配置文件第一行内容
     */
    public static final String CONFIG_FILE_FIRST_LINE = "# gen to env ";
    /** 空模板 */
    public static final String EMPTY = "empty";
    /** 自定义实体 */
    public static final String EMPTYTEMPLATE = "自定义实体";
    /** 使用中应用 */
    public static final String USE = "use";
    /** gitlab-ci/fdev-application */
    public static final String GITLAB_CI_FDEV_APPLICATION = "gitlab-ci/fdev-application.properties";
    /** ci/fdev-application */
    public static final String CI_FDEV_APPLICATION = "ci/fdev-application.properties";
}
