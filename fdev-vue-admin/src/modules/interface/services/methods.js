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

// 通过id查询所有前端项目
export const queryApps = commonRequest(services.fapp.queryApps);

export const queryType = commonRequest(services.fapp.typeQuery);

// 查询应用信息
export const query = commonRequest(services.fapp.appQuery);

export const queryGroup = commonRequest(services.fapp.groupQuery);

// 接口列表
export const queryInterfaceList = commonRequest(
  services.finterface.queryInterfaceList
);

// 接口关系 列表
export const queryInterfaceRelation = commonRequest(
  services.finterface.queryInterfaceRelation
);

// 接口详情
export const queryInterfaceDetailById = commonRequest(
  services.finterface.queryInterfaceDetailById
);

//  通过接口id查询接口版本
export const queryInterfaceVersions = commonRequest(
  services.finterface.queryInterfaceVersions
);

// 接口/交易扫描
export const scanInterface = commonRequest(services.finterface.scanInterface);

// 生成接口对外链接
export const getInterfacesUrl = commonRequest(
  services.finterface.getInterfacesUrl
);

// 增加/删除交易标签
export async function transTags(params) {
  return request(services.finterface.updateTransTags, {
    method: 'POST',
    data: {
      id: params.id,
      tags: params.tags
    }
  });
}

// 扫描分支
export const getProjectBranchList = commonRequest(
  services.fapp.getProjectBranchList
);

// 查询分支
export const queryTransList = commonRequest(services.finterface.queryTransList);

// 查询交易详情
export const queryTransDetailById = commonRequest(
  services.finterface.queryTransDetailById
);

// 通过交易id查询交易版本
export const queryTransVersions = commonRequest(
  services.finterface.queryTransByVersion
);

// 通过交易版本查询交易信息
export const queryTransByVersion = commonRequest(
  services.finterface.queryTransByVersion
);

// 查询tags权限
export const queryTagsRole = commonRequest(services.finterface.isAppManager);

// 对外链接--接口列表查询
export const queryInterfacesList = commonRequest(
  services.finterface.queryInterfacesList
);

// 对外链接--接口详情查询
export const getInterfaceDetailById = commonRequest(
  services.finterface.getInterfaceDetailById
);

// 扫描--任务模块
export const taskScanInterface = commonRequest(
  services.finterface.taskScanInterface
);

// 扫描--判断用户是否为任务负责人、行内项目负责人、开发人员
export const isTaskManager = commonRequest(services.finterface.isTaskManager);

// 扫描--判断用户是否为应用负责人
export const isAppManager = commonRequest(services.finterface.isAppManager);

// 应用接口扫描详情
export const queryScanRecord = commonRequest(
  services.finterface.queryScanRecord
);

// 应用接口扫描详情
export async function queryTransRelation(params) {
  return request(services.finterface.queryTransRelation, {
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
export const downloadRestRelationExcel = commonRequest(
  services.finterface.downloadRestRelationExcel
);

// 更新接口详情
export const updateParamDescription = commonRequest(
  services.finterface.updateParamDescription
);

//查询接口申请列表
export const queryApplicationList = commonRequest(
  services.finterface.queryApplicationList
);

// 更新交易详情
export const modifyParamDescription = commonRequest(
  services.finterface.modifyParamDescription
);

//更新接口申请审核状态
export const updateApplicationStatus = commonRequest(
  services.finterface.updateApplicationStatus
);

// 接口调用申请
export const interfaceCallRequest = commonRequest(
  services.finterface.interfaceCallRequest
);

// 接口申请的权限
export const isManagers = commonRequest(services.finterface.isManagers);

// 查询是否有未申请的接口
export const queryIsNoApplyInterface = commonRequest(
  services.finterface.queryIsNoApplyInterface
);

//fdev接口统计
export const queryInterfaceStatistics = commonRequest(
  services.finterface.queryInterfaceStatistics
);

// 查询路由列表
export const queryRoutes = commonRequest(services.finterface.queryRoutes);

// 查询路由调用关系
export const queryRoutesRelation = commonRequest(
  services.finterface.queryRoutesRelation
);

// 查询路由详情
export const queryRoutesDetail = commonRequest(
  services.finterface.queryRoutesDetail
);

// 查询路由详情
export const getServiceChainInfo = commonRequest(
  services.finterface.getServiceChainInfo
);

// 查询应用路由配置介质列表
export const queryAppJsonList = commonRequest(
  services.finterface.queryAppJsonList
);

export const queryYapiList = commonRequest(services.finterface.queryYapiList);

// 查询总路由配置介质列表
export const queryTotalJsonList = commonRequest(
  services.finterface.queryTotalJsonList
);

export const importYapiProject = commonRequest(
  services.finterface.importYapiProject
);

// 查询总路由配置介质历史记录
export const queryTotalJsonHistory = commonRequest(
  services.finterface.queryTotalJsonHistory
);

export const convertJsonSchema = commonRequest(
  services.finterface.convertJsonSchema
);

// 查询各个版本的路由详情
export const queryRoutesDetailVer = commonRequest(
  services.finterface.queryRoutesDetailVer
);

export const queryYapiDetail = commonRequest(
  services.finterface.queryYapiDetail
);

export const deleteYapiProject = commonRequest(
  services.finterface.deleteYapiProject
);

export const deleteYapiInterface = commonRequest(
  services.finterface.deleteYapiInterface
);

export const importYapiInterface = commonRequest(
  services.finterface.importYapiInterface
);
