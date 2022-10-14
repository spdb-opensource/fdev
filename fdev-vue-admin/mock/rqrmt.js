const mockjs = require('mockjs');
// const { wrap7A, wrapError } = require('./utils/utils');

const rqModel = {
  rqrmntName: '@name',
  rqrmntNum: '125',
  'rqrmntState|1': ['意向', '撤销'],
  _id: '@id'
};

module.exports = {
  'GET /frqr/api/rqrmntNames': mockjs.mock({
    'rqrmntIdArr|5': [rqModel]
  })
};
