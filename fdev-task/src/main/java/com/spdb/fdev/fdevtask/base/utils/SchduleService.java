package com.spdb.fdev.fdevtask.base.utils;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.spdb.service.IFdevTaskService;
import com.spdb.fdev.fdevtask.spdb.service.IRedmineApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class SchduleService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IRedmineApi iRedmineApi;

    @Autowired
    private IFdevTaskService iFdevTaskService;


    @Scheduled(cron = "0 0 2 * * *")
    public void cacheRedmine(){
        Set<String> redmineSet = new HashSet();
        String logId = "init value";
        logger.info("定时任务开始："+CommonUtils.dateFormat(new Date(), Dict.FORMATE_DATE));
        try {
            Map result = iFdevTaskService.queryTaskAndRedmineNo();
            if(CommonUtils.isNullOrEmpty(result))  throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"任务不存在!"});
            redisTemplate.opsForHash().putAll("redmine.mapping",result);
            redisTemplate.expire("redmine.mapping",1, TimeUnit.DAYS);
            result.keySet().forEach(key->redmineSet.add((String)result.get(key)));
            if(!CommonUtils.isNullOrEmpty(redmineSet)){
                for(String id:redmineSet){
                    logId = id;
                    if(CommonUtils.isNullOrEmpty(id)) continue;
                    Map tmp = iRedmineApi.getRedmineInfoForExcel(id);
                    redisTemplate.opsForHash().putAll("redmine."+id,tmp);
                    redisTemplate.expire("redmine."+id,1, TimeUnit.DAYS);
                    logger.info("存储redmine信息:"+logId);
                }
            }
        } catch (Exception e) {
            logger.error("存储redmine信息出错:"+logId+e.getMessage());
        }
    }


}
