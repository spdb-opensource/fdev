package com.csii.pe.spdb.common.util;

import java.io.UnsupportedEncodingException;

public class ChineseTo16 {
	
	public static void main(String[] args) {
		
		System.out.println("====:" + gbEncoding("【资源相关】小组未来一周的任务安排"));

	}
	
	public static String gbEncoding(final String gbString) {
		char[] utfByte = gbString.toCharArray();
		String unicodeBytes = "";
		for(int i = 0; i < utfByte.length; i++) {
			String hexB = Integer.toHexString(utfByte[i]);
			if(hexB.length() <= 2) {
				hexB = "00" + hexB;
			}
			unicodeBytes = unicodeBytes + "\\u" + hexB;
		}
		return unicodeBytes;
	}
	
	public static String toChineseHex(String s) {
		String ss = s;
		byte[] bt = new byte[0];
		try {
			bt = ss.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String s1 = "";
		for(int i = 0; i < bt.length; i++) {
			String tempStr = Integer.toHexString(bt[i]);
			if (tempStr.length() > 2) {
				tempStr = tempStr.substring(tempStr.length() - 2);
				s1 = s1 + tempStr + "";
			}
		}
		String s2 = s1.toUpperCase();
		StringBuffer bf = new StringBuffer();
		for(int i = 0; i < s2.length(); i++) {
			if(i % 4 == 0) {
				bf.append( "\\u" + s2.substring(i, i+4));
			}
		}
		System.out.println(s2);
		return bf.toString();
	}
	
}
