package com.spdb.fdev.pipeline.dao;

import com.spdb.fdev.common.User;
import com.spdb.fdev.pipeline.entity.Plugin;
import com.spdb.fdev.pipeline.entity.PluginEvaluate;
import com.spdb.fdev.pipeline.entity.PluginUseCount;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IPluginDao {
    /**
     * 查询我的插件列表
     * */
    public Map queryPlugin(List<String> key, Map param, User user ) throws Exception;

    /**
     * 新增自定义插件
     * */
    void savePlugin(Plugin plugin) throws Exception;

    /**
     * 删除插件
     * */
    public void delPlugin(String pluginCode) throws Exception;

    /**
     * 查询插件历史提交记录
     * */
    public List<Plugin> queryPluginHistory(String nameId)throws Exception;


    Plugin queryPluginDetail(String pluginCode) throws Exception;

    Plugin queryPluginDetailById(String id) throws Exception;

    List<Plugin> queryPluginListByNameId(String nameId) throws Exception;

    List<Plugin> queryPluginDetailByNameId(String nameId) throws Exception;
    /**
     * 设置插件状态为失效
     * */
    void setStatusClose(String pluginCode ,User user) throws Exception;

    List<Plugin> haveDuplicateNameFile() throws Exception;

    List<Plugin> queryByIdList(List<String> ids);

    List<PluginUseCount> statisticsPluginUseCount(String pipelineId) throws Exception;

    void savePluginUseCount(PluginUseCount pluginUseCount) throws Exception;

    PluginUseCount queryPluginUseCountByPluginCodeAndBindProjectId(String pluginCode,String bindProjectId) throws Exception;

    void removeAllPluginUseCount() throws Exception;

    void upsertPluginEvaluate(PluginEvaluate pluginEvaluate);

    PluginEvaluate queryPluginEvaluate(String userId, String nameId);

    Double queryPluginEvaluateByNameId(String nameId);
}
