package com.spdb.fdev.common.aop;

import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.annoation.RemoveCachedProperty;
import com.spdb.fdev.common.dict.Dict;
import com.spdb.fdev.common.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import ognl.Ognl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
@ConditionalOnProperty(value = "redisCacheEnable")
public class LazyInitAspect {
    private static Logger logger = LoggerFactory.getLogger(LazyInitAspect.class);
    @Value("${spring.profiles.active}")
    private String env;
    @Value("${spring.redis.expire-time-in-minutes:10}")
    private long expireTimeInMinutes = 10;
    @Autowired
    private RedisTemplate redisTemplate;

    private static final String EMPTY_DATA = "__EMPTY_DATA_INITIALIZED__";

    @Pointcut("execution(* *(..)) && @annotation(lazyInitProperty)")
    public void lazyInit(LazyInitProperty lazyInitProperty) {
    }

    @Pointcut("execution(* *(..)) && @annotation(removeCachedProperty)")
    public void removeCache(RemoveCachedProperty removeCachedProperty) {
    }

    @Around("lazyInit(lazyInitProperty)")
    public Object lazyInitProcess(ProceedingJoinPoint joinPoint, LazyInitProperty lazyInitProperty) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String cacheKey = null;
        String redisKeyExpression = lazyInitProperty.redisKeyExpression();
        long duration = lazyInitProperty.redisDuration() == 0 ? expireTimeInMinutes : lazyInitProperty.redisDuration();
        if (Util.isNullOrEmpty(redisKeyExpression)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.REDISKEY_EXPRESSION});
        }
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Map paramMap = new HashMap();
        for (int i = 0; i < paramNames.length; i++) {
            paramMap.put(paramNames[i], args[i]);
        }
        cacheKey = generateRedisKeyByExpression(redisKeyExpression, paramMap);
        Object object = null;
        try {
            object = redisTemplate.opsForValue().get(cacheKey);
        }catch (Exception e){
            logger.info(e.getMessage()+"redis查询失败！");
            return null;
        }

        if (object instanceof String && EMPTY_DATA.equals(object)) {
            return null;
        }
        if (object == null) {
            object = joinPoint.proceed();
            if (object == null) {
                redisTemplate.opsForValue().set(cacheKey, EMPTY_DATA, duration, TimeUnit.MINUTES);
            } else {
                redisTemplate.opsForValue().set(cacheKey, object, duration, TimeUnit.MINUTES);
            }
        }
        logger.debug("property initialized, cached key:" + cacheKey);
        return object;
    }

    @Around("removeCache(removeCachedProperty)")
    public Object removeCachedProperty(ProceedingJoinPoint joinPoint, RemoveCachedProperty removeCachedProperty)
            throws Throwable {
        Object[] args = joinPoint.getArgs();
        String cacheKey;
        String redisKeyExpression = removeCachedProperty.redisKeyExpression();
        if (Util.isNullOrEmpty(redisKeyExpression)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.REDISKEY_EXPRESSION});
        }
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Map paramMap = new HashMap();
        for (int i = 0; i < paramNames.length; i++) {
            paramMap.put(paramNames[i], args[i]);
        }
        cacheKey = generateRedisKeyByExpression(redisKeyExpression, paramMap);
        if(!cacheKey.contains("user.login.token")){
            redisTemplate.delete(cacheKey);
        }
//        for (Object o : keys) {
//            String key = (String) o;
//            key = key.substring(env.length() + 1);
//            if (!key.contains("user.login.token"))
//                redisTemplate.delete(key);
//        }
        Object object = joinPoint.proceed();
        return object;
    }

    private String generateRedisKeyByExpression(String redisKeyExpression, Map paramMap) {
        String originExpression = redisKeyExpression;
        Pattern pattern = Pattern.compile("(?<=\\{)[^\\}]+");
        Matcher m = pattern.matcher(redisKeyExpression);
        while (m.find()) {
            String ognlExp = m.group();
            try {
                Object paramObj = Ognl.getValue(ognlExp, paramMap);
                if (!Util.isNullOrEmpty(paramObj) && paramObj instanceof String) {
                    redisKeyExpression = redisKeyExpression.replaceAll("\\{" + ognlExp + "\\}", (String) paramObj);
                } else {
                    logger.error("param object get by ognl expression must instanceof String");
                    redisKeyExpression = null;
                    break;
                }
            } catch (Exception e) {
                logger.error("get param object  by ognl expression failed with:", e);
                redisKeyExpression = null;
                break;
            }
        }
        if (Util.isNullOrEmpty(redisKeyExpression)) {
            logger.error("LazyInitAspect cannot generate redis key by expression: " + originExpression);
        } else {
            logger.debug("redis key generated by expression: " + originExpression);
        }
        return redisKeyExpression;
    }

}
