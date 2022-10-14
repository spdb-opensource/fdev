<template>
  <fdev-chip
    outline
    :removable="removable"
    @remove="$emit('remove', data)"
    square
    :color="color"
    dense
    text-color="white"
  >
    {{ name }}
    <fdev-tooltip>
      <div class="tooltip">
        <p v-for="(val, key) in data" :key="key">{{ key }}：{{ val }}</p>
      </div>
    </fdev-tooltip>
  </fdev-chip>
</template>

<script>
export default {
  name: 'Chip',
  props: {
    data: Object,
    name: String,
    removable: Boolean,
    color: {
      type: String,
      default: 'teal'
    }
  }
};
</script>

<style lang="stylus" scoped>
.tooltip
  p
    margin 0
</style>
