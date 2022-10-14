import request from '@/common/request';
import service from './serviceMap';
//修改功能菜单
export async function updateFunctionMenu(params) {
  return request(service.tadmin.updateFunctionMenu, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//添加功能菜单
export async function addFunctionMenu(params) {
  return request(service.tadmin.addFunctionMenu, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//查询系统列表
export async function listAll(params) {
  return request(service.tadmin.listAll, {
    method: 'post',
    data: {
      ...params
    }
  });
}
//根据系统id查询功能菜单
export async function queryBySysId(params) {
  return request(service.tadmin.queryBySysId, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 根据系统名称查询功能菜单
export async function queryMenuBySysIdAndLever(params) {
  return request(service.tadmin.queryMenuBySysIdAndLever, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 查询
export async function queryMenuVOListByLever(params) {
  return request(service.tadmin.queryMenuVOListByLever, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 删除叶子节点
export async function delFunctionMenu(params) {
  return request(service.tadmin.delFunctionMenu, {
    method: 'post',
    data: {
      ...params
    }
  });
}
