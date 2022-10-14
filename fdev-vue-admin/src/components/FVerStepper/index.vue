<template>
  <div class="column">
    <div v-for="(word, index) in words" :key="index" class="row justify-center">
      <div
        class="order column no-wrap items-center"
        :style="{ 'padding-top': `${word.inteval ? word.inteval : '16'}px` }"
      >
        <div class="circle row justify-center items-center">
          {{ word.value }}
        </div>
        <div class="vertical-line" v-if="index !== words.length - 1" />
      </div>
      <div class="content">
        <slot :name="`content-${word.value}`" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FVerStepper',
  props: {
    words: Array
  }
};
</script>
<style scoped lang="stylus">
.order
  width 32px
  margin-right 16px
  .circle
    width 24px
    height 24px
    line-height 22px
    border 1px solid #0063C5
    border-radius 50%
    color #0063C5
  .vertical-line
    height 100%
    border-right 4px dotted #D8E0EC
    margin-top 12px
</style>
