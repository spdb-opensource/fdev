<template>
  <div>
    <div :ref="barRef" class="bar-style"></div>
  </div>
</template>
<script>
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/bar';
import 'echarts/lib/component/legend';
import 'echarts/lib/component/title';
import 'echarts/lib/component/tooltip';

export default {
  name: 'BarChart',
  props: {
    barRef: String,
    barOpt: Object
  },
  data() {
    return {
      barChart: {}
    };
  },
  computed: {
    //柱状图展示数据
    option() {
      return {
        tooltip: {
          trigger: 'item',
          axisPointer: {
            type: 'shadow'
          }
        },
        title: {
          top: 0,
          left: -5,
          textStyle: {
            fontSize: 20,
            fontFamily: 'PingFangSC',
            fontWeight: 400,
            color: '#394853'
          },
          text: this.barOpt.title ? this.barOpt.title.text : ''
        },
        color: ['#FAD236 ', '#3AA0FF'],
        legend: {
          data: ['业务需求', '科技需求'],
          icon: 'roundRect',
          right: 0,
          top: 0,
          itemWidth: 9,
          itemHeight: 1.5,
          textStyle: {
            fontFamily: 'PingFangSC',
            fontWeight: 400,
            fontSize: '12px',
            color: '#8C8C8C'
          }
        },
        grid: {
          left: 0,
          bottom: '0%',
          right: 0,
          containLabel: true
        },
        axisLabel: {
          fontWeight: 400,
          color: '#545454',
          fontFamily: 'PingFangSC'
        },
        xAxis: [
          {
            type: 'category',
            axisTick: { show: false },
            axisLine: {
              lineStyle: {
                color: '#BFBFBF'
              }
            },
            axisLabel: {
              fontSize: 12
            },
            data: this.barOpt.xAxisData
          }
        ],
        yAxis: [
          {
            type: 'value',
            axisLine: {
              show: false
            },
            axisTick: { show: false },
            axisLabel: {
              fontSize: 12
            },
            splitLine: {
              lineStyle: {
                type: 'dotted'
              }
            }
          }
        ],
        series: [
          {
            name: '业务需求',
            type: 'bar',
            barGap: 0.05,
            barWidth: '25px',
            data: this.barOpt.series[0]
          },
          {
            name: '科技需求',
            type: 'bar',
            barWidth: '25px',
            data: this.barOpt.series[1]
          }
        ]
      };
    }
  },
  watch: {
    option: function(newval) {
      this.barChart.setOption(newval);
    }
  },
  mounted() {
    this.barChart = echarts.init(this.$refs[this.barRef]);
    this.barChart.setOption(this.option);
  }
};
</script>
<style scoped lang="stylus">
.bar-style
  width 580px
  height 326px
</style>
