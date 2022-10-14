package com.mantis.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ApiService {


    /**
     * 根据多个工单号批量查询所有任务id
     *
     * @param workNos 工单号
     */
    List<String> queryTaskNoByWorkNos(List<String> workNos) throws Exception;

    /**
     * 批量获取任务详情
     *
     * @param TaskNo 任务id
     */
    Map queryInfoByTaskNo(String TaskNo) throws Exception;

    /**
     * 根据单个工单号获取主任务id
     *
     * @param workNo 工单号
     */
    String querymainTaskNoByWorkNo(String workNo) throws Exception;

    /**
     * 根据任务id查fdev重构后任务
     *
     * @param taskNo
     * @return
     * @throws Exception
     */
    Map queryNewTaskInfoByTaskNo(String taskNo) throws Exception;

    /**
     * 根据组id查fdev组详情
     *
     * @param groupId
     * @return
     * @throws Exception
     */
    Map queryFdevGroupInfo(String groupId) throws Exception;

    /**
     * 根据应用id查应用名
     *
     * @param appId
     * @return
     * @throws Exception
     */
    String queryAppNameByAppid(String appId) throws Exception;

    /**
     * 批量查询任务基础信息
     * @param taskIds
     * @return
     */
    List<Map> queryTaskBaseInfoByIds(Set<String> taskIds) throws Exception;

    /**
     * 批量查询工单基础信息
     * @param workNos
     * @param fields
     * @return
     */
    List<Map> queryWorkOrderByNos(Set<String> workNos, List<String> fields);
}
