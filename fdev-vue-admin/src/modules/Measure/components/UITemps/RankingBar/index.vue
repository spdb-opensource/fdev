<template>
  <div ref="container" id="container" />
</template>

<script>
import { splitLineStyle, axisFont } from '@/modules/Measure/utils/constants';
import { UI_TEMP_MIXIN } from '../../../utils/mixin';
export default {
  name: 'RankingBar',
  mixins: [UI_TEMP_MIXIN],
  methods: {
    draw() {
      const seriesItem = {
        type: 'bar',
        lineStyle: {
          width: 1
        }
      };
      // label样式
      const labelStyle = {
        show: true,
        position: 'right',
        textStyle: { color: '#666666', fontSize: 12 }
      };
      // y轴样式
      const yAxisStyle = {
        show: true,
        lineStyle: {
          color: '#F0F0F0',
          width: 1.27
        }
      };
      // x轴样式
      const xAxisStyle = {
        show: true,
        lineStyle: {
          color: '#F0F0F0',
          width: 1.27
        }
      };
      let options = {
        xAxis: {
          type: 'value',
          splitLine: splitLineStyle,
          axisLine: xAxisStyle,
          axisLabel: axisFont,
          axisTick: { show: false }
        },
        yAxis: {
          type: 'category',
          data: this.dataSource.data
            .map(item => {
              return item.type;
            })
            .reverse(),
          axisTick: { show: false },
          axisLine: yAxisStyle,
          axisLabel: axisFont
        },
        grid: {
          top: '8%',
          left: '3%',
          right: '8%',
          bottom: '10%',
          containLabel: true
        },
        series: [
          {
            data: this.dataSource.data
              .map(item => {
                return item.value;
              })
              .reverse(),
            ...seriesItem,
            itemStyle: {
              color: '#5FACFB'
            },
            label: {
              ...labelStyle,
              formatter: this.dataSource.percent ? '{c}%' : '{c}'
            }
          }
        ],
        // 提示信息
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
            shadowStyle: {
              opacity: 0.8
            }
          }
        }
      };
      this.chart.setOption(Object.assign(options), true);
    }
  },
  mounted() {
    this.draw();
  },
  created() {}
};
</script>

<style scoped></style>
