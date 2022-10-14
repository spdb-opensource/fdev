package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.spdb.service.InterfaceApi;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RefreshScope
public class InterfaceApiImpl implements InterfaceApi {


    @Autowired
    private RestTransport restTransport;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void delete(String name,String branch) {
        Map param = new HashMap<>();
        param.put(Dict.BRANCH,branch);
        param.put("serviceId",name);
        try {
            param.put(Dict.REST_CODE, "deleteDataForTask");
            restTransport.submit(param);
        } catch (Exception e) {
            logger.error("发送接口失败"+e.getMessage());
        }
    }

}
