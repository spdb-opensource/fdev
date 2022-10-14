package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.DemandEnum;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IDemandBaseInfoDao;
import com.spdb.fdev.fdemand.spdb.dao.IImplementUnitDao;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.entity.RelatePartDetail;
import com.spdb.fdev.fdemand.spdb.service.ICommonBusinessService;
import com.spdb.fdev.fdemand.spdb.service.ITaskService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements ITaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private IImplementUnitDao implementUnitDao;

    @Autowired
    private IDemandBaseInfoDao demandBaseInfoDao;

    @Autowired
    private ICommonBusinessService commonBusinessService;
    /**
     * 修改任务状态为暂缓或恢复
     *
     * @param demandId
     */
    @Override
    public void taskDeferOrRecover(String demandId, Integer status) {
        try {
            List<FdevImplementUnit> unitList = implementUnitDao.queryByDemandId(demandId);
            Set<String> ids = new HashSet<>();
            if (!CommonUtils.isNullOrEmpty(unitList)) {
                unitList.forEach(fdevImplementUnit -> ids.add(fdevImplementUnit.getFdev_implement_unit_no()));
                Map<String, Object> send_map = new HashMap<>();
                send_map.put(Dict.REST_CODE, Dict.TASKRECOVER);
                send_map.put(Dict.IDS, ids);
                send_map.put(Dict.TASK_SPECTIAL_STATUS, status);
                Object count = restTransport.submit(send_map);
                logger.info("调用任务模块修改任务状态成功,共修改{}条", count);
            } else {
                logger.info("当前需求下无实施单元");
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.QUERY_TASK_FAIL, new String[]{e.getMessage()});
        }
    }

    /**
     * 查询需求是否可以暂缓和撤销
     *
     * @param demandId
     * @return
     */
    @Override
    public Map judgeDeferOrDelete(String demandId) {
        Map<String, Object> send_map = new HashMap<>();
        try {
            List<FdevImplementUnit> unitList = implementUnitDao.queryByDemandId(demandId);
            Set<String> ids = new HashSet<>();
            if (!CommonUtils.isNullOrEmpty(unitList)) {
                unitList.forEach(fdevImplementUnit -> ids.add(fdevImplementUnit.getFdev_implement_unit_no()));
                send_map.put(Dict.IDS, ids);
            }
            send_map.put(Dict.DEMAND_ID, demandId);
            send_map.put(Dict.REST_CODE, Dict.DEFER_OR_DELETE);
            Map<String, Object> result = (Map) restTransport.submit(send_map);
            return result;

        } catch (Exception e) {
            throw new FdevException(ErrorConstants.QUERY_TASK_FAIL, new String[]{e.getMessage()});
        }
    }

    /**
     * 通过实施单元 修改任务状态为恢复
     *
     * @param
     */
    @Override
    public void taskRecoverByImpl(String implId, Integer status) {
        try {
            Set<String> ids = new HashSet<>();
            ids.add(implId);
            Map<String, Object> send_map = new HashMap<>();
            send_map.put(Dict.REST_CODE, Dict.TASKRECOVER);
            send_map.put(Dict.IDS, ids);
            send_map.put(Dict.TASK_SPECTIAL_STATUS, status);
            Object count = restTransport.submit(send_map);
            logger.info("调用任务模块修改任务状态成功,共修改{}条", count);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.QUERY_TASK_FAIL, new String[]{e.getMessage()});
        }
    }

    @Override
    public List<Map> queryTaskByDemandId(String demandId) {
        try {
            Map<String, Object> send_map = new HashMap<>();
            send_map.put(Dict.REST_CODE, "queryTaskByDemandId");
            send_map.put(Dict.DEMANDID, demandId);
            List<Map> task = (List<Map>) restTransport.submit(send_map);
            return task;
        } catch (Exception e) {
            logger.info("调用任务模块查询需求下任务失败");
            throw new FdevException(ErrorConstants.QUERY_TASK_FAIL, new String[]{e.getMessage()});
        }
    }

    @Override
    public HashMap<String,String> queryDemandStatusByPart(DemandBaseInfo demandBaseInfo) {
        List<RelatePartDetail> relatePartDetailList = demandBaseInfo.getRelate_part_detail();
        HashMap<String, String> implDateAndStatus = new HashMap<>();
        if (CommonUtils.isNullOrEmpty(relatePartDetailList)) {
            //说明没有涉及板块，需求状态为预评估
            implDateAndStatus.put(Dict.PARTSTATUS, DemandEnum.DemandAssess_status.PRE_EVALUATE.getValue());
            return implDateAndStatus;
        }
        String demandId = demandBaseInfo.getId();
        List<RelatePartDetail> collect = relatePartDetailList.stream()
                .sorted(Comparator.comparing(RelatePartDetail::getAssess_status))
                .collect(Collectors.toList());
        if (DemandEnum.DemandAssess_status.PRE_EVALUATE.getValue().equals(collect.get(0).getAssess_status())) {
            //说明所有板块均未评估，则需求状态为预评估
            implDateAndStatus.put(Dict.PARTSTATUS, DemandEnum.DemandAssess_status.PRE_EVALUATE.getValue());
            return implDateAndStatus;
        } else if (DemandEnum.DemandAssess_status.EVALUATE.getValue().equals(collect.get(0).getAssess_status())) {
            //说明所有板块至少有评估中，则需求状态为评估中，获取实施单元的计划日期和实际日期
            implDateAndStatus = commonBusinessService.dateByDemandIdMap(demandId);
            implDateAndStatus.put(Dict.PARTSTATUS, DemandEnum.DemandAssess_status.EVALUATE.getValue());
            return implDateAndStatus;
        } else if (DemandEnum.DemandAssess_status.EVALUATE_OVER.getValue().equals(collect.get(0).getAssess_status())) {
            //说明所有板块都已经评估完成，则需求状态和实施单元的保持一致，获取实施单元的计划日期和实际日期
            implDateAndStatus = commonBusinessService.dateByDemandIdMap(demandId);
            implDateAndStatus.put(Dict.PARTSTATUS, DemandEnum.DemandAssess_status.EVALUATE_OVER.getValue());
            return implDateAndStatus;
        }
        return null;
    }

    /**
     * 查询 组下面 挂载的任务
     * @param groups 组id集合
     * @param stage 任务状态
     */
    public List queryByTerms(List<String> groups, List<String> stage, Boolean isParent) throws Exception {
        Map send = new HashMap();
        if (CommonUtils.isNullOrEmpty(groups) || CommonUtils.isNullOrEmpty(stage)){
            return null;
        }
        send.put(Dict.REST_CODE, "queryByTerms");
        send.put("group", groups);
        send.put("stage", stage);
        send.put("isIncludeChildren", isParent);
        return (List)((HashMap)restTransport.submit(send)).get("list");
    }


    @Override
    public List findTaskByTerms(Set<String> groupIds, Boolean isParent) throws Exception {
        if(CommonUtils.isNullOrEmpty(groupIds)){
            return null;
        }
        List<String> stage = new ArrayList<>();
        stage.add("develop");
        stage.add("sit");
        stage.add("uat");
        stage.add("rel");
        stage.add("create-info");
        stage.add("create-app");
        stage.add("create-feature");
        List<String> groups = new ArrayList<>();
        groups.addAll(groupIds);
        return queryByTerms(groups, stage, isParent);
    }
    
    /**
     * 根据研发单元查询任务信息（不为删除和废弃状态的任务数量）
     */
    @Override
    public Integer queryNotDiscarddnum(String fdevImplNo){
    	 try {
             Map<String, Object> send_map = new HashMap<>();
             send_map.put(Dict.REST_CODE, "queryNotDiscarddnum");
             send_map.put(Dict.FDEV_IMPLEMENT_UNIT_NO, fdevImplNo);
             Integer task = (Integer) restTransport.submit(send_map);
             return task;
         } catch (Exception e) {
             logger.info("调用任务模块通过研发单元编号查询任务");
             throw new FdevException(ErrorConstants.QUERY_TASK_FAIL, new String[]{e.getMessage()});
         }
    }
    
    @Override
    public void updateDemandStatus(String id)  throws Exception{
    	// TODO Auto-generated method stub
    			DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(id);
    	    	ArrayList<RelatePartDetail> relatePartDetail = demandBaseInfo.getRelate_part_detail();
    	    	Integer status = DemandEnum.DemandStatusEnum.PRE_EVALUATE.getValue();
    	    	Boolean flag_pre = false; //false 板块没有预评估状态 true 板块有预评估状态
    	    	Boolean flag = false; //false 板块没有评估中状态 true 板块有评估中状态
    	    	Boolean flag_over = false; //false 板块没有评估完成状态 true 板块有评估完成状态
    	    	if(!CommonUtils.isNullOrEmpty(relatePartDetail)) {
    	    		for(RelatePartDetail relatePar : relatePartDetail) {
    	    			if(DemandEnum.DemandAssess_status.PRE_EVALUATE.getValue().equals(relatePar.getAssess_status())) {
    	    				flag_pre = true;
    	    			}else if(DemandEnum.DemandAssess_status.EVALUATE.getValue().equals(relatePar.getAssess_status())) {
    	    				flag = true;
    	    			}else if(DemandEnum.DemandAssess_status.EVALUATE_OVER.getValue().equals(relatePar.getAssess_status())) {
    	    				flag_over = true;
    	    			}
    	    		}
    	    	}
    	    	//根据研发单元判断需求状态
    	    	List<FdevImplementUnit> implList = implementUnitDao.queryByDemandId(id);
    	    	Integer maxStatus = 0;
    	    	Integer minStatus = 10;
    	    	Integer statusImpl = 0;
    	    	if(!CommonUtils.isNullOrEmpty(implList)) {
    	    		statusImpl = 1;
    	    		for(FdevImplementUnit fdevImplementUnit : implList) {
    	    			Integer implStatus = fdevImplementUnit.getImplement_unit_status_normal();
    	    			if(implStatus > maxStatus) {
    	    				maxStatus = implStatus;
    	    			}
    	    			if(implStatus < minStatus) {
    	    				minStatus = implStatus;
    	    			}
    	    		}
    	    		if(minStatus == 2 && maxStatus == 2) {
    	    			statusImpl = 2;
    	    		}else if(minStatus == 3){
    	    			statusImpl = 3;
    	    		}else if(minStatus == 4){
    	    			statusImpl = 4;
    	    		}else if(minStatus == 5){
    	    			statusImpl = 5;
    	    		}else if(minStatus == 6){
    	    			statusImpl = 6;
    	    		}else if(minStatus == 7){
    	    			statusImpl = 7;
    	    		}else if(minStatus == 2 && maxStatus >= 3) {
    	    			statusImpl = 3;
    	    		}
    	    	}
    	    	if(flag_pre && !flag && !flag_over) {//所有板块为预评估 需求为预评估
    	    		status = DemandEnum.DemandStatusEnum.PRE_EVALUATE.getValue();
    	    	}else if(!flag_pre && !flag && flag_over && statusImpl==2) {//当所有板块为评估完成且研发单元为待实施 需求为待实施
    	    		status = DemandEnum.DemandStatusEnum.PRE_IMPLEMENT.getValue();
    	    	}else if(!flag_pre && !flag && flag_over && statusImpl > 2) {//当所有板块为评估完成需求根据研发单元变动状态
    	    		status = statusImpl;
    	    	}else if(flag_pre && !flag && flag_over) {//当存在评估完成和预评估时 需求为评估中
    	    		status = DemandEnum.DemandStatusEnum.EVALUATE.getValue();
    	    	}else if(flag) {//当所有板块为评估中 需求为评估中
    	    		status = DemandEnum.DemandStatusEnum.EVALUATE.getValue();
    	    	}else if(flag_over) {
    	    		status = DemandEnum.DemandStatusEnum.PRE_IMPLEMENT.getValue();
    	    	}
    	    	
    	    	demandBaseInfo.setDemand_status_normal(status);
    	    	demandBaseInfoDao.update(demandBaseInfo);
    }

    /**
     * 通过
     * @param fdevImplUnitNo
     * @return
     */
    @Override
    public List<Map> queryDetailByUnitNo(List<String> fdevImplUnitNo) {
        List<Map> result = new ArrayList<>();
        try {
            if(!CommonUtils.isNullOrEmpty(fdevImplUnitNo)){
                Map<String, Object> send_map = new HashMap<>();
                send_map.put(Dict.REST_CODE, "queryDetailByUnitNo");
                send_map.put(Dict.IDS, fdevImplUnitNo);
                result = (List<Map>) restTransport.submit(send_map);
            }
        } catch (Exception e) {
            logger.info("调用任务模块通过研发单元编号查询任务");
            throw new FdevException(ErrorConstants.QUERY_TASK_FAIL, new String[]{e.getMessage()});
        }
        return result;
    }
    /**
     * 通过
     * @param
     * @return
     */
    @Override
    public void updateTaskSpectialStatus(String unitNo ,String stage ,String taskSpectialStatus ) {
        try {
            Map<String, Object> send_map = new HashMap<>();
            send_map.put(Dict.REST_CODE, "updateTaskSpectialStatus");
            send_map.put(Dict.UNITNO, unitNo);
            if(!CommonUtils.isNullOrEmpty(stage)){
                send_map.put(Dict.STAGE, stage);
            }
            if(!CommonUtils.isNullOrEmpty(taskSpectialStatus)){
                send_map.put(Dict.TASKSPECTIALSTATUS, taskSpectialStatus);
            }
            restTransport.submit(send_map);
        } catch (Exception e) {
            logger.info("调用任务模块更改任务状态为暂缓或取消");
            throw new FdevException(ErrorConstants.QUERY_TASK_FAIL, new String[]{e.getMessage()});
        }
    }

    /**
     * 调用任务模块更改任务提交用户测试时间
     * @param
     * @return
     */
    @Override
    public void updateStartUatTime(String taskId ,String start_uat_test_time ) {
        try {
            Map<String, Object> send_map = new HashMap<>();
            send_map.put(Dict.REST_CODE, "updateStartUatTime");
            send_map.put(Dict.ID, taskId);
            send_map.put(Dict.START_UAT_TEST_TIME, start_uat_test_time);
            restTransport.submit(send_map);
        } catch (Exception e) {
            logger.info("调用任务模块更改任务提交用户测试时间失败");
            throw new FdevException(ErrorConstants.QUERY_TASK_FAIL, new String[]{e.getMessage()});
        }
    }

    /**
     * 通过
     * @param fdevImplUnitNo
     * @return
     */
    @Override
    public List<Map> queryTaskByUnitNos(List<String> fdevImplUnitNo) {
        List<Map> result = new ArrayList<>();
        try {
            if(!CommonUtils.isNullOrEmpty(fdevImplUnitNo)){
                Map<String, Object> send_map = new HashMap<>();
                send_map.put(Dict.REST_CODE, "queryTaskByUnitNos");
                send_map.put(Dict.IDS, fdevImplUnitNo);
                result = (List<Map>) restTransport.submit(send_map);
            }
        } catch (Exception e) {
            logger.info("调用任务模块通过研发单元编号查询任务");
            throw new FdevException(ErrorConstants.QUERY_TASK_FAIL, new String[]{e.getMessage()});
        }
        return result;
    }
    /**
     * 调用任务模块查询任务文档列表
     * @param taskId
     * @return
     */
    @Override
    public List<Map> queryDocDetail(String taskId) {
        List<Map> result = new ArrayList<>();
        try {
            if(!CommonUtils.isNullOrEmpty(taskId)){
                Map<String, Object> send_map = new HashMap<>();
                send_map.put(Dict.REST_CODE, "queryDocDetail");
                send_map.put(Dict.ID, taskId);
                Map<String, Object> data = (Map<String, Object>) restTransport.submit(send_map);
                if(!CommonUtils.isNullOrEmpty(data)){
                    result = (List<Map>) data.get(Dict.DOC);
                }
            }
        } catch (Exception e) {
            logger.info("调用任务模块查询任务文档列表失败");
            throw new FdevException(ErrorConstants.QUERY_TASK_FAIL, new String[]{e.getMessage()});
        }
        return result;
    }

}
