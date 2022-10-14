package com.spdb.fdev.fuser.spdb.dao;

import com.spdb.fdev.fuser.spdb.entity.user.StuckPoint;

public interface StuckPointDao {


    /**
     * 新增卡点
     * @param stuckPoint
     * @return
     */
    StuckPoint addStuckPoint(StuckPoint stuckPoint);

}
