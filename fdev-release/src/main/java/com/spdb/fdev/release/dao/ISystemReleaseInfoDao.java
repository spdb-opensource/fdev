package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.SystemReleaseInfo;

public interface ISystemReleaseInfoDao {
	SystemReleaseInfo querySysRlsDetail(String system_id) throws Exception;
	
	SystemReleaseInfo save(SystemReleaseInfo systemReleaseInfo) throws Exception;
	
	SystemReleaseInfo update(SystemReleaseInfo systemReleaseInfo) throws Exception;

	SystemReleaseInfo updateSysRlsInfo(String system_id, String resource_giturl)throws Exception;

	SystemReleaseInfo querySysInfoByConfigType(String configType) throws Exception;
}
