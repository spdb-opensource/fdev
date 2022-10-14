package com.spdb.fdev.fdevinterface.spdb.callable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.CommonUtil;
import com.spdb.fdev.fdevinterface.spdb.entity.InterfaceStatistics;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceStatisticsService;

@Component(Dict.VUE_INTERFACE_STATISTICS_CALLABLE)
public class VueInterfaceStatisticsCallable extends BaseScanCallable {

	private Logger logger = LoggerFactory.getLogger(VueInterfaceStatisticsCallable.class);

	@Autowired
	private InterfaceStatisticsService interfaceStatisticsService;

	@Value("${fdev.service.map.path}")
	private String serviceMapPath;


	@Override
	public Object call() {
		logger.info(Constants.INTERFACE_STATISTICS_SCAN_START);
		Map returnMap = new HashMap();
		// 获取配置文件路径
		String serviceMapPath = getServiceMapPath();
		if (StringUtils.isEmpty(serviceMapPath)) {
			return returnMap;
		}
		try {
			List<InterfaceStatistics> interfaceStatisticsList = getJsContent(serviceMapPath);
			interfaceStatisticsService.deleteInterfaceStatistics(super.getAppServiceId());
			interfaceStatisticsService.saveInterfaceStatistics(interfaceStatisticsList);
			logger.info(Constants.INTERFACE_STATISTICS_SCAN_END);
			returnMap.put(Dict.SUCCESS, Constants.INTERFACE_STATISTICS_SCAN_SUCCESS);
			return returnMap;
		} catch (FdevException e) {
			returnMap.put(Dict.ERROR, errorMessageUtil.get(e));
            logger.error("{}", errorMessageUtil.get(e));
            return returnMap;
		}
	}

	/**
	 * 获取客户端vue项目调用接口配置文件路径
	 *
	 * @return
	 */
	private String getServiceMapPath() {
		String transConfigPath;
		for (String src : super.getSrcPathList()) {
			transConfigPath = src + serviceMapPath;
			File file = new File(transConfigPath);
			if (file.exists()) {
				return transConfigPath;
			}
		}
		return null;
	}

	/**
	 * 解析客户端vue项目serviceMap.js文件
	 *
	 * @param transConfigPath
	 * @return
	 */
	private List<InterfaceStatistics> getJsContent(String serviceMapPath) {
		List<InterfaceStatistics> interfaceStatisticsList = new ArrayList<>();
		StringBuilder stringBuilder = new StringBuilder();
		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(serviceMapPath), StandardCharsets.UTF_8))) {
			String str;
			while ((str = bufferedReader.readLine()) != null) {
				if (!str.contains("//")) {
					stringBuilder.append(str);
				}
			}
		} catch (Exception e) {
			logger.error("解析客户端vue项目的serviceMap.js文件出错，{}！", e.getMessage());
			throw new FdevException(ErrorConstants.INTERFACE_STATISTICS_ERROR,
					new String[] { "解析客户端vue项目的serviceMap.js文件出错！" });
		}
		String content = stringBuilder.toString().split("export default")[1].trim();
		if (content.endsWith(";")) {
			content = content.substring(0, content.length() - 1);
		}
		try {
			// 解析serviceMap.js文件
			JSONObject jsonObject = JSONObject.parseObject(content);
			if (!jsonObject.isEmpty())
				jsonObject.entrySet().forEach(s -> {
					String contextPath = s.getKey();
					String sourceServiceName = getServiceNameByContextPath(contextPath);
					JSONObject parseObject = JSONObject.parseObject(s.getValue().toString());
					parseObject.entrySet().forEach(r -> {
						String url = r.getValue().toString();
						String name = url.substring(url.lastIndexOf("/")+1, url.length());
						InterfaceStatistics is = new InterfaceStatistics();
						is.setSourceServiceName(StringUtils.isEmpty(sourceServiceName) ? contextPath : sourceServiceName);
						is.setName(name);
						is.setUrl(url);
						is.setTargetServiceName(super.getAppServiceId());
						interfaceStatisticsList.add(is);
					});
				});
		} catch (Exception e) {
			logger.error("解析客户端vue项目的serviceMap.js文件出错，{}！", e.getMessage());
			throw new FdevException(ErrorConstants.INTERFACE_STATISTICS_ERROR,
					new String[] { "解析客户端vue项目的serviceMap.js文件出错！" });
		}
		return interfaceStatisticsList;
	}

	/**
	 * 使用map根据contextpath查找应用名称
	 * 
	 * @param contextPath
	 * @return
	 */
	private String getServiceNameByContextPath(String contextPath) {
		for (Entry<String, String> entry : CommonUtil.getContextPathMap().entrySet()) {
			if (contextPath.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return "";
	}

}
