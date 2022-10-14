import moment from 'moment';
export default {
  namespaced: true,
  modules: {
    //小组维度
    analysis: {
      namespaced: true,
      state: {
        selectedGroups: ['5c81c4d0d3e2a1126ce30049'], //所选择的组
        percapita: false, // 人均数label
        isIncludeChildren: true, //是否包含子组label
        roleGroup: '总人数', //勾选人均数label后选中的角色
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
    //电子看板
    kanban: {
      namespaced: true,
      modules: {
        //任务产出统计
        taskOutput: {
          namespaced: true,
          state: {},
          mutations: {}
        }
      }
    },
    //项目组管理；
    projectTeamManage: {
      namespaced: true,
      modules: {
        // 需求统计
        reqAnalysis: {
          namespaced: true,
          state: {
            isParent: true,
            selectedGroups: [], //所选择的组
            groupsTree: [], //组树
            taskGroupType: '4', //需求统计优先级
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
            updateTaskGroupType(state, payload) {
              state.taskGroupType = payload;
            },
            updateGroupType(state, payload) {
              state.groupType = payload;
            }
          }
        },
        // 研发单元统计
        implUnitAnalysis: {
          namespaced: true,
          state: {
            isParent: true, //是否包含子组
            selectedGroups: [], //所选择的组
            groupsTree: [], //组树，
            groupType: '4' //优先级
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
        // 小组维度任务统计
        groupTaskAnalysis: {
          namespaced: true,
          state: {
            isParent: true, //是否包含子组
            selectedGroups: [], //所选择的组
            groupsTree: [], //组树
            groupType: '0' //全部/重要选项
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
        // 用户维度任务统计
        userTaskAnalysis: {
          namespaced: true,
          state: {
            userSelected: [], //已选人员
            roles: [], //角色
            selectedCompanyList: [], //所属公司
            selectedGroups: ['5c81c4d0d3e2a1126ce30049'], //所选择的组
            groupsTree: [], //组树
            groupType: '0'
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
            updateUserSelected(state, payload) {
              state.userSelected = payload;
            },
            updateRoles(state, payload) {
              state.roles = payload;
            },
            updateSelectedCompanyList(state, payload) {
              state.selectedCompanyList = payload;
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
        // 玉衡测试相关
        aliothRelated: {
          namespaced: true,
          state: {
            isIncludeChildren: false, //是否包含子组
            selectedGroups: [], //所选择的组
            groupsTree: [], //组树
            startDate: '', //开始日期
            endDate: '' //结束日期
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
            },
            updateStartDate(state, payload) {
              state.startDate = payload;
            },
            updateEndDate(state, payload) {
              state.endDate = payload;
            }
          }
        }
      }
    },
    //排行榜
    rankingList: {
      namespaced: true,
      state: {
        percentOpened: false
      },
      mutations: {
        updatepercentOpened(state, payload) {
          state.percentOpened = payload;
        }
      }
    },
    //基础架构
    basicFrame: {
      namespaced: true,
      modules: {
        //个人优化需求
        userOptimize: {
          namespaced: true,
          state: {
            visibleColumns: [
              'name',
              'type',
              'rqrmnts_name',
              'develop',
              'feature_branch',
              'recommond_version',
              'predict_version',
              'stage',
              'due_date'
            ]
          },
          mutations: {
            updateVisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        },
        //需求延期情况
        rqrDelay: {
          namespaced: true,
          state: {
            createrqrDelayModel: {
              name: '', //维度
              memberObj: null, //参与人
              groupObj: null //名称
            },
            visibleColumns: [
              'name',
              'type',
              'rqrmnts_admin',
              'rqrmnts_name',
              'develop',
              'stage',
              'due_date',
              'delay_date'
            ]
          },
          mutations: {
            updateRqrDelayName(state, payload) {
              state.createrqrDelayModel.name = payload;
            },
            updateRqrDelayGroupObj(state, payload) {
              state.createrqrDelayModel.groupObj = payload;
            },
            updateRqrDelayMember(state, payload) {
              state.createrqrDelayModel.memberObj = payload;
            },
            updatevisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        }
      }
    },
    //生产问题
    productionProblems: {
      namespaced: true,
      modules: {
        //报表统计
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
        }
      }
    },
    //代码统计相关
    codeStatistic: {
      namespaced: true,
      modules: {
        //代码统计
        statistics: {
          namespaced: true,
          state: {
            time: {
              start_date: '', //开始时间
              end_date: '' //结束时间
            },
            tableCompany: '', //公司
            tableArea: '', //地区
            tableRole: '', //角色
            tableIsLeave: '0', //在职离职
            visibleColumns: [
              'nickName', //姓名
              'userName', //git用户名
              'companyname', //公司
              'rolename', //角色
              'total', //总行数
              'startDate', //开始时间
              'endDate', //结束时间
              'detail' //详情
            ],
            selected: '', //已选树结点
            selectId: '' //已选结点
          },
          mutations: {
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
