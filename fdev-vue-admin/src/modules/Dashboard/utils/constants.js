export const DesignType = {
  Demand: '0', //设计稿审核
  Task: '1' //设计还原审核
};

export const StageType = {
  Assign: 0, //分配阶段
  Examine: 1, //审核阶段
  Suggest: 2, //修改建议阶段
  Finish: 3 //完成阶段
};
export const name = {
  create: '创建',
  production: '确认已投产',
  rel: '进入REL',
  sit: '进入SIT',
  uat: '进入UAT',
  todo: '待实施',
  develop: '开发中'
};

export const nameGroup = {
  create: '创建',
  production: '已投产',
  rel: 'REL',
  sit: 'SIT',
  uat: 'UAT',
  todo: '待实施',
  develop: '开发中'
};

export const nameKey = {
  create: 'create_ids',
  production: 'pro_ids',
  rel: 'rel_ids',
  sit: 'sit_ids',
  uat: 'uat_ids',
  todo: 'todo_ids',
  develop: 'dev_ids'
};

export function createDelayTaskModel() {
  return {
    redmine_id: '',
    memberObj: null,
    groupObj: null,
    rqrmnt: null,
    history: 'false',
    postCondition: []
  };
}

export function createrqrDelayModel() {
  return {
    name: '',
    memberObj: null,
    groupObj: null
  };
}

export const code = {
  production: '7',
  rel: '6',
  uat: '5',
  sit: '4',
  develop: '3',
  'create-feature': '2',
  'create-app': '1',
  'create-info': '0'
};

export const sonar = {
  bugs: 'bugs',
  code_smells: '异味',
  vulnerabilities: '漏洞',
  duplicated_lines_density: '重复率'
};

export function listModel() {
  return {
    start_time: '',
    end_time: '',
    module: ['5c81c4d0d3e2a1126ce30049'],
    responsible_type: '',
    responsible_name_en: '',
    deal_status: '',
    issue_level: '',
    problemType: [],
    isIncludeChildren: true,
    company: '',
    reviewer_status: ''
  };
}

export const dealStatusOptions = ['未修复', '修复中', '修复完成'];

export const issueLevelOptions = [
  { label: '流程规范性错误', value: '1' },
  { label: '一般错误', value: '2' },
  { label: '复杂错误', value: '3' }
];

export const reviewerOptions = ['已评审', '未评审'];
export const responsibleTypeOptions = [
  { label: '开发责任人', value: '1' },
  { label: '审核责任人', value: '2' },
  { label: '内测责任人', value: '3' },
  { label: '任务牵头责任人', value: '4' }
];

export const resetList = [
  '5c81c56cd3e2a1126ce3004b',
  '5e5885cd13149c000c4a2777',
  '123asdasb3241ad13adada13',
  '5d3e93ce606eeb000a22d320',
  '5d3e93ed606eeb000a22d321',
  '5d3e93f5606eeb000a22d322',
  '5d3e93fe606eeb000a22d323',
  // '5d3e9411606eeb000a22d325', //板块6-app组
  '5f9bc3479513dd000c0a1e68', //板块6-投资组
  '5d3e9418606eeb000a22d326',
  '5d6c84ce054583000aa4e9af',
  '5f9a60899513dd000c0a1b4c' //板块9-蓉亿组
];

export const problemTypeOptions = [
  '需求分析',
  '开发',
  '代码审核',
  '数据库审核',
  '内测',
  '业测',
  '打包',
  '其他'
];

export function mergeRequestModel() {
  return {
    git_user_id: '',
    group_id: '',
    start_time: '',
    end_time: ''
  };
}
export const delayStageLists = [
  { label: '启动延期', value: 'develop' },
  { label: '内测延期', value: 'sit' },
  { label: '业测延期', value: 'uat' },
  { label: '准生产延期', value: 'rel' },
  { label: '投产延期', value: 'production' }
];
export const delayNameOptions = [
  { label: '后端组件', value: '0' },
  { label: '前端组件', value: '1' },
  { label: '后端骨架', value: '2' },
  { label: '前端骨架', value: '3' },
  { label: '基础镜像', value: '4' }
];
export const nameType = {
  '0': 'componentManage/server/list',
  '1': 'componentManage/web/weblist',
  '2': 'archetypeManage/server/archetype',
  '3': 'archetypeManage/web/webArchetype',
  '4': 'imageManage'
};
export const comType = {
  '0': '后端组件',
  '1': '前端组件',
  '2': '后端骨架',
  '3': '前端骨架',
  '4': '基础镜像'
};
export const tableMark = [
  { color: 'square-allot', text: '分配阶段' },
  { color: 'square-fixing', text: '审核阶段' },
  { color: 'square-nopass', text: '修改阶段' },
  { color: 'square-finish', text: '完成' }
];
export const visibleColumns = [
  { label: '完成情况', val: 'finshFlag', sort: 1 },
  { label: '当前阶段', val: 'currentStage', sort: 2 },
  { label: '当前阶段开始时间', val: 'currentStageTime', sort: 3 },
  { label: '审核次数', val: 'checkCount', sort: 4 }
];
export const columnsTaskOptions = [
  { label: '开发人员', val: 'uiVerifyReporter', sort: 5 },
  { label: '计划提交内测日期', val: 'plan_inner_test_time', sort: 6 },
  { label: '计划提交业测日期', val: 'plan_uat_test_start_time', sort: 7 },
  { label: '实际提交内测日期', val: 'start_inner_test_time', sort: 8 },
  { label: '实际提交业测日期', val: 'start_uat_test_time', sort: 9 }, // 实施单元编号、研发单元编号
  { label: '研发单元牵头人', val: 'implementLeaderNameCN', sort: 10 },
  { label: '实施单元编号', val: 'ipmpUnitNo', sort: 11 },
  { label: '研发单元编号', val: 'fdev_implement_unit_no', sort: 12 },
  { label: 'UI还原审核状态', val: 'review_status', sort: 13 }
];
export const columnsDemandOptions = [
  { label: '开发人员', val: 'firstUploader', sort: 5 },
  { label: '计划提交内测日期', val: 'plan_inner_test_date', sort: 6 },
  { label: '计划提交业测日期', val: 'plan_test_date', sort: 7 },
  { label: '实际提交内测日期', val: 'real_inner_test_date', sort: 8 },
  { label: '实际提交业测日期', val: 'real_test_date', sort: 9 }
];
export const jobStages = [
  { label: '创建', value: 'todoList' },
  { label: '开发中', value: 'devList' },
  { label: 'SIT', value: 'sitList' },
  { label: 'UAT', value: 'uatList' },
  { label: 'REL', value: 'relList' },
  { label: '待归档', value: 'proList' }
];

export const delayStageOptions = [
  { label: '启动延期', value: 'develop' },
  { label: '内测延期', value: 'sit' },
  { label: '业测延期', value: 'uat' },
  { label: '准生产延期', value: 'rel' },
  { label: '投产延期', value: 'production' }
];

export const dayOptions = [
  { label: '1工作日', value: '1' },
  { label: '2工作日', value: '2' },
  { label: '3工作日', value: '3' },
  { label: '4工作日', value: '4' },
  { label: '5工作日', value: '5' }
];

export const timeOptions = [
  { label: '1次', value: '1' },
  { label: '2次', value: '2' },
  { label: '3次', value: '3' },
  { label: '4次', value: '4' },
  { label: '5次', value: '5' }
];

export const lgOptions = [
  { label: '小于', value: 'lt' },
  { label: '等于', value: 'eq' },
  { label: '大于', value: 'gt' }
];

export const eqOptions = [{ label: '等于', value: 'eq' }];

export const kdResultOptions = [
  { label: '正常', value: 'ok' },
  { label: '失败', value: 'fail' }
];

export const doneResultOptions = [
  { label: '通过', value: 'pass' },
  { label: '未通过', value: 'noPass' }
];

export const currentOptions = [
  { label: '分配中', value: 'alloting' },
  { label: '审核中', value: 'checking' },
  { label: '修改中', value: 'updateing' },
  { label: '完成', value: 'finish' }
];

export const conditionOptions = [
  { label: '开发上传-审核反馈', value: 'uploadToReturn' },
  { label: '审核反馈-开发上传', value: 'returnToUpload' },
  { label: '审核次数', value: 'checkCount' },
  { label: '卡点状态', value: 'positionStatus' },
  { label: '完成情况', value: 'finshFlag' },
  { label: '当前阶段', value: 'currentStage' }
];

export const filterArray = function(val) {
  if (val === '1' || val === '2') {
    return dayOptions;
  } else if (val === '3') {
    return timeOptions;
  } else if (val === '4') {
    return kdResultOptions;
  } else if (val === '5') {
    return doneResultOptions;
  } else if (val === '6') {
    return currentOptions;
  } else {
    return [];
  }
};

export const filterSArray = function(val) {
  if (
    val === 'uploadToReturn' ||
    val === 'returnToUpload' ||
    val === 'checkCount'
  ) {
    return lgOptions;
  } else if (
    val === 'positionStatus' ||
    val === 'finshFlag' ||
    val === 'currentStage'
  ) {
    return eqOptions;
  } else {
    return [];
  }
};

export const cunstomQuery = function() {
  return {
    firstSelected: null,
    firstOptions: [
      {
        label: '开发上传-审核反馈',
        value: 'uploadToReturn'
      },
      {
        label: '审核反馈-开发上传',
        value: 'returnToUpload'
      },
      {
        label: '审核次数',
        value: 'checkCount'
      },
      {
        label: '卡点状态',
        value: 'positionStatus'
      },
      {
        label: '完成情况',
        value: 'finshFlag'
      },
      {
        label: '当前阶段',
        value: 'currentStage'
      }
    ],
    secondSelected: null,
    secondOptions: [
      {
        label: '小于',
        value: 'lt'
      },
      {
        label: '等于',
        value: 'eq'
      },
      {
        label: '大于',
        value: 'gt'
      }
    ],
    thirdSelected: null,
    thirdOptions: []
  };
};

export const taskText = '';
export const mockData = [
  {
    groupName: '开发服务中心',
    finishedList: [
      {
        id: '6160015600d1ff0012e93f3c',
        name: 'ui测试1',
        redmine_id: 'FDEV-2021-09-26-00031',
        desc: '',
        spdb_master: ['5ff3c442e275e80013d3a97e'],
        master: ['5ff3c442e275e80013d3a97e'],
        group: '603716d89f7ed34b70eb40bb',
        project_id: '605451af8803d10012420360',
        project_name: 'fdev-cli-vueinfo',
        plan_start_time: '2021/09/27',
        start_time: '2021/10/08',
        plan_inner_test_time: '2021/09/29',
        start_inner_test_time: null,
        uat_merge_time: null,
        rel_merge_time: null,
        plan_uat_test_start_time: '2021/09/30',
        plan_uat_test_stop_time: '2021/10/11',
        start_uat_test_time: null,
        stop_uat_test_time: null,
        plan_rel_test_time: '2021/10/11',
        start_rel_test_time: null,
        plan_fire_time: '2021/10/12',
        fire_time: null,
        stage: 'develop',
        old_stage: null,
        feature_branch: 'feature-uics-1008',
        sit_merge_id: null,
        uat_merge_id: null,
        developer: [],
        tester: [],
        doc: [],
        concern: null,
        review: {
          other_system: [],
          data_base_alter: [
            {
              id: '6160015600d1ff0012e93f3d',
              name: '否',
              audit: false
            }
          ],
          securityTest: '不涉及',
          specialCase: [],
          commonProfile: false
        },
        report: null,
        creator: '5ff3c442e275e80013d3a97e',
        uat_testObject: null,
        rqrmnt_no: '61502afeef4d290012ec0f50',
        tag: [],
        uat_test_time: null,
        folder_id: null,
        reviewer: '5fdac6a107400100124ac2ed',
        review_status: 'finished',
        uiVerifyReporter: null,
        doc_uploader: null,
        system_remould: '否',
        impl_data: '否',
        reinforce: null,
        test_merge_id: null,
        sonarId: null,
        scanTime: null,
        difficulty_desc: '',
        designMap: {
          uploaded: [
            {
              name: '许上宇',
              remark: '',
              time: '2021-10-08 16:41:50'
            }
          ],
          wait_allot: [
            {
              name: '许上宇',
              remark: '发起审核  6 23',
              time: '2021-10-08 16:42:16'
            }
          ],
          fixing: [
            {
              name: '许上宇',
              remark: '',
              time: '2021-10-08 17:07:01'
            }
          ],
          finished: [
            {
              name: '胡亚竹',
              remark: '',
              time: '2021-10-08 17:23:29'
            }
          ]
        },
        taskType: null,
        nocodeInfoMap: null,
        newDoc: null,
        designRemark: null,
        proWantWindow: '2021/10/12',
        confirmBtn: '0',
        testKeyNote: null,
        taskSpectialStatus: null,
        deferTime: null,
        recoverTime: null,
        confirmFileDate: null,
        fdev_implement_unit_no: 'FDEV-2021-09-26-00031',
        storyId: null,
        jiraKey: null,
        checkCount: '1',
        finshFlag: 'pass',
        positionStatus: 'fail',
        currentStage: 'updateing',
        currentStageTime: '2021-10-08 17:23:29',
        implementLeaderNameCN: null,
        designDoc: [
          {
            minioPath: '6160015600d1ff0012e93f3c/ios/0/应用模块_6.zip',
            docType: 'ios',
            fileName: '应用模块_6.zip',
            uploadStage: '0'
          },
          {
            minioPath:
              '6160015600d1ff0012e93f3c/android/0/应用模块210223-标注切图.zip',
            docType: 'android',
            fileName: '应用模块210223-标注切图.zip',
            uploadStage: '0'
          }
        ],
        modify_reason: null
      }
    ],
    unfinishedList: [
      {
        id: '613823d25f01700012a4741c',
        name: '测试UI审核流程',
        redmine_id: 'FDEV-2020-11-23-00021',
        desc: '',
        spdb_master: ['5fb76ba3cabbe20012567193'],
        master: ['5fb76ba3cabbe20012567193', '5ff3c442e275e80013d3a97e'],
        group: '603716d89f7ed34b70eb40bb',
        project_id: '5fb5d4c2d47140001262a0ad',
        project_name: 'msper-xiaoneng-test',
        plan_start_time: '2020/11/18',
        start_time: '2021/09/08',
        plan_inner_test_time: '2020/11/18',
        start_inner_test_time: null,
        uat_merge_time: null,
        rel_merge_time: null,
        plan_uat_test_start_time: '2020/11/18',
        plan_uat_test_stop_time: '2020/11/18',
        start_uat_test_time: null,
        stop_uat_test_time: null,
        plan_rel_test_time: '2020/11/18',
        start_rel_test_time: null,
        plan_fire_time: '2020/11/26',
        fire_time: null,
        stage: 'develop',
        old_stage: null,
        feature_branch: 'feature_UIopt_test',
        sit_merge_id: null,
        uat_merge_id: null,
        developer: [],
        tester: [],
        doc: [],
        concern: null,
        review: {
          other_system: [],
          data_base_alter: [
            {
              id: '613823d25f01700012a4741d',
              name: '否',
              audit: false
            }
          ],
          securityTest: '不涉及',
          specialCase: [],
          commonProfile: false
        },
        report: null,
        creator: '5fb76ba3cabbe20012567193',
        uat_testObject: null,
        rqrmnt_no: '5fbb97f16ee199001278f95d',
        tag: [],
        uat_test_time: null,
        folder_id: null,
        reviewer: '5ff3c442e275e80013d3a97e',
        review_status: 'fixing',
        uiVerifyReporter: null,
        doc_uploader: null,
        system_remould: '否',
        impl_data: '否',
        reinforce: null,
        test_merge_id: null,
        sonarId: null,
        scanTime: null,
        difficulty_desc: '',
        designMap: {
          uploaded: [
            {
              name: '韩慧',
              remark: '',
              time: '2021-09-08 14:05:53'
            }
          ],
          wait_allot: [
            {
              name: '韩慧',
              remark: '',
              time: '2021-09-08 14:10:45'
            }
          ],
          fixing: [
            {
              name: '许上宇',
              remark: '',
              time: '2021-09-26 15:15:13'
            }
          ]
        },
        taskType: null,
        nocodeInfoMap: null,
        newDoc: null,
        designRemark: null,
        proWantWindow: '2020/11/26',
        confirmBtn: '0',
        testKeyNote: null,
        taskSpectialStatus: null,
        deferTime: null,
        recoverTime: null,
        confirmFileDate: null,
        fdev_implement_unit_no: 'FDEV-2020-11-23-00021',
        storyId: null,
        jiraKey: null,
        checkCount: '0',
        finshFlag: 'noPass',
        positionStatus: 'fail',
        currentStage: 'checking',
        currentStageTime: '2021-09-26 15:15:13',
        implementLeaderNameCN: null,
        designDoc: [
          {
            minioPath: '613823d25f01700012a4741c/ios/0/1.zip',
            docType: 'ios',
            fileName: '1.zip',
            uploadStage: '0'
          },
          {
            minioPath: '613823d25f01700012a4741c/android/0/1.zip',
            docType: 'android',
            fileName: '1.zip',
            uploadStage: '0'
          }
        ],
        modify_reason: null
      },
      {
        id: '61387e605f01700012a4743a',
        name: 'UI还原测试',
        redmine_id: 'FDEV-2020-11-23-00023',
        desc: '',
        spdb_master: ['5fb76ba3cabbe20012567193'],
        master: ['5ff3c442e275e80013d3a97e'],
        group: '603716d89f7ed34b70eb40bb',
        project_id: '5fb5d4c2d47140001262a0ad',
        project_name: 'msper-xiaoneng-test',
        plan_start_time: '2020/11/11',
        start_time: '2021/09/08',
        plan_inner_test_time: '2020/11/18',
        start_inner_test_time: null,
        uat_merge_time: null,
        rel_merge_time: null,
        plan_uat_test_start_time: '2020/11/19',
        plan_uat_test_stop_time: '2020/11/19',
        start_uat_test_time: null,
        stop_uat_test_time: null,
        plan_rel_test_time: '2020/11/19',
        start_rel_test_time: null,
        plan_fire_time: '2020/11/26',
        fire_time: null,
        stage: 'develop',
        old_stage: null,
        feature_branch: 'feature-0906-testUI',
        sit_merge_id: null,
        uat_merge_id: null,
        developer: ['5ff3c442e275e80013d3a97e'],
        tester: [],
        doc: [],
        concern: null,
        review: {
          other_system: [],
          data_base_alter: [
            {
              id: '61387e605f01700012a4743b',
              name: '否',
              audit: false
            }
          ],
          securityTest: '不涉及',
          specialCase: [],
          commonProfile: false
        },
        report: null,
        creator: '5ff3c442e275e80013d3a97e',
        uat_testObject: null,
        rqrmnt_no: '5fbb97f16ee199001278f95d',
        tag: [],
        uat_test_time: null,
        folder_id: null,
        reviewer: '5ff3c442e275e80013d3a97e',
        review_status: 'nopass',
        uiVerifyReporter: null,
        doc_uploader: null,
        system_remould: '否',
        impl_data: '否',
        reinforce: null,
        test_merge_id: null,
        sonarId: null,
        scanTime: null,
        difficulty_desc: '',
        designMap: {
          uploaded: [
            {
              name: '许上宇',
              remark: '',
              time: '2021-09-08 17:25:07'
            }
          ],
          wait_allot: [
            {
              name: '许上宇',
              remark: '',
              time: '2021-09-08 17:25:20'
            }
          ],
          fixing: [
            {
              name: '许上宇',
              remark: '',
              time: '2021-09-08 17:25:46'
            }
          ],
          nopass: [
            {
              stage: 'load_nopass',
              name: '许上宇',
              remark: '',
              time: '2021-09-08 17:28:02'
            }
          ]
        },
        taskType: null,
        nocodeInfoMap: null,
        newDoc: null,
        designRemark: '',
        proWantWindow: '2020/11/26',
        confirmBtn: '0',
        testKeyNote: null,
        taskSpectialStatus: null,
        deferTime: null,
        recoverTime: null,
        confirmFileDate: null,
        fdev_implement_unit_no: 'FDEV-2020-11-23-00023',
        storyId: null,
        jiraKey: null,
        checkCount: '1',
        finshFlag: 'noPass',
        positionStatus: 'fail',
        currentStage: 'updateing',
        currentStageTime: '2021-09-08 17:28:02',
        implementLeaderNameCN: null,
        designDoc: [
          {
            minioPath: '61387e605f01700012a4743a/ios/0/应用模块.zip',
            docType: 'ios',
            fileName: '应用模块.zip',
            uploadStage: '0'
          },
          {
            minioPath: '61387e605f01700012a4743a/android/0/应用模块_6.zip',
            docType: 'android',
            fileName: '应用模块_6.zip',
            uploadStage: '0'
          },
          {
            minioPath:
              '61387e605f01700012a4743a/ios/1/应用模块-设计还原审核复审0722.zip',
            docType: 'ios',
            fileName: '应用模块-设计还原审核复审0722.zip',
            uploadStage: '1'
          },
          {
            minioPath: '61387e605f01700012a4743a/android/1/应用-新增-2.8.zip',
            docType: 'android',
            fileName: '应用-新增-2.8.zip',
            uploadStage: '1'
          }
        ],
        modify_reason: null
      },
      {
        id: '61527a61910eb7001210bb97',
        name: 'lemon tree柠檬树',
        redmine_id: 'FDEV-2021-09-26-00031',
        desc: '',
        spdb_master: ['5f90eec11d39a70012e5c937'],
        master: ['5ff3c442e275e80013d3a97e'],
        group: '603716d89f7ed34b70eb40bb',
        project_id: '5f9663943365d70012fb8309',
        project_name: 'msfts-web-vue',
        plan_start_time: '2021/09/27',
        start_time: '2021/09/28',
        plan_inner_test_time: '2021/09/29',
        start_inner_test_time: null,
        uat_merge_time: null,
        rel_merge_time: null,
        plan_uat_test_start_time: '2021/09/30',
        plan_uat_test_stop_time: '2021/10/08',
        start_uat_test_time: null,
        stop_uat_test_time: null,
        plan_rel_test_time: '2021/10/08',
        start_rel_test_time: null,
        plan_fire_time: '2021/10/09',
        fire_time: null,
        stage: 'develop',
        old_stage: null,
        feature_branch: 'feature-0928-001',
        sit_merge_id: null,
        uat_merge_id: null,
        developer: [],
        tester: [],
        doc: [],
        concern: null,
        review: {
          other_system: [],
          data_base_alter: [
            {
              id: '61527a61910eb7001210bb98',
              name: '否',
              audit: false
            }
          ],
          securityTest: '不涉及',
          specialCase: [],
          commonProfile: false
        },
        report: null,
        creator: '5ff3c442e275e80013d3a97e',
        uat_testObject: null,
        rqrmnt_no: '61502afeef4d290012ec0f50',
        tag: [],
        uat_test_time: null,
        folder_id: null,
        reviewer: null,
        review_status: 'nopass',
        uiVerifyReporter: null,
        doc_uploader: null,
        system_remould: '否',
        impl_data: '否',
        reinforce: null,
        test_merge_id: null,
        sonarId: null,
        scanTime: null,
        difficulty_desc: '',
        designMap: {
          uploaded: [
            {
              name: '许上宇',
              remark: '',
              time: '2021-09-28 15:32:48'
            }
          ],
          wait_allot: [
            {
              name: '许上宇',
              remark: '',
              time: '2021-09-28 13:51:49'
            }
          ],
          nopass: [
            {
              stage: 'load_nopass',
              name: '许上宇',
              remark: '',
              time: '2021-09-28 14:47:17'
            },
            {
              stage: 'load_upload',
              name: '许上宇',
              remark: '第二次开发上传',
              time: '2021-09-28 15:19:25'
            },
            {
              stage: 'load_nopass',
              name: '许上宇',
              remark: '二轮审核反馈拒绝， 样式不一样',
              time: '2021-09-28 15:30:57'
            },
            {
              stage: 'load_upload',
              name: '许上宇',
              remark: '第三轮开发上传截图描述',
              time: '2021-09-28 15:35:08'
            },
            {
              stage: 'load_nopass',
              name: '许上宇',
              remark: '第三轮审核反馈拒绝',
              time: '2021-09-28 17:16:54'
            },
            {
              stage: 'load_upload',
              name: '许上宇',
              remark: '第四次开发上传',
              time: '2021-09-28 17:25:44'
            },
            {
              stage: 'load_nopass',
              name: '许上宇',
              remark: '第四次审核反馈拒绝',
              time: '2021-09-29 09:25:53'
            },
            {
              stage: 'load_upload',
              name: '许上宇',
              remark: '',
              time: '2021-10-08 10:05:41'
            }
          ]
        },
        taskType: null,
        nocodeInfoMap: null,
        newDoc: null,
        designRemark: null,
        proWantWindow: '2021/10/09',
        confirmBtn: '0',
        testKeyNote: null,
        taskSpectialStatus: null,
        deferTime: null,
        recoverTime: null,
        confirmFileDate: null,
        fdev_implement_unit_no: 'FDEV-2021-09-26-00031',
        storyId: null,
        jiraKey: null,
        checkCount: '4',
        finshFlag: 'noPass',
        positionStatus: 'fail',
        currentStage: 'checking',
        currentStageTime: '2021-10-08 10:05:41',
        implementLeaderNameCN: null,
        designDoc: [
          {
            minioPath: '61527a61910eb7001210bb97/ios/1/应用模块.zip',
            docType: 'ios',
            fileName: '应用模块.zip',
            uploadStage: '1'
          },
          {
            minioPath: '61527a61910eb7001210bb97/android/1/应用模块_6.zip',
            docType: 'android',
            fileName: '应用模块_6.zip',
            uploadStage: '1'
          },
          {
            minioPath: '61527a61910eb7001210bb97/ios/2/返回.svg.zip',
            docType: 'ios',
            fileName: '返回.svg.zip',
            uploadStage: '2'
          },
          {
            minioPath:
              '61527a61910eb7001210bb97/android/2/fdev通用组件210315.zip',
            docType: 'android',
            fileName: 'fdev通用组件210315.zip',
            uploadStage: '2'
          },
          {
            minioPath: '61527a61910eb7001210bb97/ios/3/应用合并210517.zip',
            docType: 'ios',
            fileName: '应用合并210517.zip',
            uploadStage: '3'
          },
          {
            minioPath:
              '61527a61910eb7001210bb97/android/3/投产流程(1)210311.zip',
            docType: 'android',
            fileName: '投产流程(1)210311.zip',
            uploadStage: '3'
          },
          {
            minioPath:
              '61527a61910eb7001210bb97/design/0/应用-新增设计稿2-2.8.zip',
            docType: 'design',
            fileName: '应用-新增设计稿2-2.8.zip',
            uploadStage: '0'
          },
          {
            minioPath:
              '61527a61910eb7001210bb97/ios/4/应用模块210223-标注切图.zip',
            docType: 'ios',
            fileName: '应用模块210223-标注切图.zip',
            uploadStage: '3'
          },
          {
            minioPath:
              '61527a61910eb7001210bb97/android/4/研发协作代码扫描210510名字可能会很长的zip文件.zip',
            docType: 'android',
            fileName: '研发协作代码扫描210510名字可能会很长的zip文件.zip',
            uploadStage: '3'
          },
          {
            minioPath:
              '61527a61910eb7001210bb97/ios/5/应用模块210223-标注切图.zip',
            docType: 'ios',
            fileName: '应用模块210223-标注切图.zip',
            uploadStage: '4'
          },
          {
            minioPath: '61527a61910eb7001210bb97/android/5/通用icon210401.zip',
            docType: 'android',
            fileName: '通用icon210401.zip',
            uploadStage: '4'
          },
          {
            minioPath: '61527a61910eb7001210bb97/ios/6/应用合并210517.zip',
            docType: 'ios',
            fileName: '应用合并210517.zip',
            uploadStage: '6'
          },
          {
            minioPath: '61527a61910eb7001210bb97/android/6/投产流程原型图2.zip',
            docType: 'android',
            fileName: '投产流程原型图2.zip',
            uploadStage: '6'
          },
          {
            minioPath: '61527a61910eb7001210bb97/ios/7/应用模块.zip',
            docType: 'ios',
            fileName: '应用模块.zip',
            uploadStage: '5'
          },
          {
            minioPath: '61527a61910eb7001210bb97/android/7/应用模块_6.zip',
            docType: 'android',
            fileName: '应用模块_6.zip',
            uploadStage: '5'
          },
          {
            minioPath: '61527a61910eb7001210bb97/ios/8/应用模块_6.zip',
            docType: 'ios',
            fileName: '应用模块_6.zip',
            uploadStage: '6'
          },
          {
            minioPath:
              '61527a61910eb7001210bb97/android/8/应用模块-设计还原审核复审0722ios.zip',
            docType: 'android',
            fileName: '应用模块-设计还原审核复审0722ios.zip',
            uploadStage: '6'
          }
        ],
        modify_reason: null
      }
    ],
    sortNum: '1-1-6',
    group: '603716d89f7ed34b70eb40bb'
  },
  {
    groupName: '互联网应用',
    finishedList: [],
    unfinishedList: [],
    sortNum: '1-1-6-4-10',
    group: '5c81c4d0d3e2a1126ce30049'
  },
  {
    groupName: '板块6-APP组',
    finishedList: [],
    unfinishedList: [
      {
        id: '5f4ca47894dd950012c7ba1b',
        name: '还原审核2',
        redmine_id: '',
        desc: 'reviewerObj',
        spdb_master: ['5daffb02f57ddb001047b48d'],
        master: ['5daff72cf57ddb001047b487'],
        group: '5d3e9411606eeb000a22d325',
        project_id: '5edeeb710fe74a0012f2cf83',
        project_name: 'mspay-web-hfvue',
        plan_start_time: '2020/08/07',
        start_time: '2020/08/31',
        plan_inner_test_time: '2020/08/07',
        start_inner_test_time: null,
        uat_merge_time: null,
        rel_merge_time: null,
        plan_uat_test_start_time: '2020/08/07',
        plan_uat_test_stop_time: '2020/08/07',
        start_uat_test_time: null,
        stop_uat_test_time: null,
        plan_rel_test_time: '2020/08/07',
        start_rel_test_time: null,
        plan_fire_time: '2020/08/07',
        fire_time: null,
        stage: 'develop',
        old_stage: null,
        feature_branch: 'hf0831',
        sit_merge_id: null,
        uat_merge_id: null,
        developer: ['5daff72cf57ddb001047b487'],
        tester: [],
        doc: [],
        concern: null,
        review: {
          other_system: [],
          data_base_alter: [
            {
              id: '5f4ca47894dd950012c7ba1c',
              name: '否',
              audit: false
            }
          ],
          securityTest: '',
          specialCase: [],
          commonProfile: false
        },
        report: null,
        creator: '5daff72cf57ddb001047b487',
        uat_testObject: null,
        rqrmnt_no: '5f2cb137c82f7b0012e28642',
        tag: [],
        uat_test_time: null,
        folder_id: '231180523366522880',
        reviewer: '5daffb02f57ddb001047b48d',
        review_status: 'nopass',
        uiVerifyReporter: null,
        doc_uploader: {
          '5daffb02f57ddb001047b48d': [
            '231208145169424384',
            '231208238912118784'
          ]
        },
        system_remould: '否',
        impl_data: '否',
        reinforce: null,
        test_merge_id: null,
        sonarId: null,
        scanTime: null,
        difficulty_desc: null,
        designMap: {
          uploaded: [
            {
              name: '言方',
              remark: '',
              time: '2020-8-31'
            }
          ],
          wait_allot: [
            {
              name: '言方',
              remark: '',
              time: '2020-8-31'
            }
          ],
          fixing: [
            {
              name: '言方',
              remark: '',
              time: '2020-8-31'
            }
          ],
          nopass: [
            {
              stage: 'load_nopass',
              name: '言方',
              remark: 'bugo1',
              time: '2020-8-31'
            },
            {
              stage: 'load_upload',
              name: '言方',
              remark: '',
              time: '2020-8-31'
            },
            {
              stage: 'load_nopass',
              name: '言方',
              remark: '2222',
              time: '2020-8-31'
            },
            {
              stage: 'load_upload',
              name: '言方',
              remark: '',
              time: '2020-9-3'
            },
            {
              stage: 'load_nopass',
              name: '言方',
              remark: '',
              time: '2020-9-3'
            }
          ]
        },
        taskType: null,
        nocodeInfoMap: null,
        newDoc: [],
        designRemark: null,
        proWantWindow: '2020/08/07',
        confirmBtn: null,
        testKeyNote: null,
        taskSpectialStatus: null,
        deferTime: null,
        recoverTime: null,
        confirmFileDate: null,
        fdev_implement_unit_no: null,
        storyId: null,
        jiraKey: null,
        checkCount: '3',
        finshFlag: 'noPass',
        positionStatus: 'fail',
        currentStage: 'updateing',
        currentStageTime: '2020-9-3',
        implementLeaderNameCN: null,
        designDoc: [
          {
            minioPath: '5f4ca47894dd950012c7ba1b/ios/0/ceshi.zip',
            docType: 'ios',
            fileName: 'ceshi.zip',
            uploadStage: '0'
          },
          {
            minioPath: '5f4ca47894dd950012c7ba1b/ios/1/code.zip',
            docType: 'ios',
            fileName: 'code.zip',
            uploadStage: '1'
          },
          {
            minioPath: '5f4ca47894dd950012c7ba1b/ios/2/表单样式.docx',
            docType: 'ios',
            fileName: '表单样式.docx',
            uploadStage: '2'
          },
          {
            minioPath: '5f4ca47894dd950012c7ba1b/ios/4/coasd大苏打大苏打de.zip',
            docType: 'ios',
            fileName: 'coasd大苏打大苏打de.zip',
            uploadStage: '3'
          },
          {
            minioPath: '5f4ca47894dd950012c7ba1b/ios/5/ceshi(1).zip',
            docType: 'ios',
            fileName: 'ceshi(1).zip',
            uploadStage: '4'
          },
          {
            minioPath: '5f4ca47894dd950012c7ba1b/ios/8/ceshi(2).zip',
            docType: 'ios',
            fileName: 'ceshi(2).zip',
            uploadStage: '6'
          },
          {
            minioPath: '5f4ca47894dd950012c7ba1b/android/0/code.zip',
            docType: 'android',
            fileName: 'code.zip',
            uploadStage: '0'
          },
          {
            minioPath: '5f4ca47894dd950012c7ba1b/android/1/ceshi.zip',
            docType: 'android',
            fileName: 'ceshi.zip',
            uploadStage: '1'
          },
          {
            minioPath: '5f4ca47894dd950012c7ba1b/android/2/提.docx',
            docType: 'android',
            fileName: '提.docx',
            uploadStage: '2'
          },
          {
            minioPath: '5f4ca47894dd950012c7ba1b/android/4/ceshi(1).zip',
            docType: 'android',
            fileName: 'ceshi(1).zip',
            uploadStage: '3'
          },
          {
            minioPath:
              '5f4ca47894dd950012c7ba1b/android/5/coasd大苏打大苏打de.zip',
            docType: 'android',
            fileName: 'coasd大苏打大苏打de.zip',
            uploadStage: '4'
          },
          {
            minioPath:
              '5f4ca47894dd950012c7ba1b/android/8/coasd大苏打大苏打de(1).zip',
            docType: 'android',
            fileName: 'coasd大苏打大苏打de(1).zip',
            uploadStage: '6'
          },
          {
            minioPath: '5f4ca47894dd950012c7ba1b/android/7/ceshi(2).zip',
            docType: 'android',
            fileName: 'ceshi(2).zip',
            uploadStage: '5'
          }
        ],
        modify_reason: null
      }
    ],
    sortNum: '1-1-6-4-10-11-6',
    group: '5d3e9411606eeb000a22d325'
  },
  {
    groupName: '公司金融组',
    finishedList: [
      {
        id: '6167da205653430012b2120c',
        name: '测试任务_完成',
        redmine_id: 'FDEV-2021-08-30-00002',
        desc: '',
        spdb_master: ['606535610680ef0012687b54'],
        master: ['606535610680ef0012687b54'],
        group: '5e5885cd13149c000c4a2777',
        project_id: '605451af8803d10012420360',
        project_name: 'fdev-cli-vueinfo',
        plan_start_time: '2021/09/01',
        start_time: '2021/10/14',
        plan_inner_test_time: '2021/09/17',
        start_inner_test_time: null,
        uat_merge_time: null,
        rel_merge_time: null,
        plan_uat_test_start_time: '2021/09/20',
        plan_uat_test_stop_time: '2021/10/14',
        start_uat_test_time: null,
        stop_uat_test_time: null,
        plan_rel_test_time: '2021/10/14',
        start_rel_test_time: null,
        plan_fire_time: '2021/10/15',
        fire_time: null,
        stage: 'develop',
        old_stage: null,
        feature_branch: '1014fenzhi8',
        sit_merge_id: null,
        uat_merge_id: null,
        developer: [],
        tester: [],
        doc: [],
        concern: null,
        review: {
          other_system: [],
          data_base_alter: [
            {
              id: '6167da205653430012b2120d',
              name: '否',
              audit: false
            }
          ],
          securityTest: '安全漏洞修复',
          specialCase: ['涉及操作系统、中间件升级/补丁修复'],
          commonProfile: false
        },
        report: null,
        creator: '606535610680ef0012687b54',
        uat_testObject: null,
        rqrmnt_no: '611f11e529a0760013e290b7',
        tag: [],
        uat_test_time: null,
        folder_id: null,
        reviewer: '606535610680ef0012687b54',
        review_status: 'finished',
        uiVerifyReporter: null,
        doc_uploader: null,
        system_remould: '否',
        impl_data: '否',
        reinforce: null,
        test_merge_id: null,
        sonarId: null,
        scanTime: null,
        difficulty_desc: '',
        designMap: {
          uploaded: [
            {
              name: '胡慧敏',
              remark: '',
              time: '2021-10-14 15:40:51'
            }
          ],
          wait_allot: [
            {
              name: '胡慧敏',
              remark: '',
              time: '2021-10-14 15:40:54'
            }
          ],
          fixing: [
            {
              name: '胡慧敏',
              remark: '',
              time: '2021-10-14 15:43:32'
            }
          ],
          finished: [
            {
              name: '胡慧敏',
              remark: '',
              time: '2021-10-14 15:43:44'
            }
          ]
        },
        taskType: null,
        nocodeInfoMap: null,
        newDoc: null,
        designRemark: null,
        proWantWindow: '2021/10/15',
        confirmBtn: '0',
        testKeyNote: null,
        taskSpectialStatus: null,
        deferTime: null,
        recoverTime: null,
        confirmFileDate: null,
        fdev_implement_unit_no: 'FDEV-2021-08-30-00002',
        storyId: null,
        jiraKey: null,
        checkCount: '1',
        finshFlag: 'pass',
        positionStatus: 'fail',
        currentStage: 'updateing',
        currentStageTime: '2021-10-14 15:43:44',
        implementLeaderNameCN: null,
        designDoc: [
          {
            minioPath: '6167da205653430012b2120c/ios/0/实施单元拆分210811.zip',
            docType: 'ios',
            fileName: '实施单元拆分210811.zip',
            uploadStage: '0'
          },
          {
            minioPath:
              '6167da205653430012b2120c/android/0/附件三：浦发银行邮箱客户端使用操作手册.zip',
            docType: 'android',
            fileName: '附件三：浦发银行邮箱客户端使用操作手册.zip',
            uploadStage: '0'
          }
        ],
        modify_reason: null
      }
    ],
    unfinishedList: [],
    sortNum: '1-1-6-4-10-2',
    group: '5e5885cd13149c000c4a2777'
  }
];
