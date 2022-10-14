<template>
  <div>
    <f-block class="q-mb-sm"
      ><fdev-tabs v-model="tab" align="left">
        <fdev-tab name="team" label="团队维度" />
        <fdev-tab name="person" label="用户维度" />
      </fdev-tabs>
      <fdev-separator
    /></f-block>
    <f-block
      ><fdev-tab-panels v-model="tab">
        <!-- 团队维度 -->
        <fdev-tab-panel name="team"><teamDimension ref="team"/></fdev-tab-panel>
        <!-- 用户维度 -->
        <fdev-tab-panel name="person"
          ><personDimension ref="person"
        /></fdev-tab-panel> </fdev-tab-panels
    ></f-block>
  </div>
</template>
<script>
import personDimension from './components/personDimension'; //用户维度
import teamDimension from './components/teamDimension'; //团队维度
export default {
  data() {
    return {
      tab: 'team'
    };
  },
  watch: {
    tab: {
      handler: function(newValue, oldValue) {
        // 切换tab时原进原出
        this.$refs[oldValue].saveData();
      }
    }
  },
  components: { personDimension, teamDimension },
  // 进入页面之前
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      sessionStorage.removeItem('IamsTree');
      sessionStorage.removeItem('IamsObj');
    }
    next();
  },
  //离开页面之前保存组树数据
  beforeRouteLeave(to, from, next) {
    this.$refs[this.tab].saveData();
    next();
  }
};
</script>
<style scoped></style>
