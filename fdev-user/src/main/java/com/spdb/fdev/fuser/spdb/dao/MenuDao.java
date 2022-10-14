package com.spdb.fdev.fuser.spdb.dao;

import com.spdb.fdev.fuser.spdb.entity.user.Menu;

import java.util.List;

public interface MenuDao {

    /**
     * 查询所有的菜单
     * @return
     * @throws Exception
     */
    List<Menu> queryMenu() throws Exception;

    /**
     * 添加菜单
     * @param menu
     * @return
     */
    Menu addMenu(Menu menu);

    /**
     * 菜单中文名查询
     * @param nameCn
     * @return
     */
    List<Menu> queryMenuByMenuCn(String nameCn, String parentId);

    /**
     * 菜单Id查询
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
    Menu updateMenu(Menu menu) throws  Exception;

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
     */
    void deleteMenu(String menuId);

    /**
     * 根据父id查询菜单
     * @param menuId
     * @return
     * @throws Exception
     */
    List<Menu> queryMenuByParentId(String menuId) throws  Exception;

    /**
     * 根据菜单id集合查询所有菜单
     * @param ids
     * @return
     */
    List<Menu> queryMenuList(List<String> ids);
}
