import {
  oauthLogin,
  queryAssignOrderImpl,
  queryMyOrderImpl,
  iWantToassignImpl,
  queryAllUserImpl,
  submitAddOrderFormImpl,
  submitOrderFormImpl,
  updateOrderImpl,
  queryMessageImpl,
  ignoreMessageImpl,
  ignoreAllMessageImpl,
  queryMessageCount,
  queryTaskDoc,
  queryWorkOrderStageList,
  workOrderRollback,
  queryTotalImpl,
  queryRqrFilesByOrderNo,
  preview,
  rollBackWorkOrder,
  exportUserAllOrder,
  exportExcelData,
  queryTaskListImpl,
  verifyOrderName,
  queryTasks,
  queryMergeOrderList,
  splitWorkOrder,
  mergeWorkOrder,
  queryFdevTaskByWorkNo,
  queryWorkOrderName,
  refuseTask,
  querySplitOrderList,
  queryLastTransFilePath
} from '@/services/order';
import { addPlan } from '@/services/plan';
import { exportExcel, getUserListByRole } from '@/common/utlis';
export default {
  namespaced: true,

  state: {
    filePath: '',
    splitWorkOrderList: [],
    defaultWorkOrderName: '',
    fdevTaskByWorkNo: [],
    splitWorkOrderInfo: {},
    mergeWorkOrder: {},
    taskAllData: {},
    mergeOrderList: {},
    verifyOrderNameBoolean: Boolean,
    oauth: [],
    assignOrderImpl: [],
    myOrderImpl: [],
    testerOptions: [],
    allUserImpl: {},
    messageImpl: [],
    assessorList: [],
    groupList: [],
    messageCount: 0,
    taskDoc: [],
    count: {},
    wordOrderStage: [],
    stageList: [],
    rqrFiles: [],
    previewUrl: {},
    allOrder: '',
    childTaskList: [],
    rqrFilesByOrderNo: {}
  },

  getters: {
    managers: ({ allUserImpl }) => {
      return allUserImpl.managers;
    },
    groupLeaders: ({ allUserImpl }) => {
      return allUserImpl.groupLeaders;
    },
    testers: ({ allUserImpl }) => {
      return allUserImpl.testers;
    }
  },

  actions: {
    async queryLastTransFilePath({ commit }, payload) {
      const response = await queryLastTransFilePath(payload);
      commit('saveFilePath', response);
    },
    async querySplitOrderList({ commit }, payload) {
      const response = await querySplitOrderList(payload);
      commit('saveQuerySplitOrderList', response);
    },
    async refuseTask({ commit }, payload) {
      await refuseTask(payload);
    },
    async queryWorkOrderName({ commit }, payload) {
      const response = await queryWorkOrderName(payload);
      commit('saveQueryWorkOrderName', response);
    },
    async queryFdevTaskByWorkNo({ commit }, payload) {
      const response = await queryFdevTaskByWorkNo(payload);
      commit('saveQueryFdevTaskByWorkNo', response);
    },
    async splitWorkOrder({ commit }, payload) {
      const response = await splitWorkOrder(payload);
      commit('saveSplitWorkOrder', response);
    },
    async mergeWorkOrder({ commit }, payload) {
      const response = await mergeWorkOrder(payload);
      commit('saveMergeWorkOrder', response);
    },
    async queryTasks({ commit }, payload) {
      const response = await queryTasks(payload);
      commit('saveQueryTasks', response);
    },
    async queryMergeOrderList({ commit }, payload) {
      const response = await queryMergeOrderList(payload);
      commit('saveMergeOrderList', response);
    },
    // queryMergeOrderList
    async verifyOrderName({ commit }, payload) {
      const response = await verifyOrderName(payload);
      commit('saveVerifyOrderName', response);
    },
    async oauthLogin({ commit }, payload) {
      const response = await oauthLogin(payload);
      commit('saveOauthLogin', response);
    },
    async queryAssignOrderImpl({ commit }, payload) {
      const response = await queryAssignOrderImpl(payload);
      commit('saveAssignOrderImpl', response);
    },
    async queryMyOrderImpl({ commit }, payload) {
      const response = await queryMyOrderImpl(payload);
      commit('saveMyOrderImpl', response);
    },
    async queryAllUserImpl({ commit }, payload) {
      const response = await queryAllUserImpl(payload);
      commit('saveAllUserImpl', response);
    },
    async queryMessageImpl({ commit }, payload) {
      const response = await queryMessageImpl(payload);
      commit('saveMessageImpl', response);
    },
    async queryAssessorList({ commit }, payload) {
      const response = await getUserListByRole();
      commit('saveAssessorList', response);
    },
    async selectNameImpl({ commit }, payload) {
      const response = await getUserListByRole([
        '测试人员',
        '玉衡-测试管理员',
        '玉衡超级管理员',
        '玉衡-测试组长'
      ]);
      commit('saveGroupList', response);
    },
    async queryMessageCount({ commit }, payload) {
      const response = await queryMessageCount(payload);
      commit('saveMessageCount', response);
    },
    async queryTaskDoc({ commit }, payload) {
      const response = await queryTaskDoc(payload);
      commit('saveTaskDoc', response);
    },
    async queryTotalImpl({ commit }, payload) {
      const response = await queryTotalImpl(payload);
      commit('saveCount', response);
    },
    async queryRqrFilesByOrderNo({ commit }, payload) {
      const response = await queryRqrFilesByOrderNo(payload);
      commit('saveRqrFilesByOrderNo', response);
    },
    async preview({ commit }, payload) {
      const response = await preview(payload);
      commit('savePreview', response);
    },
    async rollBackWorkOrder({ commit }, payload) {
      await rollBackWorkOrder(payload);
    },
    async exportUserAllOrder({ commit }, payload) {
      const response = await exportUserAllOrder(payload);
      commit('saveUserAllOrder', response);
    },
    async iWantToassignImpl({ commit }, payload) {
      await iWantToassignImpl(payload);
    },
    async submitAddOrderFormImpl({ commit }, payload) {
      await submitAddOrderFormImpl(payload);
    },
    async submitOrderFormImpl({ commit }, payload) {
      await submitOrderFormImpl(payload);
    },
    async updateOrderImpl({ commit }, payload) {
      await updateOrderImpl(payload);
    },
    async ignoreMessageImpl({ commit }, payload) {
      await ignoreMessageImpl(payload);
    },
    async ignoreAllMessageImpl({ commit }, payload) {
      await ignoreAllMessageImpl(payload);
    },
    async queryWorkOrderStageList({ commit }, payload) {
      const response = await queryWorkOrderStageList(payload);
      commit('saveWordOrderStage', response);
    },
    async workOrderRollback({ commit }, payload) {
      await workOrderRollback(payload);
    },
    async rollBackWorkOrder({ commit }, payload) {
      await rollBackWorkOrder(payload);
    },
    async addPlan({ commit }, payload) {
      await addPlan(payload);
    },
    async downExcel({ commit }, payload) {
      const response = await exportExcelData(payload);
      exportExcel(response);
    },
    async queryTaskList({ commit }, payload) {
      const response = await queryTaskListImpl(payload);
      commit('saveChildTaskList', response);
    }
  },

  mutations: {
    saveFilePath(state, payload) {
      state.filePath = payload;
    },
    saveQuerySplitOrderList(state, payload) {
      state.splitWorkOrderList = payload;
    },
    saveQueryWorkOrderName(state, payload) {
      state.defaultWorkOrderName = payload;
    },
    saveQueryFdevTaskByWorkNo(state, payload) {
      state.fdevTaskByWorkNo = payload;
    },
    saveSplitWorkOrder(state, payload) {
      state.splitWorkOrderInfo = payload;
    },
    saveMergeWorkOrder(state, payload) {
      state.mergeWorkOrder = payload;
    },
    // saveMergeWorkOrder
    saveQueryTasks(state, payload) {
      state.taskAllData = payload;
    },
    saveMergeOrderList(state, payload) {
      state.mergeOrderList = payload;
    },
    saveRqrFilesByOrderNo(state, payload) {
      state.rqrFilesByOrderNo = payload;
    },
    saveVerifyOrderName(state, payload) {
      state.verifyOrderNameBoolean = payload;
    },
    saveOauthLogin(state, payload) {
      state.oauth = payload;
    },
    saveMyOrderImpl(state, payload) {
      state.myOrderImpl = payload;
    },
    saveAllUserImpl(state, payload) {
      state.allUserImpl = payload;
    },
    saveMessageImpl(state, payload) {
      state.messageImpl = payload;
    },
    saveAssessorList(state, payload) {
      state.assessorList = payload;
    },
    saveGroupList(state, payload) {
      state.groupList = payload;
    },
    saveCount(state, payload) {
      state.count = payload;
    },
    saveRqrFiles(state, payload) {
      state.rqrFiles = payload;
    },
    savePreview(state, payload) {
      state.previewUrl = payload;
    },
    saveUserAllOrder(state, payload) {
      state.allOrder = payload;
    },
    saveMessageCount(state, payload) {
      state.messageCount = payload.total;
    },
    saveTaskDoc(state, payload) {
      state.taskDoc = payload.doc ? payload.doc : [];
    },
    saveWordOrderStage(state, payload) {
      state.wordOrderStage = payload;
    },
    saveAssignOrderImpl(state, payload) {
      state.assignOrderImpl = payload;
    },
    saveChildTaskList(state, payload) {
      state.childTaskList = payload ? payload : [];
    }
  }
};
