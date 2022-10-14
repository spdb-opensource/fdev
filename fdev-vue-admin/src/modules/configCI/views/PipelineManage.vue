<template>
  <!-- 编辑,模板编辑页面 -->
  <div v-if="CIDetail" class="column fit">
    <div
      class="q-px-lg items-center row no-wrap justify-between"
      v-if="isFromPipeTemp || isFromAddTemp"
    >
      <f-formitem
        class="text-blue-9 text-no-wrap page-title-style"
        row
        label="流水线模版名称:"
        label-style="margin-left: 15px"
      >
        <fdev-input
          :borderless="nameBorderless"
          :outlined="nameOutLined"
          v-model="CIDetail.name"
          @focus="nameFocus"
          @blur="nameBlur"
          placeholder="请输入流水线模版名称"
        >
          <template v-slot:append>
            <f-icon name="edit" :width="16" :height="16" />
          </template>
        </fdev-input>
      </f-formitem>
      <div class="row items-center no-wrap">
        <div v-for="(item, index) in pipeTempBtns" :key="index">
          <fdev-btn
            normal
            class="q-mr-sm"
            :ficon="item.icon"
            :label="item.label"
            @click="item.callback"
          />
        </div>
      </div>
    </div>
    <div class="q-pl-xs items-center row no-wrap justify-between" v-else>
      <f-formitem
        class="text-blue-9 text-no-wrap page-title-style"
        row
        label="流水线名称:"
        label-style="margin-left: 15px"
      >
        <fdev-input
          :borderless="nameBorderless"
          :outlined="nameOutLined"
          v-model="CIDetail.name"
          @focus="nameFocus"
          @blur="nameBlur"
          placeholder="请输入流水线名称"
          :title="CIDetail.name"
        >
          <template v-slot:append>
            <f-icon name="edit" :width="16" :height="16" />
          </template>
        </fdev-input>
      </f-formitem>
      <div class="row items-center no-wrap">
        <fdev-btn
          class="q-mr-sm"
          v-if="!(CIDetail.fixedModeFlag || ifTempManege)"
          normal
          ficon="combine"
          label="拼接"
          @click="this.openConcatPipe"
        />
        <div v-for="(item, index) in btns" :key="index">
          <fdev-btn
            class="q-mr-sm"
            normal
            :ficon="item.icon"
            :label="item.label"
            @click="item.callback"
          />
        </div>
      </div>
    </div>
    <div class="text-orange-5 text-caption q-ml-md" v-if="requiredWarning">
      提示：标红项表示需要补全参数
    </div>
    <!-- 流水线图 -->
    <div class="row q-my-md no-wrap scroll-thin-x full-width">
      <div class="col-2">
        <div
          class="text-center text-subtitle2 text-grey-9 title-app q-mt-md q-mb-lg"
        >
          代码源
        </div>
        <!-- 流水线模板编辑时,代码源不让选择 -->
        <SelectApp
          :readonly="cantSelectApp"
          v-model="CIDetail.bindProject"
          class="select-app"
          :disabled="isFromPipeTemp || isFromAddTemp"
        />
      </div>
      <div class="col">
        <CIPanorama :CIData="CIData" :canvasSize="canvasSize" />
      </div>
    </div>
    <!-- 编辑任务弹窗,后进入jobEdit -->
    <JobDetail
      v-model="jobViewDia"
      @receive-job-info="editJob"
      type="edit"
      :jobInfo="jobViewData"
    />
    <!-- 进入stepDetail -->
    <StepView
      v-model="stepViewDia"
      @receive-step-info="editStep"
      type="edit"
      :dialog-data="stepViewData"
    />
    <SelectBranchDia
      v-if="selectBranchDia"
      :id="pipelineId"
      v-model="selectBranchDia"
      :pipeData="CIDetail"
    />
    <!-- 选择任务组弹窗,选完之后打开编辑任务弹窗 -->
    <SelectJob
      v-model="selectJobModalOpened"
      @getJob="getJob"
      @close="selectJobModalOpened = false"
    />
    <f-dialog v-model="typeDialog" title="保存流水线模板">
      <f-formitem label="模板类型">
        <div class="flex">
          <fdev-radio
            val="false"
            v-model="fixedModeFlag"
            label="自由模板"
            class="q-pr-lg"
          />
          <fdev-radio val="true" v-model="fixedModeFlag" label="固定模板" />
        </div>
      </f-formitem>
      <div class="q-ml-md text-grey-7 q-mt-md">
        根据固定模板创建的流水线不可以更改步骤结构，自由模板可以增减步骤。
      </div>
      <div class="float-right q-pa-md">
        <fdev-btn label="取消" outline dialog @click="typeDialog = false" />
        <fdev-btn label="确定" dialog class="q-ml-md" @click="addPipeTemp" />
      </div>
    </f-dialog>
    <fdev-form @submit="concatPipe">
      <f-dialog v-model="concatPipeDia" f-sl-sc title="拼接流水线">
        <f-formitem
          help="在当前流水线末尾拼接所选择流水线"
          label="选择流水线"
          diaS
          ><fdev-select
            :placeholder="concatPipePlacehoder"
            ref="concatPipe"
            :rules="[val => val || '请选择待拼接流水线']"
            :options="appPipelineList"
            v-model="concatPipeline"
          />
        </f-formitem>
        <template v-slot:btnSlot>
          <fdev-btn label="取消" outline dialog @click="CancelConcatPipe" />
          <fdev-btn label="拼接" type="submit" @click="concatPipe" dialog />
        </template> </f-dialog
    ></fdev-form>
  </div>
</template>
<script>
import LocalStorage from '#/plugins/LocalStorage';
import CIPanorama from '../components/CIPanorama';
import JobDetail from './JobManage/JobDetail';
import { CI_COLORS, CI_ICON, CI_PANORAMA_SIZE } from '../utils/constants';
import { nodeCoordinate, canvasSize, ResPrompt, flat } from '../utils/utils';
import StepView from '../views/StepManage/StepView';
import SelectApp from '../components/SelectApp';
import SelectBranchDia from '../components/SelectBranchDia';
import SelectJob from './StepManage/SelectJob';
import { resolveResponseError } from '@/utils/utils';

import {
  queryAbandonDetail,
  queryPipelineDetailById,
  pipelineUpdate,
  queryImageList,
  queryPipelineTempDetailById,
  pipelineAdd,
  queryAppPipelineList
} from '../services/method';
import { successNotify } from '@/utils/utils';
import { mapActions } from 'vuex';
export default {
  name: 'PipelineManage',
  components: {
    CIPanorama,
    JobDetail,
    SelectApp,
    StepView,
    SelectBranchDia,
    SelectJob
  },
  props: {
    id: String,
    fromType: String
  },
  data() {
    return {
      concatPipePlacehoder: '请选择流水线',
      appPipelineList: null,
      concatPipeline: null,
      cantSelectApp: false,
      nameBorderless: true,
      nameOutLined: false,
      selectBranchDia: false,
      defaultImg: { id: '', name: '', path: '' },
      jobViewDia: false,
      jobViewData: null,
      stepViewData: null,
      stepViewDia: false,
      nowManage: null,
      CIDetail: null,
      pipelineId: null,
      selectJobModalOpened: false,
      pipelineTemplateNameId: '',
      pipelineTemplateId: '',
      typeDialog: false,
      fixedModeFlag: 'false',
      concatPipeDia: false,
      requiredWarning: false //是否有未输入项
    };
  },
  computed: {
    isFromTemp() {
      return this.fromType === 'temp';
    },
    // 编辑流水线模板，不校验必输，不让选应用，不可执行
    isFromPipeTemp() {
      return this.fromType === 'template';
    },
    // 新增流水线模板，不校验必输，不让选应用，不可执行
    isFromAddTemp() {
      return this.fromType === 'addTemplate';
    },
    //是否是模板编辑、修改操作
    ifTempManege() {
      return ['addTemplate', 'template'].includes(this.fromType);
    },
    // 固定模式新增流水线，只允许在原有结构修改，不可删除和新增
    isFromFixed() {
      return this.fromType === 'fixed' || this.CIDetail.fixedModeFlag;
    },
    btns() {
      return [
        {
          label: '保存',
          icon: 'download',
          callback: this.afterSavePipeline
        },
        {
          icon: 'run',
          label: '保存并执行',
          callback: this.runPipeline
        },
        {
          icon: 'close',
          label: '取消',
          style: 'text-blue-grey-5',
          callback: this.cancleManage
        }
      ];
    },
    pipeTempBtns() {
      return [
        {
          label: '保存',
          icon: 'download',
          callback: this.afterSaveTempPipeline
        },
        {
          icon: 'close',
          label: '取消',
          style: 'text-blue-grey-5',
          callback: this.cancleTempManage
        }
      ];
    },
    bindProjectFlag() {
      return this.CIDetail.bindProject && this.CIDetail.bindProject.projectId;
    },
    cantSaveWords() {
      return !this.CIDetail.name
        ? '请输入流水线名称'
        : !this.bindProjectFlag
        ? '请选择应用'
        : this.pipelineCanSave.stages.length === 0
        ? '当前可保存阶段数为0'
        : null;
    },
    pipelineCanSaveFlag() {
      return (
        this.bindProjectFlag &&
        this.pipelineCanSave.stages.length > 0 &&
        this.CIDetail.name
      );
    },
    canvasSize() {
      return this.canvasSizeCal(this.CIDetail.stages, true);
    },
    pipelineCanSave() {
      let pipelineCanSave = JSON.parse(JSON.stringify(this.pipelineDetail));
      if (!this.$route.query.type) {
        pipelineCanSave.pipelineTemplateNameId = this.pipelineTemplateNameId;
        pipelineCanSave.pipelineTemplateId = this.pipelineTemplateId;
      }

      // 暂时注释流水线新增和编辑报红部分的数据校验不通过
      // let detailStages = this.pipelineDetail.stages;
      // let stages = [];
      // for (let i = 0; i < detailStages.length; i++) {
      //   if (detailStages[i].jobs.every(job => !job.warning)) {
      //     stages.push(detailStages[i]);
      //   } else break;
      // }
      // pipelineCanSave.stages = stages;
      return pipelineCanSave;
    },
    pipelineDetail() {
      let pipelineDetail = JSON.parse(JSON.stringify(this.CIDetail));
      pipelineDetail.stages.forEach(stage =>
        stage.jobs.forEach(job => {
          // 流水线模版编辑不校验
          job.warning = !job.steps.every(step => !step.warning);
        })
      );
      return pipelineDetail;
    },
    CIData() {
      flat();
      let { stages } = this.pipelineDetail;
      stages = stages || [];
      let stagesLen = stages.length;
      let stageIndex = Array.from(stages.keys());
      let ciNodes = [];
      let triggerModeNodes = [];
      let extraNodes = [];
      let extraEdges = [];
      let edgesInTrigger = [];
      let triggerEdges = [];
      let triggerEdgesSchedule = [];
      const {
        jobXSpace,
        nodeWidth,
        jobYSpace,
        nodeHeight,
        iconSize
      } = CI_PANORAMA_SIZE;

      let addIndex = stageIndex.concat(stagesLen);
      extraNodes = addIndex
        .map(sInd => {
          // 固定模式，不可添加，隐藏icon
          if (this.isFromFixed) {
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
          } else {
            return [
              {
                id: 'add-' + sInd,
                x: sInd * (jobXSpace + nodeWidth) + 50,
                y: (jobYSpace - iconSize) / 2 + nodeHeight * 1.5,
                type: 'add-stage',
                onClick: this.openStepDialog,
                funcType: 'addStage'
              },
              {
                id: 'b-' + sInd,
                x: sInd * (jobXSpace + nodeWidth) + iconSize / 2 + 50,
                y: this.canvasSize.height,
                color: CI_COLORS.line,
                type: 'dot-node'
              }
            ];
          }
        })
        .flat();
      extraNodes.push({
        id: 'add-' + (stagesLen + 1),
        x: (stagesLen + 1.5) * jobXSpace + stagesLen * nodeWidth + 50,
        y: jobYSpace / 2 + nodeHeight * 1.5,
        type: 'dot-node',
        color: CI_COLORS.line
      });
      ciNodes = stages
        .map((stage, sInd) => {
          let { jobs } = stage;
          let nodeCoordinate = this.nodeCoordinate(sInd, stages);
          let nodesPipe = [
            jobs.map((job, jInd) => {
              let nodeInJob = job.steps.map((step, pInd) => {
                let id = sInd + '-' + jInd + '-' + pInd;
                let nodeCoordinate = this.nodeCoordinate(id, stages);
                return {
                  id: id,
                  ...nodeCoordinate,
                  type: 'ci-node',
                  basicFunc: this.openStepEditDia,
                  rightBtnFunc: this.deleteStep,
                  bottomBtnFunc: this.openStepDialog,
                  funcType: 'addStep',
                  label: step.name,
                  isFill: false,
                  //  流水线模版编辑不校验,流水线模板新增不校验
                  // leftImg:
                  //   step.warning && !(this.isFromPipeTemp || this.isFromAddTemp)
                  //     ? CI_ICON.alert
                  //     : null,
                  // rightImg:
                  //   step.warning && !this.isFromPipeTemp
                  //     ? CI_ICON.error
                  //     : CI_ICON.errorBlue,

                  //bottomImg 验证插件是否失效 true有效, false失效,当没有这个值时有效
                  bottomImg:
                    step.pluginInfo.validFlag === true ||
                    step.pluginInfo.validFlag === undefined
                      ? null
                      : CI_ICON.warnYellow,
                  color:
                    step.warning && !(this.isFromPipeTemp || this.isFromAddTemp)
                      ? CI_COLORS.error
                      : CI_COLORS.running
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
                // 流水线模版编辑不校验
                // leftImg:
                //   job.warning && !(this.isFromPipeTemp || this.isFromAddTemp)
                //     ? CI_ICON.alertWhite
                //     : null,
                // 固定模式不可删除，隐藏icon
                rightImg: !this.isFromFixed ? CI_ICON.errorWhite : null,
                // bottomImg:
                //   job.warning && !this.isFromPipeTemp
                //     ? CI_ICON.addRed
                //     : CI_ICON.add,
                color:
                  job.warning && !(this.isFromPipeTemp || this.isFromAddTemp)
                    ? CI_COLORS.error
                    : CI_COLORS.running,
                rightBtnFunc: this.deleteJob,
                bottomBtnFunc: this.openStepDialog,
                funcType: 'addStep',
                basicFunc: this.openJobEditDia
              });
              if (job.warning && !(this.isFromPipeTemp || this.isFromAddTemp)) {
                // eslint-disable-next-line vue/no-side-effects-in-computed-properties
                this.requiredWarning = true;
              }
              return nodeInJob;
            }),
            {
              id: 'stage-' + sInd,
              type: 'stage-name',
              basicFunc: this.changeStageName,
              editImg: CI_ICON.edit,
              x: jobXSpace / 2 + sInd * (jobXSpace + nodeWidth) + 50,
              y: 0,
              eidtable: true,
              stageName: stages[sInd].name
            }
          ];
          // 固定模式，不可添加job
          if (!this.isFromFixed) {
            nodesPipe.push({
              id: 'addJob-' + sInd,
              type: 'add-job',
              onClick: this.openStepDialog,
              funcType: 'addJob',
              ...nodeCoordinate
            });
          }
          return nodesPipe;
        })
        .flat()
        .flat()
        .flat();
      stagesLen > 0
        ? (extraEdges = stages[stagesLen - 1].jobs.map((job, jInd) => {
            return {
              source: stagesLen - 1 + '-' + jInd,
              target: 'add-' + stagesLen,
              type: 'cubic-horizontal',
              sourceAnchor: 1,
              targetAnchor: 0,
              style: {
                stroke: CI_COLORS.line
              }
            };
          }))
        : null;
      extraEdges = extraEdges.concat(
        [
          {
            source: 'add-' + stagesLen,
            target: 'add-' + (stagesLen + 1),
            type: 'line',
            sourceAnchor: 1,
            targetAnchor: 0,
            style: {
              lineDash: [5, 5],
              stroke: CI_COLORS.line
            }
          }
        ],
        addIndex.map(sInd => {
          return {
            source: 'add-' + sInd,
            target: 'b-' + sInd,
            type: 'line',
            sourceAnchor: 3,
            targetAnchor: 2,
            style: {
              stroke: CI_COLORS.line
            }
          };
        })
      );
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
                          targetAnchor: 2,
                          style: {
                            stroke: CI_COLORS.line
                          }
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
              sourceAnchor: 1,
              targetAnchor: 0,
              type: 'cubic-horizontal'
            };
          }),
          stages[sInd].jobs.map((job, jInd) => {
            return {
              source: 'add-' + sInd,
              target: sInd + '-' + jInd,
              sourceAnchor: 1,
              targetAnchor: 0,
              type: 'cubic-horizontal'
            };
          })
        ])
        .flat()
        .flat();
      let lineBetweenTriggerAndStage =
        stages.length > 0
          ? stages[0].jobs.map((job, jInd) => {
              return {
                type: 'cubic-horizontal',
                source: 'add-0',
                target: '0-' + jInd
              };
            })
          : [];
      let edges = [
        triggerEdges,
        triggerEdgesSchedule,
        lineInJob,
        lineBetweenStage,
        lineBetweenTriggerAndStage,
        extraEdges,
        edgesInTrigger
      ].flat();
      return { nodes, edges };
    }
  },
  watch: {
    'CIDetail.bindProject.projectId': {
      async handler(val) {
        if (val) {
          let appPipelineList = await queryAppPipelineList({
            applicationId: val,
            pageNum: 0,
            pageSize: 0
          });
          let { total, pipelineList } = appPipelineList;
          if (total > 0) {
            this.appPipelineList = pipelineList.map(val => {
              let { name, id } = val;
              return { label: name, value: id };
            });
            this.concatPipePlacehoder = '请选择流水线';
          } else {
            this.appPipelineList = null;
            this.concatPipePlacehoder = '当前无可选择流水线';
          }
        }
      },
      immediate: true
    }
  },
  methods: {
    ...mapActions('configCIForm', [
      'pipelineTemplateUpdate',
      'pipelineTemplateAdd'
    ]),
    nodeCoordinate: nodeCoordinate,
    canvasSizeCal: canvasSize,
    nameFocus() {
      this.nameOutLined = true;
      this.nameBorderless = false;
    },
    /*
      TODO 选择任务的逻辑，数据组装
      + addStage
      slectJob =>{category, job}
      任务编辑 jobEdit
      插件信息编辑 pluginEdit
      或者插件新增 selectPlugin
    */
    getJob({ job, info }) {
      this.selectJobModalOpened = false;
      this.jobViewDia = true;
      // TODO add的应该是一个stage
      // this.jobViewData = this.pipelineDetail.stages[sInd].jobs[jInd];
      /* let param = {
        stageName: info.categoryName,
        jobs: [job],
        name: info.categoryName,
        status: null,
        jobNames: ''
      }; */
      // this.CIDetail.stages.push(param);
      job.steps = job.steps.map(step => {
        return {
          ...step,
          name: step.pluginInfo.name,
          warning: true
        };
      });
      this.jobViewData = {
        name: info.categoryName,
        ...job,
        warning: true
      };
    },
    openConcatPipe() {
      if (this.bindProjectFlag) {
        this.concatPipeDia = true;
      } else
        this.$q.notify({
          type: 'negative',
          message: '请先选择应用',
          position: 'center'
        });
    },
    async concatPipe() {
      this.$refs.concatPipe.validate();
      if (!this.$refs.concatPipe.hasError) {
        let concatPipe = await queryPipelineDetailById({
          id: this.concatPipeline.value
        });
        this.CIDetail.stages = this.CIDetail.stages.concat(concatPipe.stages);
        this.concatPipeDia = false;
      }
    },
    CancelConcatPipe() {
      this.concatPipeDia = false;
      this.concatPipeline = null;
    },

    nameBlur() {
      this.nameBorderless = true;
      this.nameOutLined = false;
    },
    changeStageName(id, name) {
      if (name) {
        this.CIDetail.stages[id].name = name;
      } else return;
    },
    cancleManage() {
      this.$route.query.fromDetail
        ? this.$router.replace(`/configCI/pipelineDetail/${this.id}`)
        : this.$router.back();
    },
    cancleTempManage() {
      this.$router.back();
    },
    async savePipeline() {
      if (!this.pipelineCanSaveFlag) {
        this.$q.notify({
          message: this.cantSaveWords,
          color: 'negative',
          position: 'top',
          icon: 'cancel',
          classes: 'notify-inline'
        });
        return;
      } else {
        return this.$route.query.type &&
          this.$route.query.type === 'editPipeline'
          ? await resolveResponseError(() =>
              pipelineUpdate({ ...this.pipelineCanSave })
            )
          : await resolveResponseError(() =>
              pipelineAdd({ ...this.pipelineCanSave })
            );
      }
    },
    afterSavePipeline() {
      LocalStorage.set('scriptData', '');
      let warningArray = [];
      this.pipelineCanSave.stages.forEach(stage => {
        stage.jobs.forEach(job => {
          warningArray.push(job.warning);
        });
      });
      this.requiredWarning = warningArray.some(warn => {
        return warn === true;
      });
      if (this.requiredWarning) {
        this.$q
          .dialog({
            title: '提醒',
            message:
              '流水线运行所需参数尚未填写完整，保存后将不能正常运行，是否确认保存？',
            cancel: true,
            persistent: true
          })
          .onOk(() => {
            ResPrompt(this.savePipeline, null, id => {
              this.saveTip(id);
            });
          });
      } else {
        ResPrompt(this.savePipeline, null, id => {
          this.saveTip(id);
        });
      }
    },
    saveTip(id) {
      this.$q
        .dialog({
          title: '保存成功',
          message: '流水线保存成功，您还可以在触发规则下设置触发规则！',
          ok: '设置触发规则',
          cancel: '去详情'
        })
        .onOk(() => {
          this.$router.replace({
            path: `/configCI/triggerRule/${id}`
          });
        })
        .onCancel(() => {
          this.$router.replace({
            path: `/configCI/pipelineDetail/${id}`
          });
        });
    },
    async afterSaveTempPipeline() {
      // 模板新增
      if (this.isFromAddTemp) {
        this.typeDialog = true;
      } else {
        // 模板修改
        await this.pipelineTemplateUpdate({
          ...this.pipelineDetail,
          objectId: {}
        });
        successNotify('保存成功！');
        this.$router.replace({ path: '/configCI/toolbox' });
      }
    },
    async addPipeTemp() {
      if (this.fixedModeFlag === 'true') {
        this.pipelineDetail.fixedModeFlag = true;
      } else {
        this.pipelineDetail.fixedModeFlag = false;
      }
      await this.pipelineTemplateAdd({
        ...this.pipelineDetail,
        objectId: {}
      });
      successNotify('保存成功！');
      this.typeDialog = false;
      this.$router.replace({ path: '/configCI/toolbox' });
    },
    async runPipeline() {
      LocalStorage.set('scriptData', '');
      await ResPrompt(this.savePipeline, null, rep => {
        this.pipelineId = rep;
        this.selectBranchDia = true;
      });
    },
    //打开添加step弹框
    openStepDialog(id, funcType) {
      this.nowManage = { id, funcType };
      this.selectJobModalOpened = true;
    },
    //添加stage
    addStage(id, data) {
      let { name } = data;
      id = Number(id.split('-')[1]);
      data.steps = data.steps.map(item => {
        if (item.pluginInfo) {
          return item;
        }
        return {
          ...item,
          pluginInfo: {
            name: item.name,
            pluginName: item.pluginName,
            pluginCode: item.pluginCode
          }
        };
      });
      this.CIDetail.stages.splice(id, 0, {
        name: name,
        jobs: [
          {
            name: name,
            ...data
          }
        ]
      });
    },
    //添加任务已填数据
    addJob(id, data) {
      let oneJob = data.steps[0];
      let { name } = oneJob;
      id = Number(id.split('-')[1]);
      this.CIDetail.stages[id].jobs.push({
        name: name,
        image: this.defaultImg,
        steps: [oneJob],
        runnerClusterId: data.runnerClusterId
      });
      let jobIndex = this.CIDetail.stages[id].jobs.length - 1;
      if (data.steps.length > 1) {
        data.steps.map((item, index) => {
          if (index !== 0) {
            this.CIDetail.stages[id].jobs[jobIndex].steps.push(item);
          }
        });
      }
    },
    //点击流水变步骤头，打开编辑任务
    openJobEditDia(id) {
      let [sInd, jInd] = id.split('-');
      this.jobViewData = this.pipelineDetail.stages[sInd].jobs[jInd];
      this.nowManage = { id: id };
      this.jobViewDia = true;
    },
    //修改步骤
    editJob(data) {
      let [sInd, jInd] = this.nowManage.id.split('-');
      if (sInd === 'add') {
        this.addStage(this.nowManage.id, data);
      } else if (sInd === 'addJob') {
        this.addJob(this.nowManage.id, data);
      } else {
        this.CIDetail.stages[sInd].jobs.splice(jInd, 1, data);
      }
    },
    //删除任务
    deleteJob(id) {
      let [sInd, jInd] = id.split('-');
      this.CIDetail.stages[sInd].jobs.length === 1
        ? this.CIDetail.stages.splice(sInd, 1)
        : this.CIDetail.stages[sInd].jobs.splice(jInd, 1);
    },
    //添加步骤
    addStep(id, data) {
      let [sInd, jInd, pInd] = id.split('-').map(x => Number(x));
      pInd === undefined ? (pInd = -1) : null;
      this.CIDetail.stages[sInd].jobs[jInd].steps.splice(pInd + 1, 0, data);
    },
    //open edit step dialog
    openStepEditDia(id) {
      let [sInd, jInd, pInd] = id.split('-');
      // this.stepViewData是stepDetail组件中传入的stepInfo值
      this.stepViewData = this.pipelineDetail.stages[sInd].jobs[jInd].steps[
        pInd
      ];
      this.nowManage = { id: id };
      this.stepViewDia = true;
    },
    //修改步骤
    editStep(data) {
      let [sInd, jInd, pInd] = this.nowManage.id.split('-');
      this.CIDetail.stages[sInd].jobs[jInd].steps.splice(pInd, 1, data);
    },
    //删除步骤
    deleteStep(id) {
      let [sInd, jInd, pInd] = id.split('-').map(x => Number(x));
      this.CIDetail.stages[sInd].jobs[jInd].steps.length > 1
        ? this.CIDetail.stages[sInd].jobs[jInd].steps.splice(pInd, 1)
        : this.deleteJob(sInd + '-' + jInd);
    }
  },
  async mounted() {
    let imgs = (await queryImageList()) || [];
    this.defaultImg = imgs[0];
    // 从流水线详情页进
    if (this.$route.query.type && this.$route.query.type === 'editPipeline') {
      this.CIDetail = await queryPipelineDetailById({ id: this.id });
    } else {
      // 从流水线模板相关入口进
      let response = await queryPipelineTempDetailById({ id: this.id });
      let { appId } = this.$route.query;
      let { categoryId, categoryName } = this.$route.query;
      this.pipelineTemplateNameId = response.nameId;
      this.pipelineTemplateId = response.id;
      response.category.categoryId = categoryId;
      response.category.categoryName = categoryName;
      if (appId) {
        let appInfo = await queryAbandonDetail({ id: appId });
        let { name_en, name_zh, gitlab_project_id } = appInfo;
        response.bindProject = {
          nameEn: name_en,
          nameCn: name_zh,
          projectId: appId,
          gitlabProjectId: gitlab_project_id
        };
        this.cantSelectApp = true;
      } else {
        response.bindProject = {
          nameEn: '请选择应用'
        };
      }
      this.CIDetail = response;
    }
  }
};
</script>
<style scoped lang="stylus">
.page-title-style
  position relative
.page-title-style::before
  content ' '
  background-color #1565C0
  width 4px
  height 16px
  top 10px
  position absolute
  border-radius 2px
  // margin-right:2px
  // min-width: 10px;
  // max-width: 10px;
  // margin-right: 2px;
/deep/.page-title-style .label
  min-width 0
.select-app
  position relative
  z-index 1
  top 2px
  margin-top 0px
/deep/ .q-input .q-field__marginal
  background-color: none;
/deep/ .q-field__marginal
  background: none
</style>
