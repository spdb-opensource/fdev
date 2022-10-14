<template>
  <f-block>
    <div align="right" class="q-mb-lg">
      <fdev-btn
        @click="versionBack"
        normal
        v-if="CIDetail && CIDetail.status === '0'"
        label="版本回退"
      />
    </div>
    <fdev-tabs v-model="tab">
      <fdev-tab
        v-for="item in tabs"
        :key="item.name"
        :name="item.name"
        :label="item.label"
      />
    </fdev-tabs>
    <fdev-tab-panels v-model="tab">
      <fdev-tab-panel name="detail" class="q-pa-none">
        <div class="row no-wrap full-width scroll-normal">
          <div class="col-2">
            <div
              class="text-center text-subtitle2 text-grey-9 title-app q-mt-md q-mb-lg"
            >
              代码源
            </div>
            <SelectApp
              v-if="CIDetail"
              :readonly="true"
              v-model="CIDetail.bindProject"
              class="select-app"
            />
          </div>
          <div class="col">
            <CIPanorama
              v-if="CIDetail"
              height="70vh"
              :CIData="CIData"
              :canvasSize="canvasSize"
            />
          </div>
        </div>
      </fdev-tab-panel>

      <fdev-tab-panel name="rules">
        <TriggerRule :id="id" style="padding:16px 0px" />
      </fdev-tab-panel>
    </fdev-tab-panels>

    <StepView v-model="stepViewDia" type="detail" :dialog-data="stepViewData" />
    <JobDetail v-model="jobViewDia" type="detail" :jobInfo="jobViewData" />
  </f-block>
</template>

<script>
import CIPanorama from '../components/CIPanorama';
import StepView from './StepManage/StepView';
import JobDetail from './JobManage/JobDetail';
import SelectApp from '../components/SelectApp';
// import TriggerCfg from '../components/TriggerCfg';
import TriggerRule from '../views/TriggerRule';
import { queryPipelineDetailById } from '../services/method';
import { CI_COLORS, CI_PANORAMA_SIZE, CI_ICON } from '../utils/constants';
import { nodeCoordinate, canvasSize, flat } from '../utils/utils';
import { mapActions, mapState } from 'vuex';
import { successNotify } from '@/utils/utils';

export default {
  name: 'HistoryPanorama',
  components: { CIPanorama, StepView, JobDetail, SelectApp, TriggerRule },
  props: {
    id: String
  },
  data() {
    return {
      stepViewData: null,
      stepViewDia: false,
      jobViewData: null,
      jobViewDia: false,
      CIDetail: null,
      tab: 'detail',
      tabs: [
        {
          name: 'detail',
          label: '流水线详情'
        },
        {
          name: 'rules',
          label: '规则配置'
        }
      ]
      // historyPipelineId: this.$route.id
    };
  },
  methods: {
    ...mapActions('configCIForm', ['setPipelineRollBack']),
    nodeCoordinate: nodeCoordinate,
    canvasSizeCal: canvasSize,
    openStepDetail(id) {
      let [sInd, jInd, pInd] = id.split('-');
      this.stepViewData = this.CIDetail.stages[sInd].jobs[jInd].steps[pInd];
      this.stepViewDia = true;
    },
    openJobDetail(id) {
      let [sInd, jInd] = id.split('-');
      this.jobViewData = this.CIDetail.stages[sInd].jobs[jInd];
      this.jobViewDia = true;
    },
    async versionBack() {
      await this.setPipelineRollBack({
        pipelineId: this.$route.params.id
      });
      successNotify('版本回退成功');
      if (this.currentVersionId) {
        this.$router.push({
          path: `/configCI/pipelineDetail/${this.currentVersionId}`
        });
      } else {
        this.$router.go(-1);
      }
    }
  },

  async mounted() {
    let response = await queryPipelineDetailById({ id: this.id });
    this.CIDetail = response;
  },
  computed: {
    ...mapState('configCIForm', ['currentVersionId']),
    canvasSize() {
      let triggerBranchNum = null;
      return this.canvasSizeCal(this.CIDetail.stages, false, triggerBranchNum);
    },
    CIData() {
      flat();
      let { stages } = this.CIDetail;
      let stagesLen = stages.length;
      let stageIndex = Array.from(stages.keys());
      let ciNodes = [];
      let triggerModeNodes = [];
      let triggerEdges = [];
      let triggerEdgesSchedule = [];
      let extraNodes = [];
      let extraEdges = [];
      let { jobXSpace, nodeWidth, jobYSpace, nodeHeight } = CI_PANORAMA_SIZE;
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
            }),
            {
              id: 'stage-' + sInd,
              type: 'stage-name',
              x: jobXSpace / 2 + sInd * (jobXSpace + nodeWidth) + 50,
              y: 0,
              eidtable: false,
              stageName: stages[sInd].name
            }
          ];
        })
        .flat()
        .flat()
        .flat();
      extraEdges = stageIndex.map(sInd => {
        return {
          source: 'add-' + sInd,
          target: 'b-' + sInd,
          type: 'line',
          sourceAnchor: 3,
          targetAnchor: 2
        };
      });
      triggerModeNodes.push({
        id: 'qishidian',
        type: 'donut',
        size: 0.5,
        x: 0,
        y: jobYSpace / 2 + nodeHeight * 1.5
        // color: CI_COLORS.running
      });
      extraEdges.push({
        source: 'qishidian',
        target: 'add-' + 0,
        type: 'line',
        sourceAnchor: 0,
        targetAnchor: 0,
        style: {
          // lineDash: [5, 5],
          // stroke: CI_COLORS.line
        }
      });
      let nodes = [triggerModeNodes, ciNodes, extraNodes].flat();
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
          source: 'add-0',
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
</style>
