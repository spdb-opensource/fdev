import Vue from 'vue';
import { FGstree } from './FGstree/index.js';

const FdevComps = require.context(
  './',
  true,
  /(F|Fdev|Config)[A-Z]\w+\/index.vue$/
);
FdevComps.keys().forEach(key => {
  let component = FdevComps(key);
  component = component.default || component;
  Vue.component(component.name, component);
});

Vue.component('FGstree', FGstree);

// export const CompNames = FdevComps.keys().map(key => key.split('/')[1]);

const Demos = require.context('./', true, /(F|Fdev|Config)[A-Z]\w+\/demo.vue$/);
export let CompDemos = {};
Demos.keys().forEach(key => {
  let demo = Demos(key);
  demo = demo.default || demo;
  CompDemos[demo.name] = demo;
});
export const CompNames = Demos.keys().map(key => {
  let demo = Demos(key);
  demo = demo.default || demo;
  return demo.name;
});
