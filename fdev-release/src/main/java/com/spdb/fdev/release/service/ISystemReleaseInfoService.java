package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.SystemReleaseInfo;

import java.util.List;
import java.util.Map;

public interface ISystemReleaseInfoService {
	/**
	 * 根据所属系统id查询系统信息
	 * @param system_id
	 * @return
	 * @throws Exception
	 */
	SystemReleaseInfo querySysRlsDetail(String system_id) throws Exception;
	/**
	 * 查询系统rel信息
	 * @return
	 * @throws Exception
	 */
	List<Map<String, String>> querySysRlsInfo()throws Exception;
	/**
	 * 修改系统缩写
	 * @param system_id
	 * @return
	 */
	SystemReleaseInfo updateSysRlsInfo(String system_id, String resource_giturl) throws Exception;
	
	/**
	 * 根据所属系统id查询系统信息
	 * @param configType
	 * @return
	 * @throws Exception
	 */
	SystemReleaseInfo querySysInfoByConfigType(String configType) throws Exception;
	
}
