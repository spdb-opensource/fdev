package com.spdb.fdev.fuser.spdb.service;

import com.spdb.fdev.fuser.spdb.entity.user.StuckPoint;

import java.util.Map;

public interface StuckPointService {

    /**
     *
     * 新增卡点
     * @param stuckPoint
     * @return
     */
    StuckPoint addStuckPoint(StuckPoint stuckPoint);


    StuckPoint checkStuckPointMapFieldsIsNull(Map map);
}
