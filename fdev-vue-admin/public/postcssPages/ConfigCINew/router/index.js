const FileContentModify = () => import('../views/FileContentModify.vue');
const PipelineLogTable = () => import('../views/pipelineLogTable.vue');
const LogList = () => import('../components/logList.vue');
// const MdToHtml = () => import('../views/StepManage/mdToHtml.vue');
const PipelineManage = () => import('../views/PipelineManage.vue');
const LogPanorama = () => import('../views/LogPanorama.vue');
const JobLogProfile = () => import('../views/JobLogProfile.vue');
const CIDetail = () => import('../views/CIDetail.vue');
const PipelinePanorama = () => import('../views/PipelinePanorama.vue');
const TriggerCfg = () => import('../components/TriggerCfg.vue');

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
        path: 'pipelineLogTable',
        name: 'pipelineLogTable',
        meta: {
          nameCn: '流水线管理',
          fstMenu: 'configCI',
          secMenu: 'pipelineLogTable',
          icon: 'pipelineLogTable'
        },
        component: PipelineLogTable
      },
      {
        path: 'logList/:id',
        name: 'logList',
        meta: {
          hideInMenu: true,
          nameCn: '流水线日志列表',
          fstMenu: 'configCI',
          secMenu: 'logList'
        },
        component: LogList
      },
      // {
      //   path: 'pipelineManage/:pluginCode',
      //   name: 'mdToHtml',
      //   meta: {
      //     hideInMenu: true
      //   },
      //   component: MdToHtml
      // },
      {
        path: 'pipelineManage/:id/:fromType',
        name: 'pipelineManage',
        props: true,
        meta: {
          hideInMenu: true,
          nameCn: '流水线编辑',
          fstMenu: 'configCI',
          secMenu: 'pipelineManage'
        },
        component: PipelineManage
      },
      {
        path: 'pipelineManage/:id/:template',
        name: 'pipelineManage',
        props: true,
        meta: {
          hideInMenu: true,
          nameCn: '流水线编辑',
          fstMenu: 'configCI',
          secMenu: 'pipelineManage'
        },
        component: PipelineManage
      },
      {
        path: 'logPanorama/:id/:isPop',
        name: 'logPanorama',
        props: true,
        meta: {
          hideInMenu: true,
          nameCn: '流水线日志全景图',
          fstMenu: 'configCI',
          secMenu: 'logPanorama'
        },
        component: LogPanorama
      },
      {
        path: 'jobLogProfile/:id',
        name: 'JobLogProfile',
        props: true,
        meta: {
          hideInMenu: true,
          nameCn: '任务日志详情',
          fstMenu: 'configCI',
          secMenu: 'JobLogProfile'
        },
        component: JobLogProfile
      },
      {
        path: 'CIDetail/:id',
        name: 'CIDetail',
        props: true,
        meta: {
          hideInMenu: true,
          nameCn: 'CI详情',
          fstMenu: 'configCI',
          secMenu: 'CIDetail'
        },
        component: CIDetail,
        children: [
          {
            path: 'panorama',
            props: true,
            component: PipelinePanorama,
            meta: {
              nameCn: '流水线详情',
              fstMenu: 'configCI',
              secMenu: 'CIDetail'
            }
          },
          {
            path: 'triggerRules',
            props: true,
            component: TriggerCfg,
            meta: {
              nameCn: '触发规则',
              fstMenu: 'configCI',
              secMenu: 'CIDetail'
            }
          }
        ]
      },
      {
        path: 'FileContentModify',
        name: 'FileContentModify',
        meta: {
          nameCn: '配置文件编辑',
          fstMenu: 'configCI',
          secMenu: 'FileContentModify',
          hideInMenu: true
        },
        component: FileContentModify
      }
    ]
  }
];
