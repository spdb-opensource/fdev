const NetworkOpen = () => import('@/modules/Network/views/NetworkOpen');
const NetworkClose = () => import('@/modules/Network/views/NetworkClose');
const NetworkRemind = () => import('@/modules/Network/views/NetworkRemind');
const VDIApproval = () => import('@/modules/Network/views/VDIApproval');
const VMApproval = () => import('@/modules/Network/views/VMApproval');
const codeTool = () => import('@/modules/Network/views/CodeTool/index');
const codeIssueList = () => import('@/modules/Network/views/codeIssueList');
const codeToolDetail = () => import('@/modules/Network/views/CodeTool/detail');
const addOrders = () => import('@/modules/Network/views/CodeTool/addOrders');
const updateOrders = () =>
  import('@/modules/Network/views/CodeTool/updateOrders');
// const toolGuide = () => import('@/modules/Network/views/ToolGuide/index');
export default [
  {
    path: '/aAndA',
    name: 'aAndA',
    meta: {
      nameCn: '申请与审核',
      fstMenu: 'flowAndTool',
      secMenu: 'aAndA',
      icon: 'aAndA'
    },
    children: [
      {
        path: 'network',
        name: 'network',
        meta: {
          nameCn: '网络审核',
          fstMenu: 'flowAndTool',
          secMenu: 'aAndA'
        },
        children: [
          {
            path: 'networkOpen',
            name: 'networkOpen',
            meta: {
              nameCn: 'KF网络开通审核',
              fstMenu: 'flowAndTool',
              secMenu: 'aAndA'
            },
            component: NetworkOpen
          },
          {
            path: 'networkClose',
            name: 'networkClose',
            meta: {
              nameCn: 'KF网络关闭审核',
              fstMenu: 'flowAndTool',
              secMenu: 'aAndA'
            },
            component: NetworkClose
          },
          {
            path: 'networkRemind',
            name: 'networkRemind',
            meta: {
              nameCn: '网络关闭提醒',
              fstMenu: 'flowAndTool',
              secMenu: 'aAndA'
            },
            component: NetworkRemind
          },
          {
            path: 'vdiApproval',
            name: 'vdiApproval',
            meta: {
              nameCn: 'VDI网段迁移',
              fstMenu: 'flowAndTool',
              secMenu: 'aAndA'
            },
            component: VDIApproval
          },
          {
            path: 'vmApproval',
            name: 'vmApproval',
            meta: {
              nameCn: '虚拟机网段迁移',
              fstMenu: 'flowAndTool',
              secMenu: 'aAndA'
            },
            component: VMApproval
          }
        ]
      },
      {
        path: 'codeTool',
        name: 'codeTool',
        meta: {
          nameCn: '代码审核工单列表',
          fstMenu: 'flowAndTool',
          secMenu: 'aAndA'
        },
        component: codeTool
      },
      {
        path: 'codeIssueList',
        name: 'codeIssueList',
        meta: {
          nameCn: '代码审核',
          fstMenu: 'flowAndTool',
          secMenu: 'aAndA'
        },
        component: codeIssueList
      },
      {
        path: 'codeTool/:id',
        name: 'codeToolDetail',
        meta: {
          nameCn: '工单详情',
          fstMenu: 'flowAndTool',
          secMenu: 'aAndA',
          hideInMenu: true
        },
        component: codeToolDetail
      },
      {
        path: 'addOrders',
        name: 'addOrders',
        meta: {
          nameCn: '新建工单',
          fstMenu: 'flowAndTool',
          secMenu: 'aAndA',
          hideInMenu: true
        },
        component: addOrders
      },
      {
        path: 'updateOrders/:id',
        name: 'updateOrders',
        meta: {
          nameCn: '编辑工单',
          fstMenu: 'flowAndTool',
          secMenu: 'aAndA',
          hideInMenu: true
        },
        component: updateOrders
      }
    ]
  }
  // {
  //   path: 'flowAndTool/toolGuide',
  //   name: 'toolGuide',
  //   meta: {
  //     nameCn: '工具导航',
  //     fstMenu: 'flowAndTool',
  //     secMenu: 'toolGuide',
  //     icon: 'toolguide'
  //   },
  //   component: toolGuide
  // }
];
