<template>
  <f-dialog v-model="dialogOpen" right f-sc title="路由规范">
    <div style="width:860px">
      <Route />
    </div>
    <template v-slot:btnSlot>
      <fdev-btn dialog label="确认" @click="closeDialog" />
    </template>
  </f-dialog>
</template>

<script>
import Route from '../components/Route';

export default {
  name: 'RouteDialog',
  components: { Route },
  props: ['value'],
  data() {
    return {};
  },
  computed: {
    // 弹窗打开或关闭
    dialogOpen: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit('input', val);
      }
    }
  },
  methods: {
    // 关闭弹窗
    closeDialog() {
      this.dialogOpen = false;
    }
  }
};
</script>
