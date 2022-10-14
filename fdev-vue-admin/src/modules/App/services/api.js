export default {
  fapp: {
    getPipelineById: '/fapp/api/vip/getPipelineById',
    vipGetLog: '/fapp/api/vip/getLog',
    runPipeline: '/fapp/api/vip/runPipeline',
    cancelVipDeploy: '/fapp/api/vip/cancel',
    queryPipelinesList: '/fapp/api/vip/pipelines',
    queryWithEnv: '/fapp/api/app/queryWithEnv',
    appAdd: '/fapp/api/app/add',
    appDeleteById: '/fapp/api/app/deleteAppById',
    createPipelineSchedule: '/fapp/api/app/createPipelineSchedule',
    appQuery: '/fapp/api/app/query',
    queryAppNum: '/fapp/api/app/queryAppNum',
    appSave: '/fapp/api/app/save',
    saveByAsync: '/fapp/api/app/saveByAsync',
    appUpdate: '/fapp/api/app/update',
    createPipeline: '/fapp/api/gitlabapi/createPipeline',
    getGroup: '/fapp/api/gitlabapi/getGroup',
    getProjectBranchList: '/fapp/api/gitlabapi/getProjectBranchList',
    queryAllBranch: '/fapp/api/gitlabapi/queryAllBranch',
    queryAppNameEnByGitlabProjectId:
      '/fapp/api/gitlabapi/queryAppNameEnByGitlabProjectId',
    queryPipelinesWithJobs: '/fapp/api/gitlabapi/queryPipelinesWithJobs',
    queryProject: '/fapp/api/gitlabapi/queryProject',
    queryTraces: '/fapp/api/gitlabapi/queryTraces',
    gitlabciFindById: '/fapp/api/gitlabci/findbyid',
    gitlabciQuery: '/fapp/api/gitlabci/query',
    gitlabciSave: '/fapp/api/gitlabci/save',
    gitlabciUpdate: '/fapp/api/gitlabci/update',
    gitlabapigetGroupGit: '/fapp/api/gitlabapi/getGroupGit',
    typeQuery: '/fapp/api/type/query',
    findById: '/fapp/api/app/findbyid',
    queryAutoTest: '/fapp/api/autotest/query',
    updateAutoTest: '/fapp/api/autotest/update',
    queryMantisWithAppId: '/fapp/api/app/queryAppMantis',
    myAppQuery: '/fapp/api/app/queryMyApps',
    queryPagination: '/fapp/api/app/queryPagination',
    queryApps: '/fapp/api/app/queryApps',
    queryAppSystem: '/fapp/api/serviceSystem/querySystem',
    bindSystem: '/fapp/api/app/bindSystem',
    getTestFlag: '/fapp/api/app/getTestFlag' //获取应用是否涉及内测
  },
  fwebhook: {
    queryRunnerJobLog: '/fwebhook/api/webHook/getFdevRunnerJobLog',
    queryRunnerJobLogDetail: '/fwebhook/api/webHook/getFdevRunnerJobLogDetail',
    retryAutoTest: '/fwebhook/api/webHook/retryAutoTest'
  },
  fsonar: {
    projectAnalyses: '/fsonar/api/sonar/projectAnalyses',
    getProjectInfos: '/fsonar/api/sonar/getProjectInfo',
    getAnalysesHistory: '/fsonar/api/sonar/getAnalysesHistory',
    componentTree: '/fsonar/api/sonar/componentTree',
    scanningFeatureBranch: '/fsonar/api/sonar/scanningFeatureBranch',
    featureProjectAnalyses: '/fsonar/api/sonar/featureProjectAnalyses',
    getProjectFeatureInfo: '/fsonar/api/sonar/getProjectFeatureInfo',
    featureComponentTree: '/fsonar/api/sonar/featureComponentTree'
  },
  frelease: {
    queryAppTagPiplines: '/frelease/api/application/queryAppTagPiplines',
    queryTestRunPiplines: '/frelease/api/application/queryTestRunPiplines'
  },
  fuser: {
    groupQuery: '/fuser/api/group/query',
    currentUser: '/fuser/api/user/currentUser'
  },
  ftask: {
    queryDomains: '/fapp/api/serviceSystem/queryDomains',
    querySystem: '/fapp/api/serviceSystem/queryServiceSystem',
    taskQuery: '/ftask/api/task/query'
  },
  fconfigci: {
    queryMinePipelineTemplateList:
      '/fconfigci/api/pipelineTemplate/queryMinePipelineTemplateList', //查询我的流水线模板列表
    queryAppPipelineList: '/fconfigci/api/pipeline/queryAppPipelineList' //查询应用所属流水线列表
  },
  fenvconfig: {
    envQuery: '/fenvconfig/api/v2/env/query',
    queryVarByEnvAndType: '/fenvconfig/api/v2/var/queryVarByEnvAndType',
    queryByLabels: '/fenvconfig/api/v2/env/queryByLabels',
    queryDeploy: '/fenvconfig/api/v2/appProInfo/queryDeploy',
    queryByLabelsFuzzy: '/fenvconfig/api/v2/env/queryByLabelsFuzzy',
    queryEnvByAppId: '/fenvconfig/api/v2/env/queryEnvByAppId',
    previewConfigFile: '/fenvconfig/api/v2/configfile/previewConfigFile',
    outSideTemplateQuery: '/fenvconfig/api/v2/outSideTemplate/query',
    pirvateModelQuery: '/fenvconfig/api/v2/model/queryExcludePirvateModel',
    queryAutoEnv: '/fenvconfig/api/v2/env/queryByLabelsFuzzy',
    outsideTemplateSave:
      '/fenvconfig/api/v2/outSideTemplate/outsideTemplateSave'
  },
  fcomponent: {
    queryConfigTemplate: '/fcomponent/api/config/queryConfigTemplate'
  },
  finterface: {
    getInterfacesUrl: '/finterface/api/interface/getInterfacesUrl',
    queryInterfaceList: '/finterface/api/interface/queryInterfaceList',
    isAppManager: '/finterface/api/interface/isAppManager'
  }
};
