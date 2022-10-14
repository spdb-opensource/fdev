<template>
  <f-block page>
    <Loading :visible="stackBarChartLoading">
      <UserSelected @switch="switchShow" @queryClick="init" />
      <!-- 图表部分 -->
      <div v-show="showSwitch === 'chartShow'">
        <div class="contain-wrapper q-mt-md q-pl-sm q-pt-md bg-white">
          <div class="row flex-between">
            <p class="chart-title">
              各人员{{ listModel.start_date }} -
              {{ listModel.end_date }}不同阶段任务数量统计表
            </p>
            <fdev-btn
              ficon="download"
              normal
              label="下载"
              @click="downloadstageTaskExcel"
            />
          </div>
          <div class="row q-pa-sm table-wrapper">
            <f-formitem page label="开始时间">
              <f-date
                :options="startOptions"
                v-model="listModel.start_date"
                @input="initBarChartData"
              />
            </f-formitem>
            <f-formitem page label="结束时间">
              <f-date
                :options="endOptions"
                v-model="listModel.end_date"
                @input="initBarChartData"
              />
            </f-formitem>
          </div>
          <DoubleBarchart
            v-show="showSwitch === 'chartShow'"
            id="stackBar"
            class="bg-white"
            width="100%"
            ref="doubleBarchart"
            :chart-data="chartData"
            @click="handleTaskListOpen"
            :height="
              userSelected.length < 10
                ? '350px'
                : `${350 + userSelected.length * 30}px`
            "
          />
        </div>

        <div class="contain-wrapper q-mt-md q-pl-sm q-pt-md bg-white">
          <p class="inline-block title q-ml-sm">
            各人员挡板交易量统计({{ lastMonth }})
          </p>
          <fdev-btn
            class="float-right"
            ficon="download"
            normal
            label="下载"
            @click="downloadLastMonthExcel"
          ></fdev-btn>
          <StackBarChart
            id="userLastChart"
            class="bg-white"
            width="76vw"
            :chart-top="chartTop"
            :chart-data="lastMonthChart"
            :height="
              userSelected.length < 10
                ? '350px'
                : `${350 + userSelected.length * 30}px`
            "
          />
        </div>

        <div class="contain-wrapper q-mt-md q-pl-sm q-pt-md bg-white">
          <p class="inline-block title q-ml-sm">
            各人员挡板交易量统计({{ thisMonth }}～今天)
          </p>
          <fdev-btn
            class="float-right"
            ficon="download"
            normal
            label="下载"
            @click="downloadThisMonthExcel"
          ></fdev-btn>
          <StackBarChart
            id="userChart"
            class="bg-white"
            width="76vw"
            :chart-top="chartTop"
            :chart-data="thisMonthChart"
            :height="
              userSelected.length < 10
                ? '350px'
                : `${350 + userSelected.length * 30}px`
            "
          />
        </div>
      </div>
      <TaskListDialog
        v-model="taskListOpen"
        :dateTitle="title"
        :showLoading="showLoading"
      />

      <!-- 表格部分 -->
      <div v-show="showSwitch === 'formShow'">
        <div class="contain-wrapper q-mt-md bg-white">
          <p class="chart-title q-pl-sm q-pt-md q-mb-lg">
            各人员{{ listModel.start_date }} -
            {{ listModel.end_date }}不同阶段任务数量统计表
          </p>
          <fdev-space />
          <div class="row q-pa-sm table-wrapper">
            <f-formitem page label="开始时间">
              <f-date
                :options="startOptions"
                v-model="listModel.start_date"
                @input="initBarChartData"
              />
            </f-formitem>
            <f-formitem page label="结束时间">
              <f-date
                :options="endOptions"
                v-model="listModel.end_date"
                @input="initBarChartData"
              />
            </f-formitem>
            <fdev-btn
              class="btn-position"
              ficon="download"
              normal
              label="下载"
              @click="downloadstageTaskExcel"
            />
          </div>
          <UserTaskForm
            id="stageTask"
            :formHeader="leftGroup"
            :position="leftPosition"
            :formData="chartData"
            :getUserName="getUserName"
          />
        </div>

        <div class="contain-wrapper row q-mt-md">
          <div class="col bg-white">
            <p class="inline-block title q-ml-sm">
              各人员挡板交易量统计({{ lastMonth }})
            </p>
            <fdev-btn
              class="float-right"
              ficon="download"
              normal
              label="下载"
              @click="downloadLastMonthExcel"
            ></fdev-btn>
            <UserBaffleForm
              id="lastMonth"
              :form-data="iamsUserLastMonthChart"
            />
          </div>

          <div class="col q-ml-sm bg-white">
            <p class="inline-block title q-ml-sm">
              各人员挡板交易量统计({{ thisMonth }}～今天)
            </p>
            <fdev-btn
              class="float-right"
              ficon="download"
              normal
              label="下载"
              @click="downloadThisMonthExcel"
            ></fdev-btn>
            <UserBaffleForm
              id="thisMonth"
              :form-data="iamsUserThisMonthChart"
            />
          </div>
        </div>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapGetters } from 'vuex';
import StackBarChart from '@/modules/Dashboard/components/Chart/StackBarChart';
import DoubleBarchart from '@/modules/Dashboard/components/Chart/DoubleBarchart';
import TaskListDialog from '@/modules/Dashboard/components/Chart/TaskListDialog';
import moment from 'moment';
import UserTaskForm from '@/modules/Dashboard/components/form/UserTaskForm';
import UserBaffleForm from '@/modules/Dashboard/components/form/UserBaffleForm';
import UserSelected from '@/modules/Dashboard/components/UserSelected';

export default {
  components: {
    TaskListDialog,
    Loading,
    StackBarChart,
    DoubleBarchart,
    UserTaskForm,
    UserBaffleForm,
    UserSelected
  },
  data() {
    return {
      showLoading: false,
      userSelected: [],
      chartData: {
        xAxis: [],
        xAxisId: [],
        left: {},
        right: {}
      },
      formData: {
        xAxis: []
      },
      stackBarChartLoading: false,
      roles: [],
      userList: [],
      taskListOpen: false,
      thisMonthChart: {},
      lastMonthChart: {},
      thisMonth: '',
      title: null,
      lastMonth: '',
      listModel: {
        start_date: moment(new Date())
          .subtract(1, 'month')
          .format('YYYY/MM/DD'),
        end_date: moment(new Date()).format('YYYY/MM/DD')
      },
      height: '350px',
      showSwitch: 'chartShow',
      leftGroup: ['创建', '开发中', 'SIT', 'UAT', 'REL', '已投产'],
      leftPosition: 'left',
      chartTop: '2%',
      searchObj: {}
    };
  },
  watch: {
    showSwitch(val) {
      if (val) {
        this.$refs.doubleBarchart.chartResize();
      }
    }
  },
  computed: {
    ...mapState('dashboard', {
      usersList: 'userList',
      userChartData: 'userChartData',
      iamsUserThisMonthChart: 'iamsUserThisMonthChart',
      iamsUserLastMonthChart: 'iamsUserLastMonthChart',
      taskNumByUserIdsDate: 'taskNumByUserIdsDate'
    }),
    ...mapGetters('dashboard', ['iamsUserChartFormatter'])
  },
  methods: {
    ...mapActions('dashboard', [
      'queryUserCoreData',
      'queryTaskNumByMember',
      'queryTasksByIds',
      'queryIamsUserChart',
      'queryTaskNumByUserIdsDate',
      'queryIamsUserLastMonthChart'
    ]),
    /* 处理图表数据 */
    async initBarChartData(type = 'bar', stack = 'stack') {
      if (this.userSelected.length === 0) {
        this.chartData = {
          xAxis: [],
          xAxisId: [],
          left: {},
          right: {}
        };
        return;
      }
      this.stackBarChartLoading = true;
      const end_date = this.listModel.end_date.replace(/-/g, '/');
      const start_date = this.listModel.start_date.replace(/-/g, '/');
      await this.queryTaskNumByUserIdsDate({
        user_ids: this.userSelected.map(item => item.id),
        roles: this.roles,
        end_date,
        start_date
      });
      const xAxisId = Object.keys(this.taskNumByUserIdsDate);
      const xAxis = xAxisId.map(id => {
        return this.getUserName(id);
      });
      this.chartData = {
        left: this.taskNumByUserIdsDate,
        xAxisId,
        xAxis
      };
      this.$refs.doubleBarchart.draw();
      this.stackBarChartLoading = false;
      try {
        this.$refs.qDateProxyStart.hide();
        this.$refs.qDateProxyEnd.hide();
      } catch (err) {
        //
      }
    },
    getUserName(userId) {
      const users = this.userList.find(item => {
        return item.id === userId;
      });
      const { user_name_cn } = users;
      return user_name_cn;
    },
    async handleTaskListOpen({ group, seriesId, name }) {
      this.taskListOpen = true;
      this.showLoading = true;
      const userId = this.userSelected.find(item => {
        return item.user_name_cn === group;
      }).id;
      let ids = [];
      if (seriesId.includes('-')) {
        this.title = null;
        ids = this.userChartData[userId][seriesId.split('-')[1]];
      } else {
        const end_date = this.listModel.end_date.replace(/-/g, '/');
        const start_date = this.listModel.start_date.replace(/-/g, '/');
        this.title = `${start_date}-${end_date}内进入${
          seriesId.split('_')[0]
        }任务列表`;
        ids = this.taskNumByUserIdsDate[userId][seriesId];
      }
      await this.queryTasksByIds({ ids: ids });
      this.showLoading = false;
    },
    initIamsChart() {
      this.thisMonth = moment(new Date()).format('YYYY-MM');
      this.lastMonth = moment(new Date())
        .subtract(1, 'month')
        .format('YYYY-MM');
      const email = this.userSelected.map(item => item.email);
      /* 本月数据 */
      this.queryIamsUserChart({
        email: email,
        date: this.thisMonth
      }).then(() => {
        this.thisMonthChart = this.iamsUserChartFormatter(
          this.iamsUserThisMonthChart,
          email
        );
      });
      /* 上月数据 */
      this.queryIamsUserLastMonthChart({
        email: email,
        date: this.lastMonth
      }).then(() => {
        this.lastMonthChart = this.iamsUserChartFormatter(
          this.iamsUserLastMonthChart,
          email
        );
      });
    },
    endOptions(date) {
      this.listModel.start_date = this.listModel.start_date
        ? this.listModel.start_date
        : '';
      return date > this.listModel.start_date.replace(/-/g, '/');
    },
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
      this.height =
        this.userSelected.length < 10
          ? '350px'
          : `${350 + this.userSelected.length * 30}px`;
      this.initBarChartData();
      this.initIamsChart();
    },
    switchShow(data) {
      this.showSwitch = data;
    },
    // 下载表格
    downloadstageTaskExcel() {
      const end_date = this.listModel.end_date.replace(/-/g, '/');
      const start_date = this.listModel.start_date.replace(/-/g, '/');
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'stageTask',
          '各人员' + start_date + '-' + end_date + '不同阶段任务数量统计表.xlsx'
        );
      });
    },
    downloadLastMonthExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'lastMonth',
          '各人员挡板交易量统计' + this.lastMonth + '.xlsx'
        );
      });
    },
    downloadThisMonthExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'thisMonth',
          '各人员挡板交易量统计' + this.thisMonth + '～今天.xlsx'
        );
      });
    }
  },
  async created() {
    if (sessionStorage.getItem('dateObj')) {
      const { startDate, endDate, showSwitch } = JSON.parse(
        sessionStorage.getItem('dateObj')
      );
      this.listModel.start_date = startDate;
      this.listModel.end_date = endDate;
      this.showSwitch = showSwitch;
    }
    this.stackBarChartLoading = true;
    await this.queryUserCoreData();
    this.userList = [];
    this.usersList.forEach(item => {
      if (item.status === '0') {
        this.userList.push({ ...item, selected: false });
      }
    });
    let item = JSON.parse(sessionStorage.getItem('taskOutputObj'));
    if (item && item.ids && item.ids.length > 0) {
      this.init(item.ids, item.roles, item.companyList, item.groupList);
    }
    this.stackBarChartLoading = false;
  },
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('taskOutputObj');
      sessionStorage.removeItem('dateObj');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    sessionStorage.setItem('taskOutputObj', JSON.stringify(this.searchObj));
    sessionStorage.setItem(
      'dateObj',
      JSON.stringify({
        startDate: this.listModel.start_date,
        endDate: this.listModel.end_date,
        showSwitch: this.showSwitch
      })
    );
    next();
  }
};
</script>

<style lang="stylus" scoped>
.chart-title
  height 40px
  line-height 40px
  color #008acd
  font-size 18px
.table-wrapper
  position relative
  .btn-position
    position absolute
    right 5px
    top 0.4rem
.title
  height 40px
  line-height 40px
  color #008acd
  font-size 18px
.flex-between{
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
