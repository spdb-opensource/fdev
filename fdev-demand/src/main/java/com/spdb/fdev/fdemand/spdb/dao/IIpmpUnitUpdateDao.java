package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.entity.IpmpUnitUpdate;

public interface IIpmpUnitUpdateDao {

    /**
     * 根据创建时间，获取最新的一条记录
     * @return
     */
    IpmpUnitUpdate queryNewInfo();

    IpmpUnitUpdate save(IpmpUnitUpdate ipmpunitUpdate);

}
