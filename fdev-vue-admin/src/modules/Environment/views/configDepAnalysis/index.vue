<template>
  <f-block>
    <fdev-tabs :value="tabs" @input="saveTabs($event)" align="left">
      <fdev-tab name="field" label="实体-属性维度" />
      <fdev-tab name="mapping" label="映射值维度" />
      <fdev-tab name="creatConfigFile" label="生成配置文件" />
    </fdev-tabs>
    <fdev-separator />
    <fdev-tab-panels
      :value="tabs"
      @input="saveTabs($event)"
      animated
      keep-alive
    >
      <fdev-tab-panel name="field">
        <Property />
      </fdev-tab-panel>

      <fdev-tab-panel name="mapping">
        <Mapping />
      </fdev-tab-panel>

      <fdev-tab-panel name="creatConfigFile">
        <CreatConfigFile />
      </fdev-tab-panel>
    </fdev-tab-panels>
  </f-block>
</template>

<script>
import Property from './Property';
import Mapping from './Mapping';
import CreatConfigFile from './CreateConfigFile';
import { mapState, mapMutations } from 'vuex';

export default {
  name: 'ModelEnvList',
  components: {
    Property,
    Mapping,
    CreatConfigFile
  },

  computed: {
    ...mapState('userActionSaveEnv/aloneConfFile', ['tabs'])
  },
  methods: {
    ...mapMutations('userActionSaveEnv/aloneConfFile', ['saveTabs'])
  }
};
</script>
