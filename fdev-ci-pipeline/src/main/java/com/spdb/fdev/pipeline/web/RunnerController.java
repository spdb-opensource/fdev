package com.spdb.fdev.pipeline.web;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.annotation.nonull.RunnerToken;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.pipeline.entity.RunnerCluster;
import com.spdb.fdev.pipeline.service.IRunnerClusterService;
import com.spdb.fdev.pipeline.service.IRunnerInfoService;
import com.spdb.fdev.pipeline.service.IRunnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v4")
public class RunnerController {

    private static Logger logger = LoggerFactory.getLogger(RunnerController.class);

    @Autowired
    private IRunnerService runnerService;

    @Autowired
    private IRunnerInfoService runnerInfoService;

    @Autowired
    private IRunnerClusterService runnerClusterService;

    /**
     * runner 请求job 队列
     *
     * @param request
     * @param resp
     * @return
     * @throws Exception
     */
    @PostMapping("/jobs/request")
    @RunnerToken()
    public Map getJob(HttpServletRequest request, HttpServletResponse resp) throws Exception {
//        Map<String, Object> info = (Map<String, Object>)param.get(Dict.INFO);
//        logger.info("*******************  jobs/request  param :" + JSONObject.toJSONString(param));
//        String token = (String)param.get(Dict.TOKEN);
        String token = request.getHeader("Runner-token");
        Map job = runnerService.getJob(token);
        if (CommonUtils.isNullOrEmpty(job)) {
            resp.setStatus(204);
        } else {
            resp.setStatus(201);
            logger.info("******************  response job:" + JSONObject.toJSONString(job));
        }
        return job;
    }

    /**
     * runner请求插件信息
     *
     * @param param
     * @param resp
     * @return
     * @throws Exception
     */
    @PostMapping("/plugins/request")
    @RunnerToken()
    public Map getPlugins(@RequestBody Map<String, Object> param, HttpServletResponse resp) throws Exception {
        Map job = runnerService.getPlugins(param);
        if (CommonUtils.isNullOrEmpty(job)) {
            logger.info("**************** plugins/request  return null");
            resp.setStatus(204);
        } else {
            resp.setStatus(201);
            logger.info("***************  plugins/request  return job: " + JSONObject.toJSONString(job));
        }

        return job;
    }

    /**
     * runner 获取插件输入参数
     *
     * @param param
     * @param resp
     * @return
     * @throws Exception
     */
    @PostMapping("/plugins/input")
    @RunnerToken()
    public Map getPluginInput(@RequestBody Map<String, Object> param, HttpServletResponse resp) throws Exception {
        Map job = runnerService.getPluginInput(param);
        if (CommonUtils.isNullOrEmpty(job)) {
            resp.setStatus(204);
        } else {
            resp.setStatus(201);
            logger.info("****************** input response job: " + JSONObject.toJSONString(job));
        }
        return job;
    }

    /**
     * runner 返回插件结果
     *
     * @param param
     * @param resp
     * @return
     * @throws Exception
     */
    @PostMapping("/plugins/output")
    @RunnerToken()
    public String getPluginOutput(@RequestBody Map<String, Object> param, HttpServletResponse resp) throws Exception {
        runnerService.getPluginOutput(param);
        return Dict.SUCCESS;
    }

    /**
     * runner 返回 构建物上传地址
     *
     * @param param
     * @param resp
     * @return
     * @throws Exception
     */
    @PostMapping("/artifacts/webhook")
    @RunnerToken()
    public String artifactsWebhook(@RequestBody Map<String, Object> param, HttpServletResponse resp) throws Exception {
        logger.info("***************  artifacts/webhook param :" + JSONObject.toJSONString(param));
        runnerService.artifactsWebhook(param);
        return Dict.SUCCESS;
    }

    /**
     * runner请求 构建物地址
     *
     * @param param
     * @param resp
     * @return
     * @throws Exception
     */
    @PostMapping("/artifacts/request")
    @RunnerToken()
    public Map getArtifacts(@RequestBody Map<String, Object> param, HttpServletResponse resp) throws Exception {
        logger.info("***************  artifacts/request param " + JSONObject.toJSONString(param));
        List<Map> artifacts = runnerService.getArtifacts(param);
        Map result = new HashMap();
        if (CommonUtils.isNullOrEmpty(artifacts)) {
            artifacts = new ArrayList<>();
        }
        result.put(Dict.ARTIFACTS, artifacts);
        logger.info("***************  artifacts/request param result " + JSONObject.toJSONString(result));
        return result;
    }

    /**
     * job 执行完，runner返回job结果
     *
     * @param param
     * @param resp
     * @throws Exception
     */
    @PostMapping("/jobs/webhook")
    @RunnerToken()
    public void jobWebHook(@RequestBody Map<String, Object> param, HttpServletResponse resp) throws Exception {
        runnerService.jobWebHook(param);
    }

    /**
     * job执行过程中，runner不断推送最新日志，我们往Redis放job日志缓存
     *
     * @param output
     * @param request
     * @return
     * @throws Exception
     */
    @PatchMapping(value = "/jobs/trace")
    @RunnerToken()
    public ResponseEntity<String> trace(@RequestBody String output, HttpServletRequest request) throws Exception {
        String token = request.getHeader("Job-Token");
        String range = request.getHeader("Content-Range");
        runnerService.setJobLog(output, token, range);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Job-Status", "ok");
        responseHeaders.set("Range", range);
        return new ResponseEntity<>("", responseHeaders, HttpStatus.ACCEPTED); // 顺便把状态码也设置了
    }

    /**
     * runner 调用，降低网络风暴使用
     *
     * @param data
     */
    @RequestMapping(value = "/api/v4/jobs/update", method = RequestMethod.PUT)
    @RunnerToken()
    public void update(@RequestBody String data) {
        // System.out.println(data);
    }

    /**
     * 清除待执行job队列
     *
     * @throws Exception
     */
    @PostMapping("/jobs/clear")
    public void getCacheJobExe(@RequestBody String runnerClusterId) throws Exception {
        runnerService.jobClear(runnerClusterId);
    }

    @PostMapping("/jobs/register")
    public Map registerJob(@RequestBody Map<String, Object> param, HttpServletResponse resp) throws Exception {
        Map<String, Object> info = (Map<String, Object>) param.get(Dict.INFO);
        String token = runnerInfoService.generateToken(info);
        Map<String, String> resMap = new HashMap<>();
        resMap.put(Dict.TOKEN, token);
        resp.setStatus(200);
        return resMap;
    }

    @RequestMapping(value = "/getAllRunnerCluster", method = RequestMethod.POST)
    public JsonResult getAllRunnerCluster(@RequestBody Map<String, Object> param) {
        return JsonResultUtil.buildSuccess(runnerClusterService.getAllRunnerCluster());
    }


    @RequestMapping(value = "/addRunnerCluster", method = RequestMethod.POST)
    public JsonResult addRunnerCluster(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(runnerClusterService.addRunnerCluster(param));
    }

    /**
     * 根据参数来查询runner集群信息
     *
     * @param param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getRunnerClusterInfoByParam", method = RequestMethod.POST)
    public JsonResult getRunnerClusterInfoByParam(@RequestBody Map<String, Object> param) throws Exception {
        List<RunnerCluster> runnerClusterList = runnerClusterService.getRunnerClusterByParam(param);
            return JsonResultUtil.buildSuccess(runnerClusterList);
    }

    /**
     * 根据id来查询runnerCluster 集群
     *
     * @param param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getRunnerClusterInfoById", method = RequestMethod.POST)
    public JsonResult getRunnerClusterInfoById(@RequestBody Map<String, Object> param) throws Exception {
        String runnerClusterId = (String) param.get(Dict.RUNNERCLUSTERID);
        return JsonResultUtil.buildSuccess(runnerClusterService.getRunnerClusterById(runnerClusterId));
    }


    @RequestMapping(value = "/getRunnerInfoById", method = RequestMethod.POST)
    public JsonResult getRunnerInfoById(@RequestBody Map<String, Object> param) throws Exception {
        String runnerId = (String) param.get(Dict.RUNNERID);
        return JsonResultUtil.buildSuccess(runnerInfoService.getRunnerInfoById(runnerId));
    }

    @RequestMapping(value = "/updateRunnerCluster", method = RequestMethod.POST)
    public JsonResult updateRunnerCluster(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(runnerClusterService.updateRunnerCluster(param));
    }

    @RequestMapping(value = "/getAllRunnerInfo", method = RequestMethod.POST)
    public JsonResult getAllRunnerInfo(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(runnerInfoService.getAllRunnerInfo(param));
    }

    @RequestMapping(value = "/getMinioMd5", method = RequestMethod.POST)
    public Object getMinioMd5(@RequestBody Map<String, Object> param) throws Exception {
        return runnerInfoService.getMinioMd5(param);
    }

}
