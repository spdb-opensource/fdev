<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        :data="scanRecordList.list"
        :columns="columns"
        row-key="id"
        class="bg-white q-mt-md my-sticky-column-table"
        @request="init"
        :on-search="init"
        :pagination.sync="pagination"
        no-data-label="没有可用数据，请输入查询条件"
        :visible-columns="visibleColumns"
      >
        <template v-slot:top-bottom>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="应用英文名">
            <fdev-select
              use-input
              clearable
              type="text"
              option-label="name_en"
              option-value="name_en"
              :options="filterProject"
              ref="service_id"
              :value="service_id"
              @input="updateServiceId($event)"
              @filter="projectFilter"
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
          <f-formitem class="col-4 q-pr-sm" bottom-page label="应用所属小组">
            <fdev-select
              use-input
              option-label="name"
              option-value="id"
              map-options
              emit-value
              clearable
              ref="GroupId"
              @filter="filterGroup"
              :options="groupOptions"
              :value="GroupId"
              @input="updateGroupId($event)"
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
          <f-formitem class="col-4 q-pr-sm" bottom-page label="分支">
            <fdev-input
              ref="branch"
              :value="branch"
              @input="updateBranch($event)"
              @keyup.enter="init"
              type="text"
            />
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="触发扫描方式">
            <fdev-select
              input-debounce="0"
              :options="typeList"
              emit-value
              map-options
              option-label="label"
              option-value="value"
              :value="type"
              @input="updateType($event)"
            />
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="开始日期">
            <f-date
              @input="onTableRequestStartTime($event)"
              :options="startTimeOptions"
              :value="StartTime"
              mask="YYYY-MM-DD"
            />
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="结束日期">
            <f-date
              @input="onTableRequestEndTime($event)"
              :options="endTimeOptions"
              :value="EndTime"
              mask="YYYY-MM-DD"
            />
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="扫描成功">
            <fdev-toggle
              :value="IsScanSuccessFlag"
              @input="updateIsScanSuccessFlag($event)"
              left-label
              true-value="1"
              false-value="0"
            />
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="最近扫描">
            <fdev-toggle
              :value="RecentlyScanFlag"
              @input="updateRecentlyScanFlag($event)"
              left-label
              true-value="1"
              false-value="0"
            />
          </f-formitem>
        </template>
        <template v-slot:top-left>
          <fdev-chip
            color="positive"
            text-color="white"
            icon="check"
            label="扫描成功"
            size="xs"
          />
          <fdev-chip
            color="red"
            text-color="white"
            icon="close"
            label="扫描失败"
            size="xs"
          />
          <fdev-chip
            color="teal"
            text-color="white"
            icon="remove"
            label="应用不涉及"
            size="xs"
          />
        </template>
        <template v-slot:body-cell-serviceId="props">
          <fdev-td class="text-ellipsis">
            <router-link
              v-if="props.row.appId"
              :to="{
                path: `/app/list/${props.row.appId}-interface`
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
        <template v-slot:body-cell-rest="props">
          <fdev-td auto-width>
            <fdev-tooltip
              anchor="top middle"
              self="bottom middle"
              :offest="[0, 0]"
              v-if="props.value.code === '2'"
            >
              <span>{{ props.value.msg }}</span>
            </fdev-tooltip>
            <span v-if="props.value.code === '0'">-</span>
            <f-icon
              v-else
              :color="colorFilter(props.value.code)"
              size="20px"
              :name="iconFilter(props.value.code)"
            />
          </fdev-td>
        </template>
        <template v-slot:body-cell-restRel="props">
          <fdev-td auto-width>
            <fdev-tooltip
              anchor="top middle"
              self="bottom middle"
              :offest="[0, 0]"
              v-if="props.value.code === '2'"
            >
              <span>{{ props.value.msg }}</span>
            </fdev-tooltip>
            <span v-if="props.value.code === '0'">-</span>
            <f-icon
              v-else
              :color="colorFilter(props.value.code)"
              size="20px"
              :name="iconFilter(props.value.code)"
            />
          </fdev-td>
        </template>
        <template v-slot:body-cell-soap="props">
          <fdev-td auto-width>
            <fdev-tooltip
              anchor="top middle"
              self="bottom middle"
              :offest="[0, 0]"
              v-if="props.value.code === '2'"
            >
              <span>{{ props.value.msg }}</span>
            </fdev-tooltip>
            <span v-if="props.value.code === '0'">-</span>
            <f-icon
              v-else
              :color="colorFilter(props.value.code)"
              size="20px"
              :name="iconFilter(props.value.code)"
            />
          </fdev-td>
        </template>
        <template v-slot:body-cell-soapRel="props">
          <fdev-td auto-width>
            <fdev-tooltip
              anchor="top middle"
              self="bottom middle"
              :offest="[0, 0]"
              v-if="props.value.code === '2'"
            >
              <span>{{ props.value.msg }}</span>
            </fdev-tooltip>
            <span v-if="props.value.code === '0'">-</span>
            <f-icon
              v-else
              :color="colorFilter(props.value.code)"
              size="20px"
              :name="iconFilter(props.value.code)"
            />
          </fdev-td>
        </template>
        <template v-slot:body-cell-sopRel="props">
          <fdev-td auto-width>
            <fdev-tooltip
              anchor="top middle"
              self="bottom middle"
              :offest="[0, 0]"
              v-if="props.value.code === '2'"
            >
              <span>{{ props.value.msg }}</span>
            </fdev-tooltip>
            <span v-if="props.value.code === '0'">-</span>
            <f-icon
              v-else
              :color="colorFilter(props.value.code)"
              size="20px"
              :name="iconFilter(props.value.code)"
            />
          </fdev-td>
        </template>
        <template v-slot:body-cell-trans="props">
          <fdev-td auto-width>
            <fdev-tooltip
              anchor="top middle"
              self="bottom middle"
              :offest="[0, 0]"
              v-if="props.value.code === '2'"
            >
              <span>{{ props.value.msg }}</span>
            </fdev-tooltip>
            <span v-if="props.value.code === '0'">-</span>
            <f-icon
              v-else
              :color="colorFilter(props.value.code)"
              size="20px"
              :name="iconFilter(props.value.code)"
            />
          </fdev-td>
        </template>
        <template v-slot:body-cell-transRel="props">
          <fdev-td auto-width>
            <fdev-tooltip
              anchor="top middle"
              self="bottom middle"
              :offest="[0, 0]"
              v-if="props.value.code === '2'"
            >
              <span>{{ props.value.msg }}</span>
            </fdev-tooltip>
            <span v-if="props.value.code === '0'">-</span>
            <f-icon
              v-else
              :color="colorFilter(props.value.code)"
              size="20px"
              :name="iconFilter(props.value.code)"
            />
          </fdev-td>
        </template>
        <template v-slot:body-cell-router="props">
          <fdev-td
            auto-width
            :class="props.value.code === '2' ? 'hover-cursor' : ''"
          >
            <fdev-tooltip
              anchor="top middle"
              self="bottom middle"
              :offest="[0, 0]"
              v-if="props.value.code === '2'"
            >
              <span>{{ props.value.msg }}</span>
            </fdev-tooltip>
            <span v-if="props.value.code === '0'">-</span>
            <f-icon
              v-else
              :color="colorFilter(props.value.code)"
              size="20px"
              :name="iconFilter(props.value.code)"
            />
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import { typeList, scanRecordColumns } from '../../utils/constants';
import { errorNotify } from '@/utils/utils';
import { setPagination, getPagination } from '../../utils/setting';
export default {
  name: 'ScanRecord',
  components: { Loading },
  data() {
    return {
      loading: false,
      typeList: typeList,
      pagination: {
        rowsPerPage: getPagination().rowsPerPage || 5,
        rowsNumber: 0,
        page: 1
      },
      columns: scanRecordColumns(),
      filterProject: [],
      groupOptions: [],
      scanOptions: [
        { label: '', value: '' },
        { label: '否', value: '0' },
        { label: '是', value: '1' }
      ],
      scanSucOptions: [
        { label: '', value: '' },
        { label: '否', value: '0' },
        { label: '是', value: '1' }
      ]
    };
  },
  watch: {
    'pagination.rowsPerPage': {
      handler(val) {
        setPagination({
          rowsPerPage: val
        });
      },
      deep: true
    },
    service_id(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
    GroupId(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
    branch(val) {
      if (!val) {
        this.init();
      }
    },
    type(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
    StartTime(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
    EndTime(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
    IsScanSuccessFlag(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
    RecentlyScanFlag(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    }
  },
  computed: {
    ...mapState('userActionSaveInterface/dynamic/scanningRecords', [
      'service_id',
      'GroupId',
      'branch',
      'type',
      'StartTime',
      'EndTime',
      'IsScanSuccessFlag',
      'RecentlyScanFlag',
      'visibleColumns'
    ]),
    ...mapState('userForm', {
      groups: 'groups'
    }),
    ...mapState('interfaceForm', ['scanRecordList']),
    ...mapState('appForm', ['vueAppData'])
  },
  methods: {
    ...mapMutations('userActionSaveInterface/dynamic/scanningRecords', [
      'updateServiceId',
      'updateGroupId',
      'updateBranch',
      'updateType',
      'updateStartTime',
      'updateEndTime',
      'updateIsScanSuccessFlag',
      'updateRecentlyScanFlag',
      'updateVisibleColumns'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('interfaceForm', ['queryScanRecord']),
    ...mapActions('appForm', ['queryApps']),
    startTimeOptions(date) {
      if (this.EndTime) {
        return date < this.EndTime.replace(/-/g, '/');
      }
      return true;
    },
    endTimeOptions(date) {
      if (this.StartTime) {
        return date > this.StartTime.replace(/-/g, '/');
      }
      return true;
    },
    onTableRequestStartTime(event) {
      this.updateStartTime(event);
      // this.$refs.qDateProxyStart.hide();
      // this.$refs.qDateProxyEnd.hide();
    },
    onTableRequestEndTime(event) {
      this.updateEndTime(event);
      // this.$refs.qDateProxyStart.hide();
      // this.$refs.qDateProxyEnd.hide();
    },
    filterGroup(val, update, abort) {
      update(() => {
        this.groupOptions = this.groups.filter(
          tag => tag.name.indexOf(val) > -1
        );
      });
    },
    async downloadScanRecord() {
      let params = {
        branch: this.branch,
        type: this.type,
        IsScanSuccessFlag: this.IsScanSuccessFlag,
        RecentlyScanFlag: this.RecentlyScanFlag,
        service_id: this.service_id ? this.service_id.name_en : '',
        StartTime: this.StartTime ? this.StartTime : '',
        EndTime: this.EndTime ? this.EndTime : '',
        GroupId: this.GroupId ? this.GroupId : ''
      };
      const baseUrl = window.location.href.split('fdev')[0];
      const downUrl = `${baseUrl}finterface/api/interface/downloadScanRecordFile`;
      if (
        params.service_id == '' &&
        params.branch == '' &&
        params.type == '' &&
        params.StartTime == '' &&
        params.EndTime == '' &&
        params.GroupId == '' &&
        params.IsScanSuccessFlag == '0' &&
        params.RecentlyScanFlag == '0'
      ) {
        errorNotify('请输入导出条件');
      } else {
        window.open(
          `${downUrl}?service_id=${params.service_id}&branch=${
            params.branch
          }&type=${params.type}&StartTime=${params.StartTime}&EndTime=${
            params.EndTime
          }&GroupId=${params.GroupId}&IsScanSuccessFlag=${
            params.IsScanSuccessFlag
          }&RecentlyScanFlag=${params.RecentlyScanFlag}`
        );
      }
    },
    /* 处理图表数据 */
    async initBarChartData(type = 'bar', stack = 'stack') {
      this.$refs.qDateProxyStart.hide();
      this.$refs.qDateProxyEnd.hide();
      if (this.userSelected.length === 0) {
        this.chartData = {
          xAxis: [],
          xAxisId: [],
          left: {},
          right: {}
        };
        return;
      }
      this.stackBarChartLoading = true;
      await this.queryTaskNumByUserIdsDate({
        user_ids: this.userSelected.map(item => item.id),
        roles: this.roles,
        ...this.listModel
      });
      const xAxisId = Object.keys(this.taskNumByUserIdsDate);
      const xAxis = xAxisId.map(id => {
        return this.getUserName(id);
      });
      this.chartData = {
        left: this.taskNumByUserIdsDate,
        xAxisId,
        xAxis
      };
      this.$refs.doubleBarchart.draw();
      this.stackBarChartLoading = false;
    },
    async init(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      let params = {
        branch: this.branch,
        type: this.type,
        IsScanSuccessFlag: this.IsScanSuccessFlag,
        RecentlyScanFlag: this.RecentlyScanFlag,
        service_id: this.service_id ? this.service_id.name_en : '',
        StartTime: this.StartTime ? this.StartTime : '',
        EndTime: this.EndTime ? this.EndTime : '',
        GroupId: this.GroupId ? this.GroupId : '',
        page: this.pagination.page,
        page_num: this.pagination.rowsPerPage
      };
      // if (
      //   params.service_id == '' &&
      //   params.branch == '' &&
      //   params.type == '' &&
      //   params.StartTime == '' &&
      //   params.EndTime == '' &&
      //   params.GroupId == '' &&
      //   params.IsScanSuccessFlag == '0' &&
      //   params.RecentlyScanFlag == '0'
      // ) {
      //   errorNotify('请输入查询条件');
      // } else {
      this.loading = true;
      await this.queryScanRecord(params);
      this.pagination.rowsNumber = this.scanRecordList.total;
      this.loading = false;
      // }
    },
    iconFilter(val) {
      if (val === '0') {
        // 横线
        return 'remove';
      } else if (val === '1') {
        return 'check'; // 对勾,成功
        // return 'checkmark'; // 对勾，失败
      } else {
        return 'close'; // 错误
      }
    },
    colorFilter(val) {
      if (val === '0') {
        return 'grey-7';
      } else if (val === '1') {
        return 'green';
      } else {
        return 'red';
      }
    },
    projectFilter(val, update) {
      update(() => {
        this.filterProject = this.vueAppData.filter(
          tag =>
            tag.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            tag.name_zh.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    }
  },
  async created() {
    this.queryApps();
    this.filterProject = this.vueAppData;
    this.fetchGroup().then(res => {
      this.groupOptions = this.groups;
    });
    let params = {
      branch: this.branch,
      type: this.type,
      IsScanSuccessFlag: this.IsScanSuccessFlag,
      RecentlyScanFlag: this.RecentlyScanFlag,
      service_id: this.service_id ? this.service_id.name_en : '',
      StartTime: this.StartTime ? this.StartTime : '',
      EndTime: this.EndTime ? this.EndTime : '',
      GroupId: this.GroupId ? this.GroupId : '',
      page: this.pagination.page,
      page_num: this.pagination.rowsPerPage
    };
    await this.queryScanRecord(params);
    this.pagination.rowsNumber = this.scanRecordList.total;
  }
};
</script>

<style lang="stylus" scoped>
.input
  min-width 150px
.from-with
  width 100%
.hover-cursor:hover
  cursor pointer
.w150
  width 150px
</style>
