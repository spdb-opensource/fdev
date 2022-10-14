package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.IpmpImplementUnit;
import com.spdb.fdev.fdemand.spdb.entity.IpmpTask;


import java.util.List;
import java.util.Map;

public interface IpmpTaskService {
    IpmpTask add(IpmpTask ipmpTask) throws Exception;

    IpmpTask deleteIpmpUnitById(Map params) throws Exception;

    IpmpTask addUnit(Map params) throws Exception;

    IpmpTask findIpmpTaskById(String id);

    List<IpmpTask> queryIpmpTaskByGroupId(Map params) throws Exception;

    List<IpmpImplementUnit> queryUnitByTakskId(Map params);

    Map searchTaskUnit(Map params) throws Exception;

    List findDemandGroup() throws Exception;

    List getAllGroup();

    IpmpTask queryGroupIdByTaskNoAndUnitNo(String taskNo,String ipmpImplementUnitNo);
}
