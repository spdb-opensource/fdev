import {
  query,
  add,
  update,
  deleteFun,
  queryMenu,
  updateMenu,
  deleteMenu,
  addMenu,
  queryRoleByMenuId,
  queryAllSection,
  addGroupUsers,
  queryAllGroup,
  queryUserByGroupId,
  canAddUserList
} from '../services/methods.js';
import { resolveResponseError } from '@/utils/utils';
export default {
  namespaced: true,
  state: {
    functionTableList: [],
    menuTableList: [],
    roleMemu: [],
    sectionAll: [], //所有条线
    addGroupUsersList: [], //批量新增组员
    allGroupList: [], //层级关系小组
    userByGroupIdList: [], //小组id查询组或子组下的人员信息
    addUserList: [] //新增组员下拉选项
  },
  actions: {
    //职能查询
    async query({ commit }, payload) {
      const response = await resolveResponseError(() => query(payload));
      commit('saveQueryFunctionTableList', response);
    },
    //新增职能
    async add({ commit }, payload) {
      await resolveResponseError(() => add(payload));
    },
    //删除职能
    async deleteFun({ commit }, payload) {
      await resolveResponseError(() => deleteFun(payload));
    },
    //修改职能
    async update({ commit }, payload) {
      await resolveResponseError(() => update(payload));
    },
    //菜单查询
    async queryMenu({ commit }, payload) {
      const response = await resolveResponseError(() => queryMenu(payload));
      commit('saveMenuTableList', response);
    },
    //修改菜单
    async updateMenu({ commit }, payload) {
      await resolveResponseError(() => updateMenu(payload));
    },
    //删除菜单
    async deleteMenu({ commit }, payload) {
      await resolveResponseError(() => deleteMenu(payload));
    },
    //新增菜单
    async addMenu({ commit }, payload) {
      await resolveResponseError(() => addMenu(payload));
    },
    //根据菜单Id查询角色
    async queryRoleByMenuId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryRoleByMenuId(payload)
      );
      commit('saveRoleMemu', response);
    },
    //查询所有条线
    async queryAllSection({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryAllSection(payload)
      );
      commit('saveSectionAll', response);
    },
    //批量新增组员
    async addGroupUsers({ commit }, payload) {
      const response = await resolveResponseError(() => addGroupUsers(payload));
      commit('saveAddGroupUsersList', response);
    },
    //按照层级关系返回全量组信息
    async queryAllGroup({ commit }, payload) {
      const response = await resolveResponseError(() => queryAllGroup(payload));
      commit('saveAllGroupList', response);
    },
    //按照层级关系返回全量组信息
    async queryUserByGroupId({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryUserByGroupId(payload)
      );
      commit('saveUserByGroupIdList', response);
    },
    //新增组员下拉选项
    async canAddUserList({ commit }, payload) {
      const response = await resolveResponseError(() =>
        canAddUserList(payload)
      );
      commit('saveAddUserList', response);
    }
  },
  mutations: {
    saveQueryFunctionTableList(state, payload) {
      state.functionTableList = payload;
    },
    saveMenuTableList(state, payload) {
      state.menuTableList = payload;
    },
    saveRoleMemu(state, payload) {
      state.roleMemu = payload;
    },
    saveSectionAll(state, payload) {
      state.sectionAll = payload;
    },
    saveAddGroupUsersList(state, payload) {
      state.addGroupUsersList = payload;
    },
    saveAllGroupList(state, payload) {
      state.allGroupList = payload;
    },
    saveUserByGroupIdList(state, payload) {
      state.userByGroupIdList = payload;
    },
    saveAddUserList(state, payload) {
      state.addUserList = payload;
    }
  }
};
