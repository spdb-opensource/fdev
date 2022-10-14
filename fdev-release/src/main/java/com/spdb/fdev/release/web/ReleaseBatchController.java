package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.ReleaseBatchRecord;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.service.IReleaseBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/releasebatch")
@RestController
public class ReleaseBatchController {

    @Autowired
    private IReleaseBatchService iReleaseBatchService;

    /**
     * 新增批次信息
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/addBatch")
    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME, Dict.BATCH_ID, Dict.APPLICATION_ID})
    public JsonResult addBatch(@RequestBody Map<String, Object> map) throws Exception {
        String releaseNodeName = String.valueOf(map.get(Dict.RELEASE_NODE_NAME));
        String batchId = String.valueOf(map.get(Dict.BATCH_ID));
        String appId = String.valueOf(map.get(Dict.APPLICATION_ID));
        String modifyReason = String.valueOf(map.getOrDefault(Dict.MODIFY_REASON, ""));
        List<String> bindAppIds = (List<String>)map.get(Dict.APPLICATIONS);
        iReleaseBatchService.addBatch(releaseNodeName, batchId, appId, modifyReason, bindAppIds);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据任务id查询任务批次信息
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryBatchInfoByTaskId")
    public JsonResult queryBatchInfoByTaskId(@RequestBody Map<String, String> map) throws Exception {
        String taskId =  map.getOrDefault(Dict.TASK_ID, "");
        String appId = map.getOrDefault(Dict.APPLICATION_ID, "");
        String releaseNodeName = map.getOrDefault(Dict.RELEASE_NODE_NAME, "");
        Map<String, Object> result = iReleaseBatchService.queryBatchInfoByTaskId(taskId, appId, releaseNodeName);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 根据应用id查询任务批次信息
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryBatchInfoByAppId")
    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME})
    public JsonResult queryBatchInfoByAppId(@RequestBody Map<String, Object> map) throws Exception {
        List<String> appIds = (List<String>)map.get(Dict.APPLICATION_ID);
        String releaseNodeName = String.valueOf(map.get(Dict.RELEASE_NODE_NAME));
        List<Map<String, String>> releaseBatchRecord = iReleaseBatchService.queryBatchInfoByAppId(appIds, releaseNodeName);
        return JsonResultUtil.buildSuccess(releaseBatchRecord);
    }

    /**
     * 根据投产窗口和批次查应用集合（变更用）
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryAppIdsByNodeAndBatch")
    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME, Dict.BATCH_ID})
    public JsonResult queryAppIdsByNodeAndBatch(@RequestBody Map<String, String> map) throws Exception {
        String releaseNodeName = map.get(Dict.RELEASE_NODE_NAME);
        String batchId = map.get(Dict.BATCH_ID);
        return JsonResultUtil.buildSuccess(iReleaseBatchService.queryAppIdsByNodeAndBatch(releaseNodeName, batchId));
    }

    /**
     * 将需求编号跑批进releaseTask
     * @return
     * @throws Exception
     */
    @RequestMapping("/batchReleaseTask")
    public JsonResult batchReleaseTask(@RequestBody Map<String, String> map) throws Exception {
        String date = map.get(Dict.DATE);
        List<ReleaseTask> result = iReleaseBatchService.batchReleaseTask(date);
        return JsonResultUtil.buildSuccess(result);
    }
}
