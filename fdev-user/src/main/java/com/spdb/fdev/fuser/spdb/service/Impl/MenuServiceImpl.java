package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.MenuDao;
import com.spdb.fdev.fuser.spdb.dao.RoleDao;
import com.spdb.fdev.fuser.spdb.entity.user.Menu;
import com.spdb.fdev.fuser.spdb.entity.user.Role;
import com.spdb.fdev.fuser.spdb.service.MenuService;
import com.spdb.fdev.fuser.spdb.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    @Resource
    private MenuDao menuDao ;

    @Resource
    private RoleDao roleDao ;

    @Autowired
    private RoleService roleService;

    /**
     * 展示菜单
     * @return
     * @throws Exception
     */
    @Override
    public List<Menu> queryMenu() throws Exception {
        //查询所有的菜单
        List<Menu> menuList= menuDao.queryMenu();
        //获取一级菜单
        List<Menu> menuIds = menuList.stream().filter(e -> "1".equals(e.getLevel())).collect(Collectors.toList());
        for (Menu menu : menuIds) {
            menu.setChildrenList(getChildMenu(menu.getMenuId()));
        }
        //菜单排序
        menuIds.sort(Comparator.comparing(Menu::getIntSort));
        return menuIds;
    }

    /**
     * 递归查询子菜单
     * @param menuId
     * @return
     * @throws Exception
     */
    private List<Menu> getChildMenu(String menuId) throws Exception {
        //子菜单
        List<Menu> childList = new ArrayList<>();
        //查询子菜单
        List<Menu> list=  menuDao.queryMenuByParentId(menuId);
        for (Menu menu : list) {
            if(!CommonUtils.isNullOrEmpty(menu.getParentId()) && menu.getParentId().equals(menuId)){
                //当前用户角色中所拥有的菜单
                childList.add(menu);//非敏捷团队菜单
            }
        }
        //子菜单排序
        childList.sort(Comparator.comparing(Menu::getIntSort));
        //再次递归循环子菜单
        for (Menu menu : childList) {
            menu.setChildrenList(getChildMenu(menu.getMenuId()));
        }
        if(childList.size() == 0){
            return new ArrayList<>();
        }
        return childList;
    }

    /**
     * 添加菜单
     * @return
     * @throws Exception
     */
    @Override
    public Menu addMenu(Menu menu) throws Exception {
        //判断当前用户是否是admin或超级管理员
        if(!roleService.checkRole(null, Constants.SUPER_MANAGER)){
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        //参数校验
        if (CommonUtils.isNullOrEmpty(menu.getNameCn())){
            throw  new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"菜单中文名称不能为空"});
        }
        boolean matches = menu.getNameEn().matches("[a-zA-Z]+");
        if (CommonUtils.isNullOrEmpty(menu.getNameEn())|| !matches){
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"菜单英文名称不能为空或必须为英文"});
        }
        if (CommonUtils.isNullOrEmpty(menu.getSort())){
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"菜单序号不能为空"});
        }
        if (CommonUtils.isNullOrEmpty(menu.getParentId())&& !"1".equals(menu.getLevel())){
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"先添加父菜单"});
        }
        if (!CommonUtils.isNullOrEmpty(this.queryMenuByMenuId(menu.getMenuId()))){
            throw  new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"菜单Id已经存在"});
        }
        if (!CommonUtils.isNullOrEmpty(this.queryMenuByMenuCn(menu.getNameCn(),menu.getParentId()))){
            throw  new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"同级下菜单名已经存在"});
        }
        if (!CommonUtils.isNullOrEmpty(this.queryMenuByMenuEn(menu.getNameEn()))){
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"菜单英文名称已存在"});
        }
        //默认设置菜单类型，后续按需调整
        ArrayList menuType = new ArrayList();
        menuType.add("1");
        menuType.add("2");
        menu.setMenuType(menuType);
        Menu menu1 = menuDao.addMenu(menu);
        return menu1;
    }

    /**
     * 根据菜单名称查询
     * @param nameCn
     * @return
     */
    @Override
    public List<Menu> queryMenuByMenuCn(String nameCn,String parentId) {
        List<Menu> menus =  menuDao.queryMenuByMenuCn(nameCn,parentId);
        return menus;
    }

    /**
     * 根据菜单Id查询菜单
     * @param menuId
     * @return
     */
    @Override
    public List<Menu> queryMenuByMenuId(String menuId) {
        List<Menu> menus= menuDao.queryMenuByMenuId(menuId);
        return menus;
    }

    /**
     * 修改菜单
     * @param menu
     * @return
     * @throws Exception
     */
    @Override
    public Menu updateMenu(Menu menu) throws Exception {
        //判断当前用户是否是admin或超级管理员
        if(!roleService.checkRole(null,Constants.SUPER_MANAGER)){
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        //参数校验
        if (CommonUtils.isNullOrEmpty(menu.getNameCn())){
            throw  new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"菜单中文名称不能为空"});
        }
        if (CommonUtils.isNullOrEmpty(menu.getSort())){
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"菜单序号不能为空"});
        }
        if (CommonUtils.isNullOrEmpty(queryMenuByMenuId(menu.getMenuId()))){
            throw  new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"该菜单不存在"});
        }
        Menu menu1=  menuDao.updateMenu(menu) ;
        return menu1;
    }

    /**
     * 根据菜单英文名查询菜单
     * @param nameEn
     * @return
     * @throws Exception
     */
    @Override
    public List<Menu> queryMenuByMenuEn(String nameEn) throws Exception {
        List<Menu> menus =  menuDao.queryMenuByMenuEn(nameEn);
        return menus;
    }

    /**
     * 删除菜单
     * @param menuId
     * @throws Exception
     */
    @Override
    public void deleteMenu(String menuId) throws Exception {
        //判断当前用户是否是admin或超级管理员
        if(!roleService.checkRole(null,Constants.SUPER_MANAGER)){
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        //根据菜单id查询子菜单
        List<Menu> menus = this.queryChildMenu(menuId);
        if (!CommonUtils.isNullOrEmpty(menus)){
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"请先删除子菜单"});
        }
        //查询当前菜单是否有角色在使用，如果有则不能删除
        List<String> menuIds = new ArrayList<>();
        menuIds.add(menuId);
        List<Role> roles= roleService.queryRoleByMenuId(menuIds);
        if (!CommonUtils.isNullOrEmpty(roles)){
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"当前菜单有角色正在使用"});
        }
        menuDao.deleteMenu(menuId);
    }

    /**
     * 查询所有的子菜单
     * @param menuId
     * @return
     * @throws Exception
     */
    @Override
    public List<Menu> queryChildMenu(String menuId) throws Exception {
        return getChildMenu(menuId);
    }

    /**
     * 查询子菜单
     * @param menuId
     * @param menuIds
     * @return
     * @throws Exception
     */
    private List<Menu> getChild(String menuId, List<String> menuIds) throws Exception {
        //子菜单
        List<Menu> childList = new ArrayList<>();
        //查询子菜单
        List<Menu> list=  menuDao.queryMenuByParentId(menuId);
        for (Menu menu : list) {
            if(!CommonUtils.isNullOrEmpty(menu.getParentId()) && menu.getParentId().equals(menuId)){
                //当前用户角色中所拥有的菜单
                if(menuIds.contains(menu.getMenuId())){
                    childList.add(menu);
                }
            }
        }
        //子菜单排序
        childList.sort(Comparator.comparing(Menu::getIntSort));
        //再次递归循环子菜单
        for (Menu menu : childList) {
            menu.setChildrenList(getChild(menu.getMenuId(),menuIds));

        }
        if(childList.size() == 0){
            return new ArrayList<>();
        }
        return childList;
    }

    /**
     * 查询用户菜单
     * @return
     * @throws Exception
     */
    @Override
    public List<Menu> queryUserMenu() throws Exception {
        List<Menu> result = new ArrayList<>();
        User sessionUser = CommonUtils.getSessionUser();
        if("admin".equals(sessionUser.getUser_name_en())){
            return this.queryMenu();
        }
        List<String> roleIds = sessionUser.getRole_id();
        List<Role> roleList= roleDao.queryRoleByIds(roleIds); //根据角色id集合查询角色
        List<String> menuList = roleList.stream().map(Role::getMenus).filter(x->x!=null).collect(ArrayList::new, ArrayList::addAll,ArrayList::addAll);
        List<String> menuIds = menuList.stream().distinct().collect(Collectors.toList());

        if(!CommonUtils.isNullOrEmpty(menuIds)){
            List<Menu> menus = menuDao.queryMenuList(menuIds);
            for (Menu menu :menus) {
                if("1".equals(menu.getLevel())){
                    menu.setChildrenList(getChild(menu.getMenuId(),menuIds));
                    result.add(menu);
                }
            }
        }
        //一级菜单排序
        result.sort(Comparator.comparing(Menu::getIntSort));
        return result;
    }

    @Override
    public void addMenus(List<Menu> menus) throws Exception {
        for(Menu menu:menus){
            Menu menu1 = addMenu(menu);
        }
    }
}
