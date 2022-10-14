import { querySysRlsInfo } from '@/services/release';

import { query } from '@/services/application';

export default {
  namespaced: true,

  state: {
    notices: [],
    loading: false,
    detailTitle: {},
    system: [],
    appData: []
  },

  getters: {
    // -> getters['account/isAdmin']
  },
  //
  actions: {
    //  async txt({ commit, state }, payload) {
    //    commit('txt', payload);
    //  },
    //  async clearNotices({ commit, state }, payload) {
    //    commit('saveClearedNotices', payload);
    //    const count = state.notices.length;
    //    commit('user/changeNotifyCount', count, { root: true });
    //  },
    async getSystem({ commit }) {
      const data = await querySysRlsInfo();
      commit('saveSystem', data);
      return data;
    },
    async getAppData({ commit }, payload) {
      const data = await query(payload);
      commit('saveAppData', data);
      return data;
    }
  },

  mutations: {
    txt(state, payload) {
      // state.notices = payload;
      state.detailTitle = payload;
    },
    saveClearedNotices(state, payload) {
      state.notices = state.notices.filter(item => item.type !== payload);
    },
    changeLoading(state, payload) {
      state.loading = payload;
    },
    saveSystem(state, data) {
      state.system = data;
    },
    saveAppData(state, data) {
      state.appData = data;
    }
  }
};
