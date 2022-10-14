export default {
  finterface: {
    getInterfaceDetailById: '/finterface/api/interface/getInterfaceDetailById',
    getInterfacesUrl: '/finterface/api/interface/getInterfacesUrl',
    isAppManager: '/finterface/api/interface/isAppManager',
    isTaskManager: '/finterface/api/interface/isTaskManager',
    queryInterfaceDetailById:
      '/finterface/api/interface/queryInterfaceDetailById',
    queryInterfaceList: '/finterface/api/interface/queryInterfaceList',
    queryInterfaceRelation: '/finterface/api/interface/queryInterfaceRelation',
    queryInterfaceVersions: '/finterface/api/interface/queryInterfaceVersions',
    queryInterfacesList: '/finterface/api/interface/queryInterfacesList',
    queryTransByVersion: '/finterface/api/interface/queryTransByVersion',
    queryTransDetailById: '/finterface/api/interface/queryTransDetailById',
    queryTransList: '/finterface/api/interface/queryTransList',
    scanInterface: '/finterface/api/interface/scanInterface',
    taskScanInterface: '/finterface/api/interface/taskScanInterface',
    updateTransTags: '/finterface/api/interface/updateTransTags',
    queryScanRecord: '/finterface/api/interface/queryScanRecord',
    queryTransRelation: '/finterface/api/interface/queryTransRelation',
    downloadRestRelationExcel:
      '/finterface/api/interface/downloadRestRelationExcel',
    updateParamDescription: '/finterface/api/interface/updateParamDescription',
    modifyParamDescription: '/finterface/api/interface/modifyParamDescription',
    queryApplicationList:
      '/finterface/api/interfaceApplication/queryApplicationList',
    updateApplicationStatus:
      '/finterface/api/interfaceApplication/updateApplicationStatus',
    interfaceCallRequest:
      '/finterface/api/interfaceApplication/interfaceCallRequest',
    isManagers: '/finterface/api/interfaceApplication/isManagers',
    queryIsNoApplyInterface:
      '/finterface/api/interfaceApplication/queryIsNoApplyInterface',
    queryInterfaceStatistics: '/finterface/api/interface/interfaceStatistics',
    queryRoutes: '/finterface/api/interface/queryRoutes',
    queryRoutesRelation: '/finterface/api/interface/queryRoutesRelation',
    queryRoutesDetail: '/finterface/api/interface/queryRoutesDetail',
    getServiceChainInfo: '/finterface/api/interface/getServiceChainInfo',
    queryAppJsonList: '/finterface/api/interface/queryAppJsonList',
    queryTotalJsonList: '/finterface/api/interface/queryTotalJsonList',
    queryTotalJsonHistory: '/finterface/api/interface/queryTotalJsonHistory',
    queryRoutesDetailVer: '/finterface/api/interface/queryRoutesDetailVer',
    queryYapiList: '/finterface/api/interface/yapiProjectList',
    importYapiProject: '/finterface/api/interface/importYapiProject',
    convertJsonSchema: '/finterface/api/interface/convertJsonSchema',
    queryYapiDetail: '/finterface/api/interface/yapiInterfaceList',
    deleteYapiProject: '/finterface/api/interface/deleteYapiProject',
    deleteYapiInterface: '/finterface/api/interface/deleteYapiInterface',
    importYapiInterface: '/finterface/api/interface/importYapiInterface'
  },
  fapp: {
    getProjectBranchList: '/fapp/api/gitlabapi/getProjectBranchList',
    queryApps: '/fapp/api/app/queryApps',
    typeQuery: '/fapp/api/type/query',
    appQuery: '/fapp/api/app/query'
  },
  fuser: {
    groupQuery: '/fuser/api/group/query'
  }
};
