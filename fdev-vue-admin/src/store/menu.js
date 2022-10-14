import routes from '@/router/config.js';
import { queryUserMenu } from '@/services/api';

export default {
  namespaced: true,

  state: {
    menuList: [],
    menuData: [],
    miniState: false //侧边栏折叠/打开状态
  },

  getters: {},
  actions: {
    async queryMenu({ commit }) {
      const menuList = await queryUserMenu();
      menuList.sort((a, b) => a.sort - b.sort);
      commit('setMenuList', menuList);
    },
    initMenu({ commit, state }, payload) {
      const menuData = format(routes.filter(route => route.name === 'app'))[0]
        .children;

      commit('setMenuData', menuData);
    }
  },
  mutations: {
    setMenuList(state, payload) {
      state.menuList = payload;
    },
    setMenuData(state, payload) {
      state.menuData = payload;
    },
    updateMiniState(state, payload) {
      state.miniState = payload;
    }
  }
};

function format(data, parentAuthority, parentPath = '') {
  return data.map(item => {
    const { path } = item;
    if (!path || path === '**') {
      return {};
    }

    let result = {
      ...item,
      ...item.meta
    };

    result.path = path.startsWith('/')
      ? path
      : parentPath.endsWith('/')
      ? `${parentPath}${path}`
      : `${parentPath}/${path}`;

    result.authority =
      item.meta && item.meta.authority ? item.meta.authority : parentAuthority;

    if (item.children) {
      const children = format(item.children, result.authority, result.path);
      // Reduce memory usage
      result.children = children;
    }
    return result;
  });
}
