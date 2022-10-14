<template>
  <div
    @mouseenter="outerH"
    @mouseleave="outerL"
    :class="outerClass"
    ref="outer"
  >
    <div :class="innerClass" ref="inner">
      <slot />
    </div>
  </div>
</template>

<script>
export default {
  name: 'FScrollarea',
  data() {
    return {
      outerClass: 'hover-scroll',
      innerClass: null,
      scrollStatus: false
    };
  },
  props: {
    normal: Boolean,
    horizontal: Boolean,
    bothXY: Boolean
  },
  mounted() {
    this.innerClass = this.innerClass1;
    if (this.$listeners['change-scroll'] !== void 0) {
      this.checkScrollStatus();
    }
  },
  methods: {
    outerH() {
      this.outerClass = this.outerClass1;
      this.innerClass = '';
      if (this.$listeners['change-scroll'] !== void 0) {
        this.checkScrollStatus();
      }
    },
    outerL() {
      this.outerClass = 'hover-scroll';
      this.innerClass = this.innerClass1;
    },
    checkScrollStatus() {
      if (!this.$refs.inner) {
        return;
      }
      const innerHeight = this.$refs.inner.clientHeight,
        outerHeight = this.$refs.outer.clientHeight;
      if (!this.scrollStatus && innerHeight > outerHeight) {
        this.scrollStatus = true;
        this.$emit('change-scroll', this.scrollStatus);
      } else if (this.scrollStatus && innerHeight <= outerHeight) {
        this.scrollStatus = false;
        this.$emit('change-scroll', this.scrollStatus);
      }
    }
  },
  computed: {
    outerClass1() {
      let md = this.normal ? 'normal' : 'thin';
      let ed = this.horizontal ? '-x' : this.bothXY ? '' : '-y';
      return `scroll-${md}${ed}`;
    },
    innerClass1() {
      if (this.$listeners['change-scroll'] !== void 0) {
        return 'mr-10';
      }
      let md = this.horizontal ? 'mb' : 'mr';
      let ed = this.normal ? 'sm' : 'xs';
      return this.bothXY ? `q-mr-${ed} q-mb-${ed}` : `q-${md}-${ed}`;
    }
  }
};
</script>

<style lang="stylus" scoped>
.hover-scroll
  overflow hidden !important
.mr-10
  margin-right 10px
</style>
