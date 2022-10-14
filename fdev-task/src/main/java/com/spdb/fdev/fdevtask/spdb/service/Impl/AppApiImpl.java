package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.service.IAppApi;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@Service
@RefreshScope
public class AppApiImpl implements IAppApi {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private IUserApi userApi;
    @Autowired
    private RestTransport restTransport;

    /**
     * 创建gitlab分支
     */
    @Override
    public Map createGitLabBranch(Map param) throws Exception {
        param.put(Dict.REST_CODE, "createBranch");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK-APP 创建分支返回为空"});
        }
//        if(result instanceof String){
//            return CommonUtils.converMap(CommonUtils.filter(JSONObject.fromObject(result)));
//        }
        return (Map) result;
    }

    /**
     * 创建应用
     */
    @Override
    public Map createApp(Map param) throws Exception {
        param.put(Dict.REST_CODE, "saveApp");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK-APP 创建应用返回为空"});
        }
        return (Map) result;
    }

    @Override
    public Map requestMerge(Map param) throws Exception {
        param.put(Dict.REST_CODE, "createMergeRequest");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK-APP 创建合并请求返回为空"});
        }
//        if(result instanceof String){
//            return CommonUtils.converMap(CommonUtils.filter(JSONObject.fromObject(result)));
//        }
        return (Map) result;
    }

    /**
     * 0 - 未发起合并 1 - 待合并 2 - 已合并，pipline进行中 3 - 已合并，pipline构建成功 4 - 已合并，pipline构建失败
     * 5 - 合并请求被关闭，可重新发起合并请求
     **/
    @Override
    public Map queryMergeInfo(Map param) throws Exception {
        Map mergeStatus = new HashMap();
        param.put(Dict.REST_CODE, "getMergeRequestInfo");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK-APP 获取merge信息返回为空"});
        }
        Map temp = (Map) result;
//        Map temp = CommonUtils.converMap(CommonUtils.filter(JSONObject.fromObject(result)));
        String status = (String) temp.get(Dict.STATE);// cannot_be_merged can_be_merged

        Object merged_by = temp.get(Dict.MERGED_BY);
        Object closed_by = temp.get(Dict.CLOSED_BY);
        Object pipeline = temp.get(Dict.PIPELINE);
        
/*        String descString=null;
        if(temp.get(Dict.DESCRIPTION)==null)
        {
        	descString="";
        }
        else{
        	descString=temp.get(Dict.DESCRIPTION).toString();
        } */

        // 待合并
        if ("opened".equals(status) && StringUtils.isEmpty(closed_by) && StringUtils.isEmpty(merged_by)) {
            mergeStatus.put(Dict.STATUS_CODE, "1");
            mergeStatus.put(Dict.STATUS_MEAN, "待合并");
        }
        // 合并请求被关闭，可重新发起合并请求
        if (Dict.CLOSED.equals(status) && !StringUtils.isEmpty(closed_by)
                && StringUtils.isEmpty(merged_by)) {
            mergeStatus.put(Dict.STATUS_CODE, "5");
            mergeStatus.put(Dict.STATUS_MEAN, "合并请求被关闭，可重新发起合并请求");
        }
        if (Dict.MERGED.equals(status)) {
            if (Dict.RUNNING.equals(pipeline)) {
                mergeStatus.put(Dict.STATUS_CODE, "2");
                mergeStatus.put(Dict.STATUS_MEAN, "已合并，pipline进行中");
            } else if (Dict.SUCCESS.equals(pipeline)) {
                mergeStatus.put(Dict.STATUS_CODE, "3");
                mergeStatus.put(Dict.STATUS_MEAN, "已合并，pipline构建成功");
            } else {
                mergeStatus.put(Dict.STATUS_CODE, "4");
                mergeStatus.put(Dict.STATUS_MEAN, "已合并，pipline构建失败");
            }
        }

        //mergeStatus.put(Dict.DESC_STRING, descString);

        return mergeStatus;
    }

    @Override
    public Map queryTestMergeInfo(Map param) throws Exception {
        Map mergeStatus = new HashMap();
        param.put(Dict.REST_CODE, "getMergeRequestInfo");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK-APP 获取merge信息返回为空"});
        }
        Map temp = (Map) result;
        String status = (String) temp.get(Dict.STATE);
        Object merged_by = temp.get(Dict.MERGED_BY);
        Object closed_by = temp.get(Dict.CLOSED_BY);

        // 待合并
        if ("opened".equals(status) && StringUtils.isEmpty(closed_by) && StringUtils.isEmpty(merged_by)) {
            mergeStatus.put(Dict.STATUS_CODE, "1");
            mergeStatus.put(Dict.STATUS_MEAN, "待合并");
        }
        // 合并请求被关闭，可重新发起合并请求
        if (Dict.CLOSED.equals(status) && !StringUtils.isEmpty(closed_by)
                && StringUtils.isEmpty(merged_by)) {
            mergeStatus.put(Dict.STATUS_CODE, "3");
            mergeStatus.put(Dict.STATUS_MEAN, "合并请求被关闭，可重新发起合并请求");
        }
        if (Dict.MERGED.equals(status)) {
            mergeStatus.put(Dict.STATUS_CODE, "2");
            mergeStatus.put(Dict.STATUS_MEAN, "已合并");
        }

        return mergeStatus;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ArrayList<Map> addMember(Map param) throws Exception {
        ArrayList names = (ArrayList) param.get(Dict.USER_NAME_EN);

        ArrayList paramNameList = new ArrayList();
        ArrayList userResultList = new ArrayList();

        Map maptmp = new HashMap(0);
        maptmp.put(Dict.IDS, names);
        userResultList = (ArrayList) userApi.queryUserList(maptmp);
        for (Object user : userResultList) {
            paramNameList.add(((Map) user).get(Dict.GITUSERID));
        }
        param.put(Dict.REST_CODE, "addMemberList");
        //坑    传的 用户 git id
        param.put(Dict.GITUSERID, paramNameList);
        ArrayList<Map> resultList = (ArrayList<Map>) restTransport.submit(param);
        if (!CommonUtils.isNullOrEmpty(resultList)) {
            for (int i = 0; i < resultList.size(); i++) {
                Map result = resultList.get(i);
                if (!CommonUtils.isNullOrEmpty(result) && !CommonUtils.isNullOrEmpty(result.get(Dict.REASON))) {
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]
                            {"git_name:" + result.get(Dict.NAME).toString() + " " + result.get(Dict.REASON).toString()});
                }
            }
        }
        return (ArrayList<Map>) resultList;
    }

    // 查询应用信息
    @Override
    public Map queryAppById(Map param) throws Exception {
        param.put(Dict.REST_CODE, "findbyid");
        Object result = restTransport.submit(param);
        return (Map) result;
    }

    @Override
    public List<String> querySitBranch(Map param) throws Exception {
        param.put(Dict.REST_CODE, "getBranchNameByAppId");
        return (List<String>) restTransport.submit(param);
    }

    @Override
    public Map update(Map map) throws Exception {
        map.put(Dict.REST_CODE, "updateApp");
        Object result = restTransport.submit(map);
        return (Map) result;
    }

    @Override
    public Map saveTask(Map param) throws Exception {
        param.put(Dict.REST_CODE, "save_task");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK 根据应用ID添加任务返回为空"});
        }
        return (Map) result;
    }

    @Override
    public void saveDoc(Map saveDocParam) throws Exception {
        try {
            saveDocParam.put(Dict.REST_CODE, "push_git");
            restTransport.submit(saveDocParam);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK-APP 上传文件失败"});
        }
    }

    /**
     * @param queryDoc
     * @return
     * @throws Exception 2019年4月2日
     * @Desc 查询任务相关的文档
     */
    @Override
    public ArrayList queryDoc(Map queryDoc) throws Exception {
        ArrayList result = new ArrayList();
        queryDoc.put(Dict.REST_CODE, "queryTaskFileList");
        Object list = restTransport.submit(queryDoc);
        if (CommonUtils.isNullOrEmpty(list)) {
            return new ArrayList();
        }
        ArrayList jlist = (ArrayList) list;
        for (Object li : jlist) {
            Iterator<String> it = ((Map) li).keySet().iterator();
            while (it.hasNext()) {
                Map temp = new HashMap();
                String key = it.next();
                String value = (String) ((Map) li).get(key);
                String type = (String) ((Map) li).get(Dict.NAME);
                if (Dict.NAME.equals(key)) {
                    continue;
                } else {
                    temp.put(Dict.TYPE, type);
                    temp.put(Dict.NAME, key);
                    temp.put(Dict.PATH, value);
                }
                result.add(temp);
            }
        }
        return result;
    }

    @Override
    public List queryAppByIds(List ids) throws Exception {
        Map param = new HashMap();
        param.put(Dict.TYPE, Dict.ID);
        param.put(Dict.IDS, ids);
        param.put(Dict.REST_CODE, "getAppByIdsOrGitlabIds");
        List result = null;
        try {
            result = (List) restTransport.submit(param);
        } catch (Exception e) {
            logger.error("查询应用失败：{}", e.getMessage());
        }
        return result;
    }

    @Override
    public Map queryAppByGitlabIds(String id) throws Exception {
        Map param = new HashMap();
        param.put(Dict.ID, id);
        param.put(Dict.REST_CODE, "getAppByGitId");
        Map result = null;
        try {
            result = (Map) restTransport.submit(param);
        } catch (Exception e) {
            logger.error("查询应用失败：{}", e.getMessage());
        }
        return result;
    }
    @Override
    public Map queryAppInfoByAppName(String nameEN){
        Map param = new HashMap();
        param.put(Dict.NAME_EN, nameEN);
        param.put(Dict.REST_CODE, "getAppInfo");
        List result = null;
        try {
            result = (List) restTransport.submit(param);
        } catch (Exception e) {
            logger.error("查询应用失败：{}", e.getMessage());
        }
        System.out.println(JSON.toJSONString(result,true));
        return (Map)result.get(0);
    }
    @Override
    public void uploadFilesByPaths(String task_id, String type, List<String> paths, User user) {
        Map param = new HashMap();
        param.put(Dict.TASK_ID, task_id);
        param.put(Dict.TYPE, type);
        param.put(Dict.PATHS, paths);
        param.put(Dict.USER, user);
        param.put(Dict.REST_CODE, "uploadFilesByPaths");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            logger.error("文件上传失败:{}", e.getMessage());
        }
    }

    @Override
    public Map queryAppTypeById(String type_id) {
        Map param = new HashMap();
        param.put(Dict.ID, type_id);
        param.put(Dict.REST_CODE, "findAppTypeById");
        Map result = null;
        try {
            result = (Map) restTransport.submit(param);
        } catch (Exception e) {
            logger.error("查询应用类型失败：{}", e.getMessage());
        }
        return result;
    }

    @Override
    public Map getSonarScanInfo(String projectId,String feature,String sonarId) {
        Map<String,String> param = new HashMap<>();
        param.put(Dict.ID, projectId);
        param.put("branch_name",feature);
        param.put("sonar_id", sonarId);
        param.put(Dict.REST_CODE, "getProjectBugs");
        Map result = new HashMap();
        try {
            result = (Map) restTransport.submit(param);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw,true));
            logger.info("调用应用模块失败！请求参数:{}Error Trace:{}",
                    JSON.toJSONString(param,true),
                    sw.toString());
            return Collections.EMPTY_MAP;
        }
        return result;
    }
    
    @Override
    public String QueryTestSwitch(String projectId) {
        //以前逻辑不判断projectId是否为null，调用应用模块后报错return 1,增加判空，直接return避免多余的调用和报错
        if(CommonUtils.isNullOrEmpty(projectId)) {
            logger.info("====QueryTestSwitch projectId is null====");
            return "1";
        }
        Map<String,String> param = new HashMap<>();
        param.put(Dict.ID, projectId);
        param.put(Dict.REST_CODE, "getTestFlag");
        Map result = new HashMap();
        try {
            result = (Map) restTransport.submit(param);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw,true));
            logger.info("调用应用模块查询是否涉及内测失败！请求参数:{}Error Trace:{}",
                    JSON.toJSONString(param,true),
                    sw.toString());
            return "1";
        }
        return CommonUtils.isNullOrEmpty(result)?"1":
                CommonUtils.isNullOrEmpty(result.get("isTest"))?"1":(String)result.get("isTest");
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "ftask.appInfo.{appId}")
    public Map queryAppByIdCache(String appId) throws Exception {
        Map param = new HashMap();
        param.put(Dict.ID, appId);
        param.put(Dict.REST_CODE, "findbyid");
        return (Map) restTransport.submit(param);
    }
}
