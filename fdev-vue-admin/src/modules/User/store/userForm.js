import {
  queryCompany,
  queryGroup,
  queryGroupAll,
  queryRole,
  queryTag,
  addTag,
  removeTag,
  removeCompany,
  removeGroup,
  removeRole,
  addCompany,
  addGroup,
  addRole,
  updateCompany,
  updateGroup,
  updateRole,
  queryCITemplate,
  querySystem,
  queryDomain,
  queryRedmineById,
  queryChildGroupById
} from '@/services/api';
import {
  queryGroupPeople,
  queryTodos,
  updateLabelById,
  queryArea,
  queryFunction,
  queryRank,
  queryUserPagination,
  queryReBuildGroupName,
  queryRoleForLDAP
} from '@/services/user';
import {
  formatOption,
  wrapOptionsTotal,
  resolveResponseError,
  appendNode,
  addAttribute
} from '@/utils/utils';
import { formatUser as format } from '@/modules/User/utils/model';
export default {
  namespaced: true,

  state: {
    groups: [],
    groupsAll: [],
    abandonGroups: [],
    companies: [],
    abandonCompanies: [],
    roles: [],
    ldapRoles: [],
    abandonRoles: [],
    isLeaves: [
      {
        label: '在职',
        value: '0'
      },
      {
        label: '离职',
        value: '1'
      }
    ],
    tags: [],
    CITemplateData: [],
    systemData: [],
    domainData: [],
    groupPeople: [],
    redmineData: [],
    todosList: [],
    childGroup: [],
    areaList: [],
    functionList: [],
    rankList: [],
    userInPage: '',
    reBuildGroupName: []
  },

  getters: {
    wrapTotal: state => key => wrapOptionsTotal(state[key]),
    groupsTree: state => {
      const { groups } = state;
      const root = groups.filter(group => !group.parent);
      const groupList = appendNode(
        root,
        groups.filter(group => group.id && group.parent)
      );
      return addAttribute(groupList);
    }
  },

  actions: {
    async fetchTag({ commit, state }, payload) {
      const response = await resolveResponseError(() => queryTag());
      commit('saveTag', formatOption(response, 'name'));
    },
    async addTag({ commit, state }, payload) {
      const response = await resolveResponseError(() => addTag(payload));
      let result = [...state.tags, formatOption(response, 'name')];
      commit('saveTag', result);
    },
    async removeTag({ commit, state }, payload) {
      payload = Array.isArray(payload) ? payload : [payload];
      await resolveResponseError(() => removeTag(payload));
      const list = state.tags.filter(
        item => !payload.some(tag => tag.id === item.id)
      );
      commit('saveTag', list);
    },

    async addRole({ commit, state }, payload) {
      const response = await resolveResponseError(() => addRole(payload));
      const list = [...state.roles, formatOption(response, 'name')];
      commit('saveRole', list);
    },
    async queryRoleForLDAP({ commit, state }, payload) {
      const response = await resolveResponseError(() =>
        queryRoleForLDAP(payload)
      );
      commit('saveLdapRoles', formatOption(response, 'name'));
    },
    async fetchRole({ commit, state }, payload) {
      const response = await resolveResponseError(() => queryRole(payload));
      commit('saveRole', formatOption(response, 'name'));
      commit('saveAbandonRole', formatOption(response, 'name'));
    },
    async removeRole({ commit, state }, payload) {
      payload = Array.isArray(payload) ? payload : [payload];
      await resolveResponseError(() => removeRole(payload));
      const list = state.roles.filter(
        item => !payload.some(role => role.id === item.id)
      );
      commit('saveRole', list);
      payload[0].status = '0';
      state.abandonRoles.push(payload[0]);
    },
    async updateRole({ commit, state }, payload) {
      const response = await resolveResponseError(() => updateRole(payload));
      let item = state.roles.find(item => item.id === payload.id);
      Object.assign(item, response);
      const list = [...state.roles];
      commit('saveRole', list);
    },

    async addGroup({ commit, state }, payload) {
      const response = await resolveResponseError(() => addGroup(payload));
      const list = [
        ...state.groupsAll,
        formatOption(
          formatGroup(Object.assign(response, { new: true })),
          'name'
        )
      ];
      commit('saveGroupAll', list);
    },
    // 查询个人组
    async fetchGroup({ commit, state }, payload) {
      const response = await resolveResponseError(() => queryGroup(payload));
      commit(
        'saveGroup',
        formatOption(response.map(group => formatGroup(group)), 'name')
      );
      commit(
        'saveAbandonGroup',
        formatOption(response.map(group => formatGroup(group)), 'name')
      );
    },
    // 查询小组信息，包括小组人数
    async fetchGroupAll({ commit, state }, payload) {
      const response = await resolveResponseError(() => queryGroupAll(payload));
      commit(
        'saveGroupAll',
        formatOption(response.map(group => formatGroup(group)), 'name')
      );
    },
    async removeGroup({ commit, state }, payload) {
      payload = Array.isArray(payload) ? payload : [payload];
      await resolveResponseError(() => removeGroup(payload));
      let list = state.groupsAll.filter(
        item => !payload.some(group => group.id === item.id)
      );
      commit('saveGroupAll', list);
      payload[0].status = '0';
      state.abandonGroups.push(payload[0]);
    },
    async updateGroup({ commit, state }, payload) {
      // const response = await resolveResponseError(() => updateGroup(payload));
      await resolveResponseError(() => updateGroup(payload));
      //let item = state.groupsAll.find(item => item.id === payload.id);
      // Object.assign(item, formatOption(response, 'name'));
      const list = [...state.groupsAll];
      commit('saveGroupAll', list);
    },

    async addCompany({ commit, state }, payload) {
      const response = await resolveResponseError(() => addCompany(payload));
      const list = [...state.companies, formatOption(response, 'name')];
      commit('saveCompany', list);
    },
    async fetchCompany({ commit, state }, payload) {
      const response = await resolveResponseError(() => queryCompany());
      commit('saveCompany', formatOption(response, 'name'));
      commit('saveAbandonCompany', formatOption(response, 'name'));
    },
    async removeCompany({ commit, state }, payload) {
      payload = Array.isArray(payload) ? payload : [payload];
      await resolveResponseError(() => removeCompany(payload));
      const list = state.companies.filter(
        item => !payload.some(company => company.id === item.id)
      );
      commit('saveCompany', list);
      payload[0].status = '0';
      state.abandonCompanies.push(payload[0]);
    },
    async updateCompany({ commit, state }, payload) {
      const response = await resolveResponseError(() => updateCompany(payload));
      let item = state.companies.find(item => item.id === payload.id);
      Object.assign(item, formatOption(response));
      const list = [...state.companies];
      commit('saveCompany', list);
    },
    async queryCITemplate({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryCITemplate(payload)
      );
      commit('saveCITemplate', response);
    },
    async querySystem({ commit }, payload) {
      const response = await resolveResponseError(() => querySystem(payload));
      commit('saveSystem', response);
    },
    async queryDomain({ commit }, payload) {
      const response = await resolveResponseError(() => queryDomain(payload));
      commit('saveDomain', response);
    },
    async queryRedmineById({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryRedmineById(payload)
      );
      commit('saveRedmine', response);
    },
    async queryGroupPeople({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryGroupPeople(payload)
      );
      commit('saveGroupPeopel', response);
    },
    async queryTodos({ commit }, payload) {
      const response = await resolveResponseError(() => queryTodos(payload));
      commit('saveTodosList', response);
    },
    async updateLabelById({ commit }, payload) {
      await resolveResponseError(() => updateLabelById(payload));
    },
    async queryChildGroupById({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryChildGroupById(payload)
      );
      commit('saveChildGroup', response);
    },
    async queryArea({ commit }, payload) {
      const response = await resolveResponseError(() => queryArea(payload));
      commit('saveArea', response);
    },
    async queryFunction({ commit }, payload) {
      const response = await resolveResponseError(() => queryFunction(payload));
      commit('saveFunction', response);
    },
    async queryRank({ commit }, payload) {
      const response = await resolveResponseError(() => queryRank(payload));
      commit('saveRank', response);
    },
    //用户查询页面分页查询
    async queryUserPagination({ commit }, payload) {
      let { list, total } = await resolveResponseError(() =>
        queryUserPagination(payload)
      );
      list = list.map(user => format(user));
      commit('saveUserPagination', { list, total });
    },
    //查询小组信息(带fullname)
    async queryReBuildGroupName({ commit }, payload) {
      const res = await resolveResponseError(() =>
        queryReBuildGroupName(payload)
      );
      commit('saveReBuildGroupName', res);
    }
  },

  mutations: {
    saveGroup(state, payload) {
      state.groups = payload.filter(item => item.status === '1');
    },
    saveGroupAll(state, payload) {
      state.groupsAll = payload.filter(item => item.status === '1');
    },
    saveAbandonGroup(state, payload) {
      state.abandonGroups = payload.filter(item => item.status === '0');
    },
    saveCompany(state, payload) {
      state.companies = payload.filter(item => item.status === '1');
    },
    saveAbandonCompany(state, payload) {
      state.abandonCompanies = payload.filter(item => item.status === '0');
    },
    saveRole(state, payload) {
      state.roles = payload.filter(item => item.status === '1');
    },
    saveLdapRoles(state, payload) {
      state.ldapRoles = payload.filter(item => item.status === '1');
    },
    saveAbandonRole(state, payload) {
      state.abandonRoles = payload.filter(item => item.status === '0');
    },
    saveTag(state, payload) {
      state.tags = payload.filter(item => item.status === '1');
    },
    saveCITemplate(state, payload) {
      state.CITemplateData = payload;
    },
    saveSystem(state, payload) {
      state.systemData = payload;
    },
    saveDomain(state, payload) {
      state.domainData = payload;
    },
    saveGroupPeopel(state, payload) {
      state.groupPeople = payload;
    },
    saveRedmine(state, payload) {
      state.redmineData = payload;
    },
    saveTodosList(state, payload) {
      state.todosList = payload;
    },
    saveChildGroup(state, payload) {
      state.childGroup = payload;
    },
    saveArea(state, payload) {
      state.areaList = payload;
    },
    saveFunction(state, payload) {
      state.functionList = payload;
    },
    saveRank(state, payload) {
      state.rankList = payload;
    },
    saveUserPagination(state, payload) {
      state.userInPage = payload;
    },
    saveReBuildGroupName(state, payload) {
      state.reBuildGroupName = payload;
    }
  }
};

function formatGroup(group) {
  return {
    ...group,
    id: group.id,
    eName: group.name_en,
    parent: group.parent_id
  };
}
