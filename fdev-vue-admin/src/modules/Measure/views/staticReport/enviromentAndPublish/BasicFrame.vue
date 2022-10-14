<template>
  <div>
    <Loading :visible="loading">
      <!-- 基础架构类型总数 -->
      <f-block block class="q-pt-llg">
        <div class="row no-wrap justify-between gap">
          <!-- 每个卡片 -->
          <div
            v-for="(value, key) in frameTotalNum"
            :key="key"
            :class="key"
            class="card-style"
          >
            <div class="q-py-md">
              <!-- 总数 -->
              <div class="number-style">{{ value }}</div>
              <!-- 类别 -->
              <div class="type-style">{{ frameType[key] }}</div>
            </div>
          </div>
        </div>
      </f-block>
      <!-- 表格、折线图 -->
      <div class="row no-wrap toTop">
        <!-- 表格展示 -->
        <f-block block class="col-7 toleft">
          <Loading :visible="verNumLoading">
            <div class="row no-wrap">
              <f-formitem
                page
                label="开始日期"
                label-style="width:56px;margin-right:32px"
                value-style="width:144px"
              >
                <f-date
                  :value="time.start_date"
                  @input="updateTimeStartDate($event)"
                  :options="startTimeOptions"
                  :rules="[
                    () =>
                      (time.end_date ? !!time.start_date : true) ||
                      '请选择开始日期'
                  ]"
                />
              </f-formitem>
              <f-formitem
                page
                label="至"
                label-style="width:14px;margin-right:8px"
                value-style="width:144px"
                class="q-ml-sm"
              >
                <f-date
                  :value="time.end_date"
                  @input="updateTimeEndDate($event)"
                  :options="endTimeOptions"
                  :rules="[
                    val => (time.start_date ? !!val : true) || '请选择结束日期'
                  ]"
                />
              </f-formitem>
              <fdev-space />
              <fdev-btn
                class="q-ml-lg"
                label="查询"
                ficon="search"
                dialog
                @click="queryVersionData"
                v-forbidMultipleClick
              />
            </div>
            <fdev-markup-table flat class="scroll">
              <tbody>
                <tr class="thead-style">
                  <td>维度</td>
                  <td>测试版本发布数</td>
                  <td>正式版本发布数</td>
                  <td>已发布版本总数</td>
                </tr>
                <tr v-for="(item, index) in typeList" :key="index">
                  <td>{{ frameType[item] }}</td>
                  <td>{{ frameIssueList[item].alpha }}</td>
                  <td>{{ frameIssueList[item].release }}</td>
                  <td>{{ frameIssueList[item].total }}</td>
                </tr>
              </tbody>
            </fdev-markup-table>
          </Loading>
        </f-block>
        <!-- 近六周数量变化趋势 -->
        <f-block block class="col">
          <LineChart
            width="100%"
            :chart-data="numLineData"
            title="近六周数量变化趋势"
          />
        </f-block>
      </div>
    </Loading>
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import moment from 'moment';
import LineChart from '@/modules/Measure/components/recentTrendLine';

export default {
  name: 'Analysis',
  data() {
    return {
      loading: false,
      verNumLoading: false,
      date: '',
      frameType: {
        archetypes: '后端骨架',
        baseImage: '基础镜像',
        components: '后端组件',
        mpassArchetypes: '前端骨架',
        mpassComponents: '前端组件'
      },
      typeList: [],
      numChartData: {},
      numLineData: {},
      typeColor: {
        archetypes: '#5574E9',
        baseImage: '#C99CFB',
        components: '#3CADFF',
        mpassArchetypes: '#F1A289',
        mpassComponents: '#47D0C2'
      },
      frameTotalNum: {} //基础架构总数
    };
  },
  components: {
    Loading,
    LineChart
  },
  computed: {
    ...mapState('userActionSaveMeasure/basicFrameAnalysis', ['time']),
    ...mapState('measureForm', [
      'frameNumData',
      'frameReleaseData',
      'frameIssueList'
    ])
  },
  methods: {
    ...mapMutations('userActionSaveMeasure/basicFrameAnalysis', [
      'updateTimeStartDate',
      'updateTimeEndDate'
    ]),
    ...mapActions('measureForm', [
      'queryNumByType',
      'queryDataByType',
      'queryIssueData'
    ]),
    // 维度数量
    async queryVersionData() {
      this.verNumLoading = true;
      await this.queryIssueData({
        start_date: this.time.start_date,
        end_date: this.time.end_date
          ? this.time.end_date
          : moment(new Date()).format('YYYY-MM-DD')
      });
      this.typeList = Object.keys(this.frameIssueList);
      this.verNumLoading = false;
    },
    // 开始时间控制
    startTimeOptions(date) {
      if (this.time.end_date) {
        return date < this.time.end_date.replace(/-/g, '/');
      }
      return true;
    },
    // 结束时间控制
    endTimeOptions(date) {
      this.time.start_date = this.time.start_date ? this.time.start_date : '';
      return date > this.time.start_date.replace(/-/g, '/');
    },
    //变化趋势折线图
    async numLineChartinit() {
      await this.queryDataByType();
      const keyList = Object.keys(this.frameReleaseData);
      const timeData = Object.keys(this.frameReleaseData[keyList[0]]);
      //横坐标日期排序
      const xAxis = timeData.sort((a, b) => {
        return a > b ? 1 : -1;
      });
      // 图例
      let legend = [];
      keyList.map(item => {
        legend.push(this.frameType[item]);
        // legend回车显示
        if (legend.length === 3) {
          legend.push('');
        }
      });
      // 数据
      const series = keyList.map(item => {
        const data = xAxis.map(val => {
          return this.frameReleaseData[item][val];
        });
        return {
          name: this.frameType[item],
          type: 'line',
          data: data,
          itemStyle: {
            normal: {
              color: this.typeColor[item],
              lineStyle: {
                width: 2
              }
            }
          },
          symbol: 'circle',
          symbolSize: 4,
          animationDuration: 2800,
          animationEasing: 'quadraticOut',
          smooth: true
        };
      });
      this.numLineData = { xAxis, series, legend };
    }
  },
  async created() {
    try {
      this.loading = true;
      // 基础架构类型总数
      await this.queryNumByType();
      this.frameTotalNum = this.frameNumData[0];
      //维度数量
      this.queryVersionData();
      // 近六周变化趋势
      this.numLineChartinit();
      this.loading = false;
    } catch (e) {
      this.loading = false;
    }
  }
};
</script>

<style lang="stylus" scoped>
.toTop
   margin-top:10px
   height:450px
.toleft
   margin-right:10px
.card-style
   width 172px
   height 90px
.number-style
   font-family: PingFangSC-Semibold;
   font-size: 32px;
   color: #FFFFFF;
   letter-spacing: 0;
   text-align: center;
   line-height: 32px;
.type-style
   font-family: PingFangSC-Semibold;
   font-size: 14px;
   color: #FFFFFF;
   letter-spacing: 0;
   text-align: center;
   line-height: 14px;
   margin-top:12px
.thead-style
   background: #F4F6FD;
.components
  background-image url('../../../assets/componentsCard.svg')
.archetypes
  background-image url('../../../assets/archetypesCard.svg')
.baseImage
  background-image url('../../../assets/baseImageCard.svg')
.mpassArchetypes
  background-image url('../../../assets/mpassArchetypesCard.svg')
.mpassComponents
  background-image url('../../../assets/mpassComponentsCard.svg')
.scroll::-webkit-scrollbar {
    /*滚动条整体样式*/
    width: 8px !important;
    /*高宽分别对应横竖滚动条的尺寸*/
    height: 8px !important;
    background: #ffffff !important;
    cursor: pointer;
}

 .scroll::-webkit-scrollbar-thumb {
    /*滚动条里面小方块*/
    border-radius: 4px !important;
    box-shadow: inset 0 0 5px rgba(240, 240, 240, 0.5) !important;
    background: rgba(176,190,197,.5) !important;
    cursor: pointer !important;
}

 .scroll::-webkit-scrollbar-track {
    /*滚动条里面轨道*/
    box-shadow: inset 0 0 5px hsla(0,0%,96.9%,.3) !important;
    border-radius: 4 !important;
    background: rgba(240, 240, 240, 0.5) !important;
    cursor: pointer !important;
}
.gap
   padding-left:32px;
   padding-right:32px
</style>
