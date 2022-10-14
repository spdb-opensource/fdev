<template>
  <Loading :visible="loading">
    <fdev-table
      :data="tableData"
      :columns="columns"
      row-key="id"
      no-export
      selection="multiple"
      :selected.sync="rowSelected"
      :pagination.sync="pagination"
      @request="pageTableRequest"
      :on-search="findMyApproveList"
      title-icon="todo_list_s_f"
      title="sit2环境部署申请审批列表"
      :visible-columns="visibleCols"
      :on-select-cols="saveVisibleColumns"
    >
      <!-- 搜索条件 -->
      <template v-slot:top-bottom>
        <!-- 所属小组 -->
        <f-formitem
          label="所属小组"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:80px"
        >
          <fdev-select
            use-input
            ref="groups"
            :value="approvalParams.groups"
            @input="updateGroupIds($event)"
            :options="groupOptions"
            @filter="groupInputFilter"
            option-label="name"
            option-value="label"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name">{{
                    scope.opt.name
                  }}</fdev-item-label>
                  <fdev-item-label :title="scope.opt.fullName" caption>
                    {{ scope.opt.fullName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <!-- 申请人 -->
        <f-formitem
          label="申请人"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:80px"
        >
          <fdev-select
            use-input
            @filter="userFilter"
            :options="userOptions"
            option-label="user_name_cn"
            option-value="id"
            :value="approvalParams.apply_user"
            @input="updateApplyUser($event)"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">{{
                    scope.opt.user_name_cn
                  }}</fdev-item-label>
                  <fdev-item-label
                    caption
                    :title="`${scope.opt.user_name_en}--${scope.opt.groupName}`"
                  >
                    {{ scope.opt.user_name_en }}--{{ scope.opt.groupName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <!-- 任务名称 -->
        <f-formitem
          label="任务名称"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:80px"
        >
          <fdev-input
            :value="approvalParams.taskName"
            @input="updatetaskName($event)"
            placeholder="请输入任务名称"
            clearable
            @keyup.enter="findMyApproveList()"
            @clear="clearTerms()"
            ><template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                @click="findMyApproveList"
              />
            </template>
          </fdev-input>
        </f-formitem>
      </template>
      <!-- 任务名称 -->
      <template v-slot:body-cell-taskName="props">
        <fdev-td :title="props.row.taskName" class="text-ellipsis">
          <router-link :to="`/job/list/${props.row.taskId}`" class="link">
            {{ props.row.taskName }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.taskName }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
        </fdev-td>
      </template>
      <!-- 需求名称跳转 -->
      <template v-slot:body-cell-oaContactName="props">
        <fdev-td :title="props.row.oaContactName">
          <div class="text-ellipsis">
            <router-link
              v-if="props.row.demandId && props.row.oaContactName"
              :to="`/rqrmn/rqrProfile/${props.row.demandId}`"
              class="link"
            >
              {{ props.row.oaContactName }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.oaContactName }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>{{ props.row.oaContactName || '-' }}</span>
          </div>
        </fdev-td>
      </template>
      <!-- 需求编号跳转 -->
      <template v-slot:body-cell-oaContactNo="props">
        <fdev-td :title="props.row.oaContactNo">
          <div class="text-ellipsis">
            {{ props.row.oaContactNo || '-' }}
          </div>
        </fdev-td>
      </template>
      <!--所属应用 -->
      <template v-slot:body-cell-applicationNameEn="props">
        <fdev-td class="text-ellipsis" :title="props.row.applicationNameEn"
          >{{ props.row.applicationNameEn }}
        </fdev-td>
      </template>
      <!-- 部署环境 -->
      <template v-slot:body-cell-deployEnv="props">
        <fdev-td :title="props.row.deployEnv">
          <div class="text-ellipsis">
            {{ props.row.deployEnv || '-' }}
          </div>
        </fdev-td>
      </template>
      <!-- 申请人 -->
      <template v-slot:body-cell-applayUserNameZh="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.value">
            <span>
              <router-link
                v-if="props.row.applayUserId"
                :to="{ path: `/user/list/${props.row.applayUserId}` }"
                class="link"
              >
                {{ props.value }}
              </router-link>
              <span v-else class="span-margin">{{ props.value }}</span>
            </span>
          </div>
        </fdev-td>
      </template>
      <!-- 申请原因-->
      <template v-slot:body-cell-overdueReason="props">
        <fdev-td class="text-ellipsis" :title="props.value">
          {{ props.row.overdueReason || '-' }}
        </fdev-td>
      </template>
      <!-- 所属小组 -->
      <template v-slot:body-cell-applayUserOwnerGroup="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.value">
            {{ props.row.applayUserOwnerGroup }}
          </div>
        </fdev-td>
      </template>
    </fdev-table>
    <!-- 确认部署按钮 -->
    <div class="text-center">
      <fdev-tooltip v-if="rowSelected.length === 0" position="top">
        请先勾选需要审批的任务
      </fdev-tooltip>
      <fdev-btn
        :disable="rowSelected.length === 0"
        label="确认部署"
        :loading="allAprovePassLoading"
        @click="handleApprover"
        v-if="isDeployWhite"
      />
    </div>
    <!-- 提示弹窗 -->
    <f-dialog
      v-model="showDialog"
      title="同一应用下部署申请都将一并被处理，部署的任务申请列表如下："
    >
      <fdev-table
        :columns="taskColumns"
        :data="taskListData"
        no-export
        noSelectCols
        style="max-height:64vh;width:40vw"
        class="dialogTable"
      >
        <template v-slot:body-cell-taskName="props">
          <fdev-td :props="props">
            <div :title="props.row.taskName" class="text-ellipsis wd100">
              {{ props.row.taskName }}
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-applicationNameEn="props">
          <fdev-td :props="props">
            <div
              :title="props.row.applicationNameEn"
              class="text-ellipsis wd100"
            >
              {{ props.row.applicationNameEn }}
            </div>
          </fdev-td>
        </template>
      </fdev-table>

      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="showDialog = false"/>
        <fdev-btn label="确定" dialog v-close-popup @click="allAprovePass"
      /></template>
    </f-dialog>
  </Loading>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { successNotify, deepClone, resolveResponseError } from '@/utils/utils';
import { createSitApprovalColumns } from '@/modules/HomePage/utils/constants';
import { queryAppDeploy, deployApps, queryDeployTask } from '@/services/job.js';
export default {
  name: 'sit2Approvals',
  components: { Loading },
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      },
      rowSelected: [],
      columns: createSitApprovalColumns(),
      eventTodoNum: 0,
      eventNum: {
        todo: 0,
        done: 0
      },
      allAprovePassLoading: false, //批量部署缓冲
      deepCloneGroups: [],
      groupOptions: [],
      userOptions: [],
      showDialog: false, //提示弹窗
      taskColumns: [
        {
          name: 'taskName',
          align: 'left',
          field: 'taskName',
          label: '任务名称'
        },
        {
          name: 'applicationNameEn',
          align: 'left',
          field: 'applicationNameEn',
          label: '应用名'
        }
      ],
      taskListData: []
    };
  },
  props: ['label', 'name'],

  watch: {
    pagination: {
      handler(val) {
        this.saveCurrentTodoPage(val);
      },
      deep: true
    },
    'approvalParams.groups'(val) {
      this.init();
    },
    'approvalParams.apply_user'(val) {
      this.init();
    }
  },

  computed: {
    ...mapState('userActionSaveHomePage/mySit2Page', [
      'approvalParams',
      'visibleCols',
      'currentTodoPage'
    ]),
    ...mapState('userForm', {
      groupsData: 'groups'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('dashboard', ['userList']),
    isDeployWhite() {
      return this.currentUser.deploy_white === '0'; //'0'是白名单用户，'1'不是
    }
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('dashboard', ['queryUserCoreData']),
    ...mapMutations('userActionSaveHomePage/mySit2Page', [
      'saveVisibleColumns',
      'saveCurrentTodoPage',
      'updateGroupIds',
      'updateApplyUser',
      'updatetaskName'
    ]),
    //清空任务名搜索条件
    clearTerms() {
      this.updatetaskName('');
      this.init();
    },
    //小组过滤
    groupInputFilter(val, update) {
      update(() => {
        this.groupOptions = this.deepCloneGroups.filter(tag =>
          tag.fullName.includes(val)
        );
      });
    },
    //申请人过滤
    userFilter(val, update, abort) {
      update(() => {
        this.userOptions = this.users.filter(
          user =>
            user.user_name_cn.includes(val) || user.user_name_en.includes(val)
        );
      });
    },

    //部署提示弹窗
    async handleApprover() {
      this.showDialog = true;
      let result = await resolveResponseError(() =>
        queryDeployTask({
          appNames: [
            ...new Set(this.rowSelected.map(item => item.applicationNameEn))
          ].join(',')
        })
      );
      this.taskListData = result;
    },
    // 批量部署
    async allAprovePass() {
      this.showDialog = false;
      this.allAprovePassLoading = true;
      try {
        await resolveResponseError(() =>
          deployApps({
            ids: this.rowSelected.map(item => item.id),
            userId: this.currentUser.id,
            deployType: '0' //部署类型：0：批量部署1：重新部署
          })
        );
        this.allAprovePassLoading = false;
        //刷新列表
        this.init();
        successNotify('批量审批通过!');
      } catch (er) {
        this.allAprovePassLoading = false;
      }
    },

    //查询列表数据
    findMyApproveList() {
      this.init();
    },
    //翻页
    pageTableRequest(props) {
      let { page, rowsPerPage, rowsNumber } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.init();
    },
    //初始化
    async init() {
      this.loading = true;
      this.rowSelected = [];
      let params = {
        pageNum: this.pagination.rowsPerPage,
        page: this.pagination.page,
        queryType: '0',
        groupId: this.approvalParams.groups
          ? this.approvalParams.groups.id
          : '', //所属小组
        taskName: this.approvalParams.taskName,
        userId: this.approvalParams.apply_user
          ? this.approvalParams.apply_user.id
          : '' //申请人
      };
      let result = await resolveResponseError(() => queryAppDeploy(params));

      // 设置数据总条数
      this.pagination.rowsNumber = result.total;
      this.tableData = result.list;
      this.eventNum.todo = result.notApprovTotal ? result.notApprovTotal : 0;
      this.eventNum.done = result.approvTotal ? result.approvTotal : 0;
      this.$emit('input', this.eventNum);
      this.loading = false;
    }
  },
  created() {
    this.pagination = this.currentTodoPage;
    this.init();
    // 获取小组和用户
    Promise.all([
      this.fetchGroup(),
      this.queryUserCoreData({ status: '0' })
    ]).then(res => {
      // 处理小组数据
      this.deepCloneGroups = deepClone(this.groupsData);
      this.groupOptions = this.deepCloneGroups;
      // 处理用户数据
      this.users = this.userList;
      this.userOptions = this.users.slice(0);
      this.approverIdsOptions = this.users.slice(0);
    });
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
/deep/.full-width .content-standard
  padding: 0px 32px 0px;

.dialogTable
  /deep/.q-table__bottom .row .items-center .justify-end
    margin-top:0px!important
</style>
