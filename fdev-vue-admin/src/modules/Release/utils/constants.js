import { taskStage } from '@/utils/utils';
import { taskStatus, catalogType } from './model';
function chineseName(val) {
  return val
    .map(item => {
      return item.user_name_cn;
    })
    .join(',');
}

function statusFilter(name) {
  return taskStatus[name];
}

// 任务列表
export const taskListColumn = [
  {
    name: 'name',
    label: '任务名称',
    field: row => row.name
  },
  {
    name: 'bank_master',
    label: '行内项目负责人',
    field: row => chineseName(row.bank_master)
  },
  {
    name: 'dev_managers',
    label: '任务负责人',
    field: row => chineseName(row.dev_managers)
  }
];

// 发布说明应用列表
export const releaseNoteAppListColumn = [
  {
    name: 'application_name_en',
    label: '应用英文名称',
    field: 'application_name_en'
  },
  {
    name: 'application_name_cn',
    label: '应用中文名称',
    field: 'application_name_cn'
  },
  {
    name: 'tag_name',
    label: 'TAG',
    field: 'tag_name'
  },
  {
    name: 'application_type',
    label: '类型',
    field: 'application_type'
  },
  {
    name: 'dev_managers_info',
    label: '负责人',
    field: 'dev_managers_info'
  },
  { name: 'btn', label: '操作' }
];

// 应用列表
export const appListColumn = [
  {
    name: 'app_name_zh',
    label: '应用名称',
    field: 'app_name_zh'
  },
  {
    name: 'app_name_en',
    label: '应用名称',
    field: 'app_name_en'
  },
  {
    name: 'app_spdb_managers',
    label: '行内项目负责人',
    field: row =>
      row.app_spdb_managers.map(user => user.user_name_cn).join('，') || '-'
  },
  {
    name: 'app_dev_managers',
    label: '应用负责人',
    field: row =>
      row.app_dev_managers.map(user => user.user_name_cn).join('，') || '-'
  },
  {
    name: 'release_branch',
    label: 'release分支',
    field: 'release_branch'
  },
  {
    name: 'product_tag',
    label: 'TAG列表',
    field: row => row.product_tag.join('，') || '-'
  },
  {
    name: 'pro_image_uri',
    label: '镜像列表',
    field: row => row.pro_image_uri.join('，') || '-'
  },
  {
    name: 'uat_env_name',
    label: 'UAT环境',
    field: 'uat_env_name'
  },
  {
    name: 'rel_env_name',
    label: 'REL环境',
    field: 'rel_env_name'
  }
];

// 发布说明列表
export const autoReleaseNoteListColumns = [
  {
    name: 'release_note_name',
    label: '发布说明',
    field: 'release_note_name'
  },
  {
    name: 'image_deliver_type',
    label: '发布类型',
    field: 'image_deliver_type'
  },
  {
    name: 'launcher_name_cn',
    label: '责任人',
    field: 'launcher_name_cn'
  },
  {
    name: 'lock_flag',
    label: '状态',
    field: 'lock_flag'
  },
  {
    name: 'lock_name_cn',
    label: '锁定人',
    field: 'lock_name_cn'
  },
  { name: 'btn', label: '操作' }
];

// 任务列表
export const taskColumns = [
  {
    name: 'name',
    label: '任务名',
    field: 'name'
  },
  {
    name: 'feature_branch',
    label: '分支名',
    field: 'feature_branch'
  }
];

// 运行任务列表
export const runTaskColumns = [
  {
    name: 'task_name',
    label: '任务名',
    field: 'task_name'
  },
  {
    name: 'task_branch',
    label: '分支名',
    field: 'task_branch'
  }
];

// 变更计划列表
export const changePlanListColumn = [
  { name: 'expand', label: '', field: 'expand' },
  {
    name: 'prod_assets_version',
    label: '变更说明',
    field: 'prod_assets_version',
    sortable: true
  },
  {
    name: 'date',
    label: '变更日期',
    field: 'date',
    sortable: true
  },
  {
    name: 'prod_spdb_no',
    label: '变更单号',
    field: 'prod_spdb_no',
    sortable: true
  },
  {
    name: 'type',
    label: '变更类型',
    field: 'type',
    sortable: true
  },
  { name: 'btn', label: '操作' }
];

// 批次下拉选项
export const batchOptions = [
  {
    label: '一批次',
    value: '1'
  },
  {
    label: '二批次',
    value: '2'
  },
  {
    label: '三批次',
    value: '3'
  }
];

export const namespaceOptions = [
  {
    label: '业务',
    value: '1'
  },
  {
    label: '网银',
    value: '2'
  }
];

// 租户下拉选项
export const leaseholderOptions = {
  gray2: [
    // 灰度网银
    {
      label: 'appgray-tenant',
      value: 'appgray-tenant'
    },
    {
      label: 'ebankgraysh-tenant/ebankgrayhf-tenant',
      value: 'ebankgraysh-tenant/ebankgrayhf-tenant'
    }
  ],
  proc1: [
    // 生产业务
    {
      label: 'sh-ebank-tenant/hf-ebank-tenant',
      value: 'sh-ebank-tenant/hf-ebank-tenant'
    },
    {
      label: 'sh-pubebank-tenant/hf-pubebank-tenant',
      value: 'sh-pubebank-tenant/hf-pubebank-tenant'
    },
    {
      label: 'ebankbat-tenant',
      value: 'ebankbat-tenant'
    }
  ],
  proc2: [
    // 生产网银
    {
      label: 'ebanksh-tenant/ebankhf-tenant',
      value: 'ebanksh-tenant/ebankhf-tenant'
    },
    {
      label: 'appper-tenant',
      value: 'appper-tenant'
    },
    {
      label: 'appcom-tenant',
      value: 'appcom-tenant'
    }
  ],
  // 批量
  batch: [
    {
      label: 'ebankbat-tenant',
      value: 'ebankbat-tenant'
    }
  ],
  // 柜面
  otc: [
    {
      label: 'sh-ebank-tenant/hf-ebank-tenant',
      value: 'sh-ebank-tenant/hf-ebank-tenant'
    }
  ]
};

// 合并状态下拉选项
export const mergeReleaseOptions = [
  {
    label: '全部',
    value: ''
  },
  {
    label: '已合并',
    value: 'true'
  },
  {
    label: '未合并',
    value: 'false'
  }
];

// 模块类型下拉框
export const moduleTypeOptions = [
  'commonconfig',
  'cfg_nas',
  'cfg_core',
  'app_nas',
  'cfgbef_mbank'
];

// 补跑策略下拉框
export const missOptions = [
  {
    label: '立即补跑',
    value: '1'
  },
  {
    label: '不补跑',
    value: '2'
  }
];

// 任务类型下拉选项
export const typeOptions = [
  {
    label: '新增任务',
    value: 'httpAddJob'
  },
  {
    label: '新增一次性任务',
    value: 'httpTriggerJob'
  },
  {
    label: '删除任务',
    value: 'httpDeleteJob'
  },
  {
    label: '更新任务信息',
    value: 'httpUpdateJob'
  }
];

// 组下拉选项
export const groupOptions = [
  {
    label: '公共组',
    value: 'common'
  },
  {
    label: '支付组',
    value: 'pay'
  },
  {
    label: '零售金融组',
    value: 'per'
  },
  {
    label: '公司组',
    value: 'ent'
  }
];

// 配置文件列定义
export const configListColumns = [
  { name: 'opt' },
  {
    name: 'module_name',
    label: '模块',
    field: 'module_name'
  },
  {
    name: 'ip',
    label: 'IP',
    field: 'ip'
  }
];

// 子配置文件列定义
export const subConfigListColumns = [
  { name: 'opt' },
  {
    name: 'fileName',
    label: '文件名',
    field: 'fileName'
  },
  {
    name: 'file_principal',
    label: '文件负责人',
    field: 'file_principal'
  },
  { name: 'btn', label: '操作' }
];

// 新建变更列定义
export const newChangeListColumn = [
  { name: 'expand', label: '', field: 'expand' },
  {
    name: 'prod_assets_version',
    label: '变更说明',
    field: 'prod_assets_version',
    sortable: true
  },
  {
    name: 'date',
    label: '变更日期',
    field: 'date',
    sortable: true
  },
  {
    name: 'prod_spdb_no',
    label: '变更单号',
    field: 'prod_spdb_no',
    sortable: true
  },
  {
    name: 'type',
    label: '变更类型',
    field: 'type',
    sortable: true
  }
];

// 发布说明数据库列定义
export const noteDatabaseColumns = [
  {
    name: 'fileName',
    label: '上传文件名',
    field: row => row.fileName
  },
  {
    name: 'file_principal',
    label: '文件负责人',
    field: row => row.file_principal
  },
  {
    name: 'principal_phone',
    label: '负责人联系方式',
    field: row => row.principal_phone
  },
  { name: 'btn', label: '操作' }
];

// 批量任务列定义
export const extPublishColumns = [
  {
    name: 'type',
    label: '任务名称',
    field: 'type'
  },
  {
    name: 'jobGroup',
    label: '组别',
    field: 'jobGroup'
  },
  {
    name: 'transName',
    label: '交易名',
    field: 'transName',
    copy: true
  },
  {
    name: 'create_time',
    label: '创建时间',
    field: 'create_time'
  },
  {
    name: 'user_name_cn',
    label: '创建人',
    field: 'user_name_cn'
  },
  { name: 'btn', label: '操作' }
];

// 批量任务数据库无操作列列定义
export const extPublishNoOptColumns = [
  {
    name: 'type',
    label: '任务名称',
    field: 'type'
  },
  {
    name: 'jobGroup',
    label: '组别',
    field: 'jobGroup'
  },
  {
    name: 'transName',
    label: '交易名',
    field: 'transName',
    copy: true
  },
  {
    name: 'create_time',
    label: '创建时间',
    field: 'create_time'
  },
  {
    name: 'user_name_cn',
    label: '创建人',
    field: 'user_name_cn'
  }
];

// 批量任务数据库列定义
export const noteDatabaseSeqNoColumns = [
  {
    name: 'seq_no',
    label: '序号',
    field: row => row.seq_no
  },
  {
    name: 'fileName',
    label: '上传文件名',
    field: row => row.fileName
  },
  {
    name: 'file_principal',
    label: '文件负责人',
    field: row => row.file_principal
  },
  {
    name: 'principal_phone',
    label: '负责人联系方式',
    field: row => row.principal_phone
  },
  { name: 'btn', label: '操作' }
];

// 有序执行文件列表
export const databaseUpdateColumns = [
  {
    name: 'filename',
    label: '文件名',
    field: row => row.fileName
  },
  {
    name: 'upload_user',
    label: '上传人',
    field: row => row.upload_user
  },
  {
    name: 'upload_time',
    label: '上传时间',
    field: row => row.upload_time
  },
  {
    name: 'source_application',
    label: '所属应用',
    field: row => row.source_application_name
  },
  {
    name: 'source',
    label: '文件来源',
    field: row => row.source
  },
  { name: 'btn', label: '操作' }
];

// 配置文件列定义
export const fileConfigColumns = [
  {
    name: 'filename',
    label: '文件名',
    field: row => row.fileName
  },
  {
    name: 'runtime_env',
    label: '运行环境',
    field: row =>
      row.runtime_env ? row.runtime_env.toString() : row.runtime_env
  }
];

// 配置文件外部列定义
export const fileConfigOutSideColunms = [
  { name: 'icon', label: '', field: 'icon' },
  {
    name: 'filename',
    label: '文件名',
    field: row => row.fileName
  },
  {
    name: 'upload_user',
    label: '上传人',
    field: row => row.upload_user
  },
  {
    name: 'upload_time',
    label: '上传时间',
    field: row => row.upload_time
  },
  {
    name: 'source_application',
    label: '所属应用',
    field: row => row.source_application_name
  },
  {
    name: 'source',
    label: '文件来源',
    field: row => row.source
  },
  { name: 'btn', label: '操作' }
];

// 对象存贮列定义
export const objSaveColumns = [
  { name: 'icon', label: '', field: 'icon' },
  {
    name: 'filename',
    label: '文件名',
    field: row => row.fileName
  },
  {
    name: 'upload_user',
    label: '上传人',
    field: row => row.upload_user
  },
  {
    name: 'upload_time',
    label: '上传时间',
    field: row => row.upload_time
  },
  {
    name: 'aws_type',
    label: '文件类型',
    field: row => row.aws_type
  },
  {
    name: 'bucket_name',
    label: '桶名称',
    field: row => row.bucket_name
  },
  {
    name: 'bucket_path',
    label: '桶内路径',
    field: row => row.bucket_path
  },
  {
    name: 'source',
    label: '文件来源',
    field: row => row.source
  },
  { name: 'btn', label: '操作' }
];

export const withoutFileColumns = [
  {
    name: 'filename',
    label: '文件名',
    field: row => row.fileName
  },
  {
    name: 'upload_user',
    label: '上传人',
    field: row => row.upload_user
  },
  {
    name: 'upload_time',
    label: '上传时间',
    field: row => row.upload_time
  },
  {
    name: 'source_application',
    label: '所属应用',
    field: row => row.source_application_name
  },
  {
    name: 'source',
    label: '文件来源',
    field: row => row.source
  },
  { name: 'btn', label: '操作' }
];

// 自动发布列定义
export const deAutoColumns = [
  {
    name: 'filename',
    label: '文件名',
    field: row => row.fileName
  },
  {
    name: 'upload_user',
    label: '上传人',
    field: row => row.upload_user
  },
  {
    name: 'upload_time',
    label: '上传时间',
    field: row => row.upload_time
  },
  {
    name: 'source',
    label: '文件来源',
    field: row => row.source
  },
  { name: 'btn', label: '操作' }
];

// 应用列表列定义
export function jobListColumns(releaseType) {
  return [
    {
      name: 'taskName',
      label: '任务名称',
      field: row => row.task_name
    },
    {
      name: 'group',
      label: '所属小组',
      field: row => row.groupFullName,
      copy: true
    },
    {
      name: 'dev_managers',
      label: '任务负责人',
      field: 'dev_managers'
    },
    {
      name: 'bank_master',
      label: '行内项目负责人',
      field: 'bank_master'
    },
    {
      name: 'taskProject',
      label: `所属${releaseType}`,
      field: row => row.task_project,
      sortable: true
    },
    {
      name: 'taskStage',
      label: '任务阶段',
      field: row => taskStage[row.task_stage]
    },
    {
      name: 'merge_release_flag',
      label: '合并release',
      field: 'merge_release_flag'
    },
    {
      name: 'merge_release_time',
      label: '合并时间',
      field: 'merge_release_time'
    },
    {
      name: 'taskStatus',
      label: '状态',
      field: row => statusFilter(row.task_status),
      sortable: true
    },
    {
      name: 'reject_reason',
      label: '拒绝理由',
      field: 'reject_reason'
    },
    { name: 'name', label: '操作', field: 'name', align: 'center' }
  ];
}

// 意向投产任务列定义
export function intentionJobListColumns(releaseType) {
  return [
    {
      name: 'task_name',
      label: '意向任务名称',
      field: 'task_name'
    },
    {
      name: 'rqrmnt_no',
      label: '意向需求编号',
      field: 'rqrmnt_no',
      copy: true
    },
    {
      name: 'rqrmnt_name',
      label: '意向需求名称',
      field: 'rqrmnt_name',
      copy: true
    },
    {
      name: 'task_group',
      label: '所属小组',
      field: 'task_group',
      copy: true
    },
    {
      name: 'dev_managers',
      label: '任务负责人',
      field: 'dev_managers'
    },
    {
      name: 'bank_master',
      label: '行内负责人',
      field: 'bank_master'
    },
    {
      name: 'task_project',
      label: `所属${releaseType}`,
      field: 'task_project'
    },
    {
      name: 'task_stage',
      label: '任务阶段',
      field: row => taskStage[row.task_stage]
    }
  ];
}

// 自动化环境列定义
export const automationEnvListColumns = [
  {
    name: 'env_name',
    field: 'env_name',
    label: '自动化发布环境'
  },
  {
    name: 'platform',
    field: 'platform',
    label: '部署平台'
  },
  {
    name: 'pro_dmz_caas',
    field: row => row.pro_dmz_caas || '-',
    label: '生产环境网银网段(CAAS)'
  },
  {
    name: 'pro_biz_caas',
    field: row => row.pro_biz_caas || '-',
    label: '生产环境业务网段(CAAS)'
  },
  {
    name: 'gray_dmz_caas',
    field: row => row.gray_dmz_caas || '-',
    label: '灰度环境网银网段(CAAS)'
  },
  {
    name: 'gray_biz_caas',
    field: row => row.gray_biz_caas || '-',
    label: '灰度环境业务网段(CAAS)'
  },
  {
    name: 'pro_dmz_scc',
    field: row => row.pro_dmz_scc || '-',
    label: '生产环境网银网段(SCC)'
  },
  {
    name: 'pro_biz_scc',
    field: row => row.pro_biz_scc || '-',
    label: '生产环境业务网段(SCC)'
  },
  {
    name: 'gray_dmz_scc',
    field: row => row.gray_dmz_scc || '-',
    label: '灰度环境网银网段(SCC)'
  },
  {
    name: 'gray_biz_scc',
    field: row => row.gray_biz_scc || '-',
    label: '灰度环境业务网段(SCC)'
  },
  {
    name: 'description',
    field: 'description',
    label: '描述'
  }
];

// 环境类型下拉选项
export const envTypeList = [
  {
    label: '生产网银网段',
    value: 'proDmz'
  },
  {
    label: '生产业务网段',
    value: 'proBiz'
  },
  {
    label: '灰度网银网段',
    value: 'grayDmz'
  },
  {
    label: '灰度业务网段',
    value: 'grayBiz'
  }
];

// 模块类型列定义
export const moduleTypeListColumns = [
  {
    name: 'catalog_name',
    field: 'catalog_name',
    label: '目录',
    copy: true
  },
  /* 2-微服务应用更新3-数据库更新4-配置文件更新5-公共配置文件更新 */
  {
    name: 'catalog_type',
    field: field => catalogType[field.catalog_type],
    label: '目录类型'
  },
  {
    name: 'description',
    field: 'description',
    label: '描述',
    copy: true
  }
];

// 脚本参数列定义
export const scriptParamsListColumns = [
  {
    name: 'key',
    field: 'key',
    label: '参数key'
  },
  {
    name: 'value',
    field: 'value',
    label: '参数value'
  },
  {
    name: 'description',
    field: 'description',
    label: '描述'
  }
];

// 用户列表列定义
export const userListColumns = [
  {
    name: 'owner_group_name',
    label: '牵头小组',
    field: 'owner_group_name'
  },
  {
    name: 'release_spdb_manager',
    label: '科技负责人',
    field: 'release_spdb_manager'
  },
  {
    name: 'release_spdb_manager_telephone',
    label: '联系电话',
    field: 'release_spdb_manager_telephone'
  },
  {
    name: 'release_manager',
    label: '投产负责人',
    field: 'release_manager'
  },
  {
    name: 'release_manager_telephone',
    label: '联系电话',
    field: 'release_manager_telephone'
  }
];

// 需求列表列定义
export const demandListColumns = [
  {
    name: 'number',
    label: '序号',
    field: 'number'
  },
  {
    name: 'tag',
    label: '标签',
    field: 'tag'
  },
  {
    name: 'ipmpUnit',
    label: '实施单元【ipmp实施单元编号/FDEV编号】',
    field: 'ipmpUnit'
  },
  {
    name: 'rqrmnt_name',
    label: '需求名称',
    field: 'rqrmnt_name'
  },
  {
    name: 'rqrmnt_no',
    label: '需求单号',
    field: 'rqrmnt_no'
  },
  {
    name: 'type',
    label: '需求类型',
    field: 'type'
  },
  {
    name: 'technology_type',
    label: '科技类型',
    field: 'technology_type'
  },
  {
    name: 'rqrmntContact',
    label: '业务联系人',
    field: 'rqrmntContact'
  },
  {
    name: 'rqrmnt_spdb_manager',
    label: '行内负责人',
    field: 'rqrmnt_spdb_manager'
  },
  {
    name: 'release_confirm_doc',
    label: '确认书情况',
    field: 'release_confirm_doc'
  },
  {
    name: 'confirmFileDateList',
    label: '上线确认书到达时间',
    field: 'confirmFileDateList'
  },
  {
    name: 'package_submit_test',
    label: '整包提测',
    field: 'package_submit_test'
  },
  {
    name: 'rel_test',
    label: '准生产提测',
    field: 'rel_test'
  },
  {
    name: 'doc',
    label: '需规地址',
    field: 'doc'
  }
];

// 需求详情列表列定义
export const demandDetailListColumns = [
  {
    name: 'group_name',
    label: '小组名',
    field: 'group_name'
  },
  {
    name: 'task_name',
    label: '任务名',
    field: 'task_name'
  },
  {
    name: 'stage',
    label: '阶段',
    field: 'stage'
  },
  {
    name: 'master',
    label: '任务负责人',
    field: 'master'
  },
  {
    name: 'confirmBtn',
    label: '任务确认书到达时间',
    field: 'confirmBtn'
  },
  {
    name: 'project_name',
    label: '应用英文名',
    field: 'project_name'
  },
  {
    name: 'specialCase',
    label: '特殊项内容',
    field: 'specialCase'
  },
  {
    name: 'dataBaseAlter',
    label: '是否涉及数据库',
    field: 'dataBaseAlter'
  },
  {
    name: 'new_add_sign',
    label: '是否新增应用',
    field: 'new_add_sign'
  },
  {
    name: 'commonProfile',
    label: '是否涉及配置文件',
    field: 'commonProfile'
  },
  {
    name: 'other_system',
    label: '同步投产系统',
    field: 'other_system'
  }
];

// 投产大窗口列定义
export const relaseBigListColumns = [
  {
    name: 'release_date',
    label: '投产大窗口日期',
    field: 'release_date'
  },
  {
    name: 'release_contact',
    label: '牵头联系人',
    field: 'release_contact'
  },
  {
    name: 'owner_group_name',
    label: '牵头小组',
    field: 'owner_group_name'
  },
  {
    name: 'creater',
    label: '创建人',
    field: 'creater'
  },
  {
    name: 'btn',
    label: '操作',
    field: 'btn'
  }
];

// 投产大窗口-需求列表列定义
export const demandBigListColumns = [
  {
    name: 'group_name',
    label: '组别',
    field: 'group_name'
  },
  {
    name: 'rqrmnt_name',
    label: '需求名称',
    field: 'rqrmnt_name'
  },
  {
    name: 'rqrmnt_no',
    label: '需求单号',
    field: 'rqrmnt_no'
  },
  {
    name: 'rqrmnt_spdb_manager',
    label: '行内负责人',
    field: 'rqrmnt_spdb_manager'
  },
  {
    name: 'rqrmntContact',
    label: '业务联系人',
    field: 'rqrmntContact'
  },
  {
    name: 'otherSystem',
    label: '涉及系统',
    field: 'otherSystem'
  }
];

// 试运行列表列定义
export const testListColumns = [
  {
    name: 'group_name',
    label: '组别',
    field: 'group_name'
  },
  {
    name: 'rqrmnt_name',
    label: '需求名称',
    field: 'rqrmnt_name'
  },
  {
    name: 'rqrmnt_no',
    label: '需求单号',
    field: 'rqrmnt_no'
  },
  {
    name: 'rqrmnt_spdb_manager',
    label: '行内负责人',
    field: 'rqrmnt_spdb_manager'
  },
  {
    name: 'rqrmntContact',
    label: '业务联系人',
    field: 'rqrmntContact'
  },
  {
    name: 'testKeyNote',
    label: '回归测试功能点',
    field: 'testKeyNote'
  }
];

// 审核意见列表列定义
export const reviewListColumns = [
  {
    name: 'taskName',
    label: '任务名',
    field: 'taskName'
  },
  {
    name: 'appName',
    label: '应用名',
    field: 'appName'
  },
  {
    name: 'group',
    label: '所属小组',
    field: 'group'
  },
  {
    name: 'applicantName',
    label: '申请人',
    field: 'applicantName'
  },
  {
    name: 'reviewers',
    label: '审核人',
    field: 'reviewers'
  },
  {
    name: 'master',
    label: '任务负责人',
    field: 'master'
  },
  {
    name: 'reviewStatus',
    label: '审核状态',
    field: 'reviewStatus'
  },
  {
    name: 'reviewIdea',
    label: '审核意见',
    field: 'reviewIdea'
  },
  {
    name: 'reviewType',
    label: '审核类型',
    field: 'reviewType'
  },
  {
    name: 'btn',
    label: '操作',
    field: 'btn'
  }
];

export function initEsfUserInfoData() {
  return {
    DEV: {
      account: '',
      password: '',
      config_area: {
        k1: null
      }
    },
    TEST: {
      account: '',
      password: '',
      config_area: {
        k1: null
      }
    },
    PROCSH: {
      account: '',
      password: '',
      config_area: {
        k1: null,
        k2: null
      }
    },
    PROCHF: {
      account: '',
      password: '',
      config_area: {
        k1: null,
        k2: null
      }
    }
  };
}

export const esfColumns = [
  { name: 'icon', label: '', field: 'icon' },
  {
    name: 'application_cn',
    label: '应用名称',
    field: 'application_cn'
  },
  {
    name: 'appSid',
    label: '应用sid',
    field: 'appSid'
  },
  {
    name: 'platform',
    label: '部署平台',
    field: 'platform'
  },
  {
    name: 'caas_network_area',
    label: 'CAAS网络模式',
    field: 'caas_network_area'
  },
  {
    name: 'scc_network_area',
    label: 'SCC网络模式',
    field: 'scc_network_area'
  },
  {
    name: 'upload_username_cn',
    label: '操作人',
    field: 'upload_username_cn'
  },
  {
    name: 'update_time',
    label: '操作时间',
    field: 'update_time'
  },
  {
    name: 'opt',
    label: '操作',
    field: 'opt'
  }
];

export const esfInfoColumns = [
  {
    name: 'runtime_env',
    label: '运行环境',
    field: 'runtime_env'
  },
  {
    name: 'account',
    label: '账号',
    field: 'account'
  },
  {
    name: 'password',
    label: '密码',
    field: 'password'
  },
  {
    name: 'clusterList',
    label: '集群',
    field: row => row.clusterList && row.clusterList.join(',')
  },
  {
    name: 'sdk_gk',
    label: 'SDK-GK',
    field: 'sdk_gk'
  },
  {
    name: 'config_area',
    label: '配置中心',
    field: 'config_area'
  }
];

export function initEsfCommonConfigData() {
  return {
    appName: null,
    appSid: '',
    DEV: {
      bucketName: '',
      files: []
    },
    TEST: {
      bucketName: '',
      files: []
    },
    PROCSH: {
      bucketNameK1: '',
      bucketNameK2: '',
      files: []
    },
    PROCHF: {
      bucketNameK1: '',
      bucketNameK2: '',
      files: []
    }
  };
}

export const esfCommonConfigColumns = [
  { name: 'icon', label: '', field: 'icon' },
  {
    name: 'source_application_name',
    label: '应用名称',
    field: 'source_application_name'
  },
  {
    name: 'appSid',
    label: '应用sid',
    field: 'appSid'
  },
  {
    name: 'upload_username_cn',
    label: '操作人',
    field: 'upload_username_cn'
  },
  {
    name: 'upload_time',
    label: '操作时间',
    field: 'upload_time'
  },
  {
    name: 'opt',
    label: '操作',
    field: 'opt'
  }
];

export const esfCommonConfigInfoColumns = [
  {
    name: 'runtime_env',
    label: '运行环境',
    field: 'runtime_env'
  },
  {
    name: 'fileName',
    label: '文件名',
    field: row => row.fileName.join('，') || '-'
  },
  {
    name: 'bucket_name',
    label: '桶名',
    field: 'bucket_name'
  }
];
// 生产问题列表
export function listModel() {
  return {
    start_time: '',
    end_time: '',
    module: ['5c81c4d0d3e2a1126ce30049'],
    responsible_type: '',
    responsible_name_en: '',
    deal_status: '',
    issue_level: '',
    problemType: [],
    isIncludeChildren: true,
    company: '',
    reviewer_status: ''
  };
}
export const dealStatusOptions = ['未修复', '修复中', '修复完成'];
export const issueLevelOptions = [
  { label: '流程规范性错误', value: '1' },
  { label: '一般错误', value: '2' },
  { label: '复杂错误', value: '3' }
];
export const responsibleTypeOptions = [
  { label: '开发责任人', value: '1' },
  { label: '审核责任人', value: '2' },
  { label: '内测责任人', value: '3' },
  { label: '任务牵头责任人', value: '4' }
];
export const problemTypeOptions = [
  '需求分析',
  '开发',
  '代码审核',
  '数据库审核',
  '内测',
  '业测',
  '打包',
  '其他'
];
export const reviewerOptions = ['已评审', '未评审'];

// BastionCommonconfig配置文件列定义
export const fileConfigBastionColumns = [
  {
    name: 'filename',
    label: '文件名',
    field: row => row.fileName
  },
  {
    name: 'child_catalog',
    label: '子目录',
    field: 'child_catalog'
  },
  {
    name: 'runtime_env',
    label: '运行环境',
    field: row =>
      row.runtime_env ? row.runtime_env.toString() : row.runtime_env
  }
];

// 模块ip映射表
// gray：灰度、proc：生产
// phone：手机、network：网银、batch：批量、otc：柜面
// sh：上海、hf：合肥
export const mapIpList = {
  commonconfig: {
    'gray-phone-sh': ['xxx'],
    'gray-phone-hf': ['xxx'],
    'gray-network-sh': ['xxx'],
    'gray-network-hf': ['xxx'],
    'proc-phone-sh': [
      'xxx',
      'xxx',
      'xxx',
      'xxx'
    ],
    'proc-phone-hf': [
      'xxx',
      'xxx',
      'xxx',
      'xxx',
      'xxx'
    ],
    'proc-network-sh': [
      'xxx',
      'xxx',
      'xxx',
      'xxx'
    ],
    'proc-network-hf': [
      'xxx',
      'xxx',
      'xxx',
      'xxx',
      'xxx'
    ],
    'proc-batch-sh': ['xxx', 'xxx'],
    'proc-batch-hf': ['xxx', 'xxx'],
    'proc-otc-sh': [
      'xxx',
      'xxx',
      'xxx',
      'xxx'
    ],
    'proc-otc-hf': [
      'xxx',
      'xxx',
      'xxx',
      'xxx'
    ]
  },
  cfg_nas: {
    'gray-phone-sh': ['xxx'],
    'gray-phone-hf': ['xxx'],
    'gray-network-sh': ['xxx'],
    'gray-network-hf': ['xxx'],
    'proc-phone-sh': ['xxx'],
    'proc-phone-hf': ['xxx'],
    'proc-network-sh': ['xxx'],
    'proc-network-hf': ['xxx'],
    'proc-batch-sh': ['xxx'],
    'proc-batch-hf': ['xxx'],
    'proc-otc-sh': ['xxx'],
    'proc-otc-hf': ['xxx']
  },
  cfg_core: {
    'gray-phone-sh': ['xxx', 'xxx'],
    'gray-phone-hf': ['xxx', 'xxx'],
    'gray-network-sh': ['xxx', 'xxx'],
    'gray-network-hf': ['xxx', 'xxx'],
    'proc-phone-sh': [
      'xxx',
      'xxx',
      'xxx',
      'xxx',
      'xxx'
    ],
    'proc-phone-hf': [
      'xxx',
      'xxx',
      'xxx',
      'xxx',
      'xxx',
      'xxx'
    ],
    'proc-network-sh': [
      'xxx',
      'xxx',
      'xxx',
      'xxx',
      'xxx'
    ],
    'proc-network-hf': [
      'xxx',
      'xxx',
      'xxx',
      'xxx',
      'xxx',
      'xxx'
    ],
    'proc-batch-sh': ['xxx', 'xxx', 'xxx'],
    'proc-batch-hf': ['xxx', 'xxx', 'xxx'],
    'proc-otc-sh': [
      'xxx',
      'xxx',
      'xxx',
      'xxx',
      'xxx'
    ],
    'proc-otc-hf': [
      'xxx',
      'xxx',
      'xxx',
      'xxx',
      'xxx'
    ]
  },
  app_nas: {
    'gray-phone-sh': ['xxx'],
    'gray-phone-hf': ['xxx'],
    'gray-network-sh': ['xxx'],
    'gray-network-hf': ['xxx'],
    'proc-phone-sh': ['xxx'],
    'proc-phone-hf': ['xxx'],
    'proc-network-sh': ['xxx'],
    'proc-network-hf': ['xxx'],
    'proc-batch-sh': ['xxx'],
    'proc-batch-hf': ['xxx'],
    'proc-otc-sh': ['xxx'],
    'proc-otc-hf': ['xxx']
  },
  cfgbef_mbank: ['xxx']
};
