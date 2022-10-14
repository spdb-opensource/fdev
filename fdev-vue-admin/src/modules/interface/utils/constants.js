// 表单数据
export function createInterfaceModel() {
  return {
    id: '',
    serviceCalling: '',
    url: '',
    requestType: '',
    requestProtocol: '',
    interfaceDesp: '',
    serviceConsumer: '',
    transId: '',
    serviceId: '',
    transOrServiceOrOperation: ''
  };
}

export function formData(params) {}

export function scanInterfaceModel() {
  return {
    name_en: '',
    branchName: '',
    type: '9'
  };
}

export const scanWebList = [
  {
    label: '扫描接口',
    value: '9'
  },
  {
    label: '扫描交易',
    value: '0'
  },
  {
    label: '全部扫描',
    value: '90'
  }
];

export const scanCliList = [
  {
    label: '扫描路由',
    value: '9'
  },
  {
    label: '扫描交易',
    value: '0'
  },
  {
    label: '全部扫描',
    value: '90'
  }
];

// 介质环境列表
export const mediaEnvColumns = [
  {
    name: 'env',
    label: '环境',
    field: 'env'
  },
  {
    name: 'datTime',
    label: '介质生成时间',
    field: 'datTime'
  },
  {
    name: 'appNum',
    label: '包含项目',
    field: 'appNum'
  },
  {
    name: 'totalTarName',
    label: '介质',
    field: 'totalTarName'
  }
];

// 介质提交历史记录列表
export const mediaPublishHistoryRecordColumns = [
  {
    name: 'branch',
    label: '分支',
    field: 'branch'
  },
  {
    name: 'cicdTime',
    label: 'CI/CD时间',
    field: 'cicdTime'
  },
  {
    name: 'opt',
    label: 'repo.json',
    field: 'opt'
  }
];

export const typeOptions = [
  { label: 'REST', value: 'REST' },
  { label: 'SOAP', value: 'SOAP' }
];

export const relationTypeOptions = [
  { label: '调用报文类型', value: '调用报文类型' },
  { label: 'REST', value: 'REST' },
  { label: 'SOAP', value: 'SOAP' },
  { label: 'SOP', value: 'SOP' }
];

export function transModel() {
  return {
    transId: '',
    transName: '',
    serviceId: '',
    branch: '',
    channel: '',
    writeJnl: '',
    needLogin: '',
    verCodeType: 'all',
    tags: '',
    branchDefault: 'master'
  };
}

export function invokeModel() {
  return {
    transId: '',
    serviceCalling: '', // 服务调用方
    serviceId: '', // 服务提供方
    branch: '',
    channel: ''
  };
}

export const channel = [
  { label: '全部', value: '' },
  { label: 'html', value: 'html' },
  { label: 'client', value: 'client' },
  { label: 'http', value: 'http' },
  { label: 'ajson', value: 'ajson' }
];

export const transFilter = {
  '1': '是',
  '0': '否',
  VerCode: '图片验证码',
  Jy: '极验',
  JyAndVerCode: '降级处理'
};

export const dataFilter = {
  '0': '否',
  '1': '是'
};

export const verCodeTypeOptions = [
  { label: '全部', value: 'all' },
  { label: 'VerCode', value: 'VerCode' },
  { label: 'Jy', value: 'Jy' },
  { label: 'JyAndVerCode', value: 'JyAndVerCode' }
];

export const typeList = [
  { label: '全部', value: '' },
  { label: '手动', value: '手动' },
  { label: '自动', value: '自动' }
];

export function columns() {
  return [
    {
      name: 'transId',
      label: '交易码',
      field: 'transId'
    },
    {
      name: 'interfaceName',
      label: '接口名称',
      field: 'interfaceName'
    },
    {
      name: 'serviceId',
      label: '提供方',
      field: 'serviceId'
    },
    {
      name: 'serviceCalling',
      label: '调用方',
      field: 'serviceCalling'
    },
    {
      name: 'branch',
      label: '调用方分支',
      field: 'branch'
    },
    {
      name: 'interfaceType',
      label: '调用报文类型',
      field: 'interfaceType'
    },
    {
      name: 'uri',
      label: '请求地址',
      field: 'uri'
    },
    {
      name: 'esbOperationId',
      label: '操作ID',
      field: 'esbOperationId'
    },
    {
      name: 'esbServiceId',
      label: '服务ID',
      field: 'esbServiceId'
    },
    {
      name: 'interfaceAlias',
      label: '接口别名',
      field: 'interfaceAlias'
    }
  ];
}

export function interfaceColumns() {
  return [
    {
      name: 'transId',
      label: '交易码',
      field: 'transId'
    },
    {
      name: 'interfaceName',
      label: '接口名称',
      field: 'interfaceName'
    },
    {
      name: 'serviceId',
      label: '提供方',
      field: 'serviceId'
    },
    {
      name: 'branch',
      label: '分支',
      field: 'branch'
    },
    {
      name: 'interfaceType',
      label: '提供报文类型',
      field: 'interfaceType'
    },
    {
      name: 'requestType',
      label: '请求报文类型',
      field: 'requestType'
    },
    {
      name: 'esbServiceId',
      label: '服务ID',
      field: 'esbServiceId'
    },
    {
      name: 'esbOperationId',
      label: '操作ID',
      field: 'esbOperationId'
    },
    {
      name: 'interfaceAlias',
      label: '接口别名',
      field: 'interfaceAlias'
    },
    {
      name: 'description',
      label: '接口描述',
      field: 'description'
    }
  ];
}

export function createApplyCallModel() {
  return {
    transId: '',
    serviceCalling: null,
    serviceId: '',
    applicant: '',
    reason: ''
  };
}

export function interfaceApplyColumns() {
  return [
    {
      name: 'transId',
      label: '交易码',
      field: 'transId'
    },
    {
      name: 'serviceCalling',
      label: '调用方',
      field: 'serviceCalling'
    },
    {
      name: 'serviceId',
      label: '提供方',
      field: 'serviceId'
    },
    {
      name: 'applicant',
      label: '申请人',
      field: 'applicant'
    },
    {
      name: 'approver',
      label: '审批人',
      field: 'approver'
    },
    {
      name: 'reason',
      label: '申请理由',
      field: 'reason'
    },
    {
      name: 'refuseReason',
      label: '拒绝理由',
      field: 'refuseReason'
    },
    {
      name: 'provideDevManagers',
      label: '提供方负责人',
      field: 'provideDevManagers'
    },
    {
      name: 'provideSpdbManagers',
      label: '提供方行内应用负责人',
      field: 'provideSpdbManagers'
    },
    {
      name: 'status',
      label: '审批状态',
      field: 'status'
    }
  ];
}

//接口统计列表查询页面
// 调用方应用名和提供方应用名
export const serviceName = [
  'interface',
  'envconfig',
  'release',
  'app',
  'component',
  'docmanage',
  'rqrmnt',
  'task',
  'notify',
  'user',
  'webhook'
];
export function createRouteApprovalModel() {
  return {
    name: '',
    module: '',
    ver: null,
    routerBranch: '',
    projectName: ''
  };
}

export function yapiColumns() {
  return {
    columns: [
      {
        name: 'project_name',
        field: 'project_name',
        label: '项目名称'
      },
      {
        name: 'import_user',
        field: 'import_user',
        label: '录入人'
      },
      {
        name: 'project_id',
        field: 'project_id',
        label: 'yapi project_id'
      },
      {
        name: 'yapi_token',
        field: 'yapi_token',
        label: 'yapi token'
      }
    ],
    profileColumns: [
      {
        name: 'interface_name',
        field: 'interface_name',
        label: '接口名称'
      },
      {
        name: 'interface_path',
        field: 'interface_path',
        label: '接口路径'
      },
      {
        name: 'btn',
        field: 'btn',
        label: '操作'
      }
    ]
  };
}

export function routerApplyColumns() {
  return [
    {
      name: 'name',
      label: '场景名称',
      field: 'name'
    },
    {
      name: 'module',
      label: '加载容器',
      field: 'module'
    },
    {
      name: 'projectName',
      label: '所属项目名称',
      field: 'projectName'
    },
    {
      name: 'path',
      label: '路径',
      field: 'path'
    },
    {
      name: 'ver',
      label: '版本',
      field: 'ver'
    },
    {
      name: 'branch',
      label: '分支',
      field: 'branch'
    }
  ];
}
export function routerRelationColumns() {
  return [
    {
      name: 'name',
      label: '场景名称',
      field: 'name'
    },
    {
      name: 'sourceProject',
      label: '调用项目名称',
      field: 'sourceProject'
    },
    {
      name: 'targetProject',
      label: '路由提供项目名称',
      field: 'targetProject'
    },
    {
      name: 'branch',
      label: '分支',
      field: 'branch'
    },
    {
      name: 'createTime',
      label: '录入时间',
      field: 'createTime'
    }
  ];
}

export function routerAppColumns() {
  return [
    {
      name: 'projectName',
      label: '所属项目名称',
      field: 'projectName'
    },
    {
      name: 'branch',
      label: '分支',
      field: 'branch'
    },
    {
      name: 'routeNum',
      label: '场景数',
      field: 'routeNum'
    },
    {
      name: 'routesVersion',
      label: '路由版本信息',
      field: 'routesVersion'
    },
    {
      name: 'cicdTime',
      label: 'CI/CD时间',
      field: 'cicdTime'
    },
    {
      name: 'repoTarName',
      label: '加密介质',
      field: 'repoTarName'
    }
  ];
}
export function routerTotalColumns() {
  return [
    {
      name: 'env',
      label: '环境',
      field: 'env'
    },
    {
      name: 'datTime',
      label: '介质生成时间',
      field: 'datTime'
    },
    {
      name: 'appNum',
      label: '包含项目',
      field: 'appNum'
    },
    {
      name: 'totalTarName',
      label: '介质',
      field: 'totalTarName'
    }
  ];
}

export function interfaceProfileColumns() {
  return {
    columns: [
      {
        name: 'name',
        label: '参数名',
        field: 'name',
        tooltip: true
      },
      {
        name: 'description',
        label: '参数描述',
        field: 'description',
        tooltip: true
      },
      { name: 'type', label: '参数类型', field: 'type' },
      { name: 'alias', label: '映射名', field: 'alias' },
      {
        name: 'required',
        label: '是否必填',
        field: row => row.required
      },
      {
        name: 'remark',
        label: '备注',
        field: 'remark',
        tooltip: true
      }
    ],
    headerColumns: [
      {
        name: 'name',
        label: '参数名',
        field: 'name',
        tooltip: true
      },
      {
        name: 'description',
        label: '参数描述',
        field: 'description',
        tooltip: true
      },
      { name: 'type', label: '参数类型', field: 'type' },
      {
        name: 'required',
        label: '是否必填',
        field: row => row.required
      },
      {
        name: 'maxLength',
        label: '最大长度',
        field: 'maxLength'
      },
      {
        name: 'remark',
        label: '备注',
        field: 'remark'
      }
    ]
  };
}

export function scanRecordColumns() {
  return [
    {
      name: 'serviceId',
      label: '应用英文名',
      field: 'serviceId'
    },
    {
      name: 'group',
      label: '应用所属小组',
      field: 'group',
      copy: true
    },
    {
      name: 'branch',
      label: '分支',
      field: 'branch'
    },
    {
      name: 'type',
      label: '触发扫描方式',
      field: 'type'
    },
    {
      name: 'rest',
      label: 'REST提供',
      field: 'rest'
    },
    {
      name: 'restRel',
      label: 'REST调用',
      field: 'restRel'
    },
    {
      name: 'soap',
      label: 'SOAP提供',
      field: 'soap'
    },
    {
      name: 'soapRel',
      label: 'SOAP调用',
      field: 'soapRel'
    },
    {
      name: 'sopRel',
      label: 'SOP调用',
      field: 'sopRel'
    },
    {
      name: 'trans',
      label: '交易提供',
      field: 'trans'
    },
    {
      name: 'transRel',
      label: '交易调用',
      field: 'transRel'
    },
    {
      name: 'router',
      label: '路由',
      field: 'router'
    },
    {
      name: 'scanTime',
      label: '扫描时间',
      field: 'scanTime'
    }
  ];
}

export function tradeListColumns() {
  return [
    {
      name: 'transId',
      label: '交易ID',
      field: 'transId'
    },
    {
      name: 'transName',
      label: '交易名称',
      field: 'transName',
      copy: true
    },
    {
      name: 'serviceId',
      label: '应用名',
      field: 'serviceId'
    },
    { name: 'branch', label: '分支', field: 'branch' },
    {
      name: 'channelIdMap',
      label: '渠道',
      field: 'channelIdMap'
    },
    {
      name: 'writeJnl',
      label: '是否记流水',
      field: prop => dataFilter[prop.writeJnl]
    },
    {
      name: 'needLogin',
      label: '是否登陆',
      field: prop => dataFilter[prop.needLogin]
    },
    {
      name: 'verCodeType',
      label: '图片验证码',
      field: prop => (prop.verCodeType ? prop.verCodeType : '-')
    },
    { name: 'tag', label: '标签', field: 'tag' }
  ];
}

export function tradeProfileColumns() {
  return {
    columns: [
      {
        name: 'name',
        label: '参数名',
        field: 'name',
        tooltip: true
      },
      {
        name: 'content',
        label: '参数描述',
        field: 'content',
        tooltip: true
      },
      { name: 'type', label: '字段类型', field: 'type' },
      {
        name: 'option',
        label: '是否必填',
        field: 'option'
      },
      {
        name: 'note',
        label: '备注',
        field: row => (row.note ? row.note : '-'),
        tooltip: true
      }
    ],
    headerColumns: [
      {
        name: 'name',
        label: '参数名',
        field: 'name',
        tooltip: true
      },
      {
        name: 'content',
        label: '参数描述',
        field: 'content',
        tooltip: true
      },
      { name: 'type', label: '参数类型', field: 'type' },
      {
        name: 'option',
        label: '是否必填',
        field: 'option'
      },
      { name: 'length', label: '字段长度', field: 'length' },
      {
        name: 'note',
        label: '备注',
        field: row => (row.note ? row.note : '-')
      }
    ]
  };
}

export function tradeRelationColumns() {
  return [
    {
      name: 'transId',
      label: '交易ID',
      field: 'transId'
    },
    {
      name: 'transName',
      label: '交易名称',
      field: 'transName'
    },
    {
      name: 'serviceId',
      label: '服务方',
      field: 'serviceId'
    },
    {
      name: 'serviceCalling',
      label: '调用方',
      field: 'serviceCalling'
    },
    {
      name: 'branch',
      label: '调用方分支',
      field: 'branch'
    },
    { name: 'channel', label: '渠道', field: 'channel' }
  ];
}
