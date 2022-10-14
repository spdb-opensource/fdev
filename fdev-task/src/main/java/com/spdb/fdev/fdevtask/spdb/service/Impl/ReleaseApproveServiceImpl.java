package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.AsyncService;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.MailUtil;
import com.spdb.fdev.fdevtask.base.utils.UserUtils;
import com.spdb.fdev.fdevtask.spdb.dao.IFdevTaskDao;
import com.spdb.fdev.fdevtask.spdb.dao.IReleaseApproveDao;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.ReleaseApprove;
import com.spdb.fdev.fdevtask.spdb.service.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author liux81
 * @DATE 2021/12/30
 */
@Service
@RefreshScope
public class ReleaseApproveServiceImpl implements IReleaseApproveService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    IFdevTaskDao fdevTaskDao;

    @Autowired
    UserVerifyUtil userVerifyUtil;

    @Autowired
    IFdevTaskService fdevTaskService;

    @Autowired
    UserUtils userUtils;
    
    @Autowired
    IUserApi userApi;

    @Autowired
    MailUtil mailUtil;

    @Autowired
    AsyncService asyncService;

    @Value("${task.release.merge.auditor.email}")
    private List<String> auditorEmailList;//审核人邮箱集合

    @Value("${release.merge.audit.href}")
    private String releaseApplyHref;//代码合并审核页面路径

    @Value("${release.merge.team}")
    private List<String> releaseMergeTeam;//须走release合并审核流程的三级组

    @Autowired
    private IReleaseTaskApi ReleaseTaskApi;
    @Autowired
    private GitApiService gitApiService;

    @Autowired
    private IReleaseApproveDao releaseApproveDao;

    @Override
    public void add(Map param) throws Exception {
        String task_id = (String) param.get(Dict.TASKID);
        String env = (String) param.get(Dict.ENV);
        //环境为空 默认uat
        if(CommonUtils.isNullOrEmpty(env)){
            env = "uat" ;
        }
        FdevTask fdevTask = fdevTaskDao.queryById(task_id);
        //uat才判断
        if("uat".equals(env)){
            //校验是否需要审核
            releaseMergeFlag(fdevTask);
            //控制一个任务只能有一条待审核的记录
            Map paramMap = new HashMap();
            paramMap.put(Dict.TASKID,task_id);
            List<Integer> status = new ArrayList<>();
            status.add(0);
            paramMap.put(Dict.STATUS,status);
            paramMap.put(Dict.ENV,env);
            Map<String, Object> releaseApproveList = releaseApproveDao.queryReleaseApproveList(paramMap);
            if(!CommonUtils.isNullOrEmpty(releaseApproveList.get("list"))){
                throw new FdevException("存在待审核的申请，请联系审核人处理后再申请合并！");
            }
            //控制git上有合并没点时，不可再申请
            Map gitParam = new HashMap();
            gitParam.put(Dict.ID,task_id);
            gitParam.put(Dict.TYPE, env );
            Map<String, Object> mergeInfo = gitApiService.getMergeRequestInfo(gitParam);
            if( !CommonUtils.isNullOrEmpty(mergeInfo) && ("1".equals(mergeInfo.get(Dict.STATUS_CODE)) || "2".equals(mergeInfo.get(Dict.STATUS_CODE)))){
                throw new FdevException("申请合并失败！存在未合并的请求，请到gitlab处理后再申请合并");
            }
        }
        String target_branch = "" ;
        if( "uat".equals(env) ){
            Map releaseInfo = ReleaseTaskApi.queryDetailByTaskId(task_id, CommonUtils.getReleaseType(fdevTask.getApplicationType()));
            Map releaseApp = (Map) releaseInfo.get(Dict.RELEASE_APPLICATION);//查询release分支名称
            target_branch = (String) releaseApp.get(Dict.RELEASE_BRANCH);
        }else if ( "sit".equals(env) ){
            target_branch = Constants.BRANCH_SIT ;
        }
        //执行新增申请

        User user = userVerifyUtil.getRedisUser();
        ReleaseApprove releaseApprove = new ReleaseApprove();
        releaseApprove.setTask_id(task_id);
        releaseApprove.setApplicant(user.getId());//申请人
        releaseApprove.setApply_desc((String) param.get(Dict.APPLYDESC));//申请描述
        releaseApprove.setTarget_branch( target_branch );//目标分支
        releaseApprove.setSource_branch(fdevTask.getFeature_branch());//源分支
        releaseApprove.setMerge_reason((String) param.get(Dict.MERGE_REASON));//合并原因
        releaseApprove.setEnv( env );//环境
        releaseApprove.setStatus(0);//初始状态待审核
        releaseApproveDao.save(releaseApprove);
        if( "uat".equals(env) ){
            //给审核人发送邮件提醒
            HashMap model = new HashMap();
            model.put(Dict.APPLICANT, user.getUser_name_cn());
            model.put(Dict.TASKNAME, fdevTask.getName());
            if(!CommonUtils.isNullOrEmpty(releaseApprove.getApply_desc())){
                model.put(Dict.DESC,",申请描述为：" + releaseApprove.getApply_desc());
            }else{
                model.put(Dict.DESC,"");
            }
            model.put(Dict.MERGE_REASON,getMergeReasonName( releaseApprove.getMerge_reason() ) );
            model.put(Dict.LINK, releaseApplyHref);
            mailUtil.sendTaskMail("email.task.release.merge.apply",model,fdevTask.getName(),auditorEmailList.toArray(new String[auditorEmailList.size()]));
        }
    }

    /**
     * 判断任务是否要走release合并审核流程，卡点条件为：
     * 1、不是首次合并release；2、任务所属小组的三级组为武汉研发A1-A6
     * @param fdevTask
     * @return
     */
    public void releaseMergeFlag(FdevTask fdevTask) throws Exception {
        String group = fdevTask.getGroup();
        Map threeLevelGroup = userUtils.getThreeLevelGroup(group);
        if( !releaseMergeTeam.contains(threeLevelGroup.get(Dict.ID))
            || !(releaseApproveDao.getCountByTaskId(fdevTask.getId()) > 0) ) {
            throw new FdevException("请刷新后重试!");
        }
    }


    /**
     * 1、数据控制仅“投产小组人员（配置文件中取）”可见可操作；
     * 2、返回结果按照申请时间倒序；
     * 3、可根据审核状态(多选)、任务所属小组（多选）、申请人（多选）、任务名称（模糊搜索）、任务id（单选精准匹配）分页查询
     * @param param
     * @return
     */
    @Override
    public Map<String, Object> releaseApproveList(Map param) throws Exception {
        User user = userVerifyUtil.getRedisUser();
        Map result = new HashMap();
        String env = (String) param.get(Dict.ENV);//环境
        if( !CommonUtils.isNullOrEmpty(param.get(Dict.END_DATE))){
            param.put(Dict.END_DATE,CommonUtils.yesterday(CommonUtils.parseDateStr((String)param.get(Dict.END_DATE)),1));
        }

        if("uat".equals(env) || CommonUtils.isNullOrEmpty(env)){
            //数据控制仅“投产小组人员（配置文件中取）”可见。从任务页面进去，任何人可查看
            if(!auditorEmailList.contains(user.getEmail()) && (CommonUtils.isNullOrEmpty(param.get("requestSource")) || !"1".equals(param.get("requestSource")))){
                return result;
            }
            param.put(Dict.ENV,"uat");
        }

        Map<String, Object> data = releaseApproveDao.queryReleaseApproveList(param);
        long count = (long)data.get(Dict.COUNT);
        List<Map> list = (List<Map>) data.get("list");
        List<Map> resultList = new ArrayList<>();

        Set<String> userIds = new HashSet<>();
        Set<String> groupIds = new HashSet<>();
        //收集所有的人员id，小组id
        for(Map map : list){
            userIds.add((String) map.get(Dict.APPLICANT));
            if(!CommonUtils.isNullOrEmpty(map.get(Dict.AUDITOR))){
                userIds.add((String) map.get(Dict.AUDITOR));
            }
            FdevTask taskInfo = (FdevTask)map.get("taskInfo");
            if(!CommonUtils.isNullOrEmpty(taskInfo.getDeveloper())){
                userIds.addAll(Arrays.asList(taskInfo.getDeveloper()));
            }
            groupIds.add(taskInfo.getGroup());
        }
        //批量查询人员信息和小组信息
        Map usersInfo = userUtils.getUserByIds(userIds);
        Map groupNames = userApi.getGroupsNameByIds(groupIds);
        //封装返回信息
        for(Map map : list){
            Map temp = new HashMap();
            FdevTask taskInfo = (FdevTask)map.get("taskInfo");
            temp.put(Dict.ID, map.get(Dict.ID));
            temp.put(Dict.TASK_ID, taskInfo.getId());//任务id
            temp.put(Dict.TASK_NAME,taskInfo.getName());//任务名称
            temp.put(Dict.PROJECT_ID, taskInfo.getProject_id());//所属应用id
            temp.put(Dict.PROJECT_NAME, taskInfo.getProject_name());//所属应用名称
            temp.put(Dict.APPLICATIONTYPE, taskInfo.getApplicationType());//应用类型
            temp.put(Dict.GROUP, taskInfo.getGroup());//任务所属组id
            //封装小组中文名
            temp.put(Dict.GROUP_NAME,groupNames.get(taskInfo.getGroup()));
            temp.put(Dict.STAGE, taskInfo.getStage());//任务阶段
            temp.put(Dict.SOURCE_BRANCH, map.get(Dict.SOURCE_BRANCH));//源分支
            temp.put(Dict.TARGET_BRANCH, map.get(Dict.TARGET_BRANCH));//目标分支
            temp.put(Dict.DEVELOPER, taskInfo.getDeveloper());//开发人员id
            //封装开发人员信息
            List<Map<String, String>> developerInfos = new ArrayList<>();
            for(String developerId : taskInfo.getDeveloper()){
                Map devInfo = new HashMap();
                devInfo.put(Dict.ID, developerId);
                devInfo.put(Dict.NAME, ((Map)usersInfo.get(developerId)).get(Dict.USER_NAME_CN));
                developerInfos.add(devInfo);
            }
            temp.put(Dict.DEVELOPER_NAME, developerInfos);
            temp.put(Dict.APPLICANT, map.get(Dict.APPLICANT));//申请人id
            //封装申请人姓名
            temp.put(Dict.APPLICANT_NAME,((Map)usersInfo.get(map.get(Dict.APPLICANT))).get(Dict.USER_NAME_CN));
            temp.put(Dict.AUDITOR, map.get(Dict.AUDITOR));//审核人id
            //封装审核人姓名
            if(!CommonUtils.isNullOrEmpty(map.get(Dict.AUDITOR))){
                temp.put(Dict.AUDITOR_NAME, ((Map) usersInfo.get(map.get(Dict.AUDITOR))).get(Dict.USER_NAME_CN));
            }
            temp.put("apply_time", map.get("apply_time"));//申请时间
            temp.put("audit_time", map.get("audit_time"));//审核时间
            temp.put("merge_reason", map.get("merge_reason"));//合并原因
            temp.put("apply_desc", map.get("apply_desc"));//申请描述
            if(!CommonUtils.isNullOrEmpty(map.get("result_desc"))){
                temp.put("result_desc", map.get("result_desc"));//结果描述
            }
            temp.put(Dict.STATUS, map.get(Dict.STATUS));//审核状态：0待审批、1通过、2拒绝

            resultList.add(temp);
        }
        List wait = new ArrayList();
        wait.add(0);
        result.put("wait_count", releaseApproveDao.count(wait));
        List finish = new ArrayList();
        finish.add(1);
        finish.add(2);
        result.put("finish_count",releaseApproveDao.count(finish));
        result.put(Dict.COUNT, count);
        result.put("list", resultList);
        return result;
    }

    @Override
    public void exportApproveList(Map<String, Object> param, HttpServletResponse resp) throws Exception {
        param.put(Dict.CURRENTPAGE,0);//不分页
        param.put(Dict.PAGESIZE,0);//不分页
        List<Map>  resultList = (List<Map>)releaseApproveList(param).get("list");
        String env = (String) param.get(Dict.ENV);//环境
        // 初始化workbook
        InputStream inputStream = null;
        XSSFWorkbook workbook = null;
        XSSFSheet sheet = null;
        //引入模板
        try {
            ClassPathResource classPathResource = null ;
            if("uat".equals(env) || CommonUtils.isNullOrEmpty(env)){
                classPathResource = new ClassPathResource("UatMergeListExport.xlsx");
                env = "uat";
            } else {
                classPathResource = new ClassPathResource("SitMergeListExport.xlsx");
            }

            inputStream = classPathResource.getInputStream();
            workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);
        } catch (Exception e1) {
            throw new FdevException("任务合并列表导出失败，请联系fdev管理员");
        }
        if(!CommonUtils.isNullOrEmpty(resultList)){
            int i=1;//行数
            for (Map map : resultList) {
                int j=0;//列数
                sheet.createRow(i);
                sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.TASK_NAME));//任务名称
                sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.PROJECT_NAME));//所属应用
                sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.GROUP_NAME));//所属小组
                String developerNames = "";
                if(!CommonUtils.isNullOrEmpty(map.get(Dict.DEVELOPER_NAME))){
                    List<Map<String, String>> developerInfos = (List<Map<String, String>>) map.get(Dict.DEVELOPER_NAME);
                    for (Map<String, String> developerInfo : developerInfos) {
                        if(CommonUtils.isNullOrEmpty(developerNames)){
                            developerNames = developerInfo.get(Dict.NAME);
                        }else {
                            developerNames += "," + developerInfo.get(Dict.NAME);
                        }

                    }
                }
                sheet.getRow(i).createCell(j++).setCellValue(developerNames);//开发人员
                sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.STAGE));//任务阶段
                sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.SOURCE_BRANCH));//源分支
                String merge_reason = (String) map.get(Dict.MERGE_REASON);
                String mergeReasonName = getMergeReasonName( merge_reason ) ;
                if( "uat".equals(env) ){
                    sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.TARGET_BRANCH));//目标分支
                    sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.APPLICANT_NAME));//申请人
                    sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.APPLY_TIME));//申请时间
                    sheet.getRow(i).createCell(j++).setCellValue(mergeReasonName);//合并原因
                    sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.APPLY_DESC));//合并说明
                    sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.AUDITOR_NAME));//审批人
                    sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.AUDIT_TIME));//审批人
                    sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.RESULT_DESC));//审批说明
                    Integer status = (Integer) map.get(Dict.STATUS);
                    String statusName = getStatus( status ) ;
                    sheet.getRow(i).createCell(j++).setCellValue(statusName);//审批结果
                }else{
                    sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.APPLICANT_NAME));//提交人
                    sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.APPLY_TIME));//提交时间
                    sheet.getRow(i).createCell(j++).setCellValue(mergeReasonName);//合并原因
                    sheet.getRow(i).createCell(j++).setCellValue((String)map.get(Dict.APPLY_DESC));//合并说明
                }
                i++;
            }
        }
        try {
            resp.reset(); resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            if( "uat".equals(env) ){
                resp.setHeader("Content-Disposition", "attachment;filename=" +
                        "MergeUatListReport"+ ".xlsx");
            }else{
                resp.setHeader("Content-Disposition", "attachment;filename=" +
                        "MergeSitListReport"+ ".xlsx");
            }

            workbook.write(resp.getOutputStream());
        } catch (IOException e) {
            throw new FdevException("任务合并列表导出失败，请联系fdev管理员");
        }

    }
    //审核状态：0待审批、1通过、2拒绝
    public String getStatus(Integer status){
        switch (status){
            case 1:
                return "通过";
            case 2:
                return "拒绝";
            default:
                return "待审批";
        }
    }
    /*
    需求问题 demandIssue
    增删注释 annotation
    修改代码冲突 updateClash
    代码审核修改 codeCheckUpdate
    兼容性修复 compatibilityRepair
    环境问题 envIssue
    改挂投产窗口 updateProWindow
    其他  other
    */
    public String getMergeReasonName(String merge_reason){
        switch ( merge_reason ){
            case "demandIssue":
                return "需求功能问题";
            case "annotation":
                return "增删注释";
            case "updateClash":
                return "修改代码冲突";
            case "codeCheckUpdate":
                return "代码审核修改";
            case "compatibilityRepair":
                return "兼容性修复";
            case "envIssue":
                return "环境问题";
            case "updateProWindow":
                return "改挂投产窗口";
            default:
                return "其他";
        }
    }


    /**
     * 1、操作权限，仅配置人员可操作
     * 2、通过后，发送合并release请求
     * @param param
     */
    @Override
    public void pass(Map param) throws Exception {
        String id = (String) param.get(Dict.ID);
        String resultDesc = "";
        if(!CommonUtils.isNullOrEmpty(param.get("resultDesc"))){
            resultDesc = (String) param.get("resultDesc");
        }
        User user = userVerifyUtil.getRedisUser();
        if(!auditorEmailList.contains(user.getEmail())){
            throw new FdevException("当前人员权限不足！");
        }
        ReleaseApprove releaseApprove = releaseApproveDao.queryById(id);
        String taskId = releaseApprove.getTask_id();
        //查询任务
        FdevTask fdevTask = fdevTaskDao.queryById(taskId);
        // 查询 uat 分支
        Map appParam = new HashMap();
        Map releaseInfo = ReleaseTaskApi.queryDetailByTaskId(taskId, CommonUtils.getReleaseType(fdevTask.getApplicationType()));
        Map releaseApp = (Map) releaseInfo.get(Dict.RELEASE_APPLICATION);
        appParam.put(Dict.ID, releaseApp.get(Dict.APPLICATION_ID));
        if (!releaseApp.containsKey(Dict.RELEASE_BRANCH)
                || CommonUtils.isNullOrEmpty((String) releaseApp.get(Dict.RELEASE_BRANCH))) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK->RELEASE 查询该任务UAT分支返回为空"});
        }
        //创建合并请求,合并请求创建者应该为合并申请人
        String applicant = releaseApprove.getApplicant();
        Map userParam = new HashMap();
        userParam.put(Dict.ID,applicant);
        Map userMap = userApi.queryUser(userParam);
        FdevTask taskNew = fdevTaskService.createReleaseMergeRequest(fdevTask,userMap,releaseApp);
        //给开发人员和任务负责人发送邮件提醒合并请求
        sendEmailByPass(taskNew, user.getUser_name_cn());
        //结果记录
        releaseApprove.setStatus(1);
        releaseApprove.setResult_desc(resultDesc);//审核结果描述
        releaseApprove.setAuditor(user.getId());//审核人
        releaseApprove.setAudit_time(CommonUtils.dateFormat(new Date(),CommonUtils.DATE_TIME_PATTERN));//审核时间
        releaseApproveDao.update(releaseApprove);
    }

    @Async
    public void sendEmailByPass(FdevTask taskNew, String auditorName) throws Exception {
        Set<String> person = new HashSet<>();//收件人列表
        person.addAll(Arrays.asList(taskNew.getDeveloper()));
        person.addAll(Arrays.asList(taskNew.getMaster()));
        person.addAll(Arrays.asList(taskNew.getSpdb_master()));
        //查询收件人邮箱
        Map<String, Map> userInfoMap = userUtils.getUserByIds(person);
        List<String> toUserEmail = userInfoMap.values().stream().map(userInfo -> (String)userInfo.get(Dict.EMAIL)).collect(Collectors.toList());
        //查询应用gitlab地址
        Map<String,Object> projectInfo = fdevTaskService.getProjectInfo(taskNew.getProject_id(), taskNew.getApplicationType());
        String gitlabUrl = String.valueOf(projectInfo.get(Dict.GITLABURL));
        gitlabUrl = gitlabUrl.substring(0, gitlabUrl.length()-4) + "/-/merge_requests/" + taskNew.getUat_merge_id();
        HashMap model = new HashMap();
        model.put(Dict.AUDITOR, auditorName);
        model.put(Dict.TASKNAME, taskNew.getName());
        model.put(Dict.URL, gitlabUrl);
        try {
            mailUtil.sendTaskMail("email.task.release.merge.pass", model, taskNew.getName(), toUserEmail.toArray(new String[toUserEmail.size()]));
        } catch (Exception e) {
            logger.info(">>>sendEmailByPass fail,{}", e.getMessage());
        }
    }
    /**
     * 审批拒绝
     * 1、仅配置人员可操作
     * 2、记录审核表
     * @param param
     */
    @Override
    public void refuse(Map param) throws Exception {
        String id = (String) param.get(Dict.ID);
        String resultDesc = "";
        if(!CommonUtils.isNullOrEmpty(param.get("resultDesc"))){
            resultDesc = (String) param.get("resultDesc");
        }
        User user = userVerifyUtil.getRedisUser();
        if(!auditorEmailList.contains(user.getEmail())){
            throw new FdevException("当前人员权限不足！");
        }
        ReleaseApprove releaseApprove = releaseApproveDao.queryById(id);
        //结果记录
        releaseApprove.setStatus(2);
        releaseApprove.setResult_desc(resultDesc);//审核结果描述
        releaseApprove.setAuditor(user.getId());//审核人
        releaseApprove.setAudit_time(CommonUtils.dateFormat(new Date(),CommonUtils.DATE_TIME_PATTERN));//审核时间
        releaseApproveDao.update(releaseApprove);
        //给申请人发送拒绝的邮件通知
        FdevTask fdevTask = fdevTaskDao.queryById(releaseApprove.getTask_id());
        HashMap model = new HashMap();
        model.put("auditor", user.getUser_name_cn());
        model.put("taskName", fdevTask.getName());
        model.put("desc", resultDesc);
        model.put("link", releaseApplyHref);
        asyncService.sendFastMail(model,new String[]{releaseApprove.getApplicant()},"email.task.release.merge.refuse",fdevTask.getName());
    }
}
