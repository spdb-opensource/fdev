<template>
  <div class="q-pa-md">
    <tabs-block :data="tabData" default-tab="page2">
      <fdev-tab-panel name="page1">
        <page1 />
      </fdev-tab-panel>

      <fdev-tab-panel name="page2">
        <page2 />
      </fdev-tab-panel>
    </tabs-block>
  </div>
</template>

<script>
import TabsBlock from './index';
import page1 from './page1';
import page2 from './page2';

export default {
  components: { TabsBlock, page1, page2 },
  tabData() {
    return {
      data: [
        {
          name: 'page1',
          label: '页面1'
        },
        {
          name: 'page2',
          label: '页面2'
        }
      ]
    };
  }
};
</script>
<style lang="stylus" scoped></style>
