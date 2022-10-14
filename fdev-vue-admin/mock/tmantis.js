const mockjs = require('mockjs');
const list = mockjs.mock({
  'array|10': [
    {
      problem_phenomenon: '问题描述',
      requirement_name: '需求名称',
      occurred_time: '@date()',
      issue_type: '代码审核',
      is_trigger_issue: '@cword("是否", 1)',
      severity: '@cword("是否", 1)',
      deal_status: '@cword("是否", 1)'
    }
  ]
});
const proProblemModel = {
  audit_responsible: ['王五'],
  backlog_schedule_list: [
    {
      backlog_schedule: '',
      backlog_schedule_complete_percentage: '',
      backlog_schedule_complete_time: '',
      backlog_schedule_current_completion: '',
      backlog_schedule_reviewer: ''
    }
  ],
  deal_status: '修复完成',
  dev_responsible: ['xxx', 'xxx'],
  discover_stage: '生产重发',
  files: [],
  improvement_measures: '改进措施1',
  influence_area:
    '缴费项目，经过我的账户，然后进入缴费后，页面卡列表加载不完整，致使交易不通过',
  is_gray_replication: '是',
  is_involve_urgency: '否',
  is_rel_replication: '是',
  is_trigger_issue: '是',
  is_uat_replication: '是',
  issue_level: '复杂错误',
  issue_reason: '人员粗心，测试不全面',
  issue_type: [],
  module: '移动组',
  occurred_time: '2020/05/11',
  problem_phenomenon:
    '缴费项目，经过我的账户，然后进入缴费后，页面卡列表加载不完整，致使交易不通过',
  remark: '无',
  requirement_name: '测试123',
  responsibility_list: [
    {
      responsible: 'xxx',
      responsibility_type: '其他',
      responsibility_content: '问责内容1'
    },

    {
      responsible: 'xxx',
      responsibility_type: '其他',
      responsibility_content: '问责内容2'
    }
  ],
  reviewer_comment: '',
  reviewer_status: '未评审',
  task_no: '5ea530a6933e140012961556',
  task_responsible: ['xxx'],
  test_responsible: []
};

const issueFilesDate = [
  {
    content: 'xxx',
    file_id: 4,
    file_type: 'image/png; charset=binary',
    filesize: 73019,
    id: 5,
    name: '你的工单.PNG'
  },
  {
    content: '',
    file_id: 6,
    file_type: 'image/png; charset=binary',
    filesize: 73019,
    id: 7,
    name: '测试工单.PNG'
  }
];
module.exports = {
  'POST /tmantis/proIssue/queryUserProIssues': list.array,
  'POST /tmantis/proIssue/queryProIssueById': mockjs.mock(proProblemModel),
  'POST /tmantis/proIssue/queryIssueFiles': mockjs.mock(issueFilesDate)
};
