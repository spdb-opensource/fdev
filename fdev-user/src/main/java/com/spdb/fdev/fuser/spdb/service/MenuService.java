package com.spdb.fdev.fuser.spdb.service;

import com.spdb.fdev.fuser.spdb.entity.user.Menu;

import java.util.List;

public interface MenuService {

    /**
     * 展示菜单
     * @return
     * @throws Exception
     */
    List<Menu> queryMenu() throws Exception;

    /**
     * 添加菜单
     * @return
     * @throws Exception
     */
    Menu addMenu(Menu menu) throws  Exception;

    /**
     * 根据菜单名称查询
     * @param nameCn
     * @return
     */
    List<Menu> queryMenuByMenuCn(String nameCn, String parentId);

    /**
     * 根据菜单Id查询菜单
     * @param menuId
     * @return
     */
    List<Menu> queryMenuByMenuId(String menuId);

    /**
     * 修改菜单
     * @param menu
     * @return
     * @throws Exception
     */
    Menu updateMenu(Menu menu) throws Exception;

    /**
     * 根据菜单英文名查询菜单
     * @param nameEn
     * @return
     * @throws Exception
     */
    List<Menu> queryMenuByMenuEn(String nameEn) throws Exception;

    /**
     * 删除菜单
     * @param menuId
     * @throws Exception
     */
    void deleteMenu(String menuId) throws Exception;

    /**
     * 查询所有的子菜单
     * @param menuId
     * @return
     * @throws Exception
     */
    List<Menu> queryChildMenu(String menuId) throws Exception;

    /**
     * 查询当前登录用户菜单
     * @return
     * @throws Exception
     */
    List<Menu> queryUserMenu() throws Exception;

    void addMenus(List<Menu> menus) throws Exception;
}
