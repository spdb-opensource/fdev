package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.spdb.service.RequestService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author xxx
 * @date 2020/8/20 8:22
 */
@Service
public class RequestServiceImpl implements RequestService {

    private static Logger logger = LoggerFactory.getLogger(SonarScanServiceImpl.class);

    @Autowired
    private RestTransport restTransport;

    @Override
    public void updateTaskSonar(Map<String, Object> paramMap) {
        paramMap.put(Dict.REST_CODE, "updateTaskSonarId");
        try {
            this.restTransport.submit(paramMap);
        } catch (Exception e) {
            logger.info("调任务模块接口(/ftask/api/sonarqube/updateTaskSonarId)出错：{}", e.getMessage());
        }
    }
}
