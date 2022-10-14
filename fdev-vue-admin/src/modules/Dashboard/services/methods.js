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

/*
 ********fgitwork*******
 */
export const getProjectUrl = commonRequest(services.fgitwork.getProjectUrl);

export const queryGitlabCommitDetail = commonRequest(
  services.fgitwork.queryGitlabCommitDetail
); //查询代码提交详情

export const queryGitlabCommitInfo = commonRequest(
  services.fgitwork.queryGitlabCommitInfo
);

export const getMergedInfo = commonRequest(services.fgitwork.getMergedInfo);

/*
 *******task*********
 */
export const queryGroupById = commonRequest(services.ftask.queryGroupById);

export const queryTasksByIds = commonRequest(services.ftask.queryTasksByIds); // 根据任务id集合查询任务列表

export async function exportExcel(params) {
  return request(services.ftask.exportExcel, {
    method: 'POST',
    responseType: 'arraybuffer',
    data: {
      ...params
    }
  });
} // 导出

export const queryTaskNum = commonRequest(services.ftask.queryTaskNum); // 任务 查询近6周应用数量

export const queryTaskNumByGroup = commonRequest(
  services.ftask.queryTaskNumByGroup
); // 根据小组查询各阶段任务

export const queryTaskNumByGroupDate = commonRequest(
  services.ftask.queryTaskNumByGroupDate
); // 时间段内任务数量查询

export const queryTaskNumByApp = commonRequest(
  services.ftask.queryTaskNumByApp
); // 根据应用查询各阶段任务

export const queryTaskNumByMember = commonRequest(
  services.ftask.queryTaskNumByMember
); // 查询人员关联的任务数量

export const queryTaskStatis = commonRequest(services.ftask.queryTaskStatis); // 查询人均阶段任务数(区分业务科技)

export const queryGroupStatis = commonRequest(services.ftask.queryGroupStatis); // 查询小组阶段任务数(区分业务科技)

export const queryTaskNumByUserIdsDate = commonRequest(
  services.ftask.queryTaskNumByUserIdsDate
);
export const queryGroupRqrmnt = commonRequest(services.ftask.queryGroupRqrmnt);

export const taskCardDisplay = commonRequest(services.ftask.taskCardDisplay);

export const queryReviewList = commonRequest(services.ftask.queryReviewList);

/*
 ******fapp********
 */
export const queryAppNum = commonRequest(services.fapp.queryAppNum); // 应用 查询近6周应用数量
/*
 ********feds*******
 */
export const queryIamsGroupChart = commonRequest(services.feds.groupStatistics); //小组维度

export const queryIamsUserChart = commonRequest(services.feds.userStatistics); //个人维度

/*
 ********fuser*******
 */
export const queryUserCoreData = commonRequest(
  services.fuser.queryUserCoreData
); // 根据任务id集合查询任务列表

export const queryTaskNumByGroupUser = commonRequest(
  services.fuser.queryTaskNumByGroup
);

export const queryUserStatis = commonRequest(services.fuser.queryUserStatis);

/*
 ********fdemand*******
 */
export const queryStatis = commonRequest(services.fdemand.queryStatis); // 需求维度统计

export const queryImpingDemandDashboard = commonRequest(
  services.fdemand.queryImpingDemandDashboard
);

export const queryEndDemandDashboard = commonRequest(
  services.fdemand.queryEndDemandDashboard
);

export const queryIntGroupId = commonRequest(services.fdemand.queryIntGroupId);

export const queryImplUnitStatis = commonRequest(
  services.fdemand.queryImplUnitStatis
); // 需求实施单元维度统计

export const queryReviewListDemand = commonRequest(
  services.fdemand.queryReviewList
);

/*
 ********torder*******
 */
export const queryResourceManagement = commonRequest(
  services.torder.queryResourceManagement
);
export const queryTaskSitMsg = commonRequest(services.torder.queryTaskSitMsg);

/*
 ********fcomponent*******
 */
export const queryNumByType = commonRequest(services.fcomponent.queryNumByType);

export const queryDataByType = commonRequest(
  services.fcomponent.queryDataByType
);

export const queryIssueData = commonRequest(services.fcomponent.queryIssueData);

export const queryQrmntsData = commonRequest(
  services.fcomponent.queryQrmntsData
);

export const queryIssueDelay = commonRequest(
  services.fcomponent.queryIssueDelay
);

export const queryallComName = commonRequest(
  services.fcomponent.queryallComName
);

/*
 ********sonar*******
 */
export const searchProject = commonRequest(services.fsonar.searchProject);
// 查询默认小组
export const queryDefaultGroupIds = commonRequest(
  services.freport.queryDefaultGroupIds
);
