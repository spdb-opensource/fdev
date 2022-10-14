<template>
  <fdev-select
    flat
    multiple
    class="selected q-ml-md"
    v-model="value"
    :options="options"
    display-value="选择列"
    @input="handleInputFun"
  />
</template>
<script>
export default {
  data() {
    return {
      value: [],
      options: [
        { label: '完成情况', val: 'finshFlag', key: 0 },
        { label: '当前阶段', val: 'currentStage', key: 1 },
        { label: '当前阶段开始时间', val: 'currentStageTime', key: 2 },
        { label: '审核次数', val: 'checkCount', key: 3 },
        { label: '开发人员', val: 'uiVerifyReporter', key: 4 },
        { label: '计划提交内测日期', val: 'plan_inner_test_time', key: 5 },
        { label: '计划提交业测日期', val: 'plan_uat_test_start_time', key: 6 },
        { label: '实际提交内测日期', val: 'start_inner_test_time', key: 7 },
        { label: '实际提交业测日期', val: 'start_uat_test_time', key: 8 },
        { label: '研发单元牵头人', val: 'implement_leader', key: 9 }
      ]
    };
  },
  mounted() {
    // 默认展示前面四列数据
    this.value = JSON.parse(JSON.stringify(this.options.slice(0, 4)));
    // 通知table组建
    this.$emit('colListFun', this.value);
  },
  methods: {
    // 需要联动，通知表格隐藏数据
    handleInputFun(value) {
      let props = value.sort((a, b) => {
        return a.key - b.key;
      });
      this.$emit('colListFun', props);
    }
  }
};
</script>
<style lang="stylus" scoped>
.selected
    width 70px
    height 36px
    outline none
    transform translateY(8px)
</style>
