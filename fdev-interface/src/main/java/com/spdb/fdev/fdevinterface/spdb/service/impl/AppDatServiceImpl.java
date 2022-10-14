package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.spdb.dao.AppDatDao;
import com.spdb.fdev.fdevinterface.spdb.entity.AppDat;
import com.spdb.fdev.fdevinterface.spdb.service.AppDatService;
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

/**
 * @author xxx
 * @date 2020/7/30 16:29
 */
@Service
@RefreshScope
public class AppDatServiceImpl implements AppDatService {

    @Value("${finterface.nas}")
    private String finterfaceNas;

    @Autowired
    private AppDatDao appDatDao;
    @Autowired
    private InterfaceLazyInitService interfaceLazyInitService;

    @Override
    public void save(AppDat appDat) {
        appDatDao.save(appDat);
        // 只保存最新5个版本
        deleteAppDat(appDat.getProjectName(), appDat.getBranch(), appDat.getRoutesVersion() - 5);
    }


    @Override
    public Map<String, Object> getAppDatList(Map<String, Object> requestMap) {
        Map<String, Object> appDatMap = appDatDao.getAppDatList(requestMap);
        List<AppDat> appDatList = (List<AppDat>) appDatMap.get(Dict.LIST);
        for (AppDat appDat : appDatList) {
            // 设置关联的应用Id
            appDat.setAppId(interfaceLazyInitService.getAppIdByName(appDat.getProjectName()));
        }
        return appDatMap;
    }

    @Override
    public Map<String, Object> getAppDatTarName(List<Map<String, String>> projectNameList) {
        Map<String, Object> returnMap = new HashMap<>();
        Map<String, String> projectNameMap = new HashMap<>();
        Map<String, String> nameAndBranchMap = new HashMap<>();
        for (int i = 0; i < projectNameList.size(); i++) {
            Map<String, String> map = projectNameList.get(i);
            String nameEn = map.get(Dict.NAME_EN);
            projectNameMap.put(nameEn, map.get(Dict.NAME_ZH));
            nameAndBranchMap.put(nameEn, map.get(Dict.BRANCH));
        }
        List<AppDat> appDatList = appDatDao.getAppDatTarName(nameAndBranchMap);
        if (CollectionUtils.isEmpty(appDatList)) {
            return returnMap;
        }
        // 记录本次变更存在介质的应用英文名及中文名
        Map<String, String> existDatProjectNameMap = new HashMap<>();
        StringBuilder tarName = new StringBuilder();
        for (int i = 0; i < appDatList.size(); i++) {
            AppDat appDat = appDatList.get(i);
            tarName.append(finterfaceNas + appDat.getRepoTarName());
            String projectName = appDat.getProjectName();
            existDatProjectNameMap.put(projectName, projectNameMap.get(projectName) + "-md5-" + appDat.getRepoJsonMD5());
            if (i != appDatList.size() - 1) {
                tarName.append(" ");
            }
        }
        returnMap.put("appDatName", "\"" + tarName.toString() + "\"");
        returnMap.put("existDatProjectNameMap", existDatProjectNameMap);
        return returnMap;
    }

    @Override
    public String getLastBranch(String appNameEn, String branch) {
        AppDat appDat = appDatDao.getLastBranch(appNameEn, branch);
        if (appDat != null) {
            return appDat.getBranch();
        }
        return "";
    }

    /**
     * 异步删除五个版本以前的数据及介质
     *
     * @param projectName
     * @param branch
     * @param routesVersion
     */
    @Async
    private void deleteAppDat(String projectName, String branch, int routesVersion) {
        List<AppDat> appDatList = appDatDao.deleteAppDat(projectName, branch, routesVersion);
        for (AppDat appDat : appDatList) {
            FileUtil.deleteDir(finterfaceNas + appDat.getRepoTarName());
        }
    }

}
