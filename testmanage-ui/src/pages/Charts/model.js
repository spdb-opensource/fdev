export const orderOptions = [
  { label: '日', value: 'day' },
  { label: '周', value: 'week' },
  { label: '月', value: 'month' }
];

export const rules = {
  date: [{ required: true, message: '请输入时间范围', trigger: 'blur' }]
};
export function searchOrder() {
  return {
    date: ''
  };
}

export const QualityTableData = {
  TimelyRateList: {
    //提测详情列表
    type: 0,
    title: '提测详情列表',
    list: [
      { label: '任务名', prop: 'taskName' },
      { label: '工单名', prop: 'orderName' },
      { label: '工单计划sit时间', prop: 'planSitDate' },
      { label: '行内负责人', prop: 'spdb_master' },
      { label: '负责人', prop: 'master' },
      { label: '开发人员', prop: 'developer' },
      { label: '所属组', prop: 'groupName' },
      { label: '任务阶段', prop: 'stage' },
      { label: '首次提测', prop: 'realSitTime' },
      { label: '是否准时', prop: 'timely' }
    ]
  },
  SmokeRateList: {
    //打回列表
    type: 1,
    title: '打回列表',
    list: [
      { label: '工单名', prop: 'orderName' },
      { label: '工单计划sit时间', prop: 'planSitDate' },
      { label: '打回时间', prop: 'rollBackTime' },
      { label: '打回原因', prop: 'reason' },
      { label: '打回人', prop: 'rollbackOpr' },
      { label: '任务名', prop: 'taskName' },
      { label: '行内负责人', prop: 'spdb_master' },
      { label: '负责人', prop: 'master' },
      { label: '开发人员', prop: 'developer' },
      { label: '所属组', prop: 'groupName' },
      { label: '任务阶段', prop: 'stage' }
    ]
  },
  ReopenRateList: {
    //缺陷重开列表
    type: 2,
    title: '缺陷重开列表',
    list: [
      { label: '缺陷id', prop: 'id' },
      { label: '任务名', prop: 'taskName' },
      { label: '所属组', prop: 'fdevGroupId' },
      { label: '缺陷摘要', prop: 'summary' },
      { label: '缺陷状态', prop: 'status' },
      { label: '缺陷提出人', prop: 'reporterName' },
      { label: '开发责任人', prop: 'developer' },
      { label: '分派人', prop: 'handlerName' },
      { label: '重开次数', prop: 'openTimes' }
    ]
  },
  NormalAvgList: {
    //缺陷解决时长列表
    type: 3,
    title: '缺陷解决时长列表',
    list: [
      { label: '缺陷id', prop: 'id' },
      { label: '任务名', prop: 'taskName' },
      { label: '所属组', prop: 'fdevGroupId' },
      { label: '缺陷摘要', prop: 'summary' },
      { label: '缺陷状态', prop: 'status' },
      { label: '缺陷提出人', prop: 'reporterName' },
      { label: '开发责任人', prop: 'developer' },
      { label: '分派人', prop: 'handlerName' },
      { label: '解决时长', prop: 'solveTime' }
    ]
  },
  SevAvgList: {
    //严重缺陷解决时长列表
    type: 4,
    title: '严重缺陷解决时长列表',
    list: [
      { label: '缺陷id', prop: 'id' },
      { label: '任务名', prop: 'taskName' },
      { label: '所属组', prop: 'fdevGroupId' },
      { label: '缺陷摘要', prop: 'summary' },
      { label: '缺陷状态', prop: 'status' },
      { label: '严重度', prop: 'severity' },
      { label: '优先级', prop: 'priority' },
      { label: '缺陷提出人', prop: 'reporterName' },
      { label: '开发责任人', prop: 'developer' },
      { label: '分派人', prop: 'handlerName' },
      { label: '解决时长', prop: 'solveTime' }
    ]
  }
};
