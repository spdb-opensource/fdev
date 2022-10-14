import request from '@/utils/request.js';
import services from './api';

export const commonRequest = url => async (params = {}) => {
  const response = await request(url, {
    method: 'POST',
    data: {
      ...params
    }
  });

  return response;
};

// 查询应用信息
// export const query = commonRequest(services.fapp.appQuery);

export const queryCurrent = commonRequest(services.fuser.currentUser);

// 按条价查询用户
export const queryAll = commonRequest(services.fuser.userQuery);

export const queryMyApps = commonRequest(services.fapp.myAppQuery);

//我的骨架查询--后端
export const queryMyArchetypes = commonRequest(
  services.fcomponent.queryMyArchetypes
);

export const queryGroup = commonRequest(services.fuser.groupQuery);

export async function update(params) {
  return request(services.fuser.userUpdate, {
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
      permission_id: params.auth.id,
      labels: params.tagSelected.map(label => label.id),
      telephone: params.telephone,
      status:
        typeof params.isLeave === 'string'
          ? params.isLeave
          : params.isLeave.value
    }
  });
}

export const addTag = commonRequest(services.fuser.labelAdd);

export async function queryTag() {
  return request(services.fuser.labelQuery, {
    method: 'POST',
    data: {}
  });
}
export async function removeTag(params) {
  return request(services.fuser.labelDelete, {
    method: 'POST',
    data: [...params]
  });
}

// 查区域下拉
export const queryArea = commonRequest(services.fuser.queryArea);

// 查询人员职能
export const queryFunction = commonRequest(services.fuser.queryFunction);

// 查询人员职能
export const queryRank = commonRequest(services.fuser.queryRank);

// 人员维度缺陷查询
export const queryFuserMantis = commonRequest(
  services.tmantis.queryFuserMantis
);

// 人员维度缺陷查询
export const updateFdevMantis = commonRequest(
  services.tmantis.updateFdevMantis
);

export const updateAssignUser = commonRequest(
  services.tmantis.updateAssignUser
);

// 我的生产问题
export const queryUserProIssues = commonRequest(
  services.tmantis.queryUserProIssues
);

// 任务详情-生产问题
export const queryTaskProIssues = commonRequest(
  services.tmantis.queryTaskProIssues
);

// 删除生产问题
export const deleteProIssue = commonRequest(services.tmantis.deleteProIssue);

// 改变任务阶段
export async function updateJobStage(params) {
  return request(services.ftask.taskUpdate, {
    method: 'POST',
    data: {
      id: params.id,
      stage: params.stage
    }
  });
}

export const queryCommitTips = commonRequest(services.ftask.queryCommitTips);

// 获取当前人员参与的任务列表
export const queryUserTask = commonRequest(services.ftask.queryUserTask);

// 我的组件查询--后端
export const queryMyComponent = commonRequest(
  services.fcomponent.queryMyComponent
);

//我的骨架查询--前端
export const queryMyMpassArchetypes = commonRequest(
  services.fcomponent.queryMyMpassArchetypes
);

// 我的组件查询--前端
export const queryMyMpassComponents = commonRequest(
  services.fcomponent.queryMyMpassComponents
);
// 查询用户可操作的已投产待归档任务列表
export const queryFileList = commonRequest(services.fmine.queryWaitFileTask);

// 批量归档任务
export const batchFileTask = commonRequest(
  services.fmine.updateTaskStateToFile
);
// 查询缺陷类型
export const queryOrderInfoByNo = commonRequest(
  services.torder.queryOrderInfoByNo
);
