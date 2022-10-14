export default {
  fuser: {
    query: '/fuser/api/function/query', //查询职能
    add: '/fuser/api/function/add', //新增职能
    delete: '/fuser/api/function/delete', //删除职能
    update: '/fuser/api/function/update', //编辑职能
    addMenu: '/fuser/api/menu/add', //新增菜单
    updateMenu: '/fuser/api/menu/update', //编辑菜单
    deleteMenu: '/fuser/api/menu/delete', //删除菜单
    queryMenu: '/fuser/api/menu/query', //查询菜单
    queryRoleByMenuId: '/fuser/api/menu/queryRoleByMenuId', //根据菜单id查询角色
    queryAllSection: '/fuser/api/section/queryAllSection', //查询所有条线
    addGroupUsers: '/fuser/api/group/addGroupUsers', //批量新增组员
    queryAllGroup: '/fuser/api/group/queryAllGroup', //按照层级关系返回全量组信息
    queryUserByGroupId: '/fuser/api/group/queryUserByGroupId', //根据组id查询组或子组下的人员信息
    canAddUserList: '/fuser/api/user/canAddUserList', //新增组员下拉人员
    queryApiRole: '/fuser/api/interfaceRegister/query', //查询接口登记信息
    exportApiRoleExcel: '/fuser/api/interfaceRegister/export', //导出
    addApiRole: '/fuser/api/interfaceRegister/add', //新增接口权限
    updateApiRole: '/fuser/api/interfaceRegister/update', //编辑接口登记信息
    deleteApiRoleById: '/fuser/api/interfaceRegister/delete' //删除接口登记信息
  }
};
