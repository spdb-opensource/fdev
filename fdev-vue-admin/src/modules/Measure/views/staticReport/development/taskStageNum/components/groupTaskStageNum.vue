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
          <!-- 是否包含子组 -->
          <fdev-toggle
            :value="isIncludeChildren"
            @input="updateIsIncludeChildren($event)"
            label="包含子组"
            left-label
            size="lg"
          />
        </template>
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
    </div>
    <!-- 数据部分-->
    <div>
      <!-- 各阶段数量统计 -->
      <Loading :visible="stackBarChartLoading">
        <div class="row no-wrap">
          <div class="title">
            <f-icon
              name="dashboard_s_f"
              class="text-primary"
              style="margin-right:12px"
            />
            各团队不同阶段任务数量统计
            <f-icon name="help_c_o" class="cursor-pointer text-primary" />
            <fdev-tooltip
              >日常任务、开发任务、无代码任务都将被统计,日常任务(开发、完成）将会被统计入（开发中、已投产）状态中</fdev-tooltip
            >
          </div>
          <fdev-space />
          <fdev-btn
            class="q-mr-md"
            ficon="download"
            label="下载"
            normal
            @click="downloadExcel"
            v-forbidMultipleClick
          />
          <!-- 表格图表切换按钮 -->
          <fdev-btn-toggle
            :value="showSwitch"
            @input="updateShowSwitch($event)"
            :options="[
              { label: '图表', value: 'chartShow' },
              { label: '表格', value: 'formShow' }
            ]"
          />
        </div>
        <div class="q-mt-md">
          <StackBarChart
            id="stackBar"
            width="100%"
            :height="barChartHeight"
            :chart-data="stackBarData"
            @click="handleTaskListOpen"
            :showSwitch="showSwitch"
            v-show="showSwitch === 'chartShow'"
          />
          <StackBarForm
            :fristLiName="lineName"
            :formData="stackBarData"
            v-show="showSwitch === 'formShow'"
            id="taskStageTable"
          />
        </div>
      </Loading>
      <!-- 各阶段任务列表弹窗 -->
      <TaskListDialog
        v-model="taskListOpen"
        :showLoading="showLoading"
        :taskList="taskList"
        :stageName="stageName"
        :groupName="groupName"
      />
    </div>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import StackBarChart from '@/modules/Measure/components/Chart/StackBarChart';
import TaskListDialog from '@/modules/Measure/components/TaskListDialog';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import { appendNode } from '@/utils/utils';
import StackBarForm from '@/modules/Measure/components/Form/StackBarForm';
export default {
  name: 'groupTaskStageNum',
  data() {
    return {
      analysisData: [],
      groupTreeData: [], //树
      showLoading: false,
      loading: false,
      chartTop: '2%',
      stackBarData: {},
      taskListOpen: false,
      stackBarChartLoading: false,
      stageName: null, //阶段名字
      groupName: null, //所属板块名
      ids: [],
      lineName: '所属小组',
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
      legendName: {
        create: '待实施',
        dev: '开发中',
        sit: 'SIT',
        uat: 'UAT',
        rel: 'REL',
        pro: '已投产'
      }
    };
  },
  components: {
    Loading,
    StackBarChart,
    TaskListDialog,
    StackBarForm
  },
  computed: {
    ...mapState('userActionSaveMeasure/groupTakStageNum', [
      'selectedGroups',
      'percapita',
      'isIncludeChildren',
      'roleGroup',
      'showSwitch',
      'groupsTree'
    ]),
    ...mapState('userForm', {
      groups: 'groups'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('measureForm', {
      taskList: 'taskList',
      response: 'response',
      taskNumByGroup: 'taskNumByGroup'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('userActionSaveMeasure/groupTakStageNum', ['groupData']),
    nodes() {
      const root = this.groups.filter(group => !group.parent);
      const groupList = this.appendNode(
        root,
        this.groups.filter(group => group.id && group.parent)
      );
      return this.addAttribute(groupList);
    },
    barChartHeight() {
      return String(350 + this.selectedGroups.length * 10) + 'px';
    }
  },
  methods: {
    ...mapMutations('userActionSaveMeasure/groupTakStageNum', [
      'updateSelectedGroups',
      'updatePercapita',
      'updateRoleGroup',
      'updateIsIncludeChildren',
      'updateShowSwitch',
      'updateGroupsTree'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('measureForm', [
      'queryTaskSimpleByIds',
      'queryTaskPhaseStatistics'
    ]),
    //选择组
    selectGroup(list) {
      this.updateSelectedGroups(list);
    },
    //点击groupSelectTree中的组触发的callback
    clickGroup(tree) {
      this.groupTreeData = tree;
      sessionStorage.setItem('groupTaskStageNum', JSON.stringify(tree));
    },
    //重置整棵树
    resetTree(tree) {
      this.updateSelectedGroups([this.currentUser.group_id]);
      this.updateGroupsTree([]);
      this.groupTreeData = tree;
    },
    appendNode(parent, set) {
      return appendNode(parent, set);
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
    // 获取图表数据
    async barChartDataInit() {
      this.stackBarChartLoading = true;
      // 如果没有选组前端弹窗提示
      if (!this.ids.length) {
        this.$q
          .dialog({
            title: `温馨提示`,
            message: `您还未选择所属小组，请至少选择一个组后再查询！`,
            ok: '我知道了'
          })
          .onOk(async () => {
            this.stackBarChartLoading = false;
          });
        return;
      }
      try {
        let params = {
          groupIds: this.ids,
          includeChild: this.isIncludeChildren
        };
        if (this.percapita) {
          params.taskPersonTypeForAvg = this.roleGroup.value
            ? [this.roleGroup.value]
            : ['developer', 'tester', 'master', 'spdbMaster'];
        }
        await this.queryTaskPhaseStatistics(params);
        this.stackBarData = this.initBarChartData(
          this.taskNumByGroup,
          'bar',
          'stack'
        );
        this.stackBarChartLoading = false;
      } catch (e) {
        this.stackBarChartLoading = false;
      }
    },
    // 绘制图表数据
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
      // x轴
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
            key.substring(0, this.percapita ? key.length - 3 : key.length - 5) +
            'Ids',
          type: 'bar',
          stack: stack ? '总量' : null,
          data: data,
          label: {
            normal: {
              show: true,
              formatter: params => {
                if (params.value > 0) {
                  return params.value;
                } else {
                  return '';
                }
              }
            }
          }
        };
      });
      return { series, xAxis, legend };
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
    // 点击某一阶段进入阶段任务列表
    async handleTaskListOpen({ name, seriesName, seriesId }) {
      this.taskListOpen = true;
      this.showLoading = true;
      // 获取对应阶段的任务ids
      const currentGroup = this.taskNumByGroup.find(item => {
        return item.name === name;
      });
      const ids = currentGroup[seriesId];
      // 获取当前阶段名
      this.stageName = seriesName;
      // 获取当前组名
      this.groupName = name;
      await this.queryTaskSimpleByIds({ ids: ids });
      this.showLoading = false;
    },
    /* 导出各阶段任务数量统计 */
    async downloadExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'taskStageTable',
          '各团队不同阶段任务数量统计.xlsx'
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
    /* 点击选择角色 */
    selecteRole(role) {
      this.updateRoleGroup(role);
      this.percapitaOpened();
    },
    // 原进原出
    saveData() {
      let item = JSON.parse(sessionStorage.getItem('groupTaskStageNum'));
      if (item) {
        this.updateGroupsTree(item);
      }
    }
  },
  async created() {
    // 获取小组
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
.title
  font-family: PingFangSC-Semibold;
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight:600
.toBottom
   margin-bottom:10px
.btn
  border:none
  background:#F4F7FF
/deep/ .q-toggle__label.q-anchor--skip
   padding-right:16px
</style>
