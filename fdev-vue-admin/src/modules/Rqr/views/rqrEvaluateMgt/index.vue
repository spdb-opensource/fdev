<template>
  <div>
    <tabs-block :data="tabData" :default-tab="todosTab" @change="changeTabs">
      <fdev-tab-panel name="eveluateList">
        <eveluateList ref="eveluateList" />
      </fdev-tab-panel>
      <fdev-tab-panel name="approveList">
        <approveList ref="approveList" />
      </fdev-tab-panel>
    </tabs-block>
  </div>
</template>

<script>
import approveList from './approveList';
import TabsBlock from '@/components/TabsBlock';
import eveluateList from './eveluateList';
import LocalStorage from '#/plugins/LocalStorage';
export default {
  components: { approveList, TabsBlock, eveluateList },
  data() {
    return {
      todosTab: 'eveluateList'
    };
  },
  computed: {
    tabData() {
      return [
        {
          name: 'eveluateList',
          label: `需求评估列表`
        },
        {
          name: 'approveList',
          label: `定稿日期审核列表`
        }
      ];
    }
  },
  methods: {
    changeTabs(val) {
      this.todosTab = val;
      if (val !== 'eveluateList') {
        // 需求评估列表做原进原出
        this.$refs['eveluateList'].saveData();
      }
    }
  },
  // 进入页面之前
  beforeRouteEnter(to, from, next) {
    if (from.path === '/') {
      LocalStorage.remove('rqr/evaMgtFilter');
    }
    next();
  },
  //离开页面之前保存数据
  beforeRouteLeave(to, from, next) {
    if (this.todosTab === 'eveluateList') {
      this.$refs['eveluateList'].saveData();
    }
    next();
  }
};
</script>
