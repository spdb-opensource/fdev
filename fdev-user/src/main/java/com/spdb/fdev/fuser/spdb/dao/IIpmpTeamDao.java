package com.spdb.fdev.fuser.spdb.dao;


import com.spdb.fdev.fuser.spdb.entity.user.IpmpTeam;

import java.util.List;

public interface IIpmpTeamDao {
    /**
     * 批量添加ipmp牵头单位团队
     * @param ipmpTeamList
     */
    void addIpmpTeamBatch(List<IpmpTeam> ipmpTeamList);

    /**
     * 查询牵头单位和团队信息
     * @param deptId
     * @param teamId
     * @param deptName
     * @return
     */
    List<IpmpTeam> queryIpmpLeadTeam(String deptId, String teamId, String deptName);
}
