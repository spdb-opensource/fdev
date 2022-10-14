<template>
  <f-block class="column no-wrap items-center">
    <div>
      <div class="row">
        <h4 class="col-9 q-mb-sm q-ml-xl push-style ">触发规则设置</h4>
      </div>
      <div class="trigger-style tip-title">
        任一开关打开，并且设置了规则才生效，两个都关闭则不会自动触发，只能手动触发。
        <div class="q-mt-sm">
          <span class="font-style">push事件触发：</span>
          满足指定规则的分支/标签有push事件发生时，自动触发该流水线。
        </div>
        <div class="q-mt-sm">
          <span class="font-style">定时触发：</span>
          根据cron表达式，使用指定分支定时触发该流水线。
        </div>
      </div>
    </div>
    <div class="bg-blue-1 items-center justify-between row trigger-style">
      <span class="q-ml-md push-style text-blue-grey-9">PUSH事件触发</span>
      <div>
        <span class="text-grey-8">
          {{ pushTrigger === true ? '' : '关' }}
        </span>
        <fdev-toggle
          v-model="pushTrigger"
          :disable="unableEdited || isHistoryPipeDatail"
        />
        <span class="text-grey-8 q-mr-lg">
          {{ pushTrigger === true ? '开' : '' }}
        </span>
      </div>
    </div>
    <div class="scroll-thin" v-show="pushTrigger">
      <div
        v-for="(param, i) in pushParams"
        :key="i"
        class="row cfg-item q-mb-md items-center justify-start no-wrap"
      >
        <div class="select-style q-ml-100">
          <fdev-select
            :options="branchOpts"
            v-model="param.branchType"
            :disable="unableEdited || isHistoryPipeDatail"
          />
          <fdev-tooltip position="top">
            选择分支/标签，输入分支/标签的全名，或者正则表达式, 例如^SIT.*$
          </fdev-tooltip>
        </div>
        <div class="q-ml-md">
          <fdev-input
            style="width:260px"
            placeholder="请键入分支名或正则表达式"
            v-model="param.branchName"
            :disable="unableEdited || isHistoryPipeDatail"
          />
        </div>
        <!-- <div v-if="!unableEdited" class="inline-block">
            <f-image
               v-if="i === 0"
              @click="addParam"
              class="inline-block cursor-pointer q-ml-sm"
            >
              <img
                src="../assets/add.png"
                @click="addParam"
                style="width:20px"
              />
            </f-image>
            <f-image
              v-if="pushParams.length > 1"
              class="inline-block cursor-pointer q-ml-sm"
              @click="removeParam(i)"
            >
              <img
                src="../assets/delete_blue.png"
                @click="removeParam(i)"
                style="width:20px"
              />
            </f-image>
            <f-image v-if="!confirParam(param)" class="inline-block q-ml-sm">
              <img src="../assets/alert.png" style="width:20px" />
            </f-image>
          </div> -->
        <div v-if="!unableEdited && !isHistoryPipeDatail" class="inline-block">
          <fdev-icon
            name="add_circle_outline"
            v-if="i === 0"
            @click="addParam"
            class="text-blue-9 icon-btn cursor-pointer q-ml-sm"
          />
          <fdev-icon
            name="remove_circle_outline"
            v-if="pushParams.length > 1"
            class="text-blue-9 icon-btn cursor-pointer q-ml-sm"
            @click="removeParam(i)"
          />
          <fdev-icon
            name="error_outline"
            v-if="!confirParam(param)"
            class="text-red-5 icon-btn q-ml-sm"
          />
        </div>
      </div>
    </div>
    <!-- <div class="placeholder" v-show="!pushTrigger" /> -->

    <div class="bg-blue-1 items-center justify-between row trigger-style">
      <span class="q-ml-md push-style text-blue-grey-9">定时触发</span>
      <div>
        <span class="text-grey-8">
          {{ scheduleTrigger === true ? '' : '关' }}
        </span>
        <fdev-toggle
          v-model="scheduleTrigger"
          :disable="unableEdited || isHistoryPipeDatail"
        />
        <span class="text-grey-8 q-mr-lg">
          {{ scheduleTrigger === true ? '开' : '' }}
        </span>
      </div>
    </div>

    <div class="scroll-thin" v-show="scheduleTrigger">
      <div
        v-for="(param, i) in scheduleParams"
        :key="i"
        class="cfg-item q-mb-md items-center justify-start no-wrap"
      >
        <div class="row">
          <div class="select-style q-ml-100">
            <fdev-select
              :options="branchOpts"
              v-model="param.branchType"
              :disable="unableEdited || isHistoryPipeDatail"
            />
            <fdev-tooltip position="top">
              选择分支/标签
            </fdev-tooltip>
          </div>
          <div class="q-ml-md">
            <fdev-select
              style="width:260px"
              :options="branchFn(param.branchType)"
              v-model="param.branchName"
              :disable="unableEdited || isHistoryPipeDatail"
            />
          </div>
          <!-- <div v-if="!unableEdited" style="margin-top: 5px">
              <f-image
                v-if="i === 0"
                @click="addScheduleParam"
                class="inline-block cursor-pointer q-ml-sm"
              >
                <img
                  src="../assets/add.png"
                  @click="addScheduleParam"
                  style="width:20px"
                />
              </f-image>
              <f-image
                v-if="scheduleParams.length > 1"
                class="inline-block cursor-pointer q-ml-sm"
                @click="removeScheduleParam(i)"
              >
                <img
                  src="../assets/delete_blue.png"
                  @click="removeScheduleParam(i)"
                  style="width:20px"
                />
              </f-image>
              <f-image
                v-if="!confirTimeParam(param)"
                class="inline-block q-ml-sm"
              >
                <img src="../assets/alert.png" style="width:20px" />
              </f-image>
            </div> -->
          <div
            v-if="!unableEdited && !isHistoryPipeDatail"
            class="inline-block"
          >
            <fdev-icon
              name="add_circle_outline"
              v-if="i === 0"
              @click="addScheduleParam"
              class="text-blue-9 icon-btn cursor-pointer q-ml-sm"
            />
            <fdev-icon
              name="remove_circle_outline"
              v-if="scheduleParams.length > 1"
              class="text-blue-9 icon-btn cursor-pointer q-ml-sm"
              @click="removeScheduleParam(i)"
            />
            <fdev-icon
              name="error_outline"
              v-if="!confirTimeParam(param)"
              class="text-red-5 icon-btn q-ml-sm"
            />
          </div>
        </div>
        <div class="row q-mt-md" style="margin-left:80px">
          <div class="cron-div">cron表达式</div>
          <div class="q-ml-135">
            <fdev-input
              style="width:260px"
              placeholder="请键入cron表达式"
              v-model="param.cron"
              :disable="unableEdited || isHistoryPipeDatail"
            />
            <fdev-tooltip position="right">
              cron表达式由至少6位数字组成（每个数字称作元素），<br />
              每个时间元素用空格分隔 <br />
              时间元素按顺序依次为<br />
              秒（0~59）<br />
              分钟（0~59）<br />
              小时（0~23）<br />
              天（0~31）<br />
              月（0~11）<br />
              星期（1~7 1=SUN 或SUN,MON,TUE,WED,THU,FRI,SAT)<br />
              例子：/5*****,每隔5秒执行一次
            </fdev-tooltip>
          </div>
          <div style="margin-top:5px">
            <!-- <f-image v-if="!cronValidateParam(param.cron)" class="q-ml-sm">
                <img
                  src="../assets/alert.png"
                  v-if="!cronValidateParam(param.cron)"
                  style="width:20px;height:20px"
                />
              </f-image> -->
            <fdev-icon
              style="float: right"
              name="error_outline"
              v-if="!cronValidateParam(param.cron)"
              class="text-red-5 icon-btn q-ml-sm"
            />
          </div>
        </div>
      </div>
    </div>
    <div
      class="self-center row q-mt-md"
      v-if="!unableEdited && !isHistoryPipeDatail"
    >
      <fdev-btn
        label="确定"
        class="q-mr-lg"
        @click="sureOnClick"
        :disabled="!canSubmit"
      />
      <fdev-btn
        label="取消"
        outline
        @click="cancleTrigger"
        :disabled="!canSubmit"
      />
    </div>
  </f-block>
  <!-- 
      暂不删除
      <div v-if="cornOpened" class="tip-div">
      <div class="card-header bg-blue text-subtitle2">
        cron表达式
      </div>
      <div class="dialog-wrapper bg-white">
        cron表达式由至少6位数字组成（每个数字称作元素），<br />
        每个时间元素用空格分隔 <br />
        时间元素按顺序依次为<br />
        秒（0~59）<br />
        分钟（0~59）<br />
        小时（0~23）<br />
        天（0~31）<br />
        月（0~11）<br />
        星期（1~7 1=SUN 或SUN,MON,TUE,WED,THU,FRI,SAT)<br />
        例子：/5*****,每隔5秒执行一次
      </div>
    </div> -->
</template>

<script>
const DEFAULT_PUSH_PARAM = { branchType: '分支', branchName: null };
const DEFAULT_SCHEDULE_PARAM = {
  branchType: '分支',
  branchName: null,
  cron: ''
};
import { THUMB_STYLE } from '../utils/constants';
import { mapActions, mapState } from 'vuex';
import '../utils/cron.js';
import { successNotify } from '@/utils/utils';
import { queryBranchesByPipelineId } from '../services/method';

export default {
  name: 'TriggerCfg',
  data() {
    return {
      ...THUMB_STYLE,
      triggerCfg: {},
      triggerSchedule: {},
      pushTrigger: false,
      scheduleTrigger: false,
      branchOpts: ['分支', '标签'],
      footBtns: {
        sure: this.sureOnClick,
        cancel: this.cancleTrigger
      },
      pushParams: [],
      scheduleParams: [],
      cornOpened: false,
      pipelineId: '',
      unableEdited: false,
      allBranchsOrTags: []
    };
  },
  computed: {
    ...mapState('configCIForm', {
      triggerRules: 'triggerRules',
      pipelineDetail: 'pipelineDetail'
    }),
    // 共流水线详情和流水线历史详情页用到触发规则，历史详情不可修改
    isHistoryPipeDatail() {
      return this.$route.name === 'HistoryPanorama';
    },
    canSubmit() {
      return (
        (!this.pushTrigger || this.pushParams.every(this.confirParam)) &&
        (!this.scheduleTrigger ||
          (this.scheduleParams.every(this.confirTimeParam) &&
            this.scheduleParamsVali &&
            this.scheduleTrigger))
      );
    },
    scheduleParamsVali() {
      if (!this.scheduleTrigger) {
        return true;
      }
      return this.scheduleParams.every(param => {
        let options = {
          seconds: true,
          alias: true,
          allowBlankDay: true,
          allowSevenAsSunday: true
        };
        return window.isValidCron(param.cron, options);
      });
    },
    switchFlag() {
      return this.triggerCfg.switchFlag;
    },
    timeSwitchFlag() {
      return this.triggerSchedule.switchFlag;
    }
  },
  watch: {
    switchFlag: {
      handler(newVal) {
        this.pushTrigger = [true, false].includes(newVal) ? newVal : false;
        this.pushParams = this.initPushParams();
      },
      deep: true
    }
  },
  methods: {
    ...mapActions('configCIForm', {
      queryTriggerRules: 'queryTriggerRules',
      updateTriggerRules: 'updateTriggerRules',
      queryPipelineDetailById: 'queryPipelineDetailById'
    }),
    branchFn(type, val) {
      return type === '分支'
        ? this.allBranchsOrTags.branches
        : this.allBranchsOrTags.tags;
    },
    branchFilter(val, update, abort) {
      update(() => {
        this.users = this.userOptions.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    cronValidateParam(param) {
      let options = {
        seconds: true,
        alias: true,
        allowBlankDay: true,
        allowSevenAsSunday: true
      };
      return window.isValidCron(param, options);
    },
    initPushParams() {
      return this.triggerCfg.pushParams && this.triggerCfg.pushParams.length > 0
        ? this.triggerCfg.pushParams
        : [JSON.parse(JSON.stringify(DEFAULT_PUSH_PARAM))];
    },
    initScheduleParams() {
      return this.triggerSchedule.scheduleParams &&
        this.triggerSchedule.scheduleParams.length > 0
        ? this.triggerSchedule.scheduleParams
        : [JSON.parse(JSON.stringify(DEFAULT_SCHEDULE_PARAM))];
    },
    confirParam(param) {
      return this.branchOpts.includes(param.branchType) && param.branchName;
    },
    confirTimeParam(param) {
      return this.branchOpts.includes(param.branchType) && param.branchName;
    },
    removeParam(id) {
      this.pushParams.splice(id, 1);
    },
    removeScheduleParam(id) {
      this.scheduleParams.splice(id, 1);
    },
    addParam() {
      this.pushParams.push(JSON.parse(JSON.stringify(DEFAULT_PUSH_PARAM)));
    },
    addScheduleParam() {
      this.scheduleParams.push(
        JSON.parse(JSON.stringify(DEFAULT_SCHEDULE_PARAM))
      );
    },
    mouseOver() {
      this.cornOpened = true;
    },
    mouseLeave() {
      this.cornOpened = false;
    },
    async sureOnClick() {
      let params = {
        pipelineId: this.pipelineId,
        triggerRules: {
          push: {
            switchFlag: this.pushTrigger,
            pushParams: this.pushParams
          },
          schedule: {
            switchFlag: this.scheduleTrigger,
            scheduleParams: this.scheduleParams
          }
        }
      };
      await this.updateTriggerRules(params);
      successNotify('修改流水线触发规则成功!');
      // this.$emit('getTriggerCfg');
      this.$router.replace(`/configCI/pipelineDetail/${this.pipelineId}`);
    },
    cancleTrigger() {
      this.$router.replace(`/configCI/pipelineDetail/${this.pipelineId}`);
    }
  },
  async mounted() {
    this.pipelineId = this.$route.params.id;
    await this.queryPipelineDetailById({ id: this.pipelineId });
    this.unableEdited = !this.pipelineDetail.updateRight;
    await this.queryTriggerRules({ pipelineId: this.pipelineId });
    this.triggerCfg = this.triggerRules.triggerRules.push;
    this.triggerSchedule = this.triggerRules.triggerRules.schedule;
    this.pushTrigger = this.triggerCfg.switchFlag;
    this.scheduleTrigger = this.triggerSchedule.switchFlag;
    this.pushParams = this.initPushParams();
    this.scheduleParams = this.initScheduleParams();
    // 没有触发开关权限，则不发queryBranchesByPipelineId接口
    if (!this.unableEdited) {
      this.allBranchsOrTags = await queryBranchesByPipelineId({
        pipelineId: this.pipelineId
      });
    }
  }
};
</script>

<style lang="stylus" scoped>

.trigger-style
  width 620px
  height 60px
  margin 48px 102px 34px 97px
.placeholder
  height 136px
.cfgs-style
  height 120px
  width 740px
  margin-left 102px
.schedule-area
  height 250px
.footbtn
  margin-top 40px
  width 230px
.select-style
  width 216px
  height 40px
.title-style
  font-size 24px
  line-height 32px
.push-style
  font-size 16px
  line-height 32px
.dialog-wrapper
 padding 20px
.card-header
 color white
 height 50px
 line-height 40px
 padding 10px
.tip-div
 position absolute
 top 38%
 left 10%
.tip-title
  height: auto;
  margin-top: 16px;
  margin-bottom: 16px;
.cron-div
 margin-top 4px
 margin-left 53px
.title
  color red
.font-style
 color #1976d2
 font-weight 600
 margin-left 0
.q-ml-100
  margin-left 100px
.q-ml-135
  margin-left 130px
</style>
