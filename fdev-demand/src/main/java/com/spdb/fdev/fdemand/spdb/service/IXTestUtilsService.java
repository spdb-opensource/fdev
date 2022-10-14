package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.TestOrderFile;

import java.util.List;
import java.util.Map;

public interface IXTestUtilsService {
    /**
     * 提交提测单
     *
     * @param params
     * @return
     * @throws Exception
     */
    void submitTestOrder(Map<String, Object> params) throws Exception ;
    /**
     * 编辑提测单
     *
     * @param params
     * @return
     * @throws Exception
     */
    void updateTestOrder(Map<String, Object> params) throws Exception ;
    /**
     * 提测单状态修改
     *
     * @param params
     * @return
     * @throws Exception
     */
    void modifyTestOrderStatus(Map<String, Object> params) throws Exception ;
    /**
     * 查询测试人员信息
     *
     * @param params
     * @return
     * @throws Exception
     */
    List<Map> getTestManagerInfo(Map<String, Object> params) throws Exception;

    /**
     * 附件上传
     *
     * @param files
     * @return
     * @throws Exception
     */
    void importTestOrderFile(List<TestOrderFile> files) throws Exception ;

    /**
     * 附件删除
     *
     * @param testOrderFileIds
     * @return
     * @throws Exception
     */
    void delTestOrderFile(List<String> testOrderFileIds) throws Exception ;
    /**
     * 获取科技需求测试信息
     *
     * @param demandIds
     * @return
     * @throws Exception
     */
    List<Map> getTechReqTestInfo(List<String> demandIds) throws Exception;
}
