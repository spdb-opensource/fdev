const interfaceMasterList = () =>
  import('@/modules/interface/views/interfaceList/index');
const interfaceProfile = () =>
  import('@/modules/interface/views/interfaceProfile/index');
const interfaceRelationMasterList = () =>
  import('@/modules/interface/views/interfaceRelation/index');
const YapiList = () => import('@/modules/interface/views/yapi/index');
const YapiProfile = () => import('@/modules/interface/views/yapi/profile');
const SchemaConcersion = () =>
  import('@/modules/interface/views/yapi/schemaConcersion');
const InterfaceTrading = () =>
  import('@/modules/interface/views/tradeList/index');
const invokeRelationships = () =>
  import('@/modules/interface/views/tradeRelation/index');
const TransProfile = () =>
  import('@/modules/interface/views/tradeProfile/index');
const ScanRecord = () => import('@/modules/interface/views/scanRecord/index');
const interfaceApplyList = () =>
  import('@/modules/interface/views/interfaceApply/index');
const routerList = () => import('@/modules/interface/views/routerList/index');
const routerRelation = () =>
  import('@/modules/interface/views/routerRelation/index');
const routerAppConfig = () =>
  import('@/modules/interface/views/routerAppConfig/index');
const routerAppTotal = () =>
  import('@/modules/interface/views/routerAppTotal/index');
const routerProfile = () =>
  import('@/modules/interface/views/routerProfile/index');
const ServiceLink = () => import('@/modules/interface/views/serviceLink/index');
const InterfaceStatistics = () =>
  import('@/modules/interface/views/interfaceStatistics/index');

export default [
  {
    path: '/interfaceAndRoute',
    name: 'interfaceAndRoute',
    meta: {
      nameCn: '接口及路由',
      fstMenu: 'appAndConfig',
      secMenu: 'interfaceAndRoute',
      icon: 'interfaceAndRoute'
    },
    children: [
      {
        path: 'interface',
        name: 'interface',
        meta: {
          nameCn: '接口', // 小组,公司,角色,权限
          fstMenu: 'appAndConfig',
          secMenu: 'interfaceAndRoute'
        },
        children: [
          {
            path: 'interfaceList',
            name: 'interfaceList',
            meta: {
              nameCn: '接口列表',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute'
            },
            component: interfaceMasterList
          },
          {
            path: 'interfaceRelation',
            name: 'interfaceRelation',
            meta: {
              nameCn: '调用关系',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute'
            },
            component: interfaceRelationMasterList
          },
          {
            path: 'interfaceProfile/:id',
            name: 'interfaceProfile',
            meta: {
              nameCn: '接口详情',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute',
              hideInMenu: true
            },
            component: interfaceProfile
          },
          {
            path: 'yapi',
            meta: {
              nameCn: 'Yapi项目导入',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute'
            },
            name: 'YapiProjectImport',
            component: YapiList
          },
          {
            path: 'yapi/:token',
            props: true,
            meta: {
              nameCn: '项目详情',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute',
              hideInMenu: true
            },
            name: 'yapiProfile',
            component: YapiProfile
          },
          {
            path: 'schemaConcersion',
            meta: {
              nameCn: 'Schema转换',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute',
              hideInMenu: true
            },
            name: 'SchemaConcersion',
            component: SchemaConcersion
          }
        ]
      },
      {
        path: 'interfaceTrading',
        name: 'interfaceTrading',
        meta: {
          nameCn: '交易', // 小组,公司,角色,权限
          fstMenu: 'appAndConfig',
          secMenu: 'interfaceAndRoute'
        },
        children: [
          {
            path: 'list',
            name: 'interfaceTradingList',
            meta: {
              nameCn: '交易列表',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute'
            },
            component: InterfaceTrading
          },
          {
            path: 'list/:id',
            name: 'interfaceTradingProfile',
            meta: {
              nameCn: '交易详情',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute',
              hideInMenu: true
            },
            component: TransProfile
          },
          {
            path: 'invokeRelationships',
            name: 'invokeRelationships',
            meta: {
              nameCn: '调用关系',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute'
            },
            component: invokeRelationships
          }
        ]
      },
      {
        path: 'dynamic',
        name: 'dynamic',
        meta: {
          nameCn: '动态', // 小组,公司,角色,权限
          fstMenu: 'appAndConfig',
          secMenu: 'interfaceAndRoute'
        },
        children: [
          {
            path: 'scanRecordList',
            name: 'scanningRecords',
            meta: {
              nameCn: '扫描记录',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute'
            },
            component: ScanRecord
          }
        ]
      },
      {
        path: 'interfaceCall',
        name: 'approval',
        meta: {
          nameCn: '审批',
          fstMenu: 'appAndConfig',
          secMenu: 'interfaceAndRoute'
        },
        children: [
          {
            path: 'interfaceCall',
            name: 'interfaceApproval',
            meta: {
              nameCn: '接口审批',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute'
            },
            component: interfaceApplyList
          }
        ]
      },
      {
        path: 'routerList',
        name: 'frontRouter',
        meta: {
          nameCn: '前端路由',
          fstMenu: 'appAndConfig',
          secMenu: 'interfaceAndRoute'
        },
        children: [
          {
            path: 'routerList',
            name: 'routerList',
            meta: {
              nameCn: '路由',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute'
            },
            component: routerList
          },
          {
            path: 'routerRelation',
            name: 'routerRelation',
            meta: {
              nameCn: '路由关系',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute'
            },
            component: routerRelation
          },
          {
            path: 'routerAppConfig',
            name: 'routerAppConfig',
            meta: {
              nameCn: '应用路由配置介质',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute'
            },
            component: routerAppConfig
          },
          {
            path: 'routerAppTotal',
            name: 'routerAppTotal',
            meta: {
              nameCn: '总路由配置介质',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute'
            },
            component: routerAppTotal
          },
          {
            path: 'routerProfile/:id',
            name: 'routerProfile',
            meta: {
              nameCn: '路由详情',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute',
              hideInMenu: true
            },
            component: routerProfile
          }
        ]
      },
      {
        path: 'InterfaceStatistics',
        name: 'InterfaceStatistics',
        meta: {
          nameCn: 'FDEV',
          fstMenu: 'appAndConfig',
          secMenu: 'interfaceAndRoute',
          hideInMenu: true
        },
        children: [
          {
            path: 'interfaceStatistics',
            name: 'InterfaceStatistic',
            meta: {
              nameCn: '接口统计',
              fstMenu: 'appAndConfig',
              secMenu: 'interfaceAndRoute',
              hideInMenu: true
            },
            component: InterfaceStatistics
          }
        ]
      },
      {
        path: 'serviceLink',
        meta: {
          nameCn: '服务链路',
          fstMenu: 'appAndConfig',
          secMenu: 'interfaceAndRoute'
        },
        name: 'ServiceLink',
        component: ServiceLink
      }
    ]
  }
];
