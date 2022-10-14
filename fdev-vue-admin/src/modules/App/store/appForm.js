import {
  addApp,
  addNewApp,
  appUpdate,
  queryPipelines,
  queryType,
  queryApps,
  queryAppContinuous,
  queryAppContinuousUpdate,
  queryAppContinuousSave,
  queryAppContinuousFind,
  queryTraces,
  createPipelineSchedule,
  deleteApp,
  queryAbandonDetail,
  queryAutoTest,
  updateAutoTest,
  queryRunnerJobLog,
  queryRunnerJobLogDetail,
  retryAutoTest,
  queryMantis,
  queryMyApps,
  queryPagination,
  queryWithEnv,
  queryPipelinesList,
  queryPipelinesWithJobsPage,
  cancelVipDeploy,
  runPipeline,
  vipGetLog,
  getPipelineById,
  queryAppSystem,
  bindSystem,
  getGroupGit,
  getTestFlag
} from '@/services/app';
import {
  projectAnalyses,
  getProjectInfos,
  getAnalysesHistory,
  componentTree,
  scanningFeatureBranch,
  featureProjectAnalyses,
  getProjectFeatureInfo,
  featureComponentTree
} from '@/services/sonar';
import { queryAppTagPiplines, queryTestRunPiplines } from '@/services/release';
import {
  query,
  queryProject,
  getGroup,
  queryAllBranch,
  createPipeline,
  definedDeploy,
  saveByAsync,
  queryApplicationName
} from '@/services/application';
import { resolveResponseError } from '@/utils/utils';
import moment from 'moment';
import { rating, handleCoverRating } from '@/modules/App/utils/constants';
export default {
  namespaced: true,

  state: {
    testFlag: [],
    jobsInfo: [],
    appSystemList: [],
    vipGetLogDetail: {},
    runPipelineInfo: [],
    cancelVipDeployList: [],
    vipPipelinesList: [],
    withEnvAppData: [],
    appData: [],
    rowsNumber: 10,
    MyAppData: [],
    gitlabData: {},
    groupData: Boolean,
    existedAppData: {},
    newAppData: {},
    branchList: [],
    envList: [],
    env: {},
    applicationName: '',
    pipelinesList: [],
    pipelinesListPage: {},
    envListById: [],
    appTypeData: [],
    vueAppData: [],
    appContinuousTableData: [],
    groupGitList: [],
    appContinuous: {},
    envVar: {},
    envVarList: [],
    envInfoList: [],
    jobTraces: {},
    appTagPiplines: {},
    testRunPiplines: {},
    autoTestData: {},
    runnerJobLog: [],
    runnerJobLogDetail: {},
    mantisList: [],
    tags: [],
    sonarScan: {},
    sonarDetail: {},
    sonarChart: {},
    sonarTableData: [],
    sortBy: '',
    scanBranchSonarRes: ''
  },

  getters: {},

  actions: {
    async getPipelineById({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getPipelineById(payload)
      );
      commit('saveGetPipelineById', response);
    },
    async queryAppSystem({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAppSystem(payload)
      );
      commit('saveAppSystem', response);
    },
    async getTestFlag({ commit }, payload) {
      const response = await resolveResponseError(() => getTestFlag(payload));
      commit('saveTestFlag', response);
    },
    async bindSystem({ commit }, payload) {
      await resolveResponseError(() => bindSystem(payload));
    },
    //vip部署日志
    async vipGetLog({ commit }, payload) {
      const response = await resolveResponseError(() => vipGetLog(payload));
      commit('saveVipGetLog', response);
    },
    //部署
    async runPipeline({ commit }, payload) {
      const response = await resolveResponseError(() => runPipeline(payload));
      commit('saveRunPipeline', response);
    },
    //取消部署
    async cancelVipDeploy({ commit }, payload) {
      const response = await resolveResponseError(() =>
        cancelVipDeploy(payload)
      );
      commit('saveCancelVipDeploy', response);
    },
    //vip部署列表
    async queryPipelinesList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryPipelinesList(payload)
      );
      commit('saveQueryPipelinesList', response);
    },
    // 查询涉及环境部署的应用
    async queryWithEnv({ commit }, payload) {
      const response = await resolveResponseError(() => queryWithEnv(payload));
      commit('saveQueryWithEnv', response);
    },
    // 查询应用信息
    async queryApplication({ commit }, payload) {
      const response = await resolveResponseError(() => query(payload));
      commit('saveAppData', response);
    },
    async queryDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAbandonDetail(payload)
      );
      commit('saveAppData', [response]);
    },
    //分页查询
    async queryPagination({ commit, state }, payload) {
      let { sortBy, descending } = payload;
      const res = await resolveResponseError(() => queryPagination(payload));
      const rowsNumber = res.count;
      const appData = res.appEntity;
      let arr = appData.slice(0);
      arr = arr.sort((a, b) => {
        if (sortBy === 'group') {
          return descending
            ? a.group.name.localeCompare(b.group.name, 'zh')
            : b.group.name.localeCompare(a.group.name, 'zh');
        } else if (sortBy === 'name_en') {
          return descending
            ? a.name_en.localeCompare(b.name_en)
            : b.name_en.localeCompare(a.name_en);
        }
      });
      commit('saveAppData', arr);
      commit('saveRows', rowsNumber);
      //q-table组件有可能会清空sortBy，做一个缓存
      commit('saveSortBy', sortBy);
    },
    // 查询gitlab上的项目的信息
    async queryProject({ commit }, payload) {
      const response = await resolveResponseError(() => queryProject(payload));
      commit('saveGitlabData', response);
    },
    // 查询group
    async getGroup({ commit }, payload) {
      const response = await resolveResponseError(() => getGroup(payload));
      commit('saveGroup', response);
    },
    // 录入已有应用
    async addApp({ commit }, payload) {
      const response = await resolveResponseError(() => addApp(payload));
      commit('saveExistedApp', response);
    },
    // 录入新应用
    async addNewApp({ commit }, payload) {
      const response = await resolveResponseError(() => addNewApp(payload));
      commit('saveNewApp', response);
    },
    //更新应用
    async appUpdate({ commit }, payload) {
      await resolveResponseError(() => appUpdate(payload));
    },
    async queryAllBranch({ commit }, payload) {
      try {
        const response = await resolveResponseError(() =>
          queryAllBranch(payload)
        );
        commit('saveBranchList', response);
      } catch (err) {
        commit('saveBranchList', []);
      }
    },
    async createPipeline({ commit }, payload) {
      await resolveResponseError(() => createPipeline(payload));
    },
    // 自定义部署
    async definedDeploy({ commit }, payload) {
      await resolveResponseError(() => definedDeploy(payload));
    },
    // 轮询应用新增
    async saveByAsync({ commit }, payload) {
      const response = await resolveResponseError(() => saveByAsync(payload));
      commit('saveNewApp', response);
    },
    async queryPipelinesWithJobsPage({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryPipelinesWithJobsPage(payload)
      );
      commit('savePipelinesPage', response);
    },
    async queryPipelines({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryPipelines(payload)
      );
      commit('savePipelines', response);
    },
    async queryAppType({ commit }, payload) {
      const response = await resolveResponseError(() => queryType(payload));
      commit('saveAppType', response);
    },
    async queryApps({ commit }, payload) {
      const response = await resolveResponseError(() => queryApps(payload));
      commit('saveVueApp', response);
    },
    async queryApplicationName({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryApplicationName(payload)
      );
      commit('saveApplicationName', response);
    },
    // 查询gitlab组信息
    async getGroupGit({ commit }, payload) {
      const response = await resolveResponseError(() => getGroupGit(payload));
      commit('saveGroupGitList', response);
    },
    // 查询持续集成模块
    async queryAppContinuous({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAppContinuous(payload)
      );
      commit('saveAppContinuousList', response);
    },
    // 更新持续集成模块
    async queryAppContinuousUpdate({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAppContinuousUpdate(payload)
      );
      commit('saveAppContinuous', response);
    },
    // 保存持续集成模块
    async queryAppContinuousSave({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAppContinuousSave(payload)
      );
      commit('saveAppContinuous', response);
    },
    // 通过ID查询持续集成模块
    async queryAppContinuousFind({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAppContinuousFind(payload)
      );
      commit('saveAppContinuous', response);
    },
    // 查询job日志
    async queryTraces({ commit }, payload) {
      const response = await resolveResponseError(() => queryTraces(payload));
      commit('saveTraces', response);
    },
    async queryAppTagPiplines({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAppTagPiplines(payload)
      );
      commit('saveAppTagPiplines', response);
    },
    async queryTestRunPiplines({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryTestRunPiplines(payload)
      );
      commit('saveTestRunPiplines', response);
    },
    async createPipelineSchedule({ commit }, payload) {
      await resolveResponseError(() => createPipelineSchedule(payload));
    },
    async deleteApp({ commit }, payload) {
      await resolveResponseError(() => deleteApp(payload));
    },
    // 查询应用自动化测试信息
    async queryAutoTest({ commit }, payload) {
      const response = await resolveResponseError(() => queryAutoTest(payload));
      commit('saveAutoTest', response);
    },
    // 更新应用自动化测试信息
    async updateAutoTest({ commit }, payload) {
      await resolveResponseError(() => updateAutoTest(payload));
    },
    // 查询对接自动测试平台的日志
    async queryRunnerJobLog({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryRunnerJobLog(payload)
      );
      commit('saveRunnerJobLog', response);
    },
    async queryRunnerJobLogDetail({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryRunnerJobLogDetail(payload)
      );
      commit('saveRunnerJobLogDetail', response);
    },
    async retryAutoTest({ commit }, payload) {
      await resolveResponseError(() => retryAutoTest(payload));
    },
    async queryMantis({ commit }, payload) {
      const response = await resolveResponseError(() => queryMantis(payload));
      commit('saveMantisByAppId', response);
    },
    async projectAnalyses({ commit }, payload) {
      const response = await resolveResponseError(() =>
        projectAnalyses(payload)
      );
      commit('saveSonarScan', response);
    },
    async getProjectInfos({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getProjectInfos(payload)
      );
      commit('saveSonarDetail', response);
    },
    async getAnalysesHistory({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getAnalysesHistory(payload)
      );
      commit('saveSonarChart', response);
    },
    async componentTree({ commit }, payload) {
      const response = await resolveResponseError(() => componentTree(payload));
      commit('saveSonarTableData', response);
    },
    async queryMyApps({ commit }, payload) {
      const response = await resolveResponseError(() => queryMyApps(payload));
      commit('saveMyAppData', response);
    },
    async scanningFeatureBranch({ commit }, payload) {
      const response = await resolveResponseError(() =>
        scanningFeatureBranch(payload)
      );
      commit('saveScanBranchSonarRes', response);
    },
    // 分支代码 snoar 扫描结果和应用扫描结果一样 无需单独声明变量
    async featureProjectAnalyses({ commit }, payload) {
      const response = await resolveResponseError(() =>
        featureProjectAnalyses(payload)
      );
      commit('saveSonarScan', response);
    },
    // 分支代码 snoar 扫描结果和应用扫描结果一样
    async getProjectFeatureInfo({ commit }, payload) {
      const response = await resolveResponseError(() =>
        getProjectFeatureInfo(payload)
      );
      commit('saveSonarDetail', response);
    },
    // 分支代码 snoar 扫描结果和应用扫描结果一样
    async featureComponentTree({ commit }, payload) {
      const response = await resolveResponseError(() =>
        featureComponentTree(payload)
      );
      commit('saveSonarTableData', response);
    }
  },

  mutations: {
    saveGetPipelineById(state, payload) {
      state.jobsInfo = payload;
    },
    saveAppSystem(state, payload) {
      state.appSystemList = payload;
    },
    saveTestFlag(state, payload) {
      state.testFlag = payload;
    },
    saveVipGetLog(state, payload) {
      state.vipGetLogDetail = payload;
    },
    saveRunPipeline(state, payload) {
      state.runPipelineInfo = payload;
    },
    saveCancelVipDeploy(state, payload) {
      state.cancelVipDeployList = payload;
    },
    saveQueryPipelinesList(state, payload) {
      state.vipPipelinesList = payload;
    },
    saveQueryWithEnv(state, payload) {
      state.withEnvAppData = payload;
    },
    saveAppData(state, payload) {
      state.appData = payload;
    },
    saveRows(state, payload) {
      state.rowsNumber = payload;
    },
    saveSortBy(state, payload) {
      state.sortBy = payload;
    },
    saveMyAppData(state, payload) {
      state.MyAppData = payload;
    },
    saveGitlabData(state, payload) {
      state.gitlabData = payload;
    },
    saveGroup(state, payload) {
      state.groupData = payload;
    },
    saveExistedApp(state, payload) {
      state.existedAppData = payload;
    },
    saveNewApp(state, payload) {
      state.newAppData = payload;
    },
    saveBranchList(state, payload) {
      state.branchList = payload;
    },
    saveApplicationName(state, payload) {
      state.applicationName = payload;
    },
    savePipelinesPage(state, payload) {
      state.pipelinesListPage = payload;
    },
    savePipelines(state, payload) {
      state.pipelinesList = payload;
    },
    saveAppType(state, payload) {
      state.appTypeData = payload;
    },
    saveVueApp(state, payload) {
      state.vueAppData = payload;
    },
    saveAppContinuous(state, payload) {
      state.appContinuous = payload;
    },
    saveAppContinuousList(state, payload) {
      state.appContinuousTableData = payload;
    },
    saveGroupGitList(state, payload) {
      state.groupGitList = payload;
    },
    saveTraces(state, payload) {
      state.jobTraces = payload;
    },
    saveAppTagPiplines(state, payload) {
      state.appTagPiplines = payload;
    },
    saveTestRunPiplines(state, payload) {
      state.testRunPiplines = payload;
    },
    saveAutoTest(state, payload) {
      state.autoTestData = payload;
    },
    saveRunnerJobLog(state, payload) {
      state.runnerJobLog = payload;
    },
    saveRunnerJobLogDetail(state, payload) {
      state.runnerJobLogDetail = payload;
    },
    saveMantisByAppId(state, payload) {
      state.mantisList = payload ? payload : [];
    },
    saveSonarScan(state, payload) {
      if (!payload.analyses) {
        state.sonarScan = '';
        return;
      }
      const time = moment(payload.analyses[0].date).format(
        'YYYY年MM月DD日 HH:mm:ss'
      );
      state.sonarScan = {
        ...payload,
        time: time
      };
    },
    saveSonarDetail(state, payload) {
      const ncloc = payload.ncloc;
      let size = 'XS';
      if (ncloc >= 1000) {
        size = 'S';
      } else if (ncloc >= 10000) {
        size = 'M';
      } else if (ncloc >= 100000) {
        size = 'L';
      }
      const language = payload.ncloc_language_distribution.split(';');
      state.sonarDetail = {
        ...payload,
        coverage: payload.coverage,
        ncloc: ncloc,
        num: payload.ncloc,
        language: language.map(lan => {
          const lanArr = lan.split('=');
          return { name: lanArr[0], value: lanArr[1] };
        }),
        size: size,
        duplicated_lines_density: payload.duplicated_lines_density,
        reliability_rating: rating[payload.reliability_rating],
        security_rating: rating[payload.security_rating],
        sqale_rating: rating[payload.sqale_rating],
        coverage_rating: handleCoverRating(payload.sqale_rating)
      };
    },
    saveSonarChart(state, payload) {
      state.sonarChart = payload;
    },
    saveSonarTableData(state, payload) {
      let typeIsFile = [];
      let typeNotFile = [];
      const root = [];
      payload.forEach(item => {
        const measuresObj = {};
        item.measures.forEach(i => {
          const metric = i.metric;
          measuresObj[metric] = i.value;
        });
        Object.assign(item, measuresObj);
        if (
          (item.qualifier === 'DIR' && item.name !== '/') ||
          item.name === 'pom.xml'
        ) {
          const parentPath = item.key;
          item.parentPath = parentPath;
          typeNotFile.push(item);
        } else if (item.qualifier === 'BRC') {
          root.push(item);
        } else {
          const parentPathArr = item.key.split('/');
          const parentPath = parentPathArr
            .slice(0, parentPathArr.length - 1)
            .join('/');
          item.parentPath = parentPath;
          typeIsFile.push(item);
        }
      });
      typeNotFile = handleFileTree(typeNotFile, typeIsFile);
      state.sonarTableData = handleFileTree(root, typeNotFile);
    },
    saveScanBranchSonarRes(state, payload) {
      state.scanBranchSonarRes = payload;
    }
  }
};

function handleFileTree(root, dirs) {
  if (root.length === 0) {
    return dirs;
  }
  const child = root.map((item, index) => {
    const children = [];
    let i = 0;
    dirs.forEach(dir => {
      if (dir.parentPath === item.key) {
        dir.index = i;
        children.push(dir);
        i++;
      }
    });
    return {
      ...item,
      index: index,
      children: children
    };
  });
  return child;
}
