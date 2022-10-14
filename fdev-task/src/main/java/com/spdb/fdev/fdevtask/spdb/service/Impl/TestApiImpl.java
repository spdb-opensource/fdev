package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTaskCollection;
import com.spdb.fdev.fdevtask.spdb.service.ITestApi;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;
import com.spdb.fdev.fdevtask.spdb.service.RequirementApi;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class TestApiImpl implements ITestApi {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

    @Autowired
    private RestTransport restTransport;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private FdevTaskServiceImpl fdevTaskService;

    @Autowired
    private IUserApi iUserApi;

    @Autowired
    private RequirementApi requirementApi;

    @Override
    public Map createTest(Map param) throws Exception {
        //测试管理平台
        Map map = new HashMap();
        try {
            param.put(Dict.REST_CODE, "addRequireImplementNo");
            map = (Map) restTransport.submit(param);
        } catch (Exception e) {
          //  throw new FdevException(ErrorConstants.TEST_SERVER_ERROR, new String[]{e.getMessage()});logger.info(e.getMessage());
        }
        return map;
    }

    @Override
    public Map queryTest(Map param) throws Exception {
        Map mapPara = new HashMap();
        try {
            param.put(Dict.REST_CODE, "testPlanQuery");
            mapPara = (Map) restTransport.submit(param);
            //{"1":"未开始","2":"rel","3":"sit","4":"uat","5":"延期","6":"完成"}
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.TEST_SERVER_ERROR, new String[]{e.getMessage()});
        }
        if (mapPara == null){
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"该任务对应的研发单元没有成功创建出工单"});
        }
        return mapPara;
    }

    //测试平台交互通知交易
    @Override
    public Object interactTest(Map param) throws Exception {
        //通知测试管理平台
        Object result = null;
        try {
            param.put(Dict.REST_CODE, "informTest");
            result = restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.TEST_SERVER_ERROR, new String[]{e.getMessage()});
        }
        return result;
    }


    @Override
    public Object updateOrder(Map param) throws Exception {
        Object result = null;
        try {
            param.put(Dict.REST_CODE, "updateOrder");
            result = restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.TEST_SERVER_ERROR, new String[]{e.getMessage()});
        }
        return result;
    }

    @Override
    public Map deleteOrder(Map param) throws Exception {
        //子任务编号或者jobNo必填其中一个，其他的选填
        // "taskNoOnly" : "cc",
        // "jobNo" : "",
        // "taskNo" : "",
        // "taskName" : "",
        // "internalTestStart" : "",
        // "internalTestEnd" : "",
        // "requireRemark" : ""
        Map map = new HashMap();
        String message = "";
        try {
            param.put(Dict.REST_CODE, "deleteOrder");
            message = (String) restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.APP_THIRD_SERVER_ERROR, new String[]{e.getMessage()});
        }
        map.put(Dict.STATUS, message);
        return map;
    }

    @Override
    public Map generateRequestParam(String taskId, Map changedDate) throws Exception {
        Map result = new HashMap();
        List list = new ArrayList();
        list.add(taskId);
        List tclist = fdevTaskService.queryBySubTask(new FdevTaskCollection("", "", list));
        if (!CommonUtils.isNullOrEmpty(tclist) && tclist.size() == 1) {
            String workno = (String) ((Map) tclist.get(0)).get(Dict.ID);
            result.put("workOrderNo", workno);
            for (Object o : changedDate.keySet()) {
                switch ((String) o) {
                    case Dict.PLAN_INNER_TEST_TIME:
                        result.put("planSitDate", ((String) changedDate.get(o)).replaceAll("/", "-"));
                        break;
                    case Dict.PLAN_UAT_TEST_START_TIME:
                        result.put("planUatDate", ((String) changedDate.get(o)).replaceAll("/", "-"));
                        break;
                    case Dict.PLAN_FIRE_TIME:
                        result.put("planProDate", ((String) changedDate.get(o)).replaceAll("/", "-"));
                        break;
                    default:
                        break;
                }
            }
        }
        return result;
    }

    /**
     * @param task
     * @return jobId
     * @throws FdevException
     * @deprecated 内测任务发送测试平台接口
     */
    @Override
    public String getJobId(FdevTask task, String jobid) throws Exception {
        String jobId = null;
        try {
            jobId = "";
            // 任务id,新增测试管理平台
            StringBuffer sb_en = new StringBuffer();
            StringBuffer sb_ch = new StringBuffer();
            Map testParam = new HashMap();
            Map userParam = new HashMap();
            for (String s : CommonUtils.isNullOrEmpty(task.getDeveloper()) ? new String[]{} : task.getDeveloper()) {
                userParam.put(Dict.ID, s);
                Map map = iUserApi.queryUser(userParam);
                //          sb_en.append(map.get(Dict.USER_NAME_EN) + "、");
                sb_ch.append(map.get(Dict.USER_NAME_CN) + "、");
            }
            testParam.put(Dict.PROGRAMMER, sb_ch.toString()); // 开发人员
            sb_en.delete(0, sb_en.length());
            sb_ch.delete(0, sb_ch.length());
            for (String s : CommonUtils.isNullOrEmpty(task.getTester()) ? new String[]{} : task.getTester()) {
                userParam.put(Dict.ID, s);
                Map map = iUserApi.queryUser(userParam);
                sb_en.append(map.get(Dict.USER_NAME_EN) + "、");
                sb_ch.append(map.get(Dict.USER_NAME_CN) + "、");
            }
            testParam.put(Dict.INTERNALTESTER, sb_en.toString() + ";" + sb_ch.toString()); // 测试人员
            sb_en.delete(0, sb_en.length());
            sb_ch.delete(0, sb_ch.length());
            for (String s : CommonUtils.isNullOrEmpty(task.getSpdb_master()) ? new String[]{} : task.getSpdb_master()) {
                userParam.put(Dict.ID, s);
                Map map = iUserApi.queryUser(userParam);
                sb_en.append(map.get(Dict.USER_NAME_EN) + "、");
                sb_ch.append(map.get(Dict.USER_NAME_CN) + "、");
            }
            testParam.put(Dict.UNITNO, task.getFdev_implement_unit_no()); // 研发单元编号
            testParam.put(Dict.SPDBMANAGER, sb_ch.toString()); // 行方人员
            testParam.put(Dict.RSRVFLD1, task.getProject_name()); // 任务所属应用
            testParam.put(Dict.BUSINESSTESTER, null); // 业务老师
            testParam.put(Dict.JOBNO, jobid); // 工单编号，主任务不用传
            testParam.put(Dict.TASKNO, task.getId()); // 任务编号
            testParam.put(Dict.TASKNAME, task.getName()); // 任务名称
            testParam.put(Dict.INTERNALTESTSTART, task.getPlan_inner_test_time()); // 测试开始时间
            testParam.put(Dict.INTERNALTESTEND, task.getPlan_uat_test_start_time()); // uat 开始就是sit测试结束时间
            testParam.put(Dict.EXPECTEDFINISHDATE, null); // 预计完成时间
            testParam.put(Dict.EXPECTEDPRODUCTDATE, task.getPlan_fire_time()); // 预计投产时间
            testParam.put(Dict.BUSINESSTESTSTART, null); // 行方提测时间
            testParam.put(Dict.BUSINESSTESTEND, task.getPlan_uat_test_stop_time()); // 要求业测完成时间
            testParam.put(Dict.REQUIREREMARK, task.getDesc()); // 需求备注
            testParam.put(Dict.GROUP_ID, task.getGroup());
            Map param = new HashMap();
            param.put(Dict.ID, task.getGroup());
            Map rGroup = (Map) iUserApi.queryGroup(param);
            if (CommonUtils.isNullOrEmpty(rGroup)) {
                logger.error("查询组信息失败");
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"查询组信息失败!"});
            }
            testParam.put(Dict.GROUP_NAME, rGroup.get(Dict.NAME));
            Map result = createTest(testParam);
            jobId = (String) result.get("workOrderNo");
        } catch (Exception e) {
            FdevException fdevException = new FdevException(ErrorConstants.TEST_SERVER_ERROR2, new String[]{});
            fdevException.setMessage("关联主子任务失败");
            throw fdevException;
        }
        return jobId;
    }

    @Override
    public String queryFdevSitMsg(String taskId) {
        Map params = new HashMap();
        params.put(Dict.ID, taskId);
        params.put(Dict.REST_CODE, "queryFdevSitMsg");
        Map map = new HashMap();
        String taskDesc = "";
        try {
            map = (Map) restTransport.submit(params);
        } catch (Exception e) {
            logger.error("调用玉衡测试平台查提测描述信息失败");
        }
        if (null != map) {
            taskDesc = (String) map.get("taskDesc");
        }
        return taskDesc;
    }

    @Override
    public void updateUnitNo(String taskNo, String unitNo) {
        Map params = new HashMap();
        params.put("taskNo", taskNo);
        params.put("unitNo", unitNo);
        params.put(Dict.REST_CODE, "updateUnitNo");
        try {
            restTransport.submit(params);
        } catch (Exception e) {
            logger.error("调用玉衡测试平台查提测描述信息失败");
        }

    }

    @Override
    public List queryWorkNoByUnitNo(Map params) throws Exception {
        params.put(Dict.REST_CODE, "queryWorkNoByUnitNo");
        List result = new ArrayList();
        try {
            result = (List) ((Map) restTransport.submit(params)).get("workNos");
        } catch (Exception e) {
            logger.error("调用玉衡测试平台查询失败");
        }
        return result;
    }

    @Override
    public void createYuhengOrder(String unitNo, String internalTestStart, String internalTestEnd,
                                  String expectedProductDate, String requireRemark, String group_id, String group_name) {
        //生成玉衡工单
        Map<String, String> param = new HashMap<String, String>();
        param.put(Dict.UNIT_NO, unitNo);
        param.put(Dict.INTERNAL_TEST_START,internalTestStart);
        param.put(Dict.INTERNAL_TEST_END, internalTestEnd);
        param.put(Dict.EXPECTED_PRODUCT_DATE,expectedProductDate);
        param.put(Dict.REQUIRE_REMARK, requireRemark);
        param.put(Dict.GROUP_ID_YH,group_id);
        param.put(Dict.GROUP_NAME_YUHENG, group_name);
        param.put(Dict.REST_CODE, "createWorkOrder");
        String workOrderNo = null;
        try {
            Map<String, Object> map = (Map<String, Object>) restTransport.submit(param);
            workOrderNo = (String) map.get("workOrderNo");
        }catch(Exception e){
            logger.error("---create yuheng order error---");
        }
        logger.info("--create yuheng order--" + workOrderNo+ "---");

    }

    @Override
    public void deleteTaskIssue(String id) {
        Map<String, String> param = new HashMap<>();
        param.put("task_id",id);
        param.put(Dict.REST_CODE, "deleteTaskIssue");
        try {
            restTransport.submit(param);
        } catch (Exception e) {
            logger.info("调用玉衡测试平台删除任务缺陷失败！任务id:" + id);
        }
    }

    @Override
    public void createSecurityOrder(String taskId, String taskName, String unitNo, String correlationSystem,
                                    String correlationInterface, String interfaceFilePath, String transFilePath,
                                    String remark, String appName, String developers, String group, List<Map> transList) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.TASKNO,taskId);
        param.put(Dict.TASKNAME,taskName);
        param.put(Dict.UNITNO,unitNo);
        param.put(Dict.REMARK,remark);
        param.put(Dict.CORRELATIONSYSTEM,correlationSystem);
        param.put(Dict.CORRELATIONINTERFACE,correlationInterface);
        param.put(Dict.INTERFACEFILEPATH,interfaceFilePath);
        param.put(Dict.TRANSFILEPATH,transFilePath);
        param.put(Dict.APPNAME,appName);
        param.put(Dict.DEVELOPER,developers);
        param.put(Dict.GROUP,group);
        param.put(Dict.TRANSLIST,transList);
        param.put(Dict.REST_CODE, "createSecurityWorkOrder");
        restTransport.submit(param);
    }

}
