const Mock = require('mockjs');

const order = {
  taskName: '@csentence(5)',
  No: '实施单元2019运营225-222',
  tester: [{ 'name|1-4': ['@CNAME'] }]
};
Mock.mock('/order/queryPlanOrderList', { 'list|1-5': [order] });

/* 上面是第一种 下面是第二种方式 */
/* Mock.mock('/order/queryPlanOrderList',{
"list|4":[{
taskName:'主任务名',
No:'实施单元2019运营225-222',
tester:[
{"name|1-4":['@CNAME']}
]
}]
}) */
