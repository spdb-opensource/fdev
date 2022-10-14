<template>
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 18 18"
    :width="width"
    :height="height"
    :aria-labelledby="name"
    v-on="$listeners"
    role="presentation"
  >
    <g :fill="iconColor">
      <title>{{ title }}</title>
      <components :is="name" />
    </g>
  </svg>
</template>

<script>
import F_ICONS from './icons';
export default {
  name: 'FIcon',
  components: { ...F_ICONS },

  props: {
    name: String,
    title: [String, Number],
    width: {
      type: [Number, String],
      default: 18
    },
    height: {
      type: [Number, String],
      default: 18
    },
    iconColor: {
      type: String,
      default: 'currentColor'
    }
  }
};
</script>

<style scoped>
svg {
  display: inline-block;
  vertical-align: baseline;
  margin-bottom: -2px; /* yes, I'm that particular about formatting */
}
</style>
