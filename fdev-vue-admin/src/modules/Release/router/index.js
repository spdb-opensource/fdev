const Release = () => import('@/modules/Release/views/ReleaseList');
const BigReleaseDetail = () =>
  import('@/modules/Release/views/ReleaseBigWindow/Detail');
const Process = () =>
  import('@/modules/Release/views/ReleaseBigWindow/Process');
const Contact = () =>
  import('@/modules/Release/views/ReleaseBigWindow/Contact');
const Demand = () => import('@/modules/Release/views/ReleaseBigWindow/Demand');
const Test = () => import('@/modules/Release/views/ReleaseBigWindow/Test');
const Security = () =>
  import('@/modules/Release/views/ReleaseBigWindow/Security');
const Detail = () => import('@/modules/Release/views/Manage/Detail');
const ReleaseJob = () => import('@/modules/Release/views/Manage/JobList');
const TestRunAppList = () =>
  import('@/modules/Release/views/Manage/TestRunAppList');
const ReleaseAppList = () => import('@/modules/Release/views/Manage/AppList');
const ReleaseComponentList = () =>
  import('@/modules/Release/views/Manage/componentList');
const CreateUpdate = () =>
  import('@/modules/Release/views/Manage/createUpdate');
const AutoReleaseNote = () =>
  import('@/modules/Release/views/Manage/autoReleaseNote');
const ModifiedFiles = () =>
  import('@/modules/Release/views/Manage/ModifiedFiles');
const FileUpload = () => import('@/modules/Release/views/Manage/FileUpload');
const UpdateFileConfig = () =>
  import('@/modules/Release/views/Manage/UpdateFileConfig');
const AppScale = () => import('@/modules/Release/views/Manage/AppScale');
const UpdateDetail = () =>
  import('@/modules/Release/views/Manage/updateDetail');
const AutoReleaseNoteDetail = () =>
  import('@/modules/Release/views/Manage/autoReleaseNoteDetail');
const UpdateList = () => import('@/modules/Release/views/Manage/updateList');
const UpdateFileManage = () =>
  import('@/modules/Release/views/Manage/updateFileManage');
const ChangesPlans = () => import('@/modules/Release/views/ChangesPlans');

const DatabaseUpdate = () =>
  import('@/modules/Release/views/Manage/DatabaseUpdate');
const ParamsMaintain = () => import('@/modules/Release/views/ParamsMaintain');

const ChangeTemplate = () => import('@/modules/Release/views/ChangeTemplate');
const Profile = () => import('@/modules/Release/views/ChangeTemplate/Detail');
const ReviewRelatedItems = () =>
  import('@/modules/Release/views/ReviewRelatedItems');
const ReviewDetails = () => import('@/modules/Release/views/ReviewDetails');
const AutoReleaseNoteAppList = () =>
  import('@/modules/Release/views/Manage/autoReleaseNoteAppList');
const AutoReleaseNoteConfigFile = () =>
  import('@/modules/Release/views/Manage/autoReleaseNoteConfigFile');
const AutoReleaseNoteDatabase = () =>
  import('@/modules/Release/views/Manage/autoReleaseNoteDatabase');
const ExtPublishAutoRelease = () =>
  import('@/modules/Release/views/Manage/extPublishAutoRelease');
// 生产问题列表
const ProductionProblemsList = () =>
  import('@/modules/Release/views/ProductionProblems/List');
const ProductionProblemsProfile = () =>
  import('@/modules/Release/views/ProductionProblems/Profile');
export default [
  {
    path: '/release',
    name: 'release',
    meta: {
      nameCn: '投产管理',
      icon: 'portfolio',
      fstMenu: 'release'
    },
    children: [
      {
        path: 'list',
        name: 'releaseList',
        meta: {
          nameCn: '投产窗口列表',
          fstMenu: 'release',
          secMenu: 'releaseList',
          icon: 'releaseList'
        },
        component: Release
      },
      {
        path: 'changesPlans',
        name: 'changesPlans',
        meta: {
          nameCn: '变更计划',
          fstMenu: 'release',
          secMenu: 'changesPlans',
          icon: 'changesPlans'
        },
        component: ChangesPlans
      },
      {
        path: 'changeTemplate',
        name: 'changeTemplate',
        meta: {
          nameCn: '变更模板管理',
          fstMenu: 'release',
          secMenu: 'changeTemplate',
          icon: 'changeTemplate'
        },
        component: ChangeTemplate
      },
      {
        path: 'templateDetail/:id',
        name: 'templateDetail',
        meta: {
          nameCn: '详情',
          fstMenu: 'release',
          secMenu: 'templateDetail',
          hideInMenu: true
        },
        component: Profile
      },
      {
        path: 'ReviewDetails/:id',
        name: 'ReviewDetails',
        meta: {
          nameCn: '详情',
          fstMenu: 'release',
          secMenu: 'ReviewDetails',
          hideInMenu: true
        },
        component: ReviewDetails
      },
      {
        path: 'updateFileManage/:id',
        name: 'UpdateFileManage',
        meta: {
          nameCn: '变更文件列表',
          fstMenu: 'release',
          secMenu: 'UpdateFileManage',
          hideInMenu: true
        },
        component: UpdateFileManage
      },
      {
        path: 'autoReleaseNoteDetail/:id',
        name: 'autoReleaseNoteDetail',
        meta: {
          nameCn: '发布说明管理',
          fstMenu: 'release',
          secMenu: 'autoReleaseNoteDetail',
          hideInMenu: true
        },
        component: AutoReleaseNoteDetail,
        children: [
          {
            path: 'applist',
            meta: {
              nameCn: '应用列表',
              fstMenu: 'release',
              secMenu: 'autoReleaseNoteDetail',
              hideInMenu: true
            },
            component: AutoReleaseNoteAppList
          },
          {
            path: 'configFile',
            meta: {
              nameCn: '配置文件文件列表',
              fstMenu: 'release',
              secMenu: 'autoReleaseNoteDetail',
              hideInMenu: true
            },
            component: AutoReleaseNoteConfigFile
          },
          {
            path: 'database',
            meta: {
              nameCn: '数据库列表',
              fstMenu: 'release',
              secMenu: 'autoReleaseNoteDetail',
              hideInMenu: true
            },
            component: AutoReleaseNoteDatabase
          },
          {
            path: 'extPublish',
            meta: {
              nameCn: '批量任务列表',
              fstMenu: 'release',
              secMenu: 'autoReleaseNoteDetail',
              hideInMenu: true
            },
            component: ExtPublishAutoRelease
          }
        ]
      },
      {
        path: 'updateDetail/:id',
        name: 'updateDetail',
        meta: {
          nameCn: '变更管理',
          fstMenu: 'release',
          secMenu: 'updateDetail',
          hideInMenu: true
        },
        component: UpdateDetail,
        children: [
          {
            path: 'updateList',
            meta: {
              nameCn: '变更应用列表',
              fstMenu: 'release',
              secMenu: 'updateDetail'
            },
            component: UpdateList
          },
          {
            path: 'FileUpload',
            meta: {
              nameCn: '变更文件列表',
              fstMenu: 'release',
              secMenu: 'updateDetail'
            },
            component: FileUpload
          },
          {
            path: 'AppScale',
            meta: {
              nameCn: '弹性扩展',
              fstMenu: 'release',
              secMenu: 'updateDetail'
            },
            component: AppScale
          },
          {
            path: 'DatabaseUpdate',
            meta: {
              nameCn: '数据库更新',
              fstMenu: 'release',
              secMenu: 'updateDetail'
            },
            component: DatabaseUpdate
          },
          {
            path: 'extPublish',
            meta: {
              nameCn: '批量任务列表',
              fstMenu: 'release'
            },
            component: ExtPublishAutoRelease
          }
        ]
      },
      {
        path: 'list/:id',
        name: 'list',
        meta: {
          nameCn: '详情',
          fstMenu: 'release',
          secMenu: 'list',
          hideInMenu: true
        },
        component: Detail,
        children: [
          {
            path: 'joblist',
            meta: {
              nameCn: '投产任务列表',
              fstMenu: 'release',
              secMenu: 'list',
              hideInMenu: true
            },
            component: ReleaseJob
          },
          {
            path: 'testRunAppList',
            meta: {
              nameCn: '试运行任务列表',
              fstMenu: 'release',
              secMenu: 'list',
              hideInMenu: true
            },
            component: TestRunAppList
          },
          {
            path: 'applist',
            meta: {
              nameCn: '投产应用列表',
              fstMenu: 'release',
              secMenu: 'list',
              hideInMenu: true
            },
            component: ReleaseAppList
          },
          {
            path: 'componentlist',
            meta: {
              nameCn: '投产组件列表',
              fstMenu: 'release',
              secMenu: 'list',
              hideInMenu: true
            },
            component: ReleaseComponentList
          },
          {
            path: 'archetypeList',
            meta: {
              nameCn: '投产骨架列表',
              fstMenu: 'release',
              secMenu: 'list',
              hideInMenu: true
            },
            component: ReleaseComponentList
          },
          {
            path: 'imageList',
            meta: {
              nameCn: '投产镜像列表',
              fstMenu: 'release',
              secMenu: 'list',
              hideInMenu: true
            },
            component: ReleaseComponentList
          },
          {
            path: 'CreateUpdate',
            meta: {
              nameCn: '变更列表',
              fstMenu: 'release',
              secMenu: 'list',
              hideInMenu: true
            },
            component: CreateUpdate
          },
          {
            path: 'autoReleaseNote',
            meta: {
              nameCn: '发布说明',
              fstMenu: 'release',
              secMenu: 'list',
              hideInMenu: true
            },
            component: AutoReleaseNote
          },
          {
            path: 'modifiedFiles',
            meta: {
              nameCn: '投产文档汇总',
              fstMenu: 'release',
              secMenu: 'list',
              hideInMenu: true
            },
            component: ModifiedFiles
          },
          {
            path: 'updateFileConfig',
            meta: {
              nameCn: '配置文件变更',
              fstMenu: 'release',
              secMenu: 'list',
              hideInMenu: true
            },
            component: UpdateFileConfig
          }
        ]
      },
      {
        path: 'paramsMaintain',
        name: 'paramsMaintain',
        meta: {
          nameCn: '参数维护',
          fstMenu: 'release',
          secMenu: 'paramsMaintain',
          icon: 'paramsMaintain'
        },
        component: ParamsMaintain
      },
      {
        path: 'reviewRelatedItems',
        name: 'reviewRelatedItems',
        meta: {
          nameCn: '数据库变更审核',
          fstMenu: 'release',
          secMenu: 'reviewRelatedItems',
          icon: 'reviewRelatedItems'
        },
        component: ReviewRelatedItems
      },
      {
        path: 'BigReleaseDetail/:release_date',
        meta: {
          nameCn: '投产大窗口详情',
          fstMenu: 'release',
          secMenu: 'BigReleaseDetail',
          hideInMenu: true
        },
        component: BigReleaseDetail,
        name: 'BigReleaseDetail',
        children: [
          {
            name: 'Process',
            path: 'process',
            meta: {
              hideInMenu: true,
              nameCn: '投产流程说明',
              fstMenu: 'release',
              secMenu: 'BigReleaseDetail'
            },
            component: Process
          },
          {
            name: 'Contact',
            path: 'contact',
            meta: {
              hideInMenu: true,
              nameCn: '投产联系人/变更会同人员',
              fstMenu: 'release',
              secMenu: 'BigReleaseDetail'
            },
            component: Contact
          },
          {
            name: 'Demand',
            path: 'demand',
            meta: {
              hideInMenu: true,
              nameCn: '需求列表',
              fstMenu: 'release',
              secMenu: 'BigReleaseDetail'
            },
            component: Demand
          },
          {
            name: 'Test',
            path: 'test',
            meta: {
              hideInMenu: true,
              nameCn: '提测重点',
              fstMenu: 'release',
              secMenu: 'BigReleaseDetail'
            },
            component: Test
          },
          {
            name: 'Security',
            path: 'security',
            meta: {
              hideInMenu: true,
              nameCn: '安全测试需求',
              fstMenu: 'release',
              secMenu: 'BigReleaseDetail'
            },
            component: Security
          }
        ]
      },
      // 生产问题列表
      {
        path: 'productionProblemsList',
        name: 'productionProblemsList',
        meta: {
          nameCn: '问题列表',
          fstMenu: 'release',
          secMenu: 'productionProblemsList',
          icon: 'problemList'
        },
        component: ProductionProblemsList
      },
      {
        path: 'ProductionProblems/:id',
        name: 'ProductionProblemsProfile',
        meta: {
          nameCn: '生产问题详情',
          hideInMenu: true,
          fstMenu: 'release',
          secMenu: 'problemList'
        },
        component: ProductionProblemsProfile
      }
    ]
  }
];
