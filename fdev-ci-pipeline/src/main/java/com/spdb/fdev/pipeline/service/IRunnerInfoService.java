package com.spdb.fdev.pipeline.service;

import com.spdb.fdev.pipeline.entity.RunnerInfo;
import io.minio.ObjectStat;

import java.util.List;
import java.util.Map;

/**
 * @Author: c-jiangjk
 * @Date: 2021/4/2 14:55
 */
public interface IRunnerInfoService {

   public boolean checkRunner(RunnerInfo runnerInfo) throws Exception;

   public String generateToken(Map<String, Object> map) throws Exception;

   List<RunnerInfo> getAllRunnerInfo(Map<String, Object> param);

   RunnerInfo getRunnerInfoById(String id);

   RunnerInfo getRunnerInfoByToken(String token) throws Exception;

   Map getMinioMd5(Map<String, Object> param) throws Exception;
}
