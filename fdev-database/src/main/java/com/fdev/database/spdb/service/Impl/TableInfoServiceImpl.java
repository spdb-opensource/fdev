package com.fdev.database.spdb.service.Impl;

import com.fdev.database.spdb.service.TableInfoService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TableInfoServiceImpl implements TableInfoService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Autowired
    private RestTransport restTransport;


    


}
