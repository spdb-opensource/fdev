package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.IpmpProject;

import java.util.List;

public interface IIpmpProjectService {
    /**
     * 同步全量ipmp项目任务集
     */
    void syncAllIpmpProject();

    /**
     * 查询项目和任务集信息
     * @param projectNo
     * @return
     */
    List<IpmpProject> queryIpmpProject(String projectNo);
}
