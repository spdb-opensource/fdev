export const DemandType = {
  Business: 'business', //业务类型
  Technology: 'tech', //科技类型
  Daily: 'daily' //日常类型
};
//代码审批列表
export function codeDoneColumns() {
  return [
    {
      name: 'source_branch',
      label: '源分支',
      field: 'source_branch',
      align: 'left'
    },
    {
      name: 'target_branch',
      label: '目标分支',
      field: 'target_branch',
      align: 'left'
    },
    {
      name: 'applicant_name',
      label: '申请人',
      field: 'applicant_name'
    },
    {
      name: 'apply_time',
      label: '申请时间',
      field: 'apply_time',
      align: 'left'
    },
    {
      name: 'apply_desc',
      label: '申请说明',
      field: 'apply_desc'
    },
    {
      name: 'auditor_name',
      label: '审批人',
      field: 'auditor_name'
    },
    {
      name: 'audit_time',
      label: '审批时间',
      field: 'audit_time'
    },
    {
      name: 'result_desc',
      label: '审批说明',
      field: 'result_desc'
    },
    {
      name: 'status',
      label: '审批状态',
      field: 'status',
      align: 'left'
    }
  ];
}
export const tipContent = {
  BusinessTip:
    '超过此实施单元当前阶段的计划日期或后续阶段日期，故无法设置，还请在此之前完成当前任务阶段！', //业务类型的tip
  TechnologyTip: '超过后续阶段日期，故无法设置！' //科技类型的tip
};

export function createdFinishModel() {
  return {
    finishTime: '',
    taskDesc: ''
  };
}
export function createdSelectRelationModel() {
  return {
    selectUser: ''
  };
}
export function createJobModel() {
  return {
    id: '',
    unitNo: '',
    name: '',
    jobType: '1',
    jiraId: '',
    group: null,
    rqrmnt_no: null,
    stage: {},
    partyA: [],
    partyB: [],
    creator: [],
    doc: [],
    review: {
      db: '否',
      data_base_alter: [],
      other_system: [],
      commonProfile: false,
      specialCase: [],
      securityTest: ''
    },
    developer: [],
    tester: [],
    watcher: [],
    devStartDate: '',
    sitStartDate: '',
    uatStartDate: '',
    uatEndDate: '',
    relStartDate: '',
    productionDate: '',
    proWantWindow: '',
    desc: '',
    env: [],
    tag: [],
    system_remould: '否',
    impl_data: '否',
    direction: '',
    difficulty: 1,
    fdev_implement_unit_no: '',
    taskType: '0',
    application: null,
    feature: '',
    featureType: '1',
    type: 'application',
    versionNum: '',
    simpleDemand: '1' // 是否简单需求任务 0-是（不走内测） 1-否(走内测)
  };
}

export function createRedmineData() {
  return {
    id: '',
    redmine_id: '',
    plan_start_time: '',
    plan_inner_test_time: '',
    plan_uat_test_start_time: '',
    plan_uat_test_stop_time: '',
    plan_rel_test_time: '',
    plan_fire_time: ''
  };
}

export function createEnvModel() {
  return {
    type: null,
    first_category: null,
    second_category: null,
    term: null,
    env: null
  };
}

export function createBranchModel() {
  return {
    application: null,
    feature: ''
  };
}

export const perform = {
  stages: [
    { code: 0, label: '开发', value: 'develop' },
    { code: 1, label: 'sit测试', value: 'sit' },
    { code: 2, label: 'uat测试', value: 'uat' },
    { code: 3, label: 'rel测试', value: 'rel' },
    {
      code: 4,
      label: '投产',
      value: 'production',
      type: 'success',
      title: '任务已投产',
      description: '任务已投产'
    },
    {
      code: 5,
      label: '已删除',
      value: 'abort',
      type: 'error',
      title: '任务已删除',
      description: '任务已删除'
    },
    {
      code: 6,
      label: '归档',
      value: 'file',
      type: 'success',
      title: '任务已归档',
      description: '任务已归档'
    },
    {
      code: '7',
      label: '废弃',
      value: 'discard',
      type: 'success',
      title: '任务已废弃',
      description: '任务已废弃'
    },
    { code: -1, label: '录入信息完成', value: 'create-info' },
    { code: -2, label: '录入应用完成', value: 'create-app' },
    { code: -3, label: '录入分支完成', value: 'create-feature' }
  ],
  unitType: {
    '0': '实施单元任务',
    '1': '日常任务'
  },

  networks: [
    { label: '业务', value: '业务' },
    { label: '网银dmz', value: '网银dmz' },
    { label: '其他', value: '其他' }
  ],
  fileTypes: {
    '1': '需求类',
    '2': '设计类',
    '3': '开发类',
    '4': '测试类',
    '5': '投产类',
    '5.1': '投产类-变更材料类',
    '6': '审核类',
    '6.1': '审核类-数据库审核材料'
  },
  testStatus: {
    '1': '执行通过',
    '2': '执行阻碍',
    '3': '执行失败',
    '4': '未执行',
    '': '执行中'
  },
  showData: [
    'profileName',
    'SPRING_CLOUD_CONFIG_URI',
    'hostLogsPath',
    'eurekaServerUri',
    'configServerUri',
    'CI_CAAS_REGISTRY',
    'CI_CAAS_IP',
    'CI_CAAS_TENANT',
    'eureka1ServerUri',
    'EUREKA_CLIENT_SERVICEURL_DEFAULTZONE'
  ],
  mainTasks: [{ label: '是', value: true }, { label: '否', value: false }],
  visibleColumnOptions: [
    'name',
    'project_name',
    'feature_branch',
    'stage',
    'plan_fire_time',
    'plan_inner_test_time',
    'test_tag',
    'developer',
    'master',
    'spdb_master',
    'group',
    'operation'
  ]
};
// 开发阶段前的状态
export const rqrmntState = {
  type: String,
  enums: [
    '意向',
    '预评估',
    '需求评估',
    '待实施',
    '开发中',
    'SIT',
    'UAT',
    'REL',
    '已投产'
  ]
};

export const fileType = {
  需求类: '1',
  设计类: '2',
  开发类: '3',
  测试类: '4',
  投产类: '5',
  变更材料类: '5.1',
  审核类: '6',
  数据库审核材料: '6.1',
  '投产类-测试报告类目': '7'
};

export const designOptions = {
  wait_upload: 1,
  uploaded: 1,
  wait_allot: 2,
  fixing: 3,
  fixed: 3,
  finished: 4
};
export function createProProblemModel() {
  return {
    id: '',
    task_no: '',
    requirement_name: '',
    module: '',
    occurred_time: '',
    reviewer_time: '',
    reviewer: [],
    first_occurred_time: '',
    location_time: '',
    repair_time: '',
    emergency_process: '',
    emergency_responsible: [],
    is_trigger_issue: '',
    release_node: '',
    issue_type: [],
    problem_phenomenon: '',
    influence_area: '',
    issue_reason: '',
    remark: '',
    dev_responsible: [],
    audit_responsible: [],
    test_responsible: [],
    task_responsible: [],
    discover_stage: '',
    is_uat_replication: '是',
    is_rel_replication: '是',
    is_gray_replication: '是',
    is_involve_urgency: '不涉及',
    deal_status: '未修复',
    issue_level: '',
    reviewer_status: '未评审',
    improvement_measures: '',
    reviewer_comment: '',
    responsibility_list: [
      {
        responsible: '',
        responsibility_type: '',
        responsibility_content: ''
      }
    ],
    backlog_schedule_list: [
      {
        backlog_schedule: '',
        backlog_schedule_reviewer: '',
        backlog_schedule_complete_time: '',
        backlog_schedule_current_completion: '',
        backlog_schedule_complete_percentage: ''
      }
    ],
    files: []
  };
}

export const issueTypeList = [
  '需求分析',
  '开发',
  '代码审核',
  '数据库审核',
  '内测',
  '业测',
  '打包',
  '其他'
];

export function testData() {
  return {
    status: '2', // 1开启，2关闭， 开启时不允许点击进入UAT
    id: [],
    desc: '',
    reason: '1', // 1.正常  2.缺陷  3. 需求变更
    stage: '2',
    regressionTestScope: false,
    regressionText: '',
    clientVersion: false,
    clientVersionText: '',
    testEnvText: '',
    interfaceChange: false,
    interfaceChangeText: '',
    copyTo: [],
    rqrNo: '',
    safetyTest: false,
    tripartiteSystem: '',
    systemInterface: '',
    safeDescribe: ''
  };
}

export const directionOptions = [
  {
    label: '前端',
    value: 'A'
  },
  {
    label: '服务端',
    value: 'B'
  },
  {
    label: '辅助应用',
    value: 'C'
  },
  {
    label: '其他',
    value: 'D'
  }
];

export const difficultyOptions = {
  A: [
    '页面纯样式调整',
    '页面路由和接口配套改造等',
    '页面样式联动和控制等',
    '新增功能，如：UI组件、页面等',
    '涉及前端页面大改版',
    '前端框架迁移或替换'
  ],
  B: [
    '现有交易涉及一般逻辑改造',
    '现有交易涉及接口改造、组件升级',
    '涉及数据库维护更新',
    '新增交易',
    '涉及逻辑改造和数据库维护更新',
    '涉及服务大改造、数据库大改造(特殊)'
  ],
  C: [
    '管理端交易',
    '仅对批量/host的小部分修改',
    '管理端交易且涉及大量数据维护',
    '批量/host新增交易或搬迁多个交易',
    '涉及数据仓库开发并导入数据',
    '涉及迁移或替换(特殊)'
  ],
  D: [
    '难度系数1(最简单)',
    '难度系数2',
    '难度系数3',
    '难度系数4',
    '难度系数5',
    '难度系数6(最困难)'
  ]
};
export const urgencyOptins = [
  '不涉及',
  '处对处紧急业务需求（t-7至t-4(含)）',
  '总对总紧急业务需求（t-3至t-2的9点之前）',
  '条线经理审批紧急科技需求（t-4(含)至t-1）',
  '当天紧急投产科技需求（t当天）'
];

export const securityTestOptions = [
  '不涉及',
  '安全漏洞修复',
  '涉及整包提请安全测试'
];

export const specialCaseOptions = [
  {
    label: '涉及操作系统、中间件升级/补丁修复',
    value: '涉及操作系统、中间件升级/补丁修复'
  },
  { label: '涉及数据库迁移/新建', value: '涉及数据库迁移/新建' },
  { label: '涉及网络策略变更', value: '涉及网络策略变更' },
  { label: '涉及存储策略变更', value: '涉及存储策略变更' }
];

export const fileTypeOptions = [
  { label: '设计类', value: '设计类' },
  { label: '开发类', value: '开发类' },
  { label: '测试类', value: '测试类' },
  {
    label: '投产类',
    value: '投产类',
    children: [
      { label: '自测报告', value: '投产类-自测报告' },
      { label: '变更材料类', value: '投产类-变更材料类' }
    ]
  },
  {
    label: '审核类',
    value: '审核类',
    children: [{ label: '数据库审核材料', value: '审核类-数据库审核材料' }]
  }
];

export const taskSpecialStatus = {
  '1': '暂缓',
  '3': '恢复完成'
};

// 延期任务
export const code = {
  production: '7',
  rel: '6',
  uat: '5',
  sit: '4',
  develop: '3',
  'create-feature': '2',
  'create-app': '1',
  'create-info': '0'
};
export const delayStageLists = [
  { label: '启动延期', value: 'develop' },
  { label: '内测延期', value: 'sit' },
  { label: '业测延期', value: 'uat' },
  { label: '准生产延期', value: 'rel' },
  { label: '投产延期', value: 'production' }
];
/* 电子看板*/
export const jobStages = [
  { label: '创建', value: 'todoList' },
  { label: '开发中', value: 'devList' },
  { label: 'SIT', value: 'sitList' },
  { label: 'UAT', value: 'uatList' },
  { label: 'REL', value: 'relList' },
  { label: '待归档', value: 'proList' }
];
/* 阶段颜色表 */
export const taskStageColorMap = {
  'create-info':
    'linear-gradient(270deg, rgba(226,156,70,0.50) 0%, #E29C46 100%)',
  'create-app':
    'linear-gradient(270deg, rgba(216,168,5,0.50) 0%, #AF6F02 100%)',
  'create-feature':
    'linear-gradient(270deg, rgba(218,149,78,0.50) 0%, #B35C26 100%)',
  develop: 'linear-gradient(90deg, #FD8D00 0%, rgba(253,141,0,0.50) 100%)',
  sit: 'linear-gradient(270deg, rgba(3,120,234,0.50) 0%, #0378EA 100%)',
  uat: 'linear-gradient(270deg, rgba(67,134,202,0.50) 0%, #4386CA 100%)',
  rel: 'linear-gradient(270deg, rgba(4,72,140,0.50) 0%, #04488C 100%)',
  production: 'linear-gradient(270deg, rgba(77,187,89,0.50) 0%, #4DBB59 100%)',
  file: 'linear-gradient(270deg, rgba(140,188,72,0.50) 0%, #8CBC48 100%)',
  abort: 'linear-gradient(270deg, rgba(176,190,197,0.50) 0%, #B0BEC5 100%)',
  discard: 'linear-gradient(270deg, rgba(153,153,153,0.50) 0%, #999999 100%)'
};
export const jgOption = [
  { label: '加固', id: '1' },
  { label: '不加固', id: '0' }
];
//任务新增 新建分支类型
export const applicationTypes = {
  Java微服务: 'appJava',
  Vue应用: 'appVue',
  IOS应用: 'appIos',
  Android应用: 'appAndroid',
  容器化项目: 'appDocker',
  老版服务: 'appOldService',
  镜像: 'image'
};

export const componentTypes = {
  mpass: 'componentWeb',
  back: 'componentServer'
};

export const archetTypes = {
  mpass: 'archetypeWeb',
  back: 'archetypeServer'
};

export const relMergeReasonOptions = [
  {
    label: '需求功能问题',
    value: 'demandIssue'
  },
  {
    label: '增删注释',
    value: 'annotation'
  },
  {
    label: '修改代码冲突',
    value: 'updateClash'
  },
  {
    label: '代码审核修改',
    value: 'codeCheckUpdate'
  },
  {
    label: '兼容性修复',
    value: 'compatibilityRepair'
  },
  {
    label: '改挂投产窗口',
    value: 'updateProWindow'
  },
  {
    label: '其他',
    value: 'other'
  }
];

export const relReasonMap = {
  demandIssue: '需求功能问题',
  annotation: '增删注释',
  updateClash: '修改代码冲突',
  codeCheckUpdate: '代码审核修改',
  compatibilityRepair: '兼容性修复',
  updateProWindow: '改挂投产窗口',
  other: '其他'
};

export const sitMergeReasonOptions = [
  {
    label: '需求功能问题',
    value: 'demandIssue'
  },
  {
    label: '增删注释',
    value: 'annotation'
  },
  {
    label: '修改代码冲突',
    value: 'updateClash'
  },
  {
    label: '代码审核修改',
    value: 'codeCheckUpdate'
  },
  {
    label: '兼容性修复',
    value: 'compatibilityRepair'
  },
  {
    label: '环境问题',
    value: 'envIssue'
  },
  {
    label: '其他',
    value: 'other'
  }
];

export const sitReasonMap = {
  demandIssue: '需求功能问题',
  annotation: '增删注释',
  updateClash: '修改代码冲突',
  codeCheckUpdate: '代码审核修改',
  compatibilityRepair: '兼容性修复',
  envIssue: '环境问题',
  other: '其他'
};

//sit多次合并
export function codeSitColumns() {
  return [
    {
      name: 'task_name',
      label: '任务名称',
      align: 'left',
      field: 'task_name'
    },
    {
      name: 'project_name',
      label: '所属应用',
      field: 'project_name',
      align: 'left'
    },
    {
      name: 'group_name',
      label: '所属小组',
      field: 'group_name',
      align: 'left'
    },
    {
      name: 'developer_name',
      label: '开发人员',
      field: 'developer_name',
      align: 'left'
    },
    {
      name: 'stage',
      label: '任务阶段',
      field: 'stage',
      align: 'left'
    },
    {
      name: 'source_branch',
      label: '源分支',
      field: 'source_branch',
      align: 'left'
    },
    {
      name: 'applicant_name',
      label: '提交人',
      field: 'applicant_name'
    },
    {
      name: 'apply_time',
      label: '提交时间',
      field: 'apply_time',
      align: 'left'
    },
    {
      name: 'apply_desc',
      label: '合并说明',
      field: 'apply_desc'
    },
    {
      name: 'merge_reason',
      label: '合并原因',
      field: 'merge_reason'
    }
  ];
}
