package com.spdb.fdev.fdevapp.spdb.dao;

import com.spdb.fdev.fdevapp.spdb.entity.AppVipChannel;
import java.util.List;
import java.util.Map;

public interface IVipChannelDao {


    /**
     * 保存channel记录
     *
     * @param appVipChannel
     */
    void saveVipChannel(AppVipChannel appVipChannel);

    /**
     * 查询channel记录
     *
     * @param appVipChannel
     * @return
     */
    List<AppVipChannel> queryVipChannelByParams(AppVipChannel appVipChannel);

    /**
     * 根据id来获取得到单条pipeline信息，即vipchannel信息
     *
     * @param id
     * @return
     */
    AppVipChannel queryPipelineById(String id);

    /**
     * 更新job的状态
     *
     * @param status
     * @param timestamp
     * @param stage
     */
    void updateJobStatus(String status, Long timestamp, String stage);

    /**
     * 更新job的minioUrl
     *
     * @param minioUrl
     * @param timestamp 作为更新的唯一标识
     * @param stages    作为表示那个一个job需要更新的标识
     */
    void updateMinioUrl(String minioUrl, Long timestamp, String stages);

    /**
     * 根据timestamp更新整条记录的status
     *
     * @param status
     * @param timestamp
     */
    void updateStatus(String status, Long timestamp);

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
     * 因取消pipeline而需要更新状态
     *
     * @return
     */
    AppVipChannel updateStatusForCancel(String id);


    /**
     * 分页查询channel
     *
     * @param params
     * @return
     */
    Map queryChannelListByPagination(Map params);

    /**
     * 根据条件查询获取得到minio_url对应的log，并返回
     *
     * @param id
     * @param stage
     * @return
     */
    String queryMinioUrl(String id, String stage);

    /**
     * 返回 status1 or status2 的记录
     *
     * @param status1
     * @param status2
     * @return
     */
    List<AppVipChannel> queryPipelineByTowStatus(String status1, String status2);

    /**
     * 校验当前job的状态是status
     *
     * @param status        需要校验的status
     * @param timestamp
     * @param stage
     * @return
     */
    Boolean checkedStatus(String status, Long timestamp, String stage);
}
