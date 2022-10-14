package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.base.utils.ServiceUtil;
import com.spdb.fdev.fdevenvconfig.spdb.service.IVerifyCodeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RefreshScope
public class VerifyCodeServiceImpl implements IVerifyCodeService {

    @Value("${verfityCodeRedisKey}")
    private String verfityCodeRedisKey;
    @Autowired
    private ServiceUtil serviceUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public String getVerifyCode(Map map) throws Exception{
        String userId = serviceUtil.getOpno();
        String str = (String) redisTemplate.opsForValue().get(verfityCodeRedisKey + userId);
        if (StringUtils.isBlank(str)) {
            str = getVerfityCodeSaveToRedis(userId);
        } else {
            // 当已存在验证码，用户发接口重新获取，将之前的删掉，重新生成。
            redisTemplate.delete(verfityCodeRedisKey + userId);
            str = getVerfityCodeSaveToRedis(userId);
        }
        return str;
    }

    @Override
    public void checkVerifyCode(String verifyCode) throws Exception{
        String userId = serviceUtil.getOpno();
        String str = (String) redisTemplate.opsForValue().get(verfityCodeRedisKey + userId);
        if (!verifyCode.trim().equalsIgnoreCase(str)) {
            throw new FdevException(ErrorConstants.VERFITYCODE_ERROR);
        }
    }

    private String getVerfityCodeSaveToRedis(String userid) {
        //生成验证码
        String verfityCode = CommonUtils.uuid();
        redisTemplate.opsForValue().set(verfityCodeRedisKey + userid, verfityCode, 3, TimeUnit.MINUTES);
        return verfityCode;
    }

}
