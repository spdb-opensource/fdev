import {
  queryPlugin,
  delPlugin,
  addPlugin,
  queryCollectionPipelineList,
  queryMinePipelineList,
  queryAllPipelineList,
  deletePipeline,
  updateFollowStatus,
  queryFdevciLogList,
  queryMinePipelineTemplateList,
  delTemplate,
  queryAppPipelineList,
  editPlugin,
  queryPluginHistory,
  queryLogDetailById,
  pipelineLogDetail,
  queryBranchesByPipelineId,
  queryCategory,
  queryPluginDetail,
  getYamlConfigById,
  addYamlConfig,
  querySectionEntity,
  queryTemplate,
  queryModelEnvByModelNameEn,
  queryModelTemp,
  queryMarkDown,
  stopPipeline,
  stopJob,
  copyPipelineTemplate,
  pipelineTemplateUpdate,
  saveAsPipelineTemplate,
  queryModelTemplateByContent,
  queryTypeAndContent,
  getMinioInfo,
  saveToMinio,
  retryPipeline,
  getAllJobs,
  getFullJobsByIds,
  getAllRunnerCluster,
  queryTriggerRules,
  updateTriggerRules,
  queryPipelineDetailById,
  copyPipeline,
  queryEnvList,
  queryEntityModelDetail,
  updateVisibleRange,
  pipelineTemplateAdd,
  jobsUpdate,
  delJobsTemplate,
  updateJobsVisibleRange,
  addJobsTemplate,
  getRunnerClusterInfoByParam,
  addRunnerCluster,
  getAllRunnerInfo,
  updateRunnerCluster,
  queryImageList,
  getPipelineHistoryVersion,
  setPipelineRollBack
} from '../services/method';
import { resolveResponseError } from '@/utils/utils';

export default {
  namespaced: true,

  state: {
    plugins: [],
    CollectionPipelineList: [],
    MinePipelineList: [],
    AllPipelineList: [],
    FdevciLogList: [],
    PipelineTemplateList: [],
    AppPipelineList: [],
    pluginReleaseLogList: [],
    deleteInfoList: [],
    logDetail: {},
    PipelineLogDetail: {},
    branchList: [],
    pluginCategoryInfo: [],
    pluginDetailInfo: {},
    modelList: [],
    modelTempListPaging: [],
    modelEnvList: [],
    modelTempList: {},
    markDownInfo: {},
    stopPipelineInfo: {},
    stopJobInfo: {},
    saveAsPipeTemp: '',
    envList: [],
    modeTemplate: '',
    fileInfo: {},
    jobList: [],
    jobListDetail: {},
    runners: [],
    triggerRules: {},
    pipelineDetail: {},
    copyPipelineId: {},
    entityModelDetail: {},
    yamlConfigByIdInfo: {},
    yamlConfigInfo: {},
    visibleRangeData: {},
    JobsTempAddData: {},
    runnerClusterInfo: [],
    allRunnerInfo: [],
    imageList: [],
    pipeHistoryVersion: [],
    currentVersionId: ''
  },

  getters: {},

  actions: {
    async queryLogDetailById({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryLogDetailById(payload)
      );
      commit('saveLogDetail', response);
    },
    async pipelineLogDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        pipelineLogDetail(payload)
      );
      commit('savePipelineLogDetail', response);
    },
    async queryBranchesByPipelineId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryBranchesByPipelineId(payload)
      );
      commit('saveBranchList', response);
    },
    async queryPlugin({ commit }, payload) {
      const response = await resolveResponseError(() => queryPlugin(payload));
      commit('savePlugins', response || []);
    },
    async delPlugin({ commit }, payload) {
      const response = await resolveResponseError(() => delPlugin(payload));
      commit('saveDeleteInfoList', response);
    },
    async addPlugin({ commit }, payload) {
      await resolveResponseError(() => addPlugin(payload));
    },
    async queryCollectionPipelineList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryCollectionPipelineList(payload)
      );
      commit('saveCollectionPipelineList', response);
    },
    async queryMinePipelineList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryMinePipelineList(payload)
      );
      commit('saveMinePipelineList', response);
    },
    async queryAllPipelineList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAllPipelineList(payload)
      );
      commit('saveAllPipelineList', response);
    },
    async deletePipeline({ commit }, payload) {
      await resolveResponseError(() => deletePipeline(payload));
    },
    async updateFollowStatus({ commit }, payload) {
      await resolveResponseError(() => updateFollowStatus(payload));
    },
    async queryFdevciLogList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryFdevciLogList(payload)
      );
      commit('saveFdevciLogList', response);
    },
    async queryMinePipelineTemplateList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryMinePipelineTemplateList(payload)
      );
      commit('savePipelineTemplateList', response);
    },
    async delTemplate({ commit }, payload) {
      await resolveResponseError(() => delTemplate(payload));
    },
    async queryAppPipelineList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAppPipelineList(payload)
      );
      commit('saveAppPipelineList', response);
    },
    async editPlugin({ commit }, payload) {
      await resolveResponseError(() => editPlugin(payload));
    },
    async queryPluginHistory({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryPluginHistory(payload)
      );
      commit('savePluginReleaseLogList', response);
    },
    async queryCategory({ commit }, payload) {
      const response = await resolveResponseError(() => queryCategory(payload));
      commit('savePluginCategoryInfo', response);
    },
    async queryPluginDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryPluginDetail(payload)
      );
      commit('savePluginDetailInfo', response);
    },
    //插件fileEdit类型的参数yaml按id查询
    async getYamlConfigById({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getYamlConfigById(payload)
      );
      commit('saveYamlConfigByIdInfo', response);
    },
    async addYamlConfig({ commit }, payload) {
      const response = await resolveResponseError(() => addYamlConfig(payload));
      commit('saveYamlConfigInfo', response);
    },
    async querySectionEntity({ commit }, payload) {
      const response = await resolveResponseError(() =>
        querySectionEntity(payload)
      );
      commit('saveEntityList', response);
    },
    // 查询实体模板列表分页
    async queryTemplate({ commit }, payload) {
      const response = await resolveResponseError(() => queryTemplate(payload));
      commit('saveModelTempListPaging', response || []);
    },
    //根据实体英文名或环境英文名查询实体环境映射值
    async queryModelEnvByModelNameEn({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryModelEnvByModelNameEn(payload)
      );
      commit('saveModelEnvList', response);
    },
    // 查询实体模板详情
    async getModelTempDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryModelTemp(payload)
      );
      commit('getModelDetal', response);
    },
    async queryMarkDown({ commit }, payload) {
      const response = await resolveResponseError(() => queryMarkDown(payload));
      commit('saveMarkDownInfo', response);
    },
    async stopPipeline({ commit }, payload) {
      const response = await resolveResponseError(() => stopPipeline(payload));
      commit('saveStopPipelineInfo', response);
    },
    async stopJob({ commit }, payload) {
      const response = await resolveResponseError(() => stopJob(payload));
      commit('saveStopJobInfo', response);
    },
    async copyPipelineTemplate({ commit }, payload) {
      await resolveResponseError(() => copyPipelineTemplate(payload));
    },
    async pipelineTemplateUpdate({ commit }, payload) {
      await resolveResponseError(() => pipelineTemplateUpdate(payload));
    },
    async saveAsPipelineTemplate({ commit }, payload) {
      const response = await resolveResponseError(() =>
        saveAsPipelineTemplate(payload)
      );
      commit('saveAsPipeTemplate', response);
    },
    // 查询、获取环境列表
    async queryEnvList({ commit }, payload) {
      const response = await resolveResponseError(() => queryEnvList(payload));
      commit('saveEnvList', response || []);
    },
    async queryModelTemplateByContent({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryModelTemplateByContent(payload)
      );
      commit('saveModelTemplate', response);
    },
    async queryContentByPathFromGit({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTypeAndContent(payload)
      );
      commit('saveFileInfo', response);
    },
    async queryContentByPathFromFdev({ commit }, payload) {
      const response = await resolveResponseError(() => getMinioInfo(payload));
      commit('saveFileInfo', response);
    },
    async saveToMinio({ commit }, payload) {
      const response = await resolveResponseError(() => saveToMinio(payload));
      commit('saveFileInfo', response);
    },
    // async getModelListPaging({ commit }, payload) {
    //   const response = await queryPage(payload);
    //   commit('saveModelListPaging', response);
    // },
    async retryPipeline({ commit }, payload) {
      await resolveResponseError(() => retryPipeline(payload));
    },
    async getAllJobs({ commit }, payload) {
      const response = await resolveResponseError(() => getAllJobs(payload));
      commit('saveJobList', response);
    },
    async getFullJobsByIds({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getFullJobsByIds(payload)
      );
      commit('saveJobListDetail', response);
    },
    async getAllRunnerCluster({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getAllRunnerCluster(payload)
      );
      commit('saveRunners', response);
    },
    async queryTriggerRules({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTriggerRules(payload)
      );
      commit('saveTriggerRules', response);
    },
    async updateTriggerRules({ commit }, payload) {
      await resolveResponseError(() => updateTriggerRules(payload));
    },
    async queryPipelineDetailById({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryPipelineDetailById(payload)
      );

      commit('savePipelineDetail', response);
    },
    async copyPipeline({ commit }, payload) {
      const response = await resolveResponseError(() => copyPipeline(payload));
      commit('saveCopyPipelineId', response);
    },
    async queryEntityModelDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryEntityModelDetail(payload)
      );
      commit('saveEntityModelDetail', response);
    },
    async updateVisibleRange({ commit }, payload) {
      const response = await resolveResponseError(() =>
        updateVisibleRange(payload)
      );
      commit('saveVisibleRange', response);
    },
    async pipelineTemplateAdd({ commit }, payload) {
      await resolveResponseError(() => pipelineTemplateAdd(payload));
    },
    async jobsUpdate({ commit }, payload) {
      await resolveResponseError(() => jobsUpdate(payload));
    },
    async delJobsTemplate({ commit }, payload) {
      await resolveResponseError(() => delJobsTemplate(payload));
    },
    async updateJobsVisibleRange({ commit }, payload) {
      await resolveResponseError(() => updateJobsVisibleRange(payload));
    },
    async addJobsTemplate({ commit }, payload) {
      const response = await resolveResponseError(() =>
        addJobsTemplate(payload)
      );
      commit('saveAddJobsTemplate', response);
    },
    async getRunnerClusterInfoByParam({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getRunnerClusterInfoByParam(payload)
      );
      commit('saveRunnerClusterInfo', response);
    },
    async addRunnerCluster({ commit }, payload) {
      await resolveResponseError(() => addRunnerCluster(payload));
    },
    async getAllRunnerInfo({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getAllRunnerInfo(payload)
      );
      commit('saveAllRunnerInfo', response);
    },
    async updateRunnerCluster({ commit }, payload) {
      await resolveResponseError(() => updateRunnerCluster(payload));
    },
    async queryImageList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryImageList(payload)
      );
      commit('saveImageList', response);
    },
    async getPipelineHistoryVersion({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getPipelineHistoryVersion(payload)
      );
      commit('savePipeHistoryVersion', response);
    },
    async setPipelineRollBack({ commit }, payload) {
      const response = await resolveResponseError(() =>
        setPipelineRollBack(payload)
      );
      commit('saveCurrentVersionId', response);
    }
  },

  mutations: {
    savePlugins(state, payload) {
      state.plugins = payload;
    },
    saveCollectionPipelineList(state, payload) {
      state.CollectionPipelineList = payload;
    },
    saveMinePipelineList(state, payload) {
      state.MinePipelineList = payload;
    },
    saveAllPipelineList(state, payload) {
      state.AllPipelineList = payload;
    },
    saveFdevciLogList(state, payload) {
      state.FdevciLogList = payload;
    },
    savePipelineTemplateList(state, payload) {
      state.PipelineTemplateList = payload;
    },
    saveAppPipelineList(state, payload) {
      state.AppPipelineList = payload;
    },
    savePluginReleaseLogList(state, payload) {
      state.pluginReleaseLogList = payload;
    },
    saveDeleteInfoList(state, payload) {
      state.deleteInfoList = payload.list;
    },
    saveLogDetail(state, payload) {
      state.logDetail = payload;
    },
    savePipelineLogDetail(state, payload) {
      state.pipelineLogDetail = payload;
    },
    saveBranchList(state, payload) {
      state.branchList = payload;
    },
    savePluginCategoryInfo(state, payload) {
      state.pluginCategoryInfo = payload;
    },
    savePluginDetailInfo(state, payload) {
      state.pluginDetailInfo = payload;
    },
    saveYamlConfigByIdInfo(state, payload) {
      state.yamlConfigByIdInfo = payload;
    },
    saveYamlConfigInfo(state, payload) {
      state.yamlConfigInfo = payload;
    },
    saveEntityList(state, payload) {
      state.modelList = payload;
    },
    saveModelTempListPaging(state, payload) {
      state.modelTempListPaging = payload;
    },
    saveModelEnvList(state, payload) {
      state.modelEnvList = payload;
    },
    getModelDetal(state, payload) {
      state.modelTempList = payload;
    },
    saveMarkDownInfo(state, payload) {
      state.markDownInfo = payload;
    },
    saveStopPipelineInfo(state, payload) {
      state.stopPipelineInfo = payload;
    },
    saveStopJobInfo(state, payload) {
      state.stopJobInfo = payload;
    },
    saveAsPipeTemplate(state, payload) {
      state.saveAsPipeTemp = payload;
    },
    saveEnvList(state, payload) {
      state.envList = payload;
    },
    saveModelTemplate(state, payload) {
      state.modeTemplate = payload.entityTemplateList;
    },
    saveFileInfo(state, payload) {
      state.fileInfo = payload;
    },
    saveJobList(state, payload) {
      state.jobList = payload;
    },
    saveJobListDetail(state, payload) {
      state.jobListDetail = payload;
    },
    saveRunners(state, payload) {
      state.runners = payload;
    },
    saveTriggerRules(state, payload) {
      state.triggerRules = payload;
    },
    savePipelineDetail(state, payload) {
      state.pipelineDetail = payload;
    },
    saveCopyPipelineId(state, payload) {
      state.copyPipelineId = payload;
    },
    saveEntityModelDetail(state, payload) {
      state.entityModelDetail = payload;
    },
    saveVisibleRange(state, payload) {
      state.visibleRangeData = payload;
    },
    saveAddJobsTemplate(state, payload) {
      state.JobsTempAddData = payload;
    },
    saveRunnerClusterInfo(state, payload) {
      state.runnerClusterInfo = payload;
    },
    saveAllRunnerInfo(state, payload) {
      state.allRunnerInfo = payload;
    },
    saveImageList(state, payload) {
      state.imageList = payload;
    },
    savePipeHistoryVersion(state, payload) {
      state.pipeHistoryVersion = payload;
    },
    saveCurrentVersionId(state, payload) {
      state.currentVersionId = payload;
    }
  }
};
