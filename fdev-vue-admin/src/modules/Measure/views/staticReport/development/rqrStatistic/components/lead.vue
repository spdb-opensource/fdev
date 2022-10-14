<template>
  <table
    width="100%"
    border="1"
    bordercolor="#DDDDDD"
    class="table-style table-width"
    id="leadTable"
    v-if="dataSource.length !== 0"
  >
    <thead>
      <tr>
        <td rowspan="3">组别</td>
        <td colspan="6">需求阶段</td>
        <td rowspan="2" colspan="6">待实施(排队)</td>
        <td rowspan="2" colspan="3">开发阶段(牵头)</td>
        <td rowspan="2" colspan="3">开发阶段(参与)</td>
        <td colspan="9">测试阶段</td>
        <td rowspan="2" colspan="3">暂缓</td>
        <td rowspan="3">业务需求人均负荷</td>
        <td rowspan="3">开发总人均负荷</td>
        <td rowspan="2" colspan="3">已投产</td>
        <td rowspan="2" colspan="4">合计</td>
      </tr>
      <tr>
        <td colspan="3">需求预评估</td>
        <td colspan="3">需求评估</td>
        <td colspan="3">SIT</td>
        <td colspan="3">UAT</td>
        <td colspan="3">REL</td>
      </tr>
      <tr>
        <td>业务</td>
        <td>科技</td>
        <td>日常</td>
        <td>业务</td>
        <td>科技</td>
        <td>日常</td>
        <td>业务</td>
        <td>排队</td>
        <td>科技</td>
        <td>排队</td>
        <td>日常</td>
        <td>排队</td>
        <td>业务</td>
        <td>科技</td>
        <td>日常</td>
        <td>业务</td>
        <td>科技</td>
        <td>日常</td>
        <td>业务</td>
        <td>科技</td>
        <td>日常</td>
        <td>业务</td>
        <td>科技</td>
        <td>日常</td>
        <td>业务</td>
        <td>科技</td>
        <td>日常</td>
        <td>业务</td>
        <td>科技</td>
        <td>日常</td>
        <td>业务</td>
        <td>科技</td>
        <td>日常</td>
        <td>业务</td>
        <td>科技</td>
        <td>日常</td>
        <td>总合计</td>
      </tr>
    </thead>
    <!--具体的数据值-->
    <tr v-for="(item, index) in dataSource" :key="index">
      <td class="text-center">
        {{ item.groupName }}
      </td>
      <!-- 预评估 -->
      <td class="text-center">
        {{ item.preEvaluate.busCount }}
      </td>
      <td class="text-center">
        {{ item.preEvaluate.techCount }}
      </td>
      <td class="text-center">
        {{ item.preEvaluate.dailyCount }}
      </td>
      <!-- 评估 -->
      <td class="text-center">
        {{ item.evaluate.busCount }}
      </td>
      <td class="text-center">
        {{ item.evaluate.techCount }}
      </td>
      <td class="text-center">
        {{ item.evaluate.dailyCount }}
      </td>
      <!-- 待实施 -->
      <td class="text-center">{{ item.preImplement.busCount }}</td>
      <td class="text-center">{{ item.preImplement.busLineUp }}</td>
      <td class="text-center">{{ item.preImplement.techCount }}</td>
      <td class="text-center">{{ item.preImplement.techLineUp }}</td>
      <td class="text-center">{{ item.preImplement.dailyCount }}</td>
      <td class="text-center">{{ item.preImplement.dailyLineUp }}</td>
      <!-- 开发阶段（牵头） -->
      <td class="text-center">{{ item.leaderDevelop.busCount }}</td>
      <td class="text-center">{{ item.leaderDevelop.techCount }}</td>
      <td class="text-center">{{ item.leaderDevelop.dailyCount }}</td>
      <!-- 开发阶段（参与） -->
      <td class="text-center">{{ item.joinDevelop.busCount }}</td>
      <td class="text-center">{{ item.joinDevelop.techCount }}</td>
      <td class="text-center">{{ item.joinDevelop.dailyCount }}</td>
      <!-- 测试阶段SIT、UAT、REL -->
      <td class="text-center">{{ item.sit.busCount }}</td>
      <td class="text-center">{{ item.sit.techCount }}</td>
      <td class="text-center">{{ item.sit.dailyCount }}</td>
      <td class="text-center">{{ item.uat.busCount }}</td>
      <td class="text-center">{{ item.uat.techCount }}</td>
      <td class="text-center">{{ item.uat.dailyCount }}</td>
      <td class="text-center">{{ item.rel.busCount }}</td>
      <td class="text-center">{{ item.rel.techCount }}</td>
      <td class="text-center">{{ item.rel.dailyCount }}</td>
      <!-- 暂缓 -->
      <td class="text-center">{{ item.wait.busCount }}</td>
      <td class="text-center">{{ item.wait.techCount }}</td>
      <td class="text-center">{{ item.wait.dailyCount }}</td>
      <!-- 业务需求人均负荷 -->
      <td class="text-center">{{ item.busAvg }}</td>
      <!-- 开发总人均负荷 -->
      <td class="text-center">{{ item.developAvg }}</td>
      <!-- 已投产 -->
      <td class="text-center">{{ item.pro.busCount }}</td>
      <td class="text-center">{{ item.pro.techCount }}</td>
      <td class="text-center">{{ item.pro.dailyCount }}</td>
      <!-- 合计 -->
      <td class="text-center">{{ item.total.busCount }}</td>
      <td class="text-center">{{ item.total.techCount }}</td>
      <td class="text-center">{{ item.total.dailyCount }}</td>
      <td class="text-center">
        {{ item.total.totalCount }}
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
        excel.export_html_table('leadTable', '牵头需求统计');
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
