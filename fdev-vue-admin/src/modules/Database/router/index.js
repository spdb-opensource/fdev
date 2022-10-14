const DatabaseList = () => import('@/modules/Database/views/index.vue');
const DataDictionaryList = () =>
  import('@/modules/Database/views/DataDictionaryList');
const DataDictionaryListRegister = () =>
  import('@/modules/Database/views/DataDictionaryListRegister');

export default [
  {
    path: '/database',
    name: 'database',
    meta: {
      nameCn: '数据库管理',
      icon: 'database',
      fstMenu: 'appAndConfig',
      secMenu: 'database'
    },
    children: [
      {
        path: 'list',
        component: DatabaseList,
        meta: {
          nameCn: '应用与库表',
          fstMenu: 'appAndConfig',
          secMenu: 'database'
        },
        name: 'DatabaseList'
      },
      {
        path: 'dictionary',
        component: DataDictionaryList,
        meta: {
          nameCn: '数据字典',
          fstMenu: 'appAndConfig',
          secMenu: 'database'
        },
        name: 'DataDictionaryList'
      },
      {
        path: 'dictionaryRegister',
        component: DataDictionaryListRegister,
        meta: {
          nameCn: '数据字典登记表',
          fstMenu: 'appAndConfig',
          secMenu: 'database'
        },
        name: 'DataDictionaryListRegister'
      }
    ]
  }
];
