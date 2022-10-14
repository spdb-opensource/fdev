//模块内部接口调用定义
// import request from '@/utils/request.js';
export default {
  freport: {
    // 1：仪表盘
    //查询用配置信息
    queryUserConfig: '/freport/api/dashboard/queryUserConfig',
    //保存用户配置信息
    addUserConfig: '/freport/api/dashboard/addUserConfig',
    /*************研发协作类 **********/
    //新增需求数
    queryDemandNewTrend: '/freport/api/development/queryDemandNewTrend',
    //新需求吞吐量
    queryDemandThroughputTrend:
      'freport/api/development/queryDemandThroughputTrend',
    // 当前小组应用数量
    queryAppNewTrend: 'freport/api/development/queryAppNewTrend',
    // 当前小组任务吞吐量
    queryTaskTrend: 'freport/api/development/queryTaskTrend',
    /*************投产管理类 **********/
    //投产数量变化趋势
    queryPublishCountTrend: 'freport/api/publishManage/queryPublishCountTrend',
    /*************质量管理类 **********/
    //生产问题数
    queryProIssueTrend: 'freport/api/qualityManage/queryProIssueTrend',
    /*************排行榜类 **********/
    // 代码提交排行
    queryUserCommit: '/freport/api/ranking/queryUserCommit',
    // bug前十应用、漏洞前十应用、异味前十应用、重复率前十应用
    querySonarProjectRank: '/freport/api/ranking/querySonarProjectRank',
    //2：统计报表
    // 牵头投产需求
    queryDemandThroughputStatistics:
      '/freport/api/development/queryDemandThroughputStatistics',
    // 任务吞吐量
    queryTaskThroughputStatistics:
      '/freport/api/development/queryTaskThroughputStatistics',
    //导出任务详情
    exportTaskThroughputDetail:
      '/freport/api/development/exportTaskThroughputDetail',
    queryPersonFreeStatistics:
      '/freport/api/resourceManage/queryPersonFreeStatistics', // 项目组资源闲置情况(新)
    queryTaskPhaseChangeStatistics:
      'freport/api/development/queryTaskPhaseChangeStatistics', //任务推进情况
    queryAppTaskPhaseStatistics:
      '/freport/api/development/queryAppTaskPhaseStatistics', //各应用的任务推进情况
    queryTaskPhaseStatistics:
      'freport/api/development/queryTaskPhaseStatistics', // 各小组不同阶段任务数量
    queryDefaultGroupIds: '/freport/api/dashboard/queryDefaultGroupIds', //查询默认组
    queryDemandProTrend: '/freport/api/development/queryDemandProTrend', //投产需求数
    queryDemandStatistics: 'freport/api/development/queryDemandStatistics', //需求统计
    queryCommitStatistics: 'freport/api/development/queryCommitStatistics', //代码统计列表
    exportCommitStatistics: 'freport/api/development/exportCommitStatistics', // 导出统计表格
    queryCommitByUser: 'freport/api/development/queryCommitByUser', //代码统计详情
    queryPersonStatistics: '/freport/api/resourceManage/queryPersonStatistics' //项目组规模统计
  },
  fuser: {
    queryUserStatis: '/fuser/api/user/queryUserStatis', //项目组规模
    queryUserCoreData: '/fuser/api/user/queryUserCoreData' //查询全部用户
  },
  fcomponent: {
    queryNumByType: '/fcomponent/api/dashboard/queryNumByType', //基础架构类型总数
    queryDataByType: '/fcomponent/api/dashboard/queryDataByType', //基础架构近期数量变化趋势
    queryIssueData: '/fcomponent/api/dashboard/queryIssueData' //基础架构发布版本数量
  },
  ftask: {
    queryTaskNumByApp: '/ftask/api/task/queryTaskNumByApp', // 根据应用查询各阶段任务
    queryTaskSimpleByIds: '/ftask/api/task/queryTaskSimpleByIds', // 根据任务id集合查询任务列表
    exportExcel: '/ftask/api/task/exportExcel', //导出
    queryTaskNumByUserIdsDate: '/ftask/api/task/queryTaskNumByUserIdsDate',
    queryGroupRqrmnt: '/ftask/api/task/queryGroupRqrmnt' //各组对应阶段实施需求数量需求报表统计
  },
  fapp: {
    queryForSelect: '/fapp/api/app/queryForSelect' //获取应用列表
  },
  fgitwork: {
    getMergedInfo: '/fgitwork/api/getMergedInfo', //查询mergeRequest列表
    queryCommitDiff: 'fgitwork/commitInfo/queryCommitDiff', //具体差异文件
    exportMergedInfo: 'fgitwork/api/exportMergedInfo' //mergerequest导出功能
  }
};
