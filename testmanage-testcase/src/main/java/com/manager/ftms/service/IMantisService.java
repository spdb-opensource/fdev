package com.manager.ftms.service;

import java.util.List;
import java.util.Map;

public interface IMantisService {

    List<Map> queryIssuesByFprId(String fprId) throws Exception;
}
