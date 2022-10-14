<template>
  <div>
    <div class="wrapper">
      <div class="text-h6">团队介绍</div>
      <p>{{ data.introduce ? data.introduce.main : '-' }}</p>
      <ol v-if="data.introduce">
        <li v-for="(item, index) in data.introduce.item" :key="index">
          {{ item }}
        </li>
      </ol>
    </div>

    <div class="wrapper">
      <div class="text-h6">
        建议/问题反馈
      </div>
      <a :href="data.issue.main" target="_blank">
        <fdev-chip square dense class="text-white" color="orange-5"
          >issue</fdev-chip
        >
      </a>
      <span class="text-red">
        <fdev-icon name="ion-arrow-back" />提issue请点击这里
      </span>
      <ol>
        <li v-for="(item, index) in data.issue.item" :key="index">
          {{ item }}
        </li>
      </ol>
    </div>

    <div class="wrapper">
      <div class="text-h6">
        知识库
      </div>
      <div v-if="data.knowledge === 0">-</div>
      <ul class="knowledge" v-else>
        <li v-for="item in data.knowledge" :key="item.title">
          <a :href="item.url" target="_blank">
            {{ item.title }}
          </a>
        </li>
      </ul>
    </div>

    <div class="wrapper">
      <div class="text-h6">特别感谢</div>
      <div v-if="data.thanks === 0">-</div>
      <ol v-else>
        <li v-for="(item, index) in data.thanks" :key="index">
          <span class="text-teal">{{ item.name }}：</span>
          <span>{{ item.desc }}</span>
        </li>
      </ol>
    </div>

    <div class="wrapper">
      <div class="text-h6 block">麻烦扫一扫</div>
      <div v-if="data.scan === 0">-</div>
      <div v-else>
        <img v-for="image in data.scan" :key="image" :src="image" alt="" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: ['data']
};
</script>

<style lang="stylus" scoped>
.wrapper
  margin-bottom 25px
  p, ol, ul
    margin 0
    line-height 30px
  p
    margin-left 20px
  ol
    padding-left 35px
  ul
    list-style none
    padding-left 20px
    li
      display inline-block
      padding-right 10px
      a
        color #2196f3
        position relative
        &::after
          height 1px
          width 0
          content ''
          left 0
          position absolute
          bottom -2px
          border-bottom 1px solid #2196f3
      a:hover
        &::after
          width 100%
          transition width .5s
  .text-h6
    color #333333
    display inline-block
  .link
    font-size 12px
    color white
  img
    width 150px
    height 150px
</style>
