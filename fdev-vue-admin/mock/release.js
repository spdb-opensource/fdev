const plan = [
  {
    applications: [],
    assets_giturl: 'xxx/ebank/devops/testassets-sit/',
    auto_release_log: null,
    auto_release_stage: null,
    can_operation: true,
    create_time: '2019-11-01 14:35:11',
    date: '2019/11/04',
    launcher: '5daff409f57ddb001047b480',
    launcher_name_cn: 'xxx',
    owner_groupId: '1',
    group: '1',
    owner_group_name: '1',
    plan_time: '18:00',
    prod_assets_version: '1572796800000_1',
    prod_env: null,
    prod_id: '5dbbd21f3b288c0013cb574a',
    prod_spdb_no: '1',
    reject_reason: null,
    release_node_name: '20191101_001',
    status: '0',
    temp_name: null,
    template_id: '5db00007a9f8f30012ff54b4',
    template_properties: null,
    type: 'gray',
    version: 'DOCKER_123456789_20191104_1800',
    _id: {
      timestamp: 1572590111,
      machineIdentifier: 3877004,
      processIdentifier: 19,
      counter: 13326154
    }
  }
];

const release = [];

const system = [
  {
    id: '5cacsp2cd3ey6ov874e52hz9',
    name_en: 'mspay',
    name_cn: '对外支付系统',
    system_id: '5cacsp2cd3ey6ov874e52hz9',
    system_abbr: '111',
    resource_giturl: [
      'xxx/ebank/devops/testGroup/msper-web-yingyong'
    ],
    resource_projectid: null,
    sysname_en: 'mspay',
    sysname_cn: '对外支付系统'
  },
  {
    id: '5cac052cd3e2a12490e52b90',
    name_en: 'mspmk',
    name_cn: '个人手机',
    system_id: '5cac052cd3e2a12490e52b90',
    system_abbr: '123',
    resource_giturl: [
      'xxx/ebank/devops/testGroup/msper-web-yingyong'
    ],
    resource_projectid: null,
    sysname_en: 'mspmk',
    sysname_cn: '个人手机'
  },
  {
    id: '9trcs13s73ey6th7zte52tr3',
    name_en: 'ipay',
    name_cn: '支付对内联机',
    system_id: '9trcs13s73ey6th7zte52tr3',
    system_abbr: '110',
    resource_giturl: [
      'xxx/ebank/devops/testGroup/msper-web-yingyong'
    ],
    resource_projectid: null,
    sysname_en: 'ipay',
    sysname_cn: '支付对内联机'
  },
  {
    id: '5c9dba0bd3e2a13658a8e1b6',
    name_en: 'msper',
    name_cn: '个人网银',
    sysname_en: 'msper',
    sysname_cn: '个人网银'
  },
  {
    id: '19gt613s73ey6th7ztezf54s',
    name_en: '',
    name_cn: '自定义系统',
    sysname_en: '',
    sysname_cn: '自定义系统'
  },
  {
    id: '5cacsp2cd3ey6520zte52yi1',
    name_en: 'msfts',
    name_cn: '对外投融资系统',
    sysname_en: 'msfts',
    sysname_cn: '对外投融资系统'
  },
  {
    id: '5cac052cd3e2r34874e52q10',
    name_en: 'nbh',
    name_cn: '对外公共系统',
    sysname_en: 'nbh',
    sysname_cn: '对外公共系统'
  },
  {
    id: '5cac052cd3e2a65937e52r75',
    name_en: 'ims',
    name_cn: '对内公共系统',
    sysname_en: 'ims',
    sysname_cn: '对内公共系统'
  }
];

const template = [
  {
    _id: null,
    id: '5db00007a9f8f30012ff54b4',
    temp_name: 'msper-web-pluto-变更管理_20191022_19KF441413',
    owner_system: '5cacsp2cd3ey6ov874e52hz9',
    template_type: 'gray',
    owner_app: '5daffb842ab58a0010da56d8',
    temp_giturl:
      'xxx/ebank/devops/prodtemplate-sit/blob/master/变更管理_20191022_19KF441413.xls',
    update_user: '5daff409f57ddb001047b480',
    update_time: '2019-10-23 15:42:32',
    file_md5: '512402ea170a9c9bd76f1b49d0bec5c7',
    status: null,
    update_username_cn: 'xxx',
    owner_app_name: '冥王星-pluto',
    owner_system_name: '对外支付系统',
    catalogs: [
      {
        _id: null,
        id: '5db0046801200b00122f16ba',
        template_id: '5db00007a9f8f30012ff54b4',
        catalog_name: 'commonconfig',
        description: '公共配置文件更新',
        catalog_type: '5'
      },
      {
        _id: null,
        id: '5db0046801200b00122f16b9',
        template_id: '5db00007a9f8f30012ff54b4',
        catalog_name: 'db_mbank',
        description: '手机数据库更新',
        catalog_type: '3'
      },
      {
        _id: null,
        id: '5db0046801200b00122f16bb',
        template_id: '5db00007a9f8f30012ff54b4',
        catalog_name: 'db_nbper',
        description: '个人数据库更新',
        catalog_type: '3'
      },
      {
        _id: null,
        id: '5db0046801200b00122f16bc',
        template_id: '5db00007a9f8f30012ff54b4',
        catalog_name: 'docker',
        description: '微服务应用更新',
        catalog_type: '2'
      }
    ]
  },
  {
    _id: null,
    id: '5db004ad01200b00122f16bd',
    temp_name: 'msper-web-yingyong-测试流程',
    owner_system: '5cac052cd3e2a12490e52b90',
    template_type: 'gray',
    owner_app: '5daff7dd2ab58a0010da56d4',
    temp_giturl:
      'xxx/ebank/devops/prodtemplate-sit/blob/master/测试流程.xlsx',
    update_user: '5daff409f57ddb001047b480',
    update_time: '2019-10-23 15:43:38',
    file_md5: '6f77919e135af38b081efeac17a2642c',
    status: null,
    update_username_cn: '周新丽',
    owner_app_name: '应用1',
    owner_system_name: '个人手机',
    catalogs: [
      {
        _id: null,
        id: '5db004ad01200b00122f16c3',
        template_id: '5db004ad01200b00122f16bd',
        catalog_name: 'commonconfig',
        description: '公共配置文件更新',
        catalog_type: '5'
      },
      {
        _id: null,
        id: '5db004ad01200b00122f16bf',
        template_id: '5db004ad01200b00122f16bd',
        catalog_name: 'config',
        description: '配置文件更新',
        catalog_type: '4'
      },
      {
        _id: null,
        id: '5db004ad01200b00122f16c1',
        template_id: '5db004ad01200b00122f16bd',
        catalog_name: 'db_mbank',
        description: '手机数据库更新',
        catalog_type: '3'
      },
      {
        _id: null,
        id: '5db004ad01200b00122f16c2',
        template_id: '5db004ad01200b00122f16bd',
        catalog_name: 'db_nbper',
        description: '个人数据库更新',
        catalog_type: '3'
      },
      {
        _id: null,
        id: '5db004ad01200b00122f16c0',
        template_id: '5db004ad01200b00122f16bd',
        catalog_name: 'docker',
        description: '微服务应用更新',
        catalog_type: '2'
      },
      {
        _id: null,
        id: '5db004ad01200b00122f16be',
        template_id: '5db004ad01200b00122f16bd',
        catalog_name: 'nbnewperAPP',
        description: '个人常规应用包更新',
        catalog_type: '1'
      }
    ]
  },
  {
    _id: null,
    id: '5db004fb01200b00122f16c5',
    temp_name: 'msper-web-yingyong-20191017投产清单案例',
    owner_system: '9trcs13s73ey6th7zte52tr3',
    template_type: 'proc',
    owner_app: '5daff7dd2ab58a0010da56d4',
    temp_giturl:
      'xxx/ebank/devops/prodtemplate-sit/blob/master/20191017投产清单案例.xlsx',
    update_user: '5daff409f57ddb001047b480',
    update_time: '2019-10-23 15:44:58',
    file_md5: 'ff80465a68aa770099dca09f1312350c',
    status: null,
    update_username_cn: '周新丽',
    owner_app_name: '应用1',
    owner_system_name: '支付对内联机',
    catalogs: [
      {
        _id: null,
        id: '5db004fb01200b00122f16c6',
        template_id: '5db004fb01200b00122f16c5',
        catalog_name: 'commonconfig',
        description: '公共配置文件更新',
        catalog_type: '5'
      }
    ]
  },
  {
    _id: null,
    id: '5db15821c8ebf40013c96126',
    temp_name: '变更管理_个人网银系统微服务_20190603 (1)',
    owner_system: '5cac052cd3e2a12490e52b90',
    template_type: 'gray',
    owner_app: null,
    temp_giturl:
      'xxx/ebank/devops/prodtemplate-sit/blob/master/变更管理_个人网银系统微服务_20190603 (1).xls',
    update_user: '5daff4cbf57ddb001047b482',
    update_time: '2019-10-24 15:51:58',
    file_md5: '6a1f69da0ac2b8a75816438e2b6c7590',
    status: null,
    update_username_cn: 'xxx',
    owner_app_name: '',
    owner_system_name: '个人手机',
    catalogs: [
      {
        _id: null,
        id: '5db15821c8ebf40013c96127',
        template_id: '5db15821c8ebf40013c96126',
        catalog_name: 'docker',
        description: '微服务应用更新',
        catalog_type: '2'
      }
    ]
  }
];

const templateDetail = template[0];

const optionalcatalog = [
  {
    catalog_name: '111',
    catalog_type: '2',
    description: '模块类型'
  },
  {
    catalog_name: '35',
    catalog_type: '4',
    description: '模块类型'
  }
];
const taskList = [];

const testRunBranch = {
  application_id: '5dd365e8594da200132ed6d4',
  gitlab_project_id: 295,
  id: '5e4fa36fb27a6c0014b6be34',
  release_node_name: '20201101_001',
  testrun_branch: 'testrun-20201101_001-006',
  testrun_url: null,
  transition_branch: 'transition-20201101_001-006'
};

const allProdAssets = {
  prod_assets: [
    {
      catalog_name: 'docker_scale',
      children: [
        {
          catalog_name: '/TCYZ/TCYZ',
          children: [
            {
              asset_name: 'order.txt (介质准备后自动生成)'
            }
          ],
          catalog_type: '',
          catalog_description: ''
        },
        {
          catalog_name: '/TEST/TEST',
          children: [
            {
              asset_name: 'order.txt (介质准备后自动生成)'
            }
          ],
          catalog_type: '',
          catalog_description: ''
        }
      ],
      catalog_type: null,
      catalog_description: '弹性伸缩'
    },
    {
      catalog_name: 'commonconfig',
      children: [
        {
          catalog_name: 'DEV',
          children: [
            {
              asset_name: 'test/1575597485788.jpg',
              asset_url:
                'xxx/ebank/devops/testassets-sit/20201101_001/DOCKER_9D_20201104_1430/tree/master/commonconfig/DEV/test/1575597485788.jpg'
            }
          ],
          catalog_type: '',
          catalog_description: ''
        },
        {
          catalog_name: 'TEST',
          children: [
            {
              asset_name: 'test/1575597485788.jpg',
              asset_url:
                'xxx/ebank/devops/testassets-sit/20201101_001/DOCKER_9D_20201104_1430/tree/master/commonconfig/TEST/test/1575597485788.jpg'
            }
          ],
          catalog_type: '',
          catalog_description: ''
        },
        {
          catalog_name: 'TCYZ',
          children: [
            {
              asset_name: 'test/1575597485788.jpg',
              asset_url:
                'xxx/ebank/devops/testassets-sit/20201101_001/DOCKER_9D_20201104_1430/tree/master/commonconfig/TCYZ/test/1575597485788.jpg'
            }
          ],
          catalog_type: '',
          catalog_description: ''
        },
        {
          catalog_name: 'PROCSH',
          children: [
            {
              asset_name: 'test/1575597485788.jpg',
              asset_url:
                'xxx/ebank/devops/testassets-sit/20201101_001/DOCKER_9D_20201104_1430/tree/master/commonconfig/PROCSH/test/1575597485788.jpg'
            }
          ],
          catalog_type: '',
          catalog_description: ''
        }
      ],
      catalog_type: '5',
      catalog_description: '公共配置文件更新'
    },
    {
      catalog_name: 'db_mbank',
      children: [
        {
          asset_name: 'order.txt (介质准备后自动生成)'
        },
        {
          asset_name: '1. IMG_5997.JPG',
          asset_url:
            'xxx/ebank/devops/testassets-sit/20201101_001/DOCKER_9D_20201104_1430/tree/master/db_mbank/IMG_5997.JPG'
        }
      ],
      catalog_type: '3',
      catalog_description: '手机数据库更新'
    },
    {
      catalog_name: 'config',
      children: [
        {
          asset_name: 'touchbarpet.app.zip',
          asset_url:
            'xxx/ebank/devops/testassets-sit/20201101_001/DOCKER_9D_20201104_1430/tree/master/config/touchbarpet.app.zip'
        }
      ],
      catalog_type: '4',
      catalog_description: '配置文件更新'
    }
  ],
  application_tips: ''
};

const reviewRecord = {
  record: [
    {
      taskName: '关联项审核',
      appName: 'fdev',
      master: [],
      reviewStatus: '',
      applicant: '',
      group: '',
      reviewer: [],
      reviewIdea: '',
      reviewType: ''
    }
  ],
  sum: 1
};

const reviewRecordDetail = {
  data_base_alter: ['data_base_alter'],
  ebank_common_alter: ['ebank_common_alter'],
  fire_wall_open: ['fire_wall_open'],
  interface_alter: ['interface_alter'],
  other_system: ['other_system'],
  script_alter: [],
  static_resource: []
};

module.exports = {
  'POST /frelease/api/release/queryPlan': plan,
  'POST /frelease/api/releasenode/queryReleaseNodes': release,
  'POST /frelease/api/release/query': plan,
  'POST /frelease/api/release/query/frelease/api/release/querySysRlsInfo': system,
  'POST /frelease/api/template/query': template,
  'POST /frelease/api/template/queryDetail': templateDetail,
  'POST /frelease/api/optionalcatalog/query': optionalcatalog,
  'POST /ftask/api/task/queryAllWickedTasksByAppId': taskList,
  'POST /frelease/api/testrun/createTestrunBranch': testRunBranch,
  'POST /frelease/api/release/queryAllProdAssets': allProdAssets,
  'POST /ftask/api/task/review/fuzzyQueryReviewRecord': reviewRecord,
  'POST /ftask/api/task/review/queryTaskReview': reviewRecordDetail
};
