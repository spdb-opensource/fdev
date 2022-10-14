const UIdesign = () => import('@/modules/Dashboard/views/UIdesign');
const UserOptimize = () =>
  import('@/modules/Dashboard/views/Frame/UserOptimize');
const RqrDelay = () => import('@/modules/Dashboard/views/Frame/RqrDelay');
export default [
  // UI审核进度搬迁到研发协作
  {
    path: '/UIdesign',
    name: 'UIdesign',
    meta: {
      nameCn: 'UI审核进度',
      fstMenu: 'rAndD',
      secMenu: 'uiDesign',
      icon: 'uiDesign'
    },
    component: UIdesign
  },
  // 基础架构搬迁到应用环境
  {
    path: '/BasicFrame',
    name: 'BasicFrame',
    meta: {
      nameCn: '基础架构统计',
      fstMenu: 'appAndConfig',
      secMenu: 'BasicFrame',
      icon: 'basicFrame'
    },
    children: [
      {
        path: 'user',
        name: 'userOptimization',
        meta: {
          nameCn: '个人优化需求',
          fstMenu: 'appAndConfig',
          secMenu: 'BasicFrame'
        },
        component: UserOptimize
      },
      {
        path: 'frameRqrDelay',
        name: 'frameRqrDelay',
        meta: {
          nameCn: '需求延期情况',
          fstMenu: 'appAndConfig',
          secMenu: 'BasicFrame'
        },
        component: RqrDelay
      }
    ]
  }
];
