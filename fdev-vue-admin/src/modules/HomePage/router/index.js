const MyTask = () => import('../views/myTask');
const MyInfo = () => import('../views/myInfo');
const MyTodo = () => import('../views/myTodo');
const MyDefect = () => import('../views/myDefect');
const MyApp = () => import('../views/myApp');
const MyFrame = () => import('../views/myFrame');
const MyProductionProblem = () => import('../views/myProdProblem');
const myUnitApprove = () => import('../views/myExamineApprove');
const codeMergeApprove = () => import('../views/codeMergeApprove');
const myRqrEvaluateApprove = () => import('../views/myRqrEvaluateApprove');
const mySit2Approval = () => import('../views/mySit2DeployApprove');
export default [
  {
    path: 'homepage',
    name: 'homepage',
    meta: {
      nameCn: '工作台',
      fstMenu: 'homepage',
      icon: 'mine'
    },
    children: [
      {
        name: 'myInfo',
        path: 'myInfo',
        meta: {
          nameCn: '我的信息',
          fstMenu: 'homepage',
          secMenu: 'myInfo',
          icon: 'myInfo'
        },
        children: [
          {
            path: '',
            name: 'my-info',
            meta: {
              nameCn: '我的信息',
              fstMenu: 'homepage',
              hideInBreadcrumb: true
            },
            component: MyInfo
          }
        ]
      },
      {
        path: 'myTask',
        name: 'myTask',
        meta: {
          nameCn: '我的任务',
          fstMenu: 'homepage',
          secMenu: 'myTask',
          icon: 'myTask'
        },
        children: [
          {
            path: '',
            name: 'my-task',
            meta: {
              nameCn: '我的任务',
              fstMenu: 'homepage',
              hideInBreadcrumb: true
            },
            component: MyTask
          }
        ]
      },
      {
        path: 'myTodo',
        name: 'myTodo',
        meta: {
          nameCn: '我的待办',
          fstMenu: 'homepage',
          secMenu: 'myTodo',
          icon: 'myTodo'
        },
        children: [
          {
            path: '',
            name: 'my-todo',
            meta: {
              nameCn: '我的待办',
              fstMenu: 'homepage',
              hideInBreadcrumb: true
            },
            component: MyTodo
          }
        ]
      },
      {
        path: 'myDefect',
        name: 'myDefect',
        meta: {
          nameCn: '我的缺陷',
          fstMenu: 'homepage',
          secMenu: 'myDefect',
          icon: 'myDefect'
        },
        children: [
          {
            path: '',
            name: 'my-defect',
            meta: {
              nameCn: '我的缺陷',
              fstMenu: 'homepage',
              hideInBreadcrumb: true
            },
            component: MyDefect
          }
        ]
      },
      {
        path: 'myApp',
        name: 'myApp',
        meta: {
          nameCn: '我的应用',
          fstMenu: 'homepage',
          secMenu: 'myApp',
          icon: 'myApp'
        },
        children: [
          {
            path: '',
            name: 'my-app',
            meta: {
              nameCn: '我的应用',
              fstMenu: 'homepage',
              hideInBreadcrumb: true
            },
            component: MyApp
          }
        ]
      },
      {
        path: 'myFrame',
        name: 'myFrame',
        meta: {
          nameCn: '我的基础架构',
          fstMenu: 'homepage',
          secMenu: 'myFrame',
          icon: 'myFrame'
        },
        children: [
          {
            path: '',
            name: 'my-frame',
            meta: {
              nameCn: '我的基础架构',
              fstMenu: 'homepage',
              hideInBreadcrumb: true
            },
            component: MyFrame
          }
        ]
      },
      {
        path: 'myProdProblems',
        name: 'myProdProblems',
        meta: {
          nameCn: '我的生产问题',
          fstMenu: 'homepage',
          secMenu: 'myProdProblems',
          icon: 'myProdProblems'
        },
        children: [
          {
            path: '',
            name: 'my-production-problem',
            meta: {
              nameCn: '我的生产问题',
              fstMenu: 'homepage',
              hideInBreadcrumb: true
            },
            component: MyProductionProblem
          }
        ]
      },
      {
        path: 'myExamineApprove',
        name: 'myExamineApprove',
        meta: {
          nameCn: '我的审批',
          fstMenu: 'homepage',
          secMenu: 'myExamineApprove',
          icon: 'myExamineApprove'
        },
        children: [
          {
            path: 'myUnitApprove',
            name: 'myUnitApprove',
            meta: {
              nameCn: '研发单元审批',
              fstMenu: 'homepage',
              secMenu: 'myExamineApprove'
            },
            component: myUnitApprove
          },
          {
            path: 'codeMergeApprove',
            name: 'codeMergeApprove',
            meta: {
              nameCn: '分支合并审批',
              fstMenu: 'homepage',
              secMenu: 'myExamineApprove'
            },
            component: codeMergeApprove
          },
          {
            path: 'myRqrApproval',
            name: 'myRqrApproval',
            meta: {
              nameCn: '需求评估定稿审核',
              fstMenu: 'homepage',
              secMenu: 'myExamineApprove'
            },
            component: myRqrEvaluateApprove
          },
          {
            path: 'mySitApproval',
            name: 'mySitApproval',
            meta: {
              nameCn: 'Sit2环境部署审批申请',
              fstMenu: 'homepage',
              secMenu: 'myExamineApprove'
            },
            component: mySit2Approval
          }
        ]
      }
    ]
  }
];
