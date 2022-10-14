package com.spdb.fdev.fuser.spdb.service;


import com.spdb.fdev.fuser.spdb.entity.user.IpmpTeam;

import java.util.List;

public interface IIpmpTeamService {
    /**
     * 同步全量ipmp牵头单位团队
     */
    void syncAllIpmpTeam();

    /**
     * 查询牵头单位和团队信息
     * @param deptId
     * @param teamId
     * @param deptName
     * @return
     */
    List<IpmpTeam> queryIpmpLeadTeam(String deptId, String teamId, String deptName);
}
