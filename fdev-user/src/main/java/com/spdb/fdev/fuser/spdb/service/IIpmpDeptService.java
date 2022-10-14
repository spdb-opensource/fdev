package com.spdb.fdev.fuser.spdb.service;


import com.spdb.fdev.fuser.spdb.entity.user.IpmpDept;

import java.util.List;

public interface IIpmpDeptService {
    /**
     * 同步全量组织机构信息
     */
    void syncAllIpmpDept();

    /**
     * 查询组织机构信息
     * @param deptId
     * @return
     */
    List<IpmpDept> queryIpmpDept(String deptId);
}
