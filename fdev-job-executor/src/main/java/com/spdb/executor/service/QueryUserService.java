package com.spdb.executor.service;

import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class QueryUserService {
    private Logger logger = LoggerFactory.getLogger(GitlabService.class);

    @Autowired
    private RestTransport restTransport;

    public Map queryUser(String id) throws Exception {
        Map map = new HashMap();
        map.put(Dict.REST_CODE, "queryUser");
        map.put("id", id);
        ArrayList person = (ArrayList) restTransport.submit(map);
        return (Map) person.get(0);
    }
}
