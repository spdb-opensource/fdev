<template>
  <div
    class="row no-wrap items-start"
    :class="{ 'full-width': !(page || diaS) && fullWidth }"
    v-if="align !== 'top'"
  >
    <div
      class="input-height row no-wrap items-center"
      :class="[
        { 'q-mr-md': !labelAuto && !help, 'relative-position': required },
        `justify-${aligns}`,
        labelAuto ? '' : help ? 'label-with-help' : 'label',
        labelClass
      ]"
      :style="labelStyle"
    >
      <span
        :title="label"
        class="label-font ellipsis-2-lines"
        :class="{ 'dialog-label': required }"
      >
        {{ label }}
      </span>
      <div v-if="help" class="full-height row items-center">
        <f-icon
          name="help_c_o"
          :width="14"
          :height="14"
          class="help-icon text-primary"
        />
        <fdev-tooltip>{{ help }}</fdev-tooltip>
      </div>
    </div>

    <div
      class="font"
      :class="[
        {
          col: !valueAuto && (bottomPage || (!(page || diaS) && fullWidth)),
          'input-sm': !valueAuto && page,
          'input-md': !valueAuto && !(page || diaS || fullWidth),
          'input-lg': !valueAuto && diaS
        },
        valueClass
      ]"
      style="word-break:break-all"
      :style="valueStyle"
    >
      <slot />
    </div>
  </div>

  <div v-else :class="[{ 'full-width': fullWidth }, 'column']">
    <div class="row no-wrap">
      <div
        class="input-height row no-wrap items-center justify-start"
        :class="[{ 'q-mr-md': !help, 'relative-position': required }]"
        :style="labelStyle"
      >
        <span
          :title="label"
          class="label-font"
          :class="{ 'dialog-label': required }"
        >
          {{ label }}
        </span>
        <div v-if="help" class="full-height row items-center">
          <f-icon
            name="help_c_o"
            :width="14"
            :height="14"
            class="help-icon text-primary"
          />
          <fdev-tooltip>{{ help }}</fdev-tooltip>
        </div>
      </div>
    </div>
    <div
      class="row font"
      :class="{
        'input-sm': page,
        'input-md': !(page || diaS || fullWidth),
        'input-lg': diaS
      }"
      style="word-break:break-all"
      :style="valueStyle"
    >
      <slot />
    </div>
  </div>
</template>

<script>
export default {
  inheritAttrs: false,
  name: 'FFormitem',
  props: {
    label: {
      type: String,
      required: true
    },
    align: {
      type: String,
      default: 'left',
      validator: val => ['left', 'right', 'center', 'top'].includes(val)
    },
    required: Boolean,
    help: String,
    page: Boolean,
    diaS: Boolean,
    fullWidth: Boolean,
    bottomPage: Boolean,
    labelStyle: [String, Object, Array],
    labelClass: String,
    valueStyle: [String, Object, Array],
    valueClass: String,
    labelAuto: Boolean,
    valueAuto: Boolean
  },
  computed: {
    aligns() {
      return {
        left: 'start',
        center: 'center',
        right: 'end',
        top: ''
      }[this.align];
    }
  }
};
</script>
<style scoped lang="stylus">
.font
  line-height 36px
.input-height
  height 36px
.label
  width 120px
.label-with-help
  width 136px
.label-font
  line-height 16px
  font-size 14px
  font-weight 500
.input-sm
  width 200px
.input-md
  width 280px
.input-lg
  width 300px
.help-icon
  cursor help
  margin-left 2px
.dialog-label
  line-height 22px
  &::before
    content '*'
    width 8px
    height 14px
    position absolute
    left -10px
    color #ef5350
.help-icon
  margin-bottom 0
</style>
