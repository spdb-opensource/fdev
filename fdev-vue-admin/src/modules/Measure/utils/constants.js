/***************************仪表盘 ************************/
//不同宽度适配
const MIN_SIZE = { w: 4, h: 3 };
const COL_NUM = 12;
//图表类型
const GRAPH_TYPE = {
  LineG: '折线图',
  NumCard: '数字卡片图',
  Bar: '柱状图',
  StackBar: '堆叠柱状图',
  GroupedBar: '分组柱状图',
  Pie: '饼图',
  Table: '表格',
  Radar: '雷达图',
  Dashboard: '仪表盘',
  RankingBar: '条形图'
};
const GRAPH_TYPE_KEY = Object.keys(GRAPH_TYPE);
//图表列表
const GRAPH_LIST = [
  { value: 'LineG', label: '折线图' },
  { value: 'NumCard', label: '数字卡片图' },
  { value: 'Bar', label: '柱状图' },
  { value: 'StackBar', label: '堆叠柱状图' },
  { value: 'GroupedBar', label: '分组柱状图' },
  { value: 'Pie', label: '饼图' },
  { value: 'Table', label: '表格' },
  { value: 'Radar', label: '雷达图' },
  { value: 'Dashboard', label: '仪表盘' },
  { value: 'RankingBar', label: '条形图' }
];
//组件列表icon
const COMP_MARKET_ICON = {};
GRAPH_TYPE_KEY.forEach(
  key =>
    (COMP_MARKET_ICON[key] = require('../assets/compMarketIcon/' +
      `${key}` +
      '.svg'))
);
/***************************统计报表 ************************/
// 任务吞吐量
const taskThroughputHeader = [
  { value: '新增业务任务数', isShow: false },
  { value: '新增科技任务数', isShow: false },
  { value: '新增日常任务数', isShow: true },
  { value: '投产业务任务数', isShow: false },
  { value: '投产科技任务数', isShow: false },
  { value: '完成日常任务数', isShow: true },
  { value: '投产任务总数', isShow: false },
  { value: '人数', isShow: false },
  { value: '人均投产业务任务数', isShow: false },
  { value: '人均投产科技任务数', isShow: false },
  { value: '人均完成日常任务数', isShow: true },
  { value: '人均投产任务数', isShow: false }
];
//横纵轴样式
const lineStyle = {
  show: true,
  lineStyle: {
    color: '#F0F0F0',
    width: 1.27
  }
};
//横纵坐标字体样式
const axisFont = {
  textStyle: { color: '#666666', fontSize: 12 }
};
// 网格线样式
const splitLineStyle = {
  show: true,
  lineStyle: {
    width: 1.27,
    color: '#F0F0F0'
  }
};
export {
  GRAPH_TYPE,
  GRAPH_TYPE_KEY,
  GRAPH_LIST,
  COMP_MARKET_ICON,
  MIN_SIZE,
  COL_NUM,
  taskThroughputHeader,
  lineStyle,
  axisFont,
  splitLineStyle
};
export function mergeRequestModel() {
  return {
    git_user_id: '',
    group_id: '',
    start_time: '',
    end_time: ''
  };
}
export function demandOptions() {
  return [
    {
      label: '业务需求',
      value: 'business'
    },
    {
      label: '科技需求',
      value: 'tech'
    },
    {
      label: '日常需求',
      value: 'daily'
    }
  ];
}
export function taskOptions() {
  return [
    {
      label: '开发任务',
      value: 0,
      inactive: false
    },
    {
      label: '无代码任务',
      value: 1,
      inactive: false
    },
    {
      label: '日常任务',
      value: 2,
      inactive: false
    }
  ];
}
export function appOptions() {
  return [
    {
      label: '应用',
      value: 'app'
    },
    {
      label: '组件',
      value: 'component'
    },
    {
      label: '骨架',
      value: 'archetype'
    },
    {
      label: '镜像',
      value: 'baseImage'
    }
  ];
}
