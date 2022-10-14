// const Analysis = () => import('@/modules/Dashboard/views/Analysis');
// const TaskOutput = () => import('@/modules/Dashboard/views/kanban/TaskOutput');
// const MergeRequest = () =>
//   import('@/modules/Dashboard/views/code/MergeRequest');
// const CodeStatistics = () =>
//   import('@/modules/Dashboard/views/code/CodeStatistics');
// const UIdesign = () => import('@/modules/Dashboard/views/UIdesign');
// const CodeDetail = () =>
//   import('@/modules/Dashboard/views/code/CodeStatistics/CodeDetail');
// const RequirementAnalysis = () =>
//   import('@/modules/Dashboard/views/Demand/RequirementAnalysis');
// const ImplUnitAnalysis = () =>
//   import('@/modules/Dashboard/views/Demand/ImplUnitAnalysis');
const GroupTaskAnalysis = () =>
  import('@/modules/Dashboard/views/Demand/GroupTaskAnalysis');
// const UserTaskAnalysis = () =>
//   import('@/modules/Dashboard/views/Demand/UserTaskAnalysis');
// const AliothRelated = () =>
//   import('@/modules/Dashboard/views/Demand/AliothRelated');
// const ProductionProblemsProfile = () =>
//   import('@/modules/Dashboard/views/ProductionProblems/Profile');
// const ProChartStatis = () =>
//   import('@/modules/Dashboard/views/ProductionProblems/ProChartStatis');

export default [
  {
    path: 'dashboard',
    name: 'dashboard',
    meta: {
      nameCn: 'Dashboard',
      icon: 'dashboard',
      fstMenu: 'measure',
      secMenu: 'dashboard'
    },
    children: [
      // {
      //   path: 'analysis',
      //   name: 'analysis',
      //   meta: {
      //     nameCn: '小组维度',
      //     fstMenu: 'measure',
      //     secMenu: 'dashboard'
      //   },
      //   component: Analysis
      // },
      // {
      //   path: 'kanban',
      //   name: 'kanban',
      //   meta: {
      //     nameCn: '电子看板',
      //     fstMenu: 'measure',
      //     secMenu: 'dashboard'
      //   },
      //   children: [
      //     {
      //       path: 'taskOutput',
      //       name: 'taskOutput',
      //       meta: {
      //         nameCn: '任务产出统计',
      //         fstMenu: 'measure',
      //         secMenu: 'dashboard'
      //       },
      //       component: TaskOutput
      //     }
      //     // {
      //     //   path: 'realTimeParticipation',
      //     //   name: 'realTimeParticipation',
      //     //   meta: {
      //     //     nameCn: '电子看板',
      //     //     fstMenu: 'measure',
      //     //     secMenu: 'dashboard'
      //     //   },
      //     //   component: RealTimeParticipation
      //     // }
      //   ]
      // },
      // {
      //   path: 'application',
      //   name: 'applicationDemension',
      //   meta: {
      //     nameCn: '应用维度',
      //     fstMenu: 'measure',
      //     secMenu: 'dashboard'
      //   },
      //   component: ApplicationAnalysis
      // },
      {
        path: 'requirement',
        name: 'projectTeamManage',
        meta: {
          nameCn: '项目组管理',
          fstMenu: 'measure',
          secMenu: 'dashboard'
        },
        children: [
          // {
          //   path: 'requirementAnalysis',
          //   name: 'reqAnalysis',
          //   meta: {
          //     nameCn: '需求统计',
          //     fstMenu: 'measure',
          //     secMenu: 'dashboard'
          //   },
          //   component: RequirementAnalysis
          // },
          // {
          //   path: 'implementAnalysis',
          //   name: 'implUnitAnalysis',
          //   meta: {
          //     nameCn: '实施单元统计',
          //     fstMenu: 'measure',
          //     secMenu: 'dashboard'
          //   },
          //   component: ImplUnitAnalysis
          // },
          {
            path: 'groupTaskAnalysis',
            name: 'groupTaskAnalysis',
            meta: {
              nameCn: '小组维度任务统计',
              fstMenu: 'measure',
              secMenu: 'dashboard'
            },
            component: GroupTaskAnalysis
          }
          // {
          //   path: 'userTaskAnalysis',
          //   name: 'userTaskAnalysis',
          //   meta: {
          //     nameCn: '用户维度任务统计',
          //     fstMenu: 'measure',
          //     secMenu: 'dashboard'
          //   },
          //   component: UserTaskAnalysis
          // },
          // {
          //   path: 'AliothRelated',
          //   name: 'aliothRelated',
          //   meta: {
          //     nameCn: '玉衡测试相关',
          //     fstMenu: 'measure',
          //     secMenu: 'dashboard'
          //   },
          //   component: AliothRelated
          // }
          // {
          //   path: 'AliothRelated',
          //   name: 'aliothRelated',
          //   meta: {
          //     nameCn: '玉衡测试相关',
          //     fstMenu: 'measure',
          //     secMenu: 'dashboard'
          //   },
          //   component: AliothRelated
          // }
        ]
      }
      // {
      //   path: 'ProductionProblems',
      //   name: 'productionProblems',
      //   meta: {
      //     nameCn: '生产问题',
      //     fstMenu: 'measure',
      //     secMenu: 'dashboard'
      //   },
      //   children: [
      // {
      //   path: 'List',
      //   name: 'problemsList',
      //   meta: {
      //     nameCn: '问题列表',
      //     fstMenu: 'measure',
      //     secMenu: 'dashboard'
      //   },
      //   component: ProductionProblemsList
      // },
      //     {
      //       path: 'ProChartStatis',
      //       name: 'proChartStatis',
      //       meta: {
      //         nameCn: '报表统计',
      //         fstMenu: 'measure',
      //         secMenu: 'dashboard'
      //       },
      //       component: ProChartStatis
      //     }
      //   ]
      // },
      // {
      //   path: 'ProductionProblems/:id',
      //   name: 'ProductionProblemsProfile',
      //   meta: {
      //     nameCn: '生产问题详情',
      //     hideInMenu: true,
      //     fstMenu: 'measure',
      //     secMenu: 'dashboard'
      //   },
      //   component: ProductionProblemsProfile
      // },
      // {
      //   path: 'code',
      //   name: 'codeStatistic',
      //   meta: {
      //     nameCn: '代码统计相关',
      //     fstMenu: 'measure',
      //     secMenu: 'dashboard'
      //   },
      //   children: [
      //     {
      //       path: 'statistics',
      //       name: 'statistics',
      //       meta: {
      //         nameCn: '代码统计',
      //         fstMenu: 'measure',
      //         secMenu: 'dashboard'
      //       },
      //       component: CodeStatistics
      //     },
      // {
      //   path: 'detail/:name',
      //   name: 'statisticsDetail',
      //   meta: {
      //     nameCn: '详情',
      //     hideInMenu: true,
      //     fstMenu: 'measure',
      //     secMenu: 'dashboard'
      //   },
      //   component: CodeDetail
      // }
      //     {
      //       path: 'MergeRequest',
      //       name: 'mergeRequest',
      //       meta: {
      //         nameCn: 'Merge Request',
      //         fstMenu: 'measure',
      //         secMenu: 'dashboard'
      //       },
      //       component: MergeRequest
      //     }
      //   ]
      // },
      // {
      //   path: 'uiDesign',
      //   name: 'uiDesign',
      //   meta: {
      //     nameCn: 'UI审核进度',
      //     fstMenu: 'measure',
      //     secMenu: 'dashboard'
      //   },
      //   component: UIdesign
      // }
    ]
  }
];
