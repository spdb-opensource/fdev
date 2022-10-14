package com.fdev.database.spdb.service.Impl;

import com.fdev.database.spdb.dao.AppDao;
import com.fdev.database.spdb.dao.SysInfoDao;
import com.fdev.database.spdb.entity.SysInfo;
import com.fdev.database.spdb.service.SysInfoService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


@Service
public class SysInfoServiceImpl implements SysInfoService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Autowired
    private RestTransport restTransport;

    @Resource
    private SysInfoDao sysInfoDao;

    @Override
    public List<SysInfo> query(SysInfo sysInfo) throws Exception {
        return sysInfoDao.query(sysInfo);
    }

    @Override
    public void add(SysInfo sysInfo) {
        sysInfoDao.add(sysInfo);
    }

    @Override
    public void update(SysInfo sysInfo) {
        sysInfoDao.update(sysInfo);
    }


}
