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
  name: 'LineG',
  mixins: [UI_TEMP_MIXIN],
  methods: {
    draw() {
      const seriesItem = {
        type: 'line',
        lineStyle: {
          width: 1.2
        }
      };
      // 折线图样式
      const LineItemStyle = { color: '#5FACFB' };
      // 折线图阴影样式
      const shadow = {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(95,172,251,0.30)' },
            { offset: 1, color: 'rgba(95,172,251,0.00)' }
          ],
          global: false
        }
      };
      let options = {
        xAxis: {
          type: 'category',
          data: this.dataSource.data.map(item => {
            return item.x;
          }),
          axisLine: lineStyle,
          axisLabel: axisFont,
          axisTick: { show: false }
        },
        yAxis: {
          type: 'value',
          axisLabel: axisFont,
          axisLine: lineStyle,
          axisTick: { show: false },
          splitLine: splitLineStyle
        },
        grid: {
          top: '15%',
          left: '3%',
          right: '3%',
          bottom: '10%',
          containLabel: true
        },
        series: [
          {
            data: this.dataSource.data.map(item => {
              return item.y;
            }),
            name: this.dataSource.tooltip,
            ...seriesItem,
            label: {
              show: true,
              textStyle: { color: '#666666', fontSize: 12 }
            },
            itemStyle: LineItemStyle,
            areaStyle: shadow,
            symbol: 'circle',
            symbolSize: 4
          }
        ],
        // 提示信息
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        // 图例
        legend: {
          data: [this.dataSource.tooltip],
          x: 'right',
          y: 'top',
          icon: 'circle',
          itemHeight: 8,
          itemWidth: 8
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
