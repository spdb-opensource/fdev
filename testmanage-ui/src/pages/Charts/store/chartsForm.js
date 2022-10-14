import {
  getCompletedWorkNoData,
  exportExcelFinishedOrder,
  queryRollbackReport,
  exportRollbackReport,
  queryPersonReportImpl,
  exportExcelWeekReport,
  queryOrderInfoByUser,
  queryGroupInfo,
  countUserTestCaseByTime,
  countCaseTestByGroup,
  exportPersonalDimensionReport,
  queryAllReportImpl,
  testTeamBiweekly,
  listOfDevelopDefects,
  teamDefectClassify,
  defectClassify,
  exportAllExcelWeekReport,
  selWork,
  workLineData,
  workPieData,
  exportOrderDimension,
  exportExcelUser,
  exportGroupStatement,
  exportMantisStatement,
  getDefectList,
  queryDiscountChart,
  queryQualityReport,
  queryFdevGroup,
  qualityReportNewUnit
} from '@/services/userchart';
import { queryAllGroup } from '@/services/useradmin';
import { exportExcel, getUserListByRole } from '@/common/utlis';
export default {
  namespaced: true,

  state: {
    groupList: [],
    groupLists: [],
    fdevGroupList: [],
    workNoReport: [],
    export: null,
    revertTable: [],
    exportRollback: null,
    userList: [],
    reportData: [],
    exportExcelWeek: null,
    rowDatas: [],
    groupOrder: {},
    countUserTestCase: [],
    countUserTestCaseChart: [],
    exportPersonal: null,
    reportData: [],
    doubleWeekData: [],
    weekWinnerData: [],
    teamDefectChart: [],
    defectClassifyChart: [],
    exportAllExcelWeek: null,
    workChartTable: [],
    workLineDataChart: [],
    workPieDataChart: [],
    exportOrder: null,
    exportExcelList: null,
    groupData: {},
    defectList: [],
    disCountChartData: [],
    qualityReport: [],
    qualityReportDetail: []
  },

  getters: {},

  actions: {
    async exportExcelUser({ commit }, payload) {
      const response = await exportExcelUser(payload);
      commit('saveExportExcelUser', response);
    },
    async exportOrderDimension({ commit }, payload) {
      const response = await exportOrderDimension(payload);
      commit('saveExportOrderDimension', response);
    },
    async workPieData({ commit }, payload) {
      const response = await workPieData(payload);
      commit('saveWorkPieData', response);
    },
    async workLineData({ commit }, payload) {
      const response = await workLineData(payload);
      commit('saveWorkLineData', response);
    },
    async selWork({ commit }, payload) {
      const response = await selWork(payload);
      commit('saveSelWork', response);
    },
    async exportAllExcelWeekReport({ commit }, payload) {
      const response = await exportAllExcelWeekReport(payload);
      commit('saveExportAllExcelWeekReport', response);
    },
    async defectClassify({ commit }, payload) {
      const response = await defectClassify(payload);
      commit('saveDefectClassify', response);
    },
    async teamDefectClassify({ commit }, payload) {
      const response = await teamDefectClassify(payload);
      commit('saveTeamDefectClassify', response);
    },
    async listOfDevelopDefects({ commit }, payload) {
      const response = await listOfDevelopDefects(payload);
      commit('saveListOfDevelopDefects', response);
    },
    async testTeamBiweekly({ commit }, payload) {
      const response = await testTeamBiweekly(payload);
      commit('saveTestTeamBiweekly', response);
    },
    async queryAllReportImpl({ commit }, payload) {
      const response = await queryAllReportImpl(payload);
      commit('saveQueryAllReportImpl', response);
    },
    async exportPersonalDimensionReport({ commit }, payload) {
      const response = await exportPersonalDimensionReport(payload);
      commit('saveExportPersonalDimensionReport', response);
    },
    async countCaseTestByGroup({ commit }, payload) {
      const response = await countCaseTestByGroup(payload);
      commit('saveCountCaseTestByGroup', response);
    },
    async countUserTestCaseByTime({ commit }, payload) {
      const response = await countUserTestCaseByTime(payload);
      commit('saveCountUserTestCaseByTime', response);
    },
    async queryGroupInfo({ commit }, payload) {
      const response = await queryGroupInfo(payload);
      commit('saveQueryGroupInfo', response);
    },
    async queryOrderInfoByUser({ commit }, payload) {
      const response = await queryOrderInfoByUser(payload);
      commit('saveQueryOrderInfoByUser', response);
    },
    async queryGroupName({ commit }, payload) {
      const response = await queryAllGroup({ status: 1 });
      commit('saveQueryGroupName', response);
    },
    async getCompletedWorkNoData({ commit }, payload) {
      const response = await getCompletedWorkNoData(payload);
      commit('saveGetCompletedWorkNoData', response);
    },
    async exportExcelFinishedOrder({ commit }, payload) {
      const response = await exportExcelFinishedOrder(payload);
      commit('saveExportExcelFinishedOrder', response);
    },
    async queryRollbackReport({ commit }, payload) {
      const response = await queryRollbackReport(payload);
      commit('saveQueryRollbackReport', response);
    },
    async exportRollbackReport({ commit }, payload) {
      const response = await exportRollbackReport(payload);
      commit('saveExportRollbackReport', response);
    },
    async queryAllUserName({ commit }, payload) {
      const response = await getUserListByRole();
      commit('saveQueryAllUserName', response);
    },
    async queryPersonReportImpl({ commit }, payload) {
      const response = await queryPersonReportImpl(payload);
      commit('saveQueryPersonReportImpl', response);
    },
    async exportExcelWeekReport({ commit }, payload) {
      const response = await exportExcelWeekReport(payload);
      commit('saveExportExcelWeekReport', response);
    },
    async exportGroupStatement({ commit }, payload) {
      const response = await exportGroupStatement(payload);
      commit('saveGroupData', response);
    },
    async exportMantisStatement({ commit }, payload) {
      const response = await exportMantisStatement(payload);
      exportExcel(response);
    },
    async getDefectList({ commit }, payload) {
      const response = await getDefectList(payload);
      commit('saveDefectList', response);
    },
    async queryDiscountChart({ commit }, payload) {
      const response = await queryDiscountChart(payload);
      commit('saveDisCountChartData', response);
    },
    async queryQualityReport({ commit }, payload) {
      const response = await queryQualityReport(payload);
      commit('saveQualityReport', response);
    },
    async queryFdevGroup({ commit }, payload) {
      const response = await queryFdevGroup(payload);
      commit('saveFdevGroup', response);
    },
    async qualityReportNewUnit({ commit }, payload) {
      const response = await qualityReportNewUnit(payload);
      commit('saveQualityReportDetail', response);
    }
  },

  mutations: {
    saveFdevGroup(state, payload) {
      state.fdevGroupList = payload;
    },
    saveExportExcelUser(state, payload) {
      state.exportExcelList = payload;
    },
    saveExportOrderDimension(state, payload) {
      state.exportOrder = payload;
    },
    saveWorkPieData(state, payload) {
      state.workPieDataChart = payload;
    },
    saveWorkLineData(state, payload) {
      state.workLineDataChart = payload;
    },
    saveSelWork(state, payload) {
      state.workChartTable = payload;
    },
    saveExportAllExcelWeekReport(state, payload) {
      state.exportAllExcelWeek = payload;
    },
    saveDefectClassify(state, payload) {
      state.defectClassifyChart = payload;
    },
    saveTeamDefectClassify(state, payload) {
      state.teamDefectChart = payload;
    },
    saveListOfDevelopDefects(state, payload) {
      state.weekWinnerData = payload;
    },
    saveTestTeamBiweekly(state, payload) {
      state.doubleWeekData = payload;
    },
    saveQueryAllReportImpl(state, payload) {
      state.reportData = payload;
    },
    saveExportPersonalDimensionReport(state, payload) {
      state.exportPersonal = payload;
    },
    saveCountCaseTestByGroup(state, payload) {
      state.countUserTestCaseChart = payload;
    },
    saveCountUserTestCaseByTime(state, payload) {
      state.countUserTestCase = payload;
    },
    saveQueryGroupInfo(state, payload) {
      state.groupOrder = payload;
    },
    saveQueryOrderInfoByUser(state, payload) {
      state.rowDatas = payload;
    },
    saveQueryGroupName(state, payload) {
      state.groupList = payload;
    },
    saveGetCompletedWorkNoData(state, payload) {
      state.workNoReport = payload;
    },
    saveExportExcelFinishedOrder(state, payload) {
      state.export = payload;
    },
    saveQueryRollbackReport(state, payload) {
      state.revertTable = payload;
    },
    saveExportRollbackReport(state, payload) {
      state.exportRollback = payload;
    },
    saveQueryAllUserName(state, payload) {
      state.userList = payload;
    },
    saveQueryPersonReportImpl(state, payload) {
      state.reportData = payload;
    },
    saveExportExcelWeekReport(state, payload) {
      state.exportExcelWeek = payload;
    },
    saveGroupData(state, payload) {
      state.groupData = payload;
    },
    saveDefectList(state, payload) {
      state.defectList = payload;
    },
    saveDisCountChartData(state, payload) {
      state.disCountChartData = payload;
    },
    saveQualityReport(state, payload) {
      state.qualityReport = payload;
    },
    saveQualityReportDetail(state, payload) {
      state.qualityReportDetail = payload;
    }
  }
};
