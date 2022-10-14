export function createJobModel() {
  let year = new Date().getFullYear();
  let month = new Date().getMonth() + 1;
  let day = new Date().getDate();
  let defalutDate = `${year}-${month > 9 ? month : '0' + month}-${
    day > 9 ? day : '0' + day
  }`;
  return {
    name: '',
    partyA: [],
    partyB: [],
    partyC: [],
    winName: '',
    changeNum: '',
    createUser: '',
    devStartDate: '',
    sitStartDate: defalutDate,
    uatStartDate: '',
    relStartDate: ''
  };
}

export function createReleaseNodeModel() {
  let year = new Date().getFullYear();
  let month = new Date().getMonth() + 1;
  let day = new Date().getDate();
  let defalutDate = `${year}-${month > 9 ? month : '0' + month}-${
    day > 9 ? day : '0' + day
  }`;
  return {
    releaseNodeName: '',
    releaseDate: defalutDate,
    release_date: defalutDate,
    owner_groupId: null,
    releaseManager: '',
    releaseSpdbManager: '',
    // releaseSpdbNo:'',
    gray_temp_id: [],
    proc_temp_id: [],
    uatEnvName: '',
    relEnvName: '',
    partyA: [],
    partyB: [],
    partyC: [],
    winName: '',
    changeNum: '',
    createUser: '',
    devStartDate: '',
    uatStartDate: '',
    relStartDate: '',
    release_node_name: '',
    release_manager: '',
    release_spdb_manager: '',
    uat_env_name: '',
    type: '1'
  };
}

export function formatRelease(release) {
  return release.map(releases => {
    return {
      releaseDate: releases.release_date,
      groupName: releases.owner_group_name,
      releaseNode: releases.release_node_name,
      create_user_name_cn: releases.create_user_name_cn,
      status: releases.node_status,
      createUser: releases.release_spdb_manager,
      releaseManager: releases.release_manager,
      releaseNodeName: releases.release_node_name,
      ownerGroupId: releases.owner_groupId,
      releaseSpdbManager: releases.release_spdb_manager,
      releaseSpdbNo: releases.release_spdb_no,
      uatEnvName: releases.uat_env_name,
      relEnvName: releases.rel_env_name,
      create_user: releases.create_user,
      node_status: releases.node_status,
      owner_groupId: releases.owner_groupId,
      owner_group_name: releases.owner_group_name,
      rel_env_name: releases.rel_env_name,
      release_date: releases.release_date,
      release_manager: releases.release_manager,
      release_node_name: releases.release_node_name,
      release_spdb_manager: releases.release_spdb_manager,
      release_spdb_no: releases.release_spdb_no,
      uat_env_name: releases.uat_env_name,
      _id: release._id
    };
  });
}

export function formatReleaseDetail(releaseNode) {
  return {
    ...releaseNode,
    releaseManager: releaseNode.release_manager,
    releaseNodeName: releaseNode.release_node_name,
    releaseDate: releaseNode.release_date,
    ownerGroupId: releaseNode.owner_groupId,
    releaseSpdbManager: releaseNode.release_spdb_manager,
    releaseSpdbNo: releaseNode.release_spdb_no,
    uatEnvName: releaseNode.uat_env_name,
    relEnvName: releaseNode.rel_env_name
  };
}

export function applyParams(release) {
  return [
    {
      app_name_en: '',
      app_name_zh: '',
      app_spdb_managers: '',
      app_dev_managers: '',
      release_branch: '',
      product_tag: '',
      devops_status: ''
    }
  ];
}

export function jobListParams(release) {
  return [
    {
      task_id: '',
      task_name: '',
      task_group: '',
      bank_master: '',
      release_branch: '',
      task_project: '',
      task_stage: ''
    }
  ];
}

export function releaseNoteAppModel() {
  return {
    application_name_en: '',
    application_name_cn: '',
    tag_name: '',
    application_type: '',
    application_id: '',
    catalog_type: '1',
    dev_managers_info: [],
    expand_info: {
      SHK1: '',
      SHK2: '',
      HFK1: '',
      HFK2: ''
    }
  };
}

export function releaseNoteNoTaskSqlModel() {
  return {
    fileName: '',
    fileContent: ''
  };
}

export function releaseNoteConfigModel() {
  return {
    module_name: 'commonconfig',
    module_ip: '',
    fileName: '',
    file_url: '',
    file_principal: '',
    principal_phone: '',
    diff_content: [],
    file_type: '1',
    safeguard_explain: '',
    diff_flag: '0'
  };
}

export function extPublishDialogModel() {
  return {
    type: '',
    executorId: '',
    transName: '',
    jobGroup: '',
    description: '',
    cronExpression: '',
    misfireInstr: '',
    fireTime: ''
  };
}

export function releaseNodeDetail() {
  return {
    create_user: '',
    node_status: '',
    ownerGroupId: '',
    owner_groupId: '',
    owner_group_name: '',
    relEnvName: '',
    rel_env_name: '',
    releaseDate: '',
    releaseManager: '',
    releaseNodeName: '',
    releaseSpdbManager: '',
    releaseSpdbNo: '',
    release_date: '',
    release_manager: '',
    release_node_name: '',
    release_spdb_manager: '',
    release_spdb_no: '',
    uatEnvName: '',
    uat_env_name: '',
    _id: ''
  };
}

export const filtersKey = {
  other_system: '其他系统变更',
  script_alter: '脚本变更',
  data_base_alter: '数据库变更',
  fire_wall_open: '防火墙变更',
  static_resource: '静态资源变更',
  interface_alter: '接口变更',
  ebank_common_alter: '配置文件变更'
};

export function createTemplate() {
  return {
    owner_group: '',
    owner_system: null,
    template_type: '',
    owner_app: null,
    system_abbr: '',
    group_abbr: '',
    catalogs: [],
    resource_giturl: []
  };
}

// 新建变更
export function newChangeModel() {
  return {
    release_node_name: '', // 投产窗口名称
    date: '', // 投产日期
    prod_spdb_no: '', // 变更单号
    version: '', // 变更版本
    type: 'gray', // 变更类型
    excel_template_url: null, // 变更模板对象
    applications: [], // 变更应用id列表
    plan_time: '18:00', // 预期发布时间
    image_deliver_type: '1',
    system: null,
    vContainer: '',
    vGroup: '',
    vDate: '',
    Vtime: '',
    vEnd: '',
    owner_system_name: {},
    excel_template_name: {}
  };
}

// 新建发布说明
export function newReleaseNoteModel() {
  return {
    release_node_name: '', // 投产窗口名称
    release_note_name: '', // 发布说明名称
    date: '', // 投产日期
    version: '', // 变更版本
    type: 'gray', // 变更类型
    plan_time: '18:00', // 预期发布时间
    image_deliver_type: '1',
    leaseholder: '',
    system: null,
    vContainer: '',
    vGroup: '',
    vDate: '',
    Vtime: '',
    vEnd: '',
    note_batch: '',
    namespace: '2',
    owner_system_name: {}
  };
}

export const ImageDeliverType = {
  auto: '1', //自动化发布
  deAuto: '0' //非自动化发布
};

export const type = {
  '1': '应用包更新',
  '2': '微服务应用更新',
  '3': '数据库更新',
  '4': '配置文件更新',
  '5': '公共配置文件更新',
  '6': '老NAS脚本文件更新',
  '7': '对象存储配置文件更新',
  '8': 'esf注册类更新',
  '9': '动态配置文件更新',
  '10': '堡垒机应用配置文件更新'
};

export const status = {
  '0': '介质未准备',
  '1': '已发起介质准备',
  '2': '变更已取消',
  '3': '介质准备完毕',
  '4': '介质准备失败'
};

export const product_type = {
  proc: '生产',
  gray: '灰度'
};

export function testEnv() {
  return {
    uat_env_name: '',
    rel_env_name: ''
  };
}

export function updateChanges() {
  return {
    plan_time: '',
    prod_spdb_no: '',
    template: null,
    image_deliver_type: '0'
  };
}

export const catalogType = {
  '3': '数据库更新',
  '4': '配置文件更新',
  '5': '公共配置文件更新',
  '6': '老NAS脚本文件更新',
  '7': '对象存储配置文件更新',
  '8': 'esf注册类更新',
  '9': '动态配置文件更新',
  '10': '堡垒机应用配置文件更新'
};

export const catalogTypeOptions = [
  {
    label: '数据库更新',
    value: '3'
  },
  {
    label: '配置文件更新',
    value: '4'
  },
  {
    label: '公共配置文件更新',
    value: '5'
  },
  {
    label: '老NAS脚本文件更新',
    value: '6'
  },
  {
    label: '对象存储配置文件更新',
    value: '7'
  },
  {
    label: 'esf注册类更新',
    value: '8'
  },
  {
    label: '动态配置文件更新',
    value: '9'
  },
  {
    label: '堡垒机应用配置文件更新',
    value: '10'
  }
];

export function moduleTypeModel() {
  return {
    catalog_name: '',
    catalog_type: '',
    description: ''
  };
}
export function scriptParamsModel() {
  return {
    key: '',
    value: '',
    description: ''
  };
}
export function automationEnvModel() {
  return {
    env_name: '',
    description: ''
  };
}

export const typeNoteOptions = [
  { label: '滚动发布微服务列表', value: 'docker' },
  { label: '应用组重启', value: 'docker_restart' },
  { label: '弹性扩展', value: 'docker_scale' },
  { label: '新增微服务', value: 'docker_yaml' },
  { label: '应用停止', value: 'stop_all' }
];

export const typeOptions = [
  { label: 'K1、K2镜像异步更新', value: '1' },
  { label: '单集群停止', value: '2' },
  { label: '停止应用后重启', value: '3' },
  { label: '应用重启', value: '4' }
];
export const typeOptionsObj = {
  '1': 'K1、K2镜像异步更新',
  '2': '单集群停止',
  '3': '停止应用后重启',
  '4': '应用重启'
};

export function reviewModel() {
  return {
    key: '',
    group: '',
    applicant: '',
    reviewStatus: ''
  };
}

export const taskStatus = [
  '待审核',
  '审核通过',
  '审核拒绝',
  '取消投产待审核',
  '已取消投产',
  '更换投产点待审核',
  '已投产'
];

export function releaseModel() {
  return {
    start_date: '',
    end_date: '',
    owner_groupId: ''
  };
}

export function releaseDialogModel() {
  return {
    release_date: '',
    owner_groupId: '',
    release_contact: []
  };
}

export const specialOptions = [
  { label: '全部', value: '' },
  { label: '涉及数据库', value: 'dataBaseAlter' },
  { label: '公共配置文件', value: 'commonProfile' },
  { label: '新增应用', value: 'new_add_sign' },
  { label: '特殊项', value: 'specialCase' }
];

export function changeBatchDialogModel() {
  return {
    application_id: '',
    batch_id: '',
    applications: [],
    modify_reason: '',
    release_node_name: ''
  };
}

export const batchOptions = [
  { label: '第一批次(20点左右开始)', value: '1' },
  { label: '第二批次(22点左右)', value: '2' },
  { label: '第三批次(0点以后)', value: '3' }
];

export function batchArr() {
  return [
    {
      type: '第一批次',
      batch: '1',
      children: []
    },
    {
      type: '第二批次',
      batch: '2',
      children: []
    },
    {
      type: '第三批次',
      batch: '3',
      children: []
    },
    {
      type: '所有批次',
      batch: '4',
      children: []
    }
  ];
}

export const tagColor = {
  日常流程: 'indigo-1',
  处对处: 'yellow-5',
  总对总: 'red-12',
  不予投产: 'teal-4',
  条线审批: 'yellow-5',
  老总审批: 'red-12'
};

export const fileTypeOptions = [
  { label: '特殊流程需求列表', value: '1' },
  { label: '需求列表', value: '2' }
];

export const updateFileConfigQueryColumn = [
  {
    name: 'name_cn',
    label: '应用中文名',
    field: 'name_zh',
    copy: true
  },
  {
    name: 'name_en',
    label: '应用英文名',
    field: 'name_en',
    copy: true
  },
  {
    name: 'dev_managers',
    label: '应用负责人',
    copy: true,
    field: row => row.dev_managers.map(item => item.user_name_cn).join('，')
  },
  {
    name: 'spdb_manager',
    label: '行内项目负责人',
    copy: true,
    field: row => row.spdb_managers.map(item => item.user_name_cn).join('，')
  },
  {
    name: 'group',
    label: '所属小组',
    copy: true,
    field: row => row.group.name
  },
  { name: 'gitlab', label: 'gitlab仓库', field: 'git' },
  {
    name: 'branch',
    label: '分支名',
    field: 'branch'
  }
];

export const updateFileConfigAddColumn = [
  {
    name: 'application_name',
    label: '应用名',
    field: row =>
      row.isrisk === '1'
        ? row.application_name + '（含风险）'
        : row.application_name,

    sortable: true
  },
  {
    name: 'dev_managers',
    label: '应用负责人',
    field: row => row.dev_managers.join('，'),
    sortable: true
  },
  {
    name: 'config_type',
    label: '实体属性',
    field: 'config_type',
    sortable: true
  },
  {
    name: 'release_node',
    label: '已挂载投产窗口',
    field: 'release_node',
    sortable: true
  },
  {
    name: 'asset_name',
    label: '介质已准备完毕',
    field: 'asset_name',
    sortable: true
  },
  {
    name: 'status',
    label: '是否已生成配置文件',
    field: row => (row.status === '0' ? '否' : '是'),
    sortable: true
  },
  {
    name: 'operate',
    label: '操作'
  }
];

export const updateFileConfigQueryVisibleColumn = [
  'name_cn',
  'name_en',
  'dev_managers',
  'spdb_manager',
  'group',
  'gitlab',
  'branch'
];

export const updateFileConfigAddVisibleColumn = [
  'application_name',
  'dev_managers',
  'config_type',
  'release_node',
  'asset_name',
  'status',
  'operate'
];

export function createDependencyModel() {
  return {
    map: '',
    model: '',
    field: null
  };
}
export function createExamineModel() {
  return {
    group: '', // 所属组
    applicantName: '', // 申请人
    dbType: '', // 数据库类型
    reviewers: '', // 指派初审人
    reason: '', // 申请描述
    plan_fire_time: '', // 投产日期
    taskName: '', // 任务名
    docInfo: [] // 上传的文件
  };
}
export const Prompt = {
  addedSuccessfully: '已发起审核',
  modifiedSuccessfully: '已重新发起审核'
};
export const AuditStatus = {
  pass: '通过',
  firstReviewReject: '初审拒绝',
  seconedReviewReject: '复审拒绝',
  archived: '已归档'
};
export const TaskStatus = {
  deStock: '0', // ‘0’表示从数据库变更审核页面添加的数据
  stock: '1' // ‘1’表示存量数据，即从任务处理页面添加的数据
};
export const FileType = {
  '1': '需求类',
  '2': '设计类',
  '3': '开发类',
  '4': '测试类',
  '5': '投产类-变更材料',
  '6': '审核类-数据库审核材料',
  '7': '投产类-测试报告类目'
};
