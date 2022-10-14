import {
  queryPersonFreeStatistics,
  queryNumByType,
  queryDataByType,
  queryIssueData,
  queryTaskSimpleByIds,
  queryForSelect,
  queryTaskPhaseChangeStatistics,
  queryAppTaskPhaseStatistics,
  queryTaskPhaseStatistics,
  exportExcel,
  queryDefaultGroupIds,
  queryUserCoreData,
  queryTaskNumByUserIdsDate,
  queryDemandStatistics,
  getMergedInfo,
  queryCommitStatistics,
  queryCommitByUser,
  queryCommitDiff,
  queryPersonStatistics,
  queryGroupRqrmnt //需求统计(各组对应阶段实施需求数量需求报表统计)
} from '@/modules/Measure/services/methods.js';
import { queryProIssueType, queryProIssueRate } from '@/services/mantis';
import { resolveResponseError } from '@/utils/utils';
export default {
  namespaced: true,
  state: {
    personFreeStatistics: [], //项目组资源闲置情况
    frameNumData: [], //基础架构总数
    frameReleaseData: [], //基础架构变化趋势
    frameIssueList: [], //基础架构发布版本
    taskList: [], //任务列表
    appChartData: {},
    applicationList: [], //应用列表
    stageTaskNum: {},
    taskNumByGroup: {},
    response: null,
    defaultGroup: [], //默认展示的组
    userList: [], //用户列表
    taskNumByUserIdsDate: null, //根据用户id集合和时间范围查询各阶段
    proIssueTypeChart: [], //各板块易出现问题
    proIssueRateChart: [], //投产窗口质量
    rqrStatistic: [], //需求统计
    mergeInfo: {},
    gitlabCommitInfo: [], //代码统计列表
    gitlabCommitDetail: [],
    commitDiff: [], //变更文件列表
    teamSize: {}, //项目组规模
    groupRqrmnt: {
      total: 0,
      list: []
    }
  },
  actions: {
    async queryPersonFreeStatistics({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryPersonFreeStatistics(data)
      );
      commit('savequeryPersonFreeStatistics', response);
    },
    async queryNumByType({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryNumByType(payload)
      );
      commit('saveFrameNumData', response);
    },
    async queryDataByType({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryDataByType(payload)
      );
      commit('saveFrameReleaseData', response);
    },
    async queryIssueData({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryIssueData(payload)
      );
      commit('saveFrameIssueData', response);
    },
    async queryTaskSimpleByIds({ commit, state }, data) {
      try {
        const response = await resolveResponseError(() =>
          queryTaskSimpleByIds(data)
        );
        commit('saveTaskList', response);
      } catch (err) {
        commit('saveTaskList', []);
      }
    },
    async queryAppTaskPhaseStatistics({ commit, state }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryAppTaskPhaseStatistics(data)
        );
        commit('saveAppChartData', respone);
      } catch (err) {
        commit('saveAppChartData', []);
      }
    },
    async queryForSelect({ commit, state }, data) {
      const response = await resolveResponseError(() => queryForSelect(data));
      commit('saveAppList', response);
    },
    async queryTaskPhaseChangeStatistics({ commit, state }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryTaskPhaseChangeStatistics(data)
        );
        commit('saveStageTaskNum', respone);
      } catch (err) {
        commit('saveStageTaskNum', []);
      }
    },
    async queryTaskPhaseStatistics({ commit, state }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryTaskPhaseStatistics(data)
        );
        commit('saveTaskNumByGroup', respone);
      } catch (err) {
        commit('saveTaskNumByGroup', []);
      }
    },
    async exportExcel({ commit, state }, data) {
      const response = await resolveResponseError(() => exportExcel(data));
      commit('saveResponse', response);
    },
    async queryDefaultGroupIds({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryDefaultGroupIds(data)
      );
      commit('saveDefaultGroup', response);
    },
    async queryUserCoreData({ commit, state }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryUserCoreData(data)
        );
        commit('saveUserList', respone);
      } catch (err) {
        commit('saveUserList', []);
      }
    },
    async queryTaskNumByUserIdsDate({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryTaskNumByUserIdsDate(data)
      );
      commit('saveTaskNumByUserIdsDate', response);
    },
    async queryProIssueType({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryProIssueType(data)
      );
      commit('saveProIssueTypeChart', response);
    },
    async queryProIssueRate({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryProIssueRate(data)
      );
      commit('saveProIssueRateChart', response);
    },
    async queryDemandStatistics({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryDemandStatistics(data)
      );
      commit('saveRqrStatistic', response);
    },
    async queryMergeInfo({ commit, state }, data) {
      const response = await resolveResponseError(() => getMergedInfo(data));
      commit('saveMergedInfo', response);
    },
    async queryCommitStatistics({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryCommitStatistics(data)
      );
      commit('saveGitlabCommitInfo', response);
    },
    async queryCommitByUser({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryCommitByUser(data)
      );
      commit('saveGitlabCommitDetail', response);
    },
    async queryCommitDiff({ commit, state }, data) {
      const response = await resolveResponseError(() => queryCommitDiff(data));
      commit('saveCommitDiff', response);
    },
    async queryPersonStatistics({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryPersonStatistics(data)
      );
      commit('saveTeamSize', response);
    },
    async queryGroupRqrmnt({ commit, state }, data) {
      const response = await resolveResponseError(() => queryGroupRqrmnt(data));
      commit('saveGroupRqrmnt', response);
    }
  },
  mutations: {
    savequeryPersonFreeStatistics(state, data) {
      state.personFreeStatistics = data;
    },
    saveFrameNumData(state, payload) {
      state.frameNumData = payload;
    },
    saveFrameReleaseData(state, payload) {
      state.frameReleaseData = payload;
    },
    saveFrameIssueData(state, payload) {
      state.frameIssueList = payload;
    },
    saveTaskList(state, data) {
      state.taskList = data;
    },
    saveAppChartData(state, data) {
      state.appChartData = data;
    },
    saveAppList(state, data) {
      state.applicationList = data;
    },
    saveStageTaskNum(state, data) {
      state.stageTaskNum = data;
    },
    saveTaskNumByGroup(state, data) {
      state.taskNumByGroup = data;
    },
    saveResponse(state, data) {
      state.response = data;
    },
    saveDefaultGroup(state, data) {
      state.defaultGroup = data;
    },
    saveUserList(state, data) {
      state.userList = data;
    },
    saveTaskNumByUserIdsDate(state, data) {
      state.taskNumByUserIdsDate = data;
    },
    saveProIssueRateChart(state, data) {
      state.proIssueRateChart = data;
    },
    saveProIssueTypeChart(state, data) {
      state.proIssueTypeChart = data;
    },
    saveRqrStatistic(state, data) {
      state.rqrStatistic = data;
    },
    saveMergedInfo(state, data) {
      state.mergeInfo = data;
    },
    saveGitlabCommitInfo(state, data) {
      state.gitlabCommitInfo = data;
    },
    saveGitlabCommitDetail(state, data) {
      state.gitlabCommitDetail = data;
    },
    saveCommitDiff(state, data) {
      state.commitDiff = data;
    },
    saveTeamSize(state, data) {
      state.teamSize = data;
    },
    saveGroupRqrmnt(state, data) {
      state.groupRqrmnt = data;
    }
  },
  getters: {
    proIssueRateChartAll: state => {
      let { issueRateChart } = state;
      const {
        xAxis,
        issueRateData,
        proIssueList,
        rqrIssueList
      } = issueRateChart;
      const series = [
        {
          name: '生产问题数量',
          type: 'bar',
          data: proIssueList,
          stack: null,
          label: {
            normal: {
              show: true
            }
          }
        }
      ];
      const legend = xAxis;

      const tooltip = {
        formatter: params => {
          const index = xAxis.indexOf(params[0].axisValue);
          const proIssue = proIssueList[index];
          const rqrIssue = rqrIssueList[index];
          const issueRate = issueRateData[index];
          return `${
            params[0].axisValue
          }<br/>生产问题数量：${proIssue}<br/>需求数量：${rqrIssue}<br/>占比：${issueRate}`;
        }
      };
      return { legend, xAxis, series, tooltip };
    }
  }
};
