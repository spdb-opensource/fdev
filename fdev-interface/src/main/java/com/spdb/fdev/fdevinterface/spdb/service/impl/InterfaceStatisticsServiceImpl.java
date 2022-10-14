package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.CommonUtil;
import com.spdb.fdev.fdevinterface.spdb.dao.InterfaceStatisticsDao;
import com.spdb.fdev.fdevinterface.spdb.entity.InterfaceStatistics;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceStatisticsService;
import com.spdb.fdev.fdevinterface.spdb.service.ScannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class InterfaceStatisticsServiceImpl implements InterfaceStatisticsService {

    private Logger logger = LoggerFactory.getLogger(InterfaceStatisticsServiceImpl.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private InterfaceStatisticsDao interfaceStatisticsDao;
    @Autowired
    private ScannerService scannerService;
    @Autowired
    private Environment env;

    @Value("${fdev.interface.statistics.urlmapping}")
    private String urlmapping;
    @Value("${fjob.urlmapping}")
    private String fJobUrlmapping;
    @Value("${fdev.vue.service.name.list}")
    private List<String> vueServiceNameList;

    @Override
    public Map queryInterfaceStatistics(Map params) {
        return interfaceStatisticsDao.queryInterfaceList(params);
    }

    /**
     * 发GET请求获取urlmapping的json数据(后端应用通过url获取接口统计信息）
     *
     * @param serviceName
     * @return
     */
    private String getInterfaceListByName(String serviceName) {
        String contextPath = CommonUtil.getContextPathMap().get(serviceName);
        if (StringUtils.isEmpty(contextPath)) {
            return null;
        }
        String domain = "";
        if (serviceName.startsWith(Dict.FDEV_)) {
            domain = env.getProperty(Dict.FDEV1 + serviceName.substring(5).replace("-", ".") + Dict.DOMAIN1);
        } else if (serviceName.startsWith(Dict.TEST_MANAGE_)) {
            domain = env.getProperty(Dict.FTMS1 + serviceName.substring(11).replace("-", ".") + Dict.DOMAIN1);
        }
        if (StringUtils.isEmpty(domain)) {
            logger.error("调用接口查询urlMapping出错，未配置domain");
            throw new FdevException(ErrorConstants.INTERFACE_STATISTICS_ERROR,
                    new String[]{"调用接口查询urlMapping出错，未配置domain"});
        }
        StringBuilder stringBuilder = new StringBuilder(domain).append("/").append(contextPath);
        if ("fdev-job-executor".equals(serviceName)) {
            stringBuilder = stringBuilder.append(fJobUrlmapping);
        } else {
            stringBuilder = stringBuilder.append(urlmapping);
        }
        logger.info("===========urlmapping url:{}", stringBuilder.toString());
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(stringBuilder.toString()).build();
        URI uri = uriComponents.toUri();
        String urls = null;
        try {
            urls = restTemplate.getForObject(uri, String.class);
        } catch (Exception e) {
            logger.info("调用{}模块服务出错-------", serviceName, e);
        }
        return urls;
    }

    /**
     * 解析urlmapping的json数据(后端应用通过url获取接口统计信息）
     *
     * @param json
     * @param name
     * @return
     */
    private List<InterfaceStatistics> resolveJsonString(String json, String name) {
        if (json == null) {
            return new ArrayList<InterfaceStatistics>();
        }
        logger.info("receive msg by get service name:{},json:{}", name, json);
        JsonParser jsonParser = new JsonParser();
        List<InterfaceStatistics> urlList = new ArrayList<>();
        try {
            JsonElement element = jsonParser.parse(json);
            if (element.isJsonObject()) {
                JsonObject urlMapping = element.getAsJsonObject();
                urlMapping.entrySet().forEach(s -> {
                    InterfaceStatistics is = new InterfaceStatistics();
                    String url = s.getValue().getAsString();
                    String name1 = url.substring(url.lastIndexOf("/") + 1, url.length());
                    is.setName(name1);
                    is.setTargetServiceName(name);
                    is.setUrl(url);
                    // 从接口调用url中取出contextpath，在从map中根据contextpath来取出应用名
                    String contextPath = s.getValue().getAsString().split("/", 5)[3];
                    if (CommonUtil.getContextPathMap().containsValue(contextPath))
                        CommonUtil.getContextPathMap().entrySet().forEach(r -> {
                            if (contextPath.equals(r.getValue().toString())) {
                                is.setSourceServiceName(r.getKey());
                            }
                        });
                    else
                        is.setSourceServiceName(contextPath);
                    urlList.add(is);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlList;
    }

    @Override
    public void scanInterfaceStatistics(String projectName) {
        interfaceStatisticsDao.clearInterfaceList(projectName);
        List<InterfaceStatistics> list = new ArrayList<>();
        list = resolveJsonString(getInterfaceListByName(projectName), projectName);
        interfaceStatisticsDao.saveInterfaceList(list);
    }


    @Override
    public void saveInterfaceStatistics(List<InterfaceStatistics> list) {
        interfaceStatisticsDao.saveInterfaceList(list);
    }


    @Override
    public void deleteInterfaceStatistics(String serviceName) {
        interfaceStatisticsDao.clearInterfaceList(serviceName);
    }


    @Override
    public void initList() {
        //初始化所有后端微服务统计信息
        CommonUtil.getContextPathMap().entrySet().forEach(s -> {
            scanInterfaceStatistics(s.getKey());
        });
        //初始化前端vue项目接口调用信息
        vueServiceNameList.forEach(s -> {
            scannerService.scanInterface(s, Dict.MASTER, "90", 0);
        });
    }

}
