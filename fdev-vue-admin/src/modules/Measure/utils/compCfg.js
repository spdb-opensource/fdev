import { GRAPH_LIST } from './constants';
import moment from 'moment';
export async function initCompCfg() {
  // 图表编辑弹窗的参数
  let PARAM_CFG = {
    period: {
      label: '选择周期',
      defaultOpts: [
        {
          value: '1',
          label: '每月'
        },
        { value: '0', label: '每周' }
        // { value: '2', label: '每季度' }
      ],
      compName: 'FdevSelect'
    },
    startDate: {
      label: '开始时间',
      compName: 'FDate'
    },
    endDate: {
      label: '结束时间',
      compName: 'FDate'
    },
    graphType: { label: '图表类型', compName: 'FdevSelect' },
    rankingType: {
      label: '排名顺序',
      compName: 'FdevSelect',
      defaultOpts: [
        {
          value: 'topTen',
          label: '前十名'
        },
        { value: 'lastTen', label: '后十名' }
      ]
    }
  };
  // 周期下拉选项
  let PERIOD_OPTS = PARAM_CFG.period.defaultOpts;
  // 排名顺序下拉选项
  let RANKING_OPTS = PARAM_CFG.rankingType.defaultOpts;
  // 仪表盘大类
  let COMP_TYPE = {
    development: '研发协作',
    publishManage: '投产管理',
    qualityManage: '质量管理',
    rankingList: '排行榜'
  };
  let COMP_TYPES = Object.keys(COMP_TYPE);
  // 仪表盘所有指标项
  let COMP_CFG_OBJ = {
    /*************研发协作类 **********/
    // 1：新增需求数量
    addDemandNum: {
      canAdd: true,
      nameEn: 'addDemandNum',
      nameCn: '新增需求数',
      desc: '按照您选择的周期，统计您当前所在小组在此期间内新增的需求数量',
      type: 'development',
      api: 'queryDemandNewTrend',
      paramCfg: [
        {
          type: 'graphType',
          options: GRAPH_LIST.filter(x => ['LineG', 'Bar'].includes(x.value))
        },
        {
          type: 'period',
          options: PERIOD_OPTS
        },
        {
          type: 'custom',
          label: '需求类型',
          key: 'demandType',
          compName: 'FdevSelect',
          options: [
            { value: '', label: '总和' },
            { value: 'business', label: '业务需求' },
            { value: 'tech', label: '科技需求' },
            { value: 'daily', label: '日常需求' }
          ]
        }
      ],
      paramInit: {
        graphType: { value: 'LineG', label: '折线图' },
        demandType: { value: '', label: '总和' },
        period: PERIOD_OPTS[0]
      },
      dataProcess: (data, param) => {
        let demandKeysCh = {
          total: '总和',
          tech: '科技需求',
          business: '业务需求',
          daily: '日常需求'
        };
        let graphData = [];
        let demandTypeVal =
          param.demandType.value === '' ? 'total' : param.demandType.value;
        data.product &&
          data.product.forEach(e => {
            let { period, count } = e;
            graphData.push({
              group: demandKeysCh[demandTypeVal],
              x: period,
              y: count
            });
          });
        switch (param.graphType.value) {
          case 'LineG':
            return {
              tooltip: demandKeysCh[demandTypeVal],
              data: graphData
            };
          case 'Bar':
            return {
              tooltip: demandKeysCh[demandTypeVal],
              data: graphData
            };
          default:
            return data;
        }
      },
      apiParam: function(param) {
        return {
          cycle: param.period.value,
          demandType: param.demandType.value //需求类型：总和：''、业务需求：'business'、科技需求：'tech'
        };
      }
    },
    // 2：投产需求数
    proDemandNum: {
      canAdd: true,
      nameEn: 'proDemandNum',
      nameCn: '投产需求数',
      desc: '按照您选择的周期，统计您当前所在小组在此期间内投产的需求数量',
      type: 'development',
      api: 'queryDemandProTrend',
      paramCfg: [
        {
          type: 'graphType',
          options: GRAPH_LIST.filter(x => ['LineG', 'Bar'].includes(x.value))
        },
        {
          type: 'period',
          options: PERIOD_OPTS
        },
        {
          type: 'custom',
          label: '需求类型',
          key: 'demandType',
          compName: 'FdevSelect',
          options: [
            { value: '', label: '总和' },
            { value: 'business', label: '业务需求' },
            { value: 'tech', label: '科技需求' },
            { value: 'daily', label: '日常需求' }
          ]
        }
      ],
      paramInit: {
        graphType: { value: 'Bar', label: '柱状图' },
        demandType: { value: '', label: '总和' },
        period: PERIOD_OPTS[0]
      },
      dataProcess: (data, param) => {
        let demandKeysCh = {
          total: '总和',
          tech: '科技需求',
          business: '业务需求',
          daily: '日常需求'
        };
        let graphData = [];
        let demandTypeVal =
          param.demandType.value === '' ? 'total' : param.demandType.value;
        data.product &&
          data.product.forEach(e => {
            let { period, count } = e;
            graphData.push({
              group: demandKeysCh[demandTypeVal],
              x: period,
              y: count
            });
          });
        switch (param.graphType.value) {
          case 'LineG':
            return {
              tooltip: demandKeysCh[demandTypeVal],
              data: graphData
            };
          case 'Bar':
            return {
              tooltip: demandKeysCh[demandTypeVal],
              data: graphData
            };
          default:
            return data;
        }
      },
      apiParam: function(param) {
        return {
          cycle: param.period.value,
          demandType: param.demandType.value //需求类型：总和：''、业务需求：'business'、科技需求：'tech'
        };
      }
    },
    // 2：需求吞吐量
    DemandThroughput: {
      canAdd: true,
      nameEn: 'DemandThroughput',
      nameCn: '需求吞吐量',
      desc:
        '按照您选择的周期，统计您当前所在小组在此期间内需求的新增数量和投产数量总和',
      type: 'development',
      api: 'queryDemandThroughputTrend',
      paramCfg: [
        {
          type: 'graphType',
          options: GRAPH_LIST.filter(x => ['LineG', 'Bar'].includes(x.value))
        },
        {
          type: 'period',
          options: PERIOD_OPTS
        }
      ],
      paramInit: {
        graphType: { value: 'Bar', label: '柱状图' },
        period: PERIOD_OPTS[0]
      },
      dataProcess: (data, param) => {
        let graphData = [];
        data.product &&
          data.product.forEach(e => {
            let { period, count } = e;
            graphData.push({
              group: '吞吐量',
              x: period,
              y: count
            });
          });
        switch (param.graphType.value) {
          case 'LineG':
            return {
              tooltip: '吞吐量',
              data: graphData
            };
          case 'Bar':
            return {
              tooltip: '吞吐量',
              data: graphData
            };
          default:
            return data;
        }
      },
      apiParam: function(param) {
        return { cycle: param.period.value };
      }
    },
    // 3:任务吞吐数量
    GroupTaskNum: {
      canAdd: true,
      nameEn: 'GroupTaskNum',
      nameCn: '任务吞吐量',
      desc:
        '按照您选择的周期，统计您当前所在小组在此期间内任务新增数或者投产数',
      type: 'development',
      api: 'queryTaskTrend',
      paramCfg: [
        {
          type: 'graphType',
          options: GRAPH_LIST.filter(x => ['LineG', 'Bar'].includes(x.value))
        },
        {
          type: 'period',
          options: PERIOD_OPTS
        },
        {
          type: 'custom',
          label: '类型',
          key: 'taskStage',
          compName: 'FdevSelect',
          options: [
            { value: 'create', label: '新增任务' },
            { value: 'pro', label: '投产任务' }
          ]
        }
      ],
      paramInit: {
        graphType: { value: 'LineG', label: '折线图' },
        period: PERIOD_OPTS[0],
        taskStage: { value: 'create', label: '新增任务' }
      },
      dataProcess: (data, param) => {
        let taskKeysCh = {
          create: '新增任务',
          pro: '投产任务'
        };
        let taskTypeVal = param.taskStage.value;
        let graphData = [];
        data.product &&
          data.product.forEach(e => {
            let { period, count } = e;
            graphData.push({
              group: taskKeysCh[taskTypeVal],
              x: period,
              y: count
            });
          });
        switch (param.graphType.value) {
          case 'LineG':
            return {
              tooltip: taskKeysCh[taskTypeVal],
              data: graphData
            };
          case 'Bar':
            return {
              tooltip: taskKeysCh[taskTypeVal],
              data: graphData
            };
          default:
            return data;
        }
      },
      apiParam: function(param) {
        return { cycle: param.period.value, taskStage: param.taskStage.value };
      }
    },
    /*************投产管理类 **********/
    // 1：投产次数
    publishNum: {
      canAdd: true,
      nameEn: 'publishNum',
      nameCn: '投产次数',
      desc: '按照您选择的周期，统计您当前所在小组在此期间内新增的投产次数',
      type: 'publishManage',
      unit: '%',
      api: 'queryPublishCountTrend',
      paramCfg: [
        {
          type: 'graphType',
          options: GRAPH_LIST.filter(x => ['LineG', 'Bar'].includes(x.value))
        },
        {
          type: 'period',
          options: PERIOD_OPTS
        }
      ],
      paramInit: {
        graphType: { value: 'LineG', label: '折线图' },
        period: PERIOD_OPTS[0]
      },
      dataProcess: (data, param) => {
        let graphData = [];
        data.product &&
          data.product.forEach(e => {
            let { period, count } = e;
            graphData.push({
              group: '投产次数',
              x: period,
              y: count
            });
          });
        switch (param.graphType.value) {
          case 'LineG':
            return {
              tooltip: '投产次数',
              data: graphData
            };
          case 'Bar':
            return {
              tooltip: '投产次数',
              data: graphData
            };
          default:
            return data;
        }
      },
      apiParam: function(param) {
        return { cycle: param.period.value };
      }
    },
    /*************质量管理类 **********/
    // 1：生产问题数
    publishProblemNum: {
      canAdd: true,
      nameEn: 'publishProblemNum',
      nameCn: '生产问题数',
      desc: '按照您选择的周期，统计您当前所在小组在此期间内新增生产问题数量',
      type: 'qualityManage',
      unit: '%',
      api: 'queryProIssueTrend',
      paramCfg: [
        {
          type: 'graphType',
          options: GRAPH_LIST.filter(x => ['LineG', 'Bar'].includes(x.value))
        },
        {
          type: 'period',
          options: PERIOD_OPTS
        }
      ],
      paramInit: {
        graphType: { value: 'LineG', label: '折线图' },
        period: PERIOD_OPTS[0]
      },
      dataProcess: (data, param) => {
        let graphData = [];
        data.product &&
          data.product.forEach(e => {
            let { period, count } = e;
            graphData.push({
              group: '生产问题数量',
              x: period,
              y: count
            });
          });
        switch (param.graphType.value) {
          case 'LineG':
            return {
              tooltip: '生产问题数',
              data: graphData
            };
          case 'Bar':
            return {
              tooltip: '生产问题数',
              data: graphData
            };
          default:
            return data;
        }
      },
      apiParam: function(param) {
        return { cycle: param.period.value };
      }
    },
    /*************排行榜 **********/
    // 1：代码提交行数
    CodeSubmitionRank: {
      canAdd: true,
      nameEn: 'CodeSubmitionRank',
      nameCn: '代码提交排行',
      desc: '按照您选择的时间段,统计您所在团队代码提交排行前十或末十的贡献者',
      type: 'rankingList',
      unit: '行',
      api: 'queryUserCommit',
      paramCfg: [
        {
          type: 'rankingType',
          options: RANKING_OPTS
        },
        {
          type: 'graphType',
          options: GRAPH_LIST.filter(x =>
            ['Table', 'RankingBar'].includes(x.value)
          )
        },
        {
          type: 'startDate'
        },
        {
          type: 'endDate'
        }
      ],
      paramInit: {
        graphType: { value: 'RankingBar', label: '条形图' },
        rankingType: RANKING_OPTS[0],
        startDate: moment()
          .subtract(7, 'days')
          .format('YYYY-MM-DD'),
        endDate: moment().format('YYYY-MM-DD')
      },
      dataProcess: (data, param) => {
        switch (param.graphType.value) {
          case 'Table':
            return {
              columns: [
                { name: 'num', label: '排名', field: 'num' },
                { name: 'username', label: '用户名', field: 'label' },
                {
                  name: 'count',
                  label: '代码行数',
                  field: 'value'
                }
              ],
              data: data.ranking.map((x, i) => {
                let { label, value } = x;
                return { label, value, num: i + 1 };
              })
            };
          case 'RankingBar':
            return {
              tooltip: '代码行数',
              data: data.ranking.map(x => {
                return { type: x.label, value: x.value };
              })
            };
          default:
            return data;
        }
      },
      apiParam: function(param) {
        return {
          startDate: param.startDate,
          endDate: param.endDate,
          rankingType: param.rankingType.value
        };
      }
    },
    // bug前十应用
    BranchSonarBugs: {
      canAdd: true,
      nameEn: 'BranchSonarBugs',
      nameCn: 'bug前十应用',
      desc: '根据sonar扫描fdev上所有应用的分支的结果,统计bug前十的应用',
      type: 'rankingList',
      api: 'querySonarProjectRank',
      paramCfg: [
        {
          type: 'graphType',
          options: GRAPH_LIST.filter(x => ['RankingBar'].includes(x.value))
        }
      ],
      paramInit: {
        graphType: { value: 'RankingBar', label: '条形图' }
      },
      dataProcess: (data, param) => {
        switch (param.graphType.value) {
          case 'RankingBar':
            return {
              data: data.ranking.map(x => {
                return { type: x.label, value: x.value };
              })
            };
          default:
            return data;
        }
      },
      apiParam: function(param) {
        return { code: 'bugs' };
      }
    },
    // 漏洞前十应用
    BranchSonarVulnerabilities: {
      canAdd: true,
      nameEn: 'BranchSonarVulnerabilities',
      nameCn: '漏洞前十应用',
      desc: '根据sonar扫描fdev上所有应用的分支的结果,统计漏洞前十的应用',
      type: 'rankingList',
      api: 'querySonarProjectRank',
      paramCfg: [
        {
          type: 'graphType',
          options: GRAPH_LIST.filter(x => ['RankingBar'].includes(x.value))
        }
      ],
      paramInit: {
        graphType: { value: 'RankingBar', label: '条形图' }
      },
      dataProcess: (data, param) => {
        switch (param.graphType.value) {
          case 'RankingBar':
            return {
              data: data.ranking.map(x => {
                return { type: x.label, value: x.value };
              })
            };
          default:
            return data;
        }
      },
      apiParam: function(param) {
        return { code: 'vulnerabilities' };
      }
    },
    // 异味前十应用
    BranchSonarCodeSmells: {
      canAdd: true,
      nameEn: 'BranchSonarCodeSmells',
      nameCn: '异味前十应用',
      desc: '根据sonar扫描fdev上所有应用的分支的结果,统计异味前十的应用',
      type: 'rankingList',
      api: 'querySonarProjectRank',
      paramCfg: [
        {
          type: 'graphType',
          options: GRAPH_LIST.filter(x => ['RankingBar'].includes(x.value))
        }
      ],
      paramInit: {
        graphType: { value: 'RankingBar', label: '条形图' }
      },
      dataProcess: (data, param) => {
        switch (param.graphType.value) {
          case 'RankingBar':
            return {
              data: data.ranking.map(x => {
                return { type: x.label, value: x.value };
              })
            };
          default:
            return data;
        }
      },
      apiParam: function(param) {
        return { code: 'code_smells' };
      }
    },
    // 重复率前十应用
    BranchSonarDuplicated: {
      canAdd: true,
      nameEn: 'BranchSonarDuplicated',
      nameCn: '重复率前十应用',
      desc: '根据sonar扫描fdev上所有应用的分支的结果,统计重复率前十的应用',
      type: 'rankingList',
      api: 'querySonarProjectRank',
      paramCfg: [
        {
          type: 'graphType',
          options: GRAPH_LIST.filter(x => ['RankingBar'].includes(x.value))
        }
      ],
      paramInit: {
        graphType: { value: 'RankingBar', label: '条形图' }
      },
      dataProcess: (data, param) => {
        switch (param.graphType.value) {
          case 'RankingBar':
            return {
              data: data.ranking.map(x => {
                return { type: x.label, value: x.value };
              }),
              percent: true
            };
          default:
            return data;
        }
      },
      apiParam: function(param) {
        return { code: 'duplicated_lines_density' };
      }
    }
  };
  let COMP_CFG = Object.values(COMP_CFG_OBJ);

  return { COMP_CFG_OBJ, COMP_CFG, COMP_TYPE, PARAM_CFG, COMP_TYPES };
}
