<template>
  <div
    class="circle-status"
    :style="{
      display: display,
      width: computedWidth,
      height: computedHeight,
      'background-color': color,
      'background-image': gradient
    }"
  ></div>
</template>

<script>
export default {
  name: 'FStatusColor',
  props: {
    color: {
      type: String
    },
    gradient: {
      type: String
    },
    width: {
      default: '8px',
      type: String || Number
    },
    height: {
      default: '8px',
      type: String || Number
    }
  },
  computed: {
    display() {
      return this.color || this.gradient ? 'inline-block' : 'none';
    },
    computedWidth() {
      return typeof this.width === 'string' ? this.width : this.width + 'px';
    },
    computedHeight() {
      return typeof this.height === 'string' ? this.height : this.height + 'px';
    }
  }
};
</script>

<style lang="stylus" scoped>

.circle-status
  border-radius 50%
  margin-right 5px
</style>
