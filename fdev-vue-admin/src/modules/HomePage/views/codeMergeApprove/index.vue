<template>
  <div>
    <tabs-block :data="tabData" :default-tab="todosTab" @change="saveToDoTab">
      <fdev-tab-panel name="todoApprovals">
        <todoApprovals v-model="eventNum" />
      </fdev-tab-panel>

      <fdev-tab-panel name="doneApprovals">
        <doneApprovals v-model="eventNum" />
      </fdev-tab-panel>
    </tabs-block>
  </div>
</template>

<script>
import todoApprovals from './components/todoApprovals';
import doneApprovals from './components/doneApprovals';
import TabsBlock from '@/components/TabsBlock';
import { mapMutations, mapState } from 'vuex';
export default {
  name: 'codeMergeApproval',
  components: { todoApprovals, TabsBlock, doneApprovals },
  data() {
    return {
      loading: false,
      eventNum: {
        todo: 0,
        done: 0
      }
    };
  },
  computed: {
    ...mapState('userActionSaveHomePage/myCodeList', ['todosTab']),
    tabData() {
      return [
        {
          name: 'todoApprovals',
          label: `待审批(${this.eventNum.todo})`
        },
        {
          name: 'doneApprovals',
          label: `已完成(${this.eventNum.done})`
        }
      ];
    }
  },

  methods: {
    ...mapMutations('userActionSaveHomePage/myCodeList', ['saveToDoTab'])
  }
};
</script>
