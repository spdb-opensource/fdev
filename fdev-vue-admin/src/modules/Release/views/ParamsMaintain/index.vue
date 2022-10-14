<template>
  <f-block>
    <Loading class="bg-white">
      <fdev-tabs
        class="q-mb-md"
        :value="tab"
        align="left"
        @input="updateTab($event)"
      >
        <fdev-tab name="0" label="模块类型（变更目录）" />
        <fdev-tab name="1" label="脚本参数" />
        <fdev-tab name="2" label="环境维护" />
      </fdev-tabs>

      <fdev-tab-panels :value="tab" @input="updateTab($event)" animated>
        <fdev-tab-panel name="0">
          <ModuleType :isManager="isManager" />
        </fdev-tab-panel>
        <fdev-tab-panel name="1">
          <ScriptParams :isManager="isManager" />
        </fdev-tab-panel>
        <fdev-tab-panel name="2">
          <AutomationEnv :isManager="isManager" />
        </fdev-tab-panel>
      </fdev-tab-panels>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import ModuleType from './ModuleType';
import ScriptParams from './ScriptParams';
import AutomationEnv from './AutomationEnv';
import { mapState, mapGetters, mapMutations } from 'vuex';
export default {
  name: 'AutoRelease',
  components: {
    Loading,
    ModuleType,
    ScriptParams,
    AutomationEnv
  },
  data() {
    return {};
  },
  computed: {
    ...mapState('userActionSaveRelease/paramsModuleType', ['tab']),
    ...mapState('user', ['currentUser']),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    isManager() {
      return (
        this.currentUser.role.some(role => {
          return role.name === '投产管理员';
        }) || this.isKaDianManager
      );
    }
  },
  methods: {
    ...mapMutations('userActionSaveRelease/paramsModuleType', ['updateTab'])
  }
};
</script>
