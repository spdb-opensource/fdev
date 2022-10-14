//模块内自定义公共方法
import { CI_PANORAMA_SIZE } from '../utils/constants.js';
import Notify from '#/plugins/Notify';
import LocalStorage from '#/plugins/LocalStorage';
import { successNotify } from '@/utils/utils';
import LeaderLine from 'leader-line';
import { baseUrl } from '@/utils/utils';
import axios from 'axios';
import { setTimeout } from 'core-js';
// const {
//   jobXSpace,
//   jobYSpace,
//   spaceInJob,
//   nodeHeight,
//   nodeWidth,
//   iconSize
// } = CI_PANORAMA_SIZE;

//将text复制到剪贴板
export function textToClipboard(text) {
  let dummy = document.createElement('textarea');
  document.body.appendChild(dummy);
  dummy.value = text;
  dummy.select();
  document.execCommand('copy');
  document.body.removeChild(dummy);
  successNotify('复制成功');
}
//计算CI数据的体量，用于计算画布尺寸等
const cISize = stages => {
  return stages.map(stage => stage.jobs.map(job => job.steps.length));
};

//将字符串分割后转化为Number
const strToNumArr = (id, separator) => {
  separator = separator ? separator : '-';
  return id.split(separator).map(x => Number(x));
};

//传入某个ci-node的id，返回其在画布中的位置或传入stage下标，返回节点/stage的坐标
export const nodeCoordinate = (id, stages, isTrigger) => {
  if (isTrigger) {
    let x = CI_PANORAMA_SIZE.jobXSpace / 2;
    let y =
      (id + 2) * (CI_PANORAMA_SIZE.spaceInJob + CI_PANORAMA_SIZE.nodeHeight);
    return { x, y };
  } else {
    id = String(id);
    let [isStage, isJob, isPlugin] = id.split('-').map(x => Boolean(x));
    const [sInd, jInd, pInd] = strToNumArr(id);
    const CISize = cISize(stages);
    isStage;
    let x =
      CI_PANORAMA_SIZE.jobXSpace / 2 +
      sInd * (CI_PANORAMA_SIZE.jobXSpace + CI_PANORAMA_SIZE.nodeWidth) +
      CI_PANORAMA_SIZE.iconSize / 2 +
      50;
    let stage = CISize[sInd];

    let pluginNumArr = isJob ? stage.splice(0, jInd) : stage;
    let jHeight =
      pluginNumArr
        .map(
          pNum =>
            pNum * (CI_PANORAMA_SIZE.spaceInJob + CI_PANORAMA_SIZE.nodeHeight) +
            CI_PANORAMA_SIZE.jobYSpace +
            CI_PANORAMA_SIZE.nodeHeight
        )
        .reduce((acc, curr) => acc + curr, 0) +
      CI_PANORAMA_SIZE.nodeHeight +
      CI_PANORAMA_SIZE.jobYSpace / 2;
    let y = isPlugin
      ? jHeight +
        (pInd + 1) * (CI_PANORAMA_SIZE.spaceInJob + CI_PANORAMA_SIZE.nodeHeight)
      : jHeight;
    return { x, y };
  }
};

//传入stage，返回画布尺寸
export const canvasSize = (stages, isManage, triggerBranchNum) => {
  const sIndexs = Array.from(stages.keys());
  let width = isManage
    ? stages.length *
        (CI_PANORAMA_SIZE.jobXSpace + CI_PANORAMA_SIZE.nodeWidth) +
      CI_PANORAMA_SIZE.jobXSpace
    : stages.length *
        (CI_PANORAMA_SIZE.jobXSpace + CI_PANORAMA_SIZE.nodeWidth) +
      CI_PANORAMA_SIZE.jobXSpace / 2;
  let addJobHeight = isManage
    ? CI_PANORAMA_SIZE.nodeHeight + CI_PANORAMA_SIZE.jobYSpace * 2
    : 0;
  let boxHeight = window.innerHeight * 0.7;
  let trigger = triggerBranchNum
    ? nodeCoordinate(triggerBranchNum, null, true).y
    : 0;
  let height = Math.max(
    ...sIndexs.map(sInd => nodeCoordinate(sInd, stages).y + addJobHeight),
    boxHeight,
    trigger
  );
  return { width, height };
};

//深拷贝
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

//处理接受交易
export async function ResPrompt(func, params, callback, successMsg) {
  let response = await func(params);
  if (response === undefined) return response;
  let { fdevStatus, msg } = response;
  if (fdevStatus === 'error') {
    Notify.create({
      message: msg.split('[')[0],
      color: 'negative',
      position: 'top'
    });
    return false;
  } else {
    callback && (await callback(response));
    successMsg &&
      Notify.create({
        message: successMsg,
        color: 'blue-9',
        position: 'top'
      });
    return response;
  }
}

//传入一个job中的this.$ref,在每个节点间画线
export const drawLineInJob = (obj, color) => {
  color = color ? color : '#1976D2';
  let nodes = Object.values(obj)
    .map(node => (Array.isArray(node) ? node[0] : node))
    .filter(x => x);
  if (nodes.length > 1) {
    let nodesInd = Array.from(nodes.keys());
    nodesInd.pop();
    return nodesInd.map(x =>
      lineInJob(nodes[x], nodes[x + 1], color).show('fade')
    );
  } else return [];
};

//Job中画线
const lineInJob = (start, end, color) => {
  return new LeaderLine(start, end, {
    color: color,
    size: 1,
    startPlug: 'disc',
    startPlugSize: 2.5,
    startPlugColor: 'white',
    startPlugOutline: true,
    startPlugOutlineSize: 2.5,
    startPlugOutlineColor: color,
    endPlug: 'disc',
    endPlugSize: 2.5,
    endPlugOutline: true,
    endPlugColor: 'white',
    endPlugOutlineSize: 2.5,
    endPlugOutlineColor: color,
    hide: true
  });
};

//job之间画箭头
export function drawLine(start, end, lineColor, lineEnd) {
  let color = lineColor ? lineColor : '#1976D2';
  // let dash = lineDash ? { len: 8, gap: 4 } : false;
  return new LeaderLine(start, end, {
    hide: true,
    color: color,
    size: 2,
    endPlug: lineEnd,
    endPlugSize: 2,
    path: 'grid',
    startSocket: 'right',
    endSocket: 'left'
    // dash: dash
  });
}

//两个stage的job之间画箭头
export function drawLineBetweenStage(start, end, build) {
  return build
    ? Object.keys(end.$refs).map(job => {
        return drawLine(
          start.$refs.job0[0].$refs.jobName,
          end.$refs[job][0].$refs.jobName
        );
      })
    : Object.keys(start.$refs).map(job => {
        return drawLine(
          start.$refs[job][0].$refs.jobName,
          end.$refs.job0[0].$refs.jobName
        );
      });
}

//obj的深拷贝，对于能被JSON识别的数据，function无效
export function deepCopyJSON(obj) {
  return JSON.parse(JSON.stringify(obj));
}

//obj2的key为obj1的子集，将obj2中的值赋给obj1(Object.assign())
export const objPartCopy = (obj1, obj2) => {
  let objKeys = Object.keys(obj2);
  objKeys.forEach(x => (obj1[x] = obj2[x]));
  return obj1;
};

// //换算像素高度
// export const transHeight = height => {
//   return (window.screen.height / 1080) * height + 'px';
// };

// //换算像素宽度
// export const transWidth = width => {
//   return (window.screen.width / 1920) * width + 'px';
// };

export function flat() {
  Array.prototype.flat = function(count) {
    let c = count || 1;
    let len = this.length;
    let ret = [];
    if (this.length === 0) return this;
    while (c--) {
      let _arr = [];
      let flag = false;
      if (ret.length === 0) {
        flag = true;
        for (let i = 0; i < len; i++) {
          if (this[i] instanceof Array) {
            ret.push(...this[i]);
          } else {
            ret.push(this[i]);
          }
        }
      } else {
        for (let i = 0; i < ret.length; i++) {
          if (ret[i] instanceof Array) {
            flag = true;
            _arr.push(...ret[i]);
          } else {
            _arr.push(ret[i]);
          }
        }
        ret = _arr;
      }
      if (!flag && c === Infinity) {
        break;
      }
    }
    return ret;
  };
}

/**
 * FormData()方法上传或下载文件
 * 注意，传入的参数必须是一个formdata类型
 * 如：
 * let formParam = new FormData();
 * formParam.append('参数key', file); //参数
 */
export function uploadOrDownloadFile(params_) {
  return new Promise((resolve, reject) => {
    const { type, url, params } = params_;
    //上传文件
    // let file = document.querySelector('#fileUpload').files[0];
    // file = file ? file : { size: '', name: '' };
    // let formParam = new FormData();
    // formParam.append('file', file); //参数
    const title = type === 'upload' ? '上传' : '下载';
    let config = {
      headers: {
        'Content-Type': 'multipart/form-data',
        Accept: 'application/json',
        Authorization: LocalStorage.getItem('fdev-vue-admin-jwt')
      }
    };
    axios
      .post(baseUrl + url, params, config)
      .then(res => {
        const { data } = res;
        if (data.code && data.code !== 'AAAAAAA') {
          errorNotify(title + '失败：' + data.msg);
          return;
        }
        if (type === 'upload') {
          resolve(data); //上传成功返回数据
        } else if (type === 'download') {
          exportExcel(res);
        }
        successNotify(title + '成功！');
      })
      .catch(err => {
        errorNotify(title + '失败：' + err ? err.msg : '');
      });
  });
}

// 处理下载文件流
export function exportExcel(response, filename, format) {
  /* 文件名 */
  if (!filename) {
    const resFilename = response.headers['content-disposition'];
    filename = decodeURI(resFilename.substring(resFilename.indexOf('=') + 1));
  }
  /* 文件格式 */
  format = format ? format : response.headers['content-type'];
  let file = response.data;

  if (!file.size) {
    file = new Blob([file], { type: format });
  }

  const reader = new FileReader();
  reader.readAsText(file, 'utf-8');
  reader.onload = () => {
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
    }
  };
}
export function isIE() {
  if (!!window.ActiveXObject || 'ActiveXObject' in window) {
    return true;
  } else {
    return false;
  }
}
// 防抖
export const debounce = (function() {
  let deTimer = 0;
  return function(callback, ms) {
    clearTimeout(deTimer);
    deTimer = setTimeout(callback, ms);
  };
})();
