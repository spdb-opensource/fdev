package com.spdb.fdev.fdevinterface.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.spdb.entity.ScanRecord;
import com.spdb.fdev.fdevinterface.spdb.service.RestTransportService;
import com.spdb.fdev.fdevinterface.spdb.service.ScanRecordService;
import com.spdb.fdev.fdevinterface.spdb.service.ScanSkipService;
import com.spdb.fdev.fdevinterface.spdb.service.ScannerService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/interface")
@RestController
public class ScannerController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ScannerService scannerService;
    @Resource
    private RestTransportService transportService;
    @Resource
    private ScanRecordService scanRecordService;
    @Resource
    private ScanSkipService scanSkipService;
    @Autowired
    UserVerifyUtil userVerifyUtil;

    /**
     * 接口/交易扫描 （应用模块）
     *
     * @param params
     * @return
     * @author hexy
     */
    @OperateRecord(operateDiscribe="接口模块-应用列表扫描接口")
    @PostMapping("/scanInterface")
    public synchronized JsonResult scanInterface(@RequestBody Map params) throws Exception {

        String appServiceId = (String) params.get(Dict.NAME_EN);
        String branchName = (String) params.get(Dict.BRANCH);
        String type = (String) params.get(Dict.TYPE);
        if (StringUtils.isEmpty(appServiceId) || StringUtils.isEmpty(branchName) || StringUtils.isEmpty(type)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        return JsonResultUtil.buildSuccess(scannerService.scanInterface(appServiceId, branchName, type, 0));
    }

    /**
     * 接口/交易扫描 （任务模块）
     *
     * @param params
     * @return
     * @author hexy
     */
    @OperateRecord(operateDiscribe="接口模块-任务详情扫描接口")
    @PostMapping("/taskScanInterface")
    public synchronized JsonResult taskScanInterface(@RequestBody Map params) throws Exception {

        String appServiceId = (String) params.get(Dict.NAME_EN);
        String branchName = (String) params.get(Dict.BRANCH);
        String type = (String) params.get(Dict.TYPE);
        if (StringUtils.isEmpty(appServiceId) || StringUtils.isEmpty(branchName) || StringUtils.isEmpty(type)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        return JsonResultUtil.buildSuccess(scannerService.scanInterface(appServiceId, branchName, type, 0));
    }

    /**
     * 接口/交易 自动扫描(免登录)
     *
     * @param params
     * @return
     * @author hexy
     */
    @PostMapping("/autoScan")
    public synchronized JsonResult autoScan(@RequestBody Map params) {
        String appServiceId = (String) params.get(Dict.NAME_EN);
        String branchName = (String) params.get(Dict.BRANCH);
        String type = (String) params.get(Dict.TYPE);
        if (StringUtils.isEmpty(appServiceId) || StringUtils.isEmpty(branchName) || StringUtils.isEmpty(type)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        if (scanSkipService.skipAllScanFlag(appServiceId)) {
            return JsonResultUtil.buildError("", "此项目不需要扫描接口！");
        }
        Integer projectId = (Integer) params.get(Dict.PROJECT_ID);
        return JsonResultUtil.buildSuccess(scannerService.scanInterface(appServiceId, branchName, type, projectId));
    }

    @PostMapping("/scanAll")
    public synchronized JsonResult scanAll(@RequestBody Map params) {
        String branchName = (String) params.get(Dict.BRANCH);
        List<Map> appList = transportService.getAllApp();
        for (Map map : appList) {
            String appServiceId = (String) map.get(Dict.SERVICE_ID);
            if (scanSkipService.skipAllScanFlag(appServiceId)) {
                continue;
            }
            try {
                scannerService.scanInterface(appServiceId, branchName, (String) map.get(Dict.TYPE), 0);
            } catch (Exception e) {
                logger.info("扫描{}出错!{}", map.get(Dict.SERVICE_ID), e.getMessage());
            }
        }
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询扫描记录
     *
     * @param params
     * @return
     */
    @PostMapping("/queryScanRecord")
    public JsonResult getScanRecord(@RequestBody Map params) {
        String servicedId = (String) params.get(Dict.SERVICE_ID);
        String branch = (String) params.get(Dict.BRANCH);
        String type = (String) params.get(Dict.TYPE);
        Integer page = (Integer) params.get(Dict.PAGE);
        Integer pageNum = (Integer) params.get(Dict.PAGE_NUM);
        String startTime = (String) params.get(Dict.START_TIME);
        String endTime = (String) params.get(Dict.END_TIME);
        String groupId = (String) params.get(Dict.GROUP_ID);
        String isScanSuccessFlag = (String) params.get(Dict.IS_SCAN_SUCCESS_FLAG);
        String recentlyScanFlag = (String) params.get(Dict.RECENTLY_SCAN_FLAG);
        if (   StringUtils.isEmpty(servicedId) && StringUtils.isEmpty(branch) && StringUtils.isEmpty(type) && StringUtils.isEmpty(startTime) 
        	&& StringUtils.isEmpty(endTime) && StringUtils.isEmpty(groupId) && StringUtils.isEmpty(isScanSuccessFlag) 
        	&& StringUtils.isEmpty(recentlyScanFlag) ) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_ALLEMPTY);
        }
        return JsonResultUtil.buildSuccess(scanRecordService.getScanRecord(servicedId, branch, type, page, pageNum,startTime,endTime,groupId,isScanSuccessFlag,recentlyScanFlag));
    }
    
    /**
     * 导出扫描记录 
     *
     * @author xxx
     * @throws Exception 
     */
    @RequestMapping(value = "/downloadScanRecordFile", method = RequestMethod.GET)
    public void downloadScanRecordFile(String service_id,String branch,String type,String StartTime,String EndTime,String GroupId,String IsScanSuccessFlag,String RecentlyScanFlag,HttpServletResponse response) throws Exception {
        if (   StringUtils.isEmpty(service_id) && StringUtils.isEmpty(branch) && StringUtils.isEmpty(type) && StringUtils.isEmpty(StartTime) 
        	&& StringUtils.isEmpty(EndTime) && StringUtils.isEmpty(GroupId) && StringUtils.isEmpty(IsScanSuccessFlag) 
        	&& StringUtils.isEmpty(RecentlyScanFlag) ) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_ALLEMPTY);
        }
        Map scanRecordMap = scanRecordService.getScanRecord(service_id, branch, type, 1, 0,StartTime,EndTime,GroupId,IsScanSuccessFlag,RecentlyScanFlag);
        String fileName = scanRecordService.createExcel((List<ScanRecord>) scanRecordMap.get(Dict.LIST));
      	this.scanRecordService.exportFile(fileName, response);
    }
    
    /**
     * 接口扫描记录清理
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/timingClearScanRecord", method = RequestMethod.POST)
    public JsonResult timingClearScanRecord() throws Exception{
    	scanRecordService.timingClearScanRecord();
        return JsonResultUtil.buildSuccess();
    }
    /**
     * 判断应用模块是否需要展示扫描入口：用户为应用负责人 && 不需要跳过扫描的项目
     *
     * @param params
     * @return
     */
    @PostMapping("/isAppManager")
    public JsonResult isAppManager(@RequestBody Map params) {
        String appServiceId = (String) params.get(Dict.L_SERVICEID);
        if (StringUtils.isEmpty(appServiceId)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        Map<String, Object> resultMap = new HashMap<>();
        if (!scanSkipService.skipAllScanFlag(appServiceId) && transportService.isAppDevManager(appServiceId)) {
            resultMap.put(Dict.LIMIT, true);
        } else {
            resultMap.put(Dict.LIMIT, false);
        }
        return JsonResultUtil.buildSuccess(resultMap);
    }

    /**
     * 判断任务模块是否需要展示扫描入口：用户为任务负责人、行内负责人、开发人员 && 不需要跳过扫描的项目
     *
     * @param params
     * @return
     * @author hexy
     */
    @PostMapping("/isTaskManager")
    public JsonResult isTaskManager(@RequestBody Map params) {
        String taskId = (String) params.get(Dict.ID);
        String appServiceId = (String) params.get(Dict.SERVICE_ID);
        if (StringUtils.isEmpty(taskId)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        Map<String, Object> resultMap = new HashMap<>();
        if (!StringUtils.isEmpty(appServiceId) && !scanSkipService.skipAllScanFlag(appServiceId) && transportService.isTaskManager(taskId)) {
            resultMap.put(Dict.LIMIT, true);
        } else {
            resultMap.put(Dict.LIMIT, false);
        }
        return JsonResultUtil.buildSuccess(resultMap);
    }

}
