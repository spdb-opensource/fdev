package com.spdb.fdev.codeReview.spdb.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.codeReview.base.Constant;
import com.spdb.fdev.codeReview.base.dict.Constants;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.base.dict.ErrorConstants;
import com.spdb.fdev.codeReview.base.dict.OrderEnum;
import com.spdb.fdev.codeReview.base.utils.*;
import com.spdb.fdev.codeReview.spdb.dao.IFileDao;
import com.spdb.fdev.codeReview.spdb.dao.IMeetingDao;
import com.spdb.fdev.codeReview.spdb.dao.IOrderDao;
import com.spdb.fdev.codeReview.spdb.dto.ApplyInfo;
import com.spdb.fdev.codeReview.spdb.dto.LogContent;
import com.spdb.fdev.codeReview.spdb.dto.TaskEntity;
import com.spdb.fdev.codeReview.spdb.entity.CodeFile;
import com.spdb.fdev.codeReview.spdb.entity.CodeLog;
import com.spdb.fdev.codeReview.spdb.entity.CodeMeeting;
import com.spdb.fdev.codeReview.spdb.entity.CodeOrder;
import com.spdb.fdev.codeReview.spdb.service.*;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.Code;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
@Service
public class OrderServiceImpl implements IOrderService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IOrderDao orderDao;

    @Autowired
    ILogService logService;

    @Autowired
    IMeetingService meetingService;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    UserUtil userUtil;

    @Autowired
    DemandUtil demandUtil;

    @Autowired
    AppUtil appUtil;

    @Autowired
    IMailService mailService;

    @Autowired
    IFileService fileService;
    @Autowired
    IFileDao fileDao;
    @Autowired
    IMeetingDao meetingDao;

    @Value("${code.auditor.role.id}")
    private String codeAuditorRoleId;

    @Override
    public CodeOrder add(CodeOrder codeOrder, @RequestParam MultipartFile[] requirementSpecification,
                         @RequestParam MultipartFile[] codeReviewTable, @RequestParam MultipartFile[] prototypeFigure,
                         @RequestParam MultipartFile[] demandInstructionBook) throws Exception {
        Set<String> taskIds = codeOrder.getTaskIds();
        //根据任务id集合查询涉及系统id集合
        List<Map> tasks = taskUtil.queryTasksAllByIds(taskIds);
        Set<String> systems = getSystemsByTaskIds(tasks);
        String realProductDate = taskUtil.getRealProductDate(tasks);
        codeOrder.setRealProductDate(realProductDate);
        codeOrder.setSystems(systems);
        //工单状态设置为初始状态
        codeOrder.setOrderStatus(OrderEnum.OrderStatusEnum.TO_AUDIT.getValue());
        //设置牵头小组，根据牵头人id获取牵头人对应的第三层级小组
        String leaderGroup = userUtil.getUserThreeLevelGroup(codeOrder.getLeader());
        codeOrder.setLeaderGroup(leaderGroup);
        //设置创建人、创建时间、修改人、修改时间、申请时间
        codeOrder.setCreateUser(CommonUtils.getSessionUser().getId());
        codeOrder.setCreateTime(TimeUtil.formatTodayHs());
        codeOrder.setUpdateUser(CommonUtils.getSessionUser().getId());
        codeOrder.setUpdateTime(TimeUtil.formatTodayHs());
        codeOrder.setApplyTime(TimeUtil.formatTodayHs());
        //生成工单编号
        Calendar calendar = Calendar.getInstance();
        String orderNo = "FDEV-CODE-" + calendar.get(Calendar.YEAR) + "-";
        if(orderDao.count() == 0){
            orderNo = orderNo + String.format("%05d",1);
            codeOrder.setOrderNo(orderNo);
        }else {
            CodeOrder maxOrder = orderDao.queryMaxNoThisYear(calendar.get(Calendar.YEAR) + "");
            String[] split = maxOrder.getOrderNo().split("-");
            int i = Integer.parseInt(split[split.length - 1]) + 1;
            orderNo = orderNo + String.format("%05d",i);
            codeOrder.setOrderNo(orderNo);
        }
        //生成工单id，传文件需要
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        codeOrder.setId(id);

        //上传文件
        User user = CommonUtils.getSessionUser();
        fileService.uploadFile(id,OrderEnum.OrderStatusEnum.TO_AUDIT.getValue(), Constants.REQUIREMENT_SPECIFICATION_KEY,requirementSpecification, user);
        fileService.uploadFile(id,OrderEnum.OrderStatusEnum.TO_AUDIT.getValue(),Constants.CODE_REVIEW_TABLE_KEY,codeReviewTable, user);
        if(!CommonUtils.isNullOrEmpty(prototypeFigure)){
            fileService.uploadFile(id,OrderEnum.OrderStatusEnum.TO_AUDIT.getValue(), Constants.PROTOTYPE_FIGURE_KEY,prototypeFigure, user);
        }
        if(!CommonUtils.isNullOrEmpty(demandInstructionBook)){
            fileService.uploadFile(id,OrderEnum.OrderStatusEnum.TO_AUDIT.getValue(), Constants.DEMAND_INSTRUCTION_BOOK_KEY,demandInstructionBook, user);
        }
        //入库
        CodeOrder order = orderDao.add(codeOrder);
        //更新任务和需求的代码审核标识
        List<String> demandIds = new ArrayList<>();
        demandIds.add(codeOrder.getDemandId());
        demandUtil.updateDemandCodeOrderNo(demandIds, codeOrder.getOrderNo());
        taskUtil.updateTaskCodeOrderNo(codeOrder.getTaskIds(),orderNo);
        //邮件通知代码审核角色和用户指定的人
        mailService.orderCreateMail(codeOrder, user);
        //记录日志
        addOrderLog(new CodeOrder(), codeOrder);
        return order;
    }

    public Set<String> getSystemsByTaskIds(List<Map> tasks){

        Set<String> appIds = new HashSet<>();
        Set<String> systems = new HashSet<>();
        for(Map task:tasks){
            if(!CommonUtils.isNullOrEmpty(task.get(Dict.PROJECT_ID))){
                appIds.add((String)task.get(Dict.PROJECT_ID));
            }
        }

        //根据应用id查询应用信息
        for(String appId:appIds){
            Map app = null;
            try {
                app = appUtil.findAppByid(appId);
                if(!CommonUtils.isNullOrEmpty(app)){
                    if(!CommonUtils.isNullOrEmpty(app.get(Dict.SYSTEM))){
                        systems.add(((Map)app.get(Dict.SYSTEM)).get(Dict.ID).toString());
                    }
                }
            } catch (Exception e) {
                logger.info("应用不存在！");
            }
        }
        return systems;
    }

    @Override
    public void update(CodeOrder codeOrder) throws Exception {
        String orderId = codeOrder.getId();
        CodeOrder oldCodeOrder = orderDao.queryOrderById(orderId);
        User sessionUser = CommonUtils.getSessionUser();
        //任务有变更则修改涉及系统和实际投产日期
        Set<String> systems = oldCodeOrder.getSystems();
        String realProductDate = oldCodeOrder.getRealProductDate();
        if(!oldCodeOrder.getTaskIds().equals(codeOrder.getTaskIds())){
            List<Map> tasks = taskUtil.queryTasksAllByIds(codeOrder.getTaskIds());
            systems = getSystemsByTaskIds(tasks);
            realProductDate = taskUtil.getRealProductDate(tasks);
        }
        //代码审核角色可编辑所有字段
        if(sessionUser.getRole_id().contains(codeAuditorRoleId)){
            if(oldCodeOrder.getOrderStatus() >= OrderEnum.OrderStatusEnum.FIRST_PASS.getValue()){
                //终态仅更新生产问题描述
                oldCodeOrder.setProductProblem(codeOrder.getProductProblem());
                orderDao.update(oldCodeOrder);
            }else {
                codeOrder.setSystems(systems);
                codeOrder.setOrderNo(oldCodeOrder.getOrderNo());
                codeOrder.setDemandId(oldCodeOrder.getDemandId());
                //实际投产日期
                codeOrder.setRealProductDate(realProductDate);
                //设置牵头小组
                codeOrder.setLeaderGroup(userUtil.getUserThreeLevelGroup(codeOrder.getLeader()));
                //若状态为终态，则发邮件通知牵头人
                if(codeOrder.getOrderStatus() >= OrderEnum.OrderStatusEnum.FIRST_PASS.getValue()){
                    mailService.orderFinishMail(codeOrder);
                    codeOrder.setAuditFinishTime(TimeUtil.formatTodayHs());
                }
                //若状态从其他状态改为“需会议复审”、“需线下复审”,则发送复审提醒邮件
                if(codeOrder.getOrderStatus() != oldCodeOrder.getOrderStatus()
                        && (codeOrder.getOrderStatus() == OrderEnum.OrderStatusEnum.OFFLINE_RECHECK.getValue() || codeOrder.getOrderStatus() == OrderEnum.OrderStatusEnum.MEETING_RECHECK.getValue())){
                    codeOrder.setCreateUser(oldCodeOrder.getCreateUser());//塞进发邮件所需的字段
                    mailService.recheckRemindMail(codeOrder);
                }
                orderDao.update(codeOrder);
                //记录日志，仅记录工单状态和工单结果变更
                addOrderLog(oldCodeOrder, codeOrder);
            }
        }else {
            //非代码审核角色不可编辑审核状态、审核结论、生产问题描述
            if(!CommonUtils.isNullOrEmpty(codeOrder.getTaskIds())){
                oldCodeOrder.setTaskIds(codeOrder.getTaskIds());
                //涉及系统
                oldCodeOrder.setSystems(systems);
                //实际投产日期
                oldCodeOrder.setRealProductDate(realProductDate);
            }
            if(!CommonUtils.isNullOrEmpty(codeOrder.getLeader())){
                oldCodeOrder.setLeader(codeOrder.getLeader());
                oldCodeOrder.setLeaderGroup(userUtil.getUserThreeLevelGroup(codeOrder.getLeader()));
            }
            if(!CommonUtils.isNullOrEmpty(codeOrder.getPlanProductDate())){
                oldCodeOrder.setPlanProductDate(codeOrder.getPlanProductDate());
            }
            if(!CommonUtils.isNullOrEmpty(codeOrder.getAuditContent())){
                oldCodeOrder.setAuditContent(codeOrder.getAuditContent());
            }
            if(!CommonUtils.isNullOrEmpty(codeOrder.getExpectAuditDate())){
                oldCodeOrder.setExpectAuditDate(codeOrder.getExpectAuditDate());
            }
            orderDao.update(oldCodeOrder);
        }
        //更新任务的提交代码审核标识，直接更新此工单下所有的任务
        taskUtil.updateTaskCodeOrderNo(codeOrder.getTaskIds(), oldCodeOrder.getOrderNo());
    }



    @Override
    public void deleteOrderById(String orderId) throws Exception {
        CodeOrder codeOrder = orderDao.queryOrderById(orderId);
        //终态下不可删除
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            throw new FdevException(ErrorConstants.ORDER_END_STATUS_CAN_NOT_DO);
        }
        //删除工单需控制工单下有会议记录则不可删除。
        List<CodeMeeting> codeMeetings = meetingService.queryMeetingByOrderId(orderId);
        if(!CommonUtils.isNullOrEmpty(codeMeetings)){
            throw new FdevException(ErrorConstants.HAVE_MEETING_CAN_NOT_DEL);
        }
        //删除工单时，需同步更新关联任务和关联需求的“工单编号”字段。
        demandUtil.updateDemandCodeOrderNo(new ArrayList<>(), codeOrder.getOrderNo());
        taskUtil.updateTaskCodeOrderNo(new HashSet<>(), codeOrder.getOrderNo());
        //删除对应文件
        List<CodeFile> codeFiles = fileDao.queryFilesByOrderId(orderId);
        for(CodeFile codeFile : codeFiles){
            fileService.delete(codeFile.getId());
        }
        //删除工单需要将日志同步删除。
        logService.deleteLog(orderId);
        orderDao.deleteOrderById(orderId);
    }

    @Override
    public Map queryOrders(Map param) throws Exception {
        Map resultMap = new HashMap();
        //判断是否代码审核角色
        boolean isAuditRole = false;
        User sessionUser = CommonUtils.getSessionUser();
        if(sessionUser.getRole_id().contains(codeAuditorRoleId)){
            isAuditRole = true;
        }
        Map map = orderDao.queryOrders(param, isAuditRole, sessionUser.getId());
        List<CodeOrder> codeOrderList = (List<CodeOrder>)map.get("orderList");

        Set<String> userIds = new HashSet();
        Set<String> groupIds = new HashSet();
        Set<String> demandIds = new HashSet<>();
        Set<String> systemIds = new HashSet<>();
        //获取要批量查询的组id，userId
        for(CodeOrder codeOrder : codeOrderList){
            userIds.add(codeOrder.getLeader());
            groupIds.add(codeOrder.getLeaderGroup());
            demandIds.add(codeOrder.getDemandId());
            systemIds.addAll(codeOrder.getSystems());
        }
        //查询用户中文名、组名、需求编号_需求名称
        Map userInfoMap = userUtil.getUserByIds(userIds);
        Map groupNameMap = userUtil.getGroupsNameByIds(groupIds);
        Map<String,Map> demandMap = demandUtil.getDemandsInfoByIds(demandIds);
        Map<String, String> systemsNameMap = appUtil.getSystemsNameMap(systemIds);
        for(CodeOrder codeOrder : codeOrderList){
            //给每条数据添加编辑按钮和删除按钮标识
            codeOrder.setDeleteButton(getDeleteButton(codeOrder));
            codeOrder.setUpdateButton(getUpdateButton(codeOrder));
            //封装牵头小组中文名
            codeOrder.setLeaderGroupCn((String)groupNameMap.get(codeOrder.getLeaderGroup()));
            //封装牵头人姓名
            codeOrder.setLeaderName(userUtil.getUserNameById(codeOrder.getLeader(),userInfoMap));
            //封装涉及系统名称
            codeOrder.setSystemNames(appUtil.getSystemsName(codeOrder.getSystems(), systemsNameMap));
            //封装需求编号_需求名称
            String demandNo = (String) demandMap.get(codeOrder.getDemandId()).get(Dict.OA_CONTACT_NO);
            String demandName = (String) demandMap.get(codeOrder.getDemandId()).get(Dict.OA_CONTANCT_NAME);
            codeOrder.setDemandName(demandNo+"_"+demandName);
        }

        resultMap.put(Dict.COUNT,map.get(Dict.COUNT));
        resultMap.put(Dict.ORDERLIST,map.get(Dict.ORDERLIST));
        resultMap.put(Dict.ADDBUTTON,"0");
        return resultMap;
    }

    @Override
    public CodeOrder queryOrderById(String orderId) throws Exception {
        CodeOrder codeOrder = orderDao.queryOrderById(orderId);
        //添加编辑按钮和删除按钮标识
        codeOrder.setDeleteButton(getDeleteButton(codeOrder));
        codeOrder.setUpdateButton(getUpdateButton(codeOrder));
        //包装申请复审和复审提醒按钮。
        codeOrder.setApplyRecheckButton(getApplyRecheckButton(codeOrder));//状态为“需线下复审”、“需会议复审”时，可点击
        codeOrder.setRecheckRemindButton(getRecheckRemindButton(codeOrder));//状态为“需线下复审”、“需会议复审”并且当前角色为“代码审核员”可点击
        //封装牵头小组中文名
        Set<String> leaderGroupIds = new HashSet();
        leaderGroupIds.add(codeOrder.getLeaderGroup());
        Map groupNames = userUtil.getGroupsNameByIds(leaderGroupIds);
        codeOrder.setLeaderGroupCn((String) groupNames.get(codeOrder.getLeaderGroup()));
        //包装申请信息（将初次申请信息和复审信息合并）按申请时间排序
        List<ApplyInfo> applyInfos = codeOrder.getApplyInfo();
        ApplyInfo applyInfo = new ApplyInfo(codeOrder.getCreateUser(), codeOrder.getApplyTime(), codeOrder.getAuditContent());
        if(CommonUtils.isNullOrEmpty(applyInfos)){
            applyInfos = new ArrayList<>();
        }
        applyInfos.add(applyInfo);//添加初审信息
        applyInfos.sort(new Comparator<ApplyInfo>() {
            @Override
            public int compare(ApplyInfo o1, ApplyInfo o2) {
                if(!TimeUtil.compareTime2(o1.getApplyTime(), o2.getApplyTime())){
                    return -1;
                }else if(o1.getApplyTime().equals(o2.getApplyTime())){
                    return 0;
                }
                return 1;
            }
        });

        //封装牵头人、创建人、邮件通知对象姓名
        Set<String> userIds = new HashSet();
        userIds.add(codeOrder.getLeader());
        userIds.add(codeOrder.getCreateUser());
        if(!CommonUtils.isNullOrEmpty(applyInfos)){
            for (ApplyInfo info : applyInfos) {
                userIds.add(info.getApplyUser());
            }
        }
        if(!CommonUtils.isNullOrEmpty(codeOrder.getEmailTo())){
            userIds.addAll(codeOrder.getEmailTo());
        }
        Map userInfos = userUtil.getUserByIds(userIds);
        codeOrder.setLeaderName(userUtil.getUserNameById(codeOrder.getLeader(),userInfos));
        codeOrder.setCreateUserNameCn(userUtil.getUserNameById(codeOrder.getCreateUser(),userInfos));
        String emailToNameCn = "";
        if(!CommonUtils.isNullOrEmpty(codeOrder.getEmailTo())){
            for(String mail:codeOrder.getEmailTo()){
                emailToNameCn += userUtil.getUserNameById(mail,userInfos) + " ";
            }
        }
        if(!CommonUtils.isNullOrEmpty(applyInfos)){
            for (ApplyInfo info : applyInfos) {
                info.setApplyUser(userUtil.getUserNameById(info.getApplyUser(), userInfos));
            }
        }
        codeOrder.setApplyInfo(applyInfos);
        codeOrder.setEmailToNameCn(emailToNameCn);
        //封装涉及系统名称
        codeOrder.setSystemNames(appUtil.getSystemsName(codeOrder.getSystems(), appUtil.getSystemsNameMap(codeOrder.getSystems())));
        //封装需求编号_需求名称
        Set<String> demandIds = new HashSet<>();
        demandIds.add(codeOrder.getDemandId());
        Map<String,Map> demandMap = demandUtil.getDemandsInfoByIds(demandIds);
        String demandNo = (String) demandMap.get(codeOrder.getDemandId()).get(Dict.OA_CONTACT_NO);
        String demandName = (String) demandMap.get(codeOrder.getDemandId()).get(Dict.OA_CONTANCT_NAME);
        codeOrder.setDemandName(demandNo+"_"+demandName);
        //封装任务信息，任务名称、任务状态
        List<TaskEntity> tasksInfo = taskUtil.getTasksInfoByIds(codeOrder.getTaskIds());
        codeOrder.setTasksInfo(tasksInfo);
        return codeOrder;
    }

    @Override
    public void updateByTask(CodeOrder codeOrder) throws JsonProcessingException {
        String orderNo = codeOrder.getOrderNo();
        CodeOrder codeOrder1 = orderDao.queryByOrderNo(orderNo);
        if(CommonUtils.isNullOrEmpty(codeOrder1)){
            throw new FdevException("工单编号不存在！");
        }
        orderDao.updateByTask(codeOrder);
    }

    @Override
    public void exportOrderExcel(Map param, HttpServletResponse resp) throws Exception {
        Map map = this.queryOrders(param);
        List<CodeOrder> orders = (List<CodeOrder>)map.get(Dict.ORDERLIST);
        // 初始化workbook
        InputStream inputStream = null;
        XSSFWorkbook workbook = null;
        XSSFSheet sheet = null;
        //引入模板
        try {
            ClassPathResource classPathResource=new ClassPathResource("OrderList.xlsx");
            inputStream=classPathResource.getInputStream();
            workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);
            logger.info("-------load model OrderExprotList success-----");

        } catch (Exception e1) {
            logger.error("---export---" + e1);
            throw new FdevException("工单列表导出失败，请联系fdev管理员");
        }
        int i=1;//行数
        for (CodeOrder order : orders) {
            int j=0;//列数
            sheet.createRow(i);
            sheet.getRow(i).createCell(j++).setCellValue(order.getOrderNo());//工单编号
            sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.getOrderStatusNameByValue(order.getOrderStatus()));//需求状态
            sheet.getRow(i).createCell(j++).setCellValue(order.getDemandName());//需求名称
            sheet.getRow(i).createCell(j++).setCellValue(order.getLeaderGroupCn());//牵头小组
            sheet.getRow(i).createCell(j++).setCellValue(order.getLeaderName());//牵头人
            sheet.getRow(i).createCell(j++).setCellValue(order.getSystemNames());//涉及系统名称
            sheet.getRow(i).createCell(j++).setCellValue(order.getApplyTime());//申请时间
            sheet.getRow(i).createCell(j++).setCellValue(order.getAuditFinishTime());//审核完成日期
            sheet.getRow(i).createCell(j++).setCellValue(order.getPlanProductDate());//计划投产日期
            sheet.getRow(i).createCell(j++).setCellValue(order.getExpectAuditDate());//期望审核日期
            sheet.getRow(i).createCell(j++).setCellValue(order.getRealProductDate());//实际投产日期
            //根据工单id查询会议
            List<CodeMeeting> codeMeetings = meetingDao.queryMeetingByOrderId(order.getId());
            codeMeetings.sort(new Comparator<CodeMeeting>() {
                @Override
                public int compare(CodeMeeting o1, CodeMeeting o2) {
                    if(o1.getMeetingTime().equals(o2.getMeetingTime())){
                        return 0;
                    }else if(TimeUtil.compareTime3(o1.getMeetingTime(), o2.getMeetingTime())){
                        return 1;
                    }
                    return -1;
                }
            });
            String meetingTimes = "";
            for(CodeMeeting codeMeeting : codeMeetings){
                meetingTimes += codeMeeting.getMeetingTime() + " , ";
            }
            sheet.getRow(i).createCell(j++).setCellValue("".equals(meetingTimes) ? "" : meetingTimes.substring(0,meetingTimes.length()-2));//会议时间
            i++;
        }
        try {
            resp.reset(); resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setHeader("Content-Disposition", "attachment;filename=" +
                    "OrderList"+ ".xlsx");
            workbook.write(resp.getOutputStream()); } catch (IOException e) {
            logger.error("导出工单列表时，数据读写错误" + e.getMessage());
        }
    }

    @Override
    public void updateAuditUser(String orderId) throws Exception {
        List<CodeMeeting> codeMeetings = meetingDao.queryMeetingByOrderId(orderId);
        HashSet<String> auditUserSet = new HashSet<>();
        for(CodeMeeting codeMeeting : codeMeetings){
            auditUserSet.addAll(codeMeeting.getAuditeUsers());
        }
        CodeOrder order = orderDao.queryOrderById(orderId);
        order.setAuditeUsers(auditUserSet);
        orderDao.update(order);
    }

    @Override
    public List<Map> queryListSimple() throws Exception {
        List<CodeOrder> data = orderDao.queryAll();
        List<Map> result = new ArrayList<>();
        Set<String> demandIds= new HashSet<>();
        for(CodeOrder order : data){
            demandIds.add(order.getDemandId());
        }
        //批量查询需求信息
        Map<String,Map> demandMap = demandUtil.getDemandsInfoByIds(demandIds);
        for(CodeOrder order : data){
            Map<String, String> map = new HashMap<>();
            map.put(Dict.ID, order.getId());
            map.put(Dict.ORDERNO, order.getOrderNo());
            String demandNo = (String) demandMap.get(order.getDemandId()).get(Dict.OA_CONTACT_NO);
            String demandName = (String) demandMap.get(order.getDemandId()).get(Dict.OA_CONTANCT_NAME);
            map.put(Dict.DEMANDNAME, demandNo+"_"+demandName);
            result.add(map);
        }
        return result;
    }

    /**
     * 工单复审提醒，仅工单状态为“需线下复审”、“需会议复审” 且 当前登录用户为 代码审核角色可点击
     * 发送提醒邮件给工单创建人、牵头人、当前登录用户
     * @param orderId
     */
    @Override
    public void recheckRemind(String orderId) throws Exception {
        CodeOrder codeOrder = orderDao.queryOrderById(orderId);
        if(codeOrder.getOrderStatus() != OrderEnum.OrderStatusEnum.OFFLINE_RECHECK.getValue()
                && codeOrder.getOrderStatus() != OrderEnum.OrderStatusEnum.MEETING_RECHECK.getValue()){
            throw new FdevException("仅复审状态下可提醒！");
        }
        //发邮件
        mailService.recheckRemindMail(codeOrder);
    }

    /**
     * 申请复审邮件，仅工单状态为“需线下复审”、“需会议复审”时可点击
     * 发送邮件给代码审核角色和当前登录用户
     * @param orderId
     */
    @Override
    public void applyRecheck(String orderId, String recheckContent) throws Exception {
        CodeOrder codeOrder = orderDao.queryOrderById(orderId);
        if (codeOrder.getOrderStatus() != OrderEnum.OrderStatusEnum.OFFLINE_RECHECK.getValue()
                && codeOrder.getOrderStatus() != OrderEnum.OrderStatusEnum.MEETING_RECHECK.getValue()) {
            throw new FdevException("仅【需线下复审】和【需会议复审状态】下可申请！");
        }
        //发邮件
        mailService.applyRecheckMail(codeOrder);
        //复审信息记录入库,申请人、申请时间、申请内容
        ApplyInfo applyInfo = new ApplyInfo();
        applyInfo.setApplyTime(TimeUtil.formatTodayHs());
        User sessionUser = CommonUtils.getSessionUser();
        applyInfo.setApplyUser(sessionUser.getId());
        applyInfo.setApplyContent(recheckContent);
        List<ApplyInfo> applyInfos = codeOrder.getApplyInfo();
        if(CommonUtils.isNullOrEmpty(applyInfos)){
            applyInfos = new ArrayList<>();
        }
        applyInfos.add(applyInfo);
        codeOrder.setApplyInfo(applyInfos);
        orderDao.update(codeOrder);
    }


    /**
     * 判断当前工单是否可编辑
     * 0可编辑部分字段，1可编辑全部字段，2仅可编辑生产问题描述，3不可编辑
     * 0工单非终态，当前用户为非代码审核角色
     * 1工单非终态，当前用户为代码审核角色
     * 2工单终态，当前用户为代码审核角色
     * 3工单终态，当前用户为非代码审核角色
     * @return
     */
    private String getUpdateButton(CodeOrder codeOrder) throws Exception {
        String result = "0";
        User sessionUser = CommonUtils.getSessionUser();
        Integer orderStatus = codeOrder.getOrderStatus();
        if(sessionUser.getRole_id().contains(codeAuditorRoleId)){
            //代码审核角色
            if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= orderStatus){
                //终态
                result = "2";
            }else {
                //非终态
                result = "1";
            }
        }else {
            //非代码审核角色
            if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= orderStatus){
                //终态
                result = "3";
            }
        }
        return result;
    }

    /**
     * 返回当前用户对应工单的删除按钮权限
     * 0可删除，1/2不可删除
     * 不控人员权限，工单终态不可删除、工单下有会议记录不可删除
     * @param codeOrder
     * @return
     */
    private String getDeleteButton(CodeOrder codeOrder){
        Integer orderStatus = codeOrder.getOrderStatus();
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= orderStatus){
            return "1";
        }
        List<CodeMeeting> codeMeetings = meetingService.queryMeetingByOrderId(codeOrder.getId());
        if(!CommonUtils.isNullOrEmpty(codeMeetings)){
            return "2";
        }
        return "0";
    }

    /**
     * 返回申请复审按钮权限
     * @param codeOrder
     * @return
     */
    private String getApplyRecheckButton(CodeOrder codeOrder){
        if(codeOrder.getOrderStatus() == OrderEnum.OrderStatusEnum.OFFLINE_RECHECK.getValue()
                || codeOrder.getOrderStatus() == OrderEnum.OrderStatusEnum.MEETING_RECHECK.getValue()){
            return "0";
        }
        return "1";
    }

    /**
     * 返回复审提醒按钮权限
     * @param codeOrder
     * @return
     */
    private String getRecheckRemindButton(CodeOrder codeOrder) throws Exception {
        User sessionUser = CommonUtils.getSessionUser();
        if((codeOrder.getOrderStatus() == OrderEnum.OrderStatusEnum.OFFLINE_RECHECK.getValue() || codeOrder.getOrderStatus() == OrderEnum.OrderStatusEnum.MEETING_RECHECK.getValue())
                && sessionUser.getRole_id().contains(codeAuditorRoleId)){
            return "0";
        }
        return "1";
    }

    /**
     * 记录工单操作日志，如改动了工单状态或者工单审核结论，则记录一条数据
     * @param oldCodeOrder
     * @param newCodeOrder
     * @return
     */
    public CodeLog addOrderLog(CodeOrder oldCodeOrder, CodeOrder newCodeOrder) throws Exception {
        Integer oldStatus = oldCodeOrder.getOrderStatus();
        String oldResult = oldCodeOrder.getAuditResult();
        Integer newStatus = newCodeOrder.getOrderStatus();
        String newResult = newCodeOrder.getAuditResult();
        CodeLog codeLog = new CodeLog();
        if(oldStatus != newStatus
                ||(CommonUtils.isNullOrEmpty(oldResult) && !CommonUtils.isNullOrEmpty(newResult))
                ||(!CommonUtils.isNullOrEmpty(oldResult) && CommonUtils.isNullOrEmpty(newResult))
                ||(!CommonUtils.isNullOrEmpty(oldResult) && !CommonUtils.isNullOrEmpty(newResult) && !oldResult.equals(newResult))){
            //新增一条日志
            LogContent statusContent = new LogContent();
            statusContent.setFieldName(Dict.ORDER_STATUS);
            if(!CommonUtils.isNullOrEmpty(oldStatus)){
                statusContent.setOldValue(oldStatus);
            }
            if(!CommonUtils.isNullOrEmpty(newStatus)){
                statusContent.setNewValue(newStatus);
            }
            LogContent resultContent = new LogContent();
            resultContent.setFieldName(Dict.AUDIT_RESULT);
            if(!CommonUtils.isNullOrEmpty(oldResult)){
                resultContent.setOldValue(oldResult);
            }
            if(!CommonUtils.isNullOrEmpty(newResult)){
                resultContent.setNewValue(newResult);
            }
            List<LogContent> logContents = new ArrayList<>();
            logContents.add(statusContent);
            logContents.add(resultContent);
            codeLog.setContent(logContents);
            codeLog.setOrderId(newCodeOrder.getId());
        }
        return logService.add(codeLog);
    }
}
