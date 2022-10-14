import {
  downLoadDemandReviewList,
  downLoadReviewList,
  queryGroupById,
  queryTaskNum,
  queryAppNum,
  queryTasksByIds,
  exportExcel,
  queryUserCoreData,
  queryTaskNumByApp,
  queryTaskNumByMember,
  queryTaskNumByGroup,
  queryTaskNumByGroupDate,
  queryStatis,
  queryImpingDemandDashboard,
  queryEndDemandDashboard,
  queryIntGroupId,
  queryImplUnitStatis,
  queryTaskStatis,
  queryGroupStatis,
  queryTaskNumByUserIdsDate,
  queryResourceManagement,
  queryTaskNumByGroupUser,
  queryGroupRqrmnt,
  queryTaskSitMsg,
  getMergedInfo,
  queryQrmntsData,
  queryIssueDelay,
  queryallComName,
  taskCardDisplay,
  queryGitlabCommitInfo,
  queryGitlabCommitDetail,
  getProjectUrl,
  queryReviewList,
  queryReviewListDemand
} from '@/services/dashboard';
import { searchProject } from '@/services/sonar';
import { queryIamsGroupChart, queryIamsUserChart } from '@/services/imas';
import { queryUserStatis } from '@/services/user';
import {
  resolveResponseError
  // exportExcel as exportTable
} from '@/utils/utils';
import { nameGroup, nameKey } from '@/modules/Dashboard/utils/constants';
import {
  queryUserProIssues,
  queryTaskProIssues,
  queryProIssues,
  countProIssues,
  exportProIssues,
  queryIssueDetail,
  queryIssueDetailById,
  queryProByTeam,
  queryProIssueType,
  queryProIssueRate
} from '@/services/mantis';

export default {
  namespaced: true,

  state: {
    childGroupList: [],
    downloadTable: null,
    downloadDemandTable: null,
    projectUrl: '',
    isMini: false,
    taskList: [],
    taskNum: {}, // 任务
    appNum: {}, // 应用
    response: null,
    userList: [],
    appChartData: {},
    userChartData: {},
    taskNumByGroup: {},
    taskNumByGroupDate: {},
    iamsGroupForm: {},
    iamsGroupChart: {},
    issueRateChart: {},
    iamsUserThisMonthChart: [],
    groupTooltip: '',
    groupName: '',
    groupPercentTooltip: '',
    groupPercentName: '',
    iamsUserLastMonthChart: [],
    ranking: [],
    userProIssues: [],
    taskProIssues: [],
    export: null,
    issueDetail: {},
    statis: [],
    ImpingDemands: {},
    EndDemands: {},
    GroupPartsIds: {},
    implUnitStatis: [],
    taskStatis: [],
    groupStatis: [],
    taskNumByUserIdsDate: null,
    resourceManagement: [],
    taskSitMsg: [],
    userOptimizeList: [],
    rqrDelayList: [],
    userStatis: {},
    taskNumByGroupUser: {
      companies: [],
      groups: []
    },
    issueDetailById: {},
    productionChart: {
      xAxis: []
    },
    proIssueTypeChart: [],
    proIssueRateChart: [],
    productionTable: {
      total: 0,
      list: []
    },
    groupRqrmnt: {
      total: 0,
      list: []
    },
    mergeInfo: {},
    allComNameList: [],
    realTimeTask: [],
    gitlabCommitInfo: [],
    gitlabCommitDetail: [],
    reviewList: []
  },

  getters: {
    appChart: state => {
      const { appChartData } = state;

      return initBarChartData(appChartData, 'bar', 'stack');
    },
    iamsGroupChartAll: state => {
      let { iamsGroupChart } = state;
      const {
        xAxis,
        dangbanData,
        totalUserSumList,
        userSumList
      } = iamsGroupChart;
      const series = [
        {
          name: '占比',
          type: 'bar',
          data: dangbanData,
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
          const totalUserSum = totalUserSumList[index];
          const userSum = userSumList[index];
          return `${
            params[0].axisValue
          }<br/>总人数：${totalUserSum}<br/>使用人数：${userSum}<br/>占比：${
            params[0].data
          }%`;
        }
      };
      return { legend, xAxis, series, tooltip };
    },

    iamsGroupChartAverage: state => {
      let { iamsGroupChart } = state;
      const {
        xAxis,
        averageData,
        totalUserSumList,
        totalTransCountList
      } = iamsGroupChart;
      const series = [
        {
          name: '人均交易额',
          type: 'bar',
          data: averageData,
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
          const totalUserSum = totalUserSumList[index];
          const totalTransCount = totalTransCountList[index];

          return `${
            params[0].axisValue
          }<br/>总人数：${totalUserSum}<br/>总交易量：${totalTransCount}<br/>人均交易量：${
            params[0].data
          }`;
        }
      };
      return { legend, xAxis, series, tooltip };
    },
    iamsUserChartFormatter: state => (chartData, email) => {
      const xAxis = [];
      const data = [];
      chartData.forEach(item => {
        xAxis.push(item.name);
        data.push(item.sum);
      });
      const legend = xAxis;
      const series = [
        {
          name: '交易量',
          stack: null,
          type: 'bar',
          data: data,
          label: {
            normal: {
              show: true
            }
          }
        }
      ];
      return { xAxis, legend, series };
    },
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
  },

  actions: {
    //设计稿下载
    async downLoadDemandReviewList({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        downLoadDemandReviewList(data)
      );
      commit('saveDownloadDemandTable', response);
    },
    //设计还原稿下载
    async downLoadReviewList({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        downLoadReviewList(data)
      );
      commit('saveDownloadTable', response);
    },
    async queryGroupById({ commit, state }, data) {
      const response = await resolveResponseError(() => queryGroupById(data));
      commit('saveGroupById', response);
    },
    async getProjectUrl({ commit, state }, data) {
      const response = await resolveResponseError(() => getProjectUrl(data));
      commit('saveGetProjectUrl', response);
    },
    // 任务 查询近6周应用数量
    async queryTaskNum({ commit, state }, data) {
      const response = await resolveResponseError(() => queryTaskNum(data));
      commit('saveTaskNum', response);
    },
    // 应用 查询近6周应用数量
    async queryAppNum({ commit, state }, data) {
      const response = await resolveResponseError(() => queryAppNum(data));
      commit('saveAppNum', response);
    },
    async exportExcel({ commit, state }, data) {
      const response = await resolveResponseError(() => exportExcel(data));
      commit('saveResponse', response);
    },
    async queryTasksByIds({ commit, state }, data) {
      try {
        const response = await resolveResponseError(() =>
          queryTasksByIds(data)
        );
        commit('saveTaskList', response);
      } catch (err) {
        commit('saveTaskList', []);
      }
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
    async queryTaskNumByApp({ commit, state }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryTaskNumByApp(data)
        );
        commit('saveAppChartData', respone);
      } catch (err) {
        commit('saveAppChartData', []);
      }
    },
    async queryTaskNumByMember({ commit, state }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryTaskNumByMember(data)
        );
        commit('saveUserChartData', respone);
      } catch (err) {
        commit('saveUserChartData', []);
      }
    },
    async queryTaskNumByGroup({ commit, state }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryTaskNumByGroup(data)
        );
        commit('saveTaskNumByGroup', respone);
      } catch (err) {
        commit('saveTaskNumByGroup', []);
      }
    },
    async queryTaskNumByGroupDate({ commit, state }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryTaskNumByGroupDate(data)
        );
        commit('saveTaskNumByGroupDate', respone);
      } catch (err) {
        commit('saveTaskNumByGroupDate', []);
      }
    },
    async queryIamsGroupChart({ commit, state }, data) {
      const respone = await resolveResponseError(() =>
        queryIamsGroupChart(data)
      );
      commit('saveIamsGroupChart', respone);
    },
    async queryIamsUserChart({ commit, state }, data) {
      const respone = await resolveResponseError(() =>
        queryIamsUserChart(data)
      );
      commit('saveIamsUserThisMonthChart', respone);
    },
    async queryIamsUserLastMonthChart({ commit, state }, data) {
      const respone = await resolveResponseError(() =>
        queryIamsUserChart(data)
      );
      commit('saveIamsUserLastMonthChart', respone);
    },
    async searchProject({ commit, state }, data) {
      const response = await resolveResponseError(() => searchProject(data));
      commit('saveRaking', response);
    },
    async queryUserProIssues({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryUserProIssues(data)
      );
      commit('saveUserProIssues', response);
    },
    async queryTaskProIssues({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryTaskProIssues(data)
      );
      commit('saveTaskProIssues', response);
    },
    async queryProductionTable({ commit, state }, data) {
      const response = await Promise.all([
        queryProIssues(data),
        countProIssues(data)
      ]);
      commit('saveProductionTable', response);
    },
    async exportProIssues({ commit, state }, data) {
      const response = await resolveResponseError(() => exportProIssues(data));
      commit('saveExport', response);
    },
    async queryIssueDetail({ commit, state }, data) {
      const response = await resolveResponseError(() => queryIssueDetail(data));
      commit('saveIssueDetail', response);
    },
    async queryStatis({ commit, state }, data) {
      const response = await resolveResponseError(() => queryStatis(data));
      commit('saveStatis', response);
    },
    async queryImpingDemandDashboard({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryImpingDemandDashboard(data)
      );
      commit('saveImpingDemand', response);
    },
    async queryEndDemandDashboard({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryEndDemandDashboard(data)
      );
      commit('saveEndDemand', response);
    },
    async queryIntGroupId({ commit, state }, data) {
      const response = await resolveResponseError(() => queryIntGroupId(data));
      commit('saveGroupId', response);
    },
    async queryImplUnitStatis({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryImplUnitStatis(data)
      );
      commit('saveImplUnitStatis', response);
    },
    async queryTaskStatis({ commit, state }, data) {
      const response = await resolveResponseError(() => queryTaskStatis(data));
      commit('saveTaskStatis', response);
    },
    async queryGroupStatis({ commit, state }, data) {
      const response = await resolveResponseError(() => queryGroupStatis(data));
      commit('saveGroupStatis', response);
    },
    async queryTaskNumByUserIdsDate({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryTaskNumByUserIdsDate(data)
      );
      commit('saveTaskNumByUserIdsDate', response);
    },
    async queryResourceManagement({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryResourceManagement(data)
      );
      commit('saveResourceManagement', response);
    },
    async queryTaskSitMsg({ commit, state }, data) {
      const response = await resolveResponseError(() => queryTaskSitMsg(data));
      commit('saveTaskSitMsg', response);
    },
    async queryQrmntsData({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryQrmntsData(payload)
      );
      commit('saveUserOptimizeList', response);
    },
    async queryIssueDelay({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryIssueDelay(payload)
      );
      commit('saveIssueDelayList', response);
    },
    async queryUserStatis({ commit, state }, data) {
      const response = await resolveResponseError(() => queryUserStatis(data));
      commit('saveUserStatis', response);
    },
    async queryTaskNumByGroupUser({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryTaskNumByGroupUser(data)
      );
      commit('saveTaskNumByGroupUser', response);
    },
    async queryIssueDetailById({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryIssueDetailById(data)
      );
      commit('saveIssueDetailById', response);
    },
    async queryProductionChart({ commit, state }, data) {
      const response = await resolveResponseError(() => queryProByTeam(data));
      commit('saveProductionChart', response);
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
    async queryGroupRqrmnt({ commit, state }, data) {
      const response = await resolveResponseError(() => queryGroupRqrmnt(data));
      commit('saveGroupRqrmnt', response);
    },
    async queryMergeInfo({ commit, state }, data) {
      const response = await resolveResponseError(() => getMergedInfo(data));
      commit('saveMergedInfo', response);
    },
    async queryallComName({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryallComName(payload)
      );
      commit('saveAllComNameList', response);
    },
    async taskCardDisplay({ commit }, payload) {
      const response = await resolveResponseError(() =>
        taskCardDisplay(payload)
      );
      commit('saveTaskCardDisplay', response);
    },
    async queryGitlabCommitInfo({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryGitlabCommitInfo(data)
      );
      commit('saveGitlabCommitInfo', response);
    },
    async queryGitlabCommitDetail({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryGitlabCommitDetail(data)
      );
      commit('saveGitlabCommitDetail', response);
    },
    async queryReviewList({ commit, state }, data) {
      const response = await resolveResponseError(() => queryReviewList(data));
      commit('saveReviewList', response);
    },
    async queryReviewListDemand({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryReviewListDemand(data)
      );
      commit('saveReviewList', response);
    }
  },

  mutations: {
    saveDownloadDemandTable(state, data) {
      state.downloadDemandTable = data;
    },
    saveDownloadTable(state, data) {
      state.downloadTable = data;
    },
    saveGroupById(state, data) {
      state.childGroupList = data;
    },
    saveGetProjectUrl(state, data) {
      state.projectUrl = data;
    },
    saveTaskNum(state, data) {
      state.taskNum = data;
    },
    saveAppNum(state, data) {
      state.appNum = data;
    },
    resetChart(state, data) {
      state.isMini = data;
    },
    saveTaskList(state, data) {
      state.taskList = data;
    },
    saveResponse(state, data) {
      state.response = data;
    },
    saveUserList(state, data) {
      state.userList = data;
    },
    saveAppChartData(state, data) {
      state.appChartData = data;
    },
    saveUserChartData(state, data) {
      state.userChartData = data;
    },
    saveTaskNumByGroup(state, data) {
      state.taskNumByGroup = data;
    },
    saveTaskNumByGroupDate(state, data) {
      state.taskNumByGroupDate = data;
    },
    saveAllComNameList(state, payload) {
      state.allComNameList = payload;
    },
    saveIamsGroupChart(state, data) {
      state.iamsGroupForm = data;
      if (!data) data = [];
      const config = {
        xAxis: [],
        dangbanData: [],
        averageData: [],
        totalUserSumList: [],
        totalTransCountList: [],
        userSumList: []
      };
      data.forEach(item => {
        const {
          groupName,
          percent,
          avgTransCount,
          totalUserSum,
          totalTransCount,
          userSum
        } = item;
        config.xAxis.push(groupName);
        config.dangbanData.push(percent);
        config.averageData.push(avgTransCount);
        config.totalUserSumList.push(totalUserSum);
        config.totalTransCountList.push(totalTransCount);
        config.userSumList.push(userSum);
      });
      state.iamsGroupChart = config;
    },
    saveIamsUserThisMonthChart(state, data) {
      state.iamsUserThisMonthChart = data;
    },
    saveIamsUserLastMonthChart(state, data) {
      state.iamsUserLastMonthChart = data;
    },
    saveRaking(state, data) {
      state.ranking = data;
    },
    saveUserProIssues(state, data) {
      state.userProIssues = data;
    },
    saveTaskProIssues(state, data) {
      state.taskProIssues = data;
    },
    saveExport(state, data) {
      state.export = data;
    },
    saveIssueDetail(state, data) {
      state.issueDetail = data;
    },
    saveStatis(state, data) {
      state.statis = data;
    },
    saveImpingDemand(state, data) {
      state.ImpingDemands = data;
    },
    saveEndDemand(state, data) {
      state.EndDemands = data;
    },
    saveGroupId(state, data) {
      state.GroupPartsIds = data;
    },
    saveImplUnitStatis(state, data) {
      state.implUnitStatis = data;
    },
    saveTaskStatis(state, data) {
      state.taskStatis = data;
    },
    saveGroupStatis(state, data) {
      state.groupStatis = data;
    },
    saveTaskNumByUserIdsDate(state, data) {
      state.taskNumByUserIdsDate = data;
    },
    saveResourceManagement(state, data) {
      state.resourceManagement = data;
    },
    saveTaskSitMsg(state, data) {
      state.taskSitMsg = data;
    },
    saveUserOptimizeList(state, payload) {
      state.userOptimizeList = payload;
    },
    saveIssueDelayList(state, payload) {
      state.rqrDelayList = payload;
    },
    saveUserStatis(state, data) {
      state.userStatis = data;
    },
    saveTaskNumByGroupUser(state, data) {
      state.taskNumByGroupUser = data;
    },
    saveIssueDetailById(state, data) {
      state.issueDetailById = data;
    },
    saveGroupRqrmnt(state, data) {
      state.groupRqrmnt = data;
    },
    saveTaskCardDisplay(state, data) {
      state.realTimeTask = data;
    },
    saveProductionTable(state, data) {
      state.productionTable = {
        list: data[0],
        total: data[1]
      };
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
    saveProductionChart(state, data) {
      const { xAxis, xyData, yAxis } = data;

      const series = xyData.map((item, index) => {
        return {
          data: item,
          type: 'bar',
          name: yAxis[index],
          stack: '总量',
          label: {
            normal: {
              show: true
            }
          }
        };
      });

      state.productionChart = {
        xAxis,
        series
      };
    },
    saveProIssueRateChart(state, data) {
      state.proIssueRateChart = data;
    },
    saveProIssueTypeChart(state, data) {
      state.proIssueTypeChart = data;
    },
    saveReviewList(state, data) {
      if (!data || JSON.stringify(data) === '{}') {
        state.reviewList = [];
      } else {
        state.reviewList = data;
      }
    }
  }
};

function initBarChartData(data, type, stack) {
  const xAxisId = Object.keys(data);

  const seriesNames = ['todo', 'develop', 'sit', 'uat', 'rel', 'production'];

  const legend = seriesNames.map(item => {
    return nameGroup[item];
  });

  const series = seriesNames.map((key, index) => {
    return {
      name: nameGroup[key],
      id: nameKey[key],
      type: type,
      stack: stack ? '总量' : null,
      data: xAxisId.map(id => {
        return data[id][key];
      }),
      label: {
        normal: {
          show: true
        }
      }
    };
  });
  return { series, xAxisId, legend };
}
