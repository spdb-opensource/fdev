import request from '@/utils/request';
import service from './serviceMap';

//插件列表
export async function queryPlugin(params) {
  return request(service.fconfigci.queryPlugin, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//删除插件
export async function delPlugin(params) {
  return request(service.fconfigci.delPlugin, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增自定义插件
export async function addPlugin(params) {
  return request(service.fconfigci.addPlugin, {
    method: 'POST',
    data: params
  });
}

// 查询我收藏的流水线列表
export async function queryCollectionPipelineList(params) {
  return request(service.fconfigci.queryCollectionPipelineList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//编辑插件
export async function editPlugin(params) {
  return request(service.fconfigci.editPlugin, {
    method: 'POST',
    data: params
  });
}

// 查询我的流水线列表
export async function queryMinePipelineList(params) {
  return request(service.fconfigci.queryMinePipelineList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询全部流水线列表
export async function queryAllPipelineList(params) {
  return request(service.fconfigci.queryAllPipelineList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 流水线删除
export async function deletePipeline(params) {
  return request(service.fconfigci.deletePipeline, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//流水线关注/取消关注
export async function updateFollowStatus(params) {
  return request(service.fconfigci.updateFollowStatus, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询应用fdev-ci日志列表
export async function queryFdevciLogList(params) {
  return request(service.fconfigci.queryFdevciLogList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询我的流水线模板列表
export async function queryMinePipelineTemplateList(params) {
  return request(service.fconfigci.queryMinePipelineTemplateList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 模板删除
export async function delTemplate(params) {
  return request(service.fconfigci.delTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询应用所属流水线列表
export async function queryAppPipelineList(params) {
  return request(service.fconfigci.queryAppPipelineList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryPluginHistory(params) {
  return request(service.fconfigci.queryPluginHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询草稿
export async function queryReadDraft(params) {
  return request(service.fconfigci.queryReadDraft, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询流水线详情
export async function queryPipelineDetailById(params) {
  return request(service.fconfigci.queryPipelineDetailById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询流水线模板详情
export async function queryPipelineTempDetailById(params) {
  return request(service.fconfigci.queryPipelineTempDetailById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增流水线模板
export async function pipelineTemplateAdd(params) {
  return request(service.fconfigci.pipelineTemplateAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//新增流水线
export async function pipelineAdd(params) {
  return request(service.fconfigci.pipelineAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//升级流水线模板
export async function pipelineTemplateUpdate(params) {
  return request(service.fconfigci.pipelineTemplateUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//升级流水线
export async function pipelineUpdate(params) {
  return request(service.fconfigci.pipelineUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询流水线运行详情
export async function pipelineLogDetail(params) {
  return request(service.fconfigci.pipelineLogDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//存储持续集成草稿
export async function CIdraftSave(params) {
  return request(service.fconfigci.CIdraftSave, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//手动触发流水线
export async function triggerPipeline(params) {
  return request(service.fconfigci.triggerPipeline, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询镜像列表
export async function queryImageList(params) {
  return request(service.fconfigci.queryImageList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryLogDetailById(params) {
  return request(service.fconfigci.queryLogDetailById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//重跑pipeline
export async function retryPipeline(params) {
  return request(service.fconfigci.retryPipeline, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//根据流水线Id查分支列表
export async function queryBranchesByPipelineId(params) {
  return request(service.fconfigci.queryBranchesByPipelineId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询插件分类
export async function queryCategory(params) {
  return request(service.fconfigci.queryCategory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//查询插件详情
export async function queryPluginDetail(params) {
  return request(service.fconfigci.queryPluginDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//复制流水线
export async function copyPipeline(params) {
  return request(service.fconfigci.copyPipeline, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//重试job
export async function retryJob(params) {
  return request(service.fconfigci.retryJob, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

//流水线另存为模板
export async function saveAsPipelineTemplate(params) {
  return request(service.fconfigci.saveAsPipelineTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询markDown地址
export async function queryMarkDown(params) {
  return request(service.fconfigci.queryMarkDown, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//停止流水线执行
export async function stopPipeline(params) {
  return request(service.fconfigci.stopPipeline, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//停止单个job
export async function stopJob(params) {
  return request(service.fconfigci.stopJob, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 复制流水线模版
export async function copyPipelineTemplate(params) {
  return request(service.fconfigci.copyPipelineTemplate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryModelTemplateByContent(params) {
  return request(service.fconfigci.queryModelTemplateByContent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryTypeAndContent(params) {
  return request(service.fconfigci.queryTypeAndContent, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function getMinioInfo(params) {
  return request(service.fconfigci.getMinioInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function saveToMinio(params) {
  return request(service.fconfigci.saveToMinio, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function getAllJobs(params) {
  return request(service.fconfigci.getAllJobs, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export function getFullJobsByIds(params) {
  return request(service.fconfigci.getFullJobsByIds, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export function getAllRunnerCluster(params) {
  return request(service.fconfigci.getAllRunnerCluster, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function queryTriggerRules(params) {
  return request(service.fconfigci.queryTriggerRules, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export function updateTriggerRules(params) {
  return request(service.fconfigci.updateTriggerRules, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
