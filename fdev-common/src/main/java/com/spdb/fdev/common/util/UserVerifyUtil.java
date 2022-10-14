package com.spdb.fdev.common.util;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.dict.Dict;
import com.spdb.fdev.common.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description：
 * author Hubery
 * date 2020-07-26
 * version v0.0.1
 * since v0.0.1
 **/
@Component
@RefreshScope
public class UserVerifyUtil {

    @Value("${userStuckPoint.RoleId:}")
    public String stuckPointRoleId;

    @Value("${userManager.url:}")
    public String url;

    private static Logger logger = LoggerFactory.getLogger(RestTransport.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource(name = "getRestTemplate")
    private RestTemplate restTemplate;

    /**
     * 验证用户角色是否为卡点管理员 userRoleIsStuckPointManage
     *
     * @param
     * @return
     */
    public boolean userRoleIsSPM(List<String> role_id) {
        if (Util.isNullOrEmpty(stuckPointRoleId)) {
            throw new FdevException(ErrorConstants.PARAM_CONFIG_ERROR, new String[]{Dict.USERSTUCKPOINTROLEID});
        }
        boolean isStuckPointManage = false;
        for (String roleid : role_id) {
            if (roleid != null && roleid.equals(stuckPointRoleId)) {
                isStuckPointManage = true;
                return isStuckPointManage;
            }
        }
        return isStuckPointManage;
    }

    public boolean userGroupVerify(String groupName) throws Exception {
        String back = (String) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(Dict.BACKEND_REQUEST_FLAG_KEY);
        if (Dict.BACKEND_REQUEST_FLAG_VALUE.equals(back)) {
            return true;
        }
        try {
            Map sessionFuser = getSessionFuser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(Dict.TOKEN_AUTHORIZATION_KEY));
            if (!Util.isNullOrEmpty(sessionFuser)) {
                Map group = (Map) sessionFuser.get("group");
                String fullName = ((String) group.get("fullName")).split("-")[0];
                if (fullName.equals(Util.isNullOrEmpty(groupName) ? groupName = "互联网应用" : groupName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("user group info error ! ", e.getMessage());
        }
        return false;
    }


    /**
     * 从redis中获取用户信息
     *
     * @return 用户英文名
     * @throws Exception
     */
    public User getSessionUser(String token) throws Exception {
        return (User) redisTemplate.opsForValue().get(Dict._USER + token);
    }

    /**
     * 从redis中获取用户信息
     *
     * @return 用户info
     * @throws Exception
     */
    public Map getSessionFuser(String token) throws Exception {
        return (Map) redisTemplate.opsForValue().get(Dict._FUSER + token);
    }

    /**
     * 根据当前用户当前所选团队空间查询其子组信息（包括所选团队空间）
     *
     * @return
     */
    public List<Map<String, Object>> getChildGroupsByTeamSpace() throws Exception {
        String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(Dict.TOKEN_AUTHORIZATION_KEY);
        String userNameEn = Util.getUserByToken(token);
        String groupId = (String)redisTemplate.opsForValue().get("space.groupId" + userNameEn);
        if (StringUtils.isEmpty(groupId)) {
            return new ArrayList<>();
        }
        Map<String, Object> childGroupMap = this.getChildGroup(groupId);
        // 获取当前组及其子组信息
        return (List<Map<String, Object>>) childGroupMap.get(Dict.CHILDGROUP);
    }

    /**
     * 根据组id查询以下信息，返回数据类型为Map，Map里包括以下三个字段
     * 1.section：当前组所在条线信息，数据类型为Map
     * 2.sectionChildGroup：当前组所在条线及其子组信息，若条线信息为空，则返回当前组及其子组信息，数据类型为List<Map>
     * 3.childGroup：当前组及其子组信息，数据类型为List<Map>
     *
     * @param groupId 组id
     * @return Map
     */
    public Map<String, Object> getChildGroup(String groupId) {
        Map<String, Object> childGroup = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", groupId);
        if (!url.contains("/fusermanage")) {
            return childGroup;
        }
        String[] apiUrls = url.split("api");
        Map responseEntity = restTemplate.postForObject(apiUrls[0] + "api/group/querySectionGroup", paramMap, Map.class);
        if (responseEntity != null && "AAAAAAA".equals(responseEntity.get(Dict.CODE)) && responseEntity.get("data") != null) {
            childGroup = (Map<String, Object>) responseEntity.get("data");
        } else {
            if (responseEntity != null && responseEntity.get("msg") != null) {
                logger.error("其他后台服务异常：{}", responseEntity.get("msg"));
            }
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        return childGroup;
    }

    /**
     * 获取团队信息
     *
     * @return
     * @throws Exception
     */
    public Map getTeamInfo() throws Exception {
        String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(Dict.TOKEN_AUTHORIZATION_KEY);
//        String userNameEn = tokenMangerUtil.getUserByToken(token);
        String userNameEn = Util.getUserByToken(token);
        String teamId = (String)redisTemplate.opsForValue().get("space.teamId" + userNameEn);
        //如果获取不到团队id或者服务不来自新fdev
        if (Util.isNullOrEmpty(teamId) || !this.url.contains("/fusermanage")) {
            return new HashMap();
        } else {
            Map<String, Object> paramMap = new HashMap();
            paramMap.put(Dict.TEAMID, teamId);
            String[] apiUrls = this.url.split("api");
            Map responseEntity = (Map) this.restTemplate.postForObject(apiUrls[0] + "api/team/queryTeamById", paramMap, Map.class, new Object[0]);
            if (responseEntity != null && "AAAAAAA".equals(responseEntity.get("code")) && responseEntity.get("data") != null) {
                Map<String, Object> result = (Map) responseEntity.get("data");
                return result;
            } else {
                if (responseEntity != null && responseEntity.get("msg") != null) {
                    logger.error("其他后台服务异常：{}", responseEntity.get("msg"));
                }
                throw new FdevException("COMM001");
            }
        }
    }

    /**
     * 获取当前空间
     *
     * @return
     * @throws Exception
     */
    public Map getCurrentSpace() throws Exception {
        String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(Dict.TOKEN_AUTHORIZATION_KEY);
//        String userNameEn = tokenMangerUtil.getUserByToken(token);
        String userNameEn = Util.getUserByToken(token);
        String teamId = (String)redisTemplate.opsForValue().get("space.teamId" + userNameEn);
        String groupId = (String)redisTemplate.opsForValue().get("space.groupId" + userNameEn);
        Map result = new HashMap();
        result.put(Dict.ISTEAM, !Util.isNullOrEmpty(teamId));
        result.put(Dict.GROUPID, groupId);
        result.put(Dict.TEAMID, teamId);
        return result;
    }

    /**
     * 从redis中获取用户信息
     *
     * @return 用户英文名
     * @throws Exception
     */
    public User getRedisUser() throws Exception {
        String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).
                getRequest().getHeader(Dict.TOKEN_AUTHORIZATION_KEY);
        return (User) redisTemplate.opsForValue().get(Dict._USER + token);
    }

    /**
     * 从redis中获取用户信息
     *
     * @return 用户info
     * @throws Exception
     */
    public Map getRedisFuser() throws Exception {
        String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).
                getRequest().getHeader(Dict.TOKEN_AUTHORIZATION_KEY);
        return (Map) redisTemplate.opsForValue().get(Dict._FUSER + token);
    }
}
