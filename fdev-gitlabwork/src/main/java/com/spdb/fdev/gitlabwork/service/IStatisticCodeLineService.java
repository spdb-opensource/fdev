package com.spdb.fdev.gitlabwork.service;

import com.alibaba.fastjson.JSONArray;
import com.spdb.fdev.gitlabwork.entiy.StatisticCodeLine;

import java.util.List;
import java.util.Map;

/**
 * @Author: c-jiangjk
 * @Date: 2021/3/1 15:33
 */
public interface IStatisticCodeLineService {

    List<Map<String, Object>> query() throws Exception;

    List<Map<String, Object>> query(String month) throws Exception;

    List<StatisticCodeLine> query(StatisticCodeLine statisticCodeLine);

    void upset(StatisticCodeLine statisticCodeLine) throws Exception;

    List<StatisticCodeLine> queryByDate(String startDate, String endDate);

    StatisticCodeLine getStatisticCodeLineByMonth(String month, JSONArray jsonArray) throws Exception;
}
