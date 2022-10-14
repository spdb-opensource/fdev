export default {
  ftask: {
    putJiraIssues: '/ftask/api/jira/putJiraIssues',
    updateJiraIssues: '/ftask/api/jira/updateJiraIssues',
    saveTaskAndJiraIssues: '/ftask/api/jira/saveTaskAndJiraIssues',
    queryTestTask: '/ftask/api/jira/queryTestTask',
    uploadFilesRid: '/ftask/api/task/upload/files/rid',
    nextStage: '/ftask/api/task/nextStage',
    addNoCodeRelator: '/ftask/api/task/add/noCode/relator',
    delNoCodeRelator: '/ftask/api/task/delete/noCode/relator',
    deleteFileRid: '/ftask/api/task/delete/file/rid',
    noCodeRelator: '/ftask/api/task/finish/noCode/relator',
    addNocodeTask: '/ftask/api/task/create/nocode/task',
    updateNocodeInformation: '/ftask/api/task/confirm/update/nocodeInfo', //确认修改
    queryMergeInfo: '/ftask/api/git/queryMergeInfo',
    queryDomains: '/fapp/api/serviceSystem/queryDomains',
    querySystem: '/fapp/api/serviceSystem/queryServiceSystem',
    taskAdd: '/ftask/api/task/add',
    taskDelete: '/ftask/api/task/delete',
    deleteTask: '/ftask/api/task/deleteTask',
    exportExcel: '/ftask/api/task/exportExcel',
    interactTest: '/ftask/api/task/interactTest',
    taskNewApp: '/ftask/api/task/newApp',
    newFeature: '/ftask/api/task/newFeature',
    putSitTest: '/ftask/api/task/putSitTest',
    putUatTest: '/ftask/api/task/putUatTest',
    taskQuery: '/ftask/api/task/query',
    queryBySubTask: '/ftask/api/task/queryBySubTask',
    queryByTerms: '/ftask/api/task/queryByTerms',
    queryByVague: '/ftask/api/task/queryByVague',
    queryDeleteTaskDetail: '/ftask/api/task/queryDeleteTaskDetail',
    queryEnvDetail: '/ftask/api/task/queryEnvDetail',
    queryMainTask: '/ftask/api/task/queryMainTask',
    queryTaskCByUnitNo: '/ftask/api/task/queryTaskCByUnitNo',
    queryTaskDetail: '/ftask/api/task/queryTaskDetail',
    queryTaskNum: '/ftask/api/task/queryTaskNum',
    queryTaskNumByApp: '/ftask/api/task/queryTaskNumByApp',
    queryTaskNumByGroup: '/ftask/api/task/queryTaskNumByGroup',
    queryTaskNumByGroupDate: '/ftask/api/task/queryTaskNumByGroupDate',
    queryTaskNumByMember: '/ftask/api/task/queryTaskNumByMember',
    queryTaskReview: '/ftask/api/task/queryTaskReview',
    queryTasksByIds: '/ftask/api/task/queryTasksByIds',
    queryUserTask: '/ftask/api/task/queryUserTask',
    taskUpdate: '/ftask/api/task/update',
    updateTaskReview: '/ftask/api/task/updateTaskReview',
    updateTaskStatus: '/ftask/api/task/updateTaskStatus',
    queryDocDetail: '/ftask/api/task/queryDocDetail',
    queryTestDetail: '/ftask/api/task/queryTestDetail',
    queryUATTestDetail: '/ftask/api/task/queryUATTestDetail',
    queryPostponeTask: '/ftask/api/task/queryPostponeTask', // 查询延期任务(新),
    queryFtaskMantis: '/tmantis/mantisFdev/queryFtaskMantisAll',
    queryJiraIssues: '/ftask/api/jira/queryJiraIssues',
    abandonTask: '/ftask/api/task/updateTaskToDiscardInner',
    queryTaskStatis: '/ftask/api/task/queryTaskNumByMemberRQR',
    queryGroupStatis: '/ftask/api/task/queryTaskNumByGroupinAll',
    queryNotinlineTasksByAppId: '/ftask/api/task/queryNotinlineTasksByAppId',
    deleteFile: '/ftask/api/task/review/deleteFiles',
    queryReviewRecord: '/ftask/api/task/review/fuzzyQueryReviewRecord',
    updateReviewRecord: '/ftask/api/task/review/updateTaskReviewStatus',
    queryReviewRecordStatus: '/ftask/api/task/review/queryTaskReview',
    createFirstReview: '/ftask/api/task/review/createFirstReview',
    queryTasksReviews: '/ftask/api/task/queryTaskReview',
    queryTaskNumByUserIdsDate: '/ftask/api/task/queryTaskNumByUserIdsDate',
    updateState: '/ftask/api/task/updateState',
    saveReviewRecord: '/ftask/api/task/review/saveReviewRecord',
    queryReviewRecordHistory: '/ftask/api/task/review/queryReviewRecordHistory',
    taskNameJudge: '/ftask/api/task/taskNameJudge',
    queryReviewBasicMsg: '/ftask/api/task/review/queryReviewBasicMsg',
    addReviewIdea: '/ftask/api/task/review/addReviewIdea',
    queryByTaskIdNode: '/frelease/api/task/queryByTaskIdNode',
    testReportCreate: '/ftask/api/task/doc/testReportCreate',
    queryTestMergeInfo: '/ftask/api/git/queryTestMergeInfo',
    createTestRunMerge: '/ftask/api/task/createTestRunMerge',
    queryCommitTips: '/ftask/api/git/queryCommitTips',
    getCodeQuality: '/ftask/api/sonarqube/getCodeQuality',
    getScanProcess: '/ftask/api/sonarqube/getScanProcess',
    iOSOrAndroidAppPackage: '/ftask/api/manage/generate/package',
    downloadSonarLog: '/ftask/api/sonarqube/downloadSonarLog',
    bafflePoint: '/ftask/api/manage/check/iam/properties',
    updateTaskDoc: '/ftask/api/task/doc/updateTaskDoc',
    deleteTaskDoc: '/ftask/api/task/doc/deleteTaskDoc',
    confirmBtn: '/ftask/api/task/update/confirmBtn',
    testKeyNote: '/ftask/api/task/update/testKeyNote',
    taskCardDisplay: '/ftask/api/task/taskCardDisplay',
    queryRqrDocInfo: '/ftask/api/task/queryRqrDocInfo',
    uploadDesignDoc: '/ftask/api/task/doc/uploadDesignDoc',
    queryTaskByDemandId: '/ftask/api/task/queryTaskByDemandId',
    queryReviewList: '/ftask/api/task/queryReviewList',
    queryGroupRqrmnt: '/ftask/api/task/queryGroupRqrmnt',
    addReview: '/ftask/api/task/review/addReview',
    queryJiraStoryByKey: '/ftask/api/jira/queryJiraStoryByKey',
    checkMountUnit: '/ftask/api/task/checkMountUnit'
  },
  fuser: {
    userQuery: '/fuser/api/user/query',
    groupQuery: '/fuser/api/group/query',
    queryUserCoreData: '/fuser/api/user/queryUserCoreData',
    queryGroup: '/fuser/api/group/queryGroup',
    roleQuery: '/fuser/api/role/query'
  },
  tmantis: {
    updateFdevMantis: '/tmantis/mantisFdev/updateFdevMantis',
    updateAssignUser: '/tmantis/mantisFdev/updateFdevMantis',
    queryFuserMantis: '/tmantis/mantisFdev/queryFuserMantisAll'
  },
  torder: {
    queryTaskSitMsg: '/torder/inform/queryTaskSitMsg'
  },
  fapp: {
    queryApps: '/fapp/api/app/queryApps',
    appQuery: '/fapp/api/app/query',
    queryPipelinesWithJobs: '/fapp/api/gitlabapi/queryPipelinesWithJobs'
  },
  fdemand: {
    queryAvailableIpmpUnit: '/fdemand/api/implementUnit/queryAvailableIpmpUnit',
    //根据fdev实施单元编号、需求id查实施单元信息及需求信息
    queryByFdevNoAndDemandId:
      '/fdemand/api/implementUnit/queryByFdevNoAndDemandId',
    queryPaginationByDemandId:
      '/fdemand/api/implementUnit/queryPaginationByDemandId' //查看需求下的研发单元
  },
  frelease: {
    queryDetailByTaskId: '/frelease/api/task/queryDetailByTaskId',
    queryReleaseNodes: '/frelease/api/releasenode/queryReleaseNodes',
    taskAdd: '/frelease/api/task/add',
    taskChangeNotise: '/frelease/api/rqrmnt/taskChangeNotise',
    addBatch: '/frelease/api/releasebatch/addBatch',
    queryBatchInfoByAppId: '/frelease/api/releasebatch/queryBatchInfoByAppId',
    updateTaskArchived: '/frelease/api/task/updateTaskArchived'
  },
  fdatabase: {
    upload: '/fdatabase/api/file/upload'
  },
  frqrmnt: {
    queryRedmineInfoByRedmineId: '/frqrmnt/api/implunits/queryUnitByRegexNum'
  },
  finterface: {
    isTaskManager: '/finterface/api/interface/isTaskManager',
    queryIsNoApplyInterface:
      '/finterface/api/interfaceApplication/queryIsNoApplyInterface'
  },
  fsonar: {
    scanningFeatureBranch: '/fsonar/api/sonar/scanningFeatureBranch'
  }
};
