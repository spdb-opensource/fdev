const mockjs = require('mockjs');
const { wrap7A, wrapError } = require('./utils/utils');

let user = {
  id: '@id',
  user_name_en: '@first',
  user_name_cn: '@cname',
  email: '@email',
  group: {
    id: '@id',
    'name|1': ['个人', '公司', '支付', '公共']
  },
  role: [
    {
      id: '@id',
      name: ['环境配置管理员']
    },
    {
      id: '@id',
      'name|1': [
        '行内项目负责人',
        '厂商项目负责人',
        '开发人员',
        '测试人员',
        '助理',
        '版本管理员'
      ]
    },
    {
      id: '@id',
      'name|1': [
        '行内项目负责人',
        '厂商项目负责人',
        '开发人员',
        '测试人员',
        '助理',
        '版本管理员'
      ]
    }
  ],
  permission: {
    id: '@id',
    name_en: 'admin',
    'name_cn|1': ['超级管理员', '小组管理员', '普通人员'],
    permissions: ['/api/company/add', '/api/role/add', '/api/user/add']
  },
  company: {
    id: '@id',
    'name|1': ['科蓝', '文思', '东软', '屹通']
  },
  avatar: mockjs.Random.image('100x100'),
  signature: '海纳百川，有容乃大',
  title: '交互专家',
  notifyCount: 12,
  country: 'China',
  geographic: {
    province: {
      label: '浙江省',
      key: '330000'
    },
    city: {
      label: '杭州市',
      key: '330100'
    }
  },
  address: '西湖区工专路 77 号',
  telephone: '0752-268888888',
  redmine_user: '@string',
  svn_user: '@string',
  git_user: '@string',
  'status|1': ['0', '1'],
  'is_once_login|1': ['3', '3'],
  user_label: [
    {
      id: '@id',
      name: '很有想法的',
      count: '@natural(60, 100)'
    },
    {
      id: '@id',
      name: '专注设计',
      count: '@natural(60, 100)'
    },
    {
      id: '@id',
      name: '辣~',
      count: '@natural(60, 100)'
    },
    {
      id: '@id',
      name: '大长腿',
      count: '@natural(60, 100)'
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
  ]
};

const todos = [
  {
    module: 'module',
    type: 'merge',
    description: 'description',
    manager: [
      {
        id: '5cb73b0c9b1dd800076b4ade',
        user_name_cn: 'xxx',
        user_name_en: 'xxx'
      }
    ],
    createUser: {
      id: '5cb73b0c9b1dd800076b4ade',
      user_name_cn: 'xxx',
      user_name_en: 'xxx'
    },
    createTime: '2019/08/08',
    status: '0',
    executor: {
      id: '5cb73b0c9b1dd800076b4ade',
      user_name_cn: 'xxx',
      user_name_en: 'xxx'
    },
    executeTime: '2019/08/30'
  },
  {
    module: 'module',
    type: 'merge',
    description: 'description',
    manager: [
      {
        id: '5cb73b0c9b1dd800076b4ade',
        user_name_cn: 'xxx',
        user_name_en: 'xxx'
      }
    ],
    createUser: {
      id: '5cb73b0c9b1dd800076b4ade',
      user_name_cn: 'xxx',
      user_name_en: 'xxx'
    },
    createTime: '2019/08/08',
    status: '1',
    executor: {
      id: '5cb73b0c9b1dd800076b4ade',
      user_name_cn: 'xxx',
      user_name_en: 'xxx'
    },
    executeTime: '2019/08/30'
  },
  {
    module: 'fewerhj',
    type: 'type',
    description: 'description',
    manager: [
      {
        id: '5cb73b0c9b1dd800076b4ade',
        user_name_cn: 'xxx',
        user_name_en: 'xxx'
      }
    ],
    createUser: {
      id: '5cb73b0c9b1dd800076b4ade',
      user_name_cn: 'xxx',
      user_name_en: 'xxx'
    },
    createTime: '2019/08/08',
    status: '0',
    executor: {
      id: '5cb73b0c9b1dd800076b4ade',
      user_name_cn: 'xxx',
      user_name_en: 'xxx'
    },
    executeTime: '2019/08/30'
  }
];

const queryArea = [
  { id: '1', name: ' 上海' },
  { id: '2', name: ' 合肥' },
  { id: '3', name: ' 武汉' }
];
const queryPost = [
  { id: '1', name: '测试' },
  { id: '2', name: '开发' },
  { id: '3', name: '产品' }
];
module.exports = {
  // 支持值为 Object 和 Array
  'POST /fuser/api/user/currentUser': mockjs.mock(user),
  'POST /fuser/api/user/query': mockjs.mock({
    'list|20': [user]
  })['list'],
  'POST /fuser/api/auth/login': (req, res) => {
    const { password, user_name_en, type } = req.body;
    if (password === '888888' && user_name_en === 'admin') {
      res.send(
        wrap7A({
          type,
          ...user,
          permission: {
            id: 'id',
            name_en: 'admin',
            name_cn: '超级管理员',
            permissions: ['/api/user/add']
          },
          token: 'jwt'
        })
      );
      return;
    }
    if (password === '123456' && user_name_en === 'user') {
      res.send(
        wrap7A({
          type,
          ...user,
          permission: {
            id: 'id',
            name_en: 'user',
            name_cn: '普通人员',
            permissions: ['/api/user/add']
          },
          token: 'jwt'
        })
      );
      return;
    }
    if (password === 'once' && user_name_en === 'once') {
      res.send(
        wrapError({
          type,
          ...user,
          permission: {
            id: 'id',
            name_en: 'group_admin',
            name_cn: '小组管理员',
            permissions: ['/api/user/add']
          },
          code: 'USR1000',
          msg: '首次登录，请修改登录密码',
          token: 'jwt'
        })
      );
      return;
    }
    res.send(
      wrapError({
        type,
        msg: '账户或密码错误（admin/888888）'
      })
    );
  },
  'POST /fuser/api/auth/onceLogin': (req, res) => {
    const { git_token } = req.body;
    if (git_token === '888888') {
      res.send(
        wrap7A({
          ...user,
          permission: {
            id: 'id',
            name_en: 'group_admin',
            name_cn: '小组管理员',
            permissions: ['/api/user/add']
          },
          token: 'once'
        })
      );
      return;
    }
    if (git_token === '123456') {
      res.send(
        wrap7A({
          ...user,
          permission: {
            id: 'id',
            name_en: 'group_admin',
            name_cn: '小组管理员',
            permissions: ['/api/user/add']
          },
          token: 'once'
        })
      );
      return;
    }
    res.send(
      wrapError({
        msg: '令牌错误（888888）'
      })
    );
  },
  'POST /fuser/api/user/delete': mockjs.mock({
    id: '@id'
  }),
  'POST /fuser/api/user/update': mockjs.mock(user),
  'POST /fuser/api/user/add': mockjs.mock(user),
  'POST /fuser/api/userCommissionEvent/queryCommissionEvent': mockjs.mock(
    todos
  ),
  /*
    OAuth
  */
  'POST /fuser/api/auth/queryOAuth': mockjs.mock({
    id: '@id',
    scope: 'userinfo',
    name: 'SVN',
    host: '@url'
  }),
  'POST /fuser/api/auth/createAuthCode': mockjs.mock({
    redirectUrl: '@url'
  }),
  'POST /fuser/api/user/queryArea': queryArea,
  'POST /fuser/api/user/queryPost': queryPost
};
