<template>
  <div :id="id" :class="className" :style="{ height: height, width: width }" />
</template>

<script>
import { mapState } from 'vuex';
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/bar';
import 'echarts/lib/component/legend';
import 'echarts/lib/component/title';
import 'echarts/lib/component/grid';
import 'echarts/lib/component/tooltip';
export default {
  name: 'StackBarChart',
  data() {
    return {
      chart: null
    };
  },
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    id: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '400px'
    },
    height: {
      type: String,
      default: '350px'
    },
    chartData: {
      type: Object,
      required: true
    },
    title: {
      type: String
    },
    chartTop: {
      default: '20%'
    },
    showSwitch: String
  },
  watch: {
    chartData(val) {
      if (val) {
        this.draw();
        this.chartResize();
      }
    },
    showSwitch(val) {
      if (val === 'chartShow') {
        this.chartResize();
      }
    },
    // 侧边栏收缩
    miniState(val) {
      this.chartResize();
    }
  },
  computed: {
    ...mapState('menu', {
      miniState: 'miniState'
    })
  },
  methods: {
    draw() {
      const { xAxis, series, legend, tooltip } = this.chartData;
      this.chart = echarts.init(document.getElementById(this.id));

      this.chart.clear();
      // 横纵坐标轴样式
      const lineStyle = {
        lineStyle: {
          color: '#022140',
          width: 1
        }
      };
      //横纵坐标字体样式
      const axisFont = {
        textStyle: { color: '#274484', fontSize: 14 }
      };
      const option = {
        // 颜色组
        color: [
          '#38C699',
          // '#FFC916',
          '#9C86D0',
          '#64BDFD',
          '#FFA10C',
          '#FF6C67 ',
          '#8d98b3'
        ],
        // 标题
        title: {
          text: this.title
        },
        // 图例
        legend: {
          data: legend,
          x: 'right',
          y: 'top'
        },
        // 提示
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        grid: {
          top: this.chartTop,
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          axisTick: {
            show: false
          },
          axisLine: lineStyle,
          axisLabel: axisFont,
          //网格样式
          splitLine: {
            lineStyle: {
              width: 1,
              color: '#F0F0F0'
            }
          }
        },
        yAxis: {
          data: xAxis,
          type: 'category',

          axisTick: {
            show: false
          },
          axisLine: lineStyle,
          axisLabel: axisFont
        },
        series: series
      };

      this.chart.setOption(Object.assign(option, tooltip), true);

      this.chart.off('click');

      this.chart.on('click', param => {
        if (typeof param.name === 'number') {
          return;
        }
        this.$emit('click', {
          name: param.name,
          seriesId: param.seriesId,
          seriesName: param.seriesName
        });
      });
    },
    chartResize() {
      if (!this.chart) {
        return;
      }
      const timer = setInterval(() => {
        this.chart.resize();
        clearInterval(timer);
      }, 100);
    }
  }
};
</script>
