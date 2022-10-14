import { baseUrl } from '@/utils/utils';
import service from '@/services/serviceMap';
import { filterLabels, chineseName } from './utils';

export function createEnvModel() {
  return {
    env_name_en: '',
    env_name_cn: '',
    env_message: '',
    labels: []
  };
}

export function createModelEnv() {
  return {
    env: null,
    model: null,
    variables: [],
    desc: ''
  };
}

export function createModelItem() {
  return {
    env_key: [
      {
        name_en: '',
        name_cn: '',
        data_type: 'string',
        type: '0',
        require: '0',
        desc: '',
        flag: ''
      }
    ],
    model_template_id: '',
    name_en: '',
    name_cn: '',
    desc: '',
    version: '0.1',
    env_id: '',
    model_id: '',
    platform: '',
    first_category: '',
    second_category: '',
    suffix_name: '',
    scope: ''
  };
}

export function createModelTempItem() {
  return {
    envKey: [
      {
        propKey: '',
        propNameCn: '',
        dataType: 'string',
        type: '0',
        require: '0',
        desc: '',
        flag: ''
      }
    ],
    nameEn: '',
    nameCn: '',
    desc: '',
    env_id: '',
    model_id: ''
  };
}

export function createGreatKey() {
  return {
    key: [
      {
        type: '',
        description: '',
        required: '1'
      }
    ],
    template: '0'
  };
}
export function createModel() {
  return {
    select: '实体属性',
    range: ['master'],
    name_en: '',
    field_name_en: ''
  };
}

export function createProperty() {
  return {
    state: '',
    applicant: '',
    application: ''
  };
}

export function applyMessage() {
  return {
    email_pre: '',
    apply_username: '',
    app_name_en: null,
    email_append: 'xxx',
    modify_reason: ''
  };
}

export function applyMessageListModel() {
  return {
    appllyUser: null,
    status: '',
    model: null
  };
}

export function createModelGroup() {
  return {
    id: '',
    name: '',
    models: null
  };
}
export function deployListModel() {
  return {
    env: null,
    app: null
  };
}
export function deployProfileModel() {
  return {
    modelSetType: 'deploy',
    bizEnvTestList: [],
    bizEnvProList: [],
    dmzEnvProList: [],
    dmzEnvTestList: [],
    modelSetMsg: null,
    sccModelSetMsg: null,
    app_name_en: null,
    network: [],
    schedule_env_name: null,
    sccSitList: [],
    sccUatList: [],
    sccRelList: [],
    sccProList: [],
    scc_status: '0',
    caas_status: '0'
  };
}

export function mappingModel() {
  return {
    value: '',
    type: '',
    labels: []
  };
}

export function createConfigModel() {
  return {
    mappingValue: '',
    attributeObj: '',
    entityObj: '',
    range: []
  };
}

export const modelMessageStatus = {
  checking: '核对中',
  finished: '核对完成',
  overtime: '超期未核对',
  cancel: '已取消'
};

export const statusOptions = [
  { label: '核对中', value: 'checking' },
  { label: '核对完成', value: 'finished' },
  { label: '超期未核对', value: 'overtime' },
  { label: '已取消', value: 'cancel' }
];

export const columns = [
  {
    name: 'name_en',
    label: '属性字段',
    field: 'name_en'
  },
  {
    name: 'name_cn',
    label: '属性中文名',
    field: 'name_cn'
  },
  {
    name: 'value',
    label: '属性值',
    field: 'value'
  },
  {
    name: 'own_value',
    label: '应用独有',
    field: 'own_value'
  }
];

export const networkOptions = [
  { label: '业务', value: 'biz' },
  { label: '网银', value: 'dmz' }
];

export const perform = {
  // 应用生产信息查询条件选项
  paramsOptions: [
    { label: '应用名', value: 'deploy_name' },
    { label: '租户', value: 'namespace' },
    { label: 'CPU预留', value: 'cpu_requests' },
    { label: 'CPU限制', value: 'cpu_limits' },
    { label: '内存预留', value: 'memory_requests' },
    { label: '内存限制', value: 'memory_limits' },
    { label: 'dns策略', value: 'dnspolicy' },
    { label: '挂载点', value: 'volumemounts' },
    { label: '挂载卷', value: 'volumes' },
    { label: '环境变量', value: 'env' },
    { label: '环境变量引用', value: 'envfrom' },
    { label: '预留ip', value: 'allocated_ip_segment' }
  ],
  //查询生产信息详情 运行信息
  runTableColumns: [
    {
      name: 'name',
      label: 'pod名称',
      field: 'name',
      copy: true
    },
    {
      name: 'ip',
      label: 'IP地址',
      field: 'ip',
      copy: true
    },
    {
      name: 'hostName',
      label: '主机名称',
      field: 'hostName',
      copy: true
    }
  ],
  //查询生产信息详情 存储信息
  storgeTableColumns: [
    {
      name: 'name',
      label: '名称',
      field: 'name',
      copy: true
    },
    {
      name: 'volumemounts',
      label: '容器内挂载点',
      field: 'volumemounts',
      copy: true
    },
    {
      name: 'volumes',
      label: '存储卷',
      field: 'volumes',
      copy: true
    },
    {
      name: 'subPath',
      label: 'nas盘子路径',
      field: 'subPath',
      copy: true
    }
  ],
  //查询生产信息列表
  columns: [
    {
      name: 'deploy_name',
      label: '应用名',
      field: 'deploy_name'
    },
    {
      name: 'team',
      label: '小组',
      field: row => row.group || '-',
      copy: true
    },
    {
      name: 'spdb_managers',
      label: '行内负责人',
      field: row => chineseName(row.spdb_managers) || '-'
    },
    {
      name: 'dev_managers',
      label: '应用负责人',
      field: row => chineseName(row.dev_managers) || '-'
    },
    {
      name: 'platform',
      label: '应用所属平台',
      field: 'platform'
    }
  ]
};

export const downUrl = `${baseUrl}${service.fenvconfig.exportDependencySearchResult.slice(
  1
)}`;

export function envListColumns() {
  return [
    {
      name: 'env_name_cn',
      label: '环境中文名称',
      field: row => row.name_cn || '-'
    },
    {
      name: 'env_name_en',
      label: '环境英文名称',
      field: row => row.name_en || '-'
    },
    {
      name: 'env_labels',
      label: '环境标签',
      field: row => filterLabels(row.labels) || '-'
    },
    {
      name: 'env_message',
      label: '描述信息',
      field: 'desc'
    }
  ];
}

export function modelEnvColumns() {
  return {
    columns: [
      { name: 'model', label: '实体中文名', field: 'model' },
      {
        name: 'model_name_en',
        label: '实体英文名',
        field: 'model_name_en'
      },
      {
        name: 'env',
        label: '环境中文名',
        field: 'env',
        copy: true
      },
      {
        name: 'env_name_en',
        label: '环境英文名',
        field: row => row.env_name_en || '-',
        copy: true
      },
      {
        name: 'labels',
        label: '环境标签',
        field: row => row.labels || '-'
      },
      { name: 'desc', label: '描述', field: 'desc' },
      { name: 'btn', label: '操作' }
    ],
    keyColums: [
      { name: 'name_en', label: '属性字段', field: 'name_en' },
      {
        name: 'name_cn',
        label: '属性中文名',
        field: 'name_cn'
      },
      { name: 'value', label: '属性值', field: 'value' },
      { name: 'require', label: '是否必填', field: 'require' },
      { name: 'desc', label: '描述', field: 'desc' },
      {
        name: 'data_type',
        label: '属性类型',
        field: 'data_type'
      },
      {
        name: 'type',
        label: '是否应用独有',
        field: 'type'
      }
    ],
    grandChildColumns: [
      {
        name: 'name',
        label: '属性字段',
        field: row => row.name || '-'
      },
      { name: 'desc', label: '描述信息', field: 'desc' },
      { name: 'value', label: '属性值', field: 'value' },
      {
        name: 'required',
        label: '是否必填',
        field: row => (row.required ? '是' : '否')
      }
    ],
    historyColumns: [
      {
        name: 'modelName',
        label: '实体',
        field: row => row.modelName || '-'
      },
      {
        name: 'env',
        label: '环境',
        field: row => row.env || '-'
      },
      {
        name: 'desc',
        label: '原描述',
        field: row => row.before.desc || '-'
      },
      {
        name: 'newDesc',
        label: '新描述',
        field: row => row.after.desc || '-'
      },
      {
        name: 'version',
        label: '原版本',
        field: row => row.before.version || '-'
      },
      {
        name: 'newVersion',
        label: '新版本',
        field: row => row.after.version || '-'
      },
      { name: 'username', label: '操作人', field: 'username' },
      {
        name: 'ctime',
        label: '修改时间',
        field: row => row.ctime || '-'
      },
      { name: 'btn', label: '操作' }
    ]
  };
}

export function modelListColumns() {
  return {
    columns: [
      {
        name: 'name_cn',
        label: '实体中文名',
        field: 'name_cn'
      },
      {
        name: 'name_en',
        label: '实体英文名',
        field: row => row.name_en || '-',
        copy: true
      },
      {
        name: 'one_type',
        label: '一级分类',
        field: row => row.first_category || '-'
      },
      {
        name: 'two_type',
        label: '二级分类',
        field: row => row.second_category || '-'
      },
      {
        name: 'suffix_name',
        label: '后缀名',
        field: row => row.suffix_name || '-'
      },
      {
        name: 'action_scope',
        label: '作用域',
        field: row => row.scope || '-'
      },
      {
        name: 'version',
        label: '版本',
        field: row => row.version || '-'
      },
      { name: 'desc', label: '描述信息', field: 'desc' },
      {
        name: 'model_template_id',
        label: '实体模板中文名',
        field: 'model_template_id'
      },
      {
        name: 'platform',
        label: '适用平台',
        field: 'platform'
      }
    ],
    detailColumns: [
      { name: 'name_en', label: '属性字段', field: 'name_en' },
      { name: 'name_cn', label: '中文名', field: 'name_cn' },
      {
        name: 'require',
        label: '是否必填',
        field: 'require'
      },
      {
        name: 'desc',
        label: '描述',
        field: row => row.desc || '-'
      },
      {
        name: 'data_type',
        label: '属性类型',
        field: 'data_type'
      },
      {
        name: 'type',
        label: '是否应用独有',
        field: 'type'
      }
    ],
    keyColumns: [
      { name: 'name', label: '属性字段', field: 'name' },
      {
        name: 'desc',
        label: '描述信息',
        field: row => row.desc || '-'
      },
      {
        name: 'required',
        label: '是否必填',
        field: row => (row.required ? '是' : '否')
      }
    ]
  };
}

export function modelTempColumns() {
  return {
    columns: [
      {
        name: 'nameCn',
        label: '实体模板中文名',
        field: 'nameCn'
      },
      {
        name: 'nameEn',
        label: '实体模板英文名',
        field: row => row.nameEn || '-',
        copy: true
      },
      { name: 'desc', label: '描述信息', field: 'desc' }
    ],
    detailColumns: [
      {
        name: 'prop_key',
        label: '属性字段',
        field: 'prop_key'
      },
      {
        name: 'prop_name_cn',
        label: '属性中文名',
        field: 'prop_name_cn'
      },
      // { name: 'require', label: '是否必填', field: 'require' },
      { name: 'desc', label: '描述', field: 'desc' },
      {
        name: 'data_type',
        label: '属性类型',
        field: 'data_type'
      }
      // { name: 'type', label: '是否应用独有', field: 'type' }
    ],
    keyColumns: [
      { name: 'name', label: '属性字段', field: 'name' },
      { name: 'desc', label: '描述信息', field: 'desc' },
      {
        name: 'required',
        label: '是否必填',
        field: row => (row.required ? '是' : '否')
      }
    ]
  };
}

export function configDepColumns() {
  return {
    propertyColumns: [
      {
        name: 'name_cn',
        label: '应用中文名',
        field: row => row.name_zh || '-',
        copy: true
      },
      {
        name: 'name_en',
        label: '应用英文名',
        field: row => row.name_en || '-',
        copy: true
      },
      {
        name: 'app_manager',
        label: '应用负责人',
        field: row => chineseName(row.dev_managers) || '-',
        copy: true
      },
      {
        name: 'spdb_manager',
        label: '行内项目负责人',
        field: row => chineseName(row.spdb_managers) || '-',
        copy: true
      },
      {
        name: 'group',
        label: '所属小组',
        field: row => row.group.name || '-',
        copy: true
      },
      {
        name: 'gitlab',
        label: 'gitlab仓库',
        field: row => row.git || '-',
        copy: true
      },
      {
        name: 'branch',
        label: '分支名',
        field: row => row.branch || '-'
      }
    ],
    mappingColumns: [
      {
        name: 'model_name_cn',
        label: '实体中文名',
        field: 'model_name_cn'
      },
      {
        name: 'model_name_en',
        label: '实体英文名',
        field: 'model_name_en'
      },
      {
        name: 'field_name_en',
        label: '实体属性',
        field: row => row.field_name_en || '-'
      },
      {
        name: 'env_name_cn',
        label: '环境中文名',
        field: row => row.env_name_cn || '-',
        copy: true
      },
      {
        name: 'env_name_en',
        label: '环境英文名',
        field: row => row.env_name_en || '-',
        copy: true
      },
      {
        name: 'labels',
        label: '环境标签',
        field: field => field.labels.join(',') || '-'
      },
      {
        name: 'value',
        label: '映射值',
        field: 'value'
      },
      {
        name: 'btn',
        label: '操作',
        field: 'btn'
      }
    ],
    configDepAnalysisColumns: [
      {
        name: 'name_cn',
        label: '应用中文名',
        field: row => row.name_zh || '-',
        copy: true
      },
      {
        name: 'name_en',
        label: '应用英文名',
        field: row => row.name_en || '-',
        copy: true
      },
      {
        name: 'dev_managers',
        label: '应用负责人',
        field: 'dev_managers',
        copy: true
      },
      {
        name: 'spdb_managers',
        label: '行内项目负责人',
        field: 'spdb_managers',
        copy: true
      },
      {
        name: 'group',
        label: '所属小组',
        field: row => row.group.name || '-',
        copy: true
      },
      {
        name: 'gitlab',
        label: 'gitlab仓库',
        field: row => row.git || '-',
        copy: true
      },
      {
        name: 'branch',
        label: '分支名',
        field: row => row.branch || '-'
      }
    ],
    createConfigFilecolumns: [
      {
        name: 'name_cn',
        label: '应用中文名',
        field: 'name_zh',
        copy: true
      },
      {
        name: 'name_en',
        label: '应用英文名',
        field: 'name_en',
        copy: true
      },
      {
        name: 'app_manager',
        label: '应用负责人',
        field: row => chineseName(row.dev_managers),
        copy: true
      },
      {
        name: 'spdb_manager',
        label: '行内项目负责人',
        field: row => chineseName(row.spdb_managers),
        copy: true
      },
      {
        name: 'group',
        label: '所属小组',
        field: row => row.group.name,
        copy: true
      },
      { name: 'gitlab', label: 'gitlab仓库', field: 'git' },
      {
        name: 'branch',
        label: '分支名',
        field: 'branch'
      }
    ]
  };
}

export function modelMessageColumns() {
  return [
    {
      name: 'modelNameCn',
      label: '实体',
      field: 'modelNameCn'
    },
    {
      name: 'modelNameEn',
      label: '实体英文名',
      field: 'modelNameEn'
    },
    {
      name: 'envNameCn',
      label: '环境',
      field: row => row.envNameCn || '-',
      copy: true
    },
    {
      name: 'envNameEn',
      label: '环境英文名',
      field: row => row.envNameEn || '-',
      copy: true
    },
    {
      name: 'status',
      label: '状态',
      field: 'status'
    },
    {
      name: 'type',
      label: '变动类型',
      field: 'type'
    },
    {
      name: 'applyUsername',
      label: '申请人',
      field: 'applyUsername'
    },
    {
      name: 'createTime',
      label: '申请时间',
      field: row => row.createTime || '-'
    },
    {
      name: 'id',
      label: '申请流水号',
      field: row => row.id || '-',
      copy: true
    },
    {
      name: 'applyEmail',
      label: '申请人邮箱',
      field: row => row.applyEmail || '-'
    },
    {
      name: 'desc',
      label: '描述',
      field: row => row.desc || '-'
    },
    {
      name: 'btn',
      label: '操作',
      field: 'btn'
    }
  ];
}

export const modelEnvChangeColumns = [
  {
    name: 'name_en',
    label: '属性',
    field: 'name_en'
  },
  {
    name: 'name_cn',
    label: 'CI/属性中文名',
    field: 'name_cn'
  },
  {
    name: 'oldValue',
    label: '变动前值',
    field: 'oldValue'
  },
  {
    name: 'newValue',
    label: '变动后值',
    field: 'newValue'
  }
];

export function modelGroupColumns() {
  return {
    columns: [
      {
        name: 'name_cn',
        label: '实体组名称',
        field: row => row.nameCn || '-',
        copy: true
      },
      {
        name: 'modelsInfo',
        label: '包含实体'
      },
      {
        name: 'template',
        label: '模板',
        field: row => row.template || '-'
      }
    ],
    detailColumns: [
      {
        name: 'name_en',
        label: '实体英文名',
        field: 'name_en'
      },
      {
        name: 'name_cn',
        label: '实体中文名',
        field: 'name_cn'
      },
      {
        name: 'desc',
        label: '描述信息',
        field: 'desc'
      }
    ]
  };
}

export function deployMessageColumns() {
  return {
    columns: [
      {
        name: 'appInfo',
        label: '应用英文名',
        field: 'appInfo'
      },
      {
        name: 'group',
        label: '所属组',
        field: row => row.group || '-',
        copy: true
      },
      {
        name: 'modelSet',
        label: '实体组',
        field: 'modelSet'
      },
      {
        name: 'testEnv',
        label: '测试环境',
        field: field =>
          field.testEnv
            .map(item => {
              return item.name_en;
            })
            .join('，')
      },
      {
        name: 'productEnv',
        label: '生产环境',
        field: field =>
          field.productEnv
            .map(item => {
              return item.name_en;
            })
            .join('，')
      },
      {
        name: 'btn',
        label: '操作',
        field: 'btn'
      }
    ],
    detailColumns: [
      {
        name: 'name_en',
        label: '属性字段',
        field: 'name_en'
      },
      {
        name: 'name_cn',
        label: '属性中文名',
        field: 'name_cn'
      },
      {
        name: 'value',
        label: '属性值',
        field: 'value'
      },
      {
        name: 'own_value',
        label: '应用独有',
        field: 'own_value'
      }
    ]
  };
}

export const proInfo = [
  {
    type: 'Caas平台',
    platform: [{ title: '应用' }, { title: '容器' }]
  },
  {
    type: 'SCC平台',
    platform: [{ title: '应用' }, { title: '容器' }]
  }
];
