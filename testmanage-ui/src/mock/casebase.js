import Mock from 'mockjs'; // 引入mockjs

let asideData = [
  {
    label: '系统管理',
    children: [
      { label: '部门管理' },
      { label: '机构管理' },
      {
        label: '权限管理',
        children: [
          { label: '角色管理' },
          {
            label: '功能权限级别设置',
            children: [
              { label: '角色管理' },
              {
                label: '功能权限级别设置',
                children: [{ label: '角色管理' }, { label: '功能权限级别设置' }]
              }
            ]
          }
        ]
      }
    ]
  },
  { label: '公司网银管理' },
  { label: '指定企业客户信息查询' },
  { label: '企业日志查询' },
  { label: '捐款信息统计' },
  { label: '及时语费率维护' }
];

let tableData = [
  {
    name: '案例名称',
    reslese: '功能模块',
    point: '功能点',
    describe: '案例描述',
    creationDate: '2019-7-26',
    ifCurrency: '是',
    version: '是',
    caseWriter: '案例编写人',
    remarks: '备注'
  },
  {
    name: '案例名称',
    reslese: '功能模块',
    point: '功能点',
    describe: '案例描述',
    creationDate: '2019-7-26',
    ifCurrency: '是',
    version: '是',
    caseWriter: '案例编写人',
    remarks: '备注'
  },
  {
    name: '案例名称',
    reslese: '功能模块',
    point: '功能点',
    describe: '案例描述',
    creationDate: '2019-7-26',
    ifCurrency: '是',
    version: '是',
    caseWriter: '案例编写人',
    remarks: '备注'
  },
  {
    name: '案例名称',
    reslese: '功能模块',
    point: '功能点',
    describe: '案例描述',
    creationDate: '2019-7-26',
    ifCurrency: '是',
    version: '是',
    caseWriter: '案例编写人',
    remarks: '备注'
  },
  {
    name: '案例名称',
    reslese: '功能模块',
    point: '功能点',
    describe: '案例描述',
    creationDate: '2019-7-26',
    ifCurrency: '是',
    version: '是',
    caseWriter: '案例编写人',
    remarks: '备注'
  },
  {
    name: '案例名称',
    reslese: '功能模块',
    point: '功能点',
    describe: '案例描述',
    creationDate: '2019-7-26',
    ifCurrency: '是',
    version: '是',
    caseWriter: '案例编写人',
    remarks: '备注'
  },
  {
    name: '案例名称',
    reslese: '功能模块',
    point: '功能点',
    describe: '案例描述',
    creationDate: '2019-7-26',
    ifCurrency: '是',
    version: '是',
    caseWriter: '案例编写人',
    remarks: '备注'
  }
];

Mock.mock('/api/data/mockAsideData', 'get', asideData);
Mock.mock('/api/data/mockTableData', 'get', tableData);
