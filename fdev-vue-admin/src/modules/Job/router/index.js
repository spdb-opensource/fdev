const JobList = () => import('../views/list/index.vue');
const JobProfile = () => import('../views/profile/index.vue');
const taskDetail = () => import('../views/taskDetail/index.vue');
const JobManage = () => import('../views/manage/index.vue');
const DesignReview = () => import('../views/design/index.vue');
const TestRun = () => import('../views/testRun/index.vue');
const AddProductionProblem = () => import('../views/prodProblem/add.vue');
const ModifyProductionProblem = () => import('../views/prodProblem/modify.vue');
const JobAdd = () => import('../views/add/index.vue');
const DelayTask = () => import('../views/delayTask');
const SitApprove = () => import('../views/sitApprove');
const RealTimeParticipation = () => import('../views/RealTimeParticipation');
export default [
  {
    path: '/job',
    name: 'job',
    meta: {
      nameCn: '任务',
      fstMenu: 'rAndD',
      secMenu: 'job',
      icon: 'job'
    },
    children: [
      {
        path: 'list',
        name: 'taskQuery',
        meta: {
          nameCn: '任务查询',
          fstMenu: 'rAndD',
          secMenu: 'job'
        },
        component: JobList
      },
      {
        path: 'add',
        name: 'add',
        meta: {
          nameCn: '任务新增',
          fstMenu: 'rAndD',
          secMenu: 'job'
        },
        component: JobAdd
      },
      {
        path: 'add/:id',
        name: 'add2',
        meta: {
          nameCn: '任务新增',
          hideInMenu: true,
          fstMenu: 'rAndD',
          secMenu: 'job'
        },
        component: JobAdd
      },
      // 延期任务列表
      {
        path: 'delayTaskInJob',
        name: 'delayTaskInJob',
        meta: {
          nameCn: '延期任务',
          fstMenu: 'rAndD',
          secMenu: 'job'
        },
        component: DelayTask
      },
      // sit多次合并报表
      {
        path: 'sitApprove',
        name: 'sitApprove',
        meta: {
          nameCn: 'sit多次合并',
          fstMenu: 'rAndD',
          secMenu: 'job'
        },
        component: SitApprove
      },
      {
        path: 'taskDetail/:id',
        meta: {
          nameCn: '任务详情',
          hideInMenu: true,
          fstMenu: 'rAndD',
          secMenu: 'job'
        },
        component: JobProfile
      },
      {
        path: 'list/:id',
        meta: {
          nameCn: '任务详情',
          hideInMenu: true,
          fstMenu: 'rAndD',
          secMenu: 'job'
        },
        component: taskDetail
      },
      {
        path: 'list/:id/manage',
        meta: {
          nameCn: '任务管理',
          fstMenu: 'rAndD',
          hideInMenu: true
        },
        component: JobManage
      },
      {
        path: 'list/:id/design',
        name: 'uiDesign',
        meta: {
          nameCn: '设计还原审核',
          hideInMenu: true,
          fstMenu: 'rAndD',
          secMenu: 'job'
        },
        component: DesignReview
      },
      {
        path: 'list/:id/testRun',
        name: 'jobTestRun',
        meta: {
          nameCn: '挂载试运行投产窗口',
          hideInMenu: true,
          fstMenu: 'rAndD',
          secMenu: 'job'
        },
        component: TestRun
      },
      {
        path: 'list/addProductionProblem/:id',
        meta: {
          nameCn: '新建生产问题',
          hideInMenu: true,
          fstMenu: 'rAndD',
          secMenu: 'job'
        },
        component: AddProductionProblem
      },
      {
        path: 'list/:id/modifyProductionProblem',
        meta: {
          nameCn: '修改生产问题',
          hideInMenu: true,
          fstMenu: 'rAndD',
          secMenu: 'job'
        },
        component: ModifyProductionProblem
      }
    ]
  },
  // 电子看板与任务管理同级
  {
    path: '/realTimeBoard',
    name: 'realTimeBoard',
    meta: {
      nameCn: '电子看板',
      fstMenu: 'rAndD',
      secMenu: 'realTimeBorad',
      icon: 'realTimeBoard'
    },
    component: RealTimeParticipation
  }
];
