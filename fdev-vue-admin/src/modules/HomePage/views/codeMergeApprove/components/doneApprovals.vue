<template>
  <Loading :visible="loading">
    <fdev-table
      :data="tableData"
      :columns="columns"
      row-key="id"
      :export-func="handleExportExcel"
      :pagination.sync="pagination"
      @request="pageTableRequest"
      title-icon="todo_list_s_f"
      title="已完成的审批列表"
      class="my-sticky-column-table"
      :visible-columns="visibleColsDone"
      :on-select-cols="saveVisibleColumnsDone"
    >
      <template #top-right>
        <fdev-btn normal label="查询" ficon="search" @click="queryDoneList()" />
      </template>
      <template v-slot:top-bottom>
        <f-formitem
          class="col-4 q-pr-md"
          label="日期选择"
          bottom-page
          label-style="width:100px"
        >
          <f-date
            range
            :value="dateRange"
            @input="saveDate($event)"
            mask="YYYY-MM-DD"
          />
        </f-formitem>
        <f-formitem
          label="所属小组"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <fdev-select
            use-input
            :value="groupListDone"
            :options="filterGroup"
            multiple
            @input="saveGroupListDone($event)"
            option-label="name"
            option-value="id"
            clearable
            @clear="clearGroup()"
            @filter="groupInputFilter"
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
          </fdev-select>
        </f-formitem>
        <f-formitem
          label="申请人"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <fdev-select
            use-input
            multiple
            @input="saveProposerListDone($event)"
            :value="proposerListDone"
            :options="filterUsers"
            @filter="userFilter"
            option-label="user_name_cn"
            option-value="id"
            clearable
            @clear="clearPerson()"
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
        <!-- 审核人 -->
        <f-formitem
          label="审核人"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <fdev-select
            use-input
            multiple
            @input="saveAuditorListDone($event)"
            :value="auditorListDone"
            :options="filterUsers"
            @filter="userFilter"
            option-label="user_name_cn"
            option-value="id"
            clearable
            @clear="clearAdutor()"
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
        <f-formitem
          label="任务名称"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:100px"
        >
          <fdev-input
            :value="taskNameDone"
            placeholder="请输入任务名称"
            clearable
            @clear="handleSearch()"
            @keyup.enter="handleSearch()"
            @input="saveTaskNameDone($event)"
          >
          </fdev-input>
        </f-formitem>
      </template>
      <!-- 任务名称跳转 -->
      <template v-slot:body-cell-task_name="props">
        <fdev-td class="td-width" :title="props.value">
          <router-link :to="`/job/list/${props.row.task_id}`" class="link">
            {{ props.row.task_name }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.task_name }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
        </fdev-td>
      </template>
      <!-- 所属应用 -->
      <template v-slot:body-cell-project_name="props">
        <fdev-td class="td-desc">
          <router-link
            v-if="props.row.project_id"
            :title="props.row.project_name"
            :to="
              `${getPath(props.row.applicationType)}/${props.row.project_id}`
            "
            class="link"
          >
            {{ props.row.project_name }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.project_name }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
          <span v-else title="-">-</span>
        </fdev-td>
      </template>
      <!-- 开发人员 -->
      <template v-slot:body-cell-developer_name="props">
        <fdev-td>
          <div
            v-if="
              Array.isArray(props.row.developer_name) &&
                props.row.developer.length > 0
            "
            :title="props.row.developer_name.map(v => v.name).join('，')"
            class="td-desc"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link
                v-show="item.name"
                :to="`/user/list/${item.id}`"
                class="link"
              >
                {{ item.name }}
              </router-link>
            </span>
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 任务阶段 -->
      <template v-slot:body-cell-stage="props">
        <fdev-td>
          <div :title="props.row.stage | stageFilter" class="td-desc">
            <f-status-color
              :gradient="props.row.stage | stageColorFilter"
            ></f-status-color>
            {{ props.row.stage | stageFilter }}
          </div>
        </fdev-td>
      </template>
      <!-- 申请人 -->
      <template v-slot:body-cell-applicant_name="props">
        <fdev-td>
          <div
            class="text-ellipsis"
            :title="props.value"
            v-if="props.row.applicant"
          >
            <router-link
              :to="{ path: `/user/list/${props.row.applicant}` }"
              class="link"
            >
              {{ props.value }}
            </router-link>
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 申请原因 -->
      <template v-slot:body-cell-merge_reason="props">
        <fdev-td class="td-width" :title="props.value">
          <div
            class="text-ellipsis"
            :title="props.value"
            v-if="props.row.merge_reason"
          >
            {{ props.row.merge_reason | filterReason }}
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 申请说明 -->
      <template v-slot:body-cell-apply_desc="props">
        <fdev-td class="td-width" :title="props.value">
          <div
            class="text-ellipsis"
            :title="props.value"
            v-if="props.row.apply_desc"
          >
            {{ props.row.apply_desc }}
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 审批人 -->
      <template v-slot:body-cell-auditor_name="props">
        <fdev-td>
          <div
            class="text-ellipsis"
            :title="props.value"
            v-if="props.row.auditor"
          >
            <router-link
              :to="{ path: `/user/list/${props.row.auditor}` }"
              class="link"
            >
              {{ props.value }}
            </router-link>
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 审批说明 -->
      <template v-slot:body-cell-result_desc="props">
        <fdev-td class="td-width" :title="props.value">
          <div
            class="text-ellipsis"
            :title="props.value"
            v-if="props.row.result_desc"
          >
            {{ props.row.result_desc }}
          </div>
          <div v-else>-</div>
        </fdev-td>
      </template>
      <!-- 审批结果 -->
      <template v-slot:body-cell-status="props">
        <fdev-td class="text-ellipsis">
          <div class="row no-wrap items-center">
            <f-status-color
              :gradient="approveStatus(props.row.status) | approveStatusFilter"
            ></f-status-color>
            <span :title="approveStatus(props.row.status)">
              {{ approveStatus(props.row.status) }}
            </span>
          </div>
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { taskStage, exportExcel } from '@/utils/utils';
import { codeDoneColumns } from '@/modules/HomePage/utils/constants';
import { taskStageColorMap, relReasonMap } from '@/modules/Job/utils/constants';
import { exportApproveList } from '@/services/job';
export default {
  name: 'doneApprovals',
  components: { Loading },
  data() {
    return {
      filterGroup: [],
      filterUsers: [],
      loading: false,
      tableData: [],
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      },
      columns: codeDoneColumns(),
      eventNum: {
        todo: 0,
        done: 0
      },
      queryParams: {}
    };
  },
  computed: {
    ...mapState('jobForm', ['codeApproveList']),
    ...mapState('userForm', ['groups']),
    ...mapState('dashboard', ['userList']),
    ...mapState('userActionSaveHomePage/myCodeList', [
      'visibleColsDone',
      'currentDonePage',
      'taskNameDone',
      'proposerListDone',
      'auditorListDone',
      'groupListDone',
      'dateRange'
    ])
  },
  filters: {
    stageColorFilter(val) {
      return taskStageColorMap[val];
    },
    stageFilter(val) {
      return taskStage[val];
    },
    approveStatusFilter(val) {
      const obj2 = {
        待审批:
          'linear-gradient(270deg, rgba(36,200,249,0.50) 0%, #24C8F9 100%)',
        通过: 'linear-gradient(270deg, rgba(77,187,89,0.50) 0%, #4DBB59 100%)',
        拒绝: 'linear-gradient(270deg, rgba(176,190,197,0.50) 0%, #B0BEC5 100%)'
      };
      return obj2[val];
    },
    filterReason(val) {
      return relReasonMap[val];
    }
  },
  watch: {
    pagination: {
      handler(val) {
        this.saveCurrentDonePage(val);
      },
      deep: true
    }
  },
  methods: {
    ...mapActions('jobForm', ['releaseApproveList']),
    // ...mapActions('userForm', ['fetchGroup']),
    // ...mapActions('dashboard', ['queryUserCoreData']),
    ...mapMutations('userActionSaveHomePage/myCodeList', [
      'saveVisibleColumnsDone',
      'saveGroupListDone',
      'saveDate',
      'saveProposerListDone',
      'saveAuditorListDone',
      'saveTaskNameDone',
      'saveCurrentDonePage'
    ]),
    clearGroup() {
      this.saveGroupListDone([]);
      this.queryDoneList();
    },
    clearPerson() {
      this.saveProposerListDone([]);
      this.queryDoneList();
    },
    clearAdutor() {
      this.saveAuditorListDone([]);
      this.queryDoneList();
    },
    handleSearch() {
      this.queryDoneList();
    },
    approveStatus(status) {
      const obj1 = {
        0: '待审批',
        1: '通过',
        2: '拒绝'
      };
      return obj1[status];
    },
    //清空搜索条件
    groupInputFilter(val, update) {
      update(() => {
        this.filterGroup = this.groups.filter(
          tag => tag.fullName.indexOf(val) > -1
        );
      });
    },
    userFilter(val, update, abort) {
      update(() => {
        this.filterUsers = this.userList.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    //不同的应用跳转
    getPath(val) {
      let path = '';
      switch (val) {
        case 'componentWeb':
          // 前端组件
          path = '/componentManage/web/weblist';
          break;
        case 'componentServer':
          // 后端组件
          path = '/componentManage/server/list';
          break;
        case 'archetypeWeb':
          // 前端骨架
          path = '/archetypeManage/web/webArchetype';
          break;
        case 'archetypeServer':
          // 后端骨架
          path = '/archetypeManage/server/archetype';
          break;
        case 'image':
          //镜像
          path = '/imageManage';
          break;
        default:
          //默认跳转应用
          path = '/app/list';
      }
      return path;
    },
    //翻页
    pageTableRequest(props) {
      let { page, rowsPerPage, rowsNumber } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.queryDoneList();
    },
    async queryDoneList() {
      this.loading = true;
      let groupIdList =
        this.groupListDone.length > 0
          ? this.groupListDone.map(val => val.id)
          : this.groupListDone;
      let proposerList =
        this.proposerListDone.length > 0
          ? this.proposerListDone.map(val => val.id)
          : this.proposerListDone;
      let auditorList =
        this.auditorListDone.length > 0
          ? this.auditorListDone.map(val => val.id)
          : this.auditorListDone;
      let params = {
        pageSize: this.pagination.rowsPerPage,
        currentPage: this.pagination.page,
        group: groupIdList,
        status: [1, 2],
        applicant: proposerList,
        taskName: this.taskNameDone,
        auditor: auditorList,
        start_date: this.dateRange.from,
        end_date: this.dateRange.to,
        env: 'uat'
      };
      this.queryParams = params;
      await this.releaseApproveList(params);
      // 设置数据总条数
      this.pagination.rowsNumber = this.codeApproveList.count;
      this.tableData = this.codeApproveList.list;
      this.eventNum.todo = this.codeApproveList.wait_count
        ? this.codeApproveList.wait_count
        : 0;
      this.eventNum.done = this.codeApproveList.finish_count
        ? this.codeApproveList.finish_count
        : 0;
      this.$emit('input', this.eventNum);
      this.loading = false;
    },
    async handleExportExcel() {
      let _this = this;
      let response = await exportApproveList(_this.queryParams);
      exportExcel(response);
    }
  },
  async created() {
    this.pagination = this.currentDonePage;
    this.queryDoneList();
    // await this.fetchGroup();
    // await this.queryUserCoreData({ status: '0' });
  },

  mounted() {
    const tempVisibleColumns = this.visibleColsDone;
    if (!this.visibleColsDone || this.visibleColsDone.length <= 1) {
      this.saveVisibleColumnsDone(tempVisibleColumns);
    }
  }
};
</script>

<style lang="stylus" scoped>
.td-desc
  max-width 350px
  overflow hidden
  text-overflow ellipsis
.td-width
  max-width 300px
  overflow hidden
  text-overflow ellipsis
.border-right button:after
  content: '';
  border-right: 1px solid #DDDDDD;
  display: inline-block;
  height: 14px;
  width: 1px;
  position: absolute;
  right: -5px;
  top: 11px;
.border-right .inline-block:last-child button:after
  display:none !important
</style>
