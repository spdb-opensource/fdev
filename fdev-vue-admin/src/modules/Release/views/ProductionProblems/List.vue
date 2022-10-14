<template>
  <div>
    <!-- 筛选条件 -->
    <f-block block class="q-my-md">
      <div class="row q-pb-md">
        <f-formitem
          label="所属小组"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <fdev-select
            use-input
            multiple
            option-label="name"
            option-value="id"
            map-options
            emit-value
            @filter="filterModule"
            :options="moduleOptions"
            ref="listModel.module"
            :value="listModel.module"
            @input="updateListModelModule($event)"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name">{{
                    scope.opt.name
                  }}</fdev-item-label>
                  <fdev-item-label caption :title="scope.opt.fullName">
                    {{ scope.opt.fullName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select></f-formitem
        >
        <f-formitem
          label="负责人"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <fdev-select
            use-input
            @filter="filterUser"
            :options="userOptions"
            option-label="user_name_cn"
            option-value="user_name_en"
            ref="listModel.responsible_name_en"
            :value="listModel.responsible_name_en"
            @input="updateListModelResponsibleName($event)"
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
          </fdev-select></f-formitem
        >
        <f-formitem
          label="负责人类型"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <fdev-select
            option-label="label"
            option-value="value"
            emit-value
            map-options
            :options="responsibleTypeOptions"
            ref="listModel.responsible_type"
            :value="listModel.responsible_type"
            @input="updatelistModelResponsibleType($event)"
          />
        </f-formitem>
      </div>
      <div class="row q-pb-md">
        <f-formitem
          label="评审状态"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <fdev-select
            use-input
            :value="listModel.reviewer_status"
            @input="updateListModelReviewerStatus($event)"
            :options="reviewerOptions"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem
          label="问题类型"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <fdev-select
            use-input
            multiple
            option-label="label"
            option-value="label"
            emit-value
            map-options
            :options="problemTypeOptions"
            ref="listModel.issue_level"
            :value="listModel.problemType"
            @input="updateListModelProblemType($event)"
        /></f-formitem>
        <f-formitem
          label="处理状态"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <fdev-select
            :options="dealStatusOptions"
            ref="listModel.deal_status"
            :value="listModel.deal_status"
            @input="updateListModelDealStatus($event)"
        /></f-formitem>
      </div>
      <div class="row">
        <f-formitem
          label="生产问题级别"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <fdev-select
            option-label="label"
            option-value="label"
            emit-value
            map-options
            :options="issueLevelOptions"
            ref="listModel.issue_level"
            :value="listModel.issue_level"
            @input="updateListModelIssueLevel($event)"
        /></f-formitem>
        <f-formitem
          label="开始日期"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <f-date
            :options="startOptions"
            :value="listModel.start_time"
            @input="updateListModelStartTime($event)"
          />
        </f-formitem>
        <f-formitem
          label="结束日期"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <f-date
            :options="endOptions"
            :value="listModel.end_time"
            @input="updateListModelEndTime($event)"
          />
        </f-formitem>
      </div>

      <div class="row q-pl-md q-py-md">
        <fdev-toggle
          :value="listModel.isIncludeChildren"
          @input="updatelistModelIsIncludeChildren($event)"
          label="是否包含子组"
          left-label
          class="q-pr-md"
        />
        <fdev-toggle
          :value="isChartDisplay"
          @input="updateIsChartDisplay($event)"
          label="是否显示图表"
          left-label
        />
      </div>
      <div class="text-center">
        <fdev-btn
          label="查询"
          dialog
          @click="filterData"
          class="q-mr-lg"
          ficon="search"
        />
        <fdev-btn
          :label="selectedData.length > 0 ? '导出' : '全部导出'"
          @click="exportProductionProblems"
          :loading="globalLoading['releaseForm/exportProIssues']"
          dialog
        />
      </div>
    </f-block>

    <!-- 数据展示：表格/图表 -->
    <f-block block class="q-my-md">
      <Loading
        class="q-mb-md"
        :visible="globalLoading['releaseForm/queryProductionChart']"
      >
        <StackBarChart
          chartTop="10%"
          :height="chartHeight"
          width="100%"
          id="stackBar"
          ref="chart"
          :chartData="productionChart"
          v-show="isChartDisplay"
        />
      </Loading>
      <Loading :visible="globalLoading['releaseForm/queryProductionTable']">
        <fdev-table
          class="my-sticky-column-table"
          :data="tableData"
          :columns="columns"
          row-key="id"
          title="问题列表"
          titleIcon="list_s_f"
          :pagination.sync="pagination"
          :selected.sync="selectedData"
          @request="initTable"
          noExport
          :rows-per-page-options="[5, 7, 10, 15, 25, 50, 0]"
        >
          <!-- 筛选公司 -->
          <template v-slot:top-bottom>
            <f-formitem label="公司">
              <fdev-tooltip position="top" v-if="selectCompany">
                筛选公司请将每页的行数选择为全部
              </fdev-tooltip>
              <fdev-select
                v-model="tableCompany"
                :options="companyOptions"
                options-dense
                :disable="selectCompany"
              />
            </f-formitem>
          </template>
          <!-- 需求名称 -->
          <template v-slot:body-cell-requirement_name="props">
            <fdev-td class="text-ellipsis" :title="props.value">
              {{ props.value }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>
          <!-- 应急过程 -->
          <template v-slot:body-cell-emergency_process="props">
            <fdev-td class="ellipsis" :title="props.value">
              {{ props.value
              }}<fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>
          <!-- 问题现象 -->
          <template v-slot:body-cell-problem_phenomenon="props">
            <fdev-td :title="props.value" class="ellipsis">
              <router-link
                :to="{
                  path: `/release/productionProblems/${props.row.id}`
                }"
                class="link"
              >
                <span>{{ props.value }}</span>
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.value }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </fdev-td>
          </template>

          <template v-slot:body-cell-btn="props">
            <fdev-td auto-width>
              <div class="q-gutter-x-sm row no-wrap">
                <fdev-btn
                  label="修改"
                  flat
                  :to="`/job/list/${props.row.id}/modifyProductionProblem`"
                />
                <fdev-btn
                  label="删除"
                  color="red"
                  flat
                  @click="deleteProProblem(props.row.id)"
                />
                <fdev-btn
                  label="创建任务"
                  flat
                  @click="
                    $router.push({
                      path: '/job/add',
                      query: { name: props.row.requirement_name }
                    })
                  "
                />
              </div>
            </fdev-td>
          </template>
        </fdev-table>
        <div class="text-center">
          <fdev-btn @click="addProblems" label="新增生产问题录入" />
        </div>
      </Loading>
    </f-block>
  </div>
</template>

<script>
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import {
  listModel,
  dealStatusOptions,
  issueLevelOptions,
  responsibleTypeOptions,
  problemTypeOptions,
  reviewerOptions
} from '@/modules/Release/utils/constants';
import { exportExcel, successNotify, deepClone } from '@/utils/utils';
import {
  getProductionPagination,
  setProductionPagination
} from '@/modules/Release/utils/setting';
import Loading from '@/components/Loading';
import StackBarChart from './component/StackBarChart.vue';
export default {
  name: 'ProductionProblemsList',
  components: { Loading, StackBarChart },
  data() {
    return {
      selectedData: [],
      moduleOptions: [],
      userOptions: [],
      filterModel: listModel(),
      pagination: {
        rowsPerPage: getProductionPagination(),
        rowsNumber: 0,
        page: 1,
        sortBy: ''
      },
      sortord: true,
      tableData: [],
      dealStatusOptions: dealStatusOptions,
      issueLevelOptions: issueLevelOptions,
      companyOptions: [{ label: '全部', value: 'total' }],
      responsibleTypeOptions: responsibleTypeOptions,
      reviewerOptions: reviewerOptions,
      columns: [
        {
          name: 'problem_phenomenon',
          field: 'problem_phenomenon',
          label: '问题现象',
          align: 'left',
          copy: true
        },
        {
          name: 'requirement_name',
          field: 'requirement_name',
          label: '需求名称',
          align: 'left',
          copy: true
        },
        {
          name: 'occurred_time',
          field: 'occurred_time',
          label: '发生日期',
          align: 'left',
          sortable: true
        },
        {
          name: 'first_occurred_time',
          field: 'first_occurred_time',
          label: '问题首次出现时间',
          align: 'left',
          sortable: true
        },
        {
          name: 'location_time',
          field: 'location_time',
          label: '定位时间',
          align: 'left',
          sortable: true
        },
        {
          name: 'repair_time',
          field: 'repair_time',
          label: '修复时间',
          align: 'left',
          sortable: true
        },
        {
          name: 'reviewer_time',
          field: 'reviewer_time',
          label: '评审时间',
          align: 'left',
          sortable: true
        },
        {
          name: 'reviewer',
          field: 'reviewer',
          label: '评审人',
          align: 'left'
        },
        {
          name: 'emergency_process',
          field: 'emergency_process',
          label: '应急过程',
          align: 'left',
          copy: true
        },
        {
          name: 'emergency_responsible',
          field: 'emergency_responsible',
          label: '应急负责人',
          align: 'left'
        },
        {
          name: 'issue_type',
          field: 'issue_type',
          label: '问题类型',
          align: 'left'
        },
        {
          name: 'is_trigger_issue',
          field: 'is_trigger_issue',
          label: '是否产生生产问题',
          align: 'left'
        },
        {
          name: 'issue_level',
          field: 'issue_level',
          label: '生产问题级别',
          align: 'left'
        },
        {
          name: 'deal_status',
          field: 'deal_status',
          label: '处理状态',
          align: 'left'
        },
        {
          name: 'module',
          field: 'module',
          label: '所属小组',
          align: 'left',
          copy: true
        },
        {
          name: 'dev_responsible',
          field: 'dev_responsible',
          label: '开发负责人',
          align: 'left'
        },
        {
          name: 'audit_responsible',
          field: 'audit_responsible',
          label: '代码审核负责人',
          align: 'left'
        },
        {
          name: 'test_responsible',
          field: 'test_responsible',
          label: '内测负责人',
          align: 'left'
        },
        {
          name: 'task_responsible',
          field: 'task_responsible',
          label: '牵头任务责任人',
          align: 'left'
        },
        {
          name: 'btn',
          field: 'btn',
          label: '操作',
          align: 'center'
        }
      ],
      groupList: [],
      problemTypeOptions: problemTypeOptions,
      tableCompany: {
        label: '全部',
        value: 'total'
      },
      // tableCompanies: truncate,
      selectCompany: true
    };
  },
  watch: {
    isChartDisplay(val) {
      if (val) {
        this.$refs.chart.chartResize();
      }
    },
    'pagination.rowsPerPage'(val) {
      setProductionPagination(val);
    },
    async tableCompany(val) {
      if (val.name !== '全部') {
        await this.init();
        this.tableData = this.productionTable.list.filter(
          v => v.companys.indexOf(val.name) > -1
        );
        this.pagination.rowsNumber = this.tableData.length;
      }
    }
  },
  computed: {
    ...mapState('userActionSaveRelease/problemsList', [
      'listModel',
      'isChartDisplay'
    ]),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapState('userForm', {
      groups: 'groups',
      companies: 'companies'
    }),
    ...mapState('releaseForm', [
      'productionTable',
      'export',
      'productionChart'
    ]),
    chartHeight() {
      const length = this.productionChart.xAxis.length;
      if (length < 5) {
        return '350px';
      }
      return 350 + length * 10 + 'px';
    }
  },
  methods: {
    ...mapMutations('userActionSaveRelease/problemsList', [
      'updateListModelModule',
      'updateListModelResponsibleName',
      'updatelistModelResponsibleType',
      'updateListModelReviewerStatus',
      'updatelistModelIsIncludeChildren',
      'updateListModelProblemType',
      'updateListModelDealStatus',
      'updateListModelIssueLevel',
      'updateListModelStartTime',
      'updateListModelEndTime',
      'updateIsChartDisplay'
    ]),
    ...mapActions('userForm', ['fetchGroup', 'fetchCompany']),
    ...mapActions('user', ['fetch']),
    ...mapActions('releaseForm', [
      'exportProIssues',
      'queryProductionTable',
      'queryProductionChart'
    ]),
    ...mapActions('jobForm', ['deleteProIssue']),
    addProblems() {
      this.$q
        .dialog({
          title: '提示',
          message:
            '此登记入口仅对非fdev任务产生的生产问题开放，若有fdev任务，请于我的->我的任务->任务详情页的生产问题tab页面入口进行登记！',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          this.$router.push('/job/list/addProductionProblem/999');
        });
    },

    // 删除生产问题
    deleteProProblem(id) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '确认删除此条生产问题总结？',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteProIssue({
            id: id
          });
          successNotify('删除成功！');
          this.initTable();
        });
    },
    // 所属小组筛选
    filterModule(val, update, abort) {
      update(() => {
        this.moduleOptions = this.groupList.filter(
          tag => tag.fullName.indexOf(val) > -1
        );
      });
    },
    filterUser(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.userOptions = this.userList.filter(
          v =>
            v.user_name_cn.toLowerCase().indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    endOptions(date) {
      this.listModel.start_time = this.listModel.start_time
        ? this.listModel.start_time
        : '';
      return date > this.listModel.start_time.replace(/-/g, '/');
    },
    startOptions(date) {
      if (this.listModel.end_time) {
        return date < this.listModel.end_time.replace(/-/g, '/');
      }
      return true;
    },
    async exportProductionProblems() {
      const params = {
        ...this.filterModel,
        current_page: this.pagination.page,
        page_size: this.pagination.rowsPerPage,
        ids:
          this.selectedData.length > 0
            ? this.selectedData.map(item => item.id)
            : null
      };
      await this.exportProIssues(params);
      exportExcel(this.export);
    },
    async initTable(props) {
      this.sortord = !this.sortord;
      if (props && props.pagination) {
        let { page, rowsPerPage, sortBy } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
        this.pagination.sortBy = sortBy;
      }
      await this.queryProductionTable({
        current_page: this.pagination.page,
        page_size: this.pagination.rowsPerPage,
        sortParam: this.pagination.sortBy,
        sortord: this.sortord ? 'asc ' : 'desc',
        ...this.filterModel
      });
      this.selectedData = [];

      this.pagination.rowsNumber = this.productionTable.total;
      if (this.pagination.rowsPerPage === 0) {
        this.selectCompany = false;
      } else {
        this.selectCompany = true;
        this.tableCompany.label = '全部';
      }
      return (this.tableData = this.productionTable.list);
    },
    async initChart() {
      await this.queryProductionChart(this.filterModel);
      this.$refs.chart.draw();
    },
    filterData() {
      this.filterModel = {
        ...this.listModel,
        responsible_type: this.listModel.responsible_type
          ? this.listModel.responsible_type
          : '0',
        responsible_name_en: this.listModel.responsible_name_en
          ? this.listModel.responsible_name_en.user_name_en
          : ''
      };
      this.init();
    },
    async init() {
      this.initChart();
      await this.initTable();
    }
  },
  async created() {
    this.filterData();
    this.fetch().then(res => {
      this.userOptions = this.userList;
    });
    this.fetchGroup().then(res => {
      this.moduleOptions = this.groups.slice(0);
      this.groupList = deepClone(this.moduleOptions);
    });
    await this.fetchCompany();
    this.companyOptions = this.companies;
    this.companyOptions.unshift({ label: '全部', value: 'total' });
  }
};
</script>

<style lang="stylus" scoped></style>
