import request from '@/utils/request';
import service from './serviceMap';

//查询Caas生产信息详情
export async function queryCaasInfo(params) {
  return request(service.fblue.queryCaasInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询Caas生产信息详情
export async function querySCCInfo(params) {
  return request(service.fblue.querySCCInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询集群信息
export async function queryClusters(params) {
  return request(service.fblue.queryClusters, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询集群生产信息
export async function proInfo(params) {
  return request(service.fblue.proInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询应用生产信息列表
export async function queryDeployments(params) {
  return request(service.fblue.queryDeployments, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 获取验证码
export async function getVerifyCode(params) {
  return request(service.fenvconfig.getVerifyCode, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 实体列表-查询
export async function queryModel(params) {
  return request(service.fenvconfig.modelQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 实体模板详情
export async function queryModelTemp(params) {
  return request(service.fenvconfig.modelTempQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 实体列表-新增
export async function addModel(params) {
  return request(service.fenvconfig.modelAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 实体模板-新增
export async function addModelTemp(params) {
  return request(service.fenvconfig.modelTempAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 实体列表-更新
export async function updateModel(params) {
  return request(service.fenvconfig.modelUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 确定调用接口
export async function saveSitConfigProperties(params) {
  return request(service.fenvconfig.saveSitConfigProperties, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryEnvList(params) {
  return request(service.fenvconfig.envQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 根据应用查环境列表
export async function queryEnvListByAppId(params) {
  return request(service.fenvconfig.queryEnvByAppId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 实体环境列表--查询
export async function queryModelEvn(params) {
  return request(service.fenvconfig.varQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 实体环境列表--删除
export async function deleteAModelEvn(params) {
  return request(service.fenvconfig.varDelete, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 实体列表-删除
export async function deleteModel(params) {
  return request(service.fenvconfig.modelDelete, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function deleteEnvList(params) {
  return request(service.fenvconfig.envDelete, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateEnvList(params) {
  return request(service.fenvconfig.envUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addEnvList(params) {
  return request(service.fenvconfig.envAdd, {
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
export async function queryModleEnvDetail(params) {
  return request(service.fenvconfig.varQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询配置依赖
export async function confDependList(params) {
  return request(service.fenvconfig.queryConfigDependency, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询分类表常量信息
export async function queryModelConst(params) {
  return request(service.fenvconfig.queryModelCategory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//导出配置依赖
export async function confDependency(params) {
  return request(service.fenvconfig.exportDependencySearchResult, {
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
export async function getEnvWithModel(params) {
  return request(service.fenvconfig.copyModelEnv, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//复制环境及它的一套实体
export async function copyEnvWithModel(params) {
  return request(service.fenvconfig.updatyeCopyModelEnv, {
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
export async function queryVarByEnvAndType(params) {
  return request(service.fenvconfig.queryVarByEnvAndType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryAutoEnv(params) {
  return request(service.fenvconfig.queryAutoEnv, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function saveModelEnv(params) {
  return request(service.fenvconfig.saveModelEnv, {
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

export async function queryModelMessage(params) {
  return request(service.fenvconfig.queryModelMessage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function compare(params) {
  return request(service.fenvconfig.compare, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function finish(params) {
  return request(service.fenvconfig.finish, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function downloadAppInfo(params) {
  return request(service.fenvconfig.downloadAppInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function updateModelMessage(params) {
  return request(service.fenvconfig.updateModelMessage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function cancelModelMessage(params) {
  return request(service.fenvconfig.cancelModelMessage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryModelSetList(params) {
  return request(service.fenvconfig.queryModelSetList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function deleteModelSet(params) {
  return request(service.fenvconfig.deleteModelSet, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function getType(params) {
  return request(service.fenvconfig.getType, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function getModles(params) {
  return request(service.fenvconfig.getModles, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function saveModel(params) {
  return request(service.fenvconfig.saveModels, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function updateModels(params) {
  return request(service.fenvconfig.updateModels, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryAppProInfo(params) {
  return request(service.fenvconfig.queryAppProInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function getJsonSchema(params) {
  return request(service.fenvconfig.getJsonSchema, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryDeploy(params) {
  return request(service.fenvconfig.queryDeploy, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function bindAppInfo(params) {
  return request(service.fenvconfig.bindAppInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryAllLabels(params) {
  return request(service.fenvconfig.queryAllLabels, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryEnvKey(params) {
  return request(service.fenvconfig.queryEnvKey, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryDeployDetail(params) {
  return request(service.fenvconfig.queryDeployDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryRealTimeBindMsg(params) {
  return request(service.fenvconfig.queryRealTimeBindMsg, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryByLabelsFuzzy(params) {
  return request(service.fenvconfig.queryByLabelsFuzzy, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryProEnvByAppId(params) {
  return request(service.fenvconfig.queryProEnvByAppId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function pageQuery(params) {
  return request(service.fenvconfig.pageQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryModelEnvByValue(params) {
  return request(service.fenvconfig.queryModelEnvByValue, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//dock用户名密码校验
export async function checkConnectionDocker(params) {
  return request(service.fenvconfig.checkConnectionDocker, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//实体列表查询--后端分页
export async function queryPage(params) {
  return request(service.fenvconfig.queryPage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//实体模板列表查询--后端分页
export async function queryTempPage(params) {
  return request(service.fenvconfig.queryTempPage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//获取实体组模板包含的实体
export async function queryTemplateContainsModel(params) {
  return request(service.fenvconfig.queryTemplateContainsModel, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//获取实体与环境映射历史修改数据
export async function getMappingHistoryList(params) {
  return request(service.fenvconfig.getMappingHistoryList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//获取实体与环境映射修改详情
export async function getMappingHistoryDetail(params) {
  return request(service.fenvconfig.getMappingHistoryDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryEnvKeyList(params) {
  return request(service.fenvconfig.queryEnvKeyList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryConfigModel(params) {
  return request(service.fenvconfig.queryConfigModel, {
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

export async function batchPreviewConfigFile(params) {
  return request(service.fenvconfig.batchPreviewConfigFile, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//根据实体英文名或环境英文名查询实体环境映射值
export async function queryModelEnvByModelNameEn(params) {
  return request(service.fenvconfig.queryModelEnvByModelNameEn, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
