import { resolveResponseError, exportExcel } from '@/utils/utils';

import {
  queryByFdevNoAndDemandId,
  save,
  update,
  queryDemandList,
  deleteRqr,
  fileRqr,
  queryPaginationByDemandId,
  queryDemandInfoDetail,
  deleteById,
  deleteUnitById,
  addImplementUnit,
  supplyImplementUnit,
  updateImplementUnit,
  search,
  queryByGroupId,
  queryUnitByIpmpTaskId,
  addIpmpTask,
  addUnit,
  queryDemandDoc,
  exportExcelData,
  updateDemandDoc,
  exportAssessExcel,
  getDesignInfo,
  updateDesignStage,
  updateDesignRemark,
  assess,
  queryImplByGroupAndDemandId,
  queryAvailableIpmpUnit,
  defer,
  recover,
  updateByRecover,
  deleteDemandDoc,
  exportDemandsExcel,
  exportModelExcel,
  importDemandExcel,
  queryDemandByOaContactNo,
  updateImpl,
  queryPartInfo,
  queryIpmpUnitByDemandId,
  queryIpmpUser,
  queryIpmpProject,
  queryIpmpLeadTeam,
  mount,
  updateIpmpUnit,
  queryPaginationByIpmpUnitNo,
  assessButton,
  queryAvailableIpmpUnitNew,
  queryApproveList,
  approvePass,
  approveReject,
  queryDemandAssessDate,
  applyApprove,
  queryMyApproveList,
  queryTestOrderDetail,
  queryByTypes,
  queryRqrApproveList,
  queryMyList,
  queryCount
} from '../services/methods.js';
import { queryTaskByDemandId } from '@/services/job';

export default {
  namespaced: true,

  state: {
    fdevNOinfo: {},
    newDemandData: '',
    updateDemandData: [],
    deleteRqrData: [],
    fileRqrData: [],
    demandList: [],
    implementUnitData: [],
    demandInfoDetail: {},
    ipmpTasks: [],
    UnitNoList: [],
    deleteUnitByIdData: [],
    searchIpmpData: [],
    taskByGroupIdData: [],
    unitByIpmpTaskIdData: [],
    newIpmpTaskData: [],
    newUnitData: [],
    taskList: [],
    demandDocList: [],
    demandDocListData: null,
    jobProfile: {},
    implUnitQueriedByGroupId: [],
    relatedPartIds: [],
    ipmpUnitList: [],
    ipmpUnitListNew: [],
    assessExcel: '',
    demandListExcel: '',
    modelExcel: '',
    evaluateExcel: {},
    relatePartDetail: [],
    oaNOStatus: false,
    theQueryPartInfo: {},
    ipmpUnitListTable: [],
    ipmpUser: [],
    ipmpProject: [],
    ipmpLeadTeam: [],
    ipmpUnitNoList: [], //实施单元下研发单元列表
    assessButFlag: {}, //评估完成按钮
    updateIpmpUnits: {},
    approveList: [], //研发单元审批列表
    myApproveList: [], //我的研发单元审批列表
    demandAssessDate: {}, //评估完成时间和XY值
    testOrderDetails: {}, //提测单详情
    demandLabelList: [], //需求标签列表
    finalApprovalList: [], //定稿日期审核列表
    myApprovalList: [], //我的定稿日期审核列表
    myRqrProvalCount: null //定稿审核数量
  },

  getters: {
    groupsCannotSelect: state => {
      const { relatePartDetail, implUnitQueriedByGroupId } = state;
      const hasAdd = implUnitQueriedByGroupId.some(item => {
        return item.add_flag > 0;
      });
      return relatePartDetail
        .filter(part => {
          return hasAdd || part.assess_status > 1;
        })
        .map(part => part.part_id);
    },
    repGroupsCannotSelect: state => {
      const { relatePartDetail, implUnitQueriedByGroupId } = state;
      const allNew = implUnitQueriedByGroupId.every(item => {
        return item.add_flag === 0 || item.add_flag === null;
      });
      return relatePartDetail
        .filter(part => {
          return allNew && part.assess_status <= 1;
        })
        .map(part => part.part_id);
    }
  },

  actions: {
    async queryByFdevNoAndDemandId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryByFdevNoAndDemandId(payload)
      );
      commit('saveFdevNoInfo', response);
    },
    async exportAssessExcel({ commit }, payload) {
      const response = await resolveResponseError(() =>
        exportAssessExcel(payload)
      );
      exportExcel(response, '需求评估表.xlsx');
    },
    async updateDemandDoc({ commit }, payload) {
      await resolveResponseError(() => updateDemandDoc(payload));
    },
    async exportExcelData({ commit }, payload) {
      const response = await resolveResponseError(() =>
        exportExcelData(payload)
      );
      exportExcel(response);
      // commit('saveDemandDoc', response);
    },
    async queryDemandDoc({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryDemandDoc(payload)
      );
      commit('saveDemandDoc', response);
    },
    async queryTaskByDemandId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTaskByDemandId(payload)
      );
      commit('saveTaskByDemandId', response);
    },
    async save({ commit }, payload) {
      const response = await resolveResponseError(() => save(payload));
      commit('saveNewDemandData', response);
    },
    async queryDemandList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryDemandList(payload)
      );
      commit('saveDemandList', response);
    },
    async update({ commit }, payload) {
      const response = await resolveResponseError(() => update(payload));
      commit('saveUpdateDemand', response);
      // const response = await resolveResponseError(() => save(payload));
      // commit('updateDemand', response);
    },
    async deleteRqr({ commit }, payload) {
      const response = await resolveResponseError(() => deleteRqr(payload));
      commit('saveDeleteDemand', response);
    },
    async fileRqr({ commit }, payload) {
      const response = await resolveResponseError(() => fileRqr(payload));
      commit('saveFileDemand', response);
    },
    async queryPaginationByDemandId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryPaginationByDemandId(payload)
      );
      commit('saveImplementUnitData', response);
    },
    async queryDemandInfoDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryDemandInfoDetail(payload)
      );
      commit('saveDemandInfoDetail', response);
      commit('saveRelatedPartIds', response.relate_part);
      commit('saveRelatePartDetail', response.relate_part_detail);
    },
    async deleteUnitById({ commit }, payload) {
      const response = await resolveResponseError(() =>
        deleteUnitById(payload)
      );
      commit('saveDeleteUnitById', response);
    },
    async addImplementUnit({ commit }, payload) {
      await resolveResponseError(() => addImplementUnit(payload));
    },
    async supplyImplementUnit({ commit }, payload) {
      await resolveResponseError(() => supplyImplementUnit(payload));
    },
    async updateImplementUnit({ commit }, payload) {
      await resolveResponseError(() => updateImplementUnit(payload));
    },
    async search({ commit }, payload) {
      const response = await resolveResponseError(() => search(payload));
      commit('saveTaskAndIpmpData', response);
    },
    async queryByGroupId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryByGroupId(payload)
      );
      commit('saveTaskByGroupIdData', response);
      commit('saveIpmpTasks', response);
    },
    async queryUnitByIpmpTaskId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryUnitByIpmpTaskId(payload)
      );
      commit('saveUnitByIpmpTaskIdData', response);
    },
    async addIpmpTask({ commit }, payload) {
      const response = await resolveResponseError(() => addIpmpTask(payload));
      commit('saveNewIpmpTaskData', response);
    },
    async addUnit({ commit }, payload) {
      const response = await resolveResponseError(() => addUnit(payload));
      commit('saveNewUnitData', response);
    },
    async getDesignInfo({ commit }, payload) {
      const response = await resolveResponseError(() => getDesignInfo(payload));
      commit('saveJobProfile', response);
    },
    async updateDesignStage({ commit }, payload) {
      await resolveResponseError(() => updateDesignStage(payload));
    },
    async updateDesignRemark({ commit }, payload) {
      await resolveResponseError(() => updateDesignRemark(payload));
    },
    async deleteById({ commit }, payload) {
      await resolveResponseError(() => deleteById(payload));
    },
    async assess({ commit }, payload) {
      await resolveResponseError(() => assess(payload));
    },
    async queryImplByGroupAndDemandId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryImplByGroupAndDemandId(payload)
      );
      commit('saveImplUnitQueriedByGroupId', response);
    },
    async queryAvailableIpmpUnit({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAvailableIpmpUnit(payload)
      );
      commit('saveIpmpUnit', response);
    },
    async queryAvailableIpmpUnitNew({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAvailableIpmpUnitNew(payload)
      );
      commit('saveIpmpUnitNew', response);
    },
    async defer({ commit }, payload) {
      await resolveResponseError(() => defer(payload));
    },
    async recover({ commit }, payload) {
      await resolveResponseError(() => recover(payload));
    },
    async updateByRecover({ commit }, payload) {
      await resolveResponseError(() => updateByRecover(payload));
    },
    async deleteDemandDoc({ commit }, payload) {
      await resolveResponseError(() => deleteDemandDoc(payload));
    },
    async exportDemandsExcel({ commit }, payload) {
      const response = await resolveResponseError(() =>
        exportDemandsExcel(payload)
      );
      commit('saveDemandListExcel', response);
    },
    async exportModelExcel({ commit }, payload) {
      const response = await resolveResponseError(() =>
        exportModelExcel(payload)
      );
      commit('saveModelExcel', response);
    },
    async importDemandExcel({ commit }, payload) {
      const response = await resolveResponseError(() =>
        importDemandExcel(payload)
      );
      commit('saveEvaluateExcel', response);
    },
    async queryDemandByOaContactNo({ commit }, payload) {
      await queryDemandByOaContactNo(payload).then(res => {
        if (res === null) {
          commit('saveOaNOStatus', res);
        } else {
          commit('saveOaNOStatus', res.demandCount);
        }
      });
    },
    async updateImpl({ commit }, payload) {
      await resolveResponseError(() => updateImpl(payload));
    },
    async theSaveQueryPartInfo({ commit }, payload) {
      const response = await resolveResponseError(() => queryPartInfo(payload));
      commit('saveQueryPartInfo', response);
    },
    async queryIpmpUnitByDemandId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryIpmpUnitByDemandId(payload)
      );
      commit('saveIpmpUnitListTable', response);
    },
    //获取ipmp人员信息
    async queryIpmpUser({ commit }, payload) {
      const response = await resolveResponseError(() => queryIpmpUser(payload));
      commit('saveIpmpUser', response);
    },
    //查询项目和任务集信息
    async queryIpmpProject({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryIpmpProject(payload)
      );
      commit('saveIpmpProject', response);
    },
    //查询项目和任务集信息
    async queryIpmpLeadTeam({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryIpmpLeadTeam(payload)
      );
      commit('saveIpmpLeadTeam', response);
    },
    //挂载
    async mount({ commit }, payload) {
      await resolveResponseError(() => mount(payload));
    },
    //查询实施单元下研发单元
    async queryPaginationByIpmpUnitNo({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryPaginationByIpmpUnitNo(payload)
      );
      commit('saveIpmpUnitNoList', response);
    },
    //查询实施单元下研发单元
    async updateIpmpUnit({ commit }, payload) {
      let response = await resolveResponseError(() => updateIpmpUnit(payload));
      commit('saveUpdateIpmpUnit', response);
    },
    //查询评估完成按钮是否置灰
    async assessButton({ commit }, payload) {
      const response = await resolveResponseError(() => assessButton(payload));
      commit('saveAssessButFlag', response);
    },
    //查询研发单元审批列表
    async queryApproveList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryApproveList(payload)
      );
      commit('saveApprovelist', response);
    },
    //查询我的研发单元审批列表
    async queryMyApproveList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryMyApproveList(payload)
      );
      commit('saveMyApprovelist', response);
    },
    //审批研发单元通过
    async approvePass({ commit }, payload) {
      await resolveResponseError(() => approvePass(payload));
    },
    //审批研发单元拒绝
    async approveReject({ commit }, payload) {
      await resolveResponseError(() => approveReject(payload));
    },
    //申请研发单元审批
    async applyApprove({ commit }, payload) {
      await resolveResponseError(() => applyApprove(payload));
    },
    //查询评估完成时间和XY值
    async queryDemandAssessDate({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryDemandAssessDate(payload)
      );
      commit('saveDemandAssessDate', response);
    },
    //查询提测单详情
    async queryTestOrderDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTestOrderDetail(payload)
      );
      commit('saveOrderDetail', response);
    },
    //查询需求标签
    async queryByTypes({ commit }, payload) {
      const response = await resolveResponseError(() => queryByTypes(payload));
      commit('saveDemandLabelList', response);
    },
    //列表展示定稿日期修改申请
    async queryRqrApproveList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryRqrApproveList(payload)
      );
      commit('saveRqrApprovalList', response);
    },
    //查看我的需求评估定稿审批列表
    async queryMyList({ commit }, payload) {
      const response = await resolveResponseError(() => queryMyList(payload));
      commit('saveMyApprovalList', response);
    },
    async queryCount({ commit }, payload) {
      const response = await resolveResponseError(() => queryCount(payload));
      commit('saveMyRqrApprovalCount', response);
    }
  },

  mutations: {
    saveFdevNoInfo(state, payload) {
      state.fdevNOinfo = payload;
    },
    saveAssessExcel(state, payload) {
      state.assessExcel = payload;
    },
    saveNewDemandData(state, payload) {
      state.newDemandData = payload;
    },
    saveDemandList(state, payload) {
      state.demandList = payload;
    },
    saveUpdateDemand(state, payload) {
      state.updateDemandData = payload;
    },
    saveDeleteDemand(state, payload) {
      state.deleteRqrData = payload;
    },
    saveFileDemand(state, payload) {
      state.fileRqrData = payload;
    },
    saveImplementUnitData(state, payload) {
      state.implementUnitData = payload;
    },
    saveDemandInfoDetail(state, payload) {
      state.demandInfoDetail = payload;
    },
    saveIpmpTasks(state, payload) {
      state.ipmpTasks = payload;
    },
    saveUnitNoList(state, payload) {
      state.UnitNoList = payload.data;
    },
    saveDeleteUnitById(state, payload) {
      state.deleteUnitByIdData = payload;
    },
    saveTaskByDemandId(state, payload) {
      state.taskList = payload;
    },
    saveTaskAndIpmpData(state, payload) {
      state.searchIpmpData = payload;
    },
    saveTaskByGroupIdData(state, payload) {
      state.taskByGroupIdData = payload;
    },
    saveUnitByIpmpTaskIdData(state, payload) {
      state.unitByIpmpTaskIdData = payload;
    },
    saveNewIpmpTaskData(state, payload) {
      state.newIpmpTaskData = payload;
    },
    saveNewUnitData(state, payload) {
      state.newUnitData = payload;
    },
    saveDemandDoc(state, payload) {
      state.demandDocList = payload.data;
      state.demandDocListData = payload;
    },
    saveJobProfile(state, payload) {
      state.jobProfile = payload;
    },
    saveImplUnitQueriedByGroupId(state, payload) {
      state.implUnitQueriedByGroupId = payload;
    },
    saveRelatedPartIds(state, payload) {
      state.relatedPartIds = payload;
    },
    saveIpmpUnit(state, payload) {
      state.ipmpUnitList = payload;
    },
    saveIpmpUnitNew(state, payload) {
      state.ipmpUnitListNew = payload;
    },
    saveDemandListExcel(state, payload) {
      state.demandListExcel = payload;
    },
    saveModelExcel(state, payload) {
      state.modelExcel = payload;
    },
    saveEvaluateExcel(state, payload) {
      state.evaluateExcel = payload;
    },
    saveRelatePartDetail(state, payload) {
      state.relatePartDetail = payload;
    },
    saveOaNOStatus(state, payload) {
      state.oaNOStatus = payload;
    },
    saveQueryPartInfo(state, payload) {
      state.theQueryPartInfo = payload;
    },
    saveIpmpUnitListTable(state, payload) {
      state.ipmpUnitListTable = payload;
    },
    saveIpmpUser(state, payload) {
      state.ipmpUser = payload;
    },
    saveIpmpProject(state, payload) {
      state.ipmpProject = payload;
    },
    saveIpmpLeadTeam(state, payload) {
      state.ipmpLeadTeam = payload;
    },
    saveIpmpUnitNoList(state, payload) {
      state.ipmpUnitNoList = payload;
    },
    saveUpdateIpmpUnit(state, payload) {
      state.updateIpmpUnits = payload;
    },
    saveAssessButFlag(state, payload) {
      state.assessButFlag = payload;
    },
    saveApprovelist(state, payload) {
      state.approveList = payload;
    },
    saveMyApprovelist(state, payload) {
      state.myApproveList = payload;
    },
    saveDemandAssessDate(state, payload) {
      state.demandAssessDate = payload;
    },
    saveOrderDetail(state, payload) {
      state.testOrderDetails = payload;
    },
    saveDemandLabelList(state, payload) {
      state.demandLabelList = payload;
    },
    saveRqrApprovalList(state, payload) {
      state.finalApprovalList = payload;
    },
    saveMyApprovalList(state, payload) {
      state.myApprovalList = payload;
    },
    saveMyRqrApprovalCount(state, payload) {
      state.myRqrProvalCount = payload;
    }
  }
};
