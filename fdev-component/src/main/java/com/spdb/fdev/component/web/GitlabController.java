package com.spdb.fdev.component.web;


import com.alibaba.fastjson.JSON;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.ShellUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.component.service.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/gitlabapi")
@RestController
@RefreshScope
public class GitlabController {

    @Value("${gitlab.manager.token}")
    private String token;

    @Value("${python.path}")
    private String python_path;

    @Autowired
    private IComponentRecordService componentRecordService;

    @Autowired
    private IArchetypeRecordService archetypeRecordService;

    @Autowired
    private IBaseImageRecordService baseImageRecordService;

    @Autowired
    private IGitlabSerice gitlabSerice;

    @Autowired
    private IComponentApplicationService componentApplicationService;

    @Autowired
    private IAppService appService;

    @Autowired
    private IMpassRelaseIssueService mpassRelaseIssueService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * 组件回调webhook接口
     *
     * @param req
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/webHook", method = RequestMethod.POST)
    public JsonResult webHook(@RequestBody String req, HttpServletRequest request) throws Exception {
        //接收Merge回调请求
        if (Dict.MERGE_REQUEST_HOOK.equals(request.getHeader(Dict.X_GITLAB_EVENT))) {
            Map<String, Object> parse = (Map<String, Object>) JSON.parse(req);
            if (CollectionUtils.isEmpty(parse)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
            Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
            String state = (String) attributes.get(Dict.STATE);
            Integer iid = (Integer) attributes.get(Dict.IID);
            Integer project_id = (Integer) attributes.get(Dict.SOURCE_PROJECT_ID);
            String source_branch = (String) attributes.get(Dict.SOURCE_BRANCH);
            String target_branch = (String) attributes.get(Dict.TARGET_BRANCH);
            //当state为merged时，分支合并成功
            if ((Dict.MERGED.equals(state))
                    && ((!StringUtils.isBlank(source_branch) && source_branch.contains("-SNAPSHOT") && Dict.MASTER.equals(target_branch)))) {
                componentRecordService.mergedCallBack(state, iid, project_id);
                logger.info("@@@@@@@@@ 发送merge callback 成功");
            }


       /*     //当state为merged且合并至SIT或者合并至release时，分支合并成功
            if ((Dict.MERGED.equals(state))
                    && (!StringUtils.isBlank(source_branch)) && (!StringUtils.isBlank(target_branch))) {

                if ((target_branch.equals("SIT")) || (target_branch.contains("release-"))) {
                    componentRecordService.mergedSitAndReleaseCallBack(state, iid, project_id, target_branch);
                    logger.info("@@@@@@@@@ 发送merge callback 成功");
                }
            }


            //当state为merged且release分支合并至master分支时，分支合并成功
            if ((Dict.MERGED.equals(state))
                    && (!StringUtils.isBlank(source_branch)) && (!StringUtils.isBlank(target_branch))) {

                if (source_branch.contains("release-") && Dict.MASTER.equals(target_branch)) {
                    componentRecordService.mergedMasterCallBack(state, iid, project_id, target_branch);
                    logger.info("@@@@@@@@@ 发送merge callback 成功");
                }
            }
*/
        }
        //接收PipeLine回调请求
        if (Dict.PIPELINE_HOOK.equals(request.getHeader(Dict.X_GITLAB_EVENT))) {
            Map<String, Object> parse = (Map<String, Object>) JSON.parse(req);
            System.out.println("parse=" + parse);
            if (CollectionUtils.isEmpty(parse)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
            Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
            Map<String, Object> project = (Map<String, Object>) parse.get(Dict.PROJECT);
            String version = null;
            List<Map> mapList = (List<Map>) attributes.get("variables");
            if (mapList != null && mapList.size() > 0) {
                for (Map map : mapList) {
                    if ("version".equals(map.get("key"))) {
                        version = (String) map.get("value");
                    }
                }
            }
            String status = (String) attributes.get(Dict.STATUS);
            String pipid = String.valueOf(attributes.get(Dict.ID));
            if (Dict.SUCCESS.equals(status)) {
                gitlabSerice.updateComponentAfterPip(version, pipid);
            }
        }
        return null;
    }


    /**
     * 测试python扫描组件和骨架关联应用脚本执行情况
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/pythonResult")
    public JsonResult pythonresult(@RequestBody Map<String, String> requestParam) throws Exception {
        String dir = requestParam.get("dir");
        String script = requestParam.get("script");
        String[] cmdA = {"python", python_path + script, dir};
        Process pr = Runtime.getRuntime().exec(cmdA);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                pr.getInputStream()));
             BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));){
            StringBuffer result = new StringBuffer();
            StringBuffer errorBuffer = new StringBuffer();
            String line = in.readLine();
            result.append(line);
            String line2 = error.readLine();
            errorBuffer.append(line2);
            if (!CommonUtils.isNullOrEmpty(errorBuffer.toString())) {
                FdevException fdevException = new FdevException(ErrorConstants.SERVER_ERROR);
                fdevException.setMessage(errorBuffer.toString());
                throw fdevException;
            }
            in.close();
            pr.waitFor();
            return JsonResultUtil.buildSuccess(result.toString());
        } catch (Exception e) {
            logger.error("执行{}的时候出现错误，错误信息如下{}", "python" + python_path + script + dir, e);
            return JsonResultUtil.buildSuccess("python" + python_path + script + dir + e);
        }
    }


    /**
     * 获取mpass组件版本
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/getMpassComponentVersion")
    public JsonResult getNodeVersion(@RequestBody Map<String, Object> requestParam) {
        String npmName = (String) requestParam.get(Dict.NPM_NAME);
        String npmGroup = (String) requestParam.get(Dict.NPM_GROUP);
        Boolean flag = (Boolean) requestParam.get(Dict.VALUE);
        String result = ShellUtils.cellShell(npmName, npmGroup, flag);
        if (flag) {
            List<String> versionList = new ArrayList<>();
            if (StringUtils.isNotBlank(result) && result.length() > 1) {
                String[] versionArray = result.substring(1, result.length() - 1).split(",");
                if (!CommonUtils.isNullOrEmpty(versionArray)) {
                    for (String version : versionArray) {
                        version = version.replaceAll("[\\[|\\]|\\']", "").trim();
                        versionList.add(version);
                    }
                }
            }
            return JsonResultUtil.buildSuccess(versionList);
        } else
            return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 骨架回调webhook接口
     *
     * @param req
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/archetypeWebHook", method = RequestMethod.POST)
    public JsonResult archetypeWebHook(@RequestBody String req, HttpServletRequest request) throws Exception {
        String header = request.getHeader(Dict.X_GITLAB_EVENT);
        //接收Merge回调请求
        if (Dict.MERGE_REQUEST_HOOK.equals(header)) {
            Map<String, Object> parse = (Map<String, Object>) JSON.parse(req);
            if (CollectionUtils.isEmpty(parse)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
            Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
            String state = (String) attributes.get(Dict.STATE);
            Integer iid = (Integer) attributes.get(Dict.IID);
            Integer project_id = (Integer) attributes.get(Dict.SOURCE_PROJECT_ID);
            String source_branch = (String) attributes.get(Dict.SOURCE_BRANCH);
            String target_branch = (String) attributes.get(Dict.TARGET_BRANCH);

            //当state为merged时，分支合并成功
            if (Dict.MERGED.equals(state) && !StringUtils.isBlank(source_branch)) {

                if (target_branch.contains("-SNAPSHOT") || source_branch.contains("-SNAPSHOT")) {
                    archetypeRecordService.mergedCallBack(state, iid, project_id);
                    logger.info("@@@@@@@@@ 发送merge callback 成功");
                }
            }


            //当state为merged且合并至SIT或者合并至master时，分支合并成功
            if ((Dict.MERGED.equals(state))
                    && (!StringUtils.isBlank(source_branch)) && (!StringUtils.isBlank(target_branch))) {

                if ((target_branch.equals("SIT")) || (source_branch.contains("release-") && Dict.MASTER.equals(target_branch))) {
                    archetypeRecordService.mergedSitAndMasterCallBack(state, iid, project_id, target_branch);
                    logger.info("@@@@@@@@@ 发送merge callback 成功");
                }
            }


        }


        //接收PipeLine回调请求
        if (Dict.PIPELINE_HOOK.equals(header)) {
            Map<String, Object> parse = (Map<String, Object>) JSON.parse(req);
            System.out.println("parse=" + parse);
            if (CollectionUtils.isEmpty(parse)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
            Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
            Map<String, Object> project = (Map<String, Object>) parse.get(Dict.PROJECT);
            String version = null;
            List<Map> mapList = (List<Map>) attributes.get("variables");
            if (mapList != null && mapList.size() > 0) {
                for (Map map : mapList) {
                    if ("version".equals(map.get("key"))) {
                        version = (String) map.get("value");
                    }
                }
            }
            String status = (String) attributes.get(Dict.STATUS);
            String pipid = String.valueOf(attributes.get(Dict.ID));
            if (Dict.SUCCESS.equals(status)) {
                archetypeRecordService.pipiCallBack(version, pipid);
            }
        }
        return null;
    }

    /**
     * 基础镜像发布webhook回调接口
     *
     * @param req
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/imageWebHook", method = RequestMethod.POST)
    public JsonResult imageWebHook(@RequestBody String req, HttpServletRequest request) throws Exception {
        String header = request.getHeader(Dict.X_GITLAB_EVENT);
        //接收Merge回调请求
        if (Dict.MERGE_REQUEST_HOOK.equals(header)) {
            Map<String, Object> parse = (Map<String, Object>) JSON.parse(req);
            if (CollectionUtils.isEmpty(parse)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
            Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
            String state = (String) attributes.get(Dict.STATE);
            Integer iid = (Integer) attributes.get(Dict.IID);
            Integer project_id = (Integer) attributes.get(Dict.SOURCE_PROJECT_ID);
            String source_branch = (String) attributes.get(Dict.SOURCE_BRANCH);
            String target_branch = (String) attributes.get(Dict.TARGET_BRANCH);
            //当state为merged时，分支合并成功
            if (Dict.MERGED.equals(state) && !StringUtils.isBlank(source_branch)) {

                if (!source_branch.contains("release-")&&!target_branch.equals("SIT")&&!target_branch.contains("release")) {
                    baseImageRecordService.mergedCallBack(state, iid, project_id);
                    logger.info("@@@@@@@@@ 发送merge callback 成功");
                }
            }

            //当state为merged时，分支合并成功
            if (Dict.MERGED.equals(state) && !StringUtils.isBlank(source_branch)) {

                if (source_branch.contains("release-") && Dict.MASTER.equals(target_branch)) {
                    baseImageRecordService.mergedMasterCallBack(state, iid, project_id);
                    logger.info("@@@@@@@@@ 发送merge callback 成功");
                }
            }


        }

        //接收PipeLine回调请求
        if (Dict.PIPELINE_HOOK.equals(header)) {
            Map<String, Object> parse = (Map<String, Object>) JSON.parse(req);
            System.out.println("parse=" + parse);
            if (CollectionUtils.isEmpty(parse)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
            Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
            String status = (String) attributes.get(Dict.STATUS);
            String ref = (String) attributes.get(Dict.REF);//当前分支名
            Boolean tag = (Boolean) attributes.get(Constants.TAG);//是否tag分支
            Map<String, Object> project = (Map<String, Object>) parse.get(Dict.PROJECT);
            //项目gitlab id
            String gitlabId = project.get(Dict.ID) + "";
            if (Dict.SUCCESS.equals(status) && tag) {
                baseImageRecordService.pipiCallBack(gitlabId, ref);
            }
        }
        return null;
    }

    /**
     * mpass组件pipeline回调事件
     *
     * @param req
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/mpassWebHook", method = RequestMethod.POST)
    public JsonResult mpassWebHook(@RequestBody String req, HttpServletRequest request) throws Exception {
        String header = request.getHeader(Dict.X_GITLAB_EVENT);
        //接收Merge回调请求
        if (Dict.MERGE_REQUEST_HOOK.equals(header)) {
            Map<String, Object> parse = (Map<String, Object>) JSON.parse(req);
            if (CollectionUtils.isEmpty(parse)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
            Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
            String state = (String) attributes.get(Dict.STATE);
            Integer iid = (Integer) attributes.get(Dict.IID);
            Integer project_id = (Integer) attributes.get(Dict.SOURCE_PROJECT_ID);
            String source_branch = (String) attributes.get(Dict.SOURCE_BRANCH);
            String target_branch = (String) attributes.get(Dict.TARGET_BRANCH);
            //当state为merged时，分支合并成功
            if (Dict.MERGED.equals(state)
                    && StringUtils.isNotBlank(source_branch)
                    && StringUtils.isNotBlank(target_branch) && !source_branch.contains("release-") && Dict.MASTER.equals(target_branch)) {
                mpassRelaseIssueService.mergedCallBack(iid, project_id);
                logger.info("@@@@@@@@@ 发送merge callback 成功");
            }


            //当state为merged且合并至SIT或者合并至release时，分支合并成功
            if ((Dict.MERGED.equals(state))
                    && (!StringUtils.isBlank(source_branch)) && (!StringUtils.isBlank(target_branch))) {

                if ((target_branch.equals("SIT")) || (target_branch.contains("release-"))) {
                    mpassRelaseIssueService.mergedSitAndReleaseCallBack(state, iid, project_id, target_branch);
                    logger.info("@@@@@@@@@ 发送merge callback 成功");
                }
            }


            //当state为merged且release分支合并至master分支时，分支合并成功
            if ((Dict.MERGED.equals(state))
                    && (!StringUtils.isBlank(source_branch)) && (!StringUtils.isBlank(target_branch))) {

                if (source_branch.contains("release-") && Dict.MASTER.equals(target_branch)) {
                    mpassRelaseIssueService.mergedMasterCallBack(state, iid, project_id, target_branch);
                    logger.info("@@@@@@@@@ 发送merge callback 成功");
                }
            }

        }


        //接收PipeLine回调请求
        if (Dict.PIPELINE_HOOK.equals(header)) {
            Map<String, Object> parse = (Map<String, Object>) JSON.parse(req);
            System.out.println("parse=" + parse);
            if (CollectionUtils.isEmpty(parse)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
            Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
            String status = (String) attributes.get(Dict.STATUS);//当前pipeline状态
            String ref = (String) attributes.get(Dict.REF);//当前分支名
            Boolean tag = (Boolean) attributes.get(Constants.TAG);//是否tag分支
            Map<String, Object> project = (Map<String, Object>) parse.get(Dict.PROJECT);
            String gitlab_url = (String) project.get(Constants.WEB_URL);//项目gitlab地址
            //项目gitlab id
            String gitlabId = project.get(Dict.ID) + "";
            if (Dict.SUCCESS.equals(status) && tag) {
                mpassRelaseIssueService.pipiCallBack(gitlab_url, gitlabId, ref);
            }
        }
        return null;
    }


    /**
     * 对于所有的应用，master分支merged增加组件扫描
     *
     * @param req
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/scanComponentWebHook", method = RequestMethod.POST)
    public JsonResult scanComponentWebHook(@RequestBody String req, HttpServletRequest request) throws Exception {
        //接收Merge回调请求
        if (Dict.MERGE_REQUEST_HOOK.equals(request.getHeader(Dict.X_GITLAB_EVENT))) {
            Map<String, Object> parse = (Map<String, Object>) JSON.parse(req);
            if (CollectionUtils.isEmpty(parse)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST);
            }
            Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
            String state = (String) attributes.get(Dict.STATE);
            Integer project_id = (Integer) attributes.get(Dict.SOURCE_PROJECT_ID);
            String target_branch = (String) attributes.get(Dict.TARGET_BRANCH);
            //当state为merged时，分支合并成功
            if ((Dict.MERGED.equals(state))
                    && ((StringUtils.isNotBlank(target_branch) && Dict.MASTER.equals(target_branch)))) {
                logger.info("@@@@@@@@@ merge callback 成功,开始进行组件扫描");
                Map map = appService.getAppByGitId(String.valueOf(project_id));
                if (map != null) {
                    String id = (String) map.get(Dict.ID);
                    componentApplicationService.scanApplication(id);
                }
                logger.info("@@@@@@@@@ merge callback 成功,组件扫描结束");
            }
        }
        return null;
    }


    @RequestValidate(NotEmptyFields = {Dict.NAME, Dict.ID, Dict.REF, Dict.TYPE})
    @RequestMapping(value = "/createBranch", method = RequestMethod.POST)
    public JsonResult createBranch(@RequestBody Map requestParam) throws Exception {
        String type = (String) requestParam.get(Dict.TYPE);
        String name = (String) requestParam.get(Dict.NAME);
        String id = (String) requestParam.get(Dict.ID);
        String ref = (String) requestParam.get(Dict.REF);
        String projectId = gitlabSerice.getProjectIdById(id, type);

        if (CommonUtils.isNullOrEmpty(projectId)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"@@@@@ 未查询到相关应用  应用id=" + id});
        }
        Object result = gitlabSerice.createBranch(projectId, name, ref, token);
        return JsonResultUtil.buildSuccess(result);
    }

}
