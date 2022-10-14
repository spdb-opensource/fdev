const mockjs = require('mockjs');

const company = mockjs.mock([
  {
    id: '@id',
    name: '科蓝',
    count: '@natural(60, 100)'
  },
  {
    id: '@id',
    name: '文思',
    count: '@natural(60, 100)'
  },
  {
    id: '@id',
    name: '东软',
    count: '@natural(60, 100)'
  },
  {
    id: '@id',
    name: '屹通',
    count: '@natural(60, 100)'
  }
]);

module.exports = {
  'POST /fuser/api/company/query': company,
  'POST /fuser/api/company/delete': mockjs.mock({
    id: '@id'
  }),
  'POST /fuser/api/company/add': mockjs.mock({
    id: '@id',
    name: '@string',
    count: '@natural(60, 100)'
  }),
  'POST /fuser/api/company/update': company[0]
};
