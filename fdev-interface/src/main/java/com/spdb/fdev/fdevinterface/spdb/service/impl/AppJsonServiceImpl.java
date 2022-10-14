package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.CommonUtil;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.spdb.dao.AppDatDao;
import com.spdb.fdev.fdevinterface.spdb.dao.AppJsonDao;
import com.spdb.fdev.fdevinterface.spdb.entity.AppDat;
import com.spdb.fdev.fdevinterface.spdb.entity.AppJson;
import com.spdb.fdev.fdevinterface.spdb.service.AppJsonService;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceLazyInitService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class AppJsonServiceImpl implements AppJsonService {

    @Value("${finterface.nas}")
    private String finterfaceNas;
    @Autowired
    private AppJsonDao appJsonDao;
    @Autowired
    private AppDatDao appDatDao;
    @Autowired
    private InterfaceLazyInitService interfaceLazyInitService;

    /**
     * 保存实体Appjosn
     * @param appJson
     */
    @Override
    public void save(AppJson appJson) {
        String projectName = appJson.getProjectName();
        String branch = appJson.getBranch();
        appJsonDao.updateIsNew(projectName , branch);
        appJsonDao.save(appJson);
        // 只保存最新5个版本
        deleteAppJson(appJson.getProjectName(), appJson.getBranch(), appJson.getRoutesVersion() - 5);
    }

    /**
     * 条件查询AppJson实体列表
     * @param requestMap
     * @return
     */
    @Override
    public Map<String, Object> getAppJsonList(Map<String, Object> requestMap) {
        Map<String, Object> appJsonMap = appJsonDao.getAppJsonList(requestMap);
        List<AppJson> appJsonList = (List<AppJson>) appJsonMap.get(Dict.LIST);
        for (AppJson appJson : appJsonList) {
            // 设置关联的应用Id
            appJson.setAppId(interfaceLazyInitService.getAppIdByName(appJson.getProjectName()));
        }
        return appJsonMap;
    }

    /**
     *  根据projectNameList中的应用查询最新的AppJson实体列表
     * @param projectNameList 应用列表
     * @return
     */
    @Override
    public Map<String, Object> getAppJsonLast(List<Map<String, String>> projectNameList) {
        Map<String, Object> returnMap = new HashMap<>();
        Map<String, String> projectNameMap = new HashMap<>();
        Map<String, String> nameAndBranchMap = new HashMap<>();
        for (int i = 0; i < projectNameList.size(); i++) {
            Map<String, String> map = projectNameList.get(i);
            String nameEn = map.get(Dict.NAME_EN);
            projectNameMap.put(nameEn, map.get(Dict.NAME_ZH));
            nameAndBranchMap.put(nameEn, map.get(Dict.BRANCH));
        }
        List<AppJson> appJsonList = appJsonDao.getAppJsonLast(nameAndBranchMap);
        if (CollectionUtils.isEmpty(appJsonList)) {
            return returnMap;
        }
        returnMap.put(Dict.APPJSON, appJsonList);
        return returnMap;
    }


    @Override
    public String getLastBranch(String appNameEn, String branch) {
        AppJson appJson = appJsonDao.getLastBranch(appNameEn, branch);
        if (appJson != null) {
            return appJson.getBranch();
        }
        return "";
    }

    /**
     * 查找最新实体判断应用是否第一次投产
     * @param requestMap tag 投产tag名  project_name项目名
     * @return
     */
    @Override
    public Map<String , Object> queryLastAppJson(Map<String, Object> requestMap) {
        String tag = (String) requestMap.get(Dict.BRANCH);
        String project_name = (String) requestMap.get(Dict.PROJECT_NAME);
        String[] splits = tag.split("_");
        String branch = splits[0];
        //升序ASC 将AppJson存入list集合中（历史数据--->最新数据）
        List<AppJson>  firstAppJsons = appJsonDao.queryLastAppJson(branch, project_name);
        List<AppJson>  appJsons = appJsonDao.queryLastAppJson(Dict.PRO_LOWER, project_name);
        List<AppDat>  firstAppDats = appDatDao.queryLastAppDat(branch, project_name);
        List<AppDat>  appDats = appDatDao.queryLastAppDat(Dict.PRO_LOWER, project_name);
        Map appJsonInforMap = new HashMap();
        Integer appDatsNum = Util.isNullOrEmpty(appDats) ? 0 : appDats.size();
        Integer appJsonsNum = Util.isNullOrEmpty(appJsons) ? 0 : appJsons.size() ;
        Integer firstAppDatsNum = Util.isNullOrEmpty(firstAppDats) ? 0 : firstAppDats.size() ;
        Integer firstAppJsonsNum = Util.isNullOrEmpty(firstAppJsons) ? 0 : firstAppJsons.size() ;
        Integer proAppNum = appDatsNum + appJsonsNum;
        Integer FirstReleaseAppNum = firstAppDatsNum + firstAppJsonsNum;
        if (proAppNum.equals(FirstReleaseAppNum) ){
            appJsonInforMap.put(Dict.ISFIRST , true);
        }else {
            appJsonInforMap.put(Dict.ISFIRST , false);
        }
        for (AppJson appJson : firstAppJsons) {
            if(appJson.getBranch().equals(tag)){
                appJsonInforMap.put(Dict.APPJSON,  CommonUtil.object2Map(appJson));
            }
        }

        appJsonInforMap.put(Dict.CFGTYPE,"cfg_nas");
        return appJsonInforMap;
    }

    /**
     * 异步删除五个版本以前的数据及介质
     *
     * @param projectName
     * @param branch
     * @param routesVersion
     */
    @Async
    private void deleteAppJson(String projectName, String branch, int routesVersion) {
        List<AppJson> appJsonList = appJsonDao.deleteAppJson(projectName, branch, routesVersion);
        for (AppJson appJson : appJsonList) {
            FileUtil.deleteDir(finterfaceNas + appJson.getRepoTarName());
        }
    }

}
