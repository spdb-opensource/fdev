package com.spdb.fdev.pipeline.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.entity.Author;
import com.spdb.fdev.pipeline.entity.Entity;
import com.spdb.fdev.pipeline.service.IAppService;
import com.spdb.fdev.pipeline.service.IUserService;
import com.spdb.fdev.pipeline.transport.GitlabTransport;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class AppServiceImpl implements IAppService {
    @Autowired
    private RestTransport restTransport;

    @Value("${gitlab.api.url}")
    private String gitlabApiUrl;

    @Value("${gitlab.manager.token}")
    private String gitlabManagerToken;

    @Autowired
    private GitlabTransport gitlabTransport;
    @Autowired
    private IUserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);

    @Override
    public Map<String, Object> queryAppDetailById(String id) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        // 发app模块获取appliaction详细信息
        send_map.put(Dict.ID, id);
        send_map.put(Dict.REST_CODE, "findbyId");
        return (Map<String, Object>) restTransport.submit(send_map);
    }

    @Override
    public Map<String, Object> queryGitProjectDetail(String id) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        // 发app模块获取appliaction详细信息
        send_map.put(Dict.ID, id);
        send_map.put(Dict.REST_CODE, "queryGitProject");
        return (Map<String, Object>) restTransport.submit(send_map);
    }

    @Override
    public List queryEntityVariables(String[] entitys, String env) throws Exception {
        List variables = new ArrayList();
        for (String nameEn : entitys) {
            Map<String, Object> send_map = new HashMap<>();
            // 发app模块获取appliaction详细信息
            send_map.put(Dict.NAME_EN, nameEn);
            send_map.put(Dict.ENV_NAME_EN, env);
            send_map.put(Dict.REST_CODE, "queryModelEnvByModelNameEn");
            List<Map<String, Object>> list = (List<Map<String, Object>>)restTransport.submit(send_map);
            if(CommonUtils.isNullOrEmpty(list)){
                continue;
            }
            variables.addAll((List)list.get(0).get(Dict.VARIABLES));
        }
        return variables;
    }

    @Override
    public Map queryAppDetailByGitId(Integer gitlabProjectId) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        // 发app模块获取appliaction详细信息
        send_map.put(Dict.ID, gitlabProjectId);
        send_map.put(Dict.REST_CODE, "queryAppDetailByGitId");
        return (Map)restTransport.submit(send_map);
    }

    /**
     * 调用老的应用模块
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> queryMyApps(String userId) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        // 查询我的应用
        // 判断上下文根，判断是否是老流水线调用（老模块调用老模块）
        send_map.put(Dict.USER_ID, userId);
        send_map.put(Dict.REST_CODE, "queryMyApps");
        return (List<Map<String, Object>>) restTransport.submit(send_map);
    }


    public boolean branchExists(Integer gitlabId, String branchName) {
        String branchUrl = new StringBuilder(gitlabApiUrl).append("projects/").append(gitlabId).append("/repository/branches/")
                .append(branchName).toString();
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN_UP, gitlabManagerToken);
        boolean flag = false;
        try {
            gitlabTransport.submitGet(branchUrl, header);
            flag = true;
        } catch (Exception e) {
            logger.error("gitlabId:{}对应{}分支不存在", gitlabId, branchName);
        }
        return flag;
    }

    @Override
    public boolean checkTagExisted(Integer gitlabId, String branchName) throws Exception {
        String tagUrl = new StringBuilder(gitlabApiUrl).append("projects/").append(gitlabId).append("/repository/tags/")
                .append(branchName).toString();
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN_UP, gitlabManagerToken);
        boolean flag = false;
        try {
            gitlabTransport.submitGet(tagUrl, header);
            flag = true;
        } catch (Exception e) {
            logger.error("gitlabId:{}对应{}分支不存在", gitlabId, branchName);
        }
        return flag;
    }

    @Override
    public List<Map> queryModelTemplateDetailInfo(String id) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.ID,id);
        send_map.put(Dict.REST_CODE, "queryEntityTemplateById");
        Map data = (Map<String, Object>)restTransport.submit(send_map);
        Object envKey = data.get("properties");
        List<Map> envKeyList = null;
        if (!CommonUtils.isNullOrEmpty(envKey)){
            envKeyList = (List<Map>) envKey;
        }
        return envKeyList;
    }

    @Override
    public Author queryUserByNameEn(String gitUserNameEn) throws Exception {
        Author author = new Author();
        Map<String, Object> send_map = new HashMap<>();
        send_map.put("git_user",gitUserNameEn);
        send_map.put(Dict.REST_CODE, "query");
        List<Map> data = (List<Map>)restTransport.submit(send_map);
        if (!CommonUtils.isNullOrEmpty(data)){
            String id = (String) data.get(0).get("id");
            String nameEn = (String) data.get(0).get("user_name_en");
            String nameCn = (String) data.get(0).get("user_name_cn");
            author.setId(id);
            author.setNameEn(nameEn);
            author.setNameCn(nameCn);
        }
        return author;
    }

    @Override
    public List<String> queryCurrentAndChildGroup(String id) throws Exception {
        List<String> resultList = new ArrayList<>();
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.ID,id);
        send_map.put(Dict.REST_CODE, "queryCurrentAndChildGroup");
        List<Map> data = (List<Map>)restTransport.submit(send_map);
        if (!CommonUtils.isNullOrEmpty(data)){
           for (Map map : data){
               if (Constants.STATUS_OPEN.equals(map.get(Dict.STATUS))){
                   String groupId = String.valueOf(map.get(Dict.ID));
                   resultList.add(groupId);
               }
           }
        }
        return resultList;
    }

    /**
     * 根据实体id和环境名确定唯一的环境映射值
     * 暂时未用
     * @param entityId
     * @param envName
     * @return
     * @throws Exception
     */
    @Override
    public Map queryEntityModelDetail(String entityId,String envName) throws Exception {
        Map resultMap = new HashMap();
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.ID,entityId);
        //send_map.put(Dict.ID,entityId);
        send_map.put(Dict.REST_CODE, "queryModelEnvByModelNameEn");
        Map data = (Map<String, Object>)restTransport.submit(send_map);
        Object envKey = data.get("propertiesValue");
        Map envKeyMap = new HashMap();
        if (!CommonUtils.isNullOrEmpty(envKey)){
            envKeyMap =(Map) envKey;
            for (Object key:envKeyMap.keySet()){
                Object o = envKeyMap.get(key);
                if (!CommonUtils.isNullOrEmpty(o)){
                    Map innerMap = (Map)o;
                    for (Object innerKey : innerMap.keySet()){
                        if (String.valueOf(innerKey).equals(envName)){
                            Object innerMapValue = innerMap.get(innerKey);
                            resultMap.putAll((Map)innerMapValue);
                            break;
                        }
                    }
                }
            }
        }
        return resultMap;
    }

    /**
     * 根据实体和环境查询映射值
     * @param entityIds
     * @param env
     * @return
     * @throws Exception
     */
    @Override
    public Map queryEntityMapping(String[] entityIds, String env) throws Exception {
        Map resultMap = new HashMap();
        Map<String, Object> send_map = new HashMap<>();
        send_map.put("entityIdList", entityIds);
        send_map.put("envNameEn", env);
        send_map.put(Dict.REST_CODE, "queryEntityMapping");
        List<Map<String, Object>> list = (List<Map<String, Object>>)restTransport.submit(send_map);
        if(!CommonUtils.isNullOrEmpty(list)){
            Object propertiesValue = list.get(0).get("propertiesValue");
            if (!CommonUtils.isNullOrEmpty(propertiesValue)){
                Map propertiesValueMap = (Map)propertiesValue;
                if (!CommonUtils.isNullOrEmpty(propertiesValueMap.get(env))){
                    resultMap = (Map) propertiesValueMap.get(env);
                }
            }
        }
        return resultMap;
    }

    @Override
    public Map queryUserInfoByUserId(String userId) throws Exception {
        Map userMap = new HashMap();
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.ID,userId);
        send_map.put(Dict.REST_CODE, "query");
        List<Map> data = (List<Map>)restTransport.submit(send_map);
        if (!CommonUtils.isNullOrEmpty(data)){
            String id = String.valueOf(data.get(0).get("id"));
            String nameEn = String.valueOf(data.get(0).get("user_name_en"));
            String nameCn = String.valueOf(data.get(0).get("user_name_cn"));
            String groupId = String.valueOf(data.get(0).get("group_id"));
            userMap.put(Dict.ID,id);
            userMap.put(Dict.NAMEEN,nameEn);
            userMap.put(Dict.NAMECN,nameCn);
            userMap.put(Dict.GROUPID,groupId);
        }
        return userMap;
    }
}
