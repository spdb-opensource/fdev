package com.spdb.fdev.fuser.spdb.dao.Impl;

import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.dao.MenuDao;
import com.spdb.fdev.fuser.spdb.entity.user.Menu;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class MenuDaoImpl implements MenuDao {

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 查询所有的菜单
     * @return
     * @throws Exception
     */
    @Override
    public List<Menu> queryMenu() throws Exception {
        return mongoTemplate.findAll(Menu.class);
    }

    /**
     * 添加菜单
     * @param menu
     * @return
     */
    @Override
    public Menu addMenu(Menu menu) {
        return mongoTemplate.save(menu);
    }

    /**
     * 菜单中文名查询
     * @param nameCn
     * @return
     */
    @Override
    public List<Menu> queryMenuByMenuCn(String nameCn, String parentId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.NAMECN).is(nameCn).and(Dict.PARENTID).is(parentId));
        List<Menu> menus = mongoTemplate.find(query, Menu.class);
        return menus;
    }

    /**
     * 菜单Id查询
     * @param menuId
     * @return
     */
    @Override
    public List<Menu> queryMenuByMenuId(String menuId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.MENUID).is(menuId));
        List<Menu> menus = mongoTemplate.find(query, Menu.class);
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
        Query query = Query.query(Criteria.where(Dict.MENUID).is(menu.getMenuId()));
        Update update = new Update();
        update.set(Dict.MENUID,menu.getMenuId());
        update.set(Dict.NAMECN,menu.getNameCn());
        update.set(Dict.NAMEEN,menu.getNameEn());
        update.set(Dict.PARENTID,menu.getParentId());
        update.set(Dict.PATH,menu.getPath());
        update.set(Dict.SORT,menu.getSort());
        update.set(Dict.LEVEL,menu.getLevel());
        update.set(Dict.MENUTYPE,menu.getMenuType());
        mongoTemplate.findAndModify(query, update,  Menu.class);
        return mongoTemplate.findOne(query, Menu.class);
    }

    /**
     * 根据菜单英文名查询菜单
     * @param nameEn
     * @return
     * @throws Exception
     */
    @Override
    public List<Menu> queryMenuByMenuEn(String nameEn) throws Exception {
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.NAMEEN).is(nameEn));
        List<Menu> menus = mongoTemplate.find(query, Menu.class);
        return menus;
    }

    /**
     * 删除菜单
     * @param menuId
     */
    @Override
    public void deleteMenu(String menuId) {
        Query query = Query.query(Criteria.where(Dict.MENUID).is(menuId));
        mongoTemplate.remove(query, Menu.class) ;
    }

    /**
     * 根据父id查询菜单
     * @param menuId
     * @return
     * @throws Exception
     */
    @Override
    public List<Menu> queryMenuByParentId(String menuId) throws Exception {
        Query query = new Query();
        query.addCriteria(Criteria.where(Dict.PARENTID).is(menuId));
        List<Menu> menus = mongoTemplate.find(query, Menu.class);
        return menus;
    }

    /**
     * 根据菜单id集合查询所有菜单
     * @param ids
     * @return
     */
    @Override
    public List<Menu> queryMenuList(List<String> ids) {
        Query query = Query.query(Criteria.where(Dict.MENUID).in(ids));
        return mongoTemplate.find(query, Menu.class);
    }
}
