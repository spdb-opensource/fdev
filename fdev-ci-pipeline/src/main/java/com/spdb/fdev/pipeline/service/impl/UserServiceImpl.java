package com.spdb.fdev.pipeline.service.impl;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.pipeline.entity.Author;
import com.spdb.fdev.pipeline.service.IUserService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements IUserService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /*@Autowired
    private FdevUserCacheUtil fdevUserCacheUtil;*/

    @Value("${userManager.url:}")
    public String url;

    @Override
    public String getUserGitToken(String gitUsername) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        // 发app模块获取appliaction详细信息
        send_map.put("git_user", gitUsername);
        send_map.put(Dict.REST_CODE, "queryUserCoreData");
        String gitToken;
        try{
            List<Map<String, Object>> result = (List<Map<String, Object>>) restTransport.submit(send_map);
            gitToken = (String) result.get(0).get("git_token");
        }catch (Exception e){
            logger.error("发送用户模块，查询用户失败" + gitUsername);
            throw new FdevException(ErrorConstants.QUERY_USER_IS_FAILD, new String[]{gitUsername});
        }
        return gitToken;
    }

  /*  @Override
    public Map getUserInfoByUserNameEn(String nameEn) {

        Map<String, Object> send_map = new HashMap<>();
        // 发app模块获取appliaction详细信息
        send_map.put("user_name_en", nameEn);
        send_map.put(Dict.REST_CODE, "query");
        Map user;
        try{
            List<Map<String, Object>> result = (List<Map<String, Object>>) restTransport.submit(send_map);
            user = (Map) result.get(0);
        }catch (Exception e){
            logger.error("发送用户模块，查询用户失败" + nameEn);
            throw new FdevException(ErrorConstants.QUERY_USER_IS_FAILD, new String[]{nameEn});
        }
        return user;
    }*/

    @Override
    public User getUserFromRedis() throws Exception {
        /*User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));*/
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute("_USER");
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
        }
        return user;
    }

    @Override
    public Author getAuthor() throws Exception {
        Author author = new Author();
        User user = this.getUserFromRedis();
        /*BeanUtils.copyProperties(user,author);*/
        author.setId(user.getId());
        author.setNameEn(user.getUser_name_en());
        author.setNameCn(user.getUser_name_cn());
        return author;
    }


    /**
     * 根据当前用户的groupId来获取条线section，Common包实现
     *
     * @param groupId
     * @return
     * @throws Exception
     */
//    每个group Map里面的结构为如下
//    {
//        "id": "组id",
//        "name": "组名",
//        "fullName": "组名全称",
//        "parent_id": "父组id",
//        "status": "状态：1表示正常，0表示已删除。此接口返回的数据status都为1",
//        "sortNum": "排序计数",
//        "groupType": "组类型：1表示处，2表示二级处，3表示条线"
//    }

    /**
     * 获取当前组以及子组信息
     *
     * @param groupId
     * @return
     */
    @Override
    public List<Map> getChildGroupByGroupId(String groupId) {
//        Map<String, Object> groupMap = userVerifyUtil.getChildGroup(groupId);
        Map<String, Object> groupMap = this.querySectionUser(groupId);
        //当前条线信息section
        if (CommonUtils.isNullOrEmpty(groupMap)) {
            return null;
        }else {
            //获取group信息
            List<Map> childGroup = (List<Map>) groupMap.get(Dict.CHILDGROUP);
            return childGroup;
        }
    }

    /**
     * 获取当前group id 下的条线以及子组信息
     *
     * @param groupId
     * @return
     */
    /*@Override
    public List<Map> getSectionChildByGroupId(String groupId) {
        Map<String, Object> groupMap = userVerifyUtil.getChildGroup(groupId);
        //当前条线信息section
        if (CommonUtils.isNullOrEmpty(groupMap)) {
            return null;
        }else {
            //获取group信息
            List<Map> sectionChildGroup = (List<Map>) groupMap.get(Dict.SECTIONCHILDGROUP);
            return sectionChildGroup;
        }
    }*/

    /**
     * 获取当前组的条线 id集合
     *
     * @param groupId
     * @return
     */
    /*@Override
    public List<String> getLineIdsByGroupId(String groupId) {
        List<String> lineIdList = new ArrayList<>();
        List<Map> sectionChildByGroup = getSectionChildByGroupId(groupId);
        if (CommonUtils.isNullOrEmpty(sectionChildByGroup)) {
            return null;
        }
        sectionChildByGroup.stream().forEach(e ->{
            String groupType = (String) e.get(Dict.GROUPTYPE);
            if (groupType.equals(Constants.GROUPTYPETHREE)) {
                //当是条线就取出来
                lineIdList.add((String) e.get(Dict.ID));
            }
        });
        logger.info("******getLineIdsByGroupId, send groupId: "+groupId+", result groupids: "+ lineIdList);
        return lineIdList;
    }*/

    /**
     * 获取当前组以及子组的组id集合
     *
     * @param groupId
     * @return
     */
    @Override
    public List<String> getChildGroupIdsByGroupId(String groupId) {
        List<String> groupIds = new ArrayList<>();
        List<Map> childGroupByGroup = getChildGroupByGroupId(groupId);
        if (CommonUtils.isNullOrEmpty(childGroupByGroup))
            return null;
        childGroupByGroup.stream().forEach(e -> {
            groupIds.add((String) e.get(Dict.ID));
        });
        logger.info("******queryChildGroupIdsByGroupId, send groupId: "+groupId+", result groupids: "+ groupIds);
        return groupIds;
    }


    /**
     * 通过id查询用户信息
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public Map queryUserById(String id) {
        Map userCoreData = null;
        try {
            userCoreData = this.queryUserCoreData(id);
            if (CommonUtils.isNullOrEmpty(userCoreData))
                throw new FdevException(ErrorConstants.USER_NOT_AUTHOR, new String[]{id});
        } catch (Exception e) {
            logger.error("发送用户模块，查询用户失败" + id);
            throw new FdevException(ErrorConstants.QUERY_USER_IS_FAILD, new String[]{id});
        }
        return userCoreData;
    }

    /**
     * 通过id查询用户信息
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public Map queryUserByIdComm(String id) throws Exception {
        Map userCoreData = null;
        try {
//            userCoreData = fdevUserCacheUtil.queryUserCoreData(id);
            userCoreData = this.queryUserCoreData(id);
        } catch (IOException e) {
            logger.error("发送用户模块，查询用户失败" + id);
            throw new FdevException(ErrorConstants.QUERY_USER_IS_FAILD, new String[]{id});
        }
        return userCoreData;
    }


    /**
     * 根据组id查询条线id
     *
     * @param groupId
     * @return
     * @throws Exception
     */
    @Override
    public Map querySectionUser(String groupId) {
        Map<String, Object> send_map = new HashMap<>();
        // 发app模块获取appliaction详细信息
        send_map.put("id", groupId);
        send_map.put("status", "1");
        send_map.put(Dict.REST_CODE, "querySectionUser");
        Map<String, Object> result = new HashMap<>();
        try{
            List chilgGroup = (List) restTransport.submit(send_map);
            result.put("childGroup", chilgGroup);
        }catch (Exception e){
            logger.error("发送用户模块，查询用户失败" + groupId);
            throw new FdevException(ErrorConstants.QUERY_USER_IS_FAILD, new String[]{groupId});
        }
        if (CommonUtils.isNullOrEmpty(result))
            return null;
        else
            return result;
    }

    public Map queryUserCoreData(String userId) throws IOException {
        new ArrayList();
        Map userMap = new HashMap();
        Map<String, Object> send_map = new HashMap();
        // 发app模块获取appliaction详细信息
        send_map.put("id", userId);
        send_map.put(Dict.REST_CODE, "queryUserCoreData");
        String gitToken;
        try{
            List<Map<String, Object>> result = (List<Map<String, Object>>) restTransport.submit(send_map);
            userMap = result.get(0);
        }catch (Exception e){
            logger.error("发送用户模块，查询用户失败" + userId);
            throw new FdevException(ErrorConstants.QUERY_USER_IS_FAILD, new String[]{userId});
        }
        return userMap;
    }

}
