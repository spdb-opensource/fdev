<template>
  <f-block>
    <div class="group-fixed q-pb-md q-pt-sm bg-white">
      <GroupSelectTree
        v-if="analysisData && analysisData.length > 0"
        :dataSource="analysisData"
        @selectGroup="selectGroup"
        @clickGroup="clickGroup"
        @resetTree="resetTree"
      />
      <div class="q-my-md row items-center">
        <fdev-toggle
          :value="isIncludeChildren"
          @input="updateIsIncludeChildren($event)"
          label="包含子组"
          left-label
        />
        <fdev-toggle
          :value="percapita"
          label="人均数"
          @input="percapitaOpened($event)"
          left-label
        />
        <button
          v-show="percapita"
          v-for="role in options"
          :key="role"
          class="q-mx-sm btn"
          @click="selecteRole(role)"
          :class="{ 'text-white bg-primary': roleGroup === role }"
        >
          {{ role }}
        </button>
      </div>
      <div class="justify-center no-wrap q-mt-md row line-one">
        <fdev-btn
          label="查询"
          dialog
          @click="init(selectedGroups)"
          ficon="search"
        />
        <fdev-btn-toggle
          :value="showSwitch"
          @input="updateShowSwitch($event)"
          class="f-right"
          :btnWidth="70"
          :options="[
            { label: '图表', value: 'chartShow' },
            { label: '表格', value: 'formShow' }
          ]"
        />
      </div>
    </div>
    <div
      class="justify-between no-wrap q-mt-md row"
      v-show="showSwitch === 'chartShow'"
    >
      <Loading class="column" :visible="appLineChartLoading">
        <div class="row q-px-lg q-mb-md items-center">
          <span class="text-h6 q-mr-md title">应用统计</span>
          <fdev-btn
            ficon="download"
            label="下载"
            flat
            @click="downloadAppExcel"
          />
        </div>
        <LineChart width="38vw" :chart-data="appNum" :chart-top="chartTop" />
      </Loading>
      <Loading class="column" :visible="taskLineChartLoading">
        <div class="row q-px-lg q-mb-md items-center">
          <span class="text-h6 title q-mr-md">任务统计</span>
          <fdev-btn
            ficon="download"
            label="下载"
            flat
            @click="downloadTaskExcel"
          />
        </div>
        <LineChart
          width="38vw"
          :chart-data="taskNum"
          id="task"
          :chart-top="chartTop"
        />
      </Loading>
    </div>
    <div
      class="contain-wrapper bg-white q-mt-md"
      style="max-height:500px; overflow:auto"
      v-show="showSwitch === 'formShow'"
    >
      <Loading :visible="appLineChartLoading">
        <p class="inline-block title q-ml-sm">应用统计</p>
        <fdev-btn
          class="float-right"
          ficon="download"
          label="下载"
          normal
          @click="downloadAppExcel"
        ></fdev-btn>
        <LineForm :formData="appNum" id="tableApp" />
      </Loading>

      <div class="contain-wrapper bg-white q-mt-md">
        <Loading :visible="taskLineChartLoading">
          <p class="inline-block title q-ml-sm">任务统计</p>
          <fdev-btn
            class="float-right"
            ficon="download"
            label="下载"
            normal
            @click="downloadTaskExcel"
          ></fdev-btn>
          <LineForm :formData="taskNum" id="tableTask" />
        </Loading>
      </div>
    </div>

    <div class="contain-wrapper bg-white q-mt-md">
      <Loading :visible="stackBarChartLoading">
        <p class="inline-block title q-ml-sm">
          各阶段任务数量统计
        </p>
        <fdev-btn
          class="float-right"
          ficon="download"
          label="下载"
          normal
          @click="downloadExcel"
          :loading="globalLoading['dashboard/exportExcel']"
        ></fdev-btn>
        <StackBarChart
          id="stackBar"
          width="76vw"
          :height="barChartHeight"
          :chart-data="stackBarData"
          @click="handleTaskListOpen"
          v-show="showSwitch === 'chartShow'"
        />
        <StackBarForm
          :fristLiName="lineName"
          :formData="stackBarData"
          v-show="showSwitch === 'formShow'"
        />
      </Loading>
    </div>

    <div class="contain-wrapper bg-white q-mt-md">
      <Loading :visible="barChartLoading">
        <div class="row q-pa-sm table-wrapper">
          <f-formitem page label="开始日期">
            <f-date
              :options="startTimeOptions"
              v-model="time.start_date"
              @input="barChartDataInit($event)"
            />
          </f-formitem>
          <f-formitem page label="结束日期">
            <f-date
              :options="endTimeOptions"
              v-model="time.end_date"
              @input="barChartDataInit($event)"
            />
          </f-formitem>
          <fdev-btn
            class="btn-position"
            ficon="download"
            label="下载"
            normal
            @click="downloadTimeExcel"
          ></fdev-btn>
        </div>
        <BarChart
          id="bar"
          class="bg-white"
          width="78vw"
          :chart-data="barData"
          title="指定时间内阶段发生变化的任务数量统计"
          @click="handleTaskListOpen"
          v-show="showSwitch === 'chartShow'"
        />
        <div v-show="showSwitch === 'formShow'">
          <p class="inline-block title q-ml-sm">
            指定时间内阶段发生变化的任务数量统计
          </p>
          <StackBarForm
            :fristLiName="lineName"
            :formData="barData"
            id="tableTime"
          />
        </div>
      </Loading>
    </div>

    <Loading class="contain-wrapper row q-mt-md" :visible="dangbanLoading">
      <div class="col bg-white">
        <p class="inline-block title q-ml-sm">各组挡板使用人数占比{{ date }}</p>
        <fdev-btn
          class="float-right"
          ficon="download"
          label="下载"
          normal
          @click="downloadUserNumExcel"
        ></fdev-btn>
        <StackBarChart
          id="percent"
          width="38.5vw"
          :chart-top="chartTop"
          :chart-data="iamsGroupChartAll"
          :height="barChartHeight"
          v-show="showSwitch === 'chartShow'"
        />
        <IamsGroupForm
          id="userNum"
          :formHeader="formUserHeader"
          :iamsFormDate="iamsGroupForm"
          v-show="showSwitch === 'formShow'"
        />
      </div>

      <div class="col q-ml-sm bg-white">
        <p class="inline-block title q-ml-sm">各小组挡板人均交易量{{ date }}</p>
        <fdev-btn
          class="float-right"
          ficon="download"
          label="下载"
          normal
          @click="downloadUserAverageExcel"
        ></fdev-btn>
        <StackBarChart
          id="average"
          width="38.5vw"
          :chart-top="chartTop"
          :chart-data="iamsGroupChartAverage"
          :height="barChartHeight"
          v-show="showSwitch === 'chartShow'"
        />
        <IamsGroupForm
          id="userAverage"
          :formHeader="formAverageHeader"
          :iamsFormDate="iamsGroupForm"
          v-show="showSwitch === 'formShow'"
        />
      </div>
    </Loading>

    <TaskListDialog v-model="taskListOpen" :showLoading="showLoading" />
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import LineChart from '@/modules/Dashboard/components/Chart/LineChart';
import BarChart from '@/modules/Dashboard/components/Chart/BarChart';
import StackBarChart from '@/modules/Dashboard/components/Chart/StackBarChart';
import TaskListDialog from '@/modules/Dashboard/components/Chart/TaskListDialog';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import { name, nameKey, nameGroup } from '@/modules/Dashboard/utils/constants';
import { exportExcel } from '@/utils/utils';
import moment from 'moment';
import LineForm from '@/modules/Dashboard/components/form/LineForm';
import StackBarForm from '@/modules/Dashboard/components/form/StackBarForm';
import IamsGroupForm from '@/modules/Dashboard/components/form/IamsGroupForm';
import GroupSelectTree from '@/components/UI/GroupSelectTree';
export default {
  name: 'Analysis',
  data() {
    return {
      analysisData: [],
      groupTreeData: [], //树
      showLoading: false,
      loading: false,
      groupsId: [],
      tab: 'appNum',
      appNum: {},
      chartTop: '2%',
      taskNum: {},
      barData: {},
      stackBarData: {},
      taskListOpen: false,
      appLineChartLoading: false,
      taskLineChartLoading: false,
      stackBarChartLoading: false,
      barChartLoading: false,
      ids: [],
      time: {
        start_date: moment(new Date() - 24 * 60 * 60 * 30000).format(
          'YYYY/MM/DD'
        ),
        end_date: moment(new Date()).format('YYYY/MM/DD')
      },
      roleNumOfSeleted: {},
      options: [
        '总人数',
        '开发人员',
        '测试人员',
        '行内项目负责人',
        '厂商项目负责人'
      ],
      dangbanChart: {},
      averageChart: {},
      date: '',
      dangbanLoading: false,
      formUserHeader: ['总人数', '使用人数', '占比'],
      formAverageHeader: ['总人数', '总交易量', '人均交易量'],
      lineName: '所属小组'
    };
  },
  components: {
    GroupSelectTree,
    LineChart,
    Loading,
    StackBarChart,
    BarChart,
    TaskListDialog,
    LineForm,
    StackBarForm,
    IamsGroupForm
  },
  computed: {
    ...mapState('userActionSaveDashboard/analysis', [
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
    ...mapState('dashboard', {
      response: 'response',
      appNumResponse: 'appNum',
      taskNumResponse: 'taskNum',
      taskNumByGroup: 'taskNumByGroup',
      taskNumByGroupDate: 'taskNumByGroupDate',
      iamsGroupChart: 'iamsGroupChart',
      iamsGroupForm: 'iamsGroupForm'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapGetters('dashboard', ['iamsGroupChartAll', 'iamsGroupChartAverage']),
    ...mapGetters('userActionSaveDashboard/analysis', ['groupData']),
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
    ...mapMutations('userActionSaveDashboard/analysis', [
      'updateSelectedGroups',
      'updatePercapita',
      'updateRoleGroup',
      'updateIsIncludeChildren',
      'updateShowSwitch',
      'updateGroupsTree'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('dashboard', [
      'queryTasksByIds',
      'exportExcel',
      'queryAppNum',
      'queryTaskNum',
      'queryTaskNumByGroup',
      'queryTaskNumByGroupDate',
      'queryIamsGroupChart'
    ]),
    ...mapActions('user', ['fetch']),
    //选择组
    selectGroup(list) {
      this.updateSelectedGroups(list);
    },
    //点击groupSelectTree中的组触发的callback
    clickGroup(tree) {
      this.groupTreeData = tree;
      sessionStorage.setItem('analysisTree', JSON.stringify(tree));
    },
    // 获取组名
    getGroupName(groupId, haveCount) {
      const group = this.groups.find(item => {
        return item.id === groupId;
      });
      if (!group) {
        return;
      }
      const { name, count } = group;
      return haveCount ? count : name;
    },
    // 获取图标数据
    initLineChartData(chartData, type = 'line') {
      const groupId = Object.keys(chartData);

      const legend = groupId.map(id => {
        const count =
          this.roleNumOfSeleted[id] && this.roleNumOfSeleted[id][this.roleGroup]
            ? this.roleNumOfSeleted[id][this.roleGroup]
            : 0;
        if (this.percapita) {
          return `${this.getGroupName(id)}（${count}）`;
        }
        return this.getGroupName(id);
      });

      let xAxis = [];
      const series = groupId.map(item => {
        xAxis = Object.keys(chartData[item]).sort();
        const count =
          this.roleNumOfSeleted[item] &&
          this.roleNumOfSeleted[item][this.roleGroup]
            ? this.roleNumOfSeleted[item][this.roleGroup]
            : 0;
        const data = xAxis.map(val => {
          if (this.percapita) {
            return count === 0
              ? chartData[item][val].toFixed(2)
              : (chartData[item][val] / count).toFixed(2);
          }
          return chartData[item][val];
        });

        const name = this.percapita
          ? `${this.getGroupName(item)}(${count})`
          : this.getGroupName(item);
        return {
          name: name,
          type: type,
          data: data,
          itemStyle: {
            normal: {
              lineStyle: {
                width: 2
              }
            }
          },
          animationDuration: 2800,
          animationEasing: 'quadraticOut',
          smooth: false
        };
      });
      const params = { xAxis, series, legend };
      return params;
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
      this.appLineChartLoading = true;
      this.taskLineChartLoading = true;
      this.stackBarChartLoading = true;
      this.barChartLoading = true;
      this.queryAppNum({
        ids: ids,
        isIncludeChildren: this.isIncludeChildren
      }).then(res => {
        this.appNum = this.initLineChartData(this.appNumResponse, 'line');
        this.appLineChartLoading = false;
      });
      this.queryTaskNum({
        ids: ids,
        isIncludeChildren: this.isIncludeChildren
      }).then(res => {
        this.taskNum = this.initLineChartData(this.taskNumResponse, 'line');
        this.taskLineChartLoading = false;
      });
      this.queryTaskNumByGroup({
        ids: ids,
        isIncludeChildren: this.isIncludeChildren
      }).then(res => {
        this.stackBarData = this.initBarChartData(
          this.taskNumByGroup,
          'bar',
          'stack'
        );
        this.stackBarChartLoading = false;
      });
      this.barChartDataInit();
      this.dangbanInit(ids);
    },
    appendNode(parent, set, depth = 2) {
      if (!Array.isArray(parent) || !Array.isArray(set)) {
        return [];
      }
      if (parent.length === 0 || set.length === 0) {
        return [];
      }
      if (depth === 0) {
        return parent;
      }
      const child = parent.reduce((pre, next) => {
        const nodes = set.filter(group => group.parent === next.id);
        nodes.forEach(node => (node.header = 'nodes'));

        next.children = nodes;
        return pre.concat(nodes);
      }, []);

      if (child.length > 0) {
        this.appendNode(child, set, --depth);
      }
      return parent;
    },
    initBarChartData(chartData, type, stack) {
      const groupsId = Object.keys(chartData);
      const chartName = stack ? nameGroup : name;
      const xAxis = groupsId.map(id => {
        const count =
          this.roleNumOfSeleted[id] && this.roleNumOfSeleted[id][this.roleGroup]
            ? this.roleNumOfSeleted[id][this.roleGroup]
            : 0;
        if (this.percapita) {
          return `${this.getGroupName(id)}（${count}）`;
        }
        return this.getGroupName(id);
      });
      const keyList = stack
        ? ['todo', 'develop', 'sit', 'uat', 'rel', 'production']
        : ['create', 'develop', 'sit', 'uat', 'rel', 'production'];
      const legend = keyList.map(item => {
        return chartName[item];
      });
      let series = keyList.map((key, index) => {
        let data = groupsId.map(group => {
          let result = chartData[group][key];
          if (this.percapita) {
            const count =
              this.roleNumOfSeleted[group] &&
              this.roleNumOfSeleted[group][this.roleGroup]
                ? this.roleNumOfSeleted[group][this.roleGroup]
                : 0;
            return count === 0
              ? result.toFixed(2)
              : (result / count).toFixed(2);
          }
          return result;
        });
        return {
          name: chartName[key],
          id: nameKey[key],
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
    async handleTaskListOpen({ group, seriesId, name }) {
      this.taskListOpen = true;
      this.showLoading = true;
      const groupId = this.groups.find(item => {
        return item.name === group;
      }).id;
      const ids = this[name][groupId][seriesId];
      await this.queryTasksByIds({ ids: ids });
      this.showLoading = false;
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
    //重置整棵树
    resetTree(tree) {
      this.updateSelectedGroups(['5c81c4d0d3e2a1126ce30049']);
      this.updateGroupsTree([]);
      this.groupTreeData = tree;
    },
    barChartDataInit() {
      let end_date = this.time.end_date.replace(/-/g, '/');
      let start_date = this.time.start_date.replace(/-/g, '/');
      this.queryTaskNumByGroupDate({
        end_date,
        start_date,
        ids: this.ids,
        isIncludeChildren: this.isIncludeChildren
      }).then(res => {
        this.barData = this.initBarChartData(this.taskNumByGroupDate, 'bar');
        this.barChartLoading = false;
      });
      try {
        this.$refs.qDateProxyStart.hide();
        this.$refs.qDateProxy.hide();
      } catch (e) {
        //
      }
    },
    startTimeOptions(date) {
      if (this.time.end_date) {
        return date < this.time.end_date.replace(/-/g, '/');
      }
      return true;
    },
    endTimeOptions(date) {
      this.time.start_date = this.time.start_date ? this.time.start_date : '';
      return date > this.time.start_date.replace(/-/g, '/');
    },
    /* 导出应用统计 */
    downloadAppExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('tableApp', '应用统计.xlsx');
      });
    },
    downloadTaskExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('tableTask', '任务统计.xlsx');
      });
    },
    downloadTimeExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'tableTime',
          '指定时间内阶段发生变化的任务数量统计.xlsx'
        );
      });
    },
    downloadUserNumExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'userNum',
          '各组挡板使用人数占比' + this.date + '.xlsx'
        );
      });
    },
    downloadUserAverageExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'userAverage',
          '各小组挡板人均交易量' + this.date + '.xlsx'
        );
      });
    },
    /* 导出各阶段任务数量统计 */
    /* 导出文件 */
    async downloadExcel() {
      await this.exportExcel({
        ids: this.ids,
        isIncludeChildren: this.isIncludeChildren
      });
      const responseFilename = this.response.headers['content-disposition'];
      const filename = `各阶段任务数量统计${responseFilename.substring(
        responseFilename.indexOf('=') + 1
      )}`;
      exportExcel(this.response, filename);
    },
    /* 人均数按钮开关 */
    percapitaOpened(event) {
      if ([true, false].includes(event)) {
        this.updatePercapita(event);
      }
      this.stackBarData = this.initBarChartData(
        this.taskNumByGroup,
        'bar',
        'stack'
      );
      this.barData = this.initBarChartData(this.taskNumByGroupDate, 'bar');
      this.appNum = this.initLineChartData(this.appNumResponse, 'line');
      this.taskNum = this.initLineChartData(this.taskNumResponse, 'line');
    },
    /* 查询所有用户，按小组归类，统计拥有的角色人数 */
    async getRoleOfGroup() {
      await this.fetch();
      const roleObj = {};

      this.userList.forEach(user => {
        if (!roleObj[user.group_id]) {
          roleObj[user.group_id] = {
            总人数: 1,
            name: user.group.name
          };
        } else {
          roleObj[user.group_id]['总人数'] =
            roleObj[user.group_id]['总人数'] + 1;
        }

        user.role.forEach(role => {
          if (!roleObj[user.group_id][role.name]) {
            roleObj[user.group_id][role.name] = 1;
          } else {
            roleObj[user.group_id][role.name] =
              roleObj[user.group_id][role.name] + 1;
          }
        });
      });
      this.roleNumOfSeleted = roleObj;
    },
    /* 点击选择角色 */
    selecteRole(role) {
      this.updateRoleGroup(role);
      this.percapitaOpened();
    },
    async dangbanInit(ids) {
      this.date = moment(new Date())
        .subtract(1, 'month')
        .format('YYYY-MM');
      this.dangbanLoading = true;
      await this.queryIamsGroupChart({
        date: this.date,
        isIncludeChildren: this.isIncludeChildren,
        ids: ids
      });
      this.dangbanLoading = false;
    }
  },
  async created() {
    this.getRoleOfGroup();
    await this.fetchGroup();
    // this.init(['5c81c4d0d3e2a1126ce30049']);
    this.init(this.selectedGroups);
  },
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('analysisTree');
    }
    next();
  },
  //离开页面之前保存组树数据
  beforeRouteLeave(to, from, next) {
    let item = JSON.parse(sessionStorage.getItem('analysisTree'));
    if (item) {
      this.updateGroupsTree(item);
    }
    next();
  }
};
</script>

<style lang="stylus" scoped>

.group-fixed
  margin-top -8px
  position sticky
  top 60px
  z-index 2
  border-bottom solid 1px #BBBBBB
.row
  span
    line-height 40px
.title
  height 40px
  line-height 40px
  color #008acd
  font-size 18px
.table-wrapper
  position relative
  .btn-position
    position absolute
    right 5px
.line-one
  position relative
  .f-right
    position absolute;
    right 5px;
    top 2px;
</style>
