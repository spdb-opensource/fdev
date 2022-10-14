const mockjs = require('mockjs');

const unit = {
  id: '@id',
  redmine_id: '实施单元2017运营224-022',
  plan_start_time: '2017-12-29', //计划启动开发日期
  plan_uat_test_start_time: '2018-03-30', //计划提交用户测试日期
  plan_uat_test_stop_time: '2018-04-30', //计划用户测试完成日期
  plan_fire_time: '2018-05-31', //计划投产（完成）日期
  start_time: '2017-12-29', //实际启动日期
  start_inner_test_time: '', //实际用户测试启动日期
  stop_uat_test_time: '', //实际用户测试完成日期
  fire_time: '', //实际投产（完成）日期
  'type|1': ['0', '1'] //1日常任务,  0实施单元任务
};

const system = {
  id: '@id',
  name_en: '@string',
  name_cn: '@string'
};

const gitlabci = {
  id: '@id',
  name: '@string',
  yaml_name: '@string'
};

const domain = {
  id: '@id',
  name_en: '@string',
  'name_cn|1': ['用户域', '参数域', '日志流水域', '测试']
};

const archetype = {
  id: '@id',
  name: 'online骨架',
  archetype_type: '5c861ad1db2ef90007980df2',
  desc: '微服务联机应用骨架',
  artifactId: 'maven-archetype-mspe-online',
  groupId: 'com.csii.pe',
  version: '1.0.0-SNAPSHOT'
};

const env = [
  {
    name: 'schedule',
    var_list: [
      {
        name_zh: 'subnet',
        value: 'xxx/24',
        key: 'subnet'
      },
      {
        name_zh: 'profileName',
        value: 'rel',
        key: 'profileName'
      },
      {
        name_zh: 'config 配置中心',
        value: 'xxx',
        key: 'configServerUri'
      },
      {
        name_zh: 'eureka 1 地址',
        value: 'xxx',
        key: 'eurekaServerUri'
      },
      {
        name_zh: 'eureka 2 地址',
        value: 'xxx',
        key: 'eureka1ServerUri'
      },
      {
        name_zh: '宿主机日志映射目录',
        value: '/logs/current/per',
        key: 'hostLogsPath'
      },
      {
        name_zh: 'config 配置中心全路径',
        value: 'http://ebank:xxx:9804',
        key: 'SPRING_CLOUD_CONFIG_URI'
      },
      {
        name_zh: 'eureka 配置中心全路径',
        value: 'address',
        key: 'EUREKA_CLIENT_SERVICEURL_DEFAULTZONE'
      },
      {
        name_zh: '加密平台配置文件名',
        value: 'serverList.conf',
        key: 'UNIONAPI_CONF_FILENAME'
      },
      {
        name_zh: 'CaaS ACCESS KEY',
        value: 'cdwwcalp',
        key: 'CI_CAAS_ACCESS'
      },
      {
        name_zh: 'CaaS SECRET KEY',
        value: 'h5xbmjx4rk3sc4u4phoon5zdkyk67hbmx25vvj5z',
        key: 'CI_CAAS_SECRET'
      },
      {
        name_zh: 'CaaS DCE IP',
        value: 'xxx',
        key: 'CI_CAAS_IP'
      },
      {
        name_zh: 'CaaS 用户名',
        value: 'ebank',
        key: 'CI_CAAS_USER'
      },
      {
        name_zh: 'CaaS 用户密码',
        value: 'xxx',
        key: 'CI_CAAS_PWD'
      },
      {
        name_zh: 'CaaS 镜像仓库IP',
        value: 'xxx',
        key: 'CI_CAAS_REGISTRY'
      },
      {
        name_zh: 'CaaS 镜像空间',
        value: 'ebank-service',
        key: 'CI_CAAS_REGISTRY_NAMESPACE'
      },
      {
        name_zh: 'CaaS 镜像仓库 用户名',
        value: 'ebank',
        key: 'CI_CAAS_REGISTRY_USER'
      },
      {
        name_zh: 'CaaS 镜像仓库 用户密码',
        value: 'xxx',
        key: 'CI_CAAS_REGISTRY_PASSWORD'
      }
    ]
  },
  {
    name: 'no2',
    var_list: [
      {
        name_zh: 'subnet',
        value: 'xxx/24',
        key: 'subnet'
      },
      {
        name_zh: 'profileName',
        value: 'rel',
        key: 'profileName'
      },
      {
        name_zh: 'config 配置中心',
        value: 'xxx',
        key: 'configServerUri'
      },
      {
        name_zh: 'eureka 1 地址',
        value: 'xxx',
        key: 'eurekaServerUri'
      },
      {
        name_zh: 'eureka 2 地址',
        value: 'xxx',
        key: 'eureka1ServerUri'
      },
      {
        name_zh: '宿主机日志映射目录',
        value: '/logs/current/per',
        key: 'hostLogsPath'
      },
      {
        name_zh: 'config 配置中心全路径',
        value: 'http://ebank:xxx:9804',
        key: 'SPRING_CLOUD_CONFIG_URI'
      },
      {
        name_zh: 'eureka 配置中心全路径',
        value: 'address',
        key: 'EUREKA_CLIENT_SERVICEURL_DEFAULTZONE'
      },
      {
        name_zh: '加密平台配置文件名',
        value: 'serverList.conf',
        key: 'UNIONAPI_CONF_FILENAME'
      },
      {
        name_zh: 'CaaS ACCESS KEY',
        value: 'cdwwcalp',
        key: 'CI_CAAS_ACCESS'
      },
      {
        name_zh: 'CaaS SECRET KEY',
        value: 'h5xbmjx4rk3sc4u4phoon5zdkyk67hbmx25vvj5z',
        key: 'CI_CAAS_SECRET'
      },
      {
        name_zh: 'CaaS DCE IP',
        value: 'xxx',
        key: 'CI_CAAS_IP'
      },
      {
        name_zh: 'CaaS 用户名',
        value: 'ebank',
        key: 'CI_CAAS_USER'
      },
      {
        name_zh: 'CaaS 用户密码',
        value: 'xxx',
        key: 'CI_CAAS_PWD'
      },
      {
        name_zh: 'CaaS 镜像仓库IP',
        value: 'xxx',
        key: 'CI_CAAS_REGISTRY'
      },
      {
        name_zh: 'CaaS 镜像空间',
        value: 'ebank-service',
        key: 'CI_CAAS_REGISTRY_NAMESPACE'
      },
      {
        name_zh: 'CaaS 镜像仓库 用户名',
        value: 'ebank',
        key: 'CI_CAAS_REGISTRY_USER'
      },
      {
        name_zh: 'CaaS 镜像仓库 用户密码',
        value: 'xxx',
        key: 'CI_CAAS_REGISTRY_PASSWORD'
      }
    ]
  }
];

const pipelines = [
  {
    id: 29898,
    sha: 'b3bc6564cfa75f7cda1709c6d5af40169823db2a',
    ref: 'SIT',
    status: 'success',
    web_url: 'http://xxx/ebank/devops/fdev-task/pipelines/29898'
  },
  {
    id: 29811,
    sha: '6824101c626bfaad85415aa2dfb759e94eb27482',
    ref: 'UAT',
    status: 'failed',
    web_url: 'http://xxx/ebank/devops/fdev-task/pipelines/29811'
  },
  {
    id: 29422,
    sha: '1005a5493c99b54adba402d27a36ab917ad95b99',
    ref: 'SIT',
    status: 'canceled',
    web_url: 'http://xxx/ebank/devops/fdev-task/pipelines/29422'
  }
];

const mantisList = {
  id: '@id',
  'name|1': ['任务名称:个人靠谱E投', '任务名称:任务111'],
  'mantis|5': [
    {
      id: '@id',
      summary: '摘要摘要摘要摘要摘要摘要摘要',
      description:
        '描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述',
      'status|1': ['10', '20', '30', '40', '50', '80', '90'],
      handler: '@cname',
      reporter: '@cname',
      date_submitted: '@date',
      'priority|1': ['10', '20', '30', '40', '50', '60']
    }
  ]
};

module.exports = {
  'POST /frqrmnt/api/implunits/queryUnitByRegexNum': mockjs.mock({
    'list|10': [unit]
  })['list'],

  'POST /ftask/api/serviceSystem/querySystem': mockjs.mock({
    'list|4': [system]
  })['list'],

  'POST /ftask/api/serviceSystem/queryDomains': mockjs.mock({
    'list|4': [domain]
  })['list'],

  'POST /fapp/api/gitlabci/query': mockjs.mock({
    'list|4': [gitlabci]
  })['list'],

  'POST /fapp/api/archetype/query': mockjs.mock({
    'list|4': [archetype]
  })['list'],

  'POST /fapp/api/file/upload': mockjs.mock({
    id: '@id'
  }),

  'POST /fuser/api/git/queryMergeInfo': mockjs.mock({
    id: '@id',
    'merge_status|1': ['cannot_be_merged', 'can_be_merged']
  }),

  'POST /ftask/api/task/queryEnvDetail': mockjs.mock(env),

  'POST /fapp/api/gitlabapi/queryPipelinesWithJobs': mockjs.mock(pipelines),
  'POST /fapp/api/app/queryAppMantis': mockjs.mock({
    'list|15': [mantisList]
  })['list']
};
