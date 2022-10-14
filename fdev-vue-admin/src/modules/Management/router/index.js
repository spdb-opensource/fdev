const userManagement = () => import('../views/user/index.vue');
const menuManagement = () => import('../views/menu/index.vue');
const companyManagement = () => import('../views/company/index.vue');
const roleManagement = () => import('../views/role/index.vue');
const organizationManagement = () => import('../views/organization/index.vue');
const userDetail = () => import('../views/user/components/userDetail.vue');
const apiRoleManagement = () => import('../views/api/index.vue');

export default [
  {
    path: 'management',
    name: 'management',
    meta: {
      nameCn: '设置',
      icon: 'tool',
      fstMenu: 'management'
    },
    children: [
      {
        path: 'user',
        name: 'userManagement',
        meta: {
          nameCn: '用户管理',
          fstMenu: 'management',
          secMenu: 'userManagement',
          icon: 'userManagement'
        },
        component: userManagement
      },
      {
        path: 'list/:id',
        name: 'userDetail',
        meta: {
          nameCn: '用户详情',
          fstMenu: 'management',
          secMenu: 'userDetail',
          hideInMenu: true
        },
        component: userDetail
      },
      {
        path: 'menu',
        name: 'menuManagement',
        meta: {
          nameCn: '菜单管理',
          fstMenu: 'management',
          secMenu: 'menuManagement',
          icon: 'menuManagement'
        },
        component: menuManagement
      },
      {
        path: 'company',
        name: 'companyManagement',
        meta: {
          nameCn: '公司管理',
          fstMenu: 'management',
          secMenu: 'companyManagement',
          icon: 'companyManagement'
        },
        component: companyManagement
      },
      {
        path: 'role',
        name: 'roleManagement',
        meta: {
          nameCn: '角色管理',
          fstMenu: 'management',
          secMenu: 'roleManagement',
          icon: 'roleManagement'
        },
        component: roleManagement
      },
      {
        path: 'organization',
        name: 'organizationManagement',
        meta: {
          nameCn: '组织结构管理',
          fstMenu: 'management',
          secMenu: 'organizationManagement',
          icon: 'organizationManagement'
        },
        component: organizationManagement
      },
      {
        path: 'apiRole',
        name: 'apiRoleManagement',
        meta: {
          nameCn: '接口权限登记',
          fstMenu: 'management',
          secMenu: 'apiRoleManagement',
          icon: 'apiRoleManagement'
        },
        component: apiRoleManagement
      }
    ]
  }
];
