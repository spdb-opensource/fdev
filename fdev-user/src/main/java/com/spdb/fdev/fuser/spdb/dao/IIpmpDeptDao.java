package com.spdb.fdev.fuser.spdb.dao;


import com.spdb.fdev.fuser.spdb.entity.user.IpmpDept;

import java.util.List;

public interface IIpmpDeptDao {
    /**
     * 批量添加ipmp组织机构信息
     * @param ipmpDeptList
     */
    void addIpmpDeptBatch(List<IpmpDept> ipmpDeptList);

    /**
     * 查询ipmp组织机构信息
     * @param deptId
     * @return
     */
    List<IpmpDept> queryIpmpDept(String deptId);
}
