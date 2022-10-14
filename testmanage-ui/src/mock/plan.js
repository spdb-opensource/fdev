const Mock = require('mockjs');

const reuse = {
  case_number: '@id()',
  case_name: '@CNAME()',
  case_type: '@csentence(5)',
  case_nature: '正',
  function_module: '个人网银》测试',
  function: '@csentence(3)',
  case_status: '审批通过'
};
Mock.mock('/reuse/queryReuseList', { 'list|25-51': [reuse] });
