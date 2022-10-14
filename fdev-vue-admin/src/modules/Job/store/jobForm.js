import {
  queryAppDeployByTaskId, //应用的部署状态
  queryProjectBranchList,
  addApprove,
  appDeploy,
  queryAppAscriptionGroup,
  passApprove,
  refuseApprove,
  releaseApproveList,
  checkSccOrCaas,
  queryAddTaskType,
  add as addJob,
  query as queryJob,
  remove as removeJob,
  queryMailTaskByUnitNo,
  update as updateJob,
  updateExtraConfigParam,
  updateJobStage,
  queryMainTask,
  profile as queryJobProfile,
  queryDocDetail,
  queryTestDetail,
  queryUATTestDetail,
  queryMainJobByTaskId,
  addBranch,
  queryDeleteJob,
  deleteJob,
  updateTaskStatus, //改变任务状态 是否主任务
  queryExtraConfigParam,
  addExtraConfigParam,
  addApp,
  putSitTest,
  putUatTest,
  noticeTest,
  queryEnv,
  queryUserTask,
  saveConfigTemplate,
  queryWithOption,
  fuzzyQuery,
  updateJobDate,
  updateUatReceivingParty,
  queryRqrmnts,
  queryFtaskMantis,
  queryJiraIssues,
  abandonTask,
  saveDevConfigProperties,
  deleteFile,
  createFirstReview,
  queryReviewRecordStatus,
  createFolder,
  updateState,
  queryFileList,
  deleteFileById,
  queryPreviewLink,
  addProIssue,
  saveReviewRecord,
  getJobUser,
  queryReviewRecordHistory,
  taskNameJudge,
  addReviewIdea,
  queryByTaskIdNode,
  testReportCreate,
  queryTestMergeInfo,
  createTestRunMerge,
  queryCommitTips,
  getCodeQuality,
  getScanProcess,
  iOSOrAndroidAppPackage,
  downloadSonarLog,
  exportExcelData,
  deleteFileNew,
  updateNocodeInfo,
  nocodeTask,
  noCodeRelator,
  deleteFileRid,
  delNoCodeRelator,
  filesUpload,
  addNoCodeRelator,
  nextStage,
  uploadFilesRid,
  bafflePoint,
  downloadForWps,
  updateTaskDoc,
  deleteTaskDoc,
  configFilePreview,
  confirmBtn,
  testKeyNote,
  queryRqrDocInfo,
  queryTasks,
  queryTestTask,
  queryTaskRqrmntAlert,
  queryRqrmntFileUri,
  saveTaskAndJiraIssues,
  updateJiraIssues,
  putJiraIssues,
  queryJiraStoryByKey,
  checkMountUnit,
  queryPostponeTask, //延期任务
  taskCardDisplay, //电子看板
  downloadTemplateFile,
  putSecurityTest,
  queryAllComponents, //前后端组件列表
  queryAllArchetypeTypes, //前后端骨架列表
  getAllUserAndRole //查询所有用户信息（包括角色）
} from '@/services/job';
import {
  getEnvModelList,
  getModelConstant,
  getEnvList,
  getEnvListByAppId,
  previewConfigFile,
  queryConfigTemplate,
  queryModelList,
  queryExcludePirvateModelList
} from '@/services/application';
import { queryMerger } from '@/services/api';
import {
  queryProIssueById,
  updateProIssue,
  queryIssueFiles,
  fileDownload,
  deleteIssueFile,
  addFile,
  deleteProIssue
} from '@/services/mantis';
import { resolveResponseError, exportExcel } from '@/utils/utils';
import { formatJob } from '../utils/utils';
export default {
  namespaced: true,

  state: {
    appGroup: false,
    codeApproveList: [],
    branchList: [],
    deployFlag: false,
    mountUnitFlag: {},
    taskTypeRqr: [],
    taskAllData: [],
    UATtaskAllData: [
      {
        name: 'renwu1',
        id: 'id1'
      },
      {
        name: 'renwu2',
        id: 'id2'
      }
    ],
    taskRqrmntAlert: '',
    rqrmntFileUri: '',
    uploadFilesRidInfo: [],
    nextStageInfo: {},
    addNoCodeRelatorInfo: {},
    delNoCodeRelatorInfo: {},
    deleteFileRidInfo: [],
    noCodeRelatorInfo: {},
    noCodeTask: {},
    upNoCodeInfo: {},
    jobs: [],
    bafflePointFlag: '',
    jobProfile: {},
    docDetail: {},
    testDetail: {},
    mainTask: {},
    queryDeleteJobProfile: {},
    userTaskData: [],
    jobEnvModelList: [],
    modelConstant: {},
    previewFile: {
      outSideParam: {},
      modelParam: {},
      content: ''
    },
    envList: [],
    envListByAppId: [],
    extraConfigParam: [],
    configTemplate: '',
    modelList: [],
    pirvateModelList: [],
    mergerData: {},
    envData: {},
    appData: {},
    branchData: {},
    mainTaskListData: [],
    unitMainTaskListData: [],
    jobWithOptData: {},
    fuzzyData: [],
    rqrmntsList: {},
    defectList: [],
    UATnumtList: {},
    UATdefectList: [],
    reviewRecordStatus: '',
    reviewRecordDetail: {},
    configProperties: '',
    fileFolder: {},
    designState: {},
    fileList: [],
    previewLink: {},
    proIssueDate: {},
    proIssueDateById: {},
    proIssueUpdate: {},
    issueFilesDate: {},
    fileDownloadDate: null,
    secondReviewers: [],
    reviewRecord: [],
    judgeResult: true,
    taskCanEntryUat: false,
    testMergeInfo: {},
    testRunMerge: {},
    commitTips: {}, // 分支合并时，冲突相关信息
    codeQuality: {},
    scanProcess: {},
    sonarLog: null,
    configFilePreviewObj: {},
    result: {},
    checkList: {},
    isExistJiraStory: {},
    postponeTaskList: [], //延期任务列表
    realTimeTask: [], //电子看板
    templateFile: {},
    componentList: [],
    archetList: [],
    userInfoList: [], //查询所有用户信息（包括角色）
    deployInfo: {} //部署状态信息
  },

  getters: {},

  actions: {
    async queryAppDeployByTaskId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAppDeployByTaskId(payload)
      );
      commit('saveDeployInfo', response);
    },
    async queryProjectBranchList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryProjectBranchList(payload)
      );
      commit('saveBranchList', response);
    },
    //新增合并审批
    async addApprove({ commit }, payload) {
      await resolveResponseError(() => addApprove(payload));
    },
    //新增部署审批
    async appDeploy({ commit }, payload) {
      await resolveResponseError(() => appDeploy(payload));
    },
    //合并审批拒绝
    async refuseApprove({ commit }, payload) {
      await resolveResponseError(() => refuseApprove(payload));
    },
    //合并审批通过
    async passApprove({ commit }, payload) {
      await resolveResponseError(() => passApprove(payload));
    },
    //查合并审批列表
    async releaseApproveList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        releaseApproveList(payload)
      );
      commit('saveCodeApproveList', response);
    },
    //查应用的部署信息
    async checkSccOrCaas({ commit }, payload) {
      const response = await resolveResponseError(() =>
        checkSccOrCaas(payload)
      );
      commit('saveCheckSccOrCaasg', response);
    },
    //查需求可新建的任务类型
    async queryAddTaskType({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAddTaskType(payload)
      );
      commit('saveTaskTypeRqr', response);
    },

    //查任务是否挂载了实施单元
    async checkMountUnit({ commit }, payload) {
      const response = await resolveResponseError(() =>
        checkMountUnit(payload)
      );
      commit('saveMountUnitFlag', response);
    },

    async queryJiraStoryByKey({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryJiraStoryByKey(payload)
      );
      commit('saveJiraStory', response);
    },
    async saveTaskAndJiraIssues({ commit }, payload) {
      await resolveResponseError(() => saveTaskAndJiraIssues(payload));
    },
    async updateJiraIssues({ commit }, payload) {
      await resolveResponseError(() => updateJiraIssues(payload));
    },
    async putJiraIssues({ commit }, payload) {
      await resolveResponseError(() => putJiraIssues(payload));
    },
    async queryTestTask({ commit }, payload) {
      const response = await resolveResponseError(() => queryTestTask(payload));
      commit('saveTestTasks', response);
    },
    async queryTasks({ commit }, payload) {
      const response = await resolveResponseError(() => queryTasks(payload));
      commit('saveQueryTasks', response);
    },
    async queryTaskRqrmntAlert({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTaskRqrmntAlert(payload)
      );
      commit('saveTaskRqrmntAlert', response);
    },
    async queryRqrmntFileUri({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryRqrmntFileUri(payload)
      );
      commit('saveRqrmntFileUri', response);
    },
    async uploadFilesRid({ commit }, payload) {
      let response = await resolveResponseError(() => uploadFilesRid(payload));
      response = JSON.stringify(response) === '{}' ? [] : response;
      commit('saveUploadFilesRid', response);
    },
    async nextStage({ commit }, payload) {
      const response = await resolveResponseError(() => nextStage(payload));
      commit('saveNextStage', response);
    },
    async addNoCodeRelator({ commit }, payload) {
      const response = await resolveResponseError(() =>
        addNoCodeRelator(payload)
      );
      commit('saveAddNoCodeRelator', response);
    },
    async filesUpload({ commit }, payload) {
      await resolveResponseError(() => filesUpload(payload));
    },
    async delNoCodeRelator({ commit }, payload) {
      const response = await resolveResponseError(() =>
        delNoCodeRelator(payload)
      );
      commit('saveDelNoCodeRelator', response);
    },
    async deleteFileRid({ commit }, payload) {
      const response = await resolveResponseError(() => deleteFileRid(payload));
      commit('saveDeleteFileRid', response);
    },
    async noCodeRelator({ commit }, payload) {
      const response = await resolveResponseError(() => noCodeRelator(payload));
      commit('saveNoCodeRelator', response);
    },
    async nocodeTask({ commit }, payload) {
      const response = await resolveResponseError(() => nocodeTask(payload));
      commit('saveNocodeTask', response);
    },
    async updateNocodeInfo({ commit }, payload) {
      const response = await resolveResponseError(() =>
        updateNocodeInfo(payload)
      );
      commit('saveUpdateNocodeInfo', response);
    },
    async addApp({ commit }, payload) {
      const response = await resolveResponseError(() => addApp(payload));
      commit('saveApp', response);
    },
    async addJob({ commit, state }, payload) {
      const response = await resolveResponseError(() => addJob(payload));
      const list = [...state.jobs, formatJob(response)];
      commit('saveJob', list);
      commit('saveJobProfile', response);
    },
    async addBranch({ commit }, payload) {
      const response = await resolveResponseError(() => addBranch(payload));
      commit('saveBranch', response);
    },
    async fetchJob({ commit }, payload) {
      const response = await resolveResponseError(() => queryJob(payload));
      commit('saveJob', response.map(job => formatJob(job)));
    },
    async removeJob({ commit, state }, payload) {
      payload = Array.isArray(payload) ? payload : [payload];
      await resolveResponseError(() => removeJob(payload));
      const list = state.jobs.filter(
        item => !payload.some(Job => Job.id === item.id)
      );
      commit('saveJob', list);
    },
    async updateJob({ commit, state }, payload) {
      const response = await resolveResponseError(() => updateJob(payload));
      let item = state.jobs.find(item => item.id === payload.id);
      if (item) {
        Object.assign(item, formatJob(response));
      }
      const list = [...state.jobs];
      commit('saveJob', list);
      commit('saveJobProfile', response);
    },
    async updateJobDate({ commit, state }, payload) {
      const response = await resolveResponseError(() => updateJobDate(payload));
      let item = state.jobs.find(item => item.id === payload.id);
      if (item) {
        Object.assign(item, formatJob(response));
      }
      const list = [...state.jobs];
      commit('saveJob', list);
      commit('saveJobProfile', response);
    },
    async queryMainTask({ commit }, payload) {
      const response = await resolveResponseError(() => queryMainTask(payload));
      commit('saveMainTaskList', response);
    },
    // 改变任务阶段
    async updateJobStage({ commit }, payload) {
      const response = await resolveResponseError(() =>
        updateJobStage(payload)
      );
      commit('saveJobProfile', response);
    },
    async queryMailTaskByUnitNo({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryMailTaskByUnitNo(payload)
      );
      commit('saveMailTaskByUnitNo', response);
    },
    async updateTaskStatus({ commit }, payload) {
      const response = await resolveResponseError(() =>
        updateTaskStatus(payload)
      );
      commit('saveJobProfile', response);
    },
    async queryDeleteJob({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryDeleteJob(payload)
      );
      commit('saveQqueryDeleteJobProfile', response);
    },
    async deleteJob({ commit, state }, payload) {
      await resolveResponseError(() => deleteJob(payload));
      const list = state.jobs.filter(item => payload.id !== item.id);
      commit('saveJob', list);
    },
    async queryJobProfile({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryJobProfile(payload)
      );
      commit('saveJobProfile', response);
    },
    async queryDocDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryDocDetail(payload)
      );
      commit('saveDocDetail', response);
    },
    async queryTestDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTestDetail(payload)
      );
      commit('saveTestDetail', response);
    },
    async queryUATTestDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryUATTestDetail(payload)
      );
      commit('saveUATTestDetail', response);
    },
    async queryMainJobByTaskId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryMainJobByTaskId(payload)
      );
      commit('saveMainTask', response);
    },
    //查询用户的任务
    async queryUserTask({ commit }, payload) {
      const response = await resolveResponseError(() => queryUserTask(payload));
      commit('saveUserTask', response);
    },
    // 获取实体英文名集合
    async getEnvModelList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getEnvModelList(payload)
      );
      commit('saveJobEnvModelList', response);
    },
    async getModelConstant({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getModelConstant(payload)
      );
      commit('saveModelConstant', response);
    },
    async saveConfigTemplate({ commit }, payload) {
      const response = await resolveResponseError(() =>
        saveConfigTemplate(payload)
      );
      commit('saveResult', response);
    },
    // android和iOS出测试包
    async iOSOrAndroidAppPackage({ commit }, payload) {
      await resolveResponseError(() => iOSOrAndroidAppPackage(payload));
    },
    async getEnvList({ commit }, payload) {
      const response = await resolveResponseError(() => getEnvList(payload));
      commit('saveEnvList', response);
    },
    // 根据应用查询环境列表
    async getEnvListByAppId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getEnvListByAppId(payload)
      );
      commit('saveEnvListByAppId', response);
    },
    // 预览配置文件
    async previewConfigFile({ commit }, payload) {
      const response = await resolveResponseError(() =>
        previewConfigFile(payload)
      );
      commit('savePreviewFile', response);
    },
    //查询外部配置参数（优先生效）
    async queryExtraConfigParam({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryExtraConfigParam(payload)
      );
      commit('saveExtraConfigParam', response);
    },
    //新增外部配置参数（优先生效）
    async addExtraConfigParam({ commit }, payload) {
      await resolveResponseError(() => addExtraConfigParam(payload));
    },
    //更新外部配置参数（优先生效）
    async updateExtraConfigParam({ commit }, payload) {
      await resolveResponseError(() => updateExtraConfigParam(payload));
    },
    async queryConfigTemplate({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryConfigTemplate(payload)
      );
      commit('saveConfigTemplate', response);
    },
    async queryModelList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryModelList(payload)
      );
      commit('saveModelList', response);
    },
    async queryExcludePirvateModelList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryExcludePirvateModelList(payload)
      );
      commit('savePirvateModelList', response);
    },
    async queryMerger({ commit }, payload) {
      const response = await resolveResponseError(() => queryMerger(payload));
      commit('saveMerger', response);
    },
    async putSitTest({ commit }, payload) {
      await resolveResponseError(() => putSitTest(payload));
    },
    async putUatTest({ commit }, payload) {
      await resolveResponseError(() => putUatTest(payload));
    },
    async toNoticeTest({ commit }, payload) {
      await resolveResponseError(() => noticeTest(payload));
    },
    async queryEnv({ commit }, payload) {
      const response = await resolveResponseError(() => queryEnv(payload));
      commit('saveEnv', response);
    },
    // 查询小组，阶段
    async queryJobWithOption({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryWithOption(payload)
      );
      commit('saveJobWithOption', response);
    },
    // 查询实施单元编号，任务名称，所属应用，模糊匹配
    async fuzzyQueryJob({ commit }, payload) {
      const response = await resolveResponseError(() => fuzzyQuery(payload));
      commit('saveFuzzy', response);
    },
    async updateUatReceivingParty({ commit }, payload) {
      await resolveResponseError(() => updateUatReceivingParty(payload));
    },
    async queryRqrmnts({ commit }, payload) {
      const response = await resolveResponseError(() => queryRqrmnts(payload));
      commit('saveRqrmnts', response);
    },
    async queryFtaskMantis({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryFtaskMantis(payload)
      );
      commit('saveDefect', response);
    },
    async queryJiraIssues({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryJiraIssues(payload)
      );
      commit('saveUATDefect', response);
    },
    async abandonTask({ commit }, payload) {
      await resolveResponseError(() => abandonTask(payload));
    },
    // 保存环境配置文件
    async saveDevConfigProperties({ commit }, payload) {
      const response = await resolveResponseError(() =>
        saveDevConfigProperties(payload)
      );
      commit('saveConfigProperties', response);
    },
    async deleteFile({ commit }, payload) {
      await resolveResponseError(() => deleteFile(payload));
    },
    async createFirstReview({ commit }, payload) {
      const res = await resolveResponseError(() => createFirstReview(payload));
      commit('saveTaskStatus', res.reviewStatus);
    },
    async saveReviewRecord({ commit }, payload) {
      await resolveResponseError(() => saveReviewRecord(payload));
    },
    async addReviewIdea({ commit }, payload) {
      await resolveResponseError(() => addReviewIdea(payload));
    },
    async getJobUser({ commit }, payload) {
      const res = await getJobUser();
      commit('saveSecondReviewer', res);
    },
    async queryReviewRecordHistory({ commit }, payload) {
      const res = await queryReviewRecordHistory(payload);
      commit('saveReviewRecordHistory', res);
    },
    async queryReviewRecordStatus({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryReviewRecordStatus(payload)
      );
      commit('saveReviewRecordStatus', response);
    },
    async createFolder({ commit }, payload) {
      const response = await resolveResponseError(() => createFolder(payload));
      commit('saveFileFolder', response);
    },
    async updateState({ commit }, payload) {
      const response = await resolveResponseError(() => updateState(payload));
      commit('saveUpdateState', response);
    },
    async queryFileList({ commit }, payload) {
      const response = await resolveResponseError(() => queryFileList(payload));
      commit('saveFileList', response);
    },
    async deleteFileById({ commit }, payload) {
      await resolveResponseError(() => deleteFileById(payload));
    },
    async queryPreviewLink({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryPreviewLink(payload)
      );
      commit('savePreviewLink', response);
    },
    async addProIssue({ commit }, payload) {
      const response = await resolveResponseError(() => addProIssue(payload));
      commit('saveProIssue', response);
    },
    async queryProIssueById({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryProIssueById(payload)
      );
      commit('saveProIssueById', response);
    },
    async updateProIssue({ commit }, payload) {
      const response = await resolveResponseError(() =>
        updateProIssue(payload)
      );
      commit('saveUpdateProIssue', response);
    },
    async queryIssueFiles({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryIssueFiles(payload)
      );
      commit('saveQueryIssueFiles', response);
    },
    async fileDownload({ commit }, payload) {
      const response = await resolveResponseError(() => fileDownload(payload));
      commit('savefileDownload', response);
    },
    async deleteIssueFile({ commit }, payload) {
      await resolveResponseError(() => deleteIssueFile(payload));
    },
    async addFile({ commit }, payload) {
      await resolveResponseError(() => addFile(payload));
    },
    async deleteProIssue({ commit }, payload) {
      await resolveResponseError(() => deleteProIssue(payload));
    },
    async taskNameJudge({ commit }, payload) {
      const response = await resolveResponseError(() => taskNameJudge(payload));
      commit('saveJudgeResult', response);
    },
    async queryByTaskIdNode({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryByTaskIdNode(payload)
      );
      commit('saveTaskCanEntryUat', response);
    },
    async testReportCreate({ commit }, payload) {
      await resolveResponseError(() => testReportCreate(payload));
    },
    async queryTestMergeInfo({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTestMergeInfo(payload)
      );
      commit('saveTestMergeInfo', response);
    },
    async createTestRunMerge({ commit }, payload) {
      const response = await resolveResponseError(() =>
        createTestRunMerge(payload)
      );
      commit('saveTestRunMerge', response);
    },
    async queryCommitTips({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryCommitTips(payload)
      );
      commit('saveCommitTips', response);
    },
    async getCodeQuality({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getCodeQuality(payload)
      );
      commit('saveCodeQuality', response);
    },
    async getScanProcess({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getScanProcess(payload)
      );
      commit('saveScanProcess', response);
    },
    async downloadSonarLog({ commit }, payload) {
      const response = await resolveResponseError(() =>
        downloadSonarLog(payload)
      );
      commit('saveSonarLog', response);
    },
    async configFilePreview({ commit }, payload) {
      const response = await resolveResponseError(() =>
        configFilePreview(payload)
      );
      commit('saveConfigFilePreview', response);
    },
    async downExcel({ commit }, payload) {
      const response = await resolveResponseError(() =>
        exportExcelData(payload)
      );
      exportExcel(response);
    },
    async bafflePoint({ commit }, payload) {
      const response = await resolveResponseError(() => bafflePoint(payload));
      commit('saveBafflePointFlag', response);
    },
    async queryAppAscriptionGroup({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAppAscriptionGroup(payload)
      );
      commit('saveAppGroup', response);
    },
    async downloadForWps({ commit }, payload) {
      const response = await resolveResponseError(() =>
        downloadForWps(payload)
      );
      const link = document.createElement('a');
      link.href = response.url;
      document.body.appendChild(link);
      link.click();
      window.URL.revokeObjectURL(link.href);
      document.body.removeChild(link);
    },
    async deleteFileNew({ commit }, payload) {
      await resolveResponseError(() => deleteFileNew(payload));
    },
    async updateTaskDoc({ commit }, payload) {
      await resolveResponseError(() => updateTaskDoc(payload));
    },
    async deleteTaskDoc({ commit }, payload) {
      await resolveResponseError(() => deleteTaskDoc(payload));
    },
    async confirmBtn({ commit }, payload) {
      await resolveResponseError(() => confirmBtn(payload));
    },
    async testKeyNote({ commit }, payload) {
      await resolveResponseError(() => testKeyNote(payload));
    },
    async queryRqrDocInfo({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryRqrDocInfo(payload)
      );
      commit('saveCheckList', response);
    },
    // 查询延期任务
    async queryPostponeTask({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        queryPostponeTask(data)
      );
      commit('savePostponeTask', response);
    },
    // 电子看板
    async taskCardDisplay({ commit }, payload) {
      const response = await resolveResponseError(() =>
        taskCardDisplay(payload)
      );
      commit('saveTaskCardDisplay', response);
    },
    // 下载安全测试清单模板
    async downloadTemplateFile({ commit, state }, data) {
      const response = await resolveResponseError(() =>
        downloadTemplateFile(data)
      );
      commit('saveTemplateFile', response);
    },
    // 提交安全测试
    async putSecurityTest({ commit, state }, data) {
      await resolveResponseError(() => putSecurityTest(data));
    },
    // 前后端组件列表
    async queryAllComponents({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAllComponents(payload)
      );
      commit('saveComponentList', response);
    },
    // 前后端骨架列表
    async queryAllArchetypeTypes({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAllArchetypeTypes(payload)
      );
      commit('saveArchetList', response);
    },
    //查询所有用户信息（包括角色）
    async getAllUserAndRole({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getAllUserAndRole(payload)
      );
      commit('saveUserInfoList', response);
    }
  },

  mutations: {
    saveDeployInfo(state, payload) {
      state.deployInfo = payload;
    },
    saveAppGroup(state, payload) {
      state.appGroup = payload;
    },
    saveBranchList(state, payload) {
      state.branchList = payload;
    },
    saveCodeApproveList(state, payload) {
      state.codeApproveList = payload;
    },
    saveCheckSccOrCaasg(state, payload) {
      state.deployFlag = payload.flag;
    },
    saveTaskTypeRqr(state, payload) {
      state.taskTypeRqr = payload.taskType;
    },
    saveMountUnitFlag(state, payload) {
      state.mountUnitFlag = payload;
    },
    saveQueryTasks(state, payload) {
      state.taskAllData = payload;
    },
    saveTestTasks(state, payload) {
      state.UATtaskAllData = payload;
    },
    saveTaskRqrmntAlert(state, payload) {
      state.taskRqrmntAlert = payload;
    },
    saveRqrmntFileUri(state, payload) {
      state.rqrmntFileUri = payload;
    },
    saveUploadFilesRid(state, payload) {
      state.uploadFilesRidInfo = payload;
    },
    saveNextStage(state, payload) {
      state.nextStageInfo = payload;
    },
    saveAddNoCodeRelator(state, payload) {
      state.addNoCodeRelatorInfo = payload;
    },
    saveDelNoCodeRelator(state, payload) {
      state.delNoCodeRelatorInfo = payload;
    },
    saveDeleteFileRid(state, payload) {
      state.deleteFileRidInfo = payload;
    },
    saveNoCodeRelator(state, payload) {
      state.noCodeRelatorInfo = payload;
    },
    saveNocodeTask(state, payload) {
      state.noCodeTask = payload;
    },
    saveUpdateNocodeInfo(state, payload) {
      state.upNoCodeInfo = payload;
    },
    saveBafflePointFlag(state, payload) {
      state.bafflePointFlag = payload;
    },
    saveApp(state, payload) {
      state.appData = payload;
    },
    saveJob(state, payload) {
      state.jobs = payload;
    },
    saveExtraConfigParam(state, payload) {
      state.extraConfigParam = payload;
    },
    saveBranch(state, payload) {
      state.branchData = payload;
    },
    saveJobProfile(state, payload) {
      state.jobProfile = payload;
    },
    saveDocDetail(state, payload) {
      state.docDetail = payload;
    },
    saveTestDetail(state, payload) {
      state.testDetail = payload;
    },
    saveUATTestDetail(state, payload) {
      state.UATtestDetail = payload;
    },
    saveMainTaskList(state, payload) {
      state.mainTaskListData = payload;
    },
    saveMainTask(state, payload) {
      if (payload.length > 0) {
        state.mainTask = payload[0];
      } else {
        state.mainTask = {};
      }
    },
    saveMailTaskByUnitNo(state, payload) {
      state.unitMainTaskListData = payload;
    },
    saveQqueryDeleteJobProfile(state, payload) {
      state.queryDeleteJobProfile = payload;
    },
    saveUserTask(state, payload) {
      state.userTaskData = payload;
    },
    saveJobEnvModelList(state, payload) {
      state.jobEnvModelList = payload;
    },
    saveModelConstant(state, payload) {
      state.modelConstant = payload;
    },
    saveEnvList(state, payload) {
      state.envList = payload;
    },
    //根据应用查环境列表
    saveEnvListByAppId(state, payload) {
      state.envListByAppId = payload;
    },
    savePreviewFile(state, payload) {
      state.previewFile = payload;
    },
    saveConfigTemplate(state, payload) {
      state.configTemplate = payload;
    },
    saveModelList(state, payload) {
      state.modelList = payload;
    },
    savePirvateModelList(state, payload) {
      state.pirvateModelList = payload;
    },
    saveMerger(state, payload) {
      state.mergerData = payload;
    },
    saveEnv(state, payload) {
      state.envData = payload;
    },
    saveJobWithOption(state, payload) {
      state.jobWithOptData = payload;
    },
    saveFuzzy(state, payload) {
      state.fuzzyData = payload;
    },
    saveRqrmnts(state, payload) {
      state.rqrmntsList = payload;
    },
    saveDefect(state, payload) {
      state.defectList = payload;
    },
    saveUATDefect(state, payload) {
      state.UATdefectList = payload.issuesData;
      payload.numList && (state.UATnumtList = payload.numList[0] || {});
    },
    saveReviewRecordStatus(state, payload) {
      const { status, taskList } = payload;
      state.reviewRecordStatus = status;
      state.reviewRecordDetail = taskList ? taskList[0] : {};
    },
    saveTaskStatus(state, payload) {
      state.reviewRecordStatus = payload;
    },
    saveConfigProperties(state, payload) {
      state.configProperties = payload;
    },
    saveFileFolder(state, payload) {
      state.fileFolder = payload;
    },
    saveUpdateState(state, payload) {
      state.designState = payload;
    },
    saveFileList(state, payload) {
      state.fileList = payload;
    },
    savePreviewLink(state, payload) {
      state.previewLink = payload;
    },
    saveProIssue(state, payload) {
      state.proIssueDate = payload;
    },
    saveProIssueById(state, payload) {
      state.proIssueDateById = payload;
    },
    saveUpdateProIssue(state, payload) {
      state.proIssueUpdate = payload;
    },
    saveQueryIssueFiles(state, payload) {
      state.issueFilesDate = payload;
    },
    savefileDownload(state, payload) {
      state.fileDownloadDate = payload;
    },
    saveSecondReviewer(state, payload) {
      state.secondReviewers = payload;
    },
    saveReviewRecordHistory(state, payload) {
      state.reviewRecord = payload;
    },
    saveJudgeResult(state, payload) {
      state.judgeResult = payload.result;
    },
    saveTaskCanEntryUat(state, payload) {
      state.taskCanEntryUat = payload;
    },
    saveTestMergeInfo(state, payload) {
      state.testMergeInfo = payload;
    },
    saveTestRunMerge(state, payload) {
      state.testRunMerge = payload;
    },
    saveCommitTips(state, payload) {
      state.commitTips = payload;
    },
    saveCodeQuality(state, payload) {
      state.codeQuality = payload;
    },
    saveScanProcess(state, payload) {
      state.scanProcess = payload;
    },
    saveSonarLog(state, payload) {
      state.sonarLog = payload;
    },
    saveConfigFilePreview(state, payload) {
      state.configFilePreviewObj = payload;
    },
    saveResult(state, payload) {
      const resultFormat = {
        formatError: '格式异常',
        modelErrorList: '实体异常',
        FiledErrorList: '属性异常'
      };
      const result = payload ? {} : '';
      if (payload) {
        const keys = Object.keys(payload);
        keys.forEach(key => {
          if (payload[key].length > 0) {
            result[resultFormat[key]] = payload[key];
          }
        });
      }
      state.result = result;
    },
    saveCheckList(state, payload) {
      state.checkList = payload;
    },
    saveJiraStory(state, payload) {
      state.isExistJiraStory = payload;
    },
    savePostponeTask(state, data) {
      state.postponeTaskList = data;
    },
    saveTaskCardDisplay(state, data) {
      state.realTimeTask = data;
    },
    saveTemplateFile(state, data) {
      state.templateFile = data;
    },
    saveComponentList(state, data) {
      state.componentList = data;
    },
    saveArchetList(state, data) {
      state.archetList = data;
    },
    saveUserInfoList(state, data) {
      state.userInfoList = data;
    }
  }
};
