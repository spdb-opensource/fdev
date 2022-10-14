<template>
  <div class="q-mt-md">
    <table-content
      :type.sync="type"
      :tableData="tableData"
      :selectedColList="selectedColList"
    ></table-content>
  </div>
</template>
<script>
import tableContent from './content.vue';
export default {
  components: { tableContent },
  props: {
    // 列表类型
    type: {
      type: String
    },
    // 列表数据
    tableData: {
      type: Array
    },
    // 选择列
    selectedColList: {
      type: Array
    }
  }
};
</script>
<style lang="stylus" scoped></style>
