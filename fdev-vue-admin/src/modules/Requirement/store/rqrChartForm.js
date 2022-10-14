// import { queryAllDemands } from '@/services/rqrmnt';
import { queryDemandList } from '@/services/demand';
import { resolveResponseError } from '@/utils/utils';
export default {
  namespaced: true,

  state: {
    allDemands: {
      rqrmnts: []
    },
    numInfo: {},
    everyGroupList: [],
    everyLingshouList: [],
    everyGroupDemandList: [1, 1, 1, 1, 1],
    everyLingshouDemandList: [1, 1, 1, 1, 1]
  },
  getters: {
    publishedDemands: state => {
      const { allDemands } = state;
      return allDemands.rqrmnts.filter(demand => {
        return demand.rqrmntState === '已投产';
      });
    }
  },
  actions: {
    async queryAllDemands({ commit, state }, payload) {
      const response = await resolveResponseError(() =>
        queryDemandList(payload)
      );
      commit('saveDemands', response);
    }
  },
  mutations: {
    saveDemands(state, payload) {
      state.allDemands = payload;
    },
    saveNumInfo(state, payload) {
      state.numInfo = payload;
      state.everyGroupList = payload.everyGroupList;
      state.everyLingshouList = payload.everyLingshouList;
      state.everyGroupDemandList = payload.everyGroupDemandList;
      state.everyLingshouDemandList = payload.everyLingshouDemandList;
    }
  }
};
