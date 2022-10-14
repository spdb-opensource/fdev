<template>
  <div
    class="fdev-block bg-white"
    :class="block ? 'q-pt-md block-type' : 'no-block'"
  >
    <slot />
  </div>
</template>

<script>
export default {
  name: 'FBlock',
  props: { block: Boolean }
};
</script>

<style lang="stylus" scoped>
.fdev-block
  border-radius 8px
  padding-left 32px
  padding-right 32px
.block-type
  padding-bottom 32px
.no-block
  padding-top 22px
  padding-bottom 16px
</style>
