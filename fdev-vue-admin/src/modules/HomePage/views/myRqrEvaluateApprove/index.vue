<template>
  <div>
    <tabs-block :data="tabData" :default-tab="todosTab">
      <fdev-tab-panel name="eveluateTodos">
        <eveluateApprovals label="todo" name="todo" v-model="eventNum" />
      </fdev-tab-panel>

      <fdev-tab-panel name="eveluateDone">
        <eveluateApprovalsDone
          label="done"
          name="done"
          v-model="eventNum"
        /> </fdev-tab-panel
    ></tabs-block>
  </div>
</template>
<script>
import eveluateApprovals from './components/eveluateApprovals';
import eveluateApprovalsDone from './components/eveluateApprovalsDone';
import TabsBlock from '@/components/TabsBlock';
import { mapState, mapMutations } from 'vuex';
export default {
  name: 'myRqrEvaluateApprove',
  data() {
    return {
      eventNum: {
        todo: 0,
        done: 0
      }
    };
  },
  components: { TabsBlock, eveluateApprovals, eveluateApprovalsDone },
  computed: {
    ...mapState('userActionSaveHomePage/myEveluatePage', ['todosTab']),
    tabData() {
      return [
        {
          name: 'eveluateTodos',
          label: `待审批(${this.eventNum.todo})`
        },
        {
          name: 'eveluateDone',
          label: `已完成(${this.eventNum.done})`
        }
      ];
    }
  },
  methods: {
    ...mapMutations('userActionSaveHomePage/myEveluatePage', ['saveToDoData'])
  }
};
</script>
<style lang="stylus" scoped></style>
