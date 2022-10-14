import {
  queryDevelopGroup,
  queryAssignList,
  queryAllOption,
  assignUpdate,
  queryUserCoreData
} from '@/services/assign';
import { queryAllGroup } from '@/services/useradmin';
export default {
  namespaced: true,

  state: {
    assignOrderList: [],
    developGroup: [],
    allOption: [],
    groupList: [],
    userInfo: {}
  },
  actions: {
    async queryAssignList({ commit }, payload) {
      const response = await queryAssignList(payload);
      commit('saveQueryAssignList', response);
    },
    async queryDevelopGroup({ commit }, payload) {
      const response = await queryDevelopGroup(payload);
      commit('saveDevelopGroup', response);
    },
    async queryAllOption({ commit }, payload) {
      const response = await queryAllOption(payload);
      commit('saveAllOption', response);
    },
    async assignUpdate({ commit }, payload) {
      await assignUpdate(payload);
    },
    async queryAllGroup({ commit }, payload) {
      const response = await queryAllGroup(payload);
      commit('saveGroupList', response);
    },
    async queryUserCoreData({ commit }, payload) {
      const response = await queryUserCoreData(payload);
      commit('saveUserInfo', response);
    }
  },
  mutations: {
    saveQueryAssignList(state, payload) {
      state.assignOrderList = payload;
    },
    saveDevelopGroup(state, payload) {
      state.developGroup = payload;
    },
    saveAllOption(state, payload) {
      state.allOption = payload;
    },
    saveGroupList(state, payload) {
      state.groupList = payload;
    },
    saveUserInfo(state, payload) {
      state.userInfo = payload;
    }
  }
};
