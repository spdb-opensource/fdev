package com.spdb.fdev.fuser.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.RoleDao;
import com.spdb.fdev.fuser.spdb.entity.user.Role;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import com.spdb.fdev.fuser.spdb.service.RoleService;
import com.spdb.fdev.fuser.spdb.service.UserService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "角色接口")
@RequestMapping("/api/role")
@RestController
@RefreshScope
public class RoleController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

	@Resource
	private RoleService roleService;

	@Resource
	private UserService userService;

	@Autowired
	private UserVerifyUtil userVerifyUtil;

	@Autowired
	private RoleDao roleDao;

	@Value("${user.role.list}")
	private String ldapRoleList;

	@Value("${user.manu.role.list}")
	private String manuldapRoleList;

	@ApiOperation(value = "新增角色")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult addRole(@RequestBody @ApiParam(name = "新增角色参数", value = "示例：{\"name\": \"角色\" }") Role role)
			throws Exception {

		return JsonResultUtil.buildSuccess(roleService.addRole(role));
	}

	@ApiOperation(value = "更新角色")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult updateRole(
			@RequestBody @ApiParam(name = "更新角色参数", value = "示例：{\"id\": \"5c41796ca3178a3eb4b52005\" ,\"name\": \"角色\" }") Role role)
			throws Exception {
		roleService.updateRole(role);
		return JsonResultUtil.buildSuccess(roleService.queryRole(new Role()));
	}

	@ApiOperation(value = "角色查询")
	@ApiImplicitParams(@ApiImplicitParam(name = "name", value = "角色名字"))
	@RequestMapping(value = "/query", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult queryRole(
			@RequestBody @ApiParam(name = "查询角色参数", value = "示例：id,name可任意传一个 {\"id\": \"5c41796ca3178a3eb4b52005\" ,\"name\": \"角色\" }  查询全量列表传入{}") Role role)
			throws Exception {
		List<Role> roles = roleService.queryRole(role);
		return JsonResultUtil.buildSuccess(roles);
	}

	@ApiOperation(value = "查询LADP用户可用角色")
	@ApiImplicitParams(@ApiImplicitParam(name = "name", value = "角色名字"))
	@RequestMapping(value = "/queryRoleForLDAP", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult queryRoleForLDAP(@RequestBody @ApiParam(name = "查询角色参数", value = "查询全量列表传入{}")
                                                   Map requestMap) throws Exception {
	    //是否查行内可选
		boolean isSpdb = (boolean)requestMap.get(Dict.IS_SPDB);
		//查询全部角色
		List<Role> roles = roleService.queryRole(new Role());
		//根据远程配置获取可选角色id
        List<String> roleList = Arrays.asList(
                isSpdb?ldapRoleList.split(","):manuldapRoleList.split(","));
        //筛选数据
		List<Role> res = roles.stream().filter(e -> roleList.contains(e.getId())).collect(Collectors.toList());
		return JsonResultUtil.buildSuccess(res);
	}

	@ApiOperation(value = "删除角色")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult del(
			@RequestBody @ApiParam(name = "删除角色参数", value = "示例：[ {\"id\": \"5c41796ca3178a3eb4b52005\"} ]") List<Role> roleList
			)
			throws Exception {
		Role role = roleList.get(0);
		if (CommonUtils.isNullOrEmpty(role.getId())) {
			throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"角色id为空"});
		}
		roleService.delRoleByID(role.getId());
		return JsonResultUtil.buildSuccess(roleService.queryRole(new Role()));
	}

}
