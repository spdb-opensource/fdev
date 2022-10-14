<template>
  <f-block>
    <Loading :visible="stackBarChartLoading">
      <!-- 查询条件 -->
      <div>
        <!-- 请选择应用 -->
        <f-formitem label="请选择应用">
          <fdev-select
            use-input
            :options="appOptions"
            option-label="name_en"
            option-value="id"
            v-model="inputValue"
            @filter="filteredApps"
            @input="selectApplication"
            palceholder="请输入应用名"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name_en">
                    {{ scope.opt.name_en }}
                  </fdev-item-label>
                  <fdev-item-label caption :title="scope.opt.name_zh">
                    {{ scope.opt.name_zh }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <fdev-separator class="q-mt-md" />
        <!-- 已选应用 -->
        <div class="q-mt-md">
          <span class="text-grey-9 inline-block">已选应用：</span>
          <span
            class="text-primary q-ml-md float-right cursor-pointer"
            @click="clearAll"
            v-if="appSelected.length > 0"
          >
            全部清除
          </span>
          <div class="q-mt-xs">
            <button
              v-for="(item, index) in appSelected"
              :key="index"
              class="q-mr-md q-mb-sm btn"
              :class="{
                'text-white bg-primary': item.selected === true
              }"
              @click="selectApplication(item)"
            >
              {{ item.name_en }}
            </button>
          </div>
          <div class="text-center">
            <fdev-btn
              dialog
              label="查询"
              @click="initBarChartData"
              ficon="search"
              v-forbidMultipleClick
            />
          </div>
        </div>
      </div>
      <!-- 报表和表格数据展示 -->
      <div class="min-height q-mt-sm">
        <!-- 标题，下载按钮 -->
        <div class="row np-wrap">
          <div class="title">
            <f-icon
              name="dashboard_s_f"
              class="text-primary"
              style="margin-right:12px"
            />
            各应用不同阶段任务数量统计表
            <f-icon name="help_c_o" class="cursor-pointer text-primary" />
            <fdev-tooltip
              >日常任务、开发任务、无代码任务都将被统计,日常任务(开发、完成）将会被统计入（开发中、已投产）状态中</fdev-tooltip
            >
          </div>
          <fdev-space />
          <!-- 下载按钮 -->
          <fdev-btn
            class="text-end"
            ficon="download"
            label="下载"
            normal
            @click="downloadAppExcel"
            v-if="chartData.xAxis"
            v-forbidMultipleClick
          />
          <!-- 图表表格切换 -->
          <fdev-btn-toggle
            class="q-ml-md"
            :value="showSwitch"
            @input="updateShowSwitch($event)"
            :options="[
              { label: '图表', value: 'chartShow' },
              { label: '表格', value: 'formShow' }
            ]"
          />
        </div>
        <!-- 图表表格 -->
        <div class="q-mt-lg">
          <!-- 有数据时 -->
          <div v-if="appSelected.length">
            <!-- 图表 -->
            <StackBarChart
              id="stackBar"
              :height="height"
              width="100%"
              :chart-top="chartTop"
              :chart-data="chartData"
              @click="handleTaskListOpen"
              :showSwitch="showSwitch"
              v-show="showSwitch === 'chartShow'"
            />
            <!-- 表格 -->
            <StackBarForm
              v-show="showSwitch === 'formShow'"
              :formData="chartData"
              :fristLiName="lineName"
              id="app"
              v-if="chartData.xAxis"
            />
          </div>
          <!-- 无数据时 -->
          <div v-else class="column items-center">
            <f-image name="no_data" class="q-mt-xl" />
          </div>
        </div>
      </div>
    </Loading>
    <!--某阶段任务列表弹窗 -->
    <TaskListDialog
      v-model="taskListOpen"
      :showLoading="showLoading"
      :taskList="taskList"
      :stageName="stageName"
    />
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import StackBarChart from '@/modules/Measure/components/Chart/StackBarChart';
import TaskListDialog from '@/modules/Measure/components/TaskListDialog';
import StackBarForm from '@/modules/Measure/components/Form/StackBarForm';
export default {
  name: 'ApplicationAnalysis',
  components: {
    TaskListDialog,
    Loading,
    StackBarChart,
    StackBarForm
  },
  data() {
    return {
      showLoading: false,
      chartData: {},
      stackBarChartLoading: false,
      inputValue: null,
      appOptions: [],
      taskListOpen: false,
      height: '350px',
      chartTop: '10%',
      lineName: '应用名',
      stageName: null, //阶段名字
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
  computed: {
    ...mapState('userActionSaveMeasure/applicationDemension', [
      'appSelected',
      'showSwitch',
      'appData'
    ]),
    ...mapState('measureForm', ['appChartData', 'taskList', 'applicationList'])
  },
  methods: {
    ...mapMutations('userActionSaveMeasure/applicationDemension', [
      'updateAppSelected',
      'updateShowSwitch',
      'updateAppData'
    ]),
    ...mapActions('measureForm', [
      'queryAppTaskPhaseStatistics',
      'queryTaskSimpleByIds',
      'queryForSelect'
    ]),
    /* 选择应用 */
    async selectApplication(app) {
      app.selected = !app.selected;
      let appSelected = this.appData.filter(item => item.selected === true);
      this.updateAppSelected(appSelected);
      this.inputValue = null;
    },
    // 已选应用清除
    clearAll() {
      this.appData.forEach(item => (item.selected = false));
      this.updateAppSelected([]);
    },
    /*下载表格*/
    downloadAppExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel('app', '各应用不同阶段任务数量统计表.xlsx');
      });
    },
    // 应用下拉框过滤查询
    filteredApps(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.appOptions = this.appData.filter(
          v =>
            v.name_zh.toLowerCase().indexOf(needle) > -1 ||
            v.name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    // 查看具体某阶段任务列表
    async handleTaskListOpen({ name, seriesId, seriesName }) {
      this.taskListOpen = true;
      this.showLoading = true;
      // 获取对应阶段的任务ids
      const currentGroup = this.appChartData.find(item => {
        return item.name === name;
      });
      const ids = currentGroup[seriesId];
      // 获取当前阶段名
      this.stageName = seriesName;
      // 查询当前阶段任务列表
      await this.queryTaskSimpleByIds({ ids: ids });
      this.showLoading = false;
    },
    /* 处理图表数据 */
    async initBarChartData() {
      // 图表高度
      this.height =
        this.appSelected.length < 10
          ? '350px'
          : `${350 + this.appSelected.length * 30}px`;
      //未选择应用时不处理数据
      if (this.appSelected.length === 0) {
        this.chartData = {};
        return;
      }
      this.stackBarChartLoading = true;

      await this.queryAppTaskPhaseStatistics({
        appIds: this.appSelected.map(item => item.id)
      });
      // 绘制图表
      this.chartData = this.initBarChart(this.appChartData);
      this.stackBarChartLoading = false;
    },
    // 图表绘制
    initBarChart(data) {
      // x轴
      const xAxis = data.map(item => {
        return item.name;
      });
      const seriesNames = ['create', 'dev', 'sit', 'uat', 'rel', 'pro'];
      // 图例
      const legend = seriesNames.map(item => {
        return this.legendName[item];
      });
      // 数据
      const series = seriesNames.map((key, index) => {
        return {
          name: this.legendName[key],
          id: key + 'Ids',
          type: 'bar',
          stack: 'stack',
          data: data.map(item => {
            return item[`${key}Count`];
          }),
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
    }
  },
  async created() {
    this.stackBarChartLoading = true;
    // 查询所有应用信息
    if (!this.appData.length) {
      await this.queryForSelect();
      // 获取应用列表并且每个应用默认为非选中状态
      const data = this.applicationList.map(item => {
        return { ...item, selected: false };
      });
      this.updateAppData(data);
    }
    // 图表处理
    this.initBarChartData();
    this.stackBarChartLoading = false;
  }
};
</script>

<style lang="stylus" scoped>
button
  border none
  outline none
  background none
  cursor pointer
.title
  font-family: PingFangSC-Semibold;
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight:600
.to-bottom
  margin-bottom:10px
  padding-bottom:28px
.min-height
  min-height:350px
</style>
