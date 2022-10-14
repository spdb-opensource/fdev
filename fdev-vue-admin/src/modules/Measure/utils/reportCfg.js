export async function initCompCfg() {
  // 统计报表大类
  let REPORT_OBJ = [
    { nameEn: 'development', nameCn: '研发协作', showBlock: true },
    { nameEn: 'resourceManagement', nameCn: '资源管理', showBlock: true },
    {
      nameEn: 'EenviromentAndpublish',
      nameCn: '应用环境&投产管理',
      showBlock: true
    }
  ];
  let REPORT_CFG_OBJ = {
    // 牵头投产需求
    DemandThroughput: {
      nameEn: 'DemandThroughput',
      desc: '牵头投产需求',
      type: 'development',
      img: require('../assets/demandThroughput.svg'),
      path: '/staticReport/demandThroughput'
    },
    // 任务吞吐量
    TaskThroughput: {
      nameEn: 'TaskThroughput',
      desc: '任务吞吐量',
      type: 'development',
      img: require('../assets/taskThroughput.svg'),
      path: '/staticReport/taskThroughput'
    },
    // 项目组规模
    ProjectTeamSize: {
      nameEn: 'ProjectTeamSize',
      desc: '项目组规模',
      type: 'resourceManagement',
      img: require('../assets/ProjectTeamSize.svg'),
      path: '/staticReport/ProjectTeamSize'
    },
    // 项目组资源闲置情况
    IdleResources: {
      nameEn: 'IdleResources',
      desc: '项目组资源闲置情况',
      type: 'resourceManagement',
      img: require('../assets/IdleResources.svg'),
      path: '/staticReport/IdleResources'
    },
    // 基础架构
    BasicFrame: {
      nameEn: 'BasicFrame',
      desc: '基础架构',
      type: 'EenviromentAndpublish',
      img: require('../assets/basicFrame.svg'),
      path: '/staticReport/BasicFrame'
    },
    // 任务推进情况
    TaskPhaseChange: {
      nameEn: 'TaskPhaseChange',
      desc: '任务推进情况',
      type: 'development',
      img: require('../assets/taskPromot.svg'),
      path: '/staticReport/taskPhaseChange'
    },
    // 各阶段不同任务数量统计
    TaskStageNum: {
      nameEn: 'TaskStageNum',
      desc: '不同阶段任务数量统计',
      type: 'development',
      img: require('../assets/TaskStageNum.svg'),
      path: '/staticReport/taskStageNum'
    },
    // 生产问题统计报表
    PublishProblems: {
      nameEn: 'PublishProblems',
      desc: '生产问题报表统计',
      type: 'EenviromentAndpublish',
      img: require('../assets/publishProblems.svg'),
      path: '/staticReport/publishProblems'
    },
    // 需求统计
    RqrStatistic: {
      nameEn: 'RqrStatistic',
      desc: '需求统计',
      type: 'development',
      img: require('../assets/rqrStatistic.svg'),
      path: '/staticReport/RqrStatistic'
    },
    // 研发单元统计
    ImplUnitStatistic: {
      nameEn: 'ImplUnitStatistic',
      desc: '研发单元统计',
      type: 'development',
      img: require('../assets/implUnitStatistic.svg'),
      path: '/staticReport/ImplUnitStatistic'
    },
    //小组维度任务统计
    GroupTaskAnalysis: {
      nameEn: 'GroupTaskAnalysis',
      desc: '团队任务统计',
      type: 'development',
      img: require('../assets/groupTaskStageNum.svg'),
      path: '/staticReport/GroupTaskAnalysis'
    },
    //代码统计相关
    CodeRelate: {
      nameEn: 'CodeRelate',
      desc: '代码统计相关',
      type: 'development',
      img: require('../assets/codeRelate.svg'),
      path: '/staticReport/codeRelate'
    },
    // 玉衡测试相关
    AliothRelated: {
      nameEn: 'AliothRelated',
      desc: '玉衡测试相关',
      type: 'development',
      img: require('../assets/AliothRelated.svg'),
      path: '/staticReport/AliothRelated'
    },
    //挡板相关
    IamsRelate: {
      nameEn: 'IamsRelate',
      desc: '挡板相关',
      type: 'development',
      img: require('../assets/iamsRelate.svg'),
      path: '/staticReport/IamsRelate'
    }
  };
  let REPORT_CFG = Object.values(REPORT_CFG_OBJ);
  return {
    REPORT_CFG_OBJ,
    REPORT_CFG,
    REPORT_OBJ
  };
}
