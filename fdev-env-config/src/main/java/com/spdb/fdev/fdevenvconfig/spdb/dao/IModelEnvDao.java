package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnv;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xxx
 * @date 2019/7/5 13:15
 */
public interface IModelEnvDao {
    List<ModelEnv> query(ModelEnv modelEnv) throws Exception;

    ModelEnv add(ModelEnv modelEnv);

    void delete(ModelEnv modelEnv);

    ModelEnv update(ModelEnv modelEnv) throws Exception;


    /**
     * 通过 实体集合 和 环境 查询 实体与环境的映射
     *
     * @return
     * @throws Exception
     */
    List<ModelEnv> queryModelEnvByModelsAndEnvId(Set<String> models, String envId);

    /**
     * 通过 实体环境映射id查询
     *
     * @param modelEnv
     * @return
     */
    ModelEnv queryId(ModelEnv modelEnv);

    /**
     * 通过env_id或model_id删除实体环境映射
     *
     * @param field 传"env_id"或"model_id"
     * @param param
     * @param opno
     */
    void deleteModelEnv(String field, String param, String opno);

    /**
     * modelEnv分页查询
     *
     * @param map
     * @return
     */
    Map<String, Object> pageQuery(Map map);

    /**
     * 根据实体id列表或环境id列表获取实体环境映射值,若列表都为空，则查询所有的
     *
     * @return
     */
    List<ModelEnv> getModelEnvList(List<String> modelIds, List<String> envIds);
    
    
    /**
     * 根据实体id和环境id查询实体环境映射值
     * @param modelId
     * @param envId
     * @return
     */
	ModelEnv getModelEnv(String modelId, String envId);
    
  
}
