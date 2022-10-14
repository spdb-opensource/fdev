import {
  queryApprovalList,
  updateApprovalStatus,
  openKF,
  closeKF,
  queryLogs,
  queryMeetings,
  deleteMeetingById,
  addMeeting,
  updateMeeting,
  queryOrderById,
  queryFiles,
  downloadDoc,
  uploadDoc,
  deleteDoc,
  queryProblemItem,
  addProblem,
  updateProblem,
  deleteProblemById
} from '../services/methods.js';
import { resolveResponseError, exportExcel } from '@/utils/utils';

export default {
  namespaced: true,

  state: {
    codeIssue: {},
    orderList: [],
    approvalList: [],
    logList: [],
    meetingList: [],
    orderInfoDetail: {},
    fileList: [],
    problemItem: []
  },
  getters: {},
  actions: {
    //网络审核查询
    async queryApprovalList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryApprovalList(payload)
      );
      commit('saveApprovalList', response);
    },
    //网络审核状态更新
    async updateApprovalStatus({ commit }, payload) {
      await resolveResponseError(() => updateApprovalStatus(payload));
    },

    //KF网络开通申请
    async openKF({ commit }, payload) {
      await resolveResponseError(() => openKF(payload));
    },

    //KF网络关闭申请
    async closeKF({ commit }, payload) {
      await resolveResponseError(() => closeKF(payload));
    },
    //查询日志
    async queryLogs({ commit }, payload) {
      const response = await resolveResponseError(() => queryLogs(payload));
      commit('saveLogList', response);
    },
    //查询会议
    async queryMeetings({ commit }, payload) {
      const response = await resolveResponseError(() => queryMeetings(payload));
      commit('saveMeetingList', response);
    },
    //删除会议记录
    async deleteMeetingById({ commit }, payload) {
      await resolveResponseError(() => deleteMeetingById(payload));
    },
    //添加会议记录
    async addMeeting({ commit }, payload) {
      await resolveResponseError(() => addMeeting(payload));
    },
    //更新会议记录
    async updateMeeting({ commit }, payload) {
      await resolveResponseError(() => updateMeeting(payload));
    },
    //查询工单详情
    async queryOrderById({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryOrderById(payload)
      );
      commit('saveOrderInfoDetail', response);
    },
    //查询文件列表
    async queryFiles({ commit }, payload) {
      const response = await resolveResponseError(() => queryFiles(payload));
      commit('saveFileList', response);
    },
    //下载文件
    async downloadDoc({ commit }, payload) {
      const response = await resolveResponseError(() => downloadDoc(payload));
      exportExcel(response);
    },
    //上传文件
    async uploadDoc({ commit }, payload) {
      await resolveResponseError(() => uploadDoc(payload));
    },
    //删除文件
    async deleteDoc({ commit }, payload) {
      await resolveResponseError(() => deleteDoc(payload));
    },
    //查询字典（新增问题问题项下拉框选项）
    async queryProblemItem({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryProblemItem(payload)
      );
      commit('saveProblemItem', response);
    },
    //新增问题项
    async addProblem({ commit }, payload) {
      await resolveResponseError(() => addProblem(payload));
    },
    //修改问题项
    async updateProblem({ commit }, payload) {
      await resolveResponseError(() => updateProblem(payload));
    },
    //删除问题项
    async deleteProblemById({ commit }, payload) {
      await resolveResponseError(() => deleteProblemById(payload));
    }
  },
  mutations: {
    saveApprovalList(state, payload) {
      state.approvalList = payload;
    },
    saveLogList(state, payload) {
      state.logList = payload;
    },
    saveMeetingList(state, payload) {
      state.meetingList = payload;
    },
    saveOrderInfoDetail(state, payload) {
      state.orderInfoDetail = payload;
    },
    saveFileList(state, payload) {
      state.fileList = payload;
    },
    saveProblemItem(state, payload) {
      state.problemItem = payload;
    }
  }
};
