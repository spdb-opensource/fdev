import Vue from 'vue';

import './styles/quasar.styl';
import '#/quasar.ie.polyfills';
import '@quasar/extras/roboto-font/roboto-font.css';
import '@quasar/extras/material-icons/material-icons.css';
import '@quasar/extras/material-icons-outlined/material-icons-outlined.css';
import '@quasar/extras/material-icons-round/material-icons-round.css';
import '@quasar/extras/material-icons-sharp/material-icons-sharp.css';
import '@quasar/extras/fontawesome-v5/fontawesome-v5.css';
import '@quasar/extras/ionicons-v4/ionicons-v4.css';
import '@quasar/extras/mdi-v4/mdi-v4.css';
import '@quasar/extras/mdi-v3/mdi-v3.css';
import '@quasar/extras/eva-icons/eva-icons.css';
import lang from '@/components/quasar/lang/zh-hans.js';

import Dialog from '#/plugins/Dialog';
import Notify from '#/plugins/Notify';
import LocalStorage from '#/plugins/LocalStorage';
import SessionStorage from '#/plugins/SessionStorage';

import * as components from '#/components.js';
import * as directives from '#/directives.js';
// import install from '#/install.js';
// import iconSet from '#/icon-set.js';
// import ssrUpdate from '#/ssr-update.js';
// import { version } from '@/components/quasar/package.json';
import VuePlugin from '#/vue-plugin.js';
import * as plugins from '#/plugins.js';
// import * as utils from '#/utils.js';

const Quasar = {
  ...VuePlugin,
  install(Vue, opts) {
    VuePlugin.install(Vue, {
      components,
      directives,
      plugins,
      ...opts
    });
  },
  // for when cherry-picking
  Quasar: VuePlugin
};

Vue.use(Quasar, {
  lang,
  config: {},
  plugins: {
    LocalStorage,
    SessionStorage,
    Notify,
    Dialog
  }
});
