const mockjs = require('mockjs');
const { wrap7A, wrapError } = require('./utils/utils');

const job = {
  id: '@id',
  redmine_id: '@id',
  name: '理财产品展示优化',
  creator: {
    id: '@id',
    user_name_cn: '@cname',
    user_name_en: '@first'
  },
  group: {
    id: '@id',
    name: '个人'
  },
  project_id: '@id',
  project_name: '@string',
  'stage|1': ['develop', 'sit', 'uat', 'rel', 'production', 'create-info'],
  spdb_master: [{ id: '@id', user_name_cn: '甲方' }],
  master: [{ id: '@id', user_name_cn: 'xxx' }],
  desc: '描述描述描述描述描述描述描述描述描',
  plan_start_time: '@date',
  plan_inner_test_time: '@date',
  test_end_date: '@date',
  plan_fire_time: '@date',
  fire_time: '@date',
  feature_branch: 'master',
  git: '@url',
  test_info: {
    'stageStatus|1': ['1', '2', '3'],
    testPlan: []
  },
  'doc|10': [
    {
      name: '理财产品展示优化需求说明书@string', //文档名称
      'type|1': ['需求类', '设计类', '开发类', '测试类', '投产类'], //文档类型
      path:
        'http://xxx/ebank/doc/blob/master/理财产品展示优化需求说明书.docx', //文档存放路径
      date: '@date'
    }
  ],
  review: {
    other_system: [
      {
        id: '5c889d61a3178a2c689c72b9',
        name: '@string',
        audit: false
      }
    ],
    data_base_alter: [
      {
        id: '5c889d61a3178a2c689c72b3',
        name: 'name1',
        audit: true
      },
      {
        id: '5c889d61a3178a2c689c72b4',
        name: 'name2',
        audit: false
      },
      {
        id: '5c889d61a3178a2c689c72b5',
        name: 'name3',
        audit: false
      }
    ],
    ebank_common_alter: [
      {
        id: '5c889d61a3178a2c689c72b6',
        name: '@string',
        audit: false
      }
    ],
    fire_wall_open: [
      {
        id: '5c889d61a3178a2c689c72b7',
        name: '@string',
        audit: false
      }
    ],
    interface_alter: [
      {
        id: '5c889d61a3178a2c689c72b8',
        name: '@string',
        audit: false
      }
    ],
    script_alter: [
      {
        id: '5c889d61a3178a2c689c72ba',
        name: '@string',
        audit: false
      }
    ],
    static_resource: [
      {
        id: '5c889d61a3178a2c689c72ba',
        name: '@string',
        audit: false
      }
    ]
  },
  'developer|3-5': [{ id: '@id', user_name_cn: '@cname' }],
  'tester|3-5': [{ id: '@id', user_name_cn: '@cname' }],
  'concern|3-5': [{ id: '@id', name: '@string' }],
  task_plan_uat_test_start_time: '@date',
  task_plan_rel_test_time: '@date',
  task_finally_test_time: '@date',
  'sit_merge_status|1': [
    { status_code: '0', status_mean: '已合并，pipline构建失败' }
  ],
  'uat_merge_status|1': [
    { status_code: '0', status_mean: '已合并，pipline构建失败' }
  ],
  sit_merge_id: ['@id'],
  uat_merge_id: ['@id'],
  releaseNode: {
    release_application: {
      release_branch: '@first'
    }
  }
};
const mainJob = [
  {
    id: '201906142022327989897',
    main_task_: { name: '任务-ljb-0611-1120', id: '5cff1e3e9119d52c7061e15a' },
    taskcollection: [
      {
        name: '34retf',
        id: '5d1eb8bc32adec0010bff350'
      },
      {
        name: '任务0907-05',
        id: '5d2430fcd6736c0010e542ea'
      }
    ]
  }
];

const mergeInfo = {
  'status_code|1': ['1', '2', '3'],
  status_mean: '已合并，pipline构建成功'
};

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
  'POST /ftask/api/task/query': mockjs.mock({
    'list|20': [job]
  })['list'],
  'POST /ftask/api/task/queryByTerms': mockjs.mock({
    'list|20': [job]
  })['list'],
  'POST /ftask/api/task/queryByVague': mockjs.mock({
    'list|20': [job]
  })['list'],
  'POST /ftask/api/task/delete': mockjs.mock({
    id: '@id'
  }),
  'POST /ftask/api/task/add': (req, res) => {
    setTimeout(() => res.send(wrap7A(mockjs.mock(job))), 2000);
  },
  'POST /ftask/api/task/update': mockjs.mock(job),
  'POST /ftask/api/task/queryTaskDetail': mockjs.mock(job),
  'POST /ftask/api/task/putSitTest': mockjs.mock(job),
  'POST /ftask/api/task/putUatTest': mockjs.mock(job),
  'POST /ftask/api/task/queryBySubTask': mockjs.mock(mainJob),
  'POST /ftask/api/git/queryMergeInfo': mockjs.mock(mergeInfo),
  'POST /tmantis/mantisFdev/queryFtaskMantisAll': mockjs.mock({
    'list|20': [defectList]
  })['list']
};
