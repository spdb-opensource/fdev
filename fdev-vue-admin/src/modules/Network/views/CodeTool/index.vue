<template>
  <!-- 等待层 -->
  <f-block page>
    <!-- :export-func="handleExportExcel" -->
    <Loading :visible="loading">
      <fdev-table
        ref="table"
        row-key="id"
        title="代码审核工单列表"
        titleIcon="list_s_f"
        :data="tableLists"
        :columns="columns"
        class="my-sticky-column-table"
        :pagination.sync="pagination"
        @request="pageDemandList"
        :on-search="getOrdersList"
        :visible-columns="visibleColumns"
        :onSelectCols="changSelect"
        :export-func="handleExportExcel"
      >
        <template #top-right>
          <span>
            <fdev-tooltip
              v-if="isDisableBtn(addButton, 1)"
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>{{ getErrorMsg(addButton, 1) }}</span>
            </fdev-tooltip>
            <fdev-btn
              :disable="isDisableBtn(addButton, 1)"
              normal
              ficon="add"
              label="申请代码审核"
              @click="jumpToAdd"
            />
          </span>
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
            label="牵头人"
            class="col-4 q-pr-md"
            label-style="width:110px"
            bottom-page
          >
            <fdev-select
              use-input
              clearable
              v-model="searchObj.leader"
              :options="userOptions"
              @filter="userFilter"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">
                      {{ scope.opt.user_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label
                      :title="
                        `${scope.opt.user_name_en}--${scope.opt.groupName}`
                      "
                      caption
                    >
                      {{ scope.opt.user_name_en }}--{{ scope.opt.groupName }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template></fdev-select
            >
          </f-formitem>
          <f-formitem
            label="工单编号"
            class="col-4 q-pr-md"
            label-style="width:110px"
            bottom-page
          >
            <fdev-input
              use-input
              use-chips
              v-model="searchObj.orderNo"
              @blur="getOrdersList"
              clearable
              @clear="getOrdersList"
              @keyup.enter="getOrdersList()"
            >
            </fdev-input>
          </f-formitem>
          <f-formitem
            label="牵头小组"
            class="col-4"
            label-style="width:110px"
            bottom-page
          >
            <fdev-select
              use-input
              clearable
              ref="leaderGroup"
              v-model="searchObj.leaderGroup"
              :options="groupOptions"
              @filter="groupInputFilter"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name">{{
                      scope.opt.name
                    }}</fdev-item-label>
                    <fdev-item-label :title="scope.opt.label" caption>
                      {{ scope.opt.label }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem
            label="工单状态"
            class="col-4 q-pr-md"
            bottom-page
            label-style="width:110px"
          >
            <fdev-select
              multiple
              clearable
              v-model="searchObj.orderStatusMap"
              :options="statusOptions"
              option-label="label"
              option-value="value"
            >
            </fdev-select>
          </f-formitem>
          <f-formitem
            label="需求名称/编号"
            class="col-4 q-pr-md"
            label-style="width:110px"
            bottom-page
          >
            <fdev-select
              clearable
              use-input
              option-label="oa_contact_name"
              option-value="id"
              v-model="searchObj.demandId"
              :options="demandOptions"
              @filter="rqrmntInputFilter"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.oa_contact_name">
                      {{ scope.opt.oa_contact_name }}
                    </fdev-item-label>
                    <fdev-item-label
                      :title="`${scope.opt.oa_contact_no}`"
                      caption
                    >
                      {{ scope.opt.oa_contact_no }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem
            label="创建人"
            class="col-4"
            label-style="width:110px"
            bottom-page
          >
            <fdev-select
              use-input
              clearable
              v-model="searchObj.createUser"
              :options="userOptions"
              @filter="userFilter"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">
                      {{ scope.opt.user_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label
                      :title="
                        `${scope.opt.user_name_en}--${scope.opt.groupName}`
                      "
                      caption
                    >
                      {{ scope.opt.user_name_en }}--{{ scope.opt.groupName }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template></fdev-select
            >
          </f-formitem>
          <f-formitem
            label="与我相关"
            bottom-page
            class="col-4"
            label-style="width:110px"
          >
            <fdev-toggle
              :value="aboutMe"
              @input="showAboutMe($event)"
              left-label
            />
          </f-formitem>
        </template>
        <template v-slot:body-cell-orderNo="props">
          <fdev-td>
            <div :title="props.row.orderNo" class="text-ellipsis">
              <router-link :to="`/aAndA/codeTool/${props.row.id}`" class="link">
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
        <template v-slot:body-cell-leaderName="props">
          <fdev-td>
            <div class="text-ellipsis" :title="props.row.leaderName">
              <router-link
                v-if="props.row.leader"
                :to="{ path: `/user/list/${props.row.leader}` }"
                class="link"
              >
                {{ props.row.leaderName }}
              </router-link>
              <span v-else class="span-margin">{{ props.row.leaderName }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.leaderName }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-operation="props">
          <fdev-td :auto-width="true" class="td-padding">
            <div class="opEdit ">
              <span>
                <fdev-tooltip
                  v-if="isDisableBtn(props.row, 2)"
                  anchor="top middle"
                  self="center middle"
                  :offest="[-20, 0]"
                >
                  <span>{{ getErrorMsg(props.row, 2) }}</span>
                </fdev-tooltip>
                <fdev-btn
                  :disable="isDisableBtn(props.row, 2)"
                  flat
                  label="编辑"
                  @click="jumpToEdit(props.row)"
                />
              </span>
              <span class="lflex q-mx-xs"> </span>
              <span>
                <fdev-tooltip
                  v-if="isDisableBtn(props.row, 3)"
                  anchor="top middle"
                  self="center middle"
                  :offest="[-20, 0]"
                >
                  <span>{{ getErrorMsg(props.row, 3) }}</span>
                </fdev-tooltip>
                <fdev-btn
                  flat
                  label="删除"
                  :disable="isDisableBtn(props.row, 3)"
                  @click="delOrderById(props.row)"
                />
              </span>
            </div>
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
            ref="dateField"
            :value="dateField"
            @input="updateDateType($event)"
            :options="dateTypeOptions"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="开始日期" v-if="isRealDate" diaS>
          <f-date
            :value="startDate"
            ref="startDate"
            @input="changeStartDate($event)"
            mask="YYYY-MM-DD"
            :options="startDateOptions"
          >
          </f-date>
        </f-formitem>
        <f-formitem label="结束日期" v-if="isRealDate" diaS>
          <f-date
            :value="endDate"
            ref="endDate"
            @input="changeEndDate($event)"
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
import { mapState, mapActions, mapMutations } from 'vuex';
import {
  perform,
  queryUserOptionsParams,
  statusOptions,
  dateTypeList,
  createMemory,
  moreSearchMap
} from '@/modules/Network/utils/constants';
import {
  getTableCol,
  setTableCol,
  setTableFilter,
  getTableFilter,
  setCodeToolPagination,
  getCodeToolEvapagination
} from '@/modules/Network/utils/setting';
import {
  successNotify,
  deepClone,
  getGroupFullName,
  formatOption,
  resolveResponseError,
  exportExcel
} from '@/utils/utils';
import {
  exportOrderExcel,
  queryOrders,
  deleteOrderById
} from '@/modules/Network/services/methods';
import { queryUserCoreData } from '@/modules/Rqr/services/methods';
export default {
  name: 'orderLists',
  components: { Loading },
  //离开页面之前保存数据
  beforeRouteLeave(to, from, next) {
    setTableFilter('codeTool/listFilter', JSON.stringify(this.searchObj));
    next();
  },
  watch: {
    visibleColumns(val) {
      setTableCol('codeTool/listColumns', val);
    },
    'searchObj.leader': {
      handler(val) {
        if (this.loading) return;
        this.getOrdersList();
      }
    },
    'searchObj.createUser': {
      handler(val) {
        if (this.loading) return;
        this.getOrdersList();
      }
    },
    'searchObj.leaderGroup': {
      handler(val) {
        if (this.loading) return;
        this.getOrdersList();
      }
    },
    'searchObj.orderStatusMap': {
      handler(val) {
        if (this.loading) return;
        this.getOrdersList();
      }
    },
    'searchObj.demandId': {
      handler(val) {
        if (this.loading) return;
        this.getOrdersList();
      }
    },
    aboutMe: {
      handler(val) {
        if (this.loading) return;
        this.getOrdersList();
      }
    },
    'pagination.rowsPerPage'(val) {
      setCodeToolPagination(val);
    },
    dateField(val) {
      if (val.length !== 0) {
        this.updateIsRealDate(true);
      } else {
        this.updateStartDate('');
        this.updateEndDate('');
        this.updateIsRealDate(false);
      }
    }
  },
  data() {
    return {
      loading: false,
      ...perform,
      pagination: {
        sortBy: '', //排序
        descending: false,
        page: 1, //页码
        rowsPerPage: getCodeToolEvapagination(), //每页数据大小
        rowsNumber: 0 //数据库数据总条数
      },
      columns: [
        {
          name: 'orderNo',
          label: '工单编号',
          field: 'orderNo',
          copy: true
        },
        {
          name: 'orderStatus',
          label: '工单状态',
          field: 'orderStatus',
          sortable: true
        },
        {
          name: 'demandName',
          label: '需求名称',
          field: 'demandName',
          required: true,
          copy: true
        },
        {
          name: 'leaderGroupCn',
          label: '牵头小组',
          field: 'leaderGroupCn',
          copy: true
        },
        {
          name: 'leaderName',
          label: '牵头人',
          field: 'leaderName',
          copy: true
        },
        {
          name: 'applyTime',
          label: '申请时间',
          field: 'applyTime',
          sortable: true
        },
        {
          name: 'systemNames',
          label: '涉及系统',
          field: 'systemNames'
        },
        {
          name: 'expectAuditDate',
          label: '期望审核日期',
          field: 'expectAuditDate',
          sortable: true
        },
        {
          name: 'auditFinishTime',
          label: '审核完成时间',
          field: 'auditFinishTime',
          sortable: true
        },
        {
          name: 'planProductDate',
          label: '计划投产日期',
          field: 'planProductDate',
          sortable: true
        },
        {
          name: 'operation',
          label: '操作',
          field: 'operation',
          required: true,
          align: 'left'
        }
      ],
      tableLists: [],
      searchObj: {
        leader: [], //牵头人员
        orderNo: '', //工单名称、编号
        leaderGroup: [], //牵头小组
        orderStatusMap: [], //工单状态
        demandId: '', //需求名称、编号
        applyTime: '', //申请时间,
        createUser: [] //创建人
      },
      groups: [],
      groupOptions: [], //牵头小组下拉选项
      userOptions: [], //牵头人员下拉选项
      statusOptions: statusOptions,
      visibleColumns: this.visibleColumnCodeToolOptions,
      addButton: '1', //新增按钮，true亮，false置灰
      demandOptions: [],
      moreSearch: false,
      dateTypeOptions: dateTypeList,
      memory: createMemory(),
      moreSearchMap
    };
  },
  validations: {},
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', {
      groupsData: 'groups'
    }),
    ...mapState('jobForm', ['rqrmntsList']),
    ...mapState('userActionSaveNetwork/codeTool', [
      'endDate',
      'startDate',
      'dateField',
      'isRealDate',
      'aboutMe'
    ]),
    hasMoreSearch() {
      return !this.moreSearch && this.dateField.length !== 0;
    }
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('jobForm', ['queryRqrmnts']),
    ...mapMutations('userActionSaveNetwork/codeTool', [
      'updateDateType',
      'updateStartDate',
      'updateEndDate',
      'updateIsRealDate',
      'showAboutMe'
    ]),
    //日期
    changeStartDate(val) {
      this.updateStartDate(val);
    },
    changeEndDate(val) {
      this.updateEndDate(val);
    },
    getStatusColor(status) {
      if (status == '1') {
        return '#FEC400';
      } else if (status == '2') {
        return '#02E01A ';
      } else if (status == '3') {
        return '#02E01A ';
      } else if (status == '4') {
        return '#02E01A ';
      } else if (status == '5') {
        return '#42A5F5 ';
      } else if (status == '6') {
        return '#42A5F5 ';
      } else if (status == '7') {
        return '#42A5F5 ';
      } else if (status == '8') {
        return '#b0bec5 ';
      } else {
        return '#FEC400';
      }
    },
    toString(val) {
      if (val == 0 || val) {
        return String(val);
      } else {
        return '-';
      }
    },
    getParam() {
      let queryParam = {};
      //牵头人
      if (this.searchObj.leader)
        queryParam.leader = this.searchObj.leader.value;
      //创建人
      if (this.searchObj.createUser)
        queryParam.createUser = this.searchObj.createUser.value;
      //工单名称编号
      if (this.searchObj.orderNo) queryParam.orderNo = this.searchObj.orderNo;
      //牵头小组
      if (this.searchObj.leaderGroup)
        queryParam.leaderGroup = this.searchObj.leaderGroup.value;
      //工单状态
      if (
        this.searchObj.orderStatusMap &&
        this.searchObj.orderStatusMap.length > 0
      ) {
        queryParam.orderStatus = this.searchObj.orderStatusMap.map(
          item => item.value
        );
      }
      //需求名称/编号
      if (this.searchObj.demandId)
        queryParam.demandId = this.searchObj.demandId.id;
      //申请时间
      if (this.searchObj.applyTime)
        queryParam.applyTime = this.searchObj.applyTime;
      //与我相关
      queryParam.aboutMe = this.aboutMe;
      //console.log('queryParam', JSON.stringify(queryParam));
      //时间类型、开始时间、结束时间
      if (this.dateField.length !== 0) {
        let relDateType = [];
        this.dateField.forEach(item => {
          relDateType.push(item.value);
        });
        queryParam.dateField = relDateType;
        queryParam.startDate = this.startDate;
        queryParam.endDate = this.endDate;
      }
      return queryParam;
    },
    async handleExportExcel() {
      let queryParam = this.getParam();
      let res = await resolveResponseError(() => exportOrderExcel(queryParam));
      exportExcel(res);
    },
    async getOrdersList() {
      this.loading = true;
      let queryParam = this.getParam();
      queryParam.pageSize = this.pagination.rowsPerPage;
      queryParam.index = this.pagination.page;
      if (this.pagination.sortBy) {
        queryParam.sortBy = this.pagination.sortBy;
        queryParam.descending = this.pagination.descending;
      }
      //console.log('queryParam', JSON.stringify(queryParam));
      const res = await resolveResponseError(() => queryOrders(queryParam));
      this.loading = false;
      //console.log('res', JSON.stringify(res));
      //测试代码
      this.tableLists = res.orderList;
      this.pagination.rowsNumber = res.count;
      this.addButton = res.addButton;
    },
    pageDemandList(props) {
      let {
        page,
        rowsPerPage,
        rowsNumber,
        sortBy,
        descending
      } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.pagination.descending = descending;
      this.pagination.sortBy = sortBy; //排序
      this.getOrdersList();
    },
    groupInputFilter(val, update) {
      update(() => {
        this.groupOptions = this.groups.filter(
          tag => tag.label.indexOf(val) > -1
        );
      });
    },
    rqrmntInputFilter(val, update) {
      update(() => {
        this.demandOptions = this.rqrmntsList.filter(tag => {
          return (
            tag.oa_contact_name.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            tag.oa_contact_no.toLowerCase().indexOf(val.toLowerCase()) > -1
          );
        });
      });
    },
    userFilter(val, update, abort) {
      update(() => {
        this.userOptions = this.users.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.indexOf(val) > -1
        );
      });
    },
    changSelect(clos) {
      this.visibleColumns = clos;
    },
    jumpToAdd() {
      this.$router.push({
        path: '/aAndA/addOrders',
        query: {}
      });
    },
    jumpToEdit(item) {
      this.$router.push({
        path: `/aAndA/updateOrders/${item.id}`,
        query: {}
      });
    },
    getErrorMsg(row, type) {
      if (type == 1) {
        //新增
        if (row == '1') {
          return '不可以操作';
        } else {
          return '';
        }
      } else if (type == '2') {
        //编辑
        if (row.updateButton === '3') {
          return '终态下非代码审核角色不可编辑';
        } else {
          return '';
        }
      } else {
        //删除
        if (row.deleteButton === '1') {
          return '工单终态下不可删除';
        } else if (row.deleteButton === '2') {
          return '工单下有会议记录不可删除';
        } else {
          return '';
        }
      }
    },
    isDisableBtn(row, type) {
      if (type == 1) {
        //新增
        if (row == '1') {
          return true;
        } else {
          return false;
        }
      } else if (type == '2') {
        //编辑
        if (row.updateButton == '3') {
          return true;
        } else {
          return false;
        }
      } else {
        //删除
        if (row.deleteButton !== '0') {
          return true;
        } else {
          return false;
        }
      }
    },
    getStatusName(val) {
      for (let i = 0; i < this.statusOptions.length; i++) {
        if (val == this.statusOptions[i].value) {
          return this.statusOptions[i].label;
        }
      }
      return '';
    },
    async delOrderById(row) {
      this.$q
        .dialog({
          title: `确认删除`,
          message: `是否确认删除此工单？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await resolveResponseError(() => deleteOrderById({ id: row.id }));
          successNotify('删除成功!');
          this.getOrdersList();
        });
    },
    endDateOptions(date) {
      this.updateStartDate(this.startDate ? this.startDate : '');
      return date >= this.startDate.replace(/-/g, '/');
    },
    startDateOptions(date) {
      if (this.endDate) {
        return date <= this.endDate.replace(/-/g, '/');
      }
      return true;
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
      this.getOrdersList();
    },
    resetMoreSearch() {
      this.dateField.length !== 0 && this.updateDateType([]);
      this.moreSearch = false;
      this.$nextTick(() => this.getOrdersList());
    }
  },
  async created() {
    this.loading = true;
    let filterObj = getTableFilter('codeTool/listFilter');
    if (filterObj) {
      this.searchObj = JSON.parse(filterObj);
    }
    //初始化用户列表
    //分组获取
    //初始化列表数据;
    this.getOrdersList();
    await this.fetchGroup();
    const temGroups = formatOption(this.groupsData);
    this.groups = deepClone(temGroups);
    this.groups.map(item => {
      let groupFullName = getGroupFullName(temGroups, item.id);
      item.label = groupFullName;
    });

    // 需求列表
    await this.queryRqrmnts();
    this.demandOptions = this.rqrmntsList;

    let params = {
      status: queryUserOptionsParams.status
    };
    const res = await queryUserCoreData(params);
    this.users = res.map(user => formatOption(user, 'user_name_cn'));

    this.loading = false;
  },
  mounted() {
    const tempVisibleColumns = this.visibleColumnCodeToolOptions;
    this.visibleColumns = getTableCol('codeTool/listColumns');
    if (!this.visibleColumns || this.visibleColumns.length <= 2) {
      this.visibleColumns = tempVisibleColumns;
    }
  }
};
</script>
<style lang="stylus" scoped>
.opEdit {
  display: flex;
  align-items: center;
  vertical-align: center;
}

.lflex {
  border-left: 1px solid #DDDDDD;
  height: 14px;
}

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
