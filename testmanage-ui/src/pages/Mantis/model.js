export function listModel() {
  return {
    start_time: '',
    end_time: '',
    module: '',
    responsible_type: '3',
    responsible_name_en: '',
    deal_status: '',
    issue_level: '',
    isIncludeChildren: false
  };
}

export const mantisTypeOptions = [
  { label: '一次性修复缺陷', value: '1' },
  { label: '重复打开缺陷', value: '2' }
];

export const dealStatusOptions = ['未修复', '修复中', '修复完成'];

export const issueLevelOptions = [
  { label: '流程规范性错误', value: '1' },
  { label: '一般错误', value: '2' },
  { label: '复杂错误', value: '3' }
];

export const optionDT = [
  { value: '需求问题', label: '需求问题' },
  { value: '环境问题', label: '环境问题' },
  { value: '数据问题', label: '数据问题' },
  { value: '文档问题', label: '文档问题' },
  { value: '应用缺陷', label: '应用缺陷' },
  { value: '兼容性异常', label: '兼容性异常' },
  { value: '其他问题', label: '其他问题' }
];
export const optionDTSA = [{ value: '安全缺陷', label: '安全缺陷' }];

export const optionBS = [
  { value: '编码阶段', label: '编码阶段' },
  { value: '集成阶段', label: '集成阶段' },
  { value: '设计阶段', label: '设计阶段' },
  { value: '需求阶段', label: '需求阶段' }
];
export const optionStatusQuery = [
  { value: '10', label: '新建' },
  { value: '20', label: '拒绝' },
  { value: '30', label: '确认拒绝' },
  { value: '40', label: '延迟修复' },
  { value: '50', label: '打开' },
  { value: '80', label: '已修复' },
  { value: '90', label: '关闭' }
];
export const optionSV = [
  { value: '内测版本', label: '内测版本' },
  { value: '业务版本', label: '业务版本' },
  { value: '准生产版本', label: '准生产版本' },
  { value: '灰度版本', label: '灰度版本' },
  { value: '生产版本', label: '生产版本' }
];
export const optionPriority = [
  { value: '无', label: '10' },
  { value: '低', label: '20' },
  { value: '中', label: '30' },
  { value: '高', label: '40' },
  { value: '紧急', label: '50' },
  { value: '非常紧急', label: '60' }
];
export const optionSeverity = [
  { value: '新功能', label: '10' },
  { value: '细节', label: '20' },
  { value: '文字', label: '30' },
  { value: '小调整', label: '40' },
  { value: '小错误', label: '50' },
  { value: '很严重', label: '60' },
  { value: '崩溃', label: '70' },
  { value: '宕机', label: '80' }
];
export const optionDS = [
  { value: '业务需求问题', label: '业务需求问题' },
  { value: '需规问题', label: '需规问题' },
  { value: '功能实现不完整', label: '功能实现不完整' },
  { value: '兼容性异常', label: '兼容性异常' },
  { value: '功能实现错误', label: '功能实现错误' },
  { value: '历史遗留问题', label: '历史遗留问题' },
  { value: '优化建议', label: '优化建议' },
  { value: '后台问题', label: '后台问题' },
  { value: '打包问题', label: '打包问题' },
  { value: '数据问题', label: '数据问题' },
  { value: '环境问题', label: '环境问题' },
  { value: '其他原因', label: '其他原因' }
];
// 缺陷新增
export const optionDSA = [
  { value: '水平越权漏洞', label: '水平越权漏洞' },
  { value: '重放攻击漏洞', label: '重放攻击漏洞' },
  { value: '安全功能设计机制不健全', label: '安全功能设计机制不健全' }
];
//缺陷编辑
export const securityDefectFrom = [
  { value: '水平越权漏洞', label: '水平越权漏洞' },
  { value: '重放攻击漏洞', label: '重放攻击漏洞' },
  { value: '安全功能设计机制不健全', label: '安全功能设计机制不健全' },
  { value: '其他原因', label: '其他原因' }
];
export const optionStatus = [
  { value: '10', label: '新建' },
  { value: '20', label: '拒绝' },
  { value: '30', label: '确认拒绝' },
  { value: '40', label: '延迟修复' },
  { value: '50', label: '打开' },
  { value: '80', label: '已修复' },
  { value: '90', label: '关闭' }
];
export const optionStatusEdit = [
  { value: '10', label: '新建', disabled: true },
  { value: '20', label: '拒绝', disabled: true },
  { value: '30', label: '确认拒绝' },
  { value: '40', label: '延迟修复', disabled: true },
  { value: '50', label: '打开' },
  { value: '80', label: '已修复', disabled: true },
  { value: '90', label: '关闭' }
];

export const mantisDialogRules = {
  project_id: [{ required: true, message: '请选择项目名称', trigger: 'blur' }],
  system_version: [
    { required: true, message: '请选择系统版本', trigger: 'blur' }
  ],
  stage: [{ required: true, message: '请选择归属阶段', trigger: 'blur' }],
  handler: [{ required: true, message: '请输入分派人员', trigger: 'blur' }],
  handler_en_name: [
    { required: true, message: '请输入分派人员', trigger: 'blur' }
  ],
  developer: [
    { required: true, message: '请输入开发责任人', trigger: 'change' }
  ],
  priority: [{ required: true, message: '请选择优先级', trigger: 'blur' }],
  severity: [{ required: true, message: '请选择严重性', trigger: 'blur' }],
  flaw_source: [{ required: true, message: '请选择缺陷来源', trigger: 'blur' }],
  plan_fix_date: [
    { required: true, message: '请选择预计修复时间', trigger: 'blur' }
  ],
  flaw_type: [{ required: true, message: '请选择缺陷类型', trigger: 'blur' }],
  id: [{ required: true, message: '请输入缺陷编号', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'blur' }],
  summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  reopen_reason: [
    { required: true, message: '请输入重新打开缺陷原因', trigger: 'blur' }
  ]
};
