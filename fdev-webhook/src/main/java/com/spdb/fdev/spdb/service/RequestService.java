package com.spdb.fdev.spdb.service;

import java.util.Map;

/**
 * @author xxx
 * @date 2020/8/20 8:20
 */
public interface RequestService {

    /**
     * 更新任务模块sonar相关信息
     *
     * @param paramMap
     */
    void updateTaskSonar(Map<String, Object> paramMap);

}
