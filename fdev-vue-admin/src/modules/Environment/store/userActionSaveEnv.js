/*
本文件公用的注释如下：
visibleCols  用户可选的列
currentPage  页面分页信息
termsApp  搜索条件
**/
export default {
  namespaced: true,
  modules: {
    //配置依赖分析
    aloneConfFile: {
      namespaced: true,
      state: {
        tabs: 'field'
      },
      mutations: {
        saveTabs(state, payload) {
          state.tabs = payload;
        }
      }
    },
    configDepAnalysis: {
      namespaced: true,
      state: {
        visibleCols: [
          'name_cn',
          'name_en',
          'app_manager',
          'spdb_manager',
          'group',
          'gitlab',
          'branch'
        ],
        currentPage: {
          rowsPerPage: 5
        },
        termsApp: {
          name_en: '', //实体英文名
          field_name_en: '', //实体属性
          range: ['master'] //搜索范围
        }
      },
      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveNameEn(state, payload) {
          state.termsApp.name_en = payload;
        },
        saveFieldNameEn(state, payload) {
          state.termsApp.field_name_en = payload;
        },
        saveRange(state, payload) {
          state.termsApp.range = payload;
        }
      }
    },
    createConfigFile: {
      namespaced: true,
      state: {
        visibleCols: [
          'name_cn',
          'name_en',
          'app_manager',
          'spdb_manager',
          'group',
          'gitlab',
          'branch'
        ],
        currentPage: {
          rowsPerPage: 5
        },
        model: {
          mappingValue: '',
          attributeObj: '',
          entityObj: '',
          range: []
        }
      },
      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveEntityObj(state, payload) {
          state.model.entityObj = payload;
        },
        saveAttributeObj(state, payload) {
          state.model.attributeObj = payload;
        },
        saveRange(state, payload) {
          state.model.range = payload;
        },
        saveMappingValue(state, payload) {
          state.model.mappingValue = payload;
        }
      }
    },
    mappingEnv: {
      namespaced: true,
      state: {
        visibleCols: [
          'name_cn',
          'name_en',
          'app_manager',
          'spdb_manager',
          'group',
          'gitlab',
          'branch'
        ],
        currentPage: {
          rowsPerPage: 5
        },
        terms: {
          value: '', //映射值
          type: '', //实体一级分类
          labels: [] //环境标签
        }
      },
      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveValue(state, payload) {
          state.terms.value = payload;
        },
        saveType(state, payload) {
          state.terms.type = payload;
        },
        saveLabels(state, payload) {
          state.terms.labels = payload;
        }
      }
    },
    deployMessage: {
      namespaced: true,
      state: {
        visibleCols: [
          'appInfo',
          'group',
          'modelSet',
          'testEnv',
          'productEnv',
          'btn'
        ],
        currentPage: {
          rowsPerPage: 5,
          page: 1,
          rowsNumber: 0
        },
        terms: {
          env: null, //环境
          app: null //应用
        }
      },
      mutations: {
        saveEnv(state, payload) {
          state.terms.env = payload;
        },
        saveApp(state, payload) {
          state.terms.app = payload;
        },
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        }
      }
    },
    envList: {
      namespaced: true,
      state: {
        visibleCols: [
          'env_name_cn',
          'env_name_en',
          'env_labels',
          'env_message',
          'btn'
        ],
        currentPage: {
          rowsPerPage: 5
        },
        terms: [] //用户输入数据
      },
      getters: {
        searchValue(state) {
          return state.terms.toString();
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
          state.terms = payload;
        }
      }
    },
    modelList: {
      namespaced: true,
      state: {
        visibleCols: [
          'name_cn',
          'name_en',
          'one_type',
          'two_type',
          'suffix_name',
          'action_scope',
          'desc',
          'model_template_id',
          'platform',
          'btn'
        ],
        currentPage: {
          rowsPerPage: 5,
          page: 1,
          rowsNumber: 0
        },
        terms: {
          modelNameEn: '', //组件英文名
          modelNameCn: '', //组件中文名
          modelTemplateId: null //模板id
        }
      },
      mutations: {
        saveModelNameEn(state, payload) {
          state.terms.modelNameEn = payload;
        },
        saveModelNameCn(state, payload) {
          state.terms.modelNameCn = payload;
        },
        saveModelTemplateId(state, payload) {
          state.terms.modelTemplateId = payload;
        },
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        }
      }
    },
    modelEnv: {
      namespaced: true,
      state: {
        visibleCols: [
          'model',
          'model_name_en',
          'env',
          'env_name_en',
          'labels',
          'desc',
          'btn'
        ],
        currentPage: {
          rowsPerPage: 5,
          page: 1,
          rowsNumber: 0
        },
        terms: {
          selectedModel: '', //实体
          selectedEnv: '', //环境
          labels: null //环境标签
        }
      },
      mutations: {
        saveSelectedModel(state, payload) {
          state.terms.selectedModel = payload;
        },
        saveSelectedEnv(state, payload) {
          state.terms.selectedEnv = payload;
        },
        saveLabels(state, payload) {
          state.terms.labels = payload;
        },
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        }
      }
    },
    modelGroupList: {
      namespaced: true,
      state: {
        visibleCols: ['name_cn', 'modelsInfo', 'template', 'btn'],
        currentPage: {
          rowsPerPage: 5,
          page: 1,
          rowsNumber: 0
        },
        terms: {
          template: '', //实体组模板
          modelGroupCn: '', //实体组名称
          selectedModel: null //实体
        }
      },
      mutations: {
        saveTemplate(state, payload) {
          state.terms.template = payload;
        },
        saveModelGroupCn(state, payload) {
          state.terms.modelGroupCn = payload;
        },
        saveSelectedModel(state, payload) {
          state.terms.selectedModel = payload;
        },
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        }
      }
    },
    modelMessage: {
      namespaced: true,
      state: {
        visibleCols: [
          'modelNameCn',
          'modelNameEn',
          'envNameCn',
          'envNameEn',
          'status',
          'type',
          'applyUsername',
          'createTime',
          'id',
          'btn'
        ],
        currentPage: {
          rowsPerPage: 5,
          page: 1,
          rowsNumber: 0
        },
        terms: {
          model: '', //实体
          appllyUser: '', //申请人
          status: null //状态
        }
      },
      mutations: {
        saveModel(state, payload) {
          state.terms.model = payload;
        },
        saveAppllyUser(state, payload) {
          state.terms.appllyUser = payload;
        },
        saveStatus(state, payload) {
          state.terms.status = payload;
        },
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        }
      }
    },
    modelTemplate: {
      namespaced: true,
      state: {
        visibleCols: [
          'nameCn',
          'nameEn',
          'desc'
          // 'btn'
        ],
        currentPage: {
          rowsPerPage: 5,
          page: 1,
          rowsNumber: 0
        },
        terms: {
          modelNameEn: '', //实体模板英文名
          modelNameCn: '', //实体模板中文名
          modelEnvKey: '' //实体模板属性英文名
        }
      },
      mutations: {
        saveModelNameEn(state, payload) {
          state.terms.modelNameEn = payload;
        },
        saveModelNameCn(state, payload) {
          state.terms.modelNameCn = payload;
        },
        saveModelEnvKey(state, payload) {
          state.terms.modelEnvKey = payload;
        },
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        }
      }
    },
    productionInfoList: {
      namespaced: true,
      state: {
        params: {
          type: '',
          value: ''
        },
        visibleColumns: [
          'deploy_name',
          'team',
          'dev_managers',
          'spdb_managers',
          'platform'
        ],
        currentPage: {
          rowsPerPage: 5,
          page: 1,
          rowsNumber: 0
        }
      },
      mutations: {
        saveSearchType(state, payload) {
          state.params.type = payload;
        },
        saveSearchValue(state, payload) {
          state.params.value = payload;
        },
        saveVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        }
      }
    }
  }
};
