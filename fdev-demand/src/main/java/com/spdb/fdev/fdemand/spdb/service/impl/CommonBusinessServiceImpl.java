package com.spdb.fdev.fdemand.spdb.service.impl;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IDemandBaseInfoDao;
import com.spdb.fdev.fdemand.spdb.dao.IImplementUnitDao;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.service.ICommonBusinessService;
import com.spdb.fdev.fdemand.spdb.service.IImplementUnitService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommonBusinessServiceImpl implements ICommonBusinessService {
    private static final Logger logger = LoggerFactory.getLogger(CommonBusinessServiceImpl.class);
    @Autowired
    private IImplementUnitService implementUnitService;
    @Autowired
    private IImplementUnitDao implementUnitDao;

    @Autowired
    private IDemandBaseInfoDao demandBaseInfoDao;

    @Autowired
    private RestTransport restTransport;

    @Value("${yuheng.topic.deleteOrder}")
    private String topicDel;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Value("${yuheng.topic.createWorkOrder}")
    private String topic;

    @Override
    public void updateUnitStatus(String demandId, int status, String field) throws Exception {
        //根据需求id查询所有研发单元
        List<FdevImplementUnit> listFIU = implementUnitService.queryByDemandId(demandId);
        if (CommonUtils.isNullOrEmpty(listFIU)) {
            logger.info("---this demand is null---");
            return;
        }
        if (field.equalsIgnoreCase(Dict.IMPLEMENT_UNIT_STATUS_NORMAL)) {
            for (FdevImplementUnit fdevImplementUnit : listFIU) {
                fdevImplementUnit.setImplement_unit_status_normal(status);
                implementUnitDao.update(fdevImplementUnit);

            }
        } else if (field.equalsIgnoreCase(Dict.IMPLEMENT_UNIT_STATUS_SPECIAL)) {
            for (FdevImplementUnit fdevImplementUnit : listFIU) {
                fdevImplementUnit.setImplement_unit_status_special(status);
                implementUnitDao.update(fdevImplementUnit);
            }
        } else {
            logger.info("---param is not normal or special---");
        }

    }

    @Override
    public void updateUnitDate(String demandId, String date, String field) throws Exception {
        //根据需求id查询所有的实施单元
        List<FdevImplementUnit> listFIU = implementUnitService.queryByDemandId(demandId);
        if (CommonUtils.isNullOrEmpty(listFIU)) {
            logger.info("---this demand is null---");
            return;
        }
        switch (field) {
            case Dict.DEFER_TIME:
                for (FdevImplementUnit fdevImplementUnit : listFIU) {
                    fdevImplementUnit.setDefer_time(date);
                    implementUnitDao.update(fdevImplementUnit);
                }
                break;
            case Dict.RECOVER_TIME:
                for (FdevImplementUnit fdevImplementUnit : listFIU) {
                    fdevImplementUnit.setRecover_time(date);
                    implementUnitDao.update(fdevImplementUnit);
                }
                break;
            case Dict.DELETE_TIME:
                for (FdevImplementUnit fdevImplementUnit : listFIU) {
                    fdevImplementUnit.setDelete_time(date);
                    implementUnitDao.update(fdevImplementUnit);
                }
                break;
            default:
                break;
        }

    }

    @Override
    public DemandBaseInfo updateDemandDateByImp(String demandId, Integer demandStatusNormal, String planStartDate, String planInnerTestDate, String planTestDate,
                                                String planTestFinishDate, String planProductDate, Integer demandStatusSpecial) throws Exception {
        DemandBaseInfo demandBaseInfo = demandBaseInfoDao.queryById(demandId);
        if (CommonUtils.isNullOrEmpty(demandBaseInfo)) {
            throw new FdevException(ErrorConstants.DEMAND_NULL_ERROR, new String[]{demandId});
        }

        String planStartDateBefore = demandBaseInfo.getPlan_start_date();
        if (CommonUtils.isNullOrEmpty(planStartDateBefore)) {
            demandBaseInfo.setPlan_start_date(planStartDate);
        } else if (planStartDate.compareToIgnoreCase(planStartDateBefore) < 0) {
            //说明参数比之前的日期小
            demandBaseInfo.setPlan_start_date(planStartDate);
        }
        String planInnerTestDateBefore = demandBaseInfo.getPlan_inner_test_date();
        if (CommonUtils.isNullOrEmpty(planInnerTestDateBefore)) {
            demandBaseInfo.setPlan_inner_test_date(planInnerTestDate);
        } else if (planInnerTestDate.compareToIgnoreCase(planInnerTestDateBefore) > 0) {
            //说明参数比之前的日期大
            demandBaseInfo.setPlan_inner_test_date(planInnerTestDate);
        }
        String planTestDateBefore = demandBaseInfo.getPlan_test_date();
        if (CommonUtils.isNullOrEmpty(planTestDateBefore)) {
            demandBaseInfo.setPlan_test_date(planTestDate);
        } else if (planInnerTestDate.compareToIgnoreCase(planTestDateBefore) > 0) {
            //说明参数比之前的日期大
            demandBaseInfo.setPlan_test_date(planTestDate);
        }
        String planTestFinishDateBefore = demandBaseInfo.getPlan_test_finish_date();
        if (CommonUtils.isNullOrEmpty(planTestFinishDateBefore)) {
            demandBaseInfo.setPlan_test_finish_date(planTestFinishDate);
        } else if (planTestFinishDate.compareToIgnoreCase(planTestFinishDateBefore) > 0) {
            //说明参数比之前的日期大
            demandBaseInfo.setPlan_test_finish_date(planTestFinishDate);
        }
        String planProductDateBefore = demandBaseInfo.getPlan_product_date();
        if (CommonUtils.isNullOrEmpty(planProductDateBefore)) {
            demandBaseInfo.setPlan_product_date(planProductDate);
        } else if (planProductDate.compareToIgnoreCase(planProductDateBefore) > 0) {
            //说明参数比之前的日期大
            demandBaseInfo.setPlan_product_date(planProductDate);
        }
        if (!CommonUtils.isNullOrEmpty(demandStatusNormal)) {
            demandBaseInfo.setDemand_status_normal(demandStatusNormal);
        }
        if (!CommonUtils.isNullOrEmpty(demandStatusSpecial)) {
            demandBaseInfo.setDemand_status_special(demandStatusSpecial);
        }
        return demandBaseInfo;
    }

    @Override
    public void createYuhengOrder(String unitNo, String internalTestStart, String internalTestEnd,
                                  String expectedProductDate, String requireRemark, String group_id, String group_name) {
        //生成玉衡工单
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.UNIT_NO, unitNo);
        param.put(Dict.INTERNAL_TEST_START, internalTestStart);
        param.put(Dict.INTERNAL_TEST_END, internalTestEnd);
        param.put(Dict.EXPECTED_PRODUCT_DATE, expectedProductDate);
        param.put(Dict.REQUIRE_REMARK, requireRemark);
        param.put(Dict.GROUP_ID_YH, group_id);
        param.put(Dict.GROUP_NAME_YUHENG, group_name);
        try {
            logger.info("---kafka start---");
            ListenableFuture<SendResult<byte[], byte[]>> send = kafkaTemplate.send(topic, JSON.toJSONString(param).getBytes());
            logger.info("--jsonsend--" + JSON.toJSONString(send) + "----");
        } catch (Exception e) {
            try {
                logger.error("---create yuheng start---");
                param.put(Dict.REST_CODE, "createWorkOrder");
                Map<String, Object> map = (Map<String, Object>) restTransport.submit(param);
                String workOrderNoFail = (String) map.get("workOrderNo");
                logger.info("--yuheng order--" + workOrderNoFail + "---");
            } catch (Exception e1) {
                logger.error("---create yuheng order error---");
                logger.error("---fdev_no---" + unitNo + "---");
                logger.error(e.getMessage());
            }
        }
    }

    @Override
    public HashMap<String, String> dateByDemandIdMap(String demandId) {
        HashMap<String, String> dateMap = new HashMap<String, String>();
        List<FdevImplementUnit> listFdevImp = implementUnitService.queryByDemandId(demandId);
        String planStartDate = "";
        String planInnerDate = "";
        String planTestDate = "";
        String planTestFinishDate = "";
        String planProDate = "";
        String realStartDate = "";
        String realInnerDate = "";
        String realTestDate = "";
        String realTestFinishDate = "";
        String realProDate = "";
        if (!CommonUtils.isNullOrEmpty(listFdevImp)) {
            List<FdevImplementUnit> collect = listFdevImp.stream()
                    .sorted(Comparator.comparingInt(FdevImplementUnit::getImplement_unit_status_normal))
                    .collect(Collectors.toList());
            //获取所有实施单元最小阶段
            Integer minStage = collect.get(0).getImplement_unit_status_normal();
            dateMap.put(Dict.MINSTAGE, String.valueOf(minStage));
            planStartDate = listFdevImp.get(0).getPlan_start_date();
            planInnerDate = listFdevImp.get(0).getPlan_inner_test_date();
            planTestDate = listFdevImp.get(0).getPlan_test_date();
            planTestFinishDate = listFdevImp.get(0).getPlan_test_finish_date();
            planProDate = listFdevImp.get(0).getPlan_product_date();
            realStartDate = listFdevImp.get(0).getReal_start_date() == null ? "" : listFdevImp.get(0).getReal_start_date();
            realInnerDate = listFdevImp.get(0).getReal_inner_test_date() == null ? "" : listFdevImp.get(0).getReal_inner_test_date();
            realTestDate = listFdevImp.get(0).getReal_test_date() == null ? "" : listFdevImp.get(0).getReal_test_date();
            realTestFinishDate = listFdevImp.get(0).getReal_test_finish_date() == null ? "" : listFdevImp.get(0).getReal_test_finish_date();
            realProDate = listFdevImp.get(0).getReal_product_date() == null ? "" : listFdevImp.get(0).getReal_product_date();
            if (listFdevImp.size() > 1) {
                for (int i = 1; i < listFdevImp.size(); i++) {
                    FdevImplementUnit fdevImp = listFdevImp.get(i);
                    if (planStartDate.compareToIgnoreCase(fdevImp.getPlan_start_date()) > 0) {
                        planStartDate = fdevImp.getPlan_start_date();
                    }
                    if (planInnerDate.compareToIgnoreCase(fdevImp.getPlan_inner_test_date()) < 0) {
                        planInnerDate = fdevImp.getPlan_inner_test_date();
                    }
                    if (planTestDate.compareToIgnoreCase(fdevImp.getPlan_test_date()) < 0) {
                        planTestDate = fdevImp.getPlan_test_date();
                    }
                    if (planTestFinishDate.compareToIgnoreCase(fdevImp.getPlan_test_finish_date()) < 0) {
                        planTestFinishDate = fdevImp.getPlan_test_finish_date();
                    }
                    if (planProDate.compareToIgnoreCase(fdevImp.getPlan_product_date()) < 0) {
                        planProDate = fdevImp.getPlan_product_date();
                    }
                    //实际启动日期
                    if (!CommonUtils.isNullOrEmpty(fdevImp.getReal_start_date())) {
                        if (CommonUtils.isNullOrEmpty(realStartDate)) {
                            realStartDate = fdevImp.getReal_start_date();
                        } else if (realStartDate.compareToIgnoreCase(fdevImp.getReal_start_date()) > 0) {
                            realStartDate = fdevImp.getReal_start_date();
                        }
                    }
                    //实际内测日期
                    if (!CommonUtils.isNullOrEmpty(fdevImp.getReal_inner_test_date())) {
                        if (CommonUtils.isNullOrEmpty(realInnerDate)) {
                            realInnerDate = fdevImp.getReal_inner_test_date();
                        } else if (realInnerDate.compareToIgnoreCase(fdevImp.getReal_inner_test_date()) < 0) {
                            realInnerDate = fdevImp.getReal_inner_test_date();
                        }
                    }
                    //实际业测日期
                    if (!CommonUtils.isNullOrEmpty(fdevImp.getReal_test_date())) {
                        if (CommonUtils.isNullOrEmpty(realTestDate)) {
                            realTestDate = fdevImp.getReal_test_date();
                        } else if (realTestDate.compareToIgnoreCase(fdevImp.getReal_test_date()) < 0) {
                            realTestDate = fdevImp.getReal_test_date();
                        }
                    }
                    //实际业测完成日期
                    if (!CommonUtils.isNullOrEmpty(fdevImp.getReal_test_finish_date())) {
                        if (CommonUtils.isNullOrEmpty(realTestFinishDate)) {
                            realTestFinishDate = fdevImp.getReal_test_finish_date();
                        } else if (realTestFinishDate.compareToIgnoreCase(fdevImp.getReal_test_finish_date()) < 0) {
                            realTestFinishDate = fdevImp.getReal_test_finish_date();
                        }
                    }
                    //实际投产日期
                    if (!CommonUtils.isNullOrEmpty(fdevImp.getReal_product_date())) {
                        if (CommonUtils.isNullOrEmpty(realProDate)) {
                            realProDate = fdevImp.getReal_product_date();
                        } else if (realProDate.compareToIgnoreCase(fdevImp.getReal_product_date()) < 0) {
                            realProDate = fdevImp.getReal_product_date();
                        }
                    }
                }
            }
        }
        dateMap.put(Dict.PLAN_START_DATE, planStartDate);
        dateMap.put(Dict.PLAN_INNER_TEST_DATE, planInnerDate);
        dateMap.put(Dict.PLAN_TEST_DATE, planTestDate);
        dateMap.put(Dict.PLAN_TEST_FINISH_DATE, planTestFinishDate);
        dateMap.put(Dict.PLAN_PRODUCT_DATE, planProDate);
        dateMap.put(Dict.REAL_START_DATE, realStartDate);
        dateMap.put(Dict.REAL_INNER_TEST_DATE, realInnerDate);
        dateMap.put(Dict.REAL_TEST_DATE, realTestDate);
        dateMap.put(Dict.REAL_TEST_FINISH_DATE, realTestFinishDate);
        dateMap.put(Dict.REAL_PRODUCT_DATE, realProDate);
        return dateMap;
    }

    /**
     * 需求废弃时同步删除玉衡工单
     *
     * @param demandId
     */
    @Override
    public void deleteOrder(String demandId) {
        List<FdevImplementUnit> unitList = implementUnitDao.queryByDemandId(demandId);
        List<String> units = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(unitList)) {
            unitList.stream().forEach(x -> units.add(x.getFdev_implement_unit_no()));
            Map<String, Object> param = new HashMap<>();
            param.put(Dict.FDEV_IMPLEMENT_UNIT_NO, units);
            param.put(Dict.REST_CODE, "deleteOrder");
            try {
                restTransport.submit(param);
            } catch (Exception e) {
                logger.error("删除玉衡工单失败，需求id为{},失败原因如{}", demandId, e.getMessage());
            }
            logger.info("需求{}废弃删除玉衡工单成功", demandId);
        }
        logger.info("当前需求{}下无实施单元", demandId);
    }

    @Override
    public void updateYuhengPlanDate(String unitNo, String internalTestStart, String internalTestEnd, String expectedProductDate) {
        //生成玉衡工单
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.UNIT_NO, unitNo);
        param.put(Dict.INTERNAL_TEST_START, internalTestStart);
        param.put(Dict.INTERNAL_TEST_END, internalTestEnd);
        param.put(Dict.EXPECTED_PRODUCT_DATE, expectedProductDate);
        param.put(Dict.REST_CODE, "updateYuhengPlanDate");
        try {
            restTransport.submit(param);
            logger.info("---update yuhengDate success---");
        } catch (Exception e) {
            logger.error("---fdev_no---" + unitNo + "---");
            logger.error("---update yuhengDate error --" + e.getMessage());
        }
    }

    @Override
    public Map<String,Object> testPlanQuery( String taskId ) {
        Map<String,Object> map = new HashMap<>();
        //查询任务测试状态
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.TASKNO, taskId);
        param.put(Dict.REST_CODE, "testPlanQuery");
        try {
            map = (Map<String, Object>) restTransport.submit(param);
        } catch (Exception e) {
            logger.error( "---testPlanQuery error ---" + taskId + "---");
        }
        return map;
    }

    @Override
    public Map<String,Object> exportSitReportData( String workNo ) {
        Map<String,Object> map = new HashMap<>();
        //查询任务测试状态
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.WORKNO, workNo);
        param.put(Dict.REST_CODE, "exportSitReportData");
        try {
            map = (Map<String, Object>) restTransport.submit(param);
        } catch (Exception e) {
            logger.error( "---exportSitReportData error ---" + workNo + "---");
        }
        return map;
    }

    @Override
    public Map<String,Object> queryOrderByOrderNo( String workNo ) {
        Map<String,Object> map = new HashMap<>();
        //查询任务测试状态
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.WORKNO, workNo);
        param.put(Dict.REST_CODE, "queryOrderByOrderNo");
        try {
            map = (Map<String, Object>) restTransport.submit(param);
        } catch (Exception e) {
            logger.error( "---queryOrderByOrderNo error ---" + workNo + "---");
        }
        return map;
    }

    @Override
    public String queryWorkNoByTaskId( String taskId ) {
        String workNo = "";
        //查询工单编号
        Map<String, String> param = new HashMap<String, String>();
        param.put("task_id", taskId);
        param.put(Dict.REST_CODE, "queryWorkNoByTaskId");
        try {
            workNo = (String) restTransport.submit(param);
        } catch (Exception e) {
            logger.error( "---queryWorkNoByTaskId error ---" + taskId + "---");
        }
        return workNo;
    }

    /**
     * 需求废弃时同步删除玉衡工单
     *
     */
    @Override
    public void deleteImpl(String fdevId) {
        List<String> units = new ArrayList<>();
        units.add(fdevId);
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.FDEV_IMPLEMENT_UNIT_NO, units);
        try {
            logger.info("---kafka start---");
            ListenableFuture<SendResult<byte[], byte[]>> send = kafkaTemplate.send(topicDel, JSON.toJSONString(param).getBytes());
            logger.info("--jsonsend--" + JSON.toJSONString(send) + "----");
        } catch (Exception e) {
            try {
                logger.error("---delete yuheng start---");
                param.put(Dict.REST_CODE, "deleteOrder");
                Map<String, Object> map = (Map<String, Object>) restTransport.submit(param);
            } catch (Exception e1) {
                logger.error("---create yuheng order error---");
                logger.error("---fdev_no---" + fdevId + "---");
                logger.error(e.getMessage());
            }
        }
    }

    @Override
    public HashMap<String, String> dateByDemandIdMapAssessOver(String demandId) {
        HashMap<String, String> dateMap = new HashMap<String, String>();
        List<FdevImplementUnit> listFdevImp = implementUnitService.queryImplPreImplmentByDemandId(demandId);
        String planStartDate = "";
        String planInnerDate = "";
        String planTestDate = "";
        String planTestFinishDate = "";
        String planProDate = "";
//        String realStartDate = "";
//        String realInnerDate = "";
//        String realTestDate = "";
//        String realTestFinishDate = "";
//        String realProDate = "";
        if (!CommonUtils.isNullOrEmpty(listFdevImp)) {
            List<FdevImplementUnit> collect = listFdevImp.stream()
                    .sorted(Comparator.comparingInt(FdevImplementUnit::getImplement_unit_status_normal))
                    .collect(Collectors.toList());
            //获取所有研发单元最小阶段
            Integer minStage = collect.get(0).getImplement_unit_status_normal();
            dateMap.put(Dict.MINSTAGE, String.valueOf(minStage));
            //日常需求只有开始 结束日期
            if (Dict.DAILY.equals(listFdevImp.get(0).getDemand_type())) {
                planStartDate = listFdevImp.get(0).getPlan_start_date();
                planProDate = listFdevImp.get(0).getPlan_product_date();
                if (listFdevImp.size() > 1) {
                    for (int i = 1; i < listFdevImp.size(); i++) {
                        FdevImplementUnit fdevImp = listFdevImp.get(i);
                        if (planStartDate.compareToIgnoreCase(fdevImp.getPlan_start_date()) > 0) {
                            planStartDate = fdevImp.getPlan_start_date();
                        }
                        if (!CommonUtils.isNullOrEmpty(fdevImp.getPlan_product_date()) && !CommonUtils.isNullOrEmpty(planProDate) && planProDate.compareToIgnoreCase(fdevImp.getPlan_product_date()) < 0) {
                            planProDate = fdevImp.getPlan_product_date();
                        } else if (!CommonUtils.isNullOrEmpty(fdevImp.getPlan_product_date()) && CommonUtils.isNullOrEmpty(planProDate)) {
                            planProDate = fdevImp.getPlan_product_date();
                        }
                    }
                }
            } else {
                planStartDate = listFdevImp.get(0).getPlan_start_date();
                planInnerDate = listFdevImp.get(0).getPlan_inner_test_date();
                planTestDate = listFdevImp.get(0).getPlan_test_date();
                planTestFinishDate = listFdevImp.get(0).getPlan_test_finish_date();
                planProDate = listFdevImp.get(0).getPlan_product_date();
                //            realStartDate = listFdevImp.get(0).getReal_start_date() == null ? "":listFdevImp.get(0).getReal_start_date();
                //            realInnerDate = listFdevImp.get(0).getReal_inner_test_date()== null ? "":listFdevImp.get(0).getReal_inner_test_date();
                //            realTestDate = listFdevImp.get(0).getReal_test_date()== null ? "":listFdevImp.get(0).getReal_test_date();
                //            realTestFinishDate = listFdevImp.get(0).getReal_test_finish_date()== null ? "":listFdevImp.get(0).getReal_test_finish_date();
                //            realProDate = listFdevImp.get(0).getReal_product_date()== null ? "":listFdevImp.get(0).getReal_product_date();
                if (listFdevImp.size() > 1) {
                    for (int i = 1; i < listFdevImp.size(); i++) {
                        FdevImplementUnit fdevImp = listFdevImp.get(i);
                        if (planStartDate.compareToIgnoreCase(fdevImp.getPlan_start_date()) > 0) {
                            planStartDate = fdevImp.getPlan_start_date();
                        }
                        if (planInnerDate.compareToIgnoreCase(fdevImp.getPlan_inner_test_date()) < 0) {
                            planInnerDate = fdevImp.getPlan_inner_test_date();
                        }
                        if (planTestDate.compareToIgnoreCase(fdevImp.getPlan_test_date()) < 0) {
                            planTestDate = fdevImp.getPlan_test_date();
                        }
                        if (!CommonUtils.isNullOrEmpty(fdevImp.getPlan_test_finish_date()) && !CommonUtils.isNullOrEmpty(planTestFinishDate) && planTestFinishDate.compareToIgnoreCase(fdevImp.getPlan_test_finish_date()) < 0) {
                            planTestFinishDate = fdevImp.getPlan_test_finish_date();
                        } else if (!CommonUtils.isNullOrEmpty(fdevImp.getPlan_test_finish_date()) && CommonUtils.isNullOrEmpty(planTestFinishDate)) {
                            planTestFinishDate = fdevImp.getPlan_test_finish_date();
                        }
                        if (!CommonUtils.isNullOrEmpty(fdevImp.getPlan_product_date()) && !CommonUtils.isNullOrEmpty(planProDate) && planProDate.compareToIgnoreCase(fdevImp.getPlan_product_date()) < 0) {
                            planProDate = fdevImp.getPlan_product_date();
                        } else if (!CommonUtils.isNullOrEmpty(fdevImp.getPlan_product_date()) && CommonUtils.isNullOrEmpty(planProDate)) {
                            planProDate = fdevImp.getPlan_product_date();
                        }
                        //                    //实际启动日期
                        //                    if(!CommonUtils.isNullOrEmpty(fdevImp.getReal_start_date())){
                        //                        if(CommonUtils.isNullOrEmpty(realStartDate)){
                        //                            realStartDate = fdevImp.getReal_start_date();
                        //                        }else if(realStartDate.compareToIgnoreCase(fdevImp.getReal_start_date()) > 0){
                        //                            realStartDate = fdevImp.getReal_start_date();
                        //                        }
                        //                    }
                        //                    //实际内测日期
                        //                    if(!CommonUtils.isNullOrEmpty(fdevImp.getReal_inner_test_date())){
                        //                        if(CommonUtils.isNullOrEmpty(realInnerDate)){
                        //                            realInnerDate = fdevImp.getReal_inner_test_date();
                        //                        }else if(realInnerDate.compareToIgnoreCase(fdevImp.getReal_inner_test_date()) < 0){
                        //                            realInnerDate = fdevImp.getReal_inner_test_date();
                        //                        }
                        //                    }
                        //                    //实际业测日期
                        //                    if(!CommonUtils.isNullOrEmpty(fdevImp.getReal_test_date())){
                        //                        if(CommonUtils.isNullOrEmpty(realTestDate)){
                        //                            realTestDate = fdevImp.getReal_test_date();
                        //                        }else if(realTestDate.compareToIgnoreCase(fdevImp.getReal_test_date()) < 0){
                        //                            realTestDate = fdevImp.getReal_test_date();
                        //                        }
                        //                    }
                        //                    //实际业测完成日期
                        //                    if(!CommonUtils.isNullOrEmpty(fdevImp.getReal_test_finish_date())){
                        //                        if(CommonUtils.isNullOrEmpty(realTestFinishDate)){
                        //                            realTestFinishDate = fdevImp.getReal_test_finish_date();
                        //                        }else if(realTestFinishDate.compareToIgnoreCase(fdevImp.getReal_test_finish_date()) < 0){
                        //                            realTestFinishDate = fdevImp.getReal_test_finish_date();
                        //                        }
                        //                    }
                        //                    //实际投产日期
                        //                    if(!CommonUtils.isNullOrEmpty(fdevImp.getReal_product_date())){
                        //                        if(CommonUtils.isNullOrEmpty(realProDate)){
                        //                            realProDate = fdevImp.getReal_product_date();
                        //                        }else if(realProDate.compareToIgnoreCase(fdevImp.getReal_product_date()) < 0){
                        //                            realProDate = fdevImp.getReal_product_date();
                        //                        }
                        //                    }
                    }
                }
            }
        }

        dateMap.put(Dict.PLAN_START_DATE, planStartDate);
        dateMap.put(Dict.PLAN_INNER_TEST_DATE, planInnerDate);
        dateMap.put(Dict.PLAN_TEST_DATE, planTestDate);
        dateMap.put(Dict.PLAN_TEST_FINISH_DATE, planTestFinishDate);
        dateMap.put(Dict.PLAN_PRODUCT_DATE, planProDate);
//        dateMap.put(Dict.REAL_START_DATE, realStartDate);
//        dateMap.put(Dict.REAL_INNER_TEST_DATE, realInnerDate);
//        dateMap.put(Dict.REAL_TEST_DATE, realTestDate);
//        dateMap.put(Dict.REAL_TEST_FINISH_DATE, realTestFinishDate);
//        dateMap.put(Dict.REAL_PRODUCT_DATE, realProDate);
        return dateMap;
    }

    @Override
    public List<Map> queryInnerTestData(String demandNo) {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.DEMAND_NO, demandNo);
        param.put(Dict.REST_CODE, "queryInnerTestData");
        List<Map> result = new ArrayList<>();
        try {
            result = (List<Map>) restTransport.submit(param);
        } catch (Exception e) {
            logger.info(">>>queryInnerTestData fail,{}", demandNo);
        }
        return result;
    }
}
