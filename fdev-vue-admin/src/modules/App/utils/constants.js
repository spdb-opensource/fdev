import { getGroupFullName, taskStage } from '@/utils/utils';
export function createAppModel() {
  return {
    gitlab_project_id: '',
    name_en: '',
    name_en_diy: '', //用户选择‘自定义英文名’时，‘应用英文名’的第三段
    name_zh: '',
    group: '',
    git: '',
    spdb_managers: [],
    dev_managers: [],
    archetype: null,
    gitlabci_id: '',
    sit: [{}],
    tasks_id: [],
    desc: '',
    gitlab_group: '',
    system: '', //应用所属系统
    service_system: {}, //应用所属业务域
    service_system_diy: {}, // 用户选择‘自定义英文名’时的‘所属业务域’
    newSystem: '', // service_system_diy的name_en值
    domain: {},
    domain_diy: {}, // 用户选择‘自定义英文名’时的‘应用所属域’
    newDomain: '', //domain_diy的name_en值
    schedule: '',
    importance: '',
    labels: [],
    type: null,
    env_deploy: '涉及环境部署',
    sonar_scan_switch: '1',
    isTest: '1',
    coding: null,
    appCiType: ''
  };
}

export function createExtraConfigMsg() {
  return {
    project_id: '',
    variables: []
  };
}

export function archetypeModel() {
  return {
    name: '',
    desc: '',
    artifactId: '',
    groupId: '',
    version: ''
  };
}

export function profileModel() {
  return {
    value: '',
    key: '',
    name_zh: ''
  };
}

export function formatApp(app) {
  return {
    ...app
  };
}

export function createCIModel() {
  return {
    name: '',
    yaml_name: ''
  };
}

export function createEnvVarModel() {
  return {
    var_key: '',
    var_name: ''
  };
}

export function createEnvModel() {
  return {
    env_name: '',
    env_type: '',
    env_vars: []
  };
}

// 应用概述
export function createSummaryModel() {
  return {
    name_zh: '',
    name_en: '',
    desc: '',
    spdb_managers: [],
    dev_managers: [],
    createtime: '',
    group: {},
    auto: '',
    schedule: '',
    sit: [{ auto: '', schedule: '' }],
    // status: '',
    network: '',
    git: '',
    importance: '',
    type_id: '',
    isTest: '1',
    appType: null,
    env_deploy: '涉及环境部署',
    sonar_scan_switch: 'false'
  };
}

export const icon = {
  waiting: 'ion-power',
  process: 'offline_bolt',
  passed: 'check_circle_outline',
  success: 'check_circle_outline',
  canceled: 'block',
  cancel: 'block',
  failed: 'highlight_off',
  error: 'highlight_off',
  skipped: 'chevron_right',
  running: 'ion-contrast',
  created: 'ion-radio-button-on',
  pending: 'ion-radio-button-on'
};

export const jobs_icon = {
  skipped:
    'http://xxx/assets/illustrations/skipped-job_empty-8b877955fbf175e42ae65b6cb95346e15282c6fc5b682756c329af3a0055225e.svg',
  canceled:
    'http://xxx/assets/illustrations/canceled-job_empty-e31e2dbd80462056c43251a1e3e169d5a273fb902909d54f869ed6fb10da5ed1.svg'
};

export function createPipelineScheduleModel() {
  return {
    autoSwitch: 'false'
  };
}

export const rating = {
  '1.0': 'A',
  '2.0': 'B',
  '3.0': 'C',
  '4.0': 'D',
  '5.0': 'E'
};

export function handleCoverRating(value) {
  if (value < 3) {
    return 'A';
  } else if (value < 5) {
    return 'B';
  } else if (value < 10) {
    return 'C';
  } else if (value < 20) {
    return 'D';
  } else {
    return 'E';
  }
}

export function interfaceColumns() {
  return [
    { name: 'transId', label: '交易码', field: 'transId', align: 'left' },
    {
      name: 'interfaceName',
      label: '接口名称',
      field: 'interfaceName',
      align: 'left'
    },
    {
      name: 'serviceId',
      label: '提供方',
      field: 'serviceId',
      align: 'left'
    },
    {
      name: 'branch',
      label: '分支',
      field: 'branch',
      align: 'left'
    },
    {
      name: 'requestType',
      label: '请求类型',
      field: 'requestType',
      align: 'left'
    },
    {
      name: 'interfaceType',
      label: '提供报文类型',
      field: 'interfaceType',
      align: 'left'
    },
    {
      name: 'esbServiceId',
      label: '服务ID',
      field: 'esbServiceId',
      align: 'center'
    },
    {
      name: 'esbOperationId',
      label: '操作ID',
      field: 'esbOperationId',
      align: 'center'
    },
    {
      name: 'interfaceAlias',
      label: '接口别名',
      field: 'interfaceAlias',
      align: 'center'
    },
    {
      name: 'description',
      label: '接口描述',
      field: 'description',
      align: 'center'
    }
  ];
}
export const statusOptions = [
  'pending',
  'running',
  'passed',
  'failed',
  'canceled'
];

export function createDeployModel() {
  return {
    branch: '',
    evns: '',
    specialEvns: '',
    reinforce: '1',
    desc: ''
  };
}

export const evnsOptions = [
  { label: 'test-测试', value: 'test' },
  { label: 'testrun-试运行', value: 'testrun' },
  { label: 'real-准生产', value: 'release' },
  { label: 'gray-灰度', value: 'gray' },
  { label: 'prod-生产', value: 'prod' }
];

export const specialOptions = [
  { label: '否', value: '' },
  { label: 'jrcs-兼容测试', value: 'jrcs' },
  { label: 'auto-自动化', value: 'auto' },
  { label: 'yace-压测', value: 'yace' }
];

export function snoarColumns() {
  return [
    {
      name: 'key',
      label: '',
      field: 'key',
      align: 'left',
      sortable: true
    },
    {
      name: 'bugs',
      label: 'Bugs',
      field: 'bugs',
      align: 'left',
      sortable: true
    },
    {
      name: 'vulnerabilities',
      label: '漏洞',
      field: 'vulnerabilities',
      align: 'left',
      sortable: true
    },
    {
      name: 'code_smells',
      label: '异味',
      field: 'code_smells',
      align: 'left',
      sortable: true
    },
    {
      name: 'duplicated_lines_density',
      label: '重复',
      field: field =>
        field.duplicated_lines_density
          ? field.duplicated_lines_density + '%'
          : '',
      align: 'left',
      sortable: true
    },
    {
      name: 'coverage',
      label: '覆盖率	',
      field: field => (field.coverage ? field.coverage + '%' : ''),
      align: 'left',
      sortable: true
    }
  ];
}

export function pipelinesColumns() {
  return [
    { name: 'status', label: 'Status', field: 'status', align: 'left' },
    {
      name: 'Pipeline',
      label: 'Pipeline',
      field: 'Pipeline',
      align: 'left'
    },
    { name: 'Commit', label: 'Commit', field: 'Commit', align: 'left' },
    { name: 'time', field: 'Stages', align: 'right' }
  ];
}

export function gitColumns() {
  return [
    {
      name: 'status',
      label: '状态',
      field: 'status'
    },
    {
      name: 'id',
      label: '构建号',
      field: 'id'
    },
    {
      name: 'commit',
      label: '提交',
      field: ''
    },
    {
      name: 'jobs',
      label: '阶段',
      field: ''
    },
    {
      name: 'finished_at',
      label: '最新构建时间',
      field: 'finished_at'
    }
  ];
}

export function ciListColumns() {
  return [
    {
      name: 'name',
      field: 'name',
      label: '持续集成模板名',
      align: 'left'
    },
    {
      name: 'yaml_name',
      field: 'yaml_name',
      label: 'yaml文件名',
      align: 'left'
    },
    {
      name: 'opt',
      field: 'opt',
      label: '操作',
      align: 'left'
    }
  ];
}

export function appPipelColumns() {
  return [
    {
      name: 'name',
      label: '流水线名称',
      field: 'name',
      align: 'center',
      headerClasses: 'my-special-class'
    },
    {
      name: 'desc',
      label: '描述',
      field: 'desc',
      align: 'center',
      headerClasses: 'my-special-class'
    },
    {
      name: 'nameCn',
      label: '创建人',
      field: row => row.author.nameCn,
      align: 'center',
      headerClasses: 'my-special-class'
    },
    {
      name: 'updateTime',
      label: '最近修改时间',
      field: 'updateTime',
      align: 'center',
      headerClasses: 'my-special-class'
    },
    {
      name: 'version',
      label: '变更号',
      field: 'version',
      align: 'center',
      headerClasses: 'my-special-class'
    },
    {
      name: 'btns',
      label: '更多操作',
      align: 'center',
      headerClasses: 'my-special-class'
    }
  ];
}

export function selectTempColumns() {
  return [
    {
      name: 'name',
      label: '模板名称',
      field: 'name',
      align: 'center',
      headerClasses: 'my-special-class'
    },
    {
      name: 'desc',
      label: '描述',
      field: 'desc',
      align: 'center',
      headerClasses: 'my-special-class'
    },
    {
      name: 'label',
      label: '标签',
      field: '',
      align: 'center',
      headerClasses: 'my-special-class'
    },
    {
      name: 'nameCn',
      label: '创建人',
      field: row => row.author.nameCn,
      align: 'center',
      headerClasses: 'my-special-class'
    },
    {
      name: 'updateTime',
      label: '最近修改时间',
      field: 'updateTime',
      align: 'center',
      headerClasses: 'my-special-class'
    },
    {
      name: 'btns',
      label: '预览',
      align: 'center',
      headerClasses: 'my-special-class'
    }
  ];
}

export function vipPackageColumns() {
  return [
    { name: 'App', label: 'App', field: 'App', align: 'left' },
    { name: 'status', label: 'Status', field: 'status', align: 'left' },
    {
      name: 'Triggerer',
      label: 'Triggerer',
      field: 'Triggerer',
      align: 'left'
    },
    {
      name: 'trigger_time',
      label: 'TriggerTime',
      field: 'trigger_time',
      align: 'left'
    },
    { name: 'Commit', label: 'Commit', field: 'Commit', align: 'left' },
    { name: 'Stages', label: 'Stages', field: 'Stages', align: 'left' },
    { name: 'time', field: 'time', align: 'left' },
    { name: 'btn', field: 'btn', align: 'right' }
  ];
}

export function profileTaskColumns(groups) {
  return [
    { name: 'name', label: '任务名称', field: 'name', align: 'left' },
    {
      name: 'redmine_id',
      label: '实施单元编码',
      field: 'redmine_id',
      align: 'left'
    },
    {
      name: 'stage',
      label: '任务阶段',
      field: row => taskStage[row.stage.value],
      align: 'left'
    },
    {
      name: 'group',
      label: '所属小组',
      field: row => getGroupFullName(groups, row.group.id),
      align: 'left'
    },
    {
      name: 'master',
      label: '行内项目负责人',
      field: 'spdb_master',
      align: 'left'
    }
  ];
}

export function appListColumns() {
  return [
    {
      name: 'name_zh',
      label: '应用中文名',
      field: 'name_zh',
      align: 'left'
    },
    {
      name: 'name_en',
      label: '应用英文名',
      field: 'name_en',
      align: 'left',
      sortable: true
    },
    {
      name: 'system',
      label: '应用所属系统',
      field: 'systemName_cn',
      align: 'left'
    },
    {
      name: 'group',
      label: '小组',
      field: row => row.group.name,
      align: 'left',
      sortable: true
    },
    {
      name: 'typeName',
      label: '应用类型',
      field: 'type_name',
      align: 'left'
    },
    {
      name: 'hangnei',
      label: '行内应用负责人',
      field: 'spdb_managers',
      align: 'left'
    },
    {
      name: 'yingyong',
      label: '应用负责人',
      field: 'dev_managers',
      align: 'left'
    },
    {
      name: 'label',
      label: '标签 / 重要度',
      field: 'label',
      align: 'left'
    },
    {
      name: 'git',
      label: 'git仓库',
      field: 'git',
      align: 'left'
    },
    {
      name: 'archetype_version',
      label: '应用骨架版本',
      field: 'archetype_version',
      align: 'left'
    },
    {
      name: 'def_rel',
      label: '默认rel环境',
      field: 'def_rel',
      align: 'center'
    },
    {
      name: 'def_uat',
      label: '默认uat环境',
      field: 'def_uat',
      align: 'center'
    },
    {
      name: 'desc',
      label: '应用描述',
      field: 'desc',
      align: 'center'
    },
    {
      name: 'sonar_scan_switch',
      label: 'sonar扫描卡点',
      field: row => (row.sonar_scan_switch === '1' ? '开' : '关'),
      align: 'center'
    }
  ];
}
