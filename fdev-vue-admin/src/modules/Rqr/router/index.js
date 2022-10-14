const RequirementChart = () =>
  import('../../../../public/postcssPages/RequirementChart/index.vue');
const rqrList = () => import('@/modules/Rqr/list');
const devList = () => import('@/modules/Rqr/devList.vue');
const ipmpList = () => import('@/modules/Rqr/ipmpList.vue');
const RqrAdd = () => import('@/modules/Rqr/rqrAdd');
const RqrEdit = () => import('@/modules/Rqr/rqrEdit');
// const rqrProfile = () => import('@/modules/Rqr/rqrProfile');
const UnitDetail = () => import('@/modules/Rqr/Components/UnitDetail');
// const rqrEvaluate = () => import('@/modules/Rqr/rqrEvaluate');
const DesignReviewRqr = () => import('@/modules/Rqr/DesignReview');
const DemandDetail = () => import('@/modules/Rqr/views/demandDetail');
const OtherTaskDetail = () =>
  import('@/modules/Rqr/views/demandDetail/components/otherDemand/otherDmDetail');
const rqrEvaluateMgt = () =>
  import('@/modules/Rqr/views/rqrEvaluateMgt/index.vue');
const rqrEvaluateDetail = () =>
  import('@/modules/Rqr/views/rqrEvaluateMgt/detail');
const rdUnitApprovalList = () =>
  import('@/modules/Rqr/views/rdUnitApprovalList/index');
const submitTestList = () => import('@/modules/Rqr/views/submitTest/index');
const submitTestOrder = () =>
  import('@/modules/Rqr/views/submitTest/submitTestOrder');
const submitTestDetail = () =>
  import('@/modules/Rqr/views/submitTest/submitTestDetail');
export default [
  {
    path: '/rqrmn',
    name: 'rqrmn',
    meta: {
      nameCn: '需求管理',
      fstMenu: 'rAndD',
      secMenu: 'rqrmn',
      icon: 'rqrmn'
    },
    children: [
      {
        path: 'list',
        name: 'requirementList',
        meta: {
          nameCn: '需求列表',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: rqrList
      },
      {
        path: 'devList',
        name: 'devList',
        meta: {
          nameCn: '研发单元列表',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: devList
      },
      {
        path: 'ipmpList',
        name: 'ipmpList',
        meta: {
          nameCn: '实施单元列表',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: ipmpList
      },
      {
        path: 'rqrAdd',
        name: 'rqrAdd',
        meta: {
          nameCn: '需求新增',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: RqrAdd
      },
      {
        path: 'rqrEdit/:id',
        meta: {
          nameCn: '需求修改',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn',
          hideInMenu: true
        },
        component: RqrEdit
      },
      {
        path: 'rqrEvaluateMgt',
        name: 'rqrEvaluateMgt',
        meta: {
          nameCn: '需求评估管理',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: rqrEvaluateMgt
      },
      {
        path: 'rqrEvaluateMgt/:id',
        name: 'rqrEvaluateMgtDetail',
        meta: {
          nameCn: '评估详情',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn',
          hideInMenu: true
        },
        component: rqrEvaluateDetail
      },
      {
        path: 'chart',
        name: 'chart',
        meta: {
          nameCn: '需求全景图',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: RequirementChart
      },
      {
        path: 'rqrProfile/:id',
        name: 'rqrProfile',
        meta: {
          nameCn: '需求信息',
          hideInMenu: true,
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: DemandDetail
      },
      {
        path: 'ODTaskDetail/:id/:demandId',
        name: 'ODTaskDetail',
        meta: {
          nameCn: '其他需求任务信息',
          hideInMenu: true,
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: OtherTaskDetail
      },
      {
        path: 'unitDetail/:id/:demandId',
        name: 'unitDetail',
        meta: {
          nameCn: '实施单元详情',
          hideInMenu: true,
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: UnitDetail
      },
      {
        path: 'devUnitDetails',
        name: 'devUnitDetails',
        meta: {
          nameCn: '研发单元详情',
          hideInMenu: true,
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: () => import('@/modules/Rqr/Components/devUnitDetails')
      },
      {
        path: 'designReviewRqr/:id',
        name: 'designReviewRqr', //路由英文名,
        meta: {
          nameCn: '需求稿审核',
          hideInMenu: true,
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: DesignReviewRqr
      },
      {
        path: 'rdUnitApprovalList',
        name: 'rdUnitApprovalList',
        meta: {
          nameCn: '研发单元审批列表',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: rdUnitApprovalList
      },
      {
        path: 'submitTestList',
        name: 'submitTestList',
        meta: {
          nameCn: '提测单列表',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: submitTestList
      },
      {
        path: 'submitTestDetail/:id',
        name: 'submitTestDetail',
        meta: {
          nameCn: '提测单详情',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn'
        },
        component: submitTestDetail
      },
      {
        /* type 1为研发单元入口进入新增（需要传入研发单元id），2为提交列表页进入新增，type 3为复制进入新增（需要传入被复制单id） */
        path: 'submitTestOrder/:type',
        name: 'submitTestOrder',
        meta: {
          nameCn: '创建提测单',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn',
          hideInMenu: true
        },
        component: submitTestOrder
      },
      {
        /* 4为进入编辑页 */
        path: 'submitTestOrder/:type/:id',
        name: 'submitTestOrder',
        meta: {
          nameCn: '提测单修改',
          fstMenu: 'rAndD',
          secMenu: 'rqrmn',
          hideInMenu: true
        },
        component: submitTestOrder
      }
    ]
  }
];
