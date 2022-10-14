package com.spdb.fdev.codeReview.spdb.service.impl;

import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.base.dict.ErrorConstants;
import com.spdb.fdev.codeReview.base.dict.OrderEnum;
import com.spdb.fdev.codeReview.base.utils.CommonUtils;
import com.spdb.fdev.codeReview.base.utils.DemandUtil;
import com.spdb.fdev.codeReview.base.utils.TimeUtil;
import com.spdb.fdev.codeReview.base.utils.UserUtil;
import com.spdb.fdev.codeReview.spdb.dao.IDictDao;
import com.spdb.fdev.codeReview.spdb.dao.IMeetingDao;
import com.spdb.fdev.codeReview.spdb.dao.IOrderDao;
import com.spdb.fdev.codeReview.spdb.dao.IProblemDao;
import com.spdb.fdev.codeReview.spdb.dto.ProblemDto;
import com.spdb.fdev.codeReview.spdb.entity.CodeMeeting;
import com.spdb.fdev.codeReview.spdb.entity.CodeOrder;
import com.spdb.fdev.codeReview.spdb.entity.CodeProblem;
import com.spdb.fdev.codeReview.spdb.service.IProblemService;
import com.spdb.fdev.common.exception.FdevException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
@Service
public class ProblemServiceImpl implements IProblemService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IOrderDao orderDao;

    @Autowired
    IProblemDao problemDao;

    @Autowired
    IMeetingDao meetingDao;

    @Autowired
    DemandUtil demandUtil;

    @Autowired
    UserUtil userUtil;

    @Autowired
    IDictDao dictDao;

    @Override
    public Map<String, Object> queryProblems(String orderId) {
        //判断工单是否处于终态，终态下不可新增、编辑、删除
        String buttonFlag = "0";
        CodeOrder codeOrder = orderDao.queryOrderById(orderId);
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            buttonFlag = "1";
        }
        //查询工单下的问题列表
        List<CodeMeeting> codeMeetings = meetingDao.queryMeetingByOrderId(orderId);
        Set<String> meetingIds = new HashSet<>();
        for(CodeMeeting codeMeeting : codeMeetings){
            meetingIds.add(codeMeeting.getId());
        }
        List<CodeProblem> problemList = problemDao.queryProblemsByMeetingIds(meetingIds,null, null);
        for(CodeProblem problem : problemList){
            //封装修改和删除按钮标识
            problem.setUpdateButton(buttonFlag);
            problem.setDeleteButton(buttonFlag);
            //封装问题项内容
            if(!CommonUtils.isNullOrEmpty(problem.getItemType())){
                problem.setItemTypeValue(dictDao.queryDictByKey(problem.getItemType()).getValue());
            }
        }
        //封装新增按钮标识
        Map<String,Object> result = new HashMap<>();
        result.put(Dict.ADDBUTTON,buttonFlag);
        result.put(Dict.PROBLEMLIST,problemList);
        return result;
    }

    /**
     * 删除问题，工单终态时不可删除
     * @param id
     */
    @Override
    public void deleteProblemById(String id) {
        CodeProblem problem = problemDao.queryProblemById(id);
        CodeMeeting codeMeeting = meetingDao.queryMeetingById(problem.getMeetingId());
        CodeOrder codeOrder = orderDao.queryOrderById(codeMeeting.getOrderId());
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            throw new FdevException(ErrorConstants.ORDER_END_STATUS_CAN_NOT_DO);
        }
        problemDao.deleteProblemById(id);
    }

    /**
     * 工单终态下不可新增问题
     * @param problem
     */
    @Override
    public void add(CodeProblem problem) throws Exception {
        CodeMeeting codeMeeting = meetingDao.queryMeetingById(problem.getMeetingId());
        CodeOrder codeOrder = orderDao.queryOrderById(codeMeeting.getOrderId());
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            throw new FdevException(ErrorConstants.ORDER_END_STATUS_CAN_NOT_DO);
        }
        problemDao.add(problem);
    }

    /**
     * 更新问题，工单终态下不可编辑，关联会议不可编辑
     * @param problem
     */
    @Override
    public void update(CodeProblem problem) throws Exception {
        CodeProblem oldProblem = problemDao.queryProblemById(problem.getId());
        CodeMeeting codeMeeting = meetingDao.queryMeetingById(oldProblem.getMeetingId());
        CodeOrder codeOrder = orderDao.queryOrderById(codeMeeting.getOrderId());
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            throw new FdevException(ErrorConstants.ORDER_END_STATUS_CAN_NOT_DO);
        }
        problemDao.update(problem);
    }

    @Override
    public void exportProblemExcel(Map param, HttpServletResponse resp) throws Exception {
        //查询工单下的问题列表
        String orderId = (String) param.get(Dict.ID);
        List<CodeMeeting> codeMeetings = meetingDao.queryMeetingByOrderId(orderId);
        Set<String> meetingIds = new HashSet<>();
        for(CodeMeeting meeting : codeMeetings){
            meetingIds.add(meeting.getId());
        }
        String startDate = null;
        String endDate = null;
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.STARTDATE))){
            startDate = (String)param.get(Dict.STARTDATE);
        }
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.ENDDATE))){
            endDate = (String) param.get(Dict.ENDDATE);
        }
        List<CodeProblem> codeProblems = problemDao.queryProblemsByMeetingIds(meetingIds, startDate, endDate);
        CodeOrder order = orderDao.queryOrderById(orderId);
        // 初始化workbook
        InputStream inputStream = null;
        XSSFWorkbook workbook = null;
        XSSFSheet sheet = null;
        //引入模板
        try {
            ClassPathResource classPathResource=new ClassPathResource("problemList.xlsx");
            inputStream=classPathResource.getInputStream();
            workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);
            logger.info("-------load model ProblemExprotList success-----");

        } catch (Exception e1) {
            logger.error("---export---" + e1);
            throw new FdevException("问题列表导出失败，请联系fdev管理员");
        }
        HashSet<String> userIds = new HashSet<>();
        for (CodeProblem problem : codeProblems) {
            CodeMeeting meeting = meetingDao.queryMeetingById(problem.getMeetingId());
            userIds.addAll(meeting.getAuditeUsers());
        }
        userIds.add(order.getLeader());
        //批量获取用户信息
        Map usersInfo = userUtil.getUserByIds(userIds);
        //获取牵头小组名
        HashSet<String> leaderGroupIds = new HashSet<>();
        leaderGroupIds.add(order.getLeaderGroup());
        Map groupNames = userUtil.getGroupsNameByIds(leaderGroupIds);
        //获取需求编号_需求名称
        Set<String> demandIds = new HashSet<>();
        demandIds.add(order.getDemandId());
        Map<String,Map> demandMap = demandUtil.getDemandsInfoByIds(demandIds);
        String demandNo = (String) demandMap.get(order.getDemandId()).get(Dict.OA_CONTACT_NO);
        String demandName = (String) demandMap.get(order.getDemandId()).get(Dict.OA_CONTANCT_NAME);
        int i=1;//行数
        for (CodeProblem problem : codeProblems) {
            int j=0;//列数
            sheet.createRow(i);
            sheet.getRow(i).createCell(j++).setCellValue(order.getOrderNo());//工单编号
            sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.getOrderStatusNameByValue(order.getOrderStatus()));//工单状态
            sheet.getRow(i).createCell(j++).setCellValue(demandNo + "_" + demandName);//需求名称
            sheet.getRow(i).createCell(j++).setCellValue((String) groupNames.get(order.getLeaderGroup()));//牵头小组
            sheet.getRow(i).createCell(j++).setCellValue(userUtil.getUserNameById(order.getLeader(),usersInfo));//牵头人
            sheet.getRow(i).createCell(j++).setCellValue(order.getApplyTime());//申请时间
            sheet.getRow(i).createCell(j++).setCellValue(order.getAuditFinishTime());//审核完成时间
            CodeMeeting meeting = meetingDao.queryMeetingById(problem.getMeetingId());
            //封装拼接审核人姓名集合字符串
            String auditorUsersCn = "";
            for(String userId : meeting.getAuditeUsers()){
                auditorUsersCn += userUtil.getUserNameById(userId,usersInfo) + "、";
            }
            auditorUsersCn = auditorUsersCn.substring(0, auditorUsersCn.length() - 1);
            sheet.getRow(i).createCell(j++).setCellValue(auditorUsersCn);//审核人

            sheet.getRow(i).createCell(j++).setCellValue(problem.getProblem());//问题描述
            sheet.getRow(i).createCell(j++).setCellValue(problemTypeTransfer(problem.getProblemType()));//问题类型
            if(CommonUtils.isNullOrEmpty(problem.getItemType())){
                sheet.getRow(i).createCell(j++).setCellValue("");//问题项
            }else {
                sheet.getRow(i).createCell(j++).setCellValue(dictDao.queryDictByKey(problem.getItemType()).getValue());//问题项
            }
            sheet.getRow(i).createCell(j++).setCellValue(problem.getProblemNum());//问题次数
            if(!CommonUtils.isNullOrEmpty(problem.getFixFlag())){
                sheet.getRow(i).createCell(j++).setCellValue(fixTransfer(problem.getFixFlag()));//是否已修复
            }else {
                sheet.getRow(i).createCell(j++).setCellValue("");//是否已修复
            }
            sheet.getRow(i).createCell(j++).setCellValue(problem.getFixDate());//修复时间
            sheet.getRow(i).createCell(j++).setCellValue(problem.getRemark());//备注
            i++;
        }
        try {
            resp.reset(); resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setHeader("Content-Disposition", "attachment;filename=" +
                    "problemList"+ ".xlsx");
            workbook.write(resp.getOutputStream()); } catch (IOException e) {
            logger.error("导出问题列表时，数据读写错误" + e.getMessage());
        }
    }

    @Override
    public Map queryAll(Map param) throws Exception {
        Map<String, Object> data = problemDao.queryAll(param);
        long count = (long)data.get(Dict.COUNT);
        List<ProblemDto> list = (List<ProblemDto>)data.get("list");
        //获取所有涉及的工单id、会议id、问题项key、用户id、小组id
        Set<String> orderIds = new HashSet<>();
        Set<String> meetingIds = new HashSet<>();
        Set<String> itemKeys = new HashSet<>();
        Set<String> userIds = new HashSet<>();
        Set<String> groupIds = new HashSet<>();
        for(ProblemDto problem : list){
            meetingIds.add(problem.getMeetingId());
            itemKeys.add(problem.getItemType());
        }
        //批量查询会议信息
        List<CodeMeeting> meetings = meetingDao.queryMeetingByIds(meetingIds);
        //转化为map格式
        Map<String, CodeMeeting> meetingMap = meetings.stream().collect(Collectors.toMap(CodeMeeting::getId, item -> {
            orderIds.add(item.getOrderId());
            userIds.addAll(item.getAuditeUsers());
            return item;
        }));
        //批量查询工单信息
        List<CodeOrder> orders = orderDao.queryOrderByIds(orderIds);
        //转化为map格式、获取所有的涉及需求
        Set<String> demandIds = new HashSet<>();
        Map<String, CodeOrder> orderMap = orders.stream().collect(Collectors.toMap(CodeOrder::getId, item -> {
            demandIds.add(item.getDemandId());
            userIds.add(item.getLeader());
            groupIds.add(item.getLeaderGroup());
            return item;
        }));
        //批量查询需求信息
        Map<String,Map> demandMap = demandUtil.getDemandsInfoByIds(demandIds);
        //批量查询用户信息
        Map userInfos = userUtil.getUserByIds(userIds);
        //批量查询组信息
        Map groupInfos = userUtil.getGroupsNameByIds(groupIds);
        //包装工单编号、需求名称、申请时间、会议时间、问题项内容、
        for(ProblemDto problem : list){
            CodeMeeting codeMeeting = meetingMap.get(problem.getMeetingId());
            CodeOrder order = orderMap.get(codeMeeting.getOrderId());
            problem.setOrderId(order.getId());//工单id
            problem.setOrderNo(order.getOrderNo());//工单编号
            problem.setMeetingTime(codeMeeting.getMeetingTime());//会议时间
            problem.setApplyTime(order.getApplyTime());//申请时间
            problem.setDemandId(order.getDemandId());//需求id
            String demandNo = (String) demandMap.get(order.getDemandId()).get(Dict.OA_CONTACT_NO);
            String demandName = (String) demandMap.get(order.getDemandId()).get(Dict.OA_CONTANCT_NAME);
            problem.setDemandName(demandNo+"_"+demandName);//需求名称
            problem.setOrderStatus(order.getOrderStatus());//工单状态
            problem.setLeaderGroup(order.getLeaderGroup());//牵头小组
            if(!CommonUtils.isNullOrEmpty(problem.getItemType())){
                problem.setItemTypeValue(dictDao.queryDictByKey(problem.getItemType()).getValue());//问题项内容
            }
            problem.setLeaderGroupCn((String) groupInfos.get(order.getLeaderGroup()));//牵头小组中文名
            problem.setLeader(order.getLeader());//牵头人
            problem.setLeaderName((String) ((Map) userInfos.get(order.getLeader())).get(Dict.USER_NAME_CN));//牵头人姓名
            problem.setAuditFinishDate(order.getAuditFinishTime());//审核完成日期
            problem.setAuditeUsers(codeMeeting.getAuditeUsers());//审核人id集合
            String auditeUserNames = "";
            for(String userId : codeMeeting.getAuditeUsers()){
                String userName = ((Map) userInfos.get(userId)).get(Dict.USER_NAME_CN).toString();
                auditeUserNames += userName + "、";
            }
            problem.setAuditorUsersCn(auditeUserNames.substring(0, auditeUserNames.length() - 1));//审核人姓名字符串（以、分隔）
        }
        Map result = new HashMap();
        result.put(Dict.COUNT, count);
        result.put("list", list);
        return result;
    }

    /**
     * 导出所有工单的问题列表
     * 导出结果按照工单、会议时间升序
     * @param param
     * @param resp
     */
    @Override
    public void exportAllProblemExcel(Map param, HttpServletResponse resp) throws Exception {
        Map map = queryAll(param);
        List<ProblemDto> codeProblems = (List<ProblemDto>)map.get("list");
        //按照工单、会议时间升序
        codeProblems = sortProblem(codeProblems);
        // 初始化workbook
        InputStream inputStream = null;
        XSSFWorkbook workbook = null;
        XSSFSheet sheet = null;
        //引入模板
        try {
            ClassPathResource classPathResource=new ClassPathResource("problemListAll.xlsx");
            inputStream=classPathResource.getInputStream();
            workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);
            logger.info("-------load model ProblemExprotList success-----");
        } catch (Exception e1) {
            logger.error("---export---" + e1);
            throw new FdevException("问题列表导出失败，请联系fdev管理员");
        }
        int i=1;//行数
        for (ProblemDto problem : codeProblems) {
            int j=0;//列数
            sheet.createRow(i);
            sheet.getRow(i).createCell(j++).setCellValue(problem.getOrderNo());//工单编号
            sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.getOrderStatusNameByValue(problem.getOrderStatus()));//工单状态
            sheet.getRow(i).createCell(j++).setCellValue(problem.getDemandName());//需求名称
            sheet.getRow(i).createCell(j++).setCellValue(problem.getLeaderGroupCn());//牵头小组
            sheet.getRow(i).createCell(j++).setCellValue(problem.getLeaderName());//牵头人
            sheet.getRow(i).createCell(j++).setCellValue(problem.getApplyTime().substring(0,10));//申请时间
            sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(problem.getAuditFinishDate()) ? "" : problem.getAuditFinishDate().substring(0,10));//审核完成时间
            sheet.getRow(i).createCell(j++).setCellValue(problem.getAuditorUsersCn());//审核人
            sheet.getRow(i).createCell(j++).setCellValue(problem.getMeetingTime().substring(0, 10));//会议时间
            sheet.getRow(i).createCell(j++).setCellValue(problem.getProblem());//问题描述
            sheet.getRow(i).createCell(j++).setCellValue(problemTypeTransfer(problem.getProblemType()));//问题类型
            if(CommonUtils.isNullOrEmpty(problem.getItemType())){
                sheet.getRow(i).createCell(j++).setCellValue("");//问题项
            }else {
                sheet.getRow(i).createCell(j++).setCellValue(problem.getItemTypeValue());//问题项
            }
            sheet.getRow(i).createCell(j++).setCellValue(problem.getProblemNum());//问题次数
            if(!CommonUtils.isNullOrEmpty(problem.getFixFlag())){
                sheet.getRow(i).createCell(j++).setCellValue(fixTransfer(problem.getFixFlag()));//是否已修复
            }else {
                sheet.getRow(i).createCell(j++).setCellValue("");//是否已修复
            }
            sheet.getRow(i).createCell(j++).setCellValue(problem.getFixDate());//修复时间
            sheet.getRow(i).createCell(j++).setCellValue(problem.getCreateTime());//创建时间
            sheet.getRow(i).createCell(j++).setCellValue(problem.getRemark());//备注
            i++;
        }
        try {
            resp.reset(); resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setHeader("Content-Disposition", "attachment;filename=" +
                    "problemListAll"+ ".xlsx");
            workbook.write(resp.getOutputStream()); } catch (IOException e) {
            logger.error("导出问题列表时，数据读写错误" + e.getMessage());
        }
    }

    /**
     * 将数据先按照工单申请时间升序排序，同一个工单下再按照会议时间升序排序
     * @param codeProblems
     * @return
     */
    private List<ProblemDto> sortProblem(List<ProblemDto> codeProblems) {
        codeProblems.sort(new Comparator<ProblemDto>() {
            @Override
            public int compare(ProblemDto o1, ProblemDto o2) {
                if(TimeUtil.compareTime2(o1.getApplyTime(), o2.getApplyTime())){
                    return 1;
                }else if(o1.getApplyTime().equals(o2.getApplyTime())){
                    return 0;
                }
                return -1;
            }
        });
        List<ProblemDto> result = new ArrayList<>();
        List<ProblemDto> temp = new ArrayList<>();
        for(ProblemDto problemDto : codeProblems){
            if(CommonUtils.isNullOrEmpty(temp) || temp.get(0).getOrderNo().equals(problemDto.getOrderNo())){
                temp.add(problemDto);
            }
            if(!CommonUtils.isNullOrEmpty(temp) && !temp.get(0).getOrderNo().equals(problemDto.getOrderNo())){
                //把当前集合按照会议时间升序排列，放入结果集中
                temp.sort(new Comparator<ProblemDto>() {
                    @Override
                    public int compare(ProblemDto o1, ProblemDto o2) {
                        if(TimeUtil.compareTime3(o1.getMeetingTime(), o2.getMeetingTime())){
                            return 1;
                        }else if(o1.getMeetingTime().equals(o2.getMeetingTime())){
                            return 0;
                        }
                        return -1;
                    }
                });
                result.addAll(temp);
                //将当前集合清空
                temp.clear();
                temp.add(problemDto);
            }
        }
        //加入最后一个工单的问题并排序
        if(!CommonUtils.isNullOrEmpty(temp)){
            temp.sort(new Comparator<ProblemDto>() {
                @Override
                public int compare(ProblemDto o1, ProblemDto o2) {
                    if(TimeUtil.compareTime3(o1.getMeetingTime(), o2.getMeetingTime())){
                        return 1;
                    }else if(o1.getMeetingTime().equals(o2.getMeetingTime())){
                        return 0;
                    }
                    return -1;
                }
            });
            result.addAll(temp);
        }
        return result;
    }

    /**
     * 问题类型转义
     */
    public String problemTypeTransfer(String problemType){
        String result = "";
        switch (problemType){
            case "issue" :
                result = "缺陷";
                break;
            case "advice":
                result = "建议";
                break;
            case "risk":
                result = "风险";
                break;
        }
        return result;
    }

    /**
     * 是否已修复转义
     */
    public String fixTransfer(String fixFlag){
        String result = "";
        switch (fixFlag){
            case "fixed" :
                result = "已修复";
                break;
            case "notFixed":
                result = "未修复";
                break;
        }
        return result;
    }

    /**
     * 会议批次转义
     */
    public String meetingTypeTransfer(String meetingType){
        String result = "";
        switch (meetingType){
            case "firstCheck" :
                result = "初审";
                break;
            case "recheck":
                result = "复审";
                break;
        }
        return result;
    }

}
