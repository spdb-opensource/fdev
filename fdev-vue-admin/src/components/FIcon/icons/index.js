const ficons = require.context('./', false, /.vue/);

let F_ICONS = {};
ficons.keys().forEach(key => {
  let component = ficons(key);
  component = component.default || component;
  F_ICONS[
    key
      .split('.')[1]
      .replace('/', '')
      .toLowerCase()
  ] = component;
});

export default F_ICONS;
