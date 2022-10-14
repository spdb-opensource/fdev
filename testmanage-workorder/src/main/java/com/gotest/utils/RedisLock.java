package com.gotest.utils;


import com.test.testmanagecommon.rediscluster.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class RedisLock {


    /**
     * <pre>
     * 基于Redis的SETNX操作实现的分布式锁
     * </pre>
     *
     * @author xxx
     */

    private static Logger logger = LoggerFactory.getLogger(RedisLock.class);

    public static final String FORDER_ORDER = "forder.order.";

    public static final int LOCK_TIME = 10;

    @Autowired
    private RedisUtils redisUtils;


    /**
     * 非阻塞，立即返回是否获取到锁
     *
     * @return
     */
    public boolean tryLock(String key, String lockVal, int ttl) {
        if (setnx(key,  lockVal, ttl)) { // 获取到锁
            logger.info("**** setnx success " + key);
            return true;
        }
        logger.info("**** setnx fail " + key);
        return false;
    }

    private boolean setnx(String lockKey, String val, int ttl) {
        if (!redisUtils.setNxAndTtl(lockKey, val, ttl)) {
            return true;
        }
        return false;
    }

    /**
     * 释放锁
     */
    public void unlock(String from ,String lockKey) {
         redisUtils.delete(from,lockKey);
    }


}




