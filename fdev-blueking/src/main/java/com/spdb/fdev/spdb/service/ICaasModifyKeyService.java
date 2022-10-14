package com.spdb.fdev.spdb.service;

import java.util.List;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/10/1-19:03
 * @Description: caas_modifykey的service层
 */
public interface ICaasModifyKeyService {

    //保存需要修改的key
    void updateModifyKeys(List<Map<String, String>> list);

    //获取应用私有需要修改的key
    Map<String, Object> queryPersonalModifyKeys(String deployment, String cluster, String namespace);

    //获取共有需要修改的key
    Map<String, Object> queryCommonModifyKeys();
}
