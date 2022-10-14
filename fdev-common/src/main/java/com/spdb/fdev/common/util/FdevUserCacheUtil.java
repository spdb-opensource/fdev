package com.spdb.fdev.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.dict.Dict;
import com.spdb.fdev.common.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * description：
 * author t-panbk
 * date 2021-07-15
 * version v0.0.1
 * since v0.0.1
 **/
@Component
public class FdevUserCacheUtil {


    @Value("${userManager.url:}")
    public String url;

    private static Logger logger = LoggerFactory.getLogger(RestTransport.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource(name = "getRestTemplate")
    private RestTemplate restTemplate;

    /**
     * 根据用户id从redis中获取用户信息
     *
     * @return 用户信息
     * @throws Exception
     */
    public Map getCacheUser(String userId) throws Exception {
        Map user = (Map) redisTemplate.opsForValue().get(Dict._CACHEUSER + userId);
        if (Util.isNullOrEmpty(user)) {
            user = queryUserCoreData(userId);
        }
        return user;
    }

    /**
     * 根据用户id从redis中获取用户中文名
     *
     * @return 用户中文名
     * @throws Exception
     */
    public String getCacheUserName(String userId) throws Exception {
        Map user = (Map) redisTemplate.opsForValue().get(Dict._CACHEUSER + userId);
        if (Util.isNullOrEmpty(user)) {
            user = queryUserCoreData(userId);
        }
        return (String) (!Util.isNullOrEmpty(user) ? user.get(Dict.USER_NAME_CN) : "");
    }

    /**
     * 根据团队id从redis中获取团队信息
     *
     * @return 团队信息
     * @throws Exception
     */
    public Map getCacheTeam(String teamId) throws Exception {
        Map Team = (Map) redisTemplate.opsForValue().get(Dict._CACHETEAM + teamId);
        if (Util.isNullOrEmpty(Team)) {
            Team = queryTeamDetail(teamId);
        }
        return Team;
    }
    /**
     * 根据团队id从redis中获取团队中文名
     *
     * @return 团队中文名
     * @throws Exception
     */
    public String getCacheTeamName(String teamId) throws Exception {
        Map Team = (Map) redisTemplate.opsForValue().get(Dict._CACHETEAM + teamId);
        if (Util.isNullOrEmpty(Team)) {
            Team = queryTeamDetail(teamId);
        }
        return (String) (!Util.isNullOrEmpty(Team) ? Team.get(Dict.TEAMNAME) : "");
    }
    /**
     * 根据小组id从redis中获取小组信息
     *
     * @return 用户info
     * @throws Exception
     */
    public Map getCacheGroup(String groupId) throws Exception {
        Map group = (Map) redisTemplate.opsForValue().get(Dict._CACHEGROUP + groupId);
        if (Util.isNullOrEmpty(group)) {
            group = queryGroup(groupId);
        }
        return group;
    }
    /**
     * 根据小组id从redis获取小组中文名
     *
     * @return 用户info
     * @throws Exception
     */
    public String getCacheGroupName(String groupId) throws Exception {
        Map group = (Map) redisTemplate.opsForValue().get(Dict._CACHEGROUP + groupId);
        if (Util.isNullOrEmpty(group)) {
            group = queryGroup(groupId);
        }
        return (String) (!Util.isNullOrEmpty(group) ? group.get(Dict.NAME) : "");
    }

    /**
     * 根据根据用户id以下信息，返回数据类型为Map，Map里包含用户实体属性
     *
     * @param userId 用户id
     * @return Map
     */
    public Map queryUserCoreData(String userId) throws IOException {
        List<Map> userList = new ArrayList<>();
        Map userMap = new HashMap();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", userId);
        if (!url.contains("/fusermanage")) {
            return userMap;
        }
        String[] apiUrls = url.split("api");
        Map responseEntity = restTemplate.postForObject(apiUrls[0] + "api/user/queryUserCoreData", paramMap, Map.class);
        if (responseEntity != null && "AAAAAAA".equals(responseEntity.get(Dict.CODE)) && responseEntity.get("data") != null) {
            userList = (List<Map>) responseEntity.get("data");
        } else {
            if (responseEntity != null && responseEntity.get("msg") != null) {
                logger.error("其他后台服务异常：{}", responseEntity.get("msg"));
            }
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        if(!Util.isNullOrEmpty(userList)){
            userMap = userList.get(0);
        }
        ObjectMapper objectMapper = new ObjectMapper();

        redisTemplate.opsForValue().set(Dict._CACHEUSER + userId,
                objectMapper.readValue(JSONObject.fromObject(userMap).toString(), Map.class), 7, TimeUnit.DAYS);
        return userMap;
    }

    /**
     * 根据根据小组id以下信息，返回数据类型为Map，Map里包含用户实体属性
     *
     * @param groupId 用户id
     * @return Map
     */
    public Map queryGroup(String groupId) throws IOException {
        List<Map> groupList = new ArrayList<>();
        Map groupMap = new HashMap();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", groupId);
        if (!url.contains("/fusermanage")) {
            return groupMap;
        }
        String[] apiUrls = url.split("api");
        Map responseEntity = restTemplate.postForObject(apiUrls[0] + "api/group/query", paramMap, Map.class);
        if (responseEntity != null && "AAAAAAA".equals(responseEntity.get(Dict.CODE)) && responseEntity.get("data") != null) {
            groupList = (List<Map>) responseEntity.get("data");
        } else {
            if (responseEntity != null && responseEntity.get("msg") != null) {
                logger.error("其他后台服务异常：{}", responseEntity.get("msg"));
            }
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        if(!Util.isNullOrEmpty(groupList)){
            groupMap = groupList.get(0);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        redisTemplate.opsForValue().set(Dict._CACHEGROUP + groupId,
                objectMapper.readValue(JSONObject.fromObject(groupMap).toString(), Map.class), 7, TimeUnit.DAYS);
        return groupMap;
    }
    /**
     * 根据根据小组id以下信息，返回数据类型为Map，Map里包含用户实体属性
     *
     * @param teamId 用户id
     * @return Map
     */
    public Map queryTeamDetail(String teamId) throws IOException {
        Map TeamMap = new HashMap();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("teamId", teamId);
        if (!url.contains("/fusermanage")) {
            return TeamMap;
        }
        String[] apiUrls = url.split("api");
        Map responseEntity = restTemplate.postForObject(apiUrls[0] + "api/team/queryTeamDetail", paramMap, Map.class);
        if (responseEntity != null && "AAAAAAA".equals(responseEntity.get(Dict.CODE)) && responseEntity.get("data") != null) {
            TeamMap = (Map) responseEntity.get("data");
        } else {
            if (responseEntity != null && responseEntity.get("msg") != null) {
                logger.error("其他后台服务异常：{}", responseEntity.get("msg"));
            }
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        redisTemplate.opsForValue().set(Dict._CACHETEAM + teamId,
                objectMapper.readValue(JSONObject.fromObject(TeamMap).toString(), Map.class), 7, TimeUnit.DAYS);
        return TeamMap;
    }
}
