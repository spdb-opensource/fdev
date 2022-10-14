package com.spdb.fdev.codeReview.spdb.service.impl;

import com.spdb.fdev.codeReview.base.dict.ErrorConstants;
import com.spdb.fdev.codeReview.base.dict.OrderEnum;
import com.spdb.fdev.codeReview.base.utils.CommonUtils;
import com.spdb.fdev.codeReview.base.utils.UserUtil;
import com.spdb.fdev.codeReview.spdb.dao.IMeetingDao;
import com.spdb.fdev.codeReview.spdb.dao.IOrderDao;
import com.spdb.fdev.codeReview.spdb.dao.IProblemDao;
import com.spdb.fdev.codeReview.spdb.entity.CodeMeeting;
import com.spdb.fdev.codeReview.spdb.entity.CodeOrder;
import com.spdb.fdev.codeReview.spdb.entity.CodeProblem;
import com.spdb.fdev.codeReview.spdb.service.IMeetingService;
import com.spdb.fdev.codeReview.spdb.service.IOrderService;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
@Service
public class MeetingServiceImpl implements IMeetingService {

    @Autowired
    IMeetingDao meetingDao;

    @Autowired
    IOrderDao orderDao;

    @Autowired
    IProblemDao problemDao;

    @Autowired
    UserUtil userUtil;
    @Autowired
    IOrderService orderService;

    @Value("${code.auditor.role.id}")
    private String codeAuditorRoleId;

    @Override
    public List<CodeMeeting> queryMeetingByOrderId(String orderId) {
        return meetingDao.queryMeetingByOrderId(orderId);
    }

    @Override
    public Map<String, Object> queryMeetings(String orderId) throws Exception {
        Map<String,Object> result = new HashMap<>();
        CodeOrder codeOrder = orderDao.queryOrderById(orderId);
        List<CodeMeeting> codeMeetings = meetingDao.queryMeetingByOrderId(orderId);
        //获取所有会议审核人中文名
        Set<String> userIds = new HashSet<>();
        for(CodeMeeting codeMeeting : codeMeetings){
            userIds.addAll(codeMeeting.getAuditeUsers());
        }
        Map userInfo = userUtil.getUserByIds(userIds);
        for(CodeMeeting codeMeeting : codeMeetings){
            //给每条数据添加更新和删除按钮标识，代码审核角色且工单不为终态时可更新，代码审核角色且工单不为终态且会议下无问题描述可删除
            codeMeeting.setUpdateButton(getUpdateButton(codeOrder));
            codeMeeting.setDeleteButton(getDeleteButton(codeOrder, codeMeeting.getId()));
            //封装拼接审核人姓名集合字符串
            String auditorUsersCn = "";
            for(String userId : codeMeeting.getAuditeUsers()){
                auditorUsersCn += userUtil.getUserNameById(userId,userInfo) + "、";
            }
            auditorUsersCn = auditorUsersCn.substring(0, auditorUsersCn.length() - 1);
            codeMeeting.setAuditorUsersCn(auditorUsersCn);
        }
        //添加新增按钮标识，代码审核角色且工单不为终态时可新增
        result.put("addButton",getAddButton(codeOrder));
        result.put("meetingList", codeMeetings);
        return result;
    }

    @Override
    public void deleteMeetingById(String id) throws Exception {
        //非代码审核角色不可删除
        User sessionUser = CommonUtils.getSessionUser();
        if(!sessionUser.getRole_id().contains(codeAuditorRoleId)){
            throw new FdevException(ErrorConstants.CODE_ROLE_CAN_DO);
        }
        //工单终态下不可删除
        CodeMeeting codeMeeting = meetingDao.queryMeetingById(id);
        CodeOrder codeOrder = orderDao.queryOrderById(codeMeeting.getOrderId());
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            throw new FdevException(ErrorConstants.ORDER_END_STATUS_CAN_NOT_DO);
        }
        //会议下有问题则不能删除
        List<CodeProblem> problems = problemDao.queryProblemsByMeetingId(id);
        if(!CommonUtils.isNullOrEmpty(problems)){
            throw new FdevException(ErrorConstants.HAVE_PROBLEM_CAN_NOT_DEL);
        }
        //删除会议
        meetingDao.deleteMeetingById(id);
        //若工单下无会议，则将工单置为初始状态“待审核”
        List<CodeMeeting> codeMeetings = meetingDao.queryMeetingByOrderId(codeOrder.getId());
        if(CommonUtils.isNullOrEmpty(codeMeetings)){
            codeOrder.setOrderStatus(OrderEnum.OrderStatusEnum.TO_AUDIT.getValue());
            orderDao.update(codeOrder);
        }
        //更新工单的审核人字段
        orderService.updateAuditUser(codeMeeting.getOrderId());
    }

    @Override
    public void add(CodeMeeting codeMeeting) throws Exception {
        //仅代码审核角色可新增
        User sessionUser = CommonUtils.getSessionUser();
        if(!sessionUser.getRole_id().contains(codeAuditorRoleId)){
            throw new FdevException(ErrorConstants.CODE_ROLE_CAN_DO);//仅代码审核角色可新增
        }
        //工单终态下不可新增
        CodeOrder codeOrder = orderDao.queryOrderById(codeMeeting.getOrderId());
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            throw new FdevException(ErrorConstants.ORDER_END_STATUS_CAN_NOT_DO);//工单终态下不可新增会议
        }
        //新增会议
        meetingDao.add(codeMeeting);
        //如果是本工单的第一条会议记录，则需要将工单状态改为审核中
        List<CodeMeeting> codeMeetings = meetingDao.queryMeetingByOrderId(codeMeeting.getOrderId());
        if(codeMeetings.size() == 1 && (codeOrder.getOrderStatus() <= OrderEnum.OrderStatusEnum.AUDITING.getValue())){
            codeOrder.setOrderStatus(OrderEnum.OrderStatusEnum.AUDITING.getValue());
            orderDao.update(codeOrder);
        }
        //更新工单的审核人字段
        orderService.updateAuditUser(codeMeeting.getOrderId());
    }

    /**
     * 更新会议信息，所属工单不可更改
     * @param codeMeeting
     */
    @Override
    public void update(CodeMeeting codeMeeting) throws Exception {
        //仅代码审核角色可编辑
        User sessionUser = CommonUtils.getSessionUser();
        if(!sessionUser.getRole_id().contains(codeAuditorRoleId)){
            throw new FdevException(ErrorConstants.CODE_ROLE_CAN_DO);//仅代码审核角色可更新
        }
        //工单终态不可编辑
        CodeOrder codeOrder = orderDao.queryOrderById(codeMeeting.getOrderId());
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            throw new FdevException(ErrorConstants.ORDER_END_STATUS_CAN_NOT_DO);//工单终态不可编辑
        }
        //更新工单
        meetingDao.update(codeMeeting);
        //更新工单的审核人字段
        orderService.updateAuditUser(codeOrder.getId());
    }

    /**
     * 新增按钮标识，代码审核角色且工单不为终态时可新增
     * @param codeOrder
     * @return
     */
    public String getAddButton(CodeOrder codeOrder) throws Exception {
        User sessionUser = CommonUtils.getSessionUser();
        if(!sessionUser.getRole_id().contains(codeAuditorRoleId)){
            return "1";//仅代码审核角色可新增
        }
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            return "2";//工单终态下不可新增会议
        }
        return "0";
    }

    /**
     * 代码审核角色且工单不为终态时可更新
     * @param codeOrder
     * @return
     */
    public String getUpdateButton(CodeOrder codeOrder) throws Exception {
        User sessionUser = CommonUtils.getSessionUser();
        if(!sessionUser.getRole_id().contains(codeAuditorRoleId)){
            return "1";//仅代码审核角色可更新
        }
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            return "2";//工单终态不可编辑
        }
        return "0";
    }

    /**
     * 代码审核角色且工单不为终态且会议下无问题描述可删除
     * @param codeOrder
     * @return
     */
    public String getDeleteButton(CodeOrder codeOrder, String meetingId) throws Exception {
        User sessionUser = CommonUtils.getSessionUser();
        if(!sessionUser.getRole_id().contains(codeAuditorRoleId)){
            return "1";//仅代码审核角色可删除
        }
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            return "2";//工单终态下不可删除会议
        }
        List<CodeProblem> problems = problemDao.queryProblemsByMeetingId(meetingId);
        if(!CommonUtils.isNullOrEmpty(problems)){
            return "3";//会议下存在问题，不可删除
        }
        return "0";
    }
}
