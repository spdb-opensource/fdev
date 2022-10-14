<template>
  <f-dialog
    f-dc
    right
    persistent
    v-model="open"
    :title="operateTitle"
    @before-show="initDialogData"
    @hide="handleImplUnitClose"
  >
    <Loading :visible="dialogLoading">
      <div class="row justify-between">
        <!--实施单元编号;-->
        <f-formitem
          label="实施单元编号"
          :required="isRequiredFun()"
          help="支持实施单元编号,实施单元内容,实施牵头人域账号,项目编号,实施牵头人中文名搜索"
        >
          <fdev-select
            use-input
            :disable="isLimited_ || updateFlag"
            ref="reqImplUnitModel.ipmp_implement_unit_no"
            v-model="reqImplUnitModel.ipmp_implement_unit_no"
            :options="implUnitOptions"
            option-label="implUnitNum"
            option-value="implUnitNum"
            emit-value
            @filter="unitNoFilter"
            :rules="[
              v => (isRequiredFun() ? !!v || '请输入实施单元编号' : true || '')
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-tooltip v-if="isDisplay(scope.opt)">
                    {{ ipmpTipFun(scope.opt) }}
                  </fdev-tooltip>
                  <fdev-item-label :title="scope.opt.implUnitNum">{{
                    scope.opt.implUnitNum
                  }}</fdev-item-label>
                  <fdev-item-label caption>
                    <div :title="scope.opt.implLeaderName">
                      {{ scope.opt.implLeaderName }}
                    </div>
                    <div :title="scope.opt.implContent">
                      {{ scope.opt.implContent }}
                    </div>
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>

        <!--研发单元内容-->
        <f-formitem label="研发单元内容" required>
          <fdev-input
            v-model="$v.reqImplUnitModel.implement_unit_content.$model"
            :disable="isReadOnlyFun() || updateFlag"
            ref="reqImplUnitModel.implement_unit_content"
            type="text"
            :rules="[
              () =>
                $v.reqImplUnitModel.implement_unit_content.required ||
                '请输入研发单元内容'
            ]"
          />
        </f-formitem>

        <!--研发单元牵头人-->
        <f-formitem label="研发单元牵头人" required>
          <fdev-select
            use-input
            multiple
            :disable="isReadOnlyFun() || updateFlag"
            ref="reqImplUnitModel.implement_leader_all"
            v-model="$v.reqImplUnitModel.implement_leader_all.$model"
            :options="leaderOptions"
            @filter="leaderOptionsFilterByInput"
            :rules="[
              () =>
                $v.reqImplUnitModel.implement_leader_all.required ||
                '请选择研发单元牵头人'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label
                    caption
                    :title="
                      scope.opt.user_name_en + '--' + scope.opt.group.name
                    "
                  >
                    {{ scope.opt.user_name_en }}--{{ scope.opt.group.name }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>

        <!--所属小组-->
        <f-formitem label="所属小组" required>
          <fdev-select
            use-input
            :disable="operateType === 'update' || banStatus || updateFlag"
            ref="reqImplUnitModel.group"
            v-model="reqImplUnitModel.group_cn"
            :options="belongGroupOptions"
            @filter="belongGroupsFilter"
            :rules="[v => !!v || '请选择小组']"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name">{{
                    scope.opt.name
                  }}</fdev-item-label>
                  <fdev-item-label caption :title="scope.opt.label">
                    {{ scope.opt.label }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>

        <!--计划启动开发日期-->
        <f-formitem label="计划启动开发日期" required>
          <f-date
            v-model="reqImplUnitModel.plan_start_date"
            :options="planStartDateOptions"
            :default-year-month="defaultYearMonth"
            ref="reqImplUnitModel.plan_start_date"
            :rules="[
              () =>
                $v.reqImplUnitModel.plan_start_date.required ||
                '请输入计划启动开发日期'
            ]"
          />
        </f-formitem>

        <!--计划提交内测日期-->
        <f-formitem label="计划提交内测日期" required>
          <f-date
            v-model="$v.reqImplUnitModel.plan_inner_test_date.$model"
            :options="planInnerTestDateOptions"
            ref="reqImplUnitModel.plan_inner_test_date"
            :rules="[
              () =>
                $v.reqImplUnitModel.plan_inner_test_date.required ||
                '请输入计划提交内测日期'
            ]"
          />
        </f-formitem>

        <!--计划提交用户测试日期-->
        <f-formitem label="计划提交用户测试日期" required>
          <f-date
            v-model="$v.reqImplUnitModel.plan_test_date.$model"
            :options="planTestDateOptions"
            ref="reqImplUnitModel.plan_test_date"
            :rules="[
              () =>
                $v.reqImplUnitModel.plan_test_date.required ||
                '请输入计划提交用户测试日期'
            ]"
          />
        </f-formitem>

        <!--计划用户测试完成日期-->
        <f-formitem label="计划用户测试完成日期">
          <f-date
            v-model="reqImplUnitModel.plan_test_finish_date"
            :options="planTestFinishDateOptions"
            hint=""
          />
        </f-formitem>

        <!--计划投产日期-->
        <f-formitem label="计划投产日期">
          <f-date
            v-model="reqImplUnitModel.plan_product_date"
            :options="planProductDateOptions"
            hint=""
          />
        </f-formitem>

        <!--UI审核-->
        <f-formitem label="UI审核" v-if="uiVerify" required>
          <fdev-select
            :disable="isLimited_ || updateFlag"
            option-label="label"
            option-value="value"
            emit-value
            map-options
            v-model="$v.reqImplUnitModel.ui_verify.$model"
            ref="reqImplUnitModel.ui_verify"
            :options="uiOptions"
            :rules="[
              () =>
                $v.reqImplUnitModel.ui_verify.required ||
                '请选择是否打开UI审核开关'
            ]"
          />
        </f-formitem>

        <!--预期我部人员工作量-->
        <f-formitem label="预期我部人员工作量（人月）" required>
          <fdev-input
            v-model="$v.reqImplUnitModel.dept_workload.$model"
            ref="reqImplUnitModel.dept_workload"
            :disable="isLimited_ || updateFlag"
            debounce="300"
            :rules="[
              () =>
                $v.reqImplUnitModel.dept_workload.required || '请输入工作量',
              () => $v.reqImplUnitModel.dept_workload.decimal || '只能输入数字',
              v => v > 0 || '请输入大于0的数字',
              v => !fn(v) || '输入的数字不规范'
            ]"
          >
          </fdev-input>
        </f-formitem>

        <!--预期公司人 员工作量（人月）-->
        <f-formitem label="预期公司人员工作量（人月）">
          <fdev-input
            v-model="reqImplUnitModel.company_workload"
            :disable="isLimited_ || updateFlag"
            debounce="300"
            :rules="[
              v => !isNaN(Number(v)) || '只能输入数字',
              v => Number(v) >= 0 || '只能输入大于等于0的数字',
              v => !fn1(v) || '输入的数字不规范'
            ]"
          >
          </fdev-input>
        </f-formitem>
      </div>
      <!-- 开发日期超期 提交业务超期 则回显超期类型和申请原因
      业务需求实施单元可以不必填，但需要写明申请原因 -->
      <div
        v-if="(isStartOverdue || isTestOverdue || advanceD) && !updateFlag"
        class="row justify-between"
      >
        <div v-show="advanceD" class="full-width tipColor">
          {{ advanceTip }}
        </div>
        <div
          v-show="isStartOverdue || isTestOverdue"
          class="full-width tipColor"
        >
          {{ overDateTip }}
        </div>
        <f-formitem v-show="isStartOverdue || isTestOverdue" label="超期类型">
          <fdev-input v-model="reqImplUnitModel.overdueType" disable />
        </f-formitem>
        <f-formitem
          v-show="isStartOverdue || isTestOverdue || advanceD"
          label="申请原因"
          required
        >
          <fdev-input
            v-model="reqImplUnitModel.overdueReason"
            ref="reqImplUnitModel.overdueReason"
            type="textarea"
            :rules="[
              () =>
                $v.reqImplUnitModel.overdueReason.required || '请输入申请原因'
            ]"
          />
        </f-formitem>
      </div>
    </Loading>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        v-if="!dialogLoading"
        :loading="loading"
        :label="commitBtn"
        @click="commitImplUnit"
      />
      <fdev-btn
        v-if="!dialogLoading"
        label="取消"
        outline
        dialog
        @click="handleImplUnitClose"
      />
    </template>
  </f-dialog>
</template>

<script>
import {
  createReqImplUnitModel,
  implementLeadDeptList,
  implementLeadTeamList,
  uiList
} from '../model';
import {
  formatOption,
  deepClone,
  validate,
  successNotify,
  resolveResponseError
} from '@/utils/utils';
import { formatUser } from '@/modules/User/utils/model';
import moment from 'moment';
import { mapState, mapGetters, mapActions } from 'vuex';
import { required, decimal } from 'vuelidate/lib/validators';
import Loading from '@/components/Loading';
import { queryIpmpUnitByDemandId } from '@/services/demand.js';

import {
  queryPartInfo,
  queryDemandAssessDate
} from '@/modules/Rqr/services/methods.js';

export default {
  name: 'ImplUnitAdd',
  components: { Loading },
  data() {
    return {
      msg1: '',
      defaultValue: '2021-07-01',
      reqImplUnitModel: createReqImplUnitModel(),
      isLimited_: false,
      loading: false,
      dialogLoading: false,
      uiOptions: uiList,
      selectedTask: {},
      taskOptions: [],
      groups: [],
      users: [],
      userOptions: [],
      leaderOptions: [],
      leaderOptionsClone: [],
      leaderOptionsClone2: [],
      leadDeptOptions: implementLeadDeptList,
      leadDeptOptionsClone: implementLeadDeptList,
      leadTeamOptions: implementLeadTeamList,
      leadTeamOptionsClone: implementLeadTeamList,
      belongGroupOptions: [],
      belongGroupOptionsClone: [],
      implUnitOptions: [],
      implUnitCloneOptions: [],
      dataQueryPartInfo: {},
      banStatus: false,
      dateObj: {
        planDevelopDate: '',
        planInnerTestDate: '',
        planTestStartDate: ''
      },
      isMustWrite: false,
      isFirstComming: false, // 是否初次进来
      ipmpDemandType: '',
      defaultYearMonth: '', // 默认展示年月(根据校验规则)
      timer: null, // 节流计时器
      dept_workload: '',
      company_workload: '',
      syncFlag: '', // 1 才调剩余工作量接口(其他情况不调用)
      demandAssessDate: {},
      overDateTip: '', //超期日期提示
      isStartOverdue: false, //开发日期超期标识
      isTestOverdue: false, //提交业务超期标识
      advanceTip:
        '未选择实施单元, 则需条线经理至fdev-工作台-我的审批完成审批方能新增任务。' //提前开发提示
    };
  },
  validations: {
    reqImplUnitModel: {
      ipmp_implement_unit_no: {
        required
      },
      implement_unit_content: {
        required
      },
      overdueReason: {
        required
      },
      // 实施牵头人
      implement_leader_all: {
        required
      },
      // 所属小组
      group: {
        required
      },
      plan_start_date: {
        required
      },
      plan_inner_test_date: {
        required
      },
      plan_test_date: {
        required
      },
      // 我部人员工作量
      dept_workload: {
        required,
        decimal
      },
      // 公司人员工作量
      // company_workload: {
      //   required,
      //   decimal
      // },
      // ui审核
      ui_verify: {
        required
      }
    }
  },
  watch: {
    'reqImplUnitModel.dept_workload': {
      handler: function(val, oldValue) {
        this.inputLimit(String(val), 'dept_workload', 2);
      }
    },
    'reqImplUnitModel.company_workload': {
      handler: function(val, oldValue) {
        this.inputLimit(String(val), 'company_workload', 2);
      }
    },
    'reqImplUnitModel.plan_start_date': {
      handler: function(val) {
        if (!this.updateFlag) {
          this.checkAssessDate();
        }
      }
    },
    'reqImplUnitModel.plan_test_date': {
      handler: function(val) {
        if (!this.updateFlag) {
          this.checkAssessDate();
        }
      }
    },
    'reqImplUnitModel.group': {
      handler: function(newValue, oldValue) {
        if (this.groupId) {
          this.reqImplUnitModel.group = this.groupId;
          return;
        }
        if (
          oldValue === '' &&
          this.reqImplUnitModel.implement_leader_all.length !== 0
        ) {
          this.leaderOptionsFilterByGroupId(newValue.id);
          return;
        }
        if (newValue !== '') {
          if (this.reqImplUnitModel.implement_leader_all.length !== 0) {
            this.reqImplUnitModel.implement_leader_all = [];
          }
          this.leaderOptionsFilterByGroupId(newValue.id);
        } else {
          this.leaderOptionsFilterByGroupId();
        }
      }
    },
    'reqImplUnitModel.implement_leader_all': {
      handler: function(val) {
        if (this.reqImplUnitModel.group === '' && val.length === 1) {
          this.reqImplUnitModel.group = this.groups.find(
            group => group.id === val[0].group_id
          );
        }
      }
    },
    // 勾选实施单元监听实施单元编号
    // 计划启动开发日期 planDevelopDate，
    // 计划提交内测日期 planInnerTestDate，
    // 计划提交用户测试日期 plan_test_date
    // 新增的日期比对应这些日期要小
    // 初次进来实施单元没有值或者没有赋值操作的话直接 return
    // 初次进来实施单元赋值时，需要判断是否是第一次进来。第一次进来则不清空日期, 直接回显
    // 进来后手动选择实施单元并且在选择实施单元前选了日期，则需要清空日期，重新定制日期校验规则
    // 关闭弹窗需要重置this.isFirstComming = false;
    'reqImplUnitModel.ipmp_implement_unit_no': {
      handler: function(val) {
        // 第一次进来 实施单元被赋值情况
        if (val && !this.isFirstComming) {
          // 业务需求获取剩余工作量提示(实施单元被赋值)
          if (this.demandType !== 'tech') {
            // 科技需求不做操作
            // 匹配实施单元对应日期数据
            let compare = this.implUnitOptions.filter(item => {
              return item.implUnitNum === val;
            });
            // 校验日期
            this.validateDateFun(compare);
          }
        }
        // 第一次进来 实施单元未被赋值情况
        if (!val || !this.isFirstComming) {
          this.isFirstComming = true;
          return;
        }
        if (this.demandType !== 'tech') {
          // 匹配实施单元对应日期数据
          this.getIpmpDateFun(val);
        }
      }
    }
  },
  computed: {
    ...mapState('demandsForm', {
      ipmpTasks: 'ipmpTasks',
      relatedPartIds: 'relatedPartIds',
      theQueryPartInfo: 'theQueryPartInfo'
    }),
    ...mapState('userForm', {
      groupsData: 'groups'
    }),
    ...mapGetters('user', ['isLoginUserList']),
    ...mapGetters('demandsForm', [
      'groupsCannotSelect',
      'repGroupsCannotSelect'
    ]),
    //title匹配a
    operateTitle() {
      const titelMap = {
        update: '编辑研发单元',
        add: '新增研发单元',
        rep: '补充研发单元'
      };
      return titelMap[this.operateType];
    },
    commitBtn() {
      const commitBtnMap = {
        update: '提交',
        add: '新增',
        rep: '新增'
      };
      return commitBtnMap[this.operateType];
    },
    updateFlag() {
      if (
        this.upImplUnitModel.implement_unit_status_normal === 1 ||
        this.upImplUnitModel.implement_unit_status_normal === ''
      ) {
        return false;
      } else {
        return true;
      }
    },
    open: {
      get() {
        return this.value;
      },
      set() {
        this.$emit('input', false);
      }
    },
    //判断是否是业务需求 是否选择了实施单元
    advanceD() {
      return (
        this.demandType === 'business' &&
        !this.reqImplUnitModel.ipmp_implement_unit_no
      );
    }
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    demandId: {
      type: String
    },
    groupId: {
      type: String,
      default: ''
    },
    demandType: {
      type: String
    },
    requireId: {
      type: String
    },
    upImplUnitModel: {
      type: Object,
      default: null
    },
    operateType: {
      type: String,
      default: 'add'
    },
    isDemandAdmin: {
      type: Boolean,
      default: false
    },
    isDemandLeader: {
      type: Boolean,
      default: false
    },
    isPartsLeader: {
      type: Boolean,
      default: false
    },
    isLimited: {
      type: Boolean,
      default: false
    },
    currentUserGroupId: {
      type: String
    },
    uiVerify: {
      type: Boolean
    },
    relate_part_detail: {
      type: Array,
      default: function() {
        return [];
      }
    },
    currentUser: {
      type: Object,
      default: null
    }
  },
  methods: {
    ...mapActions('demandsForm', {
      queryIpmpTasksByGroupId: 'queryByGroupId',
      addImplementUnit: 'addImplementUnit',
      supplyImplementUnit: 'supplyImplementUnit',
      updateImplementUnit: 'updateImplementUnit',
      updateByRecover: 'updateByRecover',
      queryDemandInfoDetail: 'queryDemandInfoDetail',
      theSaveQueryPartInfo: 'theSaveQueryPartInfo'
    }),
    ...mapActions('userForm', {
      queryGroup: 'fetchGroup'
    }),
    ...mapActions('user', {
      queryUser: 'fetch'
    }),
    isReadOnlyFun() {
      let { implement_unit_status_special: special } = this.upImplUnitModel;
      return this.operateType === 'update' && special === 2;
    },

    // 实施单元必填校验 业务需求实施单元可以不必填，但需要写明申请原因
    isRequiredFun() {
      // if (this.ipmpDemandType === 'business') {
      //   if (this.implUnitOptions.length > 0 && this.isMustWrite) return true;
      // }
      if (this.ipmpDemandType === 'tech') return true;
      return false;
    },
    // 提交
    async commitImplUnit() {
      this.$v.reqImplUnitModel.$touch();
      const keys = Object.keys(this.$refs).filter(
        key => this.$refs[key] && key.indexOf('reqImplUnitModel') > -1
      );
      // 实施单元未同步需要去除 校验实施单元编号的校验
      if (!this.isRequiredFun()) {
        let index = keys.indexOf('reqImplUnitModel.ipmp_implement_unit_no');
        keys.splice(index, 1);
      }
      validate(
        keys.map(key => {
          if (this.$refs[key].$refs.date) {
            return this.$refs[key].$children[0];
          }
          return this.$refs[key];
        })
      );
      // 工作量不满足要求终止提交
      if (this.reqImplUnitModel.dept_workload) {
        let dept_workload = Number(this.reqImplUnitModel.dept_workload);
        if (isNaN(dept_workload) || dept_workload <= 0) return;
        // 是否满足填写规范
        if (this.fn(this.reqImplUnitModel.dept_workload)) return;
      }
      if (this.reqImplUnitModel.company_workload) {
        let company_workload = Number(this.reqImplUnitModel.company_workload);
        if (isNaN(company_workload) || company_workload < 0) return;
        // 这里取输入框的值 01 在number后会为1
        if (this.fn1(this.reqImplUnitModel.company_workload)) return;
      }
      if (this.$v.reqImplUnitModel.$invalid) {
        const _this = this;
        const validateRes = keys.every(item => {
          if (item.indexOf('.') === -1) {
            return true;
          }
          const itemArr = item.split('.');
          return _this.$v.reqImplUnitModel[itemArr[1]].$invalid == false;
        });
        if (!validateRes) {
          return;
        }
      }
      this.loading = true;
      //如果研发单元是已评估完成则编辑不用校验超期
      if (!this.updateFlag) {
        if (
          !this.reqImplUnitModel.ipmp_implement_unit_no &&
          this.demandType === 'business'
        ) {
          //研发单元提前开发
          this.reqImplUnitModel.approveState = 'noSubmit';
          this.reqImplUnitModel.approveType = 'devApprove';
        } else if (!(this.isStartOverdue || this.isTestOverdue)) {
          //提前开发状态下 编辑时 关联实施单元且不为超期则 approveType清空 申请原因清空
          this.reqImplUnitModel.approveState = '';
          this.reqImplUnitModel.approveType = '';
          this.reqImplUnitModel.overdueReason = '';
        }
        //如果研发单元超期并且提前开发
        if (
          !this.reqImplUnitModel.ipmp_implement_unit_no &&
          this.demandType === 'business' &&
          (this.isStartOverdue || this.isTestOverdue)
        ) {
          this.reqImplUnitModel.approveType = 'dev&overdue';
        }
      }
      let cpReqImplUnitModel = JSON.parse(
        JSON.stringify(this.reqImplUnitModel)
      );
      //设置demandType
      cpReqImplUnitModel.demand_type = this.demandType;
      cpReqImplUnitModel.group_cn = cpReqImplUnitModel.group.name;
      cpReqImplUnitModel.implement_leader_all = cpReqImplUnitModel.implement_leader_all.map(
        leader => {
          return {
            id: leader.id,
            user_name_cn: leader.user_name_cn,
            user_name_en: leader.user_name_en
          };
        }
      );
      cpReqImplUnitModel.implement_leader = cpReqImplUnitModel.implement_leader_all.map(
        item => item.id
      );
      //牵头人域账号Array
      const leaderAccounts = cpReqImplUnitModel.implement_leader_all.map(
        leader => leader.user_name_en
      );
      Reflect.set(
        cpReqImplUnitModel,
        'implement_leader_account',
        leaderAccounts
      );
      cpReqImplUnitModel.dept_workload = Number(
        cpReqImplUnitModel.dept_workload
      );
      cpReqImplUnitModel.company_workload = Number(
        cpReqImplUnitModel.company_workload
      );
      if (this.demandType === 'business' || this.demandType === 'tech') {
        Reflect.deleteProperty(cpReqImplUnitModel, 'ipmp_implement_unit_no');
        if (!this.uiVerify) {
          Reflect.deleteProperty(cpReqImplUnitModel, 'ui_verify');
        }
      } else {
        // Reflect.set(
        //   cpReqImplUnitModel,
        //   'ipmpGroupId',
        //   cpReqImplUnitModel.task_no.groupId
        // );
        // cpReqImplUnitModel.task_no = cpReqImplUnitModel.task_no.taskNo;
        Reflect.deleteProperty(cpReqImplUnitModel, 'ui_verify');
      }
      if (this.operateType === 'add' || this.operateType === 'rep') {
        Reflect.set(cpReqImplUnitModel, 'demand_id', this.demandId);
        Reflect.set(cpReqImplUnitModel, 'batch_no', 1);
        Reflect.set(
          cpReqImplUnitModel,
          'ipmp_implement_unit_no',
          this.reqImplUnitModel.ipmp_implement_unit_no
        );
        Reflect.set(
          cpReqImplUnitModel,
          'group',
          // 需求评估选中涉及板块 新增补充研发单元 默认回显小组 参数group取this.reqImplUnitModel.group
          // 手动选择取this.reqImplUnitModel.group_cn.value
          this.reqImplUnitModel.group_cn.value || this.reqImplUnitModel.group
        );
        try {
          // 新增研发单元 /  补充研发单元逻辑
          // if (this.operateType === 'add') {
          //   await this.addImplementUnit(cpReqImplUnitModel);
          // } else if (this.operateType === 'rep') {
          //   await this.supplyImplementUnit(cpReqImplUnitModel);
          // }
          // 新增/补充研发单元 合并成一个按钮
          // addLight为true发新增接口 ,其他值发补充接口.
          let res = await queryPartInfo({
            demand_id: this.demandId,
            part_id: cpReqImplUnitModel.group //新增 更换所属小组
          });
          let addLight = res.addLight;
          if (addLight) {
            await this.addImplementUnit(cpReqImplUnitModel);
          } else {
            await this.supplyImplementUnit(cpReqImplUnitModel);
          }
          successNotify('新增研发单元成功！');
          this.loading = false;
          this.$emit('refImplUnitList', 'refresh', cpReqImplUnitModel.group);
        } catch (e) {
          this.loading = false;
        }
      } else {
        Reflect.set(
          cpReqImplUnitModel,
          'ipmp_implement_unit_no',
          this.reqImplUnitModel.ipmp_implement_unit_no
        );
        //特殊状态为2，更新5个计划日期
        try {
          if (this.isLimited_) {
            const {
              plan_start_date,
              plan_inner_test_date,
              plan_test_date,
              plan_test_finish_date,
              plan_product_date,
              overdueType,
              overdueReason,
              approveState
            } = cpReqImplUnitModel;
            await this.updateByRecover({
              plan_start_date,
              plan_inner_test_date,
              plan_test_date,
              plan_test_finish_date,
              plan_product_date,
              overdueType,
              overdueReason,
              approveState,
              id: this.reqImplUnitModel.id
            });
          } else {
            await this.updateImplementUnit(cpReqImplUnitModel);
          }
          successNotify('更新研发单元成功！');
          this.loading = false;
          this.$emit('refImplUnitList', 'refresh');
        } catch (e) {
          this.loading = false;
        }
      }
    },
    ipmpTipFun(item) {
      const banStatusType = ['已投产', '已撤销', '暂缓', '暂存'];
      if (this.ipmpDemandType === 'business') {
        if (
          item.usedSysCode !== 'ZH-0748' &&
          !!item.usedSysCode &&
          item.usedSysCode !== 'stockUnit'
        )
          return '该实施单元不属于fdev平台，不允许选择';
        if (item.leaderFlag === '3')
          return '该实施单元牵头人不为fdev用户, 不允许选择';
        if (banStatusType.includes(item.implStatusName))
          return '已投产、撤销，暂缓 ，暂存的实施单元不得被选择';
        if (!item.leaderGroup || !item.planInnerTestDate)
          return '请前往实施单元详情页补全实施单元牵头小组、预计提交内测时间！';
      }
      return '';
    },
    isDisplay(item) {
      const banStatusType = ['已投产', '已撤销', '暂缓', '暂存'];
      if (this.ipmpDemandType === 'business') {
        if (
          banStatusType.includes(item.implStatusName) ||
          !item.leaderGroup ||
          !item.planInnerTestDate ||
          item.leaderFlag === '3' ||
          (item.usedSysCode !== 'ZH-0748' &&
            !!item.usedSysCode &&
            item.usedSysCode !== 'stockUnit')
        ) {
          return true;
        }
        return false;
      }
      return false;
    },
    async queryDemandAssessDate() {
      const res = await resolveResponseError(() =>
        queryDemandAssessDate({ demandId: this.demandId })
      );
      this.demandAssessDate = res;
    },
    //校验评估完成日期
    checkAssessDate() {
      this.isStartOverdue = false;
      this.isTestOverdue = false;
      if (
        Object.keys(this.demandAssessDate).length == 0 ||
        !this.demandAssessDate.assessDate
      )
        return;
      if (this.reqImplUnitModel.plan_start_date) {
        this.isStartOverdue = this.compareDate(
          this.reqImplUnitModel.plan_start_date,
          this.demandAssessDate.assessDate,
          this.demandAssessDate.startOverdueDate
        );
      }
      if (this.reqImplUnitModel.plan_test_date) {
        this.isTestOverdue = this.compareDate(
          this.reqImplUnitModel.plan_test_date,
          this.demandAssessDate.assessDate,
          this.demandAssessDate.testOverdueDate
        );
      }
      this.reqImplUnitModel.overdueType = `${
        this.isStartOverdue ? '计划启动超期' : ''
      }${this.isStartOverdue && this.isTestOverdue ? '、' : ''} ${
        this.isTestOverdue ? '计划提交用户测试超期' : ''
      }`;
      this.overDateTip = `当${
        this.isStartOverdue
          ? `计划启动开发日期超过评估完成日期${
              this.demandAssessDate.startOverdueDate
            }天`
          : ''
      }${this.isStartOverdue && this.isTestOverdue ? '、' : ''}${
        this.isTestOverdue
          ? `计划提交用户测试日期超过评估完成日期${
              this.demandAssessDate.testOverdueDate
            }天`
          : ''
      }，则需条线经理至fdev-工作台-我的审批完成审批方能新增任务。(评估完成日期:${
        this.demandAssessDate.assessDate
      })`;
      if (this.isStartOverdue || this.isTestOverdue) {
        this.reqImplUnitModel.approveState = 'noSubmit';
        this.reqImplUnitModel.approveType = 'overdueApprove';
      } else {
        this.reqImplUnitModel.approveState = '';
        this.reqImplUnitModel.approveType = '';
        // this.reqImplUnitModel.overdueReason = '';
        // this.reqImplUnitModel.overdueType = '';
      }
    },
    //比较两个日期大小
    compareDate(start, end, day, type) {
      let types = type ? type : '-';
      if (!start || !end || start.length !== 10 || end.length !== 10) return;
      return (
        start.split(types).join('') - end.split(types).join('') > 0 &&
        moment(start).diff(moment(end), 'days') > day
      );
    },
    // 获取实施单元数据(第一步获取)
    async getIpmpUnitFun() {
      try {
        const res = await queryIpmpUnitByDemandId({ demandId: this.demandId });
        const banStatusType = ['已投产', '已撤销', '暂缓', '暂存'];
        this.ipmpDemandType = res.demand_type;
        res.data.forEach(item => {
          let flag = !item.planInnerTestDate || !item.leaderGroup;
          if (this.ipmpDemandType === 'business') {
            if (
              banStatusType.includes(item.implStatusName) ||
              item.leaderFlag === '3' ||
              (item.usedSysCode !== 'ZH-0748' &&
                !!item.usedSysCode &&
                item.usedSysCode !== 'stockUnit') ||
              flag
            )
              this.$set(item, 'disable', true);
          }
          // 实施单元必填
          // if (
          //   !banStatusType.includes(item.implStatusName) &&
          //   item.leaderFlag !== '3'
          // )
          //   this.isMustWrite = true;
        });

        // leaderGroup planInnerTestDate
        this.implUnitOptions = res.data;
        this.reqImplUnitModel.ipmp_implement_unit_no =
          this.upImplUnitModel.ipmp_implement_unit_no || void 0;
        this.implUnitCloneOptions = JSON.parse(
          JSON.stringify(this.implUnitOptions)
        );
        this.getDevUnitLeaderFun();
      } catch (e) {
        throw new Error(e);
      }
    },
    // 获取研发单元牵头人(第二步获取userOptions)
    async getDevUnitLeaderFun() {
      //获取用户列表
      if (this.isLoginUserList.length === 0) await this.queryUser();
      this.users = this.isLoginUserList.map(user =>
        formatOption(formatUser(user), 'name')
      );
      this.userOptions = this.users.slice();
      this.leaderOptions = this.users.slice();
      this.leaderOptionsClone = this.leaderOptions.slice();
      this.leaderOptionsClone2 = this.leaderOptions.slice();
      this.getBelongGroup();
    },
    // 获取所属小组数据（第三步获取）
    async getBelongGroup() {
      //获取分组
      await this.queryGroup();
      // theSaveQueryPartInfo获取查询板块信息 返回的字段addPart为新增研发单元的所属小组 supplyPart为补充研发单元的所属小组
      // 然后根据findGroupByGroupId获取的所有小组进行id匹配，则为所属小组options数据
      await this.theSaveQueryPartInfo({
        demand_id: this.demandId,
        part_id: this.groupId
      });
      this.dataQueryPartInfo = JSON.parse(
        JSON.stringify(this.theQueryPartInfo)
      );
      this.groups = formatOption(this.groupsData);
      //所属小组下拉框过滤为涉及板块
      this.belongGroupOptions = this.findGroupByGroupId(this.relatedPartIds);
      // 禁用小组选项
      this.belongGroupOptions.forEach(item => {
        if (this.operateType === 'rep') {
          this.dataQueryPartInfo.addPart.forEach(ctem => {
            if (item && item.id === ctem) this.$set(item, 'disable', true);
          });
        }
        if (this.operateType === 'add') {
          this.dataQueryPartInfo.supplyPart.forEach(ctem => {
            if (item && item.id === ctem) this.$set(item, 'disable', true);
          });
        }
      });
      this.belongGroupOptionsClone = this.belongGroupOptions.slice();
      if (this.demandType === 'tech') {
        //科技需求获取任务集
        await this.queryIpmpTasksByGroupId({});
        this.taskOptions = JSON.parse(JSON.stringify(this.ipmpTasks));
      }
      // 默认回显选中的板块小组
      let defaultGroup = this.belongGroupOptions.filter(
        item => item && item.id === this.groupId
      );
      this.reqImplUnitModel.group_cn =
        defaultGroup && defaultGroup.length > 0 ? defaultGroup[0].name : '';
      this.diffTypeDialogFun();
    },
    // 根据不同operateType 进行不同弹窗的操作（第四步：里面数据依赖前面几步的数据）
    async diffTypeDialogFun() {
      if (this.operateType === 'update' && this.upImplUnitModel !== null) {
        //在type为update的情况下，设置是否有只能编辑日期的限制条件
        this.isLimited_ = this.isLimited;
        //转换Model
        this.reqImplUnitModel = deepClone(
          this.transferModel(this.upImplUnitModel)
        );
        //转换牵头人name --> 牵头人对象
        this.reqImplUnitModel.implement_leader_all = this.reqImplUnitModel.implement_leader_all.map(
          leader => this.userOptions.find(user => user.id === leader.id)
        );
        //转换所属小组name --> 所属小组对象
        this.reqImplUnitModel.group = this.groups.find(
          group => group.name === this.reqImplUnitModel.group
        );
      } else {
        /*需求管理员和需求牵头人新增实施单元可以选小组，只能选预评估或者实施中的小组，
        在mapGetters中获取不能选的板块的ID*/
        await this.queryDemandInfoDetail({ id: this.demandId });
        //判断当前用户是否是板块牵头人，是的话所属小组回显当前用户所在小组
        if (!this.isDemandAdmin && !this.isDemandLeader && this.isPartsLeader) {
          let currentUserEvaGroups = this.relate_part_detail.filter(item => {
            if (item.assess_user.indexOf(this.currentUser.id) > -1) {
              return item;
            }
          });
          const groups = this.findGroupByGroupId(
            currentUserEvaGroups.map(g => g.part_id)
          );
          this.belongGroupOptions = groups.slice();
          this.belongGroupOptionsClone = this.belongGroupOptions.slice();
          if (this.operateType === 'add') {
            this.reqImplUnitModel.group = this.groupId;
            // item => this.dataQueryPartInfo.addPart.indexOf(item.id) > -1
          } else {
            this.reqImplUnitModel.group = this.groupId;
            // this.reqImplUnitModel.group = this.belongGroupOptionsClone.find(
            //   item =>
            //     item => this.dataQueryPartInfo.addPart.indexOf(item.id) > -1
            // );
          }
        }
      }
      this.dialogLoading = false;
    },
    // 处理页面默认展示日期的年月
    handleDefaultDate(val) {
      let default_Y_M = val.slice(0, 7).replace(/-/g, '/');
      this.defaultYearMonth = val ? default_Y_M : this.getCurrentDate();
    },
    // 弹窗开启初始化
    async initDialogData() {
      this.dialogLoading = true;
      // 重置日期数据
      this.dateObj.planDevelopDate = '';
      this.dateObj.planInnerTestDate = '';
      this.dateObj.planTestStartDate = '';
      this.defaultYearMonth = this.getCurrentDate();
      this.getIpmpUnitFun();
      // 获取剩余工作量
      //获取评估完成时间和XY值
      this.queryDemandAssessDate();
    },
    // 取消弹窗
    handleImplUnitClose() {
      // 重置 isMustWrite  isFirstComming
      this.isMustWrite = false;
      this.isFirstComming = false;
      this.reqImplUnitModel = createReqImplUnitModel();
      this.$emit('refImplUnitList', 'close');
    },
    // 所属小组过滤
    belongGroupsFilter(val, update, abort) {
      update(() => {
        this.belongGroupOptions = this.belongGroupOptionsClone.filter(
          group => group.fullName.indexOf(val) > -1
        );
      });
    },
    // 实施单元过滤
    unitNoFilter(val, update, abort) {
      update(() => {
        // 如果搜索值为空 直接返回整个列表数据
        if (!val) return (this.implUnitOptions = this.implUnitCloneOptions);
        this.implUnitOptions = this.implUnitCloneOptions.filter(tag => {
          if (tag.implUnitNum && tag.implUnitNum.indexOf(val) > -1) return true;
          if (tag.implContent && tag.implContent.indexOf(val) > -1) return true;
          if (tag.implLeader && tag.implLeader.indexOf(val) > -1) return true;
          if (tag.prjNum && tag.prjNum.indexOf(val) > -1) return true;
          if (tag.implLeaderName && tag.implLeaderName.indexOf(val) > -1)
            return true;
          return false;
        });
      });
    },
    inputLimit(val, key, num) {
      if (val && val.indexOf('.') > -1 && val.split('.').length <= 2) {
        if (val.split('.')[1].length > 2) {
          this.reqImplUnitModel[key] = this.reqImplUnitModel[key].substr(
            0,
            val.split('.')[0].length + num + 1
          );
        }
      }
    },
    leadDeptFilter(val, update, abort) {
      update(() => {
        this.leadDeptOptions = this.leadDeptOptionsClone.filter(
          dept => dept.indexOf(val) > -1
        );
      });
    },
    leaderTeamFilter(val, update, abort) {
      update(() => {
        this.leadTeamOptions = this.leadTeamOptionsClone.filter(
          team => team.label.indexOf(val) > -1
        );
      });
    },
    leaderOptionsFilterByInput(val, update, abort) {
      update(() => {
        this.leaderOptions = this.leaderOptionsClone2.filter(
          leader =>
            leader.user_name_cn.indexOf(val) > -1 ||
            leader.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    leaderOptionsFilterByGroupId(groupId) {
      this.leaderOptions = this.userOptions.filter(item => {
        if (item.email.indexOf('@') > -1) {
          return item.email.split('@')[1] === 'spdb.com.cn';
        }
      });
      this.leaderOptionsClone2 = this.leaderOptions.slice();
    },
    formatDate(date) {
      if (!date || typeof date === 'object') {
        return;
      }
      return moment(date).format('YYYY/MM/DD');
    },
    findGroupByGroupId(groupId) {
      if (Array.isArray(groupId)) {
        return groupId.map(group_id => this.findGroupByGroupId(group_id));
      } else {
        return this.groups.find(group => group.id === groupId);
      }
    },
    /* 计划启动开发日期 */
    planStartDateOptions(date) {
      let afterDate = this.formatDate(
        this.reqImplUnitModel.plan_inner_test_date ||
          this.reqImplUnitModel.plan_test_date ||
          this.reqImplUnitModel.plan_test_finish_date ||
          this.reqImplUnitModel.plan_product_date
      );
      if (!this.reqImplUnitModel.plan_start_date) {
        this.reqImplUnitModel.plan_start_date = afterDate ? afterDate : '';
      }
      // 勾选实施单元日期不大于该实施单元计划启动开发日期
      if (this.dateObj.planDevelopDate) {
        const afterDate = this.formatDate(this.dateObj.planDevelopDate);
        return date <= afterDate;
      }
      return afterDate ? date <= afterDate : true;
    },
    // 计划提交用户测试日期planTestStartDate，
    // 计划用户测试完成日期planTestFinishDate
    // 新增的日期比对应这些日期要小
    /* 计划内测日期 */
    planInnerTestDateOptions(date) {
      const beforeDate = this.reqImplUnitModel.plan_start_date;
      const afterDate = this.formatDate(
        this.reqImplUnitModel.plan_test_date ||
          this.reqImplUnitModel.plan_test_finish_date ||
          this.reqImplUnitModel.plan_product_date
      );
      const smallerThanAfterDate = afterDate ? date <= afterDate : true;
      if (
        !this.reqImplUnitModel.plan_inner_test_date &&
        !this.dateObj.planInnerTestDate
      ) {
        this.reqImplUnitModel.plan_inner_test_date = beforeDate
          ? beforeDate
          : '';
      }
      // 勾选实施单元日期不大于该实施单元计划提交用户测试日期
      if (this.dateObj.planInnerTestDate) {
        const afterDate = this.formatDate(
          this.dateObj.planInnerTestDate || this.reqImplUnitModel.plan_test_date
        );
        const smallerThanAfterDate = afterDate ? date <= afterDate : true;
        let cur = this.reqImplUnitModel.plan_inner_test_date;
        this.reqImplUnitModel.plan_inner_test_date = cur
          ? cur
          : beforeDate || '';
        return this.formatDate(beforeDate) <= date && smallerThanAfterDate;
      }
      return this.formatDate(beforeDate) <= date && smallerThanAfterDate;
    },
    /* 计划提交用户测试日期 */
    planTestDateOptions(date) {
      const beforeDate =
        this.reqImplUnitModel.plan_inner_test_date ||
        this.reqImplUnitModel.plan_start_date;
      const afterDate = this.formatDate(
        this.reqImplUnitModel.plan_test_finish_date ||
          this.reqImplUnitModel.plan_product_date
      );
      const smallerThanAfterDate = afterDate ? date <= afterDate : true;
      if (
        !this.reqImplUnitModel.plan_test_date &&
        !this.dateObj.planTestStartDate
      ) {
        this.reqImplUnitModel.plan_test_date = beforeDate ? beforeDate : '';
      }
      // 勾选实施单元日期不大于该实施单元计划提交用户测试日期
      if (this.dateObj.planTestStartDate) {
        const afterDate = this.formatDate(this.dateObj.planTestStartDate);
        const smallerThanAfterDate = afterDate ? date <= afterDate : true;
        let cur = this.reqImplUnitModel.plan_test_date;
        this.reqImplUnitModel.plan_test_date = cur ? cur : beforeDate || '';
        return this.formatDate(beforeDate) <= date && smallerThanAfterDate;
      }
      return this.formatDate(beforeDate) <= date && smallerThanAfterDate;
    },
    /* 计划测试完成日期 */
    planTestFinishDateOptions(date) {
      const beforeDate =
        this.reqImplUnitModel.plan_test_date ||
        this.reqImplUnitModel.plan_inner_test_date ||
        this.reqImplUnitModel.plan_start_date;
      const afterDate = this.formatDate(
        this.reqImplUnitModel.plan_product_date
      );
      const smallerThanAfterDate = afterDate ? date <= afterDate : true;
      if (!this.reqImplUnitModel.plan_test_finish_date) {
        this.reqImplUnitModel.plan_test_finish_date = beforeDate
          ? beforeDate
          : '';
      }
      // 勾选实施单元日期不大于该实施单元计划测试完成日期
      if (this.dateObj.planTestFinishDate) {
        const afterDate = this.formatDate(this.dateObj.planTestFinishDate);
        const smallerThanAfterDate = afterDate ? date <= afterDate : true;
        let cur = this.reqImplUnitModel.plan_test_finish_date;
        this.reqImplUnitModel.plan_test_finish_date = cur
          ? cur
          : beforeDate || '';
        return this.formatDate(beforeDate) <= date && smallerThanAfterDate;
      }
      return this.formatDate(beforeDate) <= date && smallerThanAfterDate;
    },
    /* 计划投产日期 */
    planProductDateOptions(date) {
      const beforeDate =
        this.reqImplUnitModel.plan_test_finish_date ||
        this.reqImplUnitModel.plan_test_date ||
        this.reqImplUnitModel.plan_inner_test_date ||
        this.reqImplUnitModel.plan_start_date;
      if (!this.reqImplUnitModel.plan_product_date) {
        this.reqImplUnitModel.plan_product_date = beforeDate ? beforeDate : '';
      }
      if (this.dateObj.planProductDate) {
        const afterDate = this.formatDate(this.dateObj.planProductDate);
        const smallerThanAfterDate = afterDate ? date <= afterDate : true;
        let cur = this.reqImplUnitModel.plan_product_date;
        this.reqImplUnitModel.plan_product_date = cur ? cur : beforeDate || '';
        return this.formatDate(beforeDate) <= date && smallerThanAfterDate;
      }
      return this.formatDate(beforeDate) <= date;
    },
    //将要上传的字段，放在一个新的model里面，只存放要上传的字段
    transferModel(model) {
      const transferedModel = createReqImplUnitModel();
      for (let key in model) {
        if (Reflect.has(transferedModel, key)) {
          transferedModel[key] = model[key];
        }
      }
      Reflect.set(transferedModel, 'id', model.id);
      return transferedModel;
    },
    resetForm() {
      this.$q
        .dialog({
          title: `清空确认`,
          message: `确认要清空表单吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(() => {
          let t = JSON.parse(JSON.stringify(this.reqImplUnitModel));
          this.reqImplUnitModel = createReqImplUnitModel();
          if (this.operateType === 'update') {
            Reflect.set(this.reqImplUnitModel, 'id', t.id);
          }
          this.reqImplUnitModel.demand_id = t.demand_id;
          this.reqImplUnitModel.batch_no = t.batch_no;
          t = null;
        });
    },
    validateFun(v) {
      let flag = this.implUnitOptions && this.implUnitOptions.length > 0;
      return flag && !!v;
    },
    // 整合实施单元日期数据
    getIpmpDateFun(val) {
      // 匹配实施单元对应日期数据
      let compare = this.implUnitOptions.filter(
        item => item.implUnitNum === val
      );
      const {
        planDevelopDate,
        planInnerTestDate,
        planTestStartDate
      } = compare[0];
      // 勾选实施单元先清空日期数据
      if (planDevelopDate < this.reqImplUnitModel.plan_start_date)
        this.reqImplUnitModel.plan_start_date = '';
      if (planInnerTestDate < this.reqImplUnitModel.plan_inner_test_date)
        this.reqImplUnitModel.plan_inner_test_date = '';
      if (planTestStartDate < this.reqImplUnitModel.plan_test_date)
        this.reqImplUnitModel.plan_test_date = '';
      // 如果存在实施单元, 日期面板需要定位到指定月份面板
      if (compare.length > 0)
        this.handleDefaultDate(compare[0].planDevelopDate);
      // 校验日期
      this.validateDateFun(compare);
    },
    // 日期与实施单元对比添加校验
    validateDateFun(detail) {
      if (detail.length === 0) return;
      const {
        planDevelopDate,
        planInnerTestDate,
        planTestStartDate,
        planTestFinishDate,
        planProductDate
      } = detail[0];
      // 勾选 或者 默认赋值回显日期（可能为空）
      this.reqImplUnitModel.plan_start_date = planDevelopDate;
      this.reqImplUnitModel.plan_inner_test_date = planInnerTestDate;
      this.reqImplUnitModel.plan_test_date = planTestStartDate;
      // 添加校验规则
      if (planDevelopDate) this.dateObj.planDevelopDate = planDevelopDate;
      if (planInnerTestDate) this.dateObj.planInnerTestDate = planInnerTestDate;
      if (planTestStartDate) this.dateObj.planTestStartDate = planTestStartDate;
      if (planTestFinishDate)
        this.dateObj.planTestFinishDate = planTestFinishDate;
      if (planProductDate) this.dateObj.planProductDate = planProductDate;
      if (!planDevelopDate)
        this.dateObj.planDevelopDate = planInnerTestDate || planTestStartDate;
      if (!planInnerTestDate)
        this.dateObj.planInnerTestDate = planTestStartDate;
    },
    getCurrentDate() {
      let current = new Date();
      let Y = current.getFullYear();
      let M = current.getMonth() + 1;
      M = M >= 10 ? M : '0' + M;
      return `${Y}/${M}`;
    },
    fn(v) {
      // 不规范返回true
      return (
        String(v)
          .split('.')[0]
          .slice(0, 1) === '0' && String(v).split('.')[0].length > 1
      );
    },
    fn1(v) {
      // 不规范返回true
      return (
        String(v)
          .split('.')[0]
          .slice(0, 1) === '0' && String(v).split('.')[0].length > 1
      );
    }
  }
};
</script>
<style lang="stylus" scoped>
.q-gutter-diaLine
  padding-bottom 20px !important

.btn-margin {
  margin: 20px;
}

.tipColor
  color #ef5350
  padding-bottom 20px
</style>
