const glob = require('glob');
const { wrap7A, wrapError } = require('./utils/utils');

/* yapi 文件夹下是从 yapi 导出的 mock 数据，只导入了状态码为 ‘AAAAAA’ 的数据 */

// const mockFiles = glob.sync('./*.js', {
const mockFiles = glob.sync('./yapi/*.js', {
  cwd: __dirname,
  ignore: ['./index.js']
});

let apis = mockFiles.reduce((memo, mockFile) => {
  const m = require(mockFile);
  memo = {
    ...memo,
    ...(m.default || m)
  };
  return memo;
}, {});

for (let key in apis) {
  if (typeof apis[key] === 'object' || typeof apis[key] === 'string') {
    let data = wrap7A(apis[key]);
    apis[key] = (req, res) => {
      res.send(data);
    };
  }
}

module.exports = apis;
