<template>
  <f-dialog
    :value="value"
    transition-show="slide-up"
    transition-hide="slide-down"
    @input="$emit('input', $event)"
    :persistent="persistent"
    @shake="confirmToClose"
    :title="title"
  >
    <div class="bg-grey-4 dialog-width" :class="className">
      <div class="dialog-wrapper">
        <slot />
      </div>
    </div>
  </f-dialog>
</template>

<script>
export default {
  name: 'Dialog',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    title: String,
    persistent: {
      type: Boolean,
      default: true
    },
    btnDisplay: {
      type: Boolean,
      default: true
    },
    confirm: {
      type: Boolean,
      default: true
    },
    className: {
      type: String,
      default: 'default-width'
    }
  },
  methods: {
    confirmToClose(e) {
      if (this.confirm) {
        this.$q
          .dialog({
            title: '关闭弹窗',
            message: '关闭弹窗后数据将会丢失，确认要关闭？',
            cancel: true,
            persistent: true
          })
          .onOk(() => {
            this.$emit('input', false);
          });
      }
    }
  }
};
</script>

<style lang="stylus" scoped>
.default-width
  min-width 560px
.dialog-width
  max-width 100%
  position relative
  .dialog-header
    position absolute !important
    z-index 2
  .dialog-wrapper
    margin-top 50px
    box-sizing border-box
    max-height calc(100vh - 98px)
    overflow auto
</style>
