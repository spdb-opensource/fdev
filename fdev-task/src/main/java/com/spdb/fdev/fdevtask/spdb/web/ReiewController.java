package com.spdb.fdev.fdevtask.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.GitHttpServer;
import com.spdb.fdev.fdevtask.base.utils.FdevTaskUtils;
import com.spdb.fdev.fdevtask.spdb.dao.ReviewRecordDao;
import com.spdb.fdev.fdevtask.spdb.entity.*;
import com.spdb.fdev.fdevtask.spdb.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLEncoder;
import java.util.*;

@RestController
@RefreshScope
@RequestMapping("/api/task/review")
public class ReiewController {

    @Autowired
    private GitHttpServer gitHttpServer;
    @Autowired
    private IFdevTaskService iFdevTaskService;
    @Autowired
    private IAppApi appApi;
    @Autowired
    private IUserApi userApi;
    @Autowired
    private ReviewRecordService reviewRecordService;

    @Autowired
    private INotifyApi iNotifyApi;
    @Autowired
    private ReviewRecordDao reviewRecordDao;
    @Value("${isSendReviewEmail}")
    private boolean issendmail;

    @Value("${review.href}")
    private String href;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Autowired
    private FdevTaskUtils fdevTaskUtils;

    @Autowired
    private IReleaseTaskApi ReleaseTaskApi;

    @RequestValidate
    @RequestMapping(value = "/deleteFiles", method = RequestMethod.POST)
    public JsonResult deleteFile(@RequestBody Map request) throws Exception {
        String id = (String) request.get("id");
        String type = (String) request.get("type");
        List files = (List) request.get("files");
        List action = new ArrayList();
        List action1 = new ArrayList();
        List action2 = new ArrayList();
        try {
            FdevTask task = iFdevTaskService.queryTaskAll(new FdevTask.FdevTaskBuilder().id(id).init());
            Map group = userApi.queryGroup(new HashMap() {{
                put(Dict.ID, task.getGroup());
            }});
            if (CommonUtils.isNullOrEmpty(group)) {

            }
            String groupName = (String) group.get(Dict.NAME);
            StringBuilder filePatch = new StringBuilder(groupName);
            StringBuilder filePatch1 = new StringBuilder(groupName);
            StringBuilder filePatch2 = new StringBuilder(groupName);
            String encode = URLEncoder.encode(task.getName(), "utf-8");
            String fileName = encode.replaceAll("%", "a");
            String fileName1 = "";
            if (fileName.length() > 10) {
                fileName1 = fileName.substring(0, 10);
            }
            filePatch.append("/").append(task.getName()).append("-").append(task.getId()).append("/").append(CommonUtils.typeMapping(type)).append("/");
            filePatch1.append("/").append(fileName).append("-").append(task.getId()).append("/").append(CommonUtils.typeMapping(type)).append("/");

            files.forEach(file -> action.add(new HashMap() {{
                put("action", "delete");
                put("file_path", filePatch.append(file));
            }}));
            gitHttpServer.deleteFile(action);
            files.forEach(file -> action1.add(new HashMap() {{
                put("action", "delete");
                put("file_path", filePatch1.append(file));
            }}));
            gitHttpServer.deleteFile(action1);
            if (CommonUtils.isNotNullOrEmpty(fileName1)) {
                filePatch2.append("/").append(fileName1).append("-").append(task.getId()).append("/").append(CommonUtils.typeMapping(type)).append("/");
                files.forEach(file -> action2.add(new HashMap() {{
                    put("action", "delete");
                    put("file_path", filePatch2.append(file));
                }}));
                gitHttpServer.deleteFile(action2);
            }

        } catch (Exception e) {
            throw new FdevException(ErrorConstants.DELETE_FILE_ERROR);
        }
        return JsonResultUtil.buildSuccess();
    }


    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/createFirstReview", method = RequestMethod.POST)
    public JsonResult createFirstReview(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        String id = (String) request.get(Dict.ID);
        //校验任务是否有“审核类-数据库审核材料”
        boolean checkout = false;
        FdevTask task1 = new FdevTask();
        task1.setId(id);
        Map map2 = iFdevTaskService.queryDocDetail(task1,null,null);
        if (map2 != null) {
            List<Map> doc = (List<Map>) map2.get(Dict.DOC);
            if (doc != null && doc.size() >= 1) {
                for (Map map : doc) {
                    if ("审核类-数据库审核材料".equals(map.get(Dict.TYPE))) {
                        checkout = true;
                    }
                }
            }
        }
        if (!checkout) {
            throw new FdevException(ErrorConstants.Review_Fail, new String[]{"请上传数据库审核材料至“审核类/数据库审核材料”目录下"});
        }
        String init_auditor_id = (String) request.get("init_auditor_id");
        List<Map> jobUser = userApi.getJobUser();
        List<String> jobUser_ids = new ArrayList<>();
        if (jobUser != null && jobUser.size() >= 1) {
            for (Map map : jobUser) {
                jobUser_ids.add((String) map.get(Dict.ID));
            }
        }
        if (jobUser_ids.size() >= 1 && jobUser_ids.contains(init_auditor_id)) {
//            throw new FdevException(ErrorConstants.REVIEW_ERROR);
        }
        Map map = new HashMap();
        map.put(Dict.ID, init_auditor_id);
        Map map1 = userApi.queryUser(map);
        ReviewUser reviewUser = new ReviewUser();
        reviewUser.setcid(init_auditor_id);
        reviewUser.setName((String) map1.get(Dict.USER_NAME_CN));
        List<ReviewUser> list1 = new ArrayList<>();
        list1.add(reviewUser);
        Object result = null;
        try {
            List<FdevTask> list = iFdevTaskService.query(new FdevTask.FdevTaskBuilder().id(id).init());
            if (!CommonUtils.isNullOrEmpty(list)) {
                FdevTask task = list.get(0);
                TaskReview review = task.getReview();
                TaskReviewChild[] sqlReviews = review.getData_base_alter();
                for (int i = 0; i < sqlReviews.length; i++) {
                    TaskReviewChild sql = sqlReviews[i];
                    ReviewRecord rr = reviewRecordDao.queryById(sql.getId());
                    rr.setReviewStatus(Dict.FIRST_REVIEW);
                    rr.setApplicant(user.getId());
                    rr.setApplicantName(user.getUser_name_cn());
                    rr.setReviewers(list1);
                    result = reviewRecordService.update(rr);
                }
            }
        } catch (FdevException e) {
            throw e;
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.FIRST_EVIEW_ERROR);
        }
        return JsonResultUtil.buildSuccess(result);
    }


    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/queryTaskReview", method = RequestMethod.POST)
    public JsonResult queryTaskReview(@RequestBody Map request) {
        String id = (String) request.get(Dict.ID);
        return JsonResultUtil.buildSuccess(reviewRecordService.queryTaskReview(id));
    }


    @RequestValidate(NotEmptyFields = {"page", "pageSize"})
    @RequestMapping(value = "/fuzzyQueryReviewRecord", method = RequestMethod.POST)
    public JsonResult fuzzyQueryReviewRecord(@RequestBody Map request) {
        //key applicant
        String key = (String) request.remove("key");
        int page = (int) request.remove("page");
        int pageSize = (int) request.remove("pageSize");
        return JsonResultUtil.buildSuccess(reviewRecordService.fuzzyQuery(key, page, pageSize, request));
    }

    @RequestValidate(NotEmptyFields = {Dict.KEY, "page", "pageSize"})
    @RequestMapping(value = "/queryReviewRecord", method = RequestMethod.POST)
    public JsonResult queryReviewRecord(@RequestBody Map request) {
        //key applicant
        int page = (int) request.remove("page");
        int pageSize = (int) request.remove("pageSize");
        return JsonResultUtil.buildSuccess(reviewRecordService.queryPageable(request, page, pageSize));
    }

    @RequestValidate(NotEmptyFields = {Dict.ID, "reviewStatus"})
    @RequestMapping(value = "/updateTaskReviewStatus", method = RequestMethod.POST)
    public JsonResult updateTaskReviewStatus(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        String newReviewStatus = (String) request.get("reviewStatus");
        String reviewIdea = (String) request.get("reviewIdea");
        String id = (String) request.get("id");
        Map mail = new HashMap();
        try {
            FdevTask task = iFdevTaskService.queryTaskById(id);
            if (!CommonUtils.isNullOrEmpty(task)) {
                String groupId = task.getGroup();
                TaskReview review = task.getReview();
                TaskReviewChild[] sqlReviews = review.getData_base_alter();
                List<TaskReviewChild> newSqlReviews = new ArrayList<>();
                for (int i = 0; i < sqlReviews.length; i++) {
                    TaskReviewChild sql = sqlReviews[i];
                    ReviewRecord rr = reviewRecordDao.queryById(sql.getId());
                    String oldReviewStatus = rr.getReviewStatus();
                    String reason = (String) request.get(Dict.REASON);
                    if (!CommonUtils.isNullOrEmpty(reason))
                        mail.put(Dict.REASON, reason);
                    rr.setReason(reason);
                    if (CommonUtils.isNotNullOrEmpty(reviewIdea)) {
                        rr.setReviewIdea(reviewIdea);
                    }
                    if (!newReviewStatus.equals(oldReviewStatus)) {
                        String reviewerId = user.getId();
                        String reviewerName = user.getUser_name_cn();
//                        存储初审复审人，，用于判断审核人不能重复
                        if (Dict.SECOND_REVIEW.equals(newReviewStatus) || Dict.FIRST_REFUSE.equals(newReviewStatus)) {
                            rr.setFirstReviewer(reviewerId + ";" + reviewerName);
                        }
                        if ((Dict.APPROVE.equals(newReviewStatus) || Dict.SECOND_REFUSE.equals(newReviewStatus))) {
                            rr.setSecondReviewer(reviewerId + ";" + reviewerName);
                        }
                        rr = rr.pushReviewer(reviewerName, reviewerId);
                    }

                    rr.setReviewStatus(newReviewStatus);
                    if (newReviewStatus.equals("通过")) {
                        sql.setAudit(true);
                    }
                    newSqlReviews.add(sql);
                    mail.put("reviewStatus", oldReviewStatus + "--->" + newReviewStatus);
                    reviewRecordService.update(rr);
                    if (issendmail) {
                        if (newReviewStatus.contains("拒绝") || newReviewStatus.contains(Dict.SECOND_REVIEW) || "通过".equals(newReviewStatus)) {
                            reviewRecordService.sendUpdateMail(mail, rr);
                        }
                    }
                    if (Dict.FIRST_REFUSE.equals(newReviewStatus) || Dict.SECOND_REFUSE.equals(newReviewStatus)) {
                        String content = "您的审核项状态有变化，请查看";
                        //若初审拒绝，通知发起审核人
                        ReviewRecord reviewRecord = new ReviewRecord();
                        reviewRecord.setTaskId(task.getId());
                        List<ReviewRecord> query = reviewRecordDao.query(reviewRecord);
                        if (query != null && query.size() >= 1) {
                            reviewRecord = query.get(0);
                        }
                        String applicant = reviewRecord.getApplicant();
                        Map user1 = new HashMap();
                        user1.put(Dict.ID, applicant);
                        Map map1 = userApi.queryUser(user1);
                        ArrayList<String> list2 = new ArrayList<>();
                        list2.add((String) map1.get(Dict.USER_NAME_EN));
                        iNotifyApi.sendUserNotify(content, list2, "0", href + "?groupid=" + groupId, "数据库审核");
                    }
                    if (Dict.SECOND_REVIEW.equals(newReviewStatus)) {
                        String content = "数据库变更请前往审核";
                        List<Map> jobUser = userApi.getJobUser();
                        ArrayList<String> names = new ArrayList<>();
                        for (Map map : jobUser) {
                            names.add((String) map.get(Dict.USER_NAME_EN));
                        }
                        iNotifyApi.sendUserNotify(content, names, "0", href + "?groupid=" + groupId, "数据库审核");
                    }
                }
                TaskReview review1 = task.getReview();
                TaskReviewChild[] data_base_alter = review1.getData_base_alter();
                TaskReviewChild[] newData_base_alter = new TaskReviewChild[data_base_alter.length];
                for (int i = 0; i < data_base_alter.length; i++) {
                    if ("通过".equals(newReviewStatus)) {
                        data_base_alter[i].setAudit(true);
                    }
                    newData_base_alter[i] = data_base_alter[i];
                }
                review1.setData_base_alter(newData_base_alter);
                iFdevTaskService.updateReview(task);
                //调用投产模块的接口，存储数据库审核文件
                if ("通过".equals(newReviewStatus))ReleaseTaskApi.dbReviewUpload(id);
            }
        } catch (FdevException e) {
            throw e;
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.UPDATE_REVIEW_ERROR);
        }
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.TASK_ID})
    @RequestMapping(value = "/queryReviewRecordHistory", method = RequestMethod.POST)
    public JsonResult queryReviewRecordHistory(@RequestBody Map request) {
        String task_id = (String) request.get(Dict.TASK_ID);
        if (CommonUtils.isNullOrEmpty(task_id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"任务id不能为空"});
        }
        return JsonResultUtil.buildSuccess(this.reviewRecordService.queryReviewRecordHistoryByTaskId(task_id));
    }

    @RequestValidate(NotEmptyFields = {Dict.TASK_ID, Dict.DOC, Dict.REVIEW_STATUS})
    @RequestMapping(value = "/saveReviewRecord", method = RequestMethod.POST)
    public JsonResult saveReviewRecord(@RequestBody Map request) throws Exception {

        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }

        request.put(Dict.USER_ID, user.getId());
        this.reviewRecordService.saveReviewRecord(request);
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {"taskId"})
    @RequestMapping(value = "/queryReviewBasicMsg", method = RequestMethod.POST)
    public JsonResult queryReviewBasicMsg(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        return JsonResultUtil.buildSuccess(this.reviewRecordService.queryReviewBasicMsgById(request));
    }

    @RequestValidate(NotEmptyFields = {Dict.TASK_ID, Dict.DOC, Dict.REVIEW_STATUS})
    @RequestMapping(value = "/addReviewIdea", method = RequestMethod.POST)
    public JsonResult addReviewIdea(@RequestBody Map request) throws Exception {
        this.reviewRecordService.addReviewIdea(request);
        return JsonResultUtil.buildSuccess(null);
    }

    @RequestMapping(value = "/addReview", method = RequestMethod.POST)
    public JsonResult addReview3(@RequestBody Map params) throws Exception {

        this.reviewRecordService.addNoCodeReview(params);
        return JsonResultUtil.buildSuccess();
    }
}
