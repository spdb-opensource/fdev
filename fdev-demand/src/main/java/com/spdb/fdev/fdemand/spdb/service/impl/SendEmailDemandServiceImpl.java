package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IIpmpUnitDao;
import com.spdb.fdev.fdemand.spdb.entity.*;
import com.spdb.fdev.fdemand.spdb.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author zhanghp4
 */
@Service
@RefreshScope
public class SendEmailDemandServiceImpl implements SendEmailDemandService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Value("${demand.email.assess.group}")
    private List<String> demandEmailAssessGroup;

    @Value("${demand.email.assess.groupReceiver}")
    private String demandEmailAssessGroupReceiver;

    @Value("${demand.email.assess.fixedReceiver}")
    private List<String> demandEmailAssessFixedReceiver;

    @Value("${isAssessSendEmail}")
    private boolean isAssessSendEmail;

    @Value("${demand.email.filePath}")
    private String filePath;

    @Value("${demand.email.todayLast}")
    private int todayLast;

    @Value("${ipmp.email.switch}")
    private boolean ipmpEmail;

    @Value("${history.demand.url}")
    private String history_demand_url;

    @Value("${x.test.url}")
    private String x_test_url;

    @Value("${history.ipmpList.url}")
    private String history_ipmpList_url;

    @Value("${ipmp.unit.url}")
    private String ipmpUnitUrl;

    @Value("${assess.demand.url}")
    private String assessDemandUrl;

    @Value("${section.info}")
    private String sectionInfo;

    @Value("${fdev.unit.approve}")
    private List<String> fdevUnitApproveUserEmail;

    @Value("${basic.group.manager.role.id}")
    private String groupManageRoleId;//团队负责人角色id

    @Value("${my.approve.url}")
    private String myApproveUrl;

    @Value("${product.delay.receiver}")
    private List<String> proDelayEamilReceiver;//投产延期排期调整提醒邮件固定收件人

    @Value("${test.order.cc}")
    private List<String> testOrderCc;//提测单邮件固定收件人

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IDemandBaseInfoService demandBaseInfoService;

    @Autowired
    private IMailService mailService;

    @Autowired
    private IFdevUserService fdevUserService;

    @Autowired
    private IIpmpUnitDao ipmpUnitDao;

    @Override
    public void sendEmailAssessDemand() throws Exception {

        if (isAssessSendEmail) {
            if (!CommonUtils.isNullOrEmpty(demandEmailAssessGroup)) {
                //获取全量用户信息
                List<Map> queryUser = roleService.queryUser();
                String groupName = null;
                for (String groupId : demandEmailAssessGroup) {
                    List<DemandBaseInfo> demands = new ArrayList<>();
                    //查询当前组和子组数据
                    List<LinkedHashMap> groupAll = roleService.queryChildGroupById(groupId);
                    if (!CommonUtils.isNullOrEmpty(groupAll)) {
                        for (LinkedHashMap linkedHashMap : groupAll) {
                            String id = (String) linkedHashMap.get(Dict.ID);
                            Set<String> listId = new HashSet<>();
                            listId.add(id);
                            //获取该组下所有的评估日期超期的需求信息
                            List<DemandBaseInfo> demandBaseInfos = demandBaseInfoService.queryDemandByGroupIdAssessOver(listId, todayLast);
                            if (!CommonUtils.isNullOrEmpty(demandBaseInfos)) {
                                demands.addAll(demandBaseInfos);
                            }
                            if (id.equals(groupId)) {
                                groupName = (String) linkedHashMap.get(Dict.NAME);
                            }
                        }
                    }
                    if (!CommonUtils.isNullOrEmpty(demands)) {
                        //获取当前日期
                        String formatDate = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
                        List<Map> initString = new ArrayList<>();//用于邮件展示
                        List<String> demandLeaderEmail = new ArrayList<>();//需求科技负责人邮箱集合
                        //即将到期需求
                        //延期需求
                        for (DemandBaseInfo demand : demands) {
                            //需求牵头人
                            String appendUserCn = CommonUtils.appendUserCn(demand, queryUser);
                            //需求牵头人邮箱
                            List<String> initEmail = CommonUtils.appendUserEmail(demand, queryUser);
                            for (String str : initEmail) {
                                if (!demandLeaderEmail.contains(str)) {
                                    demandLeaderEmail.add(str);
                                }
                            }
                            //评估超期天数
                            long countDay = CommonUtils.countDay(formatDate, demand.getDemand_assess_date());
                            //拼装提醒内容 需求编号，需求名称，需求牵头人，评估日期，评估超期天数
                            Map appendMap = CommonUtils.appendMap(demand.getOa_contact_no(), demand.getOa_contact_name(), demand.getDemand_assess_date(), appendUserCn, countDay);
                            initString.add(appendMap);
                        }
                        if (!CommonUtils.isNullOrEmpty(initString)) {
                            //即将到期需求提醒通知
                            setEmailContent("fdemand_assess", groupName, groupId,
                                    queryUser, initString, demandLeaderEmail);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void sendEmailImplDelay(IpmpUnit ipmpUnit, DemandBaseInfo demandBaseInfo, Map<String, Map> userMapAll) throws Exception {
        if (!ipmpEmail) return;//邮件开关 false 则不发送邮件

        String template = "impl_delay";
        String implContent = ipmpUnit.getImplContent();//实施单元内容
        String implUnitNum = ipmpUnit.getImplUnitNum();//实施单元编号
        String implLeader = ipmpUnit.getImplLeader();//牵头人
        String developDelayEmail = ipmpUnit.getDevelopDelayEmail();//实际启动开发延期邮件 是否有发送过邮件 1 = 已发送
        String testStartDelayEmail = ipmpUnit.getTestStartDelayEmail();//实际提交用户测试延期邮件
        String testFinishDelayEmail = ipmpUnit.getTestFinishDelayEmail();//实际用户测试完成延期邮件
        String productDelayEmail = ipmpUnit.getProductDelayEmail();//实际投产延期邮件

        if (!CommonUtils.isNullOrEmpty(implLeader) && !CommonUtils.isNullOrEmpty(demandBaseInfo) && !CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_flag())) {

            List<String> implLeaders = Arrays.asList(implLeader.split(","));
            Set<String> implLeaderSet = new HashSet<>();
            implLeaderSet.addAll(implLeaders);
            //查询牵头人信息
            if (CommonUtils.isNullOrEmpty(userMapAll)) {
                userMapAll = fdevUserService.queryByUserCoreData(null, implLeaderSet);
            }
            //Map<String,Map> userMapAll = fdevUserService.queryByUserCoreData(null,implLeaderSet);
            List<String> emailList = new ArrayList<>();
            List<String> implLeaderIds = new ArrayList<>();//牵头人IDS
            //获取邮箱
            for (String implLeaderEn : implLeaderSet) {
                if (!CommonUtils.isNullOrEmpty(userMapAll.get(implLeaderEn))) {
                    Map userMap = userMapAll.get(implLeaderEn);
                    emailList.add((String) userMap.get(Dict.EMAIL));
                    implLeaderIds.add((String) userMap.get(Dict.ID));
                }
            }
            if (!CommonUtils.isNullOrEmpty(emailList)) {
                //拼接邮件主题
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("【").append(implUnitNum).append("延期提醒】[").append(implContent).append("]");
                //待办type
                StringBuffer stringBufferType = new StringBuffer();
                stringBufferType.append("implDelay");
                //待办内容
                StringBuffer stringBufferContent = new StringBuffer();
                stringBufferContent.append("需求 ").append(demandBaseInfo.getOa_contact_no()).append(" 下 ").append(implContent).append("实施单元");
                //倒序判断发送哪个阶段的邮件待办
                if (Constants.ONE.equals(productDelayEmail)) {
                    stringBuffer.append("投产");
                    stringBufferType.append("_4");//投产延期
                    stringBufferContent.append("投产");
                } else if (Constants.ONE.equals(testFinishDelayEmail)) {
                    stringBuffer.append("用户测试完成");
                    stringBufferType.append("_3");//用户测试完成延期
                    stringBufferContent.append("用户测试完成");
                } else if (Constants.ONE.equals(testStartDelayEmail)) {
                    stringBuffer.append("提交用户测试");
                    stringBufferType.append("_2");//提交用户测试延期
                    stringBufferContent.append("提交用户测试");
                } else if (Constants.ONE.equals(developDelayEmail)) {
                    stringBuffer.append("启动开发");
                    stringBufferType.append("_1");//启动开发延期
                    stringBufferContent.append("启动开发");
                }
                stringBufferContent.append("已延期。");

                //给用户添加待办
                fdevUserService.addCommissionEvent(demandBaseInfo, implLeaderIds, stringBufferType.toString(), history_demand_url + demandBaseInfo.getId(), stringBufferContent.toString());
                //roleService.sendNotify(implLeaders, content, demandBaseInfo.getId());
                stringBuffer.append("已延期");
                String subject = stringBuffer.toString();
                List<String> filePaths = new ArrayList<>();
                filePaths.add(filePath);
                if (!CommonUtils.isNullOrEmpty(filePaths)) {
                    //组装邮件正文
                    HashMap<String, Object> model = new HashMap<>();
                    model.put(Dict.IMPLCONTENT, implContent);
                    model.put(Dict.HISTORY_DEMAND_URL, history_demand_url + demandBaseInfo.getId());//需求链接地址
                    String mailContent = mailService.getOverDueMsg(template, model);
                    HashMap<String, Object> sendMap = new HashMap<>();
                    sendMap.put(Dict.EMAIL_CONTENT, mailContent);
                    try {
                        //发送邮件
                        mailService.sendEmail(subject, template, sendMap, emailList, filePaths);
                        //更新邮件标识为已发送
                        ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                    } catch (Exception e) {
                        logger.error("#########发送实施单元延期提醒邮件失败########" + e.getMessage());
                        throw new FdevException("#########发送实施单元延期提醒失败########");
                    }
                }
            }
        }

    }

    @Override
    public void sendUserMountDevUnit(FdevImplementUnit fdevUnit, DemandBaseInfo demandBaseInfo) throws Exception {
        if (!ipmpEmail) return;//邮件开关 false 则不发送邮件
        String template = "mount_devUnit";
        String implementUnitContent = fdevUnit.getImplement_unit_content();//研发单元内容
        HashSet<String> implementLeader = fdevUnit.getImplement_leader();//牵头人
        if (!CommonUtils.isNullOrEmpty(implementLeader)) {
            //查询牵头人信息 ids
            Map<String, Map> userMap = fdevUserService.queryByUserCoreData(implementLeader, null);
            List<String> emailList = new ArrayList<>();
            List<String> nameEnList = new ArrayList<>();
            Set<Map.Entry<String, Map>> entries = userMap.entrySet();
            //获取邮箱
            if (!CommonUtils.isNullOrEmpty(entries)) {
                for (Map.Entry<String, Map> entry : entries) {
                    try {
                        String email = (String) entry.getValue().get(Dict.EMAIL);
                        String userNameEn = (String) entry.getValue().get(Dict.USER_NAME_EN);
                        emailList.add(email);
                        nameEnList.add(userNameEn);
                    } catch (Exception e) {
                        logger.error("获取人员邮箱信息错误" + entry.getKey());
                    }
                }
            }
            //给用户添加待办
            String content = "需求 " + demandBaseInfo.getOa_contact_no() + " 下 " + implementUnitContent + "研发单元未挂载元实施单元。";
            fdevUserService.addCommissionEvent(demandBaseInfo, new ArrayList<>(implementLeader), "mountDevUnit_" + fdevUnit.getFdev_implement_unit_no(), history_demand_url + demandBaseInfo.getId(), content);
            //roleService.sendNotify(nameEnList, content, demandBaseInfo.getId());
            //拼接邮件主题
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("【研发单元挂载提醒】[").append(implementUnitContent).append("]未挂载元实施单元。");
            String subject = stringBuffer.toString();
            List<String> filePaths = new ArrayList<>();
            filePaths.add(filePath);
            if (!CommonUtils.isNullOrEmpty(filePaths)) {
                //组装邮件正文
                HashMap<String, Object> model = new HashMap<>();
                model.put(Dict.IMPLEMENT_UNIT_CONTENT, implementUnitContent);
                model.put(Dict.HISTORY_DEMAND_URL, history_demand_url + demandBaseInfo.getId());//需求链接地址
                String mailContent = mailService.getOverDueMsg(template, model);
                HashMap<String, Object> sendMap = new HashMap<>();
                sendMap.put(Dict.EMAIL_CONTENT, mailContent);
                try {
                    //发送邮件
                    mailService.sendEmail(subject, template, sendMap, emailList, filePaths);
                } catch (Exception e) {
                    logger.error("#########发送研发单元挂载提醒邮件失败########" + e.getMessage());
                    throw new FdevException("#########发送研发单元挂载提醒失败########");
                }
            }
        }

    }

    @Override
    public void sendEmailUpdateDemand(DemandBaseInfo demandBaseInfo) throws Exception {
        if (!ipmpEmail) return;//邮件开关 false 则不发送邮件
        String template = "update_demand";
        String demandBaseInfoOaContactName = demandBaseInfo.getOa_contact_name();//需求名称
        HashSet<String> demandLeader = demandBaseInfo.getDemand_leader();//牵头人
        if (!CommonUtils.isNullOrEmpty(demandLeader)) {
            //查询牵头人信息
            Map<String, Map> userMap = fdevUserService.queryByUserCoreData(demandLeader, null);
            List<String> emailList = new ArrayList<>();
            Set<Map.Entry<String, Map>> entries = userMap.entrySet();
            //获取邮箱
            if (!CommonUtils.isNullOrEmpty(entries)) {
                for (Map.Entry<String, Map> entry : entries) {
                    try {
                        String email = (String) entry.getValue().get(Dict.EMAIL);
                        emailList.add(email);
                    } catch (Exception e) {
                        logger.error("获取人员邮箱信息错误" + entry.getKey());
                    }
                }
            }
            if (!CommonUtils.isNullOrEmpty(emailList)) {
                //给用户添加待办
                String content = "需求 " + demandBaseInfo.getOa_contact_no() + " 已与ipmp同步，请及时补充牵头小组、涉及小组、评估人等信息！ ";
                fdevUserService.addCommissionEvent(demandBaseInfo, new ArrayList<>(demandLeader), "updateDemand_" + demandBaseInfo.getOa_contact_no(), history_demand_url + demandBaseInfo.getId(), content);
                //roleService.sendNotify(new ArrayList<>(demandLeader), content, demandBaseInfo.getId());
                //拼接邮件主题
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("【需求信息补充提醒】[").append(demandBaseInfoOaContactName).append("]已同步，请及时补充相关信息！");
                String subject = stringBuffer.toString();
                List<String> filePaths = new ArrayList<>();
                filePaths.add(filePath);
                if (!CommonUtils.isNullOrEmpty(filePaths)) {
                    //组装邮件正文
                    HashMap<String, Object> model = new HashMap<>();
                    model.put(Dict.OA_CONTACT_NAME, demandBaseInfoOaContactName);
                    model.put(Dict.HISTORY_DEMAND_URL, history_demand_url + demandBaseInfo.getId());//需求链接地址
                    String mailContent = mailService.getOverDueMsg(template, model);
                    HashMap<String, Object> sendMap = new HashMap<>();
                    sendMap.put(Dict.EMAIL_CONTENT, mailContent);
                    try {
                        //发送邮件
                        mailService.sendEmail(subject, template, sendMap, emailList, filePaths);
                    } catch (Exception e) {
                        logger.error("#########发送补充需求信息邮件失败########" + e.getMessage());
                        throw new FdevException("#########发送补充需求信息邮件提醒失败########");
                    }
                }
            }
        }

    }

    @Override
    public void sendEmailIpmpUnitCheck(String implUnitNum) throws Exception {
        if (!ipmpEmail) return;//邮件开关 false 则不发送邮件
        String template = "impl_check";
        IpmpUnit ipmpUnit = ipmpUnitDao.queryIpmpUnitById(implUnitNum);
        String implContent = ipmpUnit.getImplContent();//实施单元内容
        String implLeader = ipmpUnit.getImplLeader();//牵头人
        if (!CommonUtils.isNullOrEmpty(implLeader)) {
            List<String> implLeaders = Arrays.asList(implLeader.split(","));
            Set<String> implLeaderSet = new HashSet<>();
            implLeaderSet.addAll(implLeaders);
            //查询牵头人信息
            Map<String, Map> userMapAll = fdevUserService.queryByUserCoreData(null, implLeaderSet);
            List<String> emailList = new ArrayList<>();
            //获取邮箱
            for (String implLeaderEn : implLeaderSet) {
                if (!CommonUtils.isNullOrEmpty(userMapAll.get(implLeaderEn))) {
                    Map userMap = userMapAll.get(implLeaderEn);
                    emailList.add((String) userMap.get(Dict.EMAIL));
                }
            }
            if (!CommonUtils.isNullOrEmpty(emailList)) {
                //拼接邮件主题
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("【实施单元核算提醒】[").append(implContent).append("]未核算。");

                String subject = stringBuffer.toString();
                List<String> filePaths = new ArrayList<>();
                filePaths.add(filePath);
                if (!CommonUtils.isNullOrEmpty(filePaths)) {
                    //组装邮件正文
                    HashMap<String, Object> model = new HashMap<>();
                    model.put(Dict.IMPLCONTENT, implContent);
                    model.put(Dict.IPMPUNITURL, ipmpUnitUrl);//IPMP链接地址
                    String mailContent = mailService.getOverDueMsg(template, model);
                    HashMap<String, Object> sendMap = new HashMap<>();
                    sendMap.put(Dict.EMAIL_CONTENT, mailContent);
                    try {
                        //发送邮件
                        mailService.sendEmail(subject, template, sendMap, emailList, filePaths);
                    } catch (Exception e) {
                        logger.error("#########发送实施单元核算提醒邮件失败########" + e.getMessage());
                        throw new FdevException("#########发送实施单元核算提醒邮件失败########");
                    }
                }
            }
        }
    }

    //邮件相关
    private void setEmailContent(String template, String groupName, String groupId,
                                 List<Map> queryUser, List<Map> initString, List<String> demandLeaderEmail) throws Exception {
        //拼接邮件主题
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("【评估时间超期提醒】[").append(groupName).append("]评估时间超期需求列表");
        String subject = stringBuffer.toString();

        //拼接收件人: 需求牵头人+组收件人+固定收件人
        //组收件人
        List<String> appendUserEmail = new ArrayList<>();

        if (!CommonUtils.isNullOrEmpty(demandEmailAssessGroupReceiver)) {
            appendUserEmail = CommonUtils.appendGroupReceiverNew(demandEmailAssessGroupReceiver, groupId);
        }
        //固定收件人
        for (String str : demandEmailAssessFixedReceiver) {
            if (!appendUserEmail.contains(str)) {
                appendUserEmail.add(str);
            }
        }
        //需求牵头人
        for (String str : demandLeaderEmail) {
            if (!appendUserEmail.contains(str)) {
                appendUserEmail.add(str);
            }
        }
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        if (!CommonUtils.isNullOrEmpty(filePaths)) {
            //发送邮件
            HashMap<String, Object> model = new HashMap<>();
            model.put(Dict.GROUPNAME, groupName);
            model.put(Dict.INITSTRING, initString);
            String mailContent = mailService.getOverDueMsg(template, model);
            HashMap<String, Object> sendMap = new HashMap<>();
            sendMap.put(Dict.EMAIL_CONTENT, mailContent);
            try {
                mailService.sendEmail(subject, template, sendMap, appendUserEmail, filePaths);
            } catch (Exception e) {
                logger.error("#########发送需求评估超期通知邮件失败########" + e.getMessage());
                throw new FdevException("#########发送需求评估超期通知邮件失败########");
            }
        }
    }

    @Async
    @Override
    public void sendEmailAssessSave(String id, String oaContactNo, String oaContactName, List<String> demandLeaderEmail) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("【请评估】").append(oaContactNo).append("需求已进入评估阶段，请尽快进行需求评估");
        String template = "demand_assess_save";
        String subject = stringBuffer.toString();
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.OA_CONTACT_NO, oaContactNo);
        model.put(Dict.OA_CONTACT_NAME, oaContactName);
        model.put(Dict.ASSESS_DEMAND_URL, assessDemandUrl + id);//需求链接地址
        String mailContent = mailService.getOverDueMsg(template, model);
        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailContent);
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(subject, template, sendMap, demandLeaderEmail, filePaths);
        } catch (Exception e) {
            logger.error("------新增评估需求时邮件发送失败------" + e.getMessage());
        }
    }

    /**
     * 评估时长为7天时，fdev邮件发送需求牵头人、吴磊、杨静：“需求编号-需求名称已评估7天，请及时推进。”
     *
     * @param id
     * @param oaContactNo
     * @param oaContactName
     * @param demandLeaderEmail
     * @throws Exception
     */
    @Async
    @Override
    public void sendEmailAssess7(String id, String oaContactNo, String oaContactName, List<String> demandLeaderEmail) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("【请及时评估】").append(oaContactNo).append("已评估7天，请及时推进");
        String template = "demand_assess7";
        String subject = stringBuffer.toString();
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.OA_CONTACT_NO, oaContactNo);
        model.put(Dict.OA_CONTACT_NAME, oaContactName);
        model.put(Dict.ASSESS_DEMAND_URL, assessDemandUrl + id);//需求链接地址
        String mailContent = mailService.getOverDueMsg(template, model);
        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailContent);
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(subject, template, sendMap, demandLeaderEmail, filePaths);
        } catch (Exception e) {
            logger.error("------需求评估达7天时邮件发送失败------" + e.getMessage());
        }
    }

    @Async
    @Override
    public void sendEmailAssess11(String id, String oaContactNo, String oaContactName, List<String> demandLeaderEmail) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("【请及时评估】").append(oaContactNo).append("已评估11天，请及时推进");
        String template = "demand_assess11";
        String subject = stringBuffer.toString();
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.OA_CONTACT_NO, oaContactNo);
        model.put(Dict.OA_CONTACT_NAME, oaContactName);
        model.put(Dict.ASSESS_DEMAND_URL, assessDemandUrl + id);//需求链接地址
        String mailContent = mailService.getOverDueMsg(template, model);
        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailContent);
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(subject, template, sendMap, demandLeaderEmail, filePaths);
        } catch (Exception e) {
            logger.error("------需求评估达11天时邮件发送失败------" + e.getMessage());
        }
    }

    @Async
    @Override
    public void sendEmailAssess14(String id, String oaContactNo, String oaContactName, Integer assessDays, List<String> demandLeaderEmail) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("【评估已超期】").append(oaContactNo).append("评估已超期，请及时推进");
        String template = "demand_assess14";
        String subject = stringBuffer.toString();
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.OA_CONTACT_NO, oaContactNo);
        model.put(Dict.OA_CONTACT_NAME, oaContactName);
        model.put(Dict.ASSESS_DAYS, assessDays);
        model.put(Dict.ASSESS_DEMAND_URL, assessDemandUrl + id);//需求链接地址
        String mailContent = mailService.getOverDueMsg(template, model);
        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailContent);
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(subject, template, sendMap, demandLeaderEmail, filePaths);
        } catch (Exception e) {
            logger.error("------需求评估达" + assessDays + "天时邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    public void sendEmailConfUpdateAssess(String id, String oaContactNo, String oaContactName, String oldState, String newState, String[] target) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("【需求评估登记表变更】").append(oaContactNo).append("状态已变更");
        String template = "conf_assess";
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.OA_CONTACT_NO, oaContactNo);
        model.put(Dict.OA_CONTACT_NAME, oaContactName);
        model.put("oldState", oldState);
        model.put("newState", newState);
        model.put(Dict.ASSESS_DEMAND_URL, assessDemandUrl + id);//需求链接地址
        HashMap<String, String> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailService.getOverDueMsg(template, model));
        try {
            mailService.sendEmail(sb.toString(), template, sendMap, target);
        } catch (Exception e) {
            logger.error("------conf同步需求状态变更邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    public void sendEmailConfRemind(String confTitle, String confUrl, String[] target) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("【请关注confluence】《");
        sb.append(confTitle);
        sb.append("》已批准，快去补充正确的需求编号");
        String template = "conf_assess_remind";
        HashMap<String, Object> model = new HashMap<>();
        model.put("conf_title", confTitle);
        model.put("conf_url", confUrl);
        HashMap<String, String> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailService.getOverDueMsg(template, model));
        try {
            mailService.sendEmail(sb.toString(), template, sendMap, target);
        } catch (Exception e) {
            logger.error("------conf同步提醒邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    public void sendEmailApplyApprove(FdevImplementUnit fdevImplementUnit, DemandBaseInfo demandBaseInfo, FdevUnitApprove fdevUnitApprove, List<String> ccEmailList) throws Exception {
        //固定收件人
        List<String> emailList = new ArrayList<>();
        emailList.addAll(fdevUnitApproveUserEmail);
        //获取条线经理邮箱
        emailList.addAll(CommonUtils.appendGroupReceiverNew(sectionInfo, fdevUnitApprove.getSectionId()));
        StringBuilder sb = new StringBuilder();
        sb.append("【研发单元申请审批】");
        sb.append(fdevImplementUnit.getFdev_implement_unit_no());
        String template = "fdev_unit_approve";
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.DEMANDNO, demandBaseInfo.getOa_contact_no());
        model.put(Dict.DEMANDNAME, demandBaseInfo.getOa_contact_name());
        model.put(Dict.FDEVUNITNO, fdevImplementUnit.getFdev_implement_unit_no());
        model.put(Dict.FDEVUNITNAME, fdevImplementUnit.getImplement_unit_content());
        model.put(Dict.MYAPPROVEURL, myApproveUrl);//我的审批Url
        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailService.getOverDueMsg(template, model));
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(sb.toString(), template, sendMap, emailList , ccEmailList , filePaths);
        } catch (Exception e) {
            logger.error("------研发单元审批邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    public void sendEmailApproveReject(FdevImplementUnit fdevImplementUnit, DemandBaseInfo demandBaseInfo, String approveRejectReason, List<String> emailList) throws Exception {
        //固定收件人
        emailList.addAll(fdevUnitApproveUserEmail);
        StringBuilder sb = new StringBuilder();
        sb.append("【研发单元审批拒绝】");
        sb.append(fdevImplementUnit.getFdev_implement_unit_no());
        String template = "fdev_unit_approve_reject";
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.DEMANDNO, demandBaseInfo.getOa_contact_no());//需求编号
        model.put(Dict.DEMANDNAME, demandBaseInfo.getOa_contact_name());//需求名称
        model.put(Dict.FDEVUNITNO, fdevImplementUnit.getFdev_implement_unit_no());//研发单元编号
        model.put(Dict.FDEVUNITNAME, fdevImplementUnit.getImplement_unit_content());//研发单元内容
        model.put(Dict.APPROVEREJECTREASONKEY, "");//拒绝原因
        model.put(Dict.APPROVEREASON, "");//拒绝原因
        model.put(Dict.HISTORY_DEMAND_URL, history_demand_url + fdevImplementUnit.getDemand_id());//需求链接地址
        //拒绝原因不为空
        if (!CommonUtils.isNullOrEmpty(approveRejectReason)) {
            model.put(Dict.APPROVEREJECTREASONKEY, "拒绝原因：");//拒绝原因
            model.put(Dict.APPROVEREASON, approveRejectReason);//拒绝原因
        }
        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailService.getOverDueMsg(template, model));
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(sb.toString(), template, sendMap, emailList, filePaths);
        } catch (Exception e) {
            logger.error("------研发单元审批拒绝邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    public void sendEmailFdevUnitOverdue(List<String> emailList, String titleHead, String fdevUnitNo, String fdevUnitName
            , String demandNo, String demandName, String demandId, String emailContent) throws Exception {
        //固定收件人
        emailList.addAll(fdevUnitApproveUserEmail);
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(titleHead).append("】");
        sb.append(fdevUnitNo);//研发单元编号
        String template = "fdev_unit_overdue";
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.DEMANDNO, demandNo);//需求编号
        model.put(Dict.DEMANDNAME, demandName);//需求名称
        model.put(Dict.FDEVUNITNO, fdevUnitNo);//研发单元编号
        model.put(Dict.FDEVUNITNAME, fdevUnitName);//研发单元内容
        model.put(Dict.EMAIL_CONTENT, emailContent);//邮件内容
        model.put(Dict.HISTORY_DEMAND_URL, history_demand_url + demandId);//需求链接地址

        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailService.getOverDueMsg(template, model));
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(sb.toString(), template, sendMap, emailList, filePaths);
        } catch (Exception e) {
            logger.error("------研发单元延期邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    public void sendEmailApprovePass(FdevImplementUnit fdevImplementUnit, DemandBaseInfo demandBaseInfo, List<String> emailList) throws Exception {
        //固定收件人
        emailList.addAll(fdevUnitApproveUserEmail);
        StringBuilder sb = new StringBuilder();
        sb.append("【研发单元审批通过】");
        sb.append(fdevImplementUnit.getFdev_implement_unit_no());
        String template = "fdev_unit_approve_pass";
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.DEMANDNO, demandBaseInfo.getOa_contact_no());//需求编号
        model.put(Dict.DEMANDNAME, demandBaseInfo.getOa_contact_name());//需求名称
        model.put(Dict.FDEVUNITNO, fdevImplementUnit.getFdev_implement_unit_no());//研发单元编号
        model.put(Dict.FDEVUNITNAME, fdevImplementUnit.getImplement_unit_content());//研发单元内容
        model.put(Dict.HISTORY_DEMAND_URL, history_demand_url + fdevImplementUnit.getDemand_id());//需求链接地址
        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailService.getOverDueMsg(template, model));
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(sb.toString(), template, sendMap, emailList, filePaths);
        } catch (Exception e) {
            logger.error("------研发单元审批通过邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    public void sendEmailImplProDelay(IpmpUnit ipmpUnit, DemandBaseInfo demandBaseInfo, Map<String, Map> userMapAll) throws Exception {
        if (!ipmpEmail) return;//邮件开关 false 则不发送邮件
        String template = "impl_product_delay";
        String implContent = ipmpUnit.getImplContent();//实施单元内容
        String implUnitNum = ipmpUnit.getImplUnitNum();//实施单元编号
        String implLeader = ipmpUnit.getImplLeader();//牵头人域账号
        String implLeaderGroup = ipmpUnit.getLeaderGroup();//实施单元牵头小组
        HashSet<String> demandLeader = demandBaseInfo.getDemand_leader();//需求牵头人id集合

        if (!CommonUtils.isNullOrEmpty(implLeader) && !CommonUtils.isNullOrEmpty(demandBaseInfo) && !CommonUtils.isNullOrEmpty(demandBaseInfo.getDemand_flag())) {
            List<String> implLeaders = Arrays.asList(implLeader.split(","));
            Set<String> implLeaderSet = new HashSet<>();
            implLeaderSet.addAll(implLeaders);
            List<String> emailList = new ArrayList<>();//邮件收件人
            //邮件发送人：需求牵头人、实施单元牵头人、吴磊、杨静、对应团队负责人（三级组团队负责人角色）
            emailList.addAll(proDelayEamilReceiver);
            //获取牵头小组所在的三级组的团队负责人
            //查询实施单元牵头人信息
            if (CommonUtils.isNullOrEmpty(userMapAll)) {
                userMapAll = fdevUserService.queryByUserCoreData(null, implLeaderSet);
            }
            //查询需求牵头人信息、查询团队负责人信息
            Map<String, Map> demandLeadersInfo = fdevUserService.queryByUserCoreData(demandLeader, null);
            Map<String, Object> threeLevelGroup = fdevUserService.getThreeLevelGroup(implLeaderGroup);
            //获取三级组的团队负责人
            List<Map> groupManagersInfo = roleService.queryGroupManage((String) threeLevelGroup.get(Dict.ID), groupManageRoleId);
            for (String demandLeaderId : demandLeader) {
                if (!CommonUtils.isNullOrEmpty(demandLeadersInfo.get(demandLeaderId))) {
                    emailList.add((String) demandLeadersInfo.get(demandLeaderId).get(Dict.EMAIL));
                }
            }
            for (Map user : groupManagersInfo) {
                emailList.add((String) user.get(Dict.EMAIL));
            }
            List<String> implLeaderIds = new ArrayList<>();//实施单元牵头人IDS
            //获取邮箱
            for (String implLeaderEn : implLeaderSet) {
                if (!CommonUtils.isNullOrEmpty(userMapAll.get(implLeaderEn))) {
                    Map userMap = userMapAll.get(implLeaderEn);
                    emailList.add((String) userMap.get(Dict.EMAIL));
                    implLeaderIds.add((String) userMap.get(Dict.ID));
                }
            }
            if (!CommonUtils.isNullOrEmpty(emailList)) {
                //拼接邮件主题
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("【").append("调整计划投产日期】[").append(demandBaseInfo.getOa_contact_no() + "] ").append("[" + implUnitNum + "] ").append("[" + implContent).append("]");
                //待办type
                StringBuffer stringBufferType = new StringBuffer();
                stringBufferType.append("implDelay");
                //待办内容
                StringBuffer stringBufferContent = new StringBuffer();
                stringBufferContent.append("需求 ").append(demandBaseInfo.getOa_contact_no()).append(" 下 ").append(implContent).append("实施单元");
                //倒序判断发送哪个阶段的邮件待办
                stringBufferContent.append("投产已延期，请申请调整计划投产日期。");

                //给用户添加待办
                fdevUserService.addCommissionEvent(demandBaseInfo, implLeaderIds, stringBufferType.toString(), history_ipmpList_url, stringBufferContent.toString());
                //roleService.sendNotify(implLeaders, content, demandBaseInfo.getId());
                stringBuffer.append("投产延期");
                String subject = stringBuffer.toString();
                List<String> filePaths = new ArrayList<>();
                filePaths.add(filePath);
                if (!CommonUtils.isNullOrEmpty(filePaths)) {
                    //组装邮件正文
                    HashMap<String, Object> model = new HashMap<>();
                    model.put(Dict.IMPLCONTENT, implContent);
                    model.put("ipmpListUrl", history_ipmpList_url);//实施单元链接地址
                    String mailContent = mailService.getOverDueMsg(template, model);
                    HashMap<String, Object> sendMap = new HashMap<>();
                    sendMap.put(Dict.EMAIL_CONTENT, mailContent);
                    try {
                        //发送邮件
                        mailService.sendEmail(subject, template, sendMap, emailList, filePaths);
                        //更新邮件标识为已发送
//                        ipmpUnitDao.updateIpmpUnit(ipmpUnit);
                    } catch (Exception e) {
                        logger.error("#########发送实施单元投产延期提醒调整排期邮件失败########" + e.getMessage());
                        throw new FdevException("#########发送实施单元延期提醒调整排期失败########");
                    }
                }
            }
        }
    }

    @Override
    public void sendSubmitTestOrder(HashMap<String,Object> testOrder,List<String> testOrderFileList,List<String> toEmailList , List<String> ccEmailList) throws Exception {
        //处理邮箱
        CommonUtils.manageEmailList(toEmailList);//收件人
        CommonUtils.manageEmailList(ccEmailList);//抄送人
        StringBuilder sb = new StringBuilder();
        //所属系统
        if(!CommonUtils.isNullOrEmpty(testOrder.get(Dict.SYSTEMNAME))){
            sb.append("【");
            sb.append(testOrder.get(Dict.SYSTEMNAME));
            sb.append("】");
        }
        sb.append("【");
        sb.append(testOrder.get(Dict.GROUP_CN));
        sb.append("】【提交业务测试】");
        sb.append(testOrder.get(Dict.OA_CONTACT_NO));
        sb.append("-");
        sb.append(testOrder.get(Dict.OA_CONTACT_NAME));
        String template = "submit_test_order";
        HashMap<String, Object> sendMap = new HashMap<>();
        String daily_cc_user_name = "";
        if (!CommonUtils.isNullOrEmpty(testOrder.get(Dict.DAILY_CC_USER_INFO))) {
            List<Map> daily_cc_user_info = (List<Map>) testOrder.get(Dict.DAILY_CC_USER_INFO);
            for (Map userInfo : daily_cc_user_info) {
                if (CommonUtils.isNullOrEmpty(daily_cc_user_name)) {
                    daily_cc_user_name = (String) userInfo.get(Dict.USER_NAME_CN);
                } else {
                    daily_cc_user_name += "," + userInfo.get(Dict.USER_NAME_CN);
                }
            }
        }
        testOrder.put(Dict.DAILY_CC_USER_NAME, daily_cc_user_name);
        //yes no 转成 是否
        testOrder.put(Dict.TRANS_INTERFACE_CHANGE, Constants.YES.equals(testOrder.get(Dict.TRANS_INTERFACE_CHANGE)) ? "是" : "否" );
        testOrder.put(Dict.DATABASE_CHANGE, Constants.YES.equals(testOrder.get(Dict.DATABASE_CHANGE)) ? "是" : "否" );
        testOrder.put(Dict.REGRESS_TEST, Constants.YES.equals(testOrder.get(Dict.REGRESS_TEST)) ? "是" : "否" );
        testOrder.put(Dict.CLIENT_CHANGE, Constants.YES.equals(testOrder.get(Dict.CLIENT_CHANGE)) ? "是" : "否" );
        String test_content = (String) testOrder.get(Dict.TEST_CONTENT);
        testOrder.put(Dict.TEST_CONTENT,test_content.replaceAll("\n","<br/>"));
        String test_environment = (String) testOrder.get(Dict.TEST_ENVIRONMENT);
        testOrder.put(Dict.TEST_ENVIRONMENT,test_environment.replaceAll("\n", "<br/>&nbsp;&nbsp;"));
        testOrder.put(Dict.SUBMIT_USER, ((Map)testOrder.get(Dict.TEST_USER_INFO)).get(Dict.USER_NAME_CN));
        sendMap.put(Dict.EMAIL_CONTENT, mailService.getOverDueMsg(template, testOrder));

        try {
            mailService.sendEmail(sb.toString(), template, sendMap, toEmailList,ccEmailList ,testOrderFileList);
        } catch (Exception e) {
            logger.error("------提测单提测邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    public void sendUpdateTestOrder(HashMap<String,Object> testOrder,List<String> testOrderFileList,List<String> toEmailList , List<String> ccEmailList , User user ) throws Exception {
        //处理邮箱
        CommonUtils.manageEmailList(toEmailList);//收件人
        CommonUtils.manageEmailList(ccEmailList);//抄送人
        StringBuilder sb = new StringBuilder();
        //所属系统
        if(!CommonUtils.isNullOrEmpty(testOrder.get(Dict.SYSTEMNAME))){
            sb.append("【");
            sb.append(testOrder.get(Dict.SYSTEMNAME));
            sb.append("】");
        }
        sb.append("【");
        sb.append(testOrder.get(Dict.GROUP_CN));
        sb.append("】【提测单信息修改】");
        sb.append(testOrder.get(Dict.OA_CONTACT_NO));
        sb.append("-");
        sb.append(testOrder.get(Dict.OA_CONTACT_NAME));
        String template = "update_test_order";
        HashMap<String, Object> sendMap = new HashMap<>();
        String daily_cc_user_name = "";
        if (!CommonUtils.isNullOrEmpty(testOrder.get(Dict.DAILY_CC_USER_INFO))) {
            List<Map> daily_cc_user_info = (List<Map>) testOrder.get(Dict.DAILY_CC_USER_INFO);
            for (Map userInfo : daily_cc_user_info) {
                if (CommonUtils.isNullOrEmpty(daily_cc_user_name)) {
                    daily_cc_user_name = (String) userInfo.get(Dict.USER_NAME_CN);
                } else {
                    daily_cc_user_name += "," + userInfo.get(Dict.USER_NAME_CN);
                }
            }
        }
        testOrder.put(Dict.DAILY_CC_USER_NAME, daily_cc_user_name);
        //yes no 转成 是否
        testOrder.put(Dict.TRANS_INTERFACE_CHANGE, Constants.YES.equals(testOrder.get(Dict.TRANS_INTERFACE_CHANGE)) ? "是" : "否" );
        testOrder.put(Dict.DATABASE_CHANGE, Constants.YES.equals(testOrder.get(Dict.DATABASE_CHANGE)) ? "是" : "否" );
        testOrder.put(Dict.REGRESS_TEST, Constants.YES.equals(testOrder.get(Dict.REGRESS_TEST)) ? "是" : "否" );
        testOrder.put(Dict.CLIENT_CHANGE, Constants.YES.equals(testOrder.get(Dict.CLIENT_CHANGE)) ? "是" : "否" );
        testOrder.put(Dict.USER_NAME_CN, user.getUser_name_cn() );//修改人名称
        String test_content = (String) testOrder.get(Dict.TEST_CONTENT);
        testOrder.put(Dict.TEST_CONTENT,test_content.replaceAll("\n", "<br/>"));
        String test_environment = (String) testOrder.get(Dict.TEST_ENVIRONMENT);
        testOrder.put(Dict.TEST_ENVIRONMENT,test_environment.replaceAll("\n", "<br/>&nbsp;&nbsp;"));
        testOrder.put(Dict.SUBMIT_USER, ((Map)testOrder.get(Dict.TEST_USER_INFO)).get(Dict.USER_NAME_CN));
        sendMap.put(Dict.EMAIL_CONTENT, mailService.getOverDueMsg(template, testOrder));
        try {
            mailService.sendEmail(sb.toString(), template, sendMap, toEmailList,ccEmailList ,testOrderFileList);
        } catch (Exception e) {
            logger.error("------提测单编辑邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    public void sendFileTestOrder(TestOrder testOrder,List<String> toEmailList , List<String> ccEmailList ) throws Exception {
        //处理邮箱
        CommonUtils.manageEmailList(toEmailList);//收件人
        CommonUtils.manageEmailList(ccEmailList);//抄送人
        //删除归档邮件中的 固定抄送人
        if( !CommonUtils.isNullOrEmpty(ccEmailList) ){
            for (String ccEmail : testOrderCc) {
                ccEmailList.remove(ccEmail);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("【");
        sb.append(testOrder.getGroup_cn());
        sb.append("】【归档提测单】");
        sb.append(testOrder.getTest_order());
        String template = "file_test_order";
        HashMap<String, Object> sendMap = new HashMap<>();
        HashMap<String, Object> testOrderMap = new HashMap<>();
        testOrderMap.put(Dict.DEMANDNO, testOrder.getOa_contact_no());//需求编号
        testOrderMap.put(Dict.DEMANDNAME, testOrder.getOa_contact_name());//需求名称
        testOrderMap.put(Dict.FDEVUNITNO, testOrder.getFdev_implement_unit_no());//研发单元
        testOrderMap.put(Dict.TEST_ORDER, testOrder.getTest_order());//提测单编号
        testOrderMap.put(Dict.HISTORY_DEMAND_URL, history_demand_url + testOrder.getDemand_id());//需求链接地址
        testOrderMap.put(Dict.X_TEST_URL, x_test_url);//云测试平台链接
        sendMap.put(Dict.EMAIL_CONTENT, mailService.getOverDueMsg(template, testOrderMap));
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(sb.toString(), template, sendMap, toEmailList,ccEmailList,filePaths);
        } catch (Exception e) {
            logger.error("------提测单归档邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    public void sendDeletedTestOrder(TestOrder testOrder,List<String> toEmailList , List<String> ccEmailList , User user ) throws Exception {
        //处理邮箱
        CommonUtils.manageEmailList(toEmailList);//收件人
        CommonUtils.manageEmailList(ccEmailList);//抄送人
        StringBuilder sb = new StringBuilder();
        //所属系统
        if(!CommonUtils.isNullOrEmpty(testOrder.getSystemName())){
            sb.append("【");
            sb.append(testOrder.getSystemName());
            sb.append("】");
        }
        sb.append("【");
        sb.append(testOrder.getGroup_cn());
        sb.append("】【撤销提测单】");
        sb.append(testOrder.getOa_contact_no());
        sb.append("-");
        sb.append(testOrder.getOa_contact_name());
        String template = "deleted_test_order";
        HashMap<String, Object> sendMap = new HashMap<>();
        HashMap<String, Object> testOrderMap = new HashMap<>();
        testOrderMap.put(Dict.USER_NAME_CN, user.getUser_name_cn());//撤销人名称
        testOrderMap.put(Dict.DEMANDNO, testOrder.getOa_contact_no());//需求编号
        testOrderMap.put(Dict.DEMANDNAME, testOrder.getOa_contact_name());//需求名称
        testOrderMap.put(Dict.FDEVUNITNO, testOrder.getFdev_implement_unit_no());//研发单元
        testOrderMap.put(Dict.TEST_ORDER, testOrder.getTest_order());//提测单编号
        testOrderMap.put(Dict.HISTORY_DEMAND_URL, history_demand_url + testOrder.getDemand_id());//需求链接地址
        testOrderMap.put(Dict.X_TEST_URL, x_test_url);//云测试平台链接
        sendMap.put(Dict.EMAIL_CONTENT, mailService.getOverDueMsg(template, testOrderMap));
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(sb.toString(), template, sendMap, toEmailList,ccEmailList,filePaths);
        } catch (Exception e) {
            logger.error("------提测单撤销邮件发送失败------" + e.getMessage());
        }
    }

    @Override
    public void sendFdevUnitWarnDelay(FdevImplementUnit fdevImplementUnit, DemandBaseInfo demandBaseInfo, List<String> emailList) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("【研发单元提测提醒】");
        sb.append(fdevImplementUnit.getFdev_implement_unit_no());
        String template = "fdev_unit_warn_delay";
        HashMap<String, Object> model = new HashMap<>();
        model.put(Dict.DEMANDNO, demandBaseInfo.getOa_contact_no());//需求编号
        model.put(Dict.DEMANDNAME, demandBaseInfo.getOa_contact_name());//需求名称
        model.put(Dict.FDEVUNITNO, fdevImplementUnit.getFdev_implement_unit_no());//研发单元编号
        model.put(Dict.FDEVUNITNAME, fdevImplementUnit.getImplement_unit_content());//研发单元内容
        model.put(Dict.HISTORY_DEMAND_URL, history_demand_url + demandBaseInfo.getId());//需求链接地址

        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put(Dict.EMAIL_CONTENT, mailService.getOverDueMsg(template, model));
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        try {
            mailService.sendEmail(sb.toString(), template, sendMap, emailList, filePaths);
        } catch (Exception e) {
            logger.error("------研发单元提醒延期邮件发送失败------" + e.getMessage());
        }
    }

}
