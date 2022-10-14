<template>
  <div>
    <!-- 搜索条件 -->
    <div>
      <UserSelected @queryClick="init" />
      <div class="row">
        <fdev-space />
        <!-- 表格图表切换按钮 -->
        <fdev-btn-toggle
          v-model="showSwitch"
          :options="[
            { label: '图表', value: 'chartShow' },
            { label: '表格', value: 'formShow' }
          ]"
        />
      </div>
    </div>
    <!-- 数据展示 -->
    <Loading :visible="stackBarChartLoading">
      <!-- 图表部分 -->
      <div v-show="showSwitch === 'chartShow'" class="q-mt-md">
        <!-- 各人员上个月挡板交易量统计 -->
        <div>
          <!-- 标题 -->
          <div class="row">
            <div class="title">
              <f-icon
                name="dashboard_s_f"
                class="text-primary"
                style="margin-right:8px"
              />
              各人员上月挡板交易量统计({{ lastMonth }})
            </div>
            <f-icon
              name="download"
              @click="downloadLastMonthExcel"
              class="text-primary q-ml-xs cursor-pointer"
            />
          </div>
          <div>
            <StackBarChart
              id="userLastChart"
              width="100%"
              :chart-top="chartTop"
              :chart-data="lastMonthChart"
              :height="
                userSelected.length < 10
                  ? '350px'
                  : `${350 + userSelected.length * 30}px`
              "
            />
          </div>
        </div>
        <!-- 各人员挡板交易量统计月初至今天 -->
        <div class="q-mt-lg">
          <!-- 标题 -->
          <div class="row">
            <div class="title">
              <f-icon
                name="dashboard_s_f"
                class="text-primary"
                style="margin-right:8px"
              />
              各人员本月挡板交易量统计({{ thisMonth }}～今天)
            </div>
            <f-icon
              name="download"
              @click="downloadThisMonthExcel"
              class="text-primary q-ml-xs cursor-pointer"
            />
          </div>
          <div>
            <StackBarChart
              id="userChart"
              width="100%"
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
      </div>

      <!-- 表格部分 -->
      <div v-show="showSwitch === 'formShow'">
        <div class="contain-wrapper row q-my-lg">
          <!-- 各人员上个月挡板交易量统计 -->
          <div class="col">
            <!-- 标题 -->
            <div class="row q-mb-md">
              <div class="title">
                <f-icon
                  name="list_s_f"
                  class="text-primary"
                  style="margin-right:8px"
                />
                各人员挡板交易量统计({{ lastMonth }})
              </div>
              <f-icon
                name="download"
                @click="downloadLastMonthExcel"
                class="text-primary q-ml-xs cursor-pointer"
              />
            </div>
            <UserBaffleForm
              id="lastMonth"
              :form-data="iamsUserLastMonthChart"
            />
          </div>
          <!-- 各人员挡板交易量统计本月初至今天 -->
          <div class="col q-ml-md">
            <!-- 标题 -->
            <div class="row q-mb-md">
              <div class="title">
                <f-icon
                  name="list_s_f"
                  class="text-primary"
                  style="margin-right:8px"
                />
                各人员本月挡板交易量统计({{ thisMonth }}～今天)
              </div>
              <f-icon
                name="download"
                @click="downloadThisMonthExcel"
                class="text-primary q-ml-xs cursor-pointer"
              />
            </div>
            <UserBaffleForm
              id="thisMonth"
              :form-data="iamsUserThisMonthChart"
            />
          </div>
        </div>
      </div>
    </Loading>
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapGetters } from 'vuex';
import StackBarChart from '@/modules/Measure/components/Chart/StackBarChart';
import moment from 'moment';
import UserBaffleForm from '@/modules/Dashboard/components/form/UserBaffleForm';
import UserSelected from '@/modules/Measure/components/UserSelected';
export default {
  components: {
    Loading,
    StackBarChart,
    UserBaffleForm,
    UserSelected
  },
  data() {
    return {
      userSelected: [],
      stackBarChartLoading: false,
      roles: [],
      thisMonthChart: {},
      lastMonthChart: {},
      thisMonth: '',
      lastMonth: '',
      height: '350px',
      showSwitch: 'chartShow',
      chartTop: '2%',
      searchObj: {}
    };
  },
  watch: {},
  computed: {
    ...mapState('dashboard', {
      iamsUserThisMonthChart: 'iamsUserThisMonthChart',
      iamsUserLastMonthChart: 'iamsUserLastMonthChart'
    }),
    ...mapGetters('dashboard', ['iamsUserChartFormatter'])
  },
  methods: {
    ...mapActions('dashboard', [
      'queryIamsUserChart',
      'queryIamsUserLastMonthChart'
    ]),
    // 挡板相关数据初始化
    async initIamsChart() {
      this.thisMonth = moment(new Date()).format('YYYY-MM');
      this.lastMonth = moment(new Date())
        .subtract(1, 'month')
        .format('YYYY-MM');
      const email = this.userSelected.map(item => item.email);
      /* 本月数据 */
      await this.queryIamsUserChart({
        email: email,
        date: this.thisMonth
      });
      this.thisMonthChart = this.iamsUserChartFormatter(
        this.iamsUserThisMonthChart,
        email
      );
      /* 上月数据 */
      await this.queryIamsUserLastMonthChart({
        email: email,
        date: this.lastMonth
      });
      this.lastMonthChart = this.iamsUserChartFormatter(
        this.iamsUserLastMonthChart,
        email
      );
    },
    // 查询
    async init(data, roleList, selectedCompanyList, selectedList) {
      this.stackBarChartLoading = true;
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
      try {
        await this.initIamsChart();
        this.stackBarChartLoading = false;
      } catch (e) {
        this.stackBarChartLoading = false;
      }
    },
    // 下载上月人员挡板交易量
    downloadLastMonthExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'lastMonth',
          '各人员挡板交易量统计' + this.lastMonth + '.xlsx'
        );
      });
    },
    // 下载本月初至今天人员挡板交易量
    downloadThisMonthExcel() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_table_to_excel(
          'thisMonth',
          '各人员挡板交易量统计' + this.thisMonth + '～今天.xlsx'
        );
      });
    },
    //离开页面时做原进原出
    saveData() {
      sessionStorage.setItem('IamsObj', JSON.stringify(this.searchObj));
    }
  },
  async created() {
    this.stackBarChartLoading = true;
    let item = JSON.parse(sessionStorage.getItem('IamsObj'));
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
