<template>
  <div>
    <Loading :visible="loading">
      <div class="items-start q-gutter-md">
        <fdev-table
          class="my-sticky-column-table"
          :data="planTableData"
          :columns="columns"
          row-key="id"
          noExport
          :rows-per-page-options="[0]"
          hide-bottom
        >
          <template v-slot:top-left>
            <f-formitem
              profile
              v-if="demandType != 'daily'"
              style="align-self: baseline"
              label="实施单元"
              label-auto
              label-style="padding-right:32px"
              value-style="width:280px;"
            >
              <fdev-select
                v-model="selectedUnitVisible"
                clearable
                @input="queryUnitList($event)"
                @filter="unitVisibleFun"
                :options="UnitOptions"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.implUnitNum">{{
                        scope.opt.implUnitNum
                      }}</fdev-item-label>
                      <fdev-item-label caption :title="scope.opt.implContent">
                        {{ scope.opt.implContent }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <!-- 其他需求任务编号 -->
            <f-formitem
              v-else
              style="align-self: baseline"
              label="其他需求任务"
              label-style="width:90px;"
              value-style="width:280px;"
              profile
            >
              <fdev-select
                v-model="otherDemandTaskNum"
                clearable
                :options="otherDemandTaskNumOptions"
                option-label="taskNum"
                option-value="taskNum"
                emit-value
                @input="updateList(groupId, $event)"
              >
              </fdev-select>
            </f-formitem>
          </template>
          <template v-slot:top-bottom-opt>
            <f-formitem
              profile
              style="align-self: baseline;"
              label="涉及小组"
              :label-style="relatePartLabelStyle"
              value-style="width:1000px"
            >
              <!-- label-style="padding-right:32px" value-style="width:1000px" -->
              <fdev-radio
                v-for="item in demandModel.relate_part_detail"
                :key="item.part_id"
                v-model="groupId"
                :val="item.part_id"
                @input="partItemClick(item)"
                style="margin-right:20px"
                :label="
                  `${item.part_name}(${unitStatusMap(item.assess_status)})`
                "
              />
            </f-formitem>
          </template>
          <template v-slot:top-right>
            <span
              v-if="
                demandDetail &&
                  demandDetail.demand_type &&
                  demandDetail.demand_type !== 'daily'
              "
            >
              <fdev-tooltip v-if="flagInfo.code === '2'" position="top">
                {{ flagInfo.msg }}
              </fdev-tooltip>
              <fdev-btn
                v-if="flagInfo.code === '0' || flagInfo.code === '2'"
                :disable="flagInfo.code === '2' ? true : false"
                normal
                label="新建提测单"
                ficon="add"
                @click="handleSubmitTestOpen"
              />
            </span>
            <!--2021/09/16 定的  新增研发单元   添加接口  判断显示逻辑 -->
            <span>
              <fdev-btn
                :disable="isShowMsg.length > 0"
                normal
                ficon="add"
                label="新增研发单元"
                @click="handleImplUnitOpen"
              />
              <fdev-tooltip v-if="isShowMsg.length > 0">{{
                isShowMsg
              }}</fdev-tooltip>
            </span>
          </template>
          <template v-slot:body-cell-create_user_all="props">
            <fdev-td
              :title="
                props.row.create_user_all &&
                  props.row.create_user_all.user_name_cn
              "
            >
              {{
                props.row.create_user_all &&
                  props.row.create_user_all.user_name_cn
              }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{
                    props.row.create_user_all &&
                      props.row.create_user_all.user_name_cn
                  }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>
          <template v-slot:body-cell-implement_leader_all="props">
            <fdev-td>
              <div
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
                  </router-link>
                  <span v-else class="span-margin">{{
                    item.user_name_cn
                  }}</span>
                </span>
              </div>
            </fdev-td>
          </template>
          <template v-slot:body-cell-fdev_implement_unit_no="props">
            <fdev-td :title="props.value">
              <div class="text-ellipsis">
                <f-icon
                  v-if="props.row.delayFlag == true"
                  name="alert_t_f"
                  style="color:red"
                  title="延期告警！"
                />
                <span
                  class="normal-link"
                  @click="unitProfileDialogOpen(props.row)"
                >
                  {{ props.row.fdev_implement_unit_no }}
                  <fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ props.row.fdev_implement_unit_no }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </span>
              </div>
            </fdev-td>
          </template>
          <template v-slot:body-cell-ipmp_implement_unit_no="props">
            <fdev-td>
              <div
                v-if="
                  props.row.ipmp_implement_unit_no &&
                    isLinkFun(props.row.have_link)
                "
                class="text-ellipsis"
              >
                <span
                  class="normal-link text-ellipsis"
                  @click="gotUnitDetail(props.row)"
                  :title="props.row.ipmp_implement_unit_no"
                >
                  <span class="text-ellipsis">
                    {{ props.row.ipmp_implement_unit_no }}
                  </span>
                </span>
              </div>
              <div
                class="text-ellipsis"
                :title="props.row.ipmp_implement_unit_no"
                v-else-if="
                  props.row.ipmp_implement_unit_no &&
                    !isLinkFun(props.row.have_link)
                "
              >
                {{ props.row.ipmp_implement_unit_no }}
              </div>

              <div v-else>
                -
              </div>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.ipmp_implement_unit_no }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>
          <!-- 涉及UI还原审核 -->
          <template v-slot:body-cell-ui_verify="props">
            <fdev-td :title="props.row.ui_verify ? '涉及' : '不涉及'">
              {{ props.row.ui_verify ? '涉及' : '不涉及' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-implement_unit_status_normal="props">
            <fdev-td>
              <div class="row no-wrap items-center">
                <div
                  :style="
                    getBgColor(
                      devUnitStatus(
                        props.row.implement_unit_status_special,
                        props.row.implement_unit_status_normal
                      )
                    )
                  "
                  class="circle-status q-mr-xs"
                ></div>
                <span
                  :title="
                    devUnitStatus(
                      props.row.implement_unit_status_special,
                      props.row.implement_unit_status_normal
                    )
                  "
                >
                  {{
                    devUnitStatus(
                      props.row.implement_unit_status_special,
                      props.row.implement_unit_status_normal
                    )
                  }}
                </span>
              </div>
            </fdev-td>
          </template>

          <template v-slot:body-cell-operate="props">
            <fdev-td :props="props">
              <div class="q-gutter-x-sm border-right row no-wrap">
                <div class="inline-block">
                  <fdev-tooltip
                    v-if="addJobBtnDisabledUnit(props.row)"
                    anchor="top middle"
                    self="bottom middle"
                  >
                    {{ addJobBtnDescByStatus(props.row) }}
                  </fdev-tooltip>
                  <fdev-btn
                    flat
                    label="新增任务"
                    :disable="addJobBtnDisabledUnit(props.row)"
                    @click="toJobAddPage(props.row)"
                  />
                </div>
                <div class="inline-block">
                  <fdev-tooltip
                    v-if="!props.row.update_flag.flag"
                    anchor="top middle"
                    self="bottom middle"
                  >
                    {{ props.row.update_flag.msg }}
                  </fdev-tooltip>
                  <fdev-btn
                    flat
                    label="编辑"
                    :disable="!props.row.update_flag.flag"
                    @click="updateImplUnit(props.row)"
                  />
                </div>
                <div
                  class="inline-block"
                  v-if="props.row.mount_flag && demandType != 'daily'"
                >
                  <fdev-btn
                    flat
                    label="挂载"
                    @click="mountImplUnit(props.row)"
                  />
                </div>
                <div class="inline-block">
                  <fdev-tooltip
                    v-if="props.row.approveFlag.flag === '1'"
                    anchor="top middle"
                    self="bottom middle"
                  >
                    {{ props.row.approveFlag.msg }}
                  </fdev-tooltip>
                  <fdev-btn
                    flat
                    label="申请审批"
                    v-if="
                      props.row.approveFlag.flag === '1' ||
                        props.row.approveFlag.flag === '2'
                    "
                    :disable="props.row.approveFlag.flag === '1'"
                    @click="applyApprove(props.row)"
                  />
                </div>
                <div class="inline-block">
                  <fdev-tooltip
                    v-if="props.row.del_flag !== 0"
                    anchor="top middle"
                    self="bottom middle"
                  >
                    {{ deleteTipWord(props.row) }}
                  </fdev-tooltip>
                  <fdev-btn
                    flat
                    label="删除"
                    @click="rowDelete(props.row)"
                    :disable="props.row.del_flag !== 0"
                  />
                </div>
              </div>
            </fdev-td>
          </template>
        </fdev-table>
        <div class="row justify-center btn-margin">
          <!-- 评估完成 -->
          <div>
            <fdev-tooltip
              v-if="shouldEvaluateTipShow"
              anchor="top middle"
              self="bottom middle"
            >
              {{ evaluateCompletedTip }}
            </fdev-tooltip>
            <fdev-btn
              :disable="shouldEvaluateBeDisabled()"
              label="评估完成"
              @click="evaluateCompleted"
              class="q-mb-lg q-mr-lg"
            />
          </div>
        </div>
      </div>
    </Loading>
    <ImplUnitAdd
      v-if="implUnitDialogOpen"
      v-model="implUnitDialogOpen"
      :demandType="demandType"
      :demandId="demandId"
      :groupId="groupId"
      :operateType="operateType"
      :uiVerify="uiVerify"
      :isDemandAdmin="isDemandAdmin"
      :isDemandLeader="isDemandLeader"
      :isPartsLeader="isPartsLeader"
      :isLimited="isLimited"
      :currentUserGroupId="currentUserGroupId"
      :upImplUnitModel="upImplUnitModel"
      :relate_part_detail="demandModel.relate_part_detail"
      :currentUser="currentUser"
      @refImplUnitList="refImplUnitList"
    ></ImplUnitAdd>
    <!-- 增加日常研发 单元-->
    <AddDailyDevelopNo
      v-if="isShowAddDailyDevelopNo"
      :isShowAddDailyDevelopNo.sync="isShowAddDailyDevelopNo"
      :demandType="demandType"
      :demandId="demandId"
      :groupId="groupId"
      :isDemandAdmin="isDemandAdmin"
      :isDemandLeader="isDemandLeader"
      :isPartsLeader="isPartsLeader"
      :reqImplUnitModel="upImplUnitModel"
      :relate_part_detail="demandModel.relate_part_detail"
      :currentUser="currentUser"
      :demandDetail="demandDetail"
      @updateList="updateList"
    />
    <!-- 编辑日常研发 单元-->
    <EditDailyDevelopNo
      v-if="isShowEditDailyDevelopNo"
      :isShowEditDailyDevelopNo.sync="isShowEditDailyDevelopNo"
      :demandType="demandType"
      :demandId="demandId"
      :groupId="groupId"
      :isDemandAdmin="isDemandAdmin"
      :isDemandLeader="isDemandLeader"
      :isPartsLeader="isPartsLeader"
      :reqImplUnitModel="upImplUnitModel"
      :relate_part_detail="demandModel.relate_part_detail"
      :currentUser="currentUser"
      :demandDetail="demandDetail"
      :isLimited="isLimited"
      :isLimitedChangeGroup="isLimitedChangeGroup"
      @updateList="updateList"
      :rowData="rowData"
    />
    <Mount
      v-if="mountDialogOpen"
      :mountIpmp="mountIpmp"
      @refreshMount="refreshMount"
    >
    </Mount>
    <div class="messageStyle" v-html="message"></div>
  </div>
</template>

<script>
import {
  createDemandModel,
  demandDetailDevelopNoColumn,
  evaluateVisibleColumns,
  demandAvailables,
  demandAssessWays,
  futureAssess,
  priorities,
  normalStatus,
  specialStatus,
  roleName,
  assessStatusMap
} from './model';
import { formatUser } from '@/modules/User/utils/model';
import {
  successNotify,
  deepClone,
  formatOption,
  resolveResponseError
} from '@/utils/utils';
// import { required } from 'vuelidate/lib/validators';
import { mapState, mapGetters, mapActions } from 'vuex';
import Loading from '@/components/Loading';
import ImplUnitAdd from './Components/ImplUnitAdd';
import Mount from './Components/Mount';

// 如果是日常研发单元点击新增研发单元加载次弹框
import AddDailyDevelopNo from '@/modules/Rqr/views/demandDetail/components/developNo/AddDailyDevelopNo.vue';
import EditDailyDevelopNo from '@/modules/Rqr/views/demandDetail/components/developNo/EditDailyDevelopNo.vue';
import {
  isShowAdd,
  queryOtherDemandTaskList,
  applyApprove,
  queryCopyFlag
} from '@/modules/Rqr/services/methods.js';
export default {
  components: {
    Loading,
    ImplUnitAdd,
    Mount, //挂载
    AddDailyDevelopNo, //新增
    EditDailyDevelopNo //编辑
  },
  props: {
    demandDetail: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      relatePartLabelStyle:
        this.demandDetail.demand_type == 'daily'
          ? 'width:90px;margin-right:16px'
          : 'width:auto;margin-right:32px',
      otherDemandTaskNum: '', //其他需求任务
      otherDemandTaskNumOptions: [], //其他需求任务列表
      isShowBtn: '', //是否显示新增 研发单元按钮
      isShowMsg: '', //是否显示新增研发单元提示
      rowData: null, //点击的行数据
      isShowAddDailyDevelopNo: false, //是否显示 新增日常研发单元弹框
      isShowEditDailyDevelopNo: false,
      loading: false,
      isDemandAdmin: false,
      isDemandLeader: false,
      isPartsLeader: false,
      isImplUnitLeader: false, //研发单元牵头人
      isIpmpmentUnitleader: false, //研发单元对应的实施单元牵头人
      ismountBtn: false,
      currentUserGroupId: '',
      demandAvailables,
      demandAssessWays,
      futureAssess,
      priorities,
      demandType: '',
      demandModel: createDemandModel(),
      demandId: '',
      evaluateCompletedTip: '',
      addTip: '',
      deleteTip: '',
      editTip: '',
      shouldEvaluateTipShow: false,
      shouldDeleteTipShow: false,
      demandStatus: '',
      unitStatus: 0,
      isLimited: false,
      isLimitedChangeGroup: false, //评估中 不允许编辑日常任务
      groupId: '',
      planTableData: [],
      columns: demandDetailDevelopNoColumn(this.demandDetail.demand_type),
      // columns: demandDetailDevelopNoColumn('daily'),
      selected: [],
      implInfo: {},
      implUnitDialogOpen: false,
      implUnitProfileDialogOpen: false,
      operateType: 'add',
      upImplUnitModel: null,
      uiVerify: false,
      groups: [],
      groupsClone: [],
      users: [],
      userOptions: [],
      visibleColumns: evaluateVisibleColumns,
      relatedPart: '',
      relatedPartsOptions: [],
      isOldDemand: false,
      editSave: false,
      relatedFilterGroup: [], //涉及板块选项
      focusInputIndex: '',
      isAssessId: [],
      isAssessName: [],
      leaderGroupOptions: [],
      editModel: createDemandModel(),
      dataQueryPartInfo: {},
      tipWords: {
        NoPermissionSupply: '您无权补充本板块研发单元',
        NoPermissionAdd: '您无权新增本板块研发单元',
        CompleteAssess: '已完成初次评估，请补充研发单元',
        NoCompleteAssess: '当前板块在此需求尚未完成初次评估',
        SpecialStatus: '该需求处于特殊状态中，不允许操作'
      },
      addJobBtnDisabled: true,
      addJobBtnDisabledDesc: '',
      unitVisible: null, //实施单元编号
      UnitOptions: [], //实施单元选项
      UnitOptionsClone: [],
      mountDialogOpen: false, //挂在弹窗
      mountIpmp: [],
      flagInfo: {
        code: '0', //0 可编辑 1不展示  2 不可编辑
        msg: '无操作权限'
      },
      message: `
        1、当研发单元未选择实施单元时，需负责人完成研发单元审批方评估完成。<br >
        2、当研发单元的计划启动开发日期超过需求的评估完成日期7天，需负责人完成研发单元审批方评估完成。<br >
        3、当研发单元的计划提交用户日期超过需求的评估完成日期30天，需负责人完成研发单元审批方评估完成。<br >
        需求的评估完成日期查看路径为：研发协作->需求管理->需求评估管理<br >
        负责人审批路径：工作台->我的审批->研发单元审批<br >
        审批记录查看路径：研发协作->需求管理->研发单元审批列表<br >
      `
    };
  },
  computed: {
    ...mapState('demandsForm', {
      implementUnitData: 'implementUnitData',
      demandInfoDetail: 'demandInfoDetail',
      implUnitQueriedByGroupId: 'implUnitQueriedByGroupId',
      relatedPartIds: 'relatedPartIds',
      theQueryPartInfo: 'theQueryPartInfo',
      ipmpUnitListTable: 'ipmpUnitListTable',
      ipmpUnitNoList: 'ipmpUnitNoList',
      assessButFlag: 'assessButFlag',
      relatePartDetail: 'relatePartDetail'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('userForm', {
      groupsData: 'groups',
      userInPage: 'userInPage'
    }),
    ...mapGetters('user', ['isLoginUserList']),
    ...mapGetters('demandsForm', ['groupsCannotSelect']),
    demand_available_label() {
      if (
        !this.demandAvailables ||
        this.demandModel.demand_available === '' ||
        this.demandModel.demand_available === null
      ) {
        return '';
      }
      return this.demandAvailables[this.demandModel.demand_available].label;
    },
    selectedUnitVisible: {
      get() {
        return this.unitVisible ? this.unitVisible.implUnitNum : '';
      },
      set(val) {
        return val;
      }
    },
    priority_label() {
      if (
        this.demandType === '' ||
        this.demandModel.priority === '' ||
        this.demandModel.priority === null
      ) {
        return '';
      }
      return this.priorities[this.demandModel.priority].label;
    },
    demand_assess_way_label() {
      if (
        this.demandType === '' ||
        this.demandModel.demand_assess_way === '' ||
        this.demandModel.demand_assess_way === null
      ) {
        return '';
      }
      return this.demandAssessWays[this.demandModel.demand_assess_way].label;
    },
    future_assess_label() {
      if (
        this.demandType === '' ||
        this.demandModel.future_assess === '' ||
        this.demandModel.future_assess === null
      ) {
        return '';
      }
      return this.futureAssess[this.demandModel.future_assess].label;
    },
    demand_leader() {
      if (
        this.demandType === '' ||
        !Array.isArray(this.demandModel.demand_leader_all) ||
        this.demandModel.demand_leader_all.length === 0
      ) {
        return '';
      }
      return this.demandModel.demand_leader_all
        .map(leaderInfo => {
          if (leaderInfo.user_name_cn) {
            return leaderInfo.user_name_cn;
          } else {
            return this.findUserByUserId(leaderInfo.id).user_name_cn;
          }
        })
        .join('，');
    },
    demand_leader_group() {
      if (
        this.demandType === '' ||
        this.demandModel.demand_leader_group === ''
      ) {
        return '';
      }
      return (
        this.findGroupByGroupId(this.demandModel.demand_leader_group) &&
        this.findGroupByGroupId(this.demandModel.demand_leader_group).name
      );
    },
    columnOptions() {
      return this.columns.slice(0, this.columns.length - 1);
    },
    cannotEditStatus() {
      return (
        this.demandModel.demand_status_normal > 7 ||
        this.demandModel.demand_status_special === 1 ||
        this.demandModel.demand_status_special === 2
      );
    }
  },
  watch: {
    //涉及板块切换时，根据新选择的板块组ID，请求刷新表格
    async relatedPart(val) {
      // 只有初始化的时候才进来 初始化  this.groupId 是空
      // 新增研发单元成功后改变了relatedPart 也会进入
      // 编辑  删除  relatedPart不变 不进入
      if (val) {
        // idOld 记录上一次的 小组id
        let idOld = this.groupId; //初始化groupId为空
        this.groupId = val.id;
        // 区分初始化(不查研发单元)还是新增(查研发单元)
        if (idOld) {
          await this.queryImplementUnitData();
        }
        await this.assessButtonfun();
        this.shouldEvaluateBeDisabled();
        // 初始化 取 设计小组的第一个值;
        await this.theSaveQueryPartInfo({
          demand_id: this.demandId,
          part_id: !idOld
            ? this.demandModel.relate_part_detail[0].part_id
            : val.id
        });
        this.dataQueryPartInfo = deepClone(this.theQueryPartInfo);
        let res = deepClone(this.theQueryPartInfo);
        if (!this.isShowBtn) {
          if (!res.addLight && !res.supplyLight) {
            // addLight supplyLight  都无false 无权点击  新建研发单元
            this.isShowMsg = '您无权在当前小组新增研发单元';
          } else {
            this.isShowMsg = '';
          }
        } else {
          this.isShowMsg = this.isShowBtn;
        }
      }
    },
    groupId: {
      handler: async function(newValue, oldValue) {
        // 切换radio 刷新 表头需求信息
        if (oldValue) {
          await this.theSaveQueryPartInfo({
            demand_id: this.demandId,
            part_id: newValue
          });
          let res = deepClone(this.theQueryPartInfo);
          if (!this.isShowBtn) {
            if (!res.addLight && !res.supplyLight) {
              // addLight supplyLight  都无false 无权点击  新建研发单元
              this.isShowMsg = '您无权在当前小组新增研发单元';
            } else {
              this.isShowMsg = '';
            }
          } else {
            this.isShowMsg = this.isShowBtn;
          }
          this.assessButtonfun();
          this.$emit('init', 'refresh');
        }
      }
    }
  },
  filters: {
    relate_part_users(val) {
      if (val && Array.isArray(val)) {
        return val.map(user => user.user_name_cn).join('，');
      }
      return '';
    },
    leader_all_user_names(val) {
      if (val && Array.isArray(val)) {
        return val.map(leader => leader.user_name_cn).join('，');
      }
      return '';
    },
    demandTypeFilter(val) {
      if (val === 'business') {
        return '业务需求';
      } else {
        return '科技需求';
      }
    },
    implementStatusNormalFiler(val) {
      if (val == 0) {
        return (val = '预评估');
      } else if (val == 1) {
        return (val = '评估中');
      } else if (val == 2) {
        return (val = '待实施');
        // 未开始
      } else if (val == 3) {
        // 进行中
        return (val = '开发中');
      } else if (val == 4) {
        return (val = 'SIT');
      } else if (val == 5) {
        return (val = 'UAT');
      } else if (val == 6) {
        return (val = 'REL');
      } else if (val == 7) {
        // 已完成
        return (val = '已投产');
      } else if (val == 8) {
        return (val = '已归档');
      } else if (val == 9) {
        return (val = '已撤销');
      }
    },
    implementStatusSpecialFilter(val) {
      if (val == 1) {
        return (val = '暂缓中');
      } else if (val == 2) {
        return (val = '恢复中');
      } else if (val == 3) {
        return (val = '恢复完成');
      }
    }
  },
  methods: {
    gotUnitDetail(data) {
      let num = data.ipmp_implement_unit_no;
      this.$router.push(`/rqrmn/unitDetail/${num}/${this.demandModel.id}`);
    },
    ...mapActions('demandsForm', [
      'queryDemandInfoDetail',
      'deleteById',
      'assess',
      'queryImplByGroupAndDemandId',
      'theSaveQueryPartInfo',
      'queryPaginationByDemandId',
      'queryIpmpUnitByDemandId', //查询实施单元列表 实施单元下拉
      'queryPaginationByIpmpUnitNo',
      'assessButton'
    ]),
    ...mapActions('userForm', {
      queryGroup: 'fetchGroup',
      queryUserPagination: 'queryUserPagination'
    }),
    ...mapActions('demandsForm', {
      queryDemandInfoDetail: 'queryDemandInfoDetail',
      update: 'update'
    }),
    toJobAddPage(row) {
      this.$router.push({
        name: 'add',
        params: {
          unitData: row
        }
      });
    },
    handleSubmitTestOpen() {
      this.$router.push({
        path: `/rqrmn/submitTestOrder/1`,
        query: {
          id: this.demandInfoDetail.id,
          oa_contact_no: this.demandInfoDetail.oa_contact_no,
          oa_contact_name: this.demandInfoDetail.oa_contact_name,
          demand_type: this.demandInfoDetail.demand_type
        }
      });
    },
    handleImplUnitOpen() {
      this.operateType = 'add';
      this.upImplUnitModel = JSON.parse(JSON.stringify(createDemandModel()));
      if (this.demandInfoDetail.demand_type == 'daily') {
        this.isShowAddDailyDevelopNo = true;
      } else {
        this.implUnitDialogOpen = true;
      }
    },
    updateImplUnit(row) {
      this.operateType = 'update';
      this.upImplUnitModel = deepClone(row);
      if (this.demandType === 'tech') {
        Reflect.set(this.upImplUnitModel, 'groupId', '');
      }
      if (this.demandType === 'business') {
        Reflect.set(this.upImplUnitModel, 'task_no', '');
        Reflect.set(this.upImplUnitModel, 'task_name', '');
        Reflect.set(this.upImplUnitModel, 'ipmp_implement_unit_no', '');
      }
      this.$set(
        this.upImplUnitModel,
        'ipmp_implement_unit_no',
        row.ipmp_implement_unit_no
      );
      // 恢复中
      if (row.implement_unit_status_special === 2) {
        this.isLimited = true;
      } else {
        this.isLimited = false;
      }
      if (this.demandType === 'daily') {
        //评估中 所属小组不可编辑
        if (row.implement_unit_status_special === 1) {
          this.isLimitedChangeGroup = true;
        }
        this.rowData = row;
        this.isShowEditDailyDevelopNo = true;
      } else {
        this.implUnitDialogOpen = true;
      }
    },
    isOtherType(key) {
      if (key && key.indexOf('其他') !== -1) {
        return true;
      } else return false;
    },
    //刷新
    async refImplUnitList(type, group_id) {
      this.upImplUnitModel = createDemandModel();
      this.implUnitDialogOpen = false;

      await this.assessButton({
        demand_id: this.demandId,
        group: this.groupId
      });
      this.shouldEvaluateBeDisabled();
      if (type === 'refresh') {
        if (this.isOldDemand === true) {
          this.demandStatus = 1;
          this.isOldDemand = false;
        }
        if (
          group_id !== undefined &&
          this.relatedPart &&
          this.relatedPart.id !== group_id
        ) {
          // 改变了 relatedPart  会进入 watch
          this.relatedPart = this.findGroupByGroupId(group_id);
          //小组不是预评估状态加chips提示
          if (this.relatedPart && this.relatedPart.status !== '0') {
            if (
              this.isAssessName.length == 0 ||
              this.isAssessName.some(item => item != this.relatedPart.name)
            )
              this.isAssessName.push(this.relatedPart.name);
          }
        } else {
          // 因为 this.relatedPart.id !== group_id 不成立所以按钮切换走不通 加上this.groupId = group_id;
          if (group_id) this.groupId = group_id;
          this.queryImplementUnitData();
        }
        if (group_id) {
          this.isAssessId.push(group_id);
        }
      } else {
        this.operateType = 'add';
      }
      await this.queryDemandInfoDetail({
        id: this.demandId
      });
      this.demandModel = deepClone(this.demandInfoDetail);
      this.queryIpmpUnitByDemandIds();
      // 科技 业务  新增研发单元 编辑研发单元 成功后 刷新 头部需求 信息
      this.$emit('init', 'refresh');
    },
    async queryImplementUnitData(param) {
      if (this.demandDetail.demand_type == 'daily') {
        // 查询日常 研发单元列表
        // changeTab  文档个 任务 基础信息
        this.updateList(this.groupId, '', 'changeTab');
      } else {
        // 查询 科技 业务 研发单元列表
        this.loading = true;
        let t;
        await this.queryImplByGroupAndDemandId({
          demand_id: this.demandId,
          group: this.groupId
        });
        t = deepClone(this.implUnitQueriedByGroupId);
        // 将所属小组id转换所属小组名字name
        t.forEach(item => {
          item.group = item.group_cn;
          item.shouldEditTipShow = false;
          item.editTip = '';
        });
        this.planTableData = t;
        //设置当前涉及板块的实施状态,当前涉及板块没有实施单元的话,状态设为预评估
        if (
          Array.isArray(this.planTableData) &&
          this.planTableData.length !== 0
        ) {
          this.demandModel.relate_part_detail.forEach(item => {
            if (
              this.relatedPart &&
              this.relatedPart.id &&
              this.relatedPart.id === item.part_id
            ) {
              this.unitStatus = item.assess_status;
            }
          });
        } else {
          this.unitStatus = 0;
        }
        this.queryCopyFlag();
        this.loading = false;
      }
    },
    addJobBtnDisabledUnit(row) {
      return (
        row.implement_unit_status_normal == normalStatus.assessmenting ||
        row.implement_unit_status_normal > normalStatus.done ||
        row.implement_unit_status_special == specialStatus.staying ||
        row.implement_unit_status_special == specialStatus.inRecovery
      );
    },
    addJobBtnDescByStatus(row) {
      //评估中 已归档 已撤销 暂缓中 恢复中
      if (row.implement_unit_status_normal == normalStatus.assessmenting) {
        return '该研发单元状态为评估中，无法关联，请联系需求牵头人完成评估';
      } else if (row.implement_unit_status_normal == normalStatus.archived) {
        return '该研发单元已归档';
      } else if (row.implement_unit_status_normal == normalStatus.undo) {
        return '该研发单元已被撤销，无法操作';
      } else if (
        row.implement_unit_status_special == specialStatus.staying ||
        row.implement_unit_status_special == specialStatus.inRecovery
      ) {
        return '该研发单元处于暂缓状态，请解除暂缓状态后继续操作';
      }
    },

    //挂载实施单元
    mountImplUnit(row) {
      this.mountDialogOpen = true;
      this.mountIpmp = row;
    },
    refreshMount(val) {
      this.mountDialogOpen = false;
      if (val) {
        this.queryImplementUnitData();
      }
    },
    //评估完成接口
    async assessButtonfun() {
      if (!this.groupId) {
        return;
      }
      await this.assessButton({
        demand_id: this.demandId,
        group: this.groupId
      });
    },
    //评估完成权限控制
    shouldEvaluateBeDisabled() {
      if (this.assessButFlag.flag === false) {
        this.evaluateCompletedTip = this.assessButFlag.msg;
        this.shouldEvaluateTipShow = true;
        return true;
      } else {
        this.shouldEvaluateTipShow = false;
        return false;
      }
    },
    findUserByUserId(userId) {
      return this.users.find(user => user.id === userId);
    },
    findGroupByGroupId(groupId) {
      if (Array.isArray(groupId)) {
        return groupId.map(id => this.findGroupByGroupId(id));
      }
      return this.groups.find(group => group.id === groupId);
    },
    async evaluateCompleted() {
      //判断是否可以评估完成-待完成
      this.$q
        .dialog({
          title: '评估完成确认',
          message:
            '<p style="color: #f22b11;font-size:16px">是否确认完成本版块本次评估？</p>',
          ok: '确定',
          cancel: '取消',
          html: true
        })
        .onOk(async () => {
          this.loading = true;
          let uncompletedUnit = [];
          if (this.demandInfoDetail.demand_type == 'daily') {
            uncompletedUnit = this.planTableData.filter(unit => {
              return unit.implement_unit_status_normal <= 1;
            });
          } else {
            uncompletedUnit = this.planTableData.filter(unit => {
              return (
                unit.implement_unit_status_normal <= 1 && !unit.approveType
              );
            });
          }
          const payload = {
            id: uncompletedUnit.map(unit => unit.id)
          };
          try {
            await this.assess(payload);
            // 更新按钮状态
            await this.theSaveQueryPartInfo({
              demand_id: this.demandId,
              part_id: this.groupId
            });
            this.dataQueryPartInfo = deepClone(this.theQueryPartInfo);
            //更新评估按钮状态
            this.assessButtonfun();
            this.shouldEvaluateBeDisabled();
            //获取需求文档状态
            await this.queryDemandInfoDetail({
              id: this.demandId
            });
            this.demandStatus = this.demandInfoDetail.demand_status_normal;
            this.isOldDemand =
              this.demandInfoDetail.isTransferRqrmnt === '1' ? true : false;
            // 更新组件内需求状态数据
            this.demandModel = deepClone(this.demandInfoDetail);
            await this.queryImplementUnitData();
            this.selected = [];
            this.loading = false;
            successNotify('评估完成！');
            // 研发单元评估完后 刷新头部需求信息和 操作按钮
            this.$emit('init', 'refresh');
            // 日常需求评估完成刷新列表
            if (this.demandInfoDetail.demand_type == 'daily') {
              // 查询列表
              this.loading = true;
              await this.queryPaginationByIpmpUnitNo({
                other_demand_task_num: this.otherDemandTaskNum || '', //其他需求任务
                demand_id: this.demandId, //需求id
                group: this.groupId //小组id
              });
              this.planTableData = this.ipmpUnitNoList.data;
              this.loading = false;
            }
          } catch (e) {
            this.loading = false;
          }
        });
    },

    unitProfileDialogOpen(unitInfo) {
      this.$router.push({
        path: '/rqrmn/devUnitDetails',
        query: { id: unitInfo.id, dev_unit_no: unitInfo.fdev_implement_unit_no }
      });
    },
    //申请审批
    async applyApprove(row) {
      this.$q
        .dialog({
          title: '申请审批确认',
          message: '确定要发起申请审批？',
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          this.loading = true;
          await resolveResponseError(() =>
            applyApprove({ fdevUnitNo: row.fdev_implement_unit_no })
          );
          this.loading = false;
          successNotify('申请审批成功');
          this.queryImplementUnitData();
        });
    },
    rowDelete(row) {
      this.$q
        .dialog({
          title: '删除确认',
          message: '确认要删当前研发单元吗？',
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          this.loading = true;
          const deleteId = [row.id];
          await this.deleteById({
            ids: deleteId
          });
          this.loading = false;
          //刷新列表
          if (this.demandType == 'daily') {
            await this.updateList(this.groupId, '', 'refresh');
          } else {
            try {
              await this.queryImplementUnitData();
              await this.queryDemandInfoDetail({
                id: this.demandId
              });
              this.demandModel = deepClone(this.demandInfoDetail);
              this.assessButtonfun();
              this.shouldEvaluateBeDisabled();
              // 更新按钮状态
              await this.theSaveQueryPartInfo({
                demand_id: this.demandId,
                part_id: this.groupId
              });
              this.dataQueryPartInfo = deepClone(this.theQueryPartInfo);
              // 先清空叉的数组
              this.isAssessId = [];
              this.isAssessName = [];
              this.relatePartDetail.forEach(item => {
                // 不是预评估状态则不能叉涉及板块对应的小组
                if (item.assess_status !== '0') {
                  this.isAssessId.push(item.part_id);
                  this.isAssessName.push(item.part_name);
                }
              });
              this.queryIpmpUnitByDemandIds();
            } catch (e) {
              this.loading = false;
            }

            this.loading = false;
            successNotify('删除成功');
            // 更新需求头部信息
            this.$emit('init', 'refresh');
          }
        });
    },
    repApplyPlan() {
      this.operateType = 'rep';
      this.upImplUnitModel = deepClone(createDemandModel);
      this.implUnitDialogOpen = true;
    },
    //查询实施单元下的研发单元
    async queryUnitList(val) {
      // val 实施单元 编号
      this.unitVisible = val;
      if (this.demandDetail.demand_type == 'daily') {
        this.updateList(this.groupId, this.otherDemandTaskNum, '');
      } else {
        if (val) {
          await this.queryPaginationByIpmpUnitNo({
            ipmp_unit_no: val.implUnitNum, //实施单元编号
            demand_id: this.demandId, //需求id
            group: this.groupId //小组id
          });
          this.planTableData = this.ipmpUnitNoList.data;
        } else {
          this.queryImplementUnitData();
        }
      }
    },
    async initQuery() {
      // await this.queryGroup();
      this.groups = formatOption(this.groupsData);
      this.leaderGroupOptions = this.groups.slice(0);
      // await this.queryUserPagination(queryUserOptionsParams);
      this.users = [].map(user => formatOption(formatUser(user), 'name'));
      this.userOptions = this.users.slice(0);
      this.userFilterOptions = this.userOptions;
      this.relatedFilterGroup = this.groups;
    },
    userOptionsFilter(group_id) {
      const myUser = this.userOptions.filter(item => {
        if (item.email.indexOf('@') > -1) {
          return item.email.split('@')[1] === 'spdb.com.cn';
        }
      });
      return myUser;
    },
    deleteTipWord(row) {
      const deleteTipMap = {
        1: '请先恢复需求正常状态再行删除',
        2: '该研发单元已挂载任务，无法删除',
        3: '请联系需求牵头人、需求管理员、实施单元牵头人删除',
        4: '您无权删除该研发单元',
        5: '待审批的研发单元不可删除'
      };
      if (row.del_flag < 6) {
        return deleteTipMap[row.del_flag];
      }
      // } else if (row.del_flag == 3) {
      //   const curPart = this.demandModel.relate_part_detail.find(item => {
      //     return item.part_id == this.groupId;
      //   });
      //   // 拿到当前板块评估人员数组
      //   const nameArr =
      //     curPart && curPart.assess_user_all.map(item => item.user_name_cn);
      //   // 判断评估人员人数
      //   if (nameArr && nameArr.length > 2) {
      //     const splitNameArr = nameArr.slice(0, 2);
      //     const splitNameStr = splitNameArr.join('/');
      //     return `请联系该板块评估人员(${splitNameStr}...)删除`;
      //   } else {
      //     const allNameStr = nameArr && nameArr.join('/');
      //     return `请联系该板块评估人员(${allNameStr})删除`;
      //   }
      // }
    },
    // 实施单元状态值转换
    unitStatusMap(status) {
      return assessStatusMap[status];
    },
    async partItemClick(item) {
      // 当前板块id
      this.groupId = item.part_id;
      // 实施单元id selectedUnitVisible
      this.queryUnitList({ implUnitNum: this.selectedUnitVisible });
    },
    // 新增研发单元无权限控制
    // addUnitPermissionFn() {
    //   //研发单元牵头人
    //   this.planTableData.forEach(item => {
    //     item.implement_leader_all.forEach(ctem => {
    //       if (ctem.id === this.currentUser.id) {
    //         this.isImplUnitLeader = true;
    //       }
    //     });
    //   });
    //   return (
    //     this.isDemandAdmin ||
    //     this.isDemandLeader ||
    //     this.isPartsLeader ||
    //     this.isImplUnitLeader
    //   );
    // },
    //挂载展示权限
    mountPermission(row) {
      //研发单元牵头人
      if (row.implement_leader === this.currentUser.id) {
        this.isImplUnitLeader = true;
      }

      //研发单元对应的实施单元牵头人
      this.isIpmpmentUnitleader =
        row.ipmp_implement_unit_no &&
        row.ipmp_unit_leader &&
        row.ipmp_unit_leader.some(item => item === this.currentUser.id);
      //已挂载实施单元且已评估完成的研发单元，不展示挂载按钮
      if (
        row.ipmp_implement_unit_no !== '' &&
        row.implement_unit_status_normal !== 1
      ) {
        this.ismountBtn = false;
        return;
      }

      // 需求管理员, 需求牵头人, 涉及小组评估人, 研发单元牵头人，研发单元对应的实施单元牵头人\
      return (
        this.isDemandAdmin ||
        this.isDemandLeader ||
        this.isPartsLeader ||
        this.isImplUnitLeader ||
        this.isIpmpmentUnitleader ||
        this.ismountBtn
      );
    },
    unitVisibleFun(val, update, abort) {
      update(() => {
        // 如果搜索值为空 直接返回整个列表数据
        if (!val) return (this.UnitOptions = this.UnitOptionsClone);
        this.UnitOptions = this.UnitOptionsClone.filter(
          tag => tag.implUnitNum.indexOf(val) > -1
        );
      });
    },
    //查询需求下的实施单元
    async queryIpmpUnitByDemandIds() {
      await this.queryIpmpUnitByDemandId({
        demandId: this.demandId,
        isTech: this.demandInfoDetail.demand_type
      });
      this.UnitOptions = formatOption(this.ipmpUnitListTable.data);
      this.UnitOptionsClone = formatOption(this.ipmpUnitListTable.data);
    },
    async init() {
      this.loading = true;
      if (this.demandDetail.demand_type == 'daily') {
        // 其他需求任务编号 列表
        this.getOtherDemandTaskList();
      }
      this.demandId = this.$route.params.id;
      this.dataQueryPartInfo = deepClone(this.theQueryPartInfo);
      this.getIsShowBtn();
      await this.queryDemandInfoDetail({
        id: this.demandId
      });
      this.demandType = this.demandInfoDetail.demand_type;
      await this.queryIpmpUnitByDemandId({
        demandId: this.demandId,
        isTech: this.demandInfoDetail.demand_type
      });
      this.UnitOptions = formatOption(this.ipmpUnitListTable.data);
      this.UnitOptionsClone = formatOption(this.ipmpUnitListTable.data);
      await this.initQuery();
      this.editModel = this.demandInfoDetail;
      this.loading = false;
      //设置当前需求状态demand_status
      this.demandStatus = this.demandInfoDetail.demand_status_normal;

      this.isOldDemand =
        this.demandInfoDetail.isTransferRqrmnt === '1' ? true : false;
      this.demandModel = deepClone(this.demandInfoDetail);
      if (this.demandType === 'business' || this.demandType === 'tech') {
        //无论何种情况，新建研发单元时，必须填写“是否涉及UI还原审核”字段
        this.uiVerify = /*this.demandModel.ui_verify*/ true;
      } else {
        this.uiVerify = false;
      }
      this.relatedPartsOptions = this.demandModel.relate_part.map(partId =>
        this.findGroupByGroupId(partId)
      );

      //判断是不是需求管理员
      this.isDemandAdmin = this.currentUser.role.some(
        role => role.name === '需求管理员'
      );
      //判断是不是需求牵头人
      this.isDemandLeader =
        this.demandModel.demand_leader_all.findIndex(
          leaderInfo => leaderInfo.id === this.currentUser.id
        ) > -1
          ? true
          : false;
      //判断是不是板块牵头人（板块的评估人员
      this.isPartsLeader = this.demandModel.relate_part_detail.some(
        part =>
          Array.isArray(part.assess_user_all) &&
          part.assess_user_all.findIndex(
            userInfo => userInfo.id === this.currentUser.id
          ) > -1
      );

      //缓存里有板块ID和可见列的话从缓存里取
      if (sessionStorage.getItem('planTableData')) {
        const model = JSON.parse(sessionStorage.getItem('planTableData'));
        if (this.relatedPartIds.includes(model.groupId)) {
          this.relatedPart = this.findGroupByGroupId(model.groupId);
        } else {
          this.relatedPart = this.relatedPartsOptions[0];
        }
        this.visibleColumns = model.visibleColumns;
      } else {
        //当前用户的GroupIdrelatedPart
        // this.currentUserGroupId = this.currentUser.group_id;
        let currentUserEvaGroups = this.editModel.relate_part_detail.filter(
          //获取当前用户评估板块
          item => {
            if (item.assess_user.indexOf(this.currentUser.id) > -1) {
              return item;
            }
          }
        );
        if (this.isDemandLeader || this.isDemandAdmin) {
          this.relatedPart = this.findGroupByGroupId(
            this.editModel.relate_part
          ).filter(item => item !== undefined)[0];
        } else {
          this.relatedPart = this.findGroupByGroupId(
            currentUserEvaGroups.map(g => g.part_id)
          ).filter(item => item !== undefined)[0];
        }
      }
      if (!this.relatedPart) {
        this.relatedPart = this.relatedPartsOptions[0];
      }
      this.addJobBtnDisabled = this.currentUser.role.every(
        role =>
          role.name !== roleName.spdbHead && role.name !== roleName.companyHead
      );
      if (this.addJobBtnDisabled) {
        this.addJobBtnDisabledDesc = '只有行内/厂商负责人可执行此操作';
      }
      //需求状态为暂缓中或恢复中，不能新增任务
      if (
        this.demandModel.demand_status_special == specialStatus.staying ||
        this.demandModel.demand_status_special == specialStatus.inRecovery
      ) {
        this.addJobBtnDisabled = true;
        this.addJobBtnDisabledDesc =
          '该实施单元处于暂缓状态，请解除暂缓状态后继续操作';
      }
      setTimeout(() => {
        this.groupId =
          this.demandModel.relate_part_detail &&
          this.demandModel.relate_part_detail[0] &&
          this.demandModel.relate_part_detail[0].part_id;
        this.updateList(this.groupId, '', 'init');
      });
      // this.updateList()
      // this.groupId =relatePartList[0]
      //删除交易多发
      // await this.theSaveQueryPartInfo({
      //   demand_id: this.demandId,
      //   part_id: this.relatedPart.id
      // });
      // this.dataQueryPartInfo = deepClone(this.theQueryPartInfo);

      // 刷新浏览器  涉及小组为空
      if (this.groupId) {
        await this.theSaveQueryPartInfo({
          demand_id: this.demandId,
          part_id: this.demandModel.relate_part_detail[0].part_id
        });
        let res = deepClone(this.theQueryPartInfo);
        if (!this.isShowBtn) {
          if (!res.addLight && !res.supplyLight) {
            // addLight supplyLight  都无false 无权点击  新建研发单元
            this.isShowMsg = '您无权在当前小组新增研发单元';
          } else {
            this.isShowMsg = '';
          }
        } else {
          this.isShowMsg = this.isShowBtn;
        }
      }
    },
    isLinkFun(arg) {
      return arg === 1;
    },
    getBgColor(statu) {
      const styleObejct = {
        评估中: 'background: #FEC400;',
        待实施: 'background: #24C8F9;',
        未开始: 'background: #24C8F9;',
        开发中: 'background: #FD8D00;',
        进行中: 'background: #FD8D00;',
        sit: 'background: #0378EA;',
        uat: 'background: #4386CA;',
        rel: 'background: #04488C;',
        已投产: 'background: #4DBB59;',
        已完成: 'background: #4DBB59;',
        已归档: 'background: #8CBC48;',
        已撤销: 'background: #B0BEC5;',
        暂缓中: 'background: #4A66DB;',
        恢复中: 'background: #F46865;',
        恢复完成: 'background: #55D1D1;'
      };
      return styleObejct[statu];
    },
    devUnitStatus(special, normal) {
      let type = this.demandInfoDetail.demand_type;
      const obj1 = {
        1: '评估中',
        2: type == 'daily' ? '未开始' : '待实施',
        3: type == 'daily' ? '进行中' : '开发中',
        4: 'sit',
        5: 'uat',
        6: 'rel',
        7: type == 'daily' ? '已完成' : '已投产',
        8: '已归档',
        9: '已撤销'
      };
      let obj2 = {
        1: '暂缓中',
        2: '恢复中',
        3: '恢复完成'
      };
      if (special && special !== 3) return obj2[special];
      if (normal) return obj1[normal];
    },
    // 判断当前用户是否展示新增按钮
    async getIsShowBtn() {
      let res = await isShowAdd({ demandId: this.demandId });
      this.isShowBtn = res;
      this.isShowMsg = res;
    },
    async updateList(groupId, other_demand_task_num, type) {
      // 切换 radio
      if (type == 'changeTab') return;
      this.loading = true;
      if (type == 'refresh') {
        // 刷小组
        // this.demandModel.relate_part_detail=;
        await this.queryDemandInfoDetail({
          id: this.demandId
        });
        // 刷评估按钮

        // 刷  头部 需求信息
        this.$emit('init', 'refresh');
        await this.assessButtonfun();
        this.demandModel = deepClone(this.demandInfoDetail);
      }
      if (type === 'init') {
        this.assessButtonfun();
      }

      this.groupId = groupId;
      // 查询列表
      await this.queryPaginationByIpmpUnitNo({
        other_demand_task_num: this.otherDemandTaskNum || '', //其他需求任务
        demand_id: this.demandId, //需求id
        group: this.groupId //小组id
      });
      this.planTableData = this.ipmpUnitNoList.data;
      this.loading = false;
    },
    async getOtherDemandTaskList() {
      const res = await queryOtherDemandTaskList({
        demandId: this.demandDetail.id
      });
      this.otherDemandTaskNumOptions = res.data;
    },
    //查询是否有复制权限
    async queryCopyFlag() {
      this.flagInfo = await resolveResponseError(() =>
        queryCopyFlag({ demand_id: this.demandDetail.id })
      );
    }
  },
  created() {
    this.init();
    this.queryCopyFlag();
  },
  beforeRouteEnter(to, from, next) {
    const { params } = from;
    if (Object.keys(params).length == 0) {
      sessionStorage.removeItem('planTableData');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    const { params } = to;
    if (Object.keys(params).length) {
      sessionStorage.setItem(
        'planTableData',
        JSON.stringify({
          groupId: this.groupId,
          visibleColumns: this.visibleColumns
        })
      );
    }
    next();
  }
};
</script>

<style lang="stylus" scoped>

.item-unit{
  width: 350px;
  margin-right:20px
}
.item-unit>>>.input-field {
  color: #000;
  width: 100px
}
.title {
  font-size: 18px;
  padding: 10px;
}

.btn-margin {
  padding-top: 20px;
}

.td-padding {
  padding-right: 16px !important;
  min
}

.mgr10
  margin-right 10px
  line-height 36px

.labStyle
  margin-left 13px
  width 136px
  line-height 36px

.my-sticky-column-table >>> {
    max-height: 600px;
  th:first-child, td:first-child, td:last-child, th:last-child {
    opacity: 1;
  }
  .q-table > thead > tr > th {
    // 表头固定
    position: sticky;
    z-index: 999;
    top: 0;
    background-color: #f4f6fd;
  }

  th:first-child, td:first-child {
    position: sticky;
    left: 0;
    z-index: 1;
  }

  td:last-child, th:last-child {
    position: sticky;
    right: 0;
    z-index: 1;
  }
  th:first-child {
    //表头第一个
    position: sticky;
    left: 0;
    z-index: 1000 !important;
  }
  .td-desc {
    max-width: 300px;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.table-head-input {
  height: 40px;
}

.td-width
  max-width 300px
  overflow hidden
  text-overflow ellipsis

.relatePartClass {
  font-size: 14px;
  color: #DDDDDD
}

.detail
  word-break: break-all;
  font-size: 14px;
  color: #616161

.titleBtnflex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.inline-block {
  display: inline-block;
}

.text-lineh{
  display: inline-block;
  line-height: 40px;
}

// .item-box {
//   width: 100%;
//   display: flex;
//   justify-content: space-between;
// }

.item-content-box {
  flex: 1;
}

.item-content {
  width: 70%;
  display: flex;
  flex-wrap: wrap;
}

.item-content-btn {
  width: 19%;
  margin-right: 1%;
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}

.init-i {
  padding: none;
  margin: none;
  font-style: normal;
}

.part-font-size {
  font-size: 12px;
}

.item-title-basis {
  flex-basis: 80px;
}

.item-end-basis {
  flex-basis: 150px;
}

.item-end {
  align-self: flex-end;
}
.wordclass
  word-break:break-word

.relateclass
  margin-left:2px
.circle-status
  width 8px
  height 8px
  border-radius:50%
.my-sticky-column-table >>> .q-table__top
  align-items flex-start !important
  .row.full-width.q-mb-md.q-pr-md.items-center
    padding-right 0
.normal-link
  color #0663BE
  cursor pointer
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

.messageStyle
  padding 20px 30px
  background #eee
  border-radius 5px
  font-size 12px
</style>
<style>
.q-inner-loading {
  z-index: 1999;
}
</style>
