package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.release.dao.ISystemReleaseInfoDao;
import com.spdb.fdev.release.entity.SystemReleaseInfo;
import com.spdb.fdev.release.service.ISystemReleaseInfoService;
import com.spdb.fdev.release.service.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class SystemReleaseInfoServiceImpl implements ISystemReleaseInfoService{
	

	@Autowired
	private ISystemReleaseInfoDao systemReleaseInfoDao;
	@Autowired
	private ITaskService taskService;
	
	public SystemReleaseInfo querySysRlsDetail(String system_id) throws Exception {
		return systemReleaseInfoDao.querySysRlsDetail(system_id);
	}

	@Override
	public List<Map<String,String>> querySysRlsInfo() throws Exception {
		List<Map> system_list = taskService.querySystem();
		List<Map<String,String>> list=new ArrayList<>();
		for (Map map : system_list) {
			SystemReleaseInfo systemInfo = querySysRlsDetail((String)map.get(Dict.ID));
			if(!CommonUtils.isNullOrEmpty(systemInfo)) {
				map.put(Dict.SYSTEM_ID, systemInfo.getSystem_id());
				map.put(Dict.RESOURCE_GITURL, Arrays.asList(systemInfo.getResource_giturl().split(";")));
				map.put(Dict.RESOURCE_PROJECTID, systemInfo.getResource_projectid());
			}
//			map.put(Dict.SYSNAME_EN, map.get(Dict.NAME_EN));
			map.put(Dict.SYSNAME_CN, map.get(Dict.NAME));
			list.add(map);
		}
		return list;
	}


	@Override
	public SystemReleaseInfo updateSysRlsInfo(String system_id, String resource_giturl) throws Exception {
		return systemReleaseInfoDao.updateSysRlsInfo(system_id, resource_giturl);
	}

	@Override
	public SystemReleaseInfo querySysInfoByConfigType(String configType) throws Exception {
		return systemReleaseInfoDao.querySysInfoByConfigType(configType);
	}
}
