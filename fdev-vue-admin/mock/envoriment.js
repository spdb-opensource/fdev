const mockjs = require('mockjs');
//环境 列表
const env = {
  id: '@id',
  'name_cn|1': ['sit测试', 'uat测试', 'rel测试', 'SIT2测试'],
  'name_en|1': ['sit', 'uat', 'rel', 'SIT2'],
  labels: ['comm', 'default'],
  desc: '环境描述@id'
};
//实体 列表
const model = {
  id: '@id',
  'name_en|1': ['dce', 'config', 'dceyaml', 'informix'],
  'name_cn|1': ['dce实体', '配置中心', 'dceyaml实体', '个人网银数据库'],
  desc: '实体描述@id',
  'first_category|1': ['a', 'b', 'c'],
  'second_category|1': ['aa', 'bb', 'cc'],
  'suffix_name|1': ['caas', 'center', 'entity'],
  'scope|1': ['deploy', 'common', 'app'],
  'type|1': ['deploy', 'runtime'],
  version: '1-1',
  env_key: [
    {
      id: '@id',
      name_en: 'variables_name_en',
      name_cn: '中文名name_cn',
      'require|1': ['1', '0'],
      desc: 'variables描述'
    }
  ]
};
//实体与环境映射 列表
const model_env = mockjs.mock([
  {
    id: '@id',
    env_id: 111,
    env: 'sit',
    model_id: '12345',
    model: '实体1',
    desc: '实体描述',
    model_namespace: '321',
    model_version: '1-1',
    model_type: '123',
    model_desc: '实体描述@model_id',
    env_name_en: '环境英文名',
    env_desc: '环境描述@env_id',
    model_name_en: '实体英文名',
    variables: [
      {
        id: '@id',
        name_en: 'variables_name_en',
        name_cn: '中文名name_cn',
        value: [
          {
            id: 'aaaaaaaaaabbbbb',
            name_en: 'pvc1',
            nfs_server_ip: 'xxx',
            path: '/wyxt-abc/',
            capacity: '100',
            nfs_version: 'v3',
            appname: '',
            note: '',
            mount_path: '/ebank/pvc1',
            sub_path: '/ebank/subpath1'
          },
          {
            id: 'aaaaaaaaaabbbbb',
            name_en: 'pvc2',
            nfs_server_ip: 'xxx',
            path: '/wyxt-abc1/',
            capacity: '150',
            nfs_version: 'v3',
            appname: '',
            note: 'a',
            mount_path: '/ebank/pvc1',
            sub_path: '/ebank/subpath1'
          }
        ],
        json_schema:
          '{"$schema": "http://jsn-schema.org/draft-04/schema#","description": "PVC","type": "array","items": {"type": "object","properties": {"name_en": {"type": "string","description": "PVC英文名"},"nfs_server_ip": {"type": "string","description": "nfs服务器ip"},"path": {"type": "string","description": "nfs服务器路径"},"capacity": {"type": "string","description": "容量(G)"},"nfs_version": {"type": "string","description": "nfs版本(默认V3)"},"app_name": {"type": "string","description": "应用名"},"note": {"type": "string","description": "备注"}},"required": ["name_en","nfs_server_ip","path","capacity","nfs_version","note"]}}',
        require: '1',
        desc: 'variables描述',
        data_type: 'array',
        json_schema_id: '@id'
      },
      {
        id: '@id',
        name_en: 'yftg',
        name_cn: 'aa',
        value: '23tfs',
        require: '1',
        desc: 'variables描述',
        data_type: ''
      },
      {
        id: '@id',
        name_en: 'lkj',
        name_cn: '交话费',
        value: {
          id: 'aaaaaaaaaabbbbb',
          name_en: '',
          nfs_server_ip: 'xxx',
          path: '/wyxt-abc1/'
        },
        json_schema:
          '{"$schema": "http://jsn-schema.org/draft-04/schema#","required": ["name_en","nfs_server_ip"],"description": "PVC","type": "object","properties": {"name_en": {"type": "string","description": "PVC英文名"},"nfs_server_ip": {"type": "string","description": "nfs服务器ip"},"path": {"type": "string","description": "nfs服务器路径"}}}',
        require: '1',
        desc: 'variables描述',
        data_type: 'object',
        json_schema_id: '@id'
      }
    ]
  },
  {
    id: '@id',
    env_id: '222',
    env: 'uat',
    model_id: '13456',
    model: '实体2',
    desc: '实体描述',
    model_namespace: '321',
    model_version: '1-1',
    model_type: '123',
    model_desc: '实体描述@model_id',
    env_name_en: '环境英文名',
    env_desc: '环境描述@env_id',
    model_name_en: '实体英文名',
    variables: [
      {
        id: '@id',
        name_en: 'variables_name_en',
        name_cn: '中文名name_cn',
        value: 'qwert',
        require: '1',
        desc: 'variables描述'
      }
    ]
  }
]);
//环境 新增-属性->申请的mock数据
const apply = mockjs.mock([
  {
    id: '@id',
    atime: '@datetime',
    type: '申请类型',
    app_id: '应用id',
    model_id: '实体id',
    logs: [
      {
        utime: '操作时间',
        status: '状态',
        opno: '操作人',
        type: '操作类型',
        remarks: '备注'
      }
    ]
  }
]);
let applyList = new Array(6).fill({}, 0, 6);
applyList = applyList.map((apply, index) =>
  mockjs.mock({
    id: '@id',
    atime: '@datetime',
    type: '申请类型',
    app_id: '应用id',
    model_id: '实体id',
    logs: [
      {
        utime: '@datetime',
        status: '状态',
        opno: '操作人',
        type: '操作类型',
        remarks: '备注'
      }
    ]
  })
);
// 配置依赖信息查询
const conf = mockjs.mock([
  {
    id: '@id',
    name_zh: '应用中文名',
    name_en: '应用英文名',
    dev_managers: [
      {
        user_name_cn: '@cname',
        user_name_en: '@name'
      }
    ],
    spdb_managers: [
      {
        user_name_cn: '@cname',
        user_name_en: '@name'
      },
      {
        user_name_cn: '@cname',
        user_name_en: '@name'
      }
    ],
    'group|1': ['xxx组', 'xxx组', 'xxx组'],
    'git|1': ['ebank_fdev/issues', 'ebank_fdev']
  }
]);
let confList = new Array(6).fill({}, 0, 6);
confList = confList.map((conf, index) =>
  mockjs.mock({
    id: '@id',
    name_zh: '应用中文名' + index,
    name_en: '应用英文名' + index,
    dev_managers: [
      {
        user_name_cn: '@cname',
        user_name_en: '@name'
      }
    ],
    spdb_managers: [
      {
        user_name_cn: '@cname',
        user_name_en: '@name'
      },
      {
        user_name_cn: '@cname',
        user_name_en: '@name'
      }
    ],
    'group|1': ['xxx组', 'xxx组', 'xxx组'],
    'git|1': ['ebank_fdev/issues', 'ebank_fdev']
  })
);
// 实体 分类表常量信息
const modelConst = mockjs.mock([
  {
    id: '@id',
    type: ['deploy', 'runtime'],
    category: [
      {
        db: ['redis', 'informix']
      },
      {
        ci: ['config', 'eureka']
      }
    ],
    scope: ['comm', 'deploy', 'ims-user-perinfo'],
    ctime: '@time',
    utime: '@time',
    opno: 'opno'
  }
]);
//配置管理员审核
const check = mockjs.mock([
  {
    id: '@id',
    type: ['deploy', 'runtime'],
    category: [
      {
        db: ['redis', 'informix']
      },
      {
        ci: ['config', 'eureka']
      }
    ],
    scope: ['comm', 'deploy', 'ims-user-perinfo'],
    ctime: '@time',
    utime: '@time',
    opno: 'opno'
  }
]);
//获取某个环境及其实体的映射
const envModel = mockjs.mock({
  env: {
    id: '@id',
    name_cn: '@cname',
    name_en: '@name',
    desc: 'fgbjnmk,dghjk',
    status: '1',
    ctime: '@datetime',
    utime: '@datetime',
    opno: '@cname'
  },
  model: [
    {
      id: '@id',
      name_cn: '@cname',
      name_en: '@name',
      type: ['deploy', 'runtime'],
      first_category: 'aaa',
      second_category: 'bbb',
      suffix_name: 'abc',
      scope: 'common',
      version: '1-1',
      desc: 'fgbjnmk,dghjk',
      status: '1',
      ctime: '@datetime',
      utime: '@datetime',
      opno: '@cname',
      env_key: [
        {
          id: '@id',
          name_cn: '@cname',
          name_en: '@name',
          desc: 'fgbjnmk,dghjk',
          require: '1',
          value: '1'
        },
        {
          id: '@id',
          name_cn: '@cname',
          name_en: '@name',
          desc: 'fgbjnmk,dghjk',
          require: '0',
          value: '1'
        }
      ]
    },
    {
      id: '@id',
      name_cn: '@cname',
      name_en: '@name',
      type: ['deploy', 'runtime'],
      first_category: 'aaa',
      second_category: 'bbb',
      suffix_name: 'abc',
      scope: 'common',
      version: '1-1',
      desc: 'fgbjnmk,dghjk',
      status: '1',
      ctime: '@datetime',
      utime: '@datetime',
      opno: '@cname',
      env_key: [
        {
          id: '@id',
          name_cn: '@cname',
          name_en: '@name',
          desc: 'fgbjnmk,dghjk',
          require: '1',
          value: '1'
        },
        {
          id: '@id',
          name_cn: '@cname',
          name_en: '@name',
          desc: 'fgbjnmk,dghjk',
          require: '1',
          value: '1'
        }
      ]
    }
  ]
});

const configTemplate = mockjs.mock({
  'list|1-10': [
    '##dce_caas平台\n#caas 镜像仓库 ip\nci_dce_caas.fdev_caas_registry=${ci_dce_caas.fdev_caas_registry}\n',
    '#caas 用户名\nci_dce_caas.fdev_caas_registry_user=${ci_dce_caas.fdev_caas_registry_user}\n',
    '#caas 镜像仓库密码\nci_dce_caas.fdev_caas_registry_password=${ci_dce_caas.fdev_caas_registry_password}\n',
    '#caas 镜像仓库空间\nci_dce_caas.fdev_caas_registry_namespace=${ci_dce_caas.fdev_caas_registry_namespace}\n',
    '#caas dce ip地址\nci_dce_caas.fdev_caas_ip=${ci_dce_caas.fdev_caas_ip}\n',
    '#caas dce 用户名\nci_dce_caas.fdev_caas_user=${ci_dce_caas.fdev_caas_user}\n',
    '#caas dce 用户密码\nci_dce_caas.fdev_caas_pwd=${ci_dce_caas.fdev_caas_pwd}\n',
    '#caas dce 租户\nci_dce_caas.fdev_caas_tenant=${ci_dce_caas.fdev_caas_tenant}\n',
    '#caas dce secret秘钥\nci_dce_caas.fdev_caas_secret=${ci_dce_caas.fdev_caas_secret}\n',
    '#caas dce access秘钥\nci_dce_caas.fdev_caas_access=${ci_dce_caas.fdev_caas_access}\n',
    '##配置中心\n#服务器1\nci_config_center.fdev_config_host1_ip=${ci_config_center.fdev_config_host1_ip}\n',
    '#服务器2\nci_config_center.fdev_config_host2_ip=${ci_config_center.fdev_config_host2_ip}\n',
    '#服务器端口\nci_config_center.fdev_config_port=${ci_config_center.fdev_config_port}'
  ],
  data: function() {
    let obj = {
      code: 'AAAAAAA',
      data: this.list.toString().replace(/,/g, ''),
      msg: '交易执行成功'
    };
    return obj.data;
  }
});

const modelMessage = {
  total: 1,
  list: [
    {
      id: '5e5f4aa26e305373d8c01e4b',
      modelId: '5e463dd02acc2f0012d4f3c7',
      modelNameEn: 'private_com_hxy',
      modelNameCn: '新增实体测试1',
      envId: '5ce5388736da13000610790e',
      envNameEn: 'DEV',
      envNameCn: '开发环境',
      modelEnvId: '5e5c631ba3dcfe001263f1f8',
      desc: 'desc',
      variables: [
        {
          id: '5e463dd02acc2f0012d4f3c8',
          value: 'apply1',
          require: '1',
          name_en: 'test1'
        },
        {
          id: '5e4657509b34d1001238fda3',
          value: 'apply4',
          require: '1',
          name_en: 'test4'
        }
      ],
      type: 'update',
      status: 'checking',
      applyEmail: 'xxx',
      applyUsername: 'xxx',
      appNameEn: '',
      checkTime: '',
      envManager: '',
      createTime: '2020-03-04 14:28:50',
      updateTime: '2020-03-04 16:35:38'
    }
  ]
};

const compareData = {
  app: [],
  variables: [],
  desc: {
    newValue: 'desc',
    oldValue: 'apply desc'
  }
};

const modelSet = {
  id: '@id',
  'name_cn|1': ['个人组', '其他组'],
  'template|1': ['deploy', 'comm'],
  modelInfo: [
    {
      id: '@id',
      'name_en|1': ['ci_config_center', 'ci_dce_caas'],
      'name_cn|1': ['dce_caas平台1', '配置中心']
    }
  ]
};

const appProInfo = {
  total: 1,
  list: [
    {
      appInfo: {
        id: '54jneldf43r3f43r34',
        name_en: 'ims-web-abc'
      },
      group: '手机组',
      modelSet: '个人组微服务部署信息',
      testEnv: ['sit-biz', 'uat-biz'],
      productEnv: ['sh-k1-biz']
    }
  ]
};

const deployDetail = {
  app_name_en: '应用英文名',
  model_set_name_en: '实体组中文名',
  test_env: ['sit测试环境'],
  pro_env: ['上海-k1', '合肥-k1'],
  model_env_mapping: [
    {
      id: 'cuibru3hq98hucfdc',
      name_en: 'ci_dce_cass',
      name_cn: 'dce_cass平台1',
      type: 'comm',
      first_category: 'ci',
      second_category: 'dce',
      suffix_name: 'cass',
      scope: 'deploy',
      version: '1.0',
      desc: 'cass平台DCE信息',
      env: 'sit测试环境',
      env_key: [
        {
          name_en: 'fdev_cass_pvc',
          name_cn: 'pvc信息',
          require: '1',
          desc: 'pvc信息',
          json_schema: 'sint occaec',
          type: '1',
          have_path: '1',
          value: [
            {
              id: '1',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'consectetur cupidatat commodo ex',
              note: 'm',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1',
              have_path: '1'
            },
            {
              id: '2',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'laboris nostrud fugiat in sit',
              note: 'officia in in deserunt',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            }
          ],
          id: '1232423rsdsd'
        },
        {
          name_en: 'fdev_cass_pvc',
          name_cn: 'pvc信息',
          require: '1',
          desc: 'pvc信息',
          json_schema: 'in consequat tempor cupidatat',
          type: '1',
          have_path: '1',
          value: [
            {
              id: '3',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'tempor enim sed id',
              note: 'ut Lorem',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '4',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'veniam dolor',
              note: 'in tempor qui',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '5',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'non exercitation et anim in',
              note: 'occaecat pariatur veniam cillum eiusmod',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '6',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'sunt ut commodo dolor',
              note: 'ut non',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            }
          ],
          id: 'kopdkfwoef'
        }
      ],
      status: '1',
      ctime: '20190808102919',
      _class: 'com.spdb.fdev.fdevenvconfig.spdb.entity.Model',
      utime: '20190808102919',
      opno: 'dfd2212g3d21rg12212gr12g1r2'
    },
    {
      id: 'cnjkhfoueqr83u8chs;z ',
      name_en: 'ci_dce_cass',
      name_cn: 'dce_cass平台1',
      type: 'comm',
      first_category: 'ci',
      second_category: 'dce',
      suffix_name: 'cass',
      scope: 'deploy',
      version: '1.0',
      desc: 'cass平台DCE信息',
      env: '上海-k1',
      env_key: [
        {
          name_en: 'fdev_cass_pvc',
          name_cn: 'pvc信息',
          require: '1',
          desc: 'pvc信息',
          json_schema: 'sint occaec',
          type: '1',
          have_path: '1',
          value: [
            {
              id: '7',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'consectetur cupidatat commodo ex',
              note: 'm',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '8',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'laboris nostrud fugiat in sit',
              note: 'officia in in deserunt',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            }
          ],
          id: 'kvokoirt'
        },
        {
          name_en: 'fdev_cass_pvc',
          name_cn: 'pvc信息',
          require: '1',
          desc: 'pvc信息',
          json_schema: 'in consequat tempor cupidatat',
          type: '1',
          have_path: '1',
          value: [
            {
              id: '9',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'tempor enim sed id',
              note: 'ut Lorem',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '10',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'veniam dolor',
              note: 'in tempor qui',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '11',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'non exercitation et anim in',
              note: 'occaecat pariatur veniam cillum eiusmod',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '12',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'sunt ut commodo dolor',
              note: 'ut non',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            }
          ],
          id: '4ktokoivmfois0w'
        }
      ],
      status: '1',
      ctime: '20190808102919',
      _class: 'com.spdb.fdev.fdevenvconfig.spdb.entity.Model',
      utime: '20190808102919',
      opno: 'dfd2212g3d21rg12212gr12g1r2'
    },
    {
      id: 'cbauifqilSHDiv',
      name_en: 'ci_dce_cass',
      name_cn: 'dce_cass平台1',
      type: 'comm',
      first_category: 'ci',
      second_category: 'dce',
      suffix_name: 'cass',
      scope: 'deploy',
      version: '1.0',
      desc: 'cass平台DCE信息',
      env: '合肥-k1',
      env_key: [
        {
          name_en: 'fdev_cass_pvc',
          name_cn: 'pvc信息',
          require: '1',
          desc: 'pvc信息',
          json_schema: 'ullamco voluptate proident amet in',
          type: '1',
          have_path: '0',
          value: [
            {
              id: '13',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'exercitation quis sunt deserunt',
              note: 'consequat in nisi',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '14',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'laboris sed eu ',
              note: 'velit laboris ',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            }
          ],
          id: 'koijdoance'
        },
        {
          name_en: 'fdev_cass_pvc',
          name_cn: 'pvc信息',
          require: '1',
          desc: 'pvc信息',
          json_schema: 'Duis Ut qui',
          type: '1',
          have_path: '1',
          value: [
            {
              id: '15',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'deserunt quis in occaecat',
              note: 'eu ea dolor eiusmod culpa',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '16',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'eiusmod ad',
              note: 'id dolor est anim',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            }
          ],
          id: 'micjaoijfiojeonqjenfcje'
        },
        {
          name_en: 'fdev_cass_pvc',
          name_cn: 'pvc信息',
          require: '1',
          desc: 'pvc信息',
          json_schema: 'sed ad eiusmod qui',
          type: '1',
          have_path: '1',
          value: [
            {
              id: '17',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'non et aute in',
              note: 'eiusmod in',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '18',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'et cillum aliqua',
              note: 'Excepteur laborum velit',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '19',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'dolore',
              note: 'minim labore magna',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '20',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'dolore minim magna',
              note: 'laborum aliqua officia',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '21',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'cillum nostrud',
              note: 'laboris anim qui',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            }
          ],
          id: 'kcoanfmemcofjiwjf'
        },
        {
          name_en: 'fdev_cass_pvc',
          name_cn: 'pvc信息',
          require: '1',
          desc: 'pvc信息',
          json_schema: 'est elit',
          type: '1',
          have_path: '1',
          value: [
            {
              id: '22',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'laboris laborum ut',
              note: 'ad ut eiusmod',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '23',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'ex fugiat ullamco',
              note: 'do cupidatat eiusmod incididunt',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '24',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'velit sunt esse',
              note: 'officia esse Exce',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '25',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'sit',
              note: 'nulla laborum ea tempor',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            },
            {
              id: '26',
              name_en: 'pvc1',
              nfs_server_ip: 'xxx',
              path: '/wyxt-abc/',
              capacity: '100',
              nfs_version: 'v3',
              appname: 'tempor deserunt',
              note: 'in reprehenderit ex',
              mount_path: '/ebank/pvc1',
              sub_path: '/ebank/subpath1'
            }
          ],
          id: 'ciokoaifiefoiwmoic'
        }
      ],
      status: '1',
      ctime: '20190808102919',
      _class: 'com.spdb.fdev.fdevenvconfig.spdb.entity.Model',
      utime: '20190808102919',
      opno: 'dfd2212g3d21rg12212gr12g1r2'
    }
  ]
};

const envLabels = {
  default: ['default'],
  test: ['sit', 'uat', 'rel', 'yace'],
  pro: ['pro', 'biz', 'dmz']
};
const templateModelList = {
  name_en: '@name',
  name_cn: '@cword(3,5)',
  id: '@id'
};
const modelListPaging = {
  name_cn: '@cword(3,5)',
  name_en: '@name',
  one_type: '@String',
  two_type: '@String',
  suffix_name: '@String',
  action_scope: '@String',
  desc: '@cword(5,8)'
};
const HistoryMsgList = {
  total: 2,
  list: [
    {
      modelName: 'ci_dce_hxy',
      before: { desc: 'test', version: '0.3' },
      after: { desc: '测试', version: '0.4' },
      username: 'xxx',
      ctime: '2020-06-19 18:15:12',
      env: 'dev1-dmz'
    },
    {
      modelName: 'ci_dce_hxy',
      before: { desc: '2345', version: '0.3' },
      after: { desc: 'test123', version: '0.5' },
      username: 'xxx',
      ctime: '2020-06-19 18:15:12',
      env: 'sit1-biz'
    }
  ]
};
const HistoryMsgDetail = [
  {
    name_en: '高级属性测试实体',
    name_cn: 'private_per_hxy_biz',
    data_type: '',
    oldValue: '123',
    newValue: 'test123'
  }
];

module.exports = {
  'POST /fenvconfig/api/v2/env/query': mockjs.mock({
    'list|5': [env]
  })['list'],
  'POST /fenvconfig/api/v2/model/query': mockjs.mock({
    'list|5': [model]
  })['list'],
  /* 'POST /fenvconfig/api/v2/configfile/queryConfigTemplate': mockjs.mock({
    data: configTemplate.data
  }), */
  'POST /fenvconfig/api/v2/var/query': model_env,
  'POST /fenvconfig/api/v2/apply/queryApplyHistory': apply.concat(applyList),
  'POST /fenvconfig/api/v2/configfile/queryConfigDependency': conf.concat(
    confList
  ),
  'POST /fenvconfig/api/v2/model/queryModelCategory': modelConst,
  'POST /fenvconfig/api/v2/apply/adminCheck': check,
  'POST /fenvconfig/api/v2/var/CopyModelEnv': envModel,
  'POST /fenvconfig/api/v2/env/delete': mockjs.mock({
    id: '@id'
  }),
  'POST /fenvconfig/api/v2/model/delete': mockjs.mock({
    id: '@id'
  }),
  'POST /fenvconfig/api/v2/var/delete': mockjs.mock({
    id: '@id'
  }),
  'POST /fenvconfig/api/v2/env/add': mockjs.mock({
    name_en: '@name',
    name_cn: '@cname',
    desc: '你好'
  }),
  'POST /fenvconfig/api/v2/env/update': mockjs.mock({
    id: '@id',
    name_en: '@name',
    name_cn: '@cname',
    desc: '你好'
  }),
  'POST /fenvconfig/api/v2/verifycode/getVerifyCode':
    parseInt(Math.random() * 10000) + '',
  // mock验证码
  'POST /fenvconfig/api/v2/modelEnvUpdateApply/list': modelMessage,
  'POST /fenvconfig/api/v2/modelEnvUpdateApply/compare': compareData,
  'POST /fenvconfig/api/v2/modelSet/list': mockjs.mock({
    'list|12': [modelSet]
  })['list'],
  'POST /fenvconfig/api/v2/appProInfo/query': appProInfo,
  'POST /fenvconfig/api/v2/appProInfo/queryDeploy': deployDetail,
  'POST /fenvconfig/api/v2/env/queryAllLabels': envLabels,
  'POST /fenvconfig/api/v2/modelSet/queryTemplateContainsModel': mockjs.mock({
    'list|15': [templateModelList]
  })['list'],
  'POST /fenvconfig/api/v2/model/pageQuery': mockjs.mock({
    'list|25': [modelListPaging]
  }),
  'POST /fenvconfig/api/v2/history/getMappingHistoryList': HistoryMsgList,
  'POST /fenvconfig/api/v2/history/getMappingHistoryDetail': HistoryMsgDetail
};
