<template>
  <div>
    <table
      border="1"
      class="table-style"
      bordercolor="#DDDDDD"
      width="100%"
      id="taskGroupDimensionTable"
    >
      <tbody>
        <tr class="th-style">
          <td>板块</td>
          <td>新增业务任务数</td>
          <td>新增科技任务数</td>
          <td v-if="showDaily">新增日常任务数</td>
          <td>投产业务任务数</td>
          <td>投产科技任务数</td>
          <td v-if="showDaily">完成日常任务数</td>
          <td>投产任务总数</td>
          <td>人数</td>
          <td>人均投产业务任务数</td>
          <td>人均投产科技任务数</td>
          <td v-if="showDaily">人均完成日常任务数</td>
          <td>人均投产任务数</td>
        </tr>
        <tr v-for="(item, index) in dataSource" :key="index" class="td-style">
          <td>{{ item.groupName }}</td>
          <td>{{ item.addBusSum }}</td>
          <td>{{ item.addTechSum }}</td>
          <td v-if="showDaily">{{ item.addDailySum }}</td>
          <td>{{ item.proBusSum }}</td>
          <td>{{ item.proTechSum }}</td>
          <td v-if="showDaily">{{ item.proDailySum }}</td>
          <td>{{ item.proSum }}</td>
          <td>{{ item.person }}</td>
          <td>{{ item.perProBusAvg }}</td>
          <td>{{ item.perProTechAvg }}</td>
          <td v-if="showDaily">{{ item.perProDailyAvg }}</td>
          <td>{{ item.perProAvg }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
<script>
import { taskThroughputHeader } from '@/modules/Measure/utils/constants';
export default {
  props: {
    dataSource: Array,
    showDaily: Boolean
  },
  data() {
    return { headerList: taskThroughputHeader };
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
.table-style
  border-collapse:collapse !important
  margin-top:26px;
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
table tr td:first-child{
  width:101px;
  max-width :101px
}
.th-style td
  padding:8px 20px
.td-style td
  padding:12px 20px
</style>
