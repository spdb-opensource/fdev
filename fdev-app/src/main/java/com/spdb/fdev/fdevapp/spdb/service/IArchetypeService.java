package com.spdb.fdev.fdevapp.spdb.service;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: luotao
 * Date: 2019/2/27
 * Time: 下午4:45
 * To change this template use File | Settings | File Templates.
 */
public interface IArchetypeService {

    /**
     * 根据id 从组件模块查询骨架信息
     * @param id
     * @return
     * @throws Exception
     */
    public Map<String,Object> queryArchetype(String id) throws Exception;

    public boolean createArchetype(Map archetype,String http_url_to_repo,String gitlabPath,String gitlabName,String gitlabPassword,String projectName,String token) throws Exception;
}
