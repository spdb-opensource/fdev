package com.manager.ftms.service.impl;

import com.manager.ftms.service.IMantisService;
import com.manager.ftms.util.Dict;
import com.test.testmanagecommon.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class MantisServiceImpl implements IMantisService {
    @Autowired
    private RestTransport restTransport;

    @Override
    public List<Map> queryIssuesByFprId(String fprId) throws Exception {
        Map sendMap = new HashMap();
        sendMap.put(Dict.ID, fprId);
        sendMap.put(Dict.REST_CODE, "mantis.queryIssueByPlanResultId");
        return (List<Map>)restTransport.submit(sendMap);
    }
}
