package com.spdb.fdev.base.utils;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.service.IAppService;
import com.spdb.fdev.pipeline.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class VaildateUtil {
    @Autowired
    private  IAppService appService;
    @Autowired
    private IUserService userService;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${spring.redis.expire-time-in-hours:10}")
    private long expireTimeInMinutes = 10L;

    private static final Logger logger = LoggerFactory.getLogger(VaildateUtil.class);
    /**
     * 判断当前用户是否为fdev应用的应用负责人或行内负责人
     * @return
     */
    public boolean projectCondition(String projectId) throws Exception{
        User user = userService.getUserFromRedis();
        return checkUserIdInProjectManager(user.getId(),projectId);
    }

    /**
     * 判断当前用户是否为fdev应用的应用负责人或行内负责人
     * @return
     */
    public boolean projectConditionNoAuth(String projectId, String userId) throws Exception{
        return checkUserIdInProjectManager(userId,projectId);
    }



    /**
     * 校验用户在负责人列表
     * @return
     */
    public boolean checkUserIdInProjectManager(String userId, String projectId) throws Exception{
        String cacheKey = Constants.PROJECT_MANAGER_REDIS_KEY_PROFIX + projectId;
        List<Map> managers = (List<Map>)redisTemplate.opsForValue().get(cacheKey);
        logger.info("******get project managers from redis, projectId:"+projectId+", managers:"+managers);

        //从redis获取managers，拿不到再发交易获取
        if(CommonUtils.isNullOrEmpty(managers)){
            managers = new ArrayList<Map>();
            Map project = appService.queryAppDetailById(projectId);
            if (CommonUtils.isNullOrEmpty(project)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"无法查询到应用信息"});
            }
            List<Map> spdbManagers = (List<Map>) project.get("spdb_managers");
            List<Map> devManagers = (List<Map>) project.get("dev_managers");
            logger.info("******query managers result, projectId:" + projectId+", managers:"+spdbManagers+devManagers);
            if (!CommonUtils.isNullOrEmpty(spdbManagers)) {
                managers.addAll(spdbManagers);
            }
            if (!CommonUtils.isNullOrEmpty(devManagers)) {
                managers.addAll(devManagers);
            }
            if (!CommonUtils.isNullOrEmpty(managers)) {
                logger.info("******put project managers to redis, projectId:"+projectId);
                redisTemplate.opsForValue().set(cacheKey, managers, this.expireTimeInMinutes, TimeUnit.HOURS);
            }
        }
        if (CommonUtils.isNullOrEmpty(managers)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用信息未包含应用负责人信息"});
        }
        for (Map person : managers) {
            if(person.get(Dict.ID).equals(userId)){
                return true;
            }
        }
        return false;
    }


}
