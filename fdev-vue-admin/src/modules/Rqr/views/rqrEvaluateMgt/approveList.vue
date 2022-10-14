<template>
  <Loading :visible="loading">
    <fdev-table
      :data="tableData"
      :columns="columns"
      row-key="id"
      :pagination.sync="pagination"
      :visible-columns="visibleColumns"
      :onSelectCols="updateVisibleColumns"
      @request="pageTableRequest"
      :loading="loading"
      titleIcon="list_s_f"
      title="定稿日期审核列表"
      class="my-sticky-column-table"
      :on-search="findApproveList"
      no-export
    >
      <template v-slot:top-bottom>
        <f-formitem
          label="需求名称/编号"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:90px"
        >
          <fdev-input
            v-model="approvalParams.oaContactNameNo"
            @keyup.enter="handleEnterFun(approvalParams.oaContactNameNo)"
            clearable
            @clear="clearDemandKeyFun()"
          >
          </fdev-input>
        </f-formitem>
        <f-formitem
          label="牵头小组"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:90px"
        >
          <fdev-select
            use-input
            multiple
            ref="demandLeaderGroups"
            :value="approvalParams.demandLeaderGroups"
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
        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:90px"
          bottom-page
          label="申请人"
        >
          <fdev-select
            use-input
            multiple
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
        <f-formitem
          label="审批状态"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:90px"
        >
          <fdev-select
            ref="approveStates"
            multiple
            :value="approvalParams.approveStates"
            @input="updateApproveStates($event)"
            :options="approveStatusOptions"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:90px"
          bottom-page
          label="审批人"
        >
          <fdev-select
            use-input
            multiple
            @filter="approverIdsFilterUser"
            :options="approverIdsOptions"
            option-label="user_name_cn"
            option-value="id"
            :value="approvalParams.approverIds"
            @input="updateApproverIds($event)"
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
      </template>
      <!-- 需求名称 -->
      <template v-slot:body-cell-oa_contact_name="props">
        <fdev-td :title="props.row.oa_contact_name">
          <div class="text-ellipsis">
            <router-link
              v-if="props.row.access_id && props.row.oa_contact_name"
              :to="`/rqrmn/rqrEvaluateMgt/${props.row.access_id}`"
              class="link"
            >
              {{ props.row.oa_contact_name }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.oa_contact_name }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>{{ props.row.oa_contact_name || '-' }}</span>
          </div>
        </fdev-td>
      </template>
      <!-- 申请原因 -->
      <template v-slot:body-cell-apply_reason="props">
        <fdev-td :title="props.row.apply_reason">
          <div class="text-ellipsis">
            <span
              >{{ props.row.apply_reason || '-'
              }}<fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.apply_reason }}
                </fdev-banner>
              </fdev-popup-proxy></span
            >
          </div>
        </fdev-td>
      </template>
      <!-- 拒绝原因 -->
      <template v-slot:body-cell-state="props">
        <fdev-td :title="props.row.state">
          <div class="text-ellipsis">
            <span
              >{{ props.row.state || '-'
              }}<fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.state }}
                </fdev-banner>
              </fdev-popup-proxy></span
            >
          </div>
        </fdev-td>
      </template>
      <!-- 申请人 -->
      <template v-slot:body-cell-apply_user="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.value">
            <span>
              <router-link
                v-if="props.row.apply_user_id"
                :to="{ path: `/user/list/${props.row.apply_user_id}` }"
                class="link"
              >
                {{ props.value }}
              </router-link>
              <span v-else class="span-margin">{{ props.value }}</span>
            </span>
          </div>
        </fdev-td>
      </template>
      <!-- 审批人 -->
      <template v-slot:body-cell-operate_user="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.value">
            <span>
              <router-link
                v-if="props.row.operate_user_id"
                :to="{ path: `/user/list/${props.row.operate_user_id}` }"
                class="link"
              >
                {{ props.value }}
              </router-link>
              <span v-else class="span-margin">{{ props.value }}</span>
            </span>
          </div>
        </fdev-td>
      </template>
      <!-- 审批状态 -->
      <template v-slot:body-cell-operate_status="props">
        <fdev-td>
          <div class="row no-wrap items-center">
            <f-status-color
              :gradient="
                approveStatus(props.row.operate_status) | approveStatusFilter
              "
            ></f-status-color>

            <span :title="approveStatus(props.row.operate_status)">
              {{ approveStatus(props.row.operate_status) }}
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
import { deepClone } from '@/utils/utils';
export default {
  name: 'approvalList',
  components: { Loading },
  data() {
    return {
      groupOptions: [],
      deepCloneGroups: [],
      columns: [
        {
          name: 'oa_contact_name',
          label: '需求名称',
          field: 'oa_contact_name',
          required: true,
          copy: true
        },
        {
          name: 'oa_contact_no',
          label: '需求编号',
          field: 'oa_contact_no',
          required: true,
          copy: true
        },
        {
          name: 'demand_leader_group_cn',
          label: '牵头小组',
          field: 'demand_leader_group_cn',
          copy: true
        },
        {
          name: 'apply_user',
          label: '申请人',
          field: 'apply_user',
          copy: true
        },
        {
          name: 'apply_reason',
          label: '申请原因',
          field: 'apply_reason',
          copy: true
        },
        {
          name: 'apply_update_time',
          label: '申请定稿日期修改时间',
          field: 'apply_update_time'
        },
        {
          name: 'create_time',
          label: '申请时间',
          field: 'create_time'
        },
        {
          name: 'operate_user',
          label: '审批人',
          field: 'operate_user',
          required: true
        },
        {
          name: 'state',
          label: '拒绝原因',
          field: 'state',
          copy: true
        },
        {
          name: 'operate_time',
          label: '操作时间',
          field: 'operate_time',
          copy: true
        },
        {
          name: 'operate_status',
          label: '审批状态',
          field: 'operate_status',
          required: true
        }
      ],
      approveStatusOptions: [
        { label: '待审批', value: 'undetermined' },
        { label: '通过', value: 'agree' },
        { label: '拒绝', value: 'disagree' }
      ],
      userOptions: [],
      approverIdsOptions: [],
      tableData: [],
      loading: false,
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      },
      searchParams: {} //查询上送的参数
    };
  },
  watch: {
    approvalParams: {
      handler(val) {
        this.queryReUnitApproval();
      },
      deep: true
    }
  },
  computed: {
    ...mapState('userActionSaveDemands/eveluateApprovalList', [
      'visibleColumns',
      'approvalParams'
    ]),
    ...mapState('dashboard', ['userList']),
    ...mapState('demandsForm', ['finalApprovalList']), //接口数据
    ...mapState('userForm', {
      groupsData: 'groups'
    })
  },
  filters: {
    //颜色
    approveStatusFilter(val) {
      const obj2 = {
        待审批:
          'linear-gradient(270deg, rgba(36,200,249,0.50) 0%, #24C8F9 100%)',
        通过: 'linear-gradient(270deg, rgba(77,187,89,0.50) 0%, #4DBB59 100%)',
        拒绝: 'linear-gradient(270deg, rgba(176,190,197,0.50) 0%, #B0BEC5 100%)'
      };
      return obj2[val];
    }
  },
  methods: {
    ...mapMutations('userActionSaveDemands/eveluateApprovalList', [
      'updateVisibleColumns',
      'updatedemandKey',
      'updateGroupIds',
      'updateApplyUser',
      'updateApproveStates',
      'updateApproverIds'
    ]),
    ...mapActions('demandsForm', ['queryRqrApproveList']),
    ...mapActions('dashboard', ['queryUserCoreData']),
    ...mapActions('userForm', ['fetchGroup']),
    // 需求名称/编号回车事件
    handleEnterFun(val) {
      this.updatedemandKey(val);
      this.queryReUnitApproval();
    },
    // 清除需求名称/编号
    clearDemandKeyFun() {
      this.queryReUnitApproval();
    },
    //状态转换
    approveStatus(status) {
      const obj1 = {
        undetermined: '待审批',
        agree: '通过',
        disagree: '拒绝'
      };
      return obj1[status];
    },
    //翻页
    pageTableRequest(props) {
      let { page, rowsPerPage, rowsNumber } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.queryReUnitApproval();
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
    //审批人过滤
    approverIdsFilterUser(val, update, abort) {
      update(() => {
        this.approverIdsOptions = this.users.filter(
          user =>
            user.user_name_cn.includes(val) || user.user_name_en.includes(val)
        );
      });
    },
    //小组
    groupInputFilter(val, update) {
      update(() => {
        this.groupOptions = this.deepCloneGroups.filter(tag =>
          tag.fullName.includes(val)
        );
      });
    },
    findApproveList() {
      this.queryReUnitApproval();
    },
    //查询审批列表
    async queryReUnitApproval() {
      this.loading = true;
      this.searchParams.pageSize = this.pagination.rowsPerPage;
      this.searchParams.pageNum = this.pagination.page;

      this.searchParams.oaContactNameNo = this.approvalParams.oaContactNameNo; //需求编号/名称
      this.searchParams.demandLeaderGroups =
        this.approvalParams.demandLeaderGroups &&
        this.approvalParams.demandLeaderGroups.map(item => item.id); //牵头小组
      this.searchParams.applyUserId =
        this.approvalParams.apply_user &&
        this.approvalParams.apply_user.map(item => item.id); //申请人
      this.searchParams.operateUserId =
        this.approvalParams.approverIds &&
        this.approvalParams.approverIds.map(item => item.id); //审批人
      this.searchParams.operateStatus =
        this.approvalParams.approveStates &&
        this.approvalParams.approveStates.map(item => item.value); //审批状态
      try {
        await this.queryRqrApproveList(this.searchParams);
        //设置数据总条数
        this.pagination.rowsNumber = this.finalApprovalList.count;
        this.tableData = this.finalApprovalList.approveList;
        this.loading = false;
      } catch (er) {
        this.loading = false;
      }
    }
  },
  async created() {
    // 查询列表数据
    await this.queryReUnitApproval(this.Param);
    // 获取小组和用户
    Promise.all([
      this.fetchGroup(),
      this.queryUserCoreData({ status: '0' })
    ]).then(() => {
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
    const tempVisibleColumns = this.visibleColumns;
    if (!this.visibleColumns || this.visibleColumns.length <= 1) {
      this.updateVisibleColumns(tempVisibleColumns);
    }
  }
};
</script>

<style lang="stylus" scoped></style>
