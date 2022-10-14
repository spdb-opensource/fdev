package com.testmanage.admin.service.impl;

import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.util.Util;
import com.testmanage.admin.entity.FunctionMenu;
import com.testmanage.admin.mapper.FunctionMenuMapper;
import com.testmanage.admin.service.IFunctionMenuService;
import com.testmanage.admin.service.IUserService;
import com.testmanage.admin.util.CommonUtil;
import com.testmanage.admin.util.Dict;
import com.testmanage.admin.util.ErrorConstants;
import com.testmanage.admin.util.MyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class FunctionMenuServiceImpl implements IFunctionMenuService {
	private static final Logger logger = LoggerFactory.getLogger(FunctionMenuServiceImpl.class);
    @Autowired
    private FunctionMenuMapper functionMenuMapper;
    @Autowired
    private IUserService userService;
    @Autowired
    private RedisUtils redisUtils;
	@Autowired
	private MyUtil myUtil;

	@Value("${user.testManager.role.id}")
	private String testManagerRoleId;

	@Value("${user.testLeader.role.id}")
	private String testLeaderRoleId;

	@Value("${user.tester.role.id}")
	private String testerRoleId;

	@Value("${user.testAdmin.role.id}")
	private String testAdminRoleId;

    @Override
    public int save(Map<String, Object> requestParam){
    	FunctionMenu functionMenu = new FunctionMenu();
		functionMenu.setFunc_model_name((String)requestParam.get(Dict.FUNC_MODEL_NAME));
		functionMenu.setLevel((Integer)requestParam.get(Dict.LEVEL));
		functionMenu.setParent_id((Integer)requestParam.get(Dict.PARENT_ID));
		functionMenu.setSys_func_id((Integer)requestParam.get(Dict.SYS_FUNC_ID));
		FunctionMenu queryFuncDetail = functionMenuMapper.queryFuncDetail(functionMenu);
    	if (!CommonUtil.isNullOrEmpty(queryFuncDetail)) {
    		throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR, new String[] {"该功能菜单名称已存在,请更换!"});
		}
		try {
			functionMenuMapper.insert(functionMenu);
		} catch (Exception e) {
			logger.error("添加功能菜单失败: ", e);
			throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {"添加功能菜单失败"});
		}
		FunctionMenu query = functionMenuMapper.queryFuncDetail(functionMenu);
		if (0 == (Integer)requestParam.get(Dict.PARENT_ID)) {
			try {				
				return functionMenuMapper.update(query.getFunc_id(), query.getFunc_model_name(),
						"0,"+query.getFunc_id(), query.getFunc_model_name());
			} catch (Exception e) {
				logger.error("添加功能菜单成功,数据更新失败: ", e);
				throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {"添加功能菜单成功,数据更新失败!"});
			}
		}else {
			try {
				FunctionMenu queryMenu = functionMenuMapper.queryMenuDetailByFuncId(query.getParent_id());
				return functionMenuMapper.update(query.getFunc_id(), query.getFunc_model_name(),
						queryMenu.getField1()+","+query.getFunc_id(), queryMenu.getField2()+">"+query.getFunc_model_name());
			} catch (Exception e) {
				logger.error("添加功能菜单成功,数据更新失败: ", e);
				throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {"添加功能菜单成功,数据更新失败!"});
			}
		}
    }
    

    @Override
    public void update(Integer funcId, String funcName) throws Exception{
    	FunctionMenu queryMenu = functionMenuMapper.queryMenuDetailByFuncId(funcId);
    	if (CommonUtil.isNullOrEmpty(queryMenu)) {
			throw new FtmsException(ErrorConstants.DATA_NOT_EXIST);
		}
    	if (CommonUtil.isNullOrEmpty(queryMenu.getField2())) {
    		logger.error(" field2字段为空 ");
			throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {" field2为空 "});
		}
    	String[] split = queryMenu.getField2().split(">");
    	split[queryMenu.getLevel()-1] = funcName;
    	StringBuilder stringBuilder = new StringBuilder();
    	String newField2 = null;
    	for (int i=0; i < split.length; i++) {
    		if (i == split.length-1) {
    			newField2 = stringBuilder.append(split[i]).toString();
			}else {
				newField2 = stringBuilder.append(split[i]).append(">").toString();
			}
		}
    	try {
    		if (0 == queryMenu.getParent_id()) {
    			functionMenuMapper.update(funcId,funcName,"0,"+queryMenu.getFunc_id(),newField2);
			}else {
				FunctionMenu queryField = functionMenuMapper.queryMenuDetailByFuncId(queryMenu.getParent_id());
				functionMenuMapper.update(funcId,funcName,queryField.getField1()+","+funcId,newField2);
			}
		} catch (Exception e) {
			logger.error("修改功能菜单失败: ", e);
			throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {"修改功能菜单失败"});
		}
    	//修改该菜单下的子菜单field2字段
    	List<FunctionMenu> functionMenus = functionMenuMapper.selectByParentId(queryMenu.getSys_func_id(), funcId);
    	if (!CommonUtil.isNullOrEmpty(functionMenus)) {			
    		//查询修改后的菜单
    		FunctionMenu updateMenu = functionMenuMapper.queryMenuDetailByFuncId(funcId);
    		updateMenus(functionMenus, updateMenu);
		}
    }
    
    private void updateMenus(List<FunctionMenu> lists, FunctionMenu updateMenu) throws Exception {
		for (FunctionMenu functionMenu : lists) {
			try {
				functionMenuMapper.update(functionMenu.getFunc_id(), functionMenu.getFunc_model_name(), 
						updateMenu.getField1()+","+functionMenu.getFunc_id(), updateMenu.getField2()+">"+functionMenu.getFunc_model_name());
			} catch (Exception e) {
				logger.error("修改功能菜单失败: ", e);
				throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {"修改功能菜单失败"});
			}
			FunctionMenu menu = functionMenuMapper.queryMenuDetailByFuncId(functionMenu.getFunc_id());
			List<FunctionMenu> menus = functionMenuMapper.selectByParentId(functionMenu.getSys_func_id(), functionMenu.getFunc_id());
			if (CommonUtil.isNullOrEmpty(menus)) {
				continue;
			}
			updateMenus(menus, menu);
		}
    }
    

    @Override
    public List<FunctionMenu> query(Integer sys_func_id, Integer parent_id) throws Exception {
        return functionMenuMapper.selectByParentId(sys_func_id,parent_id);
    }

	@Override
	public List<FunctionMenu> queryMenu() throws Exception {
		return functionMenuMapper.getMenu();
	}

	@Override
	public List<Map<String, Object>> queryMenuBysysId(Integer sys_id) throws Exception{
		try {
			return functionMenuMapper.queryMenuBysysId(sys_id);
		} catch (Exception e) {
			throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
		}
	}

	@Override
	public List<FunctionMenu> queryByFuncId(Integer func_id) throws Exception{
		return functionMenuMapper.queryByFuncId(func_id);
	}

	@Override
	public FunctionMenu queryFuncDetail(FunctionMenu functionMenu) throws Exception{
		return functionMenuMapper.queryFuncDetail(functionMenu);
	}

	@Override
	public List<FunctionMenu> queryMenuBySysIdAndLever(Map<String, Object> requestParam) throws Exception {
		try {
			return functionMenuMapper.queryMenuBySysIdAndLever(requestParam);
		} catch (Exception e) {
			throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
		}
	}


	@Override
	public FunctionMenu queryMenuDetailByFuncId(Integer funcId) throws Exception {
		try {
			return functionMenuMapper.queryMenuDetailByFuncId(funcId);
		} catch (Exception e) {
			throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR);
		}
	}


	@Override
	public List<FunctionMenu> queryAll() throws Exception {
		return functionMenuMapper.queryAll();
	}


	@Override
	public void updateData(Integer funcId, String funcName, String field1, String field2, Integer sysId) throws Exception {
		try {
			functionMenuMapper.update(funcId, funcName, field1, field2);
		} catch (Exception e) {
			logger.error("修改功能菜单失败: ", e);
			throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {"修改功能菜单失败"});
		}
		//修改该菜单下的子菜单field2字段
    	List<FunctionMenu> functionMenus = functionMenuMapper.selectByParentId(sysId, funcId);
    	if (!CommonUtil.isNullOrEmpty(functionMenus)) {			
    		//查询修改后的菜单
    		FunctionMenu updateMenu = functionMenuMapper.queryMenuDetailByFuncId(funcId);
    		updateMenus(functionMenus, updateMenu);
		}
	}

	/**
	 * 该删除为假删除
	 * 删除功能菜单，按func_id作为parent_id查找，判断是否存在有子级菜单，若存在则不能删除
	 * @param funcId
	 */
	@Override
	public void delete(Integer funcId) throws Exception {
		//获取当前用户
		Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
		if (Util.isNullOrEmpty(user)){
			throw  new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
		}
		String user_en_name = (String)user.get(Dict.USER_NAME_EN);
		//查询当前用户所拥有的role_code
		List<String> role = (List<String>)user.get(Dict.ROLE_ID);
		if (role.contains(testAdminRoleId) || role.contains(testManagerRoleId)){
			List<FunctionMenu> menus = this.functionMenuMapper.queryByParentId(funcId);
			if (!CommonUtil.isNullOrEmpty(menus)){
				throw new FtmsException(ErrorConstants.DELETE_ERROR , new String[]{"该菜单为父级菜单，无法删除!"});
			}
			if (1 == functionMenuMapper.delete(funcId)) {
				logger.info("删除菜单" + funcId + "成功");
			}else {
				logger.error("删除菜单" + funcId + "失败");
				throw new FtmsException(ErrorConstants.DELETE_ERROR, new String[]{"删除菜单失败！"});
			}
		}else
			throw new FtmsException(ErrorConstants.ROLE_ERROR, new String[]{"当前用户没有删除的权限!"});
	}


}
