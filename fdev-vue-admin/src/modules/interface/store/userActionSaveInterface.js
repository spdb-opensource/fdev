export default {
  namespaced: true,
  modules: {
    // 接口
    interFace: {
      namespaced: true,
      modules: {
        // 接口列表
        interfaceList: {
          namespaced: true,
          state: {
            interfaceTypeMaster: 'REST',
            interfaceTypeSit: 'REST',
            interfaceTypeOther: 'REST',
            interfaceModelMaster: {
              id: '',
              serviceCalling: '',
              transactionCode: '',
              branchName: '',
              url: '',
              requestType: '',
              requestProtocol: '',
              interfaceDesp: '',
              serviceConsumer: '',
              transId: '',
              serviceId: '',
              transOrServiceOrOperation: '',
              serviceProvider: ''
            },
            interfaceModelSit: {
              id: '',
              serviceCalling: '',
              transactionCode: '',
              branchName: '',
              url: '',
              requestType: '',
              requestProtocol: '',
              interfaceDesp: '',
              serviceConsumer: '',
              transId: '',
              serviceId: '',
              transOrServiceOrOperation: '',
              serviceProvider: ''
            },
            interfaceModelOther: {
              id: '',
              serviceCalling: '',
              transactionCode: '',
              branchName: '',
              url: '',
              requestType: '',
              requestProtocol: '',
              interfaceDesp: '',
              serviceConsumer: '',
              transId: '',
              serviceId: '',
              transOrServiceOrOperation: '',
              serviceProvider: ''
            },
            visibleColumnsMaster: [
              'transId',
              'interfaceName',
              'serviceId',
              'branch',
              'interfaceType'
            ],
            visibleColumnsSit: [
              'transId',
              'interfaceName',
              'serviceId',
              'branch',
              'interfaceType'
            ],
            visibleColumnsOther: [
              'transId',
              'interfaceName',
              'serviceId',
              'branch',
              'interfaceType'
            ]
          },
          mutations: {
            updateInterfaceTypeMaster(state, payload) {
              state.interfaceTypeMaster = payload;
            },
            updateInterfaceTypeSit(state, payload) {
              state.interfaceTypeSit = payload;
            },
            updateInterfaceTypeOther(state, payload) {
              state.interfaceTypeOther = payload;
            },

            updateServiceProviderMaster(state, payload) {
              state.interfaceModelMaster.serviceProvider = payload;
            },
            updateServiceProviderSit(state, payload) {
              state.interfaceModelSit.serviceProvider = payload;
            },
            updateServiceProviderOther(state, payload) {
              state.interfaceModelOther.serviceProvider = payload;
            },
            updateBranchNameOther(state, payload) {
              state.interfaceModelOther.branchName = payload;
            },

            updateCoderMaster(state, payload) {
              state.interfaceModelMaster.transactionCode = payload;
            },
            updateCodeSit(state, payload) {
              state.interfaceModelSit.transactionCode = payload;
            },
            updateCodeOther(state, payload) {
              state.interfaceModelOther.transactionCode = payload;
            },
            updateBranchName(state, payload) {
              state.interfaceModelOther.branchName = payload;
            },
            updateColumnsMaster(state, payload) {
              state.visibleColumnsMaster = payload;
            },
            updateColumnsSit(state, payload) {
              state.visibleColumnsSit = payload;
            },
            updateColumnsOther(state, payload) {
              state.visibleColumnsOther = payload;
            }
          }
        },
        //调用关系
        interfaceRelation: {
          namespaced: true,
          state: {
            interfaceModelMaster: {
              id: '',
              serviceCalling: '',
              transactionCode: '',
              branchName: '',
              url: '',
              requestType: '',
              requestProtocol: '',
              interfaceDesp: '',
              serviceConsumer: '',
              transId: '',
              serviceId: '',
              transOrServiceOrOperation: ''
            },
            interfaceTypeMaster: '调用报文类型',
            interfaceTypeSit: '调用报文类型',
            interfaceTypeOther: '调用报文类型',
            visibleColumnsMaster: [
              'transId',
              'interfaceName',
              'serviceId',
              'serviceCalling',
              'branch',
              'interfaceType'
            ],
            interfaceModelSit: {
              id: '',
              serviceCalling: '',
              transactionCode: '',
              branchName: '',
              url: '',
              requestType: '',
              requestProtocol: '',
              interfaceDesp: '',
              serviceConsumer: '',
              transId: '',
              serviceId: '',
              transOrServiceOrOperation: ''
            },
            visibleColumnsSit: [
              'transId',
              'interfaceName',
              'serviceId',
              'serviceCalling',
              'branch',
              'interfaceType'
            ],
            interfaceModelOther: {
              id: '',
              serviceCalling: '',
              transactionCode: '',
              branchName: '',
              url: '',
              requestType: '',
              requestProtocol: '',
              interfaceDesp: '',
              serviceConsumer: '',
              transId: '',
              serviceId: '',
              transOrServiceOrOperation: ''
            },
            visibleColumnsOther: [
              'transId',
              'interfaceName',
              'serviceId',
              'serviceCalling',
              'branch',
              'interfaceType'
            ]
          },
          mutations: {
            updateServiceIdMaster(state, payload) {
              state.interfaceModelMaster.serviceId = payload;
            },
            updateServiceIdSit(state, payload) {
              state.interfaceModelSit.serviceId = payload;
            },
            updateServiceIdOther(state, payload) {
              state.interfaceModelOther.serviceId = payload;
            },

            updateCallingMaster(state, payload) {
              state.interfaceModelMaster.serviceCalling = payload;
            },
            updateCallingSit(state, payload) {
              state.interfaceModelSit.serviceCalling = payload;
            },
            updateCallingOther(state, payload) {
              state.interfaceModelOther.serviceCalling = payload;
            },

            updateBranchNameOther(state, payload) {
              state.interfaceModelOther.branchName = payload;
            },

            updateInputMaster(state, payload) {
              state.interfaceModelMaster.transOrServiceOrOperation = payload;
            },
            updateInputSit(state, payload) {
              state.interfaceModelSit.transOrServiceOrOperation = payload;
            },
            updateInputOther(state, payload) {
              state.interfaceModelOther.transOrServiceOrOperation = payload;
            },

            updateTypeMaster(state, payload) {
              state.interfaceTypeMaster = payload;
            },
            updateTypeSit(state, payload) {
              state.interfaceTypeSit = payload;
            },
            updateTypeOther(state, payload) {
              state.interfaceTypeOther = payload;
            },

            updateColumnsMaster(state, payload) {
              state.visibleColumnsMaster = payload;
            },
            updateColumnsSit(state, payload) {
              state.visibleColumnsSit = payload;
            },
            updateColumnsOther(state, payload) {
              state.visibleColumnsOther = payload;
            }
          }
        },
        // Yapi项目导入
        YapiProjectImport: {
          namespaced: true,
          state: {
            filterStr: '' // 搜索项
          },
          mutations: {
            updateFilterStr(state, payload) {
              state.filterStr = payload;
            }
          }
        },
        // Yapi项目详情
        yapiProfile: {
          namespaced: true,
          state: {
            filterStr: '' // 搜索项
          },
          mutations: {
            updateFilterStr(state, payload) {
              state.filterStr = payload;
            }
          }
        }
      }
    },
    //交易
    interfaceTrading: {
      namespaced: true,
      modules: {
        //交易列表
        interfaceTradingList: {
          namespaced: true,
          state: {
            transModelMaster: {
              transId: '',
              transName: '',
              serviceId: '',
              branch: '',
              channel: '',
              writeJnl: '',
              needLogin: '',
              verCodeType: 'all',
              tags: '',
              branchDefault: 'master'
            },
            visibleColumnsMaster: [
              'transId',
              'transName',
              'serviceId',
              'branch',
              'channelIdMap',
              'writeJnl',
              'needLogin',
              'verCodeType',
              'tag'
            ],
            transModelSit: {
              transId: '',
              transName: '',
              serviceId: '',
              branch: '',
              channel: '',
              writeJnl: '',
              needLogin: '',
              verCodeType: 'all',
              tags: '',
              branchDefault: 'master'
            },
            visibleColumnsSit: [
              'transId',
              'transName',
              'serviceId',
              'branch',
              'channelIdMap',
              'writeJnl',
              'needLogin',
              'verCodeType',
              'tag'
            ],
            transModelOther: {
              transId: '',
              transName: '',
              serviceId: '',
              branch: '',
              channel: '',
              writeJnl: '',
              needLogin: '',
              verCodeType: 'all',
              tags: '',
              branchDefault: 'master'
            },
            visibleColumnsOther: [
              'transId',
              'transName',
              'serviceId',
              'branch',
              'channelIdMap',
              'writeJnl',
              'needLogin',
              'verCodeType',
              'tag'
            ]
          },
          mutations: {
            updateServiceIdMaster(state, payload) {
              state.transModelMaster.serviceId = payload;
            },
            updateServiceIdSit(state, payload) {
              state.transModelSit.serviceId = payload;
            },
            updateServiceIdOther(state, payload) {
              state.transModelOther.serviceId = payload;
            },

            updateChannelMaster(state, payload) {
              state.transModelMaster.channel = payload;
            },
            updateChannelSit(state, payload) {
              state.transModelSit.channel = payload;
            },
            updateChannelOther(state, payload) {
              state.transModelOther.channel = payload;
            },

            updateWriteJnlMaster(state, payload) {
              state.transModelMaster.writeJnl = payload;
            },
            updateWriteJnlSit(state, payload) {
              state.transModelSit.writeJnl = payload;
            },
            updateWriteJnlOther(state, payload) {
              state.transModelOther.writeJnl = payload;
            },

            updateLoginMaster(state, payload) {
              state.transModelMaster.needLogin = payload;
            },
            updateLoginSit(state, payload) {
              state.transModelSit.needLogin = payload;
            },
            updateLoginOther(state, payload) {
              state.transModelOther.needLogin = payload;
            },

            updateVerCodeMaster(state, payload) {
              state.transModelMaster.verCodeType = payload;
            },
            updateVerCodeSit(state, payload) {
              state.transModelSit.verCodeType = payload;
            },
            updateVerCodeOther(state, payload) {
              state.transModelOther.verCodeType = payload;
            },

            updateTagsMaster(state, payload) {
              state.transModelMaster.tags = payload;
            },
            updateTagsSit(state, payload) {
              state.transModelSit.tags = payload;
            },
            updateTagsOther(state, payload) {
              state.transModelOther.tags = payload;
            },

            updateTransIdMaster(state, payload) {
              state.transModelMaster.transId = payload;
            },
            updateTransIdSit(state, payload) {
              state.transModelSit.transId = payload;
            },
            updateTransIdOther(state, payload) {
              state.transModelOther.transId = payload;
            },

            updateBranchOther(state, payload) {
              state.transModelOther.branch = payload;
            },

            updateColumnsMaster(state, payload) {
              state.visibleColumnsMaster = payload;
            },
            updateColumnsSit(state, payload) {
              state.visibleColumnsSit = payload;
            },
            updateColumnsOther(state, payload) {
              state.visibleColumnsOther = payload;
            }
          }
        },
        //调用关系
        invokeRelationships: {
          namespaced: true,
          state: {
            invokeModelMaster: {
              transId: '1',
              serviceCalling: '', // 服务调用方
              serviceId: '', // 服务提供方
              branch: '',
              channel: ''
            },
            visibleColumnsMaster: [
              'transId',
              'transName',
              'serviceId',
              'serviceCalling',
              'branch',
              'channel'
            ],
            invokeModelSit: {
              transId: '',
              serviceCalling: '', // 服务调用方
              serviceId: '', // 服务提供方
              branch: '',
              channel: ''
            },
            visibleColumnsSit: [
              'transId',
              'transName',
              'serviceId',
              'serviceCalling',
              'branch',
              'channel'
            ],
            invokeModelOther: {
              transId: '',
              serviceCalling: '', // 服务调用方
              serviceId: '', // 服务提供方
              branch: '',
              channel: ''
            },
            visibleColumnsOther: [
              'transId',
              'transName',
              'serviceId',
              'serviceCalling',
              'branch',
              'channel'
            ]
          },
          mutations: {
            updateServiceIdMaster(state, payload) {
              state.invokeModelMaster.serviceId = payload;
            },
            updateServiceIdSit(state, payload) {
              state.invokeModelSit.serviceId = payload;
            },
            updateServiceIdOther(state, payload) {
              state.invokeModelOther.serviceId = payload;
            },

            updateCallingMaster(state, payload) {
              state.invokeModelMaster.serviceCalling = payload;
            },
            updateCallingSit(state, payload) {
              state.invokeModelSit.serviceCalling = payload;
            },
            updateCallingOther(state, payload) {
              state.invokeModelOther.serviceCalling = payload;
            },

            updateChannelMaster(state, payload) {
              state.invokeModelMaster.channel = payload;
            },
            updateChannelSit(state, payload) {
              state.invokeModelSit.channel = payload;
            },
            updateChannelOther(state, payload) {
              state.invokeModelOther.channel = payload;
            },

            updateBranchOther(state, payload) {
              state.invokeModelOther.branch = payload;
            },

            updateTransIdMaster(state, payload) {
              state.invokeModelMaster.transId = payload;
            },
            updateTransIdSit(state, payload) {
              state.invokeModelSit.transId = payload;
            },
            updateTransIdOther(state, payload) {
              state.invokeModelOther.transId = payload;
            },

            updateColumnsMaster(state, payload) {
              state.visibleColumnsMaster = payload;
            },
            updateColumnsSit(state, payload) {
              state.visibleColumnsSit = payload;
            },
            updateColumnsOther(state, payload) {
              state.visibleColumnsOther = payload;
            }
          }
        }
      }
    },
    // 动态
    dynamic: {
      namespaced: true,
      modules: {
        // 扫描记录
        scanningRecords: {
          namespaced: true,
          state: {
            service_id: '', // 应用英文名
            GroupId: '', // 应用所属小组
            branch: '', // 分支
            type: '', // 触发扫描方式
            StartTime: '', // 开始日期
            EndTime: '', // 结束日期
            IsScanSuccessFlag: '0', // 扫描成功
            RecentlyScanFlag: '0', // 最近扫描
            visibleColumns: [
              'serviceId',
              'group',
              'branch',
              'type',
              'rest',
              'restRel',
              'soap',
              'soapRel',
              'sopRel',
              'trans',
              'transRel',
              'router',
              'scanTime'
            ] // 选择列
          },
          mutations: {
            updateServiceId(state, payload) {
              state.service_id = payload;
            },
            updateGroupId(state, payload) {
              state.GroupId = payload;
            },
            updateBranch(state, payload) {
              state.branch = payload;
            },
            updateType(state, payload) {
              state.type = payload;
            },
            updateStartTime(state, payload) {
              state.StartTime = payload;
            },
            updateEndTime(state, payload) {
              state.EndTime = payload;
            },
            updateIsScanSuccessFlag(state, payload) {
              state.IsScanSuccessFlag = payload;
            },
            updateRecentlyScanFlag(state, payload) {
              state.RecentlyScanFlag = payload;
            },
            updateVisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        }
      }
    },
    // 审批
    approval: {
      namespaced: true,
      modules: {
        // 接口审批
        interfaceApproval: {
          namespaced: true,
          state: {
            serviceId: '', // 提供方应用
            serviceCalling: '', // 调用方应用
            applicant: '', // 申请人
            approver: '', // 审批人
            transId: '', // 交易码
            tableIsApproval: { label: '待审批', value: '1' }, // 审批状态
            visibleColumns: [
              'transId',
              'serviceCalling',
              'serviceId',
              'applicant',
              'approver',
              'reason',
              'refuseReason',
              'status'
            ] // 选择列
          },
          mutations: {
            updateServiceId(state, payload) {
              state.serviceId = payload;
            },
            updateServiceCalling(state, payload) {
              state.serviceCalling = payload;
            },
            updateApplicant(state, payload) {
              state.applicant = payload;
            },
            updateApprover(state, payload) {
              state.approver = payload;
            },
            updateTransId(state, payload) {
              state.transId = payload;
            },
            updateTableIsApproval(state, payload) {
              state.tableIsApproval = payload;
            },
            updateVisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        }
      }
    },
    // 前端路由
    frontRouter: {
      namespaced: true,
      modules: {
        // 路由
        routerList: {
          namespaced: true,
          state: {
            name: '', // 场景名称
            module: '', // 加载容器
            ver: null, // 版本
            routerBranch: '', // 分支
            projectName: '', // 所属项目名称
            visibleColumns: [
              'name',
              'module',
              'ver',
              'projectName',
              'path',
              'branch'
            ] // 选择列
          },
          mutations: {
            updateName(state, payload) {
              state.name = payload;
            },
            updateModule(state, payload) {
              state.module = payload;
            },
            updateVer(state, payload) {
              state.ver = payload;
            },
            updateRouterBranch(state, payload) {
              state.routerBranch = payload;
            },
            updateProjectName(state, payload) {
              state.projectName = payload;
            },
            updateVisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        },
        // 路由关系
        routerRelation: {
          namespaced: true,
          state: {
            routerName: '', // 场景名称
            routerBranch: '', // 分支
            sourceProject: '', // 调用项目名称
            targetProject: '', // 路由提供项目名称
            visibleColumns: [
              'name',
              'sourceProject',
              'targetProject',
              'branch',
              'createTime'
            ] // 选择列
          },
          mutations: {
            updateRouterName(state, payload) {
              state.routerName = payload;
            },
            updateRouterBranch(state, payload) {
              state.routerBranch = payload;
            },
            updateSourceProject(state, payload) {
              state.sourceProject = payload;
            },
            updateTargetProject(state, payload) {
              state.targetProject = payload;
            },
            updateVisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        },
        // 应用路由配置介质
        routerAppConfig: {
          namespaced: true,
          state: {
            name: '', // 场景名称
            routerBranch: '', // 分支
            projectName: '', // 所属项目名称
            visibleColumns: [
              'projectName',
              'branch',
              'routeNum',
              'routesVersion',
              'cicdTime',
              'repoTarName'
            ] // 选择列
          },
          mutations: {
            updateName(state, payload) {
              state.name = payload;
            },
            updateRouterBranch(state, payload) {
              state.routerBranch = payload;
            },
            updateProjectName(state, payload) {
              state.projectName = payload;
            },
            updateVisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        },
        // 总路由配置介质
        routerAppTotal: {
          namespaced: true,
          state: {
            projectName: '', // 所属项目名称
            visibleColumns: [
              'env',
              'datTime',
              'appNum',
              'centralJson',
              'totalTarName'
            ] // 选择列
          },
          mutations: {
            updateProjectName(state, payload) {
              state.projectName = payload;
            },
            updateVisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        }
      }
    },
    // 服务链路
    ServiceLink: {
      namespaced: true,
      state: {
        app: null, // 应用
        branch: 'master' // 分支
      },
      mutations: {
        updateApp(state, payload) {
          state.app = payload;
        },
        updateBranch(state, payload) {
          state.branch = payload;
        }
      }
    }
  }
};
