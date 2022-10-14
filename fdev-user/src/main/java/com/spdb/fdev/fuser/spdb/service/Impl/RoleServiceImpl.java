package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.RoleDao;
import com.spdb.fdev.fuser.spdb.dao.UserDao;
import com.spdb.fdev.fuser.spdb.entity.user.Role;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import com.spdb.fdev.fuser.spdb.service.RoleService;
import com.spdb.fdev.fuser.spdb.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RoleServiceImpl implements RoleService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());//日志打印

    @Resource
    private RoleDao roleDao;

    @Resource
    private UserDao userDao;

    @Resource
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Role addRole(Role role) throws Exception {
        String name = role.getName();
        if (StringUtils.isEmpty(name)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"角色名为空"});
        }
        List<Role> r = roleDao.queryRole2(new Role(null, name, null, null, null, null));
        if (!CommonUtils.isNullOrEmpty(r)) {
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR,new String[]{"角色已存在"});
        }
        return roleDao.addRole(role);
    }

    @Override
    public Role delRoleByID(String id) throws Exception {
        User user = new User();
        List<String> list=new ArrayList<>();
        list.add(id);
        user.setRole_id(list);
        List<Map> users = userService.queryUser(user);
        if (!CommonUtils.isNullOrEmpty(users)) {
            throw new FdevException(ErrorConstants.USR_INUSE_ERROR,new String[]{"当前角色正在被使用"});
        }
        return roleDao.delRoleByID(id);
    }

    @Override
    public List<Role> queryRole(Role role) throws Exception {
        List<Role> roles = roleDao.queryRole(role);
        for (Role r : roles) {
            int num = 0;     //角色在职人数
            User user = new User();
            List<String> list = new ArrayList<>();
            list.add(r.getId());
            user.setRole_id(list);
            user.setStatus("0");   //在职
            List<Map> users = userDao.getUserCoreData(user);
            if (users != null) {
                num = users.size();
            }
            r.setCount(num);
        }
        return roles;
    }


    @Override
    public Role updateRole(Role role) throws Exception {
        if (CommonUtils.checkObjFieldIsNull(role)) {
			throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"角色id为空"});
		}
		List<Role> old = queryRole(new Role(role.getId(), null, null,null,null,null));
		if (CommonUtils.isNullOrEmpty(old)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"角色不存在"});
		}
		//角色的菜单信息修改后，将用户踢登，强制重新登陆获取新菜单
        //获取角色相关的用户
        if(!CommonUtils.isNullOrEmpty(old.get(0).getMenus()) && !old.get(0).getMenus().equals(role.getMenus())){
            List<User> users = userDao.getUserByRole(role.getId());
            for(User user:users){
                redisTemplate.opsForValue().set(user.getUser_name_en() + "user.login.token",null);
                logger.info("角色菜单更改，将用户"+user.getUser_name_en()+"强制下线！");
            }
        }
        return roleDao.updateRole(role);
    }

    @Override
    public List<String> queryRoleid(List<String> roleNames) {
        return roleDao.queryRoleid(roleNames);
    }

    @Override
    public Role queryByName(String roleName) {
        return roleDao.queryIdByName(roleName);
    }

    @Override
    public boolean checkRole(String userNameEn, String roleName) throws Exception {
        List<String> role_ids = null;
        if(CommonUtils.isNullOrEmpty(userNameEn)){
            com.spdb.fdev.common.User sessionUser = CommonUtils.getSessionUser();
            userNameEn = sessionUser.getUser_name_en();
            role_ids = sessionUser.getRole_id();
        }else {
            User user = new User();
            user.setUser_name_en(userNameEn);
            List<Map> userCoreData = userDao.getUserCoreData(user);
            role_ids = (List<String>)userCoreData.get(0).get(Dict.ROLE_ID);
        }
        Role roleInfo = roleDao.queryIdByName(roleName);
        if((!CommonUtils.isNullOrEmpty(roleInfo) && role_ids.contains(roleInfo.getId())) || Dict.ADMIN.equals(userNameEn)){
            return true;
        }
        return false;
    }

    @Override
    public List<Role> queryRoleByMenuId(List<String> menuIds) {
        return roleDao.queryRoleByMenuId(menuIds);
    }


}
