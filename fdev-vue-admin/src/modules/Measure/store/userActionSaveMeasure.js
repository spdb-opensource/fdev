import moment from 'moment';
export default {
  namespaced: true,
  modules: {
    // 项目组规模
    projectTeamSize: {
      namespaced: true,
      state: {
        isIncludeChildren: true, //是否包含子组
        selectedGroups: [], //所选择的组
        groupsTree: [] //组树
      },
      getters: {
        //初始化组树的值
        groupData(state, getters, rootState, rootGetters) {
          return initGroupTree(
            rootGetters['userForm/groupsTree'],
            state.groupsTree
          );
        }
      },
      mutations: {
        updateIsIncludeChildren(state, payload) {
          state.isIncludeChildren = payload;
        },
        updateGroupsTree(state, payload) {
          state.groupsTree = payload;
        },
        updateSelectedGroups(state, payload) {
          state.selectedGroups = payload;
        }
      }
    },
    // 项目组资源闲置情况
    fdevTaskSum: {
      namespaced: true,
      state: {
        isIncludeChildren: true, //是否包含子组
        selectedGroups: [], //所选择的组
        groupsTree: [] //组树
      },
      getters: {
        //初始化组树的值
        groupData(state, getters, rootState, rootGetters) {
          return initGroupTree(
            rootGetters['userForm/groupsTree'],
            state.groupsTree
          );
        }
      },
      mutations: {
        updateIsIncludeChildren(state, payload) {
          state.isIncludeChildren = payload;
        },
        updateGroupsTree(state, payload) {
          state.groupsTree = payload;
        },
        updateSelectedGroups(state, payload) {
          state.selectedGroups = payload;
        }
      }
    },
    //基础架构维度
    basicFrameAnalysis: {
      namespaced: true,
      state: {
        time: {
          start_date: moment(new Date() - 24 * 60 * 60 * 30000).format(
            'YYYY-MM-DD'
          ), //开始时间
          end_date: moment(new Date()).format('YYYY-MM-DD') //结束时间
        }
      },
      mutations: {
        updateTimeStartDate(state, payload) {
          state.time.start_date = payload;
        },
        updateTimeEndDate(state, payload) {
          state.time.end_date = payload;
        }
      }
    },
    // 应用维度
    applicationDemension: {
      namespaced: true,
      state: {
        showSwitch: 'chartShow', //图表/表格
        appSelected: [],
        appData: [] //应用信息
      },
      mutations: {
        updateShowSwitch(state, payload) {
          state.showSwitch = payload;
        },
        updateAppSelected(state, payload) {
          state.appSelected = payload;
        },
        updateAppData(state, payload) {
          state.appData = payload;
        }
      }
    },
    //任务推进情况
    taskPhaseChange: {
      namespaced: true,
      state: {
        selectedGroups: [], //所选择的组
        percapita: false, // 人均数label
        isIncludeChildren: true, //是否包含子组label
        roleGroup: {
          label: '总人数',
          value: ''
        }, //勾选人均数label后选中的角色
        showSwitch: 'chartShow', //选择图表或是表格
        groupsTree: [], //组树
        demandType: ['business', 'tech', 'daily'],
        taskType: [0, 1, 2]
      },
      getters: {
        //初始化组树的值
        groupData(state, getters, rootState, rootGetters) {
          return initGroupTree(
            rootGetters['userForm/groupsTree'],
            state.groupsTree
          );
        }
      },
      mutations: {
        updateDemandType(state, payload) {
          state.demandType = payload;
        },
        updateTaskType(state, payload) {
          state.taskType = payload;
        },
        updateSelectedGroups(state, payload) {
          state.selectedGroups = payload;
        },
        updatePercapita(state, payload) {
          state.percapita = payload;
        },
        updateIsIncludeChildren(state, payload) {
          state.isIncludeChildren = payload;
        },
        updateRoleGroup(state, payload) {
          state.roleGroup = payload;
        },
        updateShowSwitch(state, payload) {
          state.showSwitch = payload;
        },
        updateGroupsTree(state, payload) {
          state.groupsTree = payload;
        }
      }
    },
    //小组维度任务不同阶段数量统计
    groupTakStageNum: {
      namespaced: true,
      state: {
        selectedGroups: [], //所选择的组
        percapita: false, // 人均数label
        isIncludeChildren: true, //是否包含子组label
        roleGroup: { label: '总人数', value: '' }, //勾选人均数label后选中的角色
        showSwitch: 'chartShow', //选择图表或是表格
        groupsTree: [] //组树
      },
      getters: {
        //初始化组树的值
        groupData(state, getters, rootState, rootGetters) {
          return initGroupTree(
            rootGetters['userForm/groupsTree'],
            state.groupsTree
          );
        }
      },
      mutations: {
        updateSelectedGroups(state, payload) {
          state.selectedGroups = payload;
        },
        updatePercapita(state, payload) {
          state.percapita = payload;
        },
        updateIsIncludeChildren(state, payload) {
          state.isIncludeChildren = payload;
        },
        updateRoleGroup(state, payload) {
          state.roleGroup = payload;
        },
        updateShowSwitch(state, payload) {
          state.showSwitch = payload;
        },
        updateGroupsTree(state, payload) {
          state.groupsTree = payload;
        }
      }
    },
    //各人员不同阶段任务数量统计
    personTaskStageNum: {
      namespaced: true,
      state: {
        listModel: {
          start_date: moment(new Date())
            .subtract(1, 'month')
            .format('YYYY/MM/DD'),
          end_date: moment(new Date()).format('YYYY/MM/DD')
        }, //默认范围为当前月往前30天
        showSwitch: 'chartShow',
        demandType: ['business', 'tech', 'daily'],
        taskType: [0, 1, 2]
      },
      mutations: {
        updateDemandType(state, payload) {
          state.demandType = payload;
        },
        updateTaskType(state, payload) {
          state.taskType = payload;
        },
        updateStartDate(state, payload) {
          state.listModel.start_date = payload;
        },
        updateEndDate(state, payload) {
          state.listModel.end_date = payload;
        },
        updateShowSwitch(state, payload) {
          state.showSwitch = payload;
        }
      }
    },
    // 生产问题报表统计
    proChartStatis: {
      namespaced: true,
      state: {
        proModel: {
          startDate: moment(new Date() - 24 * 60 * 60 * 30000).format(
            'YYYY-MM-DD'
          ),
          endDate: moment(new Date()).format('YYYY-MM-DD')
        },
        showSwitch: 'chartShow'
      },
      mutations: {
        proModelStartDate(state, payload) {
          state.proModel.startDate = payload;
        },
        proModelEndDate(state, payload) {
          state.proModel.endDate = payload;
        },
        updateShowSwitch(state, payload) {
          state.showSwitch = payload;
        }
      }
    },
    // 需求统计
    rqrStatistic: {
      namespaced: true,
      state: {
        isParent: true,
        selectedGroups: [], //所选择的组
        groupsTree: [], //组树
        groupType: '4' //实施需求数量优先级
      },
      getters: {
        //初始化组树的值
        groupData(state, getters, rootState, rootGetters) {
          return initGroupTree(
            rootGetters['userForm/groupsTree'],
            state.groupsTree
          );
        }
      },
      mutations: {
        updateIsParent(state, payload) {
          state.isParent = payload;
        },
        updateGroupsTree(state, payload) {
          state.groupsTree = payload;
        },
        updateSelectedGroups(state, payload) {
          state.selectedGroups = payload;
        },
        updateGroupType(state, payload) {
          state.groupType = payload;
        }
      }
    },
    //MergeRequest
    mergeRequest: {
      namespaced: true,
      state: {
        mergeRequestModel: {
          git_user_id: '', //用户
          group_id: '', //应用所属小组
          start_time: '', //开始时间
          end_time: '' //结束时间
        }
      },
      mutations: {
        filterModelGroupId(state, payload) {
          state.mergeRequestModel.group_id = payload;
        },
        filterModelGitUserId(state, payload) {
          state.mergeRequestModel.git_user_id = payload;
        },
        filterModelStartTime(state, payload) {
          state.mergeRequestModel.start_time = payload;
        },
        filterModelEndTime(state, payload) {
          state.mergeRequestModel.end_time = payload;
        }
      }
    },
    //代码统计
    codeStatistics: {
      namespaced: true,
      state: {
        time: {
          start_date: '', //开始时间
          end_date: '' //结束时间
        },
        tableCompany: '', //公司
        tableArea: '', //地区
        tableRole: '', //角色
        tableIsLeave: '', //在职离职
        tableContainSubGroup: false, //是否包含子组
        tableAppType: ['app'],
        visibleColumns: [
          'userNameCn', //姓名
          'userNameEn', //git用户名
          'company', //公司
          'role', //角色
          'total', //总行数
          'startDate', //开始时间
          'endDate', //结束时间
          'detail' //详情
        ],
        selected: '', //已选树结点
        selectId: '' //已选结点
      },
      mutations: {
        updateAppType(state, payload) {
          state.tableAppType = payload;
        },
        timeStartDate(state, payload) {
          state.time.start_date = payload;
        },
        timeEndDate(state, payload) {
          state.time.end_date = payload;
        },
        updateCompany(state, payload) {
          state.tableCompany = payload;
        },
        updateArea(state, payload) {
          state.tableArea = payload;
        },
        updateRole(state, payload) {
          state.tableRole = payload;
        },
        updateIsLeave(state, payload) {
          state.tableIsLeave = payload;
        },
        updateIsContainSubGroup(state, payload) {
          state.tableContainSubGroup = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        },
        updateSelected(state, payload) {
          state.selected = payload;
        },
        updateselectId(state, payload) {
          state.seletId = payload;
        }
      }
    }
  }
};

//初始化树数据
function initGroupTree(rawtree, seletedtree) {
  return rawtree.map(node => {
    let selectedNode = findNode(node.id, seletedtree);
    let newNode = {
      id: node.id,
      label: node.label,
      selected: selectedNode ? selectedNode.selected : false
    };
    if (node.children && node.children.length > 0) {
      newNode.open = selectedNode ? selectedNode.open : false;
      newNode.children = initGroupTree(node.children, seletedtree);
    }
    return newNode;
  });
}

//根据id寻找node,若无，则返回null
function findNode(id, tree) {
  for (let i = 0; i < tree.length; i++) {
    if (tree[i].id === id) return tree[i];
  }
  let newtree = tree.filter(node => node.children).map(x => x.children);
  if (newtree.length > 0) {
    newtree = newtree.reduce((acc, curr) => acc.concat(curr));
    return findNode(id, newtree);
  } else return null;
}
