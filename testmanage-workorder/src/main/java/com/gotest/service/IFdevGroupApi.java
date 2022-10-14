package com.gotest.service;

import java.util.List;
import java.util.Map;

public interface IFdevGroupApi {
    /**
     * 获取fdev小组及其子组id集合
     * @param groupId
     * @return
     * @throws Exception
     */
    public List<String> getChildGroupId(String groupId) throws  Exception;

    public Map queryGroupDetail(String groupId) throws Exception;
}
