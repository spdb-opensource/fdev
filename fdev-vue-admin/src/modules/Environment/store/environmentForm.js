import {
  saveSitConfigProperties,
  queryModel,
  queryModelTemp,
  addModel,
  addModelTemp,
  updateModel,
  queryEnvList,
  queryEnvListByAppId,
  queryModelEvn,
  deleteAModelEvn,
  deleteModel,
  deleteEnvList,
  updateEnvList,
  addEnvList,
  queryModleEnvDetail,
  confDependList,
  queryModelConst,
  confDependency,
  getEnvWithModel,
  copyEnvWithModel,
  queryVarByEnvAndType,
  queryAutoEnv,
  saveModelEnv,
  queryModelMessage,
  compare,
  finish,
  downloadAppInfo,
  updateModelMessage,
  cancelModelMessage,
  queryModelSetList,
  deleteModelSet,
  getType,
  getModles,
  saveModel,
  updateModels,
  queryAppProInfo,
  getJsonSchema,
  bindAppInfo,
  queryDeploy,
  queryAllLabels,
  queryEnvKey,
  queryDeployDetail,
  queryRealTimeBindMsg,
  getVerifyCode,
  queryByLabelsFuzzy,
  queryProEnvByAppId,
  pageQuery,
  queryModelEnvByValue,
  checkConnectionDocker,
  queryPage,
  queryTempPage,
  queryTemplateContainsModel,
  getMappingHistoryList,
  getMappingHistoryDetail,
  queryEnvKeyList,
  queryConfigModel,
  previewConfigFile,
  batchPreviewConfigFile,
  queryDeployments,
  queryModelEnvByModelNameEn,
  proInfo,
  queryClusters,
  queryCaasInfo,
  querySCCInfo
} from '@/services/envconfig';
import { resolveResponseError } from '@/utils/utils';

export default {
  namespaced: true,

  state: {
    productionInfoList: {},
    verifyCode: '',
    modelList: [],
    modelTempList: {},
    envList: [],
    envListByAppId: [],
    modelEvnList: [],
    modelEnvDetail: {},
    confList: [],
    modelConstObj: {},
    EnvModelList: {},
    exportData: '',
    envModelVar: [],
    autoEnvList: [],
    modelMessageList: [],
    compareData: {},
    appInfo: {},
    modelSetList: [],
    modelType: [],
    models: [],
    appProInfo: [],
    jsonSchema: '',
    bindAppInfoObj: {},
    deployDetail: {},
    envLables: [],
    envKey: [],
    realTimeBindMsg: {},
    envFilterByLabels: [],
    selectAppEnv: [],
    modelEnvMap: [],
    modelEnvByValueList: [],
    connectionInfo: '',
    modelListPaging: [],
    modelTempListPaging: [],
    templateModelList: [],
    HistoryMsgList: {},
    HistoryMsgDetail: [],
    envKeyList: [],
    configModelList: [],
    resPreviewConfigFile: '',
    batchPreviewConfigFile: {},
    modelEnvList: [],
    proInfo: {},
    platformClusters: {},
    caasDeploymentDetail: {},
    sccDeploymentDetail: {}
  },

  getters: {
    envLablesOption: state => {
      const { envLables } = state;
      return initLabels(envLables);
    },
    envKeyOption: state => {
      const { envKey } = state;
      return initLabels(envKey);
    },
    separateOption: state => {
      const { envLables } = state;
      return separateLabels(envLables);
    }
  },

  actions: {
    //查询Caas应用生产信息详情
    async queryCaasInfo({ commit }, payload) {
      const response = await resolveResponseError(() => queryCaasInfo(payload));
      commit('saveCaasInfo', response);
    },
    //查询scc应用生产信息详情
    async querySCCInfo({ commit }, payload) {
      const response = await resolveResponseError(() => querySCCInfo(payload));
      commit('saveSCCInfo', response);
    },
    async queryClusters({ commit }, payload) {
      const response = await resolveResponseError(() => queryClusters(payload));
      commit('saveClusters', response);
    },
    //查询集群应用信息
    async queryProInfo({ commit }, payload) {
      const response = await resolveResponseError(() => proInfo(payload));
      commit('saveProInfo', response);
    },
    //查询应用生产信息列表
    async queryDeployments({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryDeployments(payload)
      );
      commit('saveQueryDeployments', response);
    },
    // 获取验证码
    async getVerifyCode({ commit }, payload) {
      const response = await resolveResponseError(() => getVerifyCode(payload));
      commit('saveVerifyCode', response);
    },
    // 查询实体
    async getModelList({ commit }, payload) {
      const response = await resolveResponseError(() => queryModel(payload));
      commit('saveModelList', response);
    },
    // 查询实体模板详情
    async getModelTempDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryModelTemp(payload)
      );
      commit('getModelDetal', response);
    },
    // 删除实体
    async deleteModel({ commit }, payload) {
      await resolveResponseError(() => deleteModel(payload));
    },
    // 新增实体
    async addModel({ commit }, payload) {
      await resolveResponseError(() => addModel(payload));
    },
    // 新增实体模板
    async addModelTemp({ commit }, payload) {
      await resolveResponseError(() => addModelTemp(payload));
    },
    // 更新实体
    async updateModel({ commit }, payload) {
      await resolveResponseError(() => updateModel(payload));
    },
    // 查询、获取环境列表
    async getEnvList({ commit }, payload) {
      const response = await resolveResponseError(() => queryEnvList(payload));
      commit('saveEnvList', response);
    },
    // 根据应用查询环境列表
    async getEnvListByAppId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryEnvListByAppId(payload)
      );
      commit('saveEnvListByAppId', response);
    },
    // 删除环境
    async deleteEnv({ commit }, payload) {
      await resolveResponseError(() => deleteEnvList(payload));
    },
    //更新环境列表
    async updateEnvList({ commit }, payload) {
      await resolveResponseError(() => updateEnvList(payload));
    },
    // 配置模板
    async saveSitConfigProperties({ commit }, payload) {
      await resolveResponseError(() => saveSitConfigProperties(payload));
    },
    //增加环境
    async addEnvList({ commit }, payload) {
      await resolveResponseError(() => addEnvList(payload));
    },
    //查询实体环境列表
    async getModelEvn({ commit }, payload) {
      const response = await resolveResponseError(() => queryModelEvn(payload));
      commit('saveModelEvn', response);
    },
    //删除实体环境
    async deleteModelEvn({ commit }, payload) {
      await resolveResponseError(() => deleteAModelEvn(payload));
    },
    // 根据 环境id和实体id 查询实体属性对应的值
    async queryModleEnvDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryModleEnvDetail(payload)
      );
      commit('saveModelEnvDetail', response[0]);
    },
    //查询配置依赖
    async confDependList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        confDependList(payload)
      );
      commit('saveConfList', response);
    },
    //查询分类表常量信息
    async queryModelConst({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryModelConst(payload)
      );
      commit('saveConstList', response);
    },
    //导出配置依赖
    async confDependency({ commit }, payload) {
      const response = await resolveResponseError(() =>
        confDependency(payload)
      );
      commit('saveExportData', response);
    },
    //获取实体与环境映射
    async getEnvWithModel({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getEnvWithModel(payload)
      );
      commit('saveEnvWithModel', response);
    },
    //复制环境及它的一套实体
    async copyEnvWithModel({ commit }, payload) {
      await resolveResponseError(() => copyEnvWithModel(payload));
    },
    // 根据环境和实体类型查询实体与环境映射信息
    async queryVarByEnvAndType({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryVarByEnvAndType(payload)
      );
      commit('saveVarByEnvAndType', response);
    },
    async queryAutoEnv({ commit }, payload) {
      const response = await resolveResponseError(() => queryAutoEnv(payload));
      commit('saveAutoEnv', response);
    },
    //更新、新增实体环境
    async saveModelEnv({ commit }, payload) {
      await resolveResponseError(() => saveModelEnv(payload));
    },
    async queryModelMessage({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryModelMessage(payload)
      );
      commit('saveModelMessage', response);
    },
    async compare({ commit }, payload) {
      const response = await resolveResponseError(() => compare(payload));
      commit('saveCompareData', response);
    },
    async finish({ commit }, payload) {
      await resolveResponseError(() => finish(payload));
    },
    async updateModelMessage({ commit }, payload) {
      await resolveResponseError(() => updateModelMessage(payload));
    },
    async cancelModelMessage({ commit }, payload) {
      await resolveResponseError(() => cancelModelMessage(payload));
    },
    async downloadAppInfo({ commit }, payload) {
      const response = await resolveResponseError(() =>
        downloadAppInfo(payload)
      );
      commit('saveAppInfo', response);
    },
    async queryModelSetList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryModelSetList(payload)
      );
      commit('saveModelSetList', response);
    },
    async deleteModelSet({ commit }, payload) {
      await resolveResponseError(() => deleteModelSet(payload));
    },
    async getType({ commit }, payload) {
      const type = await resolveResponseError(() => getType(payload));
      commit('saveType', type);
    },
    async getModles({ commit }, payload) {
      const modles = await resolveResponseError(() => getModles(payload));
      commit('saveModels', modles);
    },
    async saveModel({ commit }, payload) {
      await resolveResponseError(() => saveModel(payload));
    },
    async updateModels({ commit }, payload) {
      await resolveResponseError(() => updateModels(payload));
    },
    async queryAppProInfo({ commit }, payload) {
      const type = await resolveResponseError(() => queryAppProInfo(payload));
      commit('saveAppProInfo', type);
    },
    async getJsonSchema({ commit }, payload) {
      const response = await resolveResponseError(() => getJsonSchema(payload));
      commit('saveJsonSchema', response);
    },
    async bindAppInfo({ commit }, payload) {
      await resolveResponseError(() => bindAppInfo(payload));
    },
    async queryDeploy({ commit }, payload) {
      const response = await resolveResponseError(() => queryDeploy(payload));
      if (!response.modelSetMsg.id) {
        response.modelSetMsg = null;
      }
      // if (!response.sccModelSetMsg.id) {
      //   response.sccModelSetMsg = null;
      // }
      commit('saveDeployDetail', response);
    },
    async queryAllLabels({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAllLabels(payload)
      );
      commit('saveEnvLabels', response);
    },
    async queryEnvKey({ commit }, payload) {
      const response = await resolveResponseError(() => queryEnvKey(payload));
      commit('saveEnvKey', response);
    },
    async queryDeployDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryDeployDetail(payload)
      );
      if (!response.modelSetMsg.id) {
        response.modelSetMsg = null;
      }
      if (!response.sccModelSetMsg.id) {
        response.sccModelSetMsg = null;
      }
      commit('saveDeployDetail', response);
    },
    async queryRealTimeBindMsg({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryRealTimeBindMsg(payload)
      );
      if (!response.modelSetMsg.id) {
        response.modelSetMsg = null;
      }
      commit('saveRealTimeBindMsg', response);
    },
    async queryByLabelsFuzzy({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryByLabelsFuzzy(payload)
      );
      commit('saveByLabelsFuzzy', response);
    },
    async queryProEnvByAppId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryProEnvByAppId(payload)
      );
      commit('saveSelectEnv', response);
    },
    async pageQuery({ commit }, payload) {
      const response = await resolveResponseError(() => pageQuery(payload));
      commit('saveModelEnvMap', response);
    },
    async queryModelEnvByValue({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryModelEnvByValue(payload)
      );
      commit('saveModelEnvByValueList', response);
    },
    async checkConnectionDocker({ commit }, payload) {
      const res = await resolveResponseError(() =>
        checkConnectionDocker(payload)
      );
      commit('saveConnect', res);
    },
    //查询实体列表--后端分页
    async getModelListPaging({ commit }, payload) {
      const response = await resolveResponseError(() => queryPage(payload));
      commit('saveModelListPaging', response);
    },
    // 查询实体模板列表分页
    async getModelTempListPaging({ commit }, payload) {
      const response = await resolveResponseError(() => queryTempPage(payload));
      commit('saveModelTempListPaging', response);
    },
    //获取实体组模板包含的实体
    async queryTemplateContainsModel({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTemplateContainsModel(payload)
      );
      commit('saveTemplateModel', response);
    },
    //获取实体与环境映射历史修改数据
    async getMappingHistoryList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getMappingHistoryList(payload)
      );
      commit('saveHistoryList', response);
    },
    //获取实体与环境映射修改详情
    async getMappingHistoryDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getMappingHistoryDetail(payload)
      );
      commit('saveHistoryDetail', response);
    },
    async queryEnvKeyList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryEnvKeyList(payload)
      );
      commit('saveEnvKeyList', response);
    },
    async queryConfigModel({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryConfigModel(payload)
      );
      commit('saveConfigModelList', response);
    },
    async getResPreviewConfigFile({ commit }, payload) {
      const response = await resolveResponseError(() =>
        previewConfigFile(payload)
      );
      commit('saveResPreviewConfigFile', response);
    },
    async getBatchPreviewConfigFile({ commit }, payload) {
      const response = await resolveResponseError(() =>
        batchPreviewConfigFile(payload)
      );
      commit('saveBatchPreviewConfigFile', response);
    },
    //根据实体英文名或环境英文名查询实体环境映射值
    async queryModelEnvByModelNameEn({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryModelEnvByModelNameEn(payload)
      );
      commit('saveModelEnvList', response);
    }
  },

  mutations: {
    saveCaasInfo(state, payload) {
      state.caasDeploymentDetail = payload;
    },
    saveSCCInfo(state, payload) {
      state.sccDeploymentDetail = payload;
    },
    saveClusters(state, payload) {
      state.platformClusters = payload;
    },
    saveProInfo(state, payload) {
      state.proInfo = payload;
    },
    saveQueryDeployments(state, payload) {
      state.productionInfoList = payload;
    },
    saveVerifyCode(state, payload) {
      state.verifyCode = payload;
    },
    saveModelList(state, payload) {
      state.modelList = payload;
    },
    getModelDetal(state, payload) {
      state.modelTempList = payload;
    },
    saveEnvList(state, payload) {
      state.envList = payload;
    },
    saveEnvListByAppId(state, payload) {
      state.envListByAppId = payload;
    },
    saveModelEvn(state, payload) {
      state.modelEvnList = payload;
    },
    saveModelEnvDetail(state, payload) {
      state.modelEnvDetail = payload;
    },
    saveConfList(state, payload) {
      state.confList = payload;
    },
    saveConstList(state, payload) {
      state.modelConstObj = payload;
    },
    saveEnvWithModel(state, payload) {
      state.EnvModelList = payload;
    },
    saveExportData(state, payload) {
      state.exportData = payload;
    },
    saveVarByEnvAndType(state, payload) {
      state.envModelVar = payload;
    },
    saveAutoEnv(state, payload) {
      state.autoEnvList = payload;
    },
    saveModelMessage(state, payload) {
      state.modelMessageList = payload;
    },
    saveCompareData(state, payload) {
      state.compareData = payload;
    },
    saveAppInfo(state, payload) {
      state.appInfo = payload;
    },
    saveModelSetList(state, payload) {
      state.modelSetList = payload;
    },
    saveType(state, payload) {
      state.modelType = payload;
    },
    saveModels(state, payload) {
      state.models = payload;
    },
    saveAppProInfo(state, payload) {
      state.appProInfo = payload;
    },
    saveJsonSchema(state, payload) {
      state.jsonSchema = payload;
    },
    saveBindAppInfo(state, payload) {
      state.bindAppInfoObj = payload;
    },
    saveDeployDetail(state, payload) {
      state.deployDetail = payload;
    },
    saveEnvLabels(state, payload) {
      state.envLables = payload;
    },
    saveEnvKey(state, payload) {
      state.envKey = payload;
    },
    saveRealTimeBindMsg(state, payload) {
      state.realTimeBindMsg = payload;
    },
    saveByLabelsFuzzy(state, payload) {
      state.envFilterByLabels = payload;
    },
    saveSelectEnv(state, payload) {
      state.selectAppEnv = payload;
    },
    saveModelEnvMap(state, payload) {
      state.modelEnvMap = payload;
    },
    saveModelEnvByValueList(state, payload) {
      state.modelEnvByValueList = payload;
    },
    saveConnect(state, payload) {
      state.connectionInfo = payload;
    },
    saveModelListPaging(state, payload) {
      state.modelListPaging = payload;
    },
    saveModelTempListPaging(state, payload) {
      state.modelTempListPaging = payload;
    },
    saveTemplateModel(state, payload) {
      state.templateModelList = payload;
    },
    saveHistoryList(state, payload) {
      state.HistoryMsgList = payload;
    },
    saveHistoryDetail(state, payload) {
      state.HistoryMsgDetail = payload;
    },
    saveEnvKeyList(state, payload) {
      state.envKeyList = payload;
    },
    saveConfigModelList(state, payload) {
      state.configModelList = payload;
    },
    saveResPreviewConfigFile(state, payload) {
      state.resPreviewConfigFile = payload;
    },
    saveBatchPreviewConfigFile(state, payload) {
      state.batchPreviewConfigFile = payload;
    },
    saveModelEnvList(state, payload) {
      state.modelEnvList = payload;
    }
  }
};
function initLabels(envLables) {
  let labelList = [];
  Object.keys(envLables).forEach(key => {
    envLables[key].forEach(item => {
      labelList.push({
        value: item,
        label: item
      });
    });
  });
  return labelList;
}
function separateLabels(envLables) {
  return Object.keys(envLables).map(key => {
    return envLables[key].map(item => item);
  });
}
