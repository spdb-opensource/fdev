package com.spdb.fdev.fdevapp.spdb.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.Constant;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.base.utils.GitlabTransport;
import com.spdb.fdev.fdevapp.base.utils.validate.ValidateApp;
import com.spdb.fdev.fdevapp.spdb.dao.IAppEntityDao;
import com.spdb.fdev.fdevapp.spdb.dao.IGitlabAPIDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;
import com.spdb.fdev.fdevapp.spdb.service.IAppEntityService;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabAPIService;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabRepositoriesService;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class GitlabAPIServiceImpl implements IGitlabAPIService {

    @Value("${gitlib.path}")
    private String url;// gitlab地址http://xxx/api/v4/
    @Value("${webhook.url}")
    private String webHook_url;// 接收webhook地址 可配置
    @Value("${webhook}")
    private String webHook;// 接收fwebhook模块地址 可配置
    @Value("${appname.regulation.url}")
    private String appname_regulation_url;

    @Autowired
    private RestTransport restTransport;
    @Autowired
    private IAppEntityService appEntityService;
    @Autowired
    private IAppEntityDao appEntityDao;
    @Autowired
    private IGitlabAPIDao gitlabAPIDao;
    @Autowired
    private GitlabTransport gitlabTransport;
    @Autowired
    private IGitlabRepositoriesService gitlabRepositoriesService;
    @Value("${gitlab.token}")
    private String token;


    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<Map> projectsBranch;

    public enum AppTypeEnum {
        JAVA, ANDROID, VUE, IOS;
    }

    /**
     * 通过项目id查询项目信息
     *
     * @param id    gitlab id
     * @param token AccessTokens验证用户
     * @return 项目信息json字符串
     * @throws Exception
     */
    @Override
    public Object queryProjectById(String id, String token) throws Exception {
        String queryProject_url = CommonUtils.projectUrl(url) + "/" + id;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitGet(queryProject_url, header);
    }

    /**
     * 创建项目
     *
     * @param path  项目路径
     * @param name  项目名称
     * @param token AccessTokens验证用户
     * @return 创建的项目信息json字符串
     * @throws Exception
     */
    @Override
    public Object createProject(String path, String name, String token) throws Exception {

        // 获取group地址
        String[] split = path.split("/");
        Map param = new HashMap();

        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        String[] split2 = url.split(Dict.API);// 截取git地址
        String replace = path.replace(split2[0], "");// 获取path full_path
        // 拼装地址
        StringBuffer searchGroup_url = new StringBuffer(url).append("groups").append("?search=")
                .append(split[split.length - 1]).append("&per_page=100").append("&page=");
        JSONArray grouplist = new JSONArray();
        int page = 0;
        do {
            page ++;
            Object projects = gitlabTransport.submitGet(searchGroup_url.append(page).toString(), header);
            grouplist.addAll(JSONObject.parseArray((String) projects));
        } while (grouplist.size()/100 == page);
        Integer namespaceid = null;
        if (CommonUtils.isNullOrEmpty(grouplist)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{" @@@@@ 创建项目路径不存在"});
        }
        for (Object group : grouplist) {
            Map map = (Map) group;
            if (map.get(Dict.FULL_PATH).equals(replace)) {
                namespaceid = (Integer) map.get(Dict.ID);
                param.put(Dict.NAMESPACE_ID, String.valueOf(namespaceid));
                param.put(Dict.NAME, name);
                param.put(Dict.PRIVATE_TOKEN_L, token);
            }
        }
        if (CommonUtils.isNullOrEmpty(namespaceid)) {
            logger.error("can't find this path! @@@@ path=" + path);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"can't find this path"});
        }

        if (gitlabRepositoriesService.isProjectExist(namespaceid, name, token)) {
            logger.error("project has exist! project name=" + name);
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{" @@@@@ project has exist"});
        }

        return gitlabTransport.submitPost(url + Dict.PROJECTS, param);
    }

    /**
     * 查询用户所有项目
     *
     * @param userid 用户id
     * @param token  AccessTokens验证用户
     * @return 用户所有项目信息的json字符串
     * @throws Exception
     */
    @Override
    public Object quertProjectByUser(String userid, String token) throws Exception {
        String userProject_url = url + "users/" + userid + "/projects";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitGet(userProject_url, header);
    }

    /**
     * 查询项目所属成员的个人信息
     *
     * @param id    应用id
     * @param token AccessTokens验证用户
     * @return 所有成员的个人信息json字符串
     * @throws Exception
     */
    @Override
    public Object queryProjectMembers(String id, String token) throws Exception {
        String projectMembers_url = CommonUtils.projectUrl(url) + "/" + id + "/members";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitGet(projectMembers_url, header);

    }

    /**
     * 添加项目webhook
     *
     * @param id           应用id
     * @param token        AccessTokens验证用户
     * @param mergeRequest 设置mergerRequest提醒 值为boolean
     * @param pipeline     设置pipeline提醒 值为boolean
     * @return 添加的webhook信息json字符串
     * @throws Exception
     */
    @Override
    public Object addProjectHook(String id, String token, String mergeRequest, String pipeline) throws Exception {
        String projectHook_url = CommonUtils.projectUrl(url) + "/" + id + "/hooks";
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.URL, webHook_url);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.MERGE_REQUESTS_EVENTS, mergeRequest);
        param.put(Dict.PIPELINE_EVENTS, pipeline);
        return gitlabTransport.submitPost(projectHook_url, param);

    }

    /**
     * 给应用新增和录入已有应用时添加fwebhook模块 webhook
     *
     * @param id
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public Object addProjectHook(String id, String token) throws Exception {
        String projectHook_url = CommonUtils.projectUrl(url) + "/" + id + "/hooks";
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.URL, webHook);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.MERGE_REQUESTS_EVENTS, Dict.TRUE);
        param.put(Dict.PIPELINE_EVENTS, Dict.TRUE);
        param.put(Dict.PUSH_EVENTS, Dict.TRUE);
        param.put(Dict.TAG_PUSH_EVENTS, Dict.TRUE);
        return gitlabTransport.submitPost(projectHook_url, param);

    }

    @Override
    public Boolean validateProjectHook(String id, String token) throws Exception {
        String projectHook_url = CommonUtils.projectUrl(url) + "/" + id + "/hooks";
        //判断是否已经增加了webhook
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        Object object = null;
        List<String> webHooks = null;
        try {
            object = gitlabTransport.submitGet(projectHook_url, header);
        } catch (Exception e) {
            logger.error("获取项目webhook错误", e.getMessage());
        }
        if (object != null) {
            webHooks = this.getWebhooks(object);
        }
        if (webHooks != null && (webHooks.contains(webHook_url) || webHooks.contains(webHook))) {
            return false;
        }
        return true;
    }

    /**
     * 创建项目pipline
     *
     * @param id        应用id
     * @param ref       所属分支 exl：master
     * @param variables 所需参数[{ "key" : "CI_DEPLOY", "value" : "deploy" }]
     * @param token     AccessTokens验证用户
     * @return pipline相关信息
     * @throws Exception
     */
    @Override
    public Object createPipeline(String id, String ref, List<Map<String, String>> variables, String token)
            throws Exception {
        String pipeline_url = CommonUtils.projectUrl(url) + "/" + id + "/pipeline";
        // 拆分 variables参数 将其重新组装为map再加入到list中
        Map<String, Object> param = new HashMap<String, Object>();
        if (!CommonUtils.isNullOrEmpty(variables)) {
            param.put(Dict.VARIABLES, variables);
        }
        param.put(Dict.ID, id);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.REF, ref);
        return gitlabTransport.submitPost(pipeline_url, param);
    }

    /**
     * 接收webhook返回信息
     *
     * @param req 接收webhook请求 获取其中的requestbody内容
     * @return 接收webhook请求 获取其中的requestbody内容
     * @throws Exception
     */
    @Override
    public String webHook(Map<String, Object> req) throws Exception {

        return null;
    }

    /**
     * 创建分支
     *
     * @param id    应用id
     * @param token 用户id
     * @param name  分支名
     * @param ref   父分支
     * @return 分支信息
     * @throws Exception
     */
    @Override
    public Object createBranch(String id, String name, String ref, String token) throws Exception {
        String branch_url = CommonUtils.projectUrl(url) + "/" + id + "/repository/branches";
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.BRANCH, name);
        param.put(Dict.REF, ref);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        Object o = gitlabTransport.submitPost(branch_url, param);
        this.setProtectedBranches(id, name, token);
        return o;
    }

    /**
     * @param id      应用id
     * @param sbranch 将要合的分支名
     * @param tbranch 目标分支
     * @param title   填写信息
     * @param token   AccessToken验证用户
     * @return 合并信息
     * @throws Exception
     */
    @Override
    public Object createMergeRequest(String id, String sbranch, String tbranch, String title, String desc, String token)
            throws Exception {
        String mergerequest_url = CommonUtils.projectUrl(url) + "/" + id + "/merge_requests";
        //检验源分支是否存在
        ValidateBranch(id, sbranch);
        //检验目标分支是否存在
        ValidateBranch(id, tbranch);
        Map<String, String> param = new HashMap<String, String>();
        if (!CommonUtils.isNullOrEmpty(desc)) {
            param.put(Dict.DESCRIPTION, desc);
        }
        param.put(Dict.SOURCE_BRANCH, sbranch);
        param.put(Dict.TARGET_BRANCH, tbranch);
        param.put(Dict.TITLE, title);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        return gitlabTransport.submitPost(mergerequest_url, param);
    }

    /**
     * 查询项目下的前1000个分支
     *
     * @param projectId
     * @return
     */
    public List<Map> getProjectsBranch(Integer projectId) {
        List<Map> repositoryList = new ArrayList<>();
        // GitLab page默认为1，per_page默认为20，最大为100
        for (int i = 1; i <= 10; i++) {
            List<Map> repoList = getProjectsBranchByPage(projectId, i);
            if (CommonUtils.isNullOrEmpty(repoList)) {
                break;
            } else {
                repositoryList.addAll(repoList);
                if (repoList.size() == 100) {
                    continue;
                } else {
                    break;
                }
            }
        }
        return repositoryList;
    }

    /**
     * 获取项目所有分支
     *
     * @param projectId
     * @param page
     * @return
     */
    public List<Map> getProjectsBranchByPage(Integer projectId, Integer page) {
        List<Map> repositoryList = new ArrayList<>();
        String gitLabUrl = CommonUtils.projectUrl(url) + "/" + projectId + "/repository/branches?" + "&page=" + page + "&per_page=100";
        MultiValueMap header = new LinkedMultiValueMap();
        header.add(Dict.PRIVATE_TOKEN, token);
        try {
            String submitGet = (String) gitlabTransport.submitGet(gitLabUrl, header);
            repositoryList = JSONArray.parseArray(submitGet, Map.class);
        } catch (Exception e) {
            logger.info("获取项目{}的分支列表失败：{}", projectId, e.getMessage());
        }
        return repositoryList;
    }

    /**
     * 查询项目下的前100个分支
     *
     * @param id    应用id
     * @param token 验证码
     * @return 所以分支名 list
     * @throws Exception
     */
    @Override
    public Object getProjectBranchList(String id, String token) throws Exception {
        return gitlabAPIDao.getProjectsBranch(id);
    }

    /**
     * 查询单次mergerequest信息
     *
     * @param id    项目id
     * @param iid   mergerequest id
     * @param token 验证码
     * @return 单次mergerequest信息
     * @throws Exception
     */
    @Override
    public Object getMergeRequestInfo(String id, String iid, String token) throws Exception {
        String mergeRequest_url = CommonUtils.projectUrl(url) + "/" + id + "/merge_requests/" + iid;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        //查询merge_request信息
        Object submitGet = gitlabTransport.submitGet(mergeRequest_url, header);
        JSONObject parseObject = JSONObject.parseObject((String) submitGet);
        Map merge_map = parseObject.toJavaObject(Map.class);
        String sha = (String) merge_map.get("merge_commit_sha");
        //使用sha值查询与之对应的piplines信息
        String pipines_url = CommonUtils.projectUrl(url) + "/" + id + "/pipelines?sha=" + sha;
        Object pipeline = gitlabTransport.submitGet(pipines_url, header);
        JSONArray parseArray = JSONObject.parseArray((String) pipeline);
        if (CommonUtils.isNullOrEmpty(parseArray)) {
            return submitGet;
        }
        Map pipines_map = (Map) parseArray.get(0);
        Object object = pipines_map.get(Dict.STATUS);
        merge_map.put(Dict.PIPELINE, object);
        return merge_map;
    }

    /**
     * 通过应用id查询 projectid
     *
     * @param appId 应用id
     * @return projectid
     * @throws Exception
     */
    @Override
    public String getProjectIDByApp(String appId) throws Exception {
        AppEntity appEbntity = appEntityDao.findById(appId);
        if (null == appEbntity) {
            return null;
        }
        Integer app_project_id = appEbntity.getGitlab_project_id();
        return String.valueOf(app_project_id);
    }

    /**
     * 通过Pipelineid查询Pipeline信息
     *
     * @param id    Pipelineid
     * @param token 验证码
     * @return Pipeline信息
     * @throws Exception
     */
    @Override
    public Object queryPipelineByPid(String id, String pid, String token) throws Exception {
        String pipeline_url = CommonUtils.projectUrl(url) + "/" + id + "/pipelines/" + pid;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitGet(pipeline_url, header);
    }

    /**
     * 通过mergaRequestId查询Pipeline信息
     *
     * @param id    mergaRequestId
     * @param token 验证码
     * @return Pipeline信息
     * @throws Exception
     */
    @Override
    public Map<String, Object> queryPipelineByMid(String id, String mid, String token) throws Exception {
        Map<String, Object> mergeRequestInfo = (Map<String, Object>) getMergeRequestInfo(id, mid, token);
        String data = (String) mergeRequestInfo.get(Dict.DATA);
        JSONObject parseObject = JSONObject.parseObject(data);
        Map javaObject = parseObject.toJavaObject(parseObject, Map.class);
        String pipeline = String.valueOf(javaObject.get(Dict.PIPELINE));
        mergeRequestInfo.put(Dict.DATA, pipeline);
        return mergeRequestInfo;
    }

    @Override
    public Object deleteBranch(String id, String branch, String token) throws Exception {
        String branch_url = CommonUtils.projectUrl(url) + "/" + id + "/repository/branches/" + branch;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitDelete(branch_url, header);

    }

    @Override
    public Object createTages(String id, String name, String ref, String token) throws Exception {
        String tags_url = CommonUtils.projectUrl(url) + "/" + id + "/repository/tags";
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.TAG_NAME, name);
        param.put(Dict.REF, ref);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        return gitlabTransport.submitPost(tags_url, param);
    }

    @Override
    public List<Map<String, Object>> getProjectTagsList(String id, String token, String order_by) throws Exception {
        if (CommonUtils.isNullOrEmpty(order_by)) {
            order_by = Dict.UPDATED;// 若无排序参数 则默认按更新时间排序
        }
        String tagsList_url = CommonUtils.projectUrl(url) + "/" + id + "/repository/tags?order_by=" + order_by;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        List<Map<String, Object>> tag = new ArrayList<>();
        Object submitGet = gitlabTransport.submitGet(tagsList_url, header);
        JSONArray taglist = JSONObject.parseArray((String) submitGet);

        for (int i = 0; i < taglist.size(); i++) {
            Map map = (Map) taglist.get(i);
            tag.add(map);
        }
        return tag;
    }

    @Override
    public void sendMergedCallBack(String state, Integer iid, Integer project_id) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Dict.MERGE_REQUEST_ID, iid);
        map.put(Dict.MERGE_STATE, state);
        map.put(Dict.GITLAB_PROJECT_ID, project_id);
        map.put(Dict.REST_CODE, Dict.MERGECALLBACK);
        restTransport.submit(map);
    }

    /**
     * 根据 gitlab ID 获取项目的信息
     *
     * @param id
     * @return
     */
    @Override
    public Map getProjectInfo(String id, String token) throws Exception {
        String projectUrl = CommonUtils.projectUrl(url) + "/" + id;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        Map map = null;
        try {
            Object resultInfo = this.gitlabTransport.submitGet(projectUrl, header);
            map = (Map) JSONObject.parse((String) resultInfo);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        return map;
    }

    /**
     * @param id    gitlab id
     * @param name  受保护分支的名字
     * @param token token
     * @return
     * @throws Exception
     */
    public Object setProtectedBranches(String id, String name, String token) throws Exception {
        if ((Dict.SIT_UP.equals(name) || "UAT".equals(name) || name.startsWith("release-")) && isProtected(id, name, token)) {
            String protected_branchesUrl = CommonUtils.projectUrl(url) + "/" + id + "/protected_branches/";
            Map<String, String> param = new HashMap<>();
            param.put(Dict.ID, id);
            param.put(Dict.NAME, name);
            param.put(Dict.PRIVATE_TOKEN_L, token);
            return this.gitlabTransport.submitPost(protected_branchesUrl, param);
        }
        return null;
    }

    /**
     * 获取 一个组信息
     *
     * @param group        组名字 /ebank/devops /ebank/devops/
     * @param gitlab_token
     * @return
     */
    @Override
    public boolean getGroupInfo(String group, String gitlab_token) throws Exception {
        boolean result = true;
        if (group.endsWith("/")) {
            group = group.substring(0, group.length() - 1);
        }
        if (group.startsWith("/")) {
            //   ebank/devops/testGroup
            group = group.substring(1);
        }
        String[] split = group.split("/");
        Map map = getGroup(split[split.length - 1], group, gitlab_token);
        if (CommonUtils.isNullOrEmpty(map)) {
            result = false;
        }
        return result;
    }

    /**
     * 创建 一个组
     *
     * @param group        组名字 /ebank/devops /ebank/devops/
     * @param gitlab_token
     * @return
     */
    @Override
    public boolean createGroup(String group, String gitlab_token) throws Exception {
        boolean result = true;
        // 创建组
        if (group.endsWith("/")) {
            group = group.substring(0, group.length() - 1);
        }
        if (group.startsWith("/")) {
            //   ebank/devops/testGroup/vs
            group = group.substring(1);
        }
        String[] split = group.split("/");
        // 查询 父group 是否存在
        String parentGroup = split[split.length - 2];
        String fullPathParentGroup = group.substring(0, group.length() - split[split.length - 1].length() - 1);

        Map parentGroupMap = getGroup(parentGroup, fullPathParentGroup, gitlab_token);
        if (CommonUtils.isNullOrEmpty(parentGroupMap)) {
            result = false;
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{parentGroup + " 不存在"});
        }
        Integer parent_id = (Integer) parentGroupMap.get(Dict.ID);
        String url = this.url + "groups";
        Map<String, Object> param = new HashMap<>();
        String createGroup = split[split.length - 1];
        param.put(Dict.PRIVATE_TOKEN_L, gitlab_token);
        param.put(Dict.NAME, createGroup);
        param.put(Dict.PATH, createGroup);
        param.put(Dict.PARENT_ID, parent_id);
        Object object = null;
        try {
            object = this.gitlabTransport.submitPost(url, param);
        } catch (Exception e) {
            logger.error(e.toString());
            logger.error("####### 组的完整信息 [{}] , 要添加 [{}] 组存在", group, createGroup);
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{createGroup + " 已经存在"});
        }
        Map parse = (Map) JSONObject.parse((String) object);
        if (CommonUtils.isNullOrEmpty(parse.get(Dict.ID))) {
            result = false;
        }
        return result;
    }

    /**
     * 查询gitlab 组信息 没有则 返回 null
     *
     * @param group
     * @param gitlab_token
     * @return
     */
    public Map getGroup(String group, String fullPath, String gitlab_token) throws Exception {
        //先查询获取得到pages
        String searchGroup_url = this.url + "groups?search=" + group + "&page=1&per_page=100";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlab_token);
        Object resultHeader = gitlabTransport.submitGetGetHeader(searchGroup_url, header);
        Map headerMap = (Map) resultHeader;
        List pageList = (List) headerMap.get("X-Total-Pages");
        Integer pages = Integer.valueOf((String) pageList.get(0));
        List resultList = new ArrayList();
        //根据pages分页多次查询获取完整的查询结果
        for (int i = 1; i <= pages; i++) {
            searchGroup_url = this.url + "groups?search=" + group + "&page=" + i + "&per_page=100";
            Object submitGet = gitlabTransport.submitGet(searchGroup_url, header);
            JSONArray grouplist = JSONObject.parseArray((String) submitGet);
            List groups = grouplist.toJavaObject(List.class);
            resultList.addAll(groups);
        }
        if (CommonUtils.isNullOrEmpty(resultList)) {
            return null;
        }
        for (Object groupl : resultList) {
            Map map = (Map) groupl;
            if (map.get(Dict.FULL_PATH).equals(fullPath)) {
                return map;
            }
        }
        return null;
    }


    /**
     * 查询gitlab项目pipeine下jobs
     *
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public Object queryJobs(Map param) throws Exception {
        String project_id = (String) param.get(Dict.PROJECT_ID);
        String pipeline_id = (String) param.get(Dict.PIPELINE_ID);
        String gitlab_token = (String) param.get(Dict.GITLAB_TOKEN);
        String url = this.url + "projects/" + project_id + "/pipelines/" + pipeline_id + "/jobs";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlab_token);
        Object submitGet = gitlabTransport.submitGet(url, header);
        JSONArray jsonA = JSONArray.parseArray((String) submitGet);
        ;
        return jsonA;
    }

    /**
     * 查询gitlab项目pipeine下简单数据的jobs，自定义封装
     *
     * @param param
     * @return
     * @throws Exception
     */
    public Object querySimpleJobs(Map param) throws Exception {
        JSONArray jsonA = (JSONArray) this.queryJobs(param);
        List<Map> resultList = new ArrayList<Map>();
        Map commit = new HashMap();
        for (Object job : jsonA) {
            Map jsonObject = (Map) job;
            resultList.add(new LinkedHashMap() {{
                put(Dict.STAGE, jsonObject.get(Dict.STAGE));
                put(Dict.NAME, jsonObject.get(Dict.NAME));
                put(Dict.STATUS, jsonObject.get(Dict.STATUS));
                put(Dict.ID, jsonObject.get(Dict.ID));
                put(Dict.DURATION, jsonObject.get(Dict.DURATION));
                put(Dict.FINISHED_AT, jsonObject.get(Dict.FINISHED_AT));
            }});
            if (CommonUtils.isNullOrEmpty(commit)) {
                commit = (Map) jsonObject.get(Dict.COMMIT);
            }
        }
        Map result = new HashMap();
        result.put(Dict.COMMIT, commit);
        //排序
        resultList.sort(Comparator.comparingInt(o->Integer.parseInt(o.get(Dict.ID).toString())));
        result.put(Dict.JOBS, resultList);
        return result;
    }

    /**
     * 查询gitlab按项目pipeine，自定义封装
     *
     * @param param
     * @return
     * @throws Exception
     */
    public Object querySinglePipeline(Map param) throws Exception {
        String project_id = (String) param.get(Dict.PROJECT_ID);
        String pipeline_id = (String) param.get(Dict.PIPELINE_ID);
        String gitlab_token = (String) param.get(Dict.GITLAB_TOKEN);
        String url = this.url + "projects/" + project_id + "/pipelines/" + pipeline_id;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlab_token);
        Object submitGet = gitlabTransport.submitGet(url, header);
        JSONObject jsonObject = JSONObject.parseObject((String) submitGet);
        return new JSONObject() {{
            put(Dict.DURATION, jsonObject.get(Dict.DURATION));
            put(Dict.REF, jsonObject.get(Dict.REF));
            put(Dict.ID, jsonObject.get(Dict.ID));
            put(Dict.USER, jsonObject.get(Dict.USER));
            put(Dict.STATUS, jsonObject.get(Dict.STATUS));
            put(Dict.SHA, jsonObject.get(Dict.SHA));
            put(Dict.FINISHED_AT, jsonObject.get(Dict.FINISHED_AT));
            put(Dict.WEB_URL, jsonObject.get(Dict.WEB_URL));
        }};
    }

    /**
     * 查询gitlab按项目pipeine
     *
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public Object queryPipelines(Map param) throws Exception {
        JSONArray jsonA = new JSONArray();
        try {
            String project_id = (String) param.get(Dict.PROJECT_ID);
            String gitlab_token = (String) param.get(Dict.GITLAB_TOKEN);
            String url = this.url + "projects/" + project_id + "/pipelines";
            MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
            header.add(Dict.PRIVATE_TOKEN, gitlab_token);
            Object submitGet = gitlabTransport.submitGet(url, header);
            jsonA = JSONArray.parseArray((String) submitGet);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"gitlab 查询异常", e.toString()});
        }
        return jsonA;
    }

    /**
     * 查询gitlab项目pipeine并组装对应jobs
     *
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public Object queryPipelinesWithJobs(Map param) throws Exception {
        List<Map> newResult = new ArrayList();
        try {
            String project_id = (String) param.get(Dict.PROJECT_ID);
            String gitlab_token = (String) param.get(Dict.GITLAB_TOKEN);
            String pages = (String) param.get("pages");
            String ref = (String) param.get(Dict.REF);
            String url = this.url + "projects/" + project_id + "/pipelines";
            if (!CommonUtils.isNullOrEmpty(ref))
                url = url + "?ref=" + ref;

            MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
            header.add(Dict.PRIVATE_TOKEN, gitlab_token);
            Object submitGet = gitlabTransport.submitGet(url, header);
            JSONArray jsonA = JSONArray.parseArray((String) submitGet);
            int num = 0;
            if (CommonUtils.isNullOrEmpty(pages)) {
                num = jsonA.size();
            } else {
                num = Math.min(Integer.parseInt(pages), jsonA.size());
            }
            for (int i = 0; i < num; i++) {
                Map pipeline = (Map) jsonA.get(i);
                Map tempParam = new HashMap() {
                    {
                        put(Dict.PROJECT_ID, String.valueOf(project_id));
                        put(Dict.GITLAB_TOKEN, gitlab_token);
                        put(Dict.PIPELINE_ID, String.valueOf(pipeline.get(Dict.ID)));
                    }
                };
                Map jobResult = (Map) this.querySimpleJobs(tempParam);
                Object pipelineTemp = this.querySinglePipeline(tempParam);
                Map pipelineMap = new ObjectMapper().convertValue(pipelineTemp, Map.class);
                pipelineMap.put(Dict.JOBS, jobResult.get(Dict.JOBS));
                pipelineMap.put(Dict.COMMIT, jobResult.get(Dict.COMMIT));
                newResult.add(pipelineMap);
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"gitlab 查询异常", e.toString()});
        }
        return newResult;
    }

    /**
     * 查询gitlab项目pipeine并组装对应jobs
     *
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public Object queryPipelinesWithJobsPage(Map param) throws Exception {
        List<Map> newResult = new ArrayList();
        Map data = new HashMap();
        try {
            String project_id = (String) param.get(Dict.PROJECT_ID);
            String gitlab_token = (String) param.get(Dict.GITLAB_TOKEN);
            Integer pageNum = (Integer) param.get("pageNum");
            Integer pageSize = (Integer) param.get("pageSize");
            String ref = (String) param.get(Dict.REF);
            String url = this.url + "projects/" + project_id + "/pipelines";
            if (!CommonUtils.isNullOrEmpty(ref))
                url = url + "?ref=" + ref;

            MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
            header.add(Dict.PRIVATE_TOKEN, gitlab_token);
            Object submitGet = gitlabTransport.submitGet(url, header);
            JSONArray jsonA = JSONArray.parseArray((String) submitGet);
            List<Object> pipelines = new ArrayList<>();
            //均不为空  且长度大于分页长度 分页
            if( !CommonUtils.isNullOrEmpty(jsonA) && !CommonUtils.isNullOrEmpty(pageNum) &&
                    !CommonUtils.isNullOrEmpty(pageSize) && jsonA.size() > pageSize ){
                int start = Math.min((pageNum - 1) * pageSize, jsonA.size());
                int end = Math.min(start + pageSize, jsonA.size());
                pipelines = jsonA.subList(start,end);
            }
            for (int i = 0; i < pipelines.size(); i++) {
                Map pipeline = (Map) pipelines.get(i);
                Map tempParam = new HashMap() {
                    {
                        put(Dict.PROJECT_ID, String.valueOf(project_id));
                        put(Dict.GITLAB_TOKEN, gitlab_token);
                        put(Dict.PIPELINE_ID, String.valueOf(pipeline.get(Dict.ID)));
                    }
                };
                Map jobResult = (Map) this.querySimpleJobs(tempParam);
                Object pipelineTemp = this.querySinglePipeline(tempParam);
                Map pipelineMap = new ObjectMapper().convertValue(pipelineTemp, Map.class);
                pipelineMap.put(Dict.JOBS, jobResult.get(Dict.JOBS));
                pipelineMap.put(Dict.COMMIT, jobResult.get(Dict.COMMIT));
                newResult.add(pipelineMap);
            }

            data.put(Dict.DATA,newResult);
            data.put(Dict.COUNT,jsonA.size());
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"gitlab 查询异常", e.toString()});
        }

        return data;
    }

    /**
     * 查询gitlab下job的json日志
     *
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public Object queryTraces(Map param) throws Exception {
        Object submitGet;
        String status;
        try {
            String project_id = (String) param.get(Dict.PROJECT_ID);
            String job_id = (String) param.get(Dict.JOB_ID);
            String gitlab_token = (String) param.get(Dict.GITLAB_TOKEN);
            String url = this.url + "projects/" + project_id + "/jobs/" + job_id + "/trace";
            MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
            header.add(Dict.PRIVATE_TOKEN, gitlab_token);
            submitGet = gitlabTransport.submitGet(url, header);
            String job_url = this.url + "projects/" + project_id + "/jobs/" + job_id;
            Object job_submitGet = gitlabTransport.submitGet(job_url, header);
            JSONObject jsonObject = JSONObject.parseObject((String) job_submitGet);
            status = (String) jsonObject.get(Dict.STATUS);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"gitlab 查询异常", e.toString()});
        }

        return new HashMap() {{
            put(Dict.TRACE, submitGet);
            put(Dict.STATUS, status);
        }};
    }

    /**
     * 判断当前要创建的分支是否 已经是受保护的
     * 是 返回 false 不是返回 true
     *
     * @param id
     * @param name
     * @param gitlab_token
     * @return
     * @throws Exception
     */
    private Boolean isProtected(String id, String name, String gitlab_token) throws Exception {
        String projectUrl = CommonUtils.projectUrl(url) + "/" + id + "/repository/branches/" + name;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, gitlab_token);
        Object result = this.gitlabTransport.submitGet(projectUrl, header);
        Map parse = (Map) JSONObject.parse((String) result);
        if (parse.size() > 2 && (!(Boolean) parse.get("protected"))) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> getAllBranch(String id, String token, HashSet<String> onlySet) throws Exception {
        ArrayList<String> list = new ArrayList<>();
        String order_by = null;
        Object branchlist = this.getProjectBranchList(id, token);
        List<Map<String, Object>> tagsList = this.getProjectTagsList(id, token, order_by);
        ArrayList branchList = (ArrayList) branchlist;
        branchList.addAll(tagsList);
        if (CommonUtils.isNullOrEmpty(branchList)) {
            return null;
        }
        for (Object object : branchList) {
            Map branchMap = (Map) object;
            Set set = branchMap.keySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() == Dict.NAME) {
                    String branch = (String) branchMap.get(Dict.NAME);
                    if (!CommonUtils.isNullOrEmpty(onlySet) && onlySet.size() > 0) {
                        for (String str : onlySet) {
                            Matcher matcher = Pattern.compile(str.replaceAll("/", "").trim()).matcher(branch);
                            if (matcher.matches()) {
                                list.add(branch);
                            }
                        }
                    } else {
                        list.add(branch);
                    }
                }
            }
        }
        return list;
    }

    @Override
    public Object deleteProjectById(String id) throws Exception {
        String queryProject_url = CommonUtils.projectUrl(url) + "/" + id;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitDelete(queryProject_url, header);
    }

    @Override
    public String getFileContent(String type_name, String id, String isInternetSystem) throws Exception {
        Map projectInfo = this.getProjectInfo(id, token);
        String name_en = null;
        //判断是否进行应用名校验
        boolean isValid = true;
        //是否是互联网条线，标识是否要校验三段式
        Boolean isInternetValid = true;
        isInternetValid = isInternetSystem.equals("1") ? true : false;
        //判断为Java微服务应用
        if (type_name.toUpperCase().contains(AppTypeEnum.JAVA.toString())) {
            String name = (String) projectInfo.get(Dict.NAME);
            String propertiesPath = "src/main/resources/application.properties";
            String xmlPath = "pom.xml";
            String[] names = name.split("-");
            if (name.endsWith("-" + Dict.PARENT) && names.length == 4) {
                xmlPath = name.split("-" + Dict.PARENT)[0] + "/pom.xml";
                propertiesPath = name.split("-" + Dict.PARENT)[0] + "/src/main/resources/application.properties";
            }
            String xmlContent = getContent(id, xmlPath, Dict.MASTER);
            String xmlName = CommonUtils.getPomNode(xmlContent);
            String propertiesContent = getContent(id, propertiesPath, Dict.MASTER);
            String propertiesName = CommonUtils.getPropertiesNode(propertiesContent);
            //校验应用英文名
            if (!CommonUtils.isNullOrEmpty(propertiesName) && propertiesName.equals(name.split("-" + Dict.PARENT)[0])) {
                name_en = propertiesName;
            } else if (!CommonUtils.isNullOrEmpty(xmlName) && xmlName.equals(name.split("-" + Dict.PARENT)[0])) {
                name_en = xmlName;
            } else {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"应用英文名",
                        "application.properties中spring.application.name的值或pom.xml的子标签artifactId的值与gitlab项目名不一致"});
            }
        }
        //判断为容器化应用
        if (type_name.equals(Constant.CONTAINER_PROJECT) || type_name.equals(Constant.OLD_VERSION_SERVICE)) {
            name_en = (String) projectInfo.get(Dict.NAME);
            isValid = false;
        }
        //判断为Vue应用
        if (type_name.toUpperCase().contains(AppTypeEnum.VUE.toString())) {
            name_en = (String) projectInfo.get(Dict.NAME);
        }
        //判断为IOS应用
        if (type_name.toUpperCase().contains(AppTypeEnum.IOS.toString())) {
            name_en = (String) projectInfo.get(Dict.NAME);
            isValid = false;
        }
        //判断为Android应用
        if (type_name.toUpperCase().contains(AppTypeEnum.ANDROID.toString())) {
            name_en = (String) projectInfo.get(Dict.NAME);
            isValid = false;
        }
        //是JAVA微服务应用或者vue应用 以及是互联网条线 需要校验三段式
        if (isValid && isInternetValid && !ValidateApp.validateAppEnName(name_en)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"APP NAME", "应用英文名不符合规范，详情请见" + appname_regulation_url});
        }
        //是JAVA微服务应用或者vue应用 不是互联网条线，不需要校验三段式
        if (isValid && !isInternetValid && !ValidateApp.validateAppEnNameNoThreeStage(name_en)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"APP NAME", "应用英文名不符合规范，详情请见" + appname_regulation_url});
        }
        //应用名第一段必须是维护的已有系统
        if (isValid && isInternetValid) {
            List<String> appServiceSystem = appEntityService.getAppServiceSystem(name_en);
            if (CommonUtils.isNullOrEmpty(appServiceSystem)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"APP NAME", "应用所属系统错误,详情请见" + appname_regulation_url});
            }
        }
        return name_en;
    }

    @Override
    public String getContent(String id, String file_path, String branchName) throws Exception {
        String path = CommonUtils.fileUtil(file_path);
        String getFileContent_url = CommonUtils.projectUrl(url) + "/" + id + "/repository/files/" + path + "?ref=" + branchName;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getFileContent_url);
        URI uri = builder.build(true).toUri();
        header.add(Dict.PRIVATE_TOKEN, token);
        String result = null;
        try {
            result = (String) this.gitlabTransport.submitGet(uri, header);
        } catch (Exception e) {
            logger.error("文件不存在 :" + file_path);
            return null;
        }
        Object parse = JSONObject.parse(result);
        Map fileContent = (Map) parse;
        String content = new String(Base64.decode((String) fileContent.get("content")), "UTF-8");
        return content;
    }

    /**
     * 获取channel的commit相关信息，通过gitlab查询，查询最新一次commit记录
     *
     * @param gitlabId gitlab上的projectId
     * @param refName  分支名
     * @return
     */
    @Override
    public Map getChannelCommitInfo(String gitlabId, String refName) {
        if (StringUtils.isEmpty(refName))
            refName = "master";
        //http://xxx/api/v4/projects/3435/repository/commits/?ref_name=master&per_page=1&page=1
        String commitUrl = CommonUtils.projectUrl(url) + "/" + gitlabId + "/repository/commits/?ref_name=" + refName + "&per_page=1&page=1";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        String result = null;
        try {
            result = (String) this.gitlabTransport.submitGet(commitUrl, header);
        } catch (Exception e) {
            logger.error("发送" + commitUrl + "请求获取commit相关信息失败");
            return null;
        }
        List parse = (List) JSONObject.parse(result);
        Map resultMap = (Map) parse.get(0);
        Map map = new HashMap();
        map.put(Dict.ID, resultMap.get(Dict.ID));
        map.put(Dict.SHORT_ID, resultMap.get(Dict.SHORT_ID));
        map.put(Dict.TITLE, resultMap.get(Dict.TITLE));
        map.put(Dict.AUTHOR_NAME, resultMap.get(Dict.AUTHOR_NAME));
        map.put(Dict.WEB_URL, resultMap.get(Dict.WEB_URL));
        return map;
    }

    /**
     * 获取project的namespace,和name_with_namespace
     *
     * @param gitlabId
     * @return
     */
    @Override
    public Map getChannelNamespace(String gitlabId) {
        String projectUrl = CommonUtils.projectUrl(url) + "/" + gitlabId;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        String result = null;
        try {
            result = (String) this.gitlabTransport.submitGet(projectUrl, header);
        } catch (Exception e) {
            logger.error("发送" + projectUrl + "请求获取namespace相关信息失败");
            return null;
        }
        Map parse = (Map) JSONObject.parse(result);
        Map namespace = (Map) parse.get(Dict.NAMESPACE);
        String name_with_namespace = (String) parse.get(Dict.PATH_WITH_NAMESPACE);
        //去空格
        //name_with_namespace = name_with_namespace.trim();
        String fullPath = (String) namespace.get(Dict.FULL_PATH);
        Map namespaceMap = new HashMap();
        namespaceMap.put(Dict.NAMESPACE, fullPath);
        namespaceMap.put(Dict.NAME_WITH_NAMESPACE, name_with_namespace);
        return namespaceMap;
    }

    /**
     * 根据gitlabId来验证项目是不是vue项目
     *
     * @param projectId
     * @return
     * @throws Exception
     */
    public String validateVueapp(String projectId) throws Exception {

        String status = null;
        String gitlaburl = CommonUtils.projectUrl(url) + "/" + projectId + "/repository/files/package.json?ref=master";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(gitlaburl);
        URI uri = builder.build(true).toUri();
        header.add(Dict.PRIVATE_TOKEN, token);
        String result = null;
        try {
            result = (String) this.gitlabTransport.submitGet(uri, header);
            status = Dict.REQUEST_SUCCESS;
        } catch (Exception e) {
            status = Dict.NOT_FOUND;
        }
        return status;
    }

    /**
     * 根据gitlab_id 检查分支是否存在
     *
     * @param id
     * @param branch
     * @throws Exception
     */
    public void ValidateBranch(String id, String branch) throws Exception {
        String branchUrl = URLEncoder.encode(branch, "UTF-8");
        String mergerequest_url = CommonUtils.projectUrl(url) + "/" + id + "/repository/branches/" + branchUrl;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(mergerequest_url);
        URI uri = builder.build(true).toUri();
        header.add(Dict.PRIVATE_TOKEN, token);
        try {
            this.gitlabTransport.submitGet(uri, header);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.BRANCH_NOT_EXIST, new String[]{branch});
        }
    }

    //获取gitlab的webhook列表
    private List<String> getWebhooks(Object object) {
        List result = new ArrayList();
        net.sf.json.JSONArray jsonArray = net.sf.json.JSONArray.fromObject(object);
        if (jsonArray != null && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                net.sf.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
                result.add(jsonObject.getString(Dict.URL));
            }
        }
        return result;
    }


    /**
     * 查询获取全量的git组信息(public)
     *
     * @return
     */
    @Override
    public List<Map> getFullGroupGit() {
        List<Map> groupInfos = new ArrayList<>();
        //获取全量的页数
        Integer privateTotalPage = getXTotalPage(true);
        //根据分页结果，查询全量的组信息
        for (Integer i = 1; i <= privateTotalPage; i++) {
            String url = this.url + "groups?all_available=true&page=" + i + "&per_page=100";
            MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
            header.add(Dict.PRIVATE_TOKEN, token);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            URI uri = builder.build(true).toUri();
            Object result;
            try {
                result = this.gitlabTransport.submitGet(uri, header);
            } catch (Exception e) {
                logger.error("查询gitlab private组信息失败!");
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"查询gitlab private组信息失败！"});
            }
            JSONArray resultJson = JSONObject.parseArray((String) result);
            List<Map> resultInfos = resultJson.toJavaList(Map.class);
            //添加到总量中
            groupInfos.addAll(resultInfos);
        }

        //过滤掉不是一级组的组信息
        groupInfos = filterGroupInfo(groupInfos);
        return groupInfos;
    }

    /**
     * 将不是一级组的过滤掉
     *
     * @param groupInfos
     */
    private List<Map> filterGroupInfo(List<Map> groupInfos) {
        if (CommonUtils.isNullOrEmpty(groupInfos))
            return null;
        groupInfos = groupInfos.stream().filter(map -> !(((String) map.get("full_path")).contains("/"))).collect(Collectors.toList());
        return groupInfos;
    }


    /**
     * 获取查询组信息返回结果（分页）的页数
     *
     * @param isPrivate 是否查询私有/共有
     * @return
     */
    private Integer getXTotalPage(Boolean isPrivate) {
        String url = this.url + "groups?all_available=true&page=1&per_page=100";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        if (isPrivate)
            header.add(Dict.PRIVATE_TOKEN, token);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = builder.build(true).toUri();
        Object resultHeader;
        try {
            resultHeader = this.gitlabTransport.submitGetGetHeader(uri, header);
        } catch (Exception e) {
            logger.error("查询gitlab全量组信息header失败!");
            throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"查询gitlab全量组信息header失败！"});
        }
        Map headerMap = (Map) resultHeader;
        List resultList = (List) headerMap.get("X-Total-Pages");
        return Integer.valueOf((String) resultList.get(0));
    }
}