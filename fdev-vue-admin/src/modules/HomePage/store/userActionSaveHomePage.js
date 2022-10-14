/*
本文件公用的注释如下：
visibleCols  用户可选的列
currentPage  页面分页信息
termsApp  搜索条件
**/
export default {
  namespaced: true,
  modules: {
    myTodoPage: {
      namespaced: true,
      state: {
        todosTab: 'todos', //todo列表tabs项
        terms: [],
        currentPage: {
          rowsPerPage: 5
        }
      },
      getters: {
        searchValue(state) {
          return state.terms.toString();
        }
      },
      mutations: {
        saveToDoData(state, payload) {
          state.todosTab = payload;
        },
        saveTerms(state, payload) {
          state.terms = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        }
      }
    },
    mytaskPage: {
      namespaced: true,
      state: {
        visibleCols: [],
        currentPage: {
          rowsPerPage: 5
        },
        searchValue: [], //用户搜索内容
        delayStage: [] //延期阶段
      },
      getters: {
        terms(state) {
          return state.searchValue.toString();
        }
      },
      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveTerms(state, payload) {
          state.searchValue = payload;
        },
        saveDelayStage(state, payload) {
          state.delayStage = payload;
        }
      }
    },
    //我的合并审批列表
    myCodeList: {
      namespaced: true,
      todosTab: 'todoApprovals', //todo列表tabs项
      state: {
        visibleCols: [
          'task_name',
          'project_name',
          'group_name',
          'developer_name',
          'stage',
          'source_branch',
          'target_branch',
          'applicant_name',
          'apply_time',
          'apply_desc',
          'operate'
        ],
        visibleColsDone: [
          'task_name',
          'project_name',
          'group_name',
          'developer_name',
          'stage',
          'source_branch',
          'target_branch',
          'applicant_name',
          'apply_time',
          'apply_desc',
          'auditor_name',
          'apply_time',
          'result_desc',
          'status'
        ],
        searchGroupList: [], //所属小组搜索
        searchProposerList: [], //申请人搜索
        searchTaskName: '', //任务名称搜索
        groupListDone: [], //所属小组搜索
        proposerListDone: [], //申请人搜索
        auditorListDone: [],
        taskNameDone: '', //任务名称搜索
        currentTodoPage: { rowsPerPage: 5, page: 1, rowsNumber: 0 },
        currentDonePage: { rowsPerPage: 5, page: 1, rowsNumber: 0 },
        dateRange: {
          from: '',
          to: ''
        }
      },
      getters: {
        searchTaskName(state) {
          return state.searchTaskName.toString().trim();
        },
        taskNameDone(state) {
          return state.taskNameDone.toString().trim();
        }
      },
      mutations: {
        saveGroupListDone(state, payload) {
          state.groupListDone = payload;
        },
        saveProposerListDone(state, payload) {
          state.proposerListDone = payload;
        },
        saveAuditorListDone(state, payload) {
          state.auditorListDone = payload;
        },
        saveTaskNameDone(state, payload) {
          state.taskNameDone = payload;
        },
        saveToDoTab(state, payload) {
          state.todosTab = payload;
        },
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveVisibleColumnsDone(state, payload) {
          state.visibleColsDone = payload;
        },
        saveGroupList(state, payload) {
          state.searchGroupList = payload;
        },
        saveProposerList(state, payload) {
          state.searchProposerList = payload;
        },
        saveTaskName(state, payload) {
          state.searchTaskName = payload;
        },
        saveCurrentTodoPage(state, payload) {
          state.currentTodoPage = payload;
        },
        saveCurrentDonePage(state, payload) {
          state.currentDonePage = payload;
        },
        saveDate(state, payload) {
          if (payload) {
            state.dateRange = payload;
          } else {
            state.dateRange = {
              from: '',
              to: ''
            };
          }
        }
      }
    },
    myAppPage: {
      namespaced: true,
      state: {
        visibleCols: ['name_zh', 'name_en', 'group', 'hangnei', 'yingyong'],
        currentPage: {
          rowsPerPage: 5
        },
        termsApp: [] //用户搜索内容
      },
      getters: {
        searchValue(state) {
          return state.termsApp.toString();
        }
      },
      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveTermsApp(state, payload) {
          state.termsApp = payload;
        }
      }
    },
    archetypeTab: {
      namespaced: true,
      state: {
        visibleCols: [
          'name_en',
          'name_cn',
          'manager_id',
          'group',
          'recommend_version',
          'type',
          'desc'
        ],
        currentPage: {
          rowsPerPage: 5
        },
        termsApp: [] //用户搜索内容
      },
      getters: {
        searchValue(state) {
          return state.termsApp.toString();
        }
      },
      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveTermsApp(state, payload) {
          state.termsApp = payload;
        }
      }
    },
    myFramePage: {
      namespaced: true,
      state: {
        tabs: 'model' //todo列表tabs项
      },
      mutations: {
        saveTabs(state, payload) {
          state.tabs = payload;
        }
      }
    },
    myDefect: {
      namespaced: true,
      state: {
        visibleCols: [
          'task_name',
          'summary',
          'description',
          'priority',
          'status',
          'handler',
          'date_submitted',
          'reporter',
          'operation'
        ],
        uatVisibleCols: [
          'taskName',
          'summary',
          'description',
          'priority',
          'status',
          'assignee',
          'created',
          'creator',
          'operation'
        ],
        currentPage: {
          rowsPerPage: 5
        },
        termsApp: {
          showDownFile: false, //显示归档缺陷
          searchValue: [] //用户搜索内容
        },
        UATtermsApp: {
          showDownFile: false, //显示归档缺陷
          searchValue: [] //用户搜索内容
        }
      },
      getters: {
        searchValueFilter(state) {
          return state.termsApp.searchValue.toString();
        }
      },
      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveSearchValueApp(state, payload) {
          state.termsApp.searchValue = payload;
        },
        saveShowDownFile(state, payload) {
          state.termsApp.showDownFile = payload;
        },
        saveUATVisibleColumns(state, payload) {
          state.uatVisibleCols = payload;
        },
        saveUATCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveUATSearchValueApp(state, payload) {
          state.UATtermsApp.searchValue = payload;
        },
        saveUATShowDownFile(state, payload) {
          state.UATtermsApp.showDownFile = payload;
        }
      }
    },
    myProduct: {
      namespaced: true,
      state: {
        visibleCols: [
          'problem_phenomenon',
          'requirement_name',
          'occurred_time',
          'issue_type',
          'is_trigger_issue',
          'issue_level',
          'deal_status',
          'btn'
        ],
        currentPage: {
          rowsPerPage: 5
        },
        termsApp: [] //用户搜索内容
      },
      getters: {
        searchValue(state) {
          return state.termsApp.toString();
        }
      },
      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveTermsApp(state, payload) {
          state.termsApp = payload;
        }
      }
    },
    webArchetypeTab: {
      namespaced: true,
      state: {
        visibleCols: [
          'name_en',
          'name_cn',
          'manager_id',
          'group',
          'recommend_version',
          'type',
          'desc'
        ],
        currentPage: {
          rowsPerPage: 5
        },
        termsApp: [] //用户搜索内容
      },
      getters: {
        searchValue(state) {
          return state.termsApp.toString();
        }
      },
      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveTermsApp(state, payload) {
          state.termsApp = payload;
        }
      }
    },
    webUnitTab: {
      namespaced: true,
      state: {
        visibleCols: [
          'name_en',
          'name_cn',
          'manager',
          'recommond_version',
          'source',
          'npm_name',
          'npm_group',
          'type',
          'group',
          'gitlab_url',
          'root_dir',
          'desc'
        ],
        currentPage: {
          rowsPerPage: 5
        },
        termsApp: [] //用户搜索内容
      },
      getters: {
        searchValue(state) {
          return state.termsApp.toString();
        }
      },
      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveTermsApp(state, payload) {
          state.termsApp = payload;
        }
      }
    },
    unitTab: {
      namespaced: true,
      state: {
        visibleCols: [
          'name_en',
          'name_cn',
          'manager_id',
          'recommond_version',
          'source'
        ],
        currentPage: {
          rowsPerPage: 5
        },
        termsApp: [] //用户搜索内容
      },
      getters: {
        searchValue(state) {
          return state.termsApp.toString();
        }
      },
      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveTermsApp(state, payload) {
          state.termsApp = payload;
        }
      }
    },
    myUnitPage: {
      namespaced: true,
      state: {
        todosTab: 'unitTodos', //todo列表tabs项
        terms: '',
        approveType: '',
        keywords: '',
        approveTypeDone: '',
        currentTodoPage: { rowsPerPage: 5, page: 1, rowsNumber: 0 },
        currentDonePage: { rowsPerPage: 5, page: 1, rowsNumber: 0 },
        visibleCols: [
          'fdevUnitNo',
          'fdevUnitName',
          'demandNo',
          'demandName',
          'fdevUnitLeaderName',
          'groupName',
          'overdueType',
          'overdueReason',
          'applicantName',
          'approveState',
          'approverName',
          'sectionName',
          'applyTime',
          'approveTime',
          'merge_reason',
          'approveReason',
          'approveType'
        ],
        visibleColsDone: [
          'fdevUnitNo',
          'fdevUnitName',
          'demandNo',
          'demandName',
          'fdevUnitLeaderName',
          'groupName',
          'overdueType',
          'overdueReason',
          'applicantName',
          'approveState',
          'approverName',
          'sectionName',
          'applyTime',
          'approveTime',
          'approveReason',
          'approveType',
          'merge_reason'
        ]
      },

      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveToDoData(state, payload) {
          state.todosTab = payload;
        },
        saveTerms(state, payload) {
          state.terms = payload;
        },
        saveType(state, payload) {
          state.approveType = payload;
        },
        saveKeywords(state, payload) {
          state.keywords = payload;
        },
        saveDoneType(state, payload) {
          state.approveTypeDone = payload;
        },
        saveVisibleColumnsDone(state, payload) {
          state.visibleColsDone = payload;
        },
        saveCurrentTodoPage(state, payload) {
          state.currentTodoPage = payload;
        },
        saveCurrentDonePage(state, payload) {
          state.currentDonePage = payload;
        }
      }
    },
    //需求评估定稿日期审批
    myEveluatePage: {
      namespaced: true,
      state: {
        todosTab: 'eveluateTodos', //todo列表tabs项
        terms: '',
        keywords: '',
        currentTodoPage: { rowsPerPage: 5, page: 1, rowsNumber: 0 },
        currentDonePage: { rowsPerPage: 5, page: 1, rowsNumber: 0 },
        visibleCols: [
          'oa_contact_no',
          'oa_contact_name',
          'demand_leader_group_cn',
          'apply_user',
          'apply_reason',
          'apply_update_time',
          'create_time',
          'operate_user',
          'operate_time',
          'operate_status',
          'state' //拒绝原因
        ],
        visibleColsDone: [
          'oa_contact_no',
          'oa_contact_name',
          'demand_leader_group_cn',
          'apply_user',
          'apply_reason',
          'apply_update_time',
          'create_time',
          'operate_user',
          'operate_time',
          'operate_status',
          'state' //拒绝原因
        ]
      },

      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveToDoData(state, payload) {
          state.todosTab = payload;
        },
        saveTerms(state, payload) {
          state.terms = payload;
        },
        saveKeywords(state, payload) {
          state.keywords = payload;
        },
        saveVisibleColumnsDone(state, payload) {
          state.visibleColsDone = payload;
        },
        saveCurrentTodoPage(state, payload) {
          state.currentTodoPage = payload;
        },
        saveCurrentDonePage(state, payload) {
          state.currentDonePage = payload;
        }
      }
    },
    //sit环境部署审批列表
    mySit2Page: {
      namespaced: true,
      state: {
        todosTab: 'unitTodos', //todo列表tabs项
        approvalParams: {
          groups: null, //所属小组
          apply_user: null, //申请人
          taskName: '' //任务名称
        },
        doneParams: {
          time: null, //日期
          groups: null, //所属小组
          apply_user: null, //申请人
          approve_user: null, //申请人
          taskName: '' //任务名称
        },
        currentTodoPage: { rowsPerPage: 5, page: 1, rowsNumber: 0 },
        currentDonePage: { rowsPerPage: 5, page: 1, rowsNumber: 0 },
        visibleCols: [
          'taskName', //任务名称
          'oaContactName', //需求名称
          'oaContactNo', //需求编号
          'applicationNameEn', //所属应用
          'deployEnv', //部署环境
          'applayUserNameZh', //申请人
          'overdueReason', //申请原因
          'applayUserOwnerGroup' //所属小组
        ],
        visibleColsDone: [
          'taskName', //任务名称
          'demandoaContactNoNo', //需求编号
          'applicationNameEn', //所属应用
          'deployEnv', //部署环境
          'applayUserNameZh', //申请人
          'overdueReason', //申请原因
          'applayUserOwnerGroup', //所属小组
          'reviewUserNameZh', //操作管理员
          'reviewTime', //操作时间
          'deploy_status' //部署状态
        ]
      },

      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveToDoData(state, payload) {
          state.todosTab = payload;
        },
        saveDoneType(state, payload) {
          state.approveTypeDone = payload;
        },
        saveVisibleColumnsDone(state, payload) {
          state.visibleColsDone = payload;
        },
        saveCurrentTodoPage(state, payload) {
          state.currentTodoPage = payload;
        },
        saveCurrentDonePage(state, payload) {
          state.currentDonePage = payload;
        },
        updateGroupIds(state, payload) {
          state.approvalParams.groups = payload;
        },
        updateApplyUser(state, payload) {
          state.approvalParams.apply_user = payload;
        },
        updatetaskName(state, payload) {
          state.approvalParams.taskName = payload;
        },
        updateDoneGroup(state, payload) {
          state.doneParams.groups = payload;
        },
        updateDoneApplyUser(state, payload) {
          state.doneParams.apply_user = payload;
        },
        updateDoneApproveUser(state, payload) {
          state.doneParams.approve_user = payload;
        },
        updateDonetaskName(state, payload) {
          state.doneParams.taskName = payload;
        }
      }
    }
  }
};
