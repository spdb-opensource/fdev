import {
  queryAll as queryUsers,
  queryCurrent,
  add as addUser,
  remove as removeUser,
  update as updateUser,
  queryOnceLogin,
  query as queryUser,
  simulateUser,
  resetPassword,
  updateGitToken
} from '@/services/user';
import {
  queryFuserMantis,
  updateFdevMantis,
  updateAssignUser
} from '@/services/mantis';
import router from '@/router/index.js';
import LocalStorage from '#/plugins/LocalStorage';
import { resolveResponseError } from '@/utils/utils';
import { setAuthority } from '@/utils/authority';
import {
  createUserModel,
  formatUser as format,
  findAuthority
} from '@/modules/User/utils/model';

export default {
  namespaced: true,

  state: {
    list: [],
    currentUser: createUserModel(),
    userList: [],
    defectList: [],
    simulateUserAuth: ''
  },

  getters: {
    isLoginUserList: state => {
      return state.list.filter(user => {
        return (
          (user.is_once_login === '1' || user.is_once_login === '3') &&
          user.status === '0'
        );
      });
    },
    checkAdmin: state => () => {
      return state.currentUser.name === 'admin';
    },
    checkRole: state => authority => {
      const roles = state.currentUser.role.map(role => role.name);
      if (!authority) {
        return true;
      }

      if (Array.isArray(authority)) {
        if (authority.indexOf(roles) >= 0) {
          return true;
        }
        if (Array.isArray(roles)) {
          for (let i = 0; i < roles.length; i += 1) {
            const element = roles[i];
            if (authority.indexOf(element) >= 0) {
              return true;
            }
          }
        }
        return false;
      }

      if (typeof authority === 'string') {
        if (authority === roles) {
          return true;
        }
        if (Array.isArray(roles)) {
          for (let i = 0; i < roles.length; i += 1) {
            const element = roles[i];
            if (authority === element) {
              return true;
            }
          }
        }
        return false;
      }
    },
    isInfrastManager: ({ currentUser }) => {
      return currentUser.role.some(role => role.name === '基础架构管理员');
    },
    isKaDianManager: ({ currentUser }) => {
      return currentUser.role.some(role => role.name === '卡点管理员');
    },
    isDemandManager: state => {
      return state.currentUser.role.some(role => {
        return role.name === '需求管理员';
      });
    }
  },
  actions: {
    async add({ commit, state }, payload) {
      const response = await resolveResponseError(() => addUser(payload));
      const list = [...state.list, format(response)];
      commit('save', list);
      // ldap完善信息时，将当前完善的用户信息作为当前用户
      if (payload.ldapRegister) {
        commit('saveCurrentUser', format(response));
        LocalStorage.set('admin-user', format(response));
        commit('authorized/reloadAuthorized', null, { root: true });
      }
    },
    async remove({ commit, state }, payload) {
      await resolveResponseError(() => removeUser(payload));
      const list = state.list.filter(
        item => !payload.some(user => user.id === item.id)
      );
      commit('save', list);
    },
    async queryOnceLogin({ commit }, payload) {
      await resolveResponseError(() => queryOnceLogin(payload));
      router.push('/account/center');
    },
    async update({ commit, state }, payload) {
      const response = await resolveResponseError(() => updateUser(payload));
      if (state.list.length > 0) {
        let item = state.list.find(item => item.id === payload.id);
        if (item) {
          Object.assign(item, format(response));
        }
        const list = [...state.list];
        commit('save', list);
      }
      // 用户修改的自己
      if (state.currentUser.id === payload.id) {
        commit('saveCurrentUser', format(response));
      }
    },
    async fetch({ commit }, payload) {
      const response = await queryUsers(payload);
      commit('save', response.map(user => format(user)));
    },
    // 木偶人
    async simulateUser({ commit }, payload) {
      const response = await resolveResponseError(() => simulateUser(payload));
      LocalStorage.set('fdev-vue-admin-jwt', response.token);
      LocalStorage.set('fdev-vue-admin-imitate', response.token);
      commit('saveSimulateUserAuth', response);
      commit('saveCurrentUser', format(response));
      commit('authorized/reloadAuthorized', null, { root: true });
    },
    async fetchCurrent({ commit, state, dispatch }, payload) {
      const response = await resolveResponseError(() => queryCurrent());
      if (response.status === 'error') {
        return dispatch('login/logout', {}, { root: true });
      }
      //注释掉 首次登录重设密码
      // if (response.is_once_login !== '3') {
      //   router.push('/login/resetPassword');
      // }
      commit('saveCurrentUser', format(response));
      commit('authorized/reloadAuthorized', null, { root: true });
    },
    async queryUser({ commit }, payload) {
      const response = await resolveResponseError(() => queryUser(payload));
      commit('saveUser', response);
    },
    async queryFuserMantis({ commit }, payload) {
      const response = await resolveResponseError(() =>
        queryFuserMantis(payload)
      );
      commit('saveDefectList', response);
    },
    async updateFdevMantis({ commit }, payload) {
      await resolveResponseError(() => updateFdevMantis(payload));
    },
    async updateAssignUser({ commit }, payload) {
      await resolveResponseError(() => updateAssignUser(payload));
    },
    async resetPassword({ commit }, payload) {
      await resolveResponseError(() => resetPassword(payload));
    },
    async updateGitToken({ commit }, payload) {
      await resolveResponseError(() => updateGitToken(payload));
    }
  },

  mutations: {
    save(state, payload) {
      state.list = payload;
    },
    saveCurrentUser(state, payload) {
      state.currentUser = payload || {};
      setAuthority(findAuthority(payload));
    },
    saveUser(state, payload) {
      state.userList = payload;
    },
    saveDefectList(state, payload) {
      state.defectList = payload;
    },
    saveSimulateUserAuth(state, payload) {
      state.simulateUserAuth = payload.token;
    }
  }
};
