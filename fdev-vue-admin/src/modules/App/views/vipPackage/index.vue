<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        :data="tableData"
        :columns="columns"
        row-key="id"
        :pagination.sync="pagination"
        @request="onTableRequest"
        noExport
        no-select-cols
        title="vip打包通道"
        title-icon="list_s_f"
      >
        <template v-slot:top-bottom>
          <f-formitem
            class="col-4"
            bottom-page
            label="中/英文名"
            label-style="width:110px"
          >
            <fdev-select
              use-input
              :value="name_en"
              :options="appOptions"
              @input="nameChange($event)"
              option-label="name_en"
              @filter="filterAppModel"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name_zh">{{
                      scope.opt.name_zh
                    }}</fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.name_en">
                      {{ scope.opt.name_en }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem
            class="col-4"
            bottom-page
            label="分支"
            v-show="name_en"
            label-style="width:110px"
          >
            <fdev-select
              :value="branch"
              :options="refOptions"
              option-label="label"
              option-value="label"
              map-options
              @input="branchChange($event)"
              emit-value
            />
          </f-formitem>
          <f-formitem
            class="col-4"
            bottom-page
            label="状态"
            label-style="width:110px"
          >
            <fdev-select
              use-input
              :value="status"
              :options="statusOptions"
              option-label="label"
              option-value="label"
              map-options
              @input="statusChange($event)"
              emit-value
            />
          </f-formitem>
          <f-formitem
            class="col-4"
            bottom-page
            label="触发者"
            label-style="width:110px"
          >
            <fdev-select
              @input="triggererChange($event)"
              use-input
              :options="userOptions"
              option-label="user_name_cn"
              option-value="triggerer"
              ref="triggerer"
              :value="triggerer"
              @filter="filterUser"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">
                      {{ scope.opt.user_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.user_name_en">
                      {{ scope.opt.user_name_en }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
        </template>
        <template v-slot:top-right>
          <fdev-btn
            ficon="manage"
            label="部署"
            normal
            @click="goToManagePage('opt')"
          />
        </template>

        <template v-slot:body-cell-App="props">
          <fdev-td :title="props.row.app.app_name">
            <div>
              <router-link
                :to="`/app/list/${props.row.app.app_id}`"
                class="link"
              >
                {{ props.row.app.app_name }}
              </router-link>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-status="props">
          <fdev-td class="status-width">
            <div class="tips" :class="props.row.status">
              <fdev-icon :name="icon[props.row.status]" />
              {{ props.row.status | passed }}
            </div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-trigger_time="props">
          <fdev-td :title="props.row.trigger_time | triggertime">
            <div>{{ props.row.trigger_time | triggertime }}</div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-Triggerer="props">
          <fdev-td :title="props.row.triggerer.user_name_cn">
            <div>
              <router-link
                :to="`/user/list/${props.row.triggerer.triggererId}`"
                class="link"
              >
                {{ props.row.triggerer.user_name_cn }}
              </router-link>
            </div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-Commit="props">
          <fdev-td class="tip-width">
            <div>
              <fdev-icon name="mdi-source-branch" color="light" />
              <a
                class="text-black refWidth"
                target="_blank"
                :href="
                  `${url(props.row.commit.web_url)}commits/${props.row.ref}`
                "
              >
                {{ props.row.ref }}
              </a>
              <fdev-icon name="ion-git-commit" color="light" />
              <a
                class="text-primary"
                target="_blank"
                :href="`${props.row.commit.web_url}`"
              >
                {{ props.row.commit.short_id }}
              </a>
              <div class="commit-msg">
                {{ props.row.commit.author_name }}
                <a
                  class="text-black"
                  target="_blank"
                  :href="`${props.row.commit.web_url}`"
                >
                  {{ props.row.commit.title }}
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

        <template v-slot:body-cell-Stages="props">
          <fdev-td auto-width class="stages">
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
                    <fdev-item
                      clickable
                      v-close-popup
                      @click="
                        jump(
                          item.stages,
                          item.status,
                          props.row.id,
                          props.row,
                          props.row.commit.web_url
                        )
                      "
                    >
                      <fdev-item-section side>
                        <fdev-icon
                          class="radius "
                          :name="icon[item.status]"
                          :class="item.status"
                        />
                      </fdev-item-section>
                      <fdev-item-section>
                        {{ item.status }}
                      </fdev-item-section>
                    </fdev-item>
                  </fdev-list>
                </fdev-menu>
              </fdev-icon>
              <fdev-tooltip position="top">
                {{ item.stages }}: {{ item.status | passed }}
              </fdev-tooltip>
              <fdev-separator />
            </div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-time="props">
          <fdev-td class="time">
            <div v-if="props.row.duration != 0">
              <fdev-icon name="timer" />
              {{ props.row | duration }}
            </div>
            <div v-if="props.row.finished_at != 0">
              <fdev-icon name="card_giftcard" />
              {{ props.row | finish }}
              <fdev-tooltip position="top">
                {{ props.row.finished_at }}
              </fdev-tooltip>
            </div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-btn="props">
          <fdev-td>
            <fdev-btn
              v-if="
                props.row.status == 'pending' || props.row.status == 'running'
              "
              ficon="cancel_c_o"
              color="red"
              @click="cancelDeploy(props.row)"
              :width="14"
              :height="14"
            />
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import moment from 'moment';
import { setInterval, clearInterval } from 'timers';
import { mapActions, mapState, mapGetters, mapMutations } from 'vuex';
import {
  statusOptions,
  icon,
  vipPackageColumns
} from '@/modules/App/utils/constants';
import { successNotify, errorNotify } from '@/utils/utils';
import { setPagination, getPagination } from '@/modules/App/utils/setting.js';

export default {
  name: 'appVipList',
  components: {
    Loading
  },
  data() {
    return {
      timer: null,
      icon: icon,
      loading: false,
      appOptions: [],
      refOptions: [],
      statusOptions: statusOptions,
      userOptions: [],
      filterListModel: {},
      pagination: {
        page: 1,
        rowsPerPage: getPagination().rowsPerPage || 10,
        rowsNumber: 10
      },
      columns: vipPackageColumns(),
      tableData: []
    };
  },
  watch: {
    'pagination.rowsPerPage': function(val) {
      setPagination({ rowsPerPage: val });
    },
    name_en(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.queryVipPackageList();
      }
    },
    branch(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.queryVipPackageList();
      }
    },
    status(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.queryVipPackageList();
      }
    },
    triggerer(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.queryVipPackageList();
      }
    }
  },
  computed: {
    ...mapState('userActionSaveApp/appVipList', [
      'name_en',
      'branch',
      'status',
      'triggerer'
    ]),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('appForm', [
      'branchList', //通过id取回的分支
      'withEnvAppData', //拿回的应用
      'vipPipelinesList', //查询返回的表格数据
      'cancelVipDeployList' //取消部署返回表格数据
    ])
  },
  filters: {
    triggertime(val) {
      return moment(parseInt(val)).format('YYYY-MM-DD HH:mm:ss');
    },
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
    ...mapMutations('userActionSaveApp/appVipList', [
      'updateNameen',
      'updateRef',
      'updateStatus',
      'updateTriggerer'
    ]),
    ...mapActions('user', ['fetch']),
    ...mapActions('appForm', [
      'queryWithEnv', //应用
      'queryAllBranch', //分支
      'queryPipelinesList', //查询的表格数据
      'cancelVipDeploy' //取消返回的表格数据
    ]),
    async nameChange(event) {
      this.updateNameen(event);
      this.updateRef('');
      await this.queryVipPackageList();
    },
    async branchChange(event) {
      this.updateRef(event);
      await this.queryVipPackageList();
    },
    async statusChange(event) {
      this.updateStatus(event);
      await this.queryVipPackageList();
    },
    async triggererChange(event) {
      this.updateTriggerer(event);
      await this.queryVipPackageList();
    },
    filterUser(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.userOptions = this.userList.filter(
          v =>
            v.user_name_cn.indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    filterAppModel(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.appOptions = this.withEnvAppData.filter(v => {
          //请求回来的应用列表
          return (
            v.name_en.toLowerCase().indexOf(needle) > -1 ||
            v.name_zh.indexOf(val) > -1
          );
        });
      });
    },
    jump(stages, status, id, row, web_url) {
      this.$router.push({
        name: 'vipJobs',
        params: {
          id: id,
          stages: stages,
          status: status,
          row: row,
          web_url: web_url
        }
      });
    },
    url(web_url) {
      if (web_url) {
        return web_url.substring(0, web_url.indexOf('-/'));
      }
    },
    polling() {
      clearInterval(this.timer);
      this.timer = setInterval(() => this.queryVipPackageList(), 10000);
    },
    goToJobLogDetail(job_id) {
      this.$router.push({
        name: 'Jobs',
        params: {}
      });
    },
    //取消部署
    async cancelDeploy(row) {
      await this.cancelVipDeploy({ id: row.id });
      this.tableData = this.cancelVipDeployList;
      await this.queryVipPackageList();
      successNotify('取消部署成功!');
    },
    //查询vip部署列表
    async onTableRequest(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      await this.queryPipelinesList({
        page: this.pagination.page,
        per_page: this.pagination.rowsPerPage,
        ...this.filterListModel
      });
      this.tableData = this.vipPipelinesList.vipChannelList;
      this.pagination.rowsNumber = this.vipPipelinesList.total;
    },
    goToManagePage(path, id) {
      this.$router.push({
        path: `/app/appDeploy/${path}`,
        query: { id }
      });
    },
    /* 点击查询时，保存查询的参数，分页时携带查询的参数；没有点击查询则不保存 */
    async queryVipPackageList() {
      this.filterListModel = {
        name_en: this.name_en ? this.name_en.name_en : null,
        ref: this.branch ? this.branch : '',
        status: this.status ? this.status : '',
        triggerer: this.triggerer ? this.triggerer.id : null
      };
      if (this.name_en) {
        await this.queryAllBranch({
          gitlab_project_id: this.name_en.gitlab_project_id.toString()
        });
        this.refOptions = this.branchList;
      }
      this.loading = true;
      await this.onTableRequest();
      this.loading = false;
    }
  },
  async created() {
    if (this.currentUser.user_name_en != 'admin') {
      errorNotify('当前用户无权限打开Vip打包通道，只有管理员才可执行此操作!');
      this.$router.push('/app/list');
      return;
    }
    await this.fetch();
    this.userOptions = this.userList;
    if (this.triggerer) {
      this.updateTriggerer(
        this.userList.find(user => user.id === this.triggerer.id)
      );
    } else {
      this.updateTriggerer(
        this.userList.find(user => user.id === this.currentUser.id)
      );
    }
    if (this.name_en) {
      this.filterListModel = {
        name_en: this.name_en ? this.name_en.name_en : null,
        ref: this.branch ? this.branch : '',
        status: this.status ? this.status : '',
        triggerer: this.triggerer ? this.triggerer : ''
      };
      await this.queryAllBranch({
        gitlab_project_id: this.name_en.gitlab_project_id.toString()
      });
      this.refOptions = this.branchList;
      await this.onTableRequest();
    }
    await this.queryVipPackageList();
    if (this.pagination.rowsPerPage) {
      /* 任务页面加轮询 */
      this.polling();
    }

    await this.queryWithEnv();
    this.appOptions = this.withEnvAppData;
  },
  mounted() {},
  destroyed() {
    clearInterval(this.timer);
  }
};
</script>

<style lang="stylus" scoped>

.status-width
  max-width 10%
  box-sizing: border-box;
  width: 10%;
  .tips
    line-height: 1.5;
    font-size 14px
    display inline-block
    vertical-align: baseline;
    padding: 2px 7px 4px;
    box-sizing border-box
    white-space: nowrap;
    border-radius: 4px;
    &.success, &.succeeded, &.passed
      border 1px solid  #1aaa55
      color: #1aaa55
      cursor text
    &.canceled
      border 1px solid  #2e2e2e;
      color: #2e2e2e;
      cursor text
    &.failed, &.error
      border 1px solid  #db3b21;
      color: #db3b21;
      cursor text
    &.running
      border 1px solid  #1f78d1;
      color: #1f78d1;
      cursor text
    &.pending
      border 1px solid  #fc9403;
      color: #fc9403;
      cursor text
.multiple
  width 200px
.tip-width
  max-width 10%
  box-sizing: border-box;
  width: 10%;
  .tip
    line-height: 1.5;
    font-size 14px
    display inline-block
    vertical-align: baseline;
    padding: 2px 7px 4px;
    box-sizing border-box
    white-space: nowrap;
    border-radius: 4px;
    &.success, &.succeeded, &.passed
      border 1px solid  #1aaa55
      color: #1aaa55
    &.canceled
      border 1px solid  #2e2e2e;
      color: #2e2e2e;
    &.failed, &.error
      border 1px solid  #db3b21;
      color: #db3b21;
    &.running
      border 1px solid  #a7a7a7;
      color: #a7a7a7;
    &.pending
      border 1px solid  #fc9403;
      color: #fc9403;
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
    width 25%!important
    max-width 150px
    .commit-msg
      overflow hidden
      text-overflow ellipsis
      width 100%
  .td-width
    width 60%!important
  .pipeline
    max-width: 121.4px;
    width 15%!important
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
.passed, .succeeded
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
.pending
  color: #fc9403;
  &:hover
    background-color: #fbe5e1;
    border-color: #fc9403;
    cursor pointer
.created
  color: #a7a7a7;
.running
  color: #1f78d1;
.pending
  color: #fc9403;
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
.btn-cancel
  width 32px
  height 32px
.q-s-width
  width 220px
.q-s-width-name
  width 300px
.link
  text-decoration: none !important;
  outline: none !important;
.select-input
 word-break break-all
.width100
  vertical-align top
</style>
