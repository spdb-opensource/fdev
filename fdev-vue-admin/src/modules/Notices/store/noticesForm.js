import {
  sendAnnounce,
  queryAnnounce,
  queryMessageByType
} from '@/services/api';
import { resolveResponseError } from '@/utils/utils';

export default {
  namespaced: true,

  state: { announceList: [], unreadMsgList: [] },

  getters: {},

  actions: {
    // 发起公告
    async sendAnnounce({ commit }, payload) {
      await resolveResponseError(() => sendAnnounce(payload));
    },
    // 查询公告
    async queryAnnounce({ commit }, payload) {
      const response = await resolveResponseError(() => queryAnnounce(payload));
      commit('saveAnnounce', response);
    },
    // 查询用户未读消息
    async queryMessageByType({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryMessageByType(payload)
      );
      commit('saveUnreadMsg', response);
    }
  },

  mutations: {
    saveSendAnnounce(state, payload) {
      state.announceList.unshift(payload);
    },
    saveAnnounce(state, payload) {
      state.announceList = payload;
    },
    saveUnreadMsg(state, payload) {
      state.unreadMsgList = payload;
    }
  }
};
