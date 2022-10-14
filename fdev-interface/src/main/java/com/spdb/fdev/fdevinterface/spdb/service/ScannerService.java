package com.spdb.fdev.fdevinterface.spdb.service;

import java.util.Map;

public interface ScannerService {

    Map scanInterface(String appServiceId, String branchName, String type, Integer projectId);

}
