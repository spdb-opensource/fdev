<template>
  <div class="pie-style" ref="fourGroups" />
</template>
<script>
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/pie';
import { mapState } from 'vuex';

export default {
  name: 'FourGroups',
  mounted() {
    this.myChart = echarts.init(this.$refs.fourGroups);
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
  watch: {
    option(val) {
      this.myChart.setOption(val);
    }
  },
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
    groupdemandAmt: {
      type: Array
    }
  },
  computed: {
    ...mapState('rqrChartForm', [
      'everyGroupDemandList',
      'everyLingshouDemandList'
    ]),
    // 饼图展示数据
    option() {
      // numInfo.everyGroupDemandList  // everyLingshouDemandList
      /* 
        lingshouDemandSum,
        zhifuDemandSum,
        gonggongDemandSum,
        gongsiDemandSum,
        lingshiDemandSum
      */
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
            radius: '55%',
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
              length: 1
            },
            data: [
              {
                name: this.names[0],
                value: this.groupdemandAmt[0],
                id: this.ids[0]
              },
              {
                name: this.names[1],
                value: this.groupdemandAmt[1],
                id: this.ids[1]
              },
              {
                name: this.names[2],
                value: this.groupdemandAmt[2],
                id: this.ids[2]
              },
              {
                name: this.names[3],
                value: this.groupdemandAmt[3],
                id: this.ids[3]
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
.pie-style {
  width: 360px;
  height: 360px;
}
</style>
