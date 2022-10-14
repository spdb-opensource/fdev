export default {
  namespaced: true,
  modules: {
    //镜像详情
    imageManageProfile: {
      namespaced: true,
      modules: {
        //骨架使用现状
        imageManageProfileArche: {
          namespaced: true,
          state: {
            selectValue: [], //输入框搜索栏
            statusType: '0', //状态
            visibleColumns: [
              'name_en',
              'name_zh',
              'archetype_version',
              'image_tag',
              'stage',
              'update_time'
            ] //可视列筛选框
          },
          mutations: {
            updateSelectValue(state, payload) {
              state.selectValue = payload;
            },
            updateType(state, payload) {
              state.statusType = payload;
            },
            updatevisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        },
        //应用使用现状
        appUsingStatus: {
          namespaced: true,
          state: {
            selectValue: [], //搜索框
            useVersion: '全部', //使用版本
            type: '全部', //版本类型
            visibleColumns: [
              'name_en',
              'name_zh',
              'image_tag',
              'stage',
              'group',
              'spdb_managers',
              'dev_managers',
              'update_time'
            ] //可视列筛选框
          },
          mutations: {
            updateSelectValue(state, payload) {
              state.selectValue = payload;
            },
            updateUseVersion(state, payload) {
              state.useVersion = payload;
            },
            updateType(state, payload) {
              state.type = payload;
            },
            updatevisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        },
        //优化需求
        imageManageOptimize: {
          namespaced: true,
          state: {
            visibleColumns: [
              'title',
              'branch',
              'name_cn',
              'stage',
              'due_date',
              'btn'
            ] //可视列筛选框
          },
          mutations: {
            updatevisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        }
      }
    },
    //镜像管理
    imageManage: {
      namespaced: true,
      state: {
        selectValue: [], //输入框搜索栏
        visibleColumns: [
          'name',
          'name_cn',
          'manager',
          'group',
          'type',
          'target_env',
          'description'
        ] //可视列筛选框
      },
      mutations: {
        updateSelectValue(state, payload) {
          state.selectValue = payload;
        },
        updatevisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    //骨架管理
    archetypeManage: {
      namespaced: true,
      modules: {
        //前端骨架
        webArchetype: {
          namespaced: true,
          modules: {
            //前端骨架详情优化需求tab页
            webArchetypeOptimize: {
              namespaced: true,
              state: {
                visibleColumns: [
                  'title',
                  'desc',
                  'feature_branch',
                  'due_date',
                  'btn'
                ] //可视列筛选框
              },
              mutations: {
                updatevisibleColumns(state, payload) {
                  state.visibleColumns = payload;
                }
              }
            }
          },
          state: {
            selectValue: [], //输入框搜索栏
            visibleColumns: [
              'name_en',
              'name_cn',
              'manager',
              'group',
              'recommend_version',
              'type',
              'desc',
              'btn'
            ] //可视列筛选框
          },
          mutations: {
            updateSelectValue(state, payload) {
              state.selectValue = payload;
            },
            updatevisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        },
        //后端骨架
        archetype: {
          namespaced: true,
          modules: {
            //后端骨架详情优化需求tab页
            archetypeOptimize: {
              namespaced: true,
              state: {
                visibleColumns: [
                  'title',
                  'name_cn',
                  'feature_branch',
                  'stage',
                  'btn'
                ] //可视列筛选框
              },
              mutations: {
                updatevisibleColumns(state, payload) {
                  state.visibleColumns = payload;
                }
              }
            }
          },
          state: {
            selectValue: [], //输入框搜索栏
            visibleColumns: [
              'name_en',
              'name_cn',
              'manager_id',
              'group',
              'recommend_version',
              'type',
              'desc',
              'btn'
            ] //可视列筛选框
          },
          mutations: {
            updateSelectValue(state, payload) {
              state.selectValue = payload;
            },
            updatevisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        }
      }
    },
    //组件管理
    componentManage: {
      namespaced: true,
      modules: {
        //前端组件
        weblist: {
          namespaced: true,
          modules: {
            //前端组件详情投产窗口tab
            webOptimize: {
              namespaced: true,
              state: {
                visibleColumns: [
                  'title',
                  'desc',
                  'manager',
                  'issue_type',
                  'feature_branch',
                  'predict_version',
                  'due_date',
                  'btn'
                ] //可视列筛选框
              },
              mutations: {
                updatevisibleColumns(state, payload) {
                  state.visibleColumns = payload;
                }
              }
            },
            //前端组件详情应用使用现状tab
            webAppUsing: {
              namespaced: true,
              state: {
                selectValue: [], //搜索框
                useVersion: '全部', //使用版本
                type: '全部', //版本类型
                visibleColumns: [
                  'name_en',
                  'name_zh',
                  'component_version',
                  'type',
                  'group',
                  'spdb_managers',
                  'dev_managers',
                  'update_time'
                ] //可视列筛选框
              },
              mutations: {
                updateSelectValue(state, payload) {
                  state.selectValue = payload;
                },
                updateUseVersion(state, payload) {
                  state.useVersion = payload;
                },
                updateType(state, payload) {
                  state.type = payload;
                },
                updatevisibleColumns(state, payload) {
                  state.visibleColumns = payload;
                }
              }
            },
            //前端组件集成现状-应用维度/组件维度tab页
            WebIntergration: {
              namespaced: true,
              state: {
                appId: '', //现有应用
                comId: '', //选择组件
                visibleColumns: [
                  'name',
                  'component_version',
                  'type',
                  'update_time'
                ], //可视列筛选框
                visibleColumnsCom: [
                  'name',
                  'component_version',
                  'type',
                  'update_time'
                ]
              },
              mutations: {
                updateAppId(state, payload) {
                  state.appId = payload;
                },
                updateComId(state, payload) {
                  state.comId = payload;
                },
                updatevisibleColumns(state, payload) {
                  state.visibleColumns = payload;
                },
                updatevisibleColumnsCom(state, payload) {
                  state.visibleColumnsCom = payload;
                }
              }
            }
          },
          state: {
            selectValue: [], //输入框搜索栏
            visibleColumns: [
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
              'desc',
              'btn',
              'skillstack',
              'businessarea'
            ] //可视列筛选框
          },
          mutations: {
            updateSelectValue(state, payload) {
              state.selectValue = payload;
            },
            updatevisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        },
        //后端组件
        componentList: {
          namespaced: true,
          modules: {
            //后端组件骨架使用现状
            ArcheUsing: {
              namespaced: true,
              state: {
                selectValue: [], //输入框搜索栏
                type: '0',
                componentType: '0',
                visibleColumns: [
                  'name_en',
                  'name_zh',
                  'archetype_version',
                  'component_version',
                  'type',
                  'component_type',
                  'update_time'
                ] //可视列筛选框
              },
              mutations: {
                updateSelectValue(state, payload) {
                  state.selectValue = payload;
                },
                updateType(state, payload) {
                  state.type = payload;
                },
                updateComponentType(state, payload) {
                  state.componentType = payload;
                },
                updatevisibleColumns(state, payload) {
                  state.visibleColumns = payload;
                }
              }
            },
            //应用使用现状
            appUsingStatus: {
              namespaced: true,
              state: {
                selectValue: [], //搜索框
                useVersion: '全部', //使用版本
                type: '全部', //版本类型
                visibleColumns: [
                  'name_en',
                  'name_zh',
                  'component_version',
                  'type',
                  'group',
                  'spdb_managers',
                  'dev_managers',
                  'update_time'
                ] //可视列筛选框
              },
              mutations: {
                updateSelectValue(state, payload) {
                  state.selectValue = payload;
                },
                updateUseVersion(state, payload) {
                  state.useVersion = payload;
                },
                updateType(state, payload) {
                  state.type = payload;
                },
                updatevisibleColumns(state, payload) {
                  state.visibleColumns = payload;
                }
              }
            },
            //后端组件rel优化页
            OptimizeRel: {
              namespaced: true,
              state: {
                visibleColumns: [
                  'title',
                  'desc',
                  'manager',
                  'issue_type',
                  'feature_branch',
                  'predict_version',
                  'due_date',
                  'btn'
                ] //可视列筛选框
              },
              mutations: {
                updatevisibleColumns(state, payload) {
                  state.visibleColumns = payload;
                }
              }
            },
            //后端组件集成现状-应用维度/组件维度tab页
            intergration: {
              namespaced: true,
              state: {
                appId: '', //现有应用
                comId: '', //选择组件
                visibleColumns: [
                  'name',
                  'component_version',
                  'type',
                  'update_time'
                ],
                visibleColumnsCom: [
                  'name',
                  'component_version',
                  'type',
                  'update_time'
                ] //可视框
              },
              mutations: {
                updateAppId(state, payload) {
                  state.appId = payload;
                },
                updateComId(state, payload) {
                  state.comId = payload;
                },
                updatevisibleColumns(state, payload) {
                  state.visibleColumns = payload;
                },
                updatevisibleColumnsCom(state, payload) {
                  state.visibleColumnsCom = payload;
                }
              }
            },
            //后端组件优化需求
            ArcheOptimize: {
              namespaced: true,
              state: {
                visibleColumns: [
                  'title',
                  'name_cn',
                  'feature_branch',
                  'target_version',
                  'stage',
                  'btn'
                ] //可视列筛选框
              },
              mutations: {
                updatevisibleColumns(state, payload) {
                  state.visibleColumns = payload;
                }
              }
            }
          },
          state: {
            selectValue: [], //输入框搜索栏
            type: null,
            visibleColumns: [
              'name_en',
              'name_cn',
              'manager_id',
              'group',
              'recommond_version',
              'type',
              'source',
              'btn'
            ] //可视列筛选框
          },
          mutations: {
            updateSelectValue(state, payload) {
              state.selectValue = payload;
            },
            updateType(state, payload) {
              state.type = payload;
            },
            updatevisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        }
      }
    }
  }
};
