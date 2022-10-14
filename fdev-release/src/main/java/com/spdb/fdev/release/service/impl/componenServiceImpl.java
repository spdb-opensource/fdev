package com.spdb.fdev.release.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.release.dao.IReleaseBatchDao;
import com.spdb.fdev.release.entity.ReleaseBatchRecord;
import com.spdb.fdev.release.service.IComponenService;
import com.spdb.fdev.transport.RestTransport;


@Service
@RefreshScope
public class componenServiceImpl implements IComponenService  {
	@Autowired
    private RestTransport restTransport;
	@Autowired
    private IReleaseBatchDao iReleaseBatchDao;
	
	@Override
    public Map<String, Object> queryComponenbyid(String id) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.ID, id);// 发组件模块获取appliaction详细信息
        send_map.put(Dict.REST_CODE, "queryComponentDetail");
        return (Map<String, Object>) restTransport.submit(send_map);
    }

	@Override
	public Map<String, Object> queryArchetypeDetail(String id) throws Exception {
		Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.ID, id);// 发组件模块获取appliaction详细信息
        send_map.put(Dict.REST_CODE, "queryArchetypeDetail");
        return (Map<String, Object>) restTransport.submit(send_map);
	}

	@Override
	public Map<String, Object> queryBaseImageDetail(String id) throws Exception {
		Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.ID, id);// 发组件模块获取appliaction详细信息
        send_map.put(Dict.REST_CODE, "queryBaseImageDetail");
        return (Map<String, Object>) restTransport.submit(send_map);
	}

	@Override
	public void createReleaseBranch(String id, String name, String ref, String type) throws Exception {
		// 发组件模块创建分支
        Map<String, String> map = new HashMap<>();
        map.put(Dict.ID, id);
        map.put(Dict.NAME, name);
        map.put(Dict.REF, ref);
        map.put(Dict.TYPE, type);// 发送componen模块 创建新的release分支
        map.put(Dict.REST_CODE, "createBranchByComponen");
        restTransport.submit(map);
		
	}

	@Override
	public Map<String, Object> queryMpassComponentDetail(String id) throws Exception {
		Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.ID, id);
        send_map.put(Dict.REST_CODE, "queryMpassComponentDetail");
        return (Map<String, Object>) restTransport.submit(send_map);
	}

	@Override
	public Map<String, Object> queryMpassArchetypeDetail(String id) throws Exception {
		Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.ID, id);
        send_map.put(Dict.REST_CODE, "queryMpassArchetypeDetail");
        return (Map<String, Object>) restTransport.submit(send_map);
	}
	
	/**
     * 根据应用ids查应用信息集合
     * @param appIds
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> getComponenByIdsOrGitlabIds(List<String> appIds, String releaseNodeName, String applicationType) throws Exception {
        Map send = new HashMap();
        send.put(Dict.IDS, appIds);
        send.put(Dict.TYPE, "applicationType");
        send.put(Dict.REST_CODE, "getDetailByIds");
        List<Object> result = (List<Object>)restTransport.submit(send);
        List<Map<String, String>> apps = new ArrayList<>();
        Map<String, String> app;
        for(Object o : result){
            app = new HashMap<>();
            String batchId = "";
            String appId = String.valueOf(((Map<String, Object>)o).get(Dict.ID));
            app.put(Dict.APPLICATION_ID, appId);
            app.put(Dict.APPLICATION_NAME_EN, String.valueOf(((Map<String, Object>)o).get(Dict.NAME_EN)));
            ReleaseBatchRecord releaseBatchRecord = iReleaseBatchDao.queryBatchRecordByAppId(releaseNodeName, appId);
            if(!CommonUtils.isNullOrEmpty(releaseBatchRecord)){
                batchId = releaseBatchRecord.getBatch_id();
            }
            app.put(Dict.BATCH_ID, batchId);
            apps.add(app);
        }
        return apps;
    }

	@Override
	public List<String> queryTagList(String release_node_name, String app_id) throws Exception {
		Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.RELEASE_NODE_NAME, release_node_name);
        send_map.put("app_id", app_id);
        send_map.put(Dict.REST_CODE, "queryTags");
        return (List<String>) restTransport.submit(send_map);
	}
}
