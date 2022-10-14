import moment from 'moment';
import nzh from 'nzh/cn';
import { parse, stringify } from 'qs';
import Notify from '#/plugins/Notify';
import { Base64 } from '@/utils/base64';

export function fixedZero(val) {
  return val * 1 < 10 ? `0${val}` : val;
}

export function getTimeDistance(type) {
  const now = new Date();
  const oneDay = 1000 * 60 * 60 * 24;

  if (type === 'today') {
    now.setHours(0);
    now.setMinutes(0);
    now.setSeconds(0);
    return [moment(now), moment(now.getTime() + (oneDay - 1000))];
  }

  if (type === 'week') {
    let day = now.getDay();
    now.setHours(0);
    now.setMinutes(0);
    now.setSeconds(0);

    if (day === 0) {
      day = 6;
    } else {
      day -= 1;
    }

    const beginTime = now.getTime() - day * oneDay;

    return [moment(beginTime), moment(beginTime + (7 * oneDay - 1000))];
  }

  if (type === 'month') {
    const year = now.getFullYear();
    const month = now.getMonth();
    const nextDate = moment(now).add(1, 'months');
    const nextYear = nextDate.year();
    const nextMonth = nextDate.month();

    return [
      moment(`${year}-${fixedZero(month + 1)}-01 00:00:00`),
      moment(
        moment(
          `${nextYear}-${fixedZero(nextMonth + 1)}-01 00:00:00`
        ).valueOf() - 1000
      )
    ];
  }

  const year = now.getFullYear();
  return [moment(`${year}-01-01 00:00:00`), moment(`${year}-12-31 23:59:59`)];
}

export function getPlainNode(nodeList, parentPath = '') {
  const arr = [];
  nodeList.forEach(node => {
    const item = node;
    item.path = `${parentPath}/${item.path || ''}`.replace(/\/+/g, '/');
    item.exact = true;
    if (item.children && !item.component) {
      arr.push(...getPlainNode(item.children, item.path));
    } else {
      if (item.children && item.component) {
        item.exact = false;
      }
      arr.push(item);
    }
  });
  return arr;
}

export function digitUppercase(n) {
  return nzh.toMoney(n);
}

function getRelation(str1, str2) {
  if (str1 === str2) {
    console.warn('Two path are equal!'); // eslint-disable-line
  }
  const arr1 = str1.split('/');
  const arr2 = str2.split('/');
  if (arr2.every((item, index) => item === arr1[index])) {
    return 1;
  }
  if (arr1.every((item, index) => item === arr2[index])) {
    return 2;
  }
  return 3;
}

function getRenderArr(routes) {
  let renderArr = [];
  renderArr.push(routes[0]);
  for (let i = 1; i < routes.length; i += 1) {
    // 去重
    renderArr = renderArr.filter(item => getRelation(item, routes[i]) !== 1);
    // 是否包含
    const isAdd = renderArr.every(item => getRelation(item, routes[i]) === 3);
    if (isAdd) {
      renderArr.push(routes[i]);
    }
  }
  return renderArr;
}

/**
 * Get router routing configuration
 * { path:{name,...param}}=>Array<{name,path ...param}>
 * @param {string} path
 * @param {routerData} routerData
 */
export function getRoutes(path, routerData) {
  let routes = Object.keys(routerData).filter(
    routePath => routePath.indexOf(path) === 0 && routePath !== path
  );
  // Replace path to '' eg. path='user' /user/name => name
  routes = routes.map(item => item.replace(path, ''));
  // Get the route to be rendered to remove the deep rendering
  const renderArr = getRenderArr(routes);
  // Conversion and stitching parameters
  const renderRoutes = renderArr.map(item => {
    const exact = !routes.some(
      route => route !== item && getRelation(route, item) === 1
    );
    return {
      exact,
      ...routerData[`${path}${item}`],
      key: `${path}${item}`,
      path: `${path}${item}`
    };
  });
  return renderRoutes;
}

export function getPageQuery() {
  return parse(window.location.href.split('?')[1]);
}

export function getQueryPath(path = '', query = {}) {
  const search = stringify(query);
  if (search.length) {
    return `${path}?${search}`;
  }
  return path;
}

const reg = /(((^https?:(?:\/\/)?)(?:[-;:&=+$,\w]+@)?[A-Za-z0-9.-]+(?::\d+)?|(?:www.|[-;:&=+$,\w]+@)[A-Za-z0-9.-]+)((?:\/[+~%/.\w-_]*)?\??(?:[-+=&;%@.\w_]*)#?(?:[\w]*))?)$/;

export function isUrl(path) {
  return reg.test(path);
}

export function formatWan(val) {
  const v = val * 1;
  if (!v || Number.isNaN(v)) return '';

  let result = val;
  if (val > 10000) {
    result = Math.floor(val / 10000);
    result = `${result}万`;
  }
  return result;
}

export function formatSelectDisplay(options, selected, defaultDisplay) {
  let display = defaultDisplay || '请选择';
  let val;
  let option;

  if (!selected) {
    return display;
  }
  if (!Array.isArray(options)) {
    return selected;
  }

  if (Array.isArray(selected)) {
    val = selected.map(sel => (sel.value ? sel.value : sel));
    option = options.filter(option => val.indexOf(option.value) > -1);
    return option;
  }

  val = selected.value ? selected.value : selected;
  option = options.find(option => option.value === val);

  if (!option) {
    return display;
  }
  return option.label;
}

function flatOption(obj, name) {
  if (!obj) {
    return {
      label: '',
      value: ''
    };
  }
  return {
    ...obj,
    label: obj[name],
    value: obj.id
  };
}
export function formatOption(obj, name) {
  name = name || 'name';
  if (Array.isArray(obj)) {
    return obj.map(each => flatOption(each, name));
  }
  return flatOption(obj, name);
}

export function formatOptionAdd(obj, name, value) {
  name = name || 'name';
  value = value || 'id';
  if (Array.isArray(obj)) {
    return obj.map(each => flatOptionAdd(each, name, value));
  }
  return flatOptionAdd(obj, name, value);
}

function flatOptionAdd(obj, name, value) {
  if (!obj) {
    return {
      label: '',
      value: ''
    };
  }
  return {
    ...obj,
    label: obj[name],
    value: obj[value]
  };
}

export function wrapOptionsTotal(options) {
  return [
    {
      label: '全部',
      value: 'total'
    },
    ...options
  ];
}

export function successNotify(message) {
  let setting = {
    color: 'positive',
    icon: 'check_circle_outline',
    position: 'top',
    classes: 'notify-inline'
  };

  if (typeof message === 'string') {
    setting.message = message;
  } else {
    Object.assign(setting, message);
  }
  Notify.create(setting);
}

export function errorNotify(message) {
  let setting = {
    color: 'negative',
    icon: 'cancel',
    position: 'top'
  };

  if (typeof message === 'string') {
    if (message.includes('未配置映射值')) {
      setting.html = true;
      let msg = '';
      message.split('\n').forEach(rowMsg => {
        msg += `<div>${rowMsg}</div>`;
      });
      setting.timeout = 5000;
      setting.message = msg;
    } else {
      setting.message = message;
    }
  } else {
    Object.assign(setting, message);
  }
  Notify.create(setting);
}

export async function resolveResponseError(request) {
  let response = await request();
  if (response.fdevStatus !== 'error') {
    return response;
  } else {
    let { code, msg } = response;
    if (code === '502' || code === '503') {
      msg = '系统维护中，请稍后重试...';
    }
    errorNotify(msg);

    const error = new Error(msg);
    error.name = code;
    error.response = response;
    throw error;
  }
}

export function validate(ref) {
  if (Array.isArray(ref)) {
    ref.forEach(each => {
      if (each && each.validate) {
        each.validate();
      } else {
        //解决自定义日期选择组件自身无validate方法，所以取$children内q-select组件并调用其validate方法
        //如果传入的VueComponent没有validatef方法，遍历each.$children子组件并调用validate方法
        if (each && each.$children && Array.isArray(each.$children)) {
          each.$children.forEach(child => {
            if (child && child.validate) {
              child.validate();
            }
          });
        }
      }
    });
  } else {
    for (let el in ref) {
      if (ref.hasOwnProperty(el)) {
        if (ref[el].validate) {
          ref[el].validate();
        }
      }
    }
  }
}

export function deepClone(obj) {
  let objClone = Array.isArray(obj) ? [] : {};
  if (obj && typeof obj === 'object') {
    for (let key in obj) {
      // 判断obj子元素是否为对象， 如果是，递归复制
      if (obj.hasOwnProperty(key)) {
        objClone[key] = JSON.parse(JSON.stringify(obj[key]));
      } else {
        objClone[key] = obj[key];
      }
    }
  }
  return objClone;
}

export function isValidReleaseDate(beginDate, endDate, addDate = 3) {
  if (beginDate) {
    if (new Date(beginDate) == 'Invalid Date') {
      // 20190426
      beginDate =
        beginDate.slice(0, 4) +
        '-' +
        beginDate.slice(4, 6) +
        '-' +
        beginDate.slice(6, 8);
    }
    beginDate = new Date(beginDate);
  }
  if (endDate) {
    if (new Date(endDate) == 'Invalid Date') {
      endDate =
        endDate.slice(0, 4) +
        '-' +
        endDate.slice(4, 6) +
        '-' +
        endDate.slice(6, 8);
    }
    endDate = new Date(endDate);
  } else {
    endDate = new Date();
    endDate = new Date(endDate.getTime() - 24 * 60 * 60 * 1000 * addDate);
  }
  endDate = moment(endDate).format('YYYYMMDD');
  beginDate = moment(beginDate).format('YYYYMMDD');
  return beginDate >= endDate;
}

let groupObj = {};
let parentGroupObj = {};
export function getGroupFullName(groupList, groupId, hasParent, labelAdd) {
  let groupArr = groupList.filter(item => {
    return item.id == groupId;
  });
  groupObj = groupArr.length > 0 ? groupArr[0] : {};
  if (groupObj.parent_id) {
    let parentGroupArr = groupList.filter(item => {
      return item.id == groupObj.parent_id;
    });
    parentGroupObj = parentGroupArr.length > 0 ? parentGroupArr[0] : {};
    groupObj.newLabel = parentGroupObj
      ? parentGroupObj.label + '-' + groupObj.label
      : groupObj.label;
    if (parentGroupObj.parent_id) {
      parentGroupObj.newLabel = parentGroupObj.label + '-' + groupObj.name;
      labelAdd = labelAdd
        ? parentGroupObj.newLabel + '-' + labelAdd
        : parentGroupObj.newLabel;
      return getGroupFullName(
        groupList,
        parentGroupObj.parent_id,
        'hasParent',
        labelAdd
      );
    } else {
      groupObj.newLabel = labelAdd
        ? groupObj.newLabel + '-' + labelAdd
        : groupObj.newLabel;
      return groupObj.newLabel;
    }
  } else {
    if (!hasParent) {
      groupObj.newLabel = groupObj.name;
    } else if (labelAdd) {
      groupObj.newLabel = `${groupObj.name}-${labelAdd}`;
    }
    return groupObj.newLabel;
  }
}

export function getGroupOf2dLabel(groupList, groupId) {
  let groupObj = groupList.find(item => {
    return item.id == groupId;
  });
  if (groupObj.parent_id) {
    let parentGroupObj = groupList.find(item => {
      return item.id == groupObj.parent_id;
    });
    groupObj.label = `${parentGroupObj.name}-${groupObj.label}`;
  }
  return groupObj;
}

// list => [arr1,arr2,obj,string,arr[string],arr[obj]...]
export function getIdsFormList(list) {
  let ids = [];
  if (!Array.isArray(list)) {
    return [];
  }
  list.forEach(arr => {
    if (arr && Array.isArray(arr) && arr.length > 0) {
      arr.map(item => {
        if (item && typeof item === 'object') {
          ids.push(item.id);
        } else if (item && typeof item === 'string') {
          ids.push(item);
        }
        return ids;
      });
    } else if (arr && typeof arr === 'object') {
      ids = arr.id ? ids.concat(arr.id) : ids;
    } else if (arr && typeof arr === 'string') {
      ids.push(arr);
    }
  });
  return ids;
}

export function getBase64queryString(params) {
  let base = new Base64();
  let str = JSON.stringify(params);
  let result = base.encode(str);
  return result;
}

export function appendNode(parent, set, depth) {
  if (!Array.isArray(parent) || !Array.isArray(set)) {
    return [];
  }
  if (parent.length === 0 || set.length === 0) {
    return [];
  }
  if (depth === 0) {
    return parent;
  }
  const child = parent.reduce((pre, next) => {
    const nodes = set.filter(group => group.parent === next.id);
    nodes.forEach(node => (node.header = 'nodes'));

    next.children = nodes;
    return pre.concat(nodes);
  }, []);

  if (child.length > 0) {
    appendNode(child, set, --depth);
  }
  return parent;
}

export const baseUrl = window.location.origin + '/';

export function exportExcel(response, filename, format) {
  /* 文件格式 */
  format = format ? format : response.headers['content-type'];
  let file = response.data;

  if (!file.size) {
    file = new Blob([file], { type: format });
  }

  const reader = new FileReader();
  reader.readAsText(file, 'utf-8');
  reader.onload = () => {
    try {
      JSON.parse(reader.result);
    } catch {
      return;
    } finally {
      /* 文件名 */
      if (!filename) {
        const resFilename = response.headers['content-disposition'];
        filename = decodeURI(
          resFilename.substring(resFilename.indexOf('=') + 1)
        );
      }

      if (isIE()) {
        window.navigator.msSaveOrOpenBlob(file, filename);
      } else {
        const link = document.createElement('a');
        const URL = window.URL.createObjectURL(file);

        link.href = URL;
        link.download = filename;
        document.body.appendChild(link);

        link.click();

        window.URL.revokeObjectURL(link.href);
        document.body.removeChild(link);
        successNotify('下载成功！');
      }
    }
  };
}

export function isIE() {
  return !!window.ActiveXObject;
}
export function addAttribute(data) {
  if (!Array.isArray(data)) {
    return data;
  }
  return data.map(item => {
    return {
      ...item,
      children: addAttribute(item.children)
    };
  });
}

export const taskStage = {
  'create-info': '录入信息完成',
  'create-app': '录入应用完成',
  'create-feature': '录入分支完成',
  develop: '开发中',
  sit: 'SIT测试',
  uat: 'UAT测试',
  rel: 'REL测试',
  production: '已投产',
  file: '已归档',
  abort: '已删除',
  discard: '任务已废弃'
};

export const taskStage1 = {
  develop: '进行中',
  production: '已完成',
  file: '已归档'
};

export const perform = {
  operation: {
    新建: ['打开', '分配'],
    打开: ['延迟修复', '已修复', '拒绝', '分配'],
    重新打开: ['延迟修复', '已修复', '拒绝', '分配'],
    延迟修复: ['已修复', '分配'],
    拒绝: ['分配'],
    确认拒绝: [],
    已修复: [],
    关闭: []
  },
  UAToperation: {
    新建: ['打开', '分配'],
    打开: ['延迟修复', '已修复', '拒绝', '分配'],
    延迟修复: ['已修复', '分配'],
    拒绝: [],
    确认拒绝: [],
    已修复: [],
    重新打开: ['遗留', '已修复', '分配'],
    遗留: ['重新打开', '分配'],
    关闭: []
  },
  status: {
    10: '新建',
    20: '拒绝',
    30: '确认拒绝',
    40: '延迟修复',
    50: '打开',
    51: '重新打开',
    60: '待确认',
    70: '已关闭',
    80: '已修复',
    90: '关闭'
  },
  UATstatus: {
    10013: '新建',
    10306: '拒绝',
    10015: '已修复',
    10310: '延迟修复',
    1: '打开',
    10308: '确认拒绝',
    10307: '重新打开',
    10737: '遗留',
    10012: '关闭'
  },
  statusName: {
    新建: '10',
    拒绝: '20',
    确认拒绝: '30',
    延迟修复: '40',
    打开: '50',
    重新打开: '51',
    待确认: '60',
    已关闭: '70',
    已修复: '80',
    关闭: '90',
    // 重新打开: '100',
    遗留: '110'
  },
  UATstatusName: {
    新建: '10013',
    拒绝: '10306',
    确认拒绝: '10308',
    延迟修复: '10310',
    打开: '1',
    已修复: '10015',
    重新打开: '10741',
    遗留: '10737',
    关闭: '10012'
  },
  priority: {
    10: '无',
    20: '低',
    30: '中',
    40: '高',
    50: '紧急',
    60: '非常紧急'
  },
  UATpriority: {
    1: '最高',
    2: '高',
    3: '中',
    4: '低',
    5: '最低'
  },
  severity: {
    '-1': '无',
    10222: 'G0-致命',
    10223: 'G1-严重',
    10224: 'G2-一般',
    10225: 'G3-轻微',
    10226: 'G4-改善'
  },
  severityOption: [
    {
      label: '无',
      value: '-1'
    },
    {
      label: 'G0-致命',
      value: '10222'
    },
    {
      label: 'G1-严重',
      value: '10223'
    },
    {
      label: 'G2-一般',
      value: '10224'
    },
    {
      label: 'G3-轻微',
      value: '10225'
    },
    {
      label: 'G4-改善',
      value: '10226'
    }
  ],
  defectSource: {
    '-1': '无',
    10205: '业务需求问题',
    10206: '需规问题',
    10208: '功能实现不完整',
    11050: '兼容性异常',
    10209: '功能实现错误',
    10212: '后台问题',
    10213: '历史遗留问题',
    10214: '数据问题',
    10215: '环境问题',
    11047: '优化建议',
    11048: '打包问题',
    11049: '其他原因'
  },
  defectType: {
    '-1': '无',
    10227: '需求问题',
    10230: '文档问题',
    10231: '应用缺陷',
    10228: '环境问题',
    10229: '数据问题',
    10243: '兼容性异常',
    10232: '其他问题'
  },
  belongStage: {
    '-1': '无',
    10233: '编码阶段',
    10234: '集成阶段',
    10235: '设计阶段',
    10236: '需求阶段'
  },
  stageOption: [
    {
      label: '无',
      value: '-1'
    },
    {
      label: '编码阶段',
      value: '10233'
    },
    {
      label: '集成阶段',
      value: '10234'
    },
    {
      label: '设计阶段',
      value: '10235'
    },
    {
      label: '需求阶段',
      value: '10236'
    }
  ],
  priorityOptions: [
    {
      label: '无',
      value: '10'
    },
    {
      label: '低',
      value: '20'
    },
    {
      label: '中',
      value: '30'
    },
    {
      label: '高',
      value: '40'
    },
    {
      label: '紧急',
      value: '50'
    },
    {
      label: '非常紧急',
      value: '60'
    }
  ],
  UATpriorityOptions: [
    {
      label: '最高',
      value: '1'
    },
    {
      label: '高',
      value: '2'
    },
    {
      label: '中',
      value: '3'
    },
    {
      label: '低',
      value: '4'
    },
    {
      label: '最低',
      value: '5'
    }
  ],
  ponderance: {
    10: '新功能',
    20: '细节',
    30: '文字',
    40: '小调整',
    50: '小错误',
    60: '很严重',
    70: '崩溃',
    80: '宕机'
  },
  defectOptions: [
    {
      label: '无',
      value: '-1'
    },
    {
      label: '需求问题',
      value: '10227'
    },
    {
      label: '文档问题',
      value: '10230'
    },
    {
      label: '应用缺陷',
      value: '10231'
    },
    {
      label: '环境问题',
      value: '10228'
    },
    {
      label: '数据问题',
      value: '10229'
    },
    {
      label: '兼容性异常',
      value: '10243'
    },
    {
      label: '其他问题',
      value: '10232'
    }
  ],
  flawSourceLists: [
    {
      label: '无',
      value: '-1'
    },
    {
      label: '业务需求问题',
      value: '10205'
    },
    {
      label: '需规问题',
      value: '10206'
    },
    {
      label: '功能实现不完整',
      value: '10208'
    },
    {
      label: '兼容性异常',
      value: '11050'
    },
    {
      label: '功能实现错误',
      value: '10209'
    },
    {
      label: '后台问题',
      value: '10212'
    },
    {
      label: '历史遗留问题',
      value: '10213'
    },
    {
      label: '数据问题',
      value: '10214'
    },
    {
      label: '环境问题',
      value: '10215'
    },
    {
      label: '优化建议',
      value: '11047'
    },
    {
      label: '打包问题',
      value: '11048'
    },
    {
      label: '兼容性异常',
      value: '11050'
    },
    {
      label: '其他原因',
      value: '11049'
    }
  ],
  ascriptionOptions: ['', '编码阶段', '集成阶段', '设计阶段', '需求阶段'],
  defectTypeOptions: [
    '',
    '需求问题',
    '文档问题',
    '应用缺陷',
    '环境问题',
    '数据问题',
    '兼容性异常',
    '其他问题'
  ],
  iterationOptions: [
    {
      label: '无',
      value: '-1'
    },
    {
      label: '1',
      value: '11119'
    },
    {
      label: '2',
      value: '11120'
    },
    {
      label: '3',
      value: '11121'
    },
    {
      label: '4',
      value: '11122'
    },
    {
      label: '5',
      value: '11123'
    },
    {
      label: '6',
      value: '11124'
    },
    {
      label: '7',
      value: '11125'
    },
    {
      label: '8',
      value: '11126'
    }
  ],
  flawSourceList: [
    {
      label: '业务需求问题',
      value: '业务需求问题'
    },
    {
      label: '需规问题',
      value: '需规问题'
    },
    {
      label: '功能实现不完整',
      value: '功能实现不完整'
    },
    {
      label: '兼容性异常',
      value: '兼容性异常'
    },
    {
      label: '功能实现错误',
      value: '功能实现错误'
    },
    {
      label: '历史遗留问题',
      value: '历史遗留问题'
    },
    {
      label: '优化建议',
      value: '优化建议'
    },
    {
      label: '后台问题',
      value: '后台问题'
    },
    {
      label: '打包问题',
      value: '打包问题'
    },
    {
      label: '数据问题',
      value: '数据问题'
    },
    {
      label: '环境问题',
      value: '环境问题'
    },
    {
      label: '其他原因',
      value: '其他原因'
    }
  ],
  typeFilter: {
    '1': '微服务窗口',
    '2': '原生窗口',
    '3': '试运行窗口',
    '4': '组件窗口',
    '5': '骨架窗口',
    '6': '镜像窗口'
  },
  typeOptions: [
    {
      value: '1',
      label: '微服务窗口'
    },
    {
      value: '2',
      label: '原生窗口'
    },
    {
      value: '3',
      label: '试运行窗口'
    },
    {
      value: '4',
      label: '组件窗口'
    },
    {
      value: '5',
      label: '骨架窗口'
    },
    {
      value: '6',
      label: '镜像窗口'
    }
  ]
};

export function goBack() {
  window.history.back();
}

export function copyValue(val) {
  const input = document.createElement('textarea');
  input.value = val;
  document.body.appendChild(input);
  input.select();
  document.execCommand('copy');
  document.body.removeChild(input);
}

export const externalWebsite = {
  fdevIssue: 'http://xxx/ebank_fdev/fdev_issuses/issues',
  userGuide: 'xxx/view/l/teu8564'
};
