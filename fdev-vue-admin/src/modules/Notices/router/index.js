const Notices = () => import('@/modules/Notices/views/message');
const Announce = () => import('@/modules/Notices/views/announce');

export default [
  {
    path: '/notices',
    name: 'notices',
    meta: {
      nameCn: '通知',
      fstMenu: 'notices'
    },
    children: [
      {
        path: 'announce',
        name: 'noticeAnnounce',
        meta: {
          nameCn: '公告',
          hideInMenu: true
        },
        component: Announce
      },
      {
        path: 'message',
        name: 'message',
        meta: {
          nameCn: '消息',
          hideInMenu: true
        },
        component: Notices
      }
    ]
  }
];
