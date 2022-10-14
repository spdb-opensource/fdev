import request from '@/utils/request';
import service from './api';

//查询职能
export async function query(params) {
  return request(service.fuser.query, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增职能
export async function add(params) {
  return request(service.fuser.add, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//删除职能
export async function deleteFun(params) {
  return request(service.fuser.delete, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//修改职能
export async function update(params) {
  return request(service.fuser.update, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//菜单查询
export async function queryMenu(params) {
  return request(service.fuser.queryMenu, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//编辑菜单
export async function updateMenu(params) {
  return request(service.fuser.updateMenu, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//删除菜单
export async function deleteMenu(params) {
  return request(service.fuser.deleteMenu, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增菜单
export async function addMenu(params) {
  return request(service.fuser.addMenu, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//根据菜单Id查询角色。
export async function queryRoleByMenuId(params) {
  return request(service.fuser.queryRoleByMenuId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询所有条线。
export async function queryAllSection(params) {
  return request(service.fuser.queryAllSection, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//批量新增组员
export async function addGroupUsers(params) {
  return request(service.fuser.addGroupUsers, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//按照层级关系返回全量组信息
export async function queryAllGroup(params) {
  return request(service.fuser.queryAllGroup, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//根据组id查询组或子组下的人员信息
export async function queryUserByGroupId(params) {
  return request(service.fuser.queryUserByGroupId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增组员下拉选项
export async function canAddUserList(params) {
  return request(service.fuser.canAddUserList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询接口登记信息
export async function queryApiRole(params) {
  return request(service.fuser.queryApiRole, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增接口权限
export async function addApiRole(params) {
  return request(service.fuser.addApiRole, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//编辑接口登记信息
export async function updateApiRole(params) {
  return request(service.fuser.updateApiRole, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//导出
export async function exportApiRoleExcel(params) {
  return request(service.fuser.exportApiRoleExcel, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

//删除接口登记信息
export async function deleteApiRoleById(params) {
  return request(service.fuser.deleteApiRoleById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
