const PipelineList = () =>
  import('@/modules/configCI/views/PipelineList/index.vue');
const ModelListNew = () =>
  import('@/modules/configCI/views/modelListNew/index.vue');
const ModelDetail = () =>
  import('@/modules/configCI/views/modelListNew/detail.vue');
const PipelineDetail = () => import('@/modules/configCI/views/PipelineDetail');
const LogPanorama = () => import('@/modules/configCI/views/LogPanorama.vue');
const MdToHtml = () =>
  import('@/modules/configCI/views/StepManage/mdToHtml.vue');
const PipelineManage = () =>
  import('@/modules/configCI/views/PipelineManage.vue');
const JobLogProfile = () =>
  import('@/modules/configCI/views/JobLogProfile.vue');
const logList = () => import('@/modules/configCI/components/logList.vue');
const TriggerRule = () => import('@/modules/configCI/views/TriggerRule');
const Tool = () => import('@/modules/configCI/views/ToolManage/Tool.vue');
const HistoryPanorama = () =>
  import('@/modules/configCI/views/HistoryPanorama.vue');

export default [
  {
    path: '/configCI',
    name: 'configCI',
    meta: {
      nameCn: '持续集成',
      fstMenu: 'configCI',
      icon: 'configci'
    },
    children: [
      {
        path: 'pipelineList',
        name: 'pipelineLogTable',
        component: PipelineList,
        meta: {
          fstMenu: 'configCI',
          secMenu: 'pipelineLogTable',
          nameCn: '流水线管理',
          icon: 'pipelineLogTable'
        }
      },
      {
        path: 'logList/:id',
        name: 'logList',
        component: logList,
        meta: {
          fstMenu: 'configCI',
          secMenu: 'logList',
          nameCn: '运行日志列表',
          hideInMenu: true
        }
      },
      {
        path: 'toolbox',
        name: 'toolbox',
        component: Tool,
        meta: {
          fstMenu: 'configCI',
          secMenu: 'toolbox',
          nameCn: '工具箱',
          icon: 'toolbox'
        }
      },
      {
        path: 'pipelineManage/:pluginCode',
        name: 'mdToHtml',
        component: MdToHtml,
        meta: {
          fstMenu: 'configCI',
          secMenu: 'mdToHtml',
          nameCn: '流水线编辑',
          hideInMenu: true
        }
      },
      {
        path: 'pipelineManage/:id/:fromType',
        name: 'pipelineEdit',
        props: true,
        component: PipelineManage,
        meta: {
          fstMenu: 'configCI',
          secMenu: 'pipelineEdit',
          nameCn: '流水线编辑',
          hideInMenu: true
        }
      },
      {
        path: 'pipelineManage/:id/:template',
        name: 'pipelineManage',
        props: true,
        component: PipelineManage,
        meta: {
          fstMenu: 'configCI',
          secMenu: 'pipelineManage',
          nameCn: '流水线编辑',
          hideInMenu: true
        }
      },
      {
        path: 'logPanorama/:id',
        name: 'logPanorama',
        props: true,
        component: LogPanorama,
        meta: {
          fstMenu: 'configCI',
          secMenu: 'logPanorama',
          nameCn: '流水线日志全景图',
          hideInMenu: true
        }
      },
      {
        path: 'jobLogProfile/:id',
        name: 'JobLogProfile',
        props: true,
        component: JobLogProfile,
        meta: {
          fstMenu: 'configCI',
          secMenu: 'JobLogProfile',
          nameCn: 'job日志详情',
          hideInMenu: true
        }
      },
      {
        path: 'pipelineDetail/:id',
        name: 'PipelineDetail',
        props: true,
        component: PipelineDetail,
        meta: {
          fstMenu: 'configCI',
          secMenu: 'PipelineDetail',
          nameCn: '流水线详情',
          hideInMenu: true
        }
      },
      {
        path: 'triggerRule/:id',
        name: 'TriggerRule',
        props: true,
        component: TriggerRule,
        meta: {
          fstMenu: 'configCI',
          secMenu: 'TriggerRule',
          nameCn: '触发规则',
          hideInMenu: true
        }
      },
      {
        path: 'historyPanorama/:id',
        name: 'HistoryPanorama',
        props: true,
        component: HistoryPanorama,
        meta: {
          fstMenu: 'configCI',
          secMenu: 'HistoryPanorama',
          nameCn: '流水线历史详情',
          hideInMenu: true
        }
      },
      {
        path: 'modelListNew',
        name: 'modelListNew',
        meta: {
          fstMenu: 'configCI',
          secMenu: 'modelListNew',
          nameCn: '新实体管理',
          icon: 'modelListNew'
        },
        component: ModelListNew
      },
      {
        path: 'modelListNew/:id',
        name: 'modelDetail',
        meta: {
          hideInMenu: true,
          nameCn: '新实体管理详情',
          fstMenu: 'configCI',
          secMenu: 'modelDetail'
        },
        component: ModelDetail
      }
    ]
  }
];
