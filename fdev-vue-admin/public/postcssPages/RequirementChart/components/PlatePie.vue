<template>
  <div class="pie-style" ref="PlatePie" />
</template>
<script>
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/pie';
import 'echarts/lib/component/tooltip';
import { mapState } from 'vuex';

export default {
  name: 'PlatePie',
  data() {
    return {
      //当前选中区域
      selectedInd: 0,
      myChart: {}
    };
  },
  props: {
    ids: {
      type: Array
    },
    names: {
      type: Array
    },
    partdemandAmt: {
      type: Array
    }
  },
  mounted() {
    this.myChart = echarts.init(this.$refs.PlatePie);
    this.myChart.setOption(this.option);
    this.myChart.dispatchAction({
      type: 'highlight',
      dataIndex: this.selectedInd
    });
    this.myChart.on('mouseover', params => {
      if (params.dataIndex !== this.selectedInd) {
        this.myChart.dispatchAction({
          type: 'downplay',
          dataIndex: this.selectedInd
        });
        this.selectedInd = params.dataIndex;
        this.myChart.dispatchAction({
          type: 'highlight',
          dataIndex: params.dataIndex
        });
        this.$emit('getGroup', params.data);
      }
    });
  },
  computed: {
    ...mapState('rqrChartForm', ['everyLingshouDemandList']),
    // 饼图展示数据
    option() {
      // numInfo.everyGroupDemandList  // everyLingshouDemandList
      return {
        color: ['#E9F5FF'],
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
              },
              emphasis: {
                // 普通图表的高亮颜色
                color: '#3AA0FF'
              }
            },
            animation: false,
            radius: '40%',
            label: {
              position: 'outter',
              formatter: '{b}',
              fontSize: 8,
              fontWeight: 400,
              fontFamily: 'PingFangSC',
              color: '#979797',
              emphasis: {
                color: '#1D2C4E'
              }
            },
            labelLine: {
              lineStyle: {
                color: 'rgba(0,0,0,0.16)'
              },
              emphasis: {
                lineStyle: {
                  color: ' #1890FF'
                }
              },
              length: 7
            },
            data: [
              {
                name: this.names[0],
                value: this.partdemandAmt[0],
                id: this.ids[0]
              },
              {
                name: this.names[1],
                value: this.partdemandAmt[1],
                id: this.ids[1]
              },
              {
                name: this.names[2],
                value: this.partdemandAmt[2],
                id: this.ids[2]
              },
              {
                name: this.names[3],
                value: this.partdemandAmt[3],
                id: this.ids[3]
              },
              {
                name: this.names[4],
                value: this.partdemandAmt[4],
                id: this.ids[4]
              },
              {
                name: this.names[5],
                value: this.partdemandAmt[5],
                id: this.ids[5]
              },
              {
                name: this.names[6],
                value: this.partdemandAmt[6],
                id: this.ids[6]
              },
              {
                name: this.names[7],
                value: this.partdemandAmt[7],
                id: this.ids[7]
              }
            ]
          }
        ]
      };
    }
  },
  watch: {
    option(val) {
      this.myChart.setOption(val);
    }
  }
};
</script>
<style scoped lang="stylus">
.pie-style {
  width: 360px;
  height: 360px;
}
</style>
