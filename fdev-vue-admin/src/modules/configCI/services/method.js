import request from '@/utils/request';
import service from './api';

//插件列表
export function queryPlugin(params) {
  return request(service.fcipipeline.queryPlugin, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//删除插件
export function delPlugin(params) {
  return request(service.fcipipeline.delPlugin, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增自定义插件
export function addPlugin(params) {
  return request(service.fcipipeline.addPlugin, {
    method: 'POST',
    data: params
  });
}

// 查询我收藏的流水线列表
export function queryCollectionPipelineList(params) {
  return request(service.fcipipeline.queryCollectionPipelineList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//编辑插件
export function editPlugin(params) {
  return request(service.fcipipeline.editPlugin, {
    method: 'POST',
    data: params
  });
}

// 查询我的流水线列表
export function queryMinePipelineList(params) {
  return request(service.fcipipeline.queryMinePipelineList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询全部流水线列表
export function queryAllPipelineList(params) {
  return request(service.fcipipeline.queryAllPipelineList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 流水线删除
export function deletePipeline(params) {
  return request(service.fcipipeline.deletePipeline, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//流水线关注/取消关注
export function updateFollowStatus(params) {
  return request(service.fcipipeline.updateFollowStatus, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询应用fdev-ci日志列表
export function queryFdevciLogList(params) {
  return request(service.fcipipeline.queryFdevciLogList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询我的流水线模板列表
export function queryMinePipelineTemplateList(params) {
  return request(service.fcipipeline.queryMinePipelineTemplateList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 模板删除
export function delTemplate(params) {
  return request(service.fcipipeline.delTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询应用所属流水线列表
export function queryAppPipelineList(params) {
  return request(service.fcipipeline.queryAppPipelineList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export function queryPluginHistory(params) {
  return request(service.fcipipeline.queryPluginHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询草稿
export function queryReadDraft(params) {
  return request(service.fcipipeline.queryReadDraft, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询流水线详情
export function queryPipelineDetailById(params) {
  return request(service.fcipipeline.queryPipelineDetailById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询流水线模板详情
export function queryPipelineTempDetailById(params) {
  return request(service.fcipipeline.queryPipelineTempDetailById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增流水线模板
export function pipelineTemplateAdd(params) {
  return request(service.fcipipeline.pipelineTemplateAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增流水线
export function pipelineAdd(params) {
  return request(service.fcipipeline.pipelineAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//升级流水线模板
export function pipelineTemplateUpdate(params) {
  return request(service.fcipipeline.pipelineTemplateUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//升级流水线
export function pipelineUpdate(params) {
  return request(service.fcipipeline.pipelineUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询流水线运行详情
export function pipelineLogDetail(params) {
  return request(service.fcipipeline.pipelineLogDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//存储持续集成草稿
export function CIdraftSave(params) {
  return request(service.fcipipeline.CIdraftSave, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//手动触发流水线
export function triggerPipeline(params) {
  return request(service.fcipipeline.triggerPipeline, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询镜像列表
export function queryImageList(params) {
  return request(service.fcipipeline.queryImageList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询工作环境镜像列表
export function queryToolImageList(params) {
  return request(service.fcipipeline.queryToolImageList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//镜像新增
export function addImage(params) {
  return request(service.fcipipeline.addImage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//更新镜像
export function updateImage(params) {
  return request(service.fcipipeline.updateImage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function queryLogDetailById(params) {
  return request(service.fcipipeline.queryLogDetailById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//重跑pipeline
export function retryPipeline(params) {
  return request(service.fcipipeline.retryPipeline, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//根据流水线Id查分支列表
export function queryBranchesByPipelineId(params) {
  return request(service.fcipipeline.queryBranchesByPipelineId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询插件分类
export function queryCategory(params) {
  return request(service.fcipipeline.queryCategory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询插件详情
export function queryPluginDetail(params) {
  return request(service.fcipipeline.queryPluginDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//复制流水线
export function copyPipeline(params) {
  return request(service.fcipipeline.copyPipeline, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//重试job
export function retryJob(params) {
  return request(service.fcipipeline.retryJob, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//流水线另存为模板
export function saveAsPipelineTemplate(params) {
  return request(service.fcipipeline.saveAsPipelineTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 应用模块--分页列表查询
export function queryPagination(params) {
  return request(service.fapp.queryPagination, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询当前用户负责的应用列表
export function queryDutyAppBaseInfo(params) {
  return request(service.fapp.queryDutyAppBaseInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export function queryCurrent() {
  return request(service.fuser.currentUser, {
    method: 'POST',
    data: {}
  });
}

export function queryAbandonDetail(params) {
  return request(service.fapp.findById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export function queryEnvList(params) {
  return request(service.fentity.queryEnvList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 实体列表-查询
// export function queryEntityModel(params) {
//   return request(service.fentity.queryEntityModel, {
//     method: 'POST',
//     data: {
//       ...params
//     }
//   });
// }
// 查询当前应用有权限的所有实体列表
export function querySectionEntity(params) {
  return request(service.fentity.querySectionEntity, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//实体模板列表查询--后端分页
export function queryTemplate(params) {
  return request(service.fentity.queryTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//根据实体英文名或环境英文名查询实体环境映射值
export function queryModelEnvByModelNameEn(params) {
  return request(service.fentity.queryModelEnvByModelNameEn, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 实体模板详情
export function queryModelTemp(params) {
  return request(service.fentity.modelTempQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询markDown地址
export function queryMarkDown(params) {
  return request(service.fcipipeline.queryMarkDown, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//停止流水线执行
export function stopPipeline(params) {
  return request(service.fcipipeline.stopPipeline, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//停止单个job
export function stopJob(params) {
  return request(service.fcipipeline.stopJob, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 复制流水线模版
export function copyPipelineTemplate(params) {
  return request(service.fcipipeline.copyPipelineTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function queryModelTemplateByContent(params) {
  return request(service.fcipipeline.queryModelTemplateByContent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function queryTypeAndContent(params) {
  return request(service.fcipipeline.queryTypeAndContent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function getMinioInfo(params) {
  return request(service.fcipipeline.getMinioInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function saveToMinio(params) {
  return request(service.fcipipeline.saveToMinio, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function getAllJobs(params) {
  return request(service.fcipipeline.getAllJobs, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function getFullJobsByIds(params) {
  return request(service.fcipipeline.getFullJobsByIds, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function getAllRunnerCluster(params) {
  return request(service.fcipipeline.getAllRunnerCluster, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function queryTriggerRules(params) {
  return request(service.fcipipeline.queryTriggerRules, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function updateTriggerRules(params) {
  return request(service.fcipipeline.updateTriggerRules, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export function queryEntityModelDetail(params) {
  return request(service.fentity.queryEntityModelDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 新增插件fileEdit类型的参数yaml
export function addYamlConfig(params) {
  return request(service.fcipipeline.addYamlConfig, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 插件fileEdit类型的参数yaml按id查询
export function getYamlConfigById(params) {
  return request(service.fcipipeline.getYamlConfigById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 修改模板可见范围
export function updateVisibleRange(params) {
  return request(service.fcipipeline.updateVisibleRange, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 请求插件结果
export function queryPluginResultData(params) {
  return request(service.fcipipeline.queryPluginResultData, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 删除任务组模板
export function delJobsTemplate(params) {
  return request(service.fcipipeline.delJobsTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 修改任务组模板可见范围
export function updateJobsVisibleRange(params) {
  return request(service.fcipipeline.updateJobsVisibleRange, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function jobsUpdate(params) {
  return request(service.fcipipeline.jobsUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export function addJobsTemplate(params) {
  return request(service.fcipipeline.addJobsTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export function getRunnerClusterInfoByParam(params) {
  return request(service.fcipipeline.getRunnerClusterInfoByParam, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export function addRunnerCluster(params) {
  return request(service.fcipipeline.addRunnerCluster, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export function getAllRunnerInfo(params) {
  return request(service.fcipipeline.getAllRunnerInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//删除，更新runnerCluster集群
export function updateRunnerCluster(params) {
  return request(service.fcipipeline.updateRunnerCluster, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//获取流水线历史版本信息
export function getPipelineHistoryVersion(params) {
  return request(service.fcipipeline.getPipelineHistoryVersion, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//流水线版本回退
export function setPipelineRollBack(params) {
  return request(service.fcipipeline.setPipelineRollBack, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//下载制品
export function downLoadArtifacts(params) {
  return request(
    service.fcipipeline.downLoadArtifacts + `?objectName=${params}`,
    {
      method: 'GET',
      responseType: 'blob'
    }
  );
}

// 查询环境-queryEnv
export function queryEnv(params) {
  return request(service.fentity.queryEnv, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增实体模型映射
export function addEntityModelClass(params) {
  return request(service.fentity.addEntityModelClass, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询实体模型列表
export function queryEntityModel(params) {
  return request(service.fentity.queryEntityModel, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询应用列表
export function queryApps(params) {
  return request(service.fservice.queryApps, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询系统列表
export function querySystem(params) {
  return request(service.fservice.querySystem, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询用户列表
export function queryUserCoreData(params) {
  return request(service.fuser.queryUserCoreData, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 应用列表分页查询
export function queryAppList(params) {
  return request(service.fservice.queryApps, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查用户所属条线
export function queryUserGroup(params) {
  return request(service.fentity.queryUserGroup, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 新建实体
export function addEntity(params) {
  return request(service.fentity.addEntity, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 复制实体
export function copyEntity(params) {
  return request(service.fentity.copyEntity, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 校验实体是否重复
export function checkEntity(params) {
  return request(service.fentity.checkEntity, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 根据id查实体模板详情
export function queryTemplateById(params) {
  return request(service.fentity.queryTemplateById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 新增实体映射
export function addEntityClass(params) {
  return request(service.fentity.addEntityClass, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 编辑实体映射
export function updateEntityClass(params) {
  return request(service.fentity.updateEntityClass, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 编辑实体-updateEntity
export function updateEntity(params) {
  return request(service.fentity.updateEntity, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 删除实体映射
export function deleteEntityClass(params) {
  return request(service.fentity.deleteEntityClass, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查看实体操作日志信息
export function queryEntityLog(params) {
  return request(service.fentity.queryEntityLog, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查看实体操作日志信息（映射值）
export function queryMapLogList(params) {
  return request(service.fentity.queryMapLogList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 获取依赖分析
export function queryConfigDependency(params) {
  return request(service.fentity.queryConfigDependency, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询当前用户是否可查看应用详情
export function queryIsShow(params) {
  return request(service.fservice.queryIsShow, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 获取部署依赖
export function queryDeployDependency(params) {
  return request(service.fentity.queryDeployDependency, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 删除实体
export function deleteEntity(params) {
  return request(service.fentity.deleteEntity, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询环境列表
export function queryEnvTypeList(params) {
  return request(service.fentity.queryEnvTypeList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
