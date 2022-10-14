<template>
  <div>
    <div class="no-wrap full-width scroll-thin">
      <CIPanorama
        v-if="CIDetail"
        :CIData="CIData"
        height="40vh"
        :canvasSize="canvasSize"
        class="ci-margin"
        :RATIO="RATIO"
        :fontSize="fontSize"
      />
    </div>

    <StepView v-model="stepViewDia" type="detail" :dialog-data="stepViewData" />
    <JobDetail v-model="jobViewDia" type="detail" :jobInfo="jobViewData" />
  </div>
</template>

<script>
import CIPanorama from '../components/CIPanorama';
import StepView from './StepManage/StepView';
import JobDetail from './JobManage/JobDetail';
import { queryPipelineDetailById } from '../services/method';
import { CI_COLORS, CI_ICON } from '../utils/constants';
import { canvasSize, flat } from '../utils/utils';

export default {
  name: 'PipelinePanorama',
  components: { CIPanorama, StepView, JobDetail },
  methods: {
    // nodeCoordinate: nodeCoordinate,
    canvasSizeCal: canvasSize,
    nodeCoordinate(id, stages, isTrigger) {
      let jobXSpace = 140 * this.RATIO;
      let nodeWidth = 260 * this.RATIO;
      let jobYSpace = 60 * this.RATIO;
      let nodeHeight = 48 * this.RATIO;
      let spaceInJob = 30 * this.RATIO;
      let iconSize = 20 * this.RATIO;
      let strToNumArr = (id, separator) => {
        separator = separator ? separator : '-';
        return id.split(separator).map(x => Number(x));
      };
      if (isTrigger) {
        let x = jobXSpace / 2;
        let y = (id + 2) * (spaceInJob + nodeHeight);
        return { x, y };
      } else {
        id = String(id);
        let [isStage, isJob, isPlugin] = id.split('-').map(x => Boolean(x));
        const [sInd, jInd, pInd] = strToNumArr(id);
        const cISize = stages => {
          return stages.map(stage => stage.jobs.map(job => job.steps.length));
        };
        const CISize = cISize(stages);
        isStage;
        let x =
          jobXSpace / 2 + sInd * (jobXSpace + nodeWidth) + iconSize / 2 + 50;
        let stage = CISize[sInd];

        let pluginNumArr = isJob ? stage.splice(0, jInd) : stage;
        let jHeight =
          pluginNumArr
            .map(
              pNum => pNum * (spaceInJob + nodeHeight) + jobYSpace + nodeHeight
            )
            .reduce((acc, curr) => acc + curr, 0) +
          nodeHeight +
          jobYSpace / 2;
        let y = isPlugin
          ? jHeight + (pInd + 1) * (spaceInJob + nodeHeight)
          : jHeight;
        return { x, y };
      }
    },
    openStepDetail(id) {
      let [sInd, jInd, pInd] = id.split('-');
      this.stepViewData = this.CIDetail.stages[sInd].jobs[jInd].steps[pInd];
      this.stepViewDia = true;
    },
    openJobDetail(id) {
      let [sInd, jInd] = id.split('-');
      this.jobViewData = this.CIDetail.stages[sInd].jobs[jInd];
      this.jobViewDia = true;
    }
  },
  props: {
    id: String
  },
  data() {
    return {
      stepViewData: null,
      stepViewDia: false,
      jobViewData: null,
      jobViewDia: false,
      CIDetail: null
    };
  },
  async mounted() {
    let response = await queryPipelineDetailById({ id: this.id });
    this.CIDetail = response;
  },
  computed: {
    canvasSize() {
      let triggerBranchNum = null;
      return this.canvasSizeCal(this.CIDetail.stages, false, triggerBranchNum);
    },
    RATIO() {
      let stagesLength = this.CIDetail.stages.length;
      // 超过5条就不缩小了,字太小看不见
      if (stagesLength > 5) {
        stagesLength = 5;
      }
      return window.innerWidth / (1900 + stagesLength * 200);
    },
    fontSize() {
      let size = window.innerWidth / 1920;
      let stagesLength = this.CIDetail.stages.length;
      if (stagesLength === 4 || stagesLength === 5) {
        return size * 14;
      } else {
        return size * 16;
      }
    },
    CIData() {
      flat();
      let { stages } = this.CIDetail;
      let stagesLen = stages.length;
      let stageIndex = Array.from(stages.keys());
      let ciNodes = [];
      let triggerEdges = [];
      let triggerEdgesSchedule = [];
      let extraNodes = [];
      let extraEdges = [];
      let jobXSpace = 140 * this.RATIO;
      let nodeWidth = 260 * this.RATIO;
      let jobYSpace = 60 * this.RATIO;
      let nodeHeight = 48 * this.RATIO;
      extraNodes = stageIndex
        .map(sInd => {
          return [
            {
              id: 'add-' + sInd,
              x: sInd * (jobXSpace + nodeWidth) + 50,
              y: jobYSpace / 2 + nodeHeight * 1.5,
              type: 'dot-node',
              color: CI_COLORS.line
            },
            {
              id: 'b-' + sInd,
              x: sInd * (jobXSpace + nodeWidth) + 50,
              y: this.canvasSize.height,
              color: CI_COLORS.line,
              type: 'dot-node'
            }
          ];
        })
        .flat();

      ciNodes = stages
        .map((stage, sInd) => {
          let { jobs } = stage;
          return [
            jobs.map((job, jInd) => {
              let nodeInJob = job.steps.map((step, pInd) => {
                let id = sInd + '-' + jInd + '-' + pInd;
                let nodeCoordinate = this.nodeCoordinate(id, stages);
                return {
                  id: id,
                  ...nodeCoordinate,
                  type: 'ci-node',
                  label: step.name,
                  isFill: false,
                  leftImg: step.warning ? CI_ICON.alert : null,
                  rightImg: null,
                  //bottomImg 验证插件是否失效 true有效, false失效,当没有这个值时有效
                  bottomImg:
                    step.pluginInfo.validFlag === true ||
                    step.pluginInfo.validFlag === undefined
                      ? null
                      : CI_ICON.warnYellow,
                  color: step.warning ? CI_COLORS.error : CI_COLORS.running,
                  basicFunc: this.openStepDetail
                };
              });
              let jobId = sInd + '-' + jInd;
              let nodeCoordinate = this.nodeCoordinate(jobId, stages);
              nodeInJob.push({
                id: jobId,
                ...nodeCoordinate,
                type: 'ci-node',
                label: job.name,
                isFill: true,
                leftImg: job.warning ? CI_ICON.alertWhite : null,
                rightImg: null,
                color: job.warning ? CI_COLORS.error : CI_COLORS.running,
                basicFunc: this.openJobDetail
              });
              return nodeInJob;
            })
          ];
        })
        .flat()
        .flat()
        .flat();
      let nodes = [ciNodes, extraNodes].flat();
      let lineInJob = stages
        .map((stage, sInd) => {
          return [
            stage.jobs.map((job, jInd) => {
              let jobIndex = [];
              jobIndex = Array.from(job.steps.keys());
              jobIndex.splice(0, 1);
              let lineInJob =
                job.steps.length > 0
                  ? jobIndex
                      .map(x => {
                        return {
                          source: sInd + '-' + jInd + '-' + (x - 1),
                          target: sInd + '-' + jInd + '-' + x,
                          type: 'line',
                          sourceAnchor: 3,
                          targetAnchor: 2
                        };
                      })
                      .concat({
                        type: 'line',
                        source: sInd + '-' + jInd,
                        target: sInd + '-' + jInd + '-0',
                        sourceAnchor: 3,
                        targetAnchor: 2
                      })
                  : [];
              return lineInJob;
            })
          ];
        })
        .flat()
        .flat()
        .flat();
      let lineBetweenStage = stageIndex
        .slice(1, stagesLen)
        .map(sInd => [
          stages[sInd - 1].jobs.map((job, jInd) => {
            return {
              source: sInd - 1 + '-' + jInd,
              target: 'add-' + sInd,
              type: 'cubic-horizontal'
            };
          }),
          stages[sInd].jobs.map((job, jInd) => {
            return {
              source: 'add-' + sInd,
              target: sInd + '-' + jInd,
              type: 'cubic-horizontal'
            };
          })
        ])
        .flat()
        .flat();
      let lineBetweenTriggerAndStage = stages[0].jobs.map((job, jInd) => {
        return {
          type: 'cubic-horizontal',
          // source: 'add-0',
          target: '0-' + jInd
        };
      });
      lineBetweenTriggerAndStage.push({
        type: 'line',
        source: 'triggerMode',
        target: 'add-0'
      });
      let edges = [
        triggerEdges,
        triggerEdgesSchedule,
        lineInJob,
        lineBetweenStage,
        lineBetweenTriggerAndStage,
        extraEdges
      ].flat();
      return { nodes, edges };
    }
  }
};
</script>
<style scoped lang="stylus">
.select-app
  position relative
  top 2px
  z-index 1
  margin-top 0px
.ci-margin
 margin-left -50px
</style>
