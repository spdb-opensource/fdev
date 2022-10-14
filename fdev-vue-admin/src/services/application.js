import request from '@/utils/request';
import service from './serviceMap';

// 查询应用信息
export async function query(params) {
  return request(service.fapp.appQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询gitlabID
export async function queryProject(params) {
  return request(service.fapp.queryProject, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询group是否存在
export async function getGroup(params) {
  return request(service.fapp.getGroup, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 根据应用id查询所有分支
export async function queryAllBranch(params) {
  return request(service.fapp.queryAllBranch, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 环境实体列表
export async function getEnvModelList(params) {
  return request(service.fenvconfig.modelQueryFuzz, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 获取环境全量信息
export async function getModelConstant(params) {
  return request(service.fenvconfig.queryModelCategory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 获取环境列表
export async function getEnvList(params) {
  return request(service.fenvconfig.envQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 根据应用获取环境列表
export async function getEnvListByAppId(params) {
  return request(service.fenvconfig.queryEnvByAppId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function previewConfigFile(params) {
  return request(service.fenvconfig.previewConfigFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 自定义部署
export async function createPipeline(params) {
  return request(service.fapp.createPipeline, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function definedDeploy(params) {
  return request(service.fenvconfig.definedDeploy, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryConfigTemplate(params) {
  return request(service.fenvconfig.queryConfigTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryModelList(params) {
  return request(service.fenvconfig.modelQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询自己应用的私有实体
export async function queryExcludePirvateModelList(params) {
  return request(service.fenvconfig.pirvateModelQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 异步新增应用
export async function saveByAsync(params) {
  return request(service.fapp.saveByAsync, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 通过环境实体id查询环境信息
export async function queryApplicationName(params) {
  return request(service.fapp.queryAppNameEnByGitlabProjectId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
