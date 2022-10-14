package com.test.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.dict.ErrorConstants;
import com.test.testmanagecommon.exception.FtmsException;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * @description 常用工具类
 * @data 2017年12月31日 下午7:06:39
 * @author huxianjin
 * @version v1.0
 * @since v1.0
 */
public class MyUtils {
	
	private Random random;
	
	public static final String STANDARDDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * 判断对象为空，支持字符串，集合，数组
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Object obj) {
		if (obj == null)
			return true;
		else if (obj instanceof CharSequence)
			return ((CharSequence) obj).length() == 0;
		else if (obj instanceof Collection)
			return ((Collection) obj).isEmpty();
		else if (obj instanceof Map)
			return ((Map) obj).isEmpty();
		else if (obj.getClass().isArray())
			return Array.getLength(obj) == 0;

		return false;
	}

	/**
	 * 判断对象不为空，支持字符串，集合，数组
	 * @param obj
	 * @return
	 */
	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}
	
	/**
	 * md5加密
	 * @param plainText
	 * @return
	 */
	public static String md5(String plainText) {
		byte[] secretBytes = null;
		try {
			secretBytes = MessageDigest.getInstance("md5").digest(
					plainText.getBytes());
		} catch (NoSuchAlgorithmException e) {
			throw new FtmsException(ErrorConstants.MD5_ERROR);
		}
		String md5code = new BigInteger(1, secretBytes).toString(16);// 16进制数字
		// 如果生成数字未满32位，需要前面补0
		for (int i = 0; i < 32 - md5code.length(); i++) {
			md5code = new StringBuilder("0").append(md5code).toString();
		}
		return md5code;
	}
	
	/**
	 * 返回当前时间字符串，默认格式：yyyy-MM-dd HH:mm:ss
	 * @param pattern：自定义格式
	 * @return
	 */
	public static final String getCurrentDateString(String pattern) {
		if (pattern == null || "".equals(pattern.trim())) {
			pattern = STANDARDDATEPATTERN;
		}
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(new Date());
	}
	
	/**
	 * 返回当前时间字符串，格式：yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static final String getCurrentDateString() {
		SimpleDateFormat format = new SimpleDateFormat(STANDARDDATEPATTERN);
		return format.format( new Date());
	}
		
	/**
	 * 获得客户端IP
	 * @param request
	 * @return
	 */
	public static String getClientIp(HttpServletRequest request) {
		String remoteAddr = "";
		if (request != null){
			remoteAddr = request.getHeader("X-FORWARDEED-FOR");
			if(remoteAddr == null || "".equals(remoteAddr)){
				remoteAddr = request.getRemoteAddr();
			}
		}
		return remoteAddr;
	}

	public static String checkJsonString(Object o){
		if(o instanceof JSONNull){
			return "";
		}
		if(o==null){
			return "";
		}
		return (String)o;
	}
	
	/**
	 * 生成工单号
	 * 规则：TMSJOBNO_yyyyMMddHHmmssSSS+四位随机数
	 * @return
	 */
	public static String getJOBNO() {
		return getCurrentDateString("yyyyMMddHHmmssSSS")+getFourRandom();
	}
	
	/**
	 * 获取四位随机数
	 * @return
	 */
	public static String getFourRandom() {
		Random random = new Random();
		String four = random.nextInt(10000)+"";
		int randLength = four.length();
		if(randLength < 4){
			for (int i = 0; i < 4-randLength; i++) {
				four = "0"+four;
			}
		}
		return four;
	}
	
	/**
	 * 截取应用名称的前两段
	 * @return
	 */
	public static String getSysModuleEnName(String SysModuleEnName){
		String[] tmp=SysModuleEnName.split("-");
		return tmp[0];
	}
	
	
    public static <T> T mapToBean(Map map, Class<T> cls) throws Exception {
        JSONObject json = JSONObject.fromObject(map);
        return jsonToBean(json, cls);
    }

    public static Map beanToMap(Object cls) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(cls, Map.class);
    }
    
    /**
     * map转bean
     *
     * @return 实体bean
     * @throws Exception
     */
    public static <T> T jsonToBean(JSONObject json, Class<T> cls) throws Exception {
        JSONObject beanJson = new JSONObject();
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            String name = f.getName();
            if ("serialVersionUID".equals(name) || "_id".equals(name)) {
                continue;
            }
            if (json.containsKey(name)) {
                beanJson.put(name, json.get(name));
            }
        }
        return (T) JSONObject.toBean(beanJson, cls);
    }
    
	public static String formatDate(String str) {
		SimpleDateFormat sf = new SimpleDateFormat(str);
		Date date = new Date();
		return sf.format(date);
	}

	public static Date parseDate(String str, String date) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat(str);
		return sf.parse(date);
	}

}
