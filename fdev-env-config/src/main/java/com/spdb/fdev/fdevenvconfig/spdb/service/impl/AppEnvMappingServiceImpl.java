package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.dao.AppEnvMappingDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppEnvMapping;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Environment;
import com.spdb.fdev.fdevenvconfig.spdb.service.IAPPEnvMapingService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IRequestService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IEnvironmentService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IlabelService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppEnvMappingServiceImpl implements IAPPEnvMapingService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ResourceBundle labels;
    private static List<String> labelsRelList;

    static {
        labels = ResourceBundle.getBundle("labels");
        Enumeration<String> labelsKeys = labels.getKeys();
        labelsRelList = new ArrayList<>();
        while (labelsKeys.hasMoreElements()) {
            String key = labelsKeys.nextElement();
            labelsRelList.add(labels.getString(key));
        }
    }

    @Autowired
    private AppEnvMappingDao appEnvMappingDao;
    @Autowired
    private IRequestService requestService;
    @Autowired
    private IEnvironmentService environmentService;
    @Autowired
    private IlabelService labelService;

    @Override
    public AppEnvMapping save(AppEnvMapping appEnvMapping) {
        return appEnvMappingDao.save(appEnvMapping);
    }

    @Override
    public List<AppEnvMapping> query(AppEnvMapping appEnvMapping) throws Exception {
        return appEnvMappingDao.query(appEnvMapping);
    }

    @Override
    public void update(AppEnvMapping appEnvMapping) {
        appEnvMappingDao.update(appEnvMapping);
    }

    @Override
    public void delete(AppEnvMapping envMapping) {
        appEnvMappingDao.delete(envMapping);
    }

    @Override
    public void addAppEnvMapping(Map requestParam) throws Exception {
        String app_id = (String) requestParam.get(Dict.APPID);
        String network = (String) requestParam.get(Dict.NETWORK);
        AppEnvMapping appEnvMapping = new AppEnvMapping();
        appEnvMapping.setApp_id(app_id);
        appEnvMapping.setNetwork(network);
        appEnvMapping.setTags(labelsRelList);
        List<AppEnvMapping> list = appEnvMappingDao.query(appEnvMapping);
        if (list.isEmpty() || list.size() < 1) {
            appEnvMappingDao.save(appEnvMapping);
        }
    }

    @Override
    public Map<String, Object> queryByPage(AppEnvMapping appEnvMapping1, int page, int per_page) {
        return appEnvMappingDao.queryByPage(appEnvMapping1, page, per_page);
    }

    /**
     * 通过应用id查询该应用是否绑定过生产环境
     *
     * @param requestMap
     * @return
     */
    @Override
    public List<Map> queryProEnvByAppId(Map requestMap) {
        String appId = String.valueOf(requestMap.get(Constants.APP_ID));
        return this.appEnvMappingDao.queryProEnvByAppId(appId);
    }

    @Override
    public List<Map> queryProEnvByGitLabId(Map<String, Object> requestMap) {
        Integer gitlabId = (Integer) requestMap.get(Dict.GITLABID);
        Map<String, Object> appByGitId;
        try {
            appByGitId = requestService.getAppByGitId(gitlabId);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.APP_NOT_EXIST);
        }
        String appId = (String) appByGitId.get(Dict.ID);
        return this.appEnvMappingDao.queryProEnvByAppId(appId);
    }

    @Override
    public Map<String, Object> queryAppEnvMapByPage(Map<String, Object> requestMap, List<String> appIdList) {
        String envId = (String) requestMap.get(Dict.ENV_ID);
        String appId = (String) requestMap.get(Dict.APPID);
        int page = (int) requestMap.get(Dict.PAGE);
        int perPage = (int) requestMap.get(Dict.PER_PAGE);
        Map<String, Object> appEnvMap = new HashMap<>();
        Environment environmentParam = new Environment();
        environmentParam.setId(envId);
        Environment environment = null;
        try {
            environment = environmentService.queryById(environmentParam);
        } catch (Exception e) {
            logger.info("根据环境id查询环境出错：{}", e.getMessage());
        }
        if (environment == null) {
            // 若未查询到环境信息，直接返回空的list
            appEnvMap.put(Dict.TOTAL, 0);
            appEnvMap.put(Dict.LIST, new ArrayList<>());
            return appEnvMap;
        }
        List<String> environmentLabels = environment.getLabels();
        // 若环境的标签包含pro，则根据环境id查找数据
        if (environmentLabels.contains(Dict.PRO)) {
            appEnvMap = appEnvMappingDao.queryByPage(appIdList, appId, envId, new ArrayList<>(), "", page, perPage);
        } else {
            // 若环境的标签不包含pro，则判断标签是否包含网段信息
            Map<String, Object> labelsMap = labelService.queryAllLabels();
            List<String> netWorkLabels = (List<String>) labelsMap.get(Dict.NETWORK);
            String networkLabel = "";
            for (String netWorkLabel : netWorkLabels) {
                if (environmentLabels.contains(netWorkLabel)) {
                    networkLabel = netWorkLabel;
                    break;
                }
            }
            // 若环境标签不包含网段信息，直接返回空的list
            if (StringUtils.isEmpty(networkLabel)) {
                appEnvMap.put(Dict.TOTAL, 0);
                appEnvMap.put(Dict.LIST, new ArrayList<>());
                return appEnvMap;
            }
            appEnvMap = appEnvMappingDao.queryByPage(appIdList, appId, "", environmentLabels, networkLabel, page, perPage);
        }
        // 组装测试环境 或 生产环境
        getEnvInfo(appEnvMap);
        return appEnvMap;
    }

    @Override
    public List<AppEnvMapping> queryByAppIds(List<String> appIdList) {
        return appEnvMappingDao.queryByAppIds(appIdList);
    }

    /**
     * 组装测试环境 或 生产环境
     *
     * @param appEnvMap
     */
    private void getEnvInfo(Map<String, Object> appEnvMap) {
        List<Map<String, Object>> returnEnvMapList = new ArrayList<>();
        List<AppEnvMapping> appEnvMappingList = (List<AppEnvMapping>) appEnvMap.get(Dict.LIST);
        // 根据查找出来的信息再次反查测试环境和生产环境
        List<String> pageAppIdList = appEnvMappingList.stream().map(AppEnvMapping::getApp_id).collect(Collectors.toList());
        List<AppEnvMapping> pageAppEnvMaps = appEnvMappingDao.queryByAppIds(pageAppIdList);
        for (AppEnvMapping appEnvMapping : appEnvMappingList) {
            String appId = appEnvMapping.getApp_id();
            List<Environment> testEnvList = new ArrayList<>();
            List<Environment> proEnvList = new ArrayList<>();
            Map<String, Object> returnEnvMap = new HashMap<>();
            returnEnvMap.put(Dict.APPID, appId);
            for (AppEnvMapping pageAppEnvMap : pageAppEnvMaps) {
                if (appId.equals(pageAppEnvMap.getApp_id())) {
                    String envId = pageAppEnvMap.getEnv_id();
                    if (StringUtils.isEmpty(envId)) {
                        // 组装测试环境
                        String network = pageAppEnvMap.getNetwork();
                        String[] networks = network.split(",");
                        List<String> tags = pageAppEnvMap.getTags();
                        Collections.addAll(tags, networks);
                        testEnvList = environmentService.queryByLabelsFuzzy(tags);
                    } else {
                        // 组装生产环境
                        Environment proEnv = environmentService.queryById(envId);
                        proEnvList.add(proEnv);
                    }
                }
            }
            returnEnvMap.put(Dict.TESTENV, testEnvList);
            returnEnvMap.put(Dict.PRODUCTENV, proEnvList);
            returnEnvMapList.add(returnEnvMap);
        }
        appEnvMap.put(Dict.LIST, returnEnvMapList);
    }
}
