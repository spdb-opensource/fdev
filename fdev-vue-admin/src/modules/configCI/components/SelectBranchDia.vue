<template>
  <f-dialog :value="value" @input="$emit('input', false)" title="运行流水">
    <div class="bg-white q-pa-md column no-wrap">
      <div class="text-subtitle2">运行于</div>
      <div class="row cfg-item items-center justify-start no-wrap q-mt-sm">
        <div class="select-style q-mr-lg">
          <fdev-select
            :options="branchOpts"
            :value="branchType"
            @input="changeBranchType"
          />
          <fdev-tooltip position="top">
            选择分支/标签
          </fdev-tooltip>
        </div>
        <div class="select-branch">
          <fdev-select
            :disable="selectDisable"
            :options="branchList"
            v-model="branch"
            use-input
            @filter="branchFilter"
          />
        </div>
      </div>
      <div class="text-subtitle2 q-mt-lg">运行变量</div>
      <div
        class="row cfg-item items-center justify-start no-wrap q-mt-md"
        v-for="(item, index) in runVariables"
        :key="index"
      >
        <div class="input-style q-mr-sm">
          <fdev-tooltip position="top">
            输入变量名
          </fdev-tooltip>
          <fdev-input v-model="item.key" clearable />
        </div>
        =
        <div class="input-style q-ml-sm q-mr-md">
          <fdev-tooltip position="top">
            输入变量值
          </fdev-tooltip>
          <fdev-input v-model="item.value" clearable />
        </div>
        <fdev-btn
          ficon="substract_r_o"
          flat
          :disabled="runVariables.length === 1"
          @click="del(index)"
          class="q-mr-sm"
        />
        <fdev-btn ficon="add_c_o" flat @click="add(index)" />
      </div>
      <div class="q-mt-sm text-grey">
        指定要在此次运行中添加的变量值，默认情况下将使用插件的输入参数
      </div>
      <div class="text-subtitle2 q-mt-md">跳过步骤</div>
      <div class="skip-style scroll-thin">
        <div
          class="row cfg-item items-center justify-start no-wrap q-mt-md"
          v-for="(item, index) in exeSkippedSteps"
          :key="item.id"
        >
          <fdev-select
            style="width:175px"
            class="q-mr-sm"
            v-model="item.stageIndex"
            :options="stageOptions"
            option-label="label"
            option-value="value"
            map-options
            emit-value
            clearable
            placeholder="选择stage"
            @input="stageInput(index, item.stageIndex)"
          />
          <fdev-select
            style="width:175px"
            class="q-mr-sm"
            v-model="item.jobIndex"
            :options="item.jobOptions"
            option-label="label"
            option-value="value"
            map-options
            emit-value
            clearable
            placeholder="选择job"
            :disabled="item.stageIndex === ''"
            :readonly="item.stageIndex === ''"
            @input="jobInput(index, item.jobIndex)"
          >
            <fdev-tooltip position="top" v-if="!item.stageIndex">
              请先选择stage
            </fdev-tooltip>
          </fdev-select>
          <fdev-select
            style="width:175px"
            class="q-mr-md"
            v-model="item.stepIndex"
            :options="item.stepOptions"
            option-label="label"
            option-value="value"
            map-options
            emit-value
            clearable
            placeholder="选择step"
            :disabled="item.jobIndex === ''"
            :readonly="item.jobIndex === ''"
          >
            <fdev-tooltip position="top" v-if="!item.jobIndex">
              请先选择job
            </fdev-tooltip>
          </fdev-select>
          <fdev-btn
            ficon="substract_r_o"
            flat
            :disabled="exeSkippedSteps.length === 1"
            @click="delExeSkip(index)"
          />
          <fdev-btn
            ficon="add_c_o"
            flat
            class="q-ml-sm"
            @click="addExeSkip(index)"
          />
        </div>
        <div class="q-mt-sm text-grey">
          跳过指定步骤不执行
        </div>
      </div>
    </div>

    <template v-slot:btnSlot
      ><fdev-btn label="取消" dialog outline @click="$emit('input', false)"/>
      <fdev-btn
        label="确定"
        :disabled="disabled"
        dialog
        @click="sureOnClick"
        :loading="btnLoading"
    /></template>
  </f-dialog>
</template>

<script>
import { queryBranchesByPipelineId, triggerPipeline } from '../services/method';

export default {
  name: 'SelectBranchDia',
  props: {
    value: Boolean,
    id: String,
    pipeData: Object,
    noReplace: Boolean
  },
  data() {
    return {
      selectDisable: false,
      branchOpts: [
        { label: '分支', value: 'branches' },
        { label: '标签', value: 'tags' }
      ],
      branchList: [],
      branchType: { label: '分支', value: 'branches' },
      footBtns: {
        sure: this.sureOnClick,
        cancel: this.closeDialog
      },
      allBranchs: null,
      branch: null,
      runVariables: [{ key: '', value: '' }],
      btnLoading: false,
      exeSkippedSteps: [{ stageIndex: '', jobIndex: '', stepIndex: '' }],
      stageOptions: [],
      selectJobs: []
    };
  },
  computed: {
    disabled() {
      return !(this.branches && this.branches.length > 0);
    },
    branches() {
      return this.allBranchs ? this.allBranchs[this.branchType.value] : null;
    },
    trggerParams() {
      return {
        tagFlag: this.branchType.value === 'tags',
        pipelineId: this.id,
        branch: this.branch,
        runVariables: this.runVariables,
        exeSkippedSteps: this.exeSkippedSteps
      };
    }
  },
  methods: {
    changeBranchType(evt) {
      this.branchType = evt;
      this.hasBranches();
    },
    hasBranches() {
      if (this.branches && this.branches.length > 0) {
        this.selectDisable = false;
        this.branch = this.branches[0];
      } else {
        this.branch = '该流水线无可选择' + this.branchType.label;
        this.selectDisable = true;
      }
    },
    del(index) {
      this.runVariables.splice(index, 1);
    },
    add(index) {
      this.runVariables.push({
        key: '',
        value: ''
      });
    },
    delExeSkip(index) {
      this.exeSkippedSteps.splice(index, 1);
    },
    addExeSkip(index) {
      this.exeSkippedSteps.push({
        stageIndex: '',
        jobIndex: '',
        stepIndex: ''
      });
    },
    // 选择stage
    stageInput(index, data) {
      // 先清空该条数组的job和step值
      (this.exeSkippedSteps[index].jobIndex = ''),
        (this.exeSkippedSteps[index].stepIndex = '');
      this.exeSkippedSteps[index].jobOptions = [];
      // 再根据所选的data,给jobOptions赋值
      let jobObj = {};
      this.selectJobs = this.pipeData.stages[data].jobs;
      this.pipeData.stages[data].jobs.forEach((job, indexJob) => {
        jobObj = {
          label: indexJob + ': ' + job.name,
          value: indexJob
        };
        this.exeSkippedSteps[index].jobOptions.push(jobObj);
      });
      this.$forceUpdate();
    },
    // 选择job
    jobInput(index, data) {
      // 先清空该条数组的step值
      this.exeSkippedSteps[index].stepIndex = '';
      this.exeSkippedSteps[index].stepOptions = [];
      // 再根据所选的data,给stepOptions赋值
      let stepObj = {};
      this.selectJobs[data].steps.forEach((step, indexStep) => {
        stepObj = {
          label: indexStep + ': ' + step.name,
          value: indexStep
        };
        this.exeSkippedSteps[index].stepOptions.push(stepObj);
      });
      this.$forceUpdate();
    },
    async sureOnClick() {
      this.btnLoading = true;
      try {
        let id = await triggerPipeline({ ...this.trggerParams });
        this.noReplace
          ? this.$router.push({
              name: 'logPanorama',
              params: { id: id },
              query: { from: 'pipeDetail' }
            })
          : this.$router.replace({
              name: 'logPanorama',
              params: { id: id },
              query: { from: 'pipeDetail' }
            });
      } finally {
        this.btnLoading = false;
      }
    },
    closeDialog() {
      this.$emit('input', false);
    },
    getStageOptions() {
      this.pipeData.stages.forEach((stag, stagIndex) => {
        let stagObj = {
          label: stagIndex + ': ' + stag.name,
          value: stagIndex
        };
        this.stageOptions.push(stagObj);
      });
    },
    branchFilter(val, update, abort) {
      update(() => {
        this.branchList = this.branches.filter(
          branch => branch.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    }
  },
  async mounted() {
    this.getStageOptions();
    this.allBranchs = await queryBranchesByPipelineId({
      pipelineId: this.id
    });
    this.hasBranches();
  }
};
</script>

<style lang="stylus" scoped>
.select-style
  width 260px
.select-branch
  width 360px
/deep/.q-item
  min-height 0
/deep/.q-item__label
  font-size 14px
  color #78909C
  line-height 20px
/deep/.q-item__section--side
  padding-right 0px
/deep/.q-item__section
  height 20px
.input-style
  width 260px
.skip-style
  max-height 300px
  margin-bottom 50px
</style>
