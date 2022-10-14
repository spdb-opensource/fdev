<template>
  <div v-if="dataSource" class="groupContent">
    <table
      border="1"
      width="100%"
      class="table-style table-width"
      bordercolor="#DDDDDD"
      id="taskGroupDimensionTable"
    >
      <thead>
        <tr>
          <!-- 需求名 -->
          <td>板块</td>
          <td :colspan="getColNum" v-if="demandType.includes('business')">
            新增业务需求任务数
          </td>
          <td :colspan="getColNum" v-if="demandType.includes('tech')">
            新增科技需求任务数
          </td>
          <td v-if="demandType.includes('daily')">
            新增日常需求任务数
          </td>
          <td :colspan="getColNum" v-if="demandType.includes('business')">
            投产业务需求任务数
          </td>
          <td :colspan="getColNum" v-if="demandType.includes('tech')">
            投产科技需求任务数
          </td>
          <td v-if="demandType.includes('daily')">
            完成日常需求任务数
          </td>
          <td :colspan="getColNum">
            投产任务总数
          </td>
          <td>人数</td>
          <td :colspan="getColNum" v-if="demandType.includes('business')">
            人均投产业务需求任务数
          </td>
          <td :colspan="getColNum" v-if="demandType.includes('tech')">
            人均投产科技需求任务数
          </td>
          <td v-if="demandType.includes('daily')">
            人均完成日常需求任务数
          </td>
          <td :colspan="getColNum">
            人均投产任务数
          </td>
        </tr>
        <tr v-if="taskType.length > 0">
          <td></td>
          <!-- 循环获取三列 -->
          <!-- 新增任务数 -->
          <td v-if="demandType.includes('business') && taskType.includes(0)">
            开发任务
          </td>
          <td v-if="demandType.includes('business') && taskType.includes(1)">
            无代码任务
          </td>
          <td v-if="demandType.includes('business') && taskType.includes(2)">
            日常任务
          </td>
          <td v-if="demandType.includes('tech') && taskType.includes(0)">
            开发任务
          </td>
          <td v-if="demandType.includes('tech') && taskType.includes(1)">
            无代码任务
          </td>
          <td v-if="demandType.includes('tech') && taskType.includes(2)">
            日常任务
          </td>
          <td v-if="demandType.includes('daily')">日常任务</td>
          <!-- 投产任务数 -->
          <td v-if="demandType.includes('business') && taskType.includes(0)">
            开发任务
          </td>
          <td v-if="demandType.includes('business') && taskType.includes(1)">
            无代码任务
          </td>
          <td v-if="demandType.includes('business') && taskType.includes(2)">
            日常任务
          </td>
          <td v-if="demandType.includes('tech') && taskType.includes(0)">
            开发任务
          </td>
          <td v-if="demandType.includes('tech') && taskType.includes(1)">
            无代码任务
          </td>
          <td v-if="demandType.includes('tech') && taskType.includes(2)">
            日常任务
          </td>
          <td v-if="demandType.includes('daily')">日常任务</td>
          <!--投产任务总数  -->
          <td v-if="taskType.includes(0)">
            开发任务
          </td>
          <td v-if="taskType.includes(1)">
            无代码任务
          </td>
          <td v-if="taskType.includes(2)">
            日常任务
          </td>
          <!--人数  -->
          <td></td>
          <!-- 人均投产任务数 -->
          <td v-if="demandType.includes('business') && taskType.includes(0)">
            开发任务
          </td>
          <td v-if="demandType.includes('business') && taskType.includes(1)">
            无代码任务
          </td>
          <td v-if="demandType.includes('business') && taskType.includes(2)">
            日常任务
          </td>
          <td v-if="demandType.includes('tech') && taskType.includes(0)">
            开发任务
          </td>
          <td v-if="demandType.includes('tech') && taskType.includes(1)">
            无代码任务
          </td>
          <td v-if="demandType.includes('tech') && taskType.includes(2)">
            日常任务
          </td>
          <td v-if="demandType.includes('daily')">日常任务</td>
          <!-- 人均投产任务总数 -->
          <td v-if="taskType.includes(0)">
            开发任务
          </td>
          <td v-if="taskType.includes(1)">
            无代码任务
          </td>
          <td v-if="taskType.includes(2)">
            日常任务
          </td>
        </tr>
      </thead>
      <!--具体的数据值-->
      <tr v-for="(item, index) in datas" :key="index">
        <td class="text-center">
          {{ item.groupName }}
        </td>
        <!-- 新增任务数 -->
        <td v-if="demandType.includes('business') && taskType.includes(0)">
          {{ item.addBusSum.devTaskSum }}
        </td>
        <td v-if="demandType.includes('business') && taskType.includes(1)">
          {{ item.addBusSum.noCodeTaskSum }}
        </td>
        <td v-if="demandType.includes('business') && taskType.includes(2)">
          {{ item.addBusSum.dailyTaskSum }}
        </td>
        <td v-if="taskType.length == 0 && item.addBusSum">
          {{ item.addBusSum.sum }}
        </td>
        <td v-if="demandType.includes('tech') && taskType.includes(0)">
          {{ item.addTechSum.devTaskSum }}
        </td>
        <td v-if="demandType.includes('tech') && taskType.includes(1)">
          {{ item.addTechSum.noCodeTaskSum }}
        </td>
        <td v-if="demandType.includes('tech') && taskType.includes(2)">
          {{ item.addTechSum.dailyTaskSum }}
        </td>
        <td v-if="taskType.length == 0 && item.addTechSum">
          {{ item.addTechSum.sum }}
        </td>
        <td v-if="demandType.includes('daily') && taskType.length">
          {{ item.addDailySum.dailyTaskSum }}
        </td>
        <td v-if="taskType.length == 0 && item.addDailySum">
          {{ item.addDailySum.sum }}
        </td>
        <!-- 投产任务数 -->
        <td v-if="demandType.includes('business') && taskType.includes(0)">
          {{ item.proBusSum.devTaskSum }}
        </td>
        <td v-if="demandType.includes('business') && taskType.includes(1)">
          {{ item.proBusSum.noCodeTaskSum }}
        </td>
        <td v-if="demandType.includes('business') && taskType.includes(2)">
          {{ item.proBusSum.dailyTaskSum }}
        </td>
        <td v-if="taskType.length == 0 && item.proBusSum">
          {{ item.proBusSum.sum }}
        </td>
        <td v-if="demandType.includes('tech') && taskType.includes(0)">
          {{ item.proTechSum.devTaskSum }}
        </td>
        <td v-if="demandType.includes('tech') && taskType.includes(1)">
          {{ item.proTechSum.noCodeTaskSum }}
        </td>
        <td v-if="demandType.includes('tech') && taskType.includes(2)">
          {{ item.proTechSum.dailyTaskSum }}
        </td>
        <td v-if="!taskType.length && item.proTechSum">
          {{ item.proTechSum.sum }}
        </td>
        <td v-if="demandType.includes('daily') && taskType.length">
          {{ item.proDailySum.dailyTaskSum }}
        </td>
        <td v-if="!taskType.length && item.proDailySum">
          {{ item.proDailySum.sum }}
        </td>
        <!-- 投产任务总数 -->
        <td v-if="taskType.includes(0)">
          {{ item.proSum.devTaskSum }}
        </td>
        <td v-if="taskType.includes(1)">
          {{ item.proSum.noCodeTaskSum }}
        </td>
        <td v-if="taskType.includes(2)">
          {{ item.proSum.dailyTaskSum }}
        </td>
        <td v-if="!taskType.length && item.proSum">
          {{ item.proSum.sum }}
        </td>
        <!--人数  -->
        <td>
          {{ item.person }}
        </td>
        <!-- 人均投产任务数7 -->
        <td v-if="demandType.includes('business') && taskType.includes(0)">
          {{ item.perProBusAvg.devTaskAvg }}
        </td>
        <td v-if="demandType.includes('business') && taskType.includes(1)">
          {{ item.perProBusAvg.noCodeTaskAvg }}
        </td>
        <td v-if="demandType.includes('business') && taskType.includes(2)">
          {{ item.perProBusAvg.dailyTaskAvg }}
        </td>
        <td v-if="!taskType.length && item.perProBusAvg">
          {{ item.perProBusAvg.sum }}
        </td>
        <td v-if="demandType.includes('tech') && taskType.includes(0)">
          {{ item.perProTechAvg.devTaskAvg }}
        </td>
        <td v-if="demandType.includes('tech') && taskType.includes(1)">
          {{ item.perProTechAvg.noCodeTaskAvg }}
        </td>
        <td v-if="demandType.includes('tech') && taskType.includes(2)">
          {{ item.perProTechAvg.dailyTaskAvg }}
        </td>
        <td v-if="!taskType.length && item.perProTechAvg">
          <!-- 后端修改后改字段 -->
          {{ item.perProTechAvg.sum }}
        </td>
        <td v-if="demandType.includes('daily') && taskType.length">
          {{ item.perProDailyAvg.dailyTaskAvg }}
        </td>
        <td v-if="!taskType.length && item.perProDailyAvg">
          <!-- 后端修改后改字段 -->
          {{ item.perProDailyAvg.sum }}
        </td>
        <!-- 人均投产任务总数 -->
        <td v-if="taskType.includes(0)">
          {{ item.perProAvg.devTaskAvg }}
        </td>
        <td v-if="taskType.includes(1)">
          {{ item.perProAvg.noCodeTaskAvg }}
        </td>
        <td v-if="taskType.includes(2)">
          {{ item.perProAvg.dailyTaskAvg }}
        </td>
        <td v-if="!taskType.length && item.perProAvg">
          {{ item.perProAvg.sum }}
        </td>
      </tr>
      <!--总数-->
      <tr>
        <td class="text-center">
          {{ totalData.groupName }}
        </td>
        <td :colspan="getColNum" v-if="demandType.includes('business')">
          {{ totalData.addBusSum }}
        </td>
        <td :colspan="getColNum" v-if="demandType.includes('tech')">
          {{ totalData.addTechSum }}
        </td>
        <td v-if="demandType.includes('daily')">
          {{ totalData.addDailySum }}
        </td>
        <td :colspan="getColNum" v-if="demandType.includes('business')">
          {{ totalData.proBusSum }}
        </td>
        <td :colspan="getColNum" v-if="demandType.includes('tech')">
          {{ totalData.proTechSum }}
        </td>
        <td v-if="demandType.includes('daily')">
          {{ totalData.proDailySum }}
        </td>
        <td :colspan="getColNum">
          {{ totalData.proSum }}
        </td>
        <td>
          {{ totalData.person }}
        </td>
        <td :colspan="getColNum" v-if="demandType.includes('business')">
          {{ totalData.perProBusAvg }}
        </td>
        <td :colspan="getColNum" v-if="demandType.includes('tech')">
          {{ totalData.perProTechAvg }}
        </td>
        <td v-if="demandType.includes('daily')">
          {{ totalData.perProDailyAvg }}
        </td>
        <td :colspan="getColNum">
          {{ totalData.perProAvg }}
        </td>
      </tr>
    </table>
  </div>
  <!-- 无数据时 -->
  <div v-else class="column items-center">
    <f-image name="no_data" class="q-mt-xl" />
  </div>
</template>
<script>
export default {
  props: {
    dataSource: Object,
    taskType: Array,
    demandType: Array
  },
  data() {
    return {};
  },
  computed: {
    getColNum() {
      return this.taskType.length > 0 ? this.taskType.length : 1;
    },
    datas() {
      return this.dataSource.datas;
    },
    totalData() {
      return this.dataSource.totalData;
    }
  },
  methods: {
    // 导出方法
    childExport() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_html_table(
          'taskGroupDimensionTable',
          '板块维度任务吞吐量'
        );
      });
    }
  }
};
</script>
<style lang="stylus" scoped>
.groupContent
  overflow-x: scroll;
  .table-style {
    border-collapse:collapse !important;
    margin-top:26px;
    font-family: PingFangSC-Regular;
    font-size: 14px;
    color: #333333;
    letter-spacing: 0;
    line-height: 14px;
  }
  .table-width
    width 130%
  tr
    td, th
      text-align center
      padding 5px
      min-width: 86px;
</style>
