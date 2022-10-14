const AppList = () => import('@/modules/App/views/list/index.vue');
const AppProfile = () => import('@/modules/App/views/profile/index.vue');
const ApplicationAdd = () => import('@/modules/App/views/add/index.vue');
const AppCIList = () => import('@/modules/App/views/ciList/index.vue');
const AppPipelines = () => import('@/modules/App/components/pipelines');
const AppJobs = () => import('@/modules/App/views/pipelines/jobs');
const AppVipList = () => import('@/modules/App/views/vipPackage/index.vue');
const AppDeploy = () => import('@/modules/App/views/vipPackage/Deploy');
const AppVipJobs = () => import('@/modules/App/views/vipPackage/vipJobs');

export default [
  {
    path: '/app',
    name: 'app',
    meta: {
      nameCn: '应用',
      fstMenu: 'appAndConfig',
      secMenu: 'app',
      icon: 'apps'
    },
    children: [
      {
        path: 'list',
        name: 'appList',
        meta: {
          nameCn: '应用列表',
          fstMenu: 'appAndConfig',
          secMenu: 'app'
        },
        component: AppList
      },
      {
        path: 'applicationAdd',
        name: 'applicationAdd',
        meta: {
          nameCn: '应用新增',
          fstMenu: 'appAndConfig',
          secMenu: 'app'
        },
        component: ApplicationAdd
      },
      {
        path: 'list/:id',
        meta: {
          nameCn: '应用详情',
          fstMenu: 'appAndConfig',
          secMenu: 'app',
          hideInMenu: true
        },
        component: AppProfile
      },
      {
        path: 'appCIlist',
        name: 'appCIList',
        meta: {
          nameCn: '持续集成模板列表',
          fstMenu: 'appAndConfig',
          secMenu: 'app'
        },
        component: AppCIList
      },
      {
        path: 'appVipList',
        name: 'appVipList',
        meta: {
          nameCn: 'Vip打包通道',
          fstMenu: 'appAndConfig',
          secMenu: 'app'
        },
        component: AppVipList
      },
      {
        path: 'appDeploy/:path',
        name: 'appDeploy',
        meta: {
          hideInMenu: true,
          nameCn: '部署',
          fstMenu: 'appAndConfig',
          secMenu: 'app'
        },
        component: AppDeploy
      },
      {
        path: 'pipelines/:id',
        name: 'pipelines',
        meta: {
          nameCn: 'pipelines',
          fstMenu: 'appAndConfig',
          secMenu: 'app',
          hideInMenu: true
        },
        component: AppPipelines
      },
      {
        path: 'jobs/:job_id/:project_id',
        name: 'Jobs',
        meta: {
          nameCn: 'Jobs',
          fstMenu: 'appAndConfig',
          secMenu: 'app',
          hideInMenu: true
        },
        component: AppJobs
      },
      {
        path: 'vipJobs/:id',
        name: 'vipJobs',
        meta: {
          nameCn: 'vipJobs',
          fstMenu: 'appAndConfig',
          secMenu: 'app',
          hideInMenu: true
        },
        component: AppVipJobs
      }
    ]
  }
];
