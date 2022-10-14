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
import 'echarts/lib/component/toolbox';
import 'echarts/lib/component/timeline';
require('echarts/theme/macarons');
import { nameGroup, nameKey } from '@/modules/Dashboard/utils/constants';

export default {
  name: 'DoubleBarChart',
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
      this.draw();
      this.chartResize();
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
      this.chart = echarts.init(document.getElementById(this.id), 'macarons');

      this.chart.clear();

      const { xAxis, xAxisId, left } = this.chartData;

      const seriesNames = [
        'create',
        'todo',
        'develop',
        'sit',
        'uat',
        'rel',
        'production'
      ];

      const legend = seriesNames.map(item => {
        return nameGroup[item];
      });

      const series = seriesNames.map((key, i) => {
        const data = xAxisId.map(id => {
          return left[id][key];
        });

        return {
          name: nameGroup[key],
          id: nameKey[key],
          type: 'bar',
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
          },
          data: data
        };
      });

      const option = {
        baseOption: {
          title: {
            textStyle: {
              fontSize: 16
            }
          },
          legend: {
            data: legend,
            top: 8,
            right: '1%'
          },
          tooltip: {
            show: true,
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
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
        }
      };

      this.chart.setOption(Object.assign(option), true);

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
    },
    changeUser() {
      this.draw();
      this.chartResize();
    }
  }
};
</script>
