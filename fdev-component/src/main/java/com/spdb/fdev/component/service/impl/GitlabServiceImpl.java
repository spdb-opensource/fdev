
package com.spdb.fdev.component.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.Constant;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.GitUtils;
import com.spdb.fdev.base.utils.ShellUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.*;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.*;
import com.spdb.fdev.component.transport.GitlabTransport;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Service
@RefreshScope
public class GitlabServiceImpl implements IGitlabSerice {

    private static Logger logger = LoggerFactory.getLogger(GitlabServiceImpl.class);

    private static final String GROUP_SEARCH = "groups?search=";

    @Value("${gitlab.api.url}")
    private String gitlabApiUrl;

    @Value("${gitlab.rootUrl}")
    private String gitlabRootUrl;

    @Value("${gitlab.manager.token}")
    private String token;

    @Value("${webhook.url}")
    private String webHook_url;// 组件接收webhook地址 可配置

    @Value("${gitlab.fileDir}")
    private String local_temp_repo;

    @Value("${gitlib.fdev-ci-template.branch}")
    private String branch;

    @Value("${gitlib.fdev-ci-template.name}")
    private String fdevCiTemplateName;

    @Value("${gitlab.manager.username}")
    private String name;

    @Value("${gitlab.manager.password}")
    private String password;

    @Value("${gitlib.fdev-ci-template.id}")
    private String templateId;

    @Value("${notify.email.send.flag}")
    private boolean email_send_flag;

    private static String FILECONTEXT = "/repository/files/";

    @Autowired
    private IComponentRecordDao componentRecordDao;

    @Autowired
    private IComponentIssueDao componentIssueDao;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IComponentApplicationDao componentApplicationDao;

    @Autowired
    private IMailService mailService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private GitlabTransport gitlabTransport;

    @Autowired
    private IAppService appService;

    @Autowired
    private IBaseImageInfoDao baseImageInfoDao;


    @Autowired
    private IComponentInfoService componentInfoService;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private IGitlabUserService gitlabUserService;

    @Autowired
    private IArchetypeInfoService archetypeInfoService;

    @Autowired
    private IArchetypeRecordService archetypeRecordService;

    @Autowired
    private IComponentArchetypeService componentArchetypeService;

    @Autowired
    private IComponentInfoDao componentInfoDao;

    @Autowired
    private IArchetypeInfoDao archetypeInfoDao;

    @Autowired
    private IMpassComponentDao mpassComponentDao;

    @Autowired
    private IMpassArchetypeDao iMpassArchetypeDao;


    public String projectUrl() {
        return gitlabApiUrl + Dict.PROJECTS;
    }


    @Override
    public List getProjectTagsList(String id, String token) throws Exception {
        String order_by = Dict.UPDATED;
        String tagsList_url = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id + "/repository/tags?order_by=" + order_by;
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

    public Map queryGroupInfo(String groupFullPath) throws FdevException {
        boolean isExist = false;
        String newGroupPath = groupFullPath.trim();
        // 获取group地址
        String[] split = newGroupPath.split("/");
        // 拼装地址
        String searchGroup_url = gitlabApiUrl + GROUP_SEARCH + split[split.length - 1] + "&per_page=100&page=1";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        Map groupInfo = new HashMap();
        try {
            String group = (String) gitlabTransport.submitGet(searchGroup_url, header);
            JSONArray jsonarray = JSON.parseArray(group);
            for (Object object : jsonarray) {
                Map map = (Map) object;
                //FULL_PATH不包括http://xxx/
                if (newGroupPath.equals(map.get(Dict.FULL_PATH))) {
                    isExist = true;
                    groupInfo = map;
                    logger.info("gitlab组地址存在!");
                    break;
                } else {
                    isExist = false;
                    continue;
                }
            }

        } catch (HttpClientErrorException e) {
            logger.error(Dict.STATUS, e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error(e.toString());
        }
        if (!isExist) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"组信息不存在"});
        }
        return groupInfo;
    }

    /**
     * 根据组id和项目名称判断项目是否存在
     *
     * @param namespaceId 组id
     * @param name        项目名称
     * @return
     * @throws Exception
     */
    public boolean isProjectExist(Integer namespaceId, String name) throws Exception {
        JSONArray project_list = new JSONArray();
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        StringBuilder searce_pro_url = new StringBuilder();
        searce_pro_url.append(projectUrl()).append("?search=").append(name);// 获取所以该名称的项目信息
        int page = 1;
        do {
            searce_pro_url.append("&per_page=100").append("&page=").append(page);
            Object projects = gitlabTransport.submitGet(searce_pro_url.toString(), header);
            project_list.addAll(JSONObject.parseArray((String) projects));
            page++;
        } while ((project_list.size() / 100 == page));
        if (CommonUtils.isNullOrEmpty(project_list)) {
            return false;
        }
        boolean flag = false;
        for (Object project : project_list) {
            Map map = (Map) project;
            Map namespace = (Map) map.get(Dict.NAMESPACE);
            String project_name = (String) map.get(Dict.NAME);
            Integer id = (Integer) namespace.get(Dict.ID);
            if (namespaceId.equals(id) && name.equalsIgnoreCase(project_name)) {// 判断groupid是否相同
                // 判断name是否相同
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 根据项目名查询项目id
     *
     * @param namespaceId
     * @param name
     * @return
     * @throws Exception
     */
    public String returnProjectId(Integer namespaceId, String name) throws Exception {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        StringBuilder searce_pro_url = new StringBuilder(projectUrl()); // 获取所有该名称的项目信息
        int page = 1;
        searce_pro_url.append("?search=").append(name).append("&per_page=100").append("&page=").append(page);
        JSONArray project_list = new JSONArray();
        do {
            Object projects = gitlabTransport.submitGet(searce_pro_url.toString(), header);
            project_list.addAll(JSONObject.parseArray((String) projects));
            page++;
        } while (project_list.size() / 100 == page);
        if (CommonUtils.isNullOrEmpty(project_list)) {// 若未查询到 则说明不存在
            throw new FdevException(ErrorConstants.RESOURCE_IS_NOT_EXIST);
        }
        for (Object project : project_list) {
            Map map = (Map) project;
            Map namespace = (Map) map.get(Dict.NAMESPACE);
            String project_name = (String) map.get(Dict.NAME);
            Integer id = (Integer) namespace.get(Dict.ID);
            if (namespaceId.equals(id) && name.equalsIgnoreCase(project_name)) {// 判断groupid是否相同
                // 判断name是否相同
                return ((Integer) map.get(Dict.ID)).toString();
            }
        }
        return null;
    }

    public String queryProjectIdByUrl(String project_url) throws Exception {
        String tmp = project_url.replace(gitlabRootUrl, "");
        if (tmp.endsWith("/")) {
            tmp = tmp.substring(0, tmp.length() - 1);
        }
        String projectName = tmp.substring(tmp.lastIndexOf('/') + 1);
        int i = tmp.lastIndexOf('/');
        if (i == -1) {
            throw new FdevException(ErrorConstants.URLERROR, new String[]{"gitlaburl地址有误"});
        }
        String path = tmp.substring(0, tmp.lastIndexOf('/'));

        Map map1 = queryGroupInfo(path);
        Integer namespaceid = (Integer) map1.get(Dict.ID);
        if (CommonUtils.isNullOrEmpty(namespaceid)) {
            logger.error("can't find this path! @@@@ path={}", path);
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"can't find this path"});
        }
        boolean projectExist = isProjectExist(namespaceid, projectName);
        if (!projectExist) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"项目不存在"});
        }
        return returnProjectId(namespaceid, projectName);
    }


    public List queryBranches(String projectId) throws Exception {
        ArrayList result = new ArrayList();
        List<Map<String, Object>> list = this.findBrancheListById(Integer.parseInt(projectId));
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                result.add(list.get(i).get(Dict.NAME));
            }
        }
        return result;
    }


    @Override
    public Object createTags(String id, String name, String ref, String token) throws Exception {
        String tags_url = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id + "/repository/tags";
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.TAG_NAME, name);
        param.put(Dict.REF, ref);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        return gitlabTransport.submitPost(tags_url, param);
    }

    @Override
    public Object createPipeline(String id, String ref, List<Map<String, String>> variables, String token)
            throws Exception {
        String pipeline_url = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id + "/pipeline";
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

    @Override
    public Object createBranch(String id, String name, String ref, String token) throws Exception {
        String branch_url = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id + "/repository/branches";
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.BRANCH, name);
        param.put(Dict.REF, ref);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        Object o = gitlabTransport.submitPost(branch_url, param);
        return o;
    }

    @Override
    public Object deleteBranch(String id, String branch, String token) throws Exception {
        String branch_url = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id + "/repository/branches/" + branch;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitDelete(branch_url, header);

    }


    /**
     * 检验分支是否存在
     *
     * @param token
     * @param gitlabId
     * @param branch
     */
    @Override
    public void checkBranch(String token, String gitlabId, String branch) throws Exception {
        String url = projectUrl() + "/" + gitlabId + "/repository/branches/" + branch;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        // 如果没有查到当前分支,此处会抛错
        this.gitlabTransport.submitGet(url, header);
    }

    /**
     * 检验分支是否存在
     *
     * @param token
     * @param gitlabId
     * @param tag
     */
    @Override
    public void checkTag(String token, String gitlabId, String tag) throws Exception {
        String url = projectUrl() + "/" + gitlabId + "/repository/tags/" + tag;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        // 如果没有查到当前分支,此处会抛错
        this.gitlabTransport.submitGet(url, header);
    }


    /**
     * 检查分支和tag
     *
     * @param token
     * @param gitlabId
     * @param type        Dict.BRANCH  Constants.TAG
     * @param branchOrTag
     */
    @Override
    public boolean checkBranchOrTag(String token, String gitlabId, String type, String branchOrTag) {
        Boolean flag = true;
        try {
            if (Dict.BRANCH.equals(type)) {
                this.checkBranch(token, gitlabId, branchOrTag);
            } else {
                this.checkTag(token, gitlabId, branchOrTag);
            }
            logger.info("查询{}成功，项目id为:{}，分支为:{}", type, gitlabId, branchOrTag);
        } catch (Exception e) {
            logger.info("查询{}失败，项目id为:{}，分支为:{}", type, gitlabId, branchOrTag);
            flag = false;
        }
        return flag;
    }


    @Override
    public Object createMergeRequest(String id, String sbranch, String tbranch, String title, String desc, String token)
            throws Exception {
        String mergerequest_url = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id + "/merge_requests";
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

    @Override
    public Object addProjectHook(String id, String token, String mergeRequest, String pipeline, String type) throws Exception {
        String hookUrl = null;
        String projectHook_url = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id + "/hooks";
        //判断是否已经增加了webhook
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN_U, token);
        Object object = null;
        List<String> webHooks = null;
        try {
            object = gitlabTransport.submitGet(projectHook_url, header);
        } catch (Exception e) {
            logger.error("获取项目webhook错误,{}", e.getMessage());
        }
        String webhookToken = type;
        if (object != null) {
            webHooks = this.getWebhooks(object);
        }
        hookUrl = webHook_url;
        if (webHooks == null || !webHooks.contains(hookUrl)) {
            Map<String, String> param = new HashMap<>();
            param.put(Dict.URL, hookUrl);
            param.put(Dict.PRIVATE_TOKEN_L, token);
            param.put(Dict.WEBHOOK_TOKEN, webhookToken);
            param.put(Dict.MERGE_REQUESTS_EVENTS, mergeRequest);
            param.put(Dict.PIPELINE_EVENTS, pipeline);
            return gitlabTransport.submitPost(projectHook_url, param);
        }
        return null;

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
     * pipeline结束后更新组件历史版本
     */
    @Override
    public void updateComponentAfterPip(String version, String pipid) throws Exception {
        ComponentRecord record = componentRecordDao.queryByPipId(pipid);
        if (record != null) {
            record.setDate(Util.simdateFormat(new Date()));
            record.setPackagetype(Constants.PIPEPACKAGE);
            String target_version = version.split("-")[0];
            if (version.contains(Constants.RELEASE)) {
                ComponentIssue issue = componentIssueDao.queryByComponentIdAndVersion(record.getComponent_id(), target_version);
                issue.setStage(Constants.STAGE_PASSED);
                componentIssueDao.update(issue);
                //将当前版本设置为推荐版本，并且将之前的推荐版本设置为正式版本
                ComponentRecord componentRecord = componentRecordDao.queryByType(record.getComponent_id(), Constants.RECORD_RECOMMEND_TYPE);
                if (componentRecord != null) {
                    componentRecord.setType(Constants.RECORD_OFFICIAL_TYPE);
                    componentRecordDao.update(componentRecord);
                }
                record.setType(Constants.RECORD_RECOMMEND_TYPE);
            }
            componentRecordDao.upsert(record);
            //判断版本是不是-RELEASE,是的话邮件发送
            if (record.getVersion().contains("-RELEASE") && email_send_flag) {
                logger.info("RELEASE版本发布，发送更新邮件");
                String component_id = record.getComponent_id();
                ComponentApplication componentApplication = new ComponentApplication();
                componentApplication.setComponent_id(component_id);
                List<ComponentApplication> componentApplications = componentApplicationDao.query(componentApplication);
                Set<String> users = new HashSet<>();
                if (!CommonUtils.isNullOrEmpty(componentApplications)) {
                    for (ComponentApplication comp : componentApplications) {
                        try {
                            Map app = appService.queryAPPbyid(comp.getApplication_id());
                            //获取应用的负责人
                            // 行内负责人
                            List<Map> spdb_managers = (List<Map>) app.get(Dict.SPDB_MANGERS);
                            users.addAll(this.getManageIds(spdb_managers));
                            // 应用负责人
                            List<Map> dev_managers = (List<Map>) app.get(Dict.DEV_MANAGERS);
                            users.addAll(this.getManageIds(dev_managers));
                        } catch (Exception e) {
                            logger.error("获取项目{}信息失败,{}", comp.getApplication_id(), e.getMessage());
                        }
                    }
                }
                //如果骨架使用此组件，也进行邮件发送
                List<ArchetypeInfo> archetypeInfoList = archetypeInfoService.query(new ArchetypeInfo());
                if (!CommonUtils.isNullOrEmpty(archetypeInfoList)) {
                    for (ArchetypeInfo archetypeInfo : archetypeInfoList) {
                        //查询骨架推荐版本
                        ArchetypeRecord archetypeRecord = archetypeRecordService.queryByArchetypeIdAndType(archetypeInfo.getId(), Constants.RECORD_RECOMMEND_TYPE);
                        if (archetypeRecord != null) {
                            //查询属于这个推荐版本的所有组件关联信息
                            List<ComponentArchetype> componentArchetypeList =
                                    componentArchetypeService.queryByArcIdAndVersionCopId(archetypeRecord.getArchetype_id(), archetypeRecord.getVersion(), component_id);
                            if (!CommonUtils.isNullOrEmpty(componentArchetypeList)) {
                                users.addAll(CommonUtils.getManageIds(archetypeInfo.getManager_id()));
                            }
                        }
                    }
                }
                if (!CommonUtils.isNullOrEmpty(users)) {
                    //获取用户的邮箱
                    Map<String, Object> mapId = new HashMap<>();
                    mapId.put(Dict.IDS, users);
                    mapId.put(Dict.REST_CODE, Dict.QUERYBYUSERCOREDATA);
                    Map<String, Map> userMap = (Map<String, Map>) restTransport.submit(mapId);
                    List<String> email_receivers = new ArrayList<>();
                    Set<Map.Entry<String, Map>> entries = userMap.entrySet();
                    if (!CommonUtils.isNullOrEmpty(entries)) {
                        for (Map.Entry<String, Map> entry : entries) {
                            try {
                                String email = (String) entry.getValue().get(Dict.EMAIL);
                                email_receivers.add(email);
                            } catch (Exception e) {
                                logger.error("获取人员邮箱信息错误" + entry.getKey());
                            }
                        }
                    }
                    //发送邮件
                    HashMap<String, String> hashMap = new HashMap();
                    String email_content = (String) redisTemplate.opsForValue().get(record.getId());
                    hashMap.put(Dict.EMAIL_CONTENT, email_content);
                    ComponentInfo componentInfo = componentInfoService.queryById(component_id);
                    String topic = Constants.SUBJECT + componentInfo.getName_cn() + "(" + version + ")";
                    mailService.sendEmail(topic, Dict.TEMPLATE_FCOMPONENT_UPDATE, hashMap, email_receivers.toArray(new String[email_receivers.size()]));
                }
            }
        }


    }

    @Override
    public void createFile(String token, String gitlab_project_id, String branch, String applicationFile, String content, String commitMessage) {
        String url = projectUrl() + "/" + gitlab_project_id + FILECONTEXT + applicationFile;
        Map<String, String> param = new HashMap<>();
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.BRANCH, branch);
        param.put(Dict.CONTENT, content);
        param.put(Dict.COMMIT_MESSAGE, commitMessage);
        URI uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
        try {
            this.gitlabTransport.submitPost(uri, param);
        } catch (Exception e) {
            try {
                param.remove(Dict.PRIVATE_TOKEN_L);
                uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
                this.gitlabTransport.submitPut(uri, token, param);
            } catch (Exception e1) {
                this.logger.error("####Update file error! rul=[{}], data=[{}]", url, param);
                throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"修改文件失败"});
            }
        }
    }

    @Override
    public String getFileContent(String token, String gitlab_project_id, String branch, String applicationFile) throws Exception {
        String url = projectUrl() + "/" + gitlab_project_id + FILECONTEXT + applicationFile + "/raw";
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(Dict.PRIVATE_TOKEN, token);
        URI uri = UriComponentsBuilder.fromHttpUrl(url).queryParam(Dict.REF, branch).build(true).toUri();
        Object content = null;
        try {
            content = this.gitlabTransport.submitGet(uri, headers);
        } catch (FdevException e) {
            if (e.getCode().equals(ErrorConstants.INVAILD_OPERATION_DATA)) {
                logger.error("应用:[{}], 分支:[{}], 文件路径:[{}], 未配置外部参数,出错信息:[{}]", gitlab_project_id, branch, applicationFile, e.getCode());
            }
        }
        if (content == null) {
            content = "";
        }
        return content.toString();
    }

    @Override
    public Object deleteTag(String gitlabId, String tagName, String token) throws Exception {
        String tag_url = CommonUtils.projectUrl(gitlabApiUrl) + "/" + gitlabId + "/repository/tags/" + tagName;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitDelete(tag_url, header);
    }


    public Map getProjectDeatilByProjectId(String token, String projectId) throws Exception {
        String pro_url = CommonUtils.projectUrl(gitlabApiUrl);
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        String requestPath = pro_url + "/" + projectId;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestPath);
        URI uri = builder.build(true).toUri();
        header.add(Dict.PRIVATE_TOKEN, token);
        String content = (String) gitlabTransport.submitGet(uri, header);
        return (Map) JSONObject.parse(content);
    }

    @Override
    public boolean addDevops(String projectId, String token, String templatePath, String projectName, String http_url_to_repo) throws Exception {
        String fdevCiTemplatePath = local_temp_repo + fdevCiTemplateName;//持续集成模板文件本仓库路径
        String localTempProjectPath = local_temp_repo + projectName;    //存放待持续集成项目的本地临时路径
        Git git = null;//持续集成模板文件本地仓库的句柄
        Git git1 = null;//待被持续集成项目本地仓库的句柄
        synchronized (this) {
            try {
                gitlabUserService.changeFdevRole(projectId, Dict.MAINTAINER);//设置权限，增加为Maintainer
                GitUtils.gitCloneFromGitlab(http_url_to_repo, localTempProjectPath, name, password, Dict.MASTER);//将待持续集成的项目clone到本地
                File file = new File(fdevCiTemplatePath);
                if (!file.exists()) {//当本地仓库不存在模板仓库时执行
                    String repoUrl = (String) getProjectDeatilByProjectId(token, templateId).get(Dict.HTTP_URL_TO_REPO);
                    GitUtils.gitCloneFromGitlab(repoUrl, fdevCiTemplatePath, name, password, branch);//将持续集成模版项目clone到本地
                }
                git = GitUtils.getGit(fdevCiTemplatePath);
                if (!GitUtils.checkLocalExisitBranch(git, branch)) {//判断本地持续集成模板文件本地仓库是否存在对应分支
                    GitUtils.createLocalBranchAndRealectRemotBranch(git, branch, branch);
                } else {
                    if (!branch.equals(git.getRepository().getBranch()))//判断持续集成模板文件本地仓库是否是对应的环境分支，若不是切切换当前环境分支
                        GitUtils.checkoutBranch(branch, git);
                }
                GitUtils.gitPullFromGitlab(git, name, password, branch);//通过指定分支pull远程分支至本地
                FileUtils.copyDirectory(new File(fdevCiTemplatePath + File.separator + templatePath), new File(localTempProjectPath));//将模板文件的内容拷贝至本地临时项目
                git1 = GitUtils.getGit(localTempProjectPath);
                GitUtils.gitPushFromGitlab(git1, "添加持续集成文件", name, password);//将本地临时项目推送到远程
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"添加持续集成模板文件失败！"});
            } finally {
                if (git != null)
                    git.close();
                if (git1 != null)
                    git1.close();
                CommonUtils.deleteFile(new File(localTempProjectPath));//清除本地项目临时仓库
                gitlabUserService.changeFdevRole(projectId, Dict.REPORTER);//回收权限，重新设置为Reporter
            }
            return true;
        }
    }

    @Override
    public Map getProjectInfo(String id, String token) throws Exception {
        String projectUrl = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id;
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
        // 拼装地址
        String searchGroup_url = gitlabApiUrl + "groups?search=" + split[split.length - 1];
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        String[] split2 = gitlabApiUrl.split(Dict.API);// 截取git地址
        String replace = path.replace(split2[0], "");// 获取path full_path
        // 通过group地址 获取groupid
        Object submitGet = gitlabTransport.submitGet(searchGroup_url, header);
        JSONArray grouplist = JSONObject.parseArray((String) submitGet);
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

        if (this.isProjectExist(namespaceid, name, token)) {
            logger.error("project has exist! project name=" + name);
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{" @@@@@ project has exist"});
        }

        return gitlabTransport.submitPost(gitlabApiUrl + Dict.PROJECTS, param);
    }


    /**
     * 判断当前项目在组下是否存在
     *
     * @param namespaceId
     * @param name
     * @param token
     * @return
     * @throws Exception
     */
    public boolean isProjectExist(Integer namespaceId, String name, String token) throws Exception {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        String searce_pro_url = CommonUtils.projectUrl(gitlabApiUrl) + "?search=" + name;//获取所以该名称的项目信息
        Object projects = gitlabTransport.submitGet(searce_pro_url, header);
        if (CommonUtils.isNullOrEmpty(projects)) {//若未查询到 则说明不存在
            return false;
        }
        JSONArray project_list = JSONObject.parseArray((String) projects);
        boolean flag = false;
        for (Object project : project_list) {
            Map map = (Map) project;
            Map namespace = (Map) map.get("namespace");
            String project_name = (String) map.get(Dict.NAME);
            Integer id = (Integer) namespace.get(Dict.ID);
            //判断groupid是否相同
            //判断name是否相同
            if (namespaceId.equals(id) && (name.equals(project_name))) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public List<Map<String, Object>> findBrancheListById(int projectId) throws IOException {
        boolean flag = true;
        List<Map<String, Object>> result = new ArrayList<>();
        int i = 1;
        while (flag) {
            String url = gitlabApiUrl + "projects/" + projectId + "/repository/branches?page=" + i + "&per_page=100";
            String body = Util.httpMethodGetExchange(url, restTemplate, token);
            if (StringUtils.isNotBlank(body)) {
                List<Map<String, Object>> branchList = Util.stringToList(body);
                if (branchList != null && branchList.size() > 0) {
                    result.addAll(branchList);
                    i++;
                } else {
                    flag = false;
                }
            } else if (body == null)
                flag = false;
        }
        logger.info(">>项目：" + projectId + ">>分支总数为：" + result.size());
        return result;
    }

    /**
     * 获取应用模块应用管理员的所有id
     *
     * @param list
     * @return
     */
    private Set<String> getManageIds(List<Map> list) {
        Set<String> result = new HashSet<>();
        if (!CommonUtils.isNullOrEmpty(list)) {
            for (Map map : list) {
                String id = (String) map.get(Dict.ID);
                result.add(id);
            }
        }
        return result;
    }

    /**
     * 获取项目当前分支合并信息
     *
     * @param id
     * @param branch
     * @param state
     * @return
     * @throws Exception
     */
    @Override
    public Object queryMergeList(String id, String branch, String state) throws Exception {
        String meregeList_url = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id + "/merge_requests?target_branch=" + branch + "&state=" + state;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitGet(meregeList_url, header);
        //JSONArray mergelist = JSONObject.parseArray((String) result);
    }

    /**
     * 获取项目当前分支pipeline信息
     *
     * @param id
     * @param branch
     * @param status
     * @return
     * @throws Exception
     */
    @Override
    public Object queryPipelineList(String id, String branch, String status) throws Exception {
        String meregeList_url = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id + "/pipelines?ref=" + branch + "&status=" + status;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitGet(meregeList_url, header);
        //JSONArray mergelist = JSONObject.parseArray((String) result);
    }

    @Override
    public String getGitLabFileContent(Integer projectId, String fileDir, String branch) {
        String url = CommonUtils.projectUrl(gitlabApiUrl) + "/" + projectId + "/repository/files/" + fileDir + "/raw?ref=" + branch;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, token);
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
            return (String) gitlabTransport.submitGet(uri, header);
        } catch (Exception e) {
            logger.info("projectId:{}", projectId);
            logger.info("beanch:{}", branch);
            return "";
        }
    }

    /**
     * 设置保护分支
     *
     * @param id
     * @param name
     * @return
     * @throws Exception
     */
    @Override
    public Object setProtectedBranches(String id, String name) throws Exception {
        try {
            String protected_branchesUrl = CommonUtils.projectUrl(gitlabApiUrl) + "/" + id + "/protected_branches/";
            Map<String, String> param = new HashMap<>();
            param.put(Dict.ID, id);
            param.put(Dict.NAME, name);
            param.put(Dict.PRIVATE_TOKEN_L, token);
            return this.gitlabTransport.submitPost(protected_branchesUrl, param);
        } catch (Exception e) {
            logger.error("添加保护分支失败,{}", e.getStackTrace());
        }
        return null;

    }

    /**
     * 更新release分支的changelog和版本
     *
     * @param mpassComponent
     * @param feature_branch
     * @param tag
     * @throws Exception
     */
    @Override
    public void updateReleaseChangeLog(MpassComponent mpassComponent, String feature_branch, String tag) throws Exception {
        String projectId = mpassComponent.getGitlab_id();
        String projectName = mpassComponent.getName_en();
        Map projectMap = this.getProjectInfo(projectId, token);
        String http_url_to_repo = (String) projectMap.get(Dict.HTTP_URL_TO_REPO);
        String localTempProjectPath = local_temp_repo + projectName;    //存放待修改项目的本地临时路径
        Git git = null;//持续集成模板文件本地仓库的句柄
        try {
            gitlabUserService.changeFdevRole(projectId, Dict.MAINTAINER);//设置权限，增加为Maintainer
            GitUtils.gitCloneFromGitlab(http_url_to_repo, localTempProjectPath, name, password, feature_branch);//将待持续集成的项目clone到本地
            String shellDir = localTempProjectPath;
            if (StringUtils.isNotBlank(mpassComponent.getRoot_dir())) {
                shellDir += "/" + mpassComponent.getRoot_dir();
            }
            ShellUtils.releaseShell(shellDir, tag);
            git = GitUtils.getGit(localTempProjectPath);
            GitUtils.gitPushFromGitlab(git, "releasePackage Update ChangeLog", name, password);//将本地临时项目推送到远程
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"更新release分支ChangeLog失败！"});
        } finally {
            if (git != null)
                git.close();
            CommonUtils.deleteFile(new File(localTempProjectPath));//清除本地项目临时仓库
            gitlabUserService.changeFdevRole(projectId, Dict.REPORTER);//回收权限，重新设置为Reporter
        }
    }

    @Override
    public void updateReleaseChangeLog(ComponentInfo component, String feature_branch, String tag) throws Exception {
        String projectId = queryProjectIdByUrl(component.getGitlab_url());
        String projectName = component.getName_en();
        Map projectMap = this.getProjectInfo(projectId, token);
        String http_url_to_repo = (String) projectMap.get(Dict.HTTP_URL_TO_REPO);
        String localTempProjectPath = local_temp_repo + projectName;    //存放待修改项目的本地临时路径
        Git git = null;//持续集成模板文件本地仓库的句柄
        try {
            gitlabUserService.changeFdevRole(projectId, Dict.MAINTAINER);//设置权限，增加为Maintainer
            GitUtils.gitCloneFromGitlab(http_url_to_repo, localTempProjectPath, name, password, feature_branch);//将待持续集成的项目clone到本地
            String shellDir = localTempProjectPath;
            if (StringUtils.isNotBlank(component.getRoot_dir())) {
                shellDir += "/" + component.getRoot_dir();
            }
            ShellUtils.releaseShell(shellDir, tag);
            git = GitUtils.getGit(localTempProjectPath);
            GitUtils.gitPushFromGitlab(git, "releasePackage Update ChangeLog", name, password);//将本地临时项目推送到远程
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new FdevException(Constant.I_GITLAB_ERROR, new String[]{"更新release分支ChangeLog失败！"});
        } finally {
            if (git != null)
                git.close();
            CommonUtils.deleteFile(new File(localTempProjectPath));//清除本地项目临时仓库
            gitlabUserService.changeFdevRole(projectId, Dict.REPORTER);//回收权限，重新设置为Reporter
        }
    }

    @Override
    public List queryTag(String projectId, String version) throws Exception {
        ArrayList result = new ArrayList();
        List<Map<String, Object>> list = this.findTagListById(Integer.parseInt(projectId), version);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                result.add(list.get(i).get(Dict.NAME));
            }
        }
        return result;
    }


    public List<Map<String, Object>> findTagListById(int projectId, String version) throws IOException {
        boolean flag = true;
        List<Map<String, Object>> result = new ArrayList<>();
        int i = 1;
        while (flag) {
            String url = gitlabApiUrl + "projects/" + projectId + "/repository/tags?search=" + version + "&page=" + i + "&per_page=100";
            String body = Util.httpMethodGetExchange(url, restTemplate, token);
            if (StringUtils.isNotBlank(body)) {
                List<Map<String, Object>> branchList = Util.stringToList(body);
                if (branchList != null && branchList.size() > 0) {
                    result.addAll(branchList);
                    i++;
                } else {
                    flag = false;
                }
            } else if (body == null)
                flag = false;
        }
        logger.info(">>项目：" + projectId + ">>分支总数为：" + result.size());
        return result;
    }

    @Override
    public String getProjectIdById(String id, String type) throws Exception {
        String gitlabId = "";
        if ("componentServer".equals(type)) {
            ComponentInfo component = componentInfoDao.queryById(id);
            //如果查不到 抛错
            if (component == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"后端组件信息不存在"});
            }
            gitlabId = component.getGitlab_id();

        } else if ("componentWeb".equals(type)) {
            MpassComponent mpassComponent = mpassComponentDao.queryById(id);
            //如果查不到 抛错
            if (mpassComponent == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"前端组件信息不存在"});
            }
            gitlabId = mpassComponent.getGitlab_id();

        } else if ("archetypeServer".equals(type)) {
            ArchetypeInfo archetype = archetypeInfoDao.queryById(id);
            //如果查不到 抛错
            if (archetype == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"后端骨架信息不存在"});
            }
            gitlabId = archetype.getGitlab_id();

        } else if ("archetypeWeb".equals(type)) {
            MpassArchetype mpassArchetype = iMpassArchetypeDao.queryById(id);
            //如果查不到 抛错
            if (mpassArchetype == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"前端骨架信息不存在"});
            }
            gitlabId = mpassArchetype.getGitlab_id();

        } else {
            BaseImageInfo baseImageInfo = baseImageInfoDao.queryById(id);
            //如果查不到 抛错
            if (baseImageInfo == null) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"前端骨架信息不存在"});
            }
            gitlabId = baseImageInfo.getGitlab_id();

        }

        return gitlabId;
    }
}
