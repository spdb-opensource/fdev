package com.mantis.service;

import com.mantis.entity.MantisIssue;

import java.util.List;
import java.util.Map;

public interface NewFdevService {
    List<MantisIssue> queryFuserMantisAll(List unitNo, String userNameEn, String status) throws Exception;

    /**
     * 重构fdev缺陷交互
     *
     * @param map
     * @return
     * @throws Exception
     */
    String updateFdevMantis(Map map) throws Exception;


    /**
     * 实施单元删除关联工单缺陷状态修改为已关闭
     * @param workNos
     * @return
     */
    void updateMantisStatus(List<String> workNos) throws Exception;

    /**
     * 任务ids关闭缺陷
     * @param taskIds
     * @return
     */
    void updateMantisByTaskIds(List<String> taskIds) throws  Exception;
}
