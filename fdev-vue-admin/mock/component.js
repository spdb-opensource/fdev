const mockjs = require('mockjs');

const component = {
  id: '@id',
  name_en: 'pe-redis-core',
  name_cn: '@centence',
  desc: '网银分布式会话redis访问组件，对于网银系统访问redis做规范封装',
  gitlab_url: 'http://xxx/ebank/CommonModules/pe-redis-core',
  groupId: 'com.csii.pe',
  artifactId: 'pe-redis-core',
  parentId: '',
  manager_id: [
    {
      id: '@id',
      user_name_cn: '@cname',
      user_name_en: '@ename'
    }
  ],
  type: '0',
  root_dir: '',
  jdk_version: '1.8',
  wiki_url: '',
  source: '0'
};

const archetype = {
  id: '@id',
  name_en: '@first',
  name_cn: '@cname',
  desc: 'fasd',
  gitlab_url: '@url',
  groupId: '@id',
  artifactId: '@id',
  'manager_id|1-5': [
    {
      user_name_cn: '@cname',
      user_name_en: '@first',
      id: '@id'
    }
  ],
  type: '123',
  wiki_url: '@url',
  'encoding|1': ['utf-8', 'gbk'],
  recommend_version: '@id'
};

const archetypeProfile = {
  id: '@id',
  name_en: '@first',
  name_cn: '@cname',
  desc: 'fasd',
  gitlab_url: '@url',
  groupId: '@id',
  artifactId: '@id',
  'manager_id|1-5': [
    {
      user_name_cn: '@cname',
      user_name_en: '@first',
      id: '@id'
    }
  ],
  type: '123',
  wiki_url: '@url',
  'encoding|1': ['utf-8', 'gbk'],
  recommend_version: '@id'
};

const archetypeHistory = {
  id: '@id',
  name_en: '@first',
  name_cn: '@cname',
  artifactId: '@id',
  'version|1': ['SNAPSHOT', 'RELEASE'],
  'type|1': ['0', '1', '2', '3'],
  update_user: '@cname',
  release_log: 'hhhhhh',
  date: '2019-1-1'
};

module.exports = {
  'POST /fcomponent/api/component/queryComponents': mockjs.mock({
    'list|5': [component]
  })['list'],
  'POST /fcomponent/api/prototype/queryArchetypes': mockjs.mock({
    'list|5': [archetype]
  })['list'],
  'POST /fcomponent/api/archetype/queryArchetypeDetail': mockjs.mock(
    archetypeProfile
  ),
  'POST /fcomponent/api/archetype/queryArchetypeHistory': mockjs.mock({
    'list|5': [archetypeHistory]
  })['list']
};
