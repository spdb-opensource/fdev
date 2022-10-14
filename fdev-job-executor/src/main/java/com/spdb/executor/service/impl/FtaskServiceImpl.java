package com.spdb.executor.service.impl;

import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.dict.ErrorConstants;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.executor.service.FtaskService;
import com.spdb.executor.service.FuserService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FtaskServiceImpl implements FtaskService {

    private Logger logger = LoggerFactory.getLogger(FtaskServiceImpl.class);

    @Autowired
    private RestTransport restTransport;


    @Override
    public List queryUserTask(String id) {

        Map sendDate = new HashMap();
        sendDate.put(Dict.REST_CODE, "queryUserTask");
        sendDate.put(Dict.ID, id);
        List resDate = new ArrayList();
        try {
            resDate = (List) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error("查询任务数据失败", e);
            throw new FdevException(ErrorConstants.FTASK_ERROR);
        }
        return resDate;
    }
}
