<template>
  <f-block>
    <Loading :visible="loading" class="row no-wrap">
      <fdev-tabs v-model="tab" vertical>
        <fdev-tab name="集成架构" label="集成架构" />
        <fdev-tab name="应用架构" label="应用架构" />
        <fdev-tab name="mpaas" label="mpaas" />
        <fdev-tab name="基础应用" label="基础应用" />
        <fdev-tab name="效能" label="效能" />
        <fdev-tab name="对公金融" label="对公金融" />
      </fdev-tabs>
      <fdev-separator vertical class="q-mx-xl" />
      <fdev-tab-panels
        v-model="tab"
        animated
        swipeable
        vertical
        transition-prev="jump-up"
        transition-next="jump-up"
      >
        <fdev-tab-panel name="集成架构">
          <Main :data="list.IntegratedArch" />
        </fdev-tab-panel>
        <fdev-tab-panel name="应用架构">
          <Main :data="list.AppArch" />
        </fdev-tab-panel>
        <fdev-tab-panel name="mpaas">
          <Main :data="list.mpaas" />
        </fdev-tab-panel>
        <fdev-tab-panel name="基础应用">
          <Main :data="list.AppDev" />
        </fdev-tab-panel>
        <fdev-tab-panel name="效能">
          <Main :data="list.fdev" />
        </fdev-tab-panel>
        <fdev-tab-panel name="对公金融">
          <Main :data="list.finance" />
        </fdev-tab-panel>
      </fdev-tab-panels>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import Main from './Main';
import { list } from './model';
import axios from 'axios';

export default {
  name: 'Homepage',
  components: { Loading, Main },
  data() {
    return {
      loading: false,
      tab: '集成架构',
      list: list
    };
  },
  created() {
    axios
      .get(`fdev-configserver/myapp/default/master/teamPage.json`)
      .then(res => {
        this.list = res.data;
      });
  }
};
</script>
