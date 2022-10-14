<template>
  <div v-if="logDetail" class="column">
    <div class="items-start row q-mb-md justify-between">
      <span
        class="text-subtitle3 text-ellipsis text-no-wrap"
        style="max-width:600px"
        :title="logDetail.pipelineName"
        >流水线名称:{{ logDetail.pipelineName }}</span
      >
      <div class="q-gutter-lg">
        <fdev-btn ficon="eye" label="详情" normal @click="gotoHistoryPanorma" />
        <fdev-btn
          v-if="
            logDetail.status === 'pending' || logDetail.status === 'running'
          "
          ficon="stop_c_o"
          label="停止"
          normal
          @click="cancelPipeline"
        />
        <fdev-btn
          v-else
          ficon="retry_c_o"
          label="重试"
          normal
          @click="retryPipeline"
        />
      </div>
      <AutoTest
        v-if="isShowAutoTest"
        v-model="isShowAutoTest"
        :infoDate="AutoTestData"
      />
    </div>

    <div class="scroll-normal fit">
      <div ref="graph" />
    </div>
    <f-dialog
      right
      v-if="isShowCastSCScan"
      v-model="isShowCastSCScan"
      title="蓝鲸cast源代码扫描"
    >
      <castSourceCodeScan :dataSurce="AutoTestData" />
    </f-dialog>
    <SonarScan
      v-if="isShowSonarScan"
      :dataSurce="sonarData"
      :isShow.sync="isShowSonarScan"
    />
  </div>
</template>
<script>
const [
  nodeWidth,
  labelWidth,
  splitLen,
  spaceX,
  px26,
  px24,
  px22,
  px20,
  px16,
  px14,
  px12,
  px8,
  px4,
  px2
] = [320, 56, 288, 82, 26, 24, 22, 20, 16, 14, 12, 8, 2].map(x => x * RATIO);
const getNodeHeight = stepNum => {
  return px20 + px24 + px4 + stepNum * (px22 + px4) + px16 + px8 * 2 + px24;
};
const getNodeY = jobs => {
  return (
    jobs
      .map(job => getNodeHeight(job.steps.length))
      .reduce((curr, acc) => curr + acc, 0) +
    px24 * (jobs.length + 2) +
    px20
  );
};
const [bgColor, fontColor, splitLineColor, timeColor] = [
  '#ffffff',
  // '#f0f2f5',
  '#37474f',
  // '#F2F5F7',
  '#C0CED6',
  '#78909C'
];
const stepY = stepInd => {
  return px20 + px24 + px8 + (px22 + px4) * stepInd;
};
const STATUS_LABEL = {
  error: '失败',
  running: '执行',
  pending: '挂载',
  waiting: '等待',
  success: '成功',
  cancel: '停止',
  skip: '跳过'
};
import { requestStream } from '@/modules/configCI/utils/request';
import {
  CI_COLORS,
  CI_ICON,
  RATIO,
  THUMB_STYLE,
  LOG_REFREASH
} from '../utils/constants';
import {
  pipelineLogDetail,
  retryPipeline,
  retryJob,
  downLoadArtifacts,
  queryPluginResultData
} from '../services/method';
import G6 from '@antv/g6';
import { ResPrompt } from '../utils/utils';
import { mapState, mapActions } from 'vuex';
import { triggerModeCn } from '../utils/constants.js';
import AutoTest from '../components/plugManage/InterfaceAutoTest';
import castSourceCodeScan from '../components/plugManage/CastSourceCodeScan';
import SonarScan from '../components/plugManage/SonarScan';
import { errorNotify } from '@/utils/utils';
export default {
  name: 'LogPanorama',
  components: {
    castSourceCodeScan,
    AutoTest,
    SonarScan
  },
  data() {
    return {
      ...THUMB_STYLE,
      logDetail: null,
      graph: null,
      timer: null,
      triggerModeCn,
      isShowAutoTest: false,
      AutoTestData: null,
      isShowCastSCScan: false,
      isShowSonarScan: false,
      castCodeInfo: {},
      sonarData: []
    };
  },
  props: {
    id: String
  },
  beforeDestroy() {
    //清除定时器
    clearInterval(this.timer);
  },
  watch: {
    //如果运行失败，清除定时器
    stopTimer(val) {
      val && clearInterval(this.timer);
    },
    logData(val) {
      if (this.graph) {
        this.graph.changeData(val);
      }
    }
  },
  computed: {
    ...mapState('configCIForm', ['stopPipelineInfo', 'stopJobInfo']),
    //是否应该停止重发交易
    stopTimer() {
      return this.logDetail
        ? ['error', 'success', 'cancel'].includes(this.logDetail.status)
        : false;
    },
    canvasSize() {
      let { stages } = this.logDetail;
      let width = (stages.length + 1) * (nodeWidth + spaceX);
      // let nodeHeight = Math.max(...stages.map(stage => getNodeY(stage.jobs)));
      let height = 550;
      return { width, height };
    },
    logData() {
      if (this.logDetail) {
        let {
          stages,
          costTime,
          startTime,
          branch,
          bindProject,
          triggerMode,
          user,
          status
        } = this.logDetail;
        status || (status = '');
        costTime || (costTime = '');
        user = user.nameEn ? user.nameCn + '(' + user.nameEn + ')' : '';
        let jobNodes = stages
          .map((stage, sInd) => {
            return stage.jobs.map((job, jInd) => {
              let { name, steps, jobExes, stepsResultInfo } = job;
              let jobExe = jobExes[job.jobExes.length - 1];
              let { jobExeStatus, jobCostTime, jobExeId } = jobExe;
              let jobBefore = stage.jobs.slice(0, jInd);
              return {
                id: sInd + '-' + jInd,
                type: 'log-node',
                jobName: name,
                jobExeId: jobExeId,
                stepNames: steps,
                status: jobExeStatus,
                costTime: jobCostTime,
                x: spaceX / 2 + (spaceX + nodeWidth) * (sInd + 1),
                y: getNodeY(jobBefore),
                stepsResultInfo
              };
            });
          })
          .flat();
        let stageNodes = stages
          .map((stage, sInd) => {
            return [
              {
                id: 'stage' + '-' + sInd,
                type: 'stage-name',
                x: (spaceX + nodeWidth) * (sInd + 1) + spaceX / 2,
                y: 1,
                stageName: stage.name
              },
              {
                id: 't' + '-' + sInd,
                type: 'dot-node',
                x: (spaceX + nodeWidth) * (sInd + 1),
                y: (px24 + px20) * 2 + px12
              },
              {
                id: 'b' + '-' + sInd,
                type: 'dot-node',
                x: (spaceX + nodeWidth) * (sInd + 1),
                y: this.canvasSize.height
              }
            ];
          })
          .flat();
        let triggerNode = {
          id: 'triggerNode',
          hideBottom: true,
          type: 'log-node',
          jobName: '执行结果',
          stepNames: [
            '触发方式:' + this.triggerModeCn[triggerMode],
            '触发分支:' + branch,
            '项目名称:' + bindProject.nameEn,
            '构建人:' + user,
            '开始时间:' + startTime,
            '耗时:' + costTime
          ],
          status: status,
          x: spaceX / 2,
          y: getNodeY([])
        };
        let nodes = [jobNodes, triggerNode, stageNodes].flat();
        let logEdgesR = stages
          .map((stage, sInd) => {
            return [
              stage.jobs.map((job, jInd) => {
                return {
                  source: 't' + '-' + sInd,
                  target: sInd + '-' + jInd,
                  sourceAnchor: 1,
                  targetAnchor: 0
                };
              }),
              {
                source: 't' + '-' + sInd,
                target: 'b' + '-' + sInd,
                type: 'line',
                sourceAnchor: 3,
                targetAnchor: 2
              }
            ];
          })
          .flat()
          .flat();
        let logEdgesL = stages
          .slice(0, stages.length - 1)
          .map((stage, sInd) => {
            return stage.jobs.map((job, jInd) => {
              return {
                target: 't' + '-' + (sInd + 1),
                source: sInd + '-' + jInd,
                sourceAnchor: 1,
                targetAnchor: 0
              };
            });
          })
          .flat();
        let trrigerEdge = {
          target: 't-0',
          source: 'triggerNode',
          sourceAnchor: 1,
          targetAnchor: 0
        };
        let edges = [logEdgesR, logEdgesL, trrigerEdge].flat();
        return { nodes, edges };
      } else return { nodes: [], edges: [] };
    }
  },
  methods: {
    ...mapActions('configCIForm', ['stopPipeline', 'stopJob']),

    async retryMethods() {
      clearInterval(this.timer);
      await this.pipelineLogDetail();
      if (!this.stopTimer) {
        this.timer = setInterval(this.pipelineLogDetail, LOG_REFREASH);
      }
    },
    async pipelineLogDetail() {
      this.logDetail = await ResPrompt(pipelineLogDetail, {
        pipelineExeId: this.id
      });
    },
    async retryPipeline() {
      await ResPrompt(retryPipeline, {
        pipelineExeId: this.id
      });
      this.retryMethods();
    },
    async cancelPipeline() {
      await this.stopPipeline({
        pipelineExeId: this.id
      });
      this.pipelineLogDetail();
    },
    async retryJob(params) {
      await ResPrompt(retryJob, {
        pipelineExeId: this.id,
        ...params
      });
      this.retryMethods();
    },
    async cancelJob(params) {
      await this.stopJob({
        pipelineExeId: this.id,
        ...params
      });
    },
    //查看流水线详情
    gotoHistoryPanorma() {
      this.$router.push({
        path: '/configCI/historyPanorama/' + this.logDetail.pipelineId
      });
    }
  },
  async created() {},
  async mounted() {
    await this.pipelineLogDetail();
    if (!this.stopTimer) {
      this.timer = setInterval(this.pipelineLogDetail, LOG_REFREASH);
    }
    G6.registerNode('stage-name', {
      drawShape: function drawShape(cfg, group) {
        const { stageName } = cfg;
        const shape = group.addShape('rect', {
          attrs: {
            width: nodeWidth,
            height: px24 * 2
          },
          name: 'stage-name'
        });
        group.addShape('text', {
          attrs: {
            text: stageName,
            fontSize: px20,
            fontWeight: 'bold',
            fill: fontColor,
            y: px20,
            textAlign: 'center',
            textBaseline: 'middle',
            x: nodeWidth / 2
          },
          name: 'stage-name'
        });
        return shape;
      }
    });
    G6.registerNode('dot-node', {
      drawShape: function drawShape(cfg, group) {
        const shape = group.addShape('rect', {
          attrs: {
            fill: CI_COLORS.line,
            width: 1,
            height: 1
          }
        });
        return shape;
      },
      getAnchorPoints() {
        return [
          [0, 0.5], // 左侧中间
          [1, 0.5], // 右侧中间
          [0.5, 0], //上中
          [0.5, 1] //下中
        ];
      }
    });
    G6.registerNode('log-node', {
      drawShape: function drawShape(cfg, group) {
        const { status, jobName, stepNames, hideBottom } = cfg;
        let { costTime } = cfg;
        !costTime && (costTime = '');
        const stepLen = stepNames.length;
        const [refreshIcon, logIcon, stopIcon] = [
          CI_ICON.refresh,
          CI_ICON.log,
          CI_ICON.stop
        ];
        const nodeHeight = hideBottom
          ? getNodeHeight(stepLen) - px26
          : getNodeHeight(stepLen);
        const [color, icon, label] = [
          CI_COLORS[status],
          CI_ICON[status],
          STATUS_LABEL[status]
        ];
        const shape = group.addShape('rect', {
          attrs: {
            fill: color,
            width: nodeWidth,
            height: 1,
            y: px20 + px12
          }
        });
        group.addShape('rect', {
          attrs: {
            fill: bgColor,
            width: nodeWidth,
            height: nodeHeight,
            stroke: color,
            lineWidth: 1,
            radius: px8
          }
        });
        group.addShape('image', {
          attrs: {
            img: icon,
            width: px24,
            height: px24,
            x: px20,
            y: px20
          }
        });
        group.addShape('text', {
          attrs: {
            text: jobName,
            fill: fontColor,
            fontWeight: 'bold',
            fontSize: px16,
            lineHeight: px24,
            textBaseline: 'top',
            y: px20,
            x: labelWidth
          }
        });
        //状态(成功/失败..)
        group.addShape('rect', {
          attrs: {
            fill: color,
            width: labelWidth,
            height: px24,
            lineWidth: 0,
            radius: px2,
            x: nodeWidth - labelWidth - px16,
            y: px20
          }
        });
        group.addShape('text', {
          attrs: {
            text: label,
            fill: 'white',
            fontSize: px14,
            lineHeight: px20,
            textAlign: 'right',
            textBaseline: 'top',
            x: nodeWidth - labelWidth + px24,
            y: px26
          }
        });
        //step步骤
        stepNames.forEach((stepName, ind) => {
          group.addShape('text', {
            attrs: {
              text: stepName,
              fill: fontColor,
              fontSize: px14,
              lineHeight: px22,
              textBaseline: 'top',
              y: stepY(ind),
              x: labelWidth
            }
          });
        });
        if (!hideBottom) {
          group.addShape('rect', {
            attrs: {
              height: 1,
              width: splitLen,
              fill: splitLineColor,
              x: px16,
              y: stepY(stepLen) + px12
            }
          });
          if (status === 'pending' || status === 'running') {
            group.addShape('rect', {
              attrs: {
                fill: bgColor,
                width: labelWidth,
                height: px20,
                y: stepY(stepLen) + px20,
                x: px26,
                cursor: 'pointer'
              },
              name: 'stop-job-btn'
            });
            group.addShape('image', {
              attrs: {
                width: px20,
                height: px20,
                img: stopIcon,
                y: stepY(stepLen) + px20,
                x: px26,
                cursor: 'pointer'
              },
              name: 'stop-job-btn'
            });
            group.addShape('text', {
              attrs: {
                text: '停止',
                fill: fontColor,
                fontSize: px14,
                lineHeight: px22,
                textBaseline: 'top',
                y: stepY(stepLen) + px24,
                x: px26 + px24,
                cursor: 'pointer'
              },
              name: 'stop-job-btn'
            });
          } else {
            group.addShape('rect', {
              attrs: {
                fill: bgColor,
                width: labelWidth,
                height: px20,
                y: stepY(stepLen) + px20,
                x: px26,
                cursor: 'pointer'
              },
              name: 'refresh-job-btn'
            });
            group.addShape('image', {
              attrs: {
                width: px20,
                height: px20,
                img: refreshIcon,
                y: stepY(stepLen) + px20,
                x: px26,
                cursor: 'pointer'
              },
              name: 'refresh-job-btn'
            });
            group.addShape('text', {
              attrs: {
                text: '重试',
                fill: fontColor,
                fontSize: px14,
                lineHeight: px22,
                textBaseline: 'top',
                y: stepY(stepLen) + px24,
                x: px26 + px24,
                cursor: 'pointer'
              },
              name: 'refresh-job-btn'
            });
          }
          group.addShape('rect', {
            attrs: {
              fill: bgColor,
              width: labelWidth,
              height: px20,
              y: stepY(stepLen) + px20,
              x: labelWidth * 2 + px8,
              cursor: 'pointer'
            },
            name: 'log-btn'
          });
          group.addShape('image', {
            attrs: {
              width: px20,
              height: px20,
              img: logIcon,
              x: labelWidth * 2 + px8,
              y: stepY(stepLen) + px20,
              cursor: 'pointer'
            },
            name: 'log-btn'
          });
          group.addShape('text', {
            attrs: {
              text: '日志',
              fill: fontColor,
              fontSize: px14,
              lineHeight: px22,
              textBaseline: 'top',
              y: stepY(stepLen) + px24,
              x: labelWidth * 2 + px24 + px8,
              cursor: 'pointer'
            },
            name: 'log-btn'
          });

          group.addShape('text', {
            attrs: {
              text: costTime,
              fill: timeColor,
              fontSize: px14,
              lineHeight: px22,
              textBaseline: 'top',
              textAlign: 'right',
              y: stepY(stepLen) + px24,
              x: nodeWidth - px24
            }
          });
          //执行步骤下的操作按钮
          const { stepsResultInfo } = cfg;
          if (stepsResultInfo && stepsResultInfo.length) {
            stepsResultInfo.forEach((el, idx) => {
              if (el) {
                const resultFlag = el.resultDisplayFlag;
                if (el.artifactsFlag) {
                  const x = resultFlag
                    ? nodeWidth - px24 * 6 + 5
                    : nodeWidth - px24 * 3;
                  group.addShape('text', {
                    attrs: {
                      text: '制品下载',
                      fill: 'red',
                      fontSize: px14,
                      lineHeight: px22,
                      textBaseline: 'top',
                      y: stepY(idx),
                      x: x,
                      cursor: 'pointer',
                      stepIndex: idx
                    },
                    name: 'copy-product-btn'
                  });
                }
                if (resultFlag) {
                  group.addShape('text', {
                    attrs: {
                      text: '查看结果',
                      fill: '#1565C0',
                      fontSize: px14,
                      lineHeight: px22,
                      textBaseline: 'top',
                      textAlign: 'right',
                      y: stepY(idx),
                      x: nodeWidth - px16,
                      cursor: 'pointer',
                      stepIndex: idx
                    },
                    name: 'cast-auto-btn'
                  });
                }
              }
            });
          }
        }
        return shape;
      },
      getAnchorPoints() {
        return [
          [0, 0.5], // 左侧中间
          [1, 0.5], // 右侧中间
          [0.5, 0], //上中
          [0.5, 1] //下中
        ];
      }
    });
    this.graph = new G6.Graph({
      container: this.$refs.graph,
      autoPaint: true,
      ...this.canvasSize,
      defaultEdge: {
        sourceAnchor: 1,
        targetAnchor: 0,
        type: 'cubic-horizontal',
        style: {
          lineWidth: 1,
          stroke: CI_COLORS.line
        }
      }
    });
    this.graph.read(this.logData);
    this.graph.on('refresh-job-btn:click', evt => {
      const { id } = evt.item._cfg.model;
      let [stageIndex, jobIndex] = id.split('-').map(x => Number(x));
      this.retryJob({ stageIndex, jobIndex });
    });
    this.graph.on('stop-job-btn:click', evt => {
      const { id } = evt.item._cfg.model;
      let [stageIndex, jobIndex] = id.split('-').map(x => Number(x));
      this.cancelJob({ stageIndex, jobIndex });
    });
    this.graph.on('log-btn:click', evt => {
      const { jobExeId } = evt.item._cfg.model;
      this.$router.push({ path: `/configCI/jobLogProfile/${jobExeId}` });
    });
    //查看cast源代码扫描和接口自动化
    this.graph.on('cast-auto-btn:click', async evt => {
      const { id, stepsResultInfo } = evt.item._cfg.model;
      let [stageIndex, jobIndex] = id.split('-').map(x => Number(x));
      let { stepIndex } = evt.target.attrs;
      let obj = stepsResultInfo[stepIndex];
      const params = {
        pipelineExeId: this.id,
        stageIndex,
        jobIndex,
        stepIndex
      };
      const res = await queryPluginResultData(params);
      const sonarData = res.measures;
      if (!res.data && !sonarData) {
        errorNotify('查询失败,' + res.msg + ',请稍后再试！');
        return;
      }
      const { nameId } = obj;
      if (nameId === '60422c7a33ded581d8f44158') {
        // 打开 cast源代码扫描
        this.isShowCastSCScan = true;
        this.AutoTestData = res.data || {};
      } else if (nameId === '60422c7a33ded581d8f4415a') {
        // 打开接口自动化测试
        this.isShowAutoTest = true;
        if (res && res.data) {
          let autoTestDetail = [res.data.rtp_data];
          this.AutoTestData = {
            case_data: res.data.case_data || [],
            rtp_data: autoTestDetail || []
          };
        }
      } else if (
        nameId === '60483c5133ded507401989bc' ||
        nameId === '60483c5133ded507401989be'
      ) {
        // 打开 sonar扫描,前者是maven项目扫描，后者是非maven扫描
        this.isShowSonarScan = true;
        this.sonarData = sonarData || [];
      }
    });
    //点击制品
    this.graph.on('copy-product-btn:click', async evt => {
      const dialog = this.$q.dialog({
        message: '下载中...0%',
        progress: true,
        persistent: true,
        ok: true
      });
      const { id } = evt.item._cfg.model;
      let [sInd, jInd] = id.split('-').map(x => Number(x));
      let { stepIndex } = evt.target.attrs;
      let params = '';
      const { artifacts } = this.logDetail;
      if (artifacts) {
        params = artifacts.filter(
          x =>
            x.job_index === jInd &&
            x.stage_index === sInd &&
            x.plugin_index === stepIndex
        )[0].object_name;
      }
      let res = await downLoadArtifacts(params);
      let { config, data } = res;
      let blob = new Blob([data]);
      let reader = new FileReader();
      reader.readAsText(blob);
      reader.onload = e => {
        let { result } = e.target;
        if (result.indexOf('{"code":"') === 0) {
          //报错处理
          result = JSON.parse(result);
          dialog.update({
            message: `下载失败：${result.msg}`
          });
          return;
        }
        requestStream(config.url, data.size, dialog);
      };
    });
  }
};
</script>
<style scoped lang="stylus">
.detail-box
  height 800px
</style>
