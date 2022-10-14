const UserList = () => import('@/modules/User/views/list');
const Detail = () => import('@/modules/User/views/detail');
const Role = () => import('@/modules/User/views/role');
const Group = () => import('@/modules/User/views/group');
const Company = () => import('@/modules/User/views/company');

export default [
  {
    path: '/user',
    name: 'user',
    meta: {
      nameCn: '用户',
      fstMenu: 'user',
      icon: 'users'
    },
    children: [
      {
        path: 'userQuery',
        name: 'userQuery',
        meta: {
          nameCn: '用户查询',
          fstMenu: 'user',
          secMenu: 'userQuery',
          icon: 'userQuery'
        },
        component: UserList
      },
      {
        path: 'list/:id',
        name: 'Detail',
        meta: {
          nameCn: '用户详情',
          fstMenu: 'user',
          secMenu: 'Detail',
          hideInMenu: true
        },
        component: Detail
      },
      {
        path: 'companyQuery',
        name: 'companyQuery',
        meta: {
          nameCn: '公司查询',
          fstMenu: 'user',
          secMenu: 'companyQuery',
          icon: 'companyQuery'
        },
        component: Company
      },
      {
        path: 'roleQuery',
        name: 'roleQuery',
        meta: {
          nameCn: '角色查询',
          fstMenu: 'user',
          secMenu: 'roleQuery',
          icon: 'roleQuery'
        },
        component: Role
      },
      {
        path: 'organizationQuery',
        name: 'organizationQuery',
        meta: {
          nameCn: '组织结构查询',
          fstMenu: 'user',
          secMenu: 'organizationQuery',
          icon: 'organizationQuery'
        },
        component: Group
      }
    ]
  }
];
