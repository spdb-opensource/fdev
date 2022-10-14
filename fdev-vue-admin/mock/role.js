const mockjs = require('mockjs');

const role = mockjs.mock([
  {
    id: '@id',
    name: '行内项目负责人',
    count: '@natural(60, 100)'
  },
  {
    id: '@id',
    name: '厂商项目负责人',
    count: '@natural(60, 100)'
  },
  {
    id: '@id',
    name: '开发人员',
    count: '@natural(60, 100)'
  },
  {
    id: '@id',
    name: '测试人员',
    count: '@natural(60, 100)'
  },
  {
    id: '@id',
    name: '助理',
    count: '@natural(60, 100)'
  },
  {
    id: '@id',
    name: '版本管理员',
    count: '@natural(60, 100)'
  }
]);

module.exports = {
  'POST /fuser/api/role/query': role,
  'POST /fuser/api/role/delete': mockjs.mock({
    id: '@id'
  }),
  'POST /fuser/api/role/add': mockjs.mock({
    id: '@id',
    name: '@string',
    count: '@natural(60, 100)'
  }),
  'POST /fuser/api/role/update': role[0]
};
