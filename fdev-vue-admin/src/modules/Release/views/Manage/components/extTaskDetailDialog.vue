<template>
  <f-dialog
    :value="value"
    title="脚本详情"
    right
    dense
    @input="$emit('input', $event)"
  >
    <f-formitem profile label="脚本内容" bottom-page label-style="width:94px;">
      <div class="text-blue-9 detail-content">
        {{ detail }}
      </div>
    </f-formitem>
    <template v-slot:btnSlot>
      <fdev-btn @click="submit" dialog label="确定" />
    </template>
  </f-dialog>
</template>

<script>
export default {
  name: 'ExtTaskDetailDialog',
  props: {
    value: {
      type: Boolean
    },
    detail: {
      type: String
    }
  },
  methods: {
    submit() {
      this.$emit('click');
    }
  }
};
</script>

<style lang="stylus" scoped>
.detail-content
  word-break break-all
/deep/ .el-input__inner
  height 36px
  margin-bottom 20px
  width 300px
  border 1px solid #bbb
/deep/ .el-icon-circle-close
  position absolute
  right -120px
  top -8px
</style>
<style>
.el-time-panel {
  z-index: 9999 !important;
}
.el-icon-time {
  height: 36px;
  line-height: normal;
}
</style>
