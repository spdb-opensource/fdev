package com.spdb.fdev.fdevapp.base.utils;

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
	
	
	
	
	
}
