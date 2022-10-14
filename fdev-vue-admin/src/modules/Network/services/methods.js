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

// 网络审核查询
export const queryApprovalList = commonRequest(
  services.fuser.queryApprovalList
);

// 网络审核状态更新
export const updateApprovalStatus = commonRequest(
  services.fuser.updateApprovalStatus
);

// 按条价查询用户
export const queryAll = commonRequest(services.fuser.userQuery);

export const queryCompany = commonRequest(services.fuser.companyQuery);
//KF网络开通申请
export const openKF = commonRequest(services.fapprove.openKF);

//KF网络关闭申请
export const closeKF = commonRequest(services.fapprove.closeKF);

//获取工单列表
export async function queryOrders(params) {
  return request(services.fprocesstool.queryOrders, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//获取问题列表
export async function queryCodeIssue(params) {
  return request(services.fprocesstool.queryCodeIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//获取问题列表
export async function queryOrdersList(params) {
  return request(services.fprocesstool.queryOrdersList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//导出问题列表
export async function exportIssueList(params) {
  return request(services.fprocesstool.exportIssueList, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
//日志查询
export async function queryLogs(params) {
  return request(services.fprocesstool.queryLogs, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询会议记录
export async function queryMeetings(params) {
  return request(services.fprocesstool.queryMeetings, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//删除会议记录
export async function deleteMeetingById(params) {
  return request(services.fprocesstool.deleteMeetingById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增会议记录
export async function addMeeting(params) {
  return request(services.fprocesstool.addMeeting, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//更新会议记录
export async function updateMeeting(params) {
  return request(services.fprocesstool.updateMeeting, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询工单详情
export async function queryOrderById(params) {
  return request(services.fprocesstool.queryOrderById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增工单
export async function addOrders(params) {
  return request(services.fprocesstool.addOrders, {
    method: 'POST',
    'Content-Type': 'multipart/form-data',
    Accept: 'application/json',
    data: params
  });
}

export async function exportOrderExcel(params) {
  return request(services.fprocesstool.exportOrderExcel, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

//查询文件列表
export async function queryFiles(params) {
  return request(services.fprocesstool.queryFiles, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//删除工单
export async function deleteOrderById(params) {
  return request(services.fprocesstool.deleteOrderById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//编辑工单
export async function updateOrders(params) {
  return request(services.fprocesstool.updateOrders, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 下载文件
export async function downloadDoc(params) {
  return request(services.fprocesstool.downloadDoc, {
    method: 'GET',
    responseType: 'arraybuffer',
    params: {
      ...params
    }
  });
}

//文件上传
export async function uploadDoc(params) {
  return request(services.fprocesstool.uploadDoc, {
    method: 'POST',
    'Content-Type': 'multipart/form-data',
    Accept: 'application/json',
    data: params
  });
}

//文件删除
export async function deleteDoc(params) {
  return request(services.fprocesstool.deleteDoc, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询问题描述列表
export async function queryProblems(params) {
  return request(services.fprocesstool.queryProblems, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增问题描述
export async function addProblem(params) {
  return request(services.fprocesstool.addProblem, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//导出问题列表
export async function exportProblemExcel(params) {
  return request(services.fprocesstool.exportProblemExcel, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

//更新问题描述
export async function updateProblem(params) {
  return request(services.fprocesstool.updateProblem, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//删除问题描述
export async function deleteProblemById(params) {
  return request(services.fprocesstool.deleteProblemById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询字典（新增问题问题项下拉框选项）
export async function queryProblemItem(params) {
  return request(services.fprocesstool.queryProblemItem, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//文件批量下载
export async function downloadAll(params) {
  return request(services.fprocesstool.downloadAll, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

//复审提醒
export async function recheckRemind(params) {
  return request(services.fprocesstool.recheckRemind, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//申请复审
export async function applyRecheck(params) {
  return request(services.fprocesstool.applyRecheck, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
