package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.fdevenvconfig.spdb.dao.IAppConfigMappingDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.impl.AppConfigMappingDaoImpl;
import com.spdb.fdev.fdevenvconfig.spdb.service.IGitlabApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/7 15:35
 * @Version 1.0
 */
@Service
@RefreshScope
public class BaseService {
    @Value("${gitlab.token}")
    protected String token;
    @Value("${gitlab.application.file}")
    protected String applicationFile;
    @Autowired
    protected IGitlabApiService gitlabApiService;
    @Autowired
    protected IAppConfigMappingDao appConfigMappingDao;

    protected Random random = new Random(System.currentTimeMillis());

}
