package com.spdb.fdev.codeReview.spdb.service.impl;

import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.base.dict.OrderEnum;
import com.spdb.fdev.codeReview.base.utils.CommonUtils;
import com.spdb.fdev.codeReview.base.utils.DemandUtil;
import com.spdb.fdev.codeReview.base.utils.MailUtil;
import com.spdb.fdev.codeReview.base.utils.UserUtil;
import com.spdb.fdev.codeReview.spdb.entity.CodeOrder;
import com.spdb.fdev.codeReview.spdb.service.IMailService;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.util.UserVerifyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author liux81
 * @DATE 2021/11/16
 */
@Service
public class MailServiceImpl implements IMailService {
    @Autowired
    UserUtil userUtil;

    @Autowired
    MailUtil mailUtil;

    @Autowired
    DemandUtil damandUtil;

    @Autowired
    UserVerifyUtil userVerifyUtil;

    @Value("${code.order.url}")
    private String orderURL;

    @Value("${code.auditor.role.id}")
    private String codeAuditorRoleId;

    @Override
    public void orderFinishMail(CodeOrder codeOrder) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put(Dict.ORDERNO, codeOrder.getOrderNo());
        map.put(Dict.ORDERSTATUS, CommonUtils.getOrderStatusNameByValue(codeOrder.getOrderStatus()));
        Set<String> demandIds = new HashSet<>();
        demandIds.add(codeOrder.getDemandId());
        Map<String,Map> demandMap = damandUtil.getDemandsInfoByIds(demandIds);
        //封装需求编号_需求名称
        String demandNo = (String) demandMap.get(codeOrder.getDemandId()).get(Dict.OA_CONTACT_NO);
        String demandName = (String) demandMap.get(codeOrder.getDemandId()).get(Dict.OA_CONTANCT_NAME);
        map.put(Dict.DEMANDNAME,demandNo+"_"+demandName);
        map.put(Dict.ORDERURL,orderURL + codeOrder.getId());
        //收件人邮箱集合
        Set<String> reciver = new HashSet<>();
        reciver.add(codeOrder.getLeader());
        Map leaders = userUtil.getUserByIds(reciver);
        List<String> reciverMails = new ArrayList<>();
        reciverMails.add((String) ((Map)leaders.get(codeOrder.getLeader())).get(Dict.EMAIL));
        reciverMails.add(CommonUtils.getSessionUser().getEmail());
        HashMap<String, String> send = new HashMap();
        send.put(Dict.EMAIL_CONTENT,mailUtil.getContent("code_status.ftl",map));
        mailUtil.sendEmail("代码审核工单已完成","code_status.ftl",send,reciverMails.toArray(new String[reciverMails.size()]));
    }

    @Async
    @Override
    public void orderCreateMail(CodeOrder codeOrder, User user) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        Set<String> userIds = new HashSet<>();
        userIds.add(codeOrder.getCreateUser());
        Map userInfo = userUtil.getUserByIds(userIds);
        map.put(Dict.CREATEUSERCN, userUtil.getUserNameById(codeOrder.getCreateUser(),userInfo));
        //封装需求编号_需求名称
        Set<String> demandIds = new HashSet<>();
        demandIds.add(codeOrder.getDemandId());
        Map<String,Map> demandMap = damandUtil.getDemandsInfoByIds(demandIds);
        String demandNo = (String) demandMap.get(codeOrder.getDemandId()).get(Dict.OA_CONTACT_NO);
        String demandName = (String) demandMap.get(codeOrder.getDemandId()).get(Dict.OA_CONTANCT_NAME);
        map.put(Dict.DEMANDNAME, demandNo+"_"+demandName);
        map.put(Dict.ORDERURL,orderURL + codeOrder.getId());
        //收件人邮箱集合
        Set<String> reciverMails = new HashSet<>();
        if(!CommonUtils.isNullOrEmpty(codeOrder.getEmailTo())){
            List<String> emails = userUtil.getEmailByIds(codeOrder.getEmailTo());
            reciverMails.addAll(emails);
        }
        //收件人加上申请人
        reciverMails.add(user.getEmail());
        //收件人加上代码审核人
        Set<String> roleIds = new HashSet<>();
        roleIds.add(codeAuditorRoleId);
        Set<String> codeAuditorEmails = userUtil.getEmailsByRoleId(roleIds);
        reciverMails.addAll(codeAuditorEmails);
        HashMap<String, String> send = new HashMap();
        send.put(Dict.EMAIL_CONTENT,mailUtil.getContent("code_create.ftl",map));
        if(!CommonUtils.isNullOrEmpty(reciverMails)){
            mailUtil.sendEmail("申请代码审核","code_create.ftl",send,reciverMails.toArray(new String[reciverMails.size()]));
        }
    }

    @Override
    public void recheckRemindMail(CodeOrder codeOrder) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        //封装需求编号_需求名称
        Set<String> demandIds = new HashSet<>();
        demandIds.add(codeOrder.getDemandId());
        Map<String,Map> demandMap = damandUtil.getDemandsInfoByIds(demandIds);
        String demandNo = (String) demandMap.get(codeOrder.getDemandId()).get(Dict.OA_CONTACT_NO);
        String demandName = (String) demandMap.get(codeOrder.getDemandId()).get(Dict.OA_CONTANCT_NAME);
        map.put(Dict.DEMANDNAME, demandNo+"_"+demandName);
        map.put(Dict.ORDERURL,orderURL + codeOrder.getId());
        map.put(Dict.ORDERNO, codeOrder.getOrderNo());
        map.put(Dict.ORDERSTATUS, CommonUtils.getOrderStatusNameByValue(codeOrder.getOrderStatus()));
        //收件人邮箱集合
        Set<String> reciver = new HashSet<>();
        reciver.add(codeOrder.getLeader());
        reciver.add(codeOrder.getCreateUser());
        User sessionUser = CommonUtils.getSessionUser();
        reciver.add(sessionUser.getId());
        Map usersInfo = userUtil.getUserByIds(reciver);
        List<String> reciverMails = new ArrayList<>();
        for(String userId : reciver){
            reciverMails.add((String) ((Map)usersInfo.get(userId)).get(Dict.EMAIL));
        }
        HashMap<String, String> send = new HashMap();
        send.put(Dict.EMAIL_CONTENT,mailUtil.getContent("code_recheck_remind.ftl",map));
        if(!CommonUtils.isNullOrEmpty(reciverMails)){
            mailUtil.sendEmail("代码复审提醒","code_recheck_remind.ftl",send,reciverMails.toArray(new String[reciverMails.size()]));
        }
    }

    @Override
    public void applyRecheckMail(CodeOrder codeOrder) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        //封装需求编号_需求名称
        Set<String> demandIds = new HashSet<>();
        demandIds.add(codeOrder.getDemandId());
        Map<String,Map> demandMap = damandUtil.getDemandsInfoByIds(demandIds);
        String demandNo = (String) demandMap.get(codeOrder.getDemandId()).get(Dict.OA_CONTACT_NO);
        String demandName = (String) demandMap.get(codeOrder.getDemandId()).get(Dict.OA_CONTANCT_NAME);
        map.put(Dict.DEMANDNAME, demandNo+"_"+demandName);
        map.put(Dict.ORDERURL,orderURL + codeOrder.getId());
        //申请人姓名
        User sessionUser = CommonUtils.getSessionUser();
        map.put(Dict.APPLYUSERNAME, sessionUser.getUser_name_cn());
        //收件人邮箱集合,代码审核角色，当前登录用户
        Set<String> reciver = new HashSet<>();
        reciver.add(sessionUser.getId());
        Map usersInfo = userUtil.getUserByIds(reciver);
        List<String> reciverMails = new ArrayList<>();
        reciverMails.add((String) ((Map)usersInfo.get(sessionUser.getId())).get(Dict.EMAIL));
        Set<String> roleSet = new HashSet<>();
        roleSet.add(codeAuditorRoleId);
        reciverMails.addAll(userUtil.getEmailsByRoleId(roleSet));
        HashMap<String, String> send = new HashMap();
        send.put(Dict.EMAIL_CONTENT,mailUtil.getContent("code_apply_recheck.ftl",map));
        if(!CommonUtils.isNullOrEmpty(reciverMails)){
            mailUtil.sendEmail("申请代码复审","code_apply_recheck.ftl",send,reciverMails.toArray(new String[reciverMails.size()]));
        }
    }


}
