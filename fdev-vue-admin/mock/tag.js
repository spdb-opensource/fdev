const mockjs = require('mockjs');
const { wrap7A } = require('./utils/utils');

module.exports = {
  'POST /fuser/api/label/query': mockjs.mock([
    {
      id: '@id',
      name: '很有想法的',
      count: '@natural(0, 0)'
    },
    {
      id: '@id',
      name: '专注设计',
      count: '@natural(0, 1)'
    },
    {
      id: '@id',
      name: '辣~',
      count: '@natural(0, 5)'
    },
    {
      id: '@id',
      name: '大长腿',
      count: '@natural(0, 2)'
    },
    {
      id: '@id',
      name: '川妹子',
      count: '@natural(60, 100)'
    },
    {
      id: '@id',
      name: '海纳百川',
      count: '@natural(60, 100)'
    }
  ]),

  'POST /fuser/api/label/add': (req, res) => {
    const { name } = req.body;
    res.send(
      wrap7A({
        id: '@id',
        name,
        count: 1
      })
    );
  },

  'POST /fuser/api/label/delete': mockjs.mock({
    id: '@id'
  })
};
