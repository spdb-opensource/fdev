package com.manager.ftms.service.impl;

import com.manager.ftms.controller.TestCaseController;
import com.manager.ftms.dao.TestcaseMapper;
import com.manager.ftms.dao.WorkOrderMapper;
import com.manager.ftms.service.ExportExcelService;
import com.manager.ftms.service.ISendEmailService;
import com.manager.ftms.service.IUserService;
import com.manager.ftms.util.Constants;
import com.manager.ftms.util.Dict;
import com.manager.ftms.util.ErrorConstants;
import com.manager.ftms.util.Utils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class SendEmailServiceImpl implements ISendEmailService {

    @Autowired
    private ExportExcelService exportExcelService;

    @Autowired
    private TestcaseMapper testcaseMapper;

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private Utils utils;

    @Value("${email.get.attachment.filePath}")
    private String emailGetAttachmentFilePath;

    @Resource
    private RestTransport restTransport;

    @Autowired
    private IUserService userService;

    private static Logger logger = LoggerFactory.getLogger(TestCaseController.class);

    /**
     * 发送案例评审邮件
     *
     * @param requestMap
     * @throws Exception
     */
    @Override
    public void sendCaseEmail(Map requestMap) throws Exception {//工单负责人，行内项目负责人，开发人员，测试人员，工单组长 主题：【需求所属小组名称】【案例评审】需求编号-任务名
        //根据工单号查询其下所有plan
        String workNo = (String) requestMap.get(Dict.WORKNO);
        List<String> targetEmails = new ArrayList<>();
        //如果前端传入email参数说明定制了收件人
        if(!Util.isNullOrEmpty(requestMap.get(Dict.EMAIL))){
            //去重
            targetEmails = (List)((List)requestMap.get(Dict.EMAIL)).stream().distinct().collect(Collectors.toList());
        }
        List<Map<String, String>> plans = new ArrayList<>();
        if(!Util.isNullOrEmpty(requestMap.get(Dict.PLAN))){
            plans = (List<Map<String, String>>)requestMap.get(Dict.PLAN);
        }else{
            plans = testcaseMapper.queryPlanIdByNo(workNo);
        }
        if(Util.isNullOrEmpty(plans)){
            logger.error("There is no plan in order" + workNo);
            throw new FtmsException(ErrorConstants.PLAN_NOT_EXIST_ERROR);
        }
        //组装excel并上送指定路径,并返回路径
        String excelPath = emailGetAttachmentFilePath + exportExcelService.sendCaseExcelToPath(plans);

        //获取邮件数据
        HashMap<String, Object> model = getMailInfo(workNo);
        String subject = "【" + model.get(Dict.GROUP_NAME) + "】【案例评审】" + model.get(Dict.MAINTASKNAME);
        List<String> to = new ArrayList<>();
        for(String email : (Set<String>)model.get(Dict.EMAIL)){
            to.add(email);
        }
        List<String> path = new ArrayList<>();
        path.add(excelPath);
        utils.sendEmail(subject, "ftms_testcase", model, Util.isNullOrEmpty(targetEmails)?to:targetEmails, path);
    }

    /**
     * 根据工单获取邮件信息
     *
     * @param workNo
     * @return
     * @throws Exception
     */
    private HashMap<String, Object> getMailInfo(String workNo) throws Exception {
        HashMap<String, Object> resultMap = new HashMap<>();
        //获取model数据
        Map workOrderMap = null;
        try {
            workOrderMap = testcaseMapper.querySpeByOrder(workNo);
        } catch (Exception e) {
            logger.error("fail to query order info :" + e.getMessage());
            throw new FtmsException(ErrorConstants.QUERY_DATA_EXCEPTION, new String[]{"工单信息查询失败"});
        }
        //主任务名
        resultMap.put(Dict.MAINTASKNAME, workOrderMap.getOrDefault(Dict.MAINTASKNAME, null));
        //组名
        String groupName = "";
        if(!Util.isNullOrEmpty(workOrderMap.get(Dict.FDEVGROUPID))){
            try {
                groupName = String.valueOf(utils.queryGroupDetailById(String.valueOf(workOrderMap.get(Dict.FDEVGROUPID))).get(Dict.NAME));
            } catch (Exception e) {
                logger.error("fail to query group info" + e);
            }
        }
        resultMap.put(Dict.GROUP_NAME, groupName);
        //组装邮箱
        Set<String> to = new HashSet<>();
        Set<String> toEmail = new HashSet<>();
        //如果任务来自fdev
        String rqrmntNo = "";
        String rqrmntName="";
        List<String> taskNames =new ArrayList<>();
        String fdev_new =(String) workOrderMap.get("fdev_new");
        if (Constants.DEFAULT_1.equals(workOrderMap.get(Dict.WORKFLAG))){
            //根据fdev创建的工单
            if ("1".equals(fdev_new) && Util.isNullOrEmpty(workOrderMap.get(Dict.MAINTASKNO)) ){
                //新fdev查询需求
                rqrmntNo = (String) workOrderMap.get("demand_no");
                rqrmntName = (String) workOrderMap.get("demand_name");
            }
            //根据主任务编号查fdev任务详情
            else if(!"1".equals(fdev_new) && !Util.isNullOrEmpty(workOrderMap.get(Dict.MAINTASKNO))){
                //老公单，任务详情里取需求编号
                Map fdev = new HashMap();
                fdev.put(Dict.ID, String.valueOf(workOrderMap.get(Dict.MAINTASKNO)));
                fdev.put(Dict.REST_CODE, "fdev.ftask.queryTaskDetail");
                try {
                    fdev = (Map) restTransport.submitSourceBack(fdev);
                    rqrmntNo = String .valueOf(((Map)fdev.get(Dict.DEMAND)).get(Dict.OA_CONTACT_NO));
                    rqrmntName = String .valueOf(((Map)fdev.get(Dict.DEMAND)).get(Dict.OA_CONTACT_NAME));
                    List<Map> userId = (List<Map>) fdev.get(Dict.DEVELOPER);
                    for (Map m : userId) {
                        to.add(m.get(Dict.USER_NAME_EN).toString());
                    }
                    userId = (List<Map>) fdev.get(Dict.SPDBMASTER);
                    for (Map m : userId) {
                        to.add(m.get(Dict.USER_NAME_EN).toString());
                    }
                    userId = (List<Map>) fdev.get(Dict.MASTER);
                    for (Map m : userId) {
                        to.add(m.get(Dict.USER_NAME_EN).toString());
                    }
                    taskNames.add((String) fdev.get(Dict.NAME));
                } catch (Exception e) {
                    logger.error("fail to query task info " + e);
                }
            }else{
                //新工单，实施单元取需求编号
                if(!Util.isNullOrEmpty(workOrderMap.get(Dict.UNIT))){
                    Map sendUnit = new HashMap();
                    sendUnit.put(Dict.FDEV_IMPLEMENT_UNIT_NO, workOrderMap.get(Dict.UNIT));
                    sendUnit.put(Dict.REST_CODE, "queryByFdevNoAndDemandId");
                    Map<String, Object> unitResult = null;
                    try {
                        unitResult = (Map<String, Object>)restTransport.submitSourceBack(sendUnit);
                        rqrmntNo = String.valueOf(((Map)unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NO));
                        rqrmntName = String.valueOf(((Map)unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NAME));
                    } catch (Exception e) {
                        logger.error("fail to query unit info " + e);
                    }
                }
            }
            //查询任务集
            List<String> taskList = workOrderMapper.queryTaskByNo(workNo);
            if(!Util.isNullOrEmpty(taskList)) {
                if ("1".equals(fdev_new)) {
                    for (String  tId : taskList) {
                        Map<String, Object> task = getNewTaskById(tId);
                        if (!Util.isNullOrEmpty(task)) {
                            List<String> assigneeList = (List<String>) task.get("assigneeList");
                            String userId = assigneeList.get(0);
                            //注入开发人员
                            Map<String, Object> map1 = queryUserCoreDataById(userId);
                            if (!Util.isNullOrEmpty(map1)) {
                                to.add((String) map1.get(Dict.USER_NAME_EN));
                            }
                            taskNames.add((String) task.get(Dict.NAME));
                        }
                    }

                } else{
                    for (String t : taskList) {
                        Map sendSub = new HashMap();
                        sendSub.put(Dict.ID, t);
                        sendSub.put(Dict.REST_CODE, "fdev.ftask.queryTaskDetail");
                        sendSub = (Map) restTransport.submitSourceBack(sendSub);
                        List<Map> subUserId = (List<Map>) sendSub.get(Dict.DEVELOPER);
                        for (Map m : subUserId) {
                            to.add(m.get(Dict.USER_NAME_EN).toString());
                        }
                        subUserId = (List<Map>) sendSub.get(Dict.SPDBMASTER);
                        for (Map m : subUserId) {
                            to.add(m.get(Dict.USER_NAME_EN).toString());
                        }
                        subUserId = (List<Map>) sendSub.get(Dict.MASTER);
                        for (Map m : subUserId) {
                            to.add(m.get(Dict.USER_NAME_EN).toString());
                        }
                        taskNames.add((String) sendSub.get(Dict.NAME));
                    }
            }
            }
        }else{
            // 玉衡自建工单
            rqrmntNo = Util.isNullOrEmpty(workOrderMap.get(Dict.UNIT))?"":String.valueOf(workOrderMap.get(Dict.UNIT));
            rqrmntName = "无";
        }
        resultMap.put(Dict.RQRMNTNO, rqrmntNo);
        resultMap.put(Dict.RQRMNTNAME,rqrmntName);
        resultMap.put(Dict.UNIT,Util.isNullOrEmpty(workOrderMap.get(Dict.UNIT))?"无":String.valueOf(workOrderMap.get(Dict.UNIT)));
        resultMap.put(Dict.TASKLIST,Util.isNullOrEmpty(taskNames)?"无":String.join("、",taskNames));
        //组装玉衡收件人
        if (!Util.isNullOrEmpty(workOrderMap.get(Dict.TESTERS))) {
            to.addAll(Arrays.asList(workOrderMap.get(Dict.TESTERS).toString().split(",")));
        }
        if (!Util.isNullOrEmpty(workOrderMap.get(Dict.GROUPLEADER))) {
            to.addAll(Arrays.asList(workOrderMap.get(Dict.GROUPLEADER).toString().split(",")));
        }
        if (!Util.isNullOrEmpty(workOrderMap.get(Dict.MANAGERS))) {
            to.add(workOrderMap.get(Dict.MANAGERS).toString());
        }
        for(String en : to){
            try {
                toEmail.add(String.valueOf(utils.queryUserCoreDataByNameEn(en).get(Dict.EMAIL)));
            } catch (Exception e) {
                logger.error("fail to query user " + en + e);
            }
        }
        resultMap.put(Dict.EMAIL, toEmail);
        return resultMap;
    }

    public Map<String, Object> queryUserCoreDataById(String user_id) throws Exception {
        Map sendData = new HashMap<String, String>();
        sendData.put(Dict.ID, user_id);
        sendData.put(Dict.REST_CODE, "queryByUserCoreData");
        List<Map> users = (List<Map>)restTransport.submitSourceBack(sendData);
        if(!Util.isNullOrEmpty(users)){
            return users.get(0);
        }
        return null;
    }

    public Map<String,Object> getNewTaskById(String id) {
        Map send = new HashMap();
        send.put(Dict.ID, id);
        send.put(Dict.REST_CODE, "getTaskById");
        Map<String,Object> map=new HashMap<>();
        try {
            map=  (Map<String, Object>) restTransport.submitSourceBack(send);
        }catch (Exception e){
            logger.error("fail to update task test status");
        }finally {
            return map;
        }
    }
}
