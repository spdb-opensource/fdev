<template>
  <div>
    <div class="result">
      <div class="icon">
        <fdev-icon
          name="check_circle"
          v-if="type === 'success'"
          color="positive"
          size="86px"
        />
        <fdev-icon
          name="mdi-set mdi-close-circle"
          v-else
          color="red"
          size="86px"
        />
      </div>

      <div class="title text-title" v-if="!!$slots.title">
        <slot name="title" />
      </div>
      <div class="description" v-if="!!$slots.description">
        <slot name="description" />
      </div>
      <div class="extra" v-if="!!$slots.extra">
        <slot name="extra" />
      </div>
      <div class="actions" v-if="!!$slots.actions">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Result',
  props: {
    type: String // success error
  },
  created() {},
  mounted() {}
};
</script>

<style lang="stylus" scoped>
@import '~#/css/variables.styl';

.result
  text-align: center;
  width: 72%;
  margin: 0 auto;
  @media screen and (max-width: $sizes.sm) {
    width: 100%;
  }
  .icon
    margin-bottom: 24px;
  .title
    font-size: 24px;
    font-weight: 500;
    line-height: 32px;
    margin-bottom: 16px;
  .description
    font-size: 14px;
    line-height: 22px;
    color: $grey-7;
    margin-bottom: 24px;
  .extra
    background: #fafafa;
    padding: 24px 40px;
    border-radius: $generic-border-radius;
    text-align: left;
    @media screen and (max-width: $sizes.sm)
      padding: 18px 20px;
  .actions
    margin-top: 32px;
</style>
