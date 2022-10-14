package com.gotest.service.serviceImpl;

import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.service.IFdevGroupApi;
import com.gotest.utils.MyUtil;
import com.test.testmanagecommon.cache.LazyInitProperty;
import com.test.testmanagecommon.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FdevGroupApiImpl implements IFdevGroupApi {
    @Autowired
    private RestTransport restTransport;
    private Logger logger = LoggerFactory.getLogger(OrderDimensionServiceImpl.class);


    @Override
    public List<String> getChildGroupId(String groupId) throws Exception {
        List groupIds= new ArrayList();
        if(MyUtil.isNullOrEmpty(groupId)){
            return  groupIds;
        }
        Map sendMap = new HashMap();
        sendMap.put(Dict.ID, groupId);
        sendMap.put(Dict.REST_CODE,"queryChildGroupById");
        List<Map<String,String>> list = (List)restTransport.submitSourceBack(sendMap);
        for (Map<String,String> group : list){
            groupIds.add(group.get(Dict.ID));
        }
        return groupIds;
    }

     @LazyInitProperty(redisKeyExpression = "torder.queryGroupDetail.${groupId}")
    @Override
    public Map queryGroupDetail(String groupId) throws Exception {
        if(MyUtil.isNullOrEmpty(groupId)){
            return  null;
        }
        Map sendMap = new HashMap();
        sendMap.put(Dict.ID, groupId);
        sendMap.put(Dict.REST_CODE,"fdev.user.queryGroupDetail");
        List<Map<String,String>> list = null;
        try {
            list = (List)restTransport.submitSourceBack(sendMap);
        } catch (Exception e) {

            logger.error("1111111111111111" + groupId);
            System.out.println("*****"+groupId+"******");
            e.printStackTrace();
        }
        return list.get(0);
    }
}
