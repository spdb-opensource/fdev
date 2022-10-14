package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.spdb.service.RequestService;
import com.spdb.fdev.spdb.service.SonarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xxx
 * @date 2020/8/20 7:57
 */
@Service
public class SonarServiceImpl implements SonarService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RequestService requestService;

    @Override
    public void getSonarMsg(Map<String, Object> parse) {
        String event = (String) parse.get(Dict.EVENT);
        String errorMsg = (String) parse.get(Dict.ERROR_MSG);
        Map<String, Object> metadata = (Map<String, Object>) parse.get(Dict.METADATA);
        String branch = (String) metadata.get(Dict.PROJECT_BRANCH);
        if (Dict.SIT_UP.equals(branch)) {
            return;
        }
        String nameEn = (String) metadata.get(Dict.PROJECT_NAME);
        if (Dict.ERROR.equals(event) && errorMsg != null) {
            errorMsg = errorMsg + "\n" + "系统内部错误，请重试！";
            String errorLogDir = nameEn + "-" + branch.replace("/", "_");
            CommonUtil.createFile("/fdev/log/sonar/" + errorLogDir, errorMsg);
            String sonarId = "/fwebhook/sonar/" + errorLogDir;
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(Dict.FEATURE_ID, branch);
            paramMap.put(Dict.WEB_NAME_EN, nameEn);
            paramMap.put(Dict.SONAR_ID, sonarId);
            requestService.updateTaskSonar(paramMap);
            logger.info("nameEn:{},branch:{},sonar_id:{}", nameEn, branch, sonarId);
        }
    }

}
