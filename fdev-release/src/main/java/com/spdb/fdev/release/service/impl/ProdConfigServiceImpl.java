package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.release.dao.IProdConfigDao;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RefreshScope
public class ProdConfigServiceImpl implements IProdConfigService {
	@Autowired
	private IProdConfigDao prodConfigDao;
	@Autowired
	private IUserService userService;
	@Value("${scripts.path}")
	private String scripts_path;
	@Autowired
	private IAsyncAutoReleaseService asyncAutoReleaseService;
	@Autowired
	private Environment enviroment;
	@Autowired
	private IProdAssetService prodAssetService;
	@Autowired
	private IReleaseCatalogService catalogService;
	@Autowired
	private IReleaseNodeService releaseNodeService;
	@Autowired
	private IRoleService roleService;

    @Autowired
    private IGroupAbbrService groupAbbrService;
    @Autowired
	private IFileService fileService;
	@Autowired
	private IAppService appService;
	@Autowired
	private RestTransport restTransport;

	private final static Logger logger = LoggerFactory.getLogger(ProdConfigServiceImpl.class);

	/**
	 * 根据投产点名称查询配置文件变更应用
	 * @param release_node_name 投产点名称
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<ProdConfigApp> queryConfigApplication(String release_node_name) throws Exception {
		return prodConfigDao.queryConfigApplication(release_node_name);
	}
	@Override
	public List<ProdConfigApp> queryConfigSum(String release_node_name) throws Exception {
		List<ProdConfigApp> applist = prodConfigDao.queryConfigApplication(release_node_name);
		return applist;
	}

	@Override
	public ProdConfigApp addConfigApplication(ProdConfigApp prodConfigApp) {
		return prodConfigDao.addConfigApplication(prodConfigApp);
	}
	/**
	 * 修改审核状态。
	 * @param release_node_name
	 * @param application_id
	 * @param ischeck
	 * @return
	 */
	@Override
	public ProdConfigApp checkConfigApplication(String release_node_name, String application_id,String ischeck) {

		return prodConfigDao.checkConfigApplication(release_node_name,application_id, ischeck);
	}

	/**
	 * 根据应用id和投产点名称查询配置文件变更应用
	 * @param application_id
	 * @param release_node_name
	 * @return
	 */
	@Override
	public ProdConfigApp queryConfigs(String application_id, String release_node_name) {

		return prodConfigDao.queryConfigs(application_id,release_node_name);
	}

	/**
	 * 更新配置变更应用表实体属性
	 * @param prodConfigApp
	 * @return
	 */
	@Override
	public ProdConfigApp updateConfig(ProdConfigApp prodConfigApp) {
		return prodConfigDao.updateConfig(prodConfigApp);
	}
	/**
	 * 更新配置变更应用表配置文件生成状态以及配置文件TAG
	 * @param prodConfigApp
	 * @return
	 */
	@Override
	public ProdConfigApp updateStatus(ProdConfigApp prodConfigApp) {
		return prodConfigDao.updateStatus(prodConfigApp);
	}

	/**
	 * 通过投产点点名称、应用ID集合查询
	 * @param release_node_name
	 * @param applications
	 * @return
	 */
	@Override
	public List<ProdConfigApp> queryConfigList(String release_node_name, List<String> applications) throws Exception{
		return prodConfigDao.queryConfigList(release_node_name,applications);
	}

	/**
	 * 删除配置文件变更应用
	 * @param release_node_name
	 * @param application_id
	 */
	@Override
	public void deleteConfig(String release_node_name, String application_id) {
		prodConfigDao.deleteConfig(release_node_name,application_id);
	}

	/**
	 * 发送config模块生成配置文件
	 * @param prams
	 * @return
	 */
	@Override
	public List<Map<String, Object>> batchSaveConfigFile(List<Map<String, String>> prams) throws Exception{
		Map<String, Object> send_map = new HashMap<>();
		send_map.put(Dict.LIST, prams);// 发config模块生成配置文件
		send_map.put(Dict.REST_CODE, "batchSaveConfigFile");
		return (List<Map<String, Object>>) restTransport.submit(send_map);
	}


}
