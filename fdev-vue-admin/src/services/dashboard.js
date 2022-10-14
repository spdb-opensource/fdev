import request from '@/utils/request';
import service from './serviceMap';

export async function getProjectUrl(params) {
  return request(service.fgitwork.getProjectUrl, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 任务 查询近6周应用数量
export async function queryTaskNum(params) {
  return request(service.ftask.queryTaskNum, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 应用 查询近6周应用数量
export async function queryAppNum(params) {
  return request(service.fapp.queryAppNum, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//设计稿下载
export async function downLoadDemandReviewList(params) {
  return request(service.fdemand.downLoadDemandReviewList, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
//设计还原稿下载
export async function downLoadReviewList(params) {
  return request(service.ftask.downLoadReviewList, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

//根据id查子组
export async function queryGroupById(params) {
  return request(service.ftask.queryGroupById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 根据小组查询各阶段任务
export async function queryTaskNumByGroup(params) {
  return request(service.ftask.queryTaskNumByGroup, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 时间段内任务数量查询
export async function queryTaskNumByGroupDate(params) {
  return request(service.ftask.queryTaskNumByGroupDate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 根据任务id集合查询任务列表
export async function queryTasksByIds(params) {
  return request(service.ftask.queryTasksByIds, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 导出
export async function exportExcel(params) {
  return request(service.ftask.exportExcel, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
// 查询基础用户信息
export async function queryUserCoreData(params) {
  return request(service.fuser.queryUserCoreData, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 根据应用查询各阶段任务
export async function queryTaskNumByApp(params) {
  return request(service.ftask.queryTaskNumByApp, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询人员关联的任务数量
export async function queryTaskNumByMember(params) {
  return request(service.ftask.queryTaskNumByMember, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 需求维度统计
export async function queryStatis(params) {
  return request(service.fdemand.queryStatis, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryImpingDemandDashboard(params) {
  return request(service.fdemand.queryImpingDemandDashboard, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryEndDemandDashboard(params) {
  return request(service.fdemand.queryEndDemandDashboard, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryIntGroupId(params) {
  return request(service.fdemand.queryIntGroupId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 需求实施单元维度统计
export async function queryImplUnitStatis(params) {
  return request(service.fdemand.queryImplUnitStatis, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询人均阶段任务数(区分业务科技)
export async function queryTaskStatis(params) {
  return request(service.ftask.queryTaskStatis, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询小组阶段任务数(区分业务科技)
export async function queryGroupStatis(params) {
  return request(service.ftask.queryGroupStatis, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryTaskNumByUserIdsDate(params) {
  return request(service.ftask.queryTaskNumByUserIdsDate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryResourceManagement(params) {
  return request(service.torder.queryResourceManagement, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryTaskSitMsg(params) {
  return request(service.torder.queryTaskSitMsg, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryTaskNumByGroupUser(params) {
  return request(service.fuser.queryTaskNumByGroup, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryGroupRqrmnt(params) {
  return request(service.ftask.queryGroupRqrmnt, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function getMergedInfo(params) {
  return request(service.fgitwork.getMergedInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryNumByType(params) {
  return request(service.fcomponent.queryNumByType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryGitlabCommitInfo(params) {
  return request(service.fgitwork.queryGitlabCommitInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryDataByType(params) {
  return request(service.fcomponent.queryDataByType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryIssueData(params) {
  return request(service.fcomponent.queryIssueData, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryQrmntsData(params) {
  return request(service.fcomponent.queryQrmntsData, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryIssueDelay(params) {
  return request(service.fcomponent.queryIssueDelay, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryallComName(params) {
  return request(service.fcomponent.queryallComName, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function taskCardDisplay(params) {
  return request(service.ftask.taskCardDisplay, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryGitlabCommitDetail(params) {
  return request(service.fgitwork.queryGitlabCommitDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryReviewList(params) {
  return request(service.ftask.queryReviewList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryReviewListDemand(params) {
  return request(service.fdemand.queryReviewList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
