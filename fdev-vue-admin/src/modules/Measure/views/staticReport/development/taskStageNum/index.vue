<template>
  <div>
    <f-block class="q-mb-sm"
      ><fdev-tabs v-model="tab" align="left">
        <fdev-tab name="team" label="团队维度" />
        <fdev-tab name="application" label="应用维度" />
        <fdev-tab name="person" label="用户维度" />
      </fdev-tabs>
      <fdev-separator
    /></f-block>

    <fdev-tab-panels v-model="tab">
      <!-- 团队维度 -->
      <fdev-tab-panel name="team"
        ><groupTaskStageNum ref="team"
      /></fdev-tab-panel>
      <!-- 应用维度 -->
      <fdev-tab-panel name="application"><appTaskStageNum /></fdev-tab-panel>
      <!-- 用户维度 -->
      <fdev-tab-panel name="person"
        ><personTaskStageNum ref="person"
      /></fdev-tab-panel>
    </fdev-tab-panels>
  </div>
</template>
<script>
import appTaskStageNum from './components/appTaskStageNum'; // 应用维度
import groupTaskStageNum from './components/groupTaskStageNum'; //团队维度
import personTaskStageNum from './components/personTaskStageNum'; //团队维度
export default {
  components: { appTaskStageNum, groupTaskStageNum, personTaskStageNum },
  data() {
    return {
      tab: 'team'
    };
  },
  watch: {
    tab: {
      handler: function(newValue, oldValue) {
        if (oldValue === 'application') {
          return;
        } else {
          this.$refs[oldValue].saveData();
        }
      }
    }
  },
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('groupTaskStageNum');
      sessionStorage.removeItem('userTaskTree');
    }
    next();
  },
  // 离开页面之前做原进原出
  beforeRouteLeave(to, from, next) {
    if (this.tab === 'application') {
      next();
      return;
    }
    this.$refs[this.tab].saveData();
    next();
  }
};
</script>
<style scoped></style>
