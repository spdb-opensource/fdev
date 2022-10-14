<template>
  <f-block
    ><fdev-tabs v-model="tab" align="left">
      <fdev-tab name="codeList" label="代码统计列表" />
      <fdev-tab name="mergeRequest" label="merge Request" />
    </fdev-tabs>
    <fdev-separator class="q-mb-lg" />
    <Loading :visible="loading">
      <fdev-tab-panels v-model="tab" animated class="panel-border">
        <!-- 代码统计 -->
        <fdev-tab-panel name="codeList"
          ><CodeStatistics ref="codeList"
        /></fdev-tab-panel>
        <!-- merge request -->
        <fdev-tab-panel name="mergeRequest"
          ><MergeRequest ref="mergeRequest"
        /></fdev-tab-panel>
      </fdev-tab-panels>
    </Loading>
  </f-block>
</template>
<script>
import Loading from '@/components/Loading';
import CodeStatistics from './components/CodeStatistics';
import MergeRequest from './components/mergeRequest';
export default {
  data() {
    return {
      tab: 'codeList',
      loading: false
    };
  },
  watch: {
    tab(val) {
      if (val !== 'codeList')
        // 代码统计列表做原进原出
        this.$refs['codeList'].saveData();
    }
  },
  components: { Loading, CodeStatistics, MergeRequest },
  methods: {},
  // 进入页面之前
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('nodes');
    }
    next();
  },
  // 离开页面时原进原出
  beforeRouteLeave(to, from, next) {
    if (this.tab === 'codeList') {
      this.$refs['codeList'].saveData();
    }

    next();
  }
};
</script>
<style lang="stylus" scoped></style>
