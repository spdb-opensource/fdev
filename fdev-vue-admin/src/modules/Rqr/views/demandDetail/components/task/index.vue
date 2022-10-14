<template>
  <f-block block>
    <Task :demandDetail="params" />
  </f-block>
</template>
<script>
import Task from '@/modules/Rqr/Components/Task.vue';

export default {
  name: 'rqrTask',
  props: {
    params: {
      type: Object
    }
  },
  data() {
    return {
      data: []
    };
  },
  components: {
    Task
  }
};
</script>
<style lang="stylus" scoped></style>
