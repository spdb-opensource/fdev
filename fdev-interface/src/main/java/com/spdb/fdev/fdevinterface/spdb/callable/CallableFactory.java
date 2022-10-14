package com.spdb.fdev.fdevinterface.spdb.callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CallableFactory {

    @Autowired
    private Map<String, BaseScanCallable> callableMap;

    public BaseScanCallable getCallableByName(String name, List<String> srcPathList, String branchName, String appServiceId, Integer projectId, String scanType) {
        BaseScanCallable baseScanCallable = callableMap.get(name);
        baseScanCallable.setSrcPathList(srcPathList);
        baseScanCallable.setBranchName(branchName);
        baseScanCallable.setAppServiceId(appServiceId);
        baseScanCallable.setProjectId(projectId);
        baseScanCallable.setScanType(scanType);
        return baseScanCallable;
    }

}
