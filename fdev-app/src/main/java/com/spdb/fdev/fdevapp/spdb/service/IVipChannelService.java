package com.spdb.fdev.fdevapp.spdb.service;

import com.spdb.fdev.fdevapp.spdb.entity.AppVipChannel;
import java.util.List;
import java.util.Map;

public interface IVipChannelService {


    /**
     * 分析获取得到channel的信息
     *
     * @param request
     */
    void analysisInfo(Map request) throws Exception;

    /**
     * 根据条件获取channel信息
     *
     * @param appVipChannel
     * @return
     */
    List<AppVipChannel> getVipChannel(AppVipChannel appVipChannel);

    /**
     * 更新channel的job的status信息
     *
     * @param status    更新的状态
     * @param timestamp 作为标识单条记录
     * @param state     用来标识是那个job
     */
    void updateJobStatus(String status, Long timestamp, String state);

    /**
     * 更新整条vipchannel记录的status
     *
     * @param status
     * @param timestamp
     */
    void updateStatus(String status, Long timestamp);

    /**
     * 发送kafka消息
     *
     * @param variables
     * @param image
     * @param timestamp
     * @param stageName
     */
    void sendKafka(Map variables, String image, Long timestamp, String stageName);


    /**
     * 更新单个job的开始时间或结束时间
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param stage     标识那一个job的stage
     * @param timeStamp 标识那一条channel记录
     */
    void updateJobTime(Long startTime, Long endTime, String stage, Long timeStamp);


    /**
     * 取消当前runpipeline，修改状态
     *
     * @return
     */
    AppVipChannel cancelPipelines(String id);

    /**
     * 获取channel的pipeline信息，分页查询
     *
     * @return
     */
    Map getChannelListByPagination(Map params);


    /**
     * 根据条件查询到相应的minio_url对应的log，并获取得到log的content返回
     *
     * @param id
     * @param stage
     * @return
     */
    Map getChannelLogContent(String id, String stage);

    /**
     * 返回status1 or status2 的记录
     *
     * @param status1
     * @param status2
     * @return
     */
    List<AppVipChannel> getPipelineByTowStatus(String status1, String status2);

    /**
     * 更新job的minioUrl
     *
     * @param minioUrl
     * @param timestamp 唯一标识记录
     * @param stage     唯一标识记录的单个job
     */
    void updateJobMinioUrl(String minioUrl, Long timestamp, String stage);

    /**
     * 校验当前的job是否为status状态
     *
     * @param status        需要check的状态
     * @param timestamp
     * @param stage
     * @return
     */
    Boolean checkedStatus(String status, Long timestamp, String stage);

    /**
     * 根据id来获取pipeline信息，即vipchannel信息
     *
     * @param id
     * @return
     */
    AppVipChannel getPipelineById(String id);
}
