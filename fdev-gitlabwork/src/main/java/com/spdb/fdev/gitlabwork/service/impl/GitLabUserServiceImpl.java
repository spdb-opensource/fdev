package com.spdb.fdev.gitlabwork.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.gitlabwork.dao.IGitLabUserDao;
import com.spdb.fdev.gitlabwork.entiy.GitlabUser;
import com.spdb.fdev.gitlabwork.service.IGitLabUserService;
import com.spdb.fdev.gitlabwork.util.Util;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitLabUserServiceImpl
 *
 * @blame Android Team
 */
@Service
@RefreshScope
public class GitLabUserServiceImpl implements IGitLabUserService {
    private static final Logger logger = LoggerFactory.getLogger(GitLabUserServiceImpl.class);

    @Autowired
    IGitLabUserDao gitLabUserDao;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestTransport restTransport;


    /**
     * 从Fedv查询所有人员，删除本地的数据，保存新的数据，如果是新增的数据，标识位为0
     * 将公司信息单独保存到公司表中
     * 将角色信息单独保存到角色表中
     */
    @Override
    public List<GitlabUser> selectUserAndSave() {
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.REST_CODE, "queryUser");
        List<Map<String, Object>> appList;
        try {
            appList = (List<Map<String, Object>>) this.restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{"调应用模块接口(/api/user/query)出错：" + e.getMessage()});
        }

        JSONArray jsonArray = new JSONArray();
        appList.forEach(v -> {
            jsonArray.add(new JSONObject(v));
        });
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jObject = jsonArray.getJSONObject(i);
            if (jObject != null) {
                this.setGitlabUser(jObject);//新增
            }
        }
        return gitLabUserDao.select();
    }

    @Override
    public JSONArray selectGroup() {
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.REST_CODE, "queryGroup");
        List<Map<String, Object>> appList;
        try {
            appList = (List<Map<String, Object>>) this.restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{"调用户模块接口(/api/group/query)出错：" + e.getMessage()});
        }

        JSONArray jsonArray = new JSONArray();
        appList.forEach(v -> {
            jsonArray.add(new JSONObject(v));
        });
        return jsonArray;
    }

    @Override
    public JSONArray selectGroupId(Map<String, String> map) {
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.REST_CODE, "queryGroupChild");
        param.putAll(map);
        List<Map<String, Object>> appList;
        try {
            appList = (List<Map<String, Object>>) this.restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{"调应用模块接口(/api/group/queryByGroupId)出错：" + e.getMessage()});
        }

        JSONArray jsonArray = new JSONArray();
        appList.forEach(v -> {
            jsonArray.add(new JSONObject(v));
        });

        return jsonArray;
    }


    /**
     * 从数据库查询用户信息
     */
    @Override
    public List<GitlabUser> select() {
        return gitLabUserDao.select();
    }

    /**
     * 根据用户名查询用户
     */
    @Override
    public GitlabUser selectByGitUser(String gituser) {
        return gitLabUserDao.selectByGitUser(gituser);
    }

    /**
     * 根据GroupId和CompanyId查询用户
     *
     * @param groupId
     * @param companyId
     * @return
     */
    @Override
    public List<GitlabUser> selectByGroupIdAndCompanyId(String groupId, String companyId) {
        return gitLabUserDao.selectByGroupIdAndCompanyId(groupId, companyId);
    }

    @Override
    public List<GitlabUser> selectCompanyIdAndRoleName(String companyId, String roleName, String status) {
        return gitLabUserDao.selectCompanyIdAndRoleName(companyId, roleName, status);
    }

    /**
     * 根据GroupId和CompanyId、RoleName查询用户
     *
     * @param groupId
     * @param companyId
     * @return
     */
    @Override
    public Map<String, Object> selectByGroupIdAndCompanyIdRoleName(String groupId, String companyId, String rolename, String status, String area, int page, int per_page) {
        return gitLabUserDao.selectByGroupIdAndCompanyIdRoleName(groupId, companyId, rolename, status, area, page, per_page);
    }

    @Override
    public List<Map> selectArea() {
        return gitLabUserDao.selectArea();
    }

    /**
     * 根据传入的参数来查询记录
     *
     * @param gitlabUser
     * @return
     */
    @Override
    public List<GitlabUser> selectGitlabUserByParams(GitlabUser gitlabUser) {
        List groupIdList = null;
        if (!Util.isNullOrEmpty(gitlabUser.getGroupid())) {
//            groupIdList = insertGroupIdList(gitlabUser.getGroupid());
        }
        return this.gitLabUserDao.selectGitlabUserByParams(gitlabUser, groupIdList);
    }

    /**
     * 通过groupId来查询是否存在有子组
     *
     * @param groupId
     * @return
     */
//    private List insertGroupIdList(String groupId) {
//        List<String> groupIdList = new ArrayList<>();
//        groupIdList.add(groupId);
//        JSONArray jsonArray = new JSONArray();
//        GitlabGroup gitlabGroup = new GitlabGroup();
//        gitlabGroup.setParentid(groupId);
//        //用groupid作为parentid查询出第一层记录
//        List<GitlabGroup> groups = gitLabGroupService.getGroupInfoByParams(gitlabGroup);
//        if (Util.isNullOrEmpty(groups))
//            return groupIdList;
//        for (GitlabGroup group : groups) {
//            JSONObject job = new JSONObject();
//            job.put(Dict.ID, group.getGroupid());
//            job.put("label", group.getGroupname());
//            job.put(Dict.GROUPID, group.getGroupid());
//            job.put(Dict.GROUPNAME, group.getGroupname());
//            job.put("parentid", group.getParentid());
//            jsonArray.add(job);
//            //groupIdList.add(group.getGroupid());
//        }
//        //递归调用将所有的groupid的子组装入
//        this.gitLabGroupService.getGroupInfo(jsonArray);
//        //递归调用将所有的groupid装入list
//        getGroupIdFromJsonArray(jsonArray, groupIdList);
//        return groupIdList;
//    }


    /**
     * 从jsonArray取出所有的groupid来装入list中
     *
     * @param jsonArray
     * @param groupIdList
     */
    private void getGroupIdFromJsonArray(JSONArray jsonArray, List groupIdList) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            JSONArray children = jsonObject.getJSONArray("children");
            //若没有子组，便直接装入list
            if (Util.isNullOrEmpty(children)) {
                groupIdList.add(jsonObject.getString(Dict.GROUPID));
                continue;
            }
            getGroupIdFromJsonArray(children, groupIdList);
        }
    }


    /**
     * 根据fdev查询的用户结果对gitlabuser字段进行赋值，新增的标识位0，
     * 根据用户表初始化公司表中的数据
     * 根据用户表初始化角色表中的数据
     *
     * @param jObject
     */
    public void setGitlabUser(JSONObject jObject) {
        GitlabUser gitlabUser = new GitlabUser();
        gitlabUser.setUser_id(jObject.getString(Dict.ID).trim());//Id
        gitlabUser.setName(jObject.getString(Dict.USER_NAME_CN).trim());// gitlab中文名
        gitlabUser.setUsername(jObject.getString(Dict.USER_NAME_EN).trim());// gitlab英文名
        gitlabUser.setGituser(jObject.getString(Dict.GIT_USER).trim());// gitlab名
        gitlabUser.setGroupid(jObject.getString(Dict.GROUP_ID).trim());// 组id
        gitlabUser.setSign(0);//gitlab定时任务标识位
        gitlabUser.setStatus(jObject.getString(Dict.STATUS).trim());//用户是否在职，0 在职，1 离职
        JSONObject group = jObject.getJSONObject(Dict.GROUP);
        if (group != null) {
            gitlabUser.setGroupname(group.getString(Dict.NAME).trim());// 组名
        }
        JSONObject company = jObject.getJSONObject("company");
        if (company != null) {
            gitlabUser.setCompanyid(company.getString(Dict.ID).trim());//公司id
            gitlabUser.setCompanyname(company.getString(Dict.NAME).trim());//公司名
            if (company.getString(Dict.NAME).trim().equals("浦发")) {
                try {        //当area为空时，json解析的对象为jsonarray类型，不为空是为jsonobject
                    JSONObject area = jObject.getJSONObject(Dict.AREA);
                    if (area != null) {
                        gitlabUser.setAreaid(area.getString(Dict.ID).trim());// 地域id
                        gitlabUser.setAreaname(area.getString(Dict.NAME).trim());// 地域名称
                    }
                } catch (Exception e) {
                    logger.error("获取area地域信息异常", e.getMessage());
                }
            }
        }
        JSONArray jsonArray = jObject.getJSONArray(Dict.ROLE);
        StringBuilder stringBuilder = new StringBuilder();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                stringBuilder.append(jsonObject.getString(Dict.NAME).trim()).append(";");
            }
        }
        gitlabUser.setRolename(stringBuilder.toString());//角色名
        GitlabUser user = gitLabUserDao.selectByUserId(gitlabUser.getUser_id());
        if (user == null)
            gitLabUserDao.save(gitlabUser);
        else {
            gitlabUser.setConfigname(user.getConfigname());
            gitlabUser.setSign(user.getSign());
            gitLabUserDao.upsert(gitlabUser);
        }
    }
}
