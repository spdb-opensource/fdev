<template>
  <Loading :visible="loading">
    <fdev-table
      :data="tableData"
      :columns="columns"
      row-key="id"
      :pagination.sync="pagination"
      title-icon="check_s_f"
      title="审批已完成列表"
      class="my-sticky-column-table"
      :visible-columns="visibleColsDone"
      :onSelectCols="saveVisibleColumnsDone"
      @request="pageTableRequest"
      :on-search="findMyApproveList"
      :export-func="handleExportExcel"
    >
      <template v-slot:top-bottom>
        <!--日期选择  -->
        <f-formitem
          label="日期选择"
          class="col-4 q-pr-md"
          label-style="width:80px"
          bottom-page
        >
          <f-date v-model="doneParams.time" mask="YYYY-MM-DD" />
        </f-formitem>
        <!-- 所属小组 -->
        <f-formitem
          label="所属小组"
          class="col-4 q-pr-md"
          label-style="width:80px"
          bottom-page
        >
          <fdev-select
            use-input
            ref="groups"
            :value="doneParams.groups"
            @input="updateDoneGroup($event)"
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
          label-style="width:80px"
          bottom-page
        >
          <fdev-select
            use-input
            @filter="userFilter"
            :options="userOptions"
            option-label="user_name_cn"
            option-value="id"
            :value="doneParams.apply_user"
            @input="updateDoneApplyUser($event)"
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
        <!-- 审核人 -->
        <f-formitem
          label="审核人"
          class="col-4 q-pr-md"
          label-style="width:80px"
          bottom-page
        >
          <fdev-select
            use-input
            @filter="userFilter"
            :options="userOptions"
            option-label="user_name_cn"
            option-value="id"
            :value="doneParams.approve_user"
            @input="updateDoneApproveUser($event)"
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
          label-style="width:80px"
          bottom-page
        >
          <fdev-input
            :value="doneParams.taskName"
            @input="updateDonetaskName($event)"
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
            <router-link
              v-if="props.row.demandId && props.row.oaContactNo"
              :to="`/rqrmn/rqrProfile/${props.row.demandId}`"
              class="link"
            >
              {{ props.row.oaContactNo }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.oaContactNo }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>{{ props.row.oaContactNo || '-' }}</span>
          </div>
        </fdev-td>
      </template>
      <!-- 所属应用 -->
      <template v-slot:body-cell-applicationNameEn="props">
        <fdev-td class="text-ellipsis">
          <div class="row no-wrap items-center">
            {{ props.row.applicationNameEn }}
          </div>
        </fdev-td>
      </template>
      <!-- 部署环境 -->
      <template v-slot:body-cell-deployEnv="props">
        <fdev-td>
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
      <!-- 申请原因 -->
      <template v-slot:body-cell-overdueReason="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.value">
            {{ props.row.overdueReason }}
          </div>
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
      <!-- 操作管理员 -->
      <template v-slot:body-cell-reviewUserNameZh="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.value">
            <span>
              <router-link
                v-if="props.row.reviewUserId"
                :to="{ path: `/user/list/${props.row.reviewUserId}` }"
                class="link"
              >
                {{ props.value }}
              </router-link>
              <span v-else class="span-margin">{{ props.value }}</span>
            </span>
          </div>
        </fdev-td>
      </template>
      <!-- 操作时间 -->
      <template v-slot:body-cell-reviewTime="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.value">
            {{ props.row.reviewTime }}
          </div>
        </fdev-td>
      </template>
      <!-- 部署状态 -->
      <template v-slot:body-cell-deploy_status="props">
        <fdev-td class="text-ellipsis">
          <div class="row no-wrap items-center">
            <f-status-color
              :gradient="
                approveStatus(props.row.deploy_status) | approveStatusFilter
              "
            ></f-status-color>

            <span :title="approveStatus(props.row.deploy_status)">
              {{ approveStatus(props.row.deploy_status) || '-' }}
            </span>
          </div>
        </fdev-td>
      </template>

      <!-- 操作列 -->
      <template v-slot:body-cell-operation="props">
        <fdev-td :auto-width="true">
          <fdev-btn
            flat
            label="重新部署"
            class="q-mr-sm"
            :loading="btnLoading"
            :disable="cantReDeploy(props.row)"
            @click="reDeploy(props.row.id)"
          />
          <fdev-tooltip v-if="cantReDeploy(props.row)">不可操作!</fdev-tooltip>
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { doneSit2ApprovalColumns } from '@/modules/HomePage/utils/constants';
import {
  deepClone,
  resolveResponseError,
  successNotify,
  exportExcel
} from '@/utils/utils';
import {
  queryAppDeploy,
  deployApps,
  exportDeployTask
} from '@/services/job.js';
export default {
  name: 'sit2ApprovalsDone',
  components: { Loading },
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: {
        page: 1, //页码
        rowsPerPage: 5, //每页数据大小
        rowsNumber: 0
      },
      rowSelected: [],
      columns: doneSit2ApprovalColumns(),
      eventDoneNum: 0,
      eventNum: {
        todo: 0,
        done: 0
      },
      deepCloneGroups: [],
      groupOptions: [],
      userOptions: [],
      btnLoading: false
    };
  },

  props: ['label', 'name'],
  watch: {
    pagination: {
      handler(val) {
        this.saveCurrentDonePage(val);
      },
      deep: true
    },
    'doneParams.time'(val) {
      this.init();
    },
    'doneParams.groups'(val) {
      this.init();
    },
    'doneParams.apply_user'(val) {
      this.init();
    },
    'doneParams.approve_user'(val) {
      this.init();
    }
  },
  computed: {
    ...mapState('userActionSaveHomePage/mySit2Page', [
      'doneParams',
      'visibleColsDone',
      'currentDonePage'
    ]),
    ...mapState('userForm', {
      groupsData: 'groups'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('dashboard', ['userList'])
  },
  filters: {
    //部署状态
    approveStatusFilter(val) {
      const obj2 = {
        未部署:
          'linear-gradient(270deg, rgba(36,200,249,0.50) 0%, #24C8F9 100%)',
        部署失败:
          'linear-gradient(270deg, rgba(239,114,113,0.50) 0%, #db3b21 100%)',
        部署成功:
          'linear-gradient(270deg, rgba(124,196,127,0.50) 0%, #1aaa55 100%)',
        部署中:
          'linear-gradient(270deg, rgba(176,190,197,0.50) 0%, #B0BEC5 100%)'
      };
      return obj2[val];
    }
  },
  methods: {
    ...mapMutations('userActionSaveHomePage/mySit2Page', [
      'updateDoneGroup',
      'updateDoneApplyUser',
      'updateDoneApproveUser',
      'updateDonetaskName',
      'saveVisibleColumnsDone',
      'saveCurrentDonePage'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('dashboard', ['queryUserCoreData']),

    approveStatus(status) {
      const obj1 = {
        0: '未部署',
        1: '部署失败',
        2: '部署成功',
        3: '部署中'
      };
      return obj1[status];
    },
    cantReDeploy(row) {
      return (
        (row.deploy_status && row.deploy_status !== '1') ||
        this.currentUser.deploy_white !== '0'
      );
    },
    //清空搜索条件
    clearTerms() {
      this.updateDonetaskName('');
      this.init();
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
    //小组过滤
    groupInputFilter(val, update) {
      update(() => {
        this.groupOptions = this.deepCloneGroups.filter(tag =>
          tag.fullName.includes(val)
        );
      });
    },

    //支持导出
    async handleExportExcel() {
      let params = {
        pageNum: this.pagination.rowsPerPage, //查询条数
        page: this.pagination.page, //页码
        queryType: '1', //0:未审批 1:已审批
        groupId: this.doneParams.groups ? this.doneParams.groups.id : '', //所属小组
        userId: this.doneParams.apply_user ? this.doneParams.apply_user.id : '', //申请人
        reviewTime: this.doneParams.time, //日期
        taskName: this.doneParams.taskName,
        reviewUserId: this.doneParams.approve_user
          ? this.doneParams.approve_user.id
          : '' //审核人
      };
      let result = await resolveResponseError(() => exportDeployTask(params));
      exportExcel(result);
    },

    //重新部署
    async reDeploy(id) {
      try {
        this.btnLoading = true;
        await resolveResponseError(() =>
          deployApps({
            ids: [id],
            userId: this.currentUser.id,
            deployType: '1' //部署类型：0：批量部署1：重新部署
          })
        );
        successNotify('操作成功!');
        this.btnLoading = false;
        //刷新列表
        this.init();
      } catch (er) {
        this.btnLoading = false;
      }
    },

    //查找表格数据
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

    // 初始化
    async init() {
      this.loading = true;
      let params = {
        pageNum: this.pagination.rowsPerPage, //查询条数
        page: this.pagination.page, //页码
        queryType: '1', //0:未审批 1:已审批
        groupId: this.doneParams.groups ? this.doneParams.groups.id : '', //所属小组
        userId: this.doneParams.apply_user ? this.doneParams.apply_user.id : '', //申请人
        reviewTime: this.doneParams.time, //日期
        taskName: this.doneParams.taskName,
        reviewUserId: this.doneParams.approve_user
          ? this.doneParams.approve_user.id
          : '' //审核人
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
    this.pagination = this.currentDonePage;
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
    const tempVisibleColumns = this.visibleColsDone;
    if (!this.visibleCols || this.visibleColsDone.length <= 1) {
      this.saveVisibleColumnsDone(tempVisibleColumns);
    }
  }
};
</script>

<style lang="stylus" scoped>
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
