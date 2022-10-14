import { stringify } from 'qs';
import LocalStorage from '#/plugins/LocalStorage';
import router from '@/router/index.js';
import { login, once } from '@/services/api';
import { setAuthority } from '@/utils/authority';
import { getPageQuery, errorNotify, resolveResponseError } from '@/utils/utils';
import { formatUser, findAuthority } from '@/modules/User/utils/model';

export default {
  namespaced: true,

  state: {
    status: undefined,
    group: '',
    ldapUserInfo: {}
  },

  actions: {
    async login({ commit, state }, payload) {
      const response = await resolveResponseError(() => login(payload));
      // ldap登录且未完善信息
      if (response.is_once_login == 4 || !response.git_token) {
        commit('saveLdapUserInfo', response);
        commit('changeLoginStatus', response);
        // router.replace('/ldapRegisterUser');
        return;
      }
      response.currentAuthority =
        response.fdevStatus !== 'error' ? findAuthority(response) : 'guest';
      const params = getPageQuery();

      let { redirect, ...other } = params;

      if (response.fdevStatus !== 'error') {
        commit('changeLoginStatus', response);
        LocalStorage.set('admin-user', formatUser(response));
        commit('authorized/reloadAuthorized', null, { root: true });
        // router.replace(`/${redirect? redirect: '/'}` + (other? `?${stringify(other)}`: ''));
        if (response.is_once_login == 2) {
          router.replace('/account/onceLogined');
        } else {
          if (redirect) {
            router.replace(
              `${redirect}` +
                (Object.keys(other).length != 0 ? `?${stringify(other)}` : '')
            );
          } else {
            router.replace('/account/center');
          }
        }
      } else {
        // once
        if (response.code === 'USR1000') {
          LocalStorage.set('fdev-vue-admin-jwt', response.token);
          commit('saveLoginGroup', response);
          return router.replace(
            '/login/once' + (params ? `?${stringify(params)}` : '')
          );
        }
        errorNotify(response.msg);
      }
    },

    async once({ commit, state }, payload) {
      const response = await resolveResponseError(() => once(payload));

      response.currentAuthority = findAuthority(response);

      commit('changeLoginStatus', response);
      commit('authorized/reloadAuthorized', null, { root: true });

      getPageQuery();
      // let { redirect, ...other } = params;

      // router.replace(`/${redirect? redirect: '/'}` + (other? `?${stringify(other)}`: ''));
      router.replace('/account/onceLogined');
    },

    async logout({ commit, state }, payload) {
      commit('changeLoginStatus', {
        status: false,
        currentAuthority: 'guest',
        token: null
      });
      commit('authorized/reloadAuthorized', null, { root: true });
      commit('saveLdapUserInfo', {});
      if (!window.location.hash.includes('redirect')) {
        router.replace(
          '/login?' +
            stringify({
              redirect: window.location.hash.substring(2)
            })
        );
      }
    }
  },

  mutations: {
    changeLoginStatus(state, payload) {
      LocalStorage.set('fdev-vue-admin-jwt', payload.token);
      setAuthority(payload.currentAuthority);
      state.status = payload.status;
      state.type = payload.type;
    },
    saveLdapUserInfo(state, payload) {
      state.ldapUserInfo = payload;
    },
    saveLoginGroup(state, payload) {
      state.group = payload.group.name;
    }
  }
};
