package com.spdb.fdev.base.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 利用正则表达式验证数据
 * */
public class Validator {
	
	/**
	 * 正则表达式：邮箱
	 * */
	public static final String REGEX_EMAIL="^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
	
	
	
	public static boolean isEmail(String email) {
		Pattern p = Pattern.compile(REGEX_EMAIL);
		Matcher m = p.matcher(email);
		return m.matches();
	}

	/**
	 * 校验变更版本version字段
	 * @param version
	 * @return
	 */
	public static boolean versionValidate(String version) {
		String[] str = version.split("_");
		String rex = "^[0-9]+$";
		//DOCKER_G06_20201226_1800_YZX
		if(str.length<=5){
			return true;
		}else{
			//DOCKER_G06_20201226_1800_YZ_X（允许）  或 //DOCK_ER_G06_20201226_1800_YZX （不允许）
			if(str[2].length()==8&&str[3].length()==4&&Pattern.matches(rex, str[2])&&Pattern.matches(rex, str[3])){
				return true;
			}else{
				return false;
			}
		}
	}
	
	
	
	
	
}
