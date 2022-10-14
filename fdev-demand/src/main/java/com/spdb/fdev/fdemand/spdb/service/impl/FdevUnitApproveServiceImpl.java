package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.DemandBaseInfoUtil;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.*;
import com.spdb.fdev.fdemand.spdb.entity.*;
import com.spdb.fdev.fdemand.spdb.service.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@RefreshScope
@Service
public class FdevUnitApproveServiceImpl implements IFdevUnitApproveService {

    @Autowired
    private IFdevUnitApproveDao fdevUnitApproveDao;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IImplementUnitDao implementUnitDao;

    @Autowired
    private IDemandBaseInfoDao demandBaseInfoDao;

    @Autowired
    private IDemandAssessDao demandAssessDao;

    @Autowired
    private SendEmailDemandService sendEmailDemandService;

    @Autowired
    private IFdevUserService fdevUserService;

    @Autowired
    private IImplementUnitService implementUnitService;

    @Value("${start.overdue.date}")
    private String startOverdueDate;//判断计划开始日期超期时间
    @Value("${test.overdue.date}")
    private String testOverdueDate;//判断计划业测日期超期时间
    @Value("${section.info}")
    private String sectionInfo;//各条线经理



    @Override
    public Map<String, String> queryDemandAssessDate(Map<String, Object> params) throws Exception {
        //返回map
        Map<String, String> map = new HashMap();
        String demandId = (String) params.get(Dict.DEMANDID);
        if( !CommonUtils.isNullOrEmpty(demandId) ){
            //查询需求信息
            DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryDemandByImplUnitsId(demandId);
            //查询需求评估表
            DemandAssess demandAssess = demandAssessDao.queryByNo(demandBaseInfo.getOa_contact_no());
            if( !CommonUtils.isNullOrEmpty(demandAssess) ){
                //评估完成时间
                map.put(Dict.ASSESSDATE,demandAssess.getEnd_assess_date());
            }else {
                //评估完成时间
                map.put(Dict.ASSESSDATE,"");
            }

        }
        map.put(Dict.STARTOVERDUEDATE,startOverdueDate);//判断计划开始日期超期时间
        map.put(Dict.TESTOVERDUEDATE,testOverdueDate);//判断计划业测日期超期时间
        return map;
    }

    @Override
    public void applyApprove(Map<String, Object> params) throws Exception {
        User user = CommonUtils.getSessionUser();
        String fdevUnitNo = (String) params.get(Dict.FDEVUNITNO);
        //查询研发单元信息
        FdevImplementUnit fdevImplementUnit = implementUnitDao.queryByFdevNo(fdevUnitNo);
        if(!Constants.NOSUBMIT.equals(fdevImplementUnit.getApproveState()) &&
                !Constants.REJECT.equals(fdevImplementUnit.getApproveState())){
            throw new FdevException("该研发单元已提交申请,不可重复申请!");
        }

        Set<String> implement_leader = fdevImplementUnit.getImplement_leader();
        List<String> leaderList = new ArrayList<>(implement_leader);
        //查询牵头人信息
        Map<String, Object> leaderMap = roleService.queryUserbyid(leaderList.get(0));
        String sectionId = "" ;
        if( !CommonUtils.isNullOrEmpty(leaderMap) ){
            String groupId = (String) leaderMap.get(Dict.GROUP_ID_YH);
            if(!CommonUtils.isNullOrEmpty(groupId)){
                Map<String, Object> groupMap = fdevUserService.getThreeLevelGroup(groupId);
                if( !CommonUtils.isNullOrEmpty(groupMap) ){

                    if(!CommonUtils.isNullOrEmpty(groupMap.get(Dict.LEVEL))){
                        //判断是否小组 等级
                        if( 1 == (Integer) groupMap.get(Dict.LEVEL) ){
                            //1级小组无人审批默认至  固定审批人
                            sectionId = "defaultSection";
                        }else if (2 == (Integer) groupMap.get(Dict.LEVEL)){
                            // 2级小组当前小组ID就是条线ID
                            sectionId = groupId;
                        }else{
                            //不是 1 2级  3级获取父id 为条线id
                            sectionId =  (String) groupMap.get(Dict.PARENT_ID) ;
                        }

                    }
                }
            }
        }
        //条线id为空或者 不存在条线经理赋值默认值
        if(CommonUtils.isNullOrEmpty(sectionId) || !sectionInfo.contains(sectionId)){
            sectionId = "defaultSection";
        }
        //查询需求信息
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryDemandByImplUnitsId(fdevImplementUnit.getDemand_id());
        //研发单元审批实体
        FdevUnitApprove fdevUnitApprove = newFdevUnitApprove(fdevUnitNo,fdevImplementUnit.getDemand_id(),
                fdevImplementUnit.getApproveType(),fdevImplementUnit.getOverdueType(),fdevImplementUnit.getOverdueReason(),
                sectionId,user.getId(), TimeUtil.formatTodayHs());
        //申请邮件 抄送人员 研发单元牵头人 申请人
        Set<String> ccEmailList = new HashSet<>();
        HashSet<String> leaderSet = fdevImplementUnit.getImplement_leader();
        //查询牵头人信息
        Map<String, Map> userMapAll = fdevUserService.queryByUserCoreData(leaderSet, null);
        //获取邮箱
        for (String leader : leaderSet) {
            if (!CommonUtils.isNullOrEmpty(userMapAll.get(leader))) {
                Map userMap = userMapAll.get(leader);
                ccEmailList.add((String) userMap.get(Dict.EMAIL));
            }
        }
        ccEmailList.add(user.getEmail());
        //发送审批邮件
        sendEmailDemandService.sendEmailApplyApprove(fdevImplementUnit,demandBaseInfo,fdevUnitApprove,new ArrayList<>(ccEmailList));
        //研发单元审批状态置为待审批
        fdevImplementUnit.setApproveState(Constants.WAIT);
        implementUnitDao.update(fdevImplementUnit);
        //保存研发单元审批实体
        fdevUnitApproveDao.saveFdevUnitApprove(fdevUnitApprove);
    }

    //创建研发单元审批实体
    public FdevUnitApprove newFdevUnitApprove(String fdevUnitNo, String demandId,String advanceDev,String overdueType
     ,String overdueReason , String sectionId, String applicantId, String applyTime){
        FdevUnitApprove fdevUnitApprove = new FdevUnitApprove();
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        fdevUnitApprove.set_id(objectId);
        fdevUnitApprove.setId(id);
        fdevUnitApprove.setFdevUnitNo(fdevUnitNo);//研发单元编号
        fdevUnitApprove.setDemandId(demandId);//需求ID
        fdevUnitApprove.setApproveType(advanceDev);//提前开发
        fdevUnitApprove.setOverdueType(overdueType);//超期类型
        fdevUnitApprove.setOverdueReason(overdueReason);//超期原因
        fdevUnitApprove.setApproveState(Constants.WAIT);//新建待审批
        fdevUnitApprove.setSectionId(sectionId);//条线ID
        fdevUnitApprove.setApplicantId(applicantId);//申请人id
        fdevUnitApprove.setApplyTime(applyTime);//申请时间
        return fdevUnitApprove ;
    }

    @Override
    public Map<String,Object> queryMyApproveList(Map<String, Object> params) throws Exception {
        User user = CommonUtils.getSessionUser();
        String email = user.getEmail();
        Map<String,Object> map = new HashMap<>();
        //默认值
        map.put(Dict.COUNT,0);
        map.put(Dict.WAITCOUNT,0);
        map.put(Dict.DONECOUNT,0);
        map.put(Dict.APPROVELIST,new ArrayList<>());
        //条线经理列表包含当前用户
        if( sectionInfo.contains(email) ){
            String sectionId = "";
            //截取条线经理
            String[] split = sectionInfo.split(";");
            for (String sectionStr : split) {
                //判断当前用户属于哪个条线
                String[] sectionStrSplit = sectionStr.split(":");
                if( sectionStrSplit[1].contains(email) ){
                    //赋值条线Id
                    sectionId = sectionStrSplit[0];
                    break;
                }
            }
            if( !CommonUtils.isNullOrEmpty(sectionId) ){
                String code = (String) params.get(Dict.CODE);
                String keyword = (String) params.get(Dict.KEYWORD);//搜索条件 研发单元内容/编号
                Integer pageNum = (Integer) params.get(Dict.PAGENUM);
                Integer pageSize = (Integer) params.get(Dict.PAGESIZE);
                String type = (String) params.get(Dict.TYPE);// 0 = 提前开发 1 = 超期 2 = 提前开发&超期
                List<String> fdevUnitNos = new ArrayList<>();
                //查询研发单元
                List<FdevImplementUnit> fdevImplementUnit = implementUnitDao.queryFdevUnitList(keyword,null,null,null);
                if(!CommonUtils.isNullOrEmpty(fdevImplementUnit)){
                    for (FdevImplementUnit implementUnit : fdevImplementUnit) {
                        fdevUnitNos.add(implementUnit.getFdev_implement_unit_no());
                    }
                }
                    
                //查询研发单元审批列表
                map = fdevUnitApproveDao.queryMyApproveList(type,code,sectionId,fdevUnitNos,keyword,pageNum,pageSize);
                List<FdevUnitApprove> data = (List<FdevUnitApprove>) map.get(Dict.APPROVELIST);
                if( !CommonUtils.isNullOrEmpty(data) ){
                    completeFdevUnitApprove(data);
                }
            }
        }
        return map;
    }
    //封装研发单元审批表信息
    public void completeFdevUnitApprove(List<FdevUnitApprove> fdevUnitApproveList) throws Exception {
        //获取条线名称
        List<Map<String, Object>> allSection = fdevUserService.queryGroupTwoAll();

        for (FdevUnitApprove fdevUnitApprove : fdevUnitApproveList) {
            String sectionName = "";//条线名称
            for (Map<String, Object> sectionMap : allSection) {
                if(fdevUnitApprove.getSectionId().equals(sectionMap.get(Dict.ID))){
                    sectionName = (String) sectionMap.get(Dict.NAME);
                    break;
                }
            }
            fdevUnitApprove.setSectionName(sectionName);//条线名称

            //查询研发单元
            FdevImplementUnit fdevImplementUnit = implementUnitDao.queryByFdevNo(fdevUnitApprove.getFdevUnitNo());
            //查询小组名称
            Map<String, Object> groupMap = roleService.queryGroup(fdevImplementUnit.getGroup());
            fdevUnitApprove.setGroupId(fdevImplementUnit.getGroup());//小组ID
            fdevUnitApprove.setGroupName((String)groupMap.get(Dict.GROUP_NAME));//小组名称
            fdevUnitApprove.setFdevUnitName(fdevImplementUnit.getImplement_unit_content());//研发单元内容
            fdevUnitApprove.setFdevUnitState(fdevImplementUnit.getImplement_unit_status_normal());//研发单元状态
            fdevUnitApprove.setFdevUnitSpecialState(fdevImplementUnit.getImplement_unit_status_special());//研发单元特殊状态
            fdevUnitApprove.setFdevUnitLeaderId(fdevImplementUnit.getIpmp_unit_leader());//研发单元牵头人
            fdevUnitApprove.setFdevUnitLeaderInfo(fdevImplementUnit.getImplement_leader_all());//研发单元牵头人信息
            fdevUnitApprove.setPlanStartDate(fdevImplementUnit.getPlan_start_date());//研发单元计划开始日期
            fdevUnitApprove.setPlanInnerTestDate(fdevImplementUnit.getPlan_inner_test_date());//研发单元计划内测日期
            fdevUnitApprove.setPlanTestDate(fdevImplementUnit.getPlan_test_date());//研发单元计划业测日期
            fdevUnitApprove.setPlanTestFinishDate(fdevImplementUnit.getPlan_test_finish_date());//研发单元计划业测完成日期
            fdevUnitApprove.setPlanProductDate(fdevImplementUnit.getPlan_product_date());//研发单元计划投产日期
            //查询需求信息
            DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryDemandByImplUnitsId(fdevUnitApprove.getDemandId());
            fdevUnitApprove.setDemandNo(demandBaseInfo.getOa_contact_no());//需求编号
            fdevUnitApprove.setDemandName(demandBaseInfo.getOa_contact_name());//需求名称
            Map<String, Object> applicantInfo = roleService.queryUserbyid(fdevUnitApprove.getApplicantId());
            if(!CommonUtils.isNullOrEmpty(applicantInfo)){
                //申请人姓名
                fdevUnitApprove.setApplicantName((String) applicantInfo.get(Dict.USER_NAME_CN));
            }
            if(!CommonUtils.isNullOrEmpty(fdevUnitApprove.getApproverId())){
                Map<String, Object> approverInfo = roleService.queryUserbyid(fdevUnitApprove.getApproverId());
                if(!CommonUtils.isNullOrEmpty(approverInfo)){
                    //审批人
                    fdevUnitApprove.setApproverName((String) approverInfo.get(Dict.USER_NAME_CN));
                }
            }
        }
    }
    @Override
    public void approvePass(Map<String, Object> params) throws Exception {
        User user = CommonUtils.getSessionUser();

        //研发单元审批表置为审批通过
        List<FdevUnitApprove> fdevUnitApproves = fdevUnitApproveDao.passFdevUnitApproveByIds((List<String>) params.get(Dict.IDS), user.getId());
        
        List<String> fdevUnitNos = new ArrayList<>();
        for (FdevUnitApprove fdevUnitApprove : fdevUnitApproves) {
            fdevUnitNos.add(fdevUnitApprove.getFdevUnitNo());
        }
        //查询研发单元
        List<FdevImplementUnit> fdevImplementUnits = implementUnitDao.queryFdevUnitList(null, null, fdevUnitNos,null);
        //研发单元ids
        List<String> ids = new ArrayList<>();
        if(!CommonUtils.isNullOrEmpty(fdevImplementUnits)){
            HashSet<String> leaderSet = new HashSet<>();
            for (FdevImplementUnit fdevImplementUnit : fdevImplementUnits) {
                leaderSet.addAll(fdevImplementUnit.getImplement_leader());

            }
            //查询牵头人信息
            Map<String, Map> userMapAll = fdevUserService.queryByUserCoreData(leaderSet, null);

            for (FdevImplementUnit fdevImplementUnit : fdevImplementUnits) {
                //研发单元置为审批通过
                fdevImplementUnit.setApproveState(Constants.PASS);
                fdevImplementUnit.setApproverId(user.getId());//审批人ID
                fdevImplementUnit.setApproverName(user.getUser_name_cn());//审批人姓名
                implementUnitDao.update(fdevImplementUnit);
                ids.add(fdevImplementUnit.getId());
                List<String> emailList = new ArrayList<>();
                //获取邮箱
                for (String leader : leaderSet) {
                    if (!CommonUtils.isNullOrEmpty(userMapAll.get(leader))) {
                        Map userMap = userMapAll.get(leader);
                        emailList.add((String) userMap.get(Dict.EMAIL));
                    }
                }
                //查询需求信息
                DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryDemandByImplUnitsId(fdevImplementUnit.getDemand_id());
                //发送审批通过邮件
                sendEmailDemandService.sendEmailApprovePass(fdevImplementUnit,demandBaseInfo,emailList);

            }
            //评估研发单元
            Map<String, Object> map = new HashMap();
            map.put(Dict.ID,ids);//研发单元ids
            map.put(Dict.ROLE,Constants.YES);//不用进行权限校验
            implementUnitService.assess(map);
        }

    }

    @Override
    public void approveReject(Map<String, Object> params) throws Exception {
        String approveRejectReason = (String) params.get(Dict.APPROVEREJECTREASON);//拒绝原因
        User user = CommonUtils.getSessionUser();
        // 研发单元审批表置为审批拒绝
        FdevUnitApprove fdevUnitApprove = fdevUnitApproveDao.rejectFdevUnitApproveById((String) params.get(Dict.ID), approveRejectReason, user.getId());
        //查询需求信息
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryDemandByImplUnitsId(fdevUnitApprove.getDemandId());
        //查询研发单元
        FdevImplementUnit fdevImplementUnit = implementUnitDao.queryByFdevNo(fdevUnitApprove.getFdevUnitNo());
        HashSet<String> leaderSet = fdevImplementUnit.getImplement_leader();
        //查询牵头人信息
        Map<String, Map> userMapAll = fdevUserService.queryByUserCoreData(leaderSet, null);
        List<String> emailList = new ArrayList<>();
        //获取邮箱
        for (String leader : leaderSet) {
            if (!CommonUtils.isNullOrEmpty(userMapAll.get(leader))) {
                Map userMap = userMapAll.get(leader);
                emailList.add((String) userMap.get(Dict.EMAIL));
            }
        }
        //研发单元置为审批拒绝
        fdevImplementUnit.setApproveState(Constants.REJECT);
        fdevImplementUnit.setApproveReason(approveRejectReason);
        fdevImplementUnit.setApproverId(user.getId());//审批人ID
        fdevImplementUnit.setApproverName(user.getUser_name_cn());//审批人姓名
        implementUnitDao.update(fdevImplementUnit);
        //发送审批拒绝邮件
        sendEmailDemandService.sendEmailApproveReject(fdevImplementUnit,demandBaseInfo,approveRejectReason,emailList);
    }

    @Override
    public Map<String, Object> queryApproveList(Map<String, Object> params) throws Exception {
        String fdevUnitKey = (String) params.get(Dict.FDEVUNITKEY);//研发单元编号/内容
        String demandKey = (String) params.get(Dict.DEMANDKEY);//需求编号/内容
        List<String> fdevUnitLeaderIds = (List<String>) params.get(Dict.FDEVUNITLEADERIDS);//牵头人（多选）
        List<String> groupIds = (List<String>) params.get(Dict.GROUPIDS);//牵头小组（多选)
        List<String> approveStates = (List<String>) params.get(Dict.APPROVESTATES);//审批状态（多选） wait=待审批，pass=通过，  reject=拒绝
        List<String> approverIds = (List<String>) params.get(Dict.APPROVERIDS);//审批人（多选）
        Integer pageNum = (Integer) params.get(Dict.PAGENUM);//页码 送空查全部
        Integer pageSize = (Integer) params.get(Dict.PAGESIZE);//每页长度
        String type = (String) params.get(Dict.TYPE);//审批类型
        List<String> fdevUnitNos = new ArrayList<>();//研发单元编号列表
        List<String> demandIds = new ArrayList<>();//需求ID列表
        if(!CommonUtils.isNullOrEmpty(fdevUnitKey) || !CommonUtils.isNullOrEmpty(fdevUnitLeaderIds)
                || !CommonUtils.isNullOrEmpty(groupIds)){
            //查询研发单元
            List<FdevImplementUnit> fdevImplementUnit = implementUnitDao.queryFdevUnitList(fdevUnitKey,fdevUnitLeaderIds,null,groupIds);
            if(!CommonUtils.isNullOrEmpty(fdevImplementUnit)){
                for (FdevImplementUnit implementUnit : fdevImplementUnit) {
                    fdevUnitNos.add(implementUnit.getFdev_implement_unit_no());
                }
            }
        }

        if(!CommonUtils.isNullOrEmpty(demandKey)){
            //查询需求信息
            List<DemandBaseInfo> demandBaseInfoList = demandBaseInfoDao.queryDemandBaseList(demandKey);
            if(!CommonUtils.isNullOrEmpty(demandBaseInfoList)){
                for (DemandBaseInfo demandBaseInfo : demandBaseInfoList) {
                    demandIds.add(demandBaseInfo.getId());
                }
            }
        }
        Map<String, Object> map = fdevUnitApproveDao.queryApproveList(fdevUnitKey,fdevUnitLeaderIds,groupIds,fdevUnitNos, demandIds, demandKey,approveStates,approverIds,pageNum,pageSize,type);
        List<FdevUnitApprove> data = (List<FdevUnitApprove>) map.get(Dict.APPROVELIST);
        if( !CommonUtils.isNullOrEmpty(data) ){
            completeFdevUnitApprove(data);
        }
        return map;
    }

    @Override
    public void exportApproveList(Map<String, Object> params, HttpServletResponse resp) throws Exception {
        params.put(Dict.PAGENUM,null);
        params.put(Dict.PAGESIZE,null);
        Map<String, Object> map = queryApproveList(params);
        List<FdevUnitApprove> data = (List<FdevUnitApprove>) map.get(Dict.APPROVELIST);
        // 初始化workbook
        InputStream inputStream = null;
        XSSFWorkbook workbook = null;
        XSSFSheet sheet = null;
        //引入模板
        try {
            ClassPathResource classPathResource=new ClassPathResource("FdevUnitApproveExport.xlsx");
            inputStream=classPathResource.getInputStream();
            workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);
        } catch (Exception e1) {
            throw new FdevException("研发单元审批列表导出失败，请联系fdev管理员");
        }
        if( !CommonUtils.isNullOrEmpty(data) ){
            completeFdevUnitApprove(data);
            int i=1;//行数
            for (FdevUnitApprove fdev : data) {
                int j=0;//列数
                sheet.createRow(i);
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getFdevUnitNo()) ? "":fdev.getFdevUnitNo());//研发单元编号
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getFdevUnitName()) ? "":fdev.getFdevUnitName());//研发单元内容
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getDemandNo()) ? "":fdev.getDemandNo());//需求编号
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getDemandName()) ? "":fdev.getDemandName());//需求名称
                //研发单元状态
                if(!CommonUtils.isNullOrEmpty(fdev.getFdevUnitSpecialState()) && 1 == fdev.getFdevUnitSpecialState()){
                    sheet.getRow(i).createCell(j++).setCellValue("暂缓");
                }else if(!CommonUtils.isNullOrEmpty(fdev.getFdevUnitSpecialState()) && 2 == fdev.getFdevUnitSpecialState()){
                    sheet.getRow(i).createCell(j++).setCellValue("恢复中");
                }else{
                    sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getFdevUnitState()) ? "": DemandBaseInfoUtil.changeStateCn(fdev.getFdevUnitState()));//研发单元状态
                }
                if(!CommonUtils.isNullOrEmpty(fdev.getFdevUnitLeaderInfo())){
                    String user = "";
                    for(UserInfo userInfo : fdev.getFdevUnitLeaderInfo()){
                        if(CommonUtils.isNullOrEmpty(user)){
                            user = userInfo.getUser_name_cn();
                        }else{
                            user = user + "," + userInfo.getUser_name_cn();
                        }
                    }
                    sheet.getRow(i).createCell(j++).setCellValue(user);//研发单元牵头人
                }else{
                    sheet.getRow(i).createCell(j++).setCellValue("");
                }
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getGroupName()) ? "":fdev.getGroupName());//所属小组
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getPlanStartDate()) ? "":fdev.getPlanStartDate());//计划启动日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getPlanInnerTestDate()) ? "":fdev.getPlanInnerTestDate());//计划提交内测日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getPlanTestDate()) ? "":fdev.getPlanTestDate());//计划提交用户测试日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getPlanTestFinishDate()) ? "":fdev.getPlanTestFinishDate());//计划用户测试完成日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getPlanProductDate()) ? "":fdev.getPlanProductDate());//计划投产日期
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getApproveType()) ? "":CommonUtils.approveType(fdev.getApproveType()));//审批类型
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getOverdueType()) ? "":fdev.getOverdueType());//超期类别
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getOverdueReason()) ? "":fdev.getOverdueReason());//申请原因
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getApplicantName()) ? "":fdev.getApplicantName());//申请人
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getApproveState()) ? "":CommonUtils.fdevApproveState(fdev.getApproveState()));//审批状态
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getApproverName()) ? "":fdev.getApproverName());//审批人
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getSectionName()) ? "":fdev.getSectionName());//所属条线
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getApplyTime()) ? "":fdev.getApplyTime());//申请审批时间
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getApproveTime()) ? "":fdev.getApproveTime());//审批时间
                sheet.getRow(i).createCell(j++).setCellValue(CommonUtils.isNullOrEmpty(fdev.getApproveReason()) ? "":fdev.getApproveReason());//审批说明
                i++;
            }

        }
        try {
            resp.reset(); resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setHeader("Content-Disposition", "attachment;filename=" +
                    "FdevUnitApprove"+ ".xlsx");
            workbook.write(resp.getOutputStream());
        } catch (IOException e) {
        }
    }



}
