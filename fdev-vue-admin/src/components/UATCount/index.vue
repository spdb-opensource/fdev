<template>
  <div>
    <div class="q-pa-md row item-start container-div">
      <div class="section-div" :class="fromApp ? 'div-cursor' : ''">
        <div class="card-div">{{ UATnumtList.newIssueNum }}</div>
        <fdev-badge color="green">新建</fdev-badge>
      </div>
      <fdev-separator vertical />
      <div class="section-div" :class="fromApp ? 'div-cursor' : ''">
        <div class="card-div">{{ UATnumtList.openIssueNum }}</div>
        <fdev-badge>打开</fdev-badge>
      </div>
      <fdev-separator vertical />
      <div class="section-div" :class="fromApp ? 'div-cursor' : ''">
        <div class="card-div">{{ UATnumtList.reopenIssueNum }}</div>
        <fdev-badge>重新打开</fdev-badge>
      </div>
      <fdev-separator vertical />
      <div class="section-div" :class="fromApp ? 'div-cursor' : ''">
        <div class="card-div">{{ UATnumtList.delayIssueNum }}</div>
        <fdev-badge color="orange">延迟修复</fdev-badge>
      </div>
      <fdev-separator vertical />
      <div class="section-div" :class="fromApp ? 'div-cursor' : ''">
        <div class="card-div">{{ UATnumtList.leaOvIssueNum }}</div>
        <fdev-badge color="purple">遗留</fdev-badge>
      </div>
      <fdev-separator vertical />
      <div
        class="section-div count-div"
        :class="fromApp ? 'div-cursor' : ''"
        style="flex: initial;
    width: 200px;"
      >
        <div class="left-div">
          <div class="card-div">{{ UATnumtList.waitIssueNum }}</div>
          <fdev-badge color="red">待确认</fdev-badge>
        </div>
        <hr class="hr-rotate" />
        <div class="right-div">
          <div class="right-item color-reject">
            拒绝 <span class="card-span">{{ UATnumtList.refuseIssueNum }}</span>
          </div>
          <div class="color-success">
            已修复
            <span class="card-span">{{ UATnumtList.repairIssueNum }}</span>
          </div>
        </div>
      </div>
      <fdev-separator vertical />
      <div
        class="section-div count-div"
        :class="fromApp ? 'div-cursor' : ''"
        style="flex: initial;
    width: 200px;"
      >
        <div class="left-div">
          <div class="card-div">{{ UATnumtList.closedIssueNum }}</div>
          <fdev-badge color="grey">已关闭</fdev-badge>
        </div>
        <hr class="hr-rotate" />
        <div class="right-div">
          <div class="right-item color-close">
            关闭 <span class="card-span">{{ UATnumtList.closeIssueNum }}</span>
          </div>
          <div class="color-close">
            确认拒绝
            <span class="card-span">{{ UATnumtList.affRefIssueNum }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';

export default {
  name: 'Count',
  components: {},
  data() {
    return {};
  },
  computed: {
    ...mapState('jobForm', ['UATnumtList'])
  },
  props: {
    fromApp: {
      type: Boolean
    },
    defectList: {
      type: Array
    }
  },
  watch: {},
  methods: {}
};
</script>
<style lang="stylus" scoped>
@import '~#/css/variables.styl';
.card-div
  font-size 28px
.container-div
  margin-left 0
  margin-top 0
.section-div
  flex 1
  text-align center
.div-cursor
  cursor pointer
.card-span
  font-size 20px
.count-div
  display flex
.count-div > div
  flex 1
.right-div
  align-self flex-end
  margin-right 20px
.right-div > .right-item
  height 20px
.left-div
  margin-left 20px
  align-self flex-start
.hr-rotate
  height 50px
  transform rotate(25deg)
.color-close
  color grey
.color-success
  color #0a0
.color-reject
  color #e00
.q-layout-container
  max-width 55%
  max-height 70%
.td-name
  max-width 20rem
  overflow hidden
  text-overflow ellipsis
</style>
