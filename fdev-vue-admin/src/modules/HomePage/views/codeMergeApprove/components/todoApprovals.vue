<template>
  <Loading :visible="loading">
    <fdev-table
      :data="tableData"
      :columns="columns"
      row-key="id"
      no-export
      :pagination.sync="pagination"
      @request="pageTableRequest"
      title-icon="todo_list_s_f"
      title="我的审批列表"
      class="my-sticky-column-table"
      :visible-columns="visibleCols"
      :on-select-cols="saveVisibleColumns"
    >
      <template #top-right>
        <fdev-btn normal label="查询" ficon="search" @click="queryTodoList()" />
      </template>
      <template v-slot:top-bottom>
        <f-formitem
          label="所属小组"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:110px"
        >
          <fdev-select
            use-input
            :value="searchGroupList"
            multiple
            :options="filterGroup"
            @input="saveGroupList($event)"
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
          label-style="width:110px"
        >
          <fdev-select
            use-input
            multiple
            @input="saveProposerList($event)"
            :value="searchProposerList"
            :options="filterUsers"
            option-label="user_name_cn"
            option-value="id"
            @filter="userFilter"
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
        <f-formitem
          label="任务名称"
          class="col-4"
          bottom-page
          label-style="width:110px"
        >
          <fdev-input
            :value="searchTaskName"
            placeholder="请输入任务名称"
            clearable
            @clear="handleSearch()"
            @keyup.enter="handleSearch()"
            @input="saveTaskName($event)"
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
      <!-- 操作列 -->
      <template v-slot:body-cell-operate="props">
        <fdev-td :auto-width="true" class="td-padding">
          <div class="border-right">
            <div class="inline-block" style="display: inline-block;">
              <fdev-btn
                flat
                label="审批通过"
                class="q-mr-sm"
                @click="passApproveClick(props.row)"
              />
            </div>
            <div class="inline-block" style="display: inline-block;">
              <fdev-btn
                flat
                label="审批拒绝"
                class="q-mr-sm"
                @click="rejectApprove(props.row)"
              />
            </div>
          </div>
        </fdev-td>
      </template>
    </fdev-table>
    <f-dialog title="审批拒绝" f-sc v-model="rejectDialog">
      <f-formitem label="审批说明" diaS>
        <fdev-input v-model="approveRejectReason" clearable type="textarea" />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="rejectDialog = false"/>
        <fdev-btn
          label="确定"
          dialog
          @click="conformReject"
          :loading="btnloading"
      /></template>
    </f-dialog>
  </Loading>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { successNotify, taskStage } from '@/utils/utils';
import { todoCodeColumns } from '@/modules/HomePage/utils/constants';
import { taskStageColorMap, relReasonMap } from '@/modules/Job/utils/constants';
export default {
  name: 'todoApprovals',
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
      columns: todoCodeColumns(),
      eventNum: {
        todo: 0,
        done: 0
      },
      rejectDialog: false,
      approveRejectReason: '',
      btnloading: false
    };
  },
  computed: {
    ...mapState('userForm', ['groups']),
    ...mapState('dashboard', ['userList']),
    ...mapState('jobForm', ['codeApproveList']),
    ...mapState('userActionSaveHomePage/myCodeList', [
      'visibleCols',
      'currentTodoPage',
      'searchTaskName',
      'searchProposerList',
      'searchGroupList'
    ])
  },
  filters: {
    stageColorFilter(val) {
      return taskStageColorMap[val];
    },
    stageFilter(val) {
      return taskStage[val];
    },
    filterReason(val) {
      return relReasonMap[val];
    }
  },
  watch: {
    pagination: {
      handler(val) {
        this.saveCurrentTodoPage(val);
      },
      deep: true
    }
  },
  methods: {
    ...mapActions('jobForm', [
      'passApprove',
      'refuseApprove',
      'releaseApproveList'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('dashboard', ['queryUserCoreData']),
    ...mapMutations('userActionSaveHomePage/myCodeList', [
      'saveVisibleColumns',
      'saveGroupList',
      'saveProposerList',
      'saveTaskName',
      'saveCurrentTodoPage'
    ]),
    handleSearch() {
      this.queryTodoList();
    },
    clearGroup() {
      this.saveGroupList([]);
      this.queryTodoList();
    },
    clearPerson() {
      this.saveProposerList([]);
      this.queryTodoList();
    },
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
    async conformReject() {
      try {
        this.btnloading = true;
        await this.refuseApprove({
          id: this.rejectId,
          resultDesc: this.approveRejectReason
        });
        this.rejectDialog = false;
        this.btnloading = false;
        successNotify('审批拒绝成功!');
        //刷新列表
        this.queryTodoList();
      } catch (err) {
        this.btnloading = false;
      }
    },
    //点击拒绝审批按钮
    rejectApprove(row) {
      this.rejectDialog = true;
      this.rejectId = row.id;
    },
    //审批通过
    async passApproveClick(row) {
      await this.passApprove({ id: row.id });
      successNotify('审批通过!');
      //刷新列表
      this.queryTodoList();
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
      this.queryTodoList();
    },
    async queryTodoList() {
      this.loading = true;
      let groupIdList =
        this.searchGroupList.length > 0
          ? this.searchGroupList.map(val => val.id)
          : this.searchGroupList;
      let proposerList =
        this.searchProposerList.length > 0
          ? this.searchProposerList.map(val => val.id)
          : this.searchProposerList;
      let params = {
        pageSize: this.pagination.rowsPerPage,
        currentPage: this.pagination.page,
        group: groupIdList,
        status: [0],
        applicant: proposerList,
        taskName: this.searchTaskName,
        env: 'uat'
      };
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
    init() {
      this.fetchGroup();
      this.queryUserCoreData({ status: '0' });
    }
  },
  created() {
    this.pagination = this.currentTodoPage;
    this.loading = true;
    this.queryTodoList();
    this.loading = false;
    this.init();
  },
  mounted() {
    const tempVisibleColumns = this.visibleCols;
    if (!this.visibleCols || this.visibleCols.length <= 1) {
      this.saveVisibleColumns(tempVisibleColumns);
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
