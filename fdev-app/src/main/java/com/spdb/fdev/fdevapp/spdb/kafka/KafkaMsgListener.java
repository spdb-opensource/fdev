package com.spdb.fdev.fdevapp.spdb.kafka;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.fdevapp.base.dict.Constant;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.entity.AppVipChannel;
import com.spdb.fdev.fdevapp.spdb.service.IVipChannelService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RefreshScope
@Component
public class KafkaMsgListener {

    private static Logger logger = LoggerFactory.getLogger(KafkaMsgListener.class);// 控制台日志打印

    @Autowired
    private IVipChannelService vipChannelService;

    /**
     * 监听kafka event
     *
     * @param consumerRecord
     */
    @KafkaListener(topics = {"${kafka.runner.topic}"}, groupId = "fdev-app")
    public void receive(ConsumerRecord<?, ?> consumerRecord) {
        byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        Map<String, Object> parse = JSON.parseObject(params, Map.class);
        String event = (String) parse.get(Dict.EVENT);
        Map metadata = (Map) parse.get(Dict.METADATA);
        Long timestamp = (Long) metadata.get(Constant.VC_TRIGGER_TIME);
        //获取build-state
        String state = (String) metadata.get(Constant.CI_BUILD_STATE);
        //当event为start
        if (event.equals(Dict.START)) {
            //检测要修改状态的记录是否当前状态为pending，只有当前记录的状态为pending的时候才修改状态和发送kafka消息
            if (this.vipChannelService.checkedStatus(Dict.PENDING, timestamp, state)) {
                logger.info("当前job状态为pending");
                //更新job的状态为status running
                this.vipChannelService.updateJobStatus(Dict.RUNNING, timestamp, state);
                //更新状态为running，该状态为整条记录的状态
                this.vipChannelService.updateStatus(Dict.RUNNING, timestamp);
                logger.info("修改当前job状态为running");
            }
            //等待，如果正常结束就到达passed，如果timeout就failed
        } else if (event.equals(Dict.OK)) {
            //检测要修改状态的记录是否当前状态为running，只有当前记录的状态为pending的时候才修改状态和发送kafka消息
            if (this.vipChannelService.checkedStatus(Dict.RUNNING, timestamp, state)) {
                logger.info(parse.toString());
                logger.info(event + "状态kakfa返回的metadata数据" + metadata.toString());
                String minio_url = (String) parse.get(Dict.MINIO_URL);
                //更新job的minio_url地址
                this.vipChannelService.updateJobMinioUrl(minio_url, timestamp, state);
                this.vipChannelService.updateJobStatus(Dict.PASSED, timestamp, state);
                logger.info("修改当前job状态为passed");
                //走完一个job，将当前job的结束时间更新
                this.vipChannelService.updateJobTime(null, new Date().getTime(), state, timestamp);
                this.checkNextStageAndSend(timestamp, state);
            }
        } else if (event.equals(Dict.ERROR)) {
            String minio_url = (String) parse.get(Dict.MINIO_URL);
            this.vipChannelService.updateJobStatus(Dict.FAILED, timestamp, state);
            //更新job的minio_url地址
            this.vipChannelService.updateJobMinioUrl(minio_url, timestamp, state);
            logger.info("修改当前job状态为error");
        }
    }

    /**
     * 发送event修改为passed的之后，检测是否还存在有下一个stage，若存在便继续发送kafka消息执行下一个stage
     *
     * @param timestamp
     * @param state
     */
    private void checkNextStageAndSend(Long timestamp, String state) {
        AppVipChannel paramChannel = new AppVipChannel();
        paramChannel.setTrigger_time(timestamp);
        List<AppVipChannel> vipChannels = this.vipChannelService.getVipChannel(paramChannel);
        AppVipChannel vipChannel = vipChannels.get(0);
        //获取得到当前passed的优先级，查询是否还有下一个优先级
        List<Map<String, Object>> jobs = vipChannel.getJobs();
        Map variables = vipChannel.getVariables();
        Integer nextPriority = 0;
        for (Map job : jobs) {
            String stages = (String) job.get(Dict.STAGES);
            if (stages.equals(state)) {
                Integer priority = (Integer) job.get(Dict.PRIORITY);
                nextPriority = priority + 1;
                break;
            }
        }
        String nextStageName = "";
        String nextImage = "";
        for (Map job : jobs) {
            Integer priority = (Integer) job.get(Dict.PRIORITY);
            if (priority == nextPriority) {
                nextStageName = (String) job.get(Dict.STAGES);
                nextImage = (String) job.get(Dict.IMAGE);
                break;
            }
        }
        //若为空，意义为不存在有下一个stage了，已经跑完了；反之为存在，继续到下一个stage
        if (!nextStageName.isEmpty()) {
            logger.info("向下一个job " + nextStageName + "发起kafka请求");
            this.vipChannelService.sendKafka(variables, nextImage, timestamp, nextStageName);
        } else {
            //修改整个channel的记录为passed
            this.vipChannelService.updateStatus(Dict.PASSED, timestamp);
        }
    }

}
