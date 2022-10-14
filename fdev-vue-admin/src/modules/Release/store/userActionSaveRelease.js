import moment from 'moment';
import { createJobModel } from '../utils/model';
export default {
  namespaced: true,
  modules: {
    //投产窗口列表
    releaseList: {
      namespaced: true,
      state: {
        //组合的过滤条件
        terms: '',
        //搜索条件
        selectValue: [],
        //投产窗口 sit开始时间  rel开始时间
        jobModel: createJobModel(),
        releaseType: '全部',
        group: '',
        //组是否改变 0-不变   1-改变
        isGroupChange: '0',
        spdbManage: '全部',
        manager: '全部',
        currentPage: {
          rowsPerPage: 5
        },
        visibleColumns: [
          'release_node_name',
          'release_date',
          'type',
          'owner_group_name',
          'release_spdb_manager_name_cn',
          'release_manager_name_cn',
          'create_user_name_cn',
          'btn'
        ],
        // 投产大窗口 开始时间  结束时间
        releaseModel: {
          start_date: moment(new Date() - 24 * 60 * 60 * 30000).format(
            'YYYY-MM-DD'
          ),
          end_date: '',
          owner_groupId: ''
        },
        bigReleaseCurrentPage: {
          rowsPerPage: 5
        }
      },
      mutations: {
        updateIsGroupChange(state, payload) {
          state.isGroupChange = payload;
        },
        updateTerms(state, payload) {
          state.terms = payload;
        },
        updateBigReleaseCurrentPage(state, payload) {
          state.bigReleaseCurrentPage = payload;
        },
        updateCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        },
        updateReleaseModelOwnerGroupId(state, payload) {
          state.releaseModel.owner_groupId = payload;
        },
        updateReleaseModelStartDate(state, payload) {
          state.releaseModel.start_date = payload;
        },
        updateReleaseModelEndDate(state, payload) {
          state.releaseModel.end_date = payload;
        },
        updateJobModelSitStartDate(state, payload) {
          state.jobModel.sitStartDate = payload;
        },
        updateJobModelRelStartDate(state, payload) {
          state.jobModel.relStartDate = payload;
        },
        updateSelectValue(state, payload) {
          state.selectValue = payload;
        },
        updateReleaseType(state, payload) {
          state.releaseType = payload;
        },
        updateGroup(state, payload) {
          state.group = payload;
        },
        updateSpdbManage(state, payload) {
          state.spdbManage = payload;
        },
        updateManager(state, payload) {
          state.manager = payload;
        }
      }
    },
    //投产窗口列表-投产窗口-配置文件变更
    updateFileConfig: {
      namespaced: true,
      state: {
        //组合的过滤条件
        terms: {
          map: '',
          model: '',
          field: null
        },
        queryCurrentPage: {
          rowsPerPage: 5
        },
        addCurrentPage: {
          rowsPerPage: 5
        },
        visibleQueryColumns: [
          'name_cn',
          'name_en',
          'dev_managers',
          'spdb_manager',
          'group',
          'gitlab',
          'branch'
        ],
        visibleAddColumns: [
          'application_name',
          'dev_managers',
          'config_type',
          'release_node',
          'asset_name',
          'status',
          'operate'
        ]
      },
      mutations: {
        updateAddCurrentPage(state, payload) {
          state.addCurrentPage = payload;
        },
        updateQueryCurrentPage(state, payload) {
          state.queryCurrentPage = payload;
        },
        updateFileConfigQueryVisibleColumn(state, payload) {
          state.visibleQueryColumns = payload;
        },
        updateFileConfigAddVisibleColumn(state, payload) {
          state.visibleAddColumns = payload;
        },
        updateTermsMap(state, payload) {
          state.terms.map = payload;
        },
        updateTermsModel(state, payload) {
          state.terms.model = payload;
        },
        updateTermsField(state, payload) {
          state.terms.field = payload;
        }
      }
    },
    //投产窗口列表-投产窗口-投产任务列表
    releaseJobList: {
      namespaced: true,
      state: {
        //组合的过滤条件
        terms: '',
        // 搜索条件
        selectValue: [],
        // 任务的所有阶段
        taskStage: '全部',
        // 全部  与我相关
        mine: '全部',
        // 应用
        application: '全部',
        currentPage: {
          rowsPerPage: 5
        },
        visibleColumns: [
          'taskName',
          'group',
          'dev_managers',
          'bank_master',
          'taskProject',
          'taskStage',
          'taskStatus',
          'merge_release_flag',
          'merge_release_time',
          'name'
        ]
      },
      mutations: {
        updateCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        },
        updateTerms(state, payload) {
          state.terms = payload;
        },
        updateSelectValue(state, payload) {
          state.selectValue = payload;
        },
        updateTaskStage(state, payload) {
          state.taskStage = payload;
        },
        updateMine(state, payload) {
          state.mine = payload;
        },
        updateApplication(state, payload) {
          state.application = payload;
        }
      }
    },
    // 变更计划
    changesPlans: {
      namespaced: true,
      state: {
        //组合的过滤条件
        terms: '',
        // 输入的搜索条件
        selectValue: [],
        changesModel: {
          //开始时间
          startDate: moment(new Date()).format('YYYY/MM/DD'),
          // 结束时间
          endDate: ''
        },
        changes: '全部',
        selector: []
      },
      mutations: {
        updateTerms(state, payload) {
          state.terms = payload;
        },
        updateSelectValue(state, payload) {
          state.selectValue = payload;
        },
        updateStartDate(state, payload) {
          state.changesModel.startDate = payload;
        },
        updateEndDate(state, payload) {
          state.changesModel.endDate = payload;
        },
        updateChanges(state, payload) {
          state.changes = payload;
        },
        updateSelector(state, payload) {
          state.selector = payload;
        }
      }
    },
    // 变更模板管理
    changeTemplate: {
      namespaced: true,
      state: {
        //组合的过滤条件
        terms: '',
        // 输入的搜索条件
        selectValue: [],
        tableGroup: '全部',
        tableSystem: '全部',
        tableType: '全部',
        currentPage: {
          rowsPerPage: 5
        },
        visibleColumns: [
          'temp_name',
          'owner_system_name',
          'owner_group',
          'template_type',
          'update_time',
          'btn'
        ]
      },
      mutations: {
        updateTerms(state, payload) {
          state.terms = payload;
        },
        updateCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        updateSelectValue(state, payload) {
          state.selectValue = payload;
        },
        updateTableGroup(state, payload) {
          state.tableGroup = payload;
        },
        updateTableSystem(state, payload) {
          state.tableSystem = payload;
        },
        updateTableType(state, payload) {
          state.tableType = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    //数据库变更审核
    reviewRelatedItems: {
      namespaced: true,
      state: {
        listModel: {
          key: '',
          applicant: '',
          group: '',
          reviewStatus: ''
        },
        currentPage: {
          rowsPerPage: 5,
          page: 1,
          rowsNumber: 0
        },
        visibleColumns: [
          'taskName',
          'group',
          'applicantName',
          'reviewers',
          'reviewStatus',
          'btn'
        ],
        //组是否改变 0-不变   1-改变
        isGroupChange: '0'
      },
      mutations: {
        updateIsGroupChange(state, payload) {
          state.isGroupChange = payload;
        },
        updateCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        updateListModelKey(state, payload) {
          state.listModel.key = payload;
        },
        updateListModelApplicant(state, payload) {
          state.listModel.applicant = payload;
        },
        updateListModelGroup(state, payload) {
          state.listModel.group = payload;
        },
        updateListModelReviewStatus(state, payload) {
          state.listModel.reviewStatus = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    //参数维护-模块类型
    paramsModuleType: {
      namespaced: true,
      state: {
        tab: '0',
        currentPage: {
          rowsPerPage: 5
        },
        visibleColumns: ['catalog_name', 'catalog_type', 'description', 'btn']
      },
      mutations: {
        updateTab(state, payload) {
          state.tab = payload;
        },
        updateCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    //参数维护-脚本参数
    paramsScriptParams: {
      namespaced: true,
      state: {
        tab: '0',
        currentPage: {
          rowsPerPage: 5
        },
        visibleColumns: ['key', 'value', 'description', 'btn']
      },
      mutations: {
        updateTab(state, payload) {
          state.tab = payload;
        },
        updateCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    //参数维护-脚本参数
    paramsAutomationEnv: {
      namespaced: true,
      state: {
        tab: '0',
        currentPage: {
          rowsPerPage: 5
        },
        visibleColumns: [
          'env_name',
          'platform',
          'pro_dmz_caas',
          'pro_biz_caas',
          'gray_dmz_caas',
          'gray_biz_caas',
          'pro_dmz_scc',
          'pro_biz_scc',
          'gray_dmz_scc',
          'gray_biz_scc',
          'description',
          'btn'
        ]
      },
      mutations: {
        updateTab(state, payload) {
          state.tab = payload;
        },
        updateCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    //投产窗口列表-投产窗口-投产应用列表
    releaseAppList: {
      namespaced: true,
      state: {
        selectValue: []
      },
      mutations: {
        updateSelectValue(state, payload) {
          state.selectValue = payload;
        }
      }
    },
    //问题列表
    problemsList: {
      namespaced: true,
      state: {
        listModel: {
          start_time: '', //开始时间
          end_time: '', //结束时间
          module: ['5c81c4d0d3e2a1126ce30049'], //所属小组
          responsible_type: '', //负责人类型
          responsible_name_en: '', //负责人
          deal_status: '', //处理状态
          issue_level: '', //生产问题级别
          problemType: [], //问题类型
          isIncludeChildren: true, //是否包含子组
          company: '', //公司
          reviewer_status: '' //评审状态
        },
        isChartDisplay: false //是否显示图表
      },
      mutations: {
        updateListModelModule(state, payload) {
          state.listModel.module = payload;
        },
        updateListModelResponsibleName(state, payload) {
          state.listModel.responsible_name_en = payload;
        },
        updatelistModelResponsibleType(state, payload) {
          state.listModel.responsible_type = payload;
        },
        updateListModelReviewerStatus(state, payload) {
          state.listModel.reviewer_status = payload;
        },
        updatelistModelIsIncludeChildren(state, payload) {
          state.listModel.isIncludeChildren = payload;
        },
        updateListModelProblemType(state, payload) {
          state.listModel.problemType = payload;
        },
        updateListModelDealStatus(state, payload) {
          state.listModel.deal_status = payload;
        },
        updateListModelIssueLevel(state, payload) {
          state.listModel.issue_level = payload;
        },
        updateListModelStartTime(state, payload) {
          state.listModel.start_time = payload;
        },
        updateListModelEndTime(state, payload) {
          state.listModel.end_time = payload;
        },
        updateIsChartDisplay(state, payload) {
          state.isChartDisplay = payload;
        }
      }
    }
  }
};
