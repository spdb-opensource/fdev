const mockjs = require('mockjs');
const defectList = {
  id: '@id',
  summary: '摘要摘要摘要摘要摘要摘要摘要',
  description: '描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述',
  'status|1': ['10', '20', '30', '40', '50', '80', '90'],
  handler: '@cname',
  reporter: '@cname',
  date_submitted: '@date',
  'priority|1': ['10', '20', '30', '40', '50', '60']
};

module.exports = {
  'POST /tmantis/mantisFdev/queryFuserMantisAll': mockjs.mock({
    'list|20': [defectList]
  })['list'],
  'POST /tmantis/mantisFdev/updateFdevMantis': mockjs.mock({
    'list|1': [defectList]
  })['list']
};
