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

export default {
  name: 'IssueBarChart',
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

      const { legend, yAxis, chartLeft, chartRight } = this.chartData;

      const series = [...chartLeft, ...chartRight];

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

          grid: [
            {
              show: false,
              left: '2%',
              top: 60,
              bottom: 60,
              containLabel: true,
              width: '44%'
            },
            {
              show: false,
              left: '50.5%',
              top: 80,
              bottom: 60,
              width: '9%'
            },
            {
              show: false,
              right: '2%',
              top: 60,
              bottom: 60,
              containLabel: true,
              width: '44%'
            }
          ],

          xAxis: [
            {
              type: 'value',
              triggerEvent: true,
              inverse: true,
              axisLine: {
                show: false
              },
              axisTick: {
                show: false
              },
              position: 'top',
              axisLabel: {
                show: true,
                textStyle: {
                  fontSize: 12
                }
              }
            },
            {
              gridIndex: 1,
              show: false
            },
            {
              gridIndex: 2,
              type: 'value',
              axisLine: {
                show: false
              },
              axisTick: {
                show: false
              },
              position: 'top',
              axisLabel: {
                show: true,
                textStyle: {
                  fontSize: 12
                }
              }
            }
          ],
          yAxis: [
            {
              type: 'category',
              inverse: true,
              position: 'right',
              axisLine: {
                show: false
              },
              axisTick: {
                show: false
              },
              axisLabel: {
                show: false,
                margin: 8,
                interval: 0,
                textStyle: {
                  fontSize: 12
                }
              },
              data: yAxis
            },
            {
              gridIndex: 1,
              type: 'category',
              inverse: true,
              position: 'left',
              axisLine: {
                show: false
              },
              axisTick: {
                show: false
              },
              axisLabel: {
                show: true,
                interval: 0,
                textStyle: {
                  fontSize: 12
                }
              },
              data: yAxis.map(val => {
                return {
                  value: val,
                  textStyle: {
                    align: 'center'
                  }
                };
              })
            },
            {
              gridIndex: 2,
              type: 'category',
              inverse: true,
              position: 'left',
              axisLine: {
                show: false
              },
              axisTick: {
                show: false
              },
              axisLabel: {
                show: false,
                textStyle: {
                  fontSize: 12
                }
              },
              data: yAxis
            }
          ],
          series: series
        }
      };
      this.chart.setOption(Object.assign(option), true);
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
