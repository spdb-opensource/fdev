const ICONS = require.context('./', true, /.svg$/);
export let LAYOUT_ICONS = {};
ICONS.keys().forEach(key => {
  LAYOUT_ICONS[
    key
      .replace('.svg', '')
      .replace('.', '')
      .replace('/', '')
      .replace('/', '_')
  ] = ICONS(key);
});
