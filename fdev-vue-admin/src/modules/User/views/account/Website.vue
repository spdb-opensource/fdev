<template>
  <f-block>
    <div class=" Website">
      <div class="text-h6">常用网址</div>
      <div class="row">
        <div
          v-for="(link, index) in list"
          :key="index"
          class="col-3 q-mt-lg text-primary q-pl-md"
        >
          <a :href="link.url" class="text-primary" target="_blank">
            <img :src="link.icon" alt="" />
            {{ link.name }}
          </a>
        </div>
      </div>
    </div>
    <div class="website q-mt-xl">
      <div class="text-h6">接入手册</div>
      <div class="row">
        <div
          v-for="(link, index) in handbookList"
          :key="index"
          class="col-3 q-mt-lg text-primary q-pl-md"
        >
          <a :href="link.url" class="text-primary" target="_blank">
            <img :src="link.icon" alt="" />
            {{ link.name }}
          </a>
        </div>
      </div>
    </div>
  </f-block>
</template>

<script>
import { websiteList, handbook } from '@/modules/User/utils/model';
import axios from 'axios';
export default {
  name: 'Website',
  data() {
    return {
      list: [],
      handbookList: []
    };
  },
  created() {
    axios
      .get(`fdev-configserver/myapp/default/master/blogroll.json`)
      .then(res => {
        this.list = res.data.blogroll;
        this.handbookList = res.data.handbook;
      })
      .catch(err => {
        this.list = websiteList;
        this.handbookList = handbook;
      });
  }
};
</script>

<style lang="stylus" scoped>

.Website
  img
    width 16px
    vertical-align middle
  a:visited
    color #0663BE
  a:hover
    text-decoration underline
.title
  padding 10px 0 0 10px
</style>
