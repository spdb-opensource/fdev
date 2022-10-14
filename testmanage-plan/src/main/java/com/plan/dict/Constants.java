package com.plan.dict;

public class Constants {

    //交易成功返回码
    public static final String I_SUCCESS = "AAAAAAA";
    //交易成功响应信息
    public static final String M_SUCCESS = "交易执行成功";
    //交易失败返回码
    public static final String I_FAILED = "00000";
    //交易响应码
    public static final String CODE = "code";
    //交易响应信息
    public static final String MESSAGE = "msg";
    //交易响应数据
    public static final String DATA = "data";
    // nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在是才进行set，如果取XX，则只有当key已经存在时才进行set
    public static final String REDIS_NX = "nx";
    public static final String REDIS_XX = "xx";
    //expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
    public static final String REDIS_EX = "ex";
    public static final String REDIS_PX = "px";
    //用户登录验证token
    public static final String USER_TOKEN = "userToken";
    //dateFormat yyyy-mm-dd HH:mm:ss
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    //用户英文名
    public static final String USER_EN_NAME = "user_en_name";
    //用户权限
    public static final String USER_ROLE = "user_role";


    //邮件 更新工单 key
    public static final String ADDMANTIS = "email.task.addMantis";
    //邮件 更新工单 key
    public static final String UPDATEMANTIS = "email.task.updateMantis";
    //邮件， 更新人
    public static final String UPDATETASKUSER = "updateTaskUser";
    //邮件 主题
    public static final String SUBJECT = "subject";
    //邮件 发送给人
    public static final String TO = "to";
    //上下文
    public static final String CONTENT = "content";
    //邮件 模板名
    public static final String TEMPLATE_NAME = "template_name";
    //消息类型—缺陷通知
    public static final String MANTISNOTICE =  "缺陷通知";
    //默认0
    public static final String DEFAULT_0 =  "0";
}
