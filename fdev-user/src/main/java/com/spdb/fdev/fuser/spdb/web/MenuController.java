package com.spdb.fdev.fuser.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fuser.spdb.entity.user.Menu;
import com.spdb.fdev.fuser.spdb.entity.user.Role;
import com.spdb.fdev.fuser.spdb.service.MenuService;
import com.spdb.fdev.fuser.spdb.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "菜单接口")
@RequestMapping("/api/menu")
@RestController
@RefreshScope
public class MenuController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    MenuService menuService;

    @Resource
    private RoleService roleService;

    @ApiOperation(value = "展示所有菜单")
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryMenu() throws Exception {
       List<Menu> menus= menuService.queryMenu();
        return JsonResultUtil.buildSuccess(menus);
    }

    @ApiOperation(value = "新增菜单")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult addMenu(@RequestBody Menu menu) throws Exception {
        return JsonResultUtil.buildSuccess(menuService.addMenu(menu));
    }

    @ApiOperation(value = "批量新增菜单，数据初始化时使用")
    @RequestMapping(value = "/addMenus", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult addMenu(@RequestBody List<Menu> menus) throws Exception {
        menuService.addMenus(menus);
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "修改菜单")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateMenu(@RequestBody Menu menu) throws Exception {

        return JsonResultUtil.buildSuccess(menuService.updateMenu(menu));
    }

    @ApiOperation(value = "删除菜单")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult deleteMenu(@RequestBody Menu menu) throws Exception {
        menuService.deleteMenu(menu.getMenuId());
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "查询用户所有菜单")
    @RequestMapping(value = "/queryUserMenu", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryUserMenu(@RequestBody Menu menu, HttpServletRequest request) throws Exception {
        //获取当前登录的用户信息
        return  JsonResultUtil.buildSuccess(menuService.queryUserMenu());
    }

    @ApiOperation(value = "根据菜单id查询角色")
    @RequestMapping(value = "/queryRoleByMenuId", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryRoleByMenuId(@RequestBody Menu menu) throws Exception {
        List<String> menuIds = new ArrayList<>();
        menuIds.add(menu.getMenuId());
        List<Role> roles= roleService.queryRoleByMenuId(menuIds);
        return JsonResultUtil.buildSuccess(roles);
    }

}
