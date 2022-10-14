<template>
  <f-block>
    <!-- 主体 -->
    <div class="rqrContent">
      <div class="row items-center" style="margin-left: 2px;">
        <fdev-tabs class="orderTabs" v-model="tabs" @input="switchFun($event)">
          <div class="row" v-for="(item, i) in tabsList" :key="i">
            <fdev-tab :name="item.name" :label="item.label" />
          </div>
        </fdev-tabs>
        <fdev-space />
        <!-- 按钮 -->
      </div>
      <fdev-separator />
    </div>
    <div v-if="displayUtilFun" class="content-box">
      <keep-alive>
        <component
          ref="demandContent"
          @init="init"
          :is="currentTabComponent"
          :params="toolNavigationData"
        >
        </component>
      </keep-alive>
    </div>
  </f-block>
</template>
<script>
// 引入tab总入口组件
import total from '@/modules/Network/views/ToolGuide/components/total'; // 全部
import office from '@/modules/Network/views/ToolGuide/components/office'; // 办公
import learn from '@/modules/Network/views/ToolGuide/components/learn'; // 学习
import community from '@/modules/Network/views/ToolGuide/components/community'; // 社区
import development from '@/modules/Network/views/ToolGuide/components/development'; // 开发
// 引入需求tab栏
import { toolTabsList } from '@/modules/Network/utils/constants.js';
// 引入方法
import axios from 'axios';
import { websiteList } from '@/modules/Network/utils/constants.js';
export default {
  components: {
    total,
    office,
    learn,
    community,
    development
  },
  name: 'toolNavigation',
  data() {
    return {
      loading: false, // 等待层
      tabs: this.getDefTabs(), // 默认tabs
      tabsList: toolTabsList, // tab集合
      currentTabComponent: '', // 默认组件
      toolNavigationData: {} // 全部导航数据
    };
  },
  filters: {},
  created() {
    // 调试勿删 后续删除
    this.init();
  },
  computed: {
    // 取消子组件需要监听props
    displayUtilFun() {
      return JSON.stringify(this.toolNavigationData) !== '{}';
    }
  },

  methods: {
    // 初始化操作总入口
    async init() {
      axios
        .get(`fdev-configserver/myapp/default/master/toolNavigation.json`)
        .then(res => {
          this.toolNavigationData = res.data;
        })
        .catch(err => {
          this.toolNavigationData = websiteList;
        });

      this.composeTabListFun();
    },

    // tabsList数据操作总入口
    composeTabListFun(id, tab) {
      this.currentTabComponent = this.getDefTabs();
      this.addComponentsFun();
    },
    // tabs缓存本地
    memoryTabInfoFun() {
      localStorage.setItem('toolguide-tab', this.tabs);
    },
    // 先从缓存获取没有缓存在取data
    getDefTabs() {
      const tab = localStorage.getItem('toolguide-tab');
      if (tab) return tab;
      else return 'total';
    },
    // 添加组件信息
    addComponentsFun() {
      let list = ['total', 'office', 'development', 'learn', 'community'];
      for (let i in this.tabsList) {
        this.$set(this.tabsList[i], 'components', list[i]);
      }
    },
    // 切换tab修改对应组件
    switchFun(v) {
      this.currentTabComponent = this.tabsList.filter(
        e => e.name === v
      )[0].components;
      this.memoryTabInfoFun();
    }
  },
  async mounted() {}
};
</script>
<style lang="stylus" scoped>
.rqrContent
  height 59px
  padding 12px 32px 0px 32px
  background: #fff;
  border-top-right-radius 8px
  border-top-left-radius 8px

.content-box
  .f-block
      border-top-right-radius 0
      border-top-left-radius 0
      padding-top 6px//第一版 4px 第二版6px
</style>
