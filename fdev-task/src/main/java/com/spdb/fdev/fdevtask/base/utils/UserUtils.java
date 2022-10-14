package com.spdb.fdev.fdevtask.base.utils;

import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author liux81
 * @DATE 2021/12/2
 */
@Component
public class UserUtils {
    @Autowired
    private RestTransport restTransport;

    /**
     * 批量查询用户信息，返回格式为[{userId:userInfoMap},{},..]。用户id为key，用户信息为value，组装而成的集合
     * @param userIds
     * @return
     * @throws Exception
     */
    public Map getUserByIds(Set<String> userIds) throws Exception {
        Map map = new HashMap();
        map.put(Dict.IDS,userIds);
        map.put(Dict.REST_CODE, Dict.QUERYBYUSERCOREDATA);
        return (Map)restTransport.submit(map);
    }

    /**
     * 获取组的三级父组，若当前组为3级之内，则直接返回自身
     * @param groupId
     * @return
     * @throws Exception
     */
    public Map getThreeLevelGroup(String groupId) throws Exception {
        Map map = new HashMap();
        map.put(Dict.ID,groupId);
        map.put(Dict.REST_CODE, "getThreeLevelGroup");
        return (Map)restTransport.submit(map);
    }

}
