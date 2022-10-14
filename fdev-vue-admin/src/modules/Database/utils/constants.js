export function databaseListModel() {
  return {
    database_type: '',
    database_name: '',
    table_name: '',
    spdb_manager: '',
    appid: '',
    status: ''
  };
}
export function dictionaryListModel() {
  return {
    sys_id: '',
    database_type: '',
    database_name: '',
    field_en_name: '',
    system_name_cn: '',
    name: '',
    database_tableName: ''
  };
}
//新增
export function dictionaryAddListModel() {
  return {
    sys_id: '',
    system_name_cn: '',
    dict_id: '',
    record_id: '',
    database_type: '',
    database_name: '',
    field_en_name: '',
    field_ch_name: '',
    field_type: '',
    field_length: '',
    list_field_desc: '',
    is_list_field: '',
    remark: ''
  };
}
export function uploadModel() {
  return {
    files: [],
    database_type: 'mysql'
  };
}
export function importModel() {
  return {
    files: []
  };
}

export function listColumns() {
  return [
    {
      name: 'appName_cn',
      label: '应用名',
      field: 'appName_cn',
      align: 'left'
    },
    {
      name: 'appName_en',
      label: '应用英文名',
      field: 'appName_en',
      align: 'left'
    },
    {
      name: 'database_type',
      label: '数据库类型',
      field: 'database_type',
      align: 'left'
    },
    {
      name: 'database_name',
      label: '库名',
      field: 'database_name',
      align: 'left'
    },
    {
      name: 'table_name',
      label: '表名',
      field: 'table_name',
      align: 'left'
    },
    {
      name: 'manager',
      label: '行内应用负责人',
      field: row => row.appinfo.spdb_managers,
      align: 'left'
    },
    {
      name: 'btn',
      label: '操作',
      field: 'btn',
      align: 'left'
    }
  ];
}

export function dataDetailsColumns() {
  return [
    {
      name: 'column',
      label: '字段',
      align: 'left'
    },
    {
      name: 'columnType',
      label: '类型',
      align: 'left'
    }
  ];
}

export function detailsIndexsColumns() {
  return [
    {
      name: 'indexName',
      label: '索引名称',
      align: 'left'
    },
    {
      name: 'indexColumn',
      label: '索引字段',
      align: 'left'
    }
  ];
}
export function detailsUniqueIndexsColumns() {
  return [
    {
      name: 'indexName',
      label: '索引名称',
      align: 'left'
    },
    {
      name: 'indexColumn',
      label: '索引字段',
      align: 'left'
    }
  ];
}

export function dataDictionaryColumns() {
  return [
    {
      name: 'system_name_cn',
      label: '系统中文名',
      field: 'system_name_cn',
      align: 'left',
      required: true
    },
    {
      name: 'database_type',
      label: '数据库类型',
      field: 'database_type',
      align: 'left'
    },
    {
      name: 'database_name',
      label: '数据库名称',
      field: 'database_name',
      align: 'left'
    },
    {
      name: 'field_en_name',
      label: '字段英文名',
      field: 'field_en_name',
      align: 'left'
    },
    {
      name: 'field_ch_name',
      label: '字段中文名',
      field: 'field_ch_name',
      align: 'left'
    },
    {
      name: 'field_type',
      label: '字段类型',
      field: 'field_type',
      align: 'left'
    },
    {
      name: 'field_length',
      label: '字段长度',
      field: 'field_length',
      align: 'left'
    },
    {
      name: 'is_list_field',
      label: '是否列表字段',
      field: row => (row.is_list_field == 'Y' ? '是' : '否'),
      align: 'left'
    },
    {
      name: 'list_field_desc',
      label: '列表字段枚举说明',
      field: 'list_field_desc',
      align: 'left'
    },
    {
      name: 'remark',
      label: '备注信息',
      field: 'remark',
      align: 'left'
    },
    {
      name: 'first_modi_time',
      label: '首次维护日期',
      field: 'first_modi_time',
      align: 'left'
    },
    {
      name: 'first_modi_userNameEn',
      label: '首次维护人英文名',
      field: 'first_modi_userNameEn',
      align: 'left'
    },
    {
      name: 'first_modi_userName',
      label: '首次维护人姓名',
      field: 'first_modi_userName',
      align: 'left'
    },
    {
      name: 'last_modi_time',
      label: '最后维护日期',
      field: 'last_modi_time',
      align: 'left'
    },
    {
      name: 'last_modi_userNameEn',
      label: '最后维护人英文名',
      field: 'last_modi_userNameEn',
      align: 'left'
    },
    {
      name: 'last_modi_userName',
      label: '最后维护人姓名',
      field: 'last_modi_userName',
      align: 'left'
    },
    {
      name: 'btn',
      label: '操作',
      field: 'btn',
      align: 'left',
      required: true
    }
  ];
}

export function dataDictionaryRegColumns() {
  return [
    {
      name: 'system_name_cn',
      label: '所属系统',
      field: row => row.sysInfo.system_name_cn,
      align: 'left'
    },
    {
      name: 'database_type',
      label: '数据库类型',
      field: 'database_type',
      align: 'left'
    },
    {
      name: 'database_name',
      label: '库名',
      field: 'database_name',
      align: 'left'
    },
    {
      name: 'reviewers',
      label: '是否新增表',
      field: row => (row.is_new_table == 'Y' ? '是' : '否'),
      align: 'left'
    },
    {
      name: 'tableName',
      label: '表名',
      field: 'table_name',
      align: 'left'
    },
    {
      name: 'appName',
      label: '数据字典英文名',
      field: row =>
        row.new_field
          ? row.new_field
              .map(item => item.field)
              .toString()
              .split(',')
              .join('、')
          : '',
      align: 'left'
    },
    {
      name: 'btn',
      label: '操作',
      field: 'btn',
      align: 'left'
    }
  ];
}

export function dictionaryRegKeyColumns() {
  return [
    {
      name: 'field',
      label: '字段英文名',
      field: 'field',
      align: 'left'
    },
    {
      name: 'field_cn',
      label: '字段中文名',
      field: 'field_cn',
      align: 'left'
    },
    {
      name: 'field_length',
      label: '长度',
      field: 'field_length',
      align: 'left'
    },
    {
      name: 'field_type',
      label: '类型',
      field: 'field_type',
      align: 'left'
    },
    {
      name: 'is_list_field',
      label: '是否列表字段',
      field: row => (row.is_list_field == 'Y' ? '是' : '否'),
      align: 'left'
    },
    {
      name: 'btn',
      label: '操作',
      field: 'btn',
      align: 'center'
    }
  ];
}

export function dictRegChildColumns() {
  return [
    {
      name: 'system_name_cn',
      label: '系统中文名',
      field: 'system_name_cn',
      align: 'left'
    },
    {
      name: 'database_type',
      label: '数据库类型',
      field: 'database_type',
      align: 'left'
    },
    {
      name: 'database_name',
      label: '数据库名称',
      field: 'database_name',
      align: 'left'
    },
    {
      name: 'field_en_name',
      label: '字段英文名',
      field: 'field_en_name',
      align: 'left'
    },
    {
      name: 'field_ch_name',
      label: '字段中文名',
      field: 'field_ch_name',
      align: 'left'
    },
    {
      name: 'field_type',
      label: '字段类型',
      field: 'field_type',
      align: 'left'
    },
    {
      name: 'field_length',
      label: '字段长度',
      field: 'field_length',
      align: 'left'
    },
    {
      name: 'is_list_field',
      label: '是否列表字段',
      field: row => (row.is_list_field == 'Y' ? '是' : '否'),
      align: 'left'
    }
  ];
}

export function dictRegDetailColumns() {
  return [
    {
      name: 'field',
      label: '字段英文名',
      align: 'left'
    },
    {
      name: 'field_cn',
      label: '字段中文名',
      align: 'left'
    },
    {
      name: 'field_type',
      label: '字段类型',
      align: 'left'
    },
    {
      name: 'field_length',
      label: '字段长度',
      align: 'left'
    },
    {
      name: 'is_list_field',
      label: '是否列表字段',
      field: row => (row.is_list_field == 'Y' ? '是' : '否'),
      align: 'left'
    }
  ];
}
