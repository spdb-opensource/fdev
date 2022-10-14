package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.entity.IpmpTask;

import java.util.List;

public interface IpmpTaskDao {
    IpmpTask add(IpmpTask ipmpTask);

    IpmpTask update(IpmpTask ipmpTask);

    IpmpTask findIpmpTaskById(String id);

    List<IpmpTask> queryIpmpTaskByGroupId(String groupId);

    IpmpTask queryUnitByTakskId(String id);

    List<IpmpTask> queryAll();

    List<IpmpTask> queryUnitByUnitNo(String unintNo);

    IpmpTask findIpmpTaskByTaskNoAndGroupId(String groupId,String taskNo);

    List<IpmpTask> queryIpmpTaskByGroupIds(List<String> groupIds);

    IpmpTask queryGroupIdByTaskNoAndUnitNo(String taskNo, String ipmpImplementUnitNo);

}
