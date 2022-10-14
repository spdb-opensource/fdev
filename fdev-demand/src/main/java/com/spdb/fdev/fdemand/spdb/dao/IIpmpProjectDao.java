package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.entity.IpmpProject;
import com.spdb.fdev.fdemand.spdb.entity.IpmpTask;

import java.util.List;

public interface IIpmpProjectDao {
    /**
     * 批量添加ipmp项目任务集
     * @param ipmpProjectList
     */
    void addIpmpProjectBatch(List<IpmpProject> ipmpProjectList);

    /**
     * 查询项目任务集信息
     * @return
     */
    List<IpmpProject> queryIpmpProject(String projectNo);
    /**
     * 根据项目编号查询项目任务集信息
     * @param projectNo
     * @return
     */
    List<IpmpProject> queryIpmpProjectByProjectNo(String projectNo);
}
