package com.fdev.database.spdb.dao;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fdev.database.spdb.entity.SysInfo;

import java.util.*;

public interface SysInfoDao {
    /**
     * 系统信息查询
     * @param sysInfo
     * @return
     */
    List<SysInfo> query(SysInfo sysInfo) throws JsonProcessingException, Exception;

    SysInfo add(SysInfo sysInfo);

    void update(SysInfo sysInfo);
}
