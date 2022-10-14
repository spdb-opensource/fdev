import request from '@/common/request';
import service from './serviceMap';

export async function queryFdevTaskDetail(params) {
  return request(service.torder.queryFdevTaskDetail, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function query(params) {
  return request(service.tmantis.query, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 查询总数数值
export async function countIssue(params) {
  return request(service.tmantis.countIssue, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 单条数据详情
export async function queryIssueDetail(params) {
  return request(service.tmantis.queryIssueDetail, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 删除
export async function deleteMantis(params) {
  return request(service.tmantis.deleteMantis, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 编辑
export async function update(params) {
  return request(service.tmantis.update, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 删除文件
export async function deleteFile(params) {
  return request(service.tmantis.deleteFile, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 新增文件
export async function addFile(params) {
  return request(service.tmantis.addFile, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 缺陷详情
export async function queryMantisInfo(params) {
  return request(service.tmantis.queryMantisInfo, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function exportMantisList(params) {
  return request(service.tmantis.exportMantisList, {
    method: 'post',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
/* 生产问题总结查询 */
export async function queryProIssues(params) {
  return request(service.tmantis.queryProIssues, {
    method: 'post',
    data: {
      ...params
    }
  });
}
/* 生产问题总结分页 */
export async function countProIssues(params) {
  return request(service.tmantis.countProIssues, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function queryFdevGroups(params) {
  return request(service.tmantis.queryFdevGroups, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 案例缺陷
export async function queryIssueByPlanResultId(params) {
  return request(service.tmantis.queryIssueByPlanResultId, {
    method: 'post',
    data: {
      ...params
    }
  });
}
// 审批
export async function auditMantis(params) {
  return request(service.tmantis.auditMantis, {
    method: 'post',
    data: {
      ...params
    }
  });
}

export async function checkAuditAuthority(params) {
  return request(service.tmantis.checkAuditAuthority, {
    method: 'post',
    data: {
      ...params
    }
  });
}
export async function queryAuditMantisInfo(params) {
  return request(service.tmantis.queryAuditMantisInfo, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 根据测试案例id查询计划planid
export async function queryPlanIdByPlanlistTestcaseId(params) {
  return request(service.tcase.queryPlanIdByPlanlistTestcaseId, {
    method: 'post',
    data: {
      ...params
    }
  });
}

// 查询应用名
export async function queryApps(params) {
  return request(service.fapp.queryApps, {
    method: 'post',
    data: {
      ...params
    }
  });
}
