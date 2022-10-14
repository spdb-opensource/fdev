package com.mantis.service.impl;

import com.mantis.dao.MantisDao;
import com.mantis.dict.Constants;
import com.mantis.dict.Dict;
import com.mantis.dict.ErrorConstants;
import com.mantis.entity.MantisIssue;
import com.mantis.service.ApiService;
import com.mantis.service.MantisService;
import com.mantis.service.NewFdevService;
import com.mantis.util.MantisRestTemplate;
import com.mantis.util.MyUtil;
import com.mantis.util.Utils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RefreshScope
public class NewFdevServiceImpl implements NewFdevService {

    private static final Logger logger = LoggerFactory.getLogger(NewFdevServiceImpl.class);

    @Autowired
    private MantisDao mantisDao;
    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private MantisRestTemplate mantisRestTemplate;
    @Autowired
    private MantisService mantisService;
    @Autowired
    private ApiService apiService;

    @Value("${manits.issue.url}")
    private String mantis_url;
    @Value("${manits.fdev.token}")
    private String mantis_fdev_token;
    @Value("${spring.profiles.active}")
    private String env;
    @Value("${testmanage.mantis.domain}")
    private String issueUrl;


    /**
     * @param unitNos
     * @param userNameEn
     * @param status
     * @return
     * @throws Exception
     */
    @Override
    public List<MantisIssue> queryFuserMantisAll(List unitNos, String userNameEn, String status) throws Exception {
        Map result = new HashMap();
        List<MantisIssue> mantisIssues = new ArrayList<>();
        List<String> worklist = queryWorkNoByUnitNo(unitNos);
        String workNo = "";
        List<MantisIssue> mantisIssues1 = new ArrayList<>();
        if (!Util.isNullOrEmpty(worklist)) {
            workNo = "'" + String.join("','", worklist) + "'";
            mantisIssues1 = mantisDao.queryMantis(workNo, userNameEn, status, env);
        }
        return mantisIssues1;
    }

    //根据实施单元编号查询工单
    private List<String> queryWorkNoByUnitNo(List unitNos) throws Exception {
        List<String> workList = new ArrayList();
        Map<String, Object> submit = new HashMap<>();
        submit.put(Dict.UNITNO, unitNos);
        submit.put(Dict.REST_CODE, "query.workorder");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        if (!Util.isNullOrEmpty(unitNos)) {
            workList = (List) restTransport.submitWithHeaders(submit, headers);
        }
        return workList;

    }

    /**
     * 重构fdev缺陷交互
     *
     * @param map
     * @return
     * @throws Exception
     */
    @Override
    public String updateFdevMantis(Map map) throws Exception {
        String id = String.valueOf(map.get(Dict.ID));//缺陷id
        String handler_en_name = (String) map.get(Dict.HANDLER_EN_NAME);//分配给英文名
        String stage = (String) map.get(Dict.STAGE);//归属阶段
        String reason = (String) map.get(Dict.REASON); //开发原因分析
        String flaw_source = (String) map.get(Dict.FLAW_SOURCE); //缺陷来源
        String developer = (String) map.get(Dict.DEVELOPER);//开发人员英文名
        String developer_cn = (String) map.get(Dict.DEVELOPER_CN);//开发人员中文名
        String plan_fix_date = (String) map.get(Dict.PLAN_FIX_DATE);//遗留缺陷预计修复时间
        String flaw_type = (String) map.get(Dict.FLAW_TYPE);//缺陷类型
        String status = (String) map.get(Dict.STATUS);//缺陷状态
        String taskNo = (String) map.get(Dict.TASK_ID);//任务编号
        String appName_en = (String) map.getOrDefault(Dict.APP_NAME, "");//所属应用英文
        String reopen_reason = (String) map.getOrDefault(Dict.REOPEN_REASON, "");//重新打开原因
        String url = new StringBuilder(mantis_url).append("/api/rest/issues/").append(id).toString();//发送mantis接口url
        String system_version = (String) map.get(Dict.SYSTEM_VERSION); //系统版本
        system_version = system_version == null ? "" :system_version;
        //查询原数据用于判断不同
        Map<String, Object> oldData = mantisService.queryIssueDetail(id);
        //如果修改状态为新建，则检测缺陷原状态是否为新建，如果原状态不是新建,说明数据已经刷新，让用户刷新页面再修改，防止缺陷回到新建状态
        if ("10".equals(status) && !"10".equals(oldData.get(Dict.STATUS))) {
           logger.error("old status is not 10, please refresh the page");
            throw new FtmsException(ErrorConstants.MANTIS_STATUS_REFRESH);
        }
        map.put(Dict.OLDMANTISDATA, oldData);
        //判断缺陷 修改流程
        String oldStatus = (String) oldData.get(Dict.STATUS);
        String statusChange = "缺陷状态变化：" + myUtil.getChStatus(oldStatus) + " -> " + myUtil.getChStatus(status);
        if (oldStatus.equals("10") && (!status.equals("10") && !status.equals("50"))) {
            //原状态为新建的缺陷只能改为新建或打开
            logger.error(statusChange);
            throw new FtmsException(ErrorConstants.UPDATE_MANTIS_STATUS_ERROR, new String[]{statusChange});
        } else if (oldStatus.equals("40") && (!status.equals("40") && !status.equals("80"))) {
            //原状态为延期修复的缺陷只能改为延期修复或已修复
            logger.error(statusChange);
            throw new FtmsException(ErrorConstants.UPDATE_MANTIS_STATUS_ERROR, new String[]{statusChange});
        } else if (oldStatus.equals("50") && (!status.equals("50") && !status.equals("40") && !status.equals("80") && !status.equals("20"))) {
            //原状态为打开的缺陷只能改为打开或延期修复或已修复或拒绝
            logger.error(statusChange);
            throw new FtmsException(ErrorConstants.UPDATE_MANTIS_STATUS_ERROR, new String[]{statusChange});
        } else if (!oldStatus.equals(status) && (oldStatus.equals("20") || oldStatus.equals("80") || oldStatus.equals("30") || oldStatus.equals("90"))) {
            //原状态为拒绝或已修复或确认拒绝或关闭，不允fdev修改状态
            logger.error(statusChange);
            throw new FtmsException(ErrorConstants.UPDATE_MANTIS_STATUS_ERROR, new String[]{statusChange});
        }
        //判断分配人是否报告人
        if (oldData.get(Dict.REPORTER_EN_NAME).equals(map.get(Dict.HANDLER_EN_NAME))) {
            logger.error("can not allocate reporter as handler");
            throw new FtmsException(ErrorConstants.BAD_ALLOCATE_ERROR);
        }
        Map<String, Object> sendMap = new HashMap<String, Object>();
        sendMap.put(Dict.STATUS, myUtil.assemblyParamMap(Dict.ID, status));
        sendMap.put(Dict.HANDLER, myUtil.assemblyParamMap(Dict.NAME, handler_en_name));
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (!Util.isNullOrEmpty(taskNo)) {
            //查询任务的组信息
            Map taskDetail = apiService.queryNewTaskInfoByTaskNo(taskNo);
            String fdev_groupId = (String) taskDetail.get(Dict.ASSIGNEEGROUPID);
            String fdev_groupName = (String) (apiService.queryFdevGroupInfo(fdev_groupId).get(Dict.NAME));
            list.add(myUtil.assemblyCustomMap(21, fdev_groupName));   //所属fdev小组名称
            list.add(myUtil.assemblyCustomMap(73, fdev_groupId));   //所属fdev小组id
            String appId = (String) taskDetail.get("relatedApplication");
            String appName = apiService.queryAppNameByAppid(appId);
            appName_en = Util.isNullOrEmpty(appName) ? appName_en : appName;
        }
        list.add(myUtil.assemblyCustomMap(3, stage));//'归属阶段'
        list.add(myUtil.assemblyCustomMap(4, reason));//'开发原因分析'
        list.add(myUtil.assemblyCustomMap(5, flaw_source));//'缺陷来源'
        list.add(myUtil.assemblyCustomMap(6,system_version));//'系统版本'
        list.add(myUtil.assemblyCustomMap(65, developer));//'开发人员'
        list.add(myUtil.assemblyCustomMap(8, developer_cn));//'开发人员
        list.add(myUtil.assemblyCustomMap(9, Utils.dateStrToLong(plan_fix_date)));//'遗留缺陷预计修复时间'
        list.add(myUtil.assemblyCustomMap(11, flaw_type));//'缺陷类型'
        list.add(myUtil.assemblyCustomMap(19, Util.isNullOrEmpty(taskNo)?"":taskNo));//fdev任务编号
        list.add(myUtil.assemblyCustomMap(72, appName_en));//所属应用
        list.add(myUtil.assemblyCustomMap(81, reopen_reason));//重新打开原因
        sendMap.put(Dict.CUSTOM_FIELDS, list);
        String res;
        try {
            res = mantisRestTemplate.sendPatch(url, mantis_fdev_token, sendMap);
        } catch (Exception e) {
            logger.error("======" + e.getMessage());
            throw new FtmsException(ErrorConstants.UPDATE_MANTIS_ISSUES_ERROR);
        }
        //如果改为以修复或者拒绝,通知玉衡相关人员
        if ("80".equals(status) || "20".equals(status)) {
            List<String> target = new ArrayList<>();
            target.add(map.get(Dict.REPORTER_EN_NAME).toString());
            Map<String, Object> sendData = new HashMap<>();
            sendData.put(Dict.TARGET, target);
            sendData.put(Dict.CONTENT, "【缺陷变更】" + oldData.get(Dict.SUMMARY));
            sendData.put(Dict.TYPE, "缺陷变更");
            sendData.put(Dict.REST_CODE, "ftms.notify.url");
            String linkUrl = issueUrl + "/tcli/#/MantisIssue/" + id;
            sendData.put(Dict.LINKURI, linkUrl);
            try {
                restTransport.submitSourceBack(sendData);
            } catch (Exception e) {
                logger.error("fail to send ftms nofity" + e.getMessage());
            }
        }
        //如果分配的指派人变更，发fdev消息通知
        if (!oldData.get(Dict.HANDLER_EN_NAME).equals(map.get(Dict.HANDLER_EN_NAME))) {
            Map<String, String> data = new HashMap<>();
            try {
                data = (Map<String, String>) restTransport.submit(restTransport.getUrl("query.ftms.workorder"), String.valueOf(oldData.get(Dict.WORKNO)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<String> target = new ArrayList<>();
            target.add(map.get(Dict.HANDLER_EN_NAME).toString());
            Map<String, Object> sendData = new HashMap<>();
            sendData.put(Dict.TARGET, target);
            //发送fdev消息接口
            String handlerEn = map.get(Dict.HANDLER_EN_NAME).toString();
            Map<String, Object> map1 = queryUser(handlerEn);
            String newHandler =(String) map1.get(Dict.USER_NAME_CN);
            sendData.put(Dict.CONTENT, map.get("summary").toString()+"缺陷经办人由"+oldData.get(Dict.HANDLER).toString()+"改为"+newHandler+"-缺陷通知");
            sendData.put(Dict.DESC, Constants.MANTISNOTICE);//缺陷通知
            sendData.put(Dict.TYPE, "2");
            sendData.put(Dict.REST_CODE, "new.fdev.fnotify.sendUserNotify");
            try {
                restTransport.submitSourceBack(sendData);
            } catch (Exception e) {
                logger.error("fail to send fdev nofity");
            }
        }
        return res;
    }

    public Map<String,Object> queryUser(String user_name_en) throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        map.put(com.mantis.dict.Dict.USER_NAME_EN, user_name_en);
        map.put(Dict.REST_CODE, "query.fdev.user.core");
        List list = (List) restTransport.submitSourceBack(map);
        if(Util.isNullOrEmpty(list)) {
            return null;
        }
        return (Map<String,Object>)list.get(0);
    }

    /**
     * 实施单元删除关联工单缺陷状态修改为已关闭
     * @param workNos
     * @return
     */
    @Override
    public void updateMantisStatus(List<String> workNos) throws Exception {
        //通过工单查询所有的缺陷；
        if (!Utils.isEmpty(workNos)) {
            String workNo = "'" + String.join("','", workNos) + "'";
            String userNameEn=null;
            String status=null;
            List<MantisIssue> mantisIssueList = mantisDao.queryMantis(workNo,userNameEn,status,env) ;
            if (!Utils.isEmpty(mantisIssueList)) {
                for (MantisIssue mantisIssue : mantisIssueList) {
                    String url = new StringBuilder(mantis_url).append("/api/rest/issues/").append(mantisIssue.getId()).toString();
                    try {
                        mantisRestTemplate.sendDelete(url, mantis_fdev_token);
                    } catch (Exception e) {
                        logger.error("id" + mantisIssue.getId() + e);
                    }
                }
            }
        }
    }

    /**
     * 任务ids关闭缺陷
     * @param taskIds
     * @return
     */
    @Override
    public void updateMantisByTaskIds(List<String> taskIds) throws Exception {
        List<MantisIssue> issues = queryFtaskMantisAll(taskIds, "0");
        for (MantisIssue mantisIssue : issues) {
            String url = new StringBuilder(mantis_url).append("/api/rest/issues/").append(mantisIssue.getId()).toString();
            try {
                mantisRestTemplate.sendDelete(url, mantis_fdev_token);
            } catch (Exception e) {
                logger.error("id" + mantisIssue.getId() + e);
            }
        }
    }

    public List<MantisIssue> queryFtaskMantisAll(List<String> taskList, String includeCloseFlag) throws Exception {
        List<MantisIssue> result = mantisDao.queryTasksMantis(String.join(",",taskList), env,includeCloseFlag);
        return result;
    }
}
