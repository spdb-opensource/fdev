const mockjs = require('mockjs');

const queryApprovalList = {
  total: 3,
  list: []
};

module.exports = {
  'POST /fuser/api/approval/queryApprovalList': queryApprovalList
};
