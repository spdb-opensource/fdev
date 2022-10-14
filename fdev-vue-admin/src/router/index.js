import Vue from 'vue';
import Router from 'vue-router';
import Noop from '../views/Noop/index.vue';
import pageRoutes from './config';
import SessionStorage from '#/plugins/SessionStorage';

Vue.use(Router);
const router = new Router({
  routes: format(pageRoutes)
});

router.beforeEach((to, from, next) => {
  const { meta } = to;
  const oldMeta = from.meta;
  const filter = oldMeta.filter ? oldMeta.filter : '';
  if (meta && meta.nameCn) {
    document.title = `${meta.nameCn} - fdev`;
  } else {
    document.title = 'fdev';
  }
  if (meta.filter !== filter) {
    SessionStorage.remove('filterData');
  }
  next();
});

function format(data) {
  return data.map(item => {
    const result = {
      ...item
    };
    let { component, children, redirect, path } = result;
    if (!component) {
      result.component = Noop;
    }

    if (children) {
      if (!redirect) {
        result.redirect = `${path}/${children[0].path}`;
      }

      const child = format(children);
      // Reduce memory usage
      result.children = child;
    }
    return result;
  });
}

/* Error: Loading chunk chunk-xxx failed
 * Error: Loading CSS chunk chunk-xxx failed
 * Error: Loading JS chunk chunk-xxx failed
 * 路由懒加载导致
 * 用户长时间停留在某页面，在此期间重新发了包，
 * 采用懒加载路由的模块，加载那些被 webpack 修改过名字的 css 和 js 资源会报错
 */
router.onError(error => {
  const pattern = /Loading(\s)*.* chunk .* failed/g;
  const isChunkLoadFailed = error.message.match(pattern);
  if (isChunkLoadFailed) {
    location.reload();
  }
});

export default router;
