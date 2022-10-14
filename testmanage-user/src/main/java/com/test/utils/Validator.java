package com.test.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 利用正则表达式验证数据
 */
public class Validator {

    /**
     * 正则表达式：邮箱
     */
    public static final String REGEX_EMAIL = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
    /**
     * 正则表达式：密码（数字，字母，符号至少任意两者组合，不少于8位数）
     */
    public static final String PASSWORD = "^(?![0-9]+$)(?![a-zA-Z]+$)(?!([^(0-9a-zA-Z)]|[()])+$)([^(0-9a-zA-Z)]|[()]|[a-z]|[A-Z]|[0-9]){8,}$";

   //	数字，字母，符号三者组合,不少于8位数
   //	public static final String PASSWOR="^(?:(?=.*[0-9].*)(?=.*[A-Za-z].*)(?=.*[\\W].*))[\\W0-9A-Za-z]{8,}$";
    /**
     * 正则表达式：手机号码
     */
    public static final String  PHONE="^((1[0-9]))\\d{9}$";

    public static boolean isEmail(String email) {
        Pattern p = Pattern.compile(REGEX_EMAIL);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static boolean isPassWord(String password) {
        Pattern p = Pattern.compile(PASSWORD);
        Matcher m = p.matcher(password);
        return m.matches();
    }
    public static boolean isPhone(String phone) {
        Pattern p = Pattern.compile(PHONE);
        Matcher m = p.matcher(phone);
        return m.matches();
    }

}
