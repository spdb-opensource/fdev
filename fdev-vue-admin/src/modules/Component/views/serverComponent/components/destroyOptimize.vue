<template>
  <f-dialog title="废弃" f-sl-sc :value="value" @input="$emit('input', $event)">
    <div class="q-px-lg">
      <p class="text-red">
        <fdev-icon name="warning" color="red" size="30px" class="warning" />
        <span class="red-tip">您确定废弃该条优化需求吗？</span>
      </p>
      <p>当前优化需求开发分支：{{ issueBbranch }}</p>
      <p>当前优化需求阶段：{{ issueStage }}</p>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        :loading="globalLoading[`componentForm/${loading}`]"
        label="确定"
        @click="handleDelete"
      />
    </template>
  </f-dialog>
</template>

<script>
import { mapState, mapActions } from 'vuex';
export default {
  name: 'DestroyDialog',
  props: ['value', 'issueBbranch', 'issueStage', 'page'],
  data() {
    return {};
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    loading() {
      if (this.page.includes('handle')) {
        return 'destroyComponentIssue';
      } else if (this.page.includes('archetypeHandlePage')) {
        return 'destroyArchetypeIssue';
      } else {
        return 'destroyBaseImageIssue';
      }
    }
  },

  methods: {
    ...mapActions('componentForm', [
      'destroyComponentIssue',
      'destroyArchetypeIssue',
      'destroyBaseImageIssue'
    ]),
    async handleDelete() {
      this.$emit('deleteIssue');
    }
  }
};
</script>
<style lang="stylus" scoped>
.red-tip
  color: #f00;
  font-weight: 600;
  letter-spacing: 4px;
  position: relative;
  top: 3px;
  left: 8px;
  font-size: 20px;
.dialog-height
  min-height 300px
  height 0
</style>
