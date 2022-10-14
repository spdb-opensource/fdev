<template>
  <div>
    <tabs-block :data="tabData" :default-tab="tabs" @change="saveTabs">
      <fdev-tab-panel name="modelWeb">
        <WebUnitTab :role="isManagerRole" :id="web_model_id" />
      </fdev-tab-panel>

      <fdev-tab-panel name="model">
        <unit-tab :role="isManagerRole" :id="model_id" />
      </fdev-tab-panel>

      <fdev-tab-panel name="archetype">
        <archetype-tab :role="isManagerRole" :id="archetype_id" />
      </fdev-tab-panel>

      <fdev-tab-panel name="archetypeWeb">
        <WebArchetypeTab :role="isManagerRole" :id="web_archetype_id" />
      </fdev-tab-panel>
    </tabs-block>
  </div>
</template>

<script>
import { mapActions, mapMutations, mapState } from 'vuex';
import TabsBlock from '@/components/TabsBlock';
import unitTab from './unitTab';
import WebUnitTab from './WebUnitTab';
import archetypeTab from './archetypeTab';
import WebArchetypeTab from './WebArchetypeTab';

export default {
  name: 'myFrame',
  components: {
    TabsBlock,
    unitTab,
    WebUnitTab,
    archetypeTab,
    WebArchetypeTab
  },
  data() {
    return {
      tabData: [
        {
          name: 'model',
          label: '后端组件'
        },
        {
          name: 'modelWeb',
          label: '前端组件'
        },
        {
          name: 'archetype',
          label: '后端骨架'
        },
        {
          name: 'archetypeWeb',
          label: '前端骨架'
        }
      ],
      isManagerRole: false,
      model_id: 'model_id',
      web_model_id: 'web_model_id',
      archetype_id: 'archetype_id',
      web_archetype_id: 'web_archetype_id'
    };
  },
  computed: {
    ...mapState('userActionSaveHomePage/myFramePage', ['tabs'])
  },
  methods: {
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    ...mapMutations('userActionSaveHomePage/myFramePage', ['saveTabs'])
  },
  async created() {
    await this.fetchCurrent();
  },
  beforeRouteEnter(to, from, next) {
    const { params } = from;
    if (Object.keys(params).length === 0) {
      sessionStorage.removeItem('archetypeTab');
      sessionStorage.removeItem('unitTab');
    }
    next();
  }
};
</script>
