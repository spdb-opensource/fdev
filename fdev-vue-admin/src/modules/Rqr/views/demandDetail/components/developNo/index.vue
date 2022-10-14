<template>
  <f-block block>
    <!-- 研发单元 科技 业务 日常需求 -->
    <developNo
      @updateHeadAndTable="updateHeadAndTable"
      @init="init"
      v-if="params"
      :demandDetail="params"
    />
  </f-block>
</template>
<script>
//研发单元列表
import developNo from '@/modules/Rqr/rqrEvaluate.vue';

export default {
  name: 'demandDetailDevelopNo',
  components: { developNo },
  props: {
    params: {
      type: Object
    }
  },
  data() {
    return {};
  },
  created() {
    // 调试勿删 后续删除
  },
  methods: {
    init(type) {
      this.$emit('init', type);
    },
    updateHeadAndTable(type) {
      this.$emit('updateHeadAndTable', type);
    }
  }
};
</script>
<style lang="stylus" scoped></style>
