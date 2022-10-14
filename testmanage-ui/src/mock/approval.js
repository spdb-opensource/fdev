import Mock from 'mockjs';

let adminTableData = [
  {
    id: 1,
    number: '测试工单-111',
    name: '',
    type: '',
    caseNature: '',
    functionModule: '',
    functionPoint: '',
    children: [
      {
        id: 11,
        number: '执行计划1',
        name: '',
        type: '',
        caseNature: '',
        functionModule: '',
        functionPoint: '',
        children: [
          {
            id: 111,
            number: '62342',
            name: '案例1',
            type: '流程',
            caseNature: '正',
            functionModule: '个人网银》测试',
            functionPoint: 'xxxxx'
          },
          {
            id: 112,
            number: '6271h',
            name: '案例2',
            type: '登陆',
            caseNature: '反',
            functionModule: '个人网银》测试',
            functionPoint: 'xxxxx'
          }
        ]
      },
      {
        id: 12,
        number: '执行计划2',
        name: '',
        type: '',
        caseNature: '',
        functionModule: '',
        functionPoint: ''
      }
    ]
  },
  {
    id: 2,
    number: '测试工单-222',
    name: '',
    type: '',
    caseNature: '',
    functionModule: '',
    functionPoint: '',
    children: [
      {
        id: 21,
        number: '执行计划1',
        name: '',
        type: '',
        caseNature: '',
        functionModule: '',
        functionPoint: ''
      }
    ]
  },
  {
    id: 3,
    number: '测试工单-333',
    name: '',
    type: '',
    caseNature: '',
    functionModule: '',
    functionPoint: ''
  },
  {
    id: 3,
    number: '测试工单-333',
    name: '',
    type: '',
    caseNature: '',
    functionModule: '',
    functionPoint: ''
  },
  {
    id: 3,
    number: '测试工单-333',
    name: '',
    type: '',
    caseNature: '',
    functionModule: '',
    functionPoint: ''
  },
  {
    id: 3,
    number: '测试工单-333',
    name: '',
    type: '',
    caseNature: '',
    functionModule: '',
    functionPoint: ''
  },
  {
    id: 3,
    number: '测试工单-333',
    name: '',
    type: '',
    caseNature: '',
    functionModule: '',
    functionPoint: ''
  },
  {
    id: 3,
    number: '测试工单-333',
    name: '',
    type: '',
    caseNature: '',
    functionModule: '',
    functionPoint: ''
  },
  {
    id: 3,
    number: '测试工单-333',
    name: '',
    type: '',
    caseNature: '',
    functionModule: '',
    functionPoint: ''
  },
  {
    id: 3,
    number: '测试工单-333',
    name: '',
    type: '',
    caseNature: '',
    functionModule: '',
    functionPoint: ''
  }
];

Mock.mock('/api/data/mockAdminTableDate', 'get', adminTableData);
