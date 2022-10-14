package com.spdb.fdev.gitlabwork.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.gitlabwork.dao.IGitlabWorkDao;
import com.spdb.fdev.gitlabwork.dao.IStatisticCodeLineDao;
import com.spdb.fdev.gitlabwork.entiy.GitlabWork;
import com.spdb.fdev.gitlabwork.entiy.StatisticCodeLine;
import com.spdb.fdev.gitlabwork.service.IGitLabUserService;
import com.spdb.fdev.gitlabwork.service.IStatisticCodeLineService;
import com.spdb.fdev.gitlabwork.util.Util;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: c-jiangjk
 * @Date: 2021/3/1 15:33
 */
@Service
@RefreshScope
public class IStatisticCodeLineServiceImpl implements IStatisticCodeLineService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IGitLabUserService gitLabUserService;
    @Autowired
    IStatisticCodeLineDao statisticsDao;
    @Autowired
    IGitlabWorkDao gitlabWorkDao;
    @Value("${schedule.since}")
    String since;

    @Override
    public List<Map<String, Object>> query() throws Exception {
        // 获取组信息
        JSONArray groups = gitLabUserService.selectGroup();
        // 获取月份列表
        String until = Util.simpleDateFormat("yyyy-MM-dd").format(new Date());
        List<String> months = Util.getMonthBetweenDates(since, until);
        List<Map<String, Object>> res = new ArrayList<>();
        for (String month : months) {
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> valueMap = new HashMap<>();
            for (int i = 0; i < groups.size(); i++) {
                // 获取组ID
                String id = groups.getJSONObject(i).getString(Dict.ID);
                // 根据组ID获取该组人数
                Map<String, String> param = new HashMap<>();
                param.put(Dict.ID, id);
                JSONArray childGroupPerson = gitLabUserService.selectGroupId(param);
                // 获取该组当月人均代码行数
                StatisticCodeLine statisticCodeLine = getStatisticCodeLineByMonth(month, childGroupPerson);
                valueMap.put(id, statisticCodeLine.getAverage());
            }
            map.put(month, valueMap);
            res.add(map);
        }
        return res;
    }

    @Override
    public List<Map<String, Object>> query(String month) throws Exception {
        // 获取组信息
        JSONArray groups = gitLabUserService.selectGroup();
        List<Map<String, Object>> res = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> valueMap = new HashMap<>();
        for (int i = 0; i < groups.size(); i++) {
            // 获取组ID
            String id = groups.getJSONObject(i).getString(Dict.ID);
            // 根据组ID获取该组人数
            Map<String, String> param = new HashMap<>();
            param.put(Dict.ID, id);
            JSONArray childGroupPerson = gitLabUserService.selectGroupId(param);
            // 获取该组当月人均代码行数
            StatisticCodeLine statisticCodeLine = getStatisticCodeLineByMonth(month, childGroupPerson);
            valueMap.put(id, statisticCodeLine.getAverage());
        }
        map.put(month, valueMap);
        res.add(map);
        return res;
    }
    @Override
    public List<StatisticCodeLine> query(StatisticCodeLine statisticCodeLine) {
        return statisticsDao.query(statisticCodeLine);
    }

    /**
     * 判断用户在当前月份是否在职, false：离职， true： 在职
     */
    public boolean isAlive(String month, JSONObject jsonObject) throws Exception {
        if (StringUtils.isBlank(jsonObject.getString(Dict.CREATE_DATE))) {
            return false;
        }
        // 此月第一天
        Date beginDate = Util.simpleDateFormat("yyyy-MM-dd").parse(month + "-01");
        // 此月最后一天
        Date endDate = Util.getLastDayOfMonth(beginDate);
        Date createDate = Util.simpleDateFormat("yyyy-MM-dd").parse(jsonObject.getString(Dict.CREATE_DATE));
        // 1、入职时间大于月末最后一天（入职时间：2020-01-01，月末：2019-12-30）
        if (createDate.compareTo(endDate) > 0) {
            return false;
        }
        // 2、入职时间小于月末最后一天且离职时间为空
        if (StringUtils.isBlank(jsonObject.getString(Dict.LEAVE_DATE))) {
            return true;
        }
        // 3、离职时间小于月初第一天（离职时间2019-10-31，月初：2019-12-01）
        Date leaveDate = Util.simpleDateFormat("yyyy-MM-dd").parse(jsonObject.getString(Dict.LEAVE_DATE));
        if (leaveDate.compareTo(beginDate) < 0) {
            return false;
        }
        return true;
    }

    /**
     * 获取当前月份代码统计情况
     */
    @Override
    public StatisticCodeLine getStatisticCodeLineByMonth(String month, JSONArray jsonArray) throws Exception {
        StatisticCodeLine statisticCodeLine = new StatisticCodeLine();
        // 在职人员总数
        int aliveCount = 0;
        int codeLineTotal = 0;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            // 是否在职
            if (isAlive(month, jsonObject)) {
                // 获取单人的当月代码统计行数并累加
                codeLineTotal += getCommitTotal(month, jsonObject.getString(Dict.USER_NAME_EN));
                aliveCount++;
            }
        }
        double average = 0;
        if (aliveCount != 0) {
            average = (double) codeLineTotal / aliveCount;
        }
        statisticCodeLine.setPersonTotal(aliveCount);
        statisticCodeLine.setMonth(month);
        statisticCodeLine.setCodeLineTotal(codeLineTotal);
        statisticCodeLine.setAverage(average);
        logger.info("月份：{}，在职人员数目：{}，当月代码总行数：{}，人均代码行数：{}", month, aliveCount, codeLineTotal, average);
        return statisticCodeLine;
    }

    /**
     * 根据名称获取当月的代码总行数
     */
    public Integer getCommitTotal(String month, String userName) throws Exception {
        String beginDate = month + "-01";
        Date date = Util.getLastDayOfMonth(Util.simpleDateFormat("yyyy-MM-dd").parse(beginDate));
        String endDate = Util.simpleDateFormat("yyyy-MM-dd").format(date);
        List<GitlabWork> list = gitlabWorkDao.select1(userName, beginDate, endDate);
        int codeLineTotal = 0;
        for (GitlabWork gitlabWork : list) {
            codeLineTotal += gitlabWork.getTotal();
        }
        return codeLineTotal;
    }

    /**
     * 统计当前月当前组的在职人均代码行数
     */
    public List<Map<String, Double>> statistic(JSONArray jsonArray, List<String> months) throws Exception {
        List<Map<String, Double>> statisticCodeLineList = new ArrayList<>();
        for (String month : months) {
            StatisticCodeLine statisticCodeLine = getStatisticCodeLineByMonth(month, jsonArray);
            Map<String, Double> map = new HashMap<>();
            map.put(statisticCodeLine.getMonth(), statisticCodeLine.getAverage());
            statisticCodeLineList.add(map);
        }
        return statisticCodeLineList;
    }

    @Override
    public void upset(StatisticCodeLine statisticCodeLine) throws Exception {
        StatisticCodeLine tmp = new StatisticCodeLine();
        tmp.setMonth(statisticCodeLine.getMonth());
        List<StatisticCodeLine> list = this.query(tmp);
        if (list.isEmpty()) {
            statisticsDao.save(statisticCodeLine);
        } else {
            statisticsDao.update(statisticCodeLine);
        }
    }

    @Override
    public List<StatisticCodeLine> queryByDate(String startDate, String endDate) {
        return statisticsDao.queryByDate(startDate, endDate);
    }
}
