import { getGroupFullName, taskStage, taskStage1 } from '@/utils/utils';
import {
  sourceOptions,
  webTypeFilter
} from '@/modules/Component/utils/constants';
import { taskSpecialStatus } from '@/modules/Job/utils/constants';
import { sort, setOperation } from './utils';
import { approveMap } from '@/modules/Rqr/model.js';

export function createAppColumns() {
  return [
    {
      name: 'name_zh',
      label: '应用中文名',
      field: 'name_zh',
      required: true
    },
    {
      name: 'name_en',
      label: '应用英文名',
      field: 'name_en',
      sortable: true,
      required: true
    },
    {
      name: 'group',
      label: '小组',
      field: row => row.group.name,
      sortable: true,
      copy: true
    },
    {
      name: 'hangnei',
      label: '行内应用负责人',
      field: 'spdb_managers'
    },
    {
      name: 'yingyong',
      label: '应用负责人',
      field: 'dev_managers'
    },
    {
      name: 'type_name',
      label: '应用类型名称',
      field: 'type_name'
    },
    {
      name: 'network',
      label: '部署网段',
      field: 'network'
    },
    {
      name: 'label',
      label: '标签',
      field: 'label'
    },
    {
      name: 'createtime',
      label: '创建时间',
      field: 'createtime',
      sortable: true
    }
  ];
}

export function createArchetypeColumns(groups) {
  return [
    {
      name: 'name_en',
      label: '骨架英文名',
      field: row => row.name_en,
      sortable: true,
      required: true
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
      field: field => getGroupFullName(groups, field.group),
      copy: true
    },
    {
      name: 'recommend_version',
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
      label: '编码',
      field: 'encoding'
    },
    {
      name: 'desc',
      label: '骨架描述',
      field: 'desc',
      copy: true
    }
  ];
}

export function createUnitColumns() {
  return [
    {
      name: 'name_en',
      label: '组件英文名',
      field: row => row.name_en,
      sortable: true,
      required: true,
      copy: true
    },
    {
      name: 'name_cn',
      label: '组件中文名',
      field: row => row.name_cn,
      sortable: true,
      copy: true
    },
    {
      name: 'manager_id',
      label: '负责人',
      field: 'manager_id'
    },
    {
      name: 'recommond_version',
      label: '推荐版本',
      copy: true,
      field: row => (row.recommond_version ? row.recommond_version : '-')
    },
    {
      name: 'source',
      label: '组件来源',
      field: row => (row.source ? sourceOptions[row.source].label : row.source)
    },
    {
      name: 'jdk_version',
      label: 'jdk版本',
      field: 'jdk_version',
      copy: true
    },
    {
      name: 'parent_nameen',
      label: '父组件名称',
      field: 'parent_nameen'
    },
    {
      name: 'desc',
      label: '描述',
      field: 'desc',
      copy: true
    }
  ];
}

export function createWebArchetypeColumns(groups) {
  return [
    {
      name: 'name_en',
      label: '骨架英文名',
      field: row => row.name_en,
      sortable: true,
      required: true,
      copy: true
    },
    {
      name: 'name_cn',
      label: '骨架中文名',
      field: row => row.name_cn,
      copy: true
    },
    {
      name: 'manager_id',
      label: '管理员',
      field: 'manager_id'
    },
    {
      name: 'group',
      label: '所属小组',
      field: field => getGroupFullName(groups, field.group),
      copy: true
    },
    {
      name: 'desc',
      label: '骨架描述',
      field: 'desc',
      copy: true
    }
  ];
}

export function createWebUnitColumns() {
  return [
    {
      name: 'name_en',
      label: '组件英文名',
      field: 'name_en',
      required: true,
      copy: true
    },
    {
      name: 'name_cn',
      label: '组件中文名',
      field: 'name_cn',
      copy: true
    },
    {
      name: 'manager',
      label: '组件管理员',
      field: 'manager'
    },
    {
      name: 'recommond_version',
      label: '组件推荐版本',
      field: 'recommond_version',
      copy: true
    },
    {
      name: 'source',
      label: '组件来源',
      field: row => (row.source == '0' ? '组内维护(自研)' : '第三方(开源)'),
      align: 'center'
    },
    {
      name: 'npm_name',
      label: 'npm坐标name',
      field: 'npm_name',
      copy: true
    },
    {
      name: 'npm_group',
      label: 'npm坐标group',
      field: 'npm_group',
      copy: true
    },
    {
      name: 'type',
      label: '组件类型',
      field: row => webTypeFilter[row.type],
      align: 'center'
    },
    {
      name: 'group',
      label: '所属小组',
      field: 'group_name',
      copy: true
    },
    {
      name: 'gitlab_url',
      label: 'gitlab地址',
      field: 'gitlab_url',
      copy: true
    },
    {
      name: 'root_dir',
      label: '项目根路径',
      field: 'root_dir',
      copy: true
    },
    {
      name: 'desc',
      label: '描述',
      field: 'desc',
      copy: true
    }
  ];
}

export function createProdProblemsColumns() {
  return [
    {
      name: 'problem_phenomenon',
      field: 'problem_phenomenon',
      label: '问题现象',
      required: true,
      copy: true
    },
    {
      name: 'requirement_name',
      field: 'requirement_name',
      label: '需求名称',
      copy: true
    },
    {
      name: 'occurred_time',
      field: 'occurred_time',
      label: '发生日期'
    },
    {
      name: 'first_occurred_time',
      field: 'first_occurred_time',
      label: '首次发生时间'
    },
    {
      name: 'location_time',
      field: 'location_time',
      label: '定位时间'
    },
    {
      name: 'repair_time',
      field: 'repair_time',
      label: '修复时间'
    },
    {
      name: 'reviewer_time',
      field: 'reviewer_time',
      label: '评审时间'
    },
    {
      name: 'reviewer',
      field: 'reviewer',
      label: '评审人'
    },
    {
      name: 'emergency_process',
      field: 'emergency_process',
      label: '应急过程',
      copy: true
    },
    {
      name: 'emergency_responsible',
      field: 'emergency_responsible',
      label: '应急负责人'
    },
    {
      name: 'issue_type',
      field: 'issue_type',
      label: '问题类型'
    },
    {
      name: 'is_trigger_issue',
      field: 'is_trigger_issue',
      label: '是否产生生产问题'
    },
    {
      name: 'issue_level',
      field: 'issue_level',
      label: '生产问题级别'
    },
    {
      name: 'deal_status',
      field: 'deal_status',
      label: '处理状态'
    },
    {
      name: 'dev_responsible',
      field: 'dev_responsible',
      label: '开发责任人'
    },
    {
      name: 'audit_responsible',
      field: 'audit_responsible',
      label: '审核责任人'
    },
    {
      name: 'test_responsible',
      field: 'test_responsible',
      label: '内测责任人'
    },
    {
      name: 'module',
      field: 'module',
      label: '所属板块'
    },
    {
      name: 'influence_area',
      field: 'influence_area',
      label: '影响范围'
    },
    {
      name: 'issue_reason',
      field: 'issue_reason',
      label: '问题原因',
      copy: true
    },
    {
      name: 'btn',
      field: 'btn',
      label: '操作',
      required: true
    }
  ];
}

export const taskColumns = [
  {
    name: 'name',
    label: '任务名称',
    field: 'name',
    required: true
  },
  {
    name: 'project_name',
    label: '所属应用',
    field: 'project_name',
    sortable: true
  },
  {
    name: 'feature_branch',
    label: '分支名称',
    field: 'feature_branch',
    copy: true
  },
  {
    name: 'stage',
    label: '任务阶段',
    field: row =>
      row.taskType !== 2 ? taskStage[row.stage] : taskStage1[row.stage],
    sortable: true,
    sort: (a, b) => sort(a, b)
  },
  {
    name: 'taskSpectialStatus',
    label: '暂缓',
    field: row => taskSpecialStatus[row.taskSpectialStatus]
  },
  {
    name: 'plan_start_time',
    label: '计划启动日期',
    field: 'plan_start_time',
    sortable: true
  },
  {
    name: 'plan_inner_test_time',
    label: '计划内测日期',
    field: 'plan_inner_test_time',
    sortable: true
  },
  {
    name: 'plan_fire_time',
    label: '计划投产日期',
    field: 'plan_fire_time',
    sortable: true
  },
  {
    name: 'test_tag',
    label: '提测状态',
    field: 'tag'
  },
  {
    name: 'creator',
    label: '创建者',
    field: 'creator'
  },
  {
    name: 'developer',
    label: '开发人员',
    field: 'developer'
  },

  {
    name: 'master',
    label: '任务负责人',
    field: 'master'
  },
  {
    name: 'spdb_master',
    label: '行内项目负责人',
    field: 'spdb_master'
  },
  {
    name: 'tester',
    label: '测试人员',
    field: 'tester'
  },
  {
    name: 'group',
    label: '所属小组',
    field: row => row.group,
    copy: true
  },
  {
    name: 'redmine_id',
    label: '研发单元编号',
    field: 'redmine_id'
  },
  {
    name: 'uat_TestObject',
    label: 'uat测试对象',
    field: 'uat_TestObject'
  },
  {
    name: 'desc',
    label: '描述',
    field: 'desc'
  },
  {
    name: 'operation',
    label: '操作',
    field: row => setOperation(row.stage),
    sortable: true,
    required: true
  }
];

// 任务归档
export const batchFileCol = [
  {
    name: 'name',
    label: '任务名称',
    field: 'name'
  },
  {
    name: 'app_name_en',
    label: '所属应用',
    field: 'app_name_en'
  },
  {
    name: 'branch',
    label: '分支名称',
    field: 'branch'
  },
  {
    name: 'stage',
    label: '任务阶段',
    field: 'stage'
  },
  {
    name: 'group',
    label: '所属小组',
    field: 'group'
  }
];
//合并待审列表
export function todoCodeColumns() {
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
      name: 'merge_reason',
      label: '申请原因',
      field: 'merge_reason'
    },
    {
      name: 'apply_desc',
      label: '申请说明',
      field: 'apply_desc'
    },
    {
      name: 'operate',
      label: '操作',
      field: 'operate',
      align: 'left'
    }
  ];
}
//合并已完成
export function codeDoneColumns() {
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
      name: 'merge_reason',
      label: '申请原因',
      field: 'merge_reason'
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
      label: '审批结果',
      field: 'status',
      align: 'left'
    }
  ];
}

export function createReUnitColumns() {
  return [
    {
      name: 'fdevUnitNo',
      label: '研发单元编号',
      align: 'left',
      field: 'fdevUnitNo'
    },
    {
      name: 'fdevUnitName',
      label: '研发单元内容',
      field: 'fdevUnitName',
      align: 'left'
    },
    {
      name: 'demandNo',
      label: '需求编号',
      field: 'demandNo',
      align: 'left'
    },
    {
      name: 'demandName',
      label: '需求名称',
      field: 'demandName',
      align: 'left'
    },
    {
      name: 'fdevUnitState',
      label: '研发单元状态',
      field: 'fdevUnitState',
      align: 'left'
    },
    {
      name: 'approveType',
      label: '审批类型',
      field: 'approveType',
      format: (val, row) => approveMap[val],
      align: 'left'
    },
    {
      name: 'overdueType',
      label: '超期类别',
      field: 'overdueType',
      align: 'left'
    },
    {
      name: 'fdevUnitLeaderInfo',
      label: '牵头人',
      field: 'fdevUnitLeaderInfo',
      align: 'left'
    },
    {
      name: 'groupName',
      label: '所属小组',
      field: 'groupName',
      align: 'left'
    },
    {
      name: 'planStartDate',
      label: '计划启动开发日期',
      field: 'planStartDate',
      align: 'left'
    },
    {
      name: 'planInnerTestDate',
      label: '计划提交内测日期',
      field: 'planInnerTestDate',
      align: 'left'
    },
    {
      name: 'planTestDate',
      label: '计划提交用户测试日期',
      field: 'planTestDate',
      align: 'left'
    },
    {
      name: 'planTestFinishDate',
      label: '计划用户测试完成日期',
      field: 'planTestFinishDate',
      align: 'left'
    },
    {
      name: 'planProductDate',
      label: '计划投产日期',
      field: 'planProductDate',
      align: 'left'
    },
    {
      name: 'overdueReason',
      label: '申请原因',
      field: 'overdueReason',
      align: 'left'
    },
    {
      name: 'applicantName',
      label: '申请人',
      field: 'applicantName'
    },
    {
      name: 'approveState',
      label: '审批状态',
      field: 'approveState'
    },
    {
      name: 'approverName',
      label: '审批人',
      field: 'approverName'
    },
    {
      name: 'sectionName',
      label: '所属条线',
      field: 'sectionName'
    },
    {
      name: 'applyTime',
      label: '申请审批时间',
      field: 'applyTime'
    },
    {
      name: 'approveTime',
      label: '审批时间',
      field: 'approveTime'
    },
    {
      name: 'approveReason',
      label: '审批说明',
      field: 'approveReason'
    },
    {
      name: 'operate',
      label: '操作',
      field: 'operate',
      required: true,
      align: 'left'
    }
  ];
}
export function doneReUnitColumns() {
  return [
    {
      name: 'fdevUnitNo',
      label: '研发单元编号',
      align: 'left',
      field: 'fdevUnitNo'
    },
    {
      name: 'fdevUnitName',
      label: '研发单元内容',
      field: 'fdevUnitName',
      align: 'left'
    },
    {
      name: 'demandNo',
      label: '需求编号',
      field: 'demandNo',
      align: 'left'
    },
    {
      name: 'demandName',
      label: '需求名称',
      field: 'demandName',
      align: 'left'
    },
    {
      name: 'fdevUnitState',
      label: '研发单元状态',
      field: 'fdevUnitState',
      align: 'left'
    },
    {
      name: 'approveType',
      label: '审批类型',
      format: (val, row) => approveMap[val],
      field: 'approveType',
      align: 'left'
    },
    {
      name: 'overdueType',
      label: '超期类别',
      field: 'overdueType',
      align: 'left'
    },
    {
      name: 'fdevUnitLeaderInfo',
      label: '牵头人',
      field: 'fdevUnitLeaderInfo',
      align: 'left'
    },
    {
      name: 'groupName',
      label: '所属小组',
      field: 'groupName',
      align: 'left'
    },
    {
      name: 'planStartDate',
      label: '计划启动开发日期',
      field: 'planStartDate',

      align: 'left'
    },
    {
      name: 'planInnerTestDate',
      label: '计划提交内测日期',
      field: 'planInnerTestDate',

      align: 'left'
    },
    {
      name: 'planTestDate',
      label: '计划提交用户测试日期',
      field: 'planTestDate',

      align: 'left'
    },
    {
      name: 'planTestFinishDate',
      label: '计划用户测试完成日期',
      field: 'planTestFinishDate',
      align: 'left'
    },
    {
      name: 'planProductDate',
      label: '计划投产日期',
      field: 'planProductDate',
      align: 'left'
    },
    {
      name: 'overdueReason',
      label: '申请原因',
      field: 'overdueReason',
      align: 'left'
    },
    {
      name: 'applicantName',
      label: '申请人',
      field: 'applicantName'
    },

    {
      name: 'approverName',
      label: '审批人',
      field: 'approverName'
    },
    {
      name: 'sectionName',
      label: '所属条线',
      field: 'sectionName'
    },
    {
      name: 'applyTime',
      label: '申请审批时间',
      field: 'applyTime'
    },
    {
      name: 'approveTime',
      label: '审批时间',
      field: 'approveTime'
    },
    {
      name: 'approveReason',
      label: '审批说明',
      field: 'approveReason'
    },
    {
      name: 'approveState',
      label: '审批状态',
      field: 'approveState'
    }
  ];
}
// 需求评估定稿审批
export function eveluateApprovalColumns() {
  return [
    {
      name: 'oa_contact_name',
      label: '需求名称',
      field: 'oa_contact_name',
      required: true,
      copy: true
    },
    {
      name: 'oa_contact_no',
      label: '需求编号',
      field: 'oa_contact_no',
      copy: true
    },
    {
      name: 'demand_leader_group_cn',
      label: '牵头小组',
      field: 'demand_leader_group_cn',
      copy: true
    },
    {
      name: 'apply_user',
      label: '申请人',
      field: 'apply_user',
      copy: true
    },
    {
      name: 'apply_reason',
      label: '申请原因',
      field: 'apply_reason'
    },
    {
      name: 'apply_update_time',
      label: '申请定稿日期修改时间',
      field: 'apply_update_time'
    },
    {
      name: 'create_time',
      label: '申请时间',
      field: 'create_time'
    },
    {
      name: 'operate_user',
      label: '审批人',
      field: 'operate_user'
    },
    {
      name: 'operate_time',
      label: '操作时间',
      field: 'operate_time'
    },
    {
      name: 'state',
      label: '拒绝原因',
      field: 'state'
    },
    {
      name: 'operate_status',
      label: '审批状态',
      field: 'operate_status'
    },
    {
      name: 'operation',
      label: '操作',
      field: 'operation',
      required: true,
      align: 'left'
    }
  ];
}
//需求定稿审批完成列表
export function doneApprovalColumns() {
  return [
    {
      name: 'oa_contact_name',
      label: '需求名称',
      field: 'oa_contact_name',
      required: true,
      copy: true
    },
    {
      name: 'oa_contact_no',
      label: '需求编号',
      field: 'oa_contact_no',
      copy: true
    },
    {
      name: 'demand_leader_group_cn',
      label: '牵头小组',
      field: 'demand_leader_group_cn',
      copy: true
    },
    {
      name: 'apply_user',
      label: '申请人',
      field: 'apply_user',
      copy: true
    },
    {
      name: 'apply_reason',
      label: '申请原因',
      field: 'apply_reason'
    },
    {
      name: 'apply_update_time',
      label: '申请定稿日期修改时间',
      field: 'apply_update_time'
    },
    {
      name: 'create_time',
      label: '申请时间',
      field: 'create_time'
    },
    {
      name: 'operate_user',
      label: '审批人',
      field: 'operate_user'
    },
    {
      name: 'operate_time',
      label: '操作时间',
      field: 'operate_time'
    },
    {
      name: 'state',
      label: '拒绝原因',
      field: 'state'
    },
    {
      name: 'operate_status',
      label: '审批状态',
      field: 'operate_status'
    }
  ];
}
//Sit环境部署申请审批列表
export function createSitApprovalColumns() {
  return [
    {
      name: 'taskName',
      label: '任务名称',
      field: 'taskName',
      align: 'left',
      required: true
    },
    {
      name: 'oaContactName',
      label: '需求名称',
      field: 'oaContactName',
      align: 'left'
    },
    {
      name: 'oaContactNo',
      label: '需求编号',
      field: 'oaContactNo',
      align: 'left'
    },
    {
      name: 'applicationNameEn',
      label: '所属应用',
      field: 'applicationNameEn',
      align: 'left'
    },

    {
      name: 'deployEnv',
      label: '部署环境',
      field: 'deployEnv',
      align: 'left'
    },
    {
      name: 'applayUserNameZh',
      label: '申请人',
      field: 'applayUserNameZh'
    },
    {
      name: 'overdueReason',
      label: '申请原因',
      field: 'overdueReason',
      align: 'left'
    },
    {
      name: 'applayUserOwnerGroup',
      label: '所属小组',
      field: 'applayUserOwnerGroup',
      align: 'left'
    }
  ];
}
//Sit环境部署申请审批列表(已完成)
export function doneSit2ApprovalColumns() {
  return [
    {
      name: 'taskName',
      label: '任务名称',
      field: 'taskName',
      align: 'left',
      required: true
    },
    {
      name: 'oaContactName',
      label: '需求名称',
      field: 'oaContactName',
      align: 'left'
    },
    {
      name: 'oaContactNo',
      label: '需求编号',
      field: 'oaContactNo',
      align: 'left'
    },
    {
      name: 'applicationNameEn',
      label: '所属应用',
      field: 'applicationNameEn',
      align: 'left'
    },

    {
      name: 'deployEnv',
      label: '部署环境',
      field: 'deployEnv',
      align: 'left'
    },
    {
      name: 'applayUserNameZh',
      label: '申请人',
      field: 'applayUserNameZh'
    },
    {
      name: 'deploy_status',
      label: '部署状态',
      field: 'deploy_status',
      align: 'left'
    },
    {
      name: 'overdueReason',
      label: '申请原因',
      field: 'overdueReason',
      align: 'left'
    },
    {
      name: 'applayUserOwnerGroup',
      label: '所属小组',
      field: 'applayUserOwnerGroup',
      align: 'left'
    },
    {
      name: 'reviewUserNameZh',
      label: '操作管理员',
      field: 'reviewUserNameZh',
      align: 'left'
    },
    {
      name: 'reviewTime',
      label: '操作时间',
      field: 'reviewTime',
      align: 'left'
    },
    {
      name: 'operation',
      label: '操作',
      field: 'operation',
      required: true,
      align: 'left'
    }
  ];
}
