import Pages from './configUrl.js';

const BasicLayout = () => import('../layouts/BasicLayout.vue');
const CompDoc = () => import('../components/CompDoc.vue');
const Login = () => import('../views/Login/views/index.vue');
const Once = () => import('../views/Login/views/Once.vue');
const OAuth = () => import('../views/Login/views/OAuth.vue');
const ForeignInterfaceList = () =>
  import('@/modules/interface/views/foreignInterfaceList/index');
const InterfaceProfile = () =>
  import('@/modules/interface/views/interfaceProfile/index');
const Center = () => import('@/modules/User/views/account/Center');
const Website = () => import('@/modules/User/views/account/Website');
const Team = () => import('@/views/Team/index.vue');
const Exception404 = () => import('@/views/Exception/404');
const OnceLogined = () => import('@/views/Login/views/OnceLogined');

export default [
  {
    path: '/compDoc',
    name: 'compDoc',
    component: CompDoc
  },
  {
    path: '/login',
    name: 'login',
    children: [
      {
        path: '/',
        component: Login
      },
      {
        path: 'once',
        component: Once
      },
      {
        path: '/oauth',
        component: OAuth,
        meta: {
          authority: ['admin', 'group_admin', 'user'],
          noMatch: '/login'
        }
      }
    ]
  },
  {
    path: '/queryInterfacesList/:id',
    component: ForeignInterfaceList,
    name: 'foreignInterfaceList'
  },
  {
    path: '/InterfaceProfile/:id',
    component: InterfaceProfile,
    name: 'InterfaceProfile'
  },
  {
    path: '/',
    name: 'app',
    component: BasicLayout,
    meta: {
      authority: ['admin', 'group_admin', 'user'],
      noMatch: '/login'
    },
    children: Pages.concat([
      {
        path: '/',
        redirect: 'account'
      },
      {
        path: 'account',
        name: 'account',
        meta: {
          nameCn: '个人中心',
          hideInMenu: true
        },
        children: [
          {
            path: 'center',
            name: 'center',
            meta: {
              nameCn: '个人信息'
            },
            component: Center
          },
          {
            path: 'onceLogined',
            name: 'onceLogined',
            meta: {
              nameCn: '首次登陆'
            },
            component: OnceLogined
          }
        ]
      },
      {
        path: 'team',
        component: Team,
        name: 'Team',
        meta: {
          nameCn: '团队介绍',
          hideInMenu: true
        }
      },
      {
        path: 'website',
        name: 'Website',
        meta: {
          nameCn: '常用网站',
          hideInMenu: true
        },
        component: Website
      },
      {
        path: '**',
        component: Exception404
      }
    ])
  }
];
