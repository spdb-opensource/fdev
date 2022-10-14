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
    }
  },
  watch: {
    chartData(val) {
      if (val) {
        this.draw();
        this.chartResize();
      }
    },
    isMini(val) {
      this.chartResize();
    }
  },
  computed: {
    ...mapState('dashboard', {
      isMini: 'isMini'
    })
  },
  methods: {
    draw() {
      const { xAxis, series, legend, tooltip } = this.chartData;
      this.chart = echarts.init(document.getElementById(this.id), 'macarons');

      this.chart.clear();

      const option = {
        title: {
          text: this.title
        },
        legend: {
          data: legend,
          right: 20
        },
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
          }
        },
        yAxis: {
          data: xAxis,
          type: 'category',
          axisLabel: {
            interval: 0
          },
          axisTick: {
            show: false
          }
        },
        series: series
      };

      this.chart.setOption(Object.assign(option, tooltip), true);

      this.chart.off('click');

      this.chart.on('click', param => {
        if (typeof param.name === 'number') {
          return;
        }
        const index = param.name.indexOf('（');
        this.$emit('click', {
          group: index > 0 ? param.name.substring(0, index) : param.name,
          seriesId: param.seriesId,
          name: 'taskNumByGroup'
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
