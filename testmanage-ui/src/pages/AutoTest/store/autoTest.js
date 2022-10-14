import {
  queryMenu,
  addMenu,
  deleteMenu,
  updateMenu,
  addCase,
  queryCase,
  deleteCase,
  updateCase,
  excuteCase,
  genFile,
  addCaseDetail,
  queryCaseDetail,
  deleteCaseDetail,
  updateCaseDetail,
  addData,
  deleteData,
  updateData,
  queryData,
  addAssert,
  deleteAssert,
  updateAssert,
  queryAssert,
  addModule,
  deleteModule,
  updateModule,
  queryModule,
  addUser,
  deleteUser,
  updateUser,
  queryUser,
  addElementDic,
  deleteElementDic,
  updateElementDic,
  queryElementDic,
  addElement,
  deleteElement,
  updateElement,
  queryElement,
  addModuleDetail,
  deleteModuleDetail,
  updateModuleDetail,
  queryModuleDetail,
  queryCaseDetailByTestCaseNo,
  queryModuleDetailByModuleId,
  queryDataByTestCaseNo,
  queryAssertByTestCaseNo,
  queryDataByModuleId
} from '@/services/autoTest';

export default {
  namespaced: true,

  state: {
    menuData: [],
    caseList: [],
    folder: '',
    caseDetailList: [],
    dataList: [],
    assertList: [],
    moduleList: [],
    moduleDetailList: [],
    userList: [],
    elementDicList: [],
    elementList: [],
    caseDataList: [],
    caseAssertList: [],
    moduleDataList: []
  },
  actions: {
    async queryMenu({ commit }, payload) {
      const response = await queryMenu(payload);
      commit('saveMenuData', response);
    },
    async addMenu({ commit }, payload) {
      await addMenu(payload);
    },
    async deleteMenu({ commit }, payload) {
      await deleteMenu(payload);
    },
    async updateMenu({ commit }, payload) {
      await updateMenu(payload);
    },
    async queryCase({ commit }, payload) {
      const response = await queryCase(payload);
      commit('saveCaseList', response);
    },
    async addCase({ commit }, payload) {
      await addCase(payload);
    },
    async deleteCase({ commit }, payload) {
      await deleteCase(payload);
    },
    async updateCase({ commit }, payload) {
      await updateCase(payload);
    },
    async genFile({ commit }, payload) {
      const resonse = await genFile(payload);
      commit('saveFolder', resonse);
    },
    async excuteCase({ commit }, payload) {
      await excuteCase(payload);
    },
    async addCaseDetail({ commit }, payload) {
      await addCaseDetail(payload);
    },
    async queryCaseDetail({ commit }, payload) {
      const response = await queryCaseDetail(payload);
      commit('saveCaseDetail', response);
    },
    async deleteCaseDetail({ commit }, payload) {
      await deleteCaseDetail(payload);
    },
    async updateCaseDetail({ commit }, payload) {
      await updateCaseDetail(payload);
    },

    async addData({ commit }, payload) {
      await addData(payload);
    },
    async queryData({ commit }, payload) {
      const response = await queryData(payload);
      commit('saveDataList', response);
    },
    async deleteData({ commit }, payload) {
      await deleteData(payload);
    },
    async updateData({ commit }, payload) {
      await updateData(payload);
    },
    async addAssert({ commit }, payload) {
      await addAssert(payload);
    },
    async queryAssert({ commit }, payload) {
      const response = await queryAssert(payload);
      commit('saveAssertList', response);
    },
    async deleteAssert({ commit }, payload) {
      await deleteAssert(payload);
    },
    async updateAssert({ commit }, payload) {
      await updateAssert(payload);
    },

    async addModule({ commit }, payload) {
      await addModule(payload);
    },
    async queryModule({ commit }, payload) {
      const response = await queryModule(payload);
      commit('saveModule', response);
    },
    async deleteModule({ commit }, payload) {
      await deleteModule(payload);
    },
    async updateModule({ commit }, payload) {
      await updateModule(payload);
    },
    async addUser({ commit }, payload) {
      await addUser(payload);
    },
    async queryUser({ commit }, payload) {
      const response = await queryUser(payload);
      commit('saveUserList', response);
    },
    async deleteUser({ commit }, payload) {
      await deleteUser(payload);
    },
    async updateUser({ commit }, payload) {
      await updateUser(payload);
    },
    async addElementDic({ commit }, payload) {
      await addElementDic(payload);
    },
    async queryElementDic({ commit }, payload) {
      const response = await queryElementDic(payload);
      commit('saveElementDicList', response);
    },
    async deleteElementDic({ commit }, payload) {
      await deleteElementDic(payload);
    },
    async updateElementDic({ commit }, payload) {
      await updateElementDic(payload);
    },
    async addElement({ commit }, payload) {
      await addElement(payload);
    },
    async queryElement({ commit }, payload) {
      const response = await queryElement(payload);
      commit('saveElementList', response);
    },
    async deleteElement({ commit }, payload) {
      await deleteElement(payload);
    },
    async updateElement({ commit }, payload) {
      await updateElement(payload);
    },
    async addModuleDetail({ commit }, payload) {
      await addModuleDetail(payload);
    },
    async queryModuleDetail({ commit }, payload) {
      const response = await queryModuleDetail(payload);
      commit('saveModuleDetailList', response);
    },
    async deleteModuleDetail({ commit }, payload) {
      await deleteModuleDetail(payload);
    },
    async updateModuleDetail({ commit }, payload) {
      await updateModuleDetail(payload);
    },
    async queryCaseDetailByTestCaseNo({ commit }, payload) {
      const response = await queryCaseDetailByTestCaseNo(payload);
      commit('saveCaseDetail', response);
    },
    async queryModuleDetailByModuleId({ commit }, payload) {
      const response = await queryModuleDetailByModuleId(payload);
      commit('saveModuleDetailList', response);
    },
    async queryDataByTestCaseNo({ commit }, payload) {
      const response = await queryDataByTestCaseNo(payload);
      commit('saveCaseDataList', response);
    },
    async queryAssertByTestCaseNo({ commit }, payload) {
      const response = await queryAssertByTestCaseNo(payload);
      commit('saveCaseAssertList', response);
    },
    async queryDataByModuleId({ commit }, payload) {
      const response = await queryDataByModuleId(payload);
      commit('saveModuleDataList', response);
    }
  },
  mutations: {
    saveMenuData(state, payload) {
      state.menuData = payload;
    },
    saveCaseList(state, payload) {
      state.caseList = payload;
    },
    saveFolder(state, payload) {
      state.folder = payload;
    },
    saveCaseDetail(state, payload) {
      state.caseDetailList = payload;
    },
    saveDataList(state, payload) {
      state.dataList = payload;
    },
    saveAssertList(state, payload) {
      state.assertList = payload;
    },
    saveModule(state, payload) {
      state.moduleList = payload;
    },
    saveUserList(state, payload) {
      state.userList = payload;
    },
    saveElementDicList(state, payload) {
      state.elementDicList = payload;
    },
    saveElementList(state, payload) {
      state.elementList = payload;
    },
    saveModuleDetailList(state, payload) {
      state.moduleDetailList = payload;
    },
    saveCaseDataList(state, payload) {
      state.caseDataList = payload;
    },
    saveCaseAssertList(state, payload) {
      state.caseAssertList = payload;
    },
    saveModuleDataList(state, payload) {
      state.moduleDataList = payload;
    }
  }
};
