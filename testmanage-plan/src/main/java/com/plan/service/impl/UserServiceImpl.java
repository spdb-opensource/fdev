package com.plan.service.impl;

import com.plan.dict.Dict;
import com.plan.dict.ErrorConstants;
import com.plan.service.IUserService;
import com.plan.util.Utils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(MantisFlawServiceImpl.class);

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private RestTransport restTransport;

    @Override
    public Map<String, Object> getCurrentUser() throws Exception {
        Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
        if(Util.isNullOrEmpty(user)){
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        return user;
    }

    @Override
    public String queryGroupNameById(String groupId) {
        if (Utils.isEmpty(groupId)) {
            return "";
        }
        try {
            Map groupInfo = queryGroupDetail(groupId);
            return (String) groupInfo.get(Dict.NAME);
        } catch (Exception e) {
            logger.info(">>>>query group fail,groupId:{}", groupId);
        }
        return "";
    }

    @Override
    public Map queryGroupDetail(String groupId) throws Exception {
        if (Util.isNullOrEmpty(groupId)) {
            return null;
        }
        Map sendMap = new HashMap();
        sendMap.put(Dict.ID, groupId);
        sendMap.put(Dict.REST_CODE, "fdev.user.queryGroupDetail");
        List<Map<String, String>> list = (List) restTransport.submitSourceBack(sendMap);
        return list.get(0);
    }
}
