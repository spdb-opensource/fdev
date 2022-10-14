import request from '@/utils/request';
import service from './serviceMap';
// 按条价查询用户
export async function queryAll(params) {
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
      //permission_id: params.auth.id,
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
    data: params
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
