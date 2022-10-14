package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.HistoryDetails;

import java.util.List;
import java.util.Map;

/**
 * create by Idea
 * <p>
 * 记录 实体/实体映射更改操作流水
 *
 * @Author aric
 * @Date 2020/1/10 10:14
 * @Version 1.0
 */
public interface HistoryDetailsService {

    /**
     * 实体属性字段更新（删除、新增）
     */
    String MODEL_UPDATE = "modelUpdate";
    /**
     * 实体删除
     */
    String MODEL_DELETE = "modelDelete";
    /**
     * 实体与环境映射更新
     */
    String MAPPING_UPDATE = "mappingUpdate";
    /**
     * 实体与环境映射删除
     */
    String MAPPING_DELETE = "mappingDelete";

    /**
     * 记录流水
     *
     * @param historyDetails
     * @return
     */
    HistoryDetails add(HistoryDetails historyDetails);

    /**
     * 根据实体id和环境id分页查询实体与环境映射历史修改记录
     *
     * @param requestMap
     * @return
     */
    Map<String, Object> getMappingHistoryList(Map<String, Object> requestMap);

    /**
     * 根据流水id查询映射值修改详情
     *
     * @param requestMap
     * @return
     */
    List<Map<String, Object>> getMappingHistoryDetail(Map<String, Object> requestMap);
}
