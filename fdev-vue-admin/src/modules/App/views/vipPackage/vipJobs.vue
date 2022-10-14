<template>
  <Loading :visible="loading">
    <f-block>
      <div class="bg-white">
        <div class="left-right bg-white ">
          <div class="row q-mt-md q-ml-lg">
            <b class="b-name">{{ stages }}</b>
            <fdev-btn
              v-if="showBtn"
              @click="cancelDeploy"
              dialog
              label="取消"
            />
          </div>
          <div class="row q-mt-md q-ml-md">
            <div class="top-mt">
              <fdev-icon
                class="tip"
                :class="pipeline_status"
                :name="icons[pipeline_status]"
              />
              <b class="trigger-time">TriggerTime</b>
              <span class="text-primary">
                {{ rowModel.trigger_time | triggertime }}
              </span>
              for
              <span>
                <a class="text-primary" target="_blank" :href="`${webUrl}`">
                  {{ rowModel.ref }}
                </a>
              </span>
            </div>
          </div>
          <div class="row q-mt-sm q-ml-lg">
            <fdev-select
              v-model="stage"
              :options="stageOptions"
              option-label="label"
              option-value="label"
              map-options
              emit-value
              class="q-mr-sm multiple"
            />
          </div>
          <div class="row q-mt-sm q-ml-md q-mb-lg">
            <div class="top-mt">
              <fdev-icon class="tip" :class="status" :name="icons[status]" />
              {{ changeStage }}
            </div>
          </div>
        </div>
      </div>
      <div class="bg-white jobs">
        <div class="build-trace" v-if="data.trace !== ''">
          <code>
            <div class="ws-pre-div" v-for="(item, index) in trace" :key="index">
              <span class="ws-pre-wrap" :class="changeColor(item.content)">{{
                item.content
              }}</span>
            </div>
          </code>
        </div>
        <div v-else class="no-code">
          <fdev-icon
            class="icon"
            size="430px"
            :name="`img:${icon[data.status]}`"
          />
          <h4 class="desc">This job has been {{ data.status }}</h4>
        </div>
      </div>
    </f-block>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import moment from 'moment';
import { successNotify } from '@/utils/utils';
import { icon, statusOptions } from '@/modules/App/utils/constants';

export default {
  name: 'vipJobs',
  components: {
    Loading
  },
  data() {
    return {
      stage: null,
      statusOptions: statusOptions,
      icons: icon,
      status: '',
      pipeline_status: '',
      data: {},
      timer: null,
      params: {},
      loading: false,
      icon: {
        skipped: require('@/assets/skipped.svg'),
        canceled: require('@/assets/canceled.svg'),
        created: require('@/assets/created.svg')
      },
      changeStage: '',
      stageOptions: [],
      jobId: '',
      stages: '',
      trigger_time: '',
      ref: '',
      webUrl: '',
      rowModel: {},
      content: [],
      trace: []
    };
  },
  watch: {
    async stage(val) {
      if (val) {
        sessionStorage.setItem(
          'stage',
          JSON.stringify({
            stage: val
          })
        );
        this.changeStage = val;
        this.stages = val;
        await this.vipGetLog({ id: this.rowModel.id, stages: val });
        this.refOptions = this.branchList;
        this.data = this.vipGetLogDetail;
        this.status = this.data.status;
        this.pipeline_status = this.data.pipeline_status;
        this.data.trace = this.data.trace;
        this.content = this.data.trace.split(/[\n]/g);
        this.trace = [];
        for (let index in this.content) {
          this.trace.push({ content: this.content[index] });
        }
      }
    }
  },
  computed: {
    ...mapState('appForm', [
      'vipGetLogDetail',
      'cancelVipDeployList',
      'jobsInfo'
    ]),
    showBtn() {
      return (
        (this.rowModel.status == 'pending' ||
          this.rowModel.status == 'running') &&
        (this.pipeline_status == 'pending' ||
          this.pipeline_status == 'running') &&
        (this.stages != 'canceled' ||
          this.stages != 'passed' ||
          this.stages != 'failed' ||
          this.status != 'canceled' ||
          this.status != 'passed' ||
          this.status != 'failed')
      );
    }
  },
  filters: {
    triggertime(val) {
      return moment(parseInt(val)).format('YYYY-MM-DD HH:mm:ss');
    }
  },
  methods: {
    ...mapActions('appForm', {
      queryTraces: 'queryTraces',
      queryRunnerJobLogDetail: 'queryRunnerJobLogDetail',
      cancelVipDeploy: 'cancelVipDeploy',
      vipGetLog: 'vipGetLog',
      getPipelineById: 'getPipelineById'
    }),
    url(web_url) {
      return web_url.substring(0, web_url.indexOf('-/'));
    },
    passed(val) {
      return val === 'success' ? 'passed' : val;
    },
    changeColor(val) {
      if (val) {
        if (val.substring(1, 7).startsWith('[1;32m')) {
          return 'green';
        } else if (val.substring(1, 7).startsWith('[1;31m')) {
          return 'red';
        } else if (val.substring(1, 7).startsWith('[1;33m')) {
          return 'wran';
        } else {
          return 'white';
        }
      }
    },
    async getData() {
      const params = {
        id: this.jobId,
        stages: this.stage
      };
      await this.vipGetLog(params);
      this.data = this.vipGetLogDetail;
      this.status = this.data.status;
      this.pipeline_status = this.data.pipeline_status;
      this.data.trace = this.data.trace;
      this.content = this.data.trace.split(/[\n]/g);
      this.trace = [];
      for (let index in this.content) {
        this.trace.push({ content: this.content[index] });
      }
    },
    polling() {
      if (
        (this.pipeline_status == 'created' ||
          this.pipeline_status == 'pending' ||
          this.pipeline_status == 'running') &&
        (this.status == 'created' ||
          this.status == 'pending' ||
          this.status == 'running')
      ) {
        clearInterval(this.timer);
        this.timer = setInterval(() => this.getData(), 10000);
      }
    },
    async cancelDeploy() {
      await this.cancelVipDeploy({ id: this.rowModel.id });
      this.pipeline_status = this.cancelVipDeployList.status;
      successNotify('取消部署成功!');
    }
  },
  async created() {
    this.loading = true;
    sessionStorage.removeItem('deployInfo');
    if (this.$route.params.row) {
      // 传递的整条数据
      this.rowModel = this.$route.params.row;
      this.jobId = this.$route.params.id;
      //对应点击步骤流水线的状态
      this.stages = this.$route.params.stages;
      // 总的状态(图标)
      this.pipeline_status = this.$route.params.row.status;
      //下拉列表的默认值
      this.stage = this.$route.params.stages;
      //下拉列表下面一行的小状态（字）
      this.changeStage = this.$route.params.stages;
      //下拉列表下面一行的小状态（图标）
      this.status = this.$route.params.status;
      this.webUrl = this.$route.params.web_url;
    }
    this.jobId = this.$route.params.id;
    await this.getPipelineById({ id: this.jobId });
    this.rowModel = this.jobsInfo;
    for (let i = 0; i < this.jobsInfo.jobs.length; i++) {
      this.stageOptions.push(this.jobsInfo.jobs[i].stages);
    }
    if (this.$route.params.stages) {
      this.stage = this.$route.params.stages;
    } else {
      const deployInfo = JSON.parse(sessionStorage.getItem('stage'));
      this.stage = deployInfo.stage;
    }
    this.stages = this.$route.params.stages;
    sessionStorage.setItem(
      'stage',
      JSON.stringify({
        stage: this.stages
      })
    );
    await this.getData();
    this.polling();
    this.loading = false;
  },
  destroyed() {
    clearInterval(this.timer);
  }
};
</script>

.<style lang="stylus" scoped>
.jobs
  .build-trace
    background #000
    color #c4c4c4
    overflow-x auto
    padding 8px
  .no-code
    .icon
      margin 0 auto
      display block
    .desc
      text-align: center
      color #2e2e2e
      font-weight 600
      position relative
      top -100px
.tip
  line-height 1.5
  font-size 14px
  display inline-block
  vertical-align baseline
  padding 2px 7px 4px
  box-sizing border-box
  white-space nowrap
  border-radius 4px
  &.success, &.succeeded
    position relative
    top 2px
    padding-right 12px
    color: #1aaa55
  &.passed, &.succeeded
    position relative
    top 2px
    padding-right 12px
    color: #1aaa55
  &.canceled
    position relative
    top 2px
    padding-right 12px
    color #2e2e2e
  &.failed, &.error, &.pending
    position relative
    top 2px
    padding-right 12px
    color #db3b21
  &.pending
    position relative
    top 2px
    padding-right 12px
    color #fc9403
  &.error
    position relative
    top 2px
    padding-right 12px
    color #db3b21
  &.skipped
    position relative
    top 2px
    padding-right 12px
    color: #db3b21
  &.running
    color: #1f78d1
.top-mt
  margin-top 11px
.multiple
  width 220px
.tip-top
  margin-top 10px
.top-icon
  margin-top 13px
  margin-right 8px
  margin-left -8px
.right-icon
  margin-top 5px
.left-right
  float right
  width 300px
.b-name
  margin-top 25px
  margin-right 100px
  font-size 18px
.code-width
  width 70%
  height auto
.trigger-time
  padding-left 3px
.cancel-btn
  position absolute
  right 30px
.green
  color #00d600
.red
  color #db3b21
.white
  color white
.wran
  color #bdbd00
.ws-pre-div
  line-height 2
  font-size 13px
  font-family: Menlo, DejaVu Sans Mono, Liberation Mono, Consolas, Ubuntu Mono, Courier New, andale mono, lucida console, monospace
  display block
  white-space pre-wrap
.ws-pre-wrap
  word-break break-all
</style>
