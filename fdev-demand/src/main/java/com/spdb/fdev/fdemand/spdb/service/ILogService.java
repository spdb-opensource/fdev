package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;

import java.util.Map;

public interface ILogService {

    /**
     * 记录实施单元日志信息
     *
     * @throws Exception
     */
    void saveIpmpUnitEntityLog(IpmpUnit newIpmpUnit, IpmpUnit oldIpmpUnit, String updateType) throws Exception ;

    /**
     * 记录IPMP接口操作日志
     *
     * @throws Exception
     */
    void saveIpmpUnitOperateLog(String implUnitNum,String interfaceName,Map updateMap,Map response) throws Exception ;

}
