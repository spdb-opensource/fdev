package com.spdb.fdev.gitlabwork.dao;

import com.spdb.fdev.gitlabwork.entiy.StatisticCodeLine;

import java.util.List;

/**
 * @Author: c-jiangjk
 * @Date: 2021/3/1 15:26
 */
public interface IStatisticCodeLineDao {

    List<StatisticCodeLine> query(StatisticCodeLine statisticCodeLine);

    void saveAll(List<StatisticCodeLine> statistics);

    void save(StatisticCodeLine statisticCodeLine);

    void update(StatisticCodeLine statisticCodeLine) throws Exception;

    List<StatisticCodeLine> queryByDate(String startDate, String endDate);
}
