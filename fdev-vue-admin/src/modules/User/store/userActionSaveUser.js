export default {
  namespaced: true,
  modules: {
    // 用户查询
    userQuery: {
      namespaced: true,
      state: {
        terms: null, // 搜索框
        tableCompany: {
          //公司
          label: '全部',
          value: 'total'
        },
        tableGroup: {
          //小组
          label: '全部',
          value: 'total'
        },
        tableRole: {
          //角色
          label: '全部',
          value: 'total'
        },
        tableSection: '', //条线
        tableIsLeave: { label: '在职', value: '0' }, //在职/离职
        tableIsParty: { label: '全部', value: 'total' }, //政治面貌
        tableArea: { name: '全部', id: 'total' },
        tableFunction: '', //职能
        visibleColumns: [
          'user_name_cn',
          'email',
          'telephone',
          'group',
          'role',
          'company'
        ]
      },
      mutations: {
        updateTerms(state, payload) {
          state.terms = payload;
        },
        updateTableCompany(state, payload) {
          state.tableCompany = payload;
        },
        updateTableGroup(state, payload) {
          state.tableGroup = payload;
        },
        updateTableRole(state, payload) {
          state.tableRole = payload;
        },
        updateTableIsLeave(state, payload) {
          state.tableIsLeave = payload;
        },
        updateTableFunction(state, payload) {
          state.tableFunction = payload;
        },
        updateTableIsParty(state, payload) {
          state.tableIsParty = payload;
        },
        updateTableArea(state, payload) {
          state.tableArea = payload;
        },
        updateTableSection(state, payload) {
          state.tableSection = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    // 维度查询
    dimensionQuery: {
      namespaced: true,
      modules: {
        // 公司、角色
        companiesAndRoles: {
          namespaced: true,
          state: {
            companyType: '0', //公司组 使用状态 ‘0’代表‘使用’，‘1’代表‘废弃’
            roleType: '0' // 角色组 使用状态 ‘0’代表‘使用’，‘1’代表‘废弃’
          },
          mutations: {
            updateCompanyType(state, payload) {
              state.companyType = payload;
            },
            updateRoleType(state, payload) {
              state.roleType = payload;
            }
          }
        },
        // 小组
        groupDimension: {
          namespaced: true,
          state: {
            showChildGroup: false, // 展示子组
            visibleColumns: ['name', 'email', 'telephone', 'company', 'group']
          },
          mutations: {
            updateShowChildGroup(state, payload) {
              state.showChildGroup = payload;
            },
            updateVisibleColumns(state, payload) {
              state.visibleColumns = payload;
            }
          }
        }
      }
    }
  }
};
