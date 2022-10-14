package com.spdb.fdev.fdevapp.spdb.schedule.service;

import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.entity.AppVipChannel;
import com.spdb.fdev.fdevapp.spdb.service.IVipChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * ClearPipelineScheduleTasks
 *
 * @blame Android Team
 */
@Component
public class ClearPipelineScheduleTasks implements Runnable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IVipChannelService vipChannelService;

    /**
     * 清除超过半个小时的pipeline还处于running、pending的记录，改为failed和skipped
     */
    @Override
    public void run() {
        //获取当前时间戳
        long currentTimestamp = Calendar.getInstance().getTimeInMillis();
        //查询running和pending的记录
        List<AppVipChannel> channels = this.vipChannelService.getPipelineByTowStatus(Dict.PENDING, Dict.RUNNING);
        for (AppVipChannel channel : channels) {
            // 遍历job，找到当前状态为running或者pending的job
            String status = channel.getStatus();
            Long triggerTime = channel.getTrigger_time();
            List<Map<String, Object>> jobs = channel.getJobs();
            boolean flag = false;
            for (Map<String, Object> job : jobs) {
                if (status.equals(job.get(Dict.STATUS))) {
                    flag = true;
                    // 判断job触发时间是否在半个小时以前，半个小时的时间戳为1800*1000
                    Object timeObj = job.get(Dict.TRIGGER_TIME);
                    if ((Long) timeObj != 0L && currentTimestamp - (Long) timeObj > 1800 * 1000) {
                        String stage = (String) job.get(Dict.STAGES);
                        // 修改当前的job为failed，后面的job全部修改为skipped
                        this.vipChannelService.updateJobStatus(Dict.FAILED, triggerTime, stage);
                        logger.info("触发时间为{}的{}超时!", triggerTime, stage);
                        break;
                    }
                }
            }
            // 遍历job，若没有找到状态为running或者pending的job
            if (!flag) {
                for (Map<String, Object> job : jobs) {
                    String jobStatus = (String) job.get(Dict.STATUS);
                    // 若status状态为pending，所有的job应该都为created
                    if (Dict.PENDING.equals(status) && Dict.CREATED.equals(jobStatus)) {
                        //取上一次的时间
                        if (currentTimestamp - triggerTime > 1800 * 1000){
                            String stage = (String) job.get(Dict.STAGES);
                            this.vipChannelService.updateJobStatus(Dict.FAILED, triggerTime, stage);
                            logger.info("触发时间为{}的{}超时!", triggerTime, stage);
                            break;
                        }
                    }
                    if (Dict.RUNNING.equals(status) && Dict.PENDING.equals(jobStatus)) {
                        Object timeObj = job.get(Dict.TRIGGER_TIME);
                        if ((Long) timeObj != 0L && currentTimestamp - (Long) timeObj > 1800 * 1000){
                            String stage = (String) job.get(Dict.STAGES);
                            this.vipChannelService.updateJobStatus(Dict.FAILED, triggerTime, stage);
                            logger.info("触发时间为{}的{}超时!", triggerTime, stage);
                            break;
                        }
                    }
                }
            }
        }
    }
}
