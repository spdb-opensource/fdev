import { getAuthority } from '@/utils/authority';
import { queryAuth, updateAuth } from '@/services/api';
import { formatOption, resolveResponseError } from '@/utils/utils';

export default {
  namespaced: true,

  state: {
    current: null,
    list: []
  },

  getters: {
    checkPermissions: state => authority => {
      const { current } = state;
      if (!authority) {
        return true;
      }
      if (Array.isArray(authority)) {
        if (authority.indexOf(current) >= 0) {
          return true;
        }
        if (Array.isArray(current)) {
          for (let i = 0; i < current.length; i += 1) {
            const element = current[i];
            if (authority.indexOf(element) >= 0) {
              return true;
            }
          }
        }
        return false;
      }

      if (typeof authority === 'string') {
        if (authority === current) {
          return true;
        }
        if (Array.isArray(current)) {
          for (let i = 0; i < current.length; i += 1) {
            const element = current[i];
            if (authority === element) {
              return true;
            }
          }
        }
        return false;
      }
    },
    checkTransPermissions: state => authority => {
      if (!authority) {
        return true;
      }
      let { current, list } = state;
      current = Array.isArray(current) ? current : [current];
      let matchedAuth = list.filter(auth =>
        current.some(each => each === auth.name_en)
      );
      if (!matchedAuth.length) {
        return false;
      }
      let permissions = matchedAuth.reduce((pre, next) => {
        return pre.concat(next.permissions);
      }, []);

      if (Array.isArray(authority)) {
        return authority.every(each =>
          permissions.some(permission => permission === each)
        );
      }
      if (typeof authority === 'string') {
        return permissions.indexOf(authority) >= 0;
      }
      return false;
    }
  },

  actions: {
    async fetch({ commit, state }, payload) {
      const response = await queryAuth();
      commit('save', formatOption(response, 'name_cn'));
    },
    async update({ commit, state }, payload) {
      const response = await resolveResponseError(() => updateAuth(payload));
      let item = state.list.find(item => item.id === payload.id);
      Object.assign(item, formatOption(response, 'name_cn'));
      const list = [...state.list];
      commit('save', list);
    }
  },

  mutations: {
    // TODO: authority should be fetched from server
    reloadAuthorized(state) {
      let currentAuthority = getAuthority();
      if (currentAuthority) {
        if (typeof currentAuthority === 'function') {
          state.current = currentAuthority();
        }
        if (
          Object.prototype.toString.call(currentAuthority) ===
            '[object String]' ||
          Array.isArray(currentAuthority)
        ) {
          state.current = currentAuthority;
        }
      } else {
        state.current = 'NULL';
      }
    },
    save(state, payload) {
      state.list = payload;
    }
  }
};
