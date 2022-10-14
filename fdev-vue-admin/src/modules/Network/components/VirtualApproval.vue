<template>
  <f-block>
    <Loading :visible="loading">
      <div class="bg-white">
        <fdev-table
          titleIcon="list_s_f"
          :data="list"
          :title="tableTitle"
          :columns="typeColumns"
          selection="multiple"
          :selected.sync="rowSelected"
          row-key="id"
          :pagination.sync="pagination"
          @request="query"
          :visible-columns="visibleColumns"
          :on-search="query"
        >
          <template v-slot:top-bottom>
            <f-formitem
              class="col-4 q-pr-sm"
              label-style="width:100px"
              bottom-page
              label="使用人"
            >
              <fdev-select
                use-input
                multiple
                @filter="filterUser"
                :options="userOptions"
                option-label="user_name_cn"
                option-value="id"
                :value="user_id"
                @input="queryUserId($event)"
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
              label="用户名"
            >
              <fdev-input
                use-input
                :value="vm_user_name"
                @input="queryVmUserName($event)"
              >
              </fdev-input>
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
          <template v-slot:body-cell-user="props">
            <fdev-td class="text-ellipsis">
              <router-link
                v-if="props.row.user"
                :to="{
                  path: `/user/list/${props.row.user.id}`
                }"
                class="link"
                :title="props.row.user.user_name_cn"
              >
                {{ props.row.user.user_name_cn }}
              </router-link>
              <span title="props.row.user.user_name_cn || '-'" v-else>
                {{ props.row.user.user_name_cn || '-' }}
              </span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-status="props">
            <fdev-td :title="props.value ? typeStatus[props.value] : '-'">
              {{ props.value ? typeStatus[props.value] : '-' }}
            </fdev-td>
          </template>
          <template
            v-slot:body-cell-vm_user_name="props"
            v-if="this.typeApproval === 'vdi_approval'"
          >
            <fdev-td class="text-ellipsis" :title="'hdqsmsg01\\' + props.value">
              hdqsmsg01\{{ props.value }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-vm_user_name="props" v-else>
            <fdev-td :title="'dev\\' + props.value"
              >dev\{{ props.value }}</fdev-td
            >
          </template>
          <template v-slot:body-cell-netSegment>
            <fdev-td title="xxx">
             xxx
            </fdev-td>
          </template>
          <template v-slot:body-cell-applicant="props">
            <fdev-td class="text-ellipsis">
              <router-link
                :title="props.row.applicant.user_name_cn"
                v-if="props.row.applicant"
                :to="{
                  path: `/user/list/${props.row.applicant.id}`
                }"
                class="link"
              >
                {{ props.row.applicant.user_name_cn }}
              </router-link>
              <span :title="props.row.applicant.user_name_cn || '-'" v-else>
                {{ props.row.applicant.user_name_cn || '-' }}
              </span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-projectGroup>
            <fdev-td title="互联网条线" class="text-ellipsis">
              互联网条线
            </fdev-td>
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
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState, mapGetters, mapMutations } from 'vuex';
import { successNotify, errorNotify } from '@/utils/utils';
import {
  setVDIPagination,
  getVDIPagination,
  setVMPagination,
  getVMPagination
} from '@/modules/Network/utils/setting';
import { typeApply, typeStatus } from '@/modules/Network/utils/constants';

export default {
  name: 'VirtualApproval',
  components: {
    Loading
  },
  data() {
    return {
      nameVdi: this.$route.name === 'vdiApproval',
      typeApply: typeApply,
      typeStatus: typeStatus,
      rowSelected: [],
      pagination: {
        sortBy: '',
        descending: false,
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 10
      },
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
        vdi_approval: getVDIPagination(),
        vm_approval: getVMPagination()
      },
      typeSetPagination: {
        vdi_approval: setVDIPagination,
        vm_approval: setVMPagination
      }
    };
  },
  props: {
    typeApproval: {
      type: String,
      required: true
    },
    typeColumns: {
      type: Array,
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
    ...mapState('userActionSaveNetwork/vdiApproval', {
      userIdVdi: 'user_id',
      vmUserNameVdi: 'vm_user_name',
      statusVdi: 'status',
      startTimeVdi: 'startTime',
      endTimeVdi: 'endTime',
      visibleColumnsVdi: 'visibleColumns'
    }),
    ...mapState('userActionSaveNetwork/vmApproval', {
      userIdVm: 'user_id',
      vmUserNameVm: 'vm_user_name',
      statusVm: 'status',
      startTimeVm: 'startTime',
      endTimeVm: 'endTime',
      visibleColumnsVm: 'visibleColumns'
    }),
    user_id() {
      return this.nameVdi ? this.userIdVdi : this.userIdVm;
    },
    vm_user_name() {
      return this.nameVdi ? this.vmUserNameVdi : this.vmUserNameVm;
    },
    status() {
      return this.nameVdi ? this.statusVdi : this.statusVm;
    },
    startTime() {
      return this.nameVdi ? this.startTimeVdi : this.startTimeVm;
    },
    endTime() {
      return this.nameVdi ? this.endTimeVdi : this.endTimeVm;
    },
    visibleColumns() {
      return this.nameVdi ? this.visibleColumnsVdi : this.visibleColumnsVm;
    },
    isWaitApprove() {
      return this.rowSelected.every(each => each.status === 'wait_approve');
    },
    visibleColumnse() {
      return this.rowSelected.every(each => each.status === 'wait_approve');
    },
    isNetworker() {
      return this.currentUser.role.some(
        item => item.label === '网络审核员' || item.name === '卡点管理员'
      );
    },
    visibleOptions() {
      const arr = this.typeColumns.slice(0);
      return arr.splice(0, arr.length - 1);
    },
    tableTitle() {
      let title = '';
      switch (this.type) {
        case 'vdiApproval': {
          title = 'VDI网段迁移列表';
          break;
        }
        case 'vmApproval': {
          title = '虚拟机网段迁移列表';
          break;
        }
      }
      return title;
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
    ...mapActions('networkForm', ['queryApprovalList', 'updateApprovalStatus']),
    ...mapMutations('userActionSaveNetwork/vdiApproval', {
      vdiApplicant_id: 'updateUserId',
      vdiVmUserName: 'updateVmUserName',
      vdiStatus: 'updateStatus',
      vdiStartTime: 'updateStartTime',
      vdiEndTime: 'updateEndTime',
      vdiVisibleColumns: 'updateVisibleColumns'
    }),
    ...mapMutations('userActionSaveNetwork/vmApproval', {
      vmApplicant_id: 'updateUserId',
      vmVmUserName: 'updateVmUserName',
      vmStatus: 'updateStatus',
      vmStartTime: 'updateStartTime',
      vmEndTime: 'updateEndTime',
      vmVisibleColumns: 'updateVisibleColumns'
    }),
    queryUserId(data) {
      this.nameVdi ? this.vdiApplicant_id(data) : this.vmApplicant_id(data);
    },
    queryVmUserName(data) {
      this.nameVdi ? this.vdiVmUserName(data) : this.vmVmUserName(data);
    },
    queryStatus(data) {
      this.nameVdi ? this.vdiStatus(data) : this.vmStatus(data);
    },
    queryStartTime(data) {
      this.nameVdi ? this.vdiStartTime(data) : this.vmStartTime(data);
    },
    queryEndTime(data) {
      this.nameVdi ? this.vdiEndTime(data) : this.vmEndTime(data);
    },
    selectVisibleColumns(data) {
      this.nameVdi ? this.vdiVisibleColumns(data) : this.vmVisibleColumns(data);
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
              : `您审核拒绝${
                  this.rowSelected.length
                }条数据，将以邮箱的形式发送给申请提交人`,
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
        let user_id = this.user_id ? this.user_id.map(label => label.id) : null;
        let params = {
          user_id,
          vm_user_name: this.vm_user_name,
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
    }
  },
  async created() {
    await this.fetch();
    this.userOptions = this.userList;
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
