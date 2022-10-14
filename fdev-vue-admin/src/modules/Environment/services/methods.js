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

export async function queryCurrent() {
  return request(services.fuser.currentUser, {
    method: 'POST',
    data: {}
  });
}

// 查询应用信息
export const query = commonRequest(services.fapp.appQuery);

// 通过id查询所有前端项目
export const queryApps = commonRequest(services.fapp.queryApps);

// 按条价查询用户
export const queryAll = commonRequest(services.fuser.userQuery);

// 查询应用信息
export const queryDeploymentDetails = commonRequest(
  services.fenvconfig.queryDeploymentDetails
);

//同步生产信息
export async function proinfo(params) {
  return request(services.fenvconfig.proinfo, {
    method: 'GET',
    data: {
      ...params
    }
  });
}

//查询生产信息
export const queryDeployments = commonRequest(
  services.fenvconfig.queryDeployments
);

// 获取验证码
export const getVerifyCode = commonRequest(services.fenvconfig.getVerifyCode);

// 实体列表-查询
export const queryModel = commonRequest(services.fenvconfig.modelQuery);

// 实体模板详情
export const queryModelTemp = commonRequest(services.fenvconfig.modelTempQuery);

// 实体列表-新增
export const addModel = commonRequest(services.fenvconfig.modelAdd);

// 实体模板-新增
export const addModelTemp = commonRequest(services.fenvconfig.modelTempAdd);

// 实体列表-更新
export const updateModel = commonRequest(services.fenvconfig.modelUpdate);

// 确定调用接口
export const saveSitConfigProperties = commonRequest(
  services.fenvconfig.saveSitConfigProperties
);

// 环境列表
export const queryEnvList = commonRequest(services.fenvconfig.envQuery);

// 根据应用查环境列表
export const queryEnvListByAppId = commonRequest(
  services.fenvconfig.queryEnvByAppId
);

// 实体环境列表--查询
export const queryModelEvn = commonRequest(services.fenvconfig.varQuery);

// 实体环境列表--删除
export const deleteAModelEvn = commonRequest(services.fenvconfig.varDelete);

// 实体列表-删除
export const deleteModel = commonRequest(services.fenvconfig.modelDelete);

export const deleteEnvList = commonRequest(services.fenvconfig.envDelete);

export const updateEnvList = commonRequest(services.fenvconfig.envUpdate);

export async function addEnvList(params) {
  return request(services.fenvconfig.envAdd, {
    method: 'POST',
    data: {
      ...params,
      name_en: params.env_name_en,
      name_cn: params.env_name_cn,
      desc: params.env_message
    }
  });
}

// 根据 环境id和实体id 查询实体属性对应的值
export const queryModleEnvDetail = commonRequest(services.fenvconfig.varQuery);

//查询配置依赖
export const confDependList = commonRequest(
  services.fenvconfig.queryConfigDependency
);

//查询分类表常量信息
export const queryModelConst = commonRequest(
  services.fenvconfig.queryModelCategory
);

//导出配置依赖
export async function confDependency(params) {
  return request(services.fenvconfig.exportDependencySearchResult, {
    method: 'POST',
    data: {
      ...params,
      range: params.range,
      model_name_en: params.name_en.name_en,
      field_name_en: params.field_name_en.name_en
    }
  });
}

//获取实体与环境映射
export const getEnvWithModel = commonRequest(
  services.fenvconfig.queryModelCacopyModelEnvtegory
);

//复制环境及它的一套实体
export async function copyEnvWithModel(params) {
  return request(services.fenvconfig.updatyeCopyModelEnv, {
    method: 'POST',
    data: {
      ...params,
      env: {
        id: params.env.name_cn.id,
        name_cn: params.env.name_cn.name_cn,
        name_en: params.env.name_cn.name_en,
        desc: params.env.desc
      }
    }
  });
}

// 根据环境和实体类型查询实体与环境映射信息
export const queryVarByEnvAndType = commonRequest(
  services.fenvconfig.queryVarByEnvAndType
);

export const queryAutoEnv = commonRequest(services.fenvconfig.queryAutoEnv);

export async function saveModelEnv(params) {
  return request(services.fenvconfig.saveModelEnv, {
    method: 'POST',
    data: {
      ...params,
      model_id: params.model.id || '',
      model_name_en: params.model.name_en || params.model_name_en,
      model_name_cn: params.model.name_cn || '',
      env_id: params.env.id || '',
      env_name_en: params.env.name_en || params.env_name_en,
      env_name_cn: params.env.name_cn || ''
    }
  });
}

export const queryModelMessage = commonRequest(
  services.fenvconfig.queryModelMessage
);

export const compare = commonRequest(services.fenvconfig.compare);

export const finish = commonRequest(services.fenvconfig.finish);

export const downloadAppInfo = commonRequest(
  services.fenvconfig.downloadAppInfo
);

export const updateModelMessage = commonRequest(
  services.fenvconfig.updateModelMessage
);

export const cancelModelMessage = commonRequest(
  services.fenvconfig.cancelModelMessage
);

export const queryModelSetList = commonRequest(
  services.fenvconfig.queryModelSetList
);

export const deleteModelSet = commonRequest(services.fenvconfig.deleteModelSet);

export const getType = commonRequest(services.fenvconfig.getType);

export const getModles = commonRequest(services.fenvconfig.getModles);

export const saveModel = commonRequest(services.fenvconfig.saveModels);

export const updateModels = commonRequest(services.fenvconfig.updateModels);

export const queryAppProInfo = commonRequest(
  services.fenvconfig.queryAppProInfo
);

export const getJsonSchema = commonRequest(services.fenvconfig.getJsonSchema);

export const queryDeploy = commonRequest(services.fenvconfig.queryDeploy);

export const bindAppInfo = commonRequest(services.fenvconfig.bindAppInfo);

export const queryAllLabels = commonRequest(services.fenvconfig.queryAllLabels);

export const queryEnvKey = commonRequest(services.fenvconfig.queryEnvKey);

export const queryDeployDetail = commonRequest(
  services.fenvconfig.queryDeployDetail
);

export const queryRealTimeBindMsg = commonRequest(
  services.fenvconfig.queryRealTimeBindMsg
);

export const queryByLabelsFuzzy = commonRequest(
  services.fenvconfig.queryByLabelsFuzzy
);

export const queryProEnvByAppId = commonRequest(
  services.fenvconfig.queryProEnvByAppId
);

export const pageQuery = commonRequest(services.fenvconfig.pageQuery);

export const queryModelEnvByValue = commonRequest(
  services.fenvconfig.queryModelEnvByValue
);

//dock用户名密码校验
export const checkConnectionDocker = commonRequest(
  services.fenvconfig.checkConnectionDocker
);

//实体列表查询--后端分页
export const queryPage = commonRequest(services.fenvconfig.queryPage);

//实体模板列表查询--后端分页
export const queryTempPage = commonRequest(services.fenvconfig.queryTempPage);

//获取实体组模板包含的实体
export const queryTemplateContainsModel = commonRequest(
  services.fenvconfig.queryTemplateContainsModel
);

//获取实体与环境映射历史修改数据
export const getMappingHistoryList = commonRequest(
  services.fenvconfig.getMappingHistoryList
);

//获取实体与环境映射修改详情
export const getMappingHistoryDetail = commonRequest(
  services.fenvconfig.getMappingHistoryDetail
);

export const queryEnvKeyList = commonRequest(
  services.fenvconfig.queryEnvKeyList
);

export const queryConfigModel = commonRequest(
  services.fenvconfig.queryConfigModel
);

export const previewConfigFile = commonRequest(
  services.fenvconfig.previewConfigFile
);

export const batchPreviewConfigFile = commonRequest(
  services.fenvconfig.batchPreviewConfigFile
);

//根据实体英文名或环境英文名查询实体环境映射值
export const queryModelEnvByModelNameEn = commonRequest(
  services.fenvconfig.queryModelEnvByModelNameEn
);
