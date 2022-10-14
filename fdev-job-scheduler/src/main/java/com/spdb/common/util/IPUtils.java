package com.spdb.common.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPUtils {
	private static Logger logger = LoggerFactory.getLogger(IPUtils.class);

	private static final String[] PROXY_REMOTE_IP_ADDRESS = {"X-Forwarded-For","Proxy-Client-IP","WL-Proxy-Client-IP","X-Real-IP"};
	/**
	 * 获取IP地址
	 * 
	 * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
	 * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
	 */
	public static String getIpAddr(HttpServletRequest request){
		String ip = "";
		for(int i = 0; i < PROXY_REMOTE_IP_ADDRESS.length;i++){
			ip = request.getHeader(PROXY_REMOTE_IP_ADDRESS[i]);
			if(StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)){
				continue;
			}
			return ip;
		}

		if(StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)){
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 获取ip
	 *
	 * @return
	 */
	public static String getLocalIP() {
		String localIP = "127.0.0.1";
		try {
			OK:
			for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
					continue;
				}
				Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (address instanceof Inet4Address) {
						localIP = address.getHostAddress();
						break OK;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return localIP;
	}
}
