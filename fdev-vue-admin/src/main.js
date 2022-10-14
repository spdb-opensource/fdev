import Vue from 'vue';
import Vuelidate from 'vuelidate';
import { goBack } from '@/utils/utils';
//代码编辑器
import VueCodeMirror from 'vue-codemirror';
import 'codemirror/lib/codemirror.css';

import App from './App.vue';
import router from './router/index.js';
import store from './views/.storee';
import './registerServiceWorker';
import './utils/track';
import './quasar';
import '@/components/index.js';
import '@/directive/directives.js';
import { TimePicker, Cascader } from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';

Vue.use(Vuelidate);
Vue.use(TimePicker).use(Cascader);
Vue.use(VueCodeMirror);
Vue.prototype.goBack = goBack;

Vue.config.productionTip = false;

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app');
