package com.spdb.fdev.component.vo;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.entity.BaseImageRecord;
import com.spdb.fdev.component.service.IAppService;
import com.spdb.fdev.component.service.IRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class BaseImageVoService {

    private static final Logger logger = LoggerFactory.getLogger(BaseImageVoService.class);

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IAppService appService;

    /**
     * 封装骨架历史版本返回数据
     *
     * @param recordList
     * @return
     */
    public List<Map> queryBaseImageRecord(List<BaseImageRecord> recordList) {
        List<Map> result = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(recordList)) {
            for (BaseImageRecord record : recordList) {
                Map map = CommonUtils.object2Map(record);
                try {
                    //查询用户模块，返回用户的中英文名
                    roleService.addUserName(map, record.getUpdate_user());
                    //查询应用模块，将使用应用列表转为应用名称
                    HashSet<String> trialApps = record.getTrial_apps();
                    HashSet<String> trialAppNames = new HashSet<>();
                    if (!CommonUtils.isNullOrEmpty(trialApps)) {
                        for (String appId : trialApps) {
                            Map app = appService.getAppByGitId(String.valueOf(appId));
                            trialAppNames.add((String) app.get(Dict.NAME_EN));
                        }
                        map.put(Dict.TRIAL_APPS_NAMES, trialAppNames);
                    }
                } catch (Exception e) {
                    logger.error("封装镜像历史版本数据失败{}", e.getMessage());
                }
                result.add(map);
            }
        }
        return result;
    }


    /**
     * 封装骨架历史版本返回数据
     *
     * @param record
     * @return
     */
    public Map queryBaseImageRecordDetail(BaseImageRecord record) {
        Map map = CommonUtils.object2Map(record);
        //查询用户模块，返回用户的中英文名
        try {
            roleService.addUserName(map, record.getUpdate_user());
            HashSet<String> trialApps = record.getTrial_apps();
            HashSet<String> trialAppNames = new HashSet<>();
            if (!CommonUtils.isNullOrEmpty(trialApps)) {
                for (String appId : trialApps) {
                    Map app = appService.getAppByGitId(String.valueOf(appId));
                    trialAppNames.add((String) app.get(Dict.NAME_EN));
                }
                map.put(Dict.TRIAL_APPS_NAMES, trialAppNames);
            }
        } catch (Exception e) {
            logger.error("封装镜像历史版本数据失败{}", e.getMessage());
        }
        return map;
    }
}
