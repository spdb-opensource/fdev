package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IAppDeployMappingDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppDeployMapping;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.service.IDeployFileService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IRequestService;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DeployFileServiceImpl implements IDeployFileService {

    @Autowired
    private IAppDeployMappingDao appDeployMappingDao;
    @Autowired
    private IRequestService requestService;

    @Override
    public List<Map> queryDeployDependency(String modelNameEn, String fieldNameEn) throws Exception {
        List<AppDeployMapping> appDeployMappingList = appDeployMappingDao.getAppDeployMapping(modelNameEn, fieldNameEn);
        // 发应用模块
        List<Map<String, Object>> appInfoList = requestService.queryAllAppInfo();
        List<Map> allAppList = new ArrayList<>();
        for (AppDeployMapping appDeployMapping : appDeployMappingList) {
            for (Map<String, Object> appInfo : appInfoList) {
                if (String.valueOf(appDeployMapping.getGitlabId()).equals(String.valueOf(appInfo.get(Dict.GITLAB_PROJECT_ID)))) {
                    Map<String, Object> returnAppInfo = new HashMap<>();
                    returnAppInfo.put(Dict.NAME_ZH, appInfo.get(Dict.NAME_ZH));
                    returnAppInfo.put(Dict.NAME_EN, appInfo.get(Dict.NAME_EN));
                    returnAppInfo.put(Dict.DEV_MANAGERS, appInfo.get(Dict.DEV_MANAGERS));
                    returnAppInfo.put(Dict.SPDB_MANAGERS, appInfo.get(Dict.SPDB_MANAGERS));
                    returnAppInfo.put(Dict.GROUP, appInfo.get(Dict.GROUP));
                    returnAppInfo.put(Dict.GIT, appInfo.get(Dict.GIT));  //git 地址
                    returnAppInfo.put(Dict.APPID, appInfo.get(Dict.ID));    //应用ID
                    returnAppInfo.put(Dict.GITLAB_ID, ((Integer)appInfo.get(Dict.GITLAB_PROJECT_ID)).toString());  //git项目id
                    allAppList.add(returnAppInfo);
                    break;
                }
            }
        }
        return allAppList;
    }

}
