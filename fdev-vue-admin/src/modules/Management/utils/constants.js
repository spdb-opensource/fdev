export const gitOption = [
  { value: '1', label: '必填' },
  { value: '0', label: '非必填' }
];
export const rangeOption = [
  { value: '1', label: '科技' },
  { value: '3', label: '厂商、科技' }
];
export function functionColumn() {
  return [
    {
      name: 'name',
      label: '职能名称',
      align: 'left',
      sortable: true,
      field: 'name'
    },
    {
      name: 'count',
      label: '人数',
      align: 'left',
      sortable: true,
      field: 'count'
    },
    {
      name: 'spdbFlag',
      label: '职能范围',
      sortable: true,
      align: 'left',
      field: 'spdbFlag',
      format: val => {
        let obj = {
          '1': '科技',
          '3': '厂商、科技'
        };
        return obj[val];
      }
    },
    {
      name: 'type',
      label: 'Git信息必填',
      align: 'left',
      field: 'type',
      format: val => {
        let obj = {
          '0': '非必填',
          '1': '必填'
        };
        return obj[val];
      }
    },
    {
      name: 'operation',
      label: '操作',
      align: 'left',
      field: 'operation'
    }
  ];
}

export function userManagementColumn() {
  return [
    { name: 'name', label: '姓名', align: 'left', field: 'name' },
    {
      name: 'email',
      label: '邮箱',
      field: 'email',
      sortable: true,
      align: 'left'
    },
    {
      name: 'telephone',
      label: '手机号',
      field: 'telephone',
      sortable: true,
      align: 'left'
    },
    {
      name: 'group',
      label: '小组',
      field: row => (row.group ? row.group.name : '-'),
      sortable: true,
      align: 'left'
    },
    {
      name: 'role',
      label: '角色',
      field: row => row.role_label,
      align: 'left'
    },
    {
      name: 'function',
      label: '职能',
      field: row => row.function,
      align: 'left'
    },
    {
      name: 'area',
      label: '地域',
      field: row => row.area,
      align: 'left'
    },
    {
      name: 'work_num',
      label: '工号',
      field: 'work_num',
      align: 'left'
    },
    {
      name: 'sectionInfo',
      label: '所属条线',
      field: row => (row.sectionInfo ? row.sectionInfo.sectionNameCn : '-'),
      align: 'left'
    },
    {
      name: 'create_date',
      label: '入职时间',
      field: 'create_date',
      sortable: true,
      align: 'left'
    },
    {
      name: 'start_time',
      label: '开始工作时间',
      field: 'start_time',
      sortable: true,
      align: 'left'
    },
    {
      name: 'leave_date',
      label: '离职/换岗时间',
      field: 'leave_date',
      sortable: true,
      align: 'left'
    },
    {
      name: 'git_user',
      label: 'gitlab账号',
      field: 'git_user',
      align: 'left'
    },
    {
      name: 'company',
      label: '公司',
      field: row => (row.company ? row.company.name : '-'),
      sortable: true
    },
    {
      name: 'phone_type',
      label: '手机型号',
      field: 'phone_type',
      sortable: true
    },
    {
      name: 'phone_mac',
      label: '手机mac地址',
      field: 'phone_mac',
      sortable: true
    },
    {
      name: 'vm_ip',
      label: '虚机ip地址',
      field: 'vm_ip',
      sortable: true
    },
    {
      name: 'vm_name',
      label: '虚机名',
      field: 'vm_name',
      sortable: true
    },
    {
      name: 'vm_user_name',
      label: '虚机用户名/域用户',
      field: 'vm_user_name',
      sortable: true
    },
    {
      name: 'label',
      label: '标签',
      field: row => row.label,
      sortable: true,
      align: 'left'
    },
    {
      name: 'operation',
      label: '操作',
      field: 'operation',
      required: true
    }
  ];
}
// 菜单
export const menuTypeOption = [
  { value: '1', label: '非敏' },
  { value: '2', label: '敏捷' }
];

export function filterLevel(val) {
  let obj = {
    '1': '一',
    '2': '二',
    '3': '三',
    '4': '四',
    '5': '五',
    '6': '六'
  };
  return `${obj[val]}级菜单`;
}

export function filterType(val) {
  let obj = {
    '0': '部门',
    '1': '一级处',
    '2': '二级处',
    '3': '条线'
  };
  if (val <= 3) {
    return obj[val];
  }
}

export let groupTypeOption = [
  { value: '0', label: '部门' },
  { value: '1', label: '一级处' },
  { value: '2', label: '二级处' },
  { value: '3', label: '条线' },
  { value: '4', label: '自定义' }
];

export const perform = {
  visibleColumnApiRoleOptions: [
    'interfacePath',
    'roleIds',
    'functionDesc',
    'status'
  ]
};

export function createApiRoleModel() {
  return {
    interfacePath: '', //接口路径
    roleIds: [], //角色id集合
    functionDesc: '' //功能描述
  };
}
