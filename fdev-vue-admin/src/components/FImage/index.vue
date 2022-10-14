<template>
  <img
    :src="menu ? menuIcons[name] : icons[name]"
    @click="clickFunc"
    class="self-center"
    :class="[{ 'cursor-pointer': pointer }, iconSize[size]]"
    style="color:red"
  />
</template>

<script>
import icons from '@/assets/fImage';
import { LAYOUT_ICONS } from '@/layouts/assets/index.js';

export default {
  name: 'FImage',
  data() {
    return {
      icons: icons,
      menuIcons: LAYOUT_ICONS,
      iconSize: { md: 'icon-md', lg: 'icon-lg' }
    };
  },

  methods: {
    iconClass() {
      let size = { md: 'icon-md', lg: 'icon-lg' };
      let pointer = this.pointer ? 'cursor-pointer' : '';
      let iconSize = this.size ? size[this.size] : '';
      return pointer + ' ' + iconSize;
    },
    clickFunc() {
      this.$emit('click');
    }
  },
  props: {
    name: String,
    pointer: Boolean,
    size: String,
    menu: Boolean
  }
};
</script>

<style scoped lang="stylus">
.icon-md
  width 24px
  height 24px
.icon-lg
  width 32px
  height 32px
</style>
