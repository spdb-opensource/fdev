<template>
  <div class="exception">
    <div class="imgBlock">
      <div
        class="imgEle"
        :style="{ backgroundImage: `url(${img || config[pageType].img})` }"
      ></div>
    </div>
    <div class="content">
      <h1>{{ title || config[pageType].title }}</h1>
      <div class="desc">{{ desc || config[pageType].desc }}</div>
      <div class="q-gutter-x-md">
        <fdev-btn @click="$router.push(redirect)" roundash :label="backText" />
        <fdev-btn @click="$router.back()" roundash label="返回前一页" />
      </div>
    </div>
  </div>
</template>

<script>
import config from './typeConfig';

export default {
  name: 'Exception',
  props: {
    backText: {
      default: 'back to home'
    },
    type: String,
    title: String,
    desc: String,
    img: Image,
    redirect: {
      type: String,
      default: '/'
    }
  },
  data() {
    const { type } = this;
    const pageType = type in config ? type : '404';
    return {
      pageType,
      config
    };
  },
  methods: {}
};
</script>

<style lang="stylus" scoped>
@import '~#/css/variables.styl';

.exception
  display: flex;
  align-items: center;
  height: 80%;
  min-height: 500px;
  .imgBlock
    flex: 0 0 62.5%;
    width: 62.5%;
    padding-right: 152px;
    zoom: 1;
    &:before,
    &:after
      content: ' ';
      display: table;
    &:after
      clear: both;
      visibility: hidden;
      font-size: 0;
      height: 0;
  .imgEle
    height: 360px;
    width: 100%;
    max-width: 430px;
    float: right;
    background-repeat: no-repeat;
    background-position: 50% 50%;
    background-size: contain;
  .content
    flex: auto;
    h1
      color: #434e59;
      font-size: 72px;
      font-weight: 600;
      line-height: 72px;
      margin-bottom: 24px;
    .desc
      color: $grey-7;
      font-size: 20px;
      line-height: 28px;
      margin-bottom: 16px;

@media screen and (max-width: $sizes.xl)
  .exception
    .imgBlock
      padding-right: 88px;

@media screen and (max-width: $sizes.sm)
  .exception
    display: block;
    text-align: center;
    .imgBlock
      padding-right: 0;
      margin: 0 auto 24px;
      margin-bottom: -24px;
      overflow: hidden;
</style>
