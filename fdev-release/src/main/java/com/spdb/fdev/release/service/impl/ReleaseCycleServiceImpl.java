package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IReleaseCycleDao;
import com.spdb.fdev.release.entity.ReleaseCycle;
import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.service.IReleaseCycleService;
import com.spdb.fdev.release.service.IReleaseNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ReleaseCycleServiceImpl implements IReleaseCycleService {
    @Autowired
    private IReleaseCycleDao releaseCycleDao;
    @Autowired
    private IReleaseNodeService releaseNodeService;

    @Override
    public ReleaseCycle create(String releaseDate) throws Exception {
        return buildReleaseCycle(releaseDate, releaseDate.replace("-", ""));
    }

    private ReleaseCycle buildReleaseCycle(String releaseDate, String releaseNodeName) throws ParseException {
        List<String> workDays = getSevenDays(releaseDate);
        ReleaseCycle releaseCycle = new ReleaseCycle();
        releaseCycle.setRelease_node_name(releaseNodeName);
        releaseCycle.setT(releaseDate);
        releaseCycle.setT_1(workDays.get(0));
        releaseCycle.setT_2(workDays.get(1));
        releaseCycle.setT_3(workDays.get(2));
        releaseCycle.setT_4(workDays.get(3));
        releaseCycle.setT_5(workDays.get(4));
        releaseCycle.setT_6(workDays.get(5));
        releaseCycle.setT_7(workDays.get(6));
        releaseCycle.setT_8(workDays.get(7));
        releaseCycle.setCreate_time(TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        releaseCycle = releaseCycleDao.save(releaseCycle);
        return releaseCycle;
    }

    @Override
    public ReleaseCycle update(Map<String, Object> requestParam) throws Exception {
        String releaseDate = (String) requestParam.get(Dict.RELEASE_DATE);
        Map<String, String> map = (Map<String, String>) requestParam.get(Dict.RELEASE_DATE_LIST);
        String releaseNodeName=releaseDate.replace("-","");
        ReleaseNode releaseNode = releaseNodeService.queryDetail(releaseNodeName);
        if (CommonUtils.isNullOrEmpty(releaseNode)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"待修改的投产大窗口不存在"});
        }
        List<String> releaseContact = releaseNode.getRelease_contact();
        //判断当前用户是否为投产牵头人
        if (!releaseContact.contains(CommonUtils.getSessionUser().getUser_name_en())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户不是牵头联系人，没有修改权限"});
        }
        //查询周期表
        ReleaseCycle releaseCycle = releaseCycleDao.queryByReleaseNodeName(releaseNodeName);
        if (CommonUtils.isNullOrEmpty(releaseCycle)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"待修改的投产投产周期不存在"});
        }
        //修改投产周期
        String t_1 = map.get(Constants.T_1);
        String t_2 = map.get(Constants.T_2);
        String t_3 = map.get(Constants.T_3);
        String t_4 = map.get(Constants.T_4);
        String t_5 = map.get(Constants.T_5);
        String t_6 = map.get(Constants.T_6);
        String t_7 = map.get(Constants.T_7);
        String t_8 = map.get(Constants.T_8);
        if (!CommonUtils.isNullOrEmpty(t_1)) {
            releaseCycle.setT_1(t_1);
        }
        if (!CommonUtils.isNullOrEmpty(t_2)) {
            releaseCycle.setT_2(t_2);
        }
        if (!CommonUtils.isNullOrEmpty(t_3)) {
            releaseCycle.setT_3(t_3);
        }
        if (!CommonUtils.isNullOrEmpty(t_4)) {
            releaseCycle.setT_4(t_4);
        }
        if (!CommonUtils.isNullOrEmpty(t_5)) {
            releaseCycle.setT_5(t_5);
        }
        if (!CommonUtils.isNullOrEmpty(t_6)) {
            releaseCycle.setT_6(t_6);
        }
        if (!CommonUtils.isNullOrEmpty(t_7)) {
            releaseCycle.setT_7(t_7);
        }
        if (!CommonUtils.isNullOrEmpty(t_8)) {
            releaseCycle.setT_8(t_8);
        }
        //修改数据库
       releaseCycle=releaseCycleDao.update(releaseCycle);
        return releaseCycle;
    }

    @Override
    public Map<String, String> queryByReleaseDate(Map<String, String> requestParam) {
        String releaseDate = requestParam.get(Dict.RELEASE_DATE);
        ReleaseCycle releaseCycle = releaseCycleDao.queryByReleaseNodeName(releaseDate.replace("-", ""));
        if (CommonUtils.isNullOrEmpty(releaseCycle)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"投产周期不存在"});
        }
        Map<String, String> map = new HashMap<>();
        map.put(Dict.RELEASE_NODE_NAME, releaseCycle.getRelease_node_name());
        map.put(Constants.T, releaseCycle.getT());
        map.put(Constants.T_1, releaseCycle.getT_1());
        map.put(Constants.T_2, releaseCycle.getT_2());
        map.put(Constants.T_3, releaseCycle.getT_3());
        map.put(Constants.T_4, releaseCycle.getT_4());
        map.put(Constants.T_5, releaseCycle.getT_5());
        map.put(Constants.T_6, releaseCycle.getT_6());
        map.put(Constants.T_7, releaseCycle.getT_7());
        map.put(Constants.T_8, releaseCycle.getT_8());
        return map;
    }

    @Override
    public ReleaseCycle edit(String oldReleaseDate, String releaseDate) throws ParseException {
        String oldReleaseNodeName = oldReleaseDate.replace("-", "");
        ReleaseCycle releaseCycle = releaseCycleDao.queryByReleaseNodeName(oldReleaseNodeName);
        if (!CommonUtils.isNullOrEmpty(releaseCycle)) {
            //删除原有周期记录
            releaseCycleDao.deleteByReleaseNodeName(oldReleaseNodeName);
        }
        //新增修改之后的记录
        ReleaseCycle rc = buildReleaseCycle(releaseDate, releaseDate.replace("-", ""));
        return rc;
    }

    private List<String> getSevenDays(String releaseDate) throws ParseException {
        List<String> workDays = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String dateStr = getDaysBeforeStringDate(releaseDate, i);
            if (!isWeekend(dateStr)) {
                workDays.add(dateStr);
            }
        }
        return workDays;
    }

    private static boolean isWeekend(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.DATE_PARSE);
        Date date = sdf.parse(dateStr);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    private static String getDaysBeforeStringDate(String date, int n) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.DATE_PARSE);
        calendar.setTime(sdf.parse(date));
        calendar.set(Calendar.DATE, calendar.get(calendar.DATE) - n);
        return sdf.format(calendar.getTime());
    }
}
