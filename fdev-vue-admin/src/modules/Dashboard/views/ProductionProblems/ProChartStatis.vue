<template>
  <div>
    <Loading :visible="loading">
      <!-- 筛选条件 -->
      <f-block block class="q-my-md">
        <div class="row q-mt-md">
          <!-- 开始日期 -->
          <f-formitem
            class="col-4 q-pr-md"
            label="开始日期"
            label-style="width:80px"
          >
            <f-date
              :value="proModel.startDate"
              @input="proModelStartDate($event)"
              :options="sitOptions"
            />
          </f-formitem>
          <!-- 结束日期 -->
          <f-formitem
            class="col-4 q-pr-md"
            label="结束日期"
            label-style="width:80px"
          >
            <f-date
              :value="proModel.endDate"
              @input="proModelEndDate($event)"
              :options="relOptions"
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
      <f-block block class="q-my-md">
        <!-- 图表形式 -->
        <Loading
          v-show="showSwitch === 'chartShow'"
          :visible="globalLoading['dashboard/queryProIssueRate']"
        >
          <p class="inline-block title q-ml-lg">
            窗口板块投产质量
          </p>
          <IssueBarchart
            class="bg-white"
            width="100%"
            :chart-data="rateDate"
            id="rateBarchart"
            ref="rateBarchart"
            :height="`${350 + proIssueRateChart.length * 18}px`"
          />
        </Loading>
        <!-- 表格 -->
        <Loading
          v-show="showSwitch === 'formShow'"
          :visible="globalLoading['dashboard/queryProIssueRate']"
        >
          <div>
            <div class="text-h5 text-title text-center q-ma-md">
              窗口板块投产质量
            </div>
            <table
              width="100%"
              border="1"
              bordercolor="black"
              cellspacing="0"
              cellpadding="0"
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
        </Loading>
      </f-block>
      <!-- 各板块易出现问题 -->
      <f-block block class="q-mb-md">
        <!-- 图表形式 -->
        <Loading
          v-show="showSwitch === 'chartShow'"
          :visible="globalLoading['dashboard/queryProIssueType']"
        >
          <p class="inline-block title q-ml-lg">
            各板块易出现问题
          </p>
          <IssueBarchart
            class="bg-white"
            width="100%"
            :chart-data="typeDate"
            id="doubleBarchart"
            ref="doubleBarchart"
            :height="`${350 + proIssueTypeChart.length * 18}px`"
          />
        </Loading>
        <!-- 表格 -->
        <Loading
          v-show="showSwitch === 'formShow'"
          :visible="globalLoading['dashboard/queryProIssueType']"
        >
          <div>
            <div class="text-h5 text-center text-title q-ma-md">
              各板块易出现问题
            </div>
            <p class="fontColor">
              <f-icon name="alert_c_o" style="align:middle" />
              温馨提示：占比=类型数量/生产问题数
            </p>
            <table
              width="100%"
              border="1"
              bordercolor="black"
              cellspacing="0"
              cellpadding="0"
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
        </Loading>
      </f-block>
    </Loading>
  </div>
</template>

<script>
import { mapState, mapGetters, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import IssueBarchart from '@/modules/Dashboard/components/Chart/IssueBarchart.vue';
export default {
  name: 'List',
  components: {
    Loading,
    IssueBarchart
  },
  data() {
    return {
      loading: false,
      typeIssue: [],
      rateDate: {
        legend: [],
        yAxis: [],
        chartLeft: [],
        chartRight: []
      },
      typeDate: {
        legend: [],
        yAxis: [],
        chartLeft: [],
        chartRight: []
      },
      formLine: [],
      typeLineList: []
    };
  },
  computed: {
    ...mapState('userActionSaveDashboard/productionProblems/proChartStatis', [
      'proModel',
      'showSwitch'
    ]),
    ...mapState('dashboard', ['proIssueRateChart', 'proIssueTypeChart']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('dashboard', ['proIssueRateChartAll'])
  },
  methods: {
    ...mapMutations(
      'userActionSaveDashboard/productionProblems/proChartStatis',
      ['proModelStartDate', 'proModelEndDate', 'updateShowSwitch']
    ),
    ...mapActions('dashboard', ['queryProIssueRate', 'queryProIssueType']),
    relOptions(date) {
      this.proModel.startDate = this.proModel.startDate
        ? this.proModel.startDate
        : '';
      return date > this.proModel.startDate.replace(/-/g, '/');
    },
    sitOptions(date) {
      if (this.proModel.endDate) {
        return date < this.proModel.endDate.replace(/-/g, '/');
      }
      return true;
    },
    async initChartData() {
      this.queryProIssueRate({
        startDate: this.proModel.startDate,
        endDate: this.proModel.endDate
      }).then(() => {
        this.chartRateData();
        this.$refs.rateBarchart.draw();
      });

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
    switchInput(val) {
      this.updateShowSwitch(val);
      this.$refs.doubleBarchart.chartResize();
      this.$refs.rateBarchart.chartResize();
    },
    chartRateData() {
      const legend = ['生产问题数'];
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
            normal: {
              show: true,
              position: 'inside',
              textStyle: {
                fontSize: 12
              }
            },
            emphasis: {
              show: true,
              position: 'inside',
              offset: [0, 0],
              textStyle: {
                fontSize: 14
              }
            }
          },
          itemStyle: {
            normal: {
              opacity: 1
            },
            emphasis: {
              opacity: 1
            }
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
            normal: {
              show: true,
              position: 'inside',
              textStyle: {
                fontSize: 12
              }
            },
            emphasis: {
              show: true,
              position: 'inside',
              offset: [0, 0],
              textStyle: {
                fontSize: 14
              }
            }
          },
          itemStyle: {
            normal: {
              opacity: 1
            },
            emphasis: {
              opacity: 1
            }
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
            normal: {
              show: true,
              position: 'inside',
              textStyle: {
                fontSize: 12
              }
            },
            emphasis: {
              show: true,
              position: 'inside',
              offset: [0, 0],
              textStyle: {
                fontSize: 14
              }
            }
          },
          itemStyle: {
            normal: {
              opacity: 1
            },
            emphasis: {
              opacity: 1
            }
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
            normal: {
              show: true,
              position: 'inside'
            },
            emphasis: {
              show: true,
              position: 'inside',
              offset: [0, 0],
              textStyle: {
                fontSize: 14
              }
            }
          },
          itemStyle: {
            normal: {
              opacity: 1
            },
            emphasis: {
              opacity: 1
            }
          },
          percent: percent
        };
      });
      this.typeDate = { legend, yAxis, chartLeft, chartRight };
    }
  },
  async created() {
    this.formLineList();
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
  height 40px
  line-height 40px
  color #008acd
  font-size 18px
.fontColor
   color #4DBB59
   line-height 16px
</style>
