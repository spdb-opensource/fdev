package com.spdb.fdev.pipeline.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.pipeline.dao.IJobExeDao;
import com.spdb.fdev.pipeline.entity.JobExe;
import com.spdb.fdev.pipeline.service.IPluginResultService;
import com.spdb.fdev.pipeline.transport.RestTransService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PluginResultServiceImpl implements IPluginResultService {

    @Autowired
    private IJobExeDao jobExeDao;

    @Autowired
    private RestTransService restTransService;

    /**
     * 获取返回output，当不需要异步请求cast、接口自动化等结果时直接返回output，当需要异步请求的时候，返回output下的resultData
     *
     * @param pipelineExeId
     * @param stageIndex
     * @param jobIndex
     * @param stepIndex
     * @return
     * @throws Exception
     */
    @Override
    public Object getPluginResultData(String pipelineExeId, Integer stageIndex, Integer jobIndex, Integer stepIndex) throws Exception {
        JobExe jobExe = this.jobExeDao.queryJobExeByIndex(pipelineExeId, stageIndex, jobIndex);
        Map step = jobExe.getSteps().get(stepIndex);
        Map output = (Map) step.get(Dict.OUTPUT);
        Map resultRequest = null;
        if (!CommonUtils.isNullOrEmpty(output))
            resultRequest = (Map) output.get(Dict.RESULTREQUEST);
        Object result = null;
        if (!CommonUtils.isNullOrEmpty(resultRequest)) {
            //当resultRequest不为空时，发送异步请求
            //插件在执行完以后，如果需要异步获取结果，则在output中提供这样的参数
            String requestType = (String) resultRequest.get(Dict.REQUESTTYPE);
            String requestUrl = (String) resultRequest.get(Dict.REQUESTURL);
            Map requestParams = (Map) resultRequest.get(Dict.REQUESTPARAMS);
            //若没有，才会发送蓝鲸请求
            //蓝鲸请求
            result = this.restTransService.restRequestByType(requestParams, requestUrl, requestType);
            result = JSONObject.parseObject((String) result, Map.class);
            if (result != null) {
                resultRequest.put(Dict.RESULTDATA, result);
                //将结果更新入库
                output.put(Dict.RESULTREQUEST, resultRequest);
                this.jobExeDao.updateSteps(jobExe);
                return result;
            }
        }
        //同步的按原来output返回
        return output;
    }
}
