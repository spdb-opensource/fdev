const index = () => import('../index.vue');
const MainPage = () => import('../views/MainPage');
const staticReport = () => import('../views/staticReport');
const demandThroughput = () =>
  import('../views/staticReport/development/demandThroughput');
const taskThroughput = () =>
  import('../views/staticReport/development/taskThroughput');
const ProjectTeamSize = () =>
  import('../views/staticReport/resourceManagement/ProjectTeamSize');
const IdleResources = () =>
  import('../views/staticReport/resourceManagement/IdleResources');
const BasicFrame = () =>
  import('../views/staticReport/enviromentAndPublish/BasicFrame');
const TaskStageNum = () =>
  import('../views/staticReport/development/taskStageNum');
const TaskPhaseChange = () =>
  import('../views/staticReport/development/taskPhaseChange');
const PublishProblems = () =>
  import('../views/staticReport/enviromentAndPublish/publishProblems');
const RqrStatistic = () =>
  import('../views/staticReport/development/rqrStatistic');
const CodeRelate = () => import('../views/staticReport/development/codeRelate');
const CodeDetail = () =>
  import('../views/staticReport/development/codeRelate/components/CodeDetail');
const ImplUnitStatistic = () =>
  import('../views/staticReport/development/ImplUnitStatistic');
const AliothRelated = () =>
  import('../views/staticReport/development/AliothRelated');
const GroupTaskAnalysis = () =>
  import('../views/staticReport/development/GroupTaskAnalysis');
const IamsRelate = () => import('../views/staticReport/development/IamsRelate');
export default [
  {
    path: '/instrumentpanel',
    name: 'instrumentpanel',
    meta: {
      nameCn: '仪表盘',
      icon: 'instrumentpanel',
      fstMenu: 'measure',
      secMenu: 'dash'
    },
    component: index,
    children: [
      {
        path: '',
        name: 'instrument-panel',
        meta: {
          nameCn: '仪表盘',
          fstMenu: 'measure',
          hideInBreadcrumb: true
        },
        component: MainPage
      }
    ]
  },
  {
    path: '/staticReport',
    name: 'staticReport',
    meta: {
      nameCn: '统计报表',
      icon: 'staticReport',
      fstMenu: 'measure',
      secMenu: 'report'
    },
    component: staticReport,
    children: [
      {
        path: '',
        name: 'static-report',
        meta: {
          nameCn: '统计报表',
          fstMenu: 'measure',
          hideInBreadcrumb: true
        },
        component: staticReport
      }
    ]
  },
  {
    path: '/staticReport/demandThroughput',
    name: 'demandThroughput',
    meta: {
      nameCn: '牵头投产需求',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: demandThroughput
  },
  {
    path: '/staticReport/taskThroughput',
    name: 'taskThroughput',
    meta: {
      nameCn: '任务吞吐量',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: taskThroughput
  },
  {
    path: '/staticReport/ProjectTeamSize',
    name: 'projectTeamSize',
    meta: {
      nameCn: '项目组规模',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: ProjectTeamSize
  },
  {
    path: '/staticReport/IdleResources',
    name: 'IdleResources',
    meta: {
      nameCn: '项目组资源闲置情况',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: IdleResources
  },
  {
    path: '/staticReport/BasicFrame',
    name: 'BasicFrame',
    meta: {
      nameCn: '基础架构',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: BasicFrame
  },
  {
    path: '/staticReport/taskPhaseChange',
    name: 'taskPhaseChange',
    meta: {
      nameCn: '任务推进情况',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: TaskPhaseChange
  },
  {
    path: '/staticReport/taskStageNum',
    name: 'taskStageNum',
    meta: {
      nameCn: '不同阶段任务数量统计',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: TaskStageNum
  },
  {
    path: '/staticReport/publishProblems',
    name: 'PublishProblems',
    meta: {
      nameCn: '生产问题报表统计',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: PublishProblems
  },
  {
    path: '/staticReport/RqrStatistic',
    name: 'RqrStatistic',
    meta: {
      nameCn: '需求统计',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: RqrStatistic
  },
  {
    path: '/staticReport/codeRelate',
    name: 'GroupTaskStageNum',
    meta: {
      nameCn: '代码统计相关',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: CodeRelate
  },
  {
    path: '/staticReport/codeRelate/detail/:name',
    name: 'statisticsDetail',
    meta: {
      nameCn: '代码统计详情',
      hideInMenu: true,
      fstMenu: 'measure',
      secMenu: 'staticReport'
    },
    component: CodeDetail
  },
  {
    path: '/staticReport/ImplUnitStatistic',
    name: 'ImplUnitStatistic',
    meta: {
      nameCn: '研发单元统计',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: ImplUnitStatistic
  },
  {
    path: '/staticReport/AliothRelated',
    name: 'AliothRelated',
    meta: {
      nameCn: '玉衡测试相关',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: AliothRelated
  },
  {
    path: '/staticReport/GroupTaskAnalysis',
    name: 'GroupTaskAnalysis',
    meta: {
      nameCn: '小组维度任务统计',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: GroupTaskAnalysis
  },
  {
    path: '/staticReport/IamsRelate',
    name: 'IamsRelate',
    meta: {
      nameCn: '挡板相关',
      fstMenu: 'measure',
      secMenu: 'staticReport',
      hideInMenu: true
    },
    component: IamsRelate
  }
];
