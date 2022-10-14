package com.spdb.fdev.spdb.service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Author:guanz2
 * @Date:2021/10/5-13:05
 * @Description: scc_modifyKey的service层
 */
public interface ISccModifyKeyService {

    //保存需要修改的完整yaml
    void updateModifyKeys(List<LinkedHashMap<String, Object>> list);

    //获取应用私有需要修改的key
    List<LinkedHashMap<String, Object>> queryModifyKeys(String resource_code);

}
