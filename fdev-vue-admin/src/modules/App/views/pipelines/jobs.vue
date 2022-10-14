<template>
  <f-block>
    <Loading :visible="loading">
      <div class="jobs" v-if="!project_id && !jobId">
        <pre class="build-trace" v-if="data.trace !== ''">
          <code v-html="data.trace"/>
        </pre>
        <div v-else class="no-code">
          <div class="flex-center row">
            <f-image :name="icon[data.status]" />
          </div>
          <h4 class="desc">This job has been {{ data.status }}</h4>
        </div>
      </div>
      <div v-if="project_id && !jobId">
        <div class="jobs">
          <pre class="build-trace" v-if="data.trace !== ''">
          <code v-html="data.trace"/>
        </pre>
          <div v-else class="no-code">
            <div class="flex-center row">
              <f-image :name="`${icon[data.status]}`" />
            </div>
            <h4 class="desc">This job has been {{ data.status }}</h4>
          </div>
        </div>
      </div>
      <div v-if="jobId" class="jobs">
        <pre class="build-trace">
          <code v-html="data"/>
        </pre>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';

export default {
  name: 'Jobs',
  components: {
    Loading
  },
  data() {
    return {
      data: {},
      timer: null,
      params: {},
      loading: false,
      icon: { skipped: 'skipped', canceled: 'canceled', created: 'created' },
      jobId: ''
    };
  },
  computed: {
    ...mapState('appForm', ['jobTraces', 'runnerJobLogDetail'])
  },
  props: ['project_id'],
  methods: {
    ...mapActions('appForm', {
      queryTraces: 'queryTraces',
      queryRunnerJobLogDetail: 'queryRunnerJobLogDetail'
    }),
    async getData() {
      if (!this.jobId) {
        await this.queryTraces(this.params);
        this.data = this.jobTraces;
        // this.data.trace = response.trace
        this.data.trace = this.data.trace.replace(/\[0;m/g, '');
        this.data.trace = this.data.trace.replace(/\[0K/g, '');
        this.data.trace = this.data.trace.replace(/\[32;1m/g, '');
      } else {
        await this.queryRunnerJobLogDetail(this.params);
        this.data = this.runnerJobLogDetail;
        this.data = this.data.replace(/\[0;m/g, '');
        this.data = this.data.replace(/\[0K/g, '');
        this.data = this.data.replace(/\[32;1m/g, '');
      }
    },
    polling() {
      clearInterval(this.timer);
      this.timer = setInterval(() => this.getData(), 10000);
    }
  },
  async created() {
    this.loading = true;
    if (this.project_id) {
      this.params = this.project_id;
    } else {
      this.params = this.$route.params;
    }
    this.jobId = this.$route.params.id;
    this.getData();
    this.loading = false;
    this.polling();
  },
  destroyed() {
    clearInterval(this.timer);
  }
};
</script>

.<style lang="stylus" scoped>
.jobs
  .build-trace
    background: #000;
    color: #c4c4c4;
    white-space: pre;
    overflow-x: auto;
    font-size: 12px;
    border-radius: 0;
    border: 0;
    padding: 8px
  .no-code
    .desc
      text-align: center
      color: #2e2e2e;
      font-weight: 600;
      position relative
</style>
