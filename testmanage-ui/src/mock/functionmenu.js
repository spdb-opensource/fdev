const Mock = require('mockjs');

const functionmenu = {
  id: '@id()',
  systemName: '@csentence(5)',
  firstMenu: '@csentence(3)',
  secondMenu: '@csentence(3)',
  thirdMenu: '@csentence(3)',
  fourthMenu: '@csentence(3)',
  fifthMenu: '@csentence(3)',
  sixthMenu: '@csentence(3)',
  seventhMenu: '@csentence(3)'
};
Mock.mock('/api/functionmenu/queryFunctionMenu', {
  'list|25-51': [functionmenu]
});
