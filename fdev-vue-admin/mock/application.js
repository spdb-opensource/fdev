const mockjs = require('mockjs');

const application = {
  id: '@id',
  gitlab_project_id: '@id',
  name_en: 'mspmk-web-test0111wf',
  name_zh: '应用1023-01',
  desc: '这是一个测试的应用',
  group: '1',
  type_id: '5sdaq4fgvfq4eWEGW44',
  type_name: '测试',
  git: 'http://example/api/model/test.git',
  status: '创建',
  createtime: 'now()',
  spdb_managers: [],
  prod: {
    name: '模块名称:个人靠谱E投',
    container_num: '容器数量:4',
    container_spec: '容器规格:4c8g',
    data_center: '数据中心：上海，合肥',
    env: '环境：生产，灰度',
    sh_image: '合肥镜像仓库地址',
    hf_image: '上海镜像仓库地址',
    laster_version: '最后版本号:v1.0'
  },
  tasks_id: ['5esd8s4sd0gdb0s234msdff324'],
  archetype_id: '5fasaw24nj32n2369n80n1nsw',
  gitlabci_id: '5da6a12hd90k91n1mzjf23kd'
};

const ChangeTemplateData = {
  temp_list: [
    {
      id: 'dolore ipsum quis',
      temp_name: 'sit laboris culpa ut',
      owner_group: 'non cillum id sint',
      owner_system: 'in in',
      template_type: 'veniam incididunt dolore minim',
      owner_app: 'irure pariatur in quis',
      temp_giturl: 'occ',
      assets_giturl: 'mollit ullamco ut',
      update_user: 'deserunt',
      update_time: 'fugiat culpa elit sint',
      catalogs: [
        {
          catalog_name: 'laboris dolore',
          description: 'Dui',
          catalog_type: 'cillum Ut'
        },
        {
          catalog_name: 'ullamco irure',
          description: 'ut minim et',
          catalog_type: 'ullamco'
        },
        {
          catalog_name: 'ullamco',
          description: 'dolor pariatur ul',
          catalog_type: 'irure minim do magna'
        }
      ]
    },
    {
      id: 'cillum officia in anim',
      temp_name: 'laboru',
      owner_group: 'Duis incididunt do',
      owner_system: 'o',
      template_type: 'ipsum officia',
      owner_app: 'aute culpa aliqua',
      temp_giturl: 'et sit Lorem magna dolor',
      assets_giturl: 'deserunt culpa in nisi',
      update_user: 'in nulla labore ad',
      update_time: 'sint cillum',
      catalogs: [
        {
          catalog_name: 'sunt quis',
          description: 'magna',
          catalog_type: 'nulla aliqua'
        },
        {
          catalog_name: 'Duis',
          description: 'in sunt',
          catalog_type: 'ex in laborum minim'
        },
        {
          catalog_name: 'anim consequat',
          description: 'dolo',
          catalog_type: 'do et offi'
        },
        {
          catalog_name: 'consequat ad',
          description: 'ut nostrud',
          catalog_type: 'fugiat non laboris ad in'
        }
      ]
    },
    {
      id: 'fugiat aliqua Ut',
      temp_name: 'consequat officia ea',
      owner_group: 'elit',
      owner_system: 'ut ad nostrud irure',
      template_type: 'culpa',
      owner_app: 'in Du',
      temp_giturl: 'Lorem anim aliquip',
      assets_giturl: 'commodo sit am',
      update_user: 'officia tempor Ut',
      update_time: 'consequat deserunt aliqua voluptate',
      catalogs: [
        {
          catalog_name: 'occaecat Lorem do adipisicing',
          description: 'in dolore reprehenderit',
          catalog_type: 'Duis do'
        }
      ]
    },
    {
      id: 'do magna fugiat reprehenderit',
      temp_name: 'velit ad incididunt in off',
      owner_group: 'elit dolor velit',
      owner_system: 'officia veniam',
      template_type: 'quis an',
      owner_app: 'sint irure adipisicing elit',
      temp_giturl: 'aliqua amet pariatur sunt',
      assets_giturl: 'qui dolor',
      update_user: 'ex voluptate tempor minim',
      update_time: 'sint nulla',
      catalogs: [
        {
          catalog_name: 'in exercitation',
          description: 'cillum sit ea',
          catalog_type: 'ipsum anim in laborum nisi'
        },
        {
          catalog_name: 'anim cupidatat',
          description: 'aute ipsum in fugiat labore',
          catalog_type: 'non'
        },
        {
          catalog_name: 'cupidatat ex dolore sit id',
          description: 'ex nisi',
          catalog_type: 'reprehenderit proident qui Lorem'
        },
        {
          catalog_name: 'consequat ',
          description: 'commodo sint officia',
          catalog_type: 'm'
        }
      ]
    },
    {
      id: 'sunt irure deserunt sed',
      temp_name: 'nostrud',
      owner_group: 'Duis do et',
      owner_system: 'Excepteur ',
      template_type: 'velit',
      owner_app: 'consectetur labore',
      temp_giturl: 'ea in dolor dolore laborum',
      assets_giturl: 'in est',
      update_user: 'quis',
      update_time: 'fugiat tempor eu pariatur dolore',
      catalogs: [
        {
          catalog_name: 'quis ex incididunt ut',
          description: 'nisi sunt ad',
          catalog_type: 'culp'
        },
        {
          catalog_name: 'et fugiat consectetur',
          description: 'dolor',
          catalog_type: 'voluptate aliqua'
        }
      ]
    }
  ],
  Excepteur25f: false,
  esta6a: -10914453.375207111,
  ipsum5: -32005326.651729196,
  cillum117: false,
  aliquipd5: false,
  esse6a9: -5577838.435228258,
  irure7b: 'adipisicing aliqua Excepteur reprehenderit',
  cupidatate1: -88270129,
  sed9: 27149331.213993788
};

const branch = {
  name: '@First'
};

const env = {
  id: '@id',
  env_name: '@string',
  env_type: '@string'
};

const typeList = [
  { label: 'IOS', value: '1' },
  { label: 'Android', value: '2' },
  { label: 'Java', value: '3' },
  { label: 'Vue应用', value: '4' }
];

const vueList = [
  { name_zh: '应用1', name_en: 'appOne', id: 111 },
  { name_zh: '应用2', name_en: 'appTwo', id: 222 },
  { name_zh: '应用3', name_en: 'appThree', id: 333 }
];
const codeQuality = {
  'qube_switch|1': ['0', '1'],
  qube_bugs: '123',
  tips: 'dasdqw'
};
module.exports = {
  'POST /fapp/api/app/query': mockjs.mock({
    'list|10': [application]
  })['list'],
  'POST /fapp/api/app/delete': mockjs.mock({
    id: '@id'
  }),
  'POST /api/gitlabapi/queryAllBranch': mockjs.mock({
    'list|3': [branch]
  })['list'],
  'POST /fapp/api/environment/query': mockjs.mock({
    'list|3': [env]
  })['list'],

  'POST /fapp/api/app/add': mockjs.mock(application),
  'POST /fapp/api/app/update': mockjs.mock(application),
  'POST /frelease/api/template/query': mockjs.mock(ChangeTemplateData),
  'POST /frelease/api/template/queryDetail': mockjs.mock(
    ChangeTemplateData.temp_list[0]
  ),
  'POST /fapp/api/type/query': mockjs.mock(typeList),
  'POST /fapp/api/app/queryApps': mockjs.mock(vueList),
  'POST /ftask/api/sonarqube/getCodeQuality': mockjs.mock(codeQuality)
};
