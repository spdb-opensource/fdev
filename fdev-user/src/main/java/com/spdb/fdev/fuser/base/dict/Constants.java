package com.spdb.fdev.fuser.base.dict;

public class Constants {

    //邮件提醒-用户完成首次登录
    public static final String EMAIL_ONECELOGIN = "【开发协作服务】请完成用户的首次登录";
    public static final String TEMPLATE_ONECELOGIN = "fuser_oncelogin";
    //忘记密码-邮件发验证码
    public static final String EMAIL_FORGETPW = "【开发协作服务】忘记密码-用户所需验证码";
    public static final String TEMPLATE_FORGETPW = "fuser_forgetPw";
    public static final String FRAMEWORK_ADMIN = "基础架构管理员";
    public static final String ENV_ADMIN = "环境配置管理员";
    public static final String SUPER_MANAGER = "超级管理员";
    public static final String GROUP_MANAGER = "小组管理员";
    public static final String TEAM_LEADER = "团队负责人";
    public static final String USER_MANAGER = "用户管理员";
    public static final String NOPRORESOURCE = "非项目组资源";
    public static final String PLANDEPT = "规划部门";
    public static final String BUSINESSDEPT = "业务部门";
    public static final String TESTCENTRE = "测试中心";
    public static final String DEVELOPER = "开发人员";
    public static final String TESTER = "测试人员";
    public static final String NET_APPROVER = "网络审核员";
    public static final String NET_APPROVAL = "【开发协作服务】网络审核提醒";
    // 接入统一认证平台
    public static final String LDAP_CHANNEL = "0";  // 通过LDAP认证登陆的用户，行内用户
    public static final String STANDARD_CHANNEL = "1"; // 通过STANDARD方式认证的，厂商用户
    public static final String NOT_RIGHT_LOGIN_CHANNEL = "请使用LDAP方式进行登陆";
    public static final String IS_ONCE_LOGIN = "4";   // 行内用户首次登陆标识，通过LDAP
    public static final String MEMBER = "人员";  //权限
    public static final String MANUROLE = "开发人员,测试人员";  //厂商可用初始角色
    public static final String IPMP_USER = "ipmp_user";  //ipmp用户数据
    public static final String TEST_MANAGER_USER = "test_manager_user";  //XTest用户数据
    /**  ipmp牵头单位、团队  */
    public static final String IPMP_TEAM = "ipmp_team";
    /**  ipmp组织机构数据  */
    public static final String IPMP_DEPT = "ipmp_dept";
    public static final String IPMP_VERSION = "1.0";
    /** ipmp接口查询牵头单位团队信息 */
    public static final String IPMP_METHOD_GETHEADDEPTANDTEAM = "/baseImplUnit/getHeadDeptAndTeam";
    /** ipmp接口查询组织机构信息 */
    public static final String IPMP_METHOD_GETDEPTORGS = "/dept/getDeptOrgs";
    /** ipmp接口查询ipmp用户信息 */
    public static final String IPMP_METHOD_GETUSERS = "/userSync/getUsers";
    /** 用户状态，0-在职 */
    public static final String USER_STATUS_IN = "0";
    /** 测试中心团队 */
    public static final String BUSINESS_TEST_DEPT_1 = "测试服务中心";
    public static final String BUSINESS_TEST_DEPT_2 = "武汉支付测试组";
    public static final String BUSINESS_TEST_DEPT_3 = "测试服务处";
    public static final String BUSINESS_TEST_DEPT_4 = "测试实施处";
    private Constants() {
        super();
    }

}
