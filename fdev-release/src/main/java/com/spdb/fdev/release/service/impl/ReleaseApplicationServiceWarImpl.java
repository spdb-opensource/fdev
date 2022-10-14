package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.service.IReleaseApplicationWarService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 投产应用业务层 2019年3月26日
 */
@Service
public class ReleaseApplicationServiceWarImpl implements IReleaseApplicationWarService {

    private static Logger logger = LoggerFactory.getLogger(ReleaseApplicationServiceWarImpl.class);


    @Autowired
    private RestTransport restTransport;


    @Override
    public String queryApplicationId(String application_id, Integer gitlab_project_id) throws Exception {
        if (CommonUtils.isNullOrEmpty(application_id)) {
            // application_id 和 gitlab_project_id不能同时送空
            if (CommonUtils.isNullOrEmpty(gitlab_project_id)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR);
            } else {
                Map<String, Object> send = new HashMap<>();
                send.put(Dict.ID, gitlab_project_id);
                send.put(Dict.REST_CODE,"getAppByGitId");
                Map result = (Map) restTransport.submit( send);
                if (CommonUtils.isNullOrEmpty(result)) {
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[] {"通过gitid查询应用信息不存在"});
                }
                return (String) result.get(Dict.ID);
            }
        }
        return application_id;
    }

}