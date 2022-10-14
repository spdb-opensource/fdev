package com.spdb.fdev.fdevinterface.spdb.callable;

import com.spdb.fdev.common.util.ErrorMessageUtil;
import com.spdb.fdev.fdevinterface.spdb.dao.EsbRelationDao;
import com.spdb.fdev.fdevinterface.spdb.service.GitLabService;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceRelationService;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceService;
import com.spdb.fdev.fdevinterface.spdb.service.TransService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Callable;

@Component
public class BaseScanCallable implements Callable<Object> {

    @Resource
    public InterfaceService interfaceService;
    @Resource
    public InterfaceRelationService interfaceRelationService;
    @Resource
    public TransService transService;
    @Resource
    public GitLabService gitLabService;
    @Resource
    public ErrorMessageUtil errorMessageUtil;
    @Resource
    public EsbRelationDao esbRelationDao;

    // 路径相关
    @Value(value = "${path.join}")
    public String pathJoin;
    @Value(value = "${path.main.resources}")
    public String mainResources;
    @Value(value = "${git.src.main.resources}")
    public String gitSrcMainResources;

    private List<String> srcPathList;
    private String appServiceId;
    private String branchName;
    private Integer projectId;
    private String scanType;

    @Override
    public Object call() throws Exception {
        return null;
    }

    public List<String> getSrcPathList() {
        return srcPathList;
    }

    public void setSrcPathList(List<String> srcPathList) {
        this.srcPathList = srcPathList;
    }

    public String getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(String appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

}
