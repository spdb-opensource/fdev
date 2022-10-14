package com.spdb.executor.service.impl;

import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.executor.service.ReportDataService;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class ReportDataServiceImpl implements ReportDataService {


    @Autowired
    private RestTransport restTransport;

    @Override
    public void cacheSonarProject() throws Exception {
        restTransport.submit(new HashMap<String, String>() {{
            put(Dict.REST_CODE, "cacheSonarProject");
        }});
    }
}
