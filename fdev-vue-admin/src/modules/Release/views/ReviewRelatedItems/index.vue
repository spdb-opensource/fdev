<template>
  <f-block>
    <Loading :visible="loading" class="bg-white">
      <fdev-table
        titleIcon="list_s_f"
        :columns="columns"
        :pagination.sync="pagination"
        ref="table"
        title="数据库变更审核列表"
        :data="tableData"
        @request="init"
        :rows-per-page-options="[5, 10, 20, 30, 40, 50]"
        :visible-columns="visibleColumns"
      >
        <template v-slot:top-right>
          <fdev-btn
            normal
            ficon="add"
            label="新增申请"
            @click="addExamineDialog"
          />
        </template>
        <template v-slot:top-bottom>
          <f-formitem
            class="col-4 q-pr-sm"
            label-style="width:110px"
            bottom-page
            label="任务、应用名，任务负责人、审核人"
          >
            <fdev-input
              :value="listModel.key"
              @keyup.enter="filterWithData"
              @input="updateListModelKey($event)"
            >
              <template v-slot:append>
                <f-icon
                  name="search"
                  class="text-primary cursor-pointer"
                  @click="filterWithData"
                />
              </template>
            </fdev-input>
          </f-formitem>

          <f-formitem
            class="col-4 q-pr-sm"
            label-style="width:110px"
            bottom-page
            label="申请人"
          >
            <fdev-select
              use-input
              @filter="filterUser"
              :options="userOptions"
              option-label="user_name_cn"
              option-value="user_name_en"
              :value="listModel.applicant"
              @input="updateListModelApplicant($event)"
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
            class="col-4 q-pr-sm"
            label-style="width:110px"
            bottom-page
            label="小组"
          >
            <fdev-select
              use-input
              @filter="filterGroup"
              :options="groupOptions"
              option-label="fullName"
              option-value="id"
              map-options
              emit-value
              :value="listModel.group"
              @input="updateListModelGroup($event)"
            >
            </fdev-select>
          </f-formitem>

          <f-formitem
            class="col-4 q-pr-sm"
            label-style="width:110px"
            bottom-page
            label="状态"
          >
            <fdev-select
              :display-value="
                !!listModel.reviewStatus ? listModel.reviewStatus : '全部'
              "
              :value="listModel.reviewStatus"
              @input="updateListModelReviewStatus($event)"
              :options="reviewOptions"
              options-dense
            />
          </f-formitem>
        </template>
        <template v-slot:body-cell-taskName="props">
          <fdev-td auto-width class="text-ellipsis">
            <router-link
              :title="props.value"
              :to="{
                path:
                  props.row.stock === TaskStatus.deStock
                    ? `/release/ReviewDetails/${props.row.taskId}`
                    : `/job/list/${props.row.taskId}`
              }"
              class="link"
            >
              <span>{{ props.value }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>
        <template v-slot:body-cell-appName="props">
          <fdev-td auto-width class="text-ellipsis">
            <router-link
              :title="props.value"
              :to="{
                path: `/app/list/${props.row.appId}`
              }"
              class="link"
            >
              <span>{{ props.value }}</span>
            </router-link>
          </fdev-td>
        </template>
        <template v-slot:body-cell-group="props">
          <fdev-td
            :title="findGroupFullName(props.value)"
            class="text-ellipsis"
          >
            {{ findGroupFullName(props.value) }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ findGroupFullName(props.value) }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>
        <template v-slot:body-cell-applicantName="props">
          <fdev-td class="text-ellipsis">
            <router-link
              :title="props.value"
              v-if="props.row.applicant"
              :to="{
                path: `/user/list/${props.row.applicant}`
              }"
              class="link"
            >
              {{ props.value }}
            </router-link>
            <span :title="props.value" v-else>
              {{ props.value || '-' }}
            </span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-reviewers="props">
          <fdev-td class="text-ellipsis">
            <span
              v-for="(user, index) in props.value.slice(-2)"
              :key="index"
              class="q-mr-sm"
            >
              <router-link
                :title="user.name"
                :to="`/user/list/${user.cid}`"
                v-if="user.cid"
                class="link"
              >
                {{ user.name }}
              </router-link>
              <span :title="user.name" v-else>{{ user.name }}</span>
            </span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-master="props">
          <fdev-td class="text-ellipsis">
            <span
              v-for="(user, index) in props.value.slice(-2)"
              :key="index"
              class="q-mr-sm"
            >
              <router-link
                :title="user.name"
                :to="`/user/list/${user.cid}`"
                v-if="user.cid"
                class="link"
              >
                {{ user.name }}
              </router-link>
              <span v-else>{{ user.name || '-' }}</span>
            </span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-reviewIdea="props">
          <fdev-td auto-width :title="props.value" class="text-ellipsis">
            {{ props.value || '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-btn="props">
          <fdev-td auto-width class="text-ellipsis">
            <div class="q-gutter-sm row items-center no-wrap">
              <span>
                <fdev-tooltip
                  v-if="
                    props.row.reviewStatus !== AuditStatus.archived &&
                      canEdit(props.row.applicant, props.row.reviewStatus)
                  "
                  >{{
                    props.row.applicant === currentUser.id
                      ? '任务审核中，不可编辑'
                      : '该任务仅限本人修改'
                  }}</fdev-tooltip
                >
                <fdev-btn
                  label="编辑"
                  @click="editExamineDialog(props.row.taskId)"
                  :disable="
                    canEdit(props.row.applicant, props.row.reviewStatus)
                  "
                  flat
                />
              </span>
              <fdev-btn label="详情" flat @click="handleDetail(props.row)" />
            </div>
          </fdev-td>
        </template>
      </fdev-table>
      <ExamineDialog
        ref="examineDialog"
        v-model="OptimizeDialogOpen"
        :isAdd="isAdd"
        @handleExamine="handleExamine"
      />
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import ExamineDialog from '../../Components/ExamineDialog';
import { mapActions, mapState, mapGetters, mapMutations } from 'vuex';
import { filtersKey } from '../../utils/model';
import { required } from 'vuelidate/lib/validators';
import { reviewListColumns } from '../../utils/constants';
import {
  createExamineModel,
  Prompt,
  AuditStatus,
  TaskStatus
} from '../../utils/model.js';
import { successNotify } from '@/utils/utils';

export default {
  name: 'ReviewList',
  components: { Loading, ExamineDialog },
  data() {
    return {
      OptimizeDialogOpen: false,
      AuditStatus,
      TaskStatus,
      taskId: '',
      tableData: [],
      userOptions: [],
      groupOptions: [],
      detail: {},
      selectedData: {},
      loading: false,
      examineDialogOpened: false,
      filterListModel: {},
      pagination: {},
      refusedReason: '',
      columns: reviewListColumns,
      queryString: { name: 'luo', age: 18 },
      reviewReason: '',
      okDialog: false,
      refuseDialog: false,
      reviewOptions: [
        '全部',
        '待审核',
        '初审中',
        '初审拒绝',
        '复审中',
        '复审拒绝',
        '通过',
        '已归档'
      ],
      statueFilterVal: '全部',
      validations: {
        reviewReason: {
          required
        }
      },
      isAdd: true // 判断该任务是否为新增   true为新增 false为编辑
    };
  },
  watch: {
    'listModel.group'(val) {
      if (val != this.currentUser.group.id) {
        this.updateIsGroupChange('1');
        this.updateListModelGroup(val);
      }
      this.filterWithData();
    },
    'listModel.applicant'(val) {
      this.filterWithData();
    },
    'listModel.reviewStatus'(val) {
      this.filterWithData();
    },
    pagination(val) {
      this.updateCurrentPage(val);
    }
  },
  filters: {
    filtersKey(val) {
      return filtersKey[val];
    }
  },
  computed: {
    ...mapState('userActionSaveRelease/reviewRelatedItems', [
      'listModel',
      'visibleColumns',
      'currentPage',
      'isGroupChange'
    ]),
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', ['groups']),
    ...mapState('releaseForm', [
      'reviewRecord',
      'examineList',
      'reviewBasicMsg'
    ]),
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapState('jobForm', ['reviewRecordDetail', 'secondReviewers']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    isManager() {
      return this.currentUser.role.some(role => role.label === 'DBA审核人');
    },
    isSecondReviewer() {
      return !!this.secondReviewers.find(
        user => user.id === this.currentUser.id
      );
    },
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(0, columns.length - 1);
    }
  },
  methods: {
    ...mapMutations('userActionSaveRelease/reviewRelatedItems', [
      'updateListModelKey',
      'updateListModelApplicant',
      'updateListModelGroup',
      'updateListModelReviewStatus',
      'updateVisibleColumns',
      'updateCurrentPage',
      'updateIsGroupChange'
    ]),
    ...mapActions('user', ['fetch']),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('releaseForm', [
      'queryReviewRecord',
      'updateReviewRecord',
      'queryReviewBasicMsg'
    ]),
    ...mapActions('jobForm', ['queryReviewRecordStatus']),
    filter() {},
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
    filterGroup(val, update, abort) {
      update(() => {
        this.groupOptions = this.groups.filter(
          group => group.fullName.indexOf(val) > -1
        );
      });
    },
    async filterWithData() {
      const { applicant, group, key, reviewStatus } = this.listModel;
      this.filterListModel = {
        key,
        applicant: applicant ? applicant.id : null,
        group: group,
        reviewStatus: reviewStatus === '全部' ? '' : reviewStatus
      };
      Object.keys(this.filterListModel).forEach(key => {
        if (!this.filterListModel[key]) delete this.filterListModel[key];
      });
      await this.init();
    },
    findGroupFullName(id) {
      const group = this.groups.find(group => group.id === id);
      return group ? group.fullName : '';
    },
    async init(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      this.loading = true;
      await this.queryReviewRecord({
        ...this.filterListModel,
        page: this.pagination.page,
        pageSize: this.pagination.rowsPerPage
      });
      this.pagination.rowsNumber = this.reviewRecord.sum;
      this.tableData = this.reviewRecord.record;
      this.loading = false;
    },
    handleDetail(row) {
      this.$router.push({
        path: `/release/ReviewDetails/${row.taskId}`,
        query: {
          groupFullName: this.findGroupFullName(row.group)
        }
      });
    },
    canEdit(applicant, status) {
      if (applicant === this.currentUser.id) {
        // 状态为'通过'，'初审拒绝'，'复审拒绝'的任务可编辑
        return !(
          status === AuditStatus.pass ||
          status === AuditStatus.firstReviewReject ||
          status === AuditStatus.seconedReviewReject
        );
      } else {
        return true;
      }
    },
    async editExamineDialog(id) {
      this.isAdd = false;
      // 根据任务id查询审核基本信息
      await this.queryReviewBasicMsg({ taskId: id });
      // stock 用于判断该任务是否为存量数据   ‘0’表示从数据库变更审核页面添加的数据，‘1’表示存量数据，即从任务处理页面添加的数据
      this.$refs.examineDialog.stock = this.reviewBasicMsg.stock;
      this.$refs.examineDialog.reviewData = this.reviewBasicMsg;
      // 回显最后一次指派的初审人或复审人
      let reviewersArr = this.reviewBasicMsg.reviewers;
      this.$refs.examineDialog.reviewData.reviewers =
        reviewersArr[reviewersArr.length - 1].cid;
      this.OptimizeDialogOpen = true;
    },
    addExamineDialog() {
      this.isAdd = true;
      this.$refs.examineDialog.reviewData = createExamineModel();
      this.$refs.examineDialog.reviewData.group = this.currentUser.group.fullName;
      this.$refs.examineDialog.reviewData.applicantName = this.currentUser.user_name_cn;
      this.OptimizeDialogOpen = true;
    },
    async handleExamine(add) {
      this.OptimizeDialogOpen = false;
      if (add) {
        successNotify(Prompt.addedSuccessfully);
      } else {
        successNotify(Prompt.modifiedSuccessfully);
      }
      await this.init();
    }
  },
  async created() {
    this.pagination = this.currentPage;
    await this.fetchGroup();
    await this.fetch();
    this.userOptions = this.userList;
    this.groupOptions = this.groups;
    //组是否改变 0-不变   1-改变
    if (this.isGroupChange === '0') {
      this.updateListModelGroup(this.currentUser.group.id);
    }
    this.filterWithData();
  }
};
</script>

<style lang="stylus" scoped></style>
