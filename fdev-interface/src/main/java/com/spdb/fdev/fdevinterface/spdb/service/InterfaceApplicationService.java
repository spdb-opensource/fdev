package com.spdb.fdev.fdevinterface.spdb.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface InterfaceApplicationService {

    /**
     * 初始化数据
     */
    void addRecord();

    /**
     * 分页查询接口申请列表
     * @param map
     * @return
     */
    Map<String,Object> queryApproveList(Map map);

    /**
     * 查询接口申请状态
     * @param transId
     * @param serviceCalling
     * @return
     */
    Map queryStatus(String transId, String serviceCalling,String serviceId);

    /**
     * 添加接口申请记录
     * @param map
     */
    List<String> insertApproveRecord(Map map) throws Exception;

    /**
     * 修改接口申请记录
     * @param map
     */
    void updateApproveStatus(Map map) throws Exception;

    /**
     *
     * @param servicaCalling
     * @param branch
     * @return
     */
    Map isNoApplyInterface(String servicaCalling, String branch) throws Exception;

    /**
     * 判断当前用户是否是应用负责人
     * @return
     */
    boolean isManagers();
    
    /**
     * 发送fdev通知
     * @param content
     * @param type
     * @param target
     * @param hyperLink
     */
	void sendFdevNotify(String content, String type, String[] target, String hyperLink, String desc);
}
