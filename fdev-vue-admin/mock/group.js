const mockjs = require('mockjs');

const group = mockjs.mock([
  { id: '3', name: '公共组', count: 21, parent_id: '1' },
  { id: '4', name: '公司组', count: 0, parent_id: '1' },
  { id: '9', name: '移动部门', count: 0, parent_id: '' },
  { id: '5', name: '移动组', count: 0, parent_id: '1' },
  { id: '2', name: '零售金融APP组', count: 0, parent_id: '1' },
  { id: '6', name: '移动营销组', count: 0, parent_id: '1' },
  { id: '7', name: '支付组', count: 0, parent_id: '1' },
  { id: '8', name: '测试服务团队', count: 0, parent_id: '1' },
  { id: '1', name: '互联网应用', count: 1, parent_id: '' }
]);

let subGroup = new Array(20).fill({}, 0, 20);
subGroup = subGroup.map((group, index) =>
  mockjs.mock({
    id: '' + index + index,
    name: '@string',
    count: '@natural(60, 100)',
    'parent_id|1': ['1', '2', '3', '4'],
    name_en: '@string'
  })
);
let subGroup1 = new Array(20).fill({}, 0, 20);
subGroup1 = subGroup1.map((group, index) =>
  mockjs.mock({
    id: '' + index + index + index,
    name: '@string',
    count: '@natural(0, 1)',
    parent_id: '' + index + index,
    name_en: '@string'
  })
);

module.exports = {
  'POST /fuser/api/group/query': group,
  'POST /fuser/api/group/delete': mockjs.mock({
    id: '@id'
  }),
  'POST /fuser/api/group/add': mockjs.mock({
    id: '@id',
    name: '@string',
    count: '@natural(60, 100)',
    'parent_id|1': ['1', '2', '3', '4']
  }),
  'POST /fuser/api/group/update': group[0]
};
