const TEMPS = require.context('./', true, /index.vue/);

let UI_TEMPS = {};
TEMPS.keys().forEach(key => {
  let component = TEMPS(key);
  component = component.default || component;
  UI_TEMPS[component.name] = component;
});

export { UI_TEMPS };
