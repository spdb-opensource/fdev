<template>
  <div class="lineChart" ref="chart"></div>
</template>

<script>
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/bar';
require('echarts/theme/macarons');
import 'echarts/lib/component/grid';
import 'echarts/lib/component/tooltip';
import { mapState } from 'vuex';

export default {
  data() {
    return {
      chart: null,
      data: [],
      xAxis: []
    };
  },
  props: {
    gitlab_id: Number
  },
  computed: {
    ...mapState('appForm', ['sonarChart'])
  },
  methods: {
    draw() {
      this.chart = echarts.init(this.$refs.chart);

      const seriesItem = {
        type: 'line',
        symbolSize: 4,
        lineStyle: {
          width: 4
        }
      };

      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          position: {
            top: '50%',
            left: '50%'
          }
        },
        title: {
          text: '项目活动'
        },
        grid: {
          top: 25,
          bottom: 5
        },
        xAxis: {
          type: 'category',
          show: false,
          axisLine: {
            show: false
          },
          data: this.sonarChart.xAxis,
          boundaryGap: false
        },
        yAxis: {
          type: 'value',
          show: false,
          axisLine: {
            show: false
          }
        },
        series: [
          {
            name: 'bugs',
            data: this.sonarChart.bugs,
            ...seriesItem,
            itemStyle: {
              color: '#E63453'
            }
          },
          {
            name: '异味',
            data: this.sonarChart.code_smells,
            ...seriesItem,
            itemStyle: {
              color: '#5C2FF3'
            }
          },
          {
            name: '漏洞',
            data: this.sonarChart.vulnerabilities,
            ...seriesItem,
            itemStyle: {
              color: '#F4B24D'
            }
          }
        ]
      });
    }
  },
  mounted() {
    this.draw();
  }
};
</script>

<style lang="stylus" scoped>
.lineChart
  height 100%;
  width 100%
</style>
