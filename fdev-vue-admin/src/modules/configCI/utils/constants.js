//模块内自定义公共常量（多次使用，大写字母）
import { drawLine } from './utils';

export const LOG_REFREASH = 3000;
export const RATIO = window.innerWidth / 1920;
export const CI_COLORS = {
  error: '#EF5350',
  running: '#1565C0',
  pending: '#FD9405',
  waiting: '#FD9405',
  success: '#66BB6A',
  cancel: '#2E2E2E',
  line: '#B0BEC5',
  font: '#546E7A',
  skip: '#B0BEC5'
};

export const CI_ICON = {
  add: require('../assets/add.png'),
  addNormal: require('../assets/add.svg'),
  addRed: require('../assets/add_red.png'),
  alert: require('../assets/alert.png'),
  alertWhite: require('../assets/alert_white.png'),
  edit: require('../assets/edit.png'),
  error: require('../assets/error_red.png'),
  errorWhite: require('../assets/error_white.png'),
  errorBlue: require('../assets/error_blue.png'),
  pendingWhite: require('../assets/pending_white.png'),
  pending: require('../assets/pending.png'),
  waiting: require('../assets/pending.png'),
  processWhite: require('../assets/process_white.png'),
  running: require('../assets/process.png'),
  refresh: require('../assets/refresh.png'),
  success: require('../assets/success.png'),
  refreshRed: require('../assets/refresh_red.png'),
  log: require('../assets/log_icon.png'),
  stop: require('../assets/stop.png'),
  cancel: require('../assets/cancel.png'),
  invalid: require('../assets/alert.png'),
  warnYellow: require('../assets/warn-yellow.svg'),
  skip: require('../assets/skip.svg')
};
export const CI_PANORAMA_SIZE = {
  nodeWidth: 260 * RATIO,
  nodeHeight: 48 * RATIO,
  fontSize: 16 * RATIO,
  stageNameSize: 18 * RATIO,
  jobXSpace: 140 * RATIO,
  jobYSpace: 60 * RATIO,
  spaceInJob: 30 * RATIO,
  radius: 25 * RATIO,
  iconSize: 20 * RATIO
};
export const THUMB_STYLE = {
  thumbStyle: {
    borderRadius: '4px',
    backgroundColor: '#1565C0',
    width: '8px',
    opacity: 1
  },
  barStyle: {
    borderRadius: '4px',
    background: '#E3F2FD',
    width: '8px',
    opacity: 0.3
  }
};
export const modelListColumn = [
  {
    name: 'nameEn',
    label: '实体英文名',
    field: 'nameEn',
    align: 'left',
    sortable: true,
    copy: true
  },
  {
    name: 'nameCn',
    label: '实体中文名',
    field: 'nameCn',
    align: 'left',
    sortable: true,
    copy: true
  },
  {
    name: 'operate',
    label: '操作',
    align: 'center'
  }
];
export function createPluginAddModel() {
  return {
    pluginName: '',
    pluginDesc: '',
    entityTemplate: [],
    open: false
  };
}
export function createNodesTemplate() {
  return [
    {
      key: '',
      type: 'input',
      hint: '',
      label: '',
      default: '',
      required: true,
      hidden: false
    }
  ];
}
export function createDeleteColumns() {
  return [
    {
      name: 'name',
      label: '流水线名称',
      field: 'name',
      align: 'left',
      sortable: true
    },
    {
      name: 'projectName',
      label: '项目名称',
      field: row => row.bindProject.projectName,
      align: 'left',
      sortable: true
    },
    {
      name: 'createUser',
      label: '创建人',
      field: row => row.author.nameCn,
      align: 'left',
      sortable: true
    }
  ];
}

export function createLogColumns() {
  return [
    {
      name: 'createTime',
      label: '修改时间',
      field: 'createTime',
      align: 'left',
      sortable: true
    },
    {
      name: 'version',
      label: '版本号',
      field: 'version',
      align: 'left',
      sortable: true
    },
    {
      name: 'releaseLog',
      label: '变更日志',
      field: 'releaseLog',
      align: 'left',
      sortable: true
    }
  ];
}

export function createPluginEditModel() {
  return {
    pluginName: '',
    pluginDesc: '',
    entityTemplate: [],
    version: '',
    input: [],
    nameId: '',
    open: false
  };
}

//触发方式job
export const CiTriggerMode = {
  auto: '自动触发',
  manual: '手动触发'
};

//CIType
export const CIType = {
  pipeline: '流水线',
  temp: '模板'
};

//换算像素宽度
export const transWidth = width => {
  return (window.screen.width / 1920) * width + 'px';
};

//换算像素高度
export const transHeight = height => {
  return (window.screen.height / 1080) * height + 'px';
};

//常用宽高
export const CISTYLES = {
  jobW: transWidth(210),
  jobNameW: transWidth(160),
  jobWM: transWidth(260),
  labelInput: transWidth(340),
  allWH: transHeight(50),
  allWW: transWidth(50),
  titleH: transHeight(60),
  CIH: transHeight(750),
  pageW: transWidth(1644),
  stagePt: transHeight(35),
  jobLine: transHeight(23),
  jobCircle: transHeight(7),
  stagePw: transWidth(45),
  addBtnT: transWidth(80),
  triggerT: transHeight(85),
  triggerTD: transHeight(135),
  stageAddX: transWidth(15),
  logTriggerR: transWidth(100),
  dialogW: transWidth(430),
  mYInput: transHeight(20),
  diaTitleH: transHeight(70),
  diaContentH: transHeight(750),
  diaBtnB: transHeight(110),
  diaH: transHeight(961),
  labelW: transWidth(340),
  descInput: transWidth(500)
};

//Job间画线的mixin
export let drawLineInPage = {
  mounted: function() {
    setTimeout(() => {
      let jobRefArr = Object.keys(this.detailData.stages).map(sInd =>
        Object.keys(this.detailData.stages[sInd].jobs).map(
          jInd => 'job_' + sInd + '_' + jInd
        )
      );
      jobRefArr[0].forEach(node =>
        this.lines.push(
          drawLine(
            this.$refs.triggerMode.$refs.triggerMode,
            this.$refs[node][0].$refs.jobNode,
            this.lineColor
          )
        )
      );
      Object.keys(jobRefArr)
        .slice(0, jobRefArr.length - 1)
        .forEach(sInd => {
          jobRefArr[sInd].forEach(node => {
            this.lines.push(
              drawLine(
                this.$refs[node][0].$refs.jobNode,
                this.$refs[jobRefArr[Number(sInd) + 1][0]][0].$refs.jobNode,
                this.lineColor
              )
            );
          });
        });
      this.lines.forEach(line => line.show('draw'));
    }, 500);
  }
};

//temp空数据
export const enptyTemp = {
  name: '',
  desc: '',
  triggerMode: ['manual', 'auto'],
  label: [],
  stages: []
};

//line相关的mixin
export let aboutLines = {
  mounted: function() {
    //添加监听，使线随着屏幕划动而移动
    window.addEventListener('scroll', this.linePosition, true);
  },
  beforeDestroy: function() {
    //移除监听
    window.removeEventListener('scroll', this.linePosition, true);
    //删掉线
    this.lineRemove();
  },
  methods: {
    //lines位移
    linePosition() {
      this.lines.forEach(line => line.position());
    },
    //移除lines
    lineRemove() {
      this.lines.forEach(line => line.remove());
      // lines.forEach(x => x.remove());
    }
  },
  data: function() {
    return {
      //顺便引入一个样式定义
      ciStyles: CISTYLES,
      //定义lines
      lines: []
    };
  }
};

//job空数据
export const jobObj = {
  name: '',
  desc: '',
  info: {
    image: {
      name: '',
      id: '',
      path: ''
    }
  },
  steps: [stepObj]
};

//stage空数据
export const stageObj = {
  name: '',
  jobs: [jobObj]
};

//日志刷新时间
export const logRefreshTime = 3000;

//step空数据
export const stepObj = {
  name: '',
  desc: '',
  pluginInfo: {
    id: '',
    pluginName: '',
    pluginDesc: '',
    author: { nameCn: '' },
    version: '',
    input: [
      {
        key: '',
        value: ''
      }
    ],
    entityTemplate: [{ nameEn: '', id: '' }],
    entity: [{ nameEn: '', id: '' }],
    artifacts: []
  }
};
export const cronReg =
  '(((^([0-9]|[0-5][0-9])(\\,|\\-|\\/){1}([0-9]|[0-5][0-9]) )|^([0-9]|[0-5][0-9]) |^(\\* ))((([0-9]|[0-5][0-9])(\\,|\\-|\\/){1}([0-9]|[0-5][0-9]) )|([0-9]|[0-5][0-9]) |(\\* ))((([0-9]|[01][0-9]|2[0-3])(\\,|\\-|\\/){1}([0-9]|[01][0-9]|2[0-3]) )|([0-9]|[01][0-9]|2[0-3]) |(\\* ))((([0-9]|[0-2][0-9]|3[01])(\\,|\\-|\\/){1}([0-9]|[0-2][0-9]|3[01]) )|(([0-9]|[0-2][0-9]|3[01]) )|(\\? )|(\\* )|(([1-9]|[0-2][0-9]|3[01])L )|([1-7]W )|(LW )|([1-7]\\#[1-4] ))((([1-9]|0[1-9]|1[0-2])(\\,|\\-|\\/){1}([1-9]|0[1-9]|1[0-2]) )|([1-9]|0[1-9]|1[0-2]) |(\\* ))(([1-7](\\,|\\-|\\/){1}[1-7])|([1-7])|(\\?)|(\\*)|(([1-7]L)|([1-7]\\#[1-4]))))|(((^([0-9]|[0-5][0-9])(\\,|\\-|\\/){1}([0-9]|[0-5][0-9]) )|^([0-9]|[0-5][0-9]) |^(\\* ))((([0-9]|[0-5][0-9])(\\,|\\-|\\/){1}([0-9]|[0-5][0-9]) )|([0-9]|[0-5][0-9]) |(\\* ))((([0-9]|[01][0-9]|2[0-3])(\\,|\\-|\\/){1}([0-9]|[01][0-9]|2[0-3]) )|([0-9]|[01][0-9]|2[0-3]) |(\\* ))((([0-9]|[0-2][0-9]|3[01])(\\,|\\-|\\/){1}([0-9]|[0-2][0-9]|3[01]) )|(([0-9]|[0-2][0-9]|3[01]) )|(\\? )|(\\* )|(([1-9]|[0-2][0-9]|3[01])L )|([1-7]W )|(LW )|([1-7]\\#[1-4] ))((([1-9]|0[1-9]|1[0-2])(\\,|\\-|\\/){1}([1-9]|0[1-9]|1[0-2]) )|([1-9]|0[1-9]|1[0-2]) |(\\* ))(([1-7](\\,|\\-|\\/){1}[1-7] )|([1-7] )|(\\? )|(\\* )|(([1-7]L )|([1-7]\\#[1-4]) ))((19[789][0-9]|20[0-9][0-9])\\-(19[789][0-9]|20[0-9][0-9])))';

export const triggerModeCn = {
  manual: '手动',
  push: 'push事件',
  schedule: '定时'
};

export function createUpdateModel() {
  return {
    runnerClusterName: '',
    platform: '',
    runnerList: []
  };
}

// // 计算显示的字符串
// export function fittingString(str, maxWidth, fontSize) {
//   let fontWidth = fontSize * 1.3;
//   maxWidth =maxWidth *2
//   let width = this.calcStrLen(str) * fontWidth;
//   let ellipsis="...";
//   if(width > maxWidth) {
//     let actualLen = Math.floor((maxWidth -10)/ fontWidth);
//     let result = str.substring(0, actualLen) + ellipsis;
//     return result;
//   }
//   return str;
// }

//  计算字符串长度
export function calcStrLen(str) {
  let len = 0;
  for (let i = 0; i < str.length; i++) {
    if (str.charCodeAt(i) > 0 && str.charCodeAt(i) < 128) {
      len++;
    } else {
      len += 2;
    }
  }
  return len;
}

export const STATUS_CFG = {
  error: { label: '失败', color: 'red-4', icon: 'fail' },
  running: { label: '执行', color: 'cyan-4', icon: 'process' },
  waiting: { label: '等待', color: 'orange-7', icon: 'pending' },
  pending: { label: '挂载', color: 'orange-7', icon: 'pending' },
  success: { label: '成功', color: 'green-6', icon: 'success' },
  cancel: { label: '取消', color: 'blue-10', icon: 'cancel' },
  skip: { label: '跳过', color: 'blue-grey-3', icon: 'skip' }
};

export function createEntityModel() {
  return {
    nameEn: '', // 实体英文名
    nameCn: '', // 实体中文名
    entityTemplate: null // 具体的实体模板
  };
}

// 实体列表表头
export function entityColumns() {
  return {
    columns: [
      {
        name: 'nameEn',
        label: '实体英文名',
        field: 'nameEn'
      },
      {
        name: 'nameCn',
        label: '实体中文名',
        field: 'nameCn'
      },
      {
        name: 'templateName',
        label: '实体类型',
        field: 'templateName'
      },
      {
        name: 'createUserName',
        label: '实体创建人',
        field: 'createUserName'
      },
      {
        name: 'createTime',
        label: '实体创建时间',
        field: 'createTime'
      },
      {
        name: 'updateUserName',
        label: '最新修改人',
        field: 'updateUserName'
      },
      {
        name: 'updateTime',
        label: '最新修改时间',
        field: 'updateTime'
      }
    ]
  };
}

// 环境类型列表

export const envTypeList = [
  { name: 'SIT', label: 'SIT' },
  { name: 'UAT', label: 'UAT' },
  { name: 'REAL', label: 'REAL' },
  { name: 'PROD', label: 'PROD' }
];

// 环境类型背景色
export function createEnvTypeColorMap() {
  return {
    SIT: '#0378EA',
    UAT: '#8CBC48',
    REAL: '#FD8D00',
    PROD: '#4A66DB'
  };
}

export const trueOrFalseOptions = [
  {
    label: '是',
    value: true
  },
  {
    label: '否',
    value: false
  }
];

export function createOperateTypeMap() {
  return {
    0: '编辑',
    1: '新增',
    2: '删除'
  };
}

export const mapLogListColumns = [
  {
    name: 'envName',
    label: '环境'
  },
  {
    name: 'operateType',
    label: '操作类型',
    field: row => createOperateTypeMap()[row.operateType]
  },
  {
    name: 'updateUserName',
    label: '操作人'
  },
  {
    name: 'updateTime',
    label: '操作时间'
  }
];

export const mapLogDetailColumns = [
  {
    name: 'opt',
    label: '',
    field: 'opt',
    align: 'left'
  },
  {
    name: 'nameEn',
    label: '属性',
    field: 'nameEn',
    align: 'left'
  },
  {
    name: 'nameCn',
    label: '属性中文名',
    field: 'nameCn',
    align: 'left'
  },
  {
    name: 'before',
    label: '变动前值',
    align: 'left'
  },
  {
    name: 'after',
    label: '变动后值',
    align: 'left'
  }
];

export const entityLogListColumns = [
  {
    name: 'updateType',
    label: '操作类型',
    align: 'left'
  },
  {
    name: 'updateUserName',
    label: '操作人',
    align: 'left'
  },
  {
    name: 'updateTime',
    label: '操作时间',
    align: 'left'
  }
];

export const entityLogDetailColumns = [
  {
    name: 'opt',
    label: '',
    field: 'opt',
    align: 'left'
  },
  {
    name: 'desc',
    label: '描述',
    field: 'desc',
    align: 'left'
  }
];

export const configFilesdeployColumns = [
  {
    name: 'name_zh',
    label: '应用中文名'
  },
  {
    name: 'name_en',
    label: '应用英文名'
  },
  // {
  //   name: 'spdbManagers',
  //   label: '行内应用负责人',
  //   field: row =>
  //     row.spdbManagers.length === 0
  //       ? '-'
  //       : row.spdbManagers.map(item => item.nameCn).join(','),
  //   align: 'left'
  // },
  // {
  //   name: 'devManagers',
  //   label: '厂商应用负责人',
  //   field: row =>
  //     row.devManagers.length === 0
  //       ? '-'
  //       : row.devManagers.map(item => item.nameCn).join(','),
  //   align: 'left'
  // },
  // {
  //   name: 'groupName',
  //   label: '所属小组',
  //   field: row => row.groupName || '-',
  //   align: 'left'
  // },
  {
    name: 'gitAddress',
    label: 'gitlab仓库'
  }
  // {
  //   name: 'branch',
  //   label: '分支名',
  //   field: row => row.branch || '-',
  //   align: 'left'
  // }
];

export const deployColumns = [
  {
    name: 'name_zh',
    label: '应用中文名'
  },
  {
    name: 'name_en',
    label: '应用英文名'
  },
  // {
  //   name: 'spdbManagers',
  //   label: '行内应用负责人',
  //   align: 'left'
  // },
  // {
  //   name: 'devManagers',
  //   label: '厂商应用负责人',
  //   align: 'left'
  // },
  // {
  //   name: 'groupName',
  //   label: '所属小组',
  //   field: row => row.groupName || '-',
  //   align: 'left'
  // },
  {
    name: 'pipelineName',
    label: '流水线名称'
  }
];

export const propertyColumns = [
  {
    name: 'nameCn',
    label: '属性中文名',
    field: row => row.nameCn || '-',
    copy: true
  },
  {
    name: 'nameEn',
    label: '属性英文名',
    field: row => row.nameEn || '-',
    copy: true
  },
  {
    name: 'required',
    label: '是否必输'
  }
];

// tab
export const tabsList = [
  {
    name: 'entityRecord',
    label: '实体修改记录'
  },
  {
    name: 'mappiRecord',
    label: '映射值修改记录'
  },
  {
    name: 'dependency',
    label: '依赖分析'
  }
];
