package com.spdb.fdev.spdb.service;

import java.util.Set;

/**
 * @Author:guanz2
 * @Date:2021/10/5-13:03
 * @Description: scc_pod service 层
 */
public interface ISccPodService {
    //void 拉取数据
    void pullSccPodFromBlueking(Set<String> deploy_set) throws Exception;
}
