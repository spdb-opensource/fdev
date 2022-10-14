package com.spdb.fdev.fdevtask.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.Tuple;
import com.spdb.fdev.fdevtask.base.utils.WebUtils;
import com.spdb.fdev.fdevtask.spdb.dao.IFdevTaskDao;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.service.SonarQubeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Api(tags = "sonar接口")
@RestController
@RequestMapping("/api/sonarqube")
@RefreshScope
public class SonarQubeController {

    private Logger log = LoggerFactory.getLogger(SonarQubeController.class);
    @Autowired
    private SonarQubeService sonarQubeService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Autowired
    private IFdevTaskDao fdevTaskDao;

    @ApiOperation(value = "从sonar获取扫描结果", notes = "sonar调用相关")
    @PostMapping(value = "/getCodeQuality")
    @RequestValidate(NotEmptyFields = {"task_id"})
    public JsonResult getCodeQuality(@RequestBody Map params) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        String taskId = params.get("task_id").toString();
        Tuple.Tuple3 scanInfo = sonarQubeService.getSonarScanInfo(taskId);
        HashMap<String, Object> result = new HashMap<>();
        result.put("qube_switch", scanInfo.first());
        result.put("qube_bugs", scanInfo.second());
        result.put("tips", scanInfo.third());
        //如果是卡点管理员，这制空tips
        if ("1".equals(scanInfo.first()) && userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            log.info("卡点管理进行提交测试，不做卡点url = getCodeQuality");
            result.put("tips", "");
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "更新sonars扫描结果", notes = "sonar更新")
    @PostMapping(value = "/updateTaskSonarId")
    @RequestValidate(NotEmptyFields = {"feature_id", "web_name_en"})
    public JsonResult updateTaskSonarId(@RequestBody Map<Object, String> params) {
        String featureId = params.get("feature_id");
        String webNameEn = params.get("web_name_en");
        String sonarId = params.get("sonar_id");
        String scanTime = params.get("scan_time");
        String state = sonarQubeService.updateTaskSonarId(featureId, webNameEn, sonarId, scanTime);
        return JsonResultUtil.buildSuccess(state);
    }

    @ApiOperation(value = "获取扫描进度", notes = "sonar调用相关")
    @PostMapping(value = "/getScanProcess")
    @RequestValidate(NotEmptyFields = {"task_id"})
    public JsonResult getScanProcess(@RequestBody Map<Object, String> params) {
        String taskId = params.get("task_id");
        Tuple.Tuple3 scanProcess = sonarQubeService.getScanProcess(taskId);
        HashMap<String, Object> result = new HashMap<>();
        result.put("scan_stage", scanProcess.first());
        result.put("tips", scanProcess.second());
        result.put("last_scan_time", scanProcess.third());
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "下载日志文件", notes = "sonar调用相关")
    @PostMapping(value = "/downloadSonarLog")
    @RequestValidate(NotEmptyFields = {"taskId"})
    public void downloadSonarLog(@RequestBody Map<String, String> map,
                                 HttpServletResponse resp) {
        String fileName = Optional.ofNullable(map.get("fileName")).orElse("sonar");
        WebUtils.setFileDownloadHeader(resp, fileName);
        FdevTask fdevTask = fdevTaskDao.selectTaskById(map.get("taskId"));
        String sonarId = fdevTask.getSonarId();
        String[] split = sonarId.split("/");
        if (split.length <= 0) {
            return;
        }
        final String local = "/fdev/sonar/";
        StringBuffer sb = new StringBuffer(local).append(split[split.length - 1]);
        try (BufferedInputStream inStream =
                     new BufferedInputStream(new FileInputStream(sb.toString()));
             BufferedOutputStream outStream =
                     new BufferedOutputStream(resp.getOutputStream())) {
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            outStream.flush();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            log.info("文件读取失败:{}Error Trace:{}",
                    sb.toString(),
                    sw.toString());
            throw new FdevException(ErrorConstants.FILE_NOT_EXIT, new String[]{"请尝试重新扫描"});
        }
    }
}
