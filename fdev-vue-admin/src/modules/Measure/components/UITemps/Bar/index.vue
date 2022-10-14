<template>
  <div ref="container" id="container" />
</template>
<script>
import { UI_TEMP_MIXIN } from '../../../utils/mixin';
import {
  lineStyle,
  axisFont,
  splitLineStyle
} from '@/modules/Measure/utils/constants';
export default {
  name: 'Bar',
  mixins: [UI_TEMP_MIXIN],
  methods: {
    draw() {
      const seriesItem = {
        type: 'bar',
        lineStyle: {
          width: 1
        }
      };
      // 柱状图样式
      const barItemStyle = {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(95,172,251,0.5)' },
            { offset: 1, color: 'rgba(95,172,251,1)' }
          ],
          global: false
        }
      };
      // tooltip样式
      const toolTipStyle = {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
          shadowStyle: {
            opacity: 0.8
          }
        }
      };
      let options = {
        xAxis: [
          {
            type: 'category',
            data: this.dataSource.data.map(item => {
              return item.x;
            }),
            axisLine: lineStyle,
            axisLabel: axisFont,
            axisTick: { alignWithLabel: true, inside: true }
          }
        ],
        yAxis: [
          {
            type: 'value',
            axisLine: lineStyle,
            axisLabel: axisFont,
            axisTick: { show: false },
            splitLine: splitLineStyle
          }
        ],
        series: [
          {
            name: this.dataSource.tooltip,
            data: this.dataSource.data.map(item => {
              return item.y;
            }),
            ...seriesItem,
            itemStyle: barItemStyle,
            label: {
              show: true,
              position: 'top',
              textStyle: { fontSize: 12, color: '#666666' }
            }
          }
        ],
        // 提示信息
        tooltip: toolTipStyle,
        grid: {
          top: '15%',
          left: '3%',
          right: '3%',
          bottom: '10%',
          containLabel: true
        },
        // 图例
        legend: {
          data: [this.dataSource.tooltip],
          x: 'right',
          y: 'top',
          icon: 'rect',
          itemHeight: 8,
          itemWidth: 8
        }
      };
      this.chart.setOption(
        Object.assign(options, this.dataSource.tooltip),
        true
      );
    }
  },
  mounted() {
    this.draw();
  },
  created() {}
};
</script>

<style scoped></style>
