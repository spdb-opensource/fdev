import { queryUserApprovalList } from '@/services/header';
import { queryMessageUser } from '@/services/useradmin';

export default {
  namespaced: true,
  state: {
    userApprovalList: {},
    userSitMsgList: []
  },
  getters: {},
  actions: {
    async getUserApprovalList({ commit }) {
      const data = await queryUserApprovalList();
      commit('saveUserApprovalList', data);
    },
    async getMessageUser({ commit }, payload) {
      const data = await queryMessageUser(payload);
      commit('saveMessageUser', data);
    }
  },
  mutations: {
    saveUserApprovalList(state, data) {
      state.userApprovalList = data;
    },
    saveMessageUser(state, data) {
      state.userSitMsgList = data.filter(
        item =>
          item.type !== 'announce-update' &&
          item.type !== 'announce-halt' &&
          item.type !== 'version-refresh'
      );
    }
  }
};
