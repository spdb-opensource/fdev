package com.test.dict;

public class Constants {

	// 交易成功返回码
	public static final String I_SUCCESS = "AAAAAAA";
	// 交易成功响应信息
	public static final String M_SUCCESS = "交易执行成功";
	// 交易失败返回码
	public static final String I_FAILED = "00000";
	// 交易响应码
	public static final String CODE = "code";
	// 交易响应信息
	public static final String MESSAGE = "msg";
	// 交易响应数据
	public static final String DATA = "data";
	// nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在是才进行set，如果取XX，则只有当key已经存在时才进行set
	public static final String REDIS_NX = "nx";
	public static final String REDIS_XX = "xx";
	// expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
	public static final String REDIS_EX = "ex";
	public static final String REDIS_PX = "px";
	// 用户登录验证token
	public static final String USER_TOKEN = "userToken";
	// dateFormat yyyy-mm-dd HH:mm:ss
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	// 用户英文名
	public static final String USER_EN_NAME = "user_en_name";
	// 用户权限
	public static final String USER_ROLE = "user_role";

	public static final String REDIS_USER_TOKEN = "tuser.LoginToken";

	public static final String FDEV_DEVELOPER_ROLE_ID = "5c41796ca3178a3eb4b52001";

	/** 消息已读 */
	public static final String MESSAGE_READ = "1";
	/** 消息未读 */
	public static final String MESSAGE_UNREAD = "0";
	/** 固定过期时间 */
	public static final String ALWAYSEXPIRYTIME="2030-01-01 00:00:00";

	/** 零售组维度 */
	public static final String RETAILGROUP="财富-金牛座,微生活-白羊座,互联-双子座,转账-天平座,app-水瓶座,账户-射手座,生态-巨蟹座";
   /** 测试大组维度(排除组) */
	public static final String TESTGROUP="个人金融领域,公司金融领域,资源池";
	/**合并组**/
	public static final String MERGEGROUP="公司组-北极星,移动pad组";

	public static final String MESSAGE_TYPE_FILE_NAME="config/messageType.properties";

}
