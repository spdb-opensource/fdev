<template>
  <!-- 等待层 -->
  <Loading :visible="loading">
    <f-block page>
      <fdev-table
        ref="table"
        row-key="id"
        title="需求列表"
        titleIcon="list_s_f"
        :data="data"
        :columns="columns"
        class="my-sticky-column-table"
        :loading="loading"
        :pagination.sync="pagination"
        :rows-per-page-options="perPageOptions"
        @request="pageDemandList"
        :on-search="findDemandList"
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
            class="q-ml-sm"
            ficon="search"
          />
        </template>

        <template v-slot:top-bottom>
          <f-formitem
            label="牵头/评估/创建人"
            class="col-4 q-pr-md"
            bottom-page
            label-style="width:110px"
          >
            <fdev-select
              use-input
              ref="userid"
              :value="userid"
              @input="updateUserid($event)"
              :options="userOptions"
              @filter="userFilter"
              @clear="handleClearFun"
              clearable
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">
                      {{ scope.opt.user_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label :title="scope.opt.user_name_en" caption>
                      {{ scope.opt.user_name_en }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>

          <f-formitem
            label="需求名称/编号"
            class="col-4 q-pr-md"
            bottom-page
            label-style="width:110px"
          >
            <fdev-input
              use-input
              use-chips
              :value="keyword"
              @keyup.enter="handleEnterFun()"
              @input="updateKeyword($event)"
              clearable
              @clear="clearKeyWordsFun()"
            >
            </fdev-input>
          </f-formitem>

          <f-formitem
            label="小组"
            class="col-4"
            bottom-page
            label-style="width:110px"
          >
            <fdev-select
              use-input
              multiple
              ref="groupid"
              :value="groupid"
              @input="updateGroupid($event)"
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
            label="状态"
            class="col-4 q-pr-md"
            bottom-page
            label-style="width:110px"
          >
            <fdev-select
              ref="states"
              multiple
              :value="states"
              @input="updateStates($event)"
              :options="demandStatusOptions"
            >
            </fdev-select>
          </f-formitem>

          <f-formitem
            label="需求类型"
            class="col-4 q-pr-md"
            bottom-page
            label-style="width:110px"
          >
            <fdev-select
              :value="demandType"
              @input="updateDemandType($event)"
              :options="demandTypeOptions"
            />
          </f-formitem>

          <f-formitem
            label="与我相关"
            class="col-4"
            bottom-page
            label-style="width:110px"
          >
            <fdev-select
              :value="relevant"
              @input="updateRelevant($event)"
              :options="checkRangeOptions"
            />
          </f-formitem>
        </template>

        <template v-slot:body-cell-oa_contact_name="props">
          <fdev-td class="text-ellipsis">
            <f-icon
              v-if="props.row.delayFlag == true"
              name="alert_t_f"
              style="color:red"
              title="延期告警！"
            />
            <span
              v-if="props.row.oa_contact_name"
              :title="props.row.oa_contact_name"
            >
              <router-link
                class="link"
                :to="`/rqrmn/rqrProfile/${props.row.id}`"
              >
                {{ props.row.oa_contact_name }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.oa_contact_name }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </span>
            <span v-else title="-"> -</span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-demand_type="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.demand_type | demandTypeFilter"
          >
            <span>{{ props.row.demand_type | demandTypeFilter }}</span>
          </fdev-td>
        </template>
        //需求标签
        <template v-slot:body-cell-demand_label_info="props">
          <fdev-td class="text-ellipsis" :title="getLabelName(props.value)">
            <span>{{ getLabelName(props.value) }}</span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-demand_status_normal="props">
          <fdev-td
            v-if="
              props.row.demand_status_special != 1 &&
                props.row.demand_status_special != 2
            "
            :title="props.row.demand_status_normal | statesFilter"
          >
            <f-status-color
              :gradient="props.row.demand_status_normal | demandStatusFilter"
            ></f-status-color>
            {{
              props.row.demand_status_normal
                | statesFilter
                | secondStatesFilter(props.row)
            }}
          </fdev-td>
          <fdev-td
            v-if="
              props.row.demand_status_special == 1 ||
                props.row.demand_status_special == 2
            "
            :title="props.row.demand_status_normal | statesFilter"
          >
            <f-status-color
              :gradient="props.row.demand_status_special | demandStatusFilter"
            ></f-status-color>
            {{ props.row.demand_status_special | demandStatusSpecialFilter }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-design_status="props">
          <fdev-td class="text-ellipsis">
            <!-- 其他状态 -->
            <f-status-color
              :gradient="props.row.design_status | designStatusFilter"
            ></f-status-color>
            <span
              v-if="
                props.row.design_status !== 'noRelate' &&
                  props.row.design_status !== 'abnormalShutdown' &&
                  !!props.row.design_status
              "
              :title="props.row.design_status | designFilter"
            >
              <router-link
                v-if="
                  props.row.design_status !== 'noRelate' &&
                    props.row.design_status !== 'abnormalShutdown' &&
                    !!props.row.design_status
                "
                class="link"
                :to="`/rqrmn/designReviewRqr/${props.row.id}`"
              >
                {{ props.row.design_status | designFilter }}
              </router-link>
            </span>
            <!-- 不涉及状态、异常关闭状态 -->
            <span v-else :title="props.row.design_status | designFilter">
              {{ props.row.design_status | designFilter }}</span
            >
          </fdev-td>
        </template>

        <template v-slot:body-cell-demand_leader_all="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.value.map(v => v.user_name_cn).join('，')"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link
                v-if="item.id"
                :to="{ path: `/user/list/${item.id}` }"
                class="link"
              >
                {{ item.user_name_cn }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.value.map(v => v.user_name_cn).join(' ') }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
              <span v-else class="span-margin">{{ item.user_name_cn }}</span>
            </span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-priority="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.priority | priorityFilter"
          >
            <span>{{ props.row.priority | priorityFilter }}</span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-demand_property="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.demand_property | demand_propertyFilter"
          >
            <span>{{ props.row.demand_property | demand_propertyFilter }}</span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-operation="props">
          <!-- 暂缓的需求不可评估，编辑 -->
          <fdev-td :auto-width="true" class="td-padding">
            <div class="border-right">
              <div
                class="inline-block"
                v-if="props.row.isTransferRqrmnt !== '1'"
                style="display: inline-block;"
                v-show="btnShowAble(props.row)"
              >
                <fdev-tooltip
                  v-if="
                    (props.row.demand_status_normal > 7 ||
                      props.row.demand_status_special === 1) &&
                      props.row.demand_status_special != 2
                  "
                >
                  <span>该阶段不可评估</span>
                </fdev-tooltip>
                <fdev-btn
                  :disable="
                    (props.row.demand_status_special === 1 ||
                      props.row.demand_status_normal > 7) &&
                      props.row.demand_status_special != 2
                  "
                  flat
                  label="评估"
                  class="q-mr-sm"
                  @click="assessmentBtnClick(props.row)"
                />
              </div>
              <div
                v-else
                class="inline-block"
                style="display: inline-block;"
                v-show="btnShowAble(props.row)"
              >
                <fdev-tooltip v-if="props.row.demand_status_normal === 8">
                  <span>已归档的存量需求不可评估</span>
                </fdev-tooltip>
                <fdev-btn
                  flat
                  label="评估"
                  class="q-mr-sm"
                  :disable="props.row.demand_status_normal === 8"
                  @click="assessmentBtnClick(props.row)"
                />
              </div>
              <div
                class="inline-block"
                style="display: inline-block;"
                v-if="editShowAble(props.row)"
              >
                <fdev-tooltip
                  v-if="
                    props.row.demand_status_normal > 7 ||
                      props.row.demand_status_special === 1 ||
                      props.row.demand_status_special === 2
                  "
                >
                  <span>该阶段不可编辑</span>
                </fdev-tooltip>
                <fdev-btn
                  flat
                  label="编辑"
                  class="q-mr-sm"
                  :disable="
                    props.row.demand_status_normal > 7 ||
                      props.row.demand_status_special === 1 ||
                      props.row.demand_status_special === 2
                  "
                  @click="handleDialogOpen(props.row.id)"
                />
              </div>
              <div
                class="inline-block"
                style="display: inline-block;"
                v-if="
                  props.row.demand_status_normal === 7 &&
                    (isDemandManager ||
                      isDemandLeader(props.row.demand_leader_all))
                "
              >
                <fdev-btn flat label="归档" @click="handleFile(props.row.id)" />
              </div>
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </f-block>

    <f-dialog
      title="更多查询条件"
      right
      v-model="moreSearch"
      @before-close="moreSearchCancel"
      @before-show="beforeShow"
    >
      <div class="q-gutter-y-lg">
        <f-formitem label="需求标签" diaS>
          <fdev-select
            multiple
            ref="demandLabel"
            :value="demandLabel"
            :options="labelOptions"
            @input="changeDemandLabel($event)"
            option-label="value"
            option-value="id"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="本组/本组及子组" diaS>
          <fdev-select
            ref="groupQueryType"
            :value="groupQueryType"
            @input="updateGroupQueryType($event)"
            :options="groupQueryTypeOptions"
            clearable
          />
        </f-formitem>

        <f-formitem label="小组类型" diaS>
          <fdev-select
            ref="groupState"
            :value="groupState"
            @input="updateGroupState($event)"
            :options="groupStateOptions"
            clearable
          >
          </fdev-select>
        </f-formitem>

        <f-formitem label="优先级" diaS>
          <fdev-select
            :value="priority"
            @input="updatePriority($event)"
            :options="priorityOptions"
          />
        </f-formitem>

        <f-formitem label="延期选项" diaS>
          <fdev-select
            ref="dateType"
            :value="datetype"
            @input="updateDatetype($event)"
            :options="dateTypeOptions"
            :clearable="true"
            :disable="isDateType"
          >
          </fdev-select>
        </f-formitem>

        <f-formitem label="超期天数" v-if="isDelayNum" diaS>
          <fdev-input
            type="number"
            ref="delayNum"
            :value="$v.delayNum.$model"
            @input="updateDelayNum($event.replace(/-/g, ''))"
          />
        </f-formitem>

        <f-formitem label="预进行选项" diaS>
          <fdev-select
            ref="featureType"
            :value="featureType"
            @input="updateFeatureType($event)"
            :options="featureTypeOptions"
            :disable="isFeatureType"
            :clearable="true"
          >
          </fdev-select>
        </f-formitem>

        <f-formitem label="未来N天" v-if="isfeatureNum" diaS>
          <fdev-input
            type="number"
            :value="$v.featureNum.$model"
            @input="updateFeatureNum($event.replace(/-/g, ''))"
            ref="featureNum"
            @keyup.enter="findDemandList"
          />
        </f-formitem>

        <f-formitem label="设计稿审核状态" diaS>
          <fdev-select
            ref="designState"
            :value="designState"
            @input="updateDesignState($event)"
            :options="designStateOptions"
            :clearable="true"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="是否提交代码审核" diaS>
          <fdev-select
            ref="isSubmitCodeReview"
            :value="isSubmitCodeReview"
            @input="updateIsSubmitCodeReview($event)"
            :options="isSubmitCodeReviewOption"
            :clearable="true"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem label="超过N天" v-if="isstateNum" diaS>
          <fdev-input
            type="number"
            ref="stateNum"
            :value="$v.stateNum.$model"
            @input="updatestateNum($event.replace(/-/g, ''))"
            @keyup.enter="findDemandList"
          />
        </f-formitem>

        <f-formitem label="实际日期类型" diaS>
          <fdev-select
            multiple
            ref="relDateType"
            :value="relDateType"
            @input="updateRelDateType($event)"
            :options="relDateTypeOptions"
          >
          </fdev-select>
        </f-formitem>

        <f-formitem label="实际开始日期" v-if="isRealDate" diaS>
          <f-date
            :value="relStartDate"
            ref="relStartDate"
            @input="changeRelStartDate($event)"
            mask="YYYY-MM-DD"
            :options="relStartDateOptions"
          >
          </f-date>
        </f-formitem>

        <f-formitem label="实际结束日期" v-if="isRealDate" diaS>
          <f-date
            ref="relEndDate"
            :value="relEndDate"
            @input="changeRelEndDate($event)"
            :options="relEndDateOptions"
            mask="YYYY-MM-DD"
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

    <updateDialog :rqr-id="id" v-model="dialogOpen" />
    <AssessmentDialog
      v-model="assessmentDialogShow"
      :data="detailData"
    ></AssessmentDialog>
    <f-dialog
      v-model="editFlag"
      transition-show="slide-up"
      transition-hide="slide-down"
      title="去编辑信息"
    >
      <span>
        {{ editFlaTip }}
      </span>

      <template v-slot:btnSlot>
        <fdev-btn label="去编辑" dialog @click="goRqrEdit()"
      /></template>
    </f-dialog>
  </Loading>
</template>
<script>
import Loading from '@/components/Loading';
import updateDialog from './Components/updateDialog';
import AssessmentDialog from './Components/AssessmentDialog';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import { formatUser } from '@/modules/User/utils/model';
import { queryDemandFile } from '@/modules/Rqr/services/methods.js';
import { resolveResponseError } from '@/utils/utils';
import {
  successNotify,
  deepClone,
  getGroupFullName,
  formatOption,
  exportExcel
} from '@/utils/utils';
import {
  delayTypeList,
  designStatusList,
  groupQueryTypeList,
  groupStateList,
  featureTypeList,
  demandStatusList,
  realDateTypeList,
  checkRangeList,
  demandTypeList,
  priorityList,
  demandTableColumns,
  demandStatus,
  demandStatusColorMap,
  designStatusMap,
  designStatusColorMap,
  priValue,
  demandStatusSpecial,
  createMemory,
  moreSearchMap,
  isSubmitCodeReviewOption
} from './model';
import {
  setTableCol,
  getTableCol,
  setDemandPagination,
  getDemandleagination
} from './setting';
export default {
  name: 'rqrList',
  components: { Loading, updateDialog, AssessmentDialog },
  data() {
    return {
      timer: '', // 防抖计时器
      id: '',
      loading: false,
      flag: false,
      dateTypeOptions: delayTypeList,
      dateDays: '',
      featureTypeOptions: featureTypeList,
      groups: [],
      groupOptions: [],
      userOptions: [],
      groupQueryTypeOptions: groupQueryTypeList,
      groupStateOptions: groupStateList,
      demandStatusOptions: demandStatusList,
      designStateOptions: designStatusList,
      isSubmitCodeReviewOption: isSubmitCodeReviewOption,
      relDateTypeOptions: realDateTypeList,
      demandTypeOptions: demandTypeList,
      priorityOptions: priorityList,
      checkRangeOptions: checkRangeList,
      deepCloneGroups: [],
      dialogOpen: false,
      users: [],
      perPageOptions: [5, 10, 15, 20, 30, 50, 100],
      pagination: {
        sortBy: '', //排序
        page: 1, //页码
        rowsPerPage: getDemandleagination(), //每页数据大小
        rowsNumber: 0 //数据库数据总条数
      },
      columns: demandTableColumns,
      data: [],
      editFlaTip: '', //弹出窗提示
      editFlag: false,
      assessmentDialogShow: false,
      detailData: {},
      moreSearch: false,
      memory: createMemory(),
      labelOptions: [],
      moreSearchMap
    };
  },
  validations: {
    stateNum: {
      onlyChart(value) {
        if (!/^\d+$/.test(value) && value) {
          return false;
        }
        return true;
      }
    },
    featureNum: {
      onlyChart(value) {
        if (!/^\d+$/.test(value) && value) {
          return false;
        }
        return true;
      }
    },
    delayNum: {
      onlyChart(value) {
        if (!/^\d+$/.test(value) && value) {
          return false;
        }
        return true;
      }
    }
  },
  computed: {
    ...mapState('userActionSaveDemands/requirementList', [
      'userid',
      'keyword',
      'groupid',
      'groupQueryType',
      'groupState',
      'datetype',
      'isDateType',
      'delayNum',
      'isDelayNum',
      'featureType',
      'featureNum',
      'isFeatureType',
      'designState',
      'isSubmitCodeReview',
      'states',
      'stateNum',
      'isstateNum',
      'relDateType',
      'isRealDate',
      'relStartDate',
      'relEndDate',
      'demandType',
      'priority',
      'relevant',
      'visibleColumns',
      'queryParam',
      'isfeatureNum',
      'demandLabel'
    ]),
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', {
      groupsData: 'groups'
    }),
    ...mapGetters('user', ['isDemandManager', 'isLoginUserList']),
    ...mapState('demandsForm', {
      demandList: 'demandList',
      demandListExcel: 'demandListExcel',
      demandLabelList: 'demandLabelList'
    }),
    ...mapState('dashboard', ['userList']),
    //判断当前用户是否是需求管理员
    // isRqrManager() {
    //   return this.isDemandManager;
    // },
    hasMoreSearch() {
      return (
        !this.moreSearch &&
        (!!this.groupQueryType ||
          !!this.groupState ||
          this.priority.value !== '' ||
          !!this.datetype ||
          this.delayNum !== '' ||
          !!this.featureType ||
          this.featureNum !== '' ||
          !!this.designState ||
          !!this.isSubmitCodeReview ||
          this.stateNum !== '' ||
          this.relDateType.length !== 0 ||
          this.demandLabel.length !== 0)
      );
    }
  },
  watch: {
    userid(val) {
      this.updateQueryParamUserid(val ? (val.id ? val.id : '') : '');
      val && this.findDemandList();
    },
    keyword(val) {
      this.updateQueryParamKeyword(val ? val : '');
    },
    datetype(val) {
      if (val) {
        this.updateIsDelayNum(true);
        this.updateQueryParamDatetype(val.value);
        this.updateIsFeatureType(true);
      } else {
        this.updateIsDelayNum(false);
        this.updateQueryParamDatetype('');
        this.updateDelayNum('');
        this.updateIsFeatureType(false);
      }
    },
    delayNum(val) {
      this.updateQueryParamDelayNum(val ? val : '0');
    },
    demandLabel(val) {
      let arr = [];
      val.map(item => {
        arr.push(item.code);
      });
      this.updateQueryParamDemandLable(arr);
    },
    featureType(val) {
      if (val) {
        this.updateIsfeatureNum(true);
        this.updateQueryParamFeatureType(val.value);
        this.updateIsDateType(true);
      } else {
        this.updateIsfeatureNum(false);
        this.updateQueryParamFeatureType('');
        this.updateFeatureNum('');
        this.updateQueryParamFeatureNum('');
        this.updateIsDateType(false);
      }
    },
    featureNum(val) {
      this.updateQueryParamFeatureNum(val ? val : '');
    },
    groupid(val) {
      if (val.length !== 0) {
        this.updateQueryParamGroupQueryType('1');
        let group = [];
        val.forEach(item => {
          group.push(item.id);
        });
        this.updateQueryParamGroupid(group);
      } else {
        this.updateQueryParamGroupid([]);
        this.updateGroupState('');
        this.updateQueryParamGroupState('');
        this.updateGroupQueryType('');
        this.updateQueryParamGroupQueryType('');
      }
      this.debounceFun(500);
    },
    groupQueryType(val) {
      this.updateQueryParamGroupQueryType(
        val ? (val.value ? val.value : '1') : ''
      );
    },
    groupState(val) {
      this.updateQueryParamGroupState(val ? (val.value ? val.value : '0') : '');
    },
    states(val) {
      if (val.length !== 0) {
        this.updateIsstateNum(true);
        let state = [];
        val.forEach(item => {
          state.push(item.value);
        });
        this.updateQueryParamStates(state);
      } else {
        this.updateIsstateNum(false);
        this.updateQueryParamStates([]);
        this.updatestateNum('');
        this.updateQueryParamStateNum('');
      }
      this.debounceFun(500);
    },
    stateNum(val) {
      this.updateQueryParamStateNum(val ? val : '');
    },
    designState(val) {
      this.updateQueryParamDesignState(val ? val.value : '');
    },
    isSubmitCodeReview(val) {
      this.updateQueryParamIsSubmitCodeReview(val ? val.value : '');
    },
    relDateType(val) {
      if (val.length !== 0) {
        this.updateIsRealDate(true);
        let relDateType = [];
        val.forEach(item => {
          relDateType.push(item.value);
        });
        this.updateQueryParamRelDateType(relDateType);
      } else {
        this.updateRelStartDate('');
        this.updateRelEndDate('');
        this.updateIsRealDate(false);
        this.updateQueryParamRelDateType([]);
      }
    },
    relStartDate(val) {
      this.updateQueryParamRelStartDate(val ? val : '');
    },
    relEndDate(val) {
      this.updateQueryParamRelEndDate(val ? val : '');
    },
    demandType(val) {
      this.updateQueryParamDemandType(val.value);
      if (this.flag) {
        return;
      }
      this.findDemandList();
    },
    priority(val) {
      this.updateQueryParamPriority(val.value);
      if (this.flag) {
        return;
      }
    },
    relevant(val) {
      this.updateQueryParamRelevant(val.value);
      if (this.flag) {
        return;
      }
      this.findDemandList();
    },
    visibleColumns(val) {
      setTableCol('demandListTable', val);
    },
    'pagination.rowsPerPage'(val) {
      setDemandPagination(val);
    }
  },
  filters: {
    demandStatusSpecialFilter(val) {
      return demandStatusSpecial[val];
    },
    demandTypeFilter(val) {
      const type = {
        tech: '科技需求',
        business: '业务需求',
        daily: '日常需求'
      };
      return type[val];
    },
    statesFilter(val) {
      return demandStatus[val] ? demandStatus[val] : '-';
    },
    // 二次过滤
    secondStatesFilter(val, row) {
      if (row.demand_type === 'daily') {
        if (val === '开发中') return '进行中';
        if (val === '已投产') return '已完成';
        return val;
      }
      return val;
    },
    designFilter(val) {
      return designStatusMap[val] ? designStatusMap[val] : '-';
    },
    designStatusFilter(val) {
      return designStatusColorMap[val];
    },
    demandStatusFilter(val) {
      return demandStatusColorMap[val];
    },
    priorityFilter(val) {
      return priValue[val] ? priValue[val] : '-';
    },
    demand_propertyFilter(val) {
      let obj1 = {
        advancedResearch: '预研',
        keyPoint: '重点',
        routine: '常规'
      };
      return obj1[val] ? obj1[val] : '-';
    }
  },
  methods: {
    ...mapMutations('userActionSaveDemands/requirementList', [
      'updateUserid',
      'updateKeyword',
      'updateGroupid',
      'updateGroupQueryType',
      'updateGroupState',
      'updateDatetype',
      'updateIsDateType',
      'updateDelayNum',
      'updateIsDelayNum',
      'updateFeatureType',
      'updateFeatureNum',
      'updateIsfeatureNum',
      'updateIsFeatureType',
      'updateDesignState',
      'updateIsSubmitCodeReview',
      'updateStates',
      'updatestateNum',
      'updateIsstateNum',
      'updateRelDateType',
      'updateIsRealDate',
      'updateRelStartDate',
      'updateRelEndDate',
      'updateDemandType',
      'updatePriority',
      'updateRelevant',
      'updateDemandLabel',
      'updateVisibleColumns',
      //查询条件
      'updateQueryParamDatetype',
      'updateQueryParamDelayNum',
      'updateQueryParamDesignState',
      'updateQueryParamIsSubmitCodeReview',
      'updateQueryParamFeatureNum',
      'updateQueryParamFeatureType',
      'updateQueryParamGroupid',
      'updateQueryParamGroupQueryType',
      'updateQueryParamGroupState',
      'updateQueryParamKeyword',
      'updateQueryParamPriority',
      'updateQueryParamRelDateType',
      'updateQueryParamRelEndDate',
      'updateQueryParamRelStartDate',
      'updateQueryParamRelevant',
      'updateQueryParamStates',
      'updateQueryParamStateNum',
      'updateQueryParamDemandType',
      'updateQueryParamUserid',
      'updateQueryParamIndex',
      'updateQueryParamSize',
      'updateQueryParamDescending',
      'updateQueryParamGroupid',
      'updateQueryParamDemandLable'
    ]),
    ...mapActions('demandsForm', [
      'queryDemandList',
      'fileRqr',
      'exportDemandsExcel',
      'queryByTypes'
    ]),
    ...mapActions('user', {
      queryUser: 'fetch',
      queryCurrent: 'fetchCurrent'
    }),
    ...mapActions('dashboard', ['queryUserCoreData']),
    ...mapActions('userForm', ['fetchGroup']),
    editShowAble(obj) {
      return this.isDemandManager || this.isDemandLeader(obj.demand_leader_all);
    },
    //日期
    changeRelStartDate(val) {
      this.updateRelStartDate(val);
    },
    changeRelEndDate(val) {
      this.updateRelEndDate(val);
    },
    changeDemandLabel(val) {
      this.updateDemandLabel(val);
    },
    btnShowAble(obj) {
      return (
        this.isDemandManager ||
        this.isDemandLeader(obj.demand_leader_all) ||
        this.isIncludeCurrentUser(obj.relate_part_detail)
      );
    },
    //判断用户集合中是否包含当前用户 ， 当前用户是否是需求牵头人,
    isDemandLeader(users) {
      if (users && Array.isArray(users)) {
        return users.some(user => {
          return user.id === this.currentUser.id;
        });
      }
    },
    isIncludeCurrentUser(partsListObj) {
      if (partsListObj && Array.isArray(partsListObj)) {
        return partsListObj.some(part => {
          return (
            part.assess_user &&
            part.assess_user.some(id => {
              return id === this.currentUser.id;
            })
          );
        });
      }
    },
    handleDialogOpen(id) {
      this.$router.push({
        path: `/rqrmn/rqrEdit/${id}`
      });
    },
    async handleFile(id) {
      let data = await resolveResponseError(() =>
        queryDemandFile({ id: this.demandModel.id })
      );
      if (data.length > 0) {
        this.$q
          .dialog({
            title: `需求文档缺失`,
            message: '缺失:' + '\t' + data.join('、') + '，请到需求文档库上传',
            ok: '确定',
            cancel: '取消'
          })
          .onOk(() => {
            //跳转链接到文档库
            this.$router.push({
              path: `/rqrmn/rqrProfile/${this.demandModel.id}`,
              query: { tabs: 'file' }
            });
          });
      } else {
        this.$q
          .dialog({
            title: `归档确认`,
            message: `确认要归档本条需求信息吗？`,
            ok: '确定',
            cancel: '取消'
          })
          .onOk(async () => {
            await this.fileRqr({ id: id });
            successNotify('归档成功！');
            this.findDemandList();
          });
      }
    },
    userFilter(val, update, abort) {
      update(() => {
        this.userOptions = this.users.filter(
          user =>
            user.user_name_cn.includes(val) || user.user_name_en.includes(val)
        );
      });
    },
    groupInputFilter(val, update) {
      update(() => {
        this.groupOptions = this.deepCloneGroups.filter(tag =>
          tag.label.includes(val)
        );
      });
    },
    async queryDemand(param) {
      this.loading = true;
      param.size = this.pagination.rowsPerPage;
      param.index = this.pagination.page;
      param.sortBy = this.pagination.sortBy;
      param.descending = this.pagination.descending;
      try {
        await this.queryDemandList(param);
        // 设置数据总条数
        this.pagination.rowsNumber = this.demandList.count;
        this.data = this.demandList.demands;
        this.pagination.sortBy = param.sortBy;
        this.pagination.descending = param.descending;
        this.flag = false;
        this.loading = false;
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
      let param = {
        ...this.queryParam
      };
      this.loading = true;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.pagination.descending = descending;
      this.pagination.sortBy = sortBy; //排序
      this.queryDemand(param);
    },
    // 清除用户刷新列表
    handleClearFun(val) {
      this.updateQueryParamUserid('');
      this.findDemandList();
    },
    // 清除需求名称/编号
    clearKeyWordsFun() {
      this.updateQueryParamKeyword('');
      this.findDemandList();
    },
    // 需求名称/编号回车事件
    handleEnterFun() {
      this.findDemandList();
    },
    // 防抖
    debounceFun(time) {
      if (this.timer) {
        clearTimeout(this.timer);
        this.timer = null;
        this.timer = setTimeout(() => {
          this.findDemandList();
        }, time);
      } else {
        this.timer = setTimeout(() => {
          this.findDemandList();
        }, time);
      }
    },
    findDemandList() {
      this.queryDemand(this.queryParam);
    },
    async handleExportExcel() {
      let param = {
        ...this.queryParam,
        size: 0
      };
      await this.exportDemandsExcel(param);
      exportExcel(this.demandListExcel);
    },
    relEndDateOptions(date) {
      this.updateRelStartDate(this.relStartDate ? this.relStartDate : '');
      return date >= this.relStartDate.replace(/-/g, '/');
    },
    relStartDateOptions(date) {
      if (this.relEndDate) {
        return date <= this.relEndDate.replace(/-/g, '/');
      }
      return true;
    },
    //判断涉及小组及涉及小组评估人
    isEditMessage(row) {
      if (row.relate_part_detail.length === 0) {
        return true;
      } else {
        return !row.relate_part_detail.some(
          item => item.assess_user.length > 0
        );
      }
    },
    //点击评估按钮
    assessmentBtnClick(row) {
      //若需求无牵头小组或实施牵头人，涉及小组及涉及小组评估人
      //当涉及板块有值为数组类型，没值的时候传null
      if (
        row.demand_leader_group == '' ||
        row.demand_leader.length === 0 ||
        row.relate_part.length === 0 ||
        this.isEditMessage(row)
      ) {
        this.editFlag = true;
        this.editFlaTip =
          '需求牵头小组、实施牵头人、涉及小组及涉及小组评估人信息不全，请前往编辑页面补充完整';
        this.editId = row.id;
      } else {
        this.$router.push(`/rqrmn/rqrProfile/${row.id}?tab=developNo`);
        // this.$router.push(`/rqrmn/rqrEvaluate/${row.id}`);
      }

      // if (!row.demand_plan_no && row.demand_type == 'business') {
      //   //业务需求 对应需求计划编号为空
      //   this.detailData = JSON.parse(JSON.stringify(row));
      //   if (!this.detailData.oa_real_no) {
      //     this.detailData.demand_plan_no = '1999其他项目001';
      //   }
      //   this.assessmentDialogShow = true;
      // } else {
      //   this.$router.push(`/rqrmn/rqrEvaluate/${row.id}`);
      // }
    },
    goRqrEdit(row) {
      this.$router.push(`/rqrmn/rqrEdit/${this.editId}`);
      this.editFlag = false;
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
      this.findDemandList();
    },
    resetMoreSearch() {
      this.groupQueryType !== '' && this.updateGroupQueryType('');
      this.groupState !== '' && this.updateGroupState('');
      this.priority.value !== '' &&
        this.updatePriority({ label: '全部', value: '' });
      this.datetype !== '' && this.updateDatetype('');
      this.delayNum !== '' && this.updateDelayNum('');
      this.featureType !== '' && this.updateFeatureType('');
      this.featureNum !== '' && this.updateFeatureNum('');
      this.designState !== '' && this.updateDesignState('');
      this.isSubmitCodeReview !== '' && this.updateIsSubmitCodeReview('');
      this.stateNum !== '' && this.updatestateNum('');
      this.relDateType.length !== 0 && this.updateRelDateType([]);
      this.demandLabel.length !== 0 && this.updateDemandLabel([]);
      this.moreSearch = false;
      this.$nextTick(() => this.findDemandList());
    },
    async getDemandLabel() {
      await this.queryByTypes({ types: ['demandLabel'] });
      this.labelOptions = deepClone(this.demandLabelList);
    },
    //获取标签名
    getLabelName(val) {
      let name = [];
      if (Array.isArray(val) && val.length > 0) {
        val.map(item => {
          if (item.flag) {
            name.push(item.label);
          }
        });
      }
      return name.length > 0 ? name.join(', ') : '-';
    }
  },
  async created() {
    this.loading = true;
    this.getDemandLabel();
    //初始化列表数据;
    this.queryDemand(this.queryParam);
    //初始化用户、小组列表
    Promise.all([
      // 小组
      this.fetchGroup(),
      //用户
      this.queryUserCoreData()
    ]).then(() => {
      // 处理小组下拉框数据
      this.groups = formatOption(this.groupsData);
      this.deepCloneGroups = deepClone(this.groups);
      this.deepCloneGroups.map(item => {
        let groupFullName = getGroupFullName(this.groups, item.id);
        item.label = groupFullName;
      });

      this.groupOptions = this.deepCloneGroups
        .concat([{ label: '小组', id: '' }])
        .filter(item => item.id);
      this.groupOptions.sort((a, b) => {
        return a.label.localeCompare(b.label, 'zh-CN');
      });
      // 处理用户下拉框数据
      let users = this.userList;
      this.users = users.map(user => formatOption(formatUser(user), 'name'));
      this.userOptions = this.users.slice(0);
      this.loading = false;
    });
  },
  mounted() {
    const tempVisibleColumns = this.visibleColumns;
    this.updateVisibleColumns(getTableCol('demandListTable'));
    if (!this.visibleColumns || this.visibleColumns.length <= 1) {
      this.updateVisibleColumns(tempVisibleColumns);
    }
  }
};
</script>
<style lang="stylus" scoped>
>>> .q-gutter-x-lg{
  width : 100%;
}
>>> .q-gutter-x-lg{
  margin-left: 0;
}
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
