package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.entity.IpmpUnitEntityLog;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnitOperateLog;

import java.util.List;

public interface ILogDao {

    /**
     * 记录实施单元日志信息
     *
     * @param ipmpUnitEntityLog
     * @throws Exception
     */
    void saveIpmpUnitEntityLog(IpmpUnitEntityLog ipmpUnitEntityLog) throws Exception ;

    /**
     * 记录IPMP接口操作日志
     *
     * @param ipmpUnitOperateLog
     * @throws Exception
     */
    void saveIpmpUnitOperateLog(IpmpUnitOperateLog ipmpUnitOperateLog) throws Exception ;
    /**
     * 查询IPMP接口操作日志
     *
     * @param implUnitNum
     * @throws Exception
     */
    List<IpmpUnitOperateLog> queryIpmpUnitOperateLog(String implUnitNum) throws Exception ;
}
