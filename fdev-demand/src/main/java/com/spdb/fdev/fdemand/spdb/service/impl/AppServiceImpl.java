package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.service.IAppService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RefreshScope
public class AppServiceImpl implements IAppService {

    private static final Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);

    @Autowired
    private RestTransport restTransport;

    /**
     * 根据应用ids查询应用列表
     * @param appIds
     * @return
     */
    @Override
    public List<Map<String, Object>> getAppByIdsOrGitlabIds(Set<String> appIds ) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            if(!CommonUtils.isNullOrEmpty(appIds)){
                Map<String, Object> send_map = new HashMap<>();
                send_map.put(Dict.REST_CODE, "getAppByIdsOrGitlabIds");
                send_map.put(Dict.TYPE, Dict.ID);
                send_map.put(Dict.IDS, appIds);
                result = (List<Map<String, Object>>) restTransport.submit(send_map);
            }
        } catch (Exception e) {
            logger.info("查询应用列表失败");
            throw new FdevException( ErrorConstants.THIRD_SERVER_ERROR );
        }
        return result;
    }

    /**
     * 应用所属系统
     * @param systemId
     * @return
     */
    @Override
    public Map<String, String> querySystem(String systemId ) {
        Map<String, String> result = new HashMap<>();
        try {
            if(!CommonUtils.isNullOrEmpty(systemId)){
                Map<String, Object> send_map = new HashMap<>();
                send_map.put(Dict.REST_CODE, "querySystem");
                send_map.put(Dict.ID, systemId );
                List<Map<String, String>> data = (List<Map<String, String>>) restTransport.submit(send_map);
                if( !CommonUtils.isNullOrEmpty(data) ){
                    result = data.get(0);
                }
            }
        } catch (Exception e) {
            logger.info("查询应用所属系统失败");
            throw new FdevException( ErrorConstants.THIRD_SERVER_ERROR );
        }
        return result;
    }
}
