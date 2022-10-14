<template>
  <f-block>
    <Loading :visible="loading">
      <!-- <div class="bg-white"> -->
      <fdev-table
        :data="tableData"
        :columns="columns"
        hide-bottom
        :pagination.sync="pagination"
        row-key="index"
        title="流水线列表"
        titleIcon="list_s_f"
        no-select-cols
        noExport
      >
        <template v-slot:body-cell-status="props">
          <fdev-td class="tip-width">
            <div
              @click="open(props.row.web_url)"
              class="tip"
              :class="props.row.status"
            >
              <fdev-icon :name="icon[props.row.status]" />
              {{ props.row.status | passed }}
            </div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-Pipeline="props">
          <fdev-td class="text-ellipsis">
            <a target="_blank" class="text-black" :href="props.row.web_url">
              #{{ props.row.id }}
            </a>
            by
            <a
              :href="props.row.user.web_url"
              target="_blank"
              class="text-primary"
            >
              {{ props.row.user.name }}
            </a>
          </fdev-td>
        </template>

        <template v-slot:body-cell-Commit="props">
          <fdev-td class="commit" :class="fromAutoTest ? 'td-width' : ''">
            <div>
              <fdev-icon name="mdi-source-branch" color="light" />
              <a
                class="text-black refWidth"
                target="_blank"
                :href="`${url(props.row.web_url)}commits/${props.row.ref}`"
              >
                {{ props.row.ref }}
              </a>
              <fdev-icon name="ion-git-commit" color="light" />
              <a
                class="text-primary"
                target="_blank"
                :href="`${url(props.row.web_url)}commit/${props.row.sha}`"
              >
                {{ props.row.commit.short_id }}
              </a>
              <div class="commit-msg">
                {{ props.row.commit.author_name }}
                <a
                  class="text-black"
                  target="_blank"
                  :href="`${url(props.row.web_url)}commit/${props.row.sha}`"
                >
                  {{ props.row.commit.message }}
                </a>
              </div>
              <fdev-tooltip position="top">
                <fdev-icon name="mdi-source-branch" />
                {{ props.row.ref }}
                <fdev-icon name="ion-git-commit" />
                {{ props.row.commit.short_id }}
                <br />
                {{ props.row.commit.author_name }}
                {{ props.row.commit.title }}
              </fdev-tooltip>
            </div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-Stages="props" v-if="!fromAutoTest">
          <fdev-td class="stages">
            <div
              class="icon-wrapper"
              v-for="item in props.row.jobs"
              :key="item.id"
            >
              <fdev-icon
                class="radius"
                :name="icon[item.status]"
                :class="item.status"
              >
                <fdev-menu anchor="bottom left" :offset="[120, 10]">
                  <fdev-list class="listWidth">
                    <fdev-item clickable v-close-popup @click="jump(item.id)">
                      <fdev-item-section side>
                        <fdev-icon
                          class="radius "
                          :name="icon[item.status]"
                          :class="item.status"
                        />
                      </fdev-item-section>
                      <fdev-item-section>
                        {{ item.name }}
                      </fdev-item-section>
                    </fdev-item>
                  </fdev-list>
                </fdev-menu>
              </fdev-icon>
              <fdev-tooltip>
                {{ item.stage }}: {{ item.status | passed }}
              </fdev-tooltip>
              <fdev-separator />
            </div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-time="props">
          <fdev-td class="time">
            <div>
              <fdev-icon name="timer" />
              {{ props.row | duration }}
            </div>
            <div>
              <fdev-icon name="card_giftcard" />
              {{ props.row | finish }}
              <fdev-tooltip>
                {{ props.row.finished_at }}
              </fdev-tooltip>
            </div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-btn="props">
          <fdev-td>
            <fdev-btn
              label="详情"
              flat
              @click="goToJobLogDetail(props.row.job_id)"
              class="q-mr-md"
            />
            <fdev-btn
              label="重试"
              flat
              v-if="props.row.status === 'error'"
              @click="update(props.row.job_id)"
            />
          </fdev-td>
        </template>
      </fdev-table>
      <!-- <div class="text-center">
        <fdev-btn
          type="button"
          label="返回"
          class="q-mt-lg"
          @click="goBack"
          color="primary"
          text-color="white"
        />
      </div> -->
      <!-- </div> -->
    </Loading>
  </f-block>
</template>

<script>
import { icon, pipelinesColumns } from '@/modules/App/utils/constants';
import Loading from '@/components/Loading';
import moment from 'moment';
import { setInterval, clearInterval } from 'timers';
import { mapActions, mapState } from 'vuex';

export default {
  name: 'Pipelines',
  components: {
    Loading
  },
  data() {
    return {
      icon: icon,
      loading: false,
      columns: pipelinesColumns(),
      tableData: [],
      pagination: {
        rowsPerPage: 0
      },
      releaseProjectID: ''
    };
  },
  props: {
    project_id: {
      type: Number
    },
    copyRef: {
      type: String
    },
    rowNumber: {
      type: Number
    },
    application_id: {
      type: String
    },
    release_node_name: {
      type: String
    },
    fromAutoTest: {
      type: Boolean,
      default: false
    },
    env_name: {
      type: String
    },
    flag: {
      type: String,
      default: ''
    }
  },
  computed: {
    ...mapState('appForm', [
      'pipelinesList',
      'appTagPiplines',
      'testRunPiplines',
      'runnerJobLog'
    ])
  },
  filters: {
    duration(val) {
      let hour = parseInt(val.duration / 3600);
      hour =
        hour > 10
          ? parseInt(val.duration / 3600)
          : `0${parseInt(val.duration / 3600)}`;
      let minute = parseInt((val.duration % 3600) / 60);
      minute =
        minute > 10
          ? parseInt((val.duration % 3600) / 60)
          : `0${parseInt((val.duration % 3600) / 60)}`;
      let second = parseInt((val.duration % 3600) % 60);
      second =
        second > 10
          ? parseInt((val.duration % 3600) % 60)
          : `0${parseInt((val.duration % 3600) % 60)}`;
      return `${hour}:${minute}:${second}`;
    },
    finish(val) {
      return moment(val.finished_at).fromNow();
    },
    passed(val) {
      return val === 'success' ? 'passed' : val;
    }
  },
  methods: {
    ...mapActions('appForm', {
      queryPipelines: 'queryPipelines',
      queryAppTagPiplines: 'queryAppTagPiplines',
      queryRunnerJobLog: 'queryRunnerJobLog',
      retryAutoTest: 'retryAutoTest',
      queryTestRunPiplines: 'queryTestRunPiplines'
    }),
    async getData() {
      this.loading = true;
      if (this.application_id) {
        if (this.flag === 'testrun') {
          await this.queryTestRunPiplines({
            application_id: this.application_id,
            pages: this.rowNumber, //页数
            release_node_name: this.release_node_name
          });
          this.tableData = this.testRunPiplines.pipelines;
          this.releaseProjectID = this.testRunPiplines.project_id;
        } else {
          await this.queryAppTagPiplines({
            application_id: this.application_id,
            pages: this.rowNumber, //页数
            release_node_name: this.release_node_name
          });
          this.tableData = this.appTagPiplines.pipelines;
          this.releaseProjectID = this.appTagPiplines.project_id;
        }
      } else {
        if (!this.fromAutoTest) {
          if (Object.keys(this.columns).length < 5) {
            this.columns.splice(this.columns.length - 1, 0, {
              name: 'Stages',
              label: 'Stages',
              field: 'Stages',
              align: 'left'
            });
          }
          await this.queryPipelines({
            project_id: this.project_id,
            pages: this.rowNumber, //页数
            ref: this.copyRef
          });
          this.tableData = this.pipelinesList;
        } else {
          if (Object.keys(this.columns).length < 5) {
            this.columns.push({ name: 'btn', label: '操作', align: 'center' });
          }
          let params = {
            env_name: this.env_name.toUpperCase(),
            project_id: this.project_id
          };
          await this.queryRunnerJobLog(params);
          this.tableData = [];
          this.runnerJobLog.map(item => {
            let tableData = {};
            tableData.status = item.status.toLowerCase();
            tableData.commit = item.metadata.commit;
            tableData.commit.short_id = item.metadata.commit.id.slice(0, 8);
            tableData.sha = item.metadata.commit.id;
            tableData.user = {};
            tableData.user.web_url = item.metadata.pipeline.userurl;
            tableData.user.name = item.metadata.pipeline.name;
            tableData.id = item.metadata.pipeline.id;
            tableData.ref = item.metadata.commit.ref;
            tableData.web_url = item.metadata.pipeline.pipeline_url;
            let date = new Date().getTime();
            tableData.finished_at = moment(date - item.finishedtime).format(
              'YYYY-MM-DD HH:mm:ss'
            );
            tableData.duration = item.finishedtime - item.startedtime;
            tableData.job_id = item.id;
            this.tableData.push(tableData);
          });
        }
      }
      this.loading = false;
    },
    open(url) {
      window.open(url);
    },
    jump(id) {
      if (this.application_id) {
        const params = {
          project_id: this.releaseProjectID,
          job_id: id
        };
        this.$emit('job', params);
      } else {
        this.$router.push({
          name: 'Jobs',
          params: {
            job_id: id,
            project_id: this.project_id
          }
        });
      }
    },
    url(web_url) {
      return web_url.substring(0, web_url.indexOf('pipelines'));
    },
    polling() {
      const timer = setInterval(() => {
        this.getData();
      }, 10000);
      /* 页面停留超过5分钟停止轮询 */
      const countdown = setInterval(() => {
        clearInterval(timer);
        clearInterval(countdown);
      }, 300000);
      /* 侦听器，销毁定时器 */
      this.$once('hook:beforeDestroy', () => {
        clearInterval(timer);
        clearInterval(countdown);
      });
    },
    goToJobLogDetail(job_id) {
      this.$router.push({
        name: 'Jobs',
        params: {
          id: job_id,
          job_id: job_id,
          project_id: this.project_id
        }
      });
    },
    async update(job_id) {
      await this.retryAutoTest({ id: job_id });
    }
  },
  created() {
    this.getData();
    if (this.rowNumber) {
      /* 任务页面加轮询 */
      this.polling();
    }
  }
};
</script>

<style lang="stylus" scoped>
.tip-width
  box-sizing: border-box;
  .tip
    line-height: 1.5;
    font-size 14px
    display inline-block
    vertical-align: baseline;
    padding: 2px 7px 4px;
    box-sizing border-box
    white-space: nowrap;
    border-radius: 4px;
    &.success, &.succeeded
      border 1px solid  #1aaa55
      color: #1aaa55
    &.canceled
      border 1px solid  #2e2e2e;
      color: #2e2e2e;
    &.failed, &.error
      border 1px solid  #db3b21;
      color: #db3b21;
    &.running
      border 1px solid  #1f78d1;
      color: #1f78d1;
.stages
  font-size 22px
  hr
    display inline-block
    width 7px
    vertical-align: middle;
  .icon-wrapper
    &:last-child
      hr
        display none
.time
  color #707070
tr
  .commit
    width 40%!important
    max-width 250px
    .commit-msg
      overflow hidden
      text-overflow ellipsis
      width 100%
  .td-width
    width 60%!important
  .icon-wrapper
    display inline-block
    .radius
      border-radius 50%
  .refWidth
    display inline-block
    vertical-align: bottom
    text-overflow ellipsis
    max-width 150px
    overflow hidden
.success, .succeeded
  color: #1aaa55
  &:hover
    background-color: #dcf5e7
    color: #12753a;
    border-color: #12753a;
    cursor pointer
.canceled
  color: #2e2e2e;
  &:hover
    background-color: #707070;
    border-color: #1f1f1f;
    cursor pointer
.failed, .error
  color: #db3b21;
  &:hover
    background-color: #fbe5e1;
    border-color: #c0341d;
    cursor pointer
.created
  color: #a7a7a7;
  cursor pointer
.running
  cursor pointer
  color: #1f78d1;
.pending
  cursor yellow
  color: #1f78d1;
.skipped
  color #a7a7a7
  border 1px solid #a7a7a7
  border-radius 50%
  cursor pointer
  width 19px
  height 19px
a
  margin 0 5px
  &:hover
    text-decoration underline
.listWidth
  width 240px
.btn
  width 30px
</style>
