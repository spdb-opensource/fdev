const Component = () =>
  import('@/modules/Component/views/serverComponent/index');
const ImageManage = () => import('@/modules/Component/views/imageManage/index');
const ImageManageProfile = () =>
  import('@/modules/Component/views/imageManage/profile/index');
const Intergration = () =>
  import('@/modules/Component/views/serverComponent/intergration/index');
const ComponentProfile = () =>
  import('@/modules/Component/views/serverComponent/profile/index');
const IssuePage = () =>
  import('@/modules/Component/views/serverComponent/issuePage/index');
const HandlePage = () =>
  import('@/modules/Component/views/serverComponent/handlePage/index');
const HandlePageRel = () =>
  import('@/modules/Component/views/serverComponent/handleRel/index');
const HandleImageManagePage = () =>
  import('@/modules/Component/views/imageManage/handlePage/index');
const Archetype = () =>
  import('@/modules/Component/views/serverArchetype/index');
const WebArchetype = () =>
  import('@/modules/Component/views/webArchetype/index');
const ArchetypeProfile = () =>
  import('@/modules/Component/views/serverArchetype/profile/index');
const WebArchetypeProfile = () =>
  import('@/modules/Component/views/webArchetype/profile/index');
const ArchetypeHandlePage = () =>
  import('@/modules/Component/views/serverArchetype/handlePage/index');
const WebArchetypeHandlePage = () =>
  import('@/modules/Component/views/webArchetype/handlePage/index');
const ArchetypeIntergration = () =>
  import('@/modules/Component/views/serverArchetype/intergration/index');
const WebComponent = () =>
  import('@/modules/Component/views/webComponent/index');
const WebIntergration = () =>
  import('@/modules/Component/views/webComponent/intergration/index');
const WebComponentProfile = () =>
  import('@/modules/Component/views/webComponent/profile/index');
const WebIssuePage = () =>
  import('@/modules/Component/views/webComponent/issuePage/index');
const WebHandlePage = () =>
  import('@/modules/Component/views/webComponent/handlePage/index');

export default [
  {
    path: '/componentManage',
    name: 'componentManage',
    meta: {
      nameCn: '组件管理',
      fstMenu: 'appAndConfig',
      secMenu: 'componentManage',
      icon: 'componentManage'
    },
    children: [
      {
        path: 'server/list',
        name: 'server',
        meta: {
          nameCn: '后端组件',
          fstMenu: 'appAndConfig',
          secMenu: 'componentManage'
        },
        component: Component
      },
      {
        path: 'server/intergration',
        name: 'Intergration',
        meta: {
          nameCn: '集成现状',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'componentManage'
        },
        component: Intergration
      },
      {
        path: 'server/list/:id',
        name: 'ComponentProfile',
        meta: {
          nameCn: '后端组件详情',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'componentManage'
        },
        component: ComponentProfile
      },
      {
        path: 'server/issuePage/:id',
        name: 'issuePage',
        meta: {
          nameCn: '投产窗口处理',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'componentManage'
        },
        component: IssuePage
      },
      {
        path: 'server/handle/:id',
        name: 'HandlePage',
        meta: {
          nameCn: '优化需求处理',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'componentManage'
        },
        component: HandlePage
      },
      {
        path: 'server/handleRel/:id',
        name: 'HandlePageRel',
        meta: {
          nameCn: '多人开发处理',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'componentManage'
        },
        component: HandlePageRel
      },

      {
        path: 'web/weblist',
        name: 'weblist',
        meta: {
          nameCn: '前端组件',
          fstMenu: 'appAndConfig',
          secMenu: 'componentManage'
        },
        component: WebComponent
      },
      {
        path: 'web/webIntergration',
        name: 'WebIntergration',
        meta: {
          nameCn: '集成现状',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'componentManage'
        },
        component: WebIntergration
      },
      {
        path: 'web/weblist/:id',
        name: 'WebComponentProfile',
        meta: {
          nameCn: '前端组件详情',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'componentManage'
        },
        component: WebComponentProfile
      },
      {
        path: 'web/webIssuePage/:id',
        name: 'WebIssuePage',
        meta: {
          nameCn: '投产窗口处理',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'componentManage'
        },
        component: WebIssuePage
      },
      {
        path: 'web/webHandlePage/:id',
        name: 'WebHandlePage',
        meta: {
          nameCn: '多人开发处理',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'componentManage'
        },
        component: WebHandlePage
      }
    ]
  },
  {
    path: '/archetypeManage',
    name: 'archetypeManage',
    meta: {
      nameCn: '骨架管理',
      fstMenu: 'appAndConfig',
      secMenu: 'archetypeManage',
      icon: 'archetypeManage'
    },
    children: [
      {
        path: 'server/archetype',
        name: 'serverArchetype',
        meta: {
          nameCn: '后端骨架',
          fstMenu: 'appAndConfig',
          secMenu: 'archetypeManage'
        },
        component: Archetype
      },
      {
        path: 'server/archetype/:id',
        name: 'ArchetypeProfile',
        meta: {
          nameCn: '后端骨架详情',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'archetypeManage'
        },
        component: ArchetypeProfile
      },
      {
        path: 'server/archetypeHandlePage/:id/:archetype_id',
        name: 'ArchetypeHandlePage',
        meta: {
          nameCn: '优化需求处理',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'archetypeManage'
        },
        component: ArchetypeHandlePage
      },
      {
        path: 'server/archetypeIntergration/:archetype_id/:archetype_version',
        name: 'ArchetypeIntergration',
        meta: {
          nameCn: '历史版本处理',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'archetypeManage'
        },
        component: ArchetypeIntergration
      },
      {
        path: 'web/webArchetype',
        name: 'webArchetype',
        meta: {
          nameCn: '前端骨架',
          fstMenu: 'appAndConfig',
          secMenu: 'archetypeManage'
        },
        component: WebArchetype
      },
      {
        path: 'web/webArchetype/:id',
        name: 'WebArchetypeProfile',
        meta: {
          nameCn: '前端骨架详情',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'archetypeManage'
        },
        component: WebArchetypeProfile
      },
      {
        path: 'web/webArchetypeHandlePage/:id/:archetype_id',
        name: 'WebArchetypeHandlePage',
        meta: {
          nameCn: '优化需求处理',
          hideInMenu: true,
          fstMenu: 'appAndConfig',
          secMenu: 'archetypeManage'
        },
        component: WebArchetypeHandlePage
      }
    ]
  },
  {
    path: '/imageManage',
    name: 'imageManage',
    meta: {
      nameCn: '镜像管理',
      fstMenu: 'appAndConfig',
      secMenu: 'imageManage',
      icon: 'component'
    },
    component: ImageManage
  },
  {
    path: '/imageManage/:id',
    name: 'imageManageProfile',
    meta: {
      nameCn: '镜像详情',
      hideInMenu: true,
      fstMenu: 'appAndConfig',
      secMenu: 'imageManageProfile'
    },
    component: ImageManageProfile
  },
  {
    path: '/HandleImageManagePage/:id',
    name: 'HandleImageManagePage',
    meta: {
      nameCn: '优化需求处理',
      hideInMenu: true,
      fstMenu: 'appAndConfig',
      secMenu: 'HandleImageManagePage'
    },
    component: HandleImageManagePage
  }
];
