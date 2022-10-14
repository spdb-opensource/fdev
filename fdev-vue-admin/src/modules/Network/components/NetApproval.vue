<template>
  <f-block>
    <Loading :visible="loading">
      <div class="bg-white">
        <fdev-table
          titleIcon="list_s_f"
          :data="list"
          :columns="columns"
          selection="multiple"
          :selected.sync="rowSelected"
          row-key="id"
          :pagination.sync="pagination"
          @request="query"
          :title="tableTitle"
          :visible-columns="visibleColumns"
          class="my-sticky-column-table"
          :on-search="query"
        >
          <!-- <template v-slot:top-right>
            <fdev-btn
              class="q-ml-md btn-height"
              normal
              icon="add"
              :label="btnLabel"
              @click="isNetwork"
            />
          </template> -->
          <template v-slot:top-bottom>
            <f-formitem
              class="col-4 q-pr-sm"
              label-style="width:100px"
              bottom-page
              label="申请人"
            >
              <fdev-select
                use-input
                multiple
                @filter="filterUser"
                :options="userOptions"
                option-label="user_name_cn"
                option-value="id"
                :value="applicant_id"
                @input="queryApplicantId($event)"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.user_name_cn">{{
                        scope.opt.user_name_cn
                      }}</fdev-item-label>
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
              label-style="width:100px"
              bottom-page
              label="使用人公司"
            >
              <fdev-select
                use-input
                :value="company"
                @input="queryCompany($event)"
                :options="companyOptions"
                option-label="name"
                option-value="id"
              >
              </fdev-select>
            </f-formitem>
            <f-formitem
              class="col-4 q-pr-sm"
              label-style="width:100px"
              bottom-page
              label="申请状态"
            >
              <fdev-select
                use-input
                :value="status"
                @input="queryStatus($event)"
                :options="statusOptions"
              >
              </fdev-select>
            </f-formitem>
            <f-formitem
              class="col-4 q-pr-sm"
              label-style="width:100px"
              bottom-page
              label="申请开始日期"
            >
              <f-date
                mask="YYYY/MM/DD"
                @input="queryStartTime($event)"
                :options="startOptions"
                :value="startTime"
              />
            </f-formitem>
            <f-formitem
              class="col-4 q-pr-sm"
              label-style="width:100px"
              bottom-page
              label="申请结束日期"
            >
              <f-date
                mask="YYYY/MM/DD"
                @input="queryEndTime($event)"
                :options="endOptions"
                :value="endTime"
              />
            </f-formitem>
          </template>
          <template v-slot:body-cell-applicant="props">
            <fdev-td class="text-ellipsis">
              <router-link
                v-if="props.row.applicant"
                :to="{
                  path: `/user/list/${props.row.applicant.id}`
                }"
                class="link"
                :title="props.row.applicant.user_name_cn"
              >
                {{ props.row.applicant.user_name_cn }}
              </router-link>
              <span :title="props.row.applicant.user_name_cn" v-else>
                {{ props.row.applicant.user_name_cn || '-' }}
              </span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-user="props">
            <fdev-td class="text-ellipsis">
              <router-link
                :title="props.row.user.user_name_cn"
                v-if="props.row.user"
                :to="{
                  path: `/user/list/${props.row.user.id}`
                }"
                class="link"
              >
                {{ props.row.user.user_name_cn }}
              </router-link>
              <span :title="props.row.user.user_name_cn" v-else>
                {{ props.row.user.user_name_cn || '-' }}
              </span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-is_spdb="props">
            <td :title="props.value === true ? '行内' : '厂商'">
              {{ props.value === true ? '行内' : '厂商' }}
            </td>
          </template>
          <template v-slot:body-cell-vm_user_name="props">
            <td
              :title="
                props.row.user.email.indexOf('spdbdev.com') > 0
                  ? 'dev\\' + props.row.user.email
                  : 'hdqsmsg01\\' + props.value
              "
            >
              {{
                props.row.user.email.indexOf('spdbdev.com') > 0
                  ? 'dev\\' + props.row.user.email
                  : 'hdqsmsg01\\' + props.value
              }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{
                    props.row.user.email.indexOf('spdbdev.com') > 0
                      ? 'dev\\' + props.row.user.email
                      : 'hdqsmsg01\\' + props.value
                  }}
                </fdev-banner>
              </fdev-popup-proxy>
            </td>
          </template>
          <!-- <template v-slot:body-cell-purpose="props">
            <td title="个人手机银行系统平台开发测试">
              个人手机银行系统平台开发测试
            </td>
          </template> -->
          <template v-slot:body-cell-type="props">
            <td :title="props.value ? typeApply[props.value] : '-'">
              {{ props.value ? typeApply[props.value] : '-' }}
            </td>
          </template>
          <template v-slot:body-cell-status="props">
            <td title="props.value ? typeStatus[props.value] : '-'">
              {{ props.value ? typeStatus[props.value] : '-' }}
            </td>
          </template>
        </fdev-table>
        <div class="row justify-center">
          <div class="q-gutter-md inline-block">
            <fdev-tooltip anchor="top middle" v-if="rowSelected.length == 0">
              请勾选需审批的数据
            </fdev-tooltip>
            <fdev-tooltip anchor="top middle" v-if="!isWaitApprove">
              请勿勾选‘审核通过’‘审核拒绝’的数据
            </fdev-tooltip>
            <fdev-btn
              label="审核通过"
              @click="approvalBtn('passed', '通过')"
              :disable="rowSelected.length == 0 || !isWaitApprove"
              v-if="isNetworker"
            />
            <fdev-btn
              label="审核拒绝"
              @click="approvalBtn('refused', '拒绝')"
              :disable="rowSelected.length == 0 || !isWaitApprove"
              v-if="isNetworker"
            />
          </div>
        </div>
      </div>
    </Loading>
    <applyNetwork
      :isOpen="isApply"
      :dailogType="type"
      @close="isApply = false"
    />
    <closeNetwork
      :isDialogOpen="isClose"
      :dailogType="type"
      @close="isClose = false"
    />
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import applyNetwork from './applyNetwork';
import closeNetwork from './closeNetwork';
import { mapActions, mapState, mapGetters, mapMutations } from 'vuex';
import { successNotify, errorNotify } from '@/utils/utils';
import {
  setPagination,
  getPagination,
  setClosePagination,
  getClosePagination,
  setRemindPagination,
  getRemindePagination
} from '@/modules/Network/utils/setting';
import {
  kfApprovalColumns,
  typeApply,
  typeStatus
} from '@/modules/Network/utils/constants';

export default {
  name: 'NetApproval',
  components: {
    Loading,
    applyNetwork,
    closeNetwork
  },
  data() {
    return {
      columns: kfApprovalColumns(),
      nameOpen: this.$route.name === 'networkOpen',
      nameClose: this.$route.name === 'networkClose',
      typeApply: typeApply,
      typeStatus: typeStatus,
      rowSelected: [],
      isApply: false,
      isClose: false,
      pagination: {
        sortBy: '',
        descending: false,
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 10
      },
      companyOptions: [],
      labels: null,
      userOptions: [],
      loading: false,
      list: [],
      statusOptions: [
        { label: '待审核', value: 'wait_approve' },
        { label: '审核通过', value: 'passed' },
        { label: '审核拒绝', value: 'refused' }
      ],
      typeGetPagination: {
        kf_approval: getPagination(),
        kf_off_approval: getClosePagination(),
        kf_off_batch_approval: getRemindePagination()
      },
      typeSetPagination: {
        kf_approval: setPagination,
        kf_off_approval: setClosePagination,
        kf_off_batch_approval: setRemindPagination
      }
    };
  },
  props: {
    typeApproval: {
      type: String,
      required: true
    },
    type: {
      type: String,
      required: true
    }
  },
  computed: {
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('userForm', ['companies']),
    ...mapState('networkForm', ['approvalList']),
    ...mapState('userActionSaveNetwork/networkOpen', {
      applicant_idOpen: 'applicant_id',
      companyOpen: 'company',
      statusOpen: 'status',
      startTimeOpen: 'startTime',
      endTimeOpen: 'endTime',
      visibleColumnsOpen: 'visibleColumns'
    }),
    ...mapState('userActionSaveNetwork/networkClose', {
      applicant_idClose: 'applicant_id',
      companyClose: 'company',
      statusClose: 'status',
      startTimeClose: 'startTime',
      endTimeClose: 'endTime',
      visibleColumnsClose: 'visibleColumns'
    }),
    ...mapState('userActionSaveNetwork/networkRemind', {
      applicant_idRemind: 'applicant_id',
      companyRemind: 'company',
      statusRemind: 'status',
      startTimeRemind: 'startTime',
      endTimeRemind: 'endTime',
      visibleColumnsRemind: 'visibleColumns'
    }),
    company() {
      if (this.nameOpen) {
        return this.companyOpen;
      } else if (this.nameClose) {
        return this.companyClose;
      } else {
        return this.companyRemind;
      }
    },
    applicant_id() {
      if (this.nameOpen) {
        return this.applicant_idOpen;
      } else if (this.nameClose) {
        return this.applicant_idClose;
      } else {
        return this.applicant_idRemind;
      }
    },
    status() {
      if (this.nameOpen) {
        return this.statusOpen;
      } else if (this.nameClose) {
        return this.statusClose;
      } else {
        return this.statusRemind;
      }
    },
    startTime() {
      if (this.nameOpen) {
        return this.startTimeOpen;
      } else if (this.nameClose) {
        return this.startTimeClose;
      } else {
        return this.startTimeRemind;
      }
    },
    endTime() {
      if (this.nameOpen) {
        return this.endTimeOpen;
      } else if (this.nameClose) {
        return this.endTimeClose;
      } else {
        return this.endTimeRemind;
      }
    },
    visibleColumns() {
      if (this.nameOpen) {
        return this.visibleColumnsOpen;
      } else if (this.nameClose) {
        return this.visibleColumnsClose;
      } else {
        return this.visibleColumnsRemind;
      }
    },
    isWaitApprove() {
      return this.rowSelected.every(each => each.status === 'wait_approve');
    },
    isNetworker() {
      return this.currentUser.role.some(
        item => item.label === '网络审核员' || item.name === '卡点管理员'
      );
    },
    visibleOptions() {
      const arr = this.columns.slice(0);
      return arr.splice(0, arr.length - 1);
    },
    tableTitle() {
      let title = '';
      switch (this.type) {
        case 'networkOpen': {
          title = 'KF网络开通审核列表';
          break;
        }
        case 'networkClose': {
          title = 'KF网络关闭审核列表';
          break;
        }
        case 'networkRemind': {
          title = '网络关闭提醒列表';
          break;
        }
      }
      return title;
    },
    btnLabel() {
      const label = {
        networkOpen: 'KF网络开通申请',
        networkClose: 'KF网络关闭申请'
      };
      return label[this.type];
    }
  },
  watch: {
    'pagination.rowsPerPage'(val) {
      this.typeSetPagination[this.typeApproval]({
        rowsPerPage: val
      });
    }
  },
  methods: {
    ...mapActions('user', ['fetch']),
    ...mapActions('userForm', ['fetchCompany']),
    ...mapActions('networkForm', ['queryApprovalList', 'updateApprovalStatus']),
    ...mapMutations('userActionSaveNetwork/networkOpen', {
      openApplicant_id: 'updateApplicationId',
      openCompany: 'updateCompany',
      openStatus: 'updateStatus',
      openStartTime: 'updateStartTime',
      openEndTime: 'updateEndTime',
      openVisibleColumns: 'updateVisibleColumns'
    }),
    ...mapMutations('userActionSaveNetwork/networkClose', {
      closeApplicant_id: 'updateApplicationId',
      closeCompany: 'updateCompany',
      closeStatus: 'updateStatus',
      closeStartTime: 'updateStartTime',
      closeEndTime: 'updateEndTime',
      closeVisibleColumns: 'updateVisibleColumns'
    }),
    ...mapMutations('userActionSaveNetwork/networkRemind', {
      remindApplicant_id: 'updateApplicationId',
      remindCompany: 'updateCompany',
      remindStatus: 'updateStatus',
      remindStartTime: 'updateStartTime',
      remindEndTime: 'updateEndTime',
      remindeVisibleColumns: 'updateVisibleColumns'
    }),
    queryApplicantId(data) {
      if (this.nameOpen) {
        this.openApplicant_id(data);
      } else if (this.nameClose) {
        this.closeApplicant_id(data);
      } else {
        this.remindApplicant_id(data);
      }
    },
    queryCompany(data) {
      if (this.nameOpen) {
        this.openCompany(data);
      } else if (this.nameClose) {
        this.closeCompany(data);
      } else {
        this.remindCompany(data);
      }
    },
    queryStatus(data) {
      if (this.nameOpen) {
        this.openStatus(data);
      } else if (this.nameClose) {
        this.closeStatus(data);
      } else {
        this.remindStatus(data);
      }
    },
    queryStartTime(data) {
      if (this.nameOpen) {
        this.openStartTime(data);
      } else if (this.nameClose) {
        this.closeStartTime(data);
      } else {
        this.remindStartTime(data);
      }
      this.$refs.qDateProxyStart.hide();
    },
    queryEndTime(data) {
      if (this.nameOpen) {
        this.openEndTime(data);
      } else if (this.nameClose) {
        this.closeEndTime(data);
      } else {
        this.remindEndTime(data);
      }
      this.$refs.qDateProxyEnd.hide();
    },
    selectVisibleColumns(data) {
      if (this.nameOpen) {
        this.openVisibleColumns(data);
      } else if (this.nameClose) {
        this.closeVisibleColumns(data);
      } else {
        this.remindeVisibleColumns(data);
      }
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
    startOptions(date) {
      if (this.endTime) {
        return date <= this.endTime.replace(/-/g, '/');
      }
      return true;
    },
    endOptions(date) {
      if (this.startTime) {
        return date >= this.startTime.replace(/-/g, '/');
      }
      return true;
    },
    async approvalBtn(status, statusCn) {
      return this.$q
        .dialog({
          title: `审核${statusCn}`,
          message:
            statusCn === '通过'
              ? `您审核通过${
                  this.rowSelected.length
                }条数据，将以邮箱的形式发送给下一步审核人`
              : this.typeApproval === 'kf_approval'
              ? `您审核拒绝${
                  this.rowSelected.length
                }条数据，将以邮箱的形式发送给申请提交人`
              : `您将审核拒绝${this.rowSelected.length}条数据，请确认`,
          ok: '确认',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.updateApprovalStatus({
            type: this.typeApproval,
            status: status,
            ids: this.rowSelected.map(item => item.id)
          });
          successNotify(`审核${statusCn}执行成功`);
          this.rowSelected = [];
          this.query();
        });
    },
    async query(props) {
      if (Boolean(this.startTime) === Boolean(this.endTime)) {
        if (props && props.pagination) {
          let { page, rowsPerPage, sortBy, descending } = props.pagination;
          this.pagination.page = page;
          this.pagination.rowsPerPage = rowsPerPage;
          this.pagination.sortBy = sortBy;
          this.pagination.descending = descending;
        }
        this.loading = true;
        let applicant_id = this.applicant_id
          ? this.applicant_id.map(label => label.id)
          : null;
        let params = {
          applicant_id,
          company_id: this.company ? this.company.id : null,
          status: this.status ? this.status.value : null,
          type: this.typeApproval,
          startTime: this.startTime,
          endTime: this.endTime,
          page: this.pagination.page,
          pageNum: this.pagination.rowsPerPage
        };
        try {
          await this.queryApprovalList(params);
          this.pagination.rowsNumber = this.approvalList.total;
          this.list = this.approvalList.list;
          this.pagination.rowsNumber = this.approvalList.total;
        } finally {
          this.loading = false;
        }
      } else {
        errorNotify("请同时选择'开始时间'和'结束时间'");
      }
    },
    isNetwork() {
      if (this.type === 'networkOpen') {
        this.isApply = true;
      } else if (this.type === 'networkClose') {
        this.isClose = true;
      }
    }
  },
  async created() {
    await this.fetch();
    this.userOptions = this.userList;
    await this.fetchCompany();
    this.companyOptions = this.companies;
    this.query();
  }
};
</script>
<style lang="stylus" scoped>
form
  width 100%
  .row
    align-items self-start
.select
  width 210px
.clearfix:after
  content ''
  display block
  clear:both
  visibility hidden
.select-width
 min-width 150px
</style>
