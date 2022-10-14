<template>
  <div class="row justify-center">
    <div ref="countChart" class="pieChart-style"></div>
  </div>
</template>

<script>
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/pie';
import 'echarts/lib/component/title';

export default {
  name: 'CountChart',
  props: {
    countOpt: Array,
    countTotal: Number,
    countTitle: String
  },
  data() {
    return {
      myChart: {}
    };
  },
  computed: {
    //环形图展示数据
    option() {
      return {
        title: {
          top: 'center',
          left: 'center',
          textStyle: {
            rich: {
              total: {
                fontSize: 32,
                color: '#333333',
                fontWeight: 600,
                lineHeight: 32,
                align: 'center'
              },
              title: {
                fontSize: 12,
                color: '#333333',
                fontWeight: 400,
                lineHeight: 12,
                align: 'center'
              }
            }
          },
          text: [
            '{total|' + this.countTotal + '}',
            '{title|' + this.countTitle + '总数}'
          ].join('\n')
        },
        color: ['#F9816E', '#FFB64C', '#5CC49E', '#5FACFB'],
        series: [
          {
            type: 'pie',
            radius: [64, 84],
            animation: false,
            left: 'center',
            label: {
              position: 'outer',
              alignTo: 'none', //标签的对齐方式
              bleedMargin: 5,
              formatter: '{value|{c}}\n{name|{b}}',
              rich: {
                value: {
                  fontSize: 16,
                  color: '#333333',
                  fontWeight: 500,
                  lineHeight: 22,
                  align: 'center'
                },
                name: {
                  fontSize: 12,
                  color: '#333333',
                  fontWeight: 400,
                  lineHeight: 20,
                  align: 'center'
                }
              }
            },
            labelLine: {
              length: '15px'
            },
            data: this.countOpt
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
    this.myChart = echarts.init(this.$refs.countChart);
    this.myChart.setOption(this.option);
  }
};
</script>

<style scoped lang="stylus">
.pieChart-style
  width 320px
  height 248px
</style>
