import request from '@/utils/request.js';
import api from './api';

export const commonRequest = url => async (params = {}) => {
  const response = await request(url, {
    method: 'POST',
    data: {
      ...params
    }
  });

  return response;
};
//查询用户自定义看板信息
export const queryUserConfig = commonRequest(api.freport.queryUserConfig);

//新增用户自定义看板信息
export const addUserConfig = commonRequest(api.freport.addUserConfig);
/*************研发协作类 **********/
// 查询需求吞吐量
export const queryDemandThroughputTrend = commonRequest(
  api.freport.queryDemandThroughputTrend
);

//新需求数
export const queryDemandNewTrend = commonRequest(
  api.freport.queryDemandNewTrend
);
/*************投产管理类 **********/
//投产数量变化趋势
export const queryPublishCountTrend = commonRequest(
  api.freport.queryPublishCountTrend
);
/*************质量管理类 **********/
//生产问题数
export const queryProIssueTrend = commonRequest(api.freport.queryProIssueTrend);
/*************排行榜类 **********/
// 代码提交排行
export const queryUserCommit = commonRequest(api.freport.queryUserCommit);
// bug前十应用、漏洞前十应用、异味前十应用、重复率前十应用
export const querySonarProjectRank = commonRequest(
  api.freport.querySonarProjectRank
);

//牵头需求吞吐量
export const queryDemandThroughputStatistics = commonRequest(
  api.freport.queryDemandThroughputStatistics
);
// 任务吞吐量
export const queryTaskThroughputStatistics = commonRequest(
  api.freport.queryTaskThroughputStatistics
);
// 导出任务详情
export async function exportTaskThroughputDetail(params) {
  return request(api.freport.exportTaskThroughputDetail, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
// 项目组规模
export const queryUserStatis = commonRequest(api.fuser.queryUserStatis);
// 项目组资源闲置情况(新)
export const queryPersonFreeStatistics = commonRequest(
  api.freport.queryPersonFreeStatistics
);
// 基础架构类型总数
export const queryNumByType = commonRequest(api.fcomponent.queryNumByType);
//基础架构近期变化趋势
export const queryDataByType = commonRequest(api.fcomponent.queryDataByType);
// 基础架构发布版本数量
export const queryIssueData = commonRequest(api.fcomponent.queryIssueData);
// 根据应用查询各阶段任务
export const queryTaskNumByApp = commonRequest(api.ftask.queryTaskNumByApp);
// 根据任务id集合查询任务列表
export const queryTaskSimpleByIds = commonRequest(
  api.ftask.queryTaskSimpleByIds
);
//获取应用列表
export const queryForSelect = commonRequest(api.fapp.queryForSelect);
//任务推进情况
export const queryTaskPhaseChangeStatistics = commonRequest(
  api.freport.queryTaskPhaseChangeStatistics
);
//各应用的任务推进情况
export const queryAppTaskPhaseStatistics = commonRequest(
  api.freport.queryAppTaskPhaseStatistics
);
// 各小组不同阶段任务数量
export const queryTaskPhaseStatistics = commonRequest(
  api.freport.queryTaskPhaseStatistics
);
// 当前小组应用数量
export const queryAppNewTrend = commonRequest(api.freport.queryAppNewTrend);
// 当前小组任务数量
export const queryTaskTrend = commonRequest(api.freport.queryTaskTrend);
// 导出
export async function exportExcel(params) {
  return request(api.ftask.exportExcel, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
// 查询默认组
export const queryDefaultGroupIds = commonRequest(
  api.freport.queryDefaultGroupIds
);
//投产需求数
export const queryDemandProTrend = commonRequest(
  api.freport.queryDemandProTrend
);
// 查询全部用户
export const queryUserCoreData = commonRequest(api.fuser.queryUserCoreData);
// 根据用户id和日期查询用户各阶段任务数量
export const queryTaskNumByUserIdsDate = commonRequest(
  api.ftask.queryTaskNumByUserIdsDate
);
// 需求统计
export const queryDemandStatistics = commonRequest(
  api.freport.queryDemandStatistics
);
// 查询mergerequest列表
export const getMergedInfo = commonRequest(api.fgitwork.getMergedInfo);
/**新 */
//代码统计列表
export const queryCommitStatistics = commonRequest(
  api.freport.queryCommitStatistics
);
// 代码统计详情
export const queryCommitByUser = commonRequest(api.freport.queryCommitByUser);
// 提交具体差异文件
export const queryCommitDiff = commonRequest(api.fgitwork.queryCommitDiff);
// 项目组规模统计
export const queryPersonStatistics = commonRequest(
  api.freport.queryPersonStatistics
);
// merge request导出
export async function exportMergedInfo(params) {
  return request(api.fgitwork.exportMergedInfo, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}

// 导出统计报表
export async function exportCommitStatistics(params) {
  return request(api.freport.exportCommitStatistics, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
}
// 需求统计（各组对应阶段实施需求数量需求报表统计）
export const queryGroupRqrmnt = commonRequest(api.ftask.queryGroupRqrmnt);
