<template>
  <f-block page>
    <Loading :visible="loading">
      <fdev-table
        row-key="id"
        title="代码审核问题列表"
        titleIcon="list_s_f"
        :data="tableLists"
        :columns="columns"
        class="my-sticky-column-table"
        :pagination.sync="pagination"
        @request="pageRequestData"
        :on-search="getTableData"
        :visible-columns="visibleCols"
        :on-select-cols="saveVisibleCols"
        :export-func="handleExportExcel"
      >
        <template #top-right>
          <div v-show="hasMoreSearch">
            <div class="text-warning text-subtitle4 row items-center">
              <f-icon
                name="alert_t_f"
                :width="14"
                :height="14"
                class="q-mr-xs cursor-pointer tip"
              />
              <fdev-tooltip position="top" target=".tip">
                高级搜索内有更多的查询条件未清除
              </fdev-tooltip>
              有查询条件未清除哦->
            </div>
          </div>
          <fdev-btn
            normal
            label="高级搜索"
            @click="moreSearch = true"
            ficon="search"
          />
        </template>
        <template v-slot:top-bottom>
          <f-formitem
            label="问题描述"
            class="col-4 q-pr-md"
            label-style="width:110px"
            bottom-page
          >
            <fdev-input
              use-input
              use-chips
              v-model="issueDesc"
              @input="saveIssueDesc($event)"
              @blur="getTableData"
              clearable
              @clear="getTableData"
              @keyup.enter="getTableData()"
            >
            </fdev-input>
          </f-formitem>
          <f-formitem
            label="工单编号"
            class="col-4 q-pr-md"
            label-style="width:110px"
            bottom-page
          >
            <fdev-select
              use-input
              v-model="orderNo"
              @input="saveOrderNo($event)"
              option-label="orderNo"
              option-value="orderNo"
              :options="orderOptions"
              clearable
              @filter="orderFilter"
              @clear="getTableData"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.orderNo">{{
                      scope.opt.orderNo
                    }}</fdev-item-label>
                    <fdev-item-label :title="scope.opt.demandName" caption>
                      {{ scope.opt.demandName }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem
            label="问题类型"
            class="col-4"
            label-style="width:110px"
            bottom-page
          >
            <fdev-select
              clearable
              v-model="itemType"
              option-label="label"
              option-value="key"
              :options="ItemOptions"
              @input="saveItemType($event)"
            >
            </fdev-select>
          </f-formitem>
        </template>
        <!-- 工单编号 -->
        <template v-slot:body-cell-orderNo="props">
          <fdev-td>
            <div :title="props.row.orderNo" class="text-ellipsis">
              <router-link
                :to="`/aAndA/codeTool/${props.row.orderId}`"
                class="link"
              >
                {{ props.row.orderNo || '-' }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.orderNo || '-' }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </div>
          </fdev-td>
        </template>
        <!-- 工单状态 -->
        <template v-slot:body-cell-orderStatus="props">
          <fdev-td>
            <div
              v-if="props.row.orderStatus"
              :title="getStatusName(props.row.orderStatus)"
              class="status-style"
            >
              <span
                class="status-img q-mr-xs"
                :style="{ background: getStatusColor(props.row.orderStatus) }"
              />
              {{ getStatusName(props.row.orderStatus) }}
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <!-- 需求名称 -->
        <template v-slot:body-cell-demandName="props">
          <fdev-td>
            <div :title="props.row.demandName" class="text-ellipsis">
              <router-link
                :to="`/rqrmn/rqrProfile/${props.row.demandId}`"
                class="link"
              >
                {{ props.row.demandName || '-' }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.demandName || '-' }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </div>
          </fdev-td>
        </template>
        <!-- 是否已修复 -->
        <template v-slot:body-cell-fixFlag="props">
          <fdev-td>
            <div
              v-if="props.row.fixFlag"
              :title="getTransName(props.row.fixFlag, fixFlagOption)"
              class="status-style"
            >
              <span
                class="status-img q-mr-xs"
                :style="{ background: getTransColor(props.row.fixFlag) }"
              />
              {{ getTransName(props.row.fixFlag, fixFlagOption) }}
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <!-- 问题类型 -->
        <template v-slot:body-cell-problemType="props">
          <fdev-td>
            <div
              v-if="props.row.problemType"
              :title="getTransName(props.row.problemType, ItemOptions)"
              class="status-style"
            >
              <span
                class="status-img q-mr-xs"
                :style="{ background: getTransColor(props.row.problemType) }"
              />
              {{ getTransName(props.row.problemType, ItemOptions) }}
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
    <f-dialog
      title="更多查询条件"
      right
      v-model="moreSearch"
      @before-close="moreSearchCancel"
      @before-show="beforeShow"
    >
      <div class="q-gutter-y-lg">
        <f-formitem label="日期类型" diaS>
          <fdev-select
            multiple
            :value="dateField"
            @input="saveDateType($event)"
            :options="dateTypeOptions"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="开始日期" v-if="isShowDate" diaS>
          <f-date
            :value="startDate"
            ref="startDate"
            @input="saveStartDate($event)"
            mask="YYYY-MM-DD"
            :options="startDateOptions"
          >
          </f-date>
        </f-formitem>
        <f-formitem label="结束日期" v-if="isShowDate" diaS>
          <f-date
            :value="endDate"
            ref="endDate"
            @input="saveEndDate($event)"
            mask="YYYY-MM-DD"
            :options="endDateOptions"
          >
          </f-date>
        </f-formitem>
      </div>

      <template #btnSlot>
        <fdev-btn label="清空" outline dialog @click="resetMoreSearch"/>
        <fdev-btn label="取消" outline dialog @click="moreSearchCancel"/>
        <fdev-btn label="查询" dialog @click="moreSearchClick"
      /></template>
    </f-dialog>
  </f-block>
</template>
<script>
import Loading from '@/components/Loading';
import {
  codeIssueColumns,
  createMemory,
  statusOptions
} from '@/modules/Network/utils/constants';
import { mapState, mapMutations } from 'vuex';
import { exportIssueList } from '@/modules/Network/services/methods';
import { resolveResponseError, exportExcel } from '@/utils/utils';
import date from '#/utils/date.js';
import moment from 'moment';
import {
  queryCodeIssue,
  queryOrdersList
} from '@/modules/Network/services/methods';
export default {
  name: 'orderLists',
  components: { Loading },
  computed: {
    ...mapState('userActionSaveNetwork/codeIssueList', [
      'dateField',
      'isShowDate',
      'startDate',
      'endDate',
      'issueDesc',
      'orderNo',
      'itemType',
      'visibleCols',
      'currentPage'
    ]),
    hasMoreSearch() {
      return !this.moreSearch && this.dateField.length !== 0;
    }
  },
  data() {
    return {
      loading: false,
      currentDate: null,
      tableLists: [],
      moreSearch: false,
      showDate: false,
      orderOptions: [],
      orderList: [],
      statusOptions: statusOptions,
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      },
      columns: codeIssueColumns(),
      ItemOptions: [
        {
          key: 'issue',
          label: '缺陷'
        },
        {
          key: 'advice',
          label: '建议'
        },
        {
          key: 'risk',
          label: '风险'
        }
      ],
      dateTypeOptions: [
        {
          key: 'applyTime',
          label: '工单申请时间'
        },
        {
          key: 'meetingTime',
          label: '会议时间'
        },
        {
          key: 'createTime',
          label: '问题创建时间'
        }
      ],
      fixFlagOption: [
        { key: 'fixed', label: '已修复' },
        { key: 'notFixed', label: '未修复' }
      ],
      moreSearchMap: {
        dateField: 'saveDateType',
        startDate: 'saveStartDate',
        endDate: 'saveEndDate'
      },
      memory: createMemory()
    };
  },
  watch: {
    //待办，取消内容未清除
    dateField(val) {
      if (val.length !== 0) {
        this.saveisShowDate(true);
        this.saveStartDate(
          moment(this.currentDate)
            .subtract('days', 90)
            .format('YYYY-MM-DD')
        );
        this.saveEndDate(this.currentDate);
      } else {
        this.saveStartDate('');
        this.saveEndDate('');
        this.saveisShowDate(false);
      }
    }
  },
  methods: {
    ...mapMutations('userActionSaveNetwork/codeIssueList', [
      'saveStartDate',
      'saveisShowDate',
      'saveEndDate',
      'saveDateType',
      'saveVisibleCols',
      'saveIssueDesc',
      'saveCurrentPage',
      'saveOrderNo',
      'saveItemType'
    ]),
    orderFilter(val, update, abort) {
      update(() => {
        this.orderOptions = this.orderList.filter(
          order =>
            order.orderNo.indexOf(val) > -1 ||
            order.demandName.indexOf(val) > -1
        );
      });
    },
    getStatusName(val) {
      return this.statusOptions.filter(item => item.value == val).length
        ? this.statusOptions.filter(item => item.value == val)[0].label
        : '';
    },
    getStatusColor(status) {
      if (status == '1') {
        return '#FEC400';
      } else if (status == '2') {
        return '#02E01A ';
      } else if (status == '3') {
        return '#4DBB59 ';
      } else if (status == '4') {
        return '#00830E ';
      } else if (status == '5') {
        return '#24C8F9 ';
      } else if (status == '6') {
        return '#0378EA ';
      } else if (status == '7') {
        return '#04488C ';
      } else if (status == '8') {
        return '#FF740B ';
      } else {
        return '#FEC400';
      }
    },
    getTransColor(val) {
      if (val == 'fixed') {
        return '#02E01A ';
      } else if (val == 'notFixed') {
        return '#FF740B ';
      } else if (val == 'issue') {
        return '#f56c6c ';
      } else if (val == 'advice') {
        return '#409eff ';
      } else if (val == 'risk') {
        return '#f6a120 ';
      } else {
        return '#FEC400';
      }
    },
    getTransName(val, options) {
      return options.filter(item => item.key == val).length
        ? options.filter(item => item.key == val)[0].label
        : '';
    },
    endDateOptions(date) {
      this.saveStartDate(this.startDate ? this.startDate : '');
      return date >= this.startDate.replace(/-/g, '/');
    },
    startDateOptions(date) {
      if (this.endDate) {
        return date <= this.endDate.replace(/-/g, '/');
      }
      return true;
    },
    async getTableData() {
      this.loading = true;
      let param = {
        pageSize: this.pagination.rowsPerPage,
        index: this.pagination.page,
        problem: this.issueDesc,
        orderId: this.orderNo ? this.orderNo.id : this.orderNo,
        problemType: this.itemType ? this.itemType.key : this.itemType,
        dateField: this.dateField
          ? this.dateField.map(val => val.key)
          : this.dateField,
        startDate: this.startDate,
        endDate: this.endDate
      };
      const res = await resolveResponseError(() => queryCodeIssue(param));
      this.loading = false;
      this.tableLists = res.list;
      this.pagination.rowsNumber = res.count;
    },
    async handleExportExcel() {
      let param = {
        problem: this.issueDesc,
        orderId: this.orderNo ? this.orderNo.id : this.orderNo,
        problemType: this.itemType ? this.itemType.key : this.itemType,
        dateField: this.dateField
          ? this.dateField.map(val => val.key)
          : this.dateField,
        startDate: this.startDate,
        endDate: this.endDate
      };
      let res = await resolveResponseError(() => exportIssueList(param));
      exportExcel(res);
    },
    pageRequestData(props) {
      let { page, rowsPerPage, rowsNumber } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.getTableData();
    },
    beforeShow() {
      for (const key in this.memory) {
        this.memory[key] = this[key];
      }
    },
    stateDiff() {
      for (const key in this.memory) {
        this[key] !== this.memory[key] &&
          this[this.moreSearchMap[key]](this.memory[key]);
      }
    },
    moreSearchCancel() {
      // 与弹窗的初始值进行对比赋值，还原
      this.stateDiff();
      this.moreSearch = false;
    },
    moreSearchClick() {
      this.moreSearch = false;
      this.getTableData();
    },
    resetMoreSearch() {
      this.dateField.length !== 0 && this.saveDateType([]);
      this.startDate && this.saveStartDate('');
      this.endDate && this.saveEndDate('');
      this.moreSearch = false;
      this.$nextTick(() => this.getTableData());
    },
    async getOrderSelect() {
      this.orderList = await resolveResponseError(() => queryOrdersList());
    }
  },
  async created() {
    this.pagination = this.currentPage;
    this.currentDate = date.formatDate(Date.now(), 'YYYY-MM-DD');
    this.getTableData();
    this.getOrderSelect(); //查工单下拉列表
  },
  mounted() {
    const tempVisibleColumns = this.visibleCols;
    if (!this.visibleCols || this.visibleCols.length <= 1) {
      this.saveVisibleCols(tempVisibleColumns);
    }
  },
  beforeRouteLeave(to, from, next) {
    this.saveCurrentPage(this.pagination);
    next();
  }
};
</script>
<style lang="stylus" scoped>
.status-img {
  width: 8px;
  height: 8px;
  border-radius: 4px;
  background: #fff;
}

.status-style {
  display: flex;
  align-items: center;
}
</style>
