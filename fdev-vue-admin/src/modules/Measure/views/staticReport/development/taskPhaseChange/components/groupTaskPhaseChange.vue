<template>
  <f-block>
    <!-- 查询条件 -->
    <div>
      <!-- 所属小组 -->
      <f-gstree
        title="所属小组"
        title-icon="member_s_f"
        :data-source="analysisData"
        @select-group="selectGroup"
        @click-group="clickGroup"
        @reset-tree="resetTree"
        v-if="analysisData && analysisData.length > 0"
      >
        <!-- gstree自带重置按钮，两边插槽 -->
        <template #topLeft>
          <!-- 包含子组 -->
          <fdev-toggle
            size="lg"
            :value="isIncludeChildren"
            @input="updateIsIncludeChildren($event)"
            label="包含子组"
            left-label
        /></template>
        <template #topRight>
          <!-- 查询按钮 -->
          <fdev-btn
            label="查询"
            dialog
            @click="init(selectedGroups)"
            ficon="search"
            v-forbidMultipleClick/></template
      ></f-gstree>
      <div class="q-my-md row items-center">
        <!-- 人均数 -->
        <fdev-toggle
          :value="percapita"
          label="人均数"
          @input="percapitaOpened($event)"
          left-label
        />
        <!-- 人员类型 -->
        <button
          v-show="percapita"
          v-for="role in roleType"
          :key="role.value"
          class="q-mx-sm btn"
          @click="selecteRole(role)"
          :class="{ 'text-white bg-primary': roleGroup.value === role.value }"
        >
          {{ role.label }}
        </button>
      </div>
      <!-- 时间范围 -->
      <div class="row no-wrap">
        <f-formitem
          label="选择时间"
          label-style="width:56px;margin-right:28px"
          value-style="width:150px"
          class="q-mr-sm"
        >
          <f-date
            :options="startTimeOptions"
            v-model="time.start_date"
            @input="barChartDataInit($event)"
            :rules="[val => !!val || '请选择开始日期']"
          />
        </f-formitem>
        <f-formitem
          label="至"
          label-style="width:14px;margin-right:12px"
          value-style="width:150px"
        >
          <f-date
            :options="endTimeOptions"
            v-model="time.end_date"
            @input="barChartDataInit($event)"
            :rules="[val => !!val || '请选择结束日期']"
          />
        </f-formitem>
        <!--需求类型 -->
        <f-formitem label="需求类型" label-style="width:56px;margin-left:28px">
          <fdev-select
            ref="demandType"
            multiple
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :value="demandType"
            :options="demandOptions"
            @input="updateDemandType($event)"
            :rules="[val => !!val.length || '需求类型不能为空']"
          />
        </f-formitem>
        <!--任务类型 -->
        <f-formitem label="任务类型" label-style="width:56px;margin-left:28px">
          <fdev-select
            multiple
            map-options
            emit-value
            option-label="label"
            option-value="value"
            :value="taskType"
            option-disable="inactive"
            :options="taskOptions"
            @input="updateTaskType($event)"
          />
        </f-formitem>
      </div>
    </div>
    <!-- 展示数据 -->
    <div>
      <!-- 指定时间内阶段发生变化的任务数量统计 -->
      <Loading :visible="barChartLoading">
        <div class="row no-wrap q-mb-sm">
          <!-- 标题 -->
          <div class="title">
            <f-icon
              name="dashboard_s_f"
              class="text-primary"
              style="margin-right:12px"
            />
            指定时间内阶段发生变化的任务数量统计
            <f-icon name="help_c_o" class="cursor-pointer text-primary" />
            <fdev-tooltip
              >日常任务、开发任务、无代码任务都将被统计,日常任务(开发、完成）将会被统计入（启动开发、确认已投产）状态中</fdev-tooltip
            >
          </div>
          <fdev-space />
          <!-- 下载按钮 -->
          <fdev-btn
            ficon="download"
            label="下载"
            normal
            class="q-mr-md"
            @click="downloadTimeExcel"
            v-forbidMultipleClick
          />
          <!-- 表格/图表切换按钮 -->
          <fdev-btn-toggle
            :value="showSwitch"
            @input="updateShowSwitch($event)"
            :options="[
              { label: '图表', value: 'chartShow' },
              { label: '表格', value: 'formShow' }
            ]"
          />
        </div>
        <!--图表/表格 -->
        <BarChart
          id="bar"
          width="100%"
          :chart-data="barData"
          @click="handleTaskListOpen"
          v-show="showSwitch === 'chartShow'"
          :showSwitch="showSwitch"
        />
        <div v-show="showSwitch === 'formShow'">
          <StackBarForm
            :fristLiName="lineName"
            :formData="barData"
            id="tableTime"
          />
        </div>
      </Loading>
    </div>
    <!-- 展示特定阶段任务列表弹窗 -->
    <TaskListDialog
      v-model="taskListOpen"
      :showLoading="showLoading"
      :taskList="taskList"
      :stageName="stageName"
      :groupName="groupName"
    />
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import BarChart from '@/modules/Measure/components/Chart/BarChart';
import TaskListDialog from '@/modules/Measure/components/TaskListDialog';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import moment from 'moment';
import { appendNode } from '@/utils/utils';
import { demandOptions, taskOptions } from '@/modules/Measure/utils/constants';
import { required } from 'vuelidate/lib/validators';
import StackBarForm from '@/modules/Measure/components/Form/StackBarForm';
export default {
  name: 'taskPhaseChange',
  data() {
    return {
      analysisData: [],
      groupTreeData: [], //树
      showLoading: false,
      barData: {},
      taskListOpen: false,
      barChartLoading: false,
      ids: [],
      time: {
        start_date: moment(new Date() - 24 * 60 * 60 * 30000).format(
          'YYYY-MM-DD'
        ),
        end_date: moment(new Date()).format('YYYY-MM-DD')
      }, //默认时间段是从今天往前推30天
      lineName: '所属小组',
      stageName: null, //阶段名字
      groupName: null, //所属板块名
      legendName: {
        create: '创建',
        dev: '启动开发',
        sit: '进入SIT',
        uat: '进入UAT',
        rel: '进入REL',
        pro: '确认已投产'
      },
      roleType: [
        {
          label: '总人数',
          value: ''
        },
        { label: '开发人员', value: 'developer' },
        { label: '测试人员', value: 'tester' },
        { label: '厂商项目负责人', value: 'master' },
        { label: '行内项目负责人', value: 'spdbMaster' }
      ],
      demandOptions: demandOptions(),
      taskOptions: taskOptions()
    };
  },
  validations: {
    time: {
      end_date: {
        required
      },
      start_date: { required }
    },
    demandType: {
      required
    }
  },
  watch: {
    demandType(val) {
      if (val.length == 1 && val.includes('daily')) {
        this.updateTaskType([2]); //选中日常任务
        this.taskOptions[0].inactive = true;
        this.taskOptions[1].inactive = true;
        this.taskOptions[2].inactive = false;
      } else if (!val.length) {
        this.updateTaskType([]);
        this.taskOptions.map(val => (val.inactive = true));
      } else {
        this.taskOptions.map(val => (val.inactive = false));
      }
    }
  },
  components: {
    Loading,
    BarChart,
    TaskListDialog,
    StackBarForm
  },
  computed: {
    ...mapState('userActionSaveMeasure/taskPhaseChange', [
      'selectedGroups',
      'percapita',
      'isIncludeChildren',
      'roleGroup',
      'showSwitch',
      'groupsTree',
      'taskType',
      'demandType'
    ]),
    ...mapState('userForm', {
      groups: 'groups'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('measureForm', ['taskList', 'stageTaskNum']),
    ...mapGetters('userActionSaveMeasure/taskPhaseChange', ['groupData']),
    nodes() {
      const root = this.groups.filter(group => !group.parent);
      const groupList = this.appendNode(
        root,
        this.groups.filter(group => group.id && group.parent)
      );
      return this.addAttribute(groupList);
    }
  },
  methods: {
    ...mapMutations('userActionSaveMeasure/taskPhaseChange', [
      'updateSelectedGroups',
      'updatePercapita',
      'updateRoleGroup',
      'updateIsIncludeChildren',
      'updateShowSwitch',
      'updateGroupsTree',
      'updateDemandType',
      'updateTaskType'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('measureForm', [
      'queryTaskSimpleByIds',
      'queryTaskPhaseChangeStatistics'
    ]),

    //选择组
    selectGroup(list) {
      this.updateSelectedGroups(list);
    },
    //点击groupSelectTree中的组触发的callback
    clickGroup(tree) {
      this.groupTreeData = tree;
      sessionStorage.setItem('taskPhaseChangeTree', JSON.stringify(tree));
    },
    //重置整棵树
    resetTree(tree) {
      this.updateSelectedGroups([this.currentUser.group_id]);
      this.updateGroupsTree([]);
      this.groupTreeData = tree;
    },
    addAttribute(data) {
      if (!Array.isArray(data)) {
        return data;
      }
      return data.map(item => {
        return {
          ...item,
          expand: false,
          selected: item.id === '5c81c4d0d3e2a1126ce30049' ? true : false,
          children: this.addAttribute(item.children)
        };
      });
    },
    appendNode(parent, set) {
      return appendNode(parent, set);
    },
    /* 点击选择角色 */
    selecteRole(role) {
      this.updateRoleGroup(role);
      // 更新图表数据
      this.barChartDataInit();
    },
    async init(ids) {
      ids = Array.from(new Set(ids));
      this.analysisData = this.groupData.slice(0);
      this.analysisData.filter(item => {
        ids.filter(id => {
          if (id === item.id) {
            this.$set(item, 'selected', true);
          }
        });
      });
      this.ids = ids;
      // 获取图表数据
      this.barChartDataInit();
    },
    // 点击查询某一阶段任务详情
    async handleTaskListOpen({ group, seriesName, seriesId }) {
      this.taskListOpen = true;
      this.showLoading = true;
      // 获取对应阶段的任务ids
      const currentGroup = this.stageTaskNum.find(item => {
        return item.name === group;
      });
      const ids = currentGroup[seriesId];
      // 获取当前阶段名
      this.stageName = seriesName;
      // 获取当前组名
      this.groupName = group;
      // 查询当前阶段任务列表
      await this.queryTaskSimpleByIds({ ids: ids });
      this.showLoading = false;
    },
    // 获取图表数据
    async barChartDataInit() {
      if (this.$v.time.$invalid || this.$v.demandType.$invalid) {
        this.barChartLoading = false;
        return;
      }
      // 如果没有选组前端弹窗提示
      if (!this.ids.length) {
        this.$q
          .dialog({
            title: `温馨提示`,
            message: `您还未选择所属小组，请至少选择一个组后再查询！`,
            ok: '我知道了'
          })
          .onOk(async () => {
            this.barChartLoading = false;
          });
        return;
      }
      this.barChartLoading = true;
      let params = {
        startDate: this.time.start_date,
        endDate: this.time.end_date,
        groupIds: this.ids,
        includeChild: this.isIncludeChildren,
        demandType: this.demandType,
        taskType: this.taskType
      };
      if (this.percapita) {
        params.taskPersonTypeForAvg = this.roleGroup.value
          ? [this.roleGroup.value]
          : ['developer', 'tester', 'master', 'spdbMaster'];
      }
      await this.queryTaskPhaseChangeStatistics(params);
      this.barData = this.initBarChartData(this.stageTaskNum, 'bar');
      this.barChartLoading = false;
    },
    // 图表数据处理
    initBarChartData(chartData, type, stack) {
      const keyList = this.percapita
        ? ['createAvg', 'devAvg', 'sitAvg', 'uatAvg', 'relAvg', 'proAvg']
        : [
            'createCount',
            'devCount',
            'sitCount',
            'uatCount',
            'relCount',
            'proCount'
          ];
      //横坐标坐标轴label;
      const xAxis = chartData.map(item => {
        return item.name;
      });
      // 图例
      const legend = keyList.map(item => {
        return this.legendName[
          item.substring(0, this.percapita ? item.length - 3 : item.length - 5)
        ];
      });
      // 数据
      let series = keyList.map((key, index) => {
        let data = chartData.map(item => {
          return item[key];
        });
        return {
          name: this.legendName[
            key.substring(0, this.percapita ? key.length - 3 : key.length - 5)
          ],
          id:
            key == 'createCount' || key == 'createAvg'
              ? key.substring(0, 6) + 'Ids'
              : key.substring(0, 3) + 'Ids',
          type: 'bar',
          stack: stack ? '总量' : null,
          data: data,
          label: {
            normal: {
              show: true
            }
          }
        };
      });
      return { series, xAxis, legend };
    },
    // 开始时间范围控制
    startTimeOptions(date) {
      if (this.time.end_date) {
        return date <= this.time.end_date.replace(/-/g, '/');
      }
      return true;
    },
    // 结束时间范围控制
    endTimeOptions(date) {
      this.time.start_date = this.time.start_date ? this.time.start_date : '';
      return date >= this.time.start_date.replace(/-/g, '/');
    },
    // 下载
    downloadTimeExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'tableTime',
          '指定时间内阶段发生变化的任务数量统计.xlsx'
        );
      });
    },
    /* 人均数按钮开关 */
    percapitaOpened(event) {
      if ([true, false].includes(event)) {
        this.updatePercapita(event);
      }
      // 更新图表数据
      this.barChartDataInit();
    },
    //切换tab保存组树数据
    saveData(to, from, next) {
      let item = JSON.parse(sessionStorage.getItem('taskPhaseChangeTree'));
      if (item) {
        this.updateGroupsTree(item);
      }
    }
  },
  async created() {
    // 获取小组信息
    await this.fetchGroup();
    this.init(
      this.selectedGroups.length
        ? this.selectedGroups
        : [this.currentUser.group_id]
    );
  }
};
</script>

<style lang="stylus" scoped>
.toBottom
   margin-bottom:10px
.title
  font-family: PingFangSC-Semibold;
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight:600
.btn
  border:none
  background:#F4F7FF
</style>
