import { queryTaskNameTestersByNo } from '@/services/order';
import {
  UpdateTestcaseByTestcaseNo,
  AddTestcase,
  QueryAllSystem,
  CopyTestcaseToOtherPlan,
  QueryDetailByTestcaseNo,
  UpdateTestcaseByStatusWaitEffect,
  UpdateTestcaseByStatusWaitPass,
  DeleteTestcaseByTestcaseNo,
  QueryTestcaseByPlanId,
  queryPlanAllStatus,
  delBatchRelationCase,
  QueryTestCount,
  batchCommitAudit,
  batchEffectAudit,
  batchExecuteTestcase,
  batchCopyTestcaseToOtherPlan,
  exportExcelTestcase,
  downloadTemplate,
  batchAdd,
  queryRelativePeople
} from '@/services/testcase';

import { queryAllUserName } from '@/services/casebase';

import {
  queryByPlanIdAll,
  isTestcaseAddIssue,
  sendCaseEmail,
  delPlan,
  testCaseCustomSort,
  queryUserValidOrder,
  movePlanOrCase
} from '@/services/TestCaseExecute';

import { queryIssueByPlanResultId } from '@/services/mantis';
import { addPlan, queryByworkNo, updateByPrimaryKey } from '@/services/plan';

import { queryFuncMenuBySysId } from '@/services/admin';
export default {
  namespaced: true,

  state: {
    allPeoples: null,
    relativePeoples: {},
    workOrderNoData: {},
    systemNameString: [],
    caseDetail: {},
    testCaseData: [],
    planInfoList: {},
    totalCount: 0,
    fileObj: {},
    fileTemlate: {},
    allPlan: [],
    isTestCase: Boolean,
    targetWork: {},
    tableData: {},
    planListArr: [],
    menus: []
  },

  actions: {
    async queryRelativePeople({ commit }, payload) {
      let res = await queryRelativePeople(payload);
      commit('saveRelativePeople', res);
    },
    async queryAllPeople({ commit }, payload) {
      let res = await queryAllUserName(payload);
      commit('saveAllPeople', res);
    },
    // queryAllUserName
    async queryTaskNameTestersByNo({ commit }, payload) {
      let res = await queryTaskNameTestersByNo(payload);
      commit('saveWorkOrderNoData', res);
    },
    async UpdateTestcaseByTestcaseNo({ commit }, payload) {
      await UpdateTestcaseByTestcaseNo(payload);
    },
    async AddTestcase({ commit }, payload) {
      await AddTestcase(payload);
    },
    async QueryAllSystem({ commit }, payload) {
      let res = await QueryAllSystem(payload);
      commit('saveSystemName', res);
    },
    async CopyTestcaseToOtherPlan({ commit }, payload) {
      await CopyTestcaseToOtherPlan(payload);
    },
    async QueryDetailByTestcaseNo({ commit }, payload) {
      let res = await QueryDetailByTestcaseNo(payload);
      commit('saveCaseDetail', res);
    },
    async UpdateTestcaseByStatusWaitEffect({ commit }, payload) {
      await UpdateTestcaseByStatusWaitEffect(payload);
    },
    async UpdateTestcaseByStatusWaitPass({ commit }, payload) {
      await UpdateTestcaseByStatusWaitPass(payload);
    },
    async DeleteTestcaseByTestcaseNo({ commit }, payload) {
      await DeleteTestcaseByTestcaseNo(payload);
    },
    async QueryTestcaseByPlanId({ commit }, payload) {
      let res = await QueryTestcaseByPlanId(payload);
      commit('saveTestCaseData', res);
    },
    async queryPlanAllStatus({ commit }, payload) {
      let res = await queryPlanAllStatus(payload);
      commit('savePlanInfoList', res);
    },
    async delBatchRelationCase({ commit }, payload) {
      await delBatchRelationCase(payload);
    },
    async QueryTestCount({ commit }, payload) {
      let res = await QueryTestCount(payload);
      commit('saveTotalCount', res);
    },
    async batchCommitAuditFun({ commit }, payload) {
      await batchCommitAudit(payload);
    },
    async batchEffectAuditFun({ commit }, payload) {
      await batchEffectAudit(payload);
    },
    async batchExecuteTestcaseFun({ commit }, payload) {
      await batchExecuteTestcase(payload);
    },
    async batchCopyTestcaseToOtherPlanFun({ commit }, payload) {
      await batchCopyTestcaseToOtherPlan(payload);
    },
    async exportExcelTestcaseFun({ commit }, payload) {
      let res = await exportExcelTestcase(payload);
      commit('saveFileObj', res);
    },
    async downloadTemplate({ commit }, payload) {
      let res = await downloadTemplate(payload);
      commit('saveFile', res);
    },
    async batchAdd({ commit }, payload) {
      await batchAdd(payload);
    },
    async queryByPlanIdAll({ commit }, payload) {
      let res = await queryByPlanIdAll(payload);
      commit('saveAllPlan', res);
    },
    async isTestcaseAddIssue({ commit }, payload) {
      let res = await isTestcaseAddIssue(payload);
      commit('saveIsTestCase', res);
    },
    async sendCaseEmail({ commit }, payload) {
      await sendCaseEmail(payload);
    },
    async delPlan({ commit }, payload) {
      await delPlan(payload);
    },

    async testCaseCustomSort({ commit }, payload) {
      await testCaseCustomSort(payload);
    },
    async queryUserValidOrder({ commit }, payload) {
      let res = await queryUserValidOrder(payload);
      commit('saveTargetWork', res);
    },
    async movePlanOrCase({ commit }, payload) {
      await movePlanOrCase(payload);
    },
    async queryIssueByPlanResultId({ commit }, payload) {
      let res = await queryIssueByPlanResultId(payload);
      commit('saveTableData', res);
    },
    async addPlan({ commit }, payload) {
      await addPlan(payload);
    },
    async queryByworkNo({ commit }, payload) {
      let res = await queryByworkNo(payload);
      commit('savePlanList', res);
    },
    async updateByPrimaryKey({ commit }, payload) {
      await updateByPrimaryKey(payload);
    },
    async queryFuncMenuBySysId({ commit }, payload) {
      let res = await queryFuncMenuBySysId(payload);
      commit('saveMenus', res);
    }
  },

  mutations: {
    saveRelativePeople(state, payload) {
      state.relativePeoples = payload;
    },
    saveAllPeople(state, payload) {
      state.allPeoples = payload;
    },
    saveWorkOrderNoData(state, payload) {
      state.workOrderNoData = payload;
    },
    saveSystemName(state, payload) {
      state.systemNameString = payload;
    },
    saveCaseDetail(state, payload) {
      state.caseDetail = payload;
    },
    saveTestCaseData(state, payload) {
      state.testCaseData = payload;
    },
    savePlanInfoList(state, payload) {
      state.planInfoList = payload;
    },
    saveTotalCount(state, payload) {
      state.totalCount = payload;
    },
    saveFileObj(state, payload) {
      state.fileObj = payload;
    },
    saveFile(state, payload) {
      state.fileTemlate = payload;
    },
    saveAllPlan(state, payload) {
      state.allPlan = payload;
    },
    saveIsTestCase(state, payload) {
      state.isTestCase = payload;
    },
    saveTargetWork(state, payload) {
      state.targetWork = payload;
    },
    saveTableData(state, payload) {
      state.tableData = payload;
    },
    savePlanList(state, payload) {
      state.planListArr = payload;
    },
    saveMenus(state, payload) {
      state.menus = payload;
    }
  }
};
