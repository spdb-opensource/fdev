<template>
  <Loading :visible="loading">
    <fdev-table
      class="my-sticky-column-table"
      :data="tableData"
      :columns="demandDetail.demand_type == 'daily' ? columnsDaily : columns"
      row-key="id"
      :pagination.sync="pagination"
      :visible-columns="visibleColumns"
      :onSelectCols="changSelect"
      noExport
    >
      <template v-slot:top-left>
        <div class="row">
          <f-formitem
            label-auto
            profile
            label-class="q-mr-lr"
            value-style="width:160px;"
            label="小组"
          >
            <fdev-select
              use-input
              v-model="tableGroup"
              :options="tableGroups"
              @filter="groupInputFilter"
            />
          </f-formitem>
          <f-formitem
            label-auto
            profile
            label-class="q-mr-lr"
            value-style="width:160px"
            label-style="padding-left:30px"
            label="任务阶段"
          >
            <fdev-select
              v-model="tableStage"
              :options="
                demandDetail.demand_type == 'daily'
                  ? tableStagesDaily
                  : tableStages
              "
            />
          </f-formitem>
        </div>
      </template>
      <!-- 任务名称 -->
      <template v-slot:body-cell-name="props">
        <fdev-td>
          <div
            class="text-ellipsis"
            v-if="props.row.name"
            :title="props.row.name"
          >
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
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 研发单元编号 -->
      <template v-slot:body-cell-fdev_implement_unit_no="props">
        <fdev-td>
          <span
            :title="props.row.fdev_implement_unit_no"
            class="a-link text-ellipsis"
            @click="openUnitDetail(props.row)"
          >
            {{ props.value || '-' }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value || '-' }}
              </fdev-banner>
            </fdev-popup-proxy>
          </span>
        </fdev-td>
      </template>
      <!-- 行内项目负责人 -->
      <template v-slot:body-cell-spdb_master="props">
        <fdev-td>
          <div
            class="text-ellipsis"
            v-if="props.row.spdb_master && props.row.spdb_master.length > 0"
            :title="
              props.row.spdb_master.map(item => item.user_name_cn).join(', ')
            "
          >
            <span v-for="(item, index) in props.row.spdb_master" :key="index">
              <router-link
                v-if="item.user_name_cn"
                :to="`/user/list/${item.id}`"
                class="link"
              >
                {{ item.user_name_cn }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{
                      props.row.spdb_master
                        .map(item => item.user_name_cn)
                        .join('  ')
                    }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </span>
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 任务阶段 -->
      <template v-slot:body-cell-stage="props">
        <fdev-td
          v-if="
            props.row.taskSpectialStatus != 1 &&
              props.row.taskSpectialStatus != 2
          "
          :title="stageFilter(props.row.stage, props.row.taskType)"
          class="text-ellipsis"
        >
          {{ stageFilter(props.row.stage, props.row.taskType) }}
        </fdev-td>
        <fdev-td
          v-if="
            props.row.taskSpectialStatus == 1 ||
              props.row.taskSpectialStatus == 2
          "
          :title="props.row.taskSpectialStatus | taskSpectialStatusFilter"
          class="text-ellipsis"
        >
          {{ props.row.taskSpectialStatus | taskSpectialStatusFilter }}
        </fdev-td>
      </template>
      <!-- 是否已提交审核 -->
      <template v-slot:body-cell-code_order_no="props">
        <fdev-td :title="props.row.code_order_no ? '是' : '否'">
          <div class="text-ellipsis">
            {{ props.row.code_order_no ? '是' : '否' }}
          </div>
        </fdev-td>
      </template>
      <!-- 所属应用 -->
      <template v-slot:body-cell-project_name="props">
        <fdev-td :title="props.value">
          <div class="text-ellipsis" v-if="props.row.project_id">
            <router-link :to="`/app/list/${props.row.project_id}`" class="link">
              {{ props.value }}
            </router-link>
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 测试人员 -->
      <template v-slot:body-cell-tester="props">
        <fdev-td>
          <div
            class="text-ellipsis"
            v-if="props.row.tester && props.row.tester.length > 0"
            :title="props.row.tester.map(item => item.user_name_cn).join(' ')"
          >
            <span v-for="(item, index) in props.row.tester" :key="index">
              <router-link
                v-if="item.user_name_cn"
                :to="`/user/list/${item.id}`"
                class="link"
              >
                {{ item.user_name_cn }}
              </router-link>
            </span>
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 任务负责人 -->
      <template v-slot:body-cell-master="props">
        <fdev-td>
          <div
            class="text-ellipsis"
            v-if="props.row.master && props.row.master.length > 0"
            :title="props.row.master.map(item => item.user_name_cn).join(' ')"
          >
            <span v-for="(item, index) in props.row.master" :key="index">
              <router-link
                v-if="item.user_name_cn"
                :to="`/user/list/${item.id}`"
                class="link"
              >
                {{ item.user_name_cn }}
              </router-link>
            </span>
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 开发人员 -->
      <template v-slot:body-cell-developer="props">
        <fdev-td>
          <div
            :title="
              props.row.developer.map(item => item.user_name_cn).join(', ')
            "
            v-if="props.row.developer && props.row.developer.length > 0"
            class="text-ellipsis"
          >
            <span v-for="(item, index) in props.row.developer" :key="index">
              <router-link
                v-if="item.user_name_cn"
                :to="`/user/list/${item.id}`"
                class="link"
              >
                {{ item.user_name_cn || '-' }}
              </router-link>
            </span>
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 创建者 -->
      <template v-slot:body-cell-creator="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.row.creator.user_name_cn">
            <router-link :to="`/user/list/${props.value.id}`" class="link">
              {{ props.row.creator.user_name_cn || '-' }}
            </router-link>
          </div>
        </fdev-td>
      </template>
      <!-- 设计稿审核 -->
      <template v-slot:body-cell-review_status="props">
        <fdev-td>
          <div
            v-if="props.row.review_status"
            :title="props.row.review_status | filterReview"
            class="text-ellipsis"
          >
            <router-link
              :to="`/job/list/${props.row.id}/design`"
              class="link"
              v-if="props.row.review_status !== 'irrelevant'"
            >
              {{ props.row.review_status | filterReview }}
            </router-link>
            <div class="text-ellipsis" v-else>
              {{ props.row.review_status | filterReview }}
            </div>
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { perform, reviewMap } from '../model';
import {
  setTaskPagination,
  getTaskPagination,
  getTableCol,
  setTableCol
} from '../setting';
import {
  formatSelectDisplay,
  deepClone,
  wrapOptionsTotal
} from '@/utils/utils';
import Loading from '@/components/Loading';
import { queryTaskByIpmpUnitNo, queryDetailByUnitNo } from '@/services/demand';
export default {
  name: 'Task',
  components: { Loading },
  props: {
    unitNum: {
      type: String,
      default: ''
    },
    devUnitNum: {
      type: String,
      default: ''
    },
    demandDetail: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      ...perform,
      demandId: '',
      initData: [],
      loading: false,
      tableData: [],
      tableGroup: {
        label: '全部',
        value: 'total'
      },
      tableStage: {
        label: '全部',
        value: 'total'
      },
      tableStages: [
        { label: '全部', value: 'total' },
        // { label: '任务取消', value: 'abort' },
        { label: '待实施', value: 'todo' }, //待实施
        // { label: '录入基本信息', value: 'create-info' },
        // { label: '录入应用信息', value: 'create-app' },
        // { label: '录入分支信息', value: 'create-feature' },
        { label: '开发中', value: 'develop' },
        { label: 'SIT', value: 'sit' },
        { label: 'UAT', value: 'uat' },
        { label: 'REL', value: 'rel' },
        { label: '已投产', value: 'production' },
        { label: '归档', value: 'file' },
        { label: '暂缓', value: 'defer' }
        // { label: '任务废弃', value: 'file' }
      ],
      tableStagesDaily: [
        { label: '全部', value: 'total' },
        { label: '进行中', value: 'develop' },
        { label: '已完成', value: 'production' },
        { label: '已归档', value: 'file' }
      ],
      tableGroups: [],
      columns: [
        {
          name: 'name',
          label: '任务名称',
          field: 'name'
        },
        {
          name: 'fdev_implement_unit_no',
          label: '研发单元编号',
          field: 'fdev_implement_unit_no'
        },
        {
          name: 'review_status',
          label: '设计还原审核状态',
          field: 'review_status'
        },
        {
          name: 'project_name',
          label: '所属应用',
          field: 'project_name'
        },
        {
          name: 'feature_branch',
          label: '分支名称',
          field: 'feature_branch'
        },
        {
          name: 'stage',
          label: '任务阶段',
          field: 'stage'
        },
        {
          name: 'code_order_no',
          label: '是否已提交审核',
          field: 'code_order_no'
        },
        {
          name: 'plan_start_time',
          label: '计划启动开发日期',
          field: 'plan_start_time'
        },
        {
          name: 'plan_inner_test_time',
          label: '计划提交内测日期',
          field: 'plan_inner_test_time'
        },
        {
          name: 'plan_uat_test_start_time',
          label: '计划提交业测日期',
          field: 'plan_uat_test_start_time'
        },
        {
          name: 'plan_rel_test_time',
          label: '计划用户测试完成日期',
          field: 'plan_rel_test_time'
        },
        {
          name: 'plan_fire_time',
          label: '计划投产日期',
          field: 'plan_fire_time'
        },
        {
          name: 'start_time',
          label: '实际启动开发日期',
          field: 'start_time'
        },
        {
          name: 'start_inner_test_time',
          label: '实际提交内测日期',
          field: 'start_inner_test_time'
        },
        {
          name: 'start_uat_test_time',
          label: '实际提交业测日期',
          field: 'start_uat_test_time'
        },
        {
          name: 'start_rel_test_time',
          label: '实际用户测试完成日期',
          field: 'start_rel_test_time'
        },
        {
          name: 'fire_time',
          label: '实际投产日期',
          field: 'fire_time'
        },
        {
          name: 'creator',
          label: '创建者',
          field: 'creator'
        },
        {
          name: 'developer',
          label: '开发人员',
          field: 'developer'
        },
        {
          name: 'master',
          label: '任务负责人',
          field: 'master'
        },
        {
          name: 'spdb_master',
          label: '行内项目负责人',
          field: 'spdb_master'
        },
        {
          name: 'tester',
          label: '测试人员',
          field: 'tester'
        },
        {
          name: 'group',
          label: '所属小组',
          field: row => row.group.name
        },
        // {
        //   name: 'fdev_implement_unit_no',
        //   label: 'fdev内部实施单元',
        //   field: 'fdev_implement_unit_no',
        //   align: 'left'
        // },
        // {
        //   name: 'uat_TestObject',
        //   label: 'uat测试对象',
        //   field: 'uat_TestObject',
        //   align: 'left'
        // },
        {
          name: 'desc',
          label: '描述',
          field: 'desc',
          copy: true
        }
      ],
      columnsDaily: [
        {
          name: 'name',
          label: '任务名称',
          field: 'name'
        },
        {
          name: 'fdev_implement_unit_no',
          label: '研发单元编号',
          field: 'fdev_implement_unit_no'
        },
        {
          name: 'project_name',
          label: '所属应用',
          field: 'project_name'
        },
        {
          name: 'feature_branch',
          label: '分支名称',
          field: 'feature_branch'
        },
        {
          name: 'stage',
          label: '任务阶段',
          field: 'stage'
        },
        {
          name: 'code_order_no',
          label: '是否已提交审核',
          field: 'code_order_no'
        },
        {
          name: 'plan_start_time',
          label: '计划启动日期',
          field: 'plan_start_time'
        },
        {
          name: 'plan_fire_time',
          label: '计划完成日期',
          field: 'plan_fire_time'
        },
        {
          name: 'start_time',
          label: '实际启动日期',
          field: 'start_time'
        },
        {
          name: 'fire_time',
          label: '实际完成日期',
          field: 'fire_time'
        },
        {
          name: 'creator',
          label: '创建者',
          field: 'creator'
        },
        {
          name: 'developer',
          label: '开发人员',
          field: 'developer'
        },
        {
          name: 'master',
          label: '任务负责人',
          field: 'master'
        },
        {
          name: 'spdb_master',
          label: '行内项目负责人',
          field: 'spdb_master'
        },
        {
          name: 'group',
          label: '所属小组',
          field: row => row.group.name
        },
        {
          name: 'desc',
          label: '描述',
          field: 'desc',
          copy: true
        }
      ],
      pagination: getTaskPagination(),
      visibleColumns: this.visibleColumnTaskOptions
    };
  },
  watch: {
    pagination(val) {
      setTaskPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    visibleColumns(val) {
      setTableCol('rqr/Task', val);
    },
    tableGroup: {
      deep: true,
      handler(val) {
        this.filterTable();
      }
    },
    tableStage: {
      deep: true,
      handler(val) {
        this.filterTable();
      }
    }
  },
  computed: {
    ...mapState('userForm', ['groups']),
    ...mapState('demandsForm', ['taskList']),
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(0, columns.length - 1);
    }
  },
  filters: {
    taskSpectialStatusFilter(val) {
      if (val == 1) {
        return (val = '暂缓');
      } else if (val == 3) {
        return (val = '恢复完成');
      }
    },
    filterReview(val) {
      return reviewMap[val];
    }
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('demandsForm', ['queryTaskByDemandId']),
    groupInputFilter(val, update) {
      update(() => {
        this.tableGroups = this.deepCloneGroups.filter(tag =>
          tag.label.includes(val)
        );
      });
    },
    // exportExcel() { },
    formatSelectDisplay,
    // ...mapActions('dashboard', ['queryTaskSitMsg']),
    async init() {
      this.loading = true;
      //从实施单元详情来
      if (this.unitNum) {
        this.initData = await queryTaskByIpmpUnitNo({
          implUnitNum: this.unitNum
        });
      } else if (this.devUnitNum) {
        this.initData = await queryDetailByUnitNo({
          ids: [this.devUnitNum]
        });
      } else {
        await this.queryTaskByDemandId({
          demandId: this.demandId
        });
        this.initData = this.taskList;
      }
      //将3种阶段转为todo待实施
      this.initData &&
        this.initData.forEach(item => {
          if (
            item.stage === 'create-info' ||
            item.stage === 'create-app' ||
            item.stage === 'create-feature'
          ) {
            item.stage = 'todo';
          }
        });
      this.tableData = this.initData;
      this.loading = false;
    },

    openUnitDetail(info) {
      this.$router.push({
        path: '/rqrmn/devUnitDetails',
        query: { id: info.id, dev_unit_no: info.fdev_implement_unit_no }
      });
      // info.implement_leader = [];
      // if (info.implement_leader_all) {
      //   info.implement_leader_all.forEach(item => {
      //     info.implement_leader.push(item.user_name_cn);
      //   });
      // }
      // this.openUnitProfileDiaglog = true;
      // this.implInfo = info;
    },

    filterTable() {
      this.tableData = this.initData;
      if (this.tableGroup && this.tableGroup.value != 'total') {
        this.tableData = this.tableData.filter(item => {
          return item.group.id == this.tableGroup.id;
        });
      }
      if (
        this.tableStage.value != 'total' &&
        this.tableStage.value != 'defer'
      ) {
        this.tableData = this.tableData.filter(item => {
          return (
            item.stage == this.tableStage.value && item.taskSpectialStatus != 1
          );
        });
      } else if (
        this.tableStage.value != 'total' &&
        this.tableStage.value == 'defer'
      ) {
        this.tableData = this.tableData.filter(item => {
          return item.taskSpectialStatus == 1;
        });
      }
    },
    changSelect(clos) {
      this.visibleColumns = clos;
    },
    stageFilter(val, type) {
      //日常需求只有 三种状态 进行中、已完成、已归档
      if (val == 'create-info') {
        return (val = '录入基本信息');
      } else if (val == 'create-app') {
        return (val = '录入应用信息 ');
      } else if (val == 'create-feature') {
        return (val = '录入分支信息 ');
      } else if (val == 'develop') {
        if (type === 2) {
          return (val = '进行中');
        } else {
          return (val = '开发中');
        }
      } else if (val == 'sit') {
        return (val = 'SIT');
      } else if (val == 'uat') {
        return (val = 'UAT');
      } else if (val == 'rel') {
        return (val = 'REL');
      } else if (val == 'production') {
        if (type === 2) {
          return (val = '已完成');
        } else {
          return (val = '已投产');
        }
      } else if (val == 'file') {
        if (type === 2) {
          return (val = '已归档');
        } else {
          return (val = '归档');
        }
      } else if (val == 'create') {
        return (val = '创建');
      } else if (val == 'todo') {
        return (val = '待实施');
      }
    }
  },

  async created() {
    this.demandId = this.$route.params.id || this.$route.query.id;
    this.init();
    await this.fetchGroup();
    this.deepCloneGroups = deepClone(this.groups);
    this.deepCloneGroups.forEach((group, index) => {
      this.$set(this.deepCloneGroups[index], 'label', group.fullName);
    });
    this.tableGroups = wrapOptionsTotal(this.deepCloneGroups);
  },
  mounted() {
    const tempVisibleColumns = this.visibleColumnTaskOptions;
    this.visibleColumns = getTableCol('rqr/Task');
    if (!this.visibleColumns || this.visibleColumns.length <= 2) {
      this.visibleColumns = tempVisibleColumns;
    }
  }
};
</script>

<style lang="stylus" scoped>
.q-chip {
  background: white;
}

.overflow {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 250px;
  display: inline-block;
}

.width-select {
  min-width: 150px;
}

.btn {
  width: 30px;
}

.btn-last {
  width: 60px;
}

.td-width {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.td-desc {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pdr10
  padding-right 10px
.chipS
  margin 16px 0 10px
</style>
