package com.spdb.fdev.pipeline.schedule;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.entity.Pipeline;
import com.spdb.fdev.pipeline.entity.PipelineCronBatch;
import com.spdb.fdev.pipeline.entity.Schedule;
import com.spdb.fdev.pipeline.service.IPipelineCronService;
import com.spdb.fdev.pipeline.service.IPipelineExeService;
import com.spdb.fdev.pipeline.service.IPipelineService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ScheduleTask implements ScheduleTaskService{

    private static final Logger log = LoggerFactory.getLogger(ScheduleTask.class);

    @Autowired
    private IPipelineService pipelineService;

    @Autowired
    private IPipelineExeService pipelineExeService;

    @Autowired
    private IPipelineCronService pipelineCronService;


//    @Scheduled(cron = pipelineCron)
//    @Override
//    public void run() {
//
//    }

    /**
     * cron = 秒 分 时 日 月 星期中的日期
     * 每五秒执行一次
     */
    @Override
    public void scheduleExecutePipeline() {
        log.info(">>>>>>>>>>>  schedule task start <<<<<<<<<<<");
        String lastBatchNo = null;
        String currentBatchNo = CommonUtils.getCurrentBatchNo();
        //查询上一次的批次号，若第一次执行便没有批次号，默认取当前时间;插入一条批次号
        PipelineCronBatch lastBatch = this.pipelineCronService.getLastBatch();
        int count = 0;      //记录当前批次号所跑的流水线
        String cronId = "";
        if (CommonUtils.isNullOrEmpty(lastBatch)) {
            //若lastBatch为空，即为第一次执行的时候
            lastBatchNo = currentBatchNo;
        } else {
            lastBatchNo = lastBatch.getBatchNo();
        }
        //插入一条记录
        cronId = insertCronBatch(currentBatchNo, Dict.RUNNING, "", count);
        List<Pipeline> schedulePipelines = this.pipelineService.getSchedulePipelines();
        for (Pipeline schedulePipeline : schedulePipelines) {
            Schedule schedule = schedulePipeline.getTriggerRules().getSchedule();
            List<Map> scheduleParams = schedule.getScheduleParams();
            if (!CommonUtils.isNullOrEmpty(scheduleParams)) {
                for (Map scheduleParam : scheduleParams) {
                    String cron = (String) scheduleParam.get(Dict.CRON);
                    String branchType = (String) scheduleParam.get(Dict.BRANCHTYPE);
                    String branchName = (String) scheduleParam.get(Dict.BRANCHNAME);
                    try {
                        if (isCurrentCron(cron, lastBatchNo, currentBatchNo)) {
                            try {
                                this.pipelineService.triggerPipeline(schedulePipeline.getId(), branchName, branchType.equals(Dict.TAG) ? true : false, Dict.SCHEDULE,null,null, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            count++;
                            log.info(">>>>>>>>> pipelineId: " + schedulePipeline.getId() + " <<<<<<<<<<<<<<");
                            log.info(">>>>>>>>> pipeline start pending <<<<<<<<<");
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //批次更新完成 即修改状态为success
        insertCronBatch(currentBatchNo, Dict.SUCCESS, cronId, count);
        log.info(">>>>>>>>>>>  schedule task end <<<<<<<<<<<");
    }

    /**
     * 校验是否是当前的cron
     *
     * @param cron
     * @return
     */
    private Boolean isCurrentCron(String cron, String lastBatchNo, String currentBatchNo) throws ParseException {
        CronSequenceGenerator cronSequenceGenerator = null;
        try {
            cronSequenceGenerator = new CronSequenceGenerator(cron);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(">>>>>>>  cron error, cron:" + cron + "   . return false ");
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.RELEASEDATE);
        Date lastDate = sdf.parse(lastBatchNo);
        Date currentDate = sdf.parse(currentBatchNo);
        Date nextCronDate = cronSequenceGenerator.next(lastDate);
        log.info(">>>>>>>>>> nextCronDate:" + nextCronDate.getTime() + "  lastDate:" + lastDate.getTime() + "  currentDate:" + currentDate.getTime() + "  <<<<<<<<<<<");
        //当上一个批次号时间 < cron时间 <= 当前时间批次号
        if (nextCronDate.getTime() > lastDate.getTime() && nextCronDate.getTime() <= currentDate.getTime()) {
            return true;
        } else
            return false;
    }


    /**
     * 插入批次号
     */
    private String insertCronBatch(String currentBatchNo, String status, String cronId, int count) {
        PipelineCronBatch pipelineCronBatch = new PipelineCronBatch();
        pipelineCronBatch.setBatchNo(currentBatchNo);
        pipelineCronBatch.setBatchStatus(status);
        pipelineCronBatch.setCount(count);
        if (CommonUtils.isNullOrEmpty(cronId)) {
            pipelineCronBatch.setCronId(new ObjectId().toString());
        }else
            pipelineCronBatch.setCronId(cronId);
        this.pipelineCronService.upsertCronBatch(pipelineCronBatch);
        return pipelineCronBatch.getCronId();
    }
}
