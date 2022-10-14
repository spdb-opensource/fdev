<template>
  <div>
    <tabs-block :data="tabData" :default-tab="todosTab" @change="saveToDoData">
      <fdev-tab-panel name="todos">
        <Todos label="todo" name="todo" v-model="eventNum" />
      </fdev-tab-panel>

      <fdev-tab-panel name="done">
        <Todos label="done" name="done" v-model="eventNum" />
      </fdev-tab-panel>
    </tabs-block>
  </div>
</template>

<script>
import Todos from '@/modules/User/views/account/todo';
import TabsBlock from '@/components/TabsBlock';
import { mapState, mapMutations } from 'vuex';
export default {
  name: 'myTodo',
  components: { Todos, TabsBlock },
  data() {
    return {
      tableData: [],
      eventNum: {
        todos: 0,
        done: 0
      }
    };
  },
  computed: {
    ...mapState('userActionSaveHomePage/myTodoPage', ['todosTab']),
    tabData() {
      return [
        {
          name: 'todos',
          label: `待办(${this.eventNum.todos})`
        },
        {
          name: 'done',
          label: `已完成(${this.eventNum.done})`
        }
      ];
    }
  },
  methods: {
    ...mapMutations('userActionSaveHomePage/myTodoPage', ['saveToDoData'])
  },
  beforeRouteEnter(to, from, next) {
    const { params } = from;
    if (Object.keys(params).length === 0) {
      // sessionStorage.removeItem('todosTab');
      sessionStorage.removeItem('terms');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    const { params } = to;
    if (Object.keys(params).length) {
      // sessionStorage.setItem('todosTab', JSON.stringify(this.todosTab));
    }
    next();
  }
};
</script>
