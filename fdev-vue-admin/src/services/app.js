import request from '@/utils/request';
import service from './serviceMap';

export async function getPipelineById(params) {
  return request(service.fapp.getPipelineById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function vipGetLog(params) {
  return request(service.fapp.vipGetLog, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function runPipeline(params) {
  return request(service.fapp.runPipeline, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function cancelVipDeploy(params) {
  return request(service.fapp.cancelVipDeploy, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryPipelinesList(params) {
  return request(service.fapp.queryPipelinesList, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryWithEnv(params) {
  return request(service.fapp.queryWithEnv, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryType(params) {
  return request(service.fapp.typeQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function addApp(params) {
  return request(service.fapp.appAdd, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 新增新应用
export async function addNewApp(params) {
  return request(service.fapp.appSave, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 持续集成模板表格
export async function queryAppContinuous(params) {
  return request(service.fapp.gitlabciQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询应用新增页gitlab组信息
export async function getGroupGit(params) {
  return request(service.fapp.gitlabapigetGroupGit, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 新增提交
export async function queryAppContinuousSave(params) {
  return request(service.fapp.gitlabciSave, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 编辑查询
export async function queryAppContinuousFind(params) {
  return request(service.fapp.gitlabciFindById, {
    method: 'POST',
    data: {
      id: params
    }
  });
}
// 编辑提交
export async function queryAppContinuousUpdate(params) {
  return request(service.fapp.gitlabciUpdate, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 应用模块--应用列表--详情修改
export async function appUpdate(params) {
  return request(service.fapp.appUpdate, {
    method: 'POST',
    data: {
      ...params,
      spdb_managers: params.spdb_managers.map(item => {
        return {
          user_name_cn: item.user_name_cn,
          user_name_en: item.user_name_en,
          id: item.id
        };
      }),
      dev_managers: params.dev_managers.map(item => {
        return {
          user_name_cn: item.user_name_cn,
          user_name_en: item.user_name_en,
          id: item.id
        };
      }),
      group: params.group.id,
      tasks_id: params.tasks_id === '' ? [] : params.tasks_id
    }
  });
}
// 应用模块--pipelines
export async function queryPipelines(params) {
  return request(service.fapp.queryPipelinesWithJobs, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 分页查询
export async function queryPipelinesWithJobsPage(params) {
  return request(service.fapp.queryPipelinesWithJobsPage, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 应用模块--pipelines-jobs
export async function queryTraces(params) {
  return request(service.fapp.queryTraces, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 应用模块--分页列表查询
export async function queryPagination(params) {
  return request(service.fapp.queryPagination, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 自动部署
export async function createPipelineSchedule(params) {
  return request(service.fapp.createPipelineSchedule, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//删除应用
export async function deleteApp(params) {
  return request(service.fapp.appDeleteById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

export async function queryAbandonDetail(params) {
  return request(service.fapp.findById, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询应用自动化测试信息
export async function queryAutoTest(params) {
  return request(service.fapp.queryAutoTest, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 更新应用自动化测试信息
export async function updateAutoTest(params) {
  return request(service.fapp.updateAutoTest, {
    method: 'POST',
    data: {
      ...params
    }
  });
}

// 查询对接自动测试平台的日志
export async function queryRunnerJobLog(params) {
  return request(service.fwebhook.queryRunnerJobLog, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 查询对接自动测试平台的日志详情
export async function queryRunnerJobLogDetail(params) {
  return request(service.fwebhook.queryRunnerJobLogDetail, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 对接自动测试平台 重试
export async function retryAutoTest(params) {
  return request(service.fwebhook.retryAutoTest, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 通过应用id查询该应用所有任务的缺陷集合
export async function queryMantis(params) {
  return request(service.fapp.queryMantisWithAppId, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryMyApps(params) {
  return request(service.fapp.myAppQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function queryMyUnits(params) {
  return request(service.fapp.myUnitQuery, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
// 通过id查询所有前端项目
export async function queryApps(params) {
  return request(service.fapp.queryApps, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//查询应用所属系统
export async function queryAppSystem(params) {
  return request(service.fapp.queryAppSystem, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//绑定应用所属系统
export async function bindSystem(params) {
  return request(service.fapp.bindSystem, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
//获取应用是否涉及内测
export async function getTestFlag(params) {
  return request(service.fapp.getTestFlag, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
