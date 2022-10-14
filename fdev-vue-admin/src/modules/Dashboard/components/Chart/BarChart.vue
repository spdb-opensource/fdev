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
    }
  },
  watch: {
    chartData(val) {
      if (val) {
        this.draw();
      }
    },
    isMini(val) {
      const timer = setInterval(() => {
        this.draw();
        clearInterval(timer);
      }, 160);
    }
  },
  computed: {
    ...mapState('dashboard', {
      isMini: 'isMini'
    })
  },
  methods: {
    draw() {
      const { legend, xAxis, series, tooltip } = this.chartData;
      this.chart = echarts.init(document.getElementById(this.id), 'macarons');

      this.chart.clear();

      this.chart.resize();

      const option = {
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
          right: 20
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
          }
        },
        yAxis: {
          type: 'value',
          axisTick: {
            show: false
          }
        },
        series: series
      };

      this.chart.setOption(Object.assign(option, tooltip), true);

      this.chart.off('click');

      this.chart.on('click', param => {
        const index = param.name.indexOf('（');
        this.$emit('click', {
          group: index > 0 ? param.name.substring(0, index) : param.name,
          seriesId: param.seriesId,
          name: 'taskNumByGroupDate'
        });
      });
    }
  }
};
</script>
