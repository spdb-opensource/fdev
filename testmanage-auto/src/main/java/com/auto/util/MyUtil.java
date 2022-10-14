package com.auto.util;

import com.auto.dict.Dict;
import com.auto.dict.ErrorConstants;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MyUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtils redisUtils;

    public String getCurrentUserEnName() {
        Map<String, Object> userInfo = null;
        String userEnName;
        try {
            userInfo = redisUtils.getCurrentUserInfoMap();
            userEnName = String.valueOf(userInfo.get(Dict.USER_NAME_EN));
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        return userEnName;
    }
    
    public String getCurrentUserEmial() {
        Map<String, Object> userInfo = null;
        String userEmail = "";
        try {
            userInfo = redisUtils.getCurrentUserInfoMap();
            userEmail = String.valueOf(userInfo.get(Dict.EMAIL));
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        return userEmail;
    }
}
