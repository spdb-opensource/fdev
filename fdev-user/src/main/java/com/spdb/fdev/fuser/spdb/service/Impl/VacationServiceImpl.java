package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.VacationDao;
import com.spdb.fdev.fuser.spdb.entity.user.Vacation;
import com.spdb.fdev.fuser.spdb.service.VacationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VacationServiceImpl implements VacationService {


    @Autowired
    private VacationDao vacationDao;

    /**
     * 检验today是否为节假日
     * 节假日是在数据库中记录的，需要每次临近节假日都要维护（除了周六周日）
     * 将需要上班日期的is_vacation有需要的改为1，不需要上班的修改为0
     *
     * @return
     */
    @Override
    public Boolean checkTodayIsVacation() {
        String todayStr = CommonUtils.formatDate(CommonUtils.DATE_PARSE);
        Vacation result = this.vacationDao.queryVacationByTime(todayStr);
        Integer is_vacation = result.getIs_vacation();
        if (is_vacation == 1) {
            return true;
        } else
            return false;
    }
}
