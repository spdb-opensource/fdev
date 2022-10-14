import request from '@/utils/request';
import service from './serviceMap';

// 接口列表
export async function queryInterfaceList(params) {
  return request(service.finterface.queryInterfaceList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 接口关系 列表
export async function queryInterfaceRelation(params) {
  return request(service.finterface.queryInterfaceRelation, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 接口详情
export async function queryInterfaceDetailById(params) {
  return request(service.finterface.queryInterfaceDetailById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//  通过接口id查询接口版本
export async function queryInterfaceVersions(params) {
  return request(service.finterface.queryInterfaceVersions, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 接口/交易扫描
export async function scanInterface(params) {
  return request(service.finterface.scanInterface, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 生成接口对外链接
export async function getInterfacesUrl(params) {
  return request(service.finterface.getInterfacesUrl, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 增加/删除交易标签
export async function transTags(params) {
  return request(service.finterface.updateTransTags, {
    method: 'POST',
    data: {
      id: params.id,
      tags: params.tags
    }
  });
}
// 扫描分支
export async function getProjectBranchList(params) {
  return request(service.fapp.getProjectBranchList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询分支
export async function queryTransList(params) {
  return request(service.finterface.queryTransList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 交易列表导出
export async function exportTransList(params) {
  return request(service.finterface.exportTransList, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
// 查询交易详情
export async function queryTransDetailById(params) {
  return request(service.finterface.queryTransDetailById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 通过交易id查询交易版本
export async function queryTransVersions(params) {
  return request(service.finterface.queryTransByVersion, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 通过交易版本查询交易信息
export async function queryTransByVersion(params) {
  return request(service.finterface.queryTransByVersion, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// // 查询tags权限
export async function queryTagsRole(params) {
  return request(service.finterface.isAppManager, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 对外链接--接口列表查询
export async function queryInterfacesList(params) {
  return request(service.finterface.queryInterfacesList, {
    method: 'GET',
    params: {
      ...params
    }
  });
}
// 对外链接--接口详情查询
export async function getInterfaceDetailById(params) {
  return request(service.finterface.getInterfaceDetailById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 扫描--任务模块
export async function taskScanInterface(params) {
  return request(service.finterface.taskScanInterface, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 扫描--判断用户是否为任务负责人、行内项目负责人、开发人员
export async function isTaskManager(params) {
  return request(service.finterface.isTaskManager, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 扫描--判断用户是否为应用负责人
export async function isAppManager(params) {
  return request(service.finterface.isAppManager, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 应用接口扫描详情
export async function queryScanRecord(params) {
  return request(service.finterface.queryScanRecord, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 应用接口扫描详情
export async function queryTransRelation(params) {
  return request(service.finterface.queryTransRelation, {
    method: 'POST',
    data: {
      page: params.page,
      pageNum: params.rowsPerPage,
      transId: params.transId ? params.transId.split(',') : [],
      branchDefault: params.branchDefault,
      branch: params.branch,
      serviceCalling: params.serviceCalling,
      serviceId: params.serviceId,
      channel: params.channel
    }
  });
}
//下载调用关系
export async function downloadRestRelationExcel(params) {
  return request(service.finterface.downloadRestRelationExcel, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 更新接口详情
export async function updateParamDescription(params) {
  return request(service.finterface.updateParamDescription, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询接口申请列表
export async function queryApplicationList(params) {
  return request(service.finterface.queryApplicationList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 更新交易详情
export async function modifyParamDescription(params) {
  return request(service.finterface.modifyParamDescription, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//更新接口申请审核状态
export async function updateApplicationStatus(params) {
  return request(service.finterface.updateApplicationStatus, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 接口调用申请
export async function interfaceCallRequest(params) {
  return request(service.finterface.interfaceCallRequest, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 接口申请的权限
export async function isManagers(params) {
  return request(service.finterface.isManagers, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询是否有未申请的接口
export async function queryIsNoApplyInterface(params) {
  return request(service.finterface.queryIsNoApplyInterface, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//fdev接口统计
export async function queryInterfaceStatistics(params) {
  return request(service.finterface.queryInterfaceStatistics, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询路由列表
export async function queryRoutes(params) {
  return request(service.finterface.queryRoutes, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询路由调用关系
export async function queryRoutesRelation(params) {
  return request(service.finterface.queryRoutesRelation, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询路由详情
export async function queryRoutesDetail(params) {
  return request(service.finterface.queryRoutesDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询路由详情
export async function getServiceChainInfo(params) {
  return request(service.finterface.getServiceChainInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询应用路由配置介质列表
export async function queryAppJsonList(params) {
  return request(service.finterface.queryAppJsonList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryYapiList(params) {
  return request(service.finterface.queryYapiList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询总路由配置介质列表
export async function queryTotalJsonList(params) {
  return request(service.finterface.queryTotalJsonList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function importYapiProject(params) {
  return request(service.finterface.importYapiProject, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询总路由配置介质历史记录
export async function queryTotalJsonHistory(params) {
  return request(service.finterface.queryTotalJsonHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function convertJsonSchema(params) {
  return request(service.finterface.convertJsonSchema, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询各个版本的路由详情
export async function queryRoutesDetailVer(params) {
  return request(service.finterface.queryRoutesDetailVer, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryYapiDetail(params) {
  return request(service.finterface.queryYapiDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteYapiProject(params) {
  return request(service.finterface.deleteYapiProject, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteYapiInterface(params) {
  return request(service.finterface.deleteYapiInterface, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function importYapiInterface(params) {
  return request(service.finterface.importYapiInterface, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
