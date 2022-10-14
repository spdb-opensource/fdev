const moment = require('moment');

// mock data
const visitData = [];
const beginDay = new Date().getTime();

const fakeY = [7, 5, 4, 2, 4, 7, 5, 6, 5, 9, 6, 3, 1, 5, 3, 6, 5];
for (let i = 0; i < fakeY.length; i += 1) {
  visitData.push({
    x: moment(new Date(beginDay + 1000 * 60 * 60 * 24 * i)).format(
      'YYYY-MM-DD'
    ),
    y: fakeY[i]
  });
}

const visitData2 = [];
const fakeY2 = [1, 6, 4, 8, 3, 7, 2];
for (let i = 0; i < fakeY2.length; i += 1) {
  visitData2.push({
    x: moment(new Date(beginDay + 1000 * 60 * 60 * 24 * i)).format(
      'YYYY-MM-DD'
    ),
    y: fakeY2[i]
  });
}

const salesData = [];
for (let i = 0; i < 12; i += 1) {
  salesData.push({
    x: `${i + 1}月`,
    y: Math.floor(Math.random() * 1000) + 200
  });
}
const searchData = [];
for (let i = 0; i < 50; i += 1) {
  searchData.push({
    index: i + 1,
    keyword: `搜索关键词-${i}`,
    count: Math.floor(Math.random() * 1000),
    range: Math.floor(Math.random() * 100),
    status: Math.floor((Math.random() * 10) % 2)
  });
}
const salesTypeData = [
  {
    x: '家用电器',
    y: 4544
  },
  {
    x: '食用酒水',
    y: 3321
  },
  {
    x: '个护健康',
    y: 3113
  },
  {
    x: '服饰箱包',
    y: 2341
  },
  {
    x: '母婴产品',
    y: 1231
  },
  {
    x: '其他',
    y: 1231
  }
];

const salesTypeDataOnline = [
  {
    x: '家用电器',
    y: 244
  },
  {
    x: '食用酒水',
    y: 321
  },
  {
    x: '个护健康',
    y: 311
  },
  {
    x: '服饰箱包',
    y: 41
  },
  {
    x: '母婴产品',
    y: 121
  },
  {
    x: '其他',
    y: 111
  }
];

const salesTypeDataOffline = [
  {
    x: '家用电器',
    y: 99
  },
  {
    x: '食用酒水',
    y: 188
  },
  {
    x: '个护健康',
    y: 344
  },
  {
    x: '服饰箱包',
    y: 255
  },
  {
    x: '其他',
    y: 65
  }
];

const offlineData = [];
for (let i = 0; i < 10; i += 1) {
  offlineData.push({
    name: `Stores ${i}`,
    cvr: Math.ceil(Math.random() * 9) / 10
  });
}
const offlineChartData = [];
for (let i = 0; i < 20; i += 1) {
  offlineChartData.push({
    x: new Date().getTime() + 1000 * 60 * 30 * i,
    y1: Math.floor(Math.random() * 100) + 10,
    y2: Math.floor(Math.random() * 100) + 10
  });
}

const radarOriginData = [
  {
    name: '个人',
    ref: 10,
    koubei: 8,
    output: 4,
    contribute: 5,
    hot: 7
  },
  {
    name: '团队',
    ref: 3,
    koubei: 9,
    output: 6,
    contribute: 3,
    hot: 1
  },
  {
    name: '部门',
    ref: 4,
    koubei: 1,
    output: 6,
    contribute: 5,
    hot: 7
  }
];

const radarData = [];
const radarTitleMap = {
  ref: '引用',
  koubei: '口碑',
  output: '产量',
  contribute: '贡献',
  hot: '热度'
};
radarOriginData.forEach(item => {
  Object.keys(item).forEach(key => {
    if (key !== 'name') {
      radarData.push({
        name: item.name,
        label: radarTitleMap[key],
        value: item[key]
      });
    }
  });
});

const getFakeChartData = {
  visitData,
  visitData2,
  salesData,
  searchData,
  offlineData,
  offlineChartData,
  salesTypeData,
  salesTypeDataOnline,
  salesTypeDataOffline,
  radarData
};

const lineChart = {
  '1': {
    '2019/07/03': 3,
    '2019/07/24': 4,
    '2019/06/26': 3,
    '2019/07/17': 4,
    '2019/07/10': 4,
    '2019/06/19': 3
  },
  '2': {
    '2019/07/03': 251,
    '2019/07/24': 360,
    '2019/06/26': 185,
    '2019/07/17': 339,
    '2019/07/10': 311,
    '2019/06/19': 123
  },
  '3': {
    '2019/07/03': 0,
    '2019/07/24': 4,
    '2019/06/26': 0,
    '2019/07/17': 4,
    '2019/07/10': 2,
    '2019/06/19': 0
  },
  '4': {
    '2019/07/03': 0,
    '2019/07/24': 0,
    '2019/06/26': 0,
    '2019/07/17': 0,
    '2019/07/10': 0,
    '2019/06/19': 0
  }
};

const barChart = {
  '1': {
    todo: 1,
    production: 2,
    todo_ids: [],
    rel: 3,
    uat: 9,
    dev_ids: ['5db2b452d373c50013d1bc5f'],
    develop: 1,
    rel_ids: [],
    pro_ids: [],
    sit: 0,
    sit_ids: [],
    uat_ids: []
  },
  '2': {
    todo: 3,
    production: 9,
    todo_ids: [],
    rel: 5,
    uat: 1,
    dev_ids: ['5db2b452d373c50013d1bc5f'],
    develop: 3,
    rel_ids: [],
    pro_ids: [],
    sit: 8,
    sit_ids: [],
    uat_ids: []
  }
};

const user = [];

module.exports = {
  'GET /fuser/api/fake_chart_data': getFakeChartData,
  'POST /ftask/api/task/queryTaskNum': lineChart,
  'POST /ftask/api/task/queryTaskNumByGroup': barChart,
  'POST /ftask/api/task/queryTaskNumByGroupDate': barChart,
  'POST /fapp/api/app/queryAppNum': lineChart,
  'POST /fuser/api/user/queryUserCoreData': user,
  'POST /ftask/api/task/queryTaskNumByMember': barChart,
  'POST /ftask/api/task/queryTaskNumByUserIdsDate': barChart
};
