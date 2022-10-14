package com.spdb.fdev.fdevapp.spdb.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.spdb.dao.IGitlabAPIDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.base.utils.GitlabTransport;
import com.spdb.fdev.fdevapp.base.utils.HttpRequestUtils;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabUserService;

@Service
@RefreshScope
public class GitlabUserServiceImpl implements IGitlabUserService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GitlabTransport gitlabTransport;
    @Autowired
    private IGitlabAPIDao gitlabAPIDao;

    @Value("${gitlib.path}")
    private String url;// gitlab地址http://xxx/api/v4/

    @Value("${gitlab.token}")
    private String gitlab_token;

    @Value("${gitlab.userid}")
    private String userid;

    /**
     * 通过项目id查询项目信息
     *
     * @param projectId 项目id
     * @param userId    用户id
     * @param token     验证码
     * @return
     * @throws Exception
     */
    @Override
    public int queryUserRole(String projectId, String userId, String token) throws Exception {
        String role_url = url + "/projects/" + projectId + "/members/" + userId;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        Object obj = gitlabTransport.submitGet(role_url, header);
        Map result = (Map) JSONObject.parseArray((String) obj);
        int role = (Integer) result.get(Dict.ACCESS_LEVEL);
        /*
         * 10 => Guest access 20 => Reporter access 30 => Developer access 40 =>
         * Maintainer access 50 => Owner access # Only valid for groups
         */
        return role;
    }

    @Override
    public String checkGitlabUser(String userId, String token) throws Exception {
        String user_url = url + "users/" + userId;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        try {
            Object submit = gitlabTransport.submitGet(user_url, header);
            Map parseObject = JSONObject.parseObject((String) submit, Map.class);
            return (String) parseObject.get(Dict.USERNAME);
        } catch (Exception e) {
            logger.info("user not exist,userId:" + userId);
            return null;
        }
    }

    @Override
    public Object addMember(String id, String userId, String role, String token) throws Exception {
        String member_url = url + "/projects/" + id + "/members";
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.USER_ID, userId);
        param.put(Dict.ACCESS_LEVEL, role);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(param));
        return HttpRequestUtils.sendPOST(member_url, jsonObject);
    }

    @Override
    public Object addResourceMember(String id, String userId, String role, String token) throws Exception {
        String member_url = url + "/projects/" + id + "/members";
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.USER_ID, userId);
        param.put(Dict.ACCESS_LEVEL, role);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(param));
        return HttpRequestUtils.sendPOST(member_url, jsonObject);
    }

    @Override
    public Object addGroupMember(String path, String userId, String role, String token) throws Exception {
        String[] split = path.split("/");
        Map<String, String> param = new HashMap<String, String>();
        String searchGroup_url = url + "groups?search=" + split[split.length - 1];
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        String[] split2 = url.split(Dict.API);// 截取git地址
        String replace = path.replace(split2[0], "");// 获取path full_path
        Map<String, Object> result = new HashMap<String, Object>();
        // 通光group地址 获取groupid
        int namespaceid = 0;
        Object obj = gitlabTransport.submitGet(searchGroup_url, header);
        List group = (List) JSONArray.parse((String) obj);
        for (Object object : group) {
            Map map = (Map) object;
            if (map.get(Dict.FULL_PATH).equals(replace)) {
                namespaceid = (Integer) map.get(Dict.ID);
            }
        }
        if (namespaceid == 0) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关组信息"});
        }
        // 通过namespaceid查询小组成员
        String group_member_url = url + "/groups/" + namespaceid + "/members";
        Object obj1 = gitlabTransport.submitGet(group_member_url, header);
        List members = (List) JSONArray.parse((String) obj1);
        List<String> userlist = new ArrayList<String>();
        for (Object json : members) {
            Map groupuser = (Map) json;
            userlist.add(String.valueOf(groupuser.get(Dict.ID)));
        }
        if (isProjectMember(userId, userlist)) {// 若该用户在小组已有权限 则跳过
            return null;
        }
        String group_url = url + "/groups/" + namespaceid + "/members";
        param.put(Dict.USER_ID, userId);
        param.put(Dict.ACCESS_LEVEL, role);
        param.put(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitPost(group_url, param);
    }

    /**
     * 检验用户的存在与否
     *
     * @param id
     * @return
     */
    @Override
    public boolean userVaildation(String id) throws Exception {
        boolean flag = false;
        String user_url = url + "/users/" + id;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        try {
            ResponseEntity<String> sendGET = HttpRequestUtils.sendGET(user_url, header);
            if (sendGET.getBody() != null) {
                flag = true;
            }
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    @Override
    public Object editProjectMember(String id, String userId, String role, String token) throws Exception {
        String member_url = url + "/projects/" + id + "/members/" + userId + "?access_level=" + role;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitPut(member_url, header);
    }

    @Override
    public boolean isProjectMember(String user_id, List member_list) throws Exception {
        for (Object id : member_list) {
            String member = String.valueOf(id);
            if (user_id.equals(member)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, Object> queryProjectOwner(String id, String token) throws Exception {
        String project_url = url + "projects/" + id;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        Object submitGet = gitlabTransport.submitGet(project_url, header);
        JSONObject json = JSONObject.parseObject(((String) submitGet));
        Map map = json.toJavaObject(Map.class);
        if (CommonUtils.isNullOrEmpty(map)) {
            return null;
        }
        return (Map) (map.get("owner"));
    }

    @Override
    public boolean checkGitlabToken(String token) throws Exception {
        String project_url = url + "/groups?search=ebank";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        try {
            gitlabTransport.submitGet(project_url, header);//若正常发送 则token正确
            return true;
        } catch (FdevException e) {//否则token 错误
            logger.error("token出错，token：" + token);
            return false;
        }
    }


    @Override
    public void addMembers(Map map) throws Exception {
        String role = (String) map.get(Dict.ROLE);
        String projectid = (String) map.get(Dict.ID);
        List<String> user_ids = (List<String>) map.get(Dict.GIT_USER_ID);
        //查询项目下所有用户权限信息
        List<Map> userList = gitlabAPIDao.getProjectsUser(projectid, gitlab_token);
        // 获取 需要添加权限 的用户英文名列表
        for (String user_id : user_ids) {
            if (!CommonUtils.isNullOrEmpty(user_id)) {
                try {
                    if (validateUser(user_id, userList))
                        continue;
                    addMember(projectid, user_id, role, gitlab_token);
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
                            editProjectMember(projectid, user_id, role, gitlab_token);
                        } catch (Exception e2) {
                            logger.error("edit project member error " + e2);
                        }
                    }
                    logger.error("this user add member error" + user_id);
                }
            }
        }
    }

    /**
     * 检验用户是否具有maintainer权限，是返回true
     *
     * @param user_id
     * @param userList
     * @return
     * @throws Exception
     */
    public boolean validateUser(String user_id, List<Map> userList) throws Exception {
        for (Map map : userList) {
            Integer id = (Integer) map.get(Dict.ID);
            Integer integer = Integer.valueOf(user_id);
            if (id == integer) {
                Integer access_level = (Integer) map.get(Dict.ACCESS_LEVEL);
                if (access_level >= 40) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 检验用户权限，是返回true
     *
     * @param git_user_id
     * @param userList
     * @param role
     * @return
     * @throws Exception
     */
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
            String userUrl = url + "projects/" + id + "/members?page=" + i + "&per_page=100";
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

    /**
     * 修改fdev用户权限
     *
     * @param projectId 项目id
     * @param role      人员权限
     * @throws Exception
     */
    @Override
    public void changeFdevRole(String projectId, String role) throws Exception {
        //查询项目下所有用户权限信息
        List<Map> userList = this.getProjectsUser(projectId, gitlab_token);
        // 获取 需要添加权限的用户Gituser列表
        try {
            if (Dict.MAINTAINER.equals(role) && validateUser(this.userid, userList, role))
                return;
            addMember(projectId, this.userid, role, gitlab_token);
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
                    editProjectMember(projectId, this.userid, role, gitlab_token);
                } catch (Exception e2) {
                    logger.error("edit project member error " + e2);
                }
            }
            logger.error("this user add member error" + this.userid);
        }
    }

}
