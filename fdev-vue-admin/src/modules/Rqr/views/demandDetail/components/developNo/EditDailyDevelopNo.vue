<template>
  <f-dialog
    @input="$emit('update:isShowEditDailyDevelopNo', false)"
    right
    title="编辑研发单元"
    :value="isShowEditDailyDevelopNo"
    @before-show="initDialogData"
  >
    <Loading :visible="dialogLoading">
      <div>
        <!-- <!其他需求任务编号> -->
        <f-formitem
          label="其他需求任务编号"
          required
          help="支持任务编号,任务名称,厂商负责人中文名,任务负责人中文名,项目/任务集搜索"
        >
          <fdev-select
            use-input
            ref="reqImplUnitModel.other_demand_task_num"
            v-model="reqImplUnitModel.other_demand_task_num"
            :options="implUnitOptions"
            option-label="taskNum"
            option-value="taskNum"
            emit-value
            :disable="isLimited"
            @filter="unitNoFilter"
            :rules="[v => !!v || '其他需求任务编号']"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section style="cursor:pointer">
                  <fdev-item-label :title="scope.opt.taskNum">{{
                    scope.opt.taskNum
                  }}</fdev-item-label>
                  <fdev-item-label caption>
                    <div :title="scope.opt.taskLeaderName">
                      {{ scope.opt.taskLeaderName }}
                    </div>
                    <div :title="scope.opt.taskName">
                      {{ scope.opt.taskName }}
                    </div>
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <!-- <!研发单元内容> -->
        <f-formitem label="研发单元内容" required>
          <fdev-input
            :disable="isLimited"
            v-model="reqImplUnitModel.implement_unit_content"
            ref="reqImplUnitModel.implement_unit_content"
            type="text"
            :rules="[v => !!v || '请输入研发单元内容']"
          />
        </f-formitem>
        <!--研发单元牵头人-->
        <f-formitem label="研发单元牵头人" required>
          <fdev-select
            :disable="isLimited"
            use-chips
            use-input
            multiple
            ref="reqImplUnitModel.implement_leader_all"
            v-model="reqImplUnitModel.implement_leader_all"
            :options="leaderOptions"
            @filter="leaderOptionsFilterByInput"
            :rules="[v => (v && v.length) || '请选择研发单元牵头人']"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label caption>
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
            disable
            ref="reqImplUnitModel.group"
            v-model="reqImplUnitModel.group"
            :options="belongGroupOptions"
            option-label="part_name"
            option-value="part_id"
            :rules="[v => !!v || '请所属小组']"
            @filter="belongGroupsFilter"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <!-- 所属小组 前端都可选择 , 后台接口已经判断 -->
                  <fdev-item-label :title="scope.opt.part_name">
                    {{ scope.opt.part_name }}
                  </fdev-item-label>
                  <fdev-item-label caption :title="scope.opt.part_name">
                    {{ scope.opt.part_name }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <!--计划启动日期-->
        <f-formitem label="计划启动日期" required>
          <f-date
            v-model="reqImplUnitModel.plan_start_date"
            :options="planStartDateOptions"
            :default-year-month="defaultYearMonth"
            ref="reqImplUnitModel.plan_start_date"
            :rules="[v => !!v || '请输入计划启动日期']"
          />
        </f-formitem>
        <!--计划投产日期-->
        <f-formitem label="计划完成日期">
          <f-date
            v-model="reqImplUnitModel.plan_product_date"
            :options="planProductDateOptions"
            hint=""
          />
        </f-formitem>
        <!--预期我部人员工作量-->
        <f-formitem label="预期我部人员工作量（人月）" required>
          <fdev-input
            :disable="isLimited"
            v-model="reqImplUnitModel.dept_workload"
            ref="reqImplUnitModel.dept_workload"
            debounce="300"
            :rules="[
              v => !isNaN(Number(v)) || '请输入数字',
              v => !!v || '请输入工作量',
              v => Number(v) > 0 || '只能输入大于0的数字',
              v => !validateData(v) || '输入的数字不规范'
            ]"
          >
          </fdev-input>
        </f-formitem>
        <!--预期公司人 员工作量（人月）-->
        <f-formitem label="预期公司人员工作量（人月）">
          <fdev-input
            :disable="isLimited"
            v-model="reqImplUnitModel.company_workload"
            debounce="300"
            :rules="[
              v => !isNaN(Number(v)) || '请输入数字',
              v => Number(v) >= 0 || '只能输入大于等于0的数字',
              v => !validateData(v) || '输入的数字不规范'
            ]"
          >
          </fdev-input>
        </f-formitem>
      </div>
    </Loading>
    <template v-slot:btnSlot>
      <fdev-btn
        @click="editDailyDevelopNo"
        dialog
        v-if="!dialogLoading"
        label="确认"
        :loading="loading"
      />
      <fdev-btn
        label="取消"
        @click="$emit('update:isShowEditDailyDevelopNo', false)"
        outline
        dialog
      />
    </template>
  </f-dialog>
</template>
<script>
import moment from 'moment';
import Loading from '@/components/Loading';

import { queryOtherDemandTaskList } from '@/modules/Rqr/services/methods.js';
import { mapState, mapGetters, mapActions } from 'vuex';
import {
  formatOption,
  deepClone,
  successNotify,
  errorNotify
} from '@/utils/utils.js';
import { formatUser } from '@/modules/User/utils/model.js';
import { updateImplementUnit } from '@/modules/Rqr/services/methods.js';
export default {
  name: 'EditDailyDevelopNo', //编辑日常研发单元
  props: {
    isShowEditDailyDevelopNo: {
      type: Boolean,
      default: false
    },
    demandId: {
      type: String
    },
    groupId: {
      type: String
    },
    demandType: {
      type: String
    },
    isDemandAdmin: {
      //需求负责人
      type: Boolean,
      default: false
    },
    isDemandLeader: {
      //需求管理员
      type: Boolean,
      default: false
    },
    isPartsLeader: {
      //板块牵头人
      type: Boolean,
      default: false
    },
    demandDetail: {
      type: Object,
      default: () => {}
    },
    // 是否可以编辑
    // 殊研发单元状态1：暂缓中、2：恢复中、3：恢复完成
    // 状态为恢复中 不允许编辑
    isLimited: {
      type: Boolean,
      default: false
    },
    //状态为评估中 不允许改变小组
    isLimitedChangeGroup: {
      type: Boolean,
      default: false
    },
    rowData: {
      type: Object,
      default: () => {}
    }
  },
  components: {
    Loading
  },
  data() {
    return {
      reqImplUnitModel: {
        other_demand_task_num: null, //日常编号
        implement_unit_content: null, //日常内容
        implement_leader_all: null, //牵头人
        group: null, //所属小组
        plan_start_date: null, //计划开始日期可选范围 :小于等于选中的其他任务编号 返回的 planStartDate(计划开始时间) 且小于计划投产日期
        plan_product_date: null, //计划投产日期可选范围:小于等于选中的其他任务编号 返回的 planDoneDate(计划投产时间) 且大于plan_start_date(计划开始时间)
        dept_workload: null,
        company_workload: null
      },
      // reqImplUnitModel: {
      //   other_demand_task_num: '', //日常编号
      //   implement_unit_content: '12', //日常内容
      //   implement_leader_all: [
      //     {
      //       id: '5daff39df57ddb001047b47f',
      //       user_name_cn: 'xxx',
      //       user_name_en: 'xxx'
      //     }
      //   ], //牵头人
      //   group: {
      //     part_id: '6035fc8ee18171d090ae6685',
      //     part_name: '浦发银行',
      //     assess_status: '2'
      //   }, //所属小组
      //   plan_start_date: '', //计划开始日期可选范围 :小于等于选中的其他任务编号 返回的 planStartDate(计划开始时间) 且小于计划投产日期
      //   plan_product_date: '', //计划投产日期可选范围:小于等于选中的其他任务编号 返回的 planDoneDate(计划投产时间) 且大于plan_start_date(计划开始时间)
      //   dept_workload: 10,
      //   company_workload: 20
      // },
      loading: false,
      dialogLoading: false,
      implUnitOptions: [], //其他需求任务编号 筛选后的 options,
      implUnitCloneOptions: [], //其他需求任务编号接口返回的全量 options,
      groups: [],
      users: [],
      leaderOptions: [], //研发单元牵头人 过滤后
      leaderOptionsClone: [],
      leaderOptionsClone2: [],
      belongGroupOptions: [], //所属小组
      belongGroupOptionsClone: [], //所属小组  全量数据
      defaultYearMonth: '2021/08', // 默认展示年月(根据校验规则)
      // 选中其他任务编号后返回的 日期对象
      dateObj: {
        planStartDate: '',
        planDoneDate: ''
      }
    };
  },
  computed: {
    ...mapState('demandsForm', {
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
    ])
  },
  watch: {
    'reqImplUnitModel.other_demand_task_num': {
      handler: function(val) {
        if (val) {
          let currentVal = this.implUnitOptions.filter(
            item => item.taskNum === val
          );
          this.dateObj.planStartDate = currentVal[0].planStartDate; //2021/06/18
          this.dateObj.planDoneDate = currentVal[0].planDoneDate; //2021/09/14
          // 选择其他需求任务后 给 计划开始  和 计划完成赋值
          this.reqImplUnitModel.plan_start_date = currentVal[0].planStartDate;
          this.reqImplUnitModel.plan_product_date = currentVal[0].planDoneDate;
        }
      }
    },
    'reqImplUnitModel.dept_workload': {
      handler: function(val, oldValue) {
        this.inputLimit(String(val), 'dept_workload', 2);
      }
    },
    'reqImplUnitModel.company_workload': {
      handler: function(val, oldValue) {
        this.inputLimit(String(val), 'company_workload', 2);
      }
    }
  },
  methods: {
    ...mapActions('userForm', {
      queryGroup: 'fetchGroup'
    }),
    ...mapActions('user', {
      queryUser: 'fetch'
    }),
    ...mapActions('demandsForm', {
      theSaveQueryPartInfo: 'theSaveQueryPartInfo'
    }),
    validateData(v) {
      // 不规范返回true
      return (
        String(v)
          .split('.')[0]
          .slice(0, 1) === '0' && String(v).split('.')[0].length > 1
      );
    },
    // 初始化弹框
    async initDialogData() {
      // 初始化清空日常任务的 计划开始和结束日期
      this.dateObj.planStartDate = '';
      this.dateObj.planDoneDate = '';
      this.dialogLoading = true;

      //1 获取新增研发单元编号
      await this.getIpmpUnitFun();

      // 2 研发单元牵头人
      // 获取分组
      // 初始化 的时候 groupsData 为空
      await this.queryUserLists();

      //3 获取小组
      await this.queryPartInfo();
      // 初始化完成 去掉loading
      this.dialogLoading = false;
      // 编辑初始化 赋值
      this.reqImplUnitModel.other_demand_task_num = this.rowData.other_demand_task_num;
      this.reqImplUnitModel.implement_unit_content = this.rowData.implement_unit_content;

      // 牵头人回显
      this.reqImplUnitModel.implement_leader_all = this.rowData.implement_leader_all.map(
        leader => this.leaderOptionsClone2.find(user => user.id === leader.id)
      );

      // 默认回显选中的板块小组
      let defaultGroup = this.belongGroupOptionsClone.filter(
        item => item.part_id === this.rowData.group
      );
      this.reqImplUnitModel.group = defaultGroup[0].part_name;

      this.reqImplUnitModel.plan_start_date = this.rowData.plan_start_date;
      this.reqImplUnitModel.plan_product_date = this.rowData.plan_product_date;
      this.reqImplUnitModel.dept_workload = this.rowData.dept_workload;
      this.reqImplUnitModel.company_workload =
        this.rowData.company_workload || 0;
      // this.$refs['reqImplUnitModel.other_demand_task_num'].validate();
    },
    async queryUserLists() {
      if (this.groupsData.length === 0) {
        await this.queryGroup();
      }
      //获取用户列表
      if (this.isLoginUserList.length === 0) {
        await this.queryUser();
      }
      this.users = this.isLoginUserList.map(user =>
        formatOption(formatUser(user), 'name')
      );
      this.leaderOptions = deepClone(this.users);
      this.leaderOptionsClone = deepClone(this.leaderOptions);
      this.leaderOptionsClone2 = deepClone(this.leaderOptions);
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
    //确认编辑
    async editDailyDevelopNo() {
      const keys = Object.keys(this.$refs).filter(
        key => this.$refs[key] && key.indexOf('reqImplUnitModel') > -1
      );

      let res = keys.map(key => {
        if (this.$refs[key].$refs.date) {
          if (!this.$refs[key].$children[0].validate()) return 'error';
        }
        if (!this.$refs[key].validate()) return 'error';
      });
      //校验未通过  阻止代码执行
      if (res.includes('error')) return;

      // 检验通过 发编辑接口
      this.loading = true;
      let params = {};
      let cpReqImplUnitModel = JSON.parse(
        JSON.stringify(this.reqImplUnitModel)
      );
      params.demand_id = this.demandId;
      params.demand_type = this.demandType;
      params.id = this.rowData.id; //研发单元id
      // 八个表单
      params.other_demand_task_num = cpReqImplUnitModel.other_demand_task_num;
      params.implement_unit_content = cpReqImplUnitModel.implement_unit_content;
      // 日常任务传 implement_leader_all
      params.implement_leader_all = cpReqImplUnitModel.implement_leader_all;

      // 1小组回 显示后不修改 只有中文名称 要匹配出 找出 groupid
      // 2 小组回 显示后修改
      params.group =
        cpReqImplUnitModel.group && typeof cpReqImplUnitModel.group === 'string'
          ? this.rowData.group
          : cpReqImplUnitModel.group.part_id;

      params.plan_start_date = cpReqImplUnitModel.plan_start_date;
      params.plan_product_date = cpReqImplUnitModel.plan_product_date;
      params.dept_workload = cpReqImplUnitModel.dept_workload;
      params.company_workload = cpReqImplUnitModel.company_workload || 0;
      try {
        let res = await updateImplementUnit(params);
        if (res && res.code && res.code != 'AAAAAAA') {
          // 失败 res
          // {
          //   code: 'COMM004';
          //   fdevStatus: 'error';
          //   msg: '请求参数dept_workload为空异常![fdemand]';
          // }
          errorNotify(res.msg);
          return;
        } else {
          // res {}
          // 成功
          // 关闭弹框
          // 刷新 研发单元列表列表
          // 小组 id  编号 刷新
          successNotify('更新成功！');
          this.$emit('update:isShowEditDailyDevelopNo', false);
          this.$emit('updateList', params.group, '', 'refresh');
        }
      } catch (e) {
        throw new Error(e);
      } finally {
        this.loading = false;
      }
      // 关闭弹框
      this.$emit('update:isShowEditDailyDevelopNo', false);
      // 刷新 研发单元列表列表
      // 小组 id  编号 刷新
      this.$emit('updateList', params.group, '', 'refresh');
    },
    // 其他其他需求任务编号(第一步获取)
    async getIpmpUnitFun() {
      this.dialogLoading = true;
      try {
        const res = await queryOtherDemandTaskList({ demandId: this.demandId });
        this.implUnitOptions = res.data;
        this.implUnitCloneOptions = JSON.parse(
          JSON.stringify(this.implUnitOptions)
        );
        this.dialogLoading = false;
      } catch (e) {
        this.dialogLoading = false;
      }
    },
    // 其他需求任务编号
    unitNoFilter(val, update, abort) {
      update(() => {
        // 如果搜索值为空 直接返回整个列表数据
        this.implUnitOptions = this.implUnitCloneOptions.filter(
          item =>
            (item.taskNum && item.taskNum.indexOf(val) > -1) ||
            (item.taskNum &&
              item.taskNum.toLowerCase().includes(val.toLowerCase())) ||
            (item.taskName && item.taskName.indexOf(val) > -1) ||
            (item.taskName &&
              item.taskName.toLowerCase().includes(val.toLowerCase())) ||
            (item.firmLeaderName && item.firmLeaderName.indexOf(val) > -1) ||
            (item.firmLeaderName &&
              item.firmLeaderName.toLowerCase().includes(val.toLowerCase())) ||
            (item.taskLeaderName && item.taskLeaderName.indexOf(val) > -1) ||
            (item.taskLeaderName &&
              item.taskLeaderName.toLowerCase().includes(val.toLowerCase())) ||
            (item.prjNum && item.prjNum.indexOf(val) > -1) ||
            (item.prjNum &&
              item.prjNum.toLowerCase().includes(val.toLowerCase()))
        );
      });
    },
    //牵头人 过滤
    leaderOptionsFilterByInput(val, update, abort) {
      update(() => {
        this.leaderOptions = this.leaderOptionsClone2.filter(
          leader =>
            leader.user_name_cn.indexOf(val) > -1 ||
            leader.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    async queryPartInfo() {
      //3 查询所属小组
      // 根据需求id和板块id判断新增和补充的可选择的下拉小组id
      // /fdemand/api/demand/queryPartInfo
      // 初始化 的时候 groupId 为空
      await this.theSaveQueryPartInfo({
        demand_id: this.demandId,
        part_id: this.groupId
        // part_id: '603716d89f7ed34b70eb40b5'
      });
      this.dataQueryPartInfo = deepClone(this.theQueryPartInfo);
      //所属小组下拉框过滤为涉及板块
      this.belongGroupOptions = this.demandDetail.relate_part_detail;
      this.belongGroupOptionsClone = deepClone(
        this.demandDetail.relate_part_detail
      );
    },
    // 所属小组过滤
    belongGroupsFilter(val, update, abort) {
      update(async () => {
        this.belongGroupOptions = this.belongGroupOptionsClone.filter(
          group => group.part_name.indexOf(val) > -1
        );
      });
    },
    /* 计划启动日期 */
    planStartDateOptions(date) {
      // 计划启动时间小于等于计划完成时间
      // 输入的时间
      let afterDate = this.formatDate(this.reqImplUnitModel.plan_product_date);

      if (!this.reqImplUnitModel.plan_start_date) {
        this.reqImplUnitModel.plan_start_date = afterDate ? afterDate : '';
      }
      if (this.dateObj.planStartDate) {
        // 日常任务的 计划启动时间 固定的
        const planStartDate = this.formatDate(this.dateObj.planStartDate);
        return date <= planStartDate && date <= afterDate;
      }
      return afterDate ? date <= afterDate : true;
    },
    planProductDateOptions(date) {
      let minDate =
        this.formatDate(this.reqImplUnitModel.plan_start_date) || '';
      let maxDate = this.formatDate(this.dateObj.planDoneDate) || '';
      // plan_product_date: '',
      //计划投产日期可选范围:小于等于选中的其他任务编号 返回的 planDoneDate(计划投产时间) 且大于plan_start_date(计划开始时间)
      if (minDate) {
        if (maxDate) {
          return minDate <= date && date <= maxDate;
        } else {
          return minDate <= date;
        }
      } else {
        //没有 计划启动时间
        if (maxDate) {
          return date <= maxDate;
        } else {
          // 没有最小时间 也没有最大时间
          return true;
        }
      }
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
    }
  }
};
</script>
