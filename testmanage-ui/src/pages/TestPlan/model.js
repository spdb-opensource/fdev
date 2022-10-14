export function createPlanAddModel() {
  return {
    systemName: '',
    testcaseName: '',
    testcaseType: '',
    testcasePriority: '',
    testcaseNature: '',
    funcationPoint: '',
    testcasePre: '',
    testcaseDescribe: '',
    expectedResult: '',
    testcaseFuncObj: []
  };
}

export function createTestPlanAddModel() {
  return {
    form: {
      inputPlanName: '',
      inputStartTime: '',
      inputEndTime: '',
      deviceInfo: ''
    }
  };
}

export function createMantisModel() {
  return {
    taskNo: '',
    project: '',
    system_name: '',
    function_module: '',
    redmine_id: '',
    workNo: '',
    system_version: '',
    stage: '',
    developer: '',
    copy_to_list: [],
    handler: '',
    handlerCn: '',
    priority: '',
    severity: '',
    flaw_source: '',
    plan_fix_date: '',
    flaw_type: '',
    summary: '',
    description: '',
    reason: '',
    app_name: ''
  };
}

export const stateList = [
  { label: '未执行', value: '0' },
  { label: '成功', value: '1' },
  { label: '阻塞', value: '2' },
  { label: '失败', value: '3' },
  { label: '执行', value: 'allExe' }
];

export const auditList = [
  { label: '新建', value: '0' },
  { label: '待评审', value: '10' },
  { label: '审核通过', value: '11' },
  { label: '审核拒绝', value: '12' },
  { label: '待生效', value: '20' },
  { label: '生效通过', value: '21' },
  { label: '生效拒绝', value: '22' },
  { label: '通用', value: '30' },
  { label: '废弃', value: '40' }
];

export const auditList1 = [
  { label: '页面', value: '1' },
  { label: '功能', value: '2' },
  { label: '流程', value: '3' },
  { label: '链接', value: '4' },
  { label: '接口', value: '5' },
  { label: '批量', value: '6' }
];

export const dialogRules = {
  deviceInfo: [{ required: true, message: '请输入设备信息', trigger: 'blur' }],
  inputPlanName: [
    { required: true, message: '请输入计划名称', trigger: 'blur' }
  ],
  inputStartTime: [
    { required: true, message: '请选择计划开始时间', trigger: 'blur' }
  ],
  inputEndTime: [
    { required: true, message: '请选择计划结束时间', trigger: 'blur' }
  ],
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  startTime: [
    { required: true, message: '请选择计划开始时间', trigger: 'blur' }
  ],
  endTime: [{ required: true, message: '请选择计划结束时间', trigger: 'blur' }],
  selectValue: [{ required: true, message: '请选择计划名称', trigger: 'blur' }],
  testcaseNo: [{ required: true, message: '请输入案例编号', trigger: 'blur' }],
  systemId: [{ required: true, message: '请选择系统名称', trigger: 'blur' }],
  systemName: [{ required: true, message: '请选择系统名称', trigger: 'blur' }],
  testcaseName: [
    { required: true, message: '请输入案例名称', trigger: 'blur' }
  ],
  testcaseType: [
    { required: true, message: '请选择案例类型', trigger: 'blur' }
  ],
  testcasePriority: [
    { required: true, message: '请选择优先级', trigger: 'blur' }
  ],
  testcaseNature: [
    { required: true, message: '请选择案例性质', trigger: 'blur' }
  ],
  testcaseDescribe: [
    { required: true, message: '请输入案例描述', trigger: 'blur' }
  ],
  expectedResult: [
    { required: true, message: '请输入预期结果', trigger: 'blur' }
  ]
};
export const mantisDialogRules = {
  project: [
    { required: true, message: '请选择项目名称', trigger: ['blur', 'change'] }
  ],
  app_name: [{ required: true, message: '请选择应用英文名', trigger: 'blur' }],
  system_version: [
    { required: true, message: '请选择系统版本', trigger: 'blur' }
  ],
  stage: [{ required: true, message: '请选择归属阶段', trigger: 'blur' }],
  developer: [
    { required: true, message: '请选择开发责任人', trigger: ['blur', 'change'] }
  ],
  handler: [{ required: true, message: '请输入分派人员', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'blur' }],
  severity: [
    { required: true, message: '请选择严重性', trigger: ['blur', 'change'] }
  ],
  flaw_source: [
    { required: true, message: '请选择缺陷来源', trigger: ['blur', 'change'] }
  ],
  plan_fix_date: [
    { required: true, message: '请选择预计修复时间', trigger: 'blur' }
  ],
  flaw_type: [
    { required: true, message: '请选择缺陷类型', trigger: ['blur', 'change'] }
  ],
  summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }]
};

export const testcaseStatusList = {
  '0': '新建',
  '10': '待评审',
  '11': '审核通过',
  '12': '审核拒绝',
  '20': '待生效',
  '21': '生效通过',
  '22': '生效拒绝',
  '30': '通用',
  '40': '废弃'
};
