<template>
  <div class="items-start" :class="cls">
    <div class="term" v-if="label" :style="{ width: labelWidth || '' }">
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
  inject: ['col', 'layout'],
  props: {
    label: String,
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
  computed: {
    cls() {
      return this.layout !== 'vertical'
        ? `row ${this.responsive[this.col]}`
        : 'column';
    }
  },
  methods: {}
};
</script>

<style lang="stylus" scoped>

.term
  font-size: 14px;
  color: #DDDDDD;
.detail
  word-break: break-all;
  font-size: 14px;
  color: $grey-8;
</style>
