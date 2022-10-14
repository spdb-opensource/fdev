package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.IDemandAssessDao;
import com.spdb.fdev.fdemand.spdb.dao.IDemandBaseInfoDao;
import com.spdb.fdev.fdemand.spdb.dao.IFdevFinalDateApproveDao;
import com.spdb.fdev.fdemand.spdb.dto.DemandAssessDto;
import com.spdb.fdev.fdemand.spdb.dto.UpdateFinalDateQueryDto;
import com.spdb.fdev.fdemand.spdb.entity.DemandAssess;
import com.spdb.fdev.fdemand.spdb.entity.FdevFinalDateApprove;
import com.spdb.fdev.fdemand.spdb.service.AsyncService;
import com.spdb.fdev.fdemand.spdb.service.IFdevFinalDateApproveService;
import com.spdb.fdev.fdemand.spdb.service.IFdevUserService;
import com.spdb.fdev.fdemand.spdb.service.IRoleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.spdb.fdev.fdemand.base.dict.Dict.*;

@RefreshScope
@Service
public class FdevFinalDateApproveServiceImpl implements IFdevFinalDateApproveService {

    @Autowired
    private IDemandAssessDao demandAssessDao;

    @Autowired
    private IDemandBaseInfoDao demandBaseInfoDao;

    @Autowired
    private IFdevFinalDateApproveDao fdevFinalDateApproveDao;

    @Autowired
    private IFdevUserService fdevUserService;

    @Autowired
    private IRoleService roleService;

    @Value("${section.info}")
    private String sectionInfo;//各条线经理

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private IFdevUserService iFdevUserService;

    @Override
    public Map<String, Object> queryApproveList(UpdateFinalDateQueryDto queryDto) throws Exception {
        // 获取牵头组
        List<String> demandLeaderGroups = queryDto.getDemandLeaderGroups();
        List<String> accessIds = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(demandLeaderGroups)) {
            // 查询该牵头组牵头了哪些评估需求
            DemandAssessDto dto = new DemandAssessDto();
            dto.setDemandLeaderGroups(demandLeaderGroups);
            Map<String, Object> query = demandAssessDao.query(dto);
            List<DemandAssess> data = (List<DemandAssess>) query.get(Dict.DATA);
            for (DemandAssess demandAssess : data) {
                accessIds.add(demandAssess.getId());
            }
        }
        Map<String, Object> map = fdevFinalDateApproveDao.queryAll(queryDto, accessIds);
        List<FdevFinalDateApprove> data = (List<FdevFinalDateApprove>) map.get(Dict.APPROVELIST);
        if (!CommonUtils.isNullOrEmpty(data)) {
            completeUpdateFinalDateApprove(data);
        }
        return map;
    }

    @Override
    public Map<String, Object> queryMyList(UpdateFinalDateQueryDto queryDto) throws Exception {
        User user = CommonUtils.getSessionUser();
        String email = user.getEmail();
        Map<String, Object> map = new HashMap<>();
        //默认值
        map.put(COUNT, 0);
        map.put(Dict.APPROVELIST, new ArrayList<>());
        //条线经理列表包含当前用户
        if (sectionInfo.contains(email)) {
            String sectionId = "";
            //截取条线经理
            String[] split = sectionInfo.split(";");
            for (String sectionStr : split) {
                //判断当前用户属于哪个条线
                String[] sectionStrSplit = sectionStr.split(":");
                if (sectionStrSplit[1].contains(email)) {
                    //赋值条线Id
                    sectionId = sectionStrSplit[0];
                    break;
                }
            }
            if (!CommonUtils.isNullOrEmpty(sectionId)) {
                map = fdevFinalDateApproveDao.queryMyApprove(sectionId, queryDto);
            }
            List<FdevFinalDateApprove> data = (List<FdevFinalDateApprove>) map.get(Dict.APPROVELIST);
            if (!CommonUtils.isNullOrEmpty(data)) {
                completeUpdateFinalDateApprove(data);
            }
        }
        return map;
    }

    @Override
    public String agree(UpdateFinalDateQueryDto updateDto) throws Exception {
        User user = CommonUtils.getSessionUser();
        // 先判断是否可以进行审批
        List<FdevFinalDateApprove> fdevFinalDateApproveList = fdevFinalDateApproveDao.listByIds(updateDto.getIds());
        // 如果发现传递的需求评估id 都没有内容则报错
        if (CommonUtils.isNullOrEmpty(fdevFinalDateApproveList)) {
            throw new FdevException(ErrorConstants.FINAL_DATE_APPROVE_NULL);
        }
        // 如果发现其中有已经完成审批的也报错
        for (FdevFinalDateApprove finalDateApprove : fdevFinalDateApproveList) {
            if (!finalDateApprove.getOperate_status().equals(UNDETERMINED)) {
                throw new FdevException(ErrorConstants.FINAL_DATE_APPROVE_FORBID);
            }
        }
        // 批量更新
        UpdateFinalDateQueryDto newDto = new UpdateFinalDateQueryDto();
        newDto.setIds(updateDto.getIds());
        newDto.setOperateUser(user.getId());
        newDto.setStatus(AGREE);
        List<String> listEmpty = new ArrayList<>();
        List<FdevFinalDateApprove> fdevFinalDateApproves = fdevFinalDateApproveDao.updateBatch(newDto);
        StringBuilder stringBuilder = new StringBuilder();
        // 统计有异常的数据
        for (FdevFinalDateApprove finalDateApprove : fdevFinalDateApproves) {
            DemandAssess demandAssess = demandAssessDao.queryById(finalDateApprove.getAccess_id());
            if (CommonUtils.isNullOrEmpty(demandAssess)) {
                listEmpty.add(finalDateApprove.getOa_contact_no());
                continue;
            }
            // 通过id更新修改定稿日期完成记录以及定稿日期数据
            List<String> approveTimeList = demandAssess.getApprove_time_list();
            approveTimeList.add(finalDateApprove.getOperate_time());
            DemandAssess entity = new DemandAssess();
            entity.setId(demandAssess.getId());
            entity.setApprove_time_list(approveTimeList);
            entity.setFinal_date(finalDateApprove.getApply_update_time());
            demandAssessDao.update(entity);
            // 给对应的人发邮件
            Set<String> sendEmailSet = new HashSet<>();
            HashSet<String> demandLeader = demandAssess.getDemand_leader();
            String applyUserId = finalDateApprove.getApply_user_id();
            sendEmailSet.add(applyUserId);
            if (!CommonUtils.isNullOrEmpty(demandLeader)) {
                sendEmailSet.addAll(demandLeader);
            }
            if (!CommonUtils.isNullOrEmpty(sendEmailSet)) {
                Set<String> sendSet = new HashSet<>();
                for (String userId : sendEmailSet) {
                    Map map = iFdevUserService.queryUserInfo(userId);
                    String email = (String) map.get("email");
                    sendSet.add(email);
                }
                List<String> sendEmailList = new ArrayList<>(sendSet);
                String oaContactNo = demandAssess.getOa_contact_no();
                String oaContactName = demandAssess.getOa_contact_name();
                // 异步发送通过邮件
                asyncService.agreeUpdateFinalDate(oaContactNo, oaContactName, user.getUser_name_cn(), demandAssess.getId(), sendEmailList);
            }
        }
        if (!CommonUtils.isNullOrEmpty(listEmpty)) {
            String join = StringUtils.join(listEmpty, ",");
            stringBuilder.append("下列需求评估编号（" + join + ") 审批失败，请查看对应需求评估数据是否存在!");
        }
        return stringBuilder.toString();
    }

    @Override
    public void refuse(FdevFinalDateApprove updateDto) throws Exception {
        User user = CommonUtils.getSessionUser();
        String id = updateDto.getId();
        FdevFinalDateApprove queryById = fdevFinalDateApproveDao.queryById(id);
        // 判断id 是否出错
        if (CommonUtils.isNullOrEmpty(queryById)) {
            throw new FdevException(ErrorConstants.FINAL_DATE_APPROVE_NULL);
        }
        // 如果不是待审批状态则不允许审批
        if (!UNDETERMINED.equals(queryById.getOperate_status())) {
            throw new FdevException(ErrorConstants.FINAL_DATE_APPROVE_FORBID);
        }
        // 更新数据
        FdevFinalDateApprove newDto = new FdevFinalDateApprove();
        newDto.setId(id);
        newDto.setOperate_user_id(user.getId());
        newDto.setOperate_status(DISAGREE);
        newDto.setState(updateDto.getState());
        newDto.setOperate_time(TimeUtil.formatTodayHs());
        FdevFinalDateApprove finalDateApprove = fdevFinalDateApproveDao.update(newDto);
        DemandAssess demandAssess = demandAssessDao.queryById(finalDateApprove.getAccess_id());
        if (CommonUtils.isNullOrEmpty(demandAssess)) {
            throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR);
        }
        // 发送邮件
        Set<String> sendEmailSet = new HashSet<>();
        HashSet<String> demandLeader = demandAssess.getDemand_leader();
        String applyUserId = finalDateApprove.getApply_user_id();
        sendEmailSet.add(applyUserId);
        if (!CommonUtils.isNullOrEmpty(demandLeader)) {
            sendEmailSet.addAll(demandLeader);
        }
        if (!CommonUtils.isNullOrEmpty(sendEmailSet)) {
            Set<String> sendSet = new HashSet<>();
            for (String userId : sendEmailSet) {
                Map map = iFdevUserService.queryUserInfo(userId);
                String email = (String) map.get("email");
                sendSet.add(email);
            }
            List<String> sendEmailList = new ArrayList<>(sendSet);
            String oaContactNo = demandAssess.getOa_contact_no();
            String oaContactName = demandAssess.getOa_contact_name();
            // 异步发送通过邮件
            asyncService.refuseUpdateFinalDate(oaContactNo, oaContactName, user.getUser_name_cn(), updateDto.getState(), demandAssess.getId(), sendEmailList);
        }
    }

    @Override
    public Map<String, Long> queryCount() throws Exception {
        User user = CommonUtils.getSessionUser();
        String email = user.getEmail();
        Map<String, Long> map = new HashMap<>();
        //默认值
        map.put(WAITCOUNT, 0L);
        map.put(DONECOUNT, 0L);
        //条线经理列表包含当前用户
        if (sectionInfo.contains(email)) {
            String sectionId = "";
            //截取条线经理
            String[] split = sectionInfo.split(";");
            for (String sectionStr : split) {
                //判断当前用户属于哪个条线
                String[] sectionStrSplit = sectionStr.split(":");
                if (sectionStrSplit[1].contains(email)) {
                    //赋值条线Id
                    sectionId = sectionStrSplit[0];
                    break;
                }
            }
            if (!CommonUtils.isNullOrEmpty(sectionId)) {
                map = fdevFinalDateApproveDao.queryCount(sectionId);
            }
        }
        return map;
    }


    private void completeUpdateFinalDateApprove(List<FdevFinalDateApprove> data) throws Exception {
        //获取条线名称
        List<Map<String, Object>> allSection = fdevUserService.queryGroupTwoAll();
        for (FdevFinalDateApprove fdevFinalDateApprove : data) {
            //获取条线名称
            String sectionName = "";
            for (Map<String, Object> sectionMap : allSection) {
                if (fdevFinalDateApprove.getSection_id().equals(sectionMap.get(Dict.ID))) {
                    sectionName = (String) sectionMap.get(Dict.NAME);
                    break;
                }
            }
            fdevFinalDateApprove.setSection_id_cn(sectionName);
            //通过需求编号查询需求信息
            DemandAssess demandAssess = demandAssessDao.queryById(fdevFinalDateApprove.getAccess_id());
            if (!CommonUtils.isNullOrEmpty(demandAssess)) {
                fdevFinalDateApprove.setOa_contact_name(demandAssess.getOa_contact_name());
                // 查询牵头小组名称
                Map<String, Object> groupMap = roleService.queryGroup(demandAssess.getDemand_leader_group());
                fdevFinalDateApprove.setDemand_leader_group(demandAssess.getDemand_leader_group());//小组ID
                fdevFinalDateApprove.setDemand_leader_group_cn((String) groupMap.get(Dict.GROUP_NAME));//小组名称
            }
            Map<String, Object> applicantInfo = roleService.queryUserbyid(fdevFinalDateApprove.getApply_user_id());
            // 申请人名称
            if (!CommonUtils.isNullOrEmpty(fdevFinalDateApprove.getApply_user_id())) {
                //申请人姓名
                fdevFinalDateApprove.setApply_user((String) applicantInfo.get(Dict.USER_NAME_CN));
            }
            Map<String, Object> operateInfo = roleService.queryUserbyid(fdevFinalDateApprove.getOperate_user_id());
            // 审批人名称
            if (!CommonUtils.isNullOrEmpty(fdevFinalDateApprove.getOperate_user_id())) {
                //申请人姓名
                fdevFinalDateApprove.setOperate_user((String) operateInfo.get(Dict.USER_NAME_CN));
            }
            // 审批结果
            String operateStatus = fdevFinalDateApprove.getOperate_status();
            if (!CommonUtils.isNullOrEmpty(operateStatus)) {
                switch (operateStatus) {
                    case AGREE:
                        fdevFinalDateApprove.setOperate_status_cn("同意");
                        break;
                    case DISAGREE:
                        fdevFinalDateApprove.setOperate_status_cn("拒绝");
                        break;
                    case UNDETERMINED:
                        fdevFinalDateApprove.setOperate_status_cn("待审批");
                        break;
                }
            }

        }
    }
}
