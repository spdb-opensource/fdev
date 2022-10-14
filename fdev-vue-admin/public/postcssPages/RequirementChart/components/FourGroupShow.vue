<template>
  <div ref="fourGroupShow" class="pie-style" />
</template>
<script>
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/pie';
import { mapState } from 'vuex';
export default {
  name: 'FourGroupShow',
  mounted() {
    this.myChart = echarts.init(this.$refs.fourGroupShow);
    this.myChart.setOption(this.option);
  },
  data() {
    return {
      myChart: {}
    };
  },
  props: {
    groupData: {
      type: Array,
      default: () => []
    }
  },
  watch: {
    option(newval) {
      this.myChart.setOption(newval);
    }
  },
  computed: {
    ...mapState('rqrChartForm', ['everyGroupList']),
    option() {
      return {
        color: ['#3AA0FF', '#FFCA4B', '#FFA26C', '#5C93FF', '#B37FEB'],
        tooltip: {
          trigger: 'item',
          axisPointer: {
            type: 'shadow'
          },
          formatter: '{d}%'
        },
        series: [
          {
            type: 'pie',
            itemStyle: {
              normal: {
                borderWidth: 1,
                borderColor: '#ffffff'
              }
            },
            animation: false,
            radius: '65%',
            label: {
              position: 'outter',
              formatter: '{b}',
              fontSize: 9,
              fontWeight: 400,
              fontFamily: 'PingFangSC',
              color: '#979797'
            },
            labelLine: {
              lineStyle: {
                color: 'rgba(0,0,0,0.16)'
              },
              length: 1
            },
            data: [
              {
                name: '零售金融组',
                value: this.groupData[0]
              },
              {
                name: '支付组',
                value: this.groupData[1]
              },
              {
                name: '公共组',
                value: this.groupData[2]
              },
              {
                name: '公司金融组',
                value: this.groupData[3]
              }
            ]
          }
        ]
      };
    }
  }
};
</script>
<style scoped lang="stylus">
.pie-style
  width 266px
  height 266px
</style>
