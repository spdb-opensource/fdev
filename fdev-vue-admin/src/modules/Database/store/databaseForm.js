import {
  queryName,
  queryDbType,
  queryAppName,
  add,
  update,
  deleteDatabase,
  queryInfo,
  queryDbName,
  download,
  upload,
  confirmAll,
  queryManager,
  databaseQueryDetail,
  querySystemNames,
  queryUseRecordTable,
  queryDatabaseNameById,
  queryDictRecord,
  updateDictRecord,
  updateUseRecord,
  addDictRecord,
  impDictRecords,
  queryFieldType,
  queryDictRecordAll,
  downloadTemplate,
  queryAppByUser,
  addUseRecord,
  queryUseRecord
} from '@/services/database';
import { resolveResponseError, exportExcel } from '@/utils/utils';

export default {
  namespaced: true,

  state: {
    personAppName: [],
    downTemplateFiles: {},
    fieldType: [],
    dictionaryListAll: {},
    dictionaryList: {},
    useRecord: {},
    databaseNameById: [],
    systemNames: [],
    useRecordTable: [],
    databaseQueryDetailInfo: [],
    appManagerList: [],
    databaseList: {},
    databaseType: [],
    appName: [],
    databaseName: [],
    tableName: []
  },

  actions: {
    async queryAppByUser({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAppByUser(payload)
      );
      commit('saveQueryAppByUser', response);
    },
    async downloadTemplate({ commit }, payload) {
      const response = await resolveResponseError(() =>
        downloadTemplate(payload)
      );
      commit('saveDownloadTemplate', response);
    },
    async queryFieldType({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryFieldType(payload)
      );
      commit('saveQueryFieldType', response);
    },
    async impDictRecords({ commit }, payload) {
      await resolveResponseError(() => impDictRecords(payload));
    },
    async addDictRecord({ commit }, payload) {
      await resolveResponseError(() => addDictRecord(payload));
    },
    async updateDictRecord({ commit }, payload) {
      await resolveResponseError(() => updateDictRecord(payload));
    },
    // 数据字典登记表信息修改
    async updateUseRecord({ commit }, payload) {
      await resolveResponseError(() => updateUseRecord(payload));
    },
    async queryUseRecord({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryUseRecord(payload)
      );
      commit('saveUseRecord', response);
    },
    async queryDictRecord({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryDictRecord(payload)
      );
      commit('saveDictionaryList', response);
    },
    async queryDictRecordAll({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryDictRecordAll(payload)
      );
      commit('saveDictionaryListAll', response);
    },
    async queryDatabaseNameById({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryDatabaseNameById(payload)
      );
      commit('saveDatabaseNameById', response);
    },
    async querySystemNames({ commit }, payload) {
      const response = await resolveResponseError(() =>
        querySystemNames(payload)
      );
      commit('saveSystemNames', response);
    },
    async queryUseRecordTable({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryUseRecordTable(payload)
      );
      commit('saveUseRecordTable', response);
    },
    async databaseQueryDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        databaseQueryDetail(payload)
      );
      commit('saveDatabaseQueryDetail', response);
    },
    async queryManager({ commit }, payload) {
      const response = await resolveResponseError(() => queryManager(payload));
      commit('saveQueryManager', response);
    },
    async query({ commit }, payload) {
      const response = await resolveResponseError(() => queryInfo(payload));
      commit('saveDatabaseList', response);
    },
    async queryDbType({ commit }, payload) {
      const response = await resolveResponseError(() => queryDbType(payload));
      commit('saveDatabaseType', response);
    },
    async queryAppName({ commit }, payload) {
      const response = await resolveResponseError(() => queryAppName(payload));
      commit('saveAppName', response);
    },
    async queryDbName({ commit }, payload) {
      const response = await resolveResponseError(() => queryDbName(payload));
      commit('saveDatabaseName', response);
    },
    async queryName({ commit }, payload) {
      const response = await resolveResponseError(() => queryName(payload));
      commit('saveTableName', response);
    },
    async download({ commit }, payload) {
      const response = await resolveResponseError(() => download(payload));
      exportExcel(response, '应用与库表.xlsx');
    },
    async add({ commit }, payload) {
      await resolveResponseError(() => add(payload));
    },
    async update({ commit }, payload) {
      await resolveResponseError(() => update(payload));
    },
    async deleteDatabase({ commit }, payload) {
      await resolveResponseError(() => deleteDatabase(payload));
    },
    async upload({ commit }, payload) {
      await resolveResponseError(() => upload(payload));
    },
    async confirmAll({ commit }, payload) {
      await resolveResponseError(() => confirmAll(payload));
    },
    async addUseRecord({ commit }, payload) {
      await resolveResponseError(() => addUseRecord(payload));
    }
  },
  mutations: {
    saveQueryAppByUser(state, payload) {
      state.personAppName = payload;
    },
    saveDownloadTemplate(state, payload) {
      state.downTemplateFiles = payload;
    },
    saveQueryFieldType(state, payload) {
      state.fieldType = payload;
    },
    saveDictionaryListAll(state, payload) {
      state.dictionaryListAll = payload;
    },
    saveDictionaryList(state, payload) {
      state.dictionaryList = payload;
    },
    saveUseRecord(state, payload) {
      state.useRecord = payload;
    },
    saveDatabaseNameById(state, payload) {
      state.databaseNameById = payload;
    },
    saveSystemNames(state, payload) {
      state.systemNames = payload;
    },
    saveUseRecordTable(state, payload) {
      state.useRecordTable = payload;
    },
    saveDatabaseQueryDetail(state, payload) {
      state.databaseQueryDetailInfo = payload;
    },
    saveQueryManager(state, payload) {
      state.appManagerList = payload;
    },
    saveDatabaseList(state, payload) {
      state.databaseList = payload;
    },
    saveDatabaseType(state, payload) {
      state.databaseType = payload;
    },
    saveDatabaseName(state, payload) {
      state.databaseName = payload;
    },
    saveTableName(state, payload) {
      state.tableName = payload;
    },
    saveAppName(state, payload) {
      state.appName = payload;
    }
  }
};
