import request from '@/utils/request';
import service from './serviceMap';

// 我的生产问题
export async function queryUserProIssues(params) {
  return request(service.tmantis.queryUserProIssues, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 任务详情-生产问题
export async function queryTaskProIssues(params) {
  return request(service.tmantis.queryTaskProIssues, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 生产问题
export async function queryProIssues(params) {
  return request(service.tmantis.queryProIssues, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 生产问题-总条数
export async function countProIssues(params) {
  return request(service.tmantis.countProIssues, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 生产问题-导出
export async function exportProIssues(params) {
  return request(service.tmantis.exportProIssues, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
// 生产问题-详情
export async function queryIssueDetail(params) {
  return request(service.tmantis.queryIssueDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 生产问题原始信息查询
export async function queryProIssueById(params) {
  return request(service.tmantis.queryProIssueById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改生产问题
export async function updateProIssue(params) {
  return request(service.tmantis.updateProIssue, {
    method: 'POST',
    data: {
      ...params,
      dev_responsible: params.dev_responsible
        ? params.dev_responsible.map(item => item.user_name_en)
        : [],
      audit_responsible: params.audit_responsible
        ? params.audit_responsible.map(item => item.user_name_en)
        : [],
      test_responsible: params.test_responsible
        ? params.test_responsible.map(item => item.user_name_en)
        : [],
      task_responsible: params.task_responsible
        ? params.task_responsible.map(item => item.user_name_en)
        : [],
      emergency_responsible: params.emergency_responsible
        ? params.emergency_responsible.map(item => item.user_name_en)
        : [],
      reviewer: params.reviewer
        ? params.reviewer.map(item => item.user_name_en)
        : []
    }
  });
}

// 附件列表查询
export async function queryIssueFiles(params) {
  return request(service.tmantis.queryIssueFiles, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 下载附件
export async function fileDownload(params) {
  return request(service.tmantis.fileDownload, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

// 删除附件
export async function deleteIssueFile(params) {
  return request(service.tmantis.deleteFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 上传附件
export async function addFile(params) {
  return request(service.tmantis.addFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 删除生产问题
export async function deleteProIssue(params) {
  return request(service.tmantis.deleteProIssue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 人员维度缺陷查询
export async function queryFuserMantis(params) {
  return request(service.tmantis.queryFuserMantis, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 人员维度缺陷查询
export async function updateFdevMantis(params) {
  return request(service.tmantis.updateFdevMantis, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateAssignUser(params) {
  return request(service.tmantis.updateAssignUser, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryIssueDetailById(params) {
  return request(service.tmantis.queryIssueDetailById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryProByTeam(params) {
  return request(service.tmantis.queryProByTeam, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryProIssueType(params) {
  return request(service.tmantis.queryProIssueType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryProIssueRate(params) {
  return request(service.tmantis.queryProIssueRate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
