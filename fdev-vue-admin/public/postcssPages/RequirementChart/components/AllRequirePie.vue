<template>
  <div ref="allRequire" class="all-require-pie"></div>
</template>
<script>
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/pie';
import 'echarts/lib/component/tooltip';

export default {
  name: 'AllRequirePie',
  props: {
    AllRequiredata: Array
  },
  data() {
    return {
      myChart: {}
    };
  },
  computed: {
    //饼图数据
    option() {
      return {
        color: ['#3AA0FF', '#FAD236'],
        tooltip: {
          trigger: 'item',
          axisPointer: {
            type: 'shadow'
          },
          formatter: '{d}%'
        },
        series: [
          {
            itemStyle: {
              normal: {
                borderWidth: 2,
                borderColor: '#ffffff'
              }
            },
            type: 'pie',
            radius: '100%',
            animation: false,
            label: {
              position: 'outer',
              alignTo: 'none',
              bleedMargin: 5,
              formatter: '{c}',
              color: '#545454',
              fontSize: 12
            },
            labelLine: {
              length: '7px'
            },
            data: this.AllRequiredata
          }
        ]
      };
    }
  },
  watch: {
    option(val) {
      this.myChart.setOption(val);
    }
  },
  mounted() {
    this.myChart = echarts.init(this.$refs.allRequire);
    this.myChart.setOption(this.option);
  }
};
</script>
<style scoped lang="stylus">
.all-require-pie
  width 275px
  height 191px
  margin-left 159px
  margin-top 21px
</style>
