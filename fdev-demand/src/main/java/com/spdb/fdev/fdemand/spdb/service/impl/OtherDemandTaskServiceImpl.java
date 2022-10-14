package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.*;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.IImplementUnitDao;
import com.spdb.fdev.fdemand.spdb.dao.IOtherDemandTaskDao;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import com.spdb.fdev.fdemand.spdb.entity.OtherDemandTask;
import com.spdb.fdev.fdemand.spdb.service.IOtherDemandTaskService;
import com.spdb.fdev.fdemand.spdb.service.IRoleService;
import com.spdb.fdev.fdemand.spdb.unit.DealTaskUnit;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RefreshScope
@Service
public class OtherDemandTaskServiceImpl implements IOtherDemandTaskService {

    @Autowired
    private IOtherDemandTaskDao otherDemandTaskDao;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IImplementUnitDao implementUnitDao;

    @Autowired
    private DealTaskUnit dealTaskUnit;

    @Override
    public String addOtherDemandTask(OtherDemandTask otherDemandTask) throws Exception {
        User user = CommonUtils.getSessionUser();
        //权限校验 需求管理员 需求牵头人
        boolean isDemandManager = roleService.isDemandManager();
        boolean isDemandLeader = roleService.isDemandLeader( otherDemandTask.getDemandId() , user.getId() );
        if( !isDemandManager && !isDemandLeader ){
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
        }
        String taskNum = "FDEV-" + otherDemandTask.getPrjNum() + "-" +
                "其他需求任务-" + otherDemandTask.getTaskClassify() + "-" ;
        OtherDemandTask otherDemandTaskByNum = otherDemandTaskDao.queryByTaskNum(taskNum);
        if (CommonUtils.isNullOrEmpty(otherDemandTaskByNum)) {
            taskNum = taskNum + "001";
        } else {
            String taskNumByNum = otherDemandTaskByNum.getTaskNum();
            String[] taskNumByNumArray = taskNumByNum.split("-");
            int no_latest = Integer.parseInt(taskNumByNumArray[taskNumByNumArray.length - 1]) + 1;
            taskNum = taskNum + String.format("%03d", no_latest);
        }
        otherDemandTask.setTaskNum(taskNum);//任务编号
        otherDemandTask.setStatus(Constants.NOTSTART);//默认未开始
        otherDemandTask.setCreateTime(TimeUtil.getTimeStamp(new Date()));//创建时间
        otherDemandTaskDao.addOtherDemandTask(otherDemandTask);
        return taskNum ;
    }

    @Override
    public String updateOtherDemandTask(OtherDemandTask otherDemandTask) throws Exception {
        OtherDemandTask otherDemandTaskByNum = otherDemandTaskDao.queryByTaskNum(otherDemandTask.getTaskNum());
        User user = CommonUtils.getSessionUser();
        //权限校验 需求管理员 需求牵头人 属于自己的任务负责人、厂商负责人
        boolean isDemandManager = roleService.isDemandManager();
        boolean isDemandLeader = roleService.isDemandLeader( otherDemandTask.getDemandId() , user.getId() );
        boolean isOtherDemandTaskLeader = roleService.isOtherDemandTaskLeader( otherDemandTaskByNum , user.getId() );

        if( !isDemandManager && !isDemandLeader && !isOtherDemandTaskLeader){
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
        }

        otherDemandTaskByNum.setTaskName(otherDemandTask.getTaskName());
        otherDemandTaskByNum.setTaskType(otherDemandTask.getTaskType());
        otherDemandTaskByNum.setTaskClassify(otherDemandTask.getTaskClassify());
        otherDemandTaskByNum.setPrjNum(otherDemandTask.getPrjNum());
        otherDemandTaskByNum.setPlanPrjName(otherDemandTask.getPlanPrjName());
        otherDemandTaskByNum.setHeaderUnitName(otherDemandTask.getHeaderUnitName());
        otherDemandTaskByNum.setHeaderTeamName(otherDemandTask.getHeaderTeamName());
        otherDemandTaskByNum.setLeaderGroup(otherDemandTask.getLeaderGroup());
        otherDemandTaskByNum.setLeaderGroupName(otherDemandTask.getLeaderGroupName());
        otherDemandTaskByNum.setTaskLeaderId(otherDemandTask.getTaskLeaderId());
        otherDemandTaskByNum.setTaskLeaderName(otherDemandTask.getTaskLeaderName());
        otherDemandTaskByNum.setFirmLeaderId(otherDemandTask.getFirmLeaderId());
        otherDemandTaskByNum.setFirmLeaderName(otherDemandTask.getFirmLeaderName());
        otherDemandTaskByNum.setPlanStartDate(otherDemandTask.getPlanStartDate());
        otherDemandTaskByNum.setPlanDoneDate(otherDemandTask.getPlanDoneDate());
        if(!CommonUtils.isNullOrEmpty(otherDemandTask.getExpectOwnWorkload())){
            otherDemandTaskByNum.setExpectOwnWorkload(otherDemandTask.getExpectOwnWorkload());
        }
        if(!CommonUtils.isNullOrEmpty(otherDemandTask.getExpectOutWorkload())){
            otherDemandTaskByNum.setExpectOutWorkload(otherDemandTask.getExpectOutWorkload());
        }
        otherDemandTaskDao.updateOtherDemandTask(otherDemandTaskByNum);
        return otherDemandTaskByNum.getTaskNum() ;
    }

    @Override
    public void updateOtherDemandTaskWorkload(Set<String> taskNums) throws Exception {
        if(!CommonUtils.isNullOrEmpty(taskNums)){
            for (String taskNum : taskNums) {
                OtherDemandTask otherDemandTaskByNum = otherDemandTaskDao.queryByTaskNum(taskNum);
                List<FdevImplementUnit> fdevImplementUnits = implementUnitDao.queryByOtherDemandTaskNum(taskNum);
                double deptWorkload = 0.00;
                double companyWorkload = 0.00;
                if(!CommonUtils.isNullOrEmpty(fdevImplementUnits)){
                    for(FdevImplementUnit temp : fdevImplementUnits){
                        if(ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue() <= temp.getImplement_unit_status_normal()){
                            deptWorkload += temp.getDept_workload();
                            companyWorkload += temp.getCompany_workload();
                        }
                    }
                }
                otherDemandTaskByNum.setExpectOwnWorkload(deptWorkload);
                otherDemandTaskByNum.setExpectOutWorkload(companyWorkload);
                otherDemandTaskDao.updateOtherDemandTask(otherDemandTaskByNum);
            }
        }
    }

    @Override
    public String deleteAllOtherDemandTask(DemandBaseInfo demandBaseInfo) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Dict.DEMANDID,demandBaseInfo.getId());//需求ID
        Map<String, Object> returnMap = otherDemandTaskDao.queryOtherDemandTaskList(params);
        List<OtherDemandTask> otherDemandTasks = (List<OtherDemandTask>) returnMap.get(Dict.DATA);
        if(!CommonUtils.isNullOrEmpty(otherDemandTasks)){
            for (OtherDemandTask otherDemandTask : otherDemandTasks) {
                otherDemandTaskDao.deleteOtherDemandTask(otherDemandTask);
            }
        }
        return null;
    }


    @Override
    public String deleteOtherDemandTask(Map<String, Object> params) throws Exception {
        String taskNum = (String) params.get(Dict.TASKNUM);//任务编号
        User user = CommonUtils.getSessionUser();
        OtherDemandTask otherDemandTaskByNum = otherDemandTaskDao.queryByTaskNum(taskNum);
        //需求牵头人
        if(!roleService.isDemandLeader(otherDemandTaskByNum.getDemandId(), user.getId())){
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{user.getUser_name_cn()});
        }
        List<FdevImplementUnit> fdevImplementUnits = implementUnitDao.queryByOtherDemandTaskNum(taskNum);
        if(CommonUtils.isNullOrEmpty(fdevImplementUnits)){
            otherDemandTaskDao.deleteOtherDemandTask(otherDemandTaskByNum);
        }else{
            throw new FdevException(ErrorConstants.DAILY_ERROR_NOT_DELETE);
        }
        return null;
    }

    @Override
    public Map<String, Object> queryOtherDemandTaskList(Map<String, Object> params) throws Exception {
        Map<String, Object> returnMap = otherDemandTaskDao.queryOtherDemandTaskList(params);
        List<OtherDemandTask> otherDemandTasks = (List<OtherDemandTask>) returnMap.get(Dict.DATA);
        if(!CommonUtils.isNullOrEmpty(otherDemandTasks)){
            User user = CommonUtils.getSessionUser();
            for (OtherDemandTask otherDemandTask : otherDemandTasks) {
                //权限校验 需求管理员 需求牵头人 属于自己的任务负责人、厂商负责人
                otherDemandTask.setIsUpdate(roleService.isDemandManager() || roleService.isDemandLeader(otherDemandTask.getDemandId(), user.getId())
                        || roleService.isOtherDemandTaskLeader(otherDemandTask, user.getId()));
                //删除权限校验  需求牵头人 并且研发单元为空
                if(!roleService.isDemandLeader(otherDemandTask.getDemandId(), user.getId())){
                    otherDemandTask.setIsDelete( "当前用户不为需求牵头人，不允许删除！" );
                }else if(!CommonUtils.isNullOrEmpty(implementUnitDao.queryByOtherDemandTaskNum(otherDemandTask.getTaskNum()))){
                    otherDemandTask.setIsDelete( "研发单元不为空，不允许删除！" );
                }else {
                    otherDemandTask.setIsDelete( "" );
                }
            }
            returnMap.put(Dict.DATA,otherDemandTasks);
        }
        return returnMap ;
    }

    @Override
    public OtherDemandTask queryOtherDemandTask(Map<String, Object> params) throws Exception {
        OtherDemandTask otherDemandTask = otherDemandTaskDao.queryByTaskNum((String) params.get(Dict.TASKNUM));
        if(!CommonUtils.isNullOrEmpty(otherDemandTask)){
            User user = CommonUtils.getSessionUser();
            //编辑权限校验 需求管理员 需求牵头人 属于自己的任务负责人、厂商负责人
            otherDemandTask.setIsUpdate(roleService.isDemandManager() || roleService.isDemandLeader(otherDemandTask.getDemandId(), user.getId())
                    || roleService.isOtherDemandTaskLeader(otherDemandTask, user.getId()));
            //删除权限校验  需求牵头人 并且研发单元为空
            if(!roleService.isDemandLeader(otherDemandTask.getDemandId(), user.getId())){
                otherDemandTask.setIsDelete( "当前用户不为需求牵头人，不允许删除！" );
            }else if(!CommonUtils.isNullOrEmpty(implementUnitDao.queryByOtherDemandTaskNum(otherDemandTask.getTaskNum()))){
                otherDemandTask.setIsDelete( "研发单元不为空，不允许删除！" );
            }else {
                otherDemandTask.setIsDelete( "" );
            }
        }
        return otherDemandTask ;
    }

    @Override
    public void updateStatus(OtherDemandTask otherDemandTask)  throws Exception {
        //修改其他需求任务状态与实际日期
        if (!CommonUtils.isNullOrEmpty(otherDemandTask) && !Dict.DELETE.equals(otherDemandTask.getStatus())) {
            //获取所有研发单元的状态和时间
            List<FdevImplementUnit> fdevUnitList = implementUnitDao.queryByOtherDemandTaskNum(otherDemandTask.getTaskNum());
            if (!CommonUtils.isNullOrEmpty(fdevUnitList)) {
                //获取所有研发单元最小阶段
                Integer minStage = fdevUnitList.get(0).getImplement_unit_status_normal();
                //获取所有研发单元最大阶段
                Integer maxStage = fdevUnitList.get( fdevUnitList.size() - 1 ).getImplement_unit_status_normal();
                //最大已完成 最小不为 已投产状态为 进行中
                if ( minStage < ImplementUnitEnum.ImplementUnitStatusEnum.PRODUCT.getValue()
                 && ImplementUnitEnum.ImplementUnitStatusEnum.PRODUCT.getValue().equals(maxStage)) {
                    minStage = ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue();
                }else{
                    minStage = maxStage ;
                }
                if(ImplementUnitEnum.ImplementUnitStatusEnum.DEVELOP.getValue().equals(minStage) ){
                    //开发中仅保留开发中状态及以上的研发单元
                    fdevUnitList = fdevUnitList.stream().filter(fdevUnit -> fdevUnit.getImplement_unit_status_normal() >
                            ImplementUnitEnum.ImplementUnitStatusEnum.PRE_IMPLEMENT.getValue()).collect(Collectors.toList());
                }else if(ImplementUnitEnum.ImplementUnitStatusEnum.PRODUCT.getValue().equals(minStage)){
                    //已完成状态及以上的研发单元
                    fdevUnitList = fdevUnitList.stream().filter(fdevUnit -> fdevUnit.getImplement_unit_status_normal() >=
                            ImplementUnitEnum.ImplementUnitStatusEnum.PRODUCT.getValue()).collect(Collectors.toList());
                }
                //notStart=未开始 going=进行中 done=已完成 delete=删除
                switch (minStage) {
                    //未开始
                    case 2:
                        otherDemandTask.setActualStartDate("");
                        otherDemandTask.setActualDoneDate("");
                        otherDemandTask.setStatus(Dict.NOTSTART);
                        break;
                    //进行中
                    case 3:
                        otherDemandTask.setActualStartDate(dealTaskUnit.getRealStartDate(fdevUnitList));
                        otherDemandTask.setActualDoneDate("");
                        otherDemandTask.setStatus(Dict.GOING);
                        break;
                    //已完成
                    case 7:
                        otherDemandTask.setActualStartDate(dealTaskUnit.getRealStartDate(fdevUnitList));
                        otherDemandTask.setActualDoneDate(dealTaskUnit.getRealProductDate(fdevUnitList));
                        otherDemandTask.setStatus(Dict.DONE);
                        break;
                    default:
                        break;
                }
            }else {
                //研发单元为空 将实际日期置空 状态置为评估中
                otherDemandTask.setActualStartDate("");
                otherDemandTask.setActualDoneDate("");
            }
            otherDemandTaskDao.updateOtherDemandTask(otherDemandTask);
        }
    }
}
