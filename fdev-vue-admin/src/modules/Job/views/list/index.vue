<template>
  <f-block>
    <div class="shadow-1">
      <div class="row flex-center">
        <div class="col-md-6 col-xs job-top-select">
          <fdev-select
            use-input
            hide-dropdown-icon
            transition-show="jump-down"
            transition-hide="jump-down"
            v-model="term"
            :bottom-slots="true"
            :options="filteredJobs"
            @filter="jobFilter"
            placeholder="请输入任务名称、实施单元编号、标签或所属应用"
          >
            <template v-slot:append>
              <f-icon name="search" class="cursor-pointer text-primary" />
            </template>
            <template v-slot:option="scope">
              <fdev-item
                v-bind="scope.itemProps"
                v-on="scope.itemEvents"
                :to="`/job/list/${scope.opt.id}`"
              >
                <fdev-item-section>
                  <fdev-item-label :title="'任务名: ' + scope.opt.name">
                    <span class="text-grey-9 q-pr-sm">任务名:</span>
                    {{ scope.opt.name }}
                  </fdev-item-label>
                  <fdev-item-label caption>
                    <span
                      class="q-pr-lg"
                      v-if="scope.opt.demandNo"
                      :title="
                        '需求编号: ' +
                          (scope.opt.demandNo ? scope.opt.demandNo : '')
                      "
                    >
                      <span class="text-grey-9">需求编号:</span>
                      {{ scope.opt.demandNo }}
                    </span>
                    <span
                      v-show="scope.opt.unitNo"
                      :title="'实施单元编号: ' + (scope.opt.unitNo || '')"
                    >
                      <span class="text-grey-9">实施单元编号:</span>
                      {{ scope.opt.unitNo }}
                    </span>
                  </fdev-item-label>
                </fdev-item-section>
                <fdev-item-section side> </fdev-item-section>
              </fdev-item>
            </template>
            <template v-slot:hint>
              <span v-if="showInput" class="q-pr-sm"
                >归档，删除，废弃任务查询，使用下方列表查询</span
              >
            </template>
          </fdev-select>
          <div class="q-px-sm q-mt-sm">
            <fdev-expansion-item
              v-model="isCollapsed"
              :iconToggle="false"
              :headerStyle="{ display: 'none' }"
            >
            </fdev-expansion-item>
          </div>
        </div>
      </div>
      <fdev-separator class="q-my-sm" />
      <div
        class="group-fixed  items-center btn-wrapper bg-white q-my-sm q-pa-sm"
      >
        <span class="text-grey-5">所属小组：</span>
        <div style="float:right">
          <fdev-toggle
            :value="isSpectialStatus"
            @input="updateIsSpectialStatus($event)"
            color="blue"
            label="暂缓:"
            left-label
          />
          <fdev-btn flat class="q-mx-md" label="重置" @click="reset" />
          <fdev-btn flat label="收起" @click="close" />
        </div>
        <GroupsTree
          @input="queryJobWithOption"
          v-model="group"
          :data="groupsTree"
          ref="groupsTree"
        />
        <span class="text-grey-5">任务阶段：</span>
        <BtnCheckbox
          :value="stage"
          toggle-color="primary"
          :options="stages"
          @input="stageChange($event)"
        />
      </div>
    </div>
    <Loading :visible="loading['jobForm/queryJobWithOption'] || exLoading">
      <fdev-table
        :data="jobs"
        :columns="columns"
        row-key="id"
        :pagination.sync="pagination"
        :visible-columns="visibleColumns"
        @request="onTableRequest"
        class="my-sticky-column-table"
        titleIcon="list_s_f"
        title="任务列表"
        :export-func="handleExportExcel"
      >
        <template v-slot:top-bottom v-if="showInput">
          <div>
            <fdev-select
              class="q-pt-sm min-length"
              use-input
              hide-dropdown-icon
              transition-show="jump-down"
              transition-hide="jump-down"
              v-model="termAbort"
              :options="filteredJobsAbort"
              @filter="jobFilterAbort"
              placeholder="请输入任务名称、实施单元编号、标签或所属应用"
            >
              <template v-slot:append>
                <f-icon name="search" class="cursor-pointer  text-primary" />
              </template>
              <template v-slot:option="scope">
                <fdev-item
                  v-bind="scope.itemProps"
                  v-on="scope.itemEvents"
                  :to="`/job/list/${scope.opt.id}`"
                >
                  <fdev-item-section>
                    <fdev-item-label :title="'任务名: ' + scope.opt.name">
                      <span class="text-grey-9 q-pr-sm">任务名:</span>
                      {{ scope.opt.name }}
                    </fdev-item-label>
                    <fdev-item-label caption>
                      <span
                        class="q-pr-lg"
                        v-show="scope.opt.demandNo"
                        :title="
                          '需求编号: ' +
                            (scope.opt.demandNo ? scope.opt.demandNo : '')
                        "
                      >
                        <span class="text-grey-9">需求编号:</span>
                        {{ scope.opt.demandNo ? scope.opt.demandNo : '' }}
                      </span>
                      <span
                        v-show="scope.opt.unitNo"
                        :title="'实施单元编号: ' + (scope.opt.unitNo || '')"
                      >
                        <span class="text-grey-9">实施单元编号:</span>
                        {{ scope.opt.unitNo }}
                      </span>
                    </fdev-item-label>
                  </fdev-item-section>
                  <fdev-item-section side> </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </div>
        </template>
        <template v-slot:body-cell-name="props">
          <fdev-td class="td-width" :title="props.value">
            <f-icon
              v-if="props.row.delayFlag == true"
              name="alert_t_f"
              style="color:red"
              title="延期告警！"
            />
            <router-link :to="`/job/list/${props.row.id}`" class="link">
              {{ props.row.name }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.name }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>
        <template v-slot:body-cell-tag="props">
          <fdev-td
            :title="
              props.row.tag && props.row.tag.length !== 0
                ? props.row.tag.join(',')
                : '-'
            "
          >
            {{
              props.row.tag && props.row.tag.length !== 0
                ? props.row.tag.join(',')
                : '-'
            }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-creator="props">
          <fdev-td class="td-desc">
            <router-link :to="`/user/list/${props.value.id}`" class="link">
              {{ props.value.user_name_cn }}
            </router-link>
          </fdev-td>
        </template>
        <template v-slot:body-cell-reviewer="props">
          <fdev-td class="td-desc">
            <router-link :to="`/user/list/${props.value.id}`" class="link">
              {{ props.value.user_name_cn }}
            </router-link>
          </fdev-td>
        </template>
        <template v-slot:body-cell-master="props">
          <fdev-td>
            <div
              v-if="props.row.master.length > 0"
              :title="props.row.master.map(v => v.user_name_cn).join('，')"
              class="td-desc"
            >
              <span v-for="item in props.row.master" :key="item.id">
                <router-link :to="`/user/list/${item.id}`" class="link">
                  {{ item.user_name_cn }}
                </router-link>
              </span>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-spdb_master="props">
          <fdev-td>
            <div
              v-if="props.row.spdb_master.length > 0"
              :title="props.row.spdb_master.map(v => v.user_name_cn).join('，')"
              class="td-desc"
            >
              <span v-for="item in props.row.spdb_master" :key="item.id">
                <router-link :to="`/user/list/${item.id}`" class="link">
                  {{ item.user_name_cn }}
                </router-link>
              </span>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-tester="props">
          <fdev-td>
            <div
              v-if="props.row.tester.length > 0"
              :title="props.row.tester.map(v => v.user_name_cn).join('，')"
              class="td-desc"
            >
              <span v-for="(item, index) in props.value" :key="index">
                <router-link :to="`/user/list/${item.id}`" class="link">
                  {{ item.user_name_cn }}
                </router-link>
              </span>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-developer="props">
          <fdev-td>
            <div
              v-if="props.row.developer.length > 0"
              :title="props.row.developer.map(v => v.user_name_cn).join('，')"
              class="td-desc"
            >
              <span v-for="item in props.row.developer" :key="item.id">
                <router-link :to="`/user/list/${item.id}`" class="link">
                  {{ item.user_name_cn }}
                </router-link>
              </span>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-stage="props">
          <fdev-td>
            <div
              :title="stageFilter(props.row.stage.value, props.row.taskType)"
              class="td-desc"
            >
              <f-status-color
                :gradient="props.row.stage.value | stageColorFilter"
              ></f-status-color>
              {{ stageFilter(props.row.stage.value, props.row.taskType) }}
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-operation="props">
          <fdev-td
            class="row no-wrap"
            :title="
              (props.row.taskSpectialStatus == 1 ||
                props.row.taskSpectialStatus == 2) &&
                '处于暂缓状态'
            "
          >
            <div class="q-gutter-x-sm  row no-wrap">
              <fdev-btn
                flat
                label="处理"
                @click="handleBtn(props.row)"
                :disable="
                  props.row.taskSpectialStatus == 1 ||
                    props.row.taskSpectialStatus == 2
                "
                v-if="
                  !(props.row.taskType && props.row.taskType == 2) &&
                    routeTo(props.row)
                "
              />
              <fdev-btn
                flat
                color="primary"
                label="归档"
                v-if="isProduction(props.row)"
                @click="
                  handleHint(props.row.id, props.row.__index, props.row.name)
                "
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>
      <BehindMasterDialog
        :value.sync="handleShow"
        @changeHandleShow="changeDialogShow"
        :commitTabledata="commitTabledata"
      />
    </Loading>
  </f-block>
</template>

<script>
import GroupsTree from '@/components/GroupsTree';
import { mapState, mapActions, mapMutations, mapGetters } from 'vuex';
import BtnCheckbox from '@/components/BtnCheckbox';
import Loading from '@/components/Loading';
import {
  setCollapsible,
  getCollapsible,
  setPagination,
  getPagination
} from '../../utils/setting';
import { formatJob } from '../../utils/utils';
import {
  perform,
  taskSpecialStatus,
  taskStageColorMap
} from '../../utils/constants';
import {
  formatSelectDisplay,
  formatOption,
  getIdsFormList,
  successNotify,
  taskStage,
  taskStage1
} from '@/utils/utils';
// import { formatUser } from '@/modules/User/utils/model';
import BehindMasterDialog from '../../components/BehindMasterDialog';
import { queryWithOption } from '@/services/job';

export default {
  name: 'List',
  components: {
    Loading,
    BtnCheckbox,
    GroupsTree,
    BehindMasterDialog
  },
  data() {
    return {
      ...perform,
      group: [],
      term: [],
      termAbort: [],
      isCollapsed: !!getCollapsible(),
      filteredJobs: [],
      filteredJobsAbort: [],
      handleShow: false,
      rowIdToPush: '',
      commitTabledata: [],
      jobs: [],
      selectedList: [],
      pagination: {
        page: 1,
        rowsPerPage: getPagination().rowsPerPage || 5,
        rowsNumber: 10
      },
      columns: [
        { name: 'name', label: '任务名称', align: 'left', field: 'name' },
        {
          name: 'fdev_implement_unit_no',
          label: '研发单元编号',
          align: 'left',
          field: 'fdev_implement_unit_no'
        },
        {
          name: 'project_name',
          label: '所属应用',
          align: 'left',
          field: 'project_name',
          copy: true
        },
        {
          name: 'group',
          label: '小组',
          align: 'left',
          field: row => row.group,
          copy: true
        },
        {
          name: 'creator',
          label: '创建者',
          align: 'left',
          field: 'creator'
        },
        {
          name: 'developer',
          label: '开发人员',
          align: 'left',
          field: row => row.developer
        },
        {
          name: 'master',
          label: '任务负责人',
          align: 'left',
          field: row => row.master
        },
        {
          name: 'spdb_master',
          label: '行内负责人',
          align: 'left',
          field: row => row.user_name_en
        },
        {
          name: 'tester',
          label: '测试人员',
          field: 'tester',
          align: 'left'
        },
        {
          name: 'reviewer',
          label: '设计还原审核人',
          align: 'left',
          field: 'reviewer'
        },
        {
          name: 'plan_start_time',
          label: '计划启动日期',
          field: 'plan_start_time',
          align: 'left'
        },
        {
          name: 'start_time',
          label: '实际启动日期',
          field: 'start_time',
          align: 'left'
        },
        {
          name: 'plan_inner_test_time',
          label: '计划提交内测日期',
          field: 'plan_inner_test_time',
          align: 'left'
        },
        {
          name: 'start_inner_test_time',
          label: '实际提交内测日期',
          field: 'start_inner_test_time',
          align: 'left'
        },
        {
          name: 'plan_uat_test_start_time',
          label: '计划提交用户测试日期',
          field: 'plan_uat_test_start_time',
          align: 'left'
        },
        {
          name: 'start_uat_test_time',
          label: '实际提交用户测试日期',
          field: 'start_uat_test_time',
          align: 'left'
        },
        {
          name: 'plan_rel_test_time',
          label: '计划用户测试完成日期',
          field: 'plan_rel_test_time',
          align: 'left'
        },
        {
          name: 'start_rel_test_time',
          label: '实际用户测试完成日期',
          field: 'start_rel_test_time',
          align: 'left'
        },
        {
          name: 'plan_fire_time',
          label: '计划投产日期',
          field: 'plan_fire_time',
          align: 'left'
        },
        {
          name: 'fire_time',
          label: '实际投产日期',
          field: 'fire_time',
          align: 'left'
        },
        {
          name: 'demandNo',
          label: '需求编号',
          align: 'left',
          field: row => (row.demandNo ? row.demandNo : '')
        },
        {
          name: 'uat_test_time',
          label: 'UAT提测日期',
          align: 'left',
          field: 'uat_test_time'
        },
        {
          name: 'stage',
          label: '所处阶段',
          align: 'left',
          field: row => this.stageFilter(row.stage.value, row.taskType)
        },
        {
          name: 'applyApproveTimes',
          label: '审核次数',
          align: 'left',
          field: 'applyApproveTimes'
        },
        {
          name: 'taskSpectialStatus',
          label: '暂缓',
          align: 'left',
          field: row => this.statusFilter(row.taskSpectialStatus)
        },
        { name: 'tag', label: '标签', field: 'tag', align: 'left' },
        {
          name: 'feature_branch',
          label: '任务分支',
          align: 'left',
          field: 'feature_branch'
        },
        { name: 'operation', label: '操作', align: 'left' }
      ],
      taskSpecialStatus,
      exLoading: false
    };
  },
  computed: {
    ...mapState('userActionSaveJob/taskQuery', [
      'stage',
      'isSpectialStatus',
      'visibleColumns'
    ]),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapGetters('userForm', ['groupsTree']),
    ...mapState('userForm', {
      groups: 'groups'
    }),
    ...mapGetters('user', ['isKaDianManager']),
    ...mapState('jobForm', {
      jobWithOptData: 'jobWithOptData',
      fuzzyData: 'fuzzyData',
      commitTips: 'commitTips'
    }),
    ...mapState('global', ['loading']),
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(0, columns.length - 1);
    },
    showInput() {
      return (
        this.stage.indexOf('file') !== -1 ||
        this.stage.indexOf('discard') !== -1 ||
        this.stage.indexOf('abort') !== -1
      );
    }
  },
  filters: {
    stageColorFilter(val) {
      return taskStageColorMap[val];
    }
    // stageFilter(val) {
    //   return taskStage[val];
    // }
  },
  watch: {
    isSpectialStatus(val) {
      this.queryJobWithOption();
    },
    isCollapsed(val) {
      setCollapsible(val);
      if (!val) {
        this.group = [];
        this.updateStage([]);
      }
    },
    term(val) {
      this.saveJob([val]);
    },
    termAbort(val) {
      this.saveJob([val]);
    },
    'pagination.rowsPerPage': function(val) {
      setPagination({ rowsPerPage: val });
    }
  },
  methods: {
    ...mapMutations('userActionSaveJob/taskQuery', [
      'updateStage',
      'updateIsSpectialStatus',
      'updateVisibleColumns'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('jobForm', {
      queryJob: 'queryJobWithOption', // 查询小组，阶段
      fuzzyQueryJob: 'fuzzyQueryJob', //查询实施单元编号，任务名称，所属应用，模糊匹配
      updateJobStage: 'updateJobStage',
      queryCommitTips: 'queryCommitTips'
    }),
    ...mapMutations('jobForm', ['saveJob', 'saveFuzzy']),
    formatSelectDisplay,
    // 点击commit落后数dialog确定按钮后
    changeDialogShow(val) {
      this.handleShow = val;
      this.$router.push(`/job/list/${this.rowIdToPush}?tab=manage`);
    },
    // 点击处理按钮先进行dialog提示冲突数
    async handleBtn(row) {
      this.rowIdToPush = row.id;
      if (row.stage.value == 'sit' || row.stage.value == 'develop') {
        if (row.project_name && row.feature_branch) {
          await this.queryCommitTips({
            taskId: row.id
          });
          // 打开commit提交情况dialog
          if (this.commitTips.commits != 0) {
            this.commitTabledata = this.commitTips.fileList.map(item => {
              return { fileName: item };
            });
            this.handleShow = true;
          } else {
            this.$router.push(`/job/list/${this.rowIdToPush}?tab=manage`);
          }
        }
      } else {
        this.$router.push(`/job/list/${this.rowIdToPush}?tab=manage`);
      }
    },
    reset() {
      this.group = [];
      this.updateStage([]);
      this.queryJobWithOption();
    },
    close() {
      this.$refs.groupsTree.closed();
    },
    getUserName(keys) {
      let strName = '';
      for (let i = 0; i < keys.length; i++) {
        let k = keys[i];
        strName += k.user_name_cn + ',';
      }
      strName = strName.substring(0, strName.lastIndexOf(','));
      return strName;
    },
    formatJson(datas, filterVal) {
      return datas.map(row => {
        return filterVal.map(col => {
          if (
            col === 'developer' ||
            col === 'master' ||
            col === 'spdb_master' ||
            col === 'tester'
          ) {
            return this.getUserName(row[col] || []);
          } else if (col === 'creator' || col === 'reviewer') {
            if (row[col]) {
              return this.getUserName([row[col]]);
            } else return '';
          } else if (col === 'stage') {
            return this.stageFilter(row[col], row.taskType);
          } else if (col === 'taskSpectialStatus') {
            return this.statusFilter(row[col]);
          } else {
            return row[col];
          }
        });
      });
    },
    // 导出方法666
    exportTaskExcel(maps, filename) {
      const tHeader = [
        '任务名称',
        '研发单元编号',
        '需求编号',
        '所属应用',
        '小组',
        '创建者',
        '开发人员',
        '任务负责人',
        '行内负责人',
        '测试人员',
        '设计还原审核人',
        '计划启动日期',
        '实际启动日期',
        '计划提交内测日期',
        '实际提交内测日期',
        '计划提交用户测试日期',
        '实际提交用户测试日期',
        '计划用户测试完成日期',
        '实际用户测试完成日期',
        '计划投产日期',
        '实际投产日期',
        'UAT提测日期',
        '审核次数',
        '所处阶段',
        '暂缓',
        '任务分支'
      ];
      const filterVal = [
        'name',
        'fdev_implement_unit_no',
        'demandNo',
        'project_name',
        'group',
        'creator',
        'developer',
        'master',
        'spdb_master',
        'tester',
        'reviewer',
        'plan_start_time',
        'start_time',
        'plan_inner_test_time',
        'start_inner_test_time',
        'plan_uat_test_start_time',
        'start_uat_test_time',
        'plan_rel_test_time',
        'start_rel_test_time',
        'plan_fire_time',
        'fire_time',
        'uat_test_time',
        'applyApproveTimes',
        'stage',
        'taskSpectialStatus',
        'feature_branch'
      ];
      let self = this;
      import('@/utils/exportExcel').then(excel => {
        const data = self.formatJson(maps, filterVal);
        excel.export_json_to_excel({
          header: tHeader,
          data,
          autoWidth: false,
          filename,
          bookType: 'xlsx'
        });
      });
    },
    // 导出
    async handleExportExcel() {
      this.exLoading = true;
      let param = {
        group: this.group,
        stage: this.stage,
        status: this.isSpectialStatus ? 1 : null,
        size: 0
      };
      const response = await queryWithOption(param);
      this.exportTaskExcel(response.list, '任务列表');
      this.exLoading = false;
    },
    async onTableRequest({ pagination }) {
      const { page, rowsNumber, rowsPerPage } = pagination;
      this.pagination = {
        page,
        rowsPerPage,
        rowsNumber
      };
      await this.queryJobWithOption();
    },
    async stageChange(event) {
      this.updateStage(event);
      await this.queryJobWithOption();
    },
    async queryJobWithOption() {
      //查询,在这里加入分页参数即可
      await this.queryJob({
        group: this.group,
        stage: this.stage,
        page: this.pagination.page,
        per_page: this.pagination.rowsPerPage,
        status: this.isSpectialStatus ? 1 : null
      });

      this.jobs = this.jobWithOptData.list.map(job => formatJob(job));
      //定义总条数
      this.pagination.rowsNumber = this.jobWithOptData.total;

      if (this.stage.length === 0) {
        let filter = this.jobs.filter(item => {
          if (item.stage) {
            return item.stage.code !== 6;
          }
        });
        this.jobs = filter;
      }
    },
    async jobFilter(val, update, abort) {
      if (val !== '') {
        await this.fuzzyQueryJob({
          term: val
        });
      }
      update(() => {
        this.filteredJobs = formatOption(
          this.fuzzyData.map(job => formatJob(job)),
          'name'
        );
        let filter = this.filteredJobs.filter(item => {
          if (item.stage) {
            return (
              item.stage.code !== 5 &&
              item.stage.code !== 6 &&
              item.stage.code !== '7'
            );
          }
        });
        this.filteredJobs = filter;
      });
    },
    async jobFilterAbort(val, update, abort) {
      if (val !== '') {
        let checkArr = this.stage.filter((item, index) => {
          return item === 'discard' || item === 'file' || item === 'abort';
        });
        await this.fuzzyQueryJob({
          term: val,
          stage: checkArr.toString()
        });
      } else {
        this.saveFuzzy([]);
      }
      update(() => {
        this.filteredJobsAbort = formatOption(
          this.fuzzyData.map(job => formatJob(job)),
          'name'
        );
      });
    },
    routeTo(job) {
      job.reviewer = job.reviewer ? job.reviewer : {};
      let ids = getIdsFormList([
        job.partyA,
        job.partyB,
        job.developer,
        job.reviewer
      ]);
      if (
        job.stage.code >= 0 &&
        job.stage.code < 4 &&
        (ids.indexOf(this.currentUser.id) > -1 ||
          this.isKaDianManager ||
          (this.currentUser.role.some(item => item.name === 'UI团队负责人') &&
            job.review_status &&
            job.review_status !== 'irrelevant'))
      ) {
        return true;
      } else {
        return false;
      }
    },
    isProduction(job) {
      let ids = getIdsFormList([job.creator, job.partyA, job.partyB]);
      if (
        job.stage.code === 4 &&
        (ids.indexOf(this.currentUser.id) > -1 || this.isKaDianManager)
      ) {
        return true;
      } else {
        return false;
      }
    },
    async handleHint(id, index, name) {
      return this.$q
        .dialog({
          title: `确认归档`,
          message: `任务归档后，<span class="text-negative">"${name}"</span>的操作均无法执行且无法重新打开！归档后的任务只允许查看！请确认是否进行任务归档？`,
          ok: '确认',
          cancel: '取消',
          html: true
        })
        .onOk(async () => {
          await this.keepInFile(id, index);
        });
    },
    async keepInFile(id, index) {
      let next = this.stages[6];
      await this.updateJobStage({
        id: id,
        stage: next.value
      });
      successNotify('任务已归档');
      this.queryJobWithOption();
    },
    stageFilter(val, type) {
      return type !== 2 ? taskStage[val] : taskStage1[val];
    },
    statusFilter(val) {
      return this.taskSpecialStatus[val];
    }
  },
  async created() {
    if (this.stage) {
      await this.queryJobWithOption();
    }
    this.fetchGroup();
  },
  mounted() {
    if (sessionStorage.getItem('jobSeachModel')) {
      const { group } = JSON.parse(sessionStorage.getItem('jobSeachModel'));
      this.group = group;
      this.queryJobWithOption();
    }
  },
  beforeRouteEnter(to, from, next) {
    const { params } = from;
    if (Object.keys(params).length === 0) {
      sessionStorage.removeItem('jobSeachModel');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    const { params } = to;
    if (Object.keys(params).length) {
      sessionStorage.setItem(
        'jobSeachModel',
        JSON.stringify({
          group: this.group
        })
      );
    }
    next();
  }
};
</script>

<style lang="stylus" scoped>

.card-hover:hover
  cursor pointer
.td-padding
  padding-right 16px!important
.td-width
  max-width 300px
  overflow hidden
  text-overflow ellipsis
.btn-checkbox >>> .q-btn__wrapper
  &::before
    box-shadow none!important
.min-length
  min-width 450px
.td-desc
  max-width 350px
  overflow hidden
  text-overflow ellipsis
.job-top-select >>> .q-field--with-bottom
  padding-bottom:0
.job-top-select >>> .q-field--auto-height.q-field--labeled .q-field__control-container
  padding-top:0
.shadow-1
  margin-bottom 20px
</style>
