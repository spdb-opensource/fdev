const Mock = require('mockjs');

const useradmin = {
  user_en_name: '@CNAME()',
  user_name: '@CNAME()',
  user_phone: '@id()',
  email: '@email',
  role_en_name: '@csentence(5)',
  group_id: '公共组'
};
Mock.mock('/api/useradmin/queryUserList', { 'list|25-51': [useradmin] });
