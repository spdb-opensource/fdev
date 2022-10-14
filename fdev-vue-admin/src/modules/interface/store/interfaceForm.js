import {
  scanInterface,
  getInterfacesUrl,
  transTags,
  getProjectBranchList,
  queryTransList,
  exportTransList,
  queryTransDetailById,
  queryTransVersions,
  queryTransByVersion,
  queryInterfaceList,
  queryInterfacesList,
  queryInterfaceRelation,
  queryTagsRole,
  getInterfaceDetailById,
  queryInterfaceDetailById,
  queryInterfaceVersions,
  taskScanInterface,
  isTaskManager,
  isAppManager,
  queryScanRecord,
  queryTransRelation,
  downloadRestRelationExcel,
  updateParamDescription,
  modifyParamDescription,
  queryApplicationList,
  updateApplicationStatus,
  interfaceCallRequest,
  isManagers,
  queryIsNoApplyInterface,
  queryInterfaceStatistics,
  queryRoutes,
  queryRoutesRelation,
  queryRoutesDetail,
  getServiceChainInfo,
  queryAppJsonList,
  queryTotalJsonList,
  queryTotalJsonHistory,
  queryRoutesDetailVer,
  queryYapiList,
  importYapiProject,
  convertJsonSchema,
  queryYapiDetail,
  deleteYapiProject,
  deleteYapiInterface,
  importYapiInterface
} from '@/services/interface';
import { queryPagination } from '@/services/app';
import { resolveResponseError, exportExcel } from '@/utils/utils';

export default {
  namespaced: true,
  state: {
    url: '',
    gitlabBranch: [],
    transList: [],
    transDetail: {},
    versions: [],
    interfaceList: [],
    foreignInterfaceList: [],
    interfaceRelation: [],
    scanRes: {},
    tagRole: false,
    interfaceDetail: {},
    interfaceModel: {},
    interfaceVersions: [],
    taskManagerLimit: {},
    appManagerLimit: {},
    scanRecordList: [],
    invokeRelationshipsList: [],
    exportData: '',
    applicationList: [],
    limit: Boolean,
    flag: false,
    transId: [],
    provideManagers: {},
    interfaceStatistics: [],
    statisticsRowsNumber: '',
    serviceName: [],
    routesList: [],
    routesRelationList: [],
    routerDetail: {},
    serviceLink: {},
    appDatList: [],
    appTotalDatList: [],
    envHistoryList: [],
    routesDetailVer: [],
    yapiList: [],
    jsonSchema: '',
    yapiDetail: {
      import_user: {}
    },
    importMessage: ''
  },
  actions: {
    /* 应用模块的扫描 */
    async scanInterface({ commit }, payload) {
      const response = await resolveResponseError(() => scanInterface(payload));
      commit('saveScanRes', response);
    },
    /* 任务模块的扫描 */
    async taskScanInterface({ commit }, payload) {
      const response = await resolveResponseError(() =>
        taskScanInterface(payload)
      );
      commit('saveScanRes', response);
    },
    async getInterfacesUrl({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getInterfacesUrl(payload)
      );
      commit('saveUrl', response);
    },
    async transTags({ commit }, payload) {
      await resolveResponseError(() => transTags(payload));
    },
    async getProjectBranchList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getProjectBranchList(payload)
      );
      commit('savegitlabBranch', response);
    },
    async queryTransList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTransList(payload)
      );
      commit('saveTransList', response);
    },
    async exportTransList({ commit }, data) {
      const response = await resolveResponseError(() => exportTransList(data));
      let xlsName = '';
      if (data.branchDefault === 'other') {
        xlsName = '其他分支交易列表.xls';
      } else {
        xlsName = `${data.branchDefault}交易列表.xls`;
      }
      exportExcel(response, xlsName);
    },
    async queryTransDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTransDetailById(payload)
      );
      commit('saveTransDetail', response);
    },
    async queryTransByVersion({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTransByVersion(payload)
      );
      commit('saveTransDetail', response);
    },
    // 通过交易id查询交易版本
    async queryTransVersions({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTransVersions(payload)
      );
      commit('saveTransVersions', response);
    },
    async queryInterfaceList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryInterfaceList(payload)
      );
      commit('saveInterfaceList', response);
    },
    async queryInterfacesList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryInterfacesList(payload)
      );
      commit('saveForeignInterfaceList', response);
    },
    async queryInterfaceRelation({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryInterfaceRelation(payload)
      );
      commit('saveInterfaceRelation', response);
    },
    async queryTagsRole({ commit }, payload) {
      const response = await resolveResponseError(() => queryTagsRole(payload));
      commit('saveTagRole', response.limit);
    },
    // 对外--接口列表、关系详情
    async getInterfaceDetailById({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getInterfaceDetailById(payload)
      );
      commit('saveInterfaceDetail', response);
    },
    // 接口列表、关系详情
    async queryInterfaceDetailById({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryInterfaceDetailById(payload)
      );
      commit('saveInterfaceModel', response);
    },
    // 接口--版本查询
    async queryInterfaceVersions({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryInterfaceVersions(payload)
      );
      commit('saveInterfaceVersions', response);
    },
    // 接口--判断用户是否为任务负责人、行内项目负责人、开发人员
    async isTaskManager({ commit }, payload) {
      const response = await resolveResponseError(() => isTaskManager(payload));
      commit('saveTaskManagerLimit', response);
    },
    // 接口--判断用户是否为任务负责人、行内项目负责人、开发人员
    async isAppManager({ commit }, payload) {
      const response = await resolveResponseError(() => isAppManager(payload));
      commit('saveAppManagerLimit', response);
    },
    /* 应用接口扫描详情 */
    async queryScanRecord({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryScanRecord(payload)
      );
      commit('saveScanRecord', response);
    },
    /* 应用接口扫描详情 */
    async queryTransRelation({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTransRelation(payload)
      );
      commit('saveInvokeRelationships', response);
    },
    //下载调用关系
    async downloadRestRelationExcel({ commit }, payload) {
      const response = await resolveResponseError(() =>
        downloadRestRelationExcel(payload)
      );
      commit('saveExportData', response);
    },
    /* 更新接口详情 */
    async updateParamDescription({ commit }, payload) {
      await resolveResponseError(() => updateParamDescription(payload));
    },
    /* 更新交易详情 */
    async modifyParamDescription({ commit }, payload) {
      await resolveResponseError(() => modifyParamDescription(payload));
    },
    async queryApplicationList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryApplicationList(payload)
      );
      commit('saveApplicationList', response);
    },
    async updateApplicationStatus({ commit }, payload) {
      await resolveResponseError(() => updateApplicationStatus(payload));
    },
    async isManagers({ commit }, payload) {
      const response = await resolveResponseError(() => isManagers(payload));
      commit('saveAuth', response);
    },
    async interfaceCallRequest({ commit }, payload) {
      const response = await resolveResponseError(() =>
        interfaceCallRequest(payload)
      );
      commit('saveProvideManagers', response);
    },
    async queryIsNoApplyInterface({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryIsNoApplyInterface(payload)
      );
      commit('saveIsNoApplyInterface', response);
      commit('saveApplyInterfaceList', response);
    },
    //接口统计查询
    async queryInterfaceStatistics({ commit }, payload) {
      const { list, total } = await queryInterfaceStatistics(payload);
      //查询结果和总条数
      commit('saveInterfaceStatistics', list);
      commit('saveStatisticsRowsNumber', total);
    },
    async queryServiceName({ commit }, payload) {
      const { appEntity } = await queryPagination(payload);
      const arr = appEntity.map(e => e.name_en);
      commit('saveServiceName', arr);
    },
    async queryRoutes({ commit }, payload) {
      const response = await resolveResponseError(() => queryRoutes(payload));
      commit('saveRoutes', response);
    },
    async queryRoutesRelation({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryRoutesRelation(payload)
      );
      commit('saveRoutesRelation', response);
    },
    async queryRoutesDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryRoutesDetail(payload)
      );
      commit('saveRoutesDetail', response);
    },
    async getServiceChainInfo({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getServiceChainInfo(payload)
      );
      commit('saveServiceLink', response);
    },
    async queryAppJsonList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAppJsonList(payload)
      );
      commit('saveAppDatList', response);
    },
    async queryTotalJsonList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTotalJsonList(payload)
      );
      commit('saveTotalDatList', response);
    },
    async queryTotalJsonHistory({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTotalJsonHistory(payload)
      );
      commit('saveTotalDatHistory', response);
    },
    async queryRoutesDetailVer({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryRoutesDetailVer(payload)
      );
      commit('saveRoutesDetailVer', response);
    },
    async queryYapiList({ commit }, payload) {
      const response = await resolveResponseError(() => queryYapiList(payload));
      commit('saveYapiList', response);
    },
    async convertJsonSchema({ commit }, payload) {
      const response = await resolveResponseError(() =>
        convertJsonSchema(payload)
      );
      commit('saveJsonSchema', response);
    },
    async queryYapiDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryYapiDetail(payload)
      );
      commit('saveYapiDetail', response);
    },
    async importYapiProject({ commit }, payload) {
      const response = await resolveResponseError(() =>
        importYapiProject(payload)
      );
      commit('saveImportMessage', response);
    },
    async importYapiInterface({ commit }, payload) {
      await resolveResponseError(() => importYapiInterface(payload));
    },
    async deleteYapiProject({ commit }, payload) {
      await resolveResponseError(() => deleteYapiProject(payload));
    },
    async deleteYapiInterface({ commit }, payload) {
      await resolveResponseError(() => deleteYapiInterface(payload));
    }
  },
  mutations: {
    saveUrl(state, payload) {
      state.url = payload;
    },
    savegitlabBranch(state, payload) {
      state.gitlabBranch = payload;
    },
    saveTransList(state, payload) {
      state.transList = payload;
    },
    saveTransDetail(state, payload) {
      state.transDetail = payload;
    },
    saveTransVersions(state, payload) {
      state.versions = payload;
    },
    saveInterfaceList(state, payload) {
      state.interfaceList = payload;
    },
    saveForeignInterfaceList(state, payload) {
      state.foreignInterfaceList = payload.list;
    },
    saveInterfaceRelation(state, payload) {
      state.interfaceRelation = payload;
    },
    saveScanRes(state, payload) {
      state.scanRes = payload;
    },
    saveTagRole(state, payload) {
      state.tagRole = payload;
    },
    saveInterfaceDetail(state, payload) {
      state.interfaceDetail = payload;
    },
    saveInterfaceModel(state, payload) {
      state.interfaceModel = payload;
    },
    saveInterfaceVersions(state, payload) {
      state.interfaceVersions = payload;
    },
    saveTaskManagerLimit(state, payload) {
      state.taskManagerLimit = payload;
    },
    saveAppManagerLimit(state, payload) {
      state.appManagerLimit = payload;
    },
    saveScanRecord(state, payload) {
      state.scanRecordList = payload;
    },
    saveInvokeRelationships(state, payload) {
      state.invokeRelationshipsList = payload;
    },
    saveExportData(state, payload) {
      state.exportData = payload;
    },
    saveParamDescriptions(state, payload) {
      state.paramDescriptions = payload;
    },
    saveApplicationList(state, payload) {
      state.applicationList = payload;
    },
    saveAuth(state, payload) {
      state.limit = payload.limit;
    },
    saveIsNoApplyInterface(state, payload) {
      state.flag = payload.flag;
    },
    saveApplyInterfaceList(state, payload) {
      state.transId = payload.transId;
    },
    saveProvideManagers(state, payload) {
      state.provideManagers = payload;
    },
    saveInterfaceStatistics(state, payload) {
      state.interfaceStatistics = payload;
    },
    saveStatisticsRowsNumber(state, payload) {
      state.statisticsRowsNumber = payload;
    },
    saveServiceName(state, payload) {
      state.serviceName = payload;
    },
    saveRoutes(state, payload) {
      state.routesList = payload;
    },
    saveRoutesRelation(state, payload) {
      state.routesRelationList = payload;
    },
    saveRoutesDetail(state, payload) {
      state.routerDetail = payload;
    },
    saveServiceLink(state, payload) {
      state.serviceLink = payload;
    },
    saveAppDatList(state, payload) {
      state.appDatList = payload;
    },
    saveTotalDatList(state, payload) {
      state.appTotalDatList = payload;
    },
    saveTotalDatHistory(state, payload) {
      state.envHistoryList = payload;
    },
    saveRoutesDetailVer(state, payload) {
      state.routesDetailVer = payload;
    },
    saveJsonSchema(state, payload) {
      state.jsonSchema = payload;
    },
    saveYapiDetail(state, payload) {
      state.yapiDetail = payload;
    },
    saveYapiList(state, payload) {
      state.yapiList = payload;
    },
    saveImportMessage(state, payload) {
      state.importMessage = payload;
    }
  }
};
