<template>
  <div
    class="bg-white column"
    ref="page"
    :style="{ minHeight: `calc(100vh - ${minHeight + offsetTop}px)` }"
  >
    <slot />
  </div>
</template>

<script>
import { offset } from '#/utils/dom';

export default {
  name: 'Page',
  data() {
    return {
      baseOffsetTop: 128,
      minHeight: 251,
      offsetTop: 0
    };
  },
  methods: {},
  created() {},
  mounted() {},
  updated: function() {
    this.$nextTick(function() {
      this.offsetTop =
        offset(this.$refs.page).top -
        offset(document.body).top -
        this.baseOffsetTop;
    });
  }
};
</script>

<style lang="stylus" scoped>
@import '~#/css/variables.styl';
</style>
