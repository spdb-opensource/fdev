const AppEnvList = () => import('@/modules/Environment/views/envList/index');
const ProductionInfoList = () =>
  import('@/modules/Environment/views/productionInformation/index');
const ProductionInfoDetails = () =>
  import('@/modules/Environment/views/productionInformation/details');
const AppModelList = () =>
  import('@/modules/Environment/views/modelList/index');
const AppModelEdit = () => import('@/modules/Environment/views/modelList/edit');
const AppModelDetail = () =>
  import('@/modules/Environment/views/modelList/detail');
const AppModeTempList = () =>
  import('@/modules/Environment/views/modelTemp/index');
const AppModelTempEdit = () =>
  import('@/modules/Environment/views/modelTemp/edit');
const AppModelTempDetail = () =>
  import('@/modules/Environment/views/modelTemp/detail');
const AppModelEvnList = () =>
  import('@/modules/Environment/views/modelEnv/index');
const AppListEdit = () => import('@/modules/Environment/views/modelEnv/edit');
// const EnvCopy = () => import('@/modules/Environment/model_env/envCopy');
const AppHistoryMsg = () =>
  import('@/modules/Environment/views/modelEnv/history');
const AloneConfFile = () =>
  import('@/modules/Environment/views/configDepAnalysis/index');
const ModelMessage = () =>
  import('@/modules/Environment/views/modelMessage/index');
const ModelGroupList = () =>
  import('@/modules/Environment/views/modelGroup/index');
const ModelGroupManage = () =>
  import('@/modules/Environment/views/modelGroup/manage');
const DeployMessage = () =>
  import('@/modules/Environment/views/deployMessage/index');
const DeployMessageHandlePage = () =>
  import('@/modules/Environment/views/deployMessage/handlePage');
const DeployMessageProfile = () =>
  import('@/modules/Environment/views/deployMessage/profile');
const colonyProductionInfo = () =>
  import('@/modules/Environment/views/productionInformation/count');

export default [
  {
    path: 'envModel',
    name: 'envModel',
    meta: {
      nameCn: '环境配置',
      fstMenu: 'appAndConfig',
      secMenu: 'envModel',
      icon: 'envModel'
    },
    children: [
      {
        path: 'envlist',
        name: 'envList',
        meta: {
          nameCn: '环境列表',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppEnvList
      },
      {
        path: 'colonyProductionInfo',
        name: 'colonyProductionInfo',
        meta: {
          nameCn: '集群生产信息',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: colonyProductionInfo
      },
      {
        path: 'productionInfoList',
        name: 'productionInfo',
        meta: {
          nameCn: '应用生产信息',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: ProductionInfoList
      },
      {
        path: 'productionInfoDetails/:appName',
        name: 'productionInfoDetails',
        meta: {
          nameCn: '应用详细',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel',
          hideInMenu: true
        },
        component: ProductionInfoDetails
      },
      {
        path: 'modelList',
        name: 'modelList',
        meta: {
          nameCn: '实体列表',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppModelList
      },
      {
        path: 'modelEdit/add',
        name: 'modelEditAdd',
        meta: {
          hideInMenu: true,
          nameCn: '新增实体',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppModelEdit
      },
      {
        path: 'modelEdit/update',
        name: 'modelEditUpdate',
        meta: {
          hideInMenu: true,
          nameCn: '更新实体',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppModelEdit
      },
      {
        path: 'modelEdit/copy',
        name: 'modelEditCopy',
        meta: {
          hideInMenu: true,
          nameCn: '复制实体',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppModelEdit
      },
      {
        path: 'modelList/:id',
        name: 'modelDetail',
        meta: {
          hideInMenu: true,
          nameCn: '实体详情',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppModelDetail
      },
      {
        path: 'modelTempList',
        name: 'modelTemplate',
        meta: {
          nameCn: '实体模板列表',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppModeTempList
      },
      {
        path: 'modelTempEdit/add',
        name: 'modelTempEdit',
        meta: {
          hideInMenu: true,
          nameCn: '新增实体模板',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppModelTempEdit
      },
      {
        path: 'modelTempList/:id',
        name: 'modelTempDetail',
        meta: {
          hideInMenu: true,
          nameCn: '实体模板详情',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppModelTempDetail
      },
      {
        path: 'modelEvnList',
        name: 'ModelEvnList',
        meta: {
          nameCn: '实体与环境映射',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppModelEvnList
      },
      {
        path: 'modelEvnList/add',
        name: 'modelEnvEditAdd',
        meta: {
          hideInMenu: true,
          nameCn: '新增实体与环境映射',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppListEdit
      },
      {
        path: 'modelEvnList/copy',
        name: 'modelEnvEditCopy',
        meta: {
          hideInMenu: true,
          nameCn: '复制实体与环境映射',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppListEdit
      },
      {
        path: 'modelEvnList/update',
        name: 'modelEnvEditUpdate',
        meta: {
          hideInMenu: true,
          nameCn: '更新实体与环境映射',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppListEdit
      },
      {
        path: 'modelEvnList/historyMsg/:id',
        name: 'modelEnvHistory',
        meta: {
          hideInMenu: true,
          nameCn: '历史信息',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AppHistoryMsg
      },
      {
        path: 'aloneConfFile',
        name: 'aloneConfFile',
        meta: {
          nameCn: '配置依赖分析',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: AloneConfFile
      },
      // {
      //   path: 'envCopy/:path',
      //   name: 'envCopy',
      //   meta: {
      //     hideInMenu: true,
      //     name: '实体环境映射复制'
      //   },
      //   component: EnvCopy
      // },
      {
        path: 'modelMessage',
        name: 'modelMessage',
        meta: {
          nameCn: '实体信息变动管理',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: ModelMessage
      },
      {
        path: 'modelGroupList',
        name: 'ModelGroupList',
        meta: {
          nameCn: '实体组',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: ModelGroupList
      },
      {
        path: 'DeployMessage',
        name: 'DeployMessage',
        meta: {
          nameCn: '部署信息',
          filter: 'DeployMessage',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: DeployMessage
      },
      {
        path: 'handle',
        name: 'DeployMessageHandlePage',
        meta: {
          nameCn: '编辑部署信息',
          hideInMenu: true,
          filter: 'DeployMessage',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: DeployMessageHandlePage
      },
      {
        path: 'profile/:appId',
        name: 'DeployMessageProfile',
        meta: {
          nameCn: '部署信息详情',
          hideInMenu: true,
          filter: 'DeployMessage',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: DeployMessageProfile
      },
      {
        path: 'modelGroupManage/add',
        name: 'ModelGroupManageAdd',
        meta: {
          hideInMenu: true,
          nameCn: '新增实体组',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: ModelGroupManage
      },
      {
        path: 'modelGroupManage/update',
        name: 'ModelGroupManageUpdate',
        meta: {
          hideInMenu: true,
          nameCn: '编辑实体组',
          fstMenu: 'appAndConfig',
          secMenu: 'envModel'
        },
        component: ModelGroupManage
      }
    ]
  }
];
