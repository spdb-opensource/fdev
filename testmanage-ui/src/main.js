// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue';
import App from './App';
import router from './router';
import store from './store';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import globalVar from './common/globalVar';
import axios from 'axios';
import 'element-ui/lib/theme-chalk/base.css';
import '@/style/index.css';
import Vuelidate from 'vuelidate';
import './components/global';
//require('./mock/order')
// require('./mock/admin')
// require('./mock/casebase')
// require('./mock/approval')
// require('./mock/functionmenu')
// require('./mock/plan')
import request from '../src/common/request.js';
import echarts from 'echarts';

Vue.prototype.$echarts = echarts;

Vue.prototype.Global = globalVar;

Vue.use(ElementUI);
Vue.use(Vuelidate);
Vue.config.productionTip = false;

axios.defaults.withCredentials = true;
// Vue.prototype.$http = axios;
Vue.prototype.$http = request;

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>'
});
