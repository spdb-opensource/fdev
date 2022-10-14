package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.spdb.entity.AppDeployReview;
import com.spdb.fdev.fdevtask.spdb.service.IReleaseTaskApi;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RefreshScope
public class ReleaseTaskApiImpl implements IReleaseTaskApi {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

    @Autowired
    private RestTransport restTransport;


    @Override
    public Map deleteTask(String taskId) throws Exception {
        // 根据任务id查询投产点详情 taskId

        Map param = new HashMap();
        param.put(Dict.TASK_ID, taskId);
        param.put(Dict.REST_CODE, "deleteSitTask");
        Object result = restTransport.submit( param);
        return (Map) result;
    }


    @Override
    public Map queryDetailByTaskId(Map param) throws Exception {
        // 根据任务id查询投产点详情 taskId
        param.put(Dict.REST_CODE, "queryDetailByTaskId");
        Object result = restTransport.submit( param);
        return (Map) result;
    }

    @Override
    public Map queryDetailByTaskId(String taskId) throws Exception {
        // 根据任务id查询投产点详情 taskId
        Map param = new HashMap();
        param.put("task_id",taskId);
        param.put(Dict.REST_CODE, "queryDetailByTaskId");
        Object result = restTransport.submit( param);
        return (Map) result;
    }

    @Override
    public Map queryTaskByInterval(String groupId, String startDate, String endDate) throws Exception {
        // 根据任务id查询投产点详情 taskId

        Map param = new HashMap();
        param.put(Dict.OWNER_GROUPID, groupId);
        param.put(Dict.START_DATE, startDate);
        param.put(Dict.END_DATE, endDate);
        param.put(Dict.REST_CODE, "queryTaskByInterval");
        Object result = restTransport.submit(param);
        return (Map) result;
    }

    @Override
    public Map queryDetailByTaskId(String taskId, String type) throws Exception {
        Map param = new HashMap();
        param.put(Dict.TASK_ID, taskId);
        param.put(Dict.TYPE,type);
        param.put(Dict.REST_CODE, "queryDetailByTaskId");
        Object result = restTransport.submit( param);
        return (Map) result;
    }

    @Override
    public Map addRqrmntInfo(Map<String, Object> params) throws Exception {
        Map result = null;
        try {
            params.put(Dict.REST_CODE, "addRqrmntInfo");
            result = (Map)restTransport.submit( params);
        }catch (FdevException e){
            logger.error("调用投产模块addRqrmntInfo失败");
          // throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,new String[]{"添加需求信息失败！！"});
        }
        return  result;
    }

    @Override
    @Async
    public Map updateRqrmntInfoReview(Map<String,Object> params) {
        Map result = null;
        try {
            params.put(Dict.REST_CODE, "updateRqrmntInfoReview");
            result = (Map)restTransport.submit( params);
        } catch (Exception e) {
            logger.error("调用投产模块updateRqrmntInfoReview失败");
            //throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,new String[]{"更新需求信息失败！！"});
        }
        return  result;
    }

    @Override
    public Map deleteRqrmntInfoTask(Map<String, Object> params) {
        Map result = null;
        try {
            params.put(Dict.REST_CODE, "deleteRqrmntInfoTask");
            result = (Map)restTransport.submit( params);
        } catch (Exception e) {
            logger.error("调用投产模块deleteRqrmntInfoTask失败");
            //throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA,new String[]{"更新需求信息失败！！"});
        }
        return  result;
    }

    @Override
    public void dbReviewUpload(String id) {
        logger.info("调用投产模块接口{}",id);
        Map param = new HashMap();
        param.put("id",id);
        param.put(Dict.REST_CODE, "dbReviewUpload");
        try {
            restTransport.submit( param);
        } catch (Exception e) {
            logger.error("调用投产模块dbReviewUpload失败");
        }
    }

    @Override
    public Map queryRqrmntInfoByTaskNo(String id) {
        Map result = null;
        logger.info("调用投产模块接口{}",id);
        Map param = new HashMap();
        param.put("task_id",id);
        param.put(Dict.REST_CODE, "queryRqrmntInfoByTaskNo");
        try {
            result = (Map) restTransport.submit( param);
        } catch (Exception e) {
            logger.error("调用投产模块queryRqrmntInfoByTaskNo失败");
        }
        return result;
    }

    @Async
    @Override
    public void confirmRqrmntInfoTag(String confirmFileDate, String task_id ) {
        logger.info("调用投产模块接口{},{}",confirmFileDate,task_id);
        Map param = new HashMap();
        param.put("confirmFileDate",confirmFileDate);
        param.put("task_id",task_id);
        param.put(Dict.REST_CODE, "confirmRqrmntInfoTag");
        try {
            restTransport.submit( param);
        } catch (Exception e) {
            logger.error("调用投产模块confirmRqrmntInfoTag失败");
        }
    }


	@Override
	public void deploy(Map<String,Object> param) {
		logger.info("调用投产模块接口{},{}");
        param.put(Dict.REST_CODE, "deploy");
        try {
            restTransport.submit( param);
        } catch (Exception e) {
            logger.error("调用投产模块deploy失败");
        }
	}


}
