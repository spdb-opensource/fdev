package com.spdb.fdev.component.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.HttpRequestUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.service.IGitlabSerice;
import com.spdb.fdev.component.service.IGitlabUserService;
import com.spdb.fdev.component.transport.GitlabTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class GitlabUserServiceImpl implements IGitlabUserService {

    private static final Logger logger = LoggerFactory.getLogger(GitlabUserServiceImpl.class);

    @Value("${gitlab.api.url}")
    private String gitlabApiUrl;

    @Value("${gitlab.manager.token}")
    private String token;

    @Value("${gitlab.manager.userid}")
    private String userid;

    @Autowired
    private GitlabTransport gitlabTransport;

    @Autowired
    private IGitlabSerice gitlabSerice;

    @Override
    public void addMembers(Map map) throws Exception {
        String role = (String) map.get(Dict.ROLE);
        String projectid = (String) map.get(Dict.ID);
        List<String> git_user_id = (List<String>) map.get(Dict.GIT_USER_ID);
        //查询项目下所有用户权限信息
        List<Map> userList = this.getProjectsUser(projectid, token);
        // 获取 需要添加权限的用户Gituser列表
        if (!CommonUtils.isNullOrEmpty(git_user_id)) {
            for (String gitUserId : git_user_id) {// 遍历用户gitlibId列表
                try {
                    if (validateUser(gitUserId, userList, role))
                        continue;
                    addMember(projectid, gitUserId, role, token);
                } catch (HttpClientErrorException e) {
                    logger.error("e:" + e);
                    String message = new String(e.getResponseBodyAsByteArray());
                    JSONObject json = JSONObject.parseObject(message);
                    Map jsonmap = json.toJavaObject(Map.class);
                    Object msg = jsonmap.get("message");
                    if (msg instanceof Map && ((Map) msg).containsKey(Dict.ACCESS_LEVEL)) {
                        logger.error(message);
                    } else if (msg instanceof String && ((String) msg).startsWith(Dict.MEMBER)) {
                        logger.error(message);
                        try {
                            editProjectMember(projectid, gitUserId, role, token);
                        } catch (HttpClientErrorException e2) {
                            logger.error("edit project member error " + e2);
                        } catch (FdevException e1) {
                            //捕捉fdev异常
                            logger.error("FdevExceptionCode:" + e1.getCode());
                            if (!CommonUtils.isNullOrEmpty(e1.getArgs())) {
                                logger.error((String) e1.getArgs()[0]);
                            }
                        }
                    }
                    logger.error("this user add member error" + gitUserId);
                }
            }
        }
    }

    @Override
    public Object addMember(String id, String userId, String role, String token) throws Exception {
        String member_url = gitlabApiUrl + "/projects/" + id + "/members";
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.USER_ID, userId);
        param.put(Dict.ACCESS_LEVEL, role);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(param));
        return HttpRequestUtils.sendPOST(member_url, jsonObject);
    }


    /**
     * 支持分页，查询所有gitlab用户
     *
     * @param id
     * @param gitlab_token
     * @return
     * @throws Exception
     */
    @Override
    public List<Map> getProjectsUser(String id, String gitlab_token) throws Exception {
        List<Map> result = new ArrayList<>();
        boolean flag = true;
        int i = 1;
        while (flag) {
            String userUrl = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id + "/members?page=" + i + "&per_page=100";
            MultiValueMap<String, String> header = new LinkedMultiValueMap();
            header.add(Dict.PRIVATE_TOKEN, gitlab_token);
            Object object = gitlabTransport.submitGet(userUrl, header);
            if (object != null) {
                List<Map> userList = JSONArray.parseArray((String) object, Map.class);
                if (userList != null && userList.size() > 0) {
                    i++;
                    result.addAll(userList);
                } else {
                    flag = false;
                }
            } else {
                flag = false;
            }
        }
        return result;
    }

    @Override
    public String queryUserIdByName(String userName, String token) throws Exception {
        String user_url = gitlabApiUrl + "/users/?username=" + userName;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        Object submitGet = gitlabTransport.submitGet(user_url, header);
        JSONArray parseArray = JSONObject.parseArray(((String) submitGet));
        if (CommonUtils.isNullOrEmpty(parseArray)) {
            return null;
        }
        JSONObject object = (JSONObject) parseArray.get(0);
        if (CommonUtils.isNullOrEmpty(parseArray)) {
            return null;
        }
        return String.valueOf(object.get(Dict.ID));
    }

    @Override
    public boolean validateUser(String git_user_id, List<Map> userList, String role) throws Exception {
        Integer level = Integer.parseInt(role);
        for (Map map : userList) {
            Integer id = (Integer) map.get(Dict.ID);
            Integer integer = Integer.valueOf(git_user_id);
            if (id.equals(integer)) {
                Integer access_level = (Integer) map.get(Dict.ACCESS_LEVEL);
                if (access_level >= level) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public Object editProjectMember(String id, String userId, String role, String token) throws Exception {
        String member_url = gitlabApiUrl + "projects/" + id + "/members/" + userId + "?access_level=" + role;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitPut(member_url, header);
    }

    /**
     * 修改fdev用户权限
     *
     * @param projectId 项目id
     * @param role      人员权限
     * @throws Exception
     */
    @Override
    public void changeFdevRole(String projectId, String role) throws Exception {
        logger.info("fdev增加{}权限开始", role);
        //查询项目下所有用户权限信息
        List<Map> userList = this.getProjectsUser(projectId, token);
        // 获取 需要添加权限的用户Gituser列表
        try {
            if (Dict.MAINTAINER.equals(role) && validateUser(this.userid, userList, role))
                return;
            addMember(projectId, this.userid, role, token);
        } catch (HttpClientErrorException e) {
            logger.error("e:" + e);
            String message = new String(e.getResponseBodyAsByteArray());
            JSONObject json = JSONObject.parseObject(message);
            Map jsonmap = json.toJavaObject(Map.class);
            Object msg = jsonmap.get("message");
            if (msg instanceof Map && ((Map) msg).containsKey(Dict.ACCESS_LEVEL)) {
                logger.error(message);
            } else if (msg instanceof String && ((String) msg).startsWith(Dict.MEMBER)) {
                logger.error(message);
                try {
                    editProjectMember(projectId, this.userid, role, token);
                } catch (Exception e2) {
                    logger.error("edit project member error " + e2);
                }
            }
            logger.error("this user add member error" + this.userid);
        }
    }

}
