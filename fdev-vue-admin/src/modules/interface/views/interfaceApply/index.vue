<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        :data="applyList"
        :columns="columns"
        row-key="applyList.id"
        :filter="inputFilter"
        :filter-method="applyListFilter"
        :pagination.sync="pagination"
        @request="query"
        noExport
        :on-search="searchByTermS"
        title="接口审批列表"
        titleIcon="list_s_f"
        :visible-columns="visibleColumns"
      >
        <template v-slot:top-bottom>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="提供方应用">
            <fdev-select
              use-input
              clearable
              ref="serviceId"
              :value="serviceId"
              @input="updateServiceId($event)"
              :options="filterProject"
              @filter="projectFilter"
              option-label="name_en"
              option-value="name_en"
              type="text"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name_en">{{
                      scope.opt.name_en
                    }}</fdev-item-label>
                    <fdev-item-label :title="scope.opt.name_zh" caption>
                      {{ scope.opt.name_zh }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="调用方应用">
            <fdev-select
              use-input
              clearable
              ref="serviceCalling"
              :value="serviceCalling"
              @input="updateServiceCalling($event)"
              :options="filterProject"
              @filter="projectFilter"
              option-label="name_en"
              option-value="name_en"
              type="text"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name_en">{{
                      scope.opt.name_en
                    }}</fdev-item-label>
                    <fdev-item-label :title="scope.opt.name_zh" caption>
                      {{ scope.opt.name_zh }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="申请人">
            <fdev-input
              ref="applicant"
              :value="applicant"
              @input="updateApplicant($event)"
              type="text"
              @keyup.enter="searchByTermS"
            />
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="审批人">
            <fdev-input
              ref="approver"
              :value="approver"
              @input="updateApprover($event)"
              type="text"
              @keyup.enter="searchByTermS"
            />
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="交易码">
            <fdev-input
              outlined
              ref="transId"
              :value="transId"
              @input="updateTransId($event)"
              type="text"
              @keyup.enter="searchByTermS"
            />
          </f-formitem>
        </template>
        <template v-slot:top-right>
          <fdev-select
            :display-value="
              tableIsApproval.value === 'total'
                ? '审批状态'
                : formatSelectDisplay(approval, tableIsApproval)
            "
            :value="tableIsApproval"
            @input="updateTableIsApproval($event)"
            :options="approval"
            options-dense
            class="table-head-input"
          />
        </template>
        <template v-slot:body-cell-transId="props">
          <fdev-td class="text-left text-ellipsis">
            <router-link
              :to="{
                path: `/interfaceAndRoute/interface/interfaceProfile/${
                  props.row.interfaceId
                }`,
                query: { interfaceType: 'REST' }
              }"
              class="link"
              v-if="props.row.interfaceId"
              :title="props.row.transId"
              >{{ props.row.transId }}</router-link
            >
            <div v-else :title="props.row.transId || '-'">
              {{ props.row.transId || '-' }}
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-serviceCalling="props">
          <fdev-td class="text-left text-ellipsis">
            <router-link
              v-if="props.row.callingId"
              :to="{
                path: `/app/list/${props.row.callingId}-interface_${
                  props.row.serviceCalling
                }`
              }"
              class="link"
              :title="props.row.serviceCalling"
              >{{ props.row.serviceCalling }}</router-link
            >
            <div v-else :title="props.row.serviceCalling || '-'">
              {{ props.row.serviceCalling || '-' }}
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-serviceId="props">
          <fdev-td class="text-ellipsis">
            <router-link
              v-if="props.row.appId"
              :to="{
                path: `/app/list/${props.row.appId}-interface_${
                  props.row.serviceId
                }`
              }"
              class="link"
              :title="props.row.serviceId"
              >{{ props.row.serviceId }}</router-link
            >
            <div v-else :title="props.row.serviceId || '-'">
              {{ props.row.serviceId || '-' }}
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-reason="props">
          <fdev-td class="text-ellipsis" :title="props.row.reason || '-'">
            <span>
              {{ props.row.reason || '-' }}
              <fdev-popup-proxy context-menu v-if="props.row.reason">
                <fdev-banner style="max-width:300px">
                  {{ props.row.reason }}
                </fdev-banner>
              </fdev-popup-proxy>
            </span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-refuseReason="props">
          <fdev-td class="text-ellipsis" :title="props.row.refuseReason || '-'">
            <span>
              {{ props.row.refuseReason || '-' }}
              <fdev-popup-proxy context-menu v-if="props.row.refuseReason">
                <fdev-banner style="max-width:300px">
                  {{ props.row.refuseReason }}
                </fdev-banner>
              </fdev-popup-proxy>
            </span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-provideDevManagers="props">
          <fdev-td>
            <div
              :title="
                props.row.provideDevManagers.map(v => v.userName).join('，')
              "
              class="q-gutter-x-sm text-ellipsis"
            >
              <span v-for="item in props.row.provideDevManagers" :key="item.id">
                <router-link
                  :to="`/user/list/${item.id}`"
                  class="link"
                  v-if="item.id"
                >
                  <span>{{ item.userName }}</span>
                </router-link>
              </span>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-provideSpdbManagers="props">
          <fdev-td>
            <div
              :title="
                props.row.provideSpdbManagers.map(v => v.userName).join('，')
              "
              class="q-gutter-x-sm text-ellipsis"
            >
              <span
                v-for="item in props.row.provideSpdbManagers"
                :key="item.id"
              >
                <router-link
                  :to="`/user/list/${item.id}`"
                  class="link"
                  v-if="item.id"
                >
                  {{ item.userName }}
                </router-link>
              </span>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-status="props">
          <fdev-td auto-width class="text-center">
            <Authorized
              :include-me="managerId(props.row)"
              v-if="props.value === '1'"
            >
              <div class="q-gutter-x-sm">
                <fdev-btn
                  flat
                  label="通过"
                  @click="handleChangeStatusToOk(props.row)"
                />
                <fdev-btn
                  flat
                  label="拒绝"
                  @click="handleOpenConfirmModal(props.row)"
                />
              </div>
            </Authorized>
            <span
              v-else
              :title="approval[parseInt(props.value) + 1].label || '-'"
            >
              {{ approval[parseInt(props.value) + 1].label || '-' }}
            </span>
          </fdev-td>
        </template>
      </fdev-table>
      <f-dialog v-model="confirmModalOpen" title="拒绝原因">
        <f-formitem fullWidth label="拒绝原因">
          <fdev-input
            v-model="refuseReason"
            ref="refuseReason"
            autofocus
            :rules="[() => $v.refuseReason.require || '请输入拒绝原因']"
          />
        </f-formitem>
        <template v-slot:btnSlot>
          <fdev-btn dialog label="取消" v-close-popup />
          <fdev-btn
            dialog
            label="确定"
            :disable="disabled"
            @click="updateStatus"
          />
        </template>
      </f-dialog>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import Authorized from '@/components/Authorized';
import { interfaceApplyColumns } from '../../utils/constants';
import { mapState, mapActions, mapMutations } from 'vuex';
import {
  successNotify,
  formatSelectDisplay,
  getIdsFormList
} from '@/utils/utils';

export default {
  name: 'ApplyList',
  components: { Loading, Authorized },
  data() {
    return {
      columns: interfaceApplyColumns(),
      loading: false,
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 0,
        page: 1
      },
      applyList: [],
      filter: '',
      inputFilter: '',
      selectFilter: '',
      terms: [],
      approval: [
        { label: '审批状态', value: 'total' },
        { label: '审批通过', value: '0' },
        { label: '待审批', value: '1' },
        { label: '审批拒绝', value: '2' }
      ],
      confirmModalOpen: false,
      disabled: true,
      refuseReason: '',
      selector: {},
      filterProject: []
    };
  },
  validations: {
    refuseReason: {
      require(val) {
        if (!val) {
          return false;
        } else {
          return val.trim().length > 0;
        }
      }
    }
  },
  computed: {
    ...mapState('userActionSaveInterface/approval/interfaceApproval', [
      'serviceId',
      'serviceCalling',
      'applicant',
      'approver',
      'transId',
      'tableIsApproval',
      'visibleColumns'
    ]),
    ...mapState('interfaceForm', {
      applicationList: 'applicationList'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('appForm', ['vueAppData']),
    visibleOptions() {
      const arr = this.columns.slice(0);
      return arr.splice(0, arr.length - 1);
    }
  },
  watch: {
    selectFilter(val) {
      if (val === 'total') {
        this.inputFilter = this.filter;
        return;
      }
      this.inputFilter = this.filter + ',' + val;
    },
    filter(val) {
      if (this.selectFilter === 'total') {
        this.inputFilter = this.filter;
        return;
      }
      this.inputFilter = val + ',' + this.selectFilter;
    },
    terms(val) {
      this.filter = val.toString();
    },
    refuseReason(val) {
      this.disabled = !val.trim();
    },
    tableIsApproval({ value }) {
      this.applyList = this.applicationList.list;
      this.selectFilter = value;
    }
  },
  methods: {
    formatSelectDisplay,
    ...mapMutations('userActionSaveInterface/approval/interfaceApproval', [
      'updateServiceId',
      'updateServiceCalling',
      'updateApplicant',
      'updateApprover',
      'updateTransId',
      'updateTableIsApproval',
      'updateVisibleColumns'
    ]),
    ...mapActions('interfaceForm', {
      queryApplicationList: 'queryApplicationList',
      updateApplicationStatus: 'updateApplicationStatus'
    }),
    ...mapActions('appForm', ['queryApps']),
    addNewValue() {
      if (this.$refs.terms.inputValue.length) {
        this.$refs.terms.add(this.$refs.terms.inputValue);
        this.$refs.terms.inputValue = '';
      }
    },
    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    applyListFilter(rows, terms, cols, cellValue) {
      let lowerTerms = terms ? terms.toLowerCase().split(',') : [];
      // 输入的搜索内容
      let otherTerms = [];
      // 右上角的审批状态筛选
      let lastTerm = [];
      if (this.selectFilter !== 'total') {
        lastTerm.push(lowerTerms.pop());
        otherTerms = lowerTerms.slice(0, lowerTerms.length);
      } else {
        otherTerms = lowerTerms;
        lastTerm = [''];
      }
      return rows.filter(row => {
        let a = otherTerms.every(term => {
          if (term.startsWith('__') || term === '') {
            return true;
          }
          return cols.some((col, index) => {
            if (Array.isArray(cellValue(col, row))) {
              return (
                cellValue(col, row)
                  .toString()
                  .toLowerCase()
                  .indexOf(term) > -1
              );
            } else {
              return (
                (cellValue(col, row) + '').toLowerCase().indexOf(term) > -1
              );
            }
          });
        });
        let b = lastTerm.map(term => {
          if (term === '') {
            return true;
          }
          return cols.some((col, index) => {
            if (this.selectFilter === 'total') {
              return true;
            }
            if (index === this.columns.length - 1) {
              return term === cellValue(col, row);
            }
          });
        });
        return a && b[0];
      });
    },
    handleOpenConfirmModal(row) {
      this.confirmModalOpen = true;
      this.selector = row;
    },
    async updateStatus() {
      let params = { ...this.selector };
      params.approver = this.currentUser.user_name_cn;
      params.refuseReason = this.refuseReason;
      params.status = '2';
      await this.updateApplicationStatus(params);
      successNotify('申请已拒绝');
      this.confirmModalOpen = false;
      await this.getApplyList();
    },
    async handleChangeStatusToOk(row) {
      return this.$q
        .dialog({
          title: `确认申请`,
          message: `您确认通过该接口调用申请吗？`,
          ok: '确认',
          cancel: '再想想'
        })
        .onOk(async () => {
          await this.approvedApply(row);
        });
    },
    async approvedApply(row) {
      let params = { ...row };
      params.status = '0';
      params.approver = this.currentUser.user_name_cn;
      await this.updateApplicationStatus(params);
      successNotify('申请已通过');
      await this.getApplyList();
    },
    managerId({ provideDevManagers, provideSpdbManagers }) {
      return getIdsFormList([provideDevManagers, provideSpdbManagers]);
    },
    searchByTermS(props) {
      this.query(props);
    },
    async query(props) {
      this.loading = true;
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      await this.getApplyList();
      this.loading = false;
    },
    async getApplyList() {
      await this.queryApplicationList({
        page: this.pagination.page,
        pageNum: this.pagination.rowsPerPage,
        serviceCalling: this.serviceCalling ? this.serviceCalling.name_en : '',
        approver: this.approver,
        serviceId: this.serviceId ? this.serviceId.name_en : '',
        applicant: this.applicant,
        transId: this.transId,
        status:
          this.tableIsApproval.value === 'total'
            ? ''
            : this.tableIsApproval.value
      });
      this.applyList = this.applicationList.list;
      this.pagination.rowsNumber = this.applicationList.total;
    },
    projectFilter(val, update) {
      update(() => {
        this.filterProject = this.vueAppData.filter(
          tag =>
            tag.name_zh.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            tag.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    }
  },
  async created() {
    await this.getApplyList();
    await this.queryApps();
    this.filterProject = this.vueAppData;
  }
};
</script>
<style lang="stylus" scoped>
form
  width 100%
  .row
    align-items self-start
.td-width
  box-sizing border-box
  max-width 16rem
  overflow hidden
  text-overflow ellipsis
.my-requst
  position absolute
  top 0
  right 0
.textarea
  width 300px
  height 87px
.w150
  width 150px
</style>
