package com.spdb.fdev.pipeline.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.MinioUtils;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.pipeline.dao.IRunnerClusterDao;
import com.spdb.fdev.pipeline.dao.IRunnerInfoDao;
import com.spdb.fdev.pipeline.dao.impl.RunnerInfoDaoImpl;
import com.spdb.fdev.pipeline.entity.RunnerCluster;
import com.spdb.fdev.pipeline.entity.RunnerInfo;
import com.spdb.fdev.pipeline.service.IRunnerInfoService;
import io.minio.ObjectStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author: c-jiangjk
 * @Date: 2021/4/2 14:57
 */
@Service
public class RunnerInfoServiceImpl implements IRunnerInfoService {

    @Autowired
    IRunnerInfoDao runnerInfoDao;

    @Autowired
    IRunnerClusterDao runnerClusterDao;

    @Autowired
    private MinioUtils minioUtils;

    private static final Logger logger = LoggerFactory.getLogger(RunnerInfoServiceImpl.class);

    /*
     *
     * 检查Runner是否已经注册，
     * */
    @Override
    public boolean checkRunner(RunnerInfo runnerInfo) throws Exception {
        RunnerInfo info = runnerInfoDao.query(runnerInfo);
        if (CommonUtils.isNullOrEmpty(info)) {
            return false;
        }
        return true;
    }


    @Override
    @RequestValidate(NotEmptyFields = {Dict.NAME, Dict.VERSION, Dict.EXECUTOR, Dict.PLATFORM})
    public String generateToken(Map<String, Object> map) throws Exception {
        String token = uuidToToken();
        RunnerInfo runnerInfo = CommonUtils.map2Object(map, RunnerInfo.class);
        //以name唯一标识查询
        RunnerInfo tmp = this.runnerInfoDao.query(runnerInfo);
        if (CommonUtils.isNullOrEmpty(tmp)){
            runnerInfo.setToken(token);
            logger.info(">>>>>>>>> save runnerInfo:" + JSONObject.toJSONString(runnerInfo));
            this.runnerInfoDao.saveRunner(runnerInfo);
        }else {
            tmp.setToken(token);
            //更新成新生成的token
            logger.info(">>>>>>>>>> this is token is:" + runnerInfo.getToken());
            logger.info(">>>>>>>>>> this runnerInfo is exists! <<<<<<<<<<");
            this.runnerInfoDao.updateRunner(tmp);
        }
        return token;
    }

    private static String uuidToToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public List<RunnerInfo> getAllRunnerInfo(Map<String, Object> param) {
        List<RunnerInfo> runnerInfos = this.runnerInfoDao.queryAllRunnerInfo(param);
        List<String> runnerIds = runnerInfos.stream().map(e -> e.getRunnerId()).collect(Collectors.toList());
        //判断runner是否在集群中被使用过
        for (String runnerId : runnerIds) {
            //通过runnerId查询使用被使用过
            List<RunnerCluster> runnerClusters = runnerClusterDao.queryRunnerClusterByRunnerId(runnerId);
            if (!CommonUtils.isNullOrEmpty(runnerClusters)){
                runnerInfos  = runnerInfos.stream().filter(e ->!runnerId.equals(e.getRunnerId())).collect(Collectors.toList());
            }
        }
        return runnerInfos;
    }

    @Override
    public RunnerInfo getRunnerInfoById(String id) {
        RunnerInfo runnerInfo = this.runnerInfoDao.queryById(id);
        runnerInfo.setToken("");
        return runnerInfo;
    }

    @Override
    public RunnerInfo getRunnerInfoByToken(String token) throws Exception {
        RunnerInfo queryRunner = new RunnerInfo();
        queryRunner.setToken(token);
        return this.runnerInfoDao.query(queryRunner);
    }

    @Override
    public Map getMinioMd5(Map<String, Object> param) throws Exception {
        String bucket = (String) param.get("bucket");
        String object = (String) param.get("object");
        Map result = new HashMap();
        result.put("md5", minioUtils.getMd5(bucket, object));
        return result;
    }
}
