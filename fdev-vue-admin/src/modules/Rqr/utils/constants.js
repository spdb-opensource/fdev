import { approveMap } from '@/modules/Rqr/model.js';
// 需求tab
export const tabsList = [
  {
    name: 'demandInfo',
    label: '基础信息'
  },
  {
    name: 'otherDemand',
    label: '其他需求任务'
  },
  {
    name: 'unit',
    label: '实施单元'
  },
  {
    name: 'developNo',
    label: '研发单元'
  },
  {
    name: 'task',
    label: '任务'
  },
  {
    name: 'file',
    label: '文档库'
  }
];
export function createDemandModel() {
  return {
    oa_real_no: '', //OA联系单编号
    oa_contact_no: '', //需求编号
    demand_status_normal: '',
    oa_contact_name: '', //OA联系单名称
    oa_receive_date: '', //OA收件日期
    propose_demand_dept: '', //需求提出部门
    propose_demand_user: '', //需求联系人
    demand_instruction: '', //需求说明书名称
    demand_plan_no: '', //对应需求计划编号
    demand_plan_name: '', //需求计划名称
    demand_background: '', //需求背景
    former_communication: '', //前期沟通情况
    demand_available: '', //需求是否可行
    demand_assess_way: '', //需求评估方式
    demand_status_special: '',
    demand_record_no: '', //需求备案编号
    future_assess: '', //是否纳入后评估
    review_user: '', //评审人
    respect_product_date: '', //需求期望投产日期
    available_assess_idea: '', //需求可行性评估意见
    demand_leader_group: [], //牵头小组
    demand_leader_all: [],
    demand_leader: [], //牵头人
    plan_user: '', // 规划联系人
    priority: '', //优先级
    accept_date: '', //受理日期
    extra_idea: '', //实施团队可行性评估补充意见
    ui_verify: '', //是否涉及UI审核
    relate_part: [], //涉及板块
    relate_part_detail: [],
    demand_desc: '', //需求说明
    demand_create_user_all: {},
    isTransferRqrmnt: '',
    havaImpl: true, //需求下是否包含实施单元
    impl_track_user: '', //实施单元跟踪人
    tech_type: '', //科技类型
    tech_type_desc: '' //科技类型备注
  };
}
export const perform = {
  visibleColumnUnitNoOptions: [
    'fdev_implement_unit_no',
    'ipmp_implement_unit_no',
    'task_name',
    'implement_unit_content',
    'implement_unit_status_normal',
    'implement_leader_all',
    'ui_verify',
    'groupName',
    'plan_start_date',
    'plan_inner_test_date',
    'plan_test_date',
    'plan_test_finish_date',
    'plan_product_date',
    'real_start_date',
    'real_inner_test_date',
    'real_test_date',
    'real_test_finish_date',
    'real_product_date',
    'relate_system_name',
    'dept_workload',
    'company_workload',
    'create_user_all',
    'create_time',
    'operation'
  ],
  visibleColumnDevelopNoOptions: [
    'implUnitNum',
    'implStatusName',
    'implContent',
    'implLeaderName',
    'implLeader',
    'headerUnitName',
    'headerTeamName',
    'testLeaderName',
    'testLeader',
    'testLeaderEmail',
    'implDelayTypeName',
    'implDelayReason',
    'planDevelopDate',
    'planInnerTestDate',
    'planTestStartDate',
    'planTestFinishDate',
    'planProductDate',
    'acturalDevelopDate',
    'actualInnerTestDate',
    'acturalTestStartDate',
    'acturalTestFinishDate',
    'acturalProductDate',
    'relateSysName',
    'expectOwnWorkload',
    'expectOutWorkload',
    'prjNum',
    'leaderGroupName'
  ],
  visibleColumnTaskOptions: [
    'name',
    'feature_branch',
    'stage',
    'plan_start_time',
    'plan_inner_test_time',
    'plan_uat_test_start_time',
    'plan_rel_test_time',
    'plan_fire_time',
    'start_time',
    'start_inner_test_time',
    'start_uat_test_time',
    'stop_uat_test_time',
    'fire_time',
    'creator',
    'developer',
    'master',
    'spdb_master',
    'tester',
    'group',
    'redmine_id',
    'uat_TestObject',
    'desc'
  ]
};

export function createFileModel() {
  return {
    volume: '',
    fileroot: '',
    file: null,
    filename: '',
    filetype: { label: '需求说明书', value: 'demandInstruction' },
    rqrmntId: ''
  };
}

export const fileTypes = [
  { label: '需求说明书', value: 'demandInstruction' },
  { label: '技术方案', value: 'techPlan' },
  { label: '需求评审决议表', value: 'demandReviewResult' },
  { label: '会议纪要', value: 'meetMinutes' },
  { label: '其他相关材料', value: 'otherRelatedFile' },
  { label: 'confluence文档', value: 'confluenceFile' },
  { label: '需求规格说明书', value: 'demandPlanInstruction' }
];

export function createFilelist() {
  return {
    created_at: '',
    created_by: {},
    filetype: '',
    id: '',
    name: '',
    number: '',
    size: '',
    type: '',
    updated_at: '',
    updated_by: {},
    volume_id: '',
    docLink: ''
  };
}

export function updateDialogModel() {
  return {
    oa_contact_no: '',
    oa_contact_name: '',
    oa_receive_date: '',
    propose_demand_dept: '',
    propose_demand_user: '',
    demand_instruction: '',
    demand_desc: '',
    demand_plan_no: '',
    demand_plan_name: '',
    demand_background: '',
    former_communication: '',
    demand_available: '',
    demand_assess_way: '',
    demand_record_no: '',
    future_assess: '',
    review_user: '',
    respect_product_date: '',
    available_assess_idea: '',
    demand_leader_group_cn: '',
    demand_leader_all: [],
    demand_leader: [],
    plan_user: '',
    priority: '',
    accept_date: '',
    plan_start_date: '',
    plan_test_date: '',
    plan_product_date: '',
    extra_idea: '',
    ui_verify: false,
    real_start_date: '',
    real_test: '',
    real_product_date: '',
    plan_contactor: '',
    relate_part: [],
    relate_part_detail: [],
    demand_create_user_all: {}
  };
}

export const priorityOptions = [
  { label: '高', value: '0' },
  { label: '中', value: '1' },
  { label: '一般', value: '2' },
  { label: '低', value: '3' }
];

export const demandStatusList = [
  { label: '预评估', value: '0' },
  { label: '评估中', value: '1' },
  { label: '待实施', value: '2' },
  { label: '开发中', value: '3' },
  { label: 'SIT', value: '4' },
  { label: 'UAT', value: '5' },
  { label: 'REL', value: '6' },
  { label: '已投产', value: '7' },
  { label: '已归档', value: '8' },
  { label: '已撤销', value: '9' },
  { label: '暂缓', value: 'defer' }
];
export const featureTypeList = [
  { label: '启动预进行', value: 'plan_start_date' },
  { label: '提交内测预进行', value: 'plan_inner_test_date' },
  { label: '提交业测预进行', value: 'plan_test_date' },
  { label: '用户测试完成预进行', value: 'plan_test_finish_date' },
  { label: '投产预进行', value: 'plan_product_date' }
];
export const realDateTypeList = [
  { label: '需求创建日期', value: 'demand_create_time' },
  { label: '实际启动日期', value: 'real_start_date' },
  { label: '实际提交内测日期', value: 'real_inner_test_date' },
  { label: '实际提交业测日期', value: 'real_test_date' },
  { label: '实际用户测试完成日期', value: 'real_test_finish_date' },
  { label: '实际投产日期', value: 'real_product_date' }
];
export const delayTypeList = [
  { label: '启动延期', value: 'plan_start_date' },
  { label: '提交内测延期', value: 'plan_inner_test_date' },
  { label: '提交用户测试延期', value: 'plan_test_date' },
  { label: '用户测试完成延期', value: 'plan_test_finish_date' },
  { label: '投产延期', value: 'plan_product_date' }
];
export const designStatusList = [
  { label: '不涉及', value: 'noRelate' },
  { label: '未上传', value: 'uploadNot' },
  { label: '已上传', value: 'uploaded' },
  { label: '待审核', value: 'auditWait' },
  { label: '审核中', value: 'auditIn' },
  { label: '审核通过', value: 'auditPass' },
  { label: '审核不通过', value: 'auditPassNot' },
  { label: '审核完成', value: 'completedAudit' },
  { label: '异常关闭', value: 'abnormalShutdown' }
];
export const groupStateList = [
  { label: '牵头小组', value: '0' },
  { label: '涉及小组', value: '1' }
];
export const groupQueryTypeList = [
  { label: '本组', value: '0' },
  { label: '本组及子组', value: '1' }
];
export const demandTypeList = [
  { label: '全部', value: '' },
  { label: '业务需求', value: 'business' },
  { label: '科技内部需求', value: 'tech' },
  { label: '日常需求', value: 'daily' }
];
export const checkRangeList = [
  { label: '查看所有', value: false },
  { label: '与我相关', value: true }
];
export const priorityList = [
  { label: '全部', value: '' },
  { label: '高', value: '0' },
  { label: '中', value: '1' },
  { label: '一般', value: '2' },
  { label: '低', value: '3' }
];

export const designOptions = {
  noRelate: '不涉及',
  uploadNot: '未上传',
  uploaded: '已上传',
  auditWait: '待审核',
  auditIn: '审核中',
  auditPass: '审核通过',
  auditPassNot: '审核不通过',
  completedAudit: '审核完成',
  abnormalShutdown: '异常关闭'
};
export const demandTableColumns = [
  {
    name: 'oa_contact_name',
    label: '需求名称',
    field: 'oa_contact_name',
    required: true,
    sortable: true
  },
  {
    name: 'oa_contact_no',
    label: '需求编号（OA联系单编号）',
    field: 'oa_contact_no',
    sortable: true
  },
  {
    name: 'demand_type',
    label: '需求类型',
    field: 'demand_type',
    sortable: true
  },
  {
    name: 'demand_status_normal',
    label: '需求状态',
    field: 'demand_status_normal',
    sortable: true
  },
  {
    name: 'design_status',
    label: '设计稿审核状态',
    field: 'design_status',
    sortable: true
  },
  {
    name: 'priority',
    label: '优先级',
    field: 'priority',
    sortable: true
  },
  {
    name: 'propose_demand_dept',
    label: '需求部门',
    field: 'propose_demand_dept'
  },
  {
    name: 'propose_demand_user',
    label: '需求联系人',
    field: 'propose_demand_user'
  },
  {
    name: 'plan_user',
    label: '规划联系人',
    field: 'plan_user',
    sortable: true
  },
  {
    name: 'demand_leader_all',
    label: '牵头人',
    field: 'demand_leader_all',
    sortable: true
  },
  {
    name: 'demand_leader_group_cn',
    label: '所属小组',
    field: 'demand_leader_group_cn',
    sortable: true
  },
  {
    name: 'plan_start_date',
    label: '计划启动开发日期',
    field: 'plan_start_date',

    sortable: true
  },
  {
    name: 'plan_inner_test_date',
    label: '计划提交内测日期',
    field: 'plan_inner_test_date',
    sortable: true
  },
  {
    name: 'plan_test_date',
    label: '计划提交业测日期',
    field: 'plan_test_date',
    sortable: true
  },
  {
    name: 'plan_test_finish_date',
    label: '计划用户测试完成日期',
    field: 'plan_test_finish_date',
    sortable: true
  },
  {
    name: 'plan_product_date',
    label: '计划投产日期',
    field: 'plan_product_date',
    sortable: true
  },
  {
    name: 'real_start_date',
    label: '实际启动开发日期',
    field: 'real_start_date',
    sortable: true
  },
  {
    name: 'real_inner_test_date',
    label: '实际提交内测日期',
    field: 'real_inner_test_date',
    sortable: true
  },
  {
    name: 'real_test_date',
    label: '实际提交业测日期',
    field: 'real_test_date',
    sortable: true
  },
  {
    name: 'real_test_finish_date',
    label: '实际用户测试完成日期',
    field: 'real_test_finish_date',
    sortable: true
  },
  {
    name: 'real_product_date',
    label: '实际投产日期',
    field: 'real_product_date',
    sortable: true
  },
  {
    name: 'dept_workload',
    label: '需求预期我部工作量',
    field: 'dept_workload',
    sortable: true
  },
  {
    name: 'company_workload',
    label: '需求预期公司工作量',
    field: 'company_workload',
    sortable: true
  },
  {
    name: 'operation',
    label: '操作',
    field: 'operation',
    required: true
  }
];

export const taskAndIpmpTableColumns = [
  {
    name: 'groupName',
    label: '所属小组',
    field: 'groupName',
    sortable: true
  },
  {
    name: 'taskNo',
    label: '任务集编号',
    field: 'taskNo',
    sortable: true
  },
  {
    name: 'taskName',
    label: '任务集名称',
    field: 'taskName',
    sortable: true
  },
  {
    name: 'ipmpImplementUnitNo',
    label: '实施单元编号',
    field: 'ipmpImplementUnitNo',
    sortable: true
  },
  {
    name: 'implementUnitContent',
    label: '实施单元内容',
    field: 'implementUnitContent',
    sortable: true
  },
  {
    name: 'operation',
    label: '操作',
    field: 'operation'
  }
];

export function createReqImplUnitModel() {
  return {
    demand_type: '',
    batch_no: '',
    task_name: '',
    ipmp_implement_unit_no: '',
    implement_unit_content: '',
    group: '',
    implement_leader: [],
    implement_leader_account: [],
    implement_leader_all: [],
    plan_start_date: '',
    plan_inner_test_date: '',
    plan_test_date: '',
    plan_test_finish_date: '',
    plan_product_date: '',
    relate_system_name: '',
    dept_workload: '',
    company_workload: '',
    remark: '',
    demand_id: '',
    group_cn: '',
    ui_verify: null,
    ipmpGroupId: ''
  };
}

export const evaluateTableColumns = [
  {
    name: 'fdev_implement_unit_no',
    label: '研发单元编号',
    field: 'fdev_implement_unit_no',
    required: true,
    copy: true
  },
  {
    name: 'implement_unit_content',
    label: '研发单元内容',
    field: 'implement_unit_content',
    copy: true
  },
  {
    name: 'ipmp_implement_unit_no',
    label: '实施单元编号',
    field: 'ipmp_implement_unit_no',
    required: true,
    copy: true
  },
  {
    name: 'implement_leader_all',
    label: '研发单元牵头人',
    field: 'implement_leader_all',
    copy: true
  },
  {
    name: 'ui_verify',
    label: '是否涉及UI还原审核',
    field: 'ui_verify'
  },
  {
    name: 'implement_unit_status_normal',
    label: '状态',
    field: 'implement_unit_status_normal'
  },
  {
    name: 'group_cn',
    label: '所属小组',
    field: 'group_cn',
    copy: true
  },
  {
    name: 'plan_start_date',
    label: '计划启动开发日期',
    field: 'plan_start_date'
  },
  {
    name: 'plan_inner_test_date',
    label: '计划提交内测日期',
    field: 'plan_inner_test_date'
  },
  {
    name: 'plan_test_date',
    label: '计划提交用户测试日期',
    field: 'plan_test_date'
  },
  {
    name: 'plan_test_finish_date',
    label: '计划用户测试完成日期',
    field: 'plan_test_finish_date'
  },
  {
    name: 'plan_product_date',
    label: '计划投产日期',
    field: 'plan_product_date'
  },
  {
    name: 'real_start_date',
    label: '实际启动开发日期',
    field: 'real_start_date'
  },
  {
    name: 'real_inner_test_date',
    label: '实际提交内测日期',
    field: 'real_inner_test_date'
  },
  {
    name: 'real_test_date',
    label: '实际提交用户测试日期',
    field: 'real_test_date'
  },
  {
    name: 'real_test_finish_date',
    label: '实际用户测试完成日期',
    field: 'real_test_finish_date'
  },
  {
    name: 'real_product_date',
    label: '实际投产日期',
    field: 'real_product_date'
  },
  {
    name: 'dept_workload',
    label: '预期行内人员工作量',
    field: 'dept_workload'
  },
  {
    name: 'company_workload',
    label: '预期公司人员工作量',
    field: 'company_workload'
  },
  {
    name: 'create_user_all',
    label: '研发单元创建人',
    field: 'create_user_all'
  },
  {
    name: 'create_time',
    label: '研发单元创建时间',
    field: 'create_time'
  },
  // {
  //   name: 'company_workload',
  //
  //   label: '预期公司人员工作量',
  //   field: 'company_workload',
  //   sortable: true
  // },
  // {
  //   name: 'remark',
  //
  //   label: '备注',
  //   field: 'remark',
  //   sortable: true
  // },
  {
    name: 'operate',
    label: '操作',
    field: 'operate',
    required: true
  }
];

export const priorities = [
  { label: '高', value: '0' },
  { label: '中', value: '1' },
  { label: '一般', value: '2' },
  { label: '低', value: '3' }
];

export const futureAssess = [
  { label: '纳入后评估', value: '0' },
  { label: '不纳入后评估', value: '1' }
];

export const demandAvailables = [
  { label: '可行', value: '0' },
  { label: '部分可行', value: '1' },
  { label: '不可行', value: '2' }
];
export const delayAvailables = [
  { label: '业务部门尚在测试', value: 'implunit.delay.type.01' },
  { label: '需求变更', value: 'implunit.delay.type.02' },
  { label: '关联系统配合问题', value: 'implunit.delay.type.03' },
  { label: '需求复杂', value: 'implunit.delay.type.04' },
  { label: '需求优先级调整', value: 'implunit.delay.type.05' },
  { label: '第三方配合问题', value: 'implunit.delay.type.06' },
  { label: '关联需求未投产', value: 'implunit.delay.type.07' },
  { label: '商务阶段进度延误', value: 'implunit.delay.type.08' },
  { label: '配套业务制度尚未完成', value: 'implunit.delay.type.09' },
  { label: '开发问题', value: 'implunit.delay.type.10' },
  { label: '需求待细化', value: 'implunit.delay.type.11' },
  { label: '其他', value: 'implunit.delay.type.12' }
];
//涉及UI审核选项
export const uiOptions = [
  { label: '涉及', value: true },
  { label: '不涉及', value: false }
];

export const demandAssessWays = [
  { label: '业务需求评审', value: '0' },
  { label: '科技需求备案', value: '1' }
];

export const demandList = [
  { label: '业务需求', value: '1' },
  { label: '科技内部需求', value: '0' },
  { label: '日常需求', value: '2' }
];

export const implementLeadDeptList = [
  '开发服务中心',
  '系统运维中心',
  '大数据应用中心',
  '测试服务中心',
  '架构管理处',
  '规划管理处',
  '安全内控处',
  '项目管理处',
  '综合管理处',
  '创新研究中心'
];

export const implementLeadTeamList = [
  {
    label: '核心平台开发处-基础平台',
    value: '核心平台开发处-基础平台',
    dept: '开发服务中心'
  },
  {
    label: '核心平台开发处-村镇银行',
    value: '核心平台开发处-村镇银行',
    dept: '开发服务中心'
  },
  {
    label: '公共渠道开发处-公共渠道',
    value: '公共渠道开发处-公共渠道',
    dept: '开发服务中心'
  },
  {
    label: '应用开发服务分中心（上海）-互联网应用',
    value: '应用开发服务分中心（上海）-互联网应用',
    dept: '开发服务中心'
  },
  {
    label: '应用开发服务分中心（上海）-个人业务',
    value: '应用开发服务分中心（上海）-个人业务',
    dept: '开发服务中心'
  },
  {
    label: '应用开发服务分中心（上海）-公司业务',
    value: '应用开发服务分中心（上海）-公司业务',
    dept: '开发服务中心'
  },
  {
    label: '应用开发服务分中心（上海）-管理信息',
    value: '应用开发服务分中心（上海）-管理信息',
    dept: '开发服务中心'
  },
  {
    label: '应用开发服务分中心（上海）-信用卡业务',
    value: '应用开发服务分中心（上海）-信用卡业务',
    dept: '开发服务中心'
  },
  {
    label: '应用开发服务分中心（上海）-中间业务',
    value: '应用开发服务分中心（上海）-中间业务',
    dept: '开发服务中心'
  },
  {
    label: '应用开发服务分中心（合肥）-合肥开发',
    value: '应用开发服务分中心（合肥）-合肥开发',
    dept: '开发服务中心'
  },
  {
    label: '应用开发服务分中心（武汉）',
    value: '应用开发服务分中心（武汉）',
    dept: '开发服务中心'
  },
  {
    label: '应用开发服务分中心（成都）',
    value: '应用开发服务分中心（成都）',
    dept: '开发服务中心'
  },
  {
    label: '应用开发服务分中心（西安）',
    value: '应用开发服务分中心（西安）',
    dept: '开发服务中心'
  },
  { label: '保障支持处', value: '保障支持处', dept: '系统运维中心' },
  { label: '生产运行处', value: '生产运行处', dept: '系统运维中心' },
  { label: '生产调度处', value: '生产调度处', dept: '系统运维中心' },
  { label: '数据运用服务处', value: '数据运用服务处', dept: '大数据应用中心' },
  { label: '数据运用开发处', value: '数据运用开发处', dept: '大数据应用中心' },
  { label: '测试服务处', value: '测试服务处', dept: '测试服务中心' },
  { label: '测试实施处', value: '测试实施处', dept: '测试服务中心' },
  { label: '架构管理处', value: '架构管理处', dept: '架构管理处' },
  { label: '需求管理', value: '需求管理', dept: '规划管理处' },
  { label: '规划管理', value: '规划管理', dept: '规划管理处' },
  { label: '安全内控处', value: '安全内控处', dept: '安全内控处' },
  { label: '项目管理', value: '项目管理', dept: '项目管理处' },
  { label: '商务管理', value: '商务管理', dept: '项目管理处' },
  { label: '综合管理处', value: '综合管理处', dept: '综合管理处' },
  { label: '创新实验室', value: '创新实验室', dept: '创新研究中心' },
  { label: '技术研究院(处)', value: '技术研究院(处)', dept: '创新研究中心' }
];

export const evaluateVisibleColumns = [
  'implement_unit_content',
  'implement_leader_all',
  'group',
  'plan_start_date',
  'plan_inner_test_date',
  'plan_test_date',
  'plan_test_finish_date',
  'plan_product_date',
  'relate_system_name',
  'dept_workload',
  'company_workload',
  'remark'
];

export const unitVisibleColumns = [
  'implContent',
  'implUnitNum',
  'implStatusName'
];

export function createTaskAndImplModel() {
  return {
    group: '',
    taskNo: '',
    taskName: '',
    ipmpImplementUnitNo: '',
    implementUnitContent: ''
  };
}

export const uiList = [
  { label: '是', value: true },
  { label: '否', value: false }
];

//在职浦发人员不分页查询条件
export const queryUserOptionsParams = {
  company_id: '5c452d6fd3e2a12d806e0dkl',
  status: '0',
  page: 1,
  per_page: 0
};

export const normalStatus = {
  assessmenting: 1,
  toImplement: 2,
  developing: 3,
  sit: 4,
  uat: 5,
  rel: 6,
  done: 7,
  archived: 8,
  undo: 9
};

export const specialStatus = {
  staying: 1,
  inRecovery: 2,
  recoveryDone: 3
};

export const roleName = {
  spdbHead: '行内项目负责人',
  companyHead: '厂商项目负责人'
};

export const demandStatus = {
  0: '预评估',
  1: '评估中',
  2: '待实施',
  3: '开发中',
  4: 'SIT',
  5: 'UAT',
  6: 'REL',
  7: '已投产',
  8: '已归档',
  9: '已撤销'
};

export const assessStatusMap = {
  0: '预评估',
  1: '评估中',
  2: '评估完成'
};

export const demandStatusSpecial = {
  1: '暂缓',
  2: '恢复中',
  3: '恢复完成'
};

export const unitStatusSpecial = {
  '01': '暂存',
  '02': '评估中',
  '03': '待实施',
  '04': '业务测试中',
  '05': '开发中',
  '06': '已投产',
  '07': '暂缓',
  '08': '已撤销',
  '09': '业务测试完成'
};

export const delayTypeSpecial = {
  'implunit.delay.type.01': '业务部门尚在测试',
  'implunit.delay.type.02': '需求变更',
  'implunit.delay.type.03': '关联系统配合问题',
  'implunit.delay.type.04': '需求复杂',
  'implunit.delay.type.05': '需求优先级调整',
  'implunit.delay.type.06': '第三方配合问题',
  'implunit.delay.type.07': '关联需求未投产',
  'implunit.delay.type.08': '商务阶段进度延误',
  'implunit.delay.type.09': '配套业务制度尚未完成',
  'implunit.delay.type.10': '开发问题',
  'implunit.delay.type.11': '需求待细化',
  'implunit.delay.type.12': '其他'
};

export const designStatusMap = {
  noRelate: '不涉及',
  uploadNot: '未上传',
  uploaded: '已上传',
  auditWait: '待审核',
  auditIn: '审核中',
  auditPass: '审核通过',
  auditPassNot: '审核不通过',
  completedAudit: '审核完成',
  abnormalShutdown: '异常关闭'
};

export const reviewMap = {
  wait_upload: '待上传',
  uploaded: '已上传',
  wait_allot: '待分配',
  fixing: '审核中',
  nopass: '未通过',
  finished: '审核通过',
  irrelevant: '不涉及（没有审核流程）',
  uninvolved: '不涉及（有审核流程，审核流程中点不涉及，还需要UI负责人点完成）'
};

export const demandAssessWayMap = {
  0: '业务需求评审',
  1: '科技需求备案'
};

export const futrueAssessMap = {
  0: '纳入后评估',
  1: '不纳入后评估'
};

export const priValue = {
  0: '高',
  1: '中',
  2: '一般',
  3: '低'
};

export const demandAvalableMap = {
  0: '可行',
  1: '部分可行',
  2: '不可行'
};

export const ipmpUnitColumns = [
  {
    name: 'implContent',
    label: '实施单元内容',
    field: 'implContent',
    sortable: true,
    required: true,
    copy: true
  },
  {
    name: 'implUnitNum',
    label: '实施单元编号',
    field: 'implUnitNum',
    sortable: true,
    copy: true
  },
  {
    name: 'implLeaderName',
    label: '实施单元牵头人',
    field: 'implLeaderName',
    copy: true
  },
  {
    name: 'implStatusName',
    label: '实施单元状态',
    field: 'implStatusName',
    copy: true
  },
  {
    name: 'prjNum',
    label: '项目编号',
    field: 'prjNum',
    copy: true
  },
  {
    name: 'headerUnitName',
    label: '实施单元牵头单位',
    field: 'headerUnitName',
    sortable: true,
    copy: true
  },
  {
    name: 'planDevelopDate',
    label: '计划启动开发日期',
    field: 'planDevelopDate',
    sortable: true,
    copy: true
  },
  {
    name: 'planInnerTestDate',
    label: '计划提交内测日期',
    field: 'planInnerTestDate',
    sortable: true,
    copy: true
  },
  {
    name: 'planTestStartDate',
    label: '计划提交用户测试日期',
    field: 'planTestStartDate',
    sortable: true,
    copy: true
  },
  {
    name: 'planTestFinishDate',
    label: '计划用户测试完成日期',
    field: 'planTestFinishDate',
    sortable: true,
    copy: true
  },
  {
    name: 'planProductDate',
    label: '计划投产日期',
    field: 'planProductDate',
    sortable: true,
    copy: true
  },
  {
    name: 'acturalDevelopDate',
    label: '实际启动开发日期',
    field: 'acturalDevelopDate',
    sortable: true
  },
  {
    name: 'actualInnerTestDate',
    label: '实际提交内测日期',
    field: 'actualInnerTestDate',
    sortable: true
  },
  {
    name: 'acturalTestStartDate',
    label: '实际提交用户测试日期',
    field: 'acturalTestStartDate',
    sortable: true
  },
  {
    name: 'acturalTestFinishDate',
    label: '实际用户测试完成日期',
    field: 'acturalTestFinishDate',
    sortable: true
  },
  {
    name: 'acturalProductDate',
    label: '实际投产日期',
    field: 'acturalProductDate',
    sortable: true
  },
  {
    name: 'relateSysName',
    label: '涉及系统名称',
    field: 'relateSysName',
    sortable: true,
    copy: true
  },
  {
    name: 'expectOwnWorkload',
    label: '预期行内人员工作量',
    field: 'expectOwnWorkload',
    sortable: true
  },
  {
    name: 'expectOutWorkload',
    label: '预期公司人员工作量',
    field: 'expectOutWorkload',
    sortable: true
  },
  {
    name: 'leaderGroupName',
    label: '牵头小组',
    field: 'leaderGroupName',
    sortable: true,
    copy: true
  }
];

export const createMemory = function() {
  return {
    groupQueryType: null,
    groupState: null,
    priority: null,
    datetype: null,
    delayNum: null,
    featureType: null,
    featureNum: null,
    stateNum: null,
    designState: null,
    relDateType: null,
    relStartDate: null,
    relEndDate: null
  };
};

export const moreSearchMap = {
  groupQueryType: 'updateGroupQueryType',
  groupState: 'updateGroupState',
  priority: 'updatePriority',
  datetype: 'updateDatetype',
  delayNum: 'updateDelayNum',
  featureType: 'updateFeatureType',
  featureNum: 'updateFeatureNum',
  designState: 'updateDesignState',
  stateNum: 'updatestateNum',
  relDateType: 'updateRelDateType',
  relStartDate: 'updateRelStartDate',
  relEndDate: 'updateRelEndDate'
};

export const techTypes = [
  '公共科技',
  '安全修复',
  '生产问题',
  '性能调优',
  '兼容性修复',
  '其他（需备注）'
];

/*
 *实施状态, 后端字段为number类型
 */
export function getDemandStatusStr(val) {
  const obj = {
    '0': {
      name: '待实施',
      color: '#78909C'
    },
    '1': {
      name: '实施中',
      color: '#42A5F5'
    },
    '2': {
      name: '暂缓',
      color: '#FD8D00'
    },
    '3': {
      name: '撤销',
      color: '#F1524B'
    },
    '4': {
      name: '已完成',
      color: '#3FA142'
    },
    '5': {
      name: '已归档',
      color: '#bf1fa3'
    }
  };
  return obj[val] ? obj[val] : {};
}

export const devListColumn = [
  {
    name: 'fdev_implement_unit_no',
    label: '研发单元编号',
    field: 'fdev_implement_unit_no',
    required: true,
    copy: true
  },
  {
    name: 'implement_unit_content',
    label: '研发单元内容',
    field: 'implement_unit_content',
    copy: true
  },
  {
    name: 'demand_no',
    label: '需求编号',
    field: 'demand_no',
    copy: true
  },
  {
    name: 'demand_name',
    label: '需求名称',
    field: 'demand_name',
    copy: true
  },
  {
    name: 'ipmp_implement_unit_no',
    label: '实施单元编号',
    field: 'ipmp_implement_unit_no',
    required: true,
    copy: true
  },
  {
    name: 'other_demand_task_num',
    label: '其他需求任务编号',
    field: 'other_demand_task_num',
    required: true,
    copy: true
  },
  {
    name: 'implement_leader_all',
    label: '牵头人',
    field: 'implement_leader_all'
  },
  {
    name: 'implement_unit_status_normal',
    label: '状态',
    field: 'implement_unit_status_normal'
  },
  {
    name: 'group_cn',
    label: '所属小组',
    field: 'group_cn',
    copy: true
  },
  {
    name: 'plan_start_date',
    label: '计划启动开发日期',
    field: 'plan_start_date'
  },
  {
    name: 'real_start_date',
    label: '实际启动开发日期',
    field: 'real_start_date'
  },
  {
    name: 'plan_inner_test_date',
    label: '计划提交内测日期',
    field: 'plan_inner_test_date'
  },
  {
    name: 'real_inner_test_date',
    label: '实际提交内测日期',
    field: 'real_inner_test_date'
  },
  {
    name: 'plan_test_date',
    label: '计划提交用户测试日期',
    field: 'plan_test_date'
  },
  {
    name: 'real_test_date',
    label: '实际提交用户测试日期',
    field: 'real_test_date'
  },
  {
    name: 'plan_test_finish_date',
    label: '计划用户测试完成日期',
    field: 'plan_test_finish_date'
  },
  {
    name: 'real_test_finish_date',
    label: '实际用户测试完成日期',
    field: 'real_test_finish_date'
  },
  {
    name: 'plan_product_date',
    label: '计划投产/完成日期',
    field: 'plan_product_date'
  },
  {
    name: 'real_product_date',
    label: '实际投产/完成日期',
    field: 'real_product_date'
  },
  {
    name: 'dept_workload',
    label: '预期行内人员工作量',
    field: 'dept_workload'
  },
  {
    name: 'company_workload',
    label: '预期公司人员工作量',
    field: 'company_workload'
  },
  {
    name: 'create_user_all',
    label: '创建人',
    field: 'create_user_all'
  },
  {
    name: 'create_time',
    label: '创建时间',
    field: 'create_time'
  }
];
export const statusOptions = [
  { label: '评估中', value: '1' },
  { label: '待实施/未开始', value: '2' },
  { label: '开发中/进行中', value: '3' },
  { label: 'SIT', value: '4' },
  { label: 'UAT', value: '5' },
  { label: 'REL', value: '6' },
  { label: '已投产/已完成', value: '7' },
  { label: '已归档', value: '8' },
  { label: '已撤销', value: '9' },
  { label: '暂缓', value: 'defer' }
];

export const ipmpListColumn = [
  {
    name: 'implUnitNum',
    label: 'IPMP实施单元编号',
    field: 'implUnitNum',
    required: true,
    copy: true
  },
  {
    name: 'implContent',
    label: '实施单元内容',
    field: 'implContent',
    copy: true
  },
  {
    name: 'informationNum',
    label: '需求编号',
    field: 'informationNum',
    copy: true
  },
  {
    name: 'informationTitle',
    label: '需求名称',
    field: 'informationTitle',
    copy: true
  },
  {
    name: 'implStatusName',
    label: '实施状态',
    field: 'implStatusName'
  },
  {
    name: 'testLeaderName',
    label: '测试牵头人',
    field: 'testLeaderName'
  },
  {
    name: 'leaderGroupName',
    label: '牵头小组',
    field: 'leaderGroupName',
    copy: true
  },
  {
    name: 'headerUnitName',
    label: '牵头单位',
    field: 'headerUnitName',
    copy: true
  },
  {
    name: 'headerTeamName',
    label: '牵头团队',
    field: 'headerTeamName',
    copy: true
  },
  {
    name: 'planDevelopDate',
    label: '计划启动开发日期',
    field: 'planDevelopDate'
  },
  {
    name: 'planDevelopDateAdjust',
    label: '计划启动开发日期(调)',
    field: 'planDevelopDateAdjust'
  },
  {
    name: 'planInnerTestDate',
    label: '计划提交内测日期',
    field: 'planInnerTestDate'
  },
  {
    name: 'planTestStartDate',
    label: '计划提交用户测试日期',
    field: 'planTestStartDate'
  },
  {
    name: 'planTestStartDateAdjust',
    label: '计划提交用户测试日期(调)',
    field: 'planTestStartDateAdjust'
  },
  {
    name: 'planTestFinishDate',
    label: '计划用户测试完成日期',
    field: 'planTestFinishDate'
  },
  {
    name: 'planTestFinishDateAdjust',
    label: '计划用户测试完成日期(调)',
    field: 'planTestFinishDateAdjust'
  },
  {
    name: 'planProductDate',
    label: '计划投产日期',
    field: 'planProductDate'
  },
  {
    name: 'planProductDateAdjust',
    label: '计划投产日期(调)',
    field: 'planProductDateAdjust'
  },
  {
    name: 'acturalDevelopDate',
    label: '实际启动开发日期',
    field: 'acturalDevelopDate'
  },
  {
    name: 'actualInnerTestDate',
    label: '实际提交内测日期',
    field: 'actualInnerTestDate'
  },
  {
    name: 'acturalTestStartDate',
    label: '实际提交用户测试日期',
    field: 'acturalTestStartDate'
  },
  {
    name: 'acturalTestFinishDate',
    label: '实际用户测试完成日期',
    field: 'acturalTestFinishDate'
  },
  {
    name: 'acturalProductDate',
    label: '实际投产日期',
    field: 'acturalProductDate'
  },
  {
    name: 'relateSysCode',
    label: '涉及系统编号',
    field: 'relateSysCode',
    copy: true
  },
  {
    name: 'planPrjName',
    label: '拟纳入项目名称    ',
    field: 'planPrjName',
    copy: true
  },
  {
    name: 'prjNum',
    label: '项目编号',
    field: 'prjNum',
    copy: true
  },
  {
    name: 'testImplDeptName',
    label: '测试实施部门',
    field: 'testImplDeptName'
  },
  {
    name: 'implDelayTypeName',
    label: 'ipmp实施延期原因分类',
    field: 'implDelayTypeName'
  },
  {
    name: 'implDelayReason',
    label: 'ipmp实施延期原因',
    field: 'implDelayReason',
    copy: true
  },
  {
    name: 'cloudFlagName',
    label: '是否上云',
    field: 'cloudFlagName'
  },
  {
    name: 'techSchemeNo',
    label: '技术方案编号',
    field: 'techSchemeNo'
  },
  {
    name: 'checkerUserNames',
    label: '审核人',
    field: 'checkerUserNames',
    copy: true
  },
  {
    name: 'implLeaderName',
    label: '实施牵头人',
    field: 'implLeaderName',
    copy: true
  },
  {
    name: 'implChangeTypeName',
    label: 'fdev实施计划变更原因分类',
    field: 'implChangeTypeName',
    copy: true
  },
  {
    name: 'implChangeReason',
    label: 'fdev实施计划变更原因',
    field: 'implChangeReason',
    copy: true
  },
  {
    name: 'confirmDelayStage',
    label: '确认延期阶段',
    field: 'confirmDelayStage',
    copy: true
  },
  {
    name: 'businessEmail',
    label: '业务确认邮件',
    field: 'businessEmail'
  },
  {
    name: 'operation',
    label: '操作',
    field: 'operation',
    required: true,
    align: 'left'
  }
];

//研发单元审批列表
export const rdunitColums = [
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
    name: 'fdevUnitLeaderInfo',
    label: '牵头人',
    field: 'fdevUnitLeaderInfo',
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
    name: 'overdueType',
    label: '超期类别',
    field: 'overdueType',
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

//提测单列表
export const submitTestColums = [
  {
    name: 'test_order',
    label: '提测单编号',
    align: 'left',
    field: 'test_order'
  },
  {
    name: 'fdev_implement_unit_no',
    label: '研发单元编号',
    align: 'left',
    field: 'fdev_implement_unit_no'
  },
  {
    name: 'oa_contact_no',
    label: '需求编号',
    field: 'oa_contact_no',
    align: 'left'
  },
  {
    name: 'oa_contact_name',
    label: '需求名称',
    field: 'oa_contact_name',
    align: 'left'
  },
  {
    name: 'status',
    label: '状态',
    field: 'status',
    align: 'left'
  },
  {
    name: 'impl_unit_num',
    label: '实施单元编号',
    field: 'impl_unit_num',
    align: 'left'
  },
  {
    name: 'create_user_info',
    label: '创建人',
    field: 'create_user_info',
    align: 'left'
  },
  {
    name: 'group_cn',
    label: '小组',
    field: 'group_cn',
    align: 'left'
  },
  {
    name: 'plan_test_start_date',
    label: '计划提交业测日期',
    field: 'plan_test_start_date',
    align: 'left'
  },
  {
    name: 'app_name',
    label: '需求涉及的应用名称',
    field: 'app_name',
    align: 'left'
  },
  {
    name: 'developer',
    label: '开发人员',
    field: 'developer',
    align: 'left'
  },
  {
    name: 'test_content',
    label: '测试内容',
    field: 'test_content',
    align: 'left'
  },
  {
    name: 'operate',
    label: '操作',
    field: 'operate',
    align: 'left'
  }
];

export const approveStatusOptions = [
  { label: '待审批', value: 'wait' },
  { label: '通过', value: 'pass' },
  { label: '拒绝', value: 'reject' }
];

export const testStatusOptions = [
  { label: '已创建', value: 'create' },
  { label: '已提测', value: 'test' },
  { label: '已归档', value: 'file' },
  { label: '已撤销', value: 'deleted' }
];
//提交测试数据模型
export const createSubmitTestModel = {
  group: '', //小组id
  demand_id: '', //需求id
  oa_contact_no: '', //需求编号
  impl_unit_num: [], //IPMP实施单元
  impl_unit_num_show: '',
  fdev_implement_unit_no: [], //研发单元编号
  business_email: '', //业务人员邮箱
  test_manager_info: [], //测试经理用户信息类
  test_cc_user_ids: [], //提测邮件通知抄送人员id
  developer: '', //开发人员
  test_content: '', //测试内容
  unit_test_result: '', //单元测试情况
  inner_test_result: '', //内测通过情况
  trans_interface_change: { label: '是', value: 'yes' }, //是否涉及交易接口改动
  database_change: { label: '否', value: 'no' }, //是否涉及数据库改动
  regress_test: { label: '否', value: 'no' }, //是否涉及回归测试
  regress_test_range: '', //具体回归测试范围
  system: '', //涉及关联系统同步改造
  client_change: '', //是否涉及客户端更新
  client_download: '', //客户端下载地址
  test_environment: '', //测试环境
  daily_cc_user_ids: [], //测试日报抄送人员id
  remark: '', //备注
  deInstruction_files: [], //需求说明书
  req_files: [], //需求规格说明书
  other_files: [] //其他相关材料
};

export const YesOrNo = [
  { label: '是', value: 'yes' },
  { label: '否', value: 'no' }
];
