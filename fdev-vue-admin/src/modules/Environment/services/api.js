export default {
  fenvconfig: {
    queryDeploymentDetails: '/fenvconfig/api/v2/blueking/listDeploymentDetail',
    proinfo: '/fenvconfig/api/v2/blueking/updateBluekingInfo',
    queryDeployments: '/fenvconfig/api/v2/blueking/getAllDeployments',
    saveSitConfigProperties:
      '/fenvconfig/api/v2/configfile/saveSitConfigProperties',
    getVerifyCode: '/fenvconfig/api/v2/verifycode/getVerifyCode',
    exportDependencySearchResult:
      '/fenvconfig/api/v2/configfile/exportDependencySearchResult',
    previewConfigFile: '/fenvconfig/api/v2/configfile/previewConfigFile',
    queryConfigDependency:
      '/fenvconfig/api/v2/configfile/queryConfigDependency',
    queryConfigTemplate: '/fenvconfig/api/v2/configfile/queryConfigTemplate',
    saveConfigTemplate: '/fenvconfig/api/v2/configfile/saveConfigTemplate',
    envAdd: '/fenvconfig/api/v2/env/add',
    envDelete: '/fenvconfig/api/v2/env/delete',
    envQuery: '/fenvconfig/api/v2/env/query',
    queryEnvByAppId: '/fenvconfig/api/v2/env/queryEnvByAppId',
    queryByLabels: '/fenvconfig/api/v2/env/queryByLabels',
    queryByLabelsFuzzy: '/fenvconfig/api/v2/env/queryByLabelsFuzzy',
    envUpdate: '/fenvconfig/api/v2/env/update',
    modelAdd: '/fenvconfig/api/v2/model/add',
    modelTempAdd: '/fenvconfig/api/v2/modelTemplate/add',
    modelDelete: '/fenvconfig/api/v2/model/delete',
    modelQuery: '/fenvconfig/api/v2/model/query',
    modelTempQuery: '/fenvconfig/api/v2/modelTemplate/query',
    pirvateModelQuery: '/fenvconfig/api/v2/model/queryExcludePirvateModel',
    modelQueryFuzz: '/fenvconfig/api/v2/model/queryFuzz',
    queryModelCategory: '/fenvconfig/api/v2/model/queryModelCategory',
    modelUpdate: '/fenvconfig/api/v2/model/update',
    outsideTemplateSave:
      '/fenvconfig/api/v2/outSideTemplate/outsideTemplateSave',
    outSideTemplateQuery: '/fenvconfig/api/v2/outSideTemplate/query',
    outSideTemplateUpdate: '/fenvconfig/api/v2/outSideTemplate/update',
    copyModelEnv: '/fenvconfig/api/v2/var/CopyModelEnv',
    updatyeCopyModelEnv: '/fenvconfig/api/v2/var/UpdatyeCopyModelEnv',
    varDelete: '/fenvconfig/api/v2/var/delete',
    varQuery: '/fenvconfig/api/v2/var/query',
    queryVarByEnvAndType: '/fenvconfig/api/v2/var/queryVarByEnvAndType',
    queryAutoEnv: '/fenvconfig/api/v2/env/queryByLabelsFuzzy',
    saveModelEnv: '/fenvconfig/api/v2/modelEnvUpdateApply/save',
    queryModelMessage: '/fenvconfig/api/v2/modelEnvUpdateApply/list',
    compare: '/fenvconfig/api/v2/modelEnvUpdateApply/compare',
    finish: '/fenvconfig/api/v2/modelEnvUpdateApply/finish',
    downloadAppInfo: '/fenvconfig/api/v2/modelEnvUpdateApply/downloadAppInfo',
    updateModelMessage: '/fenvconfig/api/v2/modelEnvUpdateApply/update',
    cancelModelMessage: '/fenvconfig/api/v2/modelEnvUpdateApply/cancel',
    saveDevConfigProperties:
      '/fenvconfig/api/v2/configfile/saveDevConfigProperties',
    queryModelSetList: '/fenvconfig/api/v2/modelSet/list',
    deleteModelSet: '/fenvconfig/api/v2/modelSet/delete',
    getType: '/fenvconfig/api/v2/modelSet/getTemplate',
    getModles: '/fenvconfig/api/v2/modelSet/getModles',
    saveModels: '/fenvconfig/api/v2/modelSet/save',
    updateModels: '/fenvconfig/api/v2/modelSet/update',
    queryAppProInfo: '/fenvconfig/api/v2/appProInfo/query',
    getJsonSchema: '/fenvconfig/api/v2/jsonSchema/getJsonSchema',
    bindAppInfo: '/fenvconfig/api/v2/appProInfo/bind',
    queryDeploy: '/fenvconfig/api/v2/appProInfo/queryDeploy',
    queryAllLabels: '/fenvconfig/api/v2/env/queryAllLabels',
    queryEnvKey: '/fenvconfig/api/v2/model/queryEnvKey',
    queryDeployDetail: '/fenvconfig/api/v2/appProInfo/queryBindMsgByApp',
    queryRealTimeBindMsg: '/fenvconfig/api/v2/appProInfo/queryRealTimeBindMsg',
    queryProEnvByAppId: '/fenvconfig/api/v2/appEnv/queryProEnvByAppId',
    pageQuery: '/fenvconfig/api/v2/var/pageQuery',
    queryModelEnvByValue: '/fenvconfig/api/v2/var/queryModelEnvByValue',
    checkConnectionDocker:
      '/fenvconfig/api/v2/modelEnvUpdateApply/checkConnectionDocker',
    queryPage: '/fenvconfig/api/v2/model/pageQuery',
    queryTempPage: '/fenvconfig/api/v2/modelTemplate/pageQuery',
    queryTemplateContainsModel:
      '/fenvconfig/api/v2/modelSet/queryTemplateContainsModel',
    getMappingHistoryList: '/fenvconfig/api/v2/history/getMappingHistoryList',
    getMappingHistoryDetail:
      '/fenvconfig/api/v2/history/getMappingHistoryDetail',
    configFilePreview: '/fenvconfig/api/v2/configfile/configFilePreview',
    queryEnvKeyList: '/fenvconfig/api/v2/model/queryNoCiEnvKeyList',
    queryConfigModel: '/fenvconfig/api/v2/model/configModel',
    batchPreviewConfigFile:
      '/fenvconfig/api/v2/configfile/batchPreviewConfigFile',
    queryModelEnvByModelNameEn:
      '/fenvconfig/api/v2/var/queryModelEnvByModelNameEn'
  },
  fuser: {
    currentUser: '/fuser/api/user/currentUser',
    userQuery: '/fuser/api/user/query'
  },
  fapp: {
    appQuery: '/fapp/api/app/query',
    queryApps: '/fapp/api/app/queryApps'
  }
};
