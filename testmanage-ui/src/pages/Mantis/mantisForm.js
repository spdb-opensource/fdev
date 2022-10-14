import { queryMantisProjects } from '@/services/TestCaseExecute';
import { queryFdevGroup, queryGroup } from '@/services/userchart';
import {
  query,
  countIssue,
  queryIssueDetail,
  queryPlanIdByPlanlistTestcaseId,
  deleteMantis,
  update,
  deleteFile,
  addFile,
  exportMantisList,
  queryMantisInfo,
  queryApps,
  queryFdevTaskDetail
} from '@/services/mantis';
import { queryWorkOrderList } from '@/services/order';
import { queryAppByWorkNo } from '@/services/plan';
import { getUserListByRole } from '@/common/utlis';

export default {
  namespaced: true,

  state: {
    fdevTaskDetail: {},
    mantisPlanId: '',
    mantisList: [],
    issueDetailList: [],
    mantisTableDataList: {},
    totalNum: 0,
    workOrderList: [],
    mantisProjects: [],
    developerArr: [],
    reporterArr: [],
    fdevDevelopList: [],
    mantisInfo: {},
    appList: [],
    fdevGroup: [],
    fdevStatusGroup: [],
    userList: [],
    workNoAppList: []
  },
  actions: {
    async queryFdevTaskDetail({ commit }, payload) {
      let res = await queryFdevTaskDetail(payload);
      commit('saveQueryFdevTaskDetail', res);
    },
    async queryPlanIdByPlanlistTestcaseId({ commit }, payload) {
      let res = await queryPlanIdByPlanlistTestcaseId(payload);
      commit('saveMantisPlanId', res);
    },
    async deleteFile({ commit }, payload) {
      await deleteFile(payload);
    },
    async addFile({ commit }, payload) {
      await addFile(payload);
    },
    async exportMantisList({ commit }, payload) {
      let res = await exportMantisList(payload);
      commit('saveMantisList', res);
    },
    async queryIssueDetail({ commit }, payload) {
      let res = await queryIssueDetail(payload);
      commit('saveIssueDetailList', res);
    },
    async deleteMantis({ commit }, payload) {
      await deleteMantis(payload);
    },
    async update({ commit }, payload) {
      await update(payload);
    },
    async query({ commit }, payload) {
      let res = await query(payload);
      commit('saveMantisTableDataList', res);
    },
    async queryUser({ commit }, payload) {
      let res = await getUserListByRole();
      commit('saveUserList', res);
    },
    async queryAppByWorkNo({ commit }, payload) {
      let res = await queryAppByWorkNo(payload);
      commit('saveWorkNoAppList', res);
    },
    async countIssue({ commit }, payload) {
      let res = await countIssue(payload);
      commit('saveTotalNum', res);
    },
    async queryWorkOrderList({ commit }, payload) {
      let res = await queryWorkOrderList(payload);
      commit('saveWorkOrderList', res);
    },
    async queryMantisProjects({ commit }, payload) {
      let res = await queryMantisProjects(payload);
      commit('saveMantisProjects', res);
    },
    async queryDevelopList({ commit }, payload) {
      let res = await getUserListByRole(['开发人员']);
      commit('saveDeveloperArr', res);
    },
    async queryAllUserName({ commit }, payload) {
      let res = await getUserListByRole();
      commit('saveReporterArr', res);
    },
    async queryFdevUser({ commit }, payload) {
      let res = await getUserListByRole(['开发人员']);
      commit('saveFdevDevelopList', res);
    },
    async queryMantisInfo({ commit }, payload) {
      let res = await queryMantisInfo(payload);
      commit('saveMantisInfo', res);
    },
    async queryApps({ commit }, payload) {
      let res = await queryApps(payload);
      commit('saveAppList', res);
    },
    async queryFdevGroup({ commit }, payload) {
      let res = await queryFdevGroup(payload);
      commit('saveFdevGroup', res);
    },
    async queryGroup({ commit }, payload) {
      let res = await queryGroup(payload);
      commit('saveFdevStatusGroup', res);
    }
  },
  mutations: {
    saveQueryFdevTaskDetail(state, payload) {
      state.fdevTaskDetail = payload;
    },
    saveMantisPlanId(state, payload) {
      state.mantisPlanId = payload;
    },
    saveMantisList(state, payload) {
      state.mantisList = payload;
    },
    saveIssueDetailList(state, payload) {
      state.issueDetailList = payload;
    },
    saveMantisTableDataList(state, payload) {
      state.mantisTableDataList = payload;
    },
    saveTotalNum(state, payload) {
      state.totalNum = payload;
    },
    saveWorkOrderList(state, payload) {
      state.workOrderList = payload;
    },
    saveMantisProjects(state, payload) {
      state.mantisProjects = payload;
    },
    saveDeveloperArr(state, payload) {
      state.developerArr = payload;
    },
    saveReporterArr(state, payload) {
      state.reporterArr = payload;
    },
    saveFdevDevelopList(state, payload) {
      state.fdevDevelopList = payload;
    },
    saveMantisInfo(state, payload) {
      state.mantisInfo = payload;
    },
    saveAppList(state, payload) {
      state.appList = payload;
    },
    saveFdevGroup(state, payload) {
      state.fdevGroup = payload;
    },
    saveFdevStatusGroup(state, payload) {
      state.fdevStatusGroup = payload;
    },
    saveUserList(state, payload) {
      state.userList = payload;
    },
    saveWorkNoAppList(state, payload) {
      state.workNoAppList = payload;
    }
  }
};
