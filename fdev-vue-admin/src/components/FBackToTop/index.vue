<template>
  <div class="back-to-top" @click="backToTop" v-show="showReturnToTop">
    <f-icon
      :name="bttOption.iName"
      :class="[bttOption.iClass]"
      :width="bttOption.iWidth"
      :height="bttOption.iHeight"
    />
  </div>
</template>

<script>
import { scrollIt } from './scrollIt'; // 引入动画过渡的实现
export default {
  name: 'FBackToTop',
  props: {
    iName: {
      tyle: String,
      default: 'arrow_u_o'
    },
    iClass: {
      // 图标形状
      type: String,
      default: 'text-white'
    },
    iWidth: {
      type: [Number, String],
      default: 24
    },
    iHeight: {
      type: [Number, String],
      default: 24
    },
    pageY: {
      // 默认在哪个视图显示返回按钮
      type: Number,
      default: 400
    },
    transitionName: {
      // 过渡动画名称
      type: String,
      default: 'linear'
    }
  },
  data: function() {
    return {
      showReturnToTop: false
    };
  },
  computed: {
    bttOption() {
      return {
        iName: this.iName,
        iClass: this.iClass,
        iWidth: this.iWidth,
        iHeight: this.iHeight
      };
    }
  },
  methods: {
    currentPageYOffset() {
      // 判断滚动区域大于多少的时候显示返回顶部的按钮
      window.pageYOffset > this.pageY
        ? (this.showReturnToTop = true)
        : (this.showReturnToTop = false);
    },
    backToTop() {
      scrollIt(0, 500, this.transitionName, this.currentPageYOffset);
    }
  },
  created() {
    window.addEventListener('scroll', this.currentPageYOffset);
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.currentPageYOffset);
  }
};
</script>

<style scoped lang="stylus">
.back-to-top
  position fixed
  bottom 5%
  right 100px
  z-index 9999
  width auto
  padding 8px 8px 5px 8px
  border-radius 50%
  background-color #00000076
  cursor pointer
  &:hover
    background-color #000000d9
  svg
    display inline-block
    transition all 0.3s linear
</style>
