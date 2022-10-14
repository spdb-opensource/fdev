<template>
  <!-- 等待层 -->
  <Loading :visible="loading">
    <div>
      <fdev-table
        ref="table"
        row-key="id"
        title="需求评估列表"
        titleIcon="list_s_f"
        :data="tableLists"
        :columns="columns"
        class="my-sticky-column-table"
        :pagination.sync="pagination"
        @request="pageDemandList"
        :export-func="handleExportExcel"
        :on-search="getEvaMgtLists"
        :visible-columns="visibleColumns"
        :onSelectCols="changSelect"
      >
        <template #top-right>
          <!-- 高级搜索 -->
          <!-- 高级搜索提示文字 -->
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
            class="q-ml-sm"
            ficon="search"
          />
          <fdev-btn
            v-if="isAdmin"
            normal
            ficon="add"
            label="新增"
            class="q-ml-sm"
            @click="isShowowCreateDlg = true"
          />
        </template>
        <template v-slot:top-bottom>
          <f-formitem
            label="需求名称/编号"
            class="col-4 q-pr-md"
            label-style="width:90px"
            bottom-page
          >
            <fdev-input
              use-input
              use-chips
              v-model="searchObj.oaContactNameNo"
              @blur="getEvaMgtLists"
              clearable
              @clear="getEvaMgtLists"
              @keyup.enter="getEvaMgtLists()"
            >
            </fdev-input>
          </f-formitem>
          <f-formitem
            label="牵头小组"
            class="col-4 q-pr-md"
            label-style="width:90px"
            bottom-page
          >
            <fdev-select
              use-input
              multiple
              ref="demandLeaderGroups"
              v-model="searchObj.demandLeaderGroups"
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
                    <fdev-item-label :title="scope.opt.label" caption>
                      {{ scope.opt.label }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem
            label="牵头人员"
            class="col-4"
            label-style="width:90px"
            bottom-page
          >
            <fdev-select
              use-input
              multiple
              ref="demandLeader"
              v-model="searchObj.demandLeader"
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
            label="优先级"
            class="col-4 q-pr-md"
            bottom-page
            label-style="width:90px"
          >
            <fdev-select
              clearable
              ref="priority"
              v-model="searchObj.priority"
              :options="priorityOptions"
            >
            </fdev-select>
          </f-formitem>

          <f-formitem
            label="评估天数"
            class="col-4 q-pr-md"
            bottom-page
            label-style="width:90px"
          >
            <fdev-select
              clearable
              ref="gteAssessDays"
              v-model="searchObj.gteAssessDays"
              :options="evadaysOptions"
            >
            </fdev-select>
          </f-formitem>
          <f-formitem
            label="评估状态"
            class="col-4"
            bottom-page
            label-style="width:90px"
          >
            <fdev-select
              clearable
              ref="demandStatus"
              v-model="searchObj.demandStatus"
              :options="statusOptions"
            >
            </fdev-select>
          </f-formitem>
        </template>
        <template v-slot:body-cell-oa_contact_name="props">
          <fdev-td>
            <div :title="props.row.oa_contact_name" class="text-ellipsis">
              <router-link
                :to="`/rqrmn/rqrEvaluateMgt/${props.row.id}`"
                class="link"
              >
                {{ props.row.oa_contact_name || '-' }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.oa_contact_name || '-' }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-demand_leader_group="props">
          <fdev-td>
            <div
              :title="props.row.demand_leader_group_cn"
              class="text-ellipsis"
            >
              {{ props.row.demand_leader_group_cn || '' }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.demand_leader_group_cn || '' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-demand_leader="props">
          <fdev-td
            class="text-ellipsis"
            :title="
              props.row.demand_leader_info
                ? props.row.demand_leader_info
                    .map(v => v.user_name_cn)
                    .join('，')
                : null
            "
          >
            <span
              v-for="(item, index) in props.row.demand_leader_info"
              :key="index"
            >
              <router-link
                v-if="item.id"
                :to="{ path: `/user/list/${item.id}` }"
                class="link"
              >
                {{ item.user_name_cn }}
              </router-link>
              <span v-else class="span-margin">{{ item.user_name_cn }}</span>

              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{
                    props.row.demand_leader_info
                      ? props.row.demand_leader_info
                          .map(v => v.user_name_cn)
                          .join(' ')
                      : '-'
                  }}
                </fdev-banner>
              </fdev-popup-proxy>
            </span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-demand_status="props">
          <fdev-td>
            <div
              v-if="props.row.demand_status"
              :title="props.row.demand_status_name"
            >
              <f-status-color
                :gradient="props.row.demand_status_name | statusFilter"
              ></f-status-color>
              {{ props.row.demand_status_name }}
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-priority="props">
          <fdev-td>
            <div v-if="props.row.priority" :title="props.row.priority">
              {{ props.row.priority }}
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-assess_days="props">
          <fdev-td>
            <div :title="props.row.assess_days">
              {{ toString(props.row.assess_days) }}
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-overdue_type="props">
          <fdev-td>
            <div
              v-if="props.row.overdue_type_cn"
              :title="props.row.overdue_type_cn"
            >
              {{ props.row.overdue_type_cn }}
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <!-- 需求文档状态 -->
        <template v-slot:body-cell-conf_state="props">
          <fdev-td>
            <div v-if="props.row.conf_state" :title="props.row.conf_state_cn">
              {{ props.row.conf_state_cn }}
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-operation="props">
          <fdev-td :auto-width="true" class="td-padding">
            <div class="opEdit">
              <!-- 编辑 -->
              <span>
                <fdev-tooltip v-if="isDisableBtn(props.row)">
                  <span>{{ getErrorMsg(props.row) }}</span>
                </fdev-tooltip>
                <fdev-btn
                  flat
                  label="编辑"
                  :disable="isDisableBtn(props.row)"
                  @click="OnClickEditDlg(props.row)"
                />
              </span>
              <!-- 撤销 -->
              <span class="lflex q-mx-xs"> </span>
              <span>
                <fdev-tooltip v-if="isDisableBtn(props.row)">
                  <span>{{ getErrorMsg(props.row) }}</span>
                </fdev-tooltip>
                <fdev-btn
                  flat
                  label="撤销"
                  :disable="isDisableBtn(props.row)"
                  @click="delEvaMgt(props.row)"
                />
              </span>
              <!-- 暂缓：1：权限-》需求管理员、需求牵头人;2:只有评估中才能点击暂缓;暂缓中才能取消暂缓-->
              <span class="lflex q-mx-xs"> </span>
              <span>
                <fdev-tooltip v-if="isDisableBtn(props.row)">
                  <span>{{ getErrorMsg(props.row) }}</span>
                </fdev-tooltip>
                <fdev-tooltip v-if="isAfterToday(props.row)">
                  <span>当前时间小于起始评估日期不能暂缓</span>
                </fdev-tooltip>
                <!-- 取消暂缓 -->
                <fdev-btn
                  flat
                  label="取消暂缓"
                  :disable="isDisableBtn(props.row)"
                  @click="recover(props.row)"
                  v-if="props.row.demand_status === 3"
                />
                <!-- 暂缓 -->
                <fdev-btn
                  v-else
                  flat
                  label="暂缓"
                  :disable="isAfterToday(props.row) || isDisableBtn(props.row)"
                  @click="handleDefer(props.row)"
                />
              </span>
              <!-- 分析完成：1:权限：后端配置文件配置的指定人员 2:只有分析中才能完成分析-->
              <span class="lflex q-mx-xs" v-if="props.row.confirmStatus">
              </span>
              <!-- 无权限的人员不展示按钮,不在分析中状态都不能操作 -->
              <span v-if="props.row.confirmStatus">
                <fdev-tooltip v-if="props.row.demand_status !== 1">
                  <span>此状态不可操作</span>
                </fdev-tooltip>
                <fdev-btn
                  flat
                  label="分析完成"
                  :disable="props.row.demand_status !== 1"
                  @click="Complete(props.row)"
                />
              </span>
              <!-- 定稿日期修改：所有状态（分析中、分析完成、撤销、暂缓）都能定稿，从conflunce同步之后不允许修改 -->
              <!-- 权限：与编辑的权限一样,没有权限不展示 -->
              <span
                class="lflex q-mx-xs"
                v-if="props.row.operate_flag !== 'noshow'"
              >
              </span>
              <span v-if="props.row.operate_flag !== 'noshow'">
                <fdev-btn
                  flat
                  label="定稿"
                  @click="Finalize(props.row)"
                  :disable="props.row.final_date_status === 2"
                />
                <fdev-tooltip v-if="props.row.final_date_status === 2">
                  <span>已从conflunce同步不能手动定稿</span>
                </fdev-tooltip>
              </span>
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </div>
    <!-- 高级搜索弹窗 -->
    <f-dialog
      title="更多查询条件"
      v-model="moreSearch"
      @before-close="moreSearchCancel"
    >
      <!-- conflunce新增的三个查询条件：需求文档状态、需求进行中天数、需求定稿后天数 -->
      <f-formitem
        label="需求文档状态"
        class="col-4 q-pr-md"
        bottom-page
        label-style="width:90px"
      >
        <fdev-select
          clearable
          option-label="value"
          option-value="code"
          ref="confState"
          v-model="moreParamsData.confState"
          :options="confType"
          hint=""
        >
        </fdev-select>
      </f-formitem>
      <f-formitem
        label="需求提出不低于天数"
        class="col-4 q-pr-md"
        bottom-page
        label-style="width:90px"
      >
        <fdev-input
          v-model="moreParamsData.goingDays"
          @keyup.enter="moreSearchClick()"
          :rules="[
            () => $v.moreParamsData.goingDays.regValue || '只能输入大于0的整数'
          ]"
        >
        </fdev-input>
      </f-formitem>
      <f-formitem
        label="需求定稿后不低于天数"
        class="col-4 q-pr-md"
        bottom-page
        label-style="width:90px"
      >
        <fdev-input
          v-model="moreParamsData.finalDays"
          @keyup.enter="moreSearchClick()"
          :rules="[
            () => $v.moreParamsData.finalDays.regValue || '只能输入大于0的整数'
          ]"
        >
        </fdev-input>
      </f-formitem>
      <!-- 超期分类 -->
      <f-formitem
        label="超期分类"
        class="col-4 q-pr-md"
        bottom-page
        label-style="width:90px"
      >
        <fdev-select
          clearable
          option-label="value"
          option-value="code"
          ref="overdueType"
          v-model="moreParamsData.overdueType"
          :options="overdueCalOptions"
        >
        </fdev-select>
      </f-formitem>
      <template #btnSlot>
        <fdev-btn label="清空" outline dialog @click="resetMoreSearch"/>
        <fdev-btn label="查询" dialog @click="moreSearchClick"/></template
    ></f-dialog>
    <!-- 新增弹窗 -->
    <createDialog
      v-model="isShowowCreateDlg"
      @close="closeCreateDlg"
    ></createDialog>
    <!-- 修改弹窗 -->
    <updateDialog
      :overdueCalOptions="overdueCalOptions"
      :id="curObject.id"
      v-model="isShowUpdateDlg"
      @close="closeUpdateDlg"
    ></updateDialog>
    <!-- 上传文件弹窗 -->
    <uploadFile
      ref="uploadFile"
      :demandId="curObject.id"
      @reFresh="reFresh"
    ></uploadFile>
    <!-- 完成评估弹窗 -->
    <complete
      ref="complete"
      :id="curObject.id"
      :startTime="curObject.start_assess_date"
      @reFresh="reFresh"
    />
    <!-- 修改定稿日期弹窗 -->
    <finalize
      ref="finalize"
      :id="curObject.id"
      :status="curObject.final_date_status"
      @reFresh="reFresh"
    ></finalize>
  </Loading>
</template>
<script>
import Loading from '@/components/Loading';
import updateDialog from './components/updateDialog';
import createDialog from './components/createDialog';
import uploadFile from './components/uploadFile';
import complete from './components/complete';
import finalize from './components/finalize';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import {
  priorityOptions,
  queryUserOptionsParams,
  rqrEvaluateStatusColorMap
} from '../../model';
import {
  getTableCol,
  setTableCol,
  setTableFilter,
  getTableFilter,
  setEvaPagination,
  getEvapagination
} from '../../setting';
import {
  successNotify,
  errorNotify,
  deepClone,
  getGroupFullName,
  formatOption,
  resolveResponseError,
  exportExcel as exportExcelFromUtils
} from '@/utils/utils';

import {
  queryEvaMgtList,
  deleteData,
  getOverdueTypeSelect,
  exportExcel,
  cancelDefer //取消暂缓
} from '@/modules/Rqr/services/methods';
import moment from 'moment';
export default {
  name: 'rqrList',
  components: {
    Loading,
    updateDialog,
    createDialog,
    uploadFile,
    complete,
    finalize
  },
  watch: {
    visibleColumns(val) {
      setTableCol('rqr/evaMgtList', val);
    },
    'searchObj.demandLeaderGroups': {
      handler(val) {
        if (this.loading) return;
        this.getEvaMgtLists();
      }
    },
    'searchObj.demandLeader': {
      handler(val) {
        if (this.loading) return;
        this.getEvaMgtLists();
      }
    },
    'searchObj.priority': {
      handler(val) {
        if (this.loading) return;
        this.getEvaMgtLists();
      }
    },
    'searchObj.gteAssessDays': {
      handler(val) {
        if (this.loading) return;
        this.getEvaMgtLists();
      }
    },
    'searchObj.demandStatus': {
      handler(val) {
        if (this.loading) return;
        this.getEvaMgtLists();
      }
    },
    'pagination.rowsPerPage'(val) {
      setEvaPagination(val);
    }
  },
  data() {
    return {
      loading: false,
      pagination: {
        sortBy: '', //排序
        page: 1, //页码
        rowsPerPage: getEvapagination(), //每页数据大小
        rowsNumber: 0 //数据库数据总条数
      },
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
          copy: true
        },
        {
          name: 'demand_leader_group',
          label: '牵头小组',
          field: 'demand_leader_group',
          copy: true
        },
        {
          name: 'demand_leader',
          label: '牵头人员',
          field: 'demand_leader',
          copy: true
        },
        {
          name: 'demand_status',
          label: '评估状态',
          field: 'demand_status'
        },
        {
          name: 'priority',
          label: '优先级',
          field: 'priority'
        },
        {
          name: 'overdue_type',
          label: '超期分类',
          field: 'overdue_type'
        },
        {
          name: 'start_assess_date',
          label: '起始评估日期',
          field: 'start_assess_date'
        },
        {
          name: 'end_assess_date',
          label: '完成评估日期',
          field: 'end_assess_date'
        },
        {
          name: 'assess_days',
          label: '评估天数',
          field: 'assess_days'
        },
        {
          name: 'conf_state',
          label: '需求文档状态',
          field: 'conf_state'
        },
        {
          name: 'final_date',
          label: '定稿日期',
          field: 'final_date'
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
        oaContactNameNo: '', //需求名称、编号
        demandLeaderGroups: [], //牵头小组
        demandLeader: [], //牵头人员
        priority: '', //优先级
        gteAssessDays: null, //评估天数
        demandStatus: { label: '分析中', value: 1 } //评估状态
      },
      groups: [],
      groupOptions: [], //牵头小组下拉选项
      userOptions: [], //牵头人员下拉选项
      priorityOptions: priorityOptions, //优先级
      overdueCalOptions: [],
      confType: [], //需求文档状态
      //1、3、5、7、10、14、30
      evadaysOptions: [
        { value: '1', label: '>=1天' },
        { value: '3', label: '>=3天' },
        { value: '5', label: '>=5天' },
        { value: '7', label: '>=7天' },
        { value: '10', label: '>=10天' },
        { value: '14', label: '>=14天' },
        { value: '30', label: '>=1个月' }
      ],
      statusOptions: [
        { label: '分析中', value: 1 },
        { label: '分析完成', value: 2 },
        { label: '暂缓中', value: 3 },
        { label: '撤销', value: 9 }
      ], //，分析中:1，分析完成:2，撤销:9
      visibleColumns: this.visibleColumnEvaMgtOptions,
      isShowowCreateDlg: false,
      isShowUpdateDlg: false,
      curObject: {},
      moreSearch: !true //是否显示高级搜索
    };
  },
  validations: {
    moreParamsData: {
      goingDays: {
        //numeric
        regValue(val) {
          if (!val) {
            return true;
          }
          if (val > 0 && /^[1-9]\d*$/.test(val)) {
            return true;
          } else {
            return false;
          }
        }
      },
      finalDays: {
        regValue(val) {
          if (!val) {
            return true;
          }
          if (val > 0 && /^[1-9]\d*$/.test(val)) {
            return true;
          } else {
            return false;
          }
        }
      }
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', {
      groupsData: 'groups'
    }),
    ...mapGetters('user', ['isDemandManager']),
    ...mapState('userActionSaveDemands/evaluateList', ['moreParamsData']),
    ...mapState('dashboard', ['userList']),
    isAdmin() {
      return this.isDemandManager;
    },
    // 高级搜索提示文字
    hasMoreSearch() {
      return (
        !this.moreSearch &&
        (this.moreParamsData.confState != null ||
          (this.moreParamsData.goingDays != null &&
            this.moreParamsData.goingDays != '') ||
          (this.moreParamsData.finalDays != null &&
            this.moreParamsData.finalDays != '') ||
          this.moreParamsData.overdueType != null)
      );
    }
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('dashboard', ['queryUserCoreData']),
    ...mapMutations('userActionSaveDemands/evaluateList', [
      'updateConfState',
      'updateGoingDays',
      'updateFinalDays',
      'updateOverdueType'
    ]),
    //日期
    changeRelStartDate(val) {
      this.updateRelStartDate(val);
    },
    changeRelEndDate(val) {
      this.updateRelEndDate(val);
    },
    // 超期分类
    async queryOverdueTypeSelect() {
      this.overdueCalOptions = await resolveResponseError(() =>
        getOverdueTypeSelect({
          type: 'overdueType'
        })
      );
    },
    // 需求文档状态
    async queryConfType() {
      this.confType = await resolveResponseError(() =>
        getOverdueTypeSelect({
          type: 'confState'
        })
      );
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
      if (this.searchObj.oaContactNameNo)
        queryParam.oaContactNameNo = this.searchObj.oaContactNameNo;
      if (this.searchObj.priority)
        queryParam.priority = this.searchObj.priority.value;
      if (this.moreParamsData.overdueType)
        // 超期天数
        queryParam.overdueType = this.moreParamsData.overdueType.code;
      if (this.searchObj.gteAssessDays)
        queryParam.gteAssessDays = this.searchObj.gteAssessDays.value;
      if (this.searchObj.demandStatus)
        queryParam.demandStatus = this.searchObj.demandStatus.value;
      if (this.searchObj.demandLeaderGroups.length > 0)
        queryParam.demandLeaderGroups = this.searchObj.demandLeaderGroups.map(
          item => {
            return item.value;
          }
        );
      if (this.searchObj.demandLeader.length > 0)
        queryParam.demandLeader = this.searchObj.demandLeader.map(item => {
          return item.value;
        });
      // 需求文档状态
      if (this.moreParamsData.confState) {
        queryParam.confState = this.moreParamsData.confState.code;
      }
      // 需求进行中天数
      if (this.moreParamsData.goingDays) {
        queryParam.goingDays = this.moreParamsData.goingDays;
      }
      // 需求定稿后天数
      if (this.moreParamsData.finalDays) {
        queryParam.finalDays = this.moreParamsData.finalDays;
      }
      return queryParam;
    },
    async getEvaMgtLists() {
      this.loading = true;
      let queryParam = this.getParam();
      queryParam.pageSize = this.pagination.rowsPerPage;
      queryParam.pageNum = this.pagination.page;
      if ((queryParam.finalDays || queryParam.goingDays) && !this.checkNum()) {
        this.loading = false;
        return;
      }
      try {
        const res = await queryEvaMgtList(queryParam);
        this.loading = false;
        this.tableLists = res.data;
        this.pagination.rowsNumber = res.total;
      } catch (e) {
        this.loading = false;
      }
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
      this.getEvaMgtLists();
    },
    async handleExportExcel() {
      let queryParam = this.getParam();
      let res = await resolveResponseError(() => exportExcel(queryParam));
      exportExcelFromUtils(res);
    },
    groupInputFilter(val, update) {
      update(() => {
        this.groupOptions = this.groups.filter(tag => tag.label.includes(val));
      });
    },
    userFilter(val, update, abort) {
      update(() => {
        this.userOptions = this.users.filter(
          user =>
            user.user_name_cn.includes(val) || user.user_name_en.includes(val)
        );
      });
    },
    changSelect(clos) {
      this.visibleColumns = clos;
    },
    closeCreateDlg($event) {
      this.isShowowCreateDlg = false;
      if ($event) {
        this.getEvaMgtLists();
      }
    },
    closeUpdateDlg($event) {
      this.isShowUpdateDlg = false;
      if ($event) {
        this.getEvaMgtLists();
      }
    },
    // 不可操作提示的文字
    getErrorMsg(row) {
      if (row.operate_flag == 'noshow') {
        return '无权限操作';
      } else {
        return '此状态不可编辑';
      }
    },
    // 判断操作按钮是否展示
    isDisableBtn(row) {
      if (row.operate_flag == 'show') {
        return false;
      } else if (row.operate_flag == 'noshow') {
        return true;
      } else {
        return true;
      }
    },
    //判断若还未到起始评估日期则“暂缓”和“分析完成”按钮不能点击
    isAfterToday(row) {
      const today = moment(new Date()).format('YYYY/MM/DD');
      return moment(row.start_assess_date).format('YYYY/MM/DD') > today;
    },
    async delEvaMgt(row) {
      this.$q
        .dialog({
          title: `确认撤销`,
          message: `是否确认撤销本需求？`,
          ok: '是',
          cancel: '否'
        })
        .onOk(async () => {
          const res = await deleteData({ id: row.id });
          if (res && res.code && res.code != 'AAAAAAA') {
            // 失败
            errorNotify(res.msg);
          } else {
            // 成功
            successNotify('撤销成功!');
            this.getEvaMgtLists();
          }
        });
    },
    // 点击确认暂缓，打开上传文件弹窗
    handleDefer(row) {
      this.curObject = row;
      this.$refs.uploadFile.openDialog();
    },
    //取消暂缓
    recover(row) {
      this.$q
        .dialog({
          title: `确认取消暂缓`,
          message: `是否取消暂缓？`,
          ok: '是',
          cancel: '否'
        })
        .onOk(async () => {
          await resolveResponseError(() => cancelDefer({ id: row.id }));
          successNotify('恢复成功!');
          //刷新列表
          this.getEvaMgtLists();
        });
    },
    // 分析完成
    Complete(row) {
      this.curObject = row;
      this.$refs.complete.openDilaog();
    },
    // 修改定稿日期
    Finalize(row) {
      this.curObject = row;
      this.$refs.finalize.openDilaog();
    },
    // 刷新列表
    reFresh() {
      this.getEvaMgtLists();
    },
    // 打开编辑弹窗
    OnClickEditDlg(row) {
      this.isShowUpdateDlg = true;
      this.curObject = row;
    },
    // 高级搜索重置
    resetMoreSearch() {
      this.moreSearch = false;
      // 清空、更新高级搜索项
      this.updateConfState(null);
      this.updateGoingDays(null);
      this.updateFinalDays(null);
      this.updateOverdueType(null);
      this.getEvaMgtLists();
    },
    // 高级搜索查询
    moreSearchClick() {
      // 校验不通过不发接口
      if (!this.checkNum()) {
        return;
      }
      this.moreSearch = false;
      this.getEvaMgtLists();
    },
    //需求提出天数、定稿天数加上校验
    checkNum() {
      // 时间未填不发接口
      if (
        this.$v.moreParamsData.finalDays.$invalid ||
        this.$v.moreParamsData.goingDays.$invalid
      ) {
        this.barChartLoading = false;
        return false;
      }
      return true;
    },
    // 关闭高级搜索弹窗时操作
    moreSearchCancel() {
      // 如果校验不通过，关闭弹窗前清空字段
      if (this.$v.moreParamsData.finalDays.$invalid) {
        this.updateFinalDays(null);
      }
      if (this.$v.moreParamsData.goingDays.$invalid) {
        this.updateGoingDays(null);
      }
    },
    //原进原出处理,离开页面之前保存数据
    saveData() {
      setTableFilter('rqr/evaMgtFilter', JSON.stringify(this.searchObj));
    }
  },
  async created() {
    this.loading = true;
    let filterObj = getTableFilter('rqr/evaMgtFilter');
    if (filterObj) {
      this.searchObj = JSON.parse(filterObj);
    }
    //初始化列表数据;
    this.getEvaMgtLists();
    //初始化用户列表
    let params = {
      company_id: queryUserOptionsParams.company_id,
      status: queryUserOptionsParams.status
    };
    Promise.all([
      // 小组
      this.fetchGroup(),
      //用户（只查行内的用户）
      this.queryUserCoreData(params),
      this.queryOverdueTypeSelect(),
      // 查询需求文档状态
      this.queryConfType()
    ]).then(() => {
      // 处理小组数据
      const temGroups = formatOption(this.groupsData);
      this.groups = deepClone(temGroups);
      this.groups.map(item => {
        let groupFullName = getGroupFullName(temGroups, item.id);
        item.label = groupFullName;
      });
      // 处理团队数据
      this.users = this.userList.map(user =>
        formatOption(user, 'user_name_cn')
      );
      this.loading = false;
    });
  },
  mounted() {
    const tempVisibleColumns = this.visibleColumnEvaMgtOptions;
    this.visibleColumns = getTableCol('rqr/evaMgtList');
    if (!this.visibleColumns || this.visibleColumns.length <= 2) {
      this.visibleColumns = tempVisibleColumns;
    }
  },
  filters: {
    statusFilter(val) {
      return rqrEvaluateStatusColorMap[val];
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
.lflex{
  border-left:1px solid #DDDDDD;
  height: 14px;
}
</style>
