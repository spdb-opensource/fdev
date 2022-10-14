<template>
  <!-- 公共横幅 -->
  <div class="row sub-title mb-32">
    <div class="q-ml-md q-mr-sm">
      <f-icon
        :width="width"
        :height="height"
        :style="{ transform: `translate(${x}px,${y}px)` }"
        :name="iconName"
        class="text-primary"
      />
    </div>
    <div class="label">{{ label }}</div>
  </div>
</template>
<script>
export default {
  props: {
    iconName: {
      type: String
    },
    label: {
      type: String
    },
    width: {
      type: String,
      default: '16'
    },
    height: {
      type: String,
      default: '16'
    },
    x: {
      type: String
    },
    y: {
      type: String
    }
  }
};
</script>

<style lang="stylus" scoped>
.sub-title
  height 54px
  line-height 54px
  border-radius 2px
  background rgba(30,84,213,0.05)
.mb-32
  margin-bottom 32px
.label
  line-height 52px
  font-weight 500
</style>
