import {
  queryTestcaseByFuncId,
  exportCase,
  countTestcaseByFuncId,
  changeNecessary
} from '@/services/casebase';
import { listAll } from '@/services/functionmenu';
import {
  queryByPlanIdAll,
  testCaseDetailByTestcaseNoAndplanId,
  updatePlanlistTestcaseRelation,
  add
} from '@/services/TestCaseExecute';
import {
  batchRepeatedRelation,
  repeatedTestcaseForPlans
} from '@/services/testcase';
import { getUserListByRole } from '@/common/utlis';
export default {
  namespaced: true,

  state: {
    allUserNameList: [],
    testcase: {},
    exportData: {},
    countTest: 0,
    list: [],
    testCasesArr: [],
    testCaseDetailArr: [],
    updatePlanArr: []
  },

  actions: {
    async queryAllUserName({ commit }, payload) {
      let res = await getUserListByRole();
      commit('saveAllUserNameList', res);
    },
    async queryTestcaseByFuncId({ commit }, payload) {
      let res = await queryTestcaseByFuncId(payload);
      commit('saveTestcase', res);
    },
    async exportCaseFun({ commit }, payload) {
      let res = await exportCase(payload);
      commit('saveExportData', res);
    },
    async countTestcaseByFuncId({ commit }, payload) {
      let res = await countTestcaseByFuncId(payload);
      commit('saveCountTest', res);
    },
    async changeNecessary({ commit }, payload) {
      await changeNecessary(payload);
    },
    async listAll({ commit }, payload) {
      let res = await listAll(payload);
      commit('saveList', res);
    },
    async queryByPlanIdAll({ commit }, payload) {
      let res = await queryByPlanIdAll(payload);
      commit('saveTestCasesArr', res);
    },
    async testCaseDetailByTestcaseNoAndplanId({ commit }, payload) {
      let res = await testCaseDetailByTestcaseNoAndplanId(payload);
      commit('saveTestCaseDetailArr', res);
    },
    async updatePlanlistTestcaseRelation({ commit }, payload) {
      let res = await updatePlanlistTestcaseRelation(payload);
      commit('saveUpdatePlanArr', res);
    },
    async add({ commit }, payload) {
      await add(payload);
    },
    async batchRepeatedRelation({ commit }, payload) {
      await batchRepeatedRelation(payload);
    },
    async repeatedTestcaseForPlans({ commit }, payload) {
      await repeatedTestcaseForPlans(payload);
    }
  },

  mutations: {
    saveAllUserNameList(state, payload) {
      state.allUserNameList = payload;
    },
    saveTestcase(state, payload) {
      state.testcase = payload;
    },
    saveExportData(state, payload) {
      state.exportData = payload;
    },
    saveCountTest(state, payload) {
      state.countTest = payload;
    },
    saveList(state, payload) {
      state.list = payload;
    },
    saveTestCasesArr(state, payload) {
      state.testCasesArr = payload;
    },
    saveTestCaseDetailArr(state, payload) {
      state.testCaseDetailArr = payload;
    },
    saveUpdatePlanArr(state, payload) {
      state.updatePlanArr = payload;
    }
  }
};
