package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IReleaseRqrmntDao;
import com.spdb.fdev.release.entity.ReleaseRqrmnt;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.service.IReleaseRqrmntService;
import com.spdb.fdev.release.service.IReleaseTaskService;
import com.spdb.fdev.release.service.ITaskService;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.collections.MapUtils;
import org.bson.types.ObjectId;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

@EnableAsync
@Service
@RefreshScope
public class ReleaseRqrmntServiceimpl implements IReleaseRqrmntService {

    private static Logger logger = LoggerFactory.getLogger(ReleaseRqrmntServiceimpl.class);

    @Value("${task.file.resource}")
    private String taskFileResource;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private IReleaseRqrmntDao releaseRqrmntDao;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private IReleaseTaskService releaseTaskService;

    @Override
    public ReleaseRqrmnt queryByRqrmntId(String release_node_name, String rqrmnt_id, String type) {
        return releaseRqrmntDao.queryByRqrmntId(release_node_name, rqrmnt_id, type);
    }

    @Override
    public ReleaseRqrmnt editRqrmntFile(ReleaseRqrmnt releaseRqrmnt) {
        return releaseRqrmntDao.editRqrmntFile(releaseRqrmnt);
    }

    @Override
    public Map<String, Object> queryRqrmntsInfo(String rqrmnt_id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, rqrmnt_id);
        map.put(Dict.REST_CODE, "queryDemandInfoDetail");
        Map<String, Object> result = null;
        try {
            result = (Map<String, Object>) restTransport.submit(map);
        }catch (Exception e){
            logger.error("query queryRqrmntsInfo error,e:"+e);
        }
        return result;
    }

    @Override
    public String queryFdevImplUnitDetail(String fdevUnitId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("fdev_implement_unit_no", fdevUnitId);
        map.put(Dict.REST_CODE, "queryFdevImplUnitDetail");
        Map<String, Object> result = null;
        try {
            result = (Map<String, Object>) restTransport.submit(map);
        }catch (Exception e){
            logger.error("query queryFdevImplUnitDetail error,e:"+e);
        }
        String ipmpUnit = null;
        if(!CommonUtils.isNullOrEmpty(result)){
            ipmpUnit = (String) result.get("ipmp_implement_unit_no");
        }
        return ipmpUnit;
    }

    @Override
    public void saveReleaseRqrmnt(ReleaseRqrmnt releaseRqrmnt) {
        releaseRqrmntDao.saveReleaseRqrmnt(releaseRqrmnt);
    }

    @Override
    public void deleteById(String id) {
        releaseRqrmntDao.deleteById(id);
    }

    @Override
    public ReleaseRqrmnt deleteRqrmntTask(String task_id, String release_node_name) throws Exception {
        Map<String, Object> taskDetail = taskService.queryTaskDetail(task_id);
        String rqrmnt_id = (String) taskDetail.get(Dict.RQRMNT_NO);
        //查询需求类型
        Map<String, Object> rqrmnt_detail = this.queryRqrmntsInfo(rqrmnt_id);
        String rqrmnt_type = (String) rqrmnt_detail.get(Dict.DEMAND_TYPE);
        String type = "";
        if (Dict.BUSINESS.equals(rqrmnt_type)) {
            type = Constants.BUSINESS_RQRMNT;
        } else if (Dict.TECH.equals(rqrmnt_type)) {
            type = Constants.RELEASE_RQRMNT_TASK_FILE;
        }
        ReleaseRqrmnt releaseRqrmnt = releaseRqrmntDao.queryByRqrmntId(release_node_name, rqrmnt_id, type);
        if (!CommonUtils.isNullOrEmpty(releaseRqrmnt)) {
            Map<String, Object> task_map = releaseRqrmnt.getTask_map();
            if (!CommonUtils.isNullOrEmpty(task_map.get(task_id))) {
                if (task_map.keySet().size() == 1) {
                    releaseRqrmntDao.deleteById(releaseRqrmnt.getId());
                } else {
                    // 该需求下有不止一个任务,则只删除此任务
                    task_map.remove(task_id);
                    releaseRqrmnt.setTask_map(task_map);
                    releaseRqrmnt = releaseRqrmntDao.editTaskMap(releaseRqrmnt);
                }
            } else {
                logger.error("需求下未找到任务,需求id:{},任务id为:{}", rqrmnt_id, task_id);
            }
        } else {
            logger.error("{}投产窗口下未找到此需求,需求id:{},任务id为:{}", release_node_name, rqrmnt_id, task_id);
        }
        return releaseRqrmnt;
    }

    @Override
    public ReleaseRqrmnt addOrEditRqrmntTask(String task_id, String release_node_name) throws Exception {
        Map<String, Object> taskDetail = taskService.queryTaskDetail(task_id);
        if(CommonUtils.isNullOrEmpty(taskDetail)) {
            logger.error("投产任务不存在,id:{}", task_id);
            return null;
        }
        String rqrmnt_id = (String) taskDetail.get(Dict.RQRMNT_NO);
        Map<String, Object> rqrmnt_detail = this.queryRqrmntsInfo(rqrmnt_id);
        // 需求编号
        String rqrmnt_num = (String) rqrmnt_detail.get(Dict.OA_CONTACT_NO);
        // 需求名称
        String rqrmnt_name = (String) rqrmnt_detail.get(Dict.OA_CONTACT_NAME);
        // 需求类型
        String rqrmnt_type = (String) rqrmnt_detail.get(Dict.DEMAND_TYPE);
        String type = "";
        if (Dict.BUSINESS.equals(rqrmnt_type)) {
            type = Constants.BUSINESS_RQRMNT;
        } else if (Dict.TECH.equals(rqrmnt_type)) {
            type = Constants.RELEASE_RQRMNT_TASK_FILE;
        }
        // 查询此需求是否已经通过别的任务创建
        //加锁执行新增或者修改
        ReleaseRqrmnt releaseRqrmnt = null;
        RLock rLock = redissonClient.getLock(CommonUtils.generateRlockKey("file:" + release_node_name + ":" + rqrmnt_id));
        if (rLock.tryLock(15, -1L, TimeUnit.SECONDS)) {
            releaseRqrmnt = releaseRqrmntDao.queryByRqrmntId(release_node_name, rqrmnt_id,
                    type);
            if (!CommonUtils.isNullOrEmpty(releaseRqrmnt)) {
                Map<String, Object> task_map = releaseRqrmnt.getTask_map();
                handleTaskMap(taskDetail, rqrmnt_name, task_map, type);
                releaseRqrmnt.setTask_map(task_map);
                releaseRqrmntDao.editTaskMap(releaseRqrmnt);
            } else {
                Map<String, Object> task_map = new HashMap<>();
                handleTaskMap(taskDetail, rqrmnt_name, task_map, type);
                ObjectId objectId = new ObjectId();
                releaseRqrmnt = new ReleaseRqrmnt(objectId, objectId.toString(), release_node_name, rqrmnt_id,
                        rqrmnt_num, rqrmnt_name, type, task_map, CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
                releaseRqrmntDao.saveReleaseRqrmnt(releaseRqrmnt);
            }
        } else {
            logger.info("未拿到锁,投产窗口为{}，需求id为{}，需求编号为{},任务id为{}", release_node_name, rqrmnt_id, rqrmnt_num, task_id);
        }
        //释放锁
        rLock.unlock();
        return releaseRqrmnt;
    }

    @Override
    public List<ReleaseRqrmnt> queryByReleaseNodeName(String release_node_name) {
        return releaseRqrmntDao.queryByReleaseNodeName(release_node_name);
    }

    @Override
    public void updateNode(String old_release_node_name, String release_node_name) {
        releaseRqrmntDao.updateNode(old_release_node_name, release_node_name);
    }

    @Override
    public ReleaseRqrmnt buildRqrmnt(ReleaseRqrmnt releaseRqrmnt) {
        Set<String> taskIds = releaseRqrmnt.getTask_map().keySet();
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.IDS, taskIds);
        param.put(Dict.REST_CODE, "queryTaskDetailByIds");
        List<Map<String, Object>> taskList = new ArrayList<>();
        String rqrmnt_stage = null;
        List<String> operaterTimeList = new ArrayList<>();
        try {
            Map<String, Object> allTaskMap = (Map<String, Object>) restTransport.submit(param);
            for (String taskId : taskIds) {
                Map<String, Object> taskMap = (Map<String, Object>) allTaskMap.get(taskId);
                if (CommonUtils.isNullOrEmpty(taskMap) || "discard".equals(taskMap.get(Dict.STAGE))) {
                    continue;
                }
                String taskStage = (String) taskMap.get(Dict.STAGE);

                Map<String, Object> task = (Map<String, Object>) releaseRqrmnt.getTask_map().get(taskId);
                task.put(Dict.STAGE, taskStage.toUpperCase());
                //添加确认书到达时间和确认书确认人(仅限业务需求)
                        Map<String,String> confirmRecord = getConfirmRecord(taskId, Constants.CONFIRM_BTN_OPENED);
                if(!MapUtils.isEmpty(confirmRecord)){
                    String operaterName = confirmRecord.get(Dict.OPERATENAME);
                    String operaterTime = confirmRecord.get(Dict.OPERATETIME);
                    task.put(Dict.OPERATER_TIME,operaterTime);
                    task.put(Dict.OPERATER_NAME,operaterName);
                    if(Constants.BUSINESS_RQRMNT.equals(releaseRqrmnt.getType())){
                        if(!CommonUtils.isNullOrEmpty(operaterTime)){
                            operaterTimeList.add(operaterTime);
                        }
                    }
                }
                taskList.add(task);
                //获取需求中所有任务的最早阶段
                switch (taskStage) {
                    case Dict.SIT:
                        rqrmnt_stage = taskStage.toUpperCase();
                        break;
                    case Dict.UAT_LOWER:
                        rqrmnt_stage = Dict.SIT_UPCASE.equals(rqrmnt_stage) ? rqrmnt_stage : Dict.UAT_UPCASE;
                        break;
                    case Dict.REL_LOWERCASE:
                        rqrmnt_stage = Dict.SIT_UPCASE.equals(rqrmnt_stage) || Dict.UAT_UPCASE.equals(rqrmnt_stage) ? rqrmnt_stage : Dict.REL;
                        break;
                    default:
                        rqrmnt_stage = Dict.SIT_UPCASE.equals(rqrmnt_stage) || Dict.UAT_UPCASE.equals(rqrmnt_stage) || Dict.REL.equals(rqrmnt_stage) ? rqrmnt_stage : Dict.PRODUCTION;
                        break;
                }
            }
            //判断该需求下是否所有任务的确认书已到达
            if(operaterTimeList.size()==taskIds.size()){
                Collections.sort(operaterTimeList);
                //选择任务中最晚的确认书到达时间作为需求的任务书到达时间
                releaseRqrmnt.setOperate_time(operaterTimeList.get(operaterTimeList.size()-1));
            }
            releaseRqrmnt.setStage(rqrmnt_stage);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR);
        }
        // 废弃任务不做展示，如果需求下没有可展示的任务，则返回空
        if(CommonUtils.isNullOrEmpty(taskList)) {
            releaseRqrmnt = null;
        } else {
            releaseRqrmnt.setTask_list(taskList);
            releaseRqrmnt.setTask_map(null);
        }
        return releaseRqrmnt;
    }

    private void handleTaskMap(Map<String, Object> taskDetail, String rqrmnt_name, Map<String, Object> task_map, String type)  {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, String>> task_file = new ArrayList<>();
        List<Map<String,String>> fileList = (List<Map<String, String>>) taskDetail.get(Dict.NEWDOC);
        String taskId = (String) taskDetail.get(Dict.ID);
        if(!CommonUtils.isNullOrEmpty(fileList)) {
            for (Map<String, String> fileMap : fileList) {
                Map<String, String> file = new HashMap<>();
                String fileType = fileMap.get(Dict.TYPE);
                if(fileType.contains(Dict.TASK_DOC_TYPE_RELEASE)) {
                    file.put(Dict.NAME, fileMap.get(Dict.NAME));
                    file.put(Dict.PATH, fileMap.get(Dict.PATH));
                    file.put(Dict.RQRMNT_NAME_L, rqrmnt_name);
                    file.put(Dict.TASK_NAME, (String) taskDetail.get(Dict.NAME));
                    file.put(Dict.TYPE, type);
                    task_file.add(file);
                }
            }
        }
        map.put(Dict.TASK_FILE, task_file);
        map.put(Dict.TASK_ID, taskId);
        map.put(Dict.TASK_NAME, taskDetail.get(Dict.NAME));
        map.put(Dict.MASTER, taskDetail.get(Dict.MASTER));
        task_map.put((String) taskDetail.get(Dict.ID), map);
    }

    @Override
    public Map<String,String> getConfirmRecord(String taskId,String type) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID,taskId);
        map.put(Dict.TYPE,type);
        map.put(Dict.REST_CODE, "getConfirmRecord");
        Map<String,String> result = null;
        try {
            result = (Map<String,String>) restTransport.submit(map);
        } catch (Exception e) {
            logger.error("getConfirmRecord error with:{}",e);
            return new HashMap<>();
        }
        return result;
    }

    @Override
    public void changeRqrmntMsg(String task_id) throws Exception {
        //查询是否有挂载投产窗口
        ReleaseTask releaseTask = releaseTaskService.findOneTask(task_id);
        if(!CommonUtils.isNullOrEmpty(releaseTask)){
            //任务本身需求信息
            Map<String, Object> rqrmnt = taskService.queryTaskRqrmnt(task_id);
            if(CommonUtils.isNullOrEmpty(rqrmnt)){
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"任务绑定的需求不存在"});
            }
            String rqrmntId = (String)rqrmnt.get(Dict.RQRMNT_NO);
            ReleaseRqrmnt rq = releaseRqrmntDao.queryByReleaseNodeNameAndTaskId(releaseTask.getRelease_node_name(), task_id);
            if(!CommonUtils.isNullOrEmpty(rq) && !rqrmntId.equals(rq.getRqrmnt_id())){
                logger.info("已挂载投产窗口的任务更换需求编号，任务id：{}，原需求id：{}，新需求id：{}", task_id, rq.getRqrmnt_id(), rqrmntId);
                Map<String, Object> taskMap = rq.getTask_map();
                taskMap.remove(task_id);
                if(CommonUtils.isNullOrEmpty(taskMap)){
                    releaseRqrmntDao.deleteById(rq.getId());
                } else {
                    rq.setTask_map(taskMap);
                    releaseRqrmntDao.editTaskMap(rq);
                }
                addOrEditRqrmntTask(task_id,releaseTask.getRelease_node_name());
            }
        }
    }

    @Override
    public void addOrEditRqrmntTaskAsync(String task_id, String release_node_name) throws Exception {
        logger.info("------异步处理投产文件开始------");
        addOrEditRqrmntTask(task_id, release_node_name);
        logger.info("------异步处理投产文件结束------");
    }

}
