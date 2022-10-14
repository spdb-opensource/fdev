<template>
  <div>
    <PageHeader v-if="display">
      <slot name="header" />
    </PageHeader>
    <div :class="{ content: display }">
      <slot />
    </div>
  </div>
</template>

<script>
import PageHeader from '../PageHeader';
export default {
  name: 'PageHeaderWrapper',
  components: { PageHeader },
  data() {
    return {};
  },
  props: {
    display: {
      type: Boolean,
      default: true
    }
  },
  computed: {},
  methods: {},
  mounted() {}
};
</script>

<style lang="stylus" scoped>
@import '~#/css/variables.styl';

.content
  margin: 24px 24px 0;

@media screen and (max-width: $sizes.sm)
  .content
    margin: 24px 0 0;
</style>
