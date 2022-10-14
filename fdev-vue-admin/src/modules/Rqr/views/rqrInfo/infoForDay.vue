<template>
  <div>
    <!-- 主体 -->
    <f-block block>infoForDay</f-block>
  </div>
</template>
<script>
// 引入组件
export default {
  name: 'infoForDay',
  props: {
    params: Boolean
  },
  components: {},
  watch: {
    // params(val, oldVal) {
    // }
  },
  data() {
    return {
      demandType: null, // 需求类型 tech--科技需求；business--业务需求；daily--日常需求；
      tabs: 'demandInfo', // 默认tabs
      demandInfoDetail: {} //需求详情
    };
  },
  created() {
    this.init();
  },
  methods: {
    // 初始化操作总入口
    init() {}
  }
};
</script>
<style lang="stylus" scoped>
/deep/ .q-tabs__content--align-center
    justify-content flex-start
.wrap
    word-break break-all
</style>
