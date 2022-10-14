package com.plan.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.plan.dict.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plan.datamodel.PlanList;
import com.plan.dict.Dict;
import com.plan.dict.ErrorConstants;
import com.plan.service.PlanListService;
import com.plan.util.Utils;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import com.test.testmanagecommon.vaildate.RequestValidate;


@RestController
@RequestMapping("/plan")
@SuppressWarnings({"unused"})
public class PlanListController {

    @Autowired
    private PlanListService planListService;

    private Logger logger = LoggerFactory.getLogger(PlanListController.class);

    /**
     * 根据计划ID和工单编号精准删除计划
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID, Dict.WORKNO})
    @PostMapping(value = "/delPlan")
    public JsonResult deleteByPrimaryKey(@RequestBody Map map) throws Exception {
        int row = -1;
        row = planListService.deleteByPlanId(map);
        if(row!=1){
            throw new FtmsException(ErrorConstants.DATA_DELETE_ERROR);
        }
        return JsonResultUtil.buildSuccess(row);
    }

    /**
     * @param requestMap 计划实体
     * @return 受影响行数
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANNAME ,Dict.PLANSTARTDATE,Dict.PLANENDDATE,Dict.WORKNO})
    @PostMapping(value = "/addPlan")
    public JsonResult insert(@RequestBody Map<String,String> requestMap) throws Exception {
    	String planName = requestMap.get(Dict.PLANNAME);
    	String planEndDate = requestMap.get(Dict.PLANENDDATE);
    	String planStartDate = requestMap.get(Dict.PLANSTARTDATE);
    	String workNo = requestMap.get(Dict.WORKNO);
    	String deviceInfo = requestMap.get(Dict.DEVICEINFO);
    	if (Util.isNullOrEmpty(deviceInfo)){
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"设备信息不能为空"});
        }
    	PlanList planList = new PlanList();
    	planList.setPlanName(planName);
    	planList.setPlanEndDate(planEndDate);
    	planList.setPlanStartDate(planStartDate);
    	planList.setWorkNo(workNo);
    	planList.setDeviceInfo(deviceInfo);
    	int count = planListService.selectByWorkNoByPlanNameCount(workNo,planName);
    	if(count > 0) {
    		throw new FtmsException(ErrorConstants.REPET_PLAN_LIST_NAME);
    	}
        int row = -1;
        try {
            row = this.planListService.insert(planList);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR);
        }
        return JsonResultUtil.buildSuccess(row);
    }

    /**
     * 根据PlanId查询单条数据
     *
     * @param map
     * @return 返回一个条数据
     */
    @RequestValidate(NotEmptyFields = {Dict.PLANID})
    @PostMapping(value = "/queryByPlanId")
    public JsonResult queryByPlanId(@RequestBody Map map) throws Exception {
        Integer planId = (Integer) map.get(Dict.PLANID);
        List<PlanList> planLists = new ArrayList<>();
        try {
            planLists.add(this.planListService.selectByPlanId(planId));
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return JsonResultUtil.buildSuccess(planLists);
    }

    /**
     * 查询全部计划
     *
     * @return 返回计划List
     */
    @PostMapping(value = "/queryPlanListAll")
    public JsonResult selectAll() throws Exception {
        List<PlanList> lists = null;
        try {
            lists = this.planListService.selectAll();
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return JsonResultUtil.buildSuccess(lists);
    }

    /**
     * 根据主键修改数据
     * @param requestMap
     * @return 受影响行数
     */
    @PostMapping(value = "/updateByPrimaryKey")
    public JsonResult updateByPrimaryKey(@RequestBody Map<String,Object> requestMap) throws Exception {
    	Integer plan_id = (Integer) requestMap.get(Dict.PLANID);
    	String planName = (String) requestMap.get(Dict.PLANNAME);
    	String planEndDate = (String) requestMap.get(Dict.PLANENDDATE);
    	String planStartDate = (String) requestMap.get(Dict.PLANSTARTDATE);
    	String deviceInfo = (String)requestMap.get(Dict.DEVICEINFO);
    	PlanList planList = planListService.selectByPlanId(plan_id);
    	if(Utils.isEmpty(planList)) {
    		throw new FtmsException(ErrorConstants.DATA_NOT_EXIST);
    	}
    	PlanList NewPlanList = new PlanList();
    	NewPlanList.setPlanId(plan_id);
    	NewPlanList.setPlanStartDate(planStartDate);
    	NewPlanList.setPlanEndDate(planEndDate);
    	NewPlanList.setDeviceInfo(deviceInfo);
    	if(Utils.isEmpty(planName)) {//若送值planName 则代表需修改 
    		NewPlanList.setPlanName(planList.getPlanName());
    	}else {
    		NewPlanList.setPlanName(planName);
    	}
    	NewPlanList.setPlanName(planName);
    	NewPlanList.setWorkNo(planList.getWorkNo());
        planListService.updateByPrimaryKey(NewPlanList);
        return JsonResultUtil.buildSuccess(planListService.selectByPlanId(plan_id));
    }

    /**
     * 传入 工单号，或者计划名称 查询总数
     * ************************************
     *
     * @param map
     * @return 根据条件查询到的计划总数
     */
    @RequestValidate(NotBothEmptyFields = {Dict.WORKNO , Dict.PLANNAME})
    @PostMapping(value = "/selectByWorkNoByPlanNameCount")
    public JsonResult selectByWorkNoByPlanNameCount(@RequestBody Map map) throws Exception {
        String workNo = (String) map.get(Dict.WORKNO);
        String planName =(String) map.get(Dict.PLANNAME);
        int count = -1;
        try {
            count = this.planListService.selectByWorkNoByPlanNameCount(workNo,planName);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return JsonResultUtil.buildSuccess(count);
    }

    /**
     * 根据工单查询 (一)
     *
     * @param map
     * @return 返回一个条数据
     */
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    @PostMapping(value = "/queryByworkNo")
    public JsonResult selectByWorkNo(@RequestBody Map map) throws Exception {
        String  workNo=(String) map.get(Dict.WORKNO);
        List<PlanList> planLists = new ArrayList<PlanList>();
        try {
            planLists = this.planListService.queryByworkNo(workNo);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return JsonResultUtil.buildSuccess(planLists);
    }

//    /**
//     * 根据执行计划id修改工单编号
//     * @return
//     */
//    @RequestValidate(NotEmptyFields = {Dict.WORKNO,Dict.ID})
//    @PostMapping(value = "/updateWorkNoByPlanId")
//    public JsonResult updateWorkNoByPlanId(@RequestBody Map map) throws Exception {
//        String  workNo=(String) map.get(Dict.WORKNO);
//        Integer planId = (Integer) map.get(Dict.ID);
//        Integer result=null;
//        try {
//            result=this.planListService.updateWorkNoByPlanId(workNo,planId);
//        } catch (Exception e) {
//            logger.error(e.toString());
//            throw new FtmsException(ErrorConstants.UPDATE_PLANLIST_ERROR);
//        }
//        if(result==1){
//            return JsonResultUtil.buildSuccess("修改执行计划成功！");
//        }
//        return JsonResultUtil.buildError(Constants.I_FAILED,"修改执行计划失败！");
//    }
}
