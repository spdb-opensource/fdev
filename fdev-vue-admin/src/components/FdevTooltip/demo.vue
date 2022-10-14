<template>
  <div class="column items-center">
    <div class="column q-gutter-y-md q-ma-xl" style="width:30vw">
      <span class="demo bg-red">
        默认-top
        <fdev-tooltip>默认-top</fdev-tooltip>
      </span>
      <span class="demo bg-red">
        left
        <fdev-tooltip position="left">left</fdev-tooltip>
      </span>
      <span class="demo bg-red">
        right
        <fdev-tooltip position="right">right</fdev-tooltip>
      </span>
      <span class="demo bg-red">
        bottom
        <fdev-tooltip position="bottom">bottom</fdev-tooltip>
      </span>
    </div>
  </div>
</template>
<script>
export default {
  name: 'FdevTooltipDemo',
  data() {
    return {};
  },
  computed: {},
  watch: {},
  filters: {},
  methods: {},
  async created() {}
};
</script>
<style lang="stylus" scoped></style>
