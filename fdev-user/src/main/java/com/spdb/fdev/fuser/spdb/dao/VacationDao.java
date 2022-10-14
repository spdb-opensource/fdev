package com.spdb.fdev.fuser.spdb.dao;

import com.spdb.fdev.fuser.spdb.entity.user.Vacation;

public interface VacationDao {

    /**
     * 根据时间来查询获得vacation
     *
     * @param time
     * @return
     */
    Vacation queryVacationByTime(String time);

}
