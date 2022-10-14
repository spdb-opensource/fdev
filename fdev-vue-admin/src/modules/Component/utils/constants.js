import SessionStorage from '#/plugins/SessionStorage';
import { getGroupFullName } from '@/utils/utils';
function getVersionType(val) {
  return val == '0'
    ? '正式版本'
    : val == '1'
    ? '推荐版本'
    : val == '2'
    ? '废弃版本'
    : '测试版本';
}

// 组件类型
export const ComponentModelType = {
  Business: '0', //业务组件类型
  Service: '1', //服务组件类型
  Ui: '2', //ui组件类型
  Finance: '3', //金融组件类型
  Plugin: '4' //插件组件类型
};

export function componentModel() {
  return {
    name_en: '',
    name_cn: '',
    desc: '',
    gitlab_url: '',
    groupId: '',
    artifactId: '',
    manager_id: [],
    group: '',
    type: '',
    parentId: '',
    root_dir: '',
    jdk_version: '',
    wiki_url: '',
    sonar_scan_switch: '0',
    isTest: '1',
    source: '',
    recommond_version: ''
  };
}

export function relComponentModel() {
  return {
    title: '',
    manager: [],
    issue_type: '',
    feature_branch: '',
    predict_version: '',
    due_date: '',
    desc: ''
  };
}

export function devComponentModel() {
  return {
    assignee: {
      id: '',
      user_name_cn: '',
      user_name_en: ''
    },
    stage: [],
    feature_branch: '',
    sit_date: '',
    uat_date: '',
    due_date: '',
    desc: ''
  };
}

export function historyModel() {
  return {
    id: '',
    name_cn: '',
    jdk_version: '',
    release_log: '',
    type: ''
  };
}

export function mpassHisModel() {
  return {
    id: '',
    component_id: '',
    version: '',
    date: '',
    update_user: [],
    release_log: '',
    type: ''
  };
}

export function imageRecordHistoryModel() {
  return {
    update_time: '',
    trial_apps: '',
    name_cn: '',
    release_log: '',
    type: ''
  };
}

export function optimizeModel() {
  return {
    component: '',
    component_id: '',
    title: '',
    desc: '',
    target_version: '',
    feature_branch: '',
    assignee: '',
    due_date: ''
  };
}

export function imageIssueModel() {
  return {
    name: '',
    title: '',
    branch: '',
    desc: '',
    assignee: '',
    due_date: ''
  };
}

export function baseImageModel() {
  return {
    name: '',
    name_cn: '',
    gitlab_url: '',
    isTest: '1',
    manager: [],
    groupObj: '',
    type: '',
    target_env: '',
    wiki: '',
    meta_data_declare: {},
    description: ''
  };
}

export function trialPublishModel() {
  return {
    release_log: '',
    trial_apps: [],
    meta_data: []
  };
}

export function packageModel() {
  return {
    component_id: '',
    jdk_version: SessionStorage.getItem('jdk') || '1.8',
    stage: '',
    target_version: '',
    feature_branch: '',
    release_log: '',
    update_user: '',
    email_content: ''
  };
}

export function archetypeDialogModel() {
  return {
    name_en: '', //骨架英文名
    name_cn: '', //骨架中文名
    desc: '', // 骨架描述
    gitlab_url: '', // gitlab地址
    sonar_scan_switch: '0',
    isTest: '1',
    groupId: '',
    artifactId: '',
    manager_id: [], // 骨架管理员
    type: '', // 骨架类型
    wiki_url: '', // wiki地址
    encoding: '', // 编码
    groupObj: '',
    application_path: '' //环境配置文件路径
  };
}

export function archetypeOptimizeModel() {
  return {
    archetype_id: '',
    archetype: '',
    title: '',
    target_version: '',
    feature_branch: '',
    assignee: '',
    due_date: '',
    desc: ''
  };
}

export const typeOptions = [
  { label: '单模块组件', value: '0' },
  { label: '多模块组件父级模块', value: '1' },
  { label: '多模块组件子模块', value: '2' },
  { label: '多模块组件', value: '3' }
];

export const webTypeOptions = [
  { label: '业务组件', value: '0' },
  { label: '服务组件', value: '1' },
  { label: 'UI组件', value: '2' },
  { label: '金融组件', value: '3' },
  { label: '插件', value: '4' }
];

export const webTypeFilter = {
  '0': '业务组件',
  '1': '服务组件',
  '2': 'UI组件',
  '3': '金融组件',
  '4': '插件'
};

export const webSourceOptions = [
  { label: '组内维护(自研)', value: '0' },
  { label: '第三方(开源)', value: '1' }
];

export const needTypeList = [
  { needTypeName: '功能新增', id: '0' },
  { needTypeName: '问题修复', id: '1' }
];

export const imageTypeOptions = [
  { label: '微服务', value: 'mirioservice' },
  { label: '容器化', value: 'containerization' }
];

export const targetEnvOptions = [
  { label: '测试环境版本', value: 'test' },
  { label: '生产环境版本', value: 'pro' }
];

export const typeDict = {
  '0': '正式版本',
  '1': '推荐版本',
  '2': '废弃版本',
  '3': '测试版本'
};

export const webTypeDict = {
  '0': '正式版本',
  '1': '推荐版本',
  '2': '废弃版本',
  '3': '测试版本'
};

export const issueTypeDict = {
  '0': '功能新增',
  '1': '问题修复'
};

export const imageTypeDict = {
  dev: '开发版本',
  trial: '试用版本',
  release: '正式版本',
  invalid: '废弃版本'
};

export const stage = {
  '0': '开发',
  '1': '内测(alpha)',
  '2': 'rc发布(rc)',
  '3': '正式发布(release)',
  '4': '已发布'
};

export const webStageOptions = [
  { label: '新增', value: '0' },
  { label: '内测(alpha)', value: '1' },
  { label: '公测(beta)', value: '2' },
  { label: '试运行(rc)', value: '3' }
];

export const webStage = {
  '0': '新增',
  '1': '内测(alpha)',
  '2': '公测(beta)',
  '3': '试运行(rc)'
};

export const imageStage = {
  '0': '新增优化需求',
  '1': '开发测试',
  '2': '试用发布',
  '3': '正式发布（推广）'
};

export const imageStageVersion = {
  '1': 'dev',
  '2': 'trial',
  '3': 'release'
};

export const archetypeStage = {
  '0': '开发',
  '1': '内测(alpha)',
  '2': '正式发布(release)',
  '3': '已发布'
};

export const jdkVersionOptions = [
  { label: '1.7', value: '1.7' },
  { label: '1.8', value: '1.8' },
  { label: '未知', value: '' }
];

export const versionOptions = [
  { label: '全部', value: '' },
  { label: 'SNAPSHOT', value: 'snapshot' },
  { label: 'RC', value: 'rc' },
  { label: 'RELEASE', value: 'release' }
];

export const webVersionOptions = [
  { label: '全部', value: '' },
  { label: 'ALPHA', value: 'alpha' },
  { label: 'BETA', value: 'beta' },
  { label: 'RC', value: 'rc' },
  { label: 'RELEASE', value: 'release' }
];

export const imageversionOptions = [
  { label: '全部', value: '' },
  { label: 'DEV', value: 'dev' },
  { label: 'TRIAL', value: 'trial' },
  { label: 'INVALID', value: 'invalid' },
  { label: 'RELEASE', value: 'release' }
];

export const archetypeVersionOptions = [
  { label: '全部', value: '' },
  { label: 'SNAPSHOT', value: 'snapshot' },
  { label: 'RELEASE', value: 'release' }
];

export const sourceOptions = [
  { label: '组内维护(自研)', value: '0' },
  { label: '第三方(开源)', value: '1' }
];

export function createEnvModel() {
  return {
    type: null,
    first_category: null,
    second_category: null,
    term: null,
    env: ''
  };
}

export function createJobModel() {
  return {
    id: '',
    unitNo: '',
    name: '',
    group: {},
    rqrmnt: null,
    stage: {},
    partyA: [],
    partyB: [],
    doc: [],
    review: {
      external: [],
      db: '否',
      config: [],
      firewall: [],
      interface: [],
      script: [],
      asset: []
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
    desc: '',
    env: [],
    tag: []
  };
}

export function createWebComModel() {
  return {
    name_en: '',
    name_cn: '',
    desc: '',
    gitlab_url: '',
    npm_name: '',
    npm_group: '',
    isTest: '1',
    manager: [],
    source: '',
    group: '',
    root_dir: '',
    type: '',
    skillstack: '',
    businessarea: ''
  };
}

export function WebOptimizeDialogModel() {
  return {
    component: '',
    component_id: '',
    title: '',
    predict_version: '',
    issue_type: '',
    due_date: '',
    desc: '',
    feature_branch: '',
    manager: []
  };
}

export function WebAddDevDialogModel() {
  return {
    issue_id: '',
    assignee: '',
    feature_branch: '',
    due_date: '',
    desc: '',
    sit_date: '',
    uat_date: ''
  };
}

export function mpassPackageModel() {
  return {
    update_user: '',
    release_log: '',
    tag_name: ''
  };
}

export function mpassReleasePackageModel() {
  return {
    tag_name: '',
    release_log: ''
  };
}

export function WebArchetypeDialogModel() {
  return {
    name_en: '', //骨架英文名
    name_cn: '', //骨架中文名
    desc: '', // 骨架描述
    gitlab_url: '', // gitlab地址
    manager: [], // 骨架管理员
    isTest: '1',
    groupObj: ''
  };
}

export function WebArchetypeOptimizeDialogModel() {
  return {
    title: '',
    assignee: '',
    desc: '',
    feature_branch: '',
    due_date: ''
  };
}

export function archetypeHistoryModel() {
  return {
    id: '',
    archetype_id: '',
    version: '',
    date: '',
    update_user: '',
    release_log: '',
    type: ''
  };
}

export const imageManageHandlePageColumns = [
  {
    name: 'image_tag',
    label: '版本',
    field: 'image_tag',
    align: 'left'
  },
  {
    name: 'update_user',
    label: '更新人员',
    field: 'update_user',
    align: 'left'
  },
  {
    name: 'update_time',
    label: '数据更新时间',
    field: 'update_time',
    align: 'left'
  },
  {
    name: 'release_log',
    label: '发布日志',
    field: 'release_log',
    align: 'left'
  }
];

export const imageManageProfileArchetypeUsingStatusColumns = [
  {
    name: 'name_en',
    label: '骨架英文名',
    field: row => row.name_en,
    align: 'left',
    sortable: true
  },
  {
    name: 'name_zh',
    label: '骨架中文名',
    field: row => row.name_zh,
    align: 'left',
    sortable: true
  },
  {
    name: 'archetype_version',
    label: '骨架版本',
    field: 'archetype_version',
    align: 'left',
    sortable: true
  },
  {
    name: 'image_tag',
    label: '镜像版本',
    field: 'image_tag',
    align: 'left',
    sortable: true
  },
  {
    name: 'stage',
    label: '镜像版本类型',
    field: 'stage',
    align: 'left',
    sortable: true
  },
  {
    name: 'update_time',
    label: '数据更新时间',
    field: 'update_time',
    align: 'left'
  }
];

export const imageManageProfileOptimizeColumns = [
  {
    name: 'title',
    label: '标题',
    field: 'title',
    align: 'left'
  },
  {
    name: 'branch',
    label: '开发分支',
    field: 'branch',
    align: 'left'
  },
  {
    name: 'name_cn',
    label: '开发人员',
    field: 'name_cn',
    align: 'left'
  },
  {
    name: 'stage',
    label: '需求阶段',
    field: 'stage',
    align: 'left',
    sortable: true
  },
  {
    name: 'due_date',
    label: '计划完成日期',
    field: 'due_date',
    align: 'left',
    sortable: true
  },
  {
    name: 'btn',
    label: '操作',
    field: 'btn',
    align: 'left'
  }
];

export function imageManageColumns(groups) {
  return [
    {
      name: 'name',
      label: '镜像英文名',
      field: row => row.name,
      sortable: true
    },
    {
      name: 'name_cn',
      label: '镜像中文名',
      field: row => row.name_cn,
      sortable: true
    },
    {
      name: 'manager',
      label: '管理员',
      field: 'manager'
    },
    {
      name: 'group',
      label: '所属小组',
      field: field => getGroupFullName(groups, field.group),
      align: 'left'
    },
    {
      name: 'type',
      field: row => row.type,
      label: '类型',
      align: 'left'
    },
    {
      name: 'target_env',
      label: '目标环境',
      field: row => row.target_env,
      align: 'left'
    },
    {
      name: 'gitlab_url',
      label: 'gitlab地址',
      field: 'gitlab_url',
      align: 'left'
    },
    {
      name: 'wiki',
      label: 'wiki地址',
      field: 'wiki',
      align: 'left'
    },
    {
      name: 'description',
      label: '描述信息',
      field: row => row.description,
      align: 'left'
    }
  ];
}

export const serverArchetypeHandlePageColumns = [
  {
    name: 'version',
    label: '版本',
    field: 'version',
    align: 'left'
  },
  {
    name: 'update_user',
    label: '更新人员',
    field: 'update_user',
    align: 'left'
  },
  {
    name: 'date',
    label: '数据更新时间',
    field: 'date',
    align: 'left'
  },
  {
    name: 'release_log',
    label: '发布日志',
    field: 'release_log',
    align: 'left'
  }
];

export const serverArchetypeIntergrationBaseColums = [
  {
    name: 'update_time',
    label: '数据更新时间',
    field: 'update_time',
    align: 'left'
  }
];

export const serverArchetypeIntergrationApplicationsColums = [
  {
    name: 'name_en',
    label: '应用英文名',
    field: 'name_en',
    align: 'left'
  },
  {
    name: 'name_zh',
    label: '应用中文名',
    field: 'name_zh',
    align: 'left'
  }
];

export const serverArchetypeIntergrationComponentColums = [
  {
    name: 'name_en',
    label: '组件英文名',
    field: 'name_en',
    align: 'left'
  },
  {
    name: 'name_cn',
    label: '组件中文名',
    field: 'name_cn',
    align: 'left'
  },
  {
    name: 'component_version',
    label: '组件版本',
    field: 'component_version',
    align: 'left'
  },
  {
    name: 'type',
    label: '当前版本类型',
    field: 'type',
    align: 'left'
  }
];

export const serverArchetypeProfileOptimizeColums = [
  {
    name: 'title',
    label: '标题',
    field: 'title',
    align: 'left'
  },
  {
    name: 'name_cn',
    label: '开发人员',
    field: 'name_cn',
    align: 'left'
  },
  {
    name: 'feature_branch',
    label: '开发分支',
    field: 'feature_branch',
    align: 'left'
  },
  {
    name: 'target_version',
    label: '目标版本(优化后版本号)',
    field: 'target_version',
    align: 'left'
  },
  {
    name: 'stage',
    label: '需求阶段',
    field: 'stage',
    align: 'left',
    sortable: true
  },
  {
    name: 'btn',
    label: '操作',
    field: 'btn',
    align: 'left'
  }
];

export function serverArchetypeColums(groups) {
  return [
    {
      name: 'name_en',
      label: '骨架英文名',
      field: row => row.name_en,
      sortable: true
    },
    {
      name: 'name_cn',
      label: '骨架中文名',
      field: row => row.name_cn
    },
    {
      name: 'manager_id',
      label: '管理员',
      field: 'manager_id'
    },
    {
      name: 'group',
      label: '所属小组',
      field: field => getGroupFullName(groups, field.group)
    },
    {
      name: 'recommond_version',
      label: '推荐版本',
      field: row => (row.recommond_version ? row.recommond_version : '-')
    },
    {
      name: 'type',
      label: '骨架类型',
      field: 'type'
    },
    {
      name: 'encoding',
      label: '项目编码格式',
      field: 'encoding'
    },
    {
      name: 'wiki_url',
      label: 'wiki地址',
      field: 'wiki_url'
    },
    {
      name: 'desc',
      label: '骨架描述',
      field: 'desc'
    },
    {
      name: 'sonar_scan_switch',
      label: 'sonar扫描卡点',
      field: row => (row.sonar_scan_switch === '1' ? '开' : '关')
    },
    {
      name: 'isTest',
      label: '是否涉及内测',
      field: row => (row.isTest === '1' ? '是' : '否')
    },
    {
      name: 'btn',
      label: '操作',
      field: 'btn'
    }
  ];
}

export const serverComponentHandlePageColums = [
  {
    name: 'version',
    label: '版本',
    field: 'version',
    align: 'left'
  },
  {
    name: 'jdk_version',
    label: 'jdk版本',
    field: 'jdk_version',
    align: 'left'
  },
  {
    name: 'update_user',
    label: '更新人员',
    field: 'update_user',
    align: 'left'
  },
  {
    name: 'date',
    label: '数据更新时间',
    field: 'date',
    align: 'left'
  },
  {
    name: 'release_log',
    label: '发布日志',
    field: 'release_log',
    align: 'left'
  }
];

export const serverComponentHandleRelColums = [
  {
    name: 'version',
    label: 'tag名',
    field: 'version',
    align: 'left'
  },
  {
    name: 'update_user',
    label: '更新人员',
    field: 'update_user',
    align: 'left'
  },
  {
    name: 'date',
    label: '数据更新时间',
    field: 'date',
    align: 'left'
  },
  {
    name: 'release_log',
    label: '发布日志',
    field: 'release_log',
    align: 'left'
  }
];

export function serverComponentIntergrationColums(name, nameCn, nameZh) {
  return [
    {
      name: 'name',
      label: name,
      field: 'name',
      align: 'left'
    },
    {
      name: 'name_cn',
      label: nameCn,
      field: nameZh,
      align: 'left'
    },
    {
      name: 'component_version',
      label: '使用版本',
      field: 'component_version',
      align: 'left'
    },
    {
      name: 'type',
      label: '当前版本类型',
      field: 'type',
      align: 'left'
    },
    {
      name: 'update_time',
      label: '数据更新时间',
      field: 'update_time',
      align: 'left'
    }
  ];
}

export const serverComponentIssuePageColums = [
  {
    name: 'assignee',
    label: '开发人员',
    field: 'name_cn',
    align: 'left'
  },
  {
    name: 'feature_branch',
    label: '开发分支',
    field: 'feature_branch',
    align: 'left'
  },

  {
    name: 'stage',
    label: '当前阶段',
    field: 'stage',
    align: 'left'
  },
  {
    name: 'due_date',
    label: '计划完成日期',
    field: 'due_date',
    align: 'left'
  },
  {
    name: 'desc',
    label: '需求描述',
    field: 'desc',
    align: 'left'
  },
  {
    name: 'btn',
    label: '操作',
    field: 'btn',
    align: 'left'
  }
];

export const serverComponentIssuePageRecordColums = [
  {
    name: 'assignee',
    label: '开发人员',
    field: 'name_cn',
    align: 'left'
  },
  {
    name: 'branch',
    label: '开发分支',
    field: 'branch',
    align: 'left'
  },
  {
    name: 'version',
    label: '版本',
    field: 'version',
    align: 'left'
  },
  {
    name: 'release_log',
    label: '发布日志',
    field: 'release_log',
    align: 'left'
  },
  {
    name: 'type',
    label: '版本类型',
    field: row => getVersionType(row.type),
    align: 'left'
  },
  {
    name: 'date',
    label: '更新日期',
    field: 'date',
    align: 'left'
  }
];

export function serverComponentProfileAppUsingStatusColums(isFrameWork) {
  return [
    {
      name: 'name_en',
      label: '应用英文名',
      field: row => row.name_en,
      align: 'left',
      sortable: true
    },
    {
      name: 'name_zh',
      label: '应用中文名',
      field: row => row.name_zh,
      align: 'left',
      sortable: true
    },
    {
      name: !isFrameWork ? 'component_version' : 'image_tag',
      label: '使用版本',
      field: !isFrameWork ? 'component_version' : 'image_tag',
      align: 'left',
      sortable: true
    },
    {
      name: !isFrameWork ? 'type' : 'stage',
      label: '当前版本类型',
      field: !isFrameWork ? 'type' : 'stage',
      align: 'left',
      sortable: true
    },
    {
      name: 'group',
      label: '所属小组',
      field: 'group',
      align: 'left',
      sortable: true
    },
    {
      name: 'spdb_managers',
      label: '应用行内负责人',
      field: 'spdb_managers',
      align: 'left'
    },
    {
      name: 'dev_managers',
      label: '应用负责人',
      field: 'dev_managers',
      align: 'left'
    },
    {
      name: 'update_time',
      label: '数据更新时间',
      field: 'update_time',
      align: 'left'
    }
  ];
}

export const updateType = [
  { label: '新增', value: '0' },
  { label: '编辑', value: '1' }
];

export const modifyVersionColumns = [
  {
    name: 'type',
    label: '操作类型',
    field: row => updateType[row.type].label
  },
  {
    name: 'update_user',
    label: '操作人',
    field: 'update_user'
  },
  {
    name: 'date',
    label: '操作时间',
    field: 'date'
  },
  {
    name: 'content',
    label: '详情',
    field: 'content'
  }
];

export const serverComponentProfileArchetypeUsingStatusColums = [
  {
    name: 'name_en',
    label: '骨架英文名',
    field: row => row.name_en,
    align: 'left',
    sortable: true
  },
  {
    name: 'name_zh',
    label: '骨架中文名',
    field: row => row.name_zh,
    align: 'left',
    sortable: true
  },
  {
    name: 'archetype_version',
    label: '骨架版本',
    field: 'archetype_version',
    align: 'left',
    sortable: true
  },
  {
    name: 'component_version',
    label: '组件版本',
    field: 'component_version',
    align: 'left',
    sortable: true
  },
  {
    name: 'type',
    label: '骨架版本类型',
    field: 'type',
    align: 'left',
    sortable: true
  },
  {
    name: 'component_type',
    label: '组件版本类型',
    field: 'component_type',
    align: 'left',
    sortable: true
  },
  {
    name: 'update_time',
    label: '数据更新时间',
    field: 'update_time',
    align: 'left'
  }
];

export const serverComponentProfileOptimizeListColums = [
  {
    name: 'title',
    label: '标题',
    field: 'title',
    align: 'left'
  },
  {
    name: 'name_cn',
    label: '开发人员',
    field: 'name_cn',
    align: 'left'
  },
  {
    name: 'feature_branch',
    label: '开发分支',
    field: 'feature_branch',
    align: 'left'
  },
  {
    name: 'target_version',
    label: '目标版本(优化后版本号)',
    field: 'target_version',
    align: 'left'
  },
  {
    name: 'stage',
    label: '需求阶段',
    field: 'stage',
    align: 'left',
    sortable: true
  },
  {
    name: 'btn',
    label: '操作',
    field: 'btn',
    align: 'left'
  }
];

export const serverComponentProfileOptimizeRelColums = [
  {
    name: 'title',
    label: '标题',
    field: 'title',
    align: 'left'
  },
  {
    name: 'manager',
    label: '版本管理员',
    field: 'manager',
    align: 'left'
  },
  {
    name: 'issue_type',
    label: '当前优化需求类型',
    field: 'issue_type',
    align: 'left'
  },
  {
    name: 'feature_branch',
    label: 'release分支',
    field: 'feature_branch',
    align: 'left'
  },
  {
    name: 'predict_version',
    label: '预设版本号',
    field: 'predict_version',
    align: 'left'
  },
  {
    name: 'due_date',
    label: '计划完成日期',
    field: 'due_date',
    align: 'left',
    sortable: true
  },
  {
    name: 'desc',
    label: '需求描述',
    field: 'desc',
    align: 'left'
  },
  {
    name: 'btn',
    label: '操作',
    field: 'btn',
    align: 'left'
  }
];

export function serverComponentColums(groups) {
  return [
    {
      name: 'name_en',
      label: '组件英文名',
      field: row => row.name_en,
      sortable: true
    },
    {
      name: 'name_cn',
      label: '组件中文名',
      field: row => row.name_cn,
      sortable: true
    },
    {
      name: 'manager_id',
      label: '负责人',
      field: 'manager_id'
    },
    {
      name: 'group',
      label: '所属小组',
      field: field => getGroupFullName(groups, field.group)
    },
    {
      name: 'recommond_version',
      label: '推荐版本',
      field: row => (row.recommond_version ? row.recommond_version : '-')
    },
    {
      name: 'source',
      label: '组件来源',
      field: row => (row.source ? sourceOptions[row.source].label : row.source)
    },
    {
      name: 'type',
      label: '组件类型',
      field: row => (row.type ? typeOptions[row.type].label : row.type)
    },
    {
      name: 'groupId',
      label: 'maven坐标groupID',
      field: 'groupId'
    },
    {
      name: 'gitlab_url',
      label: 'gitlab_url',
      field: 'gitlab_url'
    },
    {
      name: 'jdk_version',
      label: 'jdk版本',
      field: 'jdk_version'
    },
    {
      name: 'wiki_url',
      label: 'wiki地址',
      field: 'wiki_url'
    },
    {
      name: 'root_dir',
      label: '组件代码根路径',
      field: 'root_dir'
    },
    {
      name: 'desc',
      label: '描述',
      field: 'desc'
    },
    {
      name: 'sonar_scan_switch',
      label: 'sonar扫描卡点',
      field: row => (row.sonar_scan_switch === '1' ? '开' : '关')
    },
    {
      name: 'isTest',
      label: '是否涉及内测',
      field: row => (row.isTest === '1' ? '是' : '否')
    },
    {
      name: 'btn',
      label: '操作',
      field: 'btn'
    }
  ];
}

export const webArchetypeHandlePageColums = [
  {
    name: 'tag',
    label: 'tag名称',
    field: 'tag',
    align: 'left'
  },
  {
    name: 'update_user',
    label: '更新人员',
    field: 'update_user',
    align: 'left'
  },
  {
    name: 'date',
    label: '数据更新时间',
    field: 'date',
    align: 'left'
  },
  {
    name: 'release_log',
    label: '发布日志',
    field: 'release_log',
    align: 'left'
  }
];

export const webArchetypeProfileOptimizeColums = [
  {
    name: 'title',
    label: '标题',
    field: 'title',
    align: 'left'
  },
  {
    name: 'name_cn',
    label: '开发人员',
    field: 'name_cn',
    align: 'left'
  },
  {
    name: 'feature_branch',
    label: '开发分支',
    field: 'feature_branch',
    align: 'left'
  },
  {
    name: 'due_date',
    label: '计划完成日期',
    field: 'due_date',
    align: 'left',
    sortable: true
  },
  {
    name: 'desc',
    label: '需求描述',
    field: 'desc',
    align: 'left'
  },
  {
    name: 'btn',
    label: '操作',
    field: 'btn',
    align: 'left'
  }
];

export function webArchetypeColums(groups) {
  return [
    {
      name: 'name_en',
      label: '骨架英文名',
      field: row => row.name_en,
      sortable: true
    },
    {
      name: 'name_cn',
      label: '骨架中文名',
      field: row => row.name_cn
    },
    {
      name: 'manager',
      label: '管理员',
      field: 'manager'
    },
    {
      name: 'group',
      label: '所属小组',
      field: field => getGroupFullName(groups, field.group)
    },
    {
      name: 'recommond_version',
      label: '推荐版本',
      field: row => (row.recommond_version ? row.recommond_version : '-')
    },
    {
      name: 'type',
      label: '骨架类型',
      field: 'type'
    },
    {
      name: 'encoding',
      label: '项目编码格式',
      field: 'encoding'
    },
    {
      name: 'wiki_url',
      label: 'wiki地址',
      field: 'wiki_url'
    },
    {
      name: 'desc',
      label: '骨架描述',
      field: 'desc'
    },
    {
      name: 'isTest',
      label: '是否涉及内测',
      field: row => (row.isTest === '1' ? '是' : '否')
    },
    {
      name: 'btn',
      label: '操作',
      field: 'btn'
    }
  ];
}

export const webComponentHandlePageColums = [
  {
    name: 'version',
    label: '版本',
    field: 'version',
    align: 'left'
  },
  {
    name: 'update_user',
    label: '更新人员',
    field: 'update_user',
    align: 'left'
  },
  {
    name: 'date',
    label: '数据更新时间',
    field: 'date',
    align: 'left'
  },
  {
    name: 'release_log',
    label: '发布日志',
    field: 'release_log',
    align: 'left'
  }
];

export function webComponentIntergrationColums(name, nameCn, nameZh) {
  return [
    {
      name: 'name',
      label: name,
      field: 'name',
      align: 'left'
    },
    {
      name: 'name_cn',
      label: nameCn,
      field: nameZh,
      align: 'left'
    },
    {
      name: 'component_version',
      label: '使用版本',
      field: 'component_version',
      align: 'left'
    },
    {
      name: 'type',
      label: '当前版本类型',
      field: 'type',
      align: 'left'
    },
    {
      name: 'update_time',
      label: '数据更新时间',
      field: 'update_time',
      align: 'left'
    }
  ];
}

export const webComponentIssuePageColums = [
  {
    name: 'assignee',
    label: '开发人员',
    field: 'name_cn',
    align: 'left'
  },
  {
    name: 'feature_branch',
    label: '开发分支',
    field: 'feature_branch',
    align: 'left'
  },

  {
    name: 'stage',
    label: '当前阶段',
    field: 'stage',
    align: 'left'
  },
  {
    name: 'due_date',
    label: '计划完成日期',
    field: 'due_date',
    align: 'left'
  },
  {
    name: 'desc',
    label: '需求描述',
    field: 'desc',
    align: 'left'
  },
  {
    name: 'btn',
    label: '操作',
    field: 'btn',
    align: 'left'
  }
];

export const webComponentIssuePageRecordColums = [
  {
    name: 'assignee',
    label: '开发人员',
    field: 'name_cn',
    align: 'left'
  },
  {
    name: 'branch',
    label: '开发分支',
    field: 'branch',
    align: 'left'
  },
  {
    name: 'version',
    label: '版本',
    field: 'version',
    align: 'left'
  },
  {
    name: 'release_log',
    label: '发布日志',
    field: 'release_log',
    align: 'left'
  },
  {
    name: 'type',
    label: '版本类型',
    field: row => getVersionType(row.type),
    align: 'left'
  },
  {
    name: 'date',
    label: '更新日期',
    field: 'date',
    align: 'left'
  }
];

export const webComponentProfileAppUsingStatusColums = [
  {
    name: 'name_en',
    label: '应用英文名',
    field: 'name_en',
    align: 'left',
    sortable: true
  },
  {
    name: 'name_zh',
    label: '应用中文名',
    field: 'name_zh',
    align: 'left',
    sortable: true
  },
  {
    name: 'component_version',
    label: '使用版本',
    field: 'component_version',
    align: 'left',
    sortable: true
  },
  {
    name: 'type',
    label: '当前版本类型',
    field: 'type',
    align: 'left',
    sortable: true
  },
  {
    name: 'group',
    label: '所属小组',
    field: row => row.group.name,
    align: 'left',
    sortable: true
  },
  {
    name: 'spdb_managers',
    label: '应用行内负责人',
    field: 'spdb_managers',
    align: 'left'
  },
  {
    name: 'dev_managers',
    label: '应用负责人',
    field: 'dev_managers',
    align: 'left'
  },
  {
    name: 'update_time',
    label: '数据更新时间',
    field: 'update_time',
    align: 'left'
  }
];

export const webComponentProfileOptimizeRelColums = [
  {
    name: 'title',
    label: '标题',
    field: 'title',
    align: 'left'
  },
  {
    name: 'manager',
    label: '版本管理员',
    field: 'manager',
    align: 'left'
  },
  {
    name: 'issue_type',
    label: '当前优化需求类型',
    field: 'issue_type',
    align: 'left'
  },
  {
    name: 'feature_branch',
    label: 'release分支',
    field: 'feature_branch',
    align: 'left'
  },
  {
    name: 'predict_version',
    label: '预设版本号',
    field: 'predict_version',
    align: 'left'
  },
  {
    name: 'due_date',
    label: '计划完成日期',
    field: 'due_date',
    align: 'left',
    sortable: true
  },
  {
    name: 'desc',
    label: '需求描述',
    field: 'desc',
    align: 'left'
  },
  {
    name: 'btn',
    label: '操作',
    field: 'btn',
    align: 'left'
  }
];
export function webComponentColums(groups) {
  return [
    {
      name: 'name_en',
      label: '组件英文名',
      field: 'name_en'
    },
    {
      name: 'name_cn',
      label: '组件中文名',
      field: 'name_cn'
    },
    {
      name: 'manager',
      label: '组件管理员',
      field: 'manager'
    },
    {
      name: 'recommond_version',
      label: '组件推荐版本',
      field: 'recommond_version'
    },
    {
      name: 'source',
      label: '组件来源',
      field: row => (row.source == '0' ? '组内维护(自研)' : '第三方(开源)')
    },
    {
      name: 'npm_name',
      label: 'npm坐标name',
      field: 'npm_name'
    },
    {
      name: 'npm_group',
      label: 'npm坐标group',
      field: 'npm_group'
    },
    {
      name: 'type',
      label: '组件类型',
      field: row => webTypeFilter[row.type]
    },
    {
      name: 'group',
      label: '所属小组',
      field: field => getGroupFullName(groups, field.group)
    },
    {
      name: 'gitlab_url',
      label: 'gitlab地址',
      field: 'gitlab_url'
    },
    {
      name: 'skillstack',
      label: '技术栈',
      field: row => (row.skillstack ? row.skillstack : '-')
    },
    {
      name: 'businessarea',
      label: '业务领域',
      field: row => (row.businessarea ? row.businessarea : '-')
    },
    {
      name: 'root_dir',
      label: '项目根路径',
      field: 'root_dir'
    },
    {
      name: 'desc',
      label: '描述',
      field: 'desc'
    },
    {
      name: 'isTest',
      label: '是否涉及内测',
      field: row => (row.isTest === '1' ? '是' : '否')
    },
    {
      name: 'btn',
      label: '操作',
      field: 'btn'
    }
  ];
}
