<template>
  <div class="relative-position">
    <slot />
    <fdev-inner-loading :showing="visible">
      <slot name="spinner" v-if="!!$slots.spinner"></slot>
      <fdev-spinner-gears
        v-else
        size="50px"
        color="primary"
      ></fdev-spinner-gears>
    </fdev-inner-loading>
  </div>
</template>

<script>
export default {
  name: 'Loading',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {};
  },
  methods: {}
};
</script>
