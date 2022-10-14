package com.spdb.fdev.spdb.service;

import java.util.Set;

/**
 * @Author:guanz2
 * @Date:2021/10/1-19:02
 * @Description: caas_pod service
 */
public interface ICaasPodService {
    void pullCaasPodFromBlueking(Set<String> deployment_set) throws Exception;
}
