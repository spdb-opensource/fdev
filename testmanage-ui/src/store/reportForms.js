import { queryDayTotalReport, queryDayGroupReport } from '@/services/userchart';
export default {
  namespaced: true,
  state: {
    dailyReport: [],
    dailyGroupReport: [],
    searchOrderDate: []
  },
  getters: {},
  actions: {
    async getDayTotalReport({ commit }, payload) {
      const response = await queryDayTotalReport(payload);
      commit('saveDayTotalReport', response);
      commit('saveDay', payload);
    },
    async getDayGroupReport({ commit }, payload) {
      const response = await queryDayGroupReport(payload);
      commit('saveDayGroupReport', response);
    }
  },
  mutations: {
    saveDayTotalReport(state, data) {
      state.dailyReport = data;
    },
    saveDayGroupReport(state, data) {
      state.dailyGroupReport = data;
    },
    saveDay(state, payload) {
      state.searchOrderDate = [payload.startDate, payload.endDate];
    }
  }
};
