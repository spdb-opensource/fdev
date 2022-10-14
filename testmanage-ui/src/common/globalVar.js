let contextPath = '/api/';
if (process.env.NODE_ENV === 'production') {
  contextPath = '/';
}
let utils = {
  contextPath
};
export default utils;
