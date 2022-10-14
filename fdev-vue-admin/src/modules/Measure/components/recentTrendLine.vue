<template>
  <div :id="id" :style="{ height: height, width: width }" />
</template>

<script>
import { mapState } from 'vuex';
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/line';
import 'echarts/lib/component/legend';
import 'echarts/lib/component/title';
import 'echarts/lib/component/grid';
import 'echarts/lib/component/tooltip';
export default {
  name: 'LineChart',
  data() {
    return {
      chart: null
    };
  },
  props: {
    id: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '450px'
    },
    chartData: {
      type: Object,
      required: true
    },
    title: {
      type: String
    }
  },
  watch: {
    chartData(val) {
      if (val) {
        this.draw();
      }
    },
    miniState(val) {
      const timer = setInterval(() => {
        this.draw();
        clearInterval(timer);
      }, 100);
    }
  },
  computed: {
    ...mapState('menu', {
      miniState: 'miniState'
    })
  },
  methods: {
    draw() {
      const { xAxis, series, legend } = this.chartData;
      this.chart = echarts.init(document.getElementById(this.id));
      this.chart.resize();
      // 横纵坐标轴样式
      const lineStyle = {
        lineStyle: {
          color: '#c9c9c9',
          width: 1
        }
      };
      //横纵坐标字体样式
      const axisFont = {
        textStyle: { color: '#666666', fontSize: 12 }
      };
      this.chart.setOption(
        {
          // 标题
          title: {
            text: this.title,
            textStyle: {
              color: '#333333',
              fontWeight: 600,
              fontSize: 16,
              fontFamily: 'PingFangSC-Semibold'
            }
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross'
            }
          },
          legend: {
            data: legend,
            itemHeight: 8,
            itemWidth: 8,
            itemGap: 5,
            icon: 'rect',
            x: 'right',
            y: 'top',
            textStyle: {
              color: '#333333',
              fontSize: 10,
              fontFamily: 'PingFangSC-Semibold'
            }
          },
          grid: {
            top: '16%',
            left: '3%',
            right: '1%',
            bottom: '10%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            data: xAxis,
            axisTick: {
              show: false
            },
            axisLine: lineStyle,
            axisLabel: { ...axisFont, margin: 17 }
          },
          yAxis: {
            type: 'value',
            axisTick: {
              show: false
            },
            //网格样式
            splitLine: {
              lineStyle: {
                width: 1,
                color: '#F0F0F0'
              }
            },
            axisLine: lineStyle,
            axisLabel: axisFont
          },
          series: series
        },
        true
      );
    }
  }
};
</script>
