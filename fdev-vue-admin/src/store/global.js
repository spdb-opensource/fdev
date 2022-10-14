import { queryNotice, updateNotice } from '@/services/api';
import { resolveResponseError } from '@/utils/utils';

export default {
  namespaced: true,

  state: {
    newNotices: [],
    oldNotices: [],
    loading: {},
    updateNotices: []
  },

  getters: {
    isAdmin() {} // -> getters['account/isAdmin']
  },

  actions: {
    async fetchNotices({ commit }, payload) {
      const data = await resolveResponseError(() => queryNotice(payload));
      commit('saveNotices', data);
    },
    async clearNotices({ commit }, payload) {
      await resolveResponseError(() => updateNotice(payload));
      commit('saveClearedNotices');
    },
    async updateNotifyStatus({ commit }, payload) {
      await resolveResponseError(() => updateNotice(payload));
      commit('updateNotices', payload);
    }
  },

  mutations: {
    saveLoading(state, payload) {
      state.loading = {
        ...state.loading,
        ...payload
      };
    },
    saveNotices(state, payload) {
      state.newNotices = payload.filter(item => {
        return item.state === '0';
      });
      state.oldNotices = payload.filter(item => {
        return item.state === '1';
      });
      state.updateNotices = payload.filter(item => {
        return item.type === '3';
      });
    },
    saveClearedNotices(state) {
      state.notices = [];
    },
    changeLoading(state, payload) {
      state.loading = payload;
    },

    updateNotices(state, payload) {
      let notice = state.newNotices.filter(idItem => {
        return payload.id.some(pid => {
          return pid === idItem.id;
        });
      });
      state.newNotices = state.newNotices.filter(idItem => {
        return payload.id.every(pid => {
          return pid !== idItem.id;
        });
      });
      state.oldNotices = state.oldNotices.concat(notice);
    }
  }
};
