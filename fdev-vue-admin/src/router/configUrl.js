import App from '@/modules/App/router/index.js';
import Component from '@/modules/Component/router/index.js';
import configCI from '@/modules/configCI/router/index.js';
// import DashBoard from '@/modules/Dashboard/router/index.js';
import Database from '@/modules/Database/router/index.js';
import Environment from '@/modules/Environment/router/index.js';
import Interface from '@/modules/interface/router/index.js';
import Job from '@/modules/Job/router/index.js';
import HomePage from '@/modules/HomePage/router/index.js';
import Network from '@/modules/Network/router/index.js';
import Notices from '@/modules/Notices/router/index.js';
import Release from '@/modules/Release/router/index.js';
import Rqr from '@/modules/Rqr/router/index.js';
import User from '@/modules/User/router/index.js';

import Management from '@/modules/Management/router/index.js';
import Measure from '@/modules/Measure/router/index.js';
// UI审核进度搬迁
import UIdesign from '@/modules/Dashboard/router/uiRouter.js';

const toPath = [
    'xxx',
    'xxx',
    'xxx'
  ],
  { origin } = window.location;

const Iams = [
  {
    path: 'iams',
    name: 'iams',
    meta: {
      nameCn: '挡板',
      fstMenu: 'flowAndTool',
      secMenu: 'iams',
      icon: 'iams'
    },
    toPath: toPath.slice(0, 2).includes(origin)
      ? `${origin}/iams/#/`
      : 'xxx/iams/#/'
  }
];

const Alioth = [
  {
    path: 'alioth',
    name: 'alioth',
    meta: {
      nameCn: '玉衡测试',
      fstMenu: 'rAndD',
      secMenu: 'alioth',
      icon: 'alioth'
    },
    toPath: toPath.includes(origin)
      ? `${origin}/tui/#/`
      : 'xxx/tui/#/'
  }
];

export default [].concat(
  HomePage,
  // DashBoard,
  Measure,
  User,
  Job,
  Release,
  App,
  configCI,
  Environment,
  Interface,
  Rqr,
  Component,
  Database,
  Network,
  Notices,
  Management,
  Iams,
  Alioth,
  UIdesign
  // Account
);
