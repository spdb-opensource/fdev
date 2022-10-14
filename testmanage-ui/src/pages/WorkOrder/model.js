export const orderStage = {
  '0': {
    label: '待分配',
    type: 'success'
  },
  '1': {
    label: '开发中',
    type: 'primary'
  },
  '2': {
    label: 'SIT',
    type: 'warning'
  },
  '3': {
    label: 'UAT',
    type: 'warning'
  },
  '4': {
    label: '已投产',
    type: 'danger'
  },
  '6': {
    label: 'UAT(含风险)',
    type: 'warning'
  },
  '8': {
    label: '废弃',
    type: 'warning'
  },
  '9': {
    label: '分包测试(内测完成)',
    type: 'warning'
  },
  '10': {
    label: '分包测试(含风险)',
    type: 'warning'
  },
  '12': {
    label: '安全测试(内测完成)',
    type: 'warning'
  },
  '13': {
    label: '安全测试(含风险)',
    type: 'warning'
  },
  '14': {
    label: '安全测试(不涉及)',
    type: 'warning'
  }
};

export const orderStageOther = {
  '0': {
    label: '待分配',
    type: 'success'
  },
  '1': {
    label: '开发中',
    type: 'primary'
  },
  '2': {
    label: 'SIT',
    type: 'warning'
  },
  '3': {
    label: 'UAT',
    type: 'warning'
  },
  '4': {
    label: '已投产',
    type: 'danger'
  },
  '6': {
    label: 'UAT(含风险)',
    type: 'warning'
  },
  '8': {
    label: '撤销',
    type: 'warning'
  },
  '9': {
    label: '分包测试(内测完成)',
    type: 'warning'
  },
  '10': {
    label: '分包测试(含风险)',
    type: 'warning'
  },
  '11': {
    label: '已废弃',
    type: 'warning'
  },
  '12': {
    label: '安全测试(内测完成)',
    type: 'warning'
  },
  '13': {
    label: '安全测试(含风险)',
    type: 'warning'
  },
  '14': {
    label: '安全测试(不涉及)',
    type: 'warning'
  }
};

export const stageOptions = [
  { label: '待分配', value: '0' },
  { label: '开发中', value: '1' },
  { label: 'SIT', value: '2' },
  { label: 'UAT', value: '3' },
  { label: '已投产', value: '4' },
  { label: 'UAT(含风险)', value: '6' },
  { label: '分包测试(内测完成)', value: '9' },
  { label: '分包测试(含风险)', value: '10' },
  { label: '安全测试(内测完成)', value: '12' },
  { label: '安全测试(含风险)', value: '13' },
  { label: '安全测试(不涉及)', value: '14' }
];

export const sitFlagOptions = [
  { label: '全部', value: '' },
  { label: 'fdev已提内测', value: '1' },
  { label: 'fdev未提内测', value: '0' }
];
export const orderTypeList = [
  { label: '功能测试', value: 'function' },
  { label: '安全测试', value: 'security' },
  { label: '全部', value: 'all' }
];
export const revertOptions = [
  {
    value: 1,
    label: '文档不规范'
  },
  {
    value: 2,
    label: '文档缺失'
  },
  {
    value: 3,
    label: '冒烟测试不通过'
  }
];

export const testReasonStage = {
  1: '正常',
  2: '缺陷',
  3: '需求变更'
};

export function selectMyOrders() {
  return {
    orderType: '',
    userEnName: '',
    stage: '',
    sitFlag: ''
  };
}

export function addOrderModel() {
  return {
    mainTaskName: '',
    planSitDate: '',
    planUatDate: '',
    planProDate: '',
    demandName: '',
    testers: [],
    workManager: '',
    groupLeader: [],
    remark: '',
    groupId: ''
  };
}

export function updateOrderModel() {
  return {
    mainTaskName: '',
    mainTaskNo: '',
    demandNo: '',
    demandName: '',
    workOrderNo: '',
    stage: '',
    workManager: '',
    groupLeader: [],
    testers: [],
    planSitDate: '',
    planUatDate: '',
    planProDate: '',
    uatSubmitDate: '',
    riskDescription: [],
    remark: '',
    approver: [],
    groupId: ''
  };
}

export function detailOrder() {
  return {
    mainTaskName: '',
    mainTaskNo: '',
    demandNo: '',
    demandName: '',
    workOrderNo: '',
    stage: '',
    workManager: '',
    groupLeader: '',
    testers: '',
    planSitDate: '',
    planUatDate: '',
    planProDate: '',
    remark: '',
    approver: '',
    orderType: ''
  };
}

export function assignOrderModel() {
  return {
    groupLeader: [],
    testers: [],
    groupName: ''
  };
}

export function addTestPlanModel() {
  return {
    planStartDate: '',
    planEndDate: '',
    planName: '',
    deviceInfo: '',
    workNo: ''
  };
}

export function revertOrderModel() {
  return {
    taskNo: '',
    reason: 1,
    detailInfo: '',
    workNo: ''
  };
}

export const rules = {
  testerNames: [{ required: true, message: '请输入测试人员', trigger: 'blur' }],
  splitTaskName: [
    { required: true, message: '请输入新工单名', trigger: 'blur' }
  ],
  taskNo: [{ required: true, message: '请输入任务', trigger: 'blur' }],
  taskName: [
    { required: true, message: '请输入测试任务名称', trigger: 'blur' }
  ],
  sitDate: [
    { required: true, message: '请选择计划SIT开始时间', trigger: 'blur' }
  ],
  uatDate: [
    { required: true, message: '请选择计划UAT开始时间', trigger: 'blur' }
  ],
  proDate: [{ required: true, message: '请选择计划投产时间', trigger: 'blur' }],
  /* unit: [
    {
      required: true,
      message: '请输入需求编号/实施单元编号',
      trigger: 'blur'
    }
  ], */
  manager: [{ required: true, message: '请选择负责人', trigger: 'change' }],
  planUatDate: [
    { required: true, message: '请选择计划UAT开始时间', trigger: 'blur' }
  ],
  planProDate: [
    { required: true, message: '请选择计划投产时间', trigger: 'blur' }
  ],
  approver: [{ required: true, message: '请选择审批人员', trigger: 'change' }],
  workManager: [
    { required: true, message: '请选择工单负责人', trigger: 'change' }
  ],
  riskDescription: [
    {
      type: 'array',
      required: true,
      message: '请至少选择一个含风险的原因',
      trigger: 'change'
    }
  ],
  mainTaskName: [
    { required: true, message: '请输入测试任务名称', trigger: 'blur' }
  ],
  groupLeader: [
    { required: true, message: '请选择小组负责人', trigger: 'change' }
  ],
  planSitDate: [
    { required: true, message: '请选择计划SIT开始时间', trigger: 'blur' }
  ],
  testers: [{ required: true, message: '请选择测试人员', trigger: 'change' }],
  concatTaskName: [
    { required: true, message: '请输入工单名', trigger: 'blur' }
  ],
  worksNo: [{ required: true, message: '请选择待合并工单', trigger: 'blur' }],
  stage: [{ required: true, message: '请选择测试阶段', trigger: 'change' }],
  groupName: [{ required: true, message: '请选择所属小组', trigger: 'change' }],
  groupId: [{ required: true, message: '请选择所属小组', trigger: 'change' }],
  uatSubmitDate: [
    { required: true, message: '请选择内部测试完成时间', trigger: 'blur' }
  ],
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  planStartDate: [
    { required: true, message: '请选择开始时间', trigger: 'blur' }
  ],
  planEndDate: [{ required: true, message: '请选择结束时间', trigger: 'blur' }],
  deviceInfo: [{ required: true, message: '请输入设备信息', trigger: 'blur' }],
  reason: [{ required: true, message: '请选择打回原因', trigger: 'change' }]
};
