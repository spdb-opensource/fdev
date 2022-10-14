<template>
  <f-block>
    <Loading :visible="stackBarChartLoading">
      <!-- 搜索条件 -->
      <UserSelected @queryClick="init" />
      <!-- 数据部分 -->
      <div class="q-mt-sm">
        <!-- 时间范围、下载按钮 -->
        <div class="row no-wrap">
          <f-formitem
            label-style="width:56px;margin-right:28px"
            value-style="width:150px"
            class="q-mr-sm"
            label="开始时间"
          >
            <f-date
              :options="startOptions"
              v-model="listModel.start_date"
              :rules="[val => !!val || '请选择开始日期']"
            />
          </f-formitem>
          <f-formitem
            label="至"
            label-style="width:14px;margin-right:12px"
            value-style="width:150px"
          >
            <f-date
              :options="endOptions"
              v-model="listModel.end_date"
              :rules="[val => !!val || '请选择结束日期']"
            />
          </f-formitem>
          <!--需求类型 -->
          <f-formitem
            label="需求类型"
            label-style="width:56px;margin-left:28px"
          >
            <fdev-select
              multiple
              ref="demandType"
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
          <f-formitem
            label="任务类型"
            label-style="width:56px;margin-left:28px"
          >
            <fdev-select
              multiple
              map-options
              emit-value
              option-label="label"
              option-value="value"
              :value="taskType"
              :options="taskOptions"
              option-disable="inactive"
              @input="updateTaskType($event)"
            />
          </f-formitem>
        </div>
        <div class="row no-wrap q-mb-sm">
          <!-- 标题 -->
          <div class="title">
            <f-icon
              name="dashboard_s_f"
              class="text-primary"
              style="margin-right:12px"
            />
            指定时间段内各人员任务推进情况
          </div>
          <fdev-space />
          <fdev-btn
            ficon="download"
            class="q-mr-md"
            normal
            label="下载"
            @click="downloadstageTaskExcel"
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
        <!-- 有数据时 -->
        <div v-if="userSelected.length">
          <!-- 图表部分 -->
          <div v-show="showSwitch === 'chartShow'">
            <StackBarChart
              id="stackBar"
              :height="height"
              width="100%"
              :chart-data="chartData"
              :chart-top="chartTop"
              @click="handleTaskListOpen"
              :showSwitch="showSwitch"
              v-show="showSwitch === 'chartShow'"
            />
          </div>
          <!-- 表格部分 -->
          <div v-show="showSwitch === 'formShow'">
            <StackBarForm
              v-show="showSwitch === 'formShow'"
              :formData="chartData"
              :fristLiName="lineName"
              id="stageTask"
              v-if="chartData.xAxis"
            />
          </div>
        </div>
        <!-- 无数据时 -->
        <div v-else class="column items-center">
          <f-image name="no_data" class="q-mt-xl" />
        </div>
      </div>
      <!-- 任务列表弹窗 -->
      <TaskListDialog
        v-model="taskListOpen"
        :stageName="title"
        :taskList="taskList"
        :showLoading="showLoading"
      />
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import TaskListDialog from '@/modules/Measure/components/TaskListDialog';
import UserSelected from '@/modules/Measure/components/UserSelected';
import StackBarChart from '@/modules/Measure/components/Chart/StackBarChart';
import StackBarForm from '@/modules/Measure/components/Form/StackBarForm';
import { required } from 'vuelidate/lib/validators';
import { demandOptions, taskOptions } from '@/modules/Measure/utils/constants';
export default {
  components: {
    TaskListDialog,
    Loading,
    StackBarChart,
    StackBarForm,
    UserSelected
  },
  data() {
    return {
      showLoading: false,
      userSelected: [],
      chartData: {},
      formData: {
        xAxis: []
      },
      stackBarChartLoading: false,
      roles: [],
      userList: [],
      taskListOpen: false,
      title: null,
      height: '350px',
      leftGroup: [
        '创建',
        '开发中',
        '进入SIT',
        '进入UAT',
        '进入REL',
        '确认已投产'
      ],
      leftPosition: 'left',
      searchObj: {},
      legendName: {
        create: '创建',
        develop: '启动开发',
        sit: '进入SIT',
        uat: '进入UAT',
        rel: '进入REL',
        production: '确认已投产'
      },
      nameKey: {
        create: 'create_ids',
        develop: 'dev_ids',
        sit: 'sit_ids',
        uat: 'uat_ids',
        rel: 'rel_ids',
        production: 'pro_ids'
      },
      lineName: '人员',
      chartTop: '10%',
      taskOptions: taskOptions(),
      demandOptions: demandOptions()
    };
  },
  validations: {
    listModel: {
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
  computed: {
    ...mapState('measureForm', {
      taskList: 'taskList',
      usersList: 'userList',
      taskNumByUserIdsDate: 'taskNumByUserIdsDate'
    }),
    ...mapState('userActionSaveMeasure/personTaskStageNum', [
      'listModel',
      'showSwitch',
      'demandType',
      'taskType'
    ])
  },
  methods: {
    ...mapMutations('userActionSaveMeasure/personTaskStageNum', [
      'updateStartDate',
      'updateEndDate',
      'updateShowSwitch',
      'updateDemandType',
      'updateTaskType'
    ]),
    ...mapActions('measureForm', [
      'queryTaskSimpleByIds',
      'queryUserCoreData',
      'queryTaskNumByUserIdsDate'
    ]),
    /* 获取图表数据 */
    async initBarChartData(type = 'bar', stack = 'stack') {
      // 图表高度
      this.height =
        this.userSelected.length < 10
          ? '350px'
          : `${350 + this.userSelected.length * 30}px`;
      // 没选时间不发接口
      if (this.$v.listModel.$invalid) {
        this.stackBarChartLoading = false;
        return;
      }
      // 如果没有选人前端弹窗提示
      if (!this.userSelected.length) {
        this.$q
          .dialog({
            title: `温馨提示`,
            message: `您还未选择人员，请至少选择一个人员后再查询！`,
            ok: '我知道了'
          })
          .onOk(async () => {
            this.stackBarChartLoading = false;
          });
        return;
      }
      this.stackBarChartLoading = true;
      const end_date = this.listModel.end_date.replace(/-/g, '/');
      const start_date = this.listModel.start_date.replace(/-/g, '/');
      try {
        await this.queryTaskNumByUserIdsDate({
          user_ids: this.userSelected.map(item => item.id),
          roles: this.roles,
          end_date,
          start_date,
          demandType: this.demandType,
          taskType: this.taskType
        });
        // 绘制图表
        this.chartData = this.initBarChart(this.taskNumByUserIdsDate);
        this.stackBarChartLoading = false;
      } catch (e) {
        this.stackBarChartLoading = false;
      }
    },
    // 处理图表数据
    initBarChart(charData) {
      // 纵坐标
      const xAxisId = Object.keys(charData);
      const xAxis = xAxisId.map(id => {
        return this.getUserName(id);
      });
      // 图例
      const seriesNames = [
        'create',
        'develop',
        'sit',
        'uat',
        'rel',
        'production'
      ];
      const legend = seriesNames.map(item => {
        return this.legendName[item];
      });
      //数据
      const series = seriesNames.map((key, i) => {
        const data = xAxisId.map(id => {
          return charData[id][key];
        });

        return {
          name: this.legendName[key],
          id: this.nameKey[key],
          type: 'bar',
          stack: 'stack',
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
          },
          data: data
        };
      });
      return {
        xAxis,
        legend,
        series
      };
    },
    // 获取用户名
    getUserName(userId) {
      const users = this.userList.find(item => {
        return item.id === userId;
      });
      const { user_name_cn } = users;
      return user_name_cn;
    },
    // 打开某个阶段任务列表
    async handleTaskListOpen({ name, seriesId, seriesName }) {
      this.taskListOpen = true;
      this.showLoading = true;
      const userId = this.userSelected.find(item => {
        return item.user_name_cn === name;
      }).id;
      let ids = [];
      const end_date = this.listModel.end_date.replace(/-/g, '/');
      const start_date = this.listModel.start_date.replace(/-/g, '/');
      // this.title = `${start_date}-${end_date}内进入${seriesId.split('_')[0]}`;
      this.title = `${start_date}-${end_date}内${seriesName}`;
      ids = this.taskNumByUserIdsDate[userId][seriesId];
      await this.queryTaskSimpleByIds({ ids: ids });
      this.showLoading = false;
    },
    // 结束时间控制
    endOptions(date) {
      this.listModel.start_date = this.listModel.start_date
        ? this.listModel.start_date
        : '';
      return date > this.listModel.start_date.replace(/-/g, '/');
    },
    // 开始时间控制
    startOptions(date) {
      if (this.listModel.end_date) {
        return date < this.listModel.end_date.replace(/-/g, '/');
      }
      return true;
    },
    async init(data, roleList, selectedCompanyList, selectedList) {
      const param = {
        ids: data,
        roles: roleList,
        companyList: selectedCompanyList,
        groupList: selectedList
      };
      this.searchObj = param;

      this.roles = roleList ? roleList : [];
      this.userSelected = data ? data : [];

      this.initBarChartData();
    },
    // 下载不同阶段任务数量统计表表格
    downloadstageTaskExcel() {
      const end_date = this.listModel.end_date.replace(/-/g, '/');
      const start_date = this.listModel.start_date.replace(/-/g, '/');
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'stageTask',
          '各人员' +
            start_date +
            '-' +
            end_date +
            '内阶段任务推进情况统计表.xlsx'
        );
      });
    },
    // 切换tab存储筛选条件
    saveData(to, from, next) {
      sessionStorage.setItem('taskOutputObj', JSON.stringify(this.searchObj));
    }
  },
  async created() {
    this.stackBarChartLoading = true;
    // 查询全量的人员
    await this.queryUserCoreData();
    this.userList = [];
    this.usersList.forEach(item => {
      if (item.status === '0') {
        this.userList.push({ ...item, selected: false });
      }
    });
    //获取缓存的树信息
    let item = JSON.parse(sessionStorage.getItem('taskOutputObj'));
    if (item && item.ids && item.ids.length > 0) {
      this.init(item.ids, item.roles, item.companyList, item.groupList);
    }
    this.stackBarChartLoading = false;
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
</style>
