package com.spdb.fdev.pipeline.dao;

import com.spdb.fdev.pipeline.entity.RunnerInfo;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @Author: c-jiangjk
 * @Date: 2021/4/2 14:29
 */
public interface IRunnerInfoDao {
    /*
    * 查找runner信息
    * */
    public RunnerInfo query (RunnerInfo runnerInfo) throws Exception;

    /*
    * 保存runner信息
    * */
    public void saveRunner(RunnerInfo runnerInfo) throws Exception;

    /**
     * 查询所有的runner信息
     * @return
     */
    List<RunnerInfo> queryAllRunnerInfo(Map<String, Object> param);

    void updateRunner(RunnerInfo runnerInfo) throws ParseException;

    RunnerInfo queryById(String id);
}
