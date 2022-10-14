import {
  addManualNoteInfo,
  queryManualNoteInfo,
  updateManualNoteInfo,
  createManualNote,
  queryBatchAppIdByNoteId,
  updateNoteBatchNo,
  createBatchTask,
  updateBatchTask,
  deteleBatchTask,
  queryBatchTask,
  addBatchTask,
  queryBatchTaskList,
  deleteNote,
  generateReleaseNotes,
  addNoteSql,
  queryNoteSql,
  deleteNoteSql,
  updateNoteSeqNo,
  lockNote,
  queryAllGroupAbbr,
  updateNoteConfiguration,
  deleteNoteConfiguration,
  addNoteConfiguration,
  queryNoteConfiguration,
  queryNoteDetail,
  addNoteService,
  queryNoteService,
  deleteNoteService,
  updateNoteService,
  queryReleaseNote,
  createNote,
  templateQuery,
  setTemplate,
  audit,
  updateTemplate,
  createChanges,
  queryApplications,
  queryRecord,
  queryTemplateDetail,
  queryAssetsList,
  deleteAsset,
  deleteTemplate,
  deleteApplication,
  catalogoptions,
  querySysRlsInfo,
  queryResourceBranches,
  queryResourceFiles,
  queryProfile,
  addGitlabAsset,
  moveOrder,
  queryImageTags,
  setImageTag,
  deleteAssetArr,
  trace,
  queryReleaseNodes,
  queryReleaseNodeDetail,
  queryEnv,
  getNodeName,
  create,
  update,
  setTestEnvs,
  changeReleaseNode,
  updateTaskArchived,
  deleteTask,
  queryTasks,
  auditAdd,
  updateTaskReview,
  queryTaskReview,
  queryApply,
  queryComponent,
  queryTargetVersion,
  queryReleaseVersionComponent,
  queryReleaseVersionArchetype,
  queryLatestVersionComponent,
  queryLatestVersionArchetype,
  relDevopsComponent,
  relDevopsArchetype,
  relDevopsBaseImage,
  judgeTargetVersionComponent,
  queryProdInfo,
  queryVersion,
  changesUpdate,
  queryAppWithOutSum,
  deleteRelease,
  queryGroupAbbr,
  updateGroupAbbr,
  queryPlan,
  generateProdExcel,
  updateSysRlsInfo,
  queryResourceProjects,
  packageFromTag,
  relDevops,
  queryTasksReviews,
  sendEmailForTaskManagers,
  queryApplicationTags,
  queryAppScale,
  addAppScale,
  deleteAppScale,
  updateAppScale,
  queryAllProdAssets,
  queryDeAutoAllProdAssets,
  exportProdDirection,
  tasksInSitStage,
  queryGroupSysAbbr,
  updateGroupSysAbbr,
  queryDetailByTaskId,
  queryModuleType,
  deleteModuleType,
  addModuleType,
  updateModuleType,
  queryScript,
  deleteScript,
  addScript,
  updateScript,
  queryAutomationEnv,
  deleteAutomationEnv,
  addAutomationEnv,
  updateAutomationEnv,
  queryDBAssets,
  createTestrunBranch,
  mergeTaskBranch,
  queryNotinlineTasksByAppId,
  queryTaskByTestRunId,
  findByProdId,
  queryChangesDetail,
  changeConfirmConf,
  changeReleaseConf,
  queryReviewRecord,
  updateReviewRecord,
  queryByLabelsFuzzy,
  queryReviewBasicMsg,
  queryByReleaseNodeName,
  deleteFile,
  taskChangeNotise,
  downloadFiles,
  addSystemTestFile,
  addSourceReview,
  querySourceReviewDetail,
  pullGrayBranch,
  createATemplate,
  querySystem,
  queryExcelTemplate,
  editFakeInfo,
  iOSOrAndroidAppPublish,
  createBigReleaseNode,
  queryBigReleaseNodes,
  queryBigReleaseNodeDetail,
  queryContactInfo,
  updateBigReleasenode,
  queryRqrmntInfoList,
  queryByReleaseDate,
  updateBigReleaseDate,
  queryProWantTasks,
  queryBatchInfoByTaskId,
  addBatch,
  queryRqrmntInfoListByType,
  exportRqrmntInfoList,
  updateRqrmntInfo,
  preview,
  exportRqrmntInfoListByType,
  queryBatchInfoByAppId,
  queryReleaseSystem,
  queryRqrmntInfoTasks,
  exportSpecialRqrmntInfoList,
  queryDeAutoAssets,
  deAutoUpload,
  queryPackageTags,
  setPackageTag,
  queryConfigApplication,
  addConfigApplication,
  checkConfigApplication,
  createConfig,
  deleteConfig,
  queryConfigSum,
  confirmChanges,
  deleteReleaseNode,
  querySystemDetailByProdId,
  queryDbPath,
  uploadAssets,
  addReview,
  queryProdDir,
  updateProdDir,
  updateProdDeploy,
  queryDeployTypeByAppId,
  queryEnvList,
  updateAwsAssetGroupId,
  queryAwsGroups,
  addRedLineScanReport,
  whetherWriteOrder,
  queryReplicasnu,
  updateReplicasnu,
  addEsfRegistration,
  delEsf,
  updateEsf,
  queryEsfConfiguration,
  addEsfCommonConfig,
  updateEsfcommonconfigAssets,
  queryAppStatus,
  queryAppDeployPlatformInfo,
  addChangeApplication,
  queryAppsByAddEsf,
  queryThreeLevelGroups
} from '@/services/release';
import {
  queryReleaseNodeByJob,
  queryReleaseNode,
  addJobReleaseNode
} from '@/services/production';
import {
  exportProIssues,
  queryProIssues,
  countProIssues,
  queryProByTeam,
  queryIssueDetail
} from '@/services/mantis';
import { queryGroupPeople } from '@/services/user';
import {
  resolveResponseError,
  isValidReleaseDate,
  exportExcel
} from '@/utils/utils';
import SessionStorage from '#/plugins/SessionStorage';
import moment from 'moment';

export default {
  namespaced: true,

  state: {
    batchAppIdByNoteIdList: [],
    batchTaskList: [], // 批量任务列表
    batchTask: [], // 获取批量任务 - 弹窗
    noteSqlList: [], // 发布说明数据库列表
    noteConfiguration: [], // 发布说明配置文件列表
    allGroupAbbrList: [], // 小组表示集合
    releaseNoteDetail: {}, //发布说明详情
    deleteReleaseInfo: '', //变更列表删除返回的提示信息
    deAutoAssets: [], // 自动化发布列表
    packageTags: [], // tag列表
    queryRqrmntInfo: [], // 需求列表
    templateOfChanges: [], // 变更模板
    releaseNoteAppList: [], // 发布说明应用列表
    releaseNoteList: [], // 发布说明列表
    changeApllication: [], // 变更应用列表
    changesRecord: [], // 变更记录
    templateDetail: {}, // 变更模板详情
    assetsList: [], // 变更文件列表
    user: [],
    manualNoteInfo: {},
    catalog: [],
    sysRlsInfo: {},
    branches: [], // 保存分支
    files: [],
    filesWithRuntime: [],
    runtime_arr: ['DEV', 'TEST', 'TCYZ', 'PROCSH', 'PROCHF'],
    role: false,
    image_tags: null,
    type: () => SessionStorage.getItem('type'),
    traceInfo: {},
    compareTime: false,
    step: [
      '开始',
      '新建变更目录',
      '检出配置文件',
      '检查配置',
      '准备镜像脚本',
      '传送介质',
      '检查镜像推送'
    ],
    deAutoStep: [
      '开始',
      '新建变更目录',
      '检出配置文件',
      '检查配置',
      '检出应用包',
      '传送介质'
    ],
    releaseList: [],
    releaseDetail: {},
    envType: [],
    releaseNodeName: '',
    releaseNodeData: {},
    releaseNodeList: [],
    jobReleaseNodeData: {},
    taskList: [],
    taskReview: [],
    applyList: [],
    componentList: [],
    targetVersionList: [],
    prodInfo: {},
    version: '',
    appWithOutSum: [],
    groupAbbr: '',
    file: '',
    resourceProjects: [],
    examineList: [],
    tagsList: [],
    appScaleList: [],
    prodAssets: [], // 当前变更下所有变更介质
    deAutoprodAssets: [],
    prodDirection: '',
    tasksOfAppType: null,
    groupSysAbbr: '',
    imageTagResult: {},
    isOverdue: {},
    moduleType: [],
    scriptParams: [],
    automationEnv: [],
    database: [],
    testRunBranch: {},
    appTaskList: [],
    taskByTestRunId: [],
    applicationTips: '',
    deAutoapplicationTips: '',
    assetsCatalog: [],
    changesDetail: {},
    reviewRecord: [],
    labelsFuzzy: [],
    reviewBasicMsg: {},
    releaseFiles: {
      rqrmnt_file_list: []
    },
    prodDirList: [],
    release_node_name: '',
    downFiles: '',
    confirmIsRelease: false,
    sourceReviewDetail: {},
    returnMsg: '',
    templateSystem: [],
    excelTemplate: [],
    FakeInfoData: {},
    bigRelease: [],
    bigReleaseDetail: {},
    contactInfo: [],
    rqrmntInfoList: [],
    releaseDate: {},
    wantToReleaseTasks: [],
    appBatchInfo: {},
    testInfoList: [],
    securityInfoList: [],
    wpsUrl: '',
    appUserInfo: [],
    releaseSystemList: [],
    configApplicationList: [],
    isConfigSum: '',
    systemDetailByProdId: {},
    dbPath: [],
    errorConfigList: [],
    hasKnown: false,
    deployTypeByAppId: [], // 根据应用id查到的部署平台
    envList: [], // 查询全量环境
    awsGroups: [], // 对象存储用户所属组列表
    replicasNum: null, // 各发布环境对应的副本数
    esfConfigData: null, // esf配置中心信息（配置中心下拉选项）
    appsByAddEsf: [], // 添加esf时查询到的应用列表（全量应用去除已添加过esf的）
    appPublishInfo: null, // 应用投产信息（是否投过产、应用卡片上勾选的部署平台）
    appDeployPlatformInfo: null, // 应用的部署信息（是否有CAAS和SCC平台的部署信息）
    export: null, //生产问题导出
    thirdLevelGroups: null, // 当前用户的第三层级组及其子组
    productionTable: {
      total: 0,
      list: []
    },
    releaseVersionComponent: [],
    releaseVersionArchetype: [],
    versionComponent: '',
    versionArchetype: '',
    productionChart: {
      xAxis: []
    },
    issueDetail: {},
    selectedGourpList: [], // 已选择的组
    demandFilterData: '' // 需求筛选条件
  },

  getters: {
    runtime_env: state => {
      return state.type === 'gray' ? state.runtime_arr : state.runtime_arr;
    },
    changesRecordSort: state => {
      return state.changesRecord.sort((a, b) => {
        const aDate = moment(a.date).format('YYYYMMDD');
        const bDate = moment(b.date).format('YYYYMMDD');
        return aDate - bDate;
      });
    },
    releaseNoteSort: state => {
      return state.releaseNoteList.sort((a, b) => {
        const aDate = moment(a.date).format('YYYYMMDD');
        const bDate = moment(b.date).format('YYYYMMDD');
        return aDate - bDate;
      });
    },
    /*
     *  投产大窗口权限：
     *  当前窗口的(牵头联系人 || 卡点管理员)，必须不是特殊小组；
     *  当 isReleaseContact 有牵头联系人传参时使用传参，没有就使用
     *  投产大窗口详情 bigReleaseDetail 里的牵头联系人.
     */
    isReleaseContact: (
      { bigReleaseDetail },
      getters,
      rootState,
      rootGetter
    ) => releaseContact => {
      const isKaDianManager = rootGetter['user/isKaDianManager'];
      const { currentUser } = rootState['user'];
      const release_contact = releaseContact
        ? releaseContact
        : bigReleaseDetail.release_contact;
      const isReleaseContact = release_contact
        ? release_contact.some(user => {
            return user.id === currentUser.id;
          })
        : false;

      return isKaDianManager || isReleaseContact;
    },
    /*
     *  投产窗口权限：
     *  当前窗口的(投产负责人 || 投产管理员 || 卡点管理员)；
     *  传参时 投产管理员。
     */
    releaseRole: (state, getters, rootState, rootGetter) => releaseManage => {
      let isReleaseWindowManager = false;
      const isKaDianManager = rootGetter['user/isKaDianManager'];
      const { currentUser } = rootState['user'];
      const isReleaseManager = currentUser.role.some(role => {
        return role.name === '投产管理员';
      });
      if (releaseManage) {
        isReleaseWindowManager = releaseManage.some(
          user => user.id === currentUser.id
        );
      }

      return isReleaseWindowManager || isKaDianManager || isReleaseManager;
    },
    existingDate: ({ bigRelease }) => {
      return bigRelease.map(item => {
        return item.release_date;
      });
    }
  },

  actions: {
    async exportSpecialRqrmntInfoList({ commit }, data) {
      const response = await resolveResponseError(() =>
        exportSpecialRqrmntInfoList(data)
      );
      exportExcel(response, 'relaseSpecialRqrmntList.xls');
    },
    async queryDeAutoAssets({ commit }, data) {
      const respone = await resolveResponseError(() => queryDeAutoAssets(data));
      commit('saveDeAutoAssets', respone);
    },
    async deAutoUpload({ commit }, data) {
      await resolveResponseError(() => deAutoUpload(data));
    },
    async queryPackageTags({ commit }, data) {
      const respone = await resolveResponseError(() => queryPackageTags(data));
      commit('savePackageTags', respone);
    },
    async setPackageTag({ commit }, data) {
      await resolveResponseError(() => setPackageTag(data));
    },
    //通过需求id查询需求列表
    async queryRqrmntInfoTasks({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryRqrmntInfoTasks(data)
      );
      commit('saveQueryRqrmntInfoTasks', respone);
    },
    // 获取变更模板列表
    async getTemplate({ commit }, params) {
      try {
        const respone = await resolveResponseError(() => templateQuery(params));
        commit('saveTemplate', respone);
      } catch (err) {
        commit('saveTemplate', []);
      }
    },
    // 获取变更模板列表详情
    async getTemplateDetail({ commit }, data) {
      commit('saveTemplateDetail', []);
      const respone = await resolveResponseError(() =>
        queryTemplateDetail(data)
      );
      commit('saveTemplateDetail', respone);
    },
    // 发布模板设置
    async changeName({ commit }, payload) {
      await resolveResponseError(() => setTemplate(payload));
    },
    async updateNoteBatchNo({ commit }, payload) {
      await resolveResponseError(() => updateNoteBatchNo(payload));
    },
    async deleteNote({ commit }, payload) {
      await resolveResponseError(() => deleteNote(payload));
    },
    async packageFromTag({ commit }, payload) {
      await resolveResponseError(() => packageFromTag(payload));
    },
    async queryReleaseVersionComponent({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryReleaseVersionComponent(data)
      );
      commit('saveReleaseVersionComponent', respone);
    },
    async queryReleaseVersionArchetype({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryReleaseVersionArchetype(data)
      );
      commit('saveReleaseVersionArchetype', respone);
    },
    async queryLatestVersionComponent({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryLatestVersionComponent(data)
      );
      commit('saveVersionComponent', respone);
    },
    async queryLatestVersionArchetype({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryLatestVersionArchetype(data)
      );
      commit('saveVersionArchetype', respone);
    },
    async relDevopsComponent({ commit }, payload) {
      await resolveResponseError(() => relDevopsComponent(payload));
    },
    async relDevopsArchetype({ commit }, payload) {
      await resolveResponseError(() => relDevopsArchetype(payload));
    },
    async relDevopsBaseImage({ commit }, payload) {
      await resolveResponseError(() => relDevopsBaseImage(payload));
    },
    async judgeTargetVersionComponent({ commit }, payload) {
      await resolveResponseError(() => judgeTargetVersionComponent(payload));
    },
    async relDevops({ commit }, payload) {
      await resolveResponseError(() => relDevops(payload));
    },
    // 变更审核
    async auditChanges({ commit }, data) {
      await resolveResponseError(() => audit(data));
    },
    // 编辑变更模板
    async updateChangeTemplate({ commit }, data) {
      await resolveResponseError(() => updateTemplate(data));
    },
    // 手动创建发布说明
    async createManualNote({ commit }, data) {
      await resolveResponseError(() => createManualNote(data));
    },
    // 添加手动发布说明文件
    async addManualNoteInfo({ commit }, data) {
      await resolveResponseError(() => addManualNoteInfo(data));
    },
    // 查询手动发布说明文件
    async queryManualNoteInfo({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryManualNoteInfo(data)
      );
      commit('saveManualNoteInfo', respone);
    },
    // 更新手动发布说明文件
    async updateManualNoteInfo({ commit }, data) {
      await resolveResponseError(() => updateManualNoteInfo(data));
    },
    // 新建变更模板
    async createATemplate({ commit }, data) {
      await resolveResponseError(() => createATemplate(data));
    },
    // 新建发布说明
    async createNote({ commit }, data) {
      await resolveResponseError(() => createNote(data));
    },
    // 查询发布说明列表
    async queryReleaseNote({ commit }, data) {
      const respone = await resolveResponseError(() => queryReleaseNote(data));
      commit('saveReleaseNoteList', respone);
    },
    // 新增发布说明应用
    async addNoteService({ commit }, data) {
      await resolveResponseError(() => addNoteService(data));
    },
    // 查询发布说明应用列表
    async queryNoteService({ commit }, data) {
      const respone = await resolveResponseError(() => queryNoteService(data));
      commit('saveReleaseNoteAppList', respone);
    },
    // 删除发布说明应用
    async deleteNoteService({ commit }, data) {
      await resolveResponseError(() => deleteNoteService(data));
    },
    // 获取发布说明下的应用id
    async queryBatchAppIdByNoteId({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryBatchAppIdByNoteId(data)
      );
      commit('saveBatchAppIdByNoteId', respone);
    },
    //编辑发布说明应用
    async updateNoteService({ commit }, data) {
      await resolveResponseError(() => updateNoteService(data));
    },
    async lockNote({ commit }, data) {
      await resolveResponseError(() => lockNote(data));
    },
    // 查询小组标识
    async queryAllGroupAbbr({ commit }, data) {
      const respone = await resolveResponseError(() => queryAllGroupAbbr(data));
      commit('saveAllGroupAbbr', respone);
    },
    // 查询发布说明详情
    async queryNoteDetail({ commit }, data) {
      const respone = await resolveResponseError(() => queryNoteDetail(data));
      commit('saveReleaseNoteDetail', respone);
    },
    async addNoteSql({ commit }, data) {
      await resolveResponseError(() => addNoteSql(data));
    },
    async generateReleaseNotes({ commit }, data) {
      await resolveResponseError(() => generateReleaseNotes(data));
    },
    async deleteNoteSql({ commit }, data) {
      await resolveResponseError(() => deleteNoteSql(data));
    },
    async updateNoteSeqNo({ commit }, data) {
      await resolveResponseError(() => updateNoteSeqNo(data));
    },
    async queryNoteSql({ commit }, data) {
      const respone = await resolveResponseError(() => queryNoteSql(data));
      commit('saveNoteSql', respone);
    },
    // 更新发布说明配置文件
    async updateNoteConfiguration({ commit }, data) {
      await resolveResponseError(() => updateNoteConfiguration(data));
    },
    // 删除发布说明配置文件
    async deleteNoteConfiguration({ commit }, data) {
      await resolveResponseError(() => deleteNoteConfiguration(data));
    },
    // 添加发布说明配置文件
    async addNoteConfiguration({ commit }, data) {
      await resolveResponseError(() => addNoteConfiguration(data));
    },
    // 查询发布说明配置文件
    async queryNoteConfiguration({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryNoteConfiguration(data)
      );
      commit('saveNoteConfiguration', respone);
    },
    // 新增批量任务
    async createBatchTask({ commit }, data) {
      await resolveResponseError(() => createBatchTask(data));
    },
    // 编辑批量任务
    async updateBatchTask({ commit }, data) {
      await resolveResponseError(() => updateBatchTask(data));
    },
    // 删除批量任务
    async deteleBatchTask({ commit }, data) {
      await resolveResponseError(() => deteleBatchTask(data));
    },
    // 获取批量任务
    async queryBatchTask({ commit }, data) {
      const respone = await resolveResponseError(() => queryBatchTask(data));
      commit('saveBatchTask', respone);
    },
    // 添加批量任务
    async addBatchTask({ commit }, data) {
      await resolveResponseError(() => addBatchTask(data));
    },
    // 查询批量任务列表
    async queryBatchTaskList({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryBatchTaskList(data)
      );
      commit('saveBatchTaskList', respone);
    },
    // 新建变更
    async createAChanges({ commit }, data) {
      await resolveResponseError(() => createChanges(data));
    },
    // 查询变更应用列表
    async getChangeApplications({ commit }, data) {
      const respone = await resolveResponseError(() => queryApplications(data));
      commit('saveChangeApplications', respone);
    },
    // 查询变更记录
    async getChangesRecord({ commit }, data) {
      let respone = [];
      resolveResponseError(() => queryRecord(data))
        .then(res => {
          respone = res;
          commit('saveChangesRecord', respone);
        })
        .catch(err => {
          commit('saveChangesRecord', []);
        });
    },
    // 查询变更文件列表
    async getAssetsList({ commit }, data) {
      commit('saveAssetsList', []);
      const respone = await resolveResponseError(() => queryAssetsList(data));
      commit('saveAssetsList', respone);
    },
    // 删除变更文件
    async deleteFile({ commit }, data) {
      await resolveResponseError(() => deleteAsset(data));
    },
    async deleteFileArr({ commit }, data) {
      await resolveResponseError(() => deleteAssetArr(data));
    },
    // 删除变更模板文件
    async deleteTemplate({ commit }, data) {
      await resolveResponseError(() => deleteTemplate(data));
    },
    // 删除变更模板文件
    async deleteApplication({ commit }, data) {
      await resolveResponseError(() => deleteApplication(data));
    },
    // 获取用户
    async getUser({ commit }, data) {
      const respone = await resolveResponseError(() => queryGroupPeople(data));
      commit('saveUser', respone);
    },
    // 获取变更目录
    async getCatalog({ commit }, data) {
      const respone = await resolveResponseError(() => catalogoptions(data));
      commit('saveCatalog', respone);
    },
    // 变更模板弹窗回显
    async querySysRlsInfo({ commit }, data) {
      const respone = await resolveResponseError(() => querySysRlsInfo(data));
      commit('saveSysRlsInfo', respone);
    },
    // 添加gitlab文件
    async addFile({ commit }, data) {
      await resolveResponseError(() => addGitlabAsset(data));
    },
    // 移动gitlab文件
    async moveOrder({ commit }, data) {
      await resolveResponseError(() => moveOrder(data));
    },
    // 查询镜像标签
    async queryImageTags({ commit }, data) {
      const respone = await resolveResponseError(() => queryImageTags(data));
      commit('saveImageTags', respone);
    },
    async queryNodeName({ commit }, data) {
      const respone = await resolveResponseError(() => getNodeName(data));
      commit('saveNodeName', respone);
    },
    async queryProdInfo({ commit }, data) {
      const respone = await resolveResponseError(() => queryProdInfo(data));
      commit('saveProdInfo', respone);
    },
    // 选择镜像标签
    async setImageTag({ commit }, data) {
      const respone = await resolveResponseError(() => setImageTag(data));
      commit('saveImageTagResult', respone);
    },
    async setTestEnvs({ commit }, data) {
      await resolveResponseError(() => setTestEnvs(data));
    },
    async create({ commit }, data) {
      await resolveResponseError(() => create(data));
    },
    async update({ commit }, data) {
      await resolveResponseError(() => update(data));
    },
    // 查询投产窗口该应用所有预设版本号
    async queryTargetVersion({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryTargetVersion(data)
      );
      commit('saveQueryTargetVersion', respone);
    },
    // 查询gitlab分支
    async queryResourceBranches({ commit }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryResourceBranches(data)
        );
        commit('saveResourceBranches', respone);
      } catch (err) {
        commit('saveResourceBranches', []);
      }
    },
    // 查询gitlab文件
    async queryResourceFiles({ commit }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryResourceFiles(data)
        );
        commit('saveResourceFiles', respone);
      } catch (err) {
        commit('saveResourceFiles', []);
      }
    },
    // 获取配置文件
    async queryProfile({ commit }, data) {
      const respone = await resolveResponseError(() => queryProfile(data));
      commit('saveProfile', respone);
    },
    async queryTrace({ commit }, data) {
      try {
        const respone = await resolveResponseError(() => trace(data));
        commit('saveTrace', respone);
      } catch (err) {
        commit('saveTrace', []);
      }
    },
    async queryRelease({ commit }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryReleaseNodes(data)
        );
        commit('saveRelease', respone);
      } catch (err) {
        commit('saveRelease', []);
      }
    },
    async queryReleaseNodeDetail({ commit }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryReleaseNodeDetail(data)
        );
        commit('saveReleaseDetail', respone);
      } catch (err) {
        commit('saveReleaseDetail', []);
      }
    },
    async queryReleaseNodeByJob({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryReleaseNodeByJob(payload)
      );
      commit('saveReleaseNode', response);
    },
    async addJobReleaseNode({ commit }, payload) {
      const response = await resolveResponseError(() =>
        addJobReleaseNode(payload)
      );
      commit('saveJobReleaseNode', response);
    },
    async queryEnv({ commit }, data) {
      try {
        const respone = await resolveResponseError(() => queryEnv(data));
        commit('saveEnvType', respone);
      } catch (err) {
        commit('saveEnvType', []);
      }
    },
    async queryTasks({ commit }, data) {
      try {
        const respone = await resolveResponseError(() => queryTasks(data));
        commit('saveTasks', respone);
      } catch (err) {
        commit('saveTasks', []);
      }
    },
    async queryTaskReview({ commit }, data) {
      try {
        const respone = await resolveResponseError(() => queryTaskReview(data));
        commit('saveTaskReview', respone);
      } catch (err) {
        commit('saveTaskReview', []);
      }
    },
    async queryApply({ commit }, data) {
      try {
        const respone = await resolveResponseError(() => queryApply(data));
        commit('saveApplyList', respone);
      } catch (err) {
        commit('saveApplyList', []);
      }
    },
    async queryComponent({ commit }, data) {
      try {
        const respone = await resolveResponseError(() => queryComponent(data));
        commit('saveComponentList', respone);
      } catch (err) {
        commit('saveComponentList', []);
      }
    },
    async queryAppWithOutSum({ commit }, data) {
      try {
        const respone = await resolveResponseError(() =>
          queryAppWithOutSum(data)
        );
        commit('saveAppWithOutSum', respone);
      } catch (err) {
        commit('saveAppWithOutSum', []);
      }
    },
    async queryPlan({ commit }, data) {
      try {
        const respone = await resolveResponseError(() => queryPlan(data));
        commit('saveChangesRecord', respone);
      } catch (err) {
        commit('saveChangesRecord', []);
      }
    },
    async updateGroupAbbr({ commit }, data) {
      await resolveResponseError(() => updateGroupAbbr(data));
    },
    /* 生成模板实例 */
    async generateProdExcel({ commit }, data) {
      const response = await resolveResponseError(() =>
        generateProdExcel(data)
      );
      commit('saveFile', response);
    },
    /* 修改系统标识 */
    async updateSysRlsInfo({ commit }, data) {
      await resolveResponseError(() => updateSysRlsInfo(data));
    },
    async queryReleaseNode({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryReleaseNode(payload)
      );
      commit('saveReleaseNodes', response);
    },
    async queryVersion({ commit }, payload) {
      const response = await resolveResponseError(() => queryVersion(payload));
      commit('saveVersion', response);
    },
    /* 任务解挂 */
    async deleteTask({ commit }, data) {
      await resolveResponseError(() => deleteTask(data));
    },
    async changeReleaseNode({ commit }, data) {
      await resolveResponseError(() => changeReleaseNode(data));
    },
    /* 确认已投产 */
    async updateTaskArchived({ commit }, data) {
      await resolveResponseError(() => updateTaskArchived(data));
    },
    async auditAdd({ commit }, data) {
      await resolveResponseError(() => auditAdd(data));
    },
    async updateTaskReview({ commit }, data) {
      await resolveResponseError(() => updateTaskReview(data));
    },
    async changesUpdate({ commit }, data) {
      await resolveResponseError(() => changesUpdate(data));
    },
    /* 删除变更 */
    async deleteRelease({ commit }, data) {
      const response = await resolveResponseError(() => deleteRelease(data));
      commit('saveDeleteRelease', response);
    },
    async sendEmailForTaskManagers({ commit }, data) {
      await resolveResponseError(() => sendEmailForTaskManagers(data));
    },
    /* 查询小组标识 */
    async queryGroupAbbr({ commit }, data) {
      const response = await resolveResponseError(() => queryGroupAbbr(data));
      commit('saveGroupAbbr', response);
    },
    async queryResourceProjects({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryResourceProjects(data)
      );
      commit('saveResourceProjects', response);
    },
    async queryTasksReviews({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryTasksReviews(data)
      );
      commit('saveExamineList', response);
    },
    async queryApplicationTags({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryApplicationTags(data)
      );
      commit('saveTagsList', response);
    },
    async queryAppScale({ commit }, data) {
      const response = await resolveResponseError(() => queryAppScale(data));
      commit('saveAppScale', response);
    },
    async addAppScale({ commit }, data) {
      await resolveResponseError(() => addAppScale(data));
    },
    async updateAppScale({ commit }, data) {
      await resolveResponseError(() => updateAppScale(data));
    },
    async deleteAppScale({ commit }, data) {
      await resolveResponseError(() => deleteAppScale(data));
    },
    async queryAllProdAssets({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryAllProdAssets(data)
      );
      commit('saveAllProdAssets', response);
    },
    async queryDeAutoAllProdAssets({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryDeAutoAllProdAssets(data)
      );
      commit('saveDeAutoAllProdAssets', response);
    },
    async exportProdDirection({ commit }, data) {
      const response = await resolveResponseError(() =>
        exportProdDirection(data)
      );
      commit('saveProdDirection', response);
    },
    async tasksInSitStage({ commit }, data) {
      const response = await resolveResponseError(() => tasksInSitStage(data));
      commit('saveTasksOfAppType', response);
    },
    async queryGroupSysAbbr({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryGroupSysAbbr(data)
      );
      commit('saveGroupSysAbbr', response);
    },
    async updateGroupSysAbbr({ commit }, data) {
      await resolveResponseError(() => updateGroupSysAbbr(data));
    },
    async queryDetailByTaskId({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryDetailByTaskId(data)
      );
      const time = response.release_node_name
        ? response.release_node_name.substring(
            0,
            response.release_node_name.indexOf('_')
          )
        : null;
      const { release_node_name } = response;
      commit('saveReleaseNodeName', release_node_name);
      commit('saveReleaseTime', time);
    },
    async queryModuleType({ commit }, data) {
      const response = await resolveResponseError(() => queryModuleType(data));
      commit('saveModuleType', response);
    },
    async deleteModuleType({ commit }, data) {
      await resolveResponseError(() => deleteModuleType(data));
    },
    async addModuleType({ commit }, data) {
      await resolveResponseError(() => addModuleType(data));
    },
    async updateModuleType({ commit }, data) {
      await resolveResponseError(() => updateModuleType(data));
    },
    async queryScript({ commit }, data) {
      const response = await resolveResponseError(() => queryScript(data));
      commit('saveScriptParams', response);
    },
    async deleteScript({ commit }, data) {
      await resolveResponseError(() => deleteScript(data));
    },
    async addScript({ commit }, data) {
      await resolveResponseError(() => addScript(data));
    },
    async updateScript({ commit }, data) {
      await resolveResponseError(() => updateScript(data));
    },
    async queryAutomationEnv({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryAutomationEnv(data)
      );
      commit('saveAutomationEnv', response);
    },
    async deleteAutomationEnv({ commit }, data) {
      await resolveResponseError(() => deleteAutomationEnv(data));
    },
    async addAutomationEnv({ commit }, data) {
      await resolveResponseError(() => addAutomationEnv(data));
    },
    async updateAutomationEnv({ commit }, data) {
      await resolveResponseError(() => updateAutomationEnv(data));
    },
    async queryDBAssets({ commit }, data) {
      const response = await resolveResponseError(() => queryDBAssets(data));
      commit('saveDBAssets', response);
    },
    async createTestrunBranch({ commit }, data) {
      const response = await resolveResponseError(() =>
        createTestrunBranch(data)
      );
      commit('saveTestRunBranch', response);
    },
    async mergeTaskBranch({ commit }, data) {
      await resolveResponseError(() => mergeTaskBranch(data));
    },
    async iOSOrAndroidAppPublish({ commit }, data) {
      await resolveResponseError(() => iOSOrAndroidAppPublish(data));
    },
    async queryNotinlineTasksByAppId({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryNotinlineTasksByAppId(data)
      );
      commit('saveAppTaskList', response);
    },
    async queryTaskByTestRunId({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryTaskByTestRunId(data)
      );
      commit('saveTaskByTestRunId', response);
    },
    async findByProdId({ commit }, data) {
      const response = await resolveResponseError(() => findByProdId(data));
      commit('saveAssetsCatalog', response);
    },
    // 判断是否是新版
    async queryChangesDetail({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryChangesDetail(data)
      );
      commit('saveChangesDetail', respone);
    },
    async changeConfirmConf({ commit }, data) {
      await resolveResponseError(() => changeConfirmConf(data));
    },
    async changeReleaseConf({ commit }, data) {
      await resolveResponseError(() => changeReleaseConf(data));
    },
    async queryReviewRecord({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryReviewRecord(data)
      );
      commit('saveReviewRecord', response);
    },
    async updateReviewRecord({ commit }, data) {
      await resolveResponseError(() => updateReviewRecord(data));
    },
    async queryByLabelsFuzzy({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryByLabelsFuzzy(data)
      );
      commit('saveLabelsFuzzy', response);
    },
    async queryReviewBasicMsg({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryReviewBasicMsg(data)
      );
      commit('saveReviewBasicMsg', response);
    },
    // 新增/修改审核记录
    async addReview({ commit }, data) {
      await resolveResponseError(() => addReview(data));
    },
    async queryByReleaseNodeName({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryByReleaseNodeName(data)
      );
      commit('saveReleaseFiles', response);
    },
    async queryProdDir({ commit }, data) {
      const response = await resolveResponseError(() => queryProdDir(data));
      commit('saveProdDir', response);
    },
    async updateProdDir({ commit }, data) {
      await resolveResponseError(() => updateProdDir(data));
    },
    async updateProdDeploy({ commit }, data) {
      await resolveResponseError(() => updateProdDeploy(data));
    },
    async deleteRequireFile({ commit }, data) {
      await resolveResponseError(() => deleteFile(data));
    },
    async taskChangeNotise({ commit }, data) {
      await resolveResponseError(() => taskChangeNotise(data));
    },
    async downloadFiles({ commit }, data) {
      const response = await resolveResponseError(() => downloadFiles(data));
      commit('saveDownloadFiles', response);
    },
    async addSystemTestFile({ commit }, data) {
      await resolveResponseError(() => addSystemTestFile(data));
    },
    async addSourceReview({ commit }, data) {
      await resolveResponseError(() => addSourceReview(data));
    },
    async querySourceReviewDetail({ commit }, data) {
      const response = await resolveResponseError(() =>
        querySourceReviewDetail(data)
      );
      commit('saveSourceReviewDetail', response);
    },
    async pullGrayBranch({ commit }, data) {
      const response = await resolveResponseError(() => pullGrayBranch(data));
      commit('saveReturnMsg', response);
    },
    async queryExcelTemplate({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryExcelTemplate(data)
      );
      commit('saveExcelTemplate', response);
    },
    async queryTemplateSystem({ commit }, data) {
      const res = await resolveResponseError(() => querySystem(data));
      commit('saveTemplateSystem', res);
    },
    async editFakeInfo({ commit }, data) {
      const res = await resolveResponseError(() => editFakeInfo(data));
      commit('saveFakeInfo', res);
    },
    async queryBigReleaseNodes({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryBigReleaseNodes(data)
      );
      commit('saveBigReleaseNodes', response);
    },
    async queryBigReleaseNodeDetail({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryBigReleaseNodeDetail(data)
      );
      commit('saveBigReleaseNodeDetail', response);
    },
    async queryContactInfo({ commit }, data) {
      const response = await resolveResponseError(() => queryContactInfo(data));
      commit('saveContactInfo', response);
    },
    async queryRqrmntInfoList({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryRqrmntInfoList(data)
      );
      commit('saveRqrmntInfoList', response);
    },
    async createBigReleaseNode({ commit }, data) {
      await resolveResponseError(() => createBigReleaseNode(data));
    },
    async updateBigReleasenode({ commit }, data) {
      await resolveResponseError(() => updateBigReleasenode(data));
    },
    async updateBigReleaseDate({ commit }, data) {
      await resolveResponseError(() => updateBigReleaseDate(data));
    },
    async addBatch({ commit }, data) {
      await resolveResponseError(() => addBatch(data));
    },
    async updateRqrmntInfo({ commit }, data) {
      const response = await resolveResponseError(() => updateRqrmntInfo(data));
      exportExcel(response);
    },
    async exportRqrmntInfoList({ commit }, data) {
      const response = await resolveResponseError(() =>
        exportRqrmntInfoList(data)
      );
      exportExcel(response, 'relaseRqrmntList.xls');
    },
    async exportRqrmntInfoListByType({ commit }, data) {
      const response = await resolveResponseError(() =>
        exportRqrmntInfoListByType(data)
      );
      exportExcel(response, 'relaseRqrmntList.xlsx');
    },
    async queryByReleaseDate({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryByReleaseDate(data)
      );
      commit('saveReleaseDate', response);
    },
    async queryProWantTasks({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryProWantTasks(data)
      );
      commit('saveProWantTasks', response);
    },
    async queryBatchInfoByTaskId({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryBatchInfoByTaskId(data)
      );
      commit('saveAppBatchInfo', response);
    },
    async queryRqrmntInfoListByType({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryRqrmntInfoListByType(data)
      );
      if (data.type === '1') {
        commit('saveTestInfoList', response);
      } else {
        commit('saveSecurityInfoList', response);
      }
    },
    async preview({ commit }, data) {
      const response = await resolveResponseError(() => preview(data));
      const { url } = response;
      commit('saveWpsUrl', url);
    },
    async queryBatchInfoByAppId({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryBatchInfoByAppId(data)
      );
      commit('saveBatchInfoByAppId', response);
    },
    async queryReleaseSystem({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryReleaseSystem(data)
      );
      commit('saveReleaseSystem', response);
    },
    async queryConfigApplication({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryConfigApplication(data)
      );
      commit('saveConfigApplicationList', response);
    },
    async addConfigApplication({ commit }, data) {
      await resolveResponseError(() => addConfigApplication(data));
    },
    async checkConfigApplication({ commit }, data) {
      await resolveResponseError(() => checkConfigApplication(data));
    },
    async createConfig({ commit }, data) {
      const response = await resolveResponseError(() => createConfig(data));
      commit('saveErrorConfigList', response);
    },
    async deleteConfig({ commit }, data) {
      await resolveResponseError(() => deleteConfig(data));
    },
    async queryConfigSum({ commit }, data) {
      const response = await resolveResponseError(() => queryConfigSum(data));
      commit('saveConfigSum', response);
    },
    async confirmChanges({ commit }, data) {
      await resolveResponseError(() => confirmChanges(data));
    },
    // 删除投产窗口
    async deleteReleaseNode({ commit }, data) {
      await resolveResponseError(() => deleteReleaseNode(data));
    },
    async querySystemDetailByProdId({ commit }, data) {
      const response = await resolveResponseError(() =>
        querySystemDetailByProdId(data)
      );
      commit('saveSystemDetailByProdId', response);
    },
    async queryDbPath({ commit }, data) {
      const response = await resolveResponseError(() => queryDbPath(data));
      commit('saveDbPath', response);
    },
    async uploadAssets({ commit }, data) {
      await resolveResponseError(() => uploadAssets(data));
    },
    async queryDeployTypeByAppId({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryDeployTypeByAppId(data)
      );
      commit('saveDeployTypeByAppId', respone);
    },
    async queryEnvList({ commit }, data) {
      const respone = await resolveResponseError(() => queryEnvList(data));
      commit('saveEnvList', respone);
    },
    async updateAwsAssetGroupId({ commit }, data) {
      await resolveResponseError(() => updateAwsAssetGroupId(data));
    },
    async queryAwsGroups({ commit }, data) {
      const respone = await resolveResponseError(() => queryAwsGroups(data));
      commit('saveAwsGroups', respone);
    },
    async addRedLineScanReport({ commit }, data) {
      await resolveResponseError(() => addRedLineScanReport(data));
    },
    async whetherWriteOrder({ commit }, data) {
      await resolveResponseError(() => whetherWriteOrder(data));
    },
    async queryReplicasnu({ commit }, data) {
      const respone = await resolveResponseError(() => queryReplicasnu(data));
      commit('saveReplicasNum', respone);
    },
    async updateReplicasnu({ commit }, data) {
      await resolveResponseError(() => updateReplicasnu(data));
    },
    async addEsfRegistration({ commit }, data) {
      await resolveResponseError(() => addEsfRegistration(data));
    },
    async delEsf({ commit }, data) {
      await resolveResponseError(() => delEsf(data));
    },
    async updateEsf({ commit }, data) {
      await resolveResponseError(() => updateEsf(data));
    },
    async queryEsfConfiguration({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryEsfConfiguration(data)
      );
      commit('saveEsfConfigData', respone);
    },
    async addEsfCommonConfig({ commit }, data) {
      await resolveResponseError(() => addEsfCommonConfig(data));
    },
    async updateEsfcommonconfigAssets({ commit }, data) {
      await resolveResponseError(() => updateEsfcommonconfigAssets(data));
    },
    async queryAppStatus({ commit }, data) {
      const respone = await resolveResponseError(() => queryAppStatus(data));
      commit('saveAppIsPublished', respone);
    },
    async queryAppDeployPlatformInfo({ commit }, data) {
      const respone = await resolveResponseError(() =>
        queryAppDeployPlatformInfo(data)
      );
      commit('saveAppDeployPlatformInfo', respone);
    },
    async exportProIssues({ commit, state }, data) {
      const response = await resolveResponseError(() => exportProIssues(data));
      commit('saveExport', response);
    },
    async queryProductionTable({ commit, state }, data) {
      const response = await Promise.all([
        queryProIssues(data),
        countProIssues(data)
      ]);
      commit('saveProductionTable', response);
    },
    async queryProductionChart({ commit, state }, data) {
      const response = await resolveResponseError(() => queryProByTeam(data));
      commit('saveProductionChart', response);
    },
    async queryIssueDetail({ commit, state }, data) {
      const response = await resolveResponseError(() => queryIssueDetail(data));
      commit('saveIssueDetail', response);
    },
    async addChangeApplication({ commit }, data) {
      await resolveResponseError(() => addChangeApplication(data));
    },
    async queryAppsByAddEsf({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryAppsByAddEsf(data)
      );
      commit('saveAppsByAddEsf', response);
    },
    // 查询用户所属第三层级组及子组
    async queryThreeLevelGroups({ commit }, data) {
      const response = await resolveResponseError(() =>
        queryThreeLevelGroups(data)
      );
      commit('saveThirdLevelGroups', response);
    }
  },

  mutations: {
    saveDeleteRelease(state, data) {
      state.deleteReleaseInfo = data;
    },
    saveDeAutoAssets(state, data) {
      state.deAutoAssets = data;
    },
    savePackageTags(state, data) {
      state.packageTags = data;
    },
    saveQueryRqrmntInfoTasks(state, data) {
      state.queryRqrmntInfo = data;
    },
    saveTemplate(state, data) {
      state.templateOfChanges = data;
    },
    saveChangeApplications(state, data) {
      state.changeApllication = data;
    },
    saveBatchTaskList(state, data) {
      state.batchTaskList = data;
    },
    saveBatchTask(state, data) {
      state.batchTask = data;
    },
    saveNoteConfiguration(state, data) {
      state.noteConfiguration = data;
    },
    saveAllGroupAbbr(state, data) {
      state.allGroupAbbrList = data;
    },
    saveReleaseNoteDetail(state, data) {
      state.releaseNoteDetail = data;
    },
    saveNoteSql(state, data) {
      state.noteSqlList = data;
    },
    saveReleaseNoteList(state, data) {
      state.releaseNoteList = data;
    },
    saveManualNoteInfo(state, data) {
      state.manualNoteInfo = data;
    },
    saveBatchAppIdByNoteId(state, data) {
      state.batchAppIdByNoteIdList = data;
    },
    saveReleaseNoteAppList(state, data) {
      state.releaseNoteAppList = data;
    },
    saveChangesRecord(state, data) {
      state.changesRecord = data;
    },
    saveReleaseVersionComponent(state, data) {
      state.releaseVersionComponent = data;
    },
    saveReleaseVersionArchetype(state, data) {
      state.releaseVersionArchetype = data;
    },
    saveVersionComponent(state, data) {
      state.versionComponent = data;
    },
    saveVersionArchetype(state, data) {
      state.versionArchetype = data;
    },
    saveTemplateDetail(state, data) {
      state.templateDetail = data;
    },
    saveAssetsList(state, data) {
      state.assetsList = data;
    },
    saveUser(state, data) {
      state.user = data;
    },
    saveTagsList(state, data) {
      state.tagsList = data;
    },
    saveCatalog(state, data) {
      state.catalog = data;
    },
    saveSysRlsInfo(state, data) {
      state.sysRlsInfo = data;
    },
    saveResourceBranches(state, data) {
      state.branches = data;
    },
    saveRole(state, data) {
      state.role = data;
    },
    saveImageTags(state, data) {
      state.image_tags = data;
    },
    saveType(state, data) {
      state.type = data;
    },
    saveTrace(state, data) {
      state.traceInfo = data;
    },
    saveResourceFiles(state, data) {
      state.files = data;
      state.filesWithRuntime = data.map((item, index) => {
        return { value: item, runtime_env: [], show: false };
      });
    },
    saveProfile(state, data) {
      state.profile = data;
    },
    saveCompareTime(state, data) {
      state.compareTime = data;
    },
    saveReleaseDetail(state, data) {
      state.releaseDetail = data;
    },
    saveEnvType(state, data) {
      state.envType = data;
    },
    saveNodeName(state, data) {
      state.releaseNodeName = data;
    },
    saveReleaseNode(state, payload) {
      state.releaseNodeData = payload;
    },
    saveReleaseNodes(state, payload) {
      state.releaseNodeList = payload;
    },
    saveJobReleaseNode(state, payload) {
      state.jobReleaseNodeData = payload;
    },
    saveTasks(state, data) {
      state.taskList = data;
    },
    saveTaskReview(state, data) {
      state.taskReview = data;
    },
    saveApplyList(state, data) {
      state.applyList = data;
    },
    saveComponentList(state, data) {
      state.componentList = data;
    },
    saveQueryTargetVersion(state, data) {
      state.targetVersionList = data;
    },
    saveAppWithOutSum(state, data) {
      state.appWithOutSum = data.map(app => {
        return {
          ...app,
          id: app.application_id,
          name_en: app.app_name_en,
          name_zh: app.app_name_zh
        };
      });
    },
    saveProdInfo(state, data) {
      state.prodInfo = data;
    },
    saveVersion(state, data) {
      state.version = data;
    },
    saveGroupAbbr(state, data) {
      state.groupAbbr = data;
    },
    saveFile(state, data) {
      state.file = data;
    },
    saveResourceProjects(state, data) {
      state.resourceProjects = data;
    },
    saveExamineList(state, data) {
      state.examineList = data;
    },
    saveAppScale(state, data) {
      state.appScaleList = data;
    },
    saveRelease(state, data) {
      state.releaseList = data;
    },
    saveAllProdAssets(state, data) {
      const { application_tips, prod_assets } = data;
      state.prodAssets = prod_assets;
      state.applicationTips = application_tips || '';
    },
    saveDeAutoAllProdAssets(state, data) {
      const { application_tips, prod_assets } = data;
      state.deAutoprodAssets = prod_assets;
      state.deAutoapplicationTips = application_tips || '';
    },
    saveProdDirection(state, data) {
      state.prodDirection = data;
    },
    saveTasksOfAppType(state, data) {
      state.tasksOfAppType = data;
    },
    saveGroupSysAbbr(state, data) {
      state.groupSysAbbr = data;
    },
    saveImageTagResult(state, data) {
      state.imageTagResult = data;
    },
    saveReleaseTime(state, data) {
      const isOverdue = data ? !isValidReleaseDate(data, null, 1) : false;
      const confirmIsRelease = data
        ? !isValidReleaseDate(data, null, 0)
        : false;
      state.isOverdue = isOverdue;
      state.confirmIsRelease = confirmIsRelease;
    },
    saveModuleType(state, data) {
      state.moduleType = data;
    },
    saveScriptParams(state, data) {
      state.scriptParams = data;
    },
    saveAutomationEnv(state, data) {
      state.automationEnv = data;
    },
    saveDBAssets(state, data) {
      state.database = data;
    },
    saveTestRunBranch(state, data) {
      state.testRunBranch = data;
    },
    saveAppTaskList(state, data) {
      state.appTaskList = data;
    },
    saveTaskByTestRunId(state, data) {
      state.taskByTestRunId = data;
    },
    saveAssetsCatalog(state, data) {
      state.assetsCatalog = data.map(item => {
        return {
          ...item,
          children: item.directory_details
            ? item.directory_details.map(v => {
                return { directory: v };
              })
            : null
        };
      });
    },
    saveChangesDetail(state, data) {
      state.changesDetail = data;
    },
    saveReviewRecord(state, data) {
      state.reviewRecord = data;
    },
    saveLabelsFuzzy(state, data) {
      state.labelsFuzzy = data;
    },
    saveReviewBasicMsg(state, data) {
      state.reviewBasicMsg = data;
    },
    saveReleaseFiles(state, data) {
      state.releaseFiles = data;
    },
    saveProdDir(state, data) {
      state.prodDirList = data;
    },
    saveReleaseNodeName(state, data) {
      state.release_node_name = data;
    },
    saveDownloadFiles(state, data) {
      state.downFiles = data;
    },
    saveSourceReviewDetail(state, data) {
      state.sourceReviewDetail = data;
    },
    saveReturnMsg(state, data) {
      state.returnMsg = data;
    },
    saveExcelTemplate(state, data) {
      state.excelTemplate = data;
    },
    saveTemplateSystem(state, data) {
      state.templateSystem = data;
    },
    saveFakeInfo(state, data) {
      state.FakeInfoData = data;
    },
    saveBigReleaseNodes(state, data) {
      state.bigRelease = data;
    },
    saveBigReleaseNodeDetail(state, data) {
      state.bigReleaseDetail = data;
    },
    saveContactInfo(state, data) {
      state.contactInfo = data;
    },
    saveRqrmntInfoList(state, data) {
      state.rqrmntInfoList = data;
    },
    saveProWantTasks(state, data) {
      state.wantToReleaseTasks = data;
    },
    saveReleaseDate(state, data) {
      state.releaseDate = data;
    },
    saveAppBatchInfo(state, data) {
      state.appBatchInfo = data;
    },
    saveTestInfoList(state, data) {
      state.testInfoList = data;
    },
    saveSecurityInfoList(state, data) {
      state.securityInfoList = data;
    },
    saveWpsUrl(state, data) {
      state.wpsUrl = data;
    },
    saveBatchInfoByAppId(state, data) {
      state.appUserInfo = data;
    },
    saveReleaseSystem(state, data) {
      state.releaseSystemList = data;
    },
    saveConfigApplicationList(state, data) {
      state.configApplicationList = data;
    },
    saveConfigSum(state, data) {
      state.isConfigSum = data.isConfigSum;
    },
    saveSystemDetailByProdId(state, data) {
      state.systemDetailByProdId = data;
    },
    saveDbPath(state, data) {
      state.dbPath = data;
    },
    saveErrorConfigList(state, data) {
      state.errorConfigList = data;
    },
    saveHasKnown(state, data) {
      state.hasKnown = data;
    },
    saveDeployTypeByAppId(state, data) {
      state.deployTypeByAppId = data;
    },
    saveEnvList(state, data) {
      state.envList = data;
    },
    saveAwsGroups(state, data) {
      state.awsGroups = data;
    },
    saveReplicasNum(state, data) {
      state.replicasNum = data;
    },
    saveEsfConfigData(state, data) {
      state.esfConfigData = data;
    },
    saveAppIsPublished(state, data) {
      state.appPublishInfo = data;
    },
    saveAppDeployPlatformInfo(state, data) {
      state.appDeployPlatformInfo = data;
    },
    saveExport(state, data) {
      state.export = data;
    },
    saveProductionTable(state, data) {
      state.productionTable = {
        list: data[0],
        total: data[1]
      };
    },
    saveProductionChart(state, data) {
      const { xAxis, xyData, yAxis } = data;

      const series = xyData.map((item, index) => {
        return {
          data: item,
          type: 'bar',
          name: yAxis[index],
          stack: '总量',
          label: {
            normal: {
              show: true
            }
          }
        };
      });

      state.productionChart = {
        xAxis,
        series
      };
    },
    saveIssueDetail(state, data) {
      state.issueDetail = data;
    },
    saveAppsByAddEsf(state, data) {
      state.appsByAddEsf = data;
    },
    saveSelectedGourpList(state, data) {
      state.selectedGourpList = data;
    },
    saveDemandFilterData(state, data) {
      state.demandFilterData = data;
    },
    saveThirdLevelGroups(state, data) {
      state.thirdLevelGroups = data;
    }
  }
};
