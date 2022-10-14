import request from '@/utils/request';
import service from './serviceMap';

export async function login(params) {
  return request(service.fuser.login, {
    method: 'POST',
    data: {
      user_name_en: params.userName,
      password: params.password,
      ...params
    }
  });
}

export async function once(params) {
  return request(service.fuser.onceLogin, {
    method: 'POST',
    data: {
      password: params.password,
      git_token: params.token,
      ...params
    }
  });
}

export async function queryOAuth(params) {
  return request(service.fuser.queryOAuth, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function authorize(params) {
  return request(service.fuser.createAuthCode, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询通知
export async function queryNotice(params) {
  return request(service.fnotify.queryMessage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//更新消息未读状态
export async function updateNotice(params) {
  return request(service.fnotify.updateNotifyStatus, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//发起公告
export async function sendAnnounce(params) {
  return request(service.fnotify.sendAnnounce, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询公告
export async function queryAnnounce(params) {
  return request(service.fnotify.queryAnnounce, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询用户未读消息
export async function queryMessageByType(params) {
  return request(service.fnotify.queryMessageByType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryGroup(params) {
  return request(service.fuser.groupQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询小组信息，包括小组人数
export async function queryGroupAll(params) {
  return request(service.fuser.queryGroup, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询当前组以及所有的子组
export async function queryChildGroupById(params) {
  return request(service.fuser.queryChildGroupById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addGroup(params) {
  return request(service.fuser.groupAdd, {
    method: 'POST',
    data: {
      ...params,
      parent_id: params.parent_id
    }
  });
}

export async function removeGroup(params) {
  return request(service.fuser.groupDelete, {
    method: 'POST',
    data: [...params]
  });
}
export async function updateGroup(params) {
  return request(service.fuser.groupUpdate, {
    method: 'POST',
    data: {
      ...params,
      parent_id: params.parent
    }
  });
}

export async function queryRole(params) {
  return request(service.fuser.roleQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function removeRole(params) {
  return request(service.fuser.roleDelete, {
    method: 'POST',
    data: [...params]
  });
}
export async function addRole(params) {
  return request(service.fuser.roleAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function updateRole(params) {
  return request(service.fuser.roleUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryAuth() {
  return request(service.fuser.permissionQuery, {
    method: 'POST',
    data: {}
  });
}
export async function updateAuth(params) {
  return request(service.fuser.permissionUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryTag() {
  return request(service.fuser.labelQuery, {
    method: 'POST',
    data: {}
  });
}
export async function removeTag(params) {
  return request(service.fuser.labelDelete, {
    method: 'POST',
    data: [...params]
  });
}
export async function addTag(params) {
  return request(service.fuser.labelAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryCompany() {
  return request(service.fuser.companyQuery, {
    method: 'POST',
    data: {}
  });
}
export async function removeCompany(params) {
  return request(service.fuser.companyDelete, {
    method: 'POST',
    data: [...params]
  });
}
export async function addCompany(params) {
  return request(service.fuser.companyAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function updateCompany(params) {
  return request(service.fuser.companyUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 接口换成直接查需求模块
export async function queryRedmineById(params) {
  return request(service.frqrmnt.queryRedmineInfoByRedmineId, {
    method: 'POST',
    data: {
      ...params,
      redmine_id: params.id
    }
  });
}

export async function querySystem(params) {
  return request(service.ftask.querySystem, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryDomain(params) {
  return request(service.ftask.queryDomains, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryCITemplate(params) {
  return request(service.fapp.gitlabciQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryMerger(params) {
  return request(service.ftask.queryMergeInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function getChangePasswordToken(params) {
  return request(service.fuser.getVerifyCode, {
    method: 'POST',
    data: {
      email: params.email
    }
  });
}

export async function changePassword(params) {
  return request(service.fuser.forgetPassWord, {
    method: 'POST',
    data: {
      email: params.email,
      code: params.token,
      password: params.password
    }
  });
}

export async function queryUserMenu(params) {
  return request(service.fuser.queryUserMenu, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
