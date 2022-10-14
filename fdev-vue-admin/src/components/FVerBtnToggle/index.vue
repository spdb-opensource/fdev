<template>
  <div class="row full-height">
    <div class="column justify-center full-height ele">
      <fdev-btn-toggle
        class="column full-height toggle"
        v-model="model"
        :options="options"
      />
    </div>

    <slot name="right" />
  </div>
</template>

<script>
export default {
  name: 'FVerBtnToggle',
  props: {
    value: String,
    options: Array
  },
  computed: {
    model: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit('input', val);
      }
    }
  }
};
</script>

<style scoped lang="stylus">
border-radius-first()
  border-top-left-radius 2px !important
  border-top-right-radius 2px !important
  border-bottom-left-radius 0 !important
  border-bottom-right-radius 0 !important
border-radius-mid()
  border-top-left-radius 0 !important
  border-top-right-radius 0 !important
  border-bottom-left-radius 0 !important
  border-bottom-right-radius 0 !important
border-radius-last()
  border-top-left-radius 0 !important
  border-top-right-radius 0 !important
  border-bottom-left-radius 2px !important
  border-bottom-right-radius 2px !important

.ele
  >>>
    .q-btn-group button
      width auto !important
      height 32px
      min-height 32px
      .q-btn__wrapper
        padding 9px
        min-height auto
        .q-btn__content
          line-height 14px !important
    .q-btn-group .q-btn-item:first-child
      border-radius-first()
      .q-btn__wrapper:before
        border-bottom none !important
        border-radius-first()
    .q-btn-group .q-btn-item:not(:last-child):not(:first-child)
      border-radius-mid()
      .q-btn__wrapper:before
        border-bottom none !important
        border-radius-mid()
    .q-btn-group .q-btn-item:last-child
      border-radius-last()
      .q-btn__wrapper:before
        border-radius-last()
    .q-btn-toggle .q-no-leftborder .q-btn__wrapper:before
      border-left 1px solid #bbb !important
    .q-btn-toggle .q-no-rightborder .q-btn__wrapper:before
      border-right 1px solid #bbb !important

.toggle
  &::before, &::after
    content: '';
    border-left: 1px solid #ddd;
    width: 1px;
    height: 100%;
    position: relative;
    left: 50%;
</style>
