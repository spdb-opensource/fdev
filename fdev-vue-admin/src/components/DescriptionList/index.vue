<template>
  <div :class="`row ${cls}`">
    <slot />
  </div>
</template>

<script>
export default {
  name: 'DescriptionList',
  props: {
    col: {
      default: 3,
      type: Number
    },
    layout: {
      default: 'horizontal',
      type: String
    },
    gutterX: {
      default: 'md',
      type: String
    },
    gutterY: {
      default: 'sm',
      type: String
    }
  },
  provide() {
    return {
      col: this.col,
      layout: this.layout
    };
  },
  data() {
    return {};
  },
  computed: {
    cls() {
      return `q-col-gutter-x-${this.gutterX} q-col-gutter-y-${this.gutterY}`;
    }
  },
  methods: {}
};
</script>

<style lang="stylus" scoped></style>
