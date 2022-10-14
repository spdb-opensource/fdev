export default {
  namespaced: true,
  modules: {
    // 任务查询
    taskQuery: {
      namespaced: true,
      state: {
        stage: [], // 任务阶段
        isSpectialStatus: false, // 暂缓
        visibleColumns: [
          'name',
          'redmine_id',
          'project_name',
          'group',
          'developer',
          'master',
          'spdb_master',
          'rqrmnt_no',
          'stage',
          'taskSpectialStatus',
          'tag',
          'operation'
        ] // 选择列
      },
      mutations: {
        updateStage(state, payload) {
          state.stage = payload;
        },
        updateIsSpectialStatus(state, payload) {
          state.isSpectialStatus = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    // 延期任务
    delayTask: {
      namespaced: true,
      state: {
        createDelayTaskModel: {
          redmine_id: '', //研发单元编号
          memberObj: null, //参与人
          groupObj: null, //所属小组
          rqrmnt: null, //需求名称/编号
          history: false, //是否展示历史阶段
          postCondition: [], //延期阶段
          name: '' //任务名称
        },
        visibleColumns: [
          'name',
          'rqrmnt',
          'develop',
          'group',
          'master',
          'spdb_master'
        ]
      },
      mutations: {
        updatedelayTaskRqrmnt(state, payload) {
          state.createDelayTaskModel.rqrmnt = payload;
        },
        updateDelayTaskGroupObj(state, payload) {
          state.createDelayTaskModel.groupObj = payload;
        },
        updateDelayTaskMemberObj(state, payload) {
          state.createDelayTaskModel.memberObj = payload;
        },
        updateDelayTaskPostCondition(state, payload) {
          state.createDelayTaskModel.postCondition = payload;
        },
        updateDelayTaskRedmineId(state, payload) {
          state.createDelayTaskModel.redmine_id = payload;
        },
        updateDelayTaskHistory(state, payload) {
          state.createDelayTaskModel.history = payload;
        },
        updateTaskName(state, payload) {
          state.createDelayTaskModel.name = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    //sit多次合并
    sitApproveList: {
      namespaced: true,
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
          'auditor_name',
          'apply_time',
          'result_desc',
          'merge_reason'
        ],
        groupListDone: [], //所属小组搜索
        proposerListDone: [], //申请人搜索
        auditorListDone: [],
        currentDonePage: { rowsPerPage: 5, page: 1, rowsNumber: 0 },
        dateRange: {
          from: '',
          to: ''
        }
      },
      mutations: {
        saveGroupList(state, payload) {
          state.groupListDone = payload;
        },
        saveProposerList(state, payload) {
          state.proposerListDone = payload;
        },
        saveAuditorListDone(state, payload) {
          state.auditorListDone = payload;
        },
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
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
    //电子看板
    realTimeParticipation: {
      namespaced: true,
      state: {
        delayStage: '延期选项',
        rqrmntType: 'init'
      },
      mutations: {
        updateDelayStage(state, payload) {
          state.delayStage = payload;
        },
        updateRqrmntType(state, payload) {
          state.rqrmntType = payload;
        }
      }
    }
  }
};
