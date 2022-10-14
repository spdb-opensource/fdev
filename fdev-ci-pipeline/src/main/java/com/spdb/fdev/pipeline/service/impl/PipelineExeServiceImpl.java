package com.spdb.fdev.pipeline.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.pipeline.dao.IPipelineDao;
import com.spdb.fdev.pipeline.dao.IPipelineExeDao;
import com.spdb.fdev.pipeline.entity.Pipeline;
import com.spdb.fdev.pipeline.entity.PipelineExe;
import com.spdb.fdev.pipeline.service.IPipelineExeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PipelineExeServiceImpl implements IPipelineExeService {
    @Autowired
    IPipelineExeDao pipelineExeDao;

    @Autowired
    private IPipelineDao pipelineDao;

    @Override
    public PipelineExe savePipelineExe(PipelineExe pipelineExe) throws Exception {
        return pipelineExeDao.savePipelineExe(pipelineExe);
    }

    @Override
    public Map<String, Object> queryListByPipelineIdSort(String pipelineId, String pageNum, String pageSize) {
        long skip = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageSize);
        int limit = Integer.valueOf(pageSize);
        Pipeline pipeline = this.pipelineDao.queryById(pipelineId);
        String nameId = pipeline.getNameId();
        return pipelineExeDao.queryListByPipelineIdSort(pipelineId, skip, limit, nameId);
    }

    @Override
    public Map<String, Object> queryListRegexSort(String commitId,String branch,String searchContent, String pageNum, String pageSize) {
        long skip = (Integer.valueOf(pageNum) - 1) * Integer.valueOf(pageSize);
        int limit = Integer.valueOf(pageSize);
        return pipelineExeDao.queryListRegexSort(commitId,branch,searchContent,skip,limit);
    }

    @Override
    public void save(PipelineExe pi) {
        pipelineExeDao.save(pi);
    }



    @Override
    public PipelineExe queryPipelineExeByExeId(String id) throws Exception {
        return pipelineExeDao.queryPipelineExeByExeId(id);
    }

    @Override
    public void updateStagesAndStatus(PipelineExe pipelineExe) throws Exception {
        pipelineExeDao.updateStagesAndStatus(pipelineExe);
    }

    @Override
    public void updateArtifacts(PipelineExe pipelineExe) throws Exception {
        pipelineExeDao.updateArtifacts(pipelineExe);
    }

    @Override
    public void updateStagesAndStatusAndUser(PipelineExe pipelineExe) throws Exception {
        pipelineExeDao.updateStagesAndStatusAndUser(pipelineExe);
    }

    /**
     * 根据stage中的job最新状态，获取stage最新的状态,重要逻辑，看不懂就不要动！！！
     * @param pipelineExe
     * @param stageIndex
     * @return status
     */
    @Override
    public String getStageFinalStatus(PipelineExe pipelineExe, Integer stageIndex) {
        String stageStatus = Dict.WAITING;
        Map resMap = new HashMap();
        Map stageMap = pipelineExe.getStages().get(stageIndex);
        List<Map> stageJobs = (List<Map>) stageMap.get(Dict.JOBS);
        for (Map job : stageJobs) {
            List<Map> jobExeList = (List<Map>) job.get(Dict.JOBEXES);
            Map exeMap = jobExeList.get(jobExeList.size() - 1);
            String exeStatus = (String) exeMap.get(Dict.JOBEXESTATUS);
            resMap.put(exeStatus,true);
        }
        if(resMap.containsKey(Dict.RUNNING)){
            stageStatus = Dict.RUNNING;
        }else if(resMap.containsKey(Dict.CANCEL)){
            stageStatus = Dict.CANCEL;
        }else if(resMap.containsKey(Dict.ERROR)){
            stageStatus = Dict.ERROR;
        }else if(resMap.containsKey(Dict.SUCCESS)){
            stageStatus = Dict.SUCCESS;
        }else if(resMap.containsKey(Dict.PENDING)){
            stageStatus = Dict.PENDING;
        }

        if(!Dict.PENDING.equals(stageStatus) && resMap.containsKey(Dict.PENDING)){
            stageStatus = Dict.RUNNING;
        }

        return stageStatus;
    }

    @Override
    public List<PipelineExe> queryExeByPipeLineNumberOrCommitId(String pipleNumber, String commitId) {
        return pipelineExeDao.queryExeByPipeLineNumberOrCommitId(pipleNumber,commitId);
    }

    /**
     * 根据每个job的耗时统计整条流水线的耗时
     * @param pipelineExe
     * @return
     */
    public String calculatePipelineExeCostTime(PipelineExe pipelineExe) throws Exception {
        long costTotal = 0l;
        for (Map stage : pipelineExe.getStages()) {
            List<Map> jobs = (List<Map>)stage.get(Dict.JOBS);
            for (int j = 0; j < jobs.size(); j++) {
                Map job = jobs.get(j);
                List<Map> jobExes = (List<Map>) job.get(Dict.JOBEXES);
                //获取最后一个jobexeMap
                Map jobExeMap = jobExes.get(jobExes.size() - 1);
                String startTime = (String)jobExeMap.get(Dict.JOBSTARTTIME);
                String endTime = (String)jobExeMap.get(Dict.JOBENDTIME);
                if(!CommonUtils.isNullOrEmpty(startTime) && !CommonUtils.isNullOrEmpty(endTime)){
                    long cost = CommonUtils.getCostTime(startTime, endTime, CommonUtils.STANDARDDATEPATTERN);
                    costTotal += cost;
                }
            }
        }
        return CommonUtils.longTimeToDate(costTotal);
    }

}
