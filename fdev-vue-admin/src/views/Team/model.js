export const list = {
  IntegratedArch: {
    introduce: {
      main:
        '集成架构团队主要负责偏基础设置的运维及中间件的最佳使用制定，主要包含及不限于以下',
      item: [
        '基础设置安装运维 (maven 仓库、node 仓库、x86虚拟机、签名验签机、外网域名、SSL 、CaaS 信息、NAS存储、对象存储、数据库审核 等等)。',
        '中间件使用最佳实践（redis、kafka 、nginx、mongodb 等等）。',
        '问题排查经验文档沉淀。'
      ]
    },
    issue: {
      main: 'http://xxx/ebank_fdev/IntegratedArch_issues/issues',
      item: [
        '描述环境信息: 操作系统及版本、中间件及版本',
        '问题相关日志。',
        '问题复现条件'
      ]
    },
    knowledge: [
      {
        title: '集成架构知识库',
        url: 'http: //xxx/common/hf14k'
      }
    ],
    thanks: [],
    scan: [
      'http://xxx/ebank/devops/fdev-config-test/raw/master/congfig-repo/WechatIMG1.jpeg',
      'http://xxx/ebank/devops/fdev-config-test/raw/master/congfig-repo/IMG_6585.JPG'
    ]
  },
  AppArch: {
    introduce: {
      main:
        '应用架构团队主要负责网银系统群应用架构设计和应用框架维护，持续优化应用框架及组件以服务各开发团队，提升开发效率。',
      item: [
        '微服务架构（Gateway，Eureka Server，Config Server）。',
        '容器化。',
        '微服务骨架（web微服务，online微服务，分布式批量调度）。',
        '服务端组件（开源组件，自研组件）'
      ]
    },
    issue: {
      main: 'http://xxx/ebank_fdev/AppArch_issues/issues',
      item: [
        '描述环境信息: 操作系统及版本、中间件及版本',
        '问题相关日志。',
        '问题复现条件'
      ]
    },
    knowledge: [
      {
        title: '应用架构知识库',
        url: 'xxx/apparch/'
      }
    ],
    thanks: [],
    scan: [
      'http://xxx/ebank/devops/fdev-config-test/raw/master/congfig-repo/WechatIMG1.jpeg',
      'http://xxx/ebank/devops/fdev-config-test/raw/master/congfig-repo/IMG_6585.JPG'
    ]
  },
  AppDev: {
    introduce: {
      main:
        '基础应用团队主要负责网银公共基础应用开发维护和对外基础公共能力的提供，主要包含但不限与以下',
      item: [
        '公共系统：网银柜面及微服务，电子银行管理端，网银批量，内外网动态展示，网银相关证书管理，搜索服务，etc。',
        '基础能力：公共系统对外提供服务及升级，搜索定制化等。',
        '问题排查经验文档记录。'
      ]
    },
    issue: {
      main: 'http://xxx/ebank_fdev/AppDev_issues/issues',
      item: ['建议来源，改进方向。', '问题相关日志。', '环境信息及问题复现。']
    },
    knowledge: [
      {
        title: '基础应用知识库',
        url: 'xxx/common/6f70k'
      }
    ],
    thanks: [],
    scan: [
      'http://xxx/ebank/devops/fdev-config-test/raw/master/congfig-repo/IMG_6585.JPG'
    ]
  },
  mpaas: {
    introduce: {
      main:
        'Mpaas团队主要负责手机银行前端基础公共能力的开发和维护，主要包含但不限于以下',
      item: [
        '公共支持：视觉规范提供和审核，开发框架支持，组件支持，服务端能力支持，原生重构与规范支持，性能优化支持，公共能力支持。',
        '基础能力：VUE脚手架、VUE UI组件库、VUE服务组件库、RN脚手架、RN UI组件库、RN服务组件库，服务端fResource，埋点以及足迹采集，跨端能力支持以及原生基础规范和开发。',
        '前端开发流程和规范的沉淀、优化以及宣贯。'
      ]
    },
    issue: {
      main: 'http://xxx/ebank/mpaas-issues/issues/issues',
      item: [
        '有关Mpaas团队的问题和建议。',
        '业务开发支持需求。',
        '性能优化支持需求。'
      ]
    },
    knowledge: [
      {
        title: 'mpass 服务规范',
        url: 'xxx/mpaas/anm7r'
      },
      {
        title: 'mpass 组件库',
        url: 'xxx/mpaas/xyeo'
      },
      {
        title: 'mpass UI设计',
        url: 'xxx/mpaas/08e2o'
      }
    ],
    thanks: [],
    scan: [
      'http://xxx/ebank/devops/fdev-config-test/raw/master/congfig-repo/WechatIMG1.jpeg',
      'http://xxx/ebank/devops/fdev-config-test/raw/master/congfig-repo/IMG_6585.JPG'
    ]
  },
  fdev: {
    introduce: {
      main:
        '效能团队旨在帮助开发人员工作专业化、内测人员工作集中化、管理人员高效化。从需求受理到投产验证，涵盖整个软件开发生命周期，将全流程进行统一规范化操作。核心功能点如下：',
      item: [
        '应用持续构建部署一键式；',
        '环境配置统一管理（生产，测试）；',
        '需求、资源、预算管理；',
        '接口、路由、组件、基础镜像统一管理跟踪；',
        '用户相关任务、缺陷、生产问题、应用、代办等。'
      ]
    },
    issue: {
      main: 'http://xxx/ebank_fdev/fdev_issuses/issues',
      item: [
        '描述具体使用/操作的过程',
        '问题相关日志、截图',
        '问题复现条件',
        '提建议时，需说明项目组现状及是否已规范化推广',
        '提需求时，烦请邮件提供完整的需求说明书（最好包含页面原型）'
      ]
    },
    knowledge: [
      {
        title: 'fdev 相关接入手册',
        url: 'xxx/common/xndp9/82jl'
      }
    ],
    thanks: [
      {
        name: '邱芳',
        desc: '需求管理模块需求评估完整版需求的编写人，规范了需求评估流程'
      },
      {
        name: '吴磊',
        desc: '需求管理模块初创需求的编写人'
      },
      {
        name: '沈洁',
        desc: '前端UI审核流程需求的编写人，效能团队外部需求的典范'
      }
    ],
    scan: [
      'http://xxx/ebank/devops/fdev-config-test/raw/master/congfig-repo/WechatIMG1.jpeg',
      'http://xxx/ebank/devops/fdev-config-test/raw/master/congfig-repo/IMG_6585.JPG'
    ]
  },
  finance: {
    introduce: {
      main: '对公金融团队',
      item: []
    },
    issue: {
      main: 'http://xxx/ebank/ent/micro_ent_issues/-/issues',
      item: ['问题相关日志', '问题相关截图', '问题复现条件']
    },
    knowledge: [
      {
        title: '团队文档地址',
        url: 'http://xxx/ebank/ent/doc'
      },
      {
        title: '应用架构知识库',
        url: 'xxx/apparch/'
      },
      {
        title: '投产发布说明及相关介质',
        url: 'http://xxx/ebank/ent/release-notes'
      }
    ],
    thanks: [],
    scan: []
  }
};
