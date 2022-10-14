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
require('echarts/theme/macarons');
export default {
  name: 'BarChart',
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
    showSwitch: String
  },
  watch: {
    chartData(val) {
      if (val) {
        this.draw();
      }
    },
    showSwitch(val) {
      if (val === 'chartShow') {
        this.chartResize();
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
    }),
    //获取倾斜角度
    getRotation() {
      return this.chartData.xAxis.length > 7 ? 20 : 0;
    }
  },
  methods: {
    draw() {
      const { legend, xAxis, series, tooltip } = this.chartData;
      this.chart = echarts.init(document.getElementById(this.id));
      // 横纵坐标轴样式
      const lineStyle = {
        lineStyle: {
          color: '#022140',
          width: 1
        }
      };
      //横纵坐标字体样式
      const axisFont = {
        textStyle: { color: '#274484', fontSize: 12, fontWeight: 'bold' }
      };
      this.chart.clear();

      this.chart.resize();

      const option = {
        // 颜色组
        color: [
          '#52b052',
          '#38C699',
          '#9C86D0',
          '#64BDFD',
          '#FFA10C',
          '#FF6C67 ',
          '#8d98b3'
        ],
        title: {
          text: this.title
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        legend: {
          data: legend,
          x: 'right',
          y: 'top'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: xAxis,
          axisTick: {
            show: false
          },
          axisLine: lineStyle,
          axisLabel: { ...axisFont, rotate: this.getRotation }
        },
        yAxis: {
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
        series: series
      };

      this.chart.setOption(Object.assign(option, tooltip), true);

      this.chart.off('click');

      this.chart.on('click', param => {
        this.$emit('click', {
          group: param.name,
          seriesName: param.seriesName,
          seriesId: param.seriesId
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
