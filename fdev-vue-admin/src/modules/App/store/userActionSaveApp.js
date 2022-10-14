export default {
  namespaced: true,
  modules: {
    //应用列表
    appList: {
      namespaced: true,
      state: {
        system: null, // 应用所属系统
        terms: '', // 应用中文名或英文名
        label: '', // 标签 / 重要度(以逗号隔开)
        appType: '', // 使用
        selectGroup: null, // 小组
        typeName: null, // 应用类型
        chargePerson: '', // 应用负责人 / 行内负责人
        visibleColumns: [
          'system',
          'name_zh',
          'name_en',
          'group',
          'typeName',
          'hangnei',
          'yingyong',
          'label',
          'interface',
          'sonar_scan_switch'
        ]
      },
      mutations: {
        updateSystem(state, payload) {
          state.system = payload;
        },
        updateTerms(state, payload) {
          state.terms = payload;
        },
        updateLabel(state, payload) {
          state.label = payload;
        },
        updateAppType(state, payload) {
          state.appType = payload;
        },
        updateSelectGroup(state, payload) {
          state.selectGroup = payload;
        },
        updateTypeName(state, payload) {
          state.typeName = payload;
        },
        updateChargePerson(state, payload) {
          state.chargePerson = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    //应用详情
    appProfile: {
      namespaced: true,
      state: {
        transactionCode: '',
        branchName: 'master', // '接口列表'下的'分支名'
        interfaceName: '', // '接口列表'下的'接口名称'输入项
        interfaceType: 'REST', // '接口列表'下的'提供报文类型'
        filter: '', // '关联事项'下的搜索项
        alarmVisibleColumns: ['name', 'redmine_id', 'stage', 'group', 'master'], // '关联事项'下的选择列
        visibleColumns: [
          'transId',
          'interfaceName',
          'serviceId',
          'branch',
          'interfaceType',
          'requestType'
        ]
      },
      mutations: {
        updateAlarmVisibleColumns(state, payload) {
          state.alarmVisibleColumns = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        },
        updateFilter(state, payload) {
          state.filter = payload;
        },
        updateInterfaceType(state, payload) {
          state.interfaceType = payload;
        },
        updateInterfaceName(state, payload) {
          state.interfaceName = payload;
        },
        updateBranchName(state, payload) {
          state.branchName = payload;
        },
        updateTransactionCode(state, payload) {
          state.transactionCode = payload;
        }
      }
    },
    //持续集成列表
    appCIList: {
      namespaced: true,
      state: {
        visibleColumns: ['name', 'yaml_name'] // 选择列
      },
      mutations: {
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },

    // Vip打包通道
    appVipList: {
      namespaced: true,
      state: {
        name_en: null, // 应用中文名或英文名
        branch: '', // 分支
        status: '', // 状态
        triggerer: null // 触发者
      },
      mutations: {
        updateNameen(state, payload) {
          state.name_en = payload;
        },
        updateRef(state, payload) {
          state.branch = payload;
        },
        updateStatus(state, payload) {
          state.status = payload;
        },
        updateTriggerer(state, payload) {
          state.triggerer = payload;
        }
      }
    }
  }
};
