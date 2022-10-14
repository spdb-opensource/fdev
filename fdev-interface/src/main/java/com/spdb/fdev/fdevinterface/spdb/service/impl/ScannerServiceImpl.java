package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.ErrorMessageUtil;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.spdb.callable.BaseScanCallable;
import com.spdb.fdev.fdevinterface.spdb.callable.CallableFactory;
import com.spdb.fdev.fdevinterface.spdb.entity.ScanRecord;
import com.spdb.fdev.fdevinterface.spdb.service.RestTransportService;
import com.spdb.fdev.fdevinterface.spdb.service.ScanRecordService;
import com.spdb.fdev.fdevinterface.spdb.service.ScannerService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class ScannerServiceImpl implements ScannerService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value(value = "${path.git.clone}")
    private String gitClonePath;
    @Value(value = "${git.clone.user}")
    private String gitCloneUser;
    @Value(value = "${git.clone.password}")
    private String gitClonePassword;
    @Value(value = "${msper.web.common.service.clone.url}")
    private String commonServiceCloneUrl;
    @Value(value = "${vue.project.type.id}")
    private String vueProjectTypeId;
    @Resource
    private ScanRecordService scanRecordService;
    @Autowired
    private CallableFactory callableFactory;
    @Resource
    private RestTransportService transportService;
    @Resource
    private ErrorMessageUtil errorMessageUtil;
    @Autowired
    private RestTransportServiceImpl restTransportServiceImpl;

    private ScanRecord scanRecord;
    // 创建线程池
    private static ExecutorService pool;

    static {
        pool = new ThreadPoolExecutor(Constants.TEN, Constants.TEN,
                60L, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(Constants.HUNDRED));
    }

    @Override
    public synchronized Map scanInterface(String appServiceId, String branchName, String type, Integer projectId) {
        // 去掉应用英文名中末尾的-parent
        if (appServiceId.endsWith(Dict.PARENT)) {
            appServiceId = appServiceId.replace(Dict.PARENT, "");
        }
        // 根据应用英文名，调应用模块的接口拿到cloneUrl和GitLab Project ID
        Map urlAndProIdMap = transportService.getAppCloneUrlAndProId(appServiceId);
        String cloneUrl = "";
        if (urlAndProIdMap != null && urlAndProIdMap.size() != 0) {
            cloneUrl = (String) urlAndProIdMap.get(Dict.GIT);
            if (projectId == 0) {
                projectId = (Integer) urlAndProIdMap.get(Dict.GITLAB_PROJECT_ID);
            }
        }
        // clone远程项目
        FileUtil.cloneProject(appServiceId, cloneUrl, branchName, gitClonePath, gitCloneUser, gitClonePassword, commonServiceCloneUrl);
        // 获取src文件夹的路径
        List<String> srcPathList = new ArrayList<>();
        FileUtil.getSrcPath(gitClonePath, srcPathList);
        if (CollectionUtils.isEmpty(srcPathList)) {
            throw new FdevException(ErrorConstants.SRC_FILE_NOT_EXIST, new String[]{appServiceId});
        }
        Map map = restTransportServiceImpl.getAppInfo(appServiceId);
        String typeId = "";
        if (!FileUtil.isNullOrEmpty(map)) {
            typeId = (String) map.get("type_id");
        }
        // 获取扫描类型，若没有用户信息则为自动扫描
        User user;
        String scanType = Constants.AUTO_SCAN;
        try {
            user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getSession().getAttribute("_USER");
            if (user != null) {
                scanType = Constants.HAND_SCAN + "(" + user.getUser_name_cn() + ")";
            }
        } catch (Exception e) {
            logger.info("获取当前登录用户失败!{}", e.getMessage());
        }
        scanRecord = new ScanRecord();
        scanRecord.setType(scanType);
        scanRecord.setServiceId(appServiceId);
        scanRecord.setBranch(branchName);
        // 获得扫描的线程列表
        List<Callable> callableList = getCallableList(srcPathList, appServiceId, branchName, type, projectId, typeId, scanType);
        Map returnMap;
        try {
            returnMap = runCallable(callableList);
            if (StringUtils.isEmpty(returnMap.get(Dict.SUCCESS)) && StringUtils.isEmpty(returnMap.get(Dict.ERROR))) {
                if (vueProjectTypeId.equals(typeId)) {
                    returnMap.put(Dict.ERROR, "您的项目不符合既定vue项目结构，请在接口及路由/前端路由/路由-查看路由规范下查看规范!");
                } else {
                    returnMap.put(Dict.ERROR, "您的项目不符合既定微服务项目结构，请在接口及路由/接口/接口列表-显示规范下查看规范!");
                }
            }
        } catch (Exception e) {
            logger.error("扫描{}出错!{}", appServiceId, e.getMessage());
            unknownError();
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"扫描出错!"});
        } finally {
            // 保存扫描记录
            scanRecordService.save(scanRecord);
            // 文件读取完毕，删除clone下来的代码
            FileUtil.deleteFiles(new File(gitClonePath));
        }
        return returnMap;
    }

    /**
     * 获得扫描的线程列表
     *
     * @param srcPathList
     * @param appServiceId
     * @param branchName
     * @param type
     * @param projectId
     * @return
     */
    private List<Callable> getCallableList(List<String> srcPathList, String appServiceId, String branchName, String type, Integer projectId, String typeId, String scanType) {
        List<Callable> callableList = new ArrayList<>();
        List<String> callableNameList = new ArrayList<>();
        if (Dict.MSPER_WEB_COMMON_SERVICE.equals(appServiceId)) {
            // 扫描网银公共组件
            BaseScanCallable commonSoapCallable = callableFactory.getCallableByName(Dict.COMMON_SOAP_CALLABLE, srcPathList, branchName, appServiceId, projectId, scanType);
            callableList.add(commonSoapCallable);
            return callableList;
        }
        //扫描fdev和玉衡的的前端项目,当前只扫描master分支
        if (Dict.TEST_MANAGE_UI.equals(appServiceId) || Dict.FDEV_VUE_ADMIN.equals(appServiceId)) {
            BaseScanCallable InterfaceStatisticsCallable = callableFactory.getCallableByName(Dict.VUE_INTERFACE_STATISTICS_CALLABLE, srcPathList, branchName, appServiceId, projectId, scanType);
            callableList.add(InterfaceStatisticsCallable);
            return callableList;
        }
        // 兼容原来的type
        if (type.equals("124")) {
            type = "09";
        }
        if (type.contains("0")) {
            // 扫描交易提供方
            callableNameList.add(Dict.TRANS_CALLABLE);
            if (Dict.SPDB_CLI_MOBCLI.equals(appServiceId) || Dict.SPDB_CLI_CMNET.equals(appServiceId)) {
                // 扫描客户端容器化项目交易调用方
                callableNameList.add(Dict.TRANS_RELATION_CONTAINER_CALLABLE);
            } else if (vueProjectTypeId.equals(typeId)) {
                // 扫描vue项目交易调用
                callableNameList.add(Dict.TRANS_RELATION_VUE_CALLABLE);
            }
        }
        if (type.contains("9")) {
            if (vueProjectTypeId.equals(typeId)) {
                // 扫描客户端vue项目路由调用
                callableNameList.add(Dict.VUE_ROUTER_CALLABLE);
            } else {
                // 扫描SOAP接口调用方
                callableNameList.add(Dict.SOAP_RELATION_CALLABLE);
                if (!appServiceId.startsWith(Dict.MSPER)) {
                    // 扫描SOP接口调用方，以msper开头的项目不扫
                    callableNameList.add(Dict.SOP_RELATION_CALLABLE);
                }
                // 扫描REST接口调用方
                callableNameList.add(Dict.REST_RELATION_CALLABLE);
                // 扫描REST接口提供方
                callableNameList.add(Dict.REST_CALLABLE);
                // 扫描SOAP接口提供方
                callableNameList.add(Dict.SOAP_CALLABLE);
            }
        }
        for (String callableName : callableNameList) {
            BaseScanCallable soapCallable = callableFactory.getCallableByName(callableName, srcPathList, branchName, appServiceId, projectId, scanType);
            callableList.add(soapCallable);
        }
        return callableList;
    }

    /**
     * 执行多线程并返回执行结果
     *
     * @param callableList
     * @return
     */
    private Map runCallable(List<Callable> callableList) throws ExecutionException, InterruptedException {
        Map<String, String> returnMap = new HashMap<>();
        List<Future> futureList = new ArrayList<>();
        // 记录正常返回的信息
        StringBuilder successMsg = new StringBuilder();
        // 记录异常信息
        StringBuilder errorMsg = new StringBuilder();
        // 执行多线程
        for (Callable callable : callableList) {
            Future future = pool.submit(callable);
            futureList.add(future);
        }
        // 得到线程返回结果
        for (Future future : futureList) {
            Map futureMap = (Map) future.get();
            String success = (String) futureMap.get(Dict.SUCCESS);
            String error = (String) futureMap.get(Dict.ERROR);
            if (!StringUtils.isEmpty(success)) {
                successMsg.append(success);
                getSuccessScanRecord(success);
            }
            if (!StringUtils.isEmpty(error)) {
                errorMsg.append(error);
                getErrorScanRecord(error);
            }
        }
        // 返回给前端的扫描结果
        returnMap.put(Dict.SUCCESS, successMsg.toString());
        returnMap.put(Dict.ERROR, errorMsg.toString());
        return returnMap;
    }

    private void getSuccessScanRecord(String success) {
        if (success.contains("REST接口提供方")) {
            scanRecord.setRest(getSuccessMap(success));
        }
        if (success.contains("REST接口调用方")) {
            scanRecord.setRestRel(getSuccessMap(success));
        }
        if (success.contains("SOAP接口提供方")) {
            scanRecord.setSoap(getSuccessMap(success));
        }
        if (success.contains("SOAP接口调用方")) {
            scanRecord.setSoapRel(getSuccessMap(success));
        }
        if (success.contains("SOP接口调用方")) {
            scanRecord.setSopRel(getSuccessMap(success));
        }
        if (success.contains("交易提供方")) {
            scanRecord.setTrans(getSuccessMap(success));
        }
        if (success.contains("交易调用方")) {
            scanRecord.setTransRel(getSuccessMap(success));
        }
        if (success.contains("VUE项目路由")) {
            scanRecord.setRouter(getSuccessMap(success));
        }
    }

    private void getErrorScanRecord(String error) {
        if (error.contains("REST接口提供方")) {
            scanRecord.setRest(getErrorMap(error));
        }
        if (error.contains("REST接口调用方")) {
            scanRecord.setRestRel(getErrorMap(error));
        }
        if (error.contains("SOAP接口提供方")) {
            scanRecord.setSoap(getErrorMap(error));
        }
        if (error.contains("SOAP接口调用方")) {
            scanRecord.setSoapRel(getErrorMap(error));
        }
        if (error.contains("SOP接口调用方")) {
            scanRecord.setSopRel(getErrorMap(error));
        }
        if (error.contains("交易提供方")) {
            scanRecord.setTrans(getErrorMap(error));
        }
        if (error.contains("交易调用方")) {
            scanRecord.setTransRel(getErrorMap(error));
        }
        if (error.contains("VUE项目路由")) {
            scanRecord.setRouter(getErrorMap(error));
        }
    }

    private Map getSuccessMap(String success) {
        Map<String, String> map = new HashMap<>();
        map.put(Dict.CODE, "1");
        map.put(Dict.MSG, success);
        return map;
    }

    private Map getErrorMap(String error) {
        Map<String, String> map = new HashMap<>();
        map.put(Dict.CODE, "2");
        map.put(Dict.MSG, error);
        return map;
    }

    private void unknownError() {
        Map<String, String> map = new HashMap<>();
        map.put(Dict.CODE, "2");
        map.put(Dict.MSG, "未知错误！");
        scanRecord.setRest(map);
        scanRecord.setRestRel(map);
        scanRecord.setSoap(map);
        scanRecord.setSoapRel(map);
        scanRecord.setSopRel(map);
        scanRecord.setTrans(map);
        scanRecord.setTransRel(map);
        scanRecord.setRouter(map);
    }

}
