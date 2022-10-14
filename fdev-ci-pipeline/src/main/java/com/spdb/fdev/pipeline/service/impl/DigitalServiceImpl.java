package com.spdb.fdev.pipeline.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.DateUtils;
import com.spdb.fdev.pipeline.dao.IDigitalRateDao;
import com.spdb.fdev.pipeline.dao.IPipelineDao;
import com.spdb.fdev.pipeline.dao.IPipelineExeDao;
import com.spdb.fdev.pipeline.entity.PipelineDigitalRate;
import com.spdb.fdev.pipeline.entity.PipelineExe;
import com.spdb.fdev.pipeline.service.IAppService;
import com.spdb.fdev.pipeline.service.IDigitalService;
import com.spdb.fdev.pipeline.service.IUserService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DigitalServiceImpl implements IDigitalService {

    private static final Logger logger = LoggerFactory.getLogger(DigitalServiceImpl.class);

    @Autowired
    private IDigitalRateDao digitalRateDao;

    @Autowired
    private IPipelineExeDao pipelineExeDao;

    @Autowired
    private IPipelineDao pipelineDao;

    @Autowired
    private IAppService appService;

    @Autowired
    private IUserService userService;

    @Override
    public PipelineDigitalRate getDigitalRateByNameId(String nameId) {
        return digitalRateDao.queryByNameId(nameId);
    }


    /**
     * upsert 流水线的 exeTotal 流水线构建总数，successExeTotal 成功构建总数，errorExeTotal 失败构建总数，successRate 成功率，aveErrorTime 失败平均修复时长 几个数据
     *
     * @param pipelineNameId
     * @throws Exception
     */
    @Override
    @Async
    public void calculateDigital(String pipelineNameId) throws Exception {
        //成功构建数
        Double successExeTotal = 0D;
        //失败构建数
        Double errorExeTotal = 0D;
        List<PipelineExe> pipelineExes = this.pipelineExeDao.queryExeByPipelineNameId(pipelineNameId);
        //构建总数
        Double exeTotal = Double.valueOf(pipelineExes.size());
        for (PipelineExe pipelineExe : pipelineExes) {
            String status = pipelineExe.getStatus();
            if (Dict.SUCCESS.equals(status)) {
                successExeTotal = successExeTotal + 1;
            } else if (Dict.ERROR.equals(status)) {
                errorExeTotal = errorExeTotal + 1;
            }
        }
        //成功构建率
        Double successRate = successExeTotal / exeTotal;
        String successRatestr = successRate == 0D ? "0.00%" : makeDouble2XX(successRate * 100D) + "%";
        //失败平均修复时长
        Double aveErrorTime = 0D;
        aveErrorTime = handleRetoreTime(pipelineExes);
        aveErrorTime = Double.valueOf(makeDouble2XX(aveErrorTime));
        Map upsertMap = new HashMap();
        upsertMap.put(Dict.SUCCESSEXETOTAL, successExeTotal);
        upsertMap.put(Dict.ERROREXETOTAL, errorExeTotal);
        upsertMap.put(Dict.EXETOTAL, exeTotal);
        upsertMap.put(Dict.SUCCESSRATE, successRatestr);
        upsertMap.put(Dict.AVEERRORTIME, aveErrorTime);
        upsertMap.put(Dict.NAMEID, pipelineNameId);

        logger.info(" ***** upsert map is " + JSONObject.toJSONString(upsertMap));
        PipelineDigitalRate upsertEnity = CommonUtils.map2Object(upsertMap, PipelineDigitalRate.class);
        logger.info(" ***** upsert entity is " + JSONObject.toJSONString(upsertEnity));
        this.digitalRateDao.upsert(upsertEnity);
    }

    /**
     * 处理double 保留两位小数
     *
     * @param target
     * @return
     */
    private String makeDouble2XX(Double target) {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(target);
    }

    private Double handleRetoreTime(List<PipelineExe> exes) {
        Double time = 0d;
        if (exes.size() > 1) {
            boolean flag = false;
            String firstFailendTime = null;
            String secondSuccessEndTime = null;
            long totalCount = 0;
            long totalTime = 0;
            for (PipelineExe exe : exes) {
                if (!flag && Dict.ERROR.equals(exe.getStatus())) {
                    flag = true;
                    firstFailendTime = (String) exe.getEndTime();
                }
                if (flag && Dict.SUCCESS.equals(exe.getStatus())) {
                    flag = false;
                    secondSuccessEndTime = (String) exe.getEndTime();
                    if (!CommonUtils.isNullOrEmpty(firstFailendTime) && !CommonUtils.isNullOrEmpty(secondSuccessEndTime)) {
                        try {
                            Date dateFirstFailendTime = DateUtils.stringToDateTime(firstFailendTime);
                            Date dateSecondSuccessEndTime = DateUtils.stringToDateTime(secondSuccessEndTime);
                            long cost = dateSecondSuccessEndTime.getTime() - dateFirstFailendTime.getTime();
                            totalCount++;
                            totalTime = totalTime + cost;
                        } catch (Exception e) {
                        }

                    }
                }
            }
            if (totalCount != 0 && totalTime != 0) {
                time = Double.valueOf((totalTime / totalCount) / 3600000);
            }
        }
        return time;
    }
}
