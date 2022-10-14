package com.spdb.fdev.fdevfootprint.base.utils;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.spdb.fdev.fdevfootprint.base.dict.Constants;
import com.spdb.fdev.fdevfootprint.spdb.entity.SwitchModel;

import io.netty.handler.codec.http.HttpRequest;

public class CommonUtil {
	/**
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		if (str != null && !"".equals(str) && !"null".equals(str)) {
			return false;
		}
		return true;
	}

	/**
	 * 判断请求来源是否在白名单内
	 * 
	 * @param referer
	 * @return
	 */
	public static boolean checkRefererInWhiteList(String referer, List<String> whiteList) {
		boolean via = false;
		if (!CommonUtil.isEmpty(referer) && whiteList != null && whiteList.size() > 0) {
			for (String site : whiteList) {
				if (referer.startsWith(site)) {
					via = true;
					break;
				}
			}
		}
		return via;
	}

	/**
	 * 获取IP地址
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpRequest request, SocketAddress address) {
		String ipAddress = request.headers().get(Constants.X_FORWARDED_FOR);
		if (CommonUtil.isEmpty(ipAddress)) {
			InetSocketAddress insocket = (InetSocketAddress) address;
			ipAddress = insocket.getAddress().getHostAddress();
		}
		return ipAddress;
	}

	/**
	 * 将ISO8859-1编码转换为UTF8
	 * 
	 * @param str
	 * @return
	 */
	public static String convertUTF8Str(String str) {
		String string = "";
		if (!CommonUtil.isEmpty(str)) {
			try {
				string = new String(str.getBytes("ISO8859-1"), "UTF-8");
			} catch (UnsupportedEncodingException e) {}
		}
		return string;
	}

	/**
	 * 获取当前时间字符串
	 * 
	 * @return
	 */
	public static String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		return format.format(new Date());
	}

	/**
	 * 根据IP判断渠道对应的全量采集是否开启 百分比为0，直接返回关闭状态
	 * 
	 * @param newFootDomain
	 * @param ip
	 * @param host
	 * @param switchModel
	 * @return
	 */
	public static boolean checkAutoSwitchViaIP(String newFootDomain, String ip, String host, SwitchModel switchModel) {
		boolean via = false;
		if (switchModel != null) {
			if (host.startsWith(newFootDomain) && !CommonUtil.isEmpty(ip) && !CommonUtil.isEmpty(switchModel.getAutoPercent())) {
				int percent = Integer.parseInt(switchModel.getAutoPercent());
				if (percent > 0) {
					int hashCode = ip.hashCode();
					int h = hashCode % 100;
					h = h > 0 ? h : h * -1;
					if (h <= percent) {
						via = true;
					}
				}
			}
		}
		return via;
	}

	/**
	 * 根据IP判断渠道对应的全量采集是否开启 百分比为0，直接返回关闭状态
	 * 
	 * @param ip
	 * @param switchModel
	 * @return
	 */
	public static boolean checkHandSwitchViaIP(String ip, SwitchModel switchModel) {
		boolean via = false;
		if (switchModel != null) {
			if (!CommonUtil.isEmpty(ip) && !CommonUtil.isEmpty(switchModel.getStatus())) {
				int percent = Integer.parseInt(switchModel.getStatus());
				if (percent > 0) {
					int hashCode = ip.hashCode();
					int h = hashCode % 100;
					h = h > 0 ? h : h * -1;
					if (h <= percent) {
						via = true;
					}
				}
			}
		}
		return via;
	}

	/**
	 * 根据IP判断渠道对应的全量采集是否开启 百分比为0，直接返回关闭状态
	 * 
	 * @param ip
	 * @param switchModel
	 * @return
	 */
	public static boolean checkAutoSwitchViaIP(String ip, SwitchModel switchModel) {
		boolean via = false;
		if (switchModel != null) {
			if (!CommonUtil.isEmpty(ip) && !CommonUtil.isEmpty(switchModel.getAutoPercent())) {
				int percent = Integer.parseInt(switchModel.getAutoPercent());
				if (percent > 0) {
					int hashCode = ip.hashCode();
					int h = hashCode % 100;
					h = h > 0 ? h : h * -1;
					if (h <= percent) {
						via = true;
					}
				}
			}
		}
		return via;
	}
}
