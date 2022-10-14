<template>
  <f-block class="row no-wrap">
    <div class="column text-describe q-mr-xl">
      <span class="q-mb-md text-subtitle3">触发规则设置</span>
      <span class="q-mb-lg text-body2"
        >任一开关打开，并且设置了规则才生效，两个都关闭则不会自动触发，只能手动触发。</span
      >
      <span class="q-mb-lg text-body2"
        ><span class="text-primary text-subtitle2">Push事件触发：</span
        >满足指定规则的分支/标签有push事件发生时，自动触发该流水线。</span
      ><span class="text-body2"
        ><span class="text-primary text-subtitle2">定时触发：</span
        >根据cron表达式，使用指定分支定时触发该流水线。</span
      >
      <div class="row items-center q-mt-xs">
        <f-icon name="information" class="text-primary q-mr-xs" /><span
          class="text-subtitle2"
          >cron表达式填写规则：</span
        >
      </div>
      <span class="text-body2">
        cron表达式由至少6位数字组成（每个数字称作时间元素），每个时间元素用空格分隔。
        时间元素按顺序依次为：<br />
        秒（0~59）<br />
        分钟（0~59）<br />
        小时（0~23）<br />
        天（0~31）<br />
        月（0~11）<br />
        星期（1~7， 1=周日)<br />
      </span>
    </div>
    <div class="column col justify-between">
      <div>
        <div class="rule-block q-mb-lg row no-wrap">
          <div class="column items-start q-pa-md" style="width:175px">
            <div class="row justify-between full-width q-mb-sm">
              <span class="text-subtitle2">Push事件触发</span
              ><fdev-toggle @input="changeFlag('push')" v-model="pushFlag" />
            </div>
            <fdev-btn
              :disable="!pushFlag"
              label="添加"
              ficon="add_c_o"
              flat
              @click="pushAdd"
            />
          </div>
          <div v-if="pushFlag" class="col left-border column">
            <div
              class="q-px-md row no-wrap q-pt-lg q-pb-xs items-start"
              :class="{ 'top-border': i !== 0 }"
              v-for="(push, i) in pushList"
              :key="i"
            >
              <fdev-btn-toggle
                :btnWidth="60"
                v-model="push.branchType"
                :options="[
                  { label: '分支', value: 'branch' },
                  { label: '标签', value: 'tag' }
                ]"
              /><fdev-input
                :ref="`push${i}`"
                placeholder="请输入名称或者正则表达式，如^SIT.*$"
                :rules="[val => !!val || '请输入名称或者正则表达式']"
                class="q-mx-sm col"
                v-model="push.branchName"
              /><fdev-btn
                label="删除"
                ficon="substract_r_o"
                @click="deleteRule(i, 'push')"
                flat
              />
            </div>
          </div>
        </div>
        <div class="rule-block row no-wrap">
          <div class="column items-start q-pa-md" style="width:175px">
            <div class="row justify-between full-width q-mb-sm">
              <span class="text-subtitle2">定时触发</span
              ><fdev-toggle
                @input="changeFlag('schedule')"
                v-model="scheduleFlag"
              />
            </div>
            <fdev-btn
              label="添加"
              :disable="!scheduleFlag"
              ficon="add_c_o"
              flat
              @click="scheduleAdd"
            />
          </div>
          <div v-if="scheduleFlag" class="col left-border column">
            <div
              class="column"
              :class="{ 'top-border': i !== 0 }"
              v-for="(schedule, i) in scheduleList"
              :key="i"
            >
              <div class="overflow-auto">
                <div
                  class="bg-indigogrey-0 q-pa-md q-gutter-x-lg row items-center"
                >
                  <span class="text-subtitle2">触发频率</span
                  ><fdev-radio
                    label="每天(20:30:45)"
                    @input="schedule.cron = '45 30 20 * * *'"
                    v-model="schedule.cronRule"
                    val="day"
                  /><fdev-radio
                    :label="`每周${weekDay.ch}(7:45:30)`"
                    @input="schedule.cron = `30 45 7 * * ${weekDay.num}`"
                    v-model="schedule.cronRule"
                    val="week"
                  /><fdev-radio
                    :label="`每月${monthDay}日(21:15:00)`"
                    @input="schedule.cron = `0 15 21 * ${monthDay} *`"
                    v-model="schedule.cronRule"
                    val="month"
                  />
                  <fdev-radio
                    label="自定义"
                    v-model="schedule.cronRule"
                    val="custom"
                    class="q-mr-sm"
                    @input="$refs[`schedule_cron${i}`][0].focus()"
                  />
                </div>
              </div>
              <div class="row no-wrap items-start q-pt-lg q-pb-xs q-px-md">
                <div class="column">
                  <span class="text-subtitle2" style="line-height:36px"
                    >对应cron表达式</span
                  >
                  <fdev-btn-toggle
                    :btnWidth="60"
                    style="margin-top:20px"
                    v-model="schedule.branchType"
                    :options="[
                      { label: '分支', value: 'branch' },
                      { label: '标签', value: 'tag' }
                    ]"
                  />
                </div>
                <div class="col q-mx-sm">
                  <fdev-input
                    :ref="`schedule_cron${i}`"
                    v-model="schedule.cron"
                    placeholder="请输入cron表达式"
                    :rules="[val => !!val || '请输入cron表达式']"
                    @focus="schedule.cronRule = 'custom'"
                  />
                  <fdev-input
                    :ref="`schedule${i}`"
                    :rules="[val => !!val || '请输入名称或者正则表达式']"
                    placeholder="请输入名称或者正则表达式，如^SIT.*$"
                    v-model="schedule.branchName"
                  />
                </div>
                <fdev-btn
                  flat
                  style="margin-top:30px"
                  ficon="substract_r_o"
                  label="删除"
                  @click="deleteRule(i, 'schedule')"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="self-end q-mt-xl">
        <fdev-btn
          class="q-mr-md"
          label="取消"
          @click="$router.back()"
          outline
        /><fdev-btn label="确定" @click="submitTriggerRule" />
      </div>
    </div>
  </f-block>
</template>

<script>
import { resolveResponseError } from '@/utils/utils';
import moment from 'moment';
import { queryTriggerRules, updateTriggerRules } from '../../services/method';
export default {
  name: 'TriggerRule',
  data() {
    return {
      pushFlag: false,
      pushList: [],
      scheduleFlag: false,
      scheduleList: []
    };
  },
  props: { id: String },
  computed: {
    weekDay() {
      let num = moment().weekday();
      let ch;
      switch (num) {
        case 1:
          ch = '一';
          break;
        case 2:
          ch = '二';
          break;
        case 3:
          ch = '三';
          break;
        case 4:
          ch = '四';
          break;
        case 5:
          ch = '五';
          break;
        case 6:
          ch = '六';
          break;
        case 7:
          ch = '日';
          break;
      }
      return { num, ch };
    },
    monthDay() {
      return moment().date();
    }
  },
  async mounted() {
    let triggerRule = await queryTriggerRules({ pipelineId: this.id });
    let { triggerRules } = triggerRule;
    let { push, schedule } = triggerRules;
    this.pushFlag = push.switchFlag;
    this.pushList = push.pushParams || [];
    this.scheduleFlag = schedule.switchFlag;
    this.scheduleList = schedule.scheduleParams || [];
  },
  methods: {
    changeFlag(type) {
      if (this[`${type}Flag`] && this[`${type}List`].length === 0) {
        this[`${type}Add`]();
      }
    },
    pushAdd() {
      this.pushList.push({ branchType: 'branch', branchName: '' });
    },
    scheduleAdd() {
      this.scheduleList.push({
        branchType: 'branch',
        branchName: '',
        cron: '45 30 20 * * *',
        cronRule: 'day'
      });
    },
    deleteRule(i, type) {
      this[`${type}List`].splice(i, 1);
      this[`${type}List`].length === 0 && (this[`${type}Flag`] = false);
    },
    async submitTriggerRule() {
      let pushValidates = this.pushFlag
        ? Array.from(this.pushList.keys()).map(i => this.$refs[`push${i}`][0])
        : [];
      let scheduleValidates = this.scheduleFlag
        ? Array.from(this.scheduleList.keys()).reduce((acc, curr) => {
            return acc.concat([
              this.$refs[`schedule${curr}`][0],
              this.$refs[`schedule_cron${curr}`][0]
            ]);
          }, [])
        : [];
      let validates = pushValidates.concat(scheduleValidates);
      validates.forEach(v => v.validate());
      if (!validates.some(v => v.hasError)) {
        await resolveResponseError(() =>
          updateTriggerRules({
            pipelineId: this.id,
            triggerRules: {
              push: { switchFlag: this.pushFlag, pushParams: this.pushList },
              schedule: {
                switchFlag: this.scheduleFlag,
                scheduleParams: this.scheduleList
              }
            }
          })
        );
        this.$router.back();
      }
    }
  }
};
</script>

<style lang="stylus" scoped>

.text-describe
  width 240px
.rule-block
  border 1px solid #DDDDDD
.left-border
  border-left 1px solid #DDDDDD
.top-border
  border-top 1px solid #DDDDDD
.custom
  width 120px
  background #E8FBF1
</style>
