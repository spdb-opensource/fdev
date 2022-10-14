<template>
  <div :id="id" :class="className" :style="{ height: height, width: width }" />
</template>

<script>
import { mapState } from 'vuex';
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/line';
import 'echarts/lib/component/legend';
import 'echarts/lib/component/title';
import 'echarts/lib/component/grid';
import 'echarts/lib/component/tooltip';
require('echarts/theme/macarons');
export default {
  name: 'LineChart',
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
      default: '100%'
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
      default: '15%'
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
      const { xAxis, series } = this.chartData;
      this.chart = echarts.init(document.getElementById(this.id), 'macarons');

      this.chart.resize();

      this.chart.setOption(
        {
          title: {
            text: this.title
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross'
            }
          },
          grid: {
            top: this.chartTop,
            left: '5%',
            right: '6%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
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
        },
        true
      );
    }
  }
};
</script>
