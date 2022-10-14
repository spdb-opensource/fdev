const Icons = require.context('./', false, /.svg$/);
let FdevIcons = {};
Icons.keys().forEach(key => {
  let name = key.split('.')[1].replace('/', '');
  FdevIcons[name] = Icons(key);
});
export default FdevIcons;
