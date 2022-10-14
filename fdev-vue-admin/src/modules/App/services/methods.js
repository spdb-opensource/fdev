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

// 查询应用信息
export const query = commonRequest(services.fapp.appQuery);

// 录入已有应用
export const addApp = commonRequest(services.fapp.appAdd);

// 录入新应用
export const addNewApp = commonRequest(services.fapp.appSave);

//更新应用
export async function appUpdate(params) {
  return request(services.fapp.appUpdate, {
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
export const queryPipelines = commonRequest(
  services.fapp.queryPipelinesWithJobs
);

export const queryType = commonRequest(services.fapp.typeQuery);

// 通过id查询所有前端项目
export const queryApps = commonRequest(services.fapp.queryApps);

// 查询持续集成模板
export const queryAppContinuous = commonRequest(services.fapp.gitlabciQuery);

// 更新持续集成模块
export const queryAppContinuousUpdate = commonRequest(
  services.fapp.gitlabciUpdate
);

// 保存新增持续集成模块
export const queryAppContinuousSave = commonRequest(services.fapp.gitlabciSave);

// 通过ID查询持续集成模块
export const queryAppContinuousFind = commonRequest(
  services.fapp.gitlabciFindById
);

// 查询job日志
export const queryTraces = commonRequest(services.fapp.queryTraces);

// 自动部署
export const createPipelineSchedule = commonRequest(
  services.fapp.createPipelineSchedule
);

// 删除应用
export const deleteApp = commonRequest(services.fapp.appDeleteById);

export const queryAbandonDetail = commonRequest(services.fapp.findById);

// 查询应用自动化测试信息
export const queryAutoTest = commonRequest(services.fapp.queryAutoTest);

// 更新应用自动化测试信息
export const updateAutoTest = commonRequest(services.fapp.updateAutoTest);

// 查询对接自动测试平台的日志
export const queryRunnerJobLog = commonRequest(
  services.fwebhook.queryRunnerJobLog
);

// 查询对接自动测试平台的日志详情
export const queryRunnerJobLogDetail = commonRequest(
  services.fwebhook.queryRunnerJobLogDetail
);

// 对接自动测试平台 重试
export const retryAutoTest = commonRequest(services.fwebhook.retryAutoTest);

// 通过应用id查询该应用所有任务的缺陷集合
export const queryMantis = commonRequest(services.fapp.queryMantisWithAppId);

export const queryMyApps = commonRequest(services.fapp.myAppQuery);

// 应用模块--分页列表查询
export const queryPagination = commonRequest(services.fapp.queryPagination);

// 查询涉及环境部署的应用
export const queryWithEnv = commonRequest(services.fapp.queryWithEnv);

//vip部署列表
export const queryPipelinesList = commonRequest(
  services.fapp.queryPipelinesList
);

//取消部署
export const cancelVipDeploy = commonRequest(services.fapp.cancelVipDeploy);

//部署
export const runPipeline = commonRequest(services.fapp.runPipeline);

//vip部署日志
export const vipGetLog = commonRequest(services.fapp.vipGetLog);

export const getPipelineById = commonRequest(services.fapp.getPipelineById);

//查询应用所属系统
export const queryAppSystem = commonRequest(services.fapp.queryAppSystem);

//绑定应用所属系统
export const bindSystem = commonRequest(services.fapp.bindSystem);

// 查询应用新增页gitlab组信息
export const getGroupGit = commonRequest(services.fapp.gitlabapigetGroupGit);

//获取应用是否涉及内测
export const getTestFlag = commonRequest(services.fapp.getTestFlag);

// sonar-扫描时间、扫描版本
export const projectAnalyses = commonRequest(services.fsonar.projectAnalyses);

export const getProjectInfos = commonRequest(services.fsonar.getProjectInfos);

export const getAnalysesHistory = commonRequest(
  services.fsonar.getAnalysesHistory
);

export const componentTree = commonRequest(services.fsonar.componentTree);

export const scanningFeatureBranch = commonRequest(
  services.fsonar.scanningFeatureBranch
);

export const featureProjectAnalyses = commonRequest(
  services.fsonar.featureProjectAnalyses
);

export const getProjectFeatureInfo = commonRequest(
  services.fsonar.getProjectFeatureInfo
);

export const featureComponentTree = commonRequest(
  services.fsonar.featureComponentTree
);

export const queryAppTagPiplines = commonRequest(
  services.frelease.queryAppTagPiplines
);

export const queryTestRunPiplines = commonRequest(
  services.frelease.queryTestRunPiplines
);

// 查询gitlabID
export const queryProject = commonRequest(services.fapp.queryProject);

// 查询group是否存在
export const getGroup = commonRequest(services.fapp.getGroup);

// 根据应用id查询所有分支
export const queryAllBranch = commonRequest(services.fapp.queryAllBranch);

// 自定义部署
export const createPipeline = commonRequest(services.fapp.createPipeline);

// 轮询应用新增
export const saveByAsync = commonRequest(services.fapp.saveByAsync);

// 通过环境实体id查询环境信息
export const queryApplicationName = commonRequest(
  services.fapp.queryAppNameEnByGitlabProjectId
);

// 查询个人组
export const queryGroup = commonRequest(services.fuser.groupQuery);

export const queryCITemplate = commonRequest(services.fapp.gitlabciQuery);

export const queryDomain = commonRequest(services.ftask.queryDomains);

export const querySystem = commonRequest(services.ftask.querySystem);

export const queryCurrent = commonRequest(services.fuser.currentUser);

// 查询我的流水线模板列表
export const queryMinePipelineTemplateList = commonRequest(
  services.fcipipeline.queryMinePipelineTemplateList
);

export const queryEnvList = commonRequest(services.fenvconfig.envQuery);

// 根据环境和实体类型查询实体与环境映射信息
export const queryVarByEnvAndType = commonRequest(
  services.fenvconfig.queryVarByEnvAndType
);

export const queryEnv = commonRequest(services.fenvconfig.queryByLabels);

export const queryDeploy = commonRequest(services.fenvconfig.queryDeploy);

export const queryByLabelsFuzzy = commonRequest(
  services.fenvconfig.queryByLabelsFuzzy
);

export const queryConfigTemplate = commonRequest(
  services.fenvconfig.queryConfigTemplate
);

// 获取环境列表
export const getEnvList = commonRequest(services.fenvconfig.envQuery);

// 根据应用获取环境列表
export const getEnvListByAppId = commonRequest(
  services.fenvconfig.queryEnvByAppId
);

export const previewConfigFile = commonRequest(
  services.fenvconfig.previewConfigFile
);

//查询外部配置参数（优先生效）
export const queryExtraConfigParam = commonRequest(
  services.fenvconfig.outSideTemplateQuery
);

// 任务查询，query=》queryTask
export const queryTask = commonRequest(services.ftask.taskQuery);

export const queryExcludePirvateModelList = commonRequest(
  services.fenvconfig.pirvateModelQuery
);

// 生成接口对外链接
export const getInterfacesUrl = commonRequest(
  services.finterface.getInterfacesUrl
);

// 接口列表
export const queryInterfaceList = commonRequest(
  services.finterface.queryInterfaceList
);

// 扫描分支
export const getProjectBranchList = commonRequest(
  services.fapp.getProjectBranchList
);

// 查询tags权限
export const queryTagsRole = commonRequest(services.finterface.isAppManager);

// 查询应用所属流水线列表
export const queryAppPipelineList = commonRequest(
  services.fcipipeline.queryAppPipelineList
);

export const queryAutoEnv = commonRequest(services.fenvconfig.queryAutoEnv);

//新增外部配置参数（优先生效）
export const addExtraConfigParam = commonRequest(
  services.fenvconfig.outsideTemplateSave
);
