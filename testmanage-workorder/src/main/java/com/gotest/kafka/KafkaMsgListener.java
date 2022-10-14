package com.gotest.kafka;

import com.alibaba.fastjson.JSON;
import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.service.RequireService;
import com.gotest.service.WorkOrderService;
import com.gotest.utils.MyUtil;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Component
@RestController
public class KafkaMsgListener {
    private static Logger logger = LoggerFactory.getLogger(KafkaMsgListener.class);// 日志打印

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private RequireService requireService;
    @Autowired
    private MyUtil myUtil;




    /**
     * 创建工单
     * @param consumerRecord
     * @return
     */
    @KafkaListener(topics = {"${kafka.createWorkOrder}"}, groupId = "${kafka.listener.group}")
    public JsonResult createWorkOrder(ConsumerRecord<?, ?> consumerRecord) {
            Map parse = (Map) JSON.parse((byte[]) consumerRecord.value());
            logger.info("Consume topic createWorkOrder :" + JSON.toJSONString(parse));
            try {
                String workOrder = workOrderService.createWorkOrder(parse);
                if (Util.isNullOrEmpty(workOrder)) {
                    throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR, new String[]{"创建实施单元为" + (String) parse.get(Dict.UNITNO) + "工单失败！"});
                }
                parse.put(Dict.WORKORDERNO, workOrder);
            } catch (Exception e) {
                logger.error("fail to create workOrder" + e.toString());
                throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR, new String[]{"创建实施单元为" + (String) parse.get(Dict.UNITNO) + "工单失败！"});
            }
            return JsonResultUtil.buildSuccess(parse);
    }

    /**
     * 创建任务
     * @param consumerRecord
     * @return
     */
    @KafkaListener(topics = {"${kafka.addRequireImplementNo}"}, groupId = "${kafka.listener.group}")
    public JsonResult addRequireImplementNo(ConsumerRecord<?, ?> consumerRecord) {
            Map parse = (Map) JSON.parse((byte[]) consumerRecord.value());
            logger.info("Consume topic addRequireImplementNo :" + JSON.toJSONString(parse));
            try {
                parse.put(Dict.WORKORDERNO, requireService.createFdevOrder(parse));
            } catch (Exception e) {
                logger.error("fail to create task into order" + e.toString());
                throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR, new String[]{"添加任务到工单列表失败！"});
            }
            return JsonResultUtil.buildSuccess(parse);
    }

    /**
     * 修改实施单元
     * @param consumerRecord
     * @return
     */
    @KafkaListener(topics = {"${kafka.updateUnitNo}"}, groupId = "${kafka.listener.group}")
    public JsonResult updateUnitNo(ConsumerRecord<?, ?> consumerRecord) {
            Map parse = (Map) JSON.parse((byte[]) consumerRecord.value());
            logger.info("Consume topic updateUnitNo :" + JSON.toJSONString(parse));
            Integer count = null;
            try {
                count = requireService.updateUnitNo(parse);
            } catch (Exception e) {
                logger.error("fail to change unit of task" + e.toString());
                throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{e.getMessage()});
            }
            if (count == 1) {
                return JsonResultUtil.buildSuccess("修改任务对应的实施单元成功！");
            }
            return JsonResultUtil.buildError(Constants.I_FAILED, "修改任务对应的实施单元失败！");

    }

    /**
     * 删除任务
     * @param consumerRecord
     * @return
     */
    @KafkaListener(topics = {"${kafka.deleteOrder}"}, groupId = "${kafka.listener.group}")
    public JsonResult deleteOrder(ConsumerRecord<?, ?> consumerRecord) {
            Map parse = (Map) JSON.parse((byte[]) consumerRecord.value());
            logger.info("Consume topic deleteOrder :" + JSON.toJSONString(parse));
            Integer dropTask;
            try {
                dropTask = requireService.deleteOrder(parse);
                if (Util.isNullOrEmpty(dropTask) || dropTask == 0) {
                    return JsonResultUtil.buildSuccess("删除任务失败");
                }
            } catch (Exception e) {
                logger.error("fail to delete task in order" + e.toString());
                throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"删除任务失败"});
            }
            return JsonResultUtil.buildSuccess("删除任务成功");
    }

    /*@RequestMapping("/sayKafka")
    public JsonResult sayKafka(@RequestBody Map map) throws Exception {
        ListenableFuture<SendResult<String, String>> future =
                kafkaTemplate.send("kafka.say", 0, "key", JSON.toJSONString(map));
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                logger.error("fail to send kafaka.say to kafka");
            }

            @Override
            public void onSuccess(SendResult<String, String> stringStringSendResult) {
                logger.info("send kafka.say succeed");
            }
        });
        return JsonResultUtil.buildSuccess();
    }*/


}
