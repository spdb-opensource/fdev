<template>
  <div>
    <f-block class="q-mb-sm"
      ><fdev-tabs v-model="tab" align="left">
        <fdev-tab name="group" label="团队维度" />
        <fdev-tab name="person" label="用户维度" />
      </fdev-tabs>
      <fdev-separator
    /></f-block>

    <fdev-tab-panels v-model="tab">
      <!-- 团队维度 -->
      <fdev-tab-panel name="group"
        ><groupTaskPhaseChange ref="group"
      /></fdev-tab-panel>
      <!-- 用户维度 -->
      <fdev-tab-panel name="person"
        ><personTaskStageNum ref="person"
      /></fdev-tab-panel>
    </fdev-tab-panels>
  </div>
</template>
<script>
import groupTaskPhaseChange from './components/groupTaskPhaseChange'; // 团队维度
import personTaskStageNum from './components/personTaskStageNum'; //用户维度
export default {
  components: { groupTaskPhaseChange, personTaskStageNum },
  data() {
    return {
      tab: 'group'
    };
  },
  watch: {
    tab(val) {
      //切换tab时做原进原出
      val === 'person'
        ? this.$refs.group.saveData()
        : this.$refs.person.saveData();
    }
  },
  //进入页面之前
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('taskPhaseChangeTree');
      sessionStorage.removeItem('taskOutputObj');
    }
    next();
  },
  // 离开页面之前做原进原出
  beforeRouteLeave(to, from, next) {
    this.$refs[this.tab].saveData();
    next();
  }
};
</script>
<style></style>
