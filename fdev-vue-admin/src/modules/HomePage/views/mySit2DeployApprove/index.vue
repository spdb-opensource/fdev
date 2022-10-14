<template>
  <div>
    <tabs-block :data="tabData" :default-tab="todosTab" @change="saveToDoData">
      <fdev-tab-panel name="unitTodos">
        <unitApprovals label="todo" name="todo" v-model="eventNum" />
      </fdev-tab-panel>

      <fdev-tab-panel name="unitDone">
        <unitApprovalsDone label="done" name="done" v-model="eventNum" />
      </fdev-tab-panel>
    </tabs-block>
  </div>
</template>

<script>
import unitApprovals from './components/unitApprovals';
import TabsBlock from '@/components/TabsBlock';
import unitApprovalsDone from './components/unitApprovalsDone';
import { mapState, mapMutations } from 'vuex';
export default {
  name: 'myUnitApprovals',
  components: { unitApprovals, TabsBlock, unitApprovalsDone },
  data() {
    return {
      tableData: [],
      eventNum: {
        todo: 0,
        done: 0
      },
      eventTodoNum: 0,
      eventDoneNum: 0
    };
  },
  computed: {
    ...mapState('userActionSaveHomePage/mySit2Page', ['todosTab']),
    tabData() {
      return [
        {
          name: 'unitTodos',
          label: `待审批(${this.eventNum.todo})`
        },
        {
          name: 'unitDone',
          label: `已完成(${this.eventNum.done})`
        }
      ];
    }
  },

  methods: {
    ...mapMutations('userActionSaveHomePage/mySit2Page', ['saveToDoData'])
  }
};
</script>
