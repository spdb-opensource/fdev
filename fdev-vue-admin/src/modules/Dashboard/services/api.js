export default {
  fgitwork: {
    getProjectUrl: '/fgitwork/api/work/getProjectUrl',
    queryGitlabCommitDetail: '/fgitwork/api/work/queryDetailInfo',
    queryGitlabCommitInfo:
      '/fgitwork/api/work/queryGitlabCommitInfoByGroupUser',
    getMergedInfo: '/fgitwork/api/getMergedInfo'
  },
  ftask: {
    queryGroupById: '/ftask/api/task/queryGroupById',
    queryTasksByIds: '/ftask/api/task/queryTasksByIds',
    exportExcel: '/ftask/api/task/exportExcel',
    queryTaskNum: '/ftask/api/task/queryTaskNum',
    queryTaskNumByGroup: '/ftask/api/task/queryTaskNumByGroup',
    queryTaskNumByGroupDate: '/ftask/api/task/queryTaskNumByGroupDate',
    queryTaskNumByApp: '/ftask/api/task/queryTaskNumByApp',
    queryTaskNumByMember: '/ftask/api/task/queryTaskNumByMember',
    queryTaskStatis: '/ftask/api/task/queryTaskNumByMemberRQR',
    queryGroupStatis: '/ftask/api/task/queryTaskNumByGroupinAll',
    queryTaskNumByUserIdsDate: '/ftask/api/task/queryTaskNumByUserIdsDate',
    queryGroupRqrmnt: '/ftask/api/task/queryGroupRqrmnt',
    taskCardDisplay: '/ftask/api/task/taskCardDisplay',
    queryReviewList: '/ftask/api/task/queryReviewList'
  },
  fapp: {
    queryAppNum: '/fapp/api/app/queryAppNum'
  },
  feds: {
    groupStatistics: '/feds/api/iams/groupStatistics',
    userStatistics: '/feds/api/iams/userStatistics'
  },
  fuser: {
    queryUserCoreData: '/fuser/api/user/queryUserCoreData',
    queryTaskNumByGroup: '/fuser/api/user/queryTaskNumByGroup',
    queryUserStatis: '/fuser/api/user/queryUserStatis'
  },
  fdemand: {
    queryStatis: '/fdemand/api/dashboard/queryDemandStatis', //需求报表统计
    queryImpingDemandDashboard:
      '/fdemand/api/dashboard/queryImpingDemandDashboard',
    queryEndDemandDashboard: '/fdemand/api/dashboard/queryEndDemandDashboard',
    queryIntGroupId: '/fdemand/api/dashboard/queryIntGroupId',
    queryImplUnitStatis: '/fdemand/api/dashboard/queryImplUnit', //实施单元报表统计
    queryReviewList: '/fdemand/api/design/queryReviewList'
  },
  torder: {
    queryResourceManagement: '/torder/order/queryResourceManagement',
    queryTaskSitMsg: '/torder/inform/queryTaskSitMsg'
  },
  fcomponent: {
    queryNumByType: '/fcomponent/api/dashboard/queryNumByType',
    queryDataByType: '/fcomponent/api/dashboard/queryDataByType',
    queryIssueData: '/fcomponent/api/dashboard/queryIssueData',
    queryQrmntsData: '/fcomponent/api/dashboard/queryQrmntsData',
    queryIssueDelay: '/fcomponent/api/dashboard/queryIssueDelay',
    queryallComName: '/fcomponent/api/dashboard/allIsuue'
  },
  fsonar: {
    searchProject: '/fsonar/api/sonar/searchProject'
  },
  freport: {
    queryDefaultGroupIds: '/freport/api/dashboard/queryDefaultGroupIds'
  }
};
