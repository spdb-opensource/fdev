<template>
  <div>
    <Loading :visible="loading">
      <!-- 筛选条件 -->
      <f-block block class="q-mb-md">
        <div class="row q-mt-md">
          <!-- 开始日期 -->
          <f-formitem
            class="col-4 q-pr-md"
            label="开始时间"
            label-style="width:80px"
          >
            <f-date
              :value="proModel.startDate"
              @input="proModelStartDate($event)"
              :options="startOptions"
            />
          </f-formitem>
          <!-- 结束日期 -->
          <f-formitem
            class="col-4 q-pr-md"
            label="结束时间"
            label-style="width:80px"
          >
            <f-date
              :value="proModel.endDate"
              @input="proModelEndDate($event)"
              :options="endOptions"
            />
          </f-formitem>
          <fdev-space />
          <div class="row  col-4">
            <!-- 查询按钮 -->
            <fdev-btn
              class="q-mx-md"
              dialog
              label="查询"
              @click="initChartData"
              ficon="search"
            />
            <!-- 切换按钮 -->
            <fdev-btn-toggle
              :value="showSwitch"
              @input="switchInput($event)"
              :options="[
                { label: '图表', value: 'chartShow' },
                { label: '表格', value: 'formShow' }
              ]"
            />
          </div>
        </div>
      </f-block>
      <!-- 窗口板块投产质量 -->
      <f-block block class="q-mb-md">
        <Loading :visible="globalLoading['measureForm/queryProIssueRate']">
          <!-- 标题 -->
          <div class="title">
            <f-icon
              name="dashboard_s_f"
              class="text-primary"
              style="margin-right:8px"
            />
            <span>窗口板块投产质量</span>
          </div>
          <!-- 有数据时 -->
          <div v-if="proIssueRateChart.length" class="q-mt-md">
            <!-- 图表形式 -->
            <div v-show="showSwitch === 'chartShow'">
              <IssueBarchart
                width="100%"
                :chart-data="rateDate"
                id="rateBarchart"
                ref="rateBarchart"
                :height="`${350 + proIssueRateChart.length * 18}px`"
              />
            </div>
            <!-- 表格形式 -->
            <div v-show="showSwitch === 'formShow'">
              <div>
                <p class="fontColor">
                  温馨提示：统计所选时间段内出现生产问题的窗口，以及窗口所属小组需求数和生产问题数的比例
                </p>
                <table
                  width="100%"
                  border="1"
                  bordercolor="#DDDDDD"
                  class="table-style"
                  id="table"
                >
                  <thead>
                    <tr>
                      <th>窗口</th>
                      <th>板块</th>
                      <th>生产问题数量</th>
                      <th>需求总数</th>
                      <th>生产问题百分比</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="(item, index) in this.proIssueRateChart"
                      :key="index"
                    >
                      <!-- 窗口 -->
                      <td>{{ item.release_date }}</td>
                      <!-- 板块 -->
                      <td>{{ item.owner_group_name }}</td>
                      <!-- 生产问题数量 -->
                      <td>{{ item.proIssueCount }}</td>
                      <!-- 需求总数 -->
                      <td>{{ item.rqrCount }}</td>
                      <!-- 生产问题百分比 -->
                      <td>{{ item.rqrProIssueRate }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
          <!-- 无数据时 -->
          <div v-else class="column items-center">
            <f-image name="no_data" class="q-mt-xl" />
          </div>
        </Loading>
      </f-block>
      <!-- 各板块易出现问题 -->
      <f-block block class="q-mb-md">
        <Loading :visible="globalLoading['measureForm/queryProIssueType']">
          <!-- 标题 -->
          <div class="title q-mb-md">
            <f-icon
              name="dashboard_s_f"
              class="text-primary"
              style="margin-right:12px;align:middle"
            />
            各板块易出现问题
          </div>
          <!-- 有数据时 -->
          <div v-if="proIssueTypeChart.length">
            <!-- 图表形式 -->
            <div v-show="showSwitch === 'chartShow'">
              <IssueBarchart
                width="100%"
                :chart-data="typeDate"
                id="doubleBarchart"
                ref="doubleBarchart"
                :height="`${350 + proIssueTypeChart.length * 18}px`"
              />
            </div>
            <!-- 表格 -->
            <div v-show="showSwitch === 'formShow'">
              <div>
                <p class="fontColor">
                  温馨提示：占比=类型数量/生产问题数
                </p>
                <table
                  width="100%"
                  border="1"
                  bordercolor="#DDDDDD"
                  class="table-style"
                  id="table"
                >
                  <thead>
                    <tr>
                      <th rowspan="2">窗口</th>
                      <th rowspan="2">板块</th>
                      <th rowspan="2">问题总数</th>
                      <th colspan="2">需求分析</th>
                      <th colspan="2">开发</th>
                      <th colspan="2">代码审核</th>
                      <th colspan="2">数据库审核</th>
                      <th colspan="2">内测</th>
                      <th colspan="2">业测</th>
                      <th colspan="2">打包</th>
                      <th colspan="2">其他</th>
                    </tr>

                    <tr>
                      <td v-for="(item, index) in this.formLine" :key="index">
                        {{ item }}
                      </td>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(item, index) in typeLineList" :key="index">
                      <td>{{ item.release_date }}</td>
                      <td>{{ item.groupName }}</td>
                      <td>{{ item.proIssueCount }}</td>
                      <td>{{ typeDate.chartRight[0].data[index] }}</td>
                      <td>{{ typeDate.chartRight[0].percent[index] }}</td>
                      <td>{{ typeDate.chartRight[1].data[index] }}</td>
                      <td>{{ typeDate.chartRight[1].percent[index] }}</td>
                      <td>{{ typeDate.chartRight[2].data[index] }}</td>
                      <td>{{ typeDate.chartRight[2].percent[index] }}</td>
                      <td>{{ typeDate.chartRight[3].data[index] }}</td>
                      <td>{{ typeDate.chartRight[3].percent[index] }}</td>
                      <td>{{ typeDate.chartRight[4].data[index] }}</td>
                      <td>{{ typeDate.chartRight[4].percent[index] }}</td>
                      <td>{{ typeDate.chartRight[5].data[index] }}</td>
                      <td>{{ typeDate.chartRight[5].percent[index] }}</td>
                      <td>{{ typeDate.chartRight[6].data[index] }}</td>
                      <td>{{ typeDate.chartRight[6].percent[index] }}</td>
                      <td>{{ typeDate.chartRight[7].data[index] }}</td>
                      <td>{{ typeDate.chartRight[7].percent[index] }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
          <!-- 无数据时 -->
          <div v-else class="column items-center">
            <f-image name="no_data" class="q-mt-xl" />
          </div>
        </Loading>
      </f-block>
    </Loading>
  </div>
</template>

<script>
import { mapState, mapGetters, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import IssueBarchart from '@/modules/Measure/components/Chart/IssueBarchart.vue';
export default {
  name: 'List',
  components: {
    Loading,
    IssueBarchart
  },
  data() {
    return {
      loading: false,
      rateDate: {
        legend: [],
        yAxis: [],
        chartLeft: [],
        chartRight: []
      }, //窗口板块投产质量
      typeDate: {
        legend: [],
        yAxis: [],
        chartLeft: [],
        chartRight: []
      }, //各板块易出现问题
      formLine: [], //各板块易出现问题表头
      typeLineList: [] //各板块易出现问题表格数据
    };
  },
  computed: {
    ...mapState('userActionSaveMeasure/proChartStatis', [
      'proModel',
      'showSwitch'
    ]),
    ...mapState('measureForm', ['proIssueRateChart', 'proIssueTypeChart']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('measureForm', ['proIssueRateChartAll'])
  },
  methods: {
    ...mapMutations('userActionSaveMeasure/proChartStatis', [
      'proModelStartDate',
      'proModelEndDate',
      'updateShowSwitch'
    ]),
    ...mapActions('measureForm', ['queryProIssueRate', 'queryProIssueType']),
    // 结束时间范围控制
    endOptions(date) {
      this.proModel.startDate = this.proModel.startDate
        ? this.proModel.startDate
        : '';
      return date > this.proModel.startDate.replace(/-/g, '/');
    },
    // 开始时间范围控制
    startOptions(date) {
      if (this.proModel.endDate) {
        return date < this.proModel.endDate.replace(/-/g, '/');
      }
      return true;
    },
    // 获取图表数据
    async initChartData() {
      // 板块窗口投产质量
      this.queryProIssueRate({
        startDate: this.proModel.startDate,
        endDate: this.proModel.endDate
      }).then(() => {
        this.chartRateData();
        this.$refs.rateBarchart.draw();
      });
      //各板块易出现问题
      this.queryProIssueType({
        startDate: this.proModel.startDate,
        endDate: this.proModel.endDate
      }).then(() => {
        this.typeLineList = this.proIssueTypeChart;
        this.chartTypeData();
        this.$refs.doubleBarchart.draw();
      });
    },
    formLineList() {
      for (let i = 0; i < 8; i++) {
        this.formLine.push('数量', '占比');
      }
      return this.formLine;
    },
    // 切换图表/表格
    switchInput(val) {
      this.updateShowSwitch(val);
      this.$refs.doubleBarchart.chartResize();
      this.$refs.rateBarchart.chartResize();
    },
    // 窗口板块投产质量
    chartRateData() {
      const legend = ['生产问题数', '需求数量'];
      const yAxis = [];
      const chartLeft = [
        {
          type: 'bar',
          name: '生产问题数',
          data: [],
          stack: 'left',
          barGap: 15,
          barWidth: 15,
          label: {
            show: true,
            // 0不展示
            formatter: params => {
              if (params.value > 0) {
                return params.value;
              } else {
                return '';
              }
            }
          },
          itemStyle: {
            color: '#38C699'
          }
        }
      ];
      const chartRight = [
        {
          type: 'bar',
          name: '需求数量',
          data: [],
          stack: 'right',
          barGap: 15,
          barWidth: 15,
          xAxisIndex: 2,
          yAxisIndex: 2,
          label: {
            show: true,
            // 0不展示
            formatter: params => {
              if (params.value > 0) {
                return params.value;
              } else {
                return '';
              }
            }
          },
          itemStyle: {
            color: '#FFC916'
          }
        }
      ];
      this.proIssueRateChart.forEach((item, index) => {
        yAxis.push(item.owner_group_name + ' \n ' + item.release_date);
        chartLeft[0].data.push(item.proIssueCount);
        chartRight[0].data.push(item.rqrCount);
      }),
        (this.rateDate = { legend, yAxis, chartLeft, chartRight });
    },
    // 各板块易出现问题
    chartTypeData() {
      const legend = [
        '需求分析',
        '开发',
        '代码审核',
        '数据库审核',
        '内测',
        '业测',
        '打包',
        '其他'
      ];
      const yAxis = [];
      const chartLeft = [
        {
          type: 'bar',
          name: '生产问题数',
          data: [],
          stack: 'left',
          barGap: 15,
          barWidth: 15,
          label: {
            show: true,
            // 0不展示
            formatter: params => {
              if (params.value > 0) {
                return params.value;
              } else {
                return '';
              }
            }
          },
          itemStyle: {
            color: '#38C699'
          }
        }
      ];
      const chartRight = legend.map((leg, index) => {
        const data = [];
        const percent = [];
        this.proIssueTypeChart.forEach(item => {
          if (index === 0) {
            yAxis.push(item.groupName + ' \n ' + item.release_date);
            chartLeft[0].data.push(item.proIssueCount);
          }

          if (item.issues[leg]) {
            percent.push(item.issues[leg].rate);
            data.push(item.issues[leg].count);
          } else {
            percent.push(0);
            data.push(0);
          }
        });
        return {
          name: leg,
          data: data,
          type: 'bar',
          stack: 'right',
          barGap: 15,
          barWidth: 15,
          xAxisIndex: 2,
          yAxisIndex: 2,
          label: {
            show: true,
            // 0不展示
            formatter: params => {
              if (params.value > 0) {
                return params.value;
              } else {
                return '';
              }
            }
          },
          percent: percent
        };
      });
      this.typeDate = { legend, yAxis, chartLeft, chartRight };
    }
  },
  async created() {
    // 组装各板块易出现问题表头
    this.formLineList();
    // 查询数据
    this.initChartData();
  }
};
</script>

<style lang="stylus" scoped>

tr
  td, th
    text-align center
    padding 5px
    max-width 300px
.title
  font-family: PingFangSC-Semibold;
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight:600
.fontColor
   color #4DBB59
   line-height 16px
.table-style
  border-collapse:collapse !important
  font-family: PingFangSC-Regular;
  line-height: 32px;
</style>
