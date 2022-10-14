package com.fdev.database.spdb.service;



import com.fdev.database.spdb.entity.SysInfo;

import java.util.*;

public interface SysInfoService {

    List<SysInfo> query(SysInfo sysInfo) throws Exception;

    void add(SysInfo sysInfo);

    void update(SysInfo sysInfo);
}
