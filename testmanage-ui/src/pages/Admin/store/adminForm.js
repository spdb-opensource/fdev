import { queryOrderByOrderNo, sendSitDoneMail } from '@/services/order';
import {
  updateToDiscardCase,
  queryAllSystem,
  queryFuncMenuBySysId,
  queryTestcaseByOption,
  countTestcase,
  agreeEffect as agreeCaseEffect,
  rejectEffect as rejectCaseEffect,
  updateToGeneralCase,
  batchUpdateNotiftStatus,
  agreeThrough as agreeCaseThrough,
  rejectThrough as rejectCaseThrough,
  querySitMsgDetail,
  queryOrderByNo,
  querySitMsgList,
  countSitMsgList,
  queryRole,
  queryUser,
  update
} from '@/services/admin';
import { QueryDetailByTestcaseNo } from '@/services/testcase';
import {
  updateFunctionMenu,
  addFunctionMenu as addMenu,
  listAll,
  delFunctionMenu
} from '@/services/functionmenu';
import {
  updateNotifyStatus,
  queryAnnounce,
  sendAnnounce,
  selUser as queryGroup,
  countUser,
  updateUser,
  queryAllGroup,
  exportUserList,
  queryUserDetail
} from '@/services/useradmin';
import {
  queryUpSitOrderCount,
  queryUpSitOrder,
  exportSitReportData
} from '@/services/userchart';
import { sendStartUatMail, queryUatMailInfo } from '@/services/TestCaseExecute';
import { getUserListByRole } from '@/common/utlis';

export default {
  namespaced: true,

  state: {
    orderDetail: {},
    systemList: [],
    funcMenuList: [],
    testcaseData: {},
    testcaseCount: Number,
    sitMsgDetail: {},
    orderNoDetail: {},
    sitMsgList: [],
    sitMsgCount: Number,
    detailCase: {},
    userList: [],
    listAllData: [],
    announceList: [],
    groupList: [],
    userAdminData: [],
    sitOrderCount: Number,
    sitOrderData: [],
    sitReport: {},
    userCount: Number,
    userUpdataData: {},
    groupAll: [],
    roleList: [],
    userExport: {},
    userDetail: {},
    mailInfo: {},
    userInfo: {}
  },

  getters: {},

  actions: {
    async sendSitDoneMail({ commit }, payload) {
      await sendSitDoneMail(payload);
    },
    async queryOrderByOrderNo({ commit }, payload) {
      const response = await queryOrderByOrderNo(payload);
      commit('saveOrderDetail', response);
    },
    async updateToDiscardCase({ commit }, payload) {
      await updateToDiscardCase(payload);
    },
    async queryAllSystem({ commit }, payload) {
      const response = await queryAllSystem(payload);
      commit('saveallSystem', response);
    },
    async queryFuncMenuBySysId({ commit }, payload) {
      const response = await queryFuncMenuBySysId(payload);
      commit('saveaFuncMenu', response);
    },
    async queryTestcaseByOption({ commit }, payload) {
      const response = await queryTestcaseByOption(payload);
      commit('saveaTestcase', response);
    },
    async countTestcase({ commit }, payload) {
      const response = await countTestcase(payload);
      commit('saveTestcaseCount', response);
    },
    async agreeCaseEffect({ commit }, payload) {
      await agreeCaseEffect(payload);
    },
    async rejectCaseEffect({ commit }, payload) {
      await rejectCaseEffect(payload);
    },
    async updateToGeneralCase({ commit }, payload) {
      await updateToGeneralCase(payload);
    },
    async batchUpdateNotiftStatus({ commit }, payload) {
      await batchUpdateNotiftStatus(payload);
    },
    async agreeCaseThrough({ commit }, payload) {
      await agreeCaseThrough(payload);
    },
    async rejectCaseThrough({ commit }, payload) {
      await rejectCaseThrough(payload);
    },
    async querySitMsgDetail({ commit }, payload) {
      const response = await querySitMsgDetail(payload);
      commit('saveSitMsgDetail', response);
    },
    async queryOrderByNo({ commit }, payload) {
      const response = await queryOrderByNo(payload);
      commit('saveOrderNoDetail', response);
    },
    async querySitMsgList({ commit }, payload) {
      const response = await querySitMsgList(payload);
      commit('saveSitMsgList', response);
    },

    async countSitMsgList({ commit }, payload) {
      const response = await countSitMsgList(payload);
      commit('saveSitMsgCount', response);
    },
    async QueryDetailByTestcaseNo({ commit }, payload) {
      const response = await QueryDetailByTestcaseNo(payload);
      commit('saveDetailCase', response);
    },
    async queryAllUserName({ commit }, payload) {
      const response = await getUserListByRole();
      commit('saveUser', response);
    },
    async updateFunctionMenu({ commit }, payload) {
      await updateFunctionMenu(payload);
    },
    async addMenu({ commit }, payload) {
      await addMenu(payload);
    },
    async listAll({ commit }, payload) {
      const response = await listAll(payload);
      commit('savelistAll', response);
    },
    async delFunctionMenu({ commit }, payload) {
      await delFunctionMenu(payload);
    },
    async updateNotifyStatus({ commit }, payload) {
      await updateNotifyStatus(payload);
    },
    async queryAnnounce({ commit }, payload) {
      const response = await queryAnnounce(payload);
      commit('saveAnnounce', response);
    },
    async sendAnnounce({ commit }, payload) {
      await sendAnnounce(payload);
    },
    async queryGroup({ commit }, payload) {
      const response = await queryGroup(payload);
      commit('saveGroupList', response);
    },
    async queryUserAdmin({ commit }, payload) {
      const response = await getUserListByRole(payload);
      commit('saveUserAdmin', response);
    },
    async queryUser({ commit }, payload) {
      const response = await queryUser(payload);
      commit('saveUser', response);
    },
    async countUser({ commit }, payload) {
      const response = await countUser(payload);
      commit('saveUserCount', response);
    },
    async updateUser({ commit }, payload) {
      const response = await updateUser(payload);
      commit('saveUpdateUser', response);
    },
    async queryAllGroup({ commit }, payload) {
      const response = await queryAllGroup(payload);
      commit('saveAllGroup', response);
    },
    async queryAllRole({ commit }, payload) {
      const response = await queryRole(payload);
      commit('saveAllRole', response);
    },
    async exportUserList({ commit }, payload) {
      const response = await exportUserList(payload);
      commit('saveExportUser', response);
    },
    async queryUserDetail({ commit }, payload) {
      const response = await queryUserDetail(payload);
      commit('saveUserDetail', response);
    },
    async queryUpSitOrderCount({ commit }, payload) {
      const response = await queryUpSitOrderCount(payload);
      commit('saveSitOrderCount', response);
    },
    async queryUpSitOrder({ commit }, payload) {
      const response = await queryUpSitOrder(payload);
      commit('saveSitOrderList', response);
    },
    async exportSitReportData({ commit }, payload) {
      const response = await exportSitReportData(payload);
      commit('saveExportSitReport', response);
    },
    async sendStartUatMail({ commit }, payload) {
      await sendStartUatMail(payload);
    },
    async queryUatMailInfo({ commit }, payload) {
      const response = await queryUatMailInfo(payload);
      commit('saveMailInfo', response);
    },
    async update({ commit }, payload) {
      const response = await update(payload);
      commit('saveUserInfo', response);
    }
  },

  mutations: {
    saveOrderDetail(state, payload) {
      state.orderDetail = payload;
    },
    saveallSystem(state, payload) {
      state.systemList = payload;
    },
    saveaFuncMenu(state, payload) {
      state.funcMenuList = payload;
    },
    saveaTestcase(state, payload) {
      state.testcaseData = payload;
    },
    saveTestcaseCount(state, payload) {
      state.testcaseCount = payload;
    },
    saveSitMsgDetail(state, payload) {
      state.sitMsgDetail = payload;
    },
    saveOrderNoDetail(state, payload) {
      state.orderNoDetail = payload;
    },
    saveSitMsgList(state, payload) {
      state.sitMsgList = payload;
    },
    saveSitMsgCount(state, payload) {
      state.sitMsgCount = payload;
    },
    saveDetailCase(state, payload) {
      state.detailCase = payload;
    },
    saveUser(state, payload) {
      state.userList = payload;
    },
    savelistAll(state, payload) {
      state.listAllData = payload;
    },
    saveAnnounce(state, payload) {
      state.announceList = payload;
    },
    saveGroupList(state, payload) {
      state.groupList = payload;
    },
    saveUserAdmin(state, payload) {
      state.userAdminData = payload;
    },
    saveUserCount(state, payload) {
      state.userCount = payload;
    },
    saveUpdateUser(state, payload) {
      state.userUpdataData = payload;
    },
    saveAllGroup(state, payload) {
      state.groupAll = payload;
    },
    saveAllRole(state, payload) {
      state.roleList = payload;
    },
    saveExportUser(state, payload) {
      state.userExport = payload;
    },
    saveUserDetail(state, payload) {
      state.userDetail = payload;
    },
    saveSitOrderCount(state, payload) {
      state.sitOrderCount = payload;
    },
    saveSitOrderList(state, payload) {
      state.sitOrderData = payload;
    },
    saveExportSitReport(state, payload) {
      state.sitReport = payload;
    },
    saveMailInfo(state, payload) {
      state.mailInfo = payload;
    },
    saveUserInfo(state, payload) {
      state.userInfo = payload;
    }
  }
};
