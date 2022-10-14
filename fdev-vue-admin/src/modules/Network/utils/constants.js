export function kfApprovalColumns() {
  return [
    {
      name: 'applicant',
      label: '申请人',
      field: row => row.applicant.user_name_cn,
      align: 'left'
    },
    {
      name: 'type',
      label: '申请类型',
      field: 'type',
      align: 'left'
    },
    {
      name: 'user',
      label: '使用人',
      field: row => row.user.user_name_cn,
      align: 'left'
    },
    {
      name: 'status',
      label: '申请状态',
      field: 'status',
      align: 'left'
    },
    {
      name: 'group',
      label: '所属小组',
      field: row => row.user.group.name,
      align: 'left',
      copy: true
    },
    {
      name: 'is_spdb',
      label: '使用人员类别',
      field: row => row.user.is_spdb,
      align: 'left'
    },
    {
      name: 'company',
      label: '使用人公司',
      field: row => row.user.company.name,
      align: 'left'
    },
    {
      name: 'vm_user_name',
      label: '使用人域用户',
      field: row => row.user.vm_user_name,
      align: 'left',
      copy: true
    },
    {
      name: 'phone_type',
      label: '源服务器名称',
      field: 'phone_type',
      align: 'left'
    },
    {
      name: 'phone_mac',
      label: 'MAC地址',
      field: 'phone_mac',
      align: 'left',
      copy: true
    },
    {
      name: 'purpose',
      label: '设备用途',
      align: 'left'
    },
    {
      name: 'create_time',
      label: '申请日期',
      field: 'create_time',
      align: 'left'
    }
  ];
}

export function codeProblemsColumns() {
  return [
    {
      name: 'problem',
      label: '问题描述/评审建议',
      field: 'problem',
      headerStyle: 'max-width: 712px',
      style: 'max-width: 712px',
      align: 'left',
      copy: true
    },
    {
      name: 'fixFlag',
      label: '是否已修复',
      field: 'fixFlag',
      align: 'left',
      copy: true
    },
    {
      name: 'fixDate',
      label: '修复日期',
      field: 'fixDate',
      align: 'left'
    },
    {
      name: 'problemType',
      label: '问题类型',
      field: 'type',
      align: 'left'
    },
    {
      name: 'itemTypeValue',
      label: '问题项',
      field: 'itemTypeValue',
      align: 'left'
    },
    {
      name: 'problemNum',
      label: '问题次数',
      field: 'problemNum',
      align: 'left'
    },
    {
      name: 'remark',
      label: '备注',
      field: 'remark',
      align: 'left'
    },
    {
      name: 'operation',
      label: '操作',
      field: 'operation',
      align: 'left',
      required: true
    }
  ];
}

export function vdiColumns() {
  return [
    {
      name: 'user',
      label: '使用人',
      field: row => row.user.user_name_cn,
      align: 'left'
    },
    {
      name: 'vm_user_name',
      label: 'VDI用户名',
      field: 'vm_user_name',
      align: 'left',
      copy: true
    },
    {
      name: 'vm_ip',
      label: 'IP',
      field: 'vm_ip',
      align: 'left'
    },
    {
      name: 'status',
      label: '申请状态',
      field: 'status',
      align: 'left'
    },
    {
      name: 'netSegment',
      label: '申请迁入网段',
      align: 'left'
    },
    {
      name: 'applicant',
      label: '策略需求联系人',
      field: row => row.applicant.user_name_cn,
      align: 'left'
    },
    {
      name: 'create_time',
      label: '申请日期',
      field: 'create_time',
      align: 'left'
    },
    {
      name: 'projectGroup',
      label: '项目组',
      align: 'left'
    }
  ];
}

export function vmColumns() {
  return [
    {
      name: 'user',
      label: '使用人',
      field: row => row.user.user_name_cn,
      align: 'left'
    },
    {
      name: 'vm_user_name',
      label: '虚机用户名',
      field: 'vm_user_name',
      align: 'left'
    },
    {
      name: 'vm_name',
      label: '虚机名',
      field: 'vm_name',
      align: 'left'
    },
    {
      name: 'vm_ip',
      label: '虚机IP',
      field: 'vm_ip',
      align: 'left'
    },
    {
      name: 'status',
      label: '申请状态',
      field: 'status',
      align: 'left'
    },
    {
      name: 'applicant',
      label: '策略需求联系人',
      field: row => row.applicant.user_name_cn,
      align: 'left'
    },
    {
      name: 'create_time',
      label: '申请日期',
      field: 'create_time',
      align: 'left'
    },
    {
      name: 'projectGroup',
      label: '项目组',
      align: 'left'
    }
  ];
}

export function codeIssueColumns() {
  return [
    {
      name: 'problem',
      label: '问题描述',
      field: 'problem',
      align: 'left',
      headerStyle: 'max-width: 712px',
      style: 'max-width: 712px',
      copy: true
    },
    {
      name: 'orderNo',
      label: '工单编号',
      field: 'orderNo',
      align: 'left'
    },
    {
      name: 'orderStatus',
      label: '工单状态',
      field: 'orderStatus',
      align: 'left'
    },
    {
      name: 'demandName',
      label: '需求名称',
      field: 'demandName',
      align: 'left'
    },
    {
      name: 'meetingTime',
      label: '会议时间',
      field: 'meetingTime',
      align: 'left'
    },
    {
      name: 'fixFlag',
      label: '是否已修复',
      field: 'fixFlag',
      align: 'left'
    },
    {
      name: 'fixDate',
      label: '修复日期',
      field: 'fixDate',
      align: 'left'
    },
    {
      name: 'problemType',
      label: '问题类型',
      field: 'problemType',
      align: 'left'
    },
    {
      name: 'itemTypeValue',
      label: '问题项',
      field: 'itemTypeValue',
      align: 'left'
    },
    {
      name: 'problemNum',
      label: '问题次数',
      field: 'problemNum',
      align: 'left'
    },
    {
      name: 'createTime',
      label: '创建时间',
      field: 'createTime',
      align: 'left'
    },
    {
      name: 'applyTime',
      label: '申请时间',
      field: 'applyTime',
      align: 'left'
    },
    {
      name: 'leaderGroupCn',
      label: '牵头小组',
      field: 'leaderGroupCn',
      align: 'left'
    },
    {
      name: 'leaderName',
      label: '牵头人',
      field: 'leaderName',
      align: 'left'
    },
    {
      name: 'auditFinishDate',
      label: '审核完成日期',
      field: 'auditFinishDate',
      align: 'left'
    },
    {
      name: 'auditorUsersCn',
      label: '审核人',
      field: 'auditorUsersCn',
      align: 'left'
    },
    {
      name: 'remark',
      label: '备注',
      field: 'remark',
      align: 'left'
    }
  ];
}

export const typeApply = {
  kf_approval: 'KF网络开通审核',
  kf_off_approval: 'KF网络关闭审核',
  vdi_approva: 'VDI网段迁移',
  vm_approval: '虚拟机网段迁移',
  kf_off_batch_approval: '网络关闭提醒'
};

export const typeStatus = {
  wait_approve: '待审核',
  passed: '审核通过',
  refused: '审核拒绝'
};

export const perform = {
  visibleColumnCodeToolOptions: [
    'orderNo',
    'orderStatus',
    'demandName',
    'leaderGroupCn',
    'leaderName',
    'systemNames',
    'applyTime',
    'auditFinishTime',
    'planProductDate'
  ]
};

//在职浦发人员不分页查询条件
export const queryUserOptionsParams = {
  company_id: '5c452d6fd3e2a12d806e0dkl',
  status: '0',
  page: 1,
  per_page: 0
};

export const priorityOptions = [
  { label: '是', value: '0' },
  { label: '否', value: '1' }
];

export const meetingTypeOptions = [
  { label: '初审', value: 'firstCheck' },
  { label: '复审', value: 'reCheck' }
];

export function createCodeToolListModel() {
  return {
    oa_contact_name: '', //需求名称
    oa_contact_no: '', //需求编号
    demand_leader_group: '', //牵头小组
    demand_leader: [], //牵头人员
    priority: '', //优先级
    start_assess_date: '', //起始评估日期
    assess_present: '', //评估现状
    overdue_type: '', //超期分类
    meetingTime: '', //会议时间
    address: '', //会议地点
    meetingType: '', //会议类型
    auditeUsers: [], //审核人
    beauditeUsers: [], //被审核人
    orderId: ''
  };
}

export const tabsList = [
  {
    name: 'baseInfo',
    label: '工单信息'
  },
  {
    name: 'netApproval',
    label: '会议记录'
  },
  {
    name: 'file',
    label: '文档库'
  },
  {
    name: 'logs',
    label: '日志'
  }
];

export function createFileModel() {
  return {
    volume: '',
    fileroot: '',
    file: null,
    filename: '',
    filetype: [],
    rqrmntId: ''
  };
}

export const fileTypes = [
  { label: '需求规格说明书', value: 'demandInstruction' },
  { label: '代码审核表', value: 'techPlan' },
  { label: '原型图', value: 'demandReviewResult' },
  { label: '接口文件', value: 'meetMinutes' },
  { label: '数据库文件', value: 'otherRelatedFile' }
];

export const statusOptions = [
  { label: '待审核', value: 1 },
  { label: '审核中', value: 2 },
  { label: '需线下复审', value: 3 },
  { label: '需会议复审', value: 4 },
  { label: '初审通过', value: 5 },
  { label: '线下复审通过', value: 6 },
  { label: '会议复审通过', value: 7 },
  { label: '拒绝', value: 8 }
];

export function createOrdersModel() {
  return {
    demandId: '', //对应需求
    leader: '', //牵头人
    emailTo: [], // 邮件通知人
    leaderGroup: '', //牵头小组
    planProductDate: '', //计划投产时间
    expectAuditDate: '', //期望审核时间
    taskIds: [], //涉及任务
    temTaskValue: '',
    code_files: [], //代码审核表
    req_files: [], //需求规格说明书
    deInstruction_files: [], //需求说明书
    prototype_files: [], //原型图
    orderStatus: '', //工单状态
    auditContent: '', //审核内容
    auditResult: '', //审核结论
    productProblem: '' //投产问题描述
  };
}

export function createProblemItemModel() {
  return {
    problem: '', //描述
    problemType: '', //问题类型
    itemType: '', //问题项
    problemNum: '', //问题次数
    fixFlag: '', //是否修复
    fixDate: '', //修复日期
    remark: '' //备注
  };
}

export const dateTypeList = [
  { label: '申请时间', value: 'applyTime' },
  { label: '会议时间', value: 'meetingTime' },
  { label: '计划投产日期', value: 'planProductDate' },
  { label: '实际投产日期', value: 'realProductDate' },
  { label: '审核完成时间', value: 'auditFinishTime' },
  { label: '期望审核日期', value: 'expectAuditDate' }
];

export const createMemory = function() {
  return {
    dateField: null,
    startDate: '',
    endDate: ''
  };
};
export const moreSearchMap = {
  dateField: 'updateDateType',
  startDate: 'updateStartDate',
  endDate: 'updateEndDate'
};

export const toolTabsList = [
  {
    name: 'total',
    label: '全部'
  },
  {
    name: 'office',
    label: '办公'
  },
  {
    name: 'development',
    label: '开发'
  },
  {
    name: 'learn',
    label: '学习'
  },
  {
    name: 'community',
    label: '社区'
  }
];
export const websiteList = {
  total: [
    {
      name: ' 智慧办公系统',
      url: 'http://ia.spdb.com/Portal/Default.aspx',
      iconEn: 'smartoffice',
      tooltip: '只能在办公机访问'
    },
    {
      name: '集团人力资源门户',
      url: 'http://hr.spdb.com/psp/HCMPRD/EMPLOYEE/HRMS/h/?tab=DEFAULT',
      iconEn: 'HR',
      tooltip: '只能在办公机访问'
    },
    {
      name: '会议室预定',
      url:
        'http://meeting.spdb.com:8081/meetingRoomOrder/index.html#/home/techIndex',
      iconEn: 'meetingoder',
      tooltip: '只能在办公机访问'
    },
    {
      name: 'IPMP',
      url: 'http://ipmp.spdb.com/ipmp/biz/ipmp.html#/home',
      iconEn: 'ipmp',
      tooltip: '只能在办公机访问'
    },
    {
      iconEn: 'iams',
      name: '挡板(iams)',
      url: 'xxx/iams/'
    },
    {
      iconEn: 'ftms',
      name: '玉衡测试管理平台(ftms)',
      url: 'xxx/tui/'
    },
    {
      name: '缺陷管理工具(JIRA)',
      url: 'xxx/login.jsp',
      iconEn: 'jira'
    },
    {
      name: 'gitlab',
      url: 'http://xxx/users/sign_in',
      tooltip: '只能在虚拟机内访问',
      iconEn: 'gitlab'
    },

    {
      name: '接口管理工具(yapi)',
      url: 'xxx/login',
      tooltip: '只能在虚拟机内访问',
      iconEn: 'yapi'
    },

    {
      name: 'ESB对外小工具',
      url: 'xxx/',
      tooltip: '只能在虚拟机内访问',
      iconEn: 'esb'
    },
    {
      name: 'mantis',
      url: 'xxx/login_page.php',
      tooltip: '只能在虚拟机内访问',
      iconEn: 'mantis'
    },
    {
      name: 'speed4J',
      url: 'xxx',
      iconEn: 'speed4j'
    },
    {
      name: 'learn lab',
      url: 'xxx',
      tooltip: '只能在虚拟机内访问',
      iconEn: 'learnlab'
    },
    {
      name: '浦银大学',
      url: 'http://univ.spdb.com/lms/app/login/login.jsp',
      tooltip: '只能在办公机访问',
      iconEn: 'pylearn'
    },
    {
      name: '乐浦',
      url: 'http://ic.spdb.com/#/home',
      tooltip: '只能在办公机访问',
      iconEn: 'lepu'
    },
    {
      name: '工程师指南',
      url: 'http://wiki.spdb.com/#/nav',
      tooltip: '只能在办公机访问',
      iconEn: 'wiki'
    },
    {
      name: '财务集约化',
      url: 'http://e-accounting.spdb.com/caiwugongxiang',
      tooltip: '只能在虚拟机外访问',
      iconEn: 'finance'
    },
    {
      name: 'mpaas 画板介绍',
      url: 'xxx/resource/index.html',
      iconEn: 'mpaas'
    },
    {
      name: 'fdev issue',
      url: 'http://xxx/ebank_fdev/fdev_issuses/-/issues',
      tooltip: '只能虚拟机内访问',
      iconEn: 'fdevIssue'
    },
    {
      name: '应用架构issue',
      url: 'http://xxx/ebank_fdev/AppArch_issues/-/issues',
      tooltip: '只能虚拟机内访问',
      iconEn: 'afIssue'
    },
    {
      name: '集成架构issue',
      url: 'http://xxx/ebank_fdev/AppArch_issues/-/issues',
      tooltip: '只能虚拟机内访问',
      iconEn: 'integrateIssue'
    },
    {
      name: 'mpaas issue',
      url: 'http://xxx/ebank/mpaas-issues/issues',
      tooltip: '只能虚拟机内访问',
      iconEn: 'mpaasIssue'
    },
    {
      name: '数字办公平台(新)',
      url: 'http://gcoa.spdb.com/',
      iconEn: 'oaNew',
      tooltip: '只能在办公机访问'
    },
    {
      name: '数字办公平台(老)',
      url: 'http://oa.spdb.com/SPDB.OA.Portal2/Page/Default.aspx',
      icon: require('@/modules/Network/assets/toolguide/oaOld.svg'),
      iconEn: 'oaOld',
      tooltip: '只能在办公机访问'
    },
    {
      name: '统一运维门户',
      url: 'http://pdos.spdb.com/o/esm_saas/#/portalIndex',
      iconEn: 'pdos',
      tooltip: '只能在办公机访问'
    },
    {
      name: '云测试平台',
      url: 'http://xxx/xtest/login.jsp',
      iconEn: 'xtest'
    },
    {
      name: '浦天开物',
      url: 'xxx/speedstudio/',
      iconEn: 'speedstudio'
    },
    {
      iconEn: 'usermanual',
      name: 'fdev用户手册',
      url: 'xxx/view/l/teu8564'
    },
    {
      iconEn: 'codesearch',
      name: '代码搜索',
      url: 'xxx',
      tooltip: '只能虚拟机内访问'
    },
    {
      iconEn: 'conflunce',
      name: 'Conflunce',
      url: 'http://conf.spdb.com:8090'
    }
  ],
  office: [
    {
      name: ' 智慧办公系统',
      url: 'http://ia.spdb.com/Portal/Default.aspx',

      iconEn: 'smartoffice',
      tooltip: '只能在办公机访问'
    },
    {
      name: '集团人力资源门户',
      url: 'http://hr.spdb.com/psp/HCMPRD/EMPLOYEE/HRMS/h/?tab=DEFAULT',

      iconEn: 'HR',
      tooltip: '只能在办公机访问'
    },
    {
      name: '会议室预定',
      url:
        'http://meeting.spdb.com:8081/meetingRoomOrder/index.html#/home/techIndex',

      iconEn: 'meetingoder',
      tooltip: '只能在办公机访问'
    },
    {
      name: 'IPMP',
      url: 'http://ipmp.spdb.com/ipmp/biz/ipmp.html#/home',

      iconEn: 'ipmp',
      tooltip: '只能在办公机访问'
    },
    {
      name: '数字办公平台(新)',
      url: 'http://http://gcoa.spdb.com/',

      iconEn: 'oaNew',
      tooltip: '只能在办公机访问'
    },
    {
      name: '数字办公平台(老)',
      url: 'http://oa.spdb.com/SPDB.OA.Portal2/Page/Default.aspx',
      iconEn: 'oaOld',
      tooltip: '只能在办公机访问'
    },
    {
      name: '统一运维门户',
      url: 'http://pdos.spdb.com/o/esm_saas/#/portalIndex',

      iconEn: 'pdos',
      tooltip: '只能在办公机访问'
    },
    {
      name: '财务集约化',
      url: 'http://e-accounting.spdb.com/caiwugongxiang',
      tooltip: '只能在虚拟机外访问',
      iconEn: 'finance'
    },
    {
      icon: '@/modules/Network/assets/toolguide/conflunce.svg',
      iconEn: 'conflunce',
      name: 'Conflunce',
      url: 'http://conf.spdb.com:8090'
    }
  ],
  development: [
    {
      iconEn: 'iams',
      name: '挡板(iams)',
      url: 'xxx/iams/'
    },
    {
      iconEn: 'ftms',
      name: '玉衡测试管理平台(ftms)',
      url: 'xxx/tui/'
    },
    {
      name: '缺陷管理工具(JIRA)',
      url: 'xxx/login.jsp',
      icon: require('@/modules/Network/assets/toolguide/jira.svg'),
      iconEn: 'jira'
    },
    {
      name: 'gitlab',
      url: 'http://xxx/users/sign_in',

      tooltip: '只能在虚拟机内访问',
      iconEn: 'gitlab'
    },

    {
      name: '接口管理工具(yapi)',
      url: 'xxx/login',

      tooltip: '只能在虚拟机内访问',
      iconEn: 'yapi'
    },

    {
      name: 'ESB对外小工具',
      url: 'xxx/',

      tooltip: '只能在虚拟机内访问',
      iconEn: 'esb'
    },
    {
      name: 'mantis',
      url: 'xxx/login_page.php',
      tooltip: '只能在虚拟机内访问',
      iconEn: 'mantis'
    },
    {
      name: 'speed4J',
      url: 'xxx',
      iconEn: 'speed4j'
    },
    {
      name: 'mpaas 画板介绍',
      url: 'xxx/resource/index.html',
      iconEn: 'mpaas'
    },
    {
      name: 'fdev issue',
      url: 'http://xxx/ebank_fdev/fdev_issuses/-/issues',
      tooltip: '只能虚拟机内访问',
      iconEn: 'fdevIssue'
    },
    {
      name: '应用架构issue',
      url: 'http://xxx/ebank_fdev/AppArch_issues/-/issues',
      tooltip: '只能虚拟机内访问',
      iconEn: 'afIssue'
    },
    {
      name: '集成架构issue',
      url: 'http://xxx/ebank_fdev/AppArch_issues/-/issues',
      tooltip: '只能虚拟机内访问',
      iconEn: 'integrateIssue'
    },
    {
      name: 'mpaas issue',
      url: 'http://xxx/ebank/mpaas-issues/issues',
      tooltip: '只能虚拟机内访问',
      iconEn: 'mpaasIssue'
    },
    {
      name: '云测试平台',
      url: 'http://xxx/xtest/login.jsp',
      iconEn: 'xtest'
    },
    {
      name: '浦天开物',
      url: 'xxx/speedstudio/',
      iconEn: 'speedstudio'
    },
    {
      icon: '@/modules/Network/assets/toolguide/codesearch.svg',
      iconEn: 'codesearch',
      name: '代码搜索',
      url: 'xxx',
      tooltip: '只能虚拟机内访问'
    }
  ],
  learn: [
    {
      name: '工程师指南',
      url: 'http://wiki.spdb.com/#/nav',
      tooltip: '只能在办公机访问',
      iconEn: 'wiki'
    },
    {
      name: 'learn lab',
      url: 'xxx',
      tooltip: '只能在虚拟机内访问',
      iconEn: 'learnlab'
    },
    {
      name: '浦银大学',
      url: 'http://univ.spdb.com/lms/app/login/login.jsp',
      tooltip: '只能在办公机访问',
      iconEn: 'pylearn'
    },
    {
      icon: '@/modules/Network/assets/toolguide/usermanual.svg',
      iconEn: 'usermanual',
      name: 'fdev用户手册',
      url: 'xxx/view/l/teu8564'
    }
  ],
  community: [
    {
      name: '乐浦',
      url: 'http://ic.spdb.com/#/home',
      tooltip: '只能在办公机访问',
      iconEn: 'lepu'
    }
  ]
};
