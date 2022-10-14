<template>
  <table
    width="100%"
    border="1"
    bordercolor="#DDDDDD"
    class="table-style table-width"
    id="partisTable"
    v-if="dataSource.length !== 0"
  >
    <thead>
      <tr>
        <td rowspan="3">组别</td>
        <td rowspan="2" colspan="2">待实施(排队)</td>
        <td colspan="6">测试阶段</td>
        <td colspan="2" rowspan="2">开发阶段(牵头)</td>
        <td colspan="2" rowspan="2">开发阶段(参与)</td>
        <td colspan="2" rowspan="2">暂缓</td>
        <td rowspan="3">业务需求人均负荷</td>
        <td rowspan="3">开发总人均负荷</td>
        <td rowspan="2" colspan="2">已投产</td>
        <td rowspan="2" colspan="3">合计</td>
      </tr>
      <tr>
        <td colspan="2">SIT</td>
        <td colspan="2">UAT</td>
        <td colspan="2">REL</td>
      </tr>
      <tr>
        <td>业务</td>
        <td>科技</td>
        <td>业务</td>
        <td>科技</td>
        <td>业务</td>
        <td>科技</td>
        <td>业务</td>
        <td>科技</td>
        <td>业务</td>
        <td>科技</td>
        <td>业务</td>
        <td>科技</td>
        <td>业务</td>
        <td>科技</td>
        <td>业务</td>
        <td>科技</td>
        <td>业务</td>
        <td>科技</td>
        <td>总合计</td>
      </tr>
    </thead>
    <!--具体的数据值-->
    <tr v-for="(item, index) in dataSource" :key="index">
      <!-- 组名 -->
      <td class="text-center">
        {{ item.groupName }}
      </td>
      <!-- 待实施 -->
      <td class="text-center">{{ item.dssyw }}</td>
      <td class="text-center">{{ item.dsskj }}</td>
      <!-- SIT -->
      <td class="text-center">{{ item.cssityw }}</td>
      <td class="text-center">{{ item.cssitkj }}</td>
      <!-- uat -->
      <td class="text-center">{{ item.csuatyw }}</td>
      <td class="text-center">{{ item.csuatkj }}</td>
      <!-- REL -->
      <td class="text-center">{{ item.csrelyw }}</td>
      <td class="text-center">{{ item.csrelkj }}</td>
      <!-- 开发阶段（牵头） -->
      <td class="text-center">{{ item.kaifayw }}</td>
      <td class="text-center">{{ item.kaifakj }}</td>
      <!-- 开发阶段（参与） -->
      <td class="text-center">{{ item.nogdevelopyw }}</td>
      <td class="text-center">{{ item.nogdevelopkj }}</td>
      <!-- 暂缓 -->
      <td class="text-center">{{ item.waityw }}</td>
      <td class="text-center">{{ item.waitkj }}</td>
      <!-- 业务需求人均 -->
      <td class="text-center">{{ item.ywxqrjfh }}</td>
      <!-- 开发总人均 -->
      <td class="text-center">{{ item.kaifarjfh }}</td>
      <!-- 已投产 -->
      <td class="text-center">{{ item.csproyw }}</td>
      <td class="text-center">{{ item.csprokj }}</td>
      <!-- 合计 -->
      <td class="text-center">{{ item.totalyw }}</td>
      <td class="text-center">{{ item.totalkj }}</td>
      <td class="text-center">
        {{ item.totalyw + item.totalkj }}
      </td>
    </tr>
  </table>

  <!-- 无数据时 -->
  <div v-else class="column items-center">
    <f-image name="no_data" class="q-mt-xl" />
  </div>
</template>
<script>
export default {
  props: {
    dataSource: Array
  },
  data() {
    return {};
  },
  methods: {
    // 导出方法
    childExport() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_html_table('partisTable', '各组对应阶段实施需求数量统计');
      });
    }
  },
  created() {}
};
</script>
<style lang="stylus" scoped>
.table-style
  border-collapse:collapse !important
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 24px;
.table-width
  width 130%
tr
  td, th
    text-align center
    padding 5px
</style>
