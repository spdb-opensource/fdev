import request from '@/utils/request.js';
import service from './api';

export const commonRequest = url => async (params = {}) => {
  const response = await request(url, {
    methods: 'POST',
    data: {
      ...params
    }
  });

  return response;
};

// 按条价查询用户
export async function fetch(params) {
  return request(service.fuser.userQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 按条件查询用户,强制条件->用户已完成首次登录，在职状态
export async function query(params) {
  return request(service.fuser.userQuery, {
    method: 'POST',
    data: {
      ...params,
      is_once_login: '1',
      status: '0'
    }
  });
}

export async function queryOnceLogin(params) {
  return request(service.fuser.updateUserIsOnceLogin, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询当前组以及子组的所有人员
export async function queryGroupPeople(params) {
  return request(service.fuser.queryByGroupId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryCurrent() {
  return request(service.fuser.currentUser, {
    method: 'POST',
    data: {}
  });
}

export async function add(params) {
  return request(service.fuser.userAdd, {
    method: 'POST',
    data: {
      ...params,
      user_name_cn: params.name,
      email: params.email,
      redmine_user: params.redmineAccount,
      svn_user: params.svnAccount,
      git_user: params.gitlabAccount,
      group_id: params.group.id,
      company_id: params.company.id,
      role_id: params.role.map(each => each.id),
      permission_id: params.auth.id,
      labels: params.tagSelected.map(each => each.id),
      telephone: params.telephone,
      status: params.isLeave.value,
      is_once_login: params.login,
      git_token: params.git_token
    }
  });
}

export async function remove(params) {
  return request(service.fuser.userDelete, {
    method: 'POST',
    data: params.map(param => ({
      ...param,
      user_name_en: param.eName,
      group_id: param.group.id,
      permission_id: param.auth.id
    }))
  });
}

export async function update(params) {
  return request(service.fuser.userUpdate, {
    method: 'POST',
    data: {
      ...params,
      user_name_cn: params.name,
      redmine_user: params.redmineAccount,
      svn_user: params.svnAccount,
      git_user: params.gitlabAccount,
      group_id: params.group.id,
      company_id: params.company.id,
      role_id: params.role.map(each => each.id),
      //permission_id: params.auth.id,
      labels: params.tagSelected.map(label => label.id),
      telephone: params.telephone,
      status:
        typeof params.isLeave === 'string'
          ? params.isLeave
          : params.isLeave.value
    }
  });
}

export async function queryTodos(params) {
  return request(service.fuser.queryCommissionEvent, {
    method: 'POST',
    data: {
      label: params
    }
  });
}
export async function updateLabelById(params) {
  return request(service.fuser.updateLabelById, {
    method: 'POST',
    data: {
      id: params
    }
  });
}

export async function simulateUser(params) {
  return request(service.fuser.simulateUser, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//重置密码
export async function resetPassword(params) {
  return request(service.fuser.resetPassword, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查区域下拉
export async function queryArea(params) {
  return request(service.fuser.queryArea, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryUserStatis(params) {
  return request(service.fuser.queryUserStatis, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询人员职能
export async function queryfunction(params) {
  return request(service.fuser.queryfunction, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询人员职能
export async function queryFunction(params) {
  return request(service.fuser.queryFunction, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询人员职能
export async function queryRank(params) {
  return request(service.fuser.queryRank, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//用户查询页面分页查询
export async function queryUserPagination(params) {
  return request(service.fuser.queryUserPagination, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询小组信息(带fullname)
export async function queryReBuildGroupName(params) {
  return request(service.fuser.queryReBuildGroupName, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryRoleForLDAP(params) {
  return request(service.fuser.queryRoleForLDAP, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function updateGitToken(params) {
  return request(service.fuser.updateGitToken, {
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

// 获取当前人员参与的任务列表
export async function queryUserTask(params) {
  return request(service.ftask.queryUserTask, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 改变任务阶段
export async function updateJobStage(params) {
  return request(service.ftask.taskUpdate, {
    method: 'POST',
    data: {
      id: params.id,
      stage: params.stage
    }
  });
}
