export default [
  {
    name: 'user',
    label: '用户',
    icon: 'supervisor_account',
    auth: [
      { label: '新增用户', value: '/api/user/add' },
      { label: '更新用户', value: '/api/user/update' },
      { label: '删除用户', value: '/api/user/delete' }
    ]
  },
  {
    name: 'company',
    label: '公司',
    icon: 'mdi-set mdi-sitemap',
    auth: [
      { label: '新增公司', value: '/api/company/add' },
      { label: '更新公司', value: '/api/company/update' },
      { label: '删除公司', value: '/api/company/delete' }
    ]
  },
  {
    name: 'group',
    label: '小组',
    icon: 'group',
    auth: [
      { label: '新增小组', value: '/api/group/add' },
      { label: '更新小组', value: '/api/group/update' },
      { label: '删除小组', value: '/api/group/delete' }
    ]
  },
  {
    name: 'role',
    label: '角色',
    icon: 'business',
    auth: [
      { label: '新增角色', value: '/api/role/add' },
      { label: '更新角色', value: '/api/role/update' },
      { label: '删除角色', value: '/api/role/delete' }
    ]
  },
  {
    name: 'label',
    label: '标签',
    icon: 'local_offer',
    auth: [
      { label: '新增标签', value: '/api/label/add' },
      { label: '更新标签', value: '/api/label/update' },
      { label: '删除标签', value: '/api/label/delete' }
    ]
  }
  // {
  //   name: 'auth',
  //   label: '权限',
  //   icon: 'lock',
  //   auth: [
  //     {label: '更新权限', value: '/api/permission/update'},
  //   ]
  // }
];
