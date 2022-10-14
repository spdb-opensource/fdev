package com.spdb.fdev.fuser.base.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

public class MD5 {
	
	public static String encoder(String salt, String psd) {
		try {
			StringBuilder sb = new StringBuilder();
			//1.指定加密算法
//			MessageDigest digest = MessageDigest.getInstance("MD5");
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			//2.将需要加密的字符串转换为byte，然后哈西算法
			byte[] bs = digest.digest((salt + psd).getBytes());
			//3.遍历，让其生成32位字符串，固定写法
			for(byte b : bs) {
				int i = b & 0xff;
				String hexString = Integer.toHexString(i);
				if(hexString.length() < 2) {
					hexString = "0" +hexString;
				}
				
				sb.append(hexString);
 			}
			return sb.toString();
		}catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String ldapEncoderByMd5(String password) {
        MessageDigest md5 = null;
        String md5pwd = null;
        try {
//                md5 = MessageDigest.getInstance("MD5");
                md5 = MessageDigest.getInstance("SHA-512");
                md5pwd = Base64Utils.encodeToString(md5.digest(password.getBytes("utf-8")));
        } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("No MD5 algorithm.", e);
        } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Unsupported encoding utf-8.", e);
        }
        return "{MD5}" + md5pwd;
	}
	
	public static void main(String[] args) {
		String s1 = MD5.encoder("xxx", "xxx");
		System.out.println(s1);
	}

}
