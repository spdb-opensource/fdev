package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.GitlabTransport;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author lsw
 * @description task manage
 * @date 2020/8/10
 */
@Service
@RefreshScope
public class TaskManageServiceImpl implements TaskManageService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GitlabTransport gitlabTransport;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private IFdevTaskService fdevTaskService;

    @Autowired
    private IAppApi iAppApi;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Autowired
    private GitApiService gitApiServiceImpl;

    @Autowired
    private IComponentService componentService;

    @Value("${gitlib.path}")
    private String url;// gitlab地址http://xxx/api/v4/

    @Value("${git.token}")
    private String token;

    @Value("${iams.update.files:pe-core.xml}")
    private String iamsFile;

    @Value("${iams.check.typeId}")
    private String checkId;

    @Value("${native.package.filename}")
    private String nativeFilename;

    @Value("${fdev.config.git.project.id}")
    private String fdevConfigGitId;

    @Value("${fdev.config.git.path}")
    private String fdevConfigGitPath;

    @Value("${production.git.token}")
    private String productionToken;

    @Override
    public void checkAuthentication(Map<String, Object> requestParam) throws Exception{
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
    }

    @Override
    public JsonResult generateTestPackage(Map<String, Object> requestParam) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        String ref = (String) requestParam.get(Dict.REF);
        String id = (String) requestParam.get(Dict.ID);
        String gitLabId = getGitLabId(id);
        String pipeline_url = CommonUtils.projectUrl(url) + "/" + gitLabId + "/pipeline";
        // 拆分 variables参数 将其重新组装为map再加入到list中
        Map<String, Object> param = new HashMap<String, Object>();
        String[] values = {ref, requestParam.get(Dict.DESC).toString(), user.getGit_user(),
                requestParam.get(Dict.ISREINFORCE).toString(), requestParam.get(Dict.ENV_NAME).toString()};
        String[] keys = {"FDEV_BRANCH_NAME", "FDEV_DESC", "FDEV_USERNAME", "FDEV_IS_REINFORCE", "FDEV_ENVS"};
        List<Map<String, String>> list = new LinkedList<>();
        for (int i = 0; i < values.length; i++) {
            HashMap<String, String> map = Maps.newHashMap();
            map.put("key", keys[i]);
            map.put("value", values[i]);
            list.add(map);
        }
        param.put(Dict.VARIABLES, list);
        param.put(Dict.ID, gitLabId);
        param.put(Dict.PRIVATE_TOKEN_L, token);
        param.put(Dict.REF, ref);
        Object result = gitlabTransport.submitPost(pipeline_url, param);
        return JsonResultUtil.buildSuccess(result);
    }

    @Override
    public JsonResult checkIamsProperties(Map<String, Object> requestParam) throws Exception {
        ArrayList checks = new ArrayList(Arrays.asList(checkId.split(";")));
        String gitlab_project_id = (String) requestParam.get(Dict.GITLAB_PROJECT_ID);
        Map param = new HashMap();
        param.put(Dict.ID, gitlab_project_id);
        param.put(Dict.REST_CODE, "getAppByGitId");
        Map app = (Map) restTransport.submit(param);


        if (!CommonUtils.isNullOrEmpty(app)) {
            if (!checks.contains(app.get(Dict.TYPE_ID))) {
                return JsonResultUtil.buildSuccess(true);
            }
        } else {
            return JsonResultUtil.buildSuccess(true);
        }

        //获取所有iams涉及修改的文件名
        String[] iamsFiles = iamsFile.split(";");
        //获取当前项目对应分支下所有文件
        String gitlab_url = CommonUtils.projectUrl(url) + "/" + gitlab_project_id + "/repository/tree?recursive=true&per_page=100&ref=" + requestParam.get(Dict.BRANCH_NAME);
        MultiValueMap<String, String> header = new LinkedMultiValueMap();
        header.add(Dict.PRIVATE_TOKEN, token);
        JSONArray resultArray = (JSONArray) gitlabTransport.submitGet(gitlab_url, header);
        List<HashMap> files = JSON.parseArray(resultArray.toString(), HashMap.class);
        HashSet<String> iamsFilePaths = new HashSet<>();
        for (HashMap file : files) {
            for (String iamsFileName : iamsFiles) {
                if (iamsFileName.equals(file.get("name").toString())) {
                    String urlEncoder = URLEncoder.encode(file.get("path").toString(), "UTF-8");
                    iamsFilePaths.add(urlEncoder.replace(".", "%2E"));
                }
            }
        }
        boolean result = true;
        for (String filePath : iamsFilePaths) {
            String requestUrl = CommonUtils.projectUrl(url) + "/" + requestParam.get(Dict.GITLAB_PROJECT_ID) + "/repository/files/" + filePath + "/raw";
            String urlParams = "ref=" + requestParam.get(Dict.BRANCH_NAME);
            requestUrl += "?" + urlParams;

            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(requestUrl);
            URI uri = uriComponentsBuilder.build(true).toUri();

            String str = (String) gitlabTransport.submitGetByURI(uri, header);
            String[] strs = str.split("\n");
            for (String s : strs) {
                if (s.contains("iams.properties")) {
                    result = false;
                    break;
                }
            }
        }
        return JsonResultUtil.buildSuccess(result);
    }

    private String getGitLabId(String taskId) throws Exception {
        FdevTask task = fdevTaskService.queryTaskAll(new FdevTask.FdevTaskBuilder().id(taskId).init());
        Map appInfo = iAppApi.queryAppById(new HashMap() {{
            put(Dict.ID, task.getProject_id());
        }});
        if (CommonUtils.isNullOrEmpty(task))
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{Dict.TASK});
        if (CommonUtils.isNullOrEmpty(appInfo))
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{Dict.APP});
        return String.valueOf(appInfo.get(Dict.GITLAB_PROJECT_ID));
    }

    @Override
    public JsonResult queryParamFile() throws Exception {
        String gitlabUrl = CommonUtils.projectUrl(fdevConfigGitPath) + "/" + fdevConfigGitId + "/repository/tree?recursive=true&per_page=100&ref=" + Dict.MASTER;
        MultiValueMap<String, String> header = new LinkedMultiValueMap();
        header.add(Dict.PRIVATE_TOKEN, productionToken);
        JSONArray resultArray = (JSONArray) gitlabTransport.submitGet(gitlabUrl, header);
        List<HashMap> files = JSON.parseArray(resultArray.toString(), HashMap.class);
        String filePath = new String();
        for (HashMap file : files) {
            if (nativeFilename.equals(file.get("name").toString())) {
                String urlEncoder = URLEncoder.encode(file.get("path").toString(), "UTF-8");
                filePath = urlEncoder.replace(".", "%2E");
                break;
            }
        }

        String requestUrl = CommonUtils.projectUrl(fdevConfigGitPath) + "/" + fdevConfigGitId + "/repository/files/" + filePath + "/raw";
        String urlParams = "ref=" + Dict.MASTER;
        requestUrl += "?" + urlParams;

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(requestUrl);
        URI uri = uriComponentsBuilder.build(true).toUri();

        String fileContent = (String) gitlabTransport.submitGetByURI(uri, header);
        JSONObject jsContent = new JSONObject();
        try {
            jsContent = JSONObject.parseObject(fileContent);
            logger.info(jsContent.toJSONString());
        }catch (Exception e){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"原生文件不满足json格式，请重传"});
        }

        return JsonResultUtil.buildSuccess(jsContent);
    }

    @Override
    public void generatePackageFile(Map<String, Object> requestParam) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        String id = (String) requestParam.get(Dict.ID);
        //查询任务信息
        FdevTask task = fdevTaskService.queryTaskById(id);
        if(CommonUtils.isNullOrEmpty(task)) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[] {"任务不存在"});
        }
        //如果关联的应用类型是前端组件或者后端组件，调用组件模块接口出beta包或者alpha包
        if(CommonUtils.isComponent(task.getApplicationType())) {
            //出包分支，如果是开发阶段，从开发分支出包，如果是SIT阶段，用户选择开发分支或者SIT分支
            String branch = (String) requestParam.get(Dict.REF);
            String packageType = (String) requestParam.get(Dict.PACKAGETYPE);
            String targetVersion = (String) requestParam.get(Dict.TARGETVERSION);
            String componentId = task.getProject_id();
            String componentType = Constants.COMPONENT_WEB.equals(task.getApplicationType()) ? Constants.MPASS : Constants.BACK;
            componentService.testPackage(componentId, componentType, task.getVersionNum(), branch, packageType, targetVersion);
//            String versionNum = task.getVersionNum();
//            //查询组件的gitlab id
//            Map<String,Object> component = componentService.queryComponentWebDetail(task.getProject_id());
//            if(CommonUtils.isNullOrEmpty(component)) {
//                throw new FdevException(ErrorConstants.TASK_ERROR, new String[] {"组件信息不存在"});
//            }
//            String gitlabId = (String) component.get(Dict.GITLABID);
//            //生成包名
//            String tagName = "v"+versionNum+"-alpha.";
//            //根据tag名查询tag列表，取最后一个的序号+1
//            int index = 0;
//            List<Map> tags = gitApiServiceImpl.searchTags(gitlabId, tagName);
//            if(!CommonUtils.isNullOrEmpty(tags)) {
//                //获取tag列表名称最后的序号列表，并排序
//                List<String> tagNameIndexList = tags.stream().map(tag -> ((String) tag.get(Dict.NAME)).substring(
//                        ((String) tag.get(Dict.NAME)).lastIndexOf(".")+1
//                )).filter(name -> CommonUtils.isNumber(name)).sorted().collect(Collectors.toList());
//                if(CommonUtils.isNullOrEmpty(tagNameIndexList)) {
//                    index = Integer.parseInt(tagNameIndexList.get(tagNameIndexList.size()-1)) + 1;
//                }
//            }
//            tagName = tagName + index;
//            //拉取tag包
//            gitApiServiceImpl.createTag(gitlabId, task.getFeature_branch(), tagName);
        }else {
            String gitLabId = getGitLabId(id);
            String pipeline_url = CommonUtils.projectUrl(url) + "/" + gitLabId + "/pipeline";
            // 拆分 variables参数 将其重新组装为map再加入到list中
            Map<String, Object> param = new HashMap<String, Object>();

            String[] values = {(String) requestParam.get(Dict.REF), requestParam.get(Dict.DESC).toString(), user.getGit_user()};
            String[] keys = {"FDEV_BRANCH_NAME", "FDEV_DESC", "FDEV_USERNAME"};
            List<Map<String, String>> list = new LinkedList<>();
            for (int i = 0; i < values.length; i++) {
                HashMap<String, String> map = Maps.newHashMap();
                map.put("key", keys[i]);
                map.put("value", values[i]);
                list.add(map);
            }
            HashMap<String, Object> multiEnvMap = (HashMap<String, Object>) requestParam.get("multi_env");
            JSONObject multiEnvJson = JSONObject.parseObject(JSON.toJSONString(multiEnvMap));
            Map<String, String> map = new HashMap<>();
            map.put("key","MULTI_ENV");
            map.put("value",multiEnvJson.toString());
            list.add(map);
            param.put(Dict.VARIABLES, list);
            param.put(Dict.ID, gitLabId);
            param.put(Dict.PRIVATE_TOKEN_L, token);
            param.put(Dict.REF, (String) requestParam.get(Dict.REF));
            logger.info(param.toString());
            gitlabTransport.submitPost(pipeline_url, param);
        }
    }
}
