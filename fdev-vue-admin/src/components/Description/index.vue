<template>
  <div class="row items-start" :class="responsive[col]">
    <div class="term" v-if="label" :style="{ width: labelWidth || '150px' }">
      {{ label }}：
    </div>
    <div class="detail col" :class="ellipsis ? 'ellipsis' : ''">
      <slot />
    </div>
  </div>
</template>

<script>
export default {
  name: 'Description',
  props: {
    label: String,
    col: {
      default: 3,
      type: Number
    },
    ellipsis: Boolean,
    labelWidth: String
  },
  data() {
    return {
      responsive: {
        1: 'col-xs-12',
        2: 'col-xs-12 col-sm-6',
        3: 'col-xs-12 col-sm-6 col-md-4',
        4: 'col-xs-12 col-sm-6 col-md-4 col-lg-3'
      }
    };
  },
  methods: {}
};
</script>

<style lang="stylus" scoped>
.term
  font-size: 14px;
  color: #212121;
.detail
  word-break: break-all;
  font-size: 14px;
  color: $grey-8;
</style>
