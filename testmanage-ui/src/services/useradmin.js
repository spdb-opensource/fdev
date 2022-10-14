import request from '@/common/request';
import service from './serviceMap';
export async function query(params) {
  return request(service.tuser.query, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function countUser(params) {
  return request(service.tuser.countUser, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function deleteUser(params) {
  return request(service.tuser.deleteUser, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function updateUser(params) {
  return request(service.tuser.updateUser, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function addUser(params) {
  return request(service.tuser.addUser, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function selUser(params) {
  return request(service.tuser.selUser, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 用户管理查询接口
export async function queryUserByUserName(params) {
  return request(service.tuser.queryUserByUserName, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 输入Mantis Token接口
export async function saveMantisToken(params) {
  return request(service.tuser.saveMantisToken, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 用户管理查询所有组
export async function queryAllGroup(params) {
  return request(service.tuser.queryAllGroup, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 用户管理查询所有角色
export async function queryAllRole(params) {
  return request(service.tuser.queryAllRole, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 导出用户管理列表
export async function exportUserList(params) {
  return request(service.tuser.exportUserList, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

// 查询所有sit消息
export async function queryMessageUser(params) {
  return request(service.tuser.queryMessageUser, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 更新更新用户通知消息状态
export async function updateNotifyStatus(params) {
  return request(service.tuser.updateNotifyStatus, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 查询所有公告
export async function queryAnnounce(params) {
  return request(service.tuser.queryAnnounce, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 新增公告
export async function sendAnnounce(params) {
  return request(service.tuser.sendAnnounce, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryUserDetail(params) {
  return request(service.tuser.queryUserDetail, {
    method: 'post',
    data: {
      ...params
    }
  });
}
