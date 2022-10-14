<template>
  <div v-if="appInfo.appCiType === 'fdev-ci'">
    <PipelineList :appId="applicationId" />
  </div>

  <div class="q-mt-md" v-else>
    <div class="tabSty">
      <fdev-tabs v-model="tabpanel">
        <fdev-tab name="logsTab" label="流水线日志" />
        <fdev-tab
          name="proPipelines"
          label="项目流水线"
          v-if="showAll && appInfo.appCiType === 'fdev-ci' && isAppManager"
        />
        <Authorized
          :include-me="managerArr"
          v-if="
            showAll &&
              (!appInfo.appCiType || appInfo.appCiType === 'git-ci') &&
              !appInfo.status
          "
        >
          <fdev-tab name="customDeploy" label="自定义部署" />
        </Authorized>
      </fdev-tabs>
    </div>

    <fdev-tab-panels v-model="tabpanel">
      <fdev-tab-panel name="logsTab">
        <pipelinesLogs
          :application-id="applicationId"
          :app-info="appInfo"
        ></pipelinesLogs>
      </fdev-tab-panel>

      <fdev-tab-panel name="proPipelines">
        <appPipelineList :applicationId="applicationId"></appPipelineList>
      </fdev-tab-panel>

      <fdev-tab-panel name="customDeploy">
        <customDeploy
          :id="appInfo.id"
          :gitlab_project_id="appInfo.gitlab_project_id"
          :type="appInfo.type_name"
        />
      </fdev-tab-panel>
    </fdev-tab-panels>
  </div>
</template>
<script>
// import PipelineList from '../../../../../public/postcssPages/ConfigCINew/components/PipelineList';
import PipelineList from '@/modules/configCI/views/PipelineList/index';
import pipelinesLogs from '@/modules/App/components/pipelinesLogs';
import appPipelineList from '@/modules/App/views/pipelines/components/appPipelineList';
import Authorized from '@/components/Authorized';
import customDeploy from '@/modules/App/views/pipelines/components/customDeploy';
import { mapState } from 'vuex';

export default {
  name: '',
  components: {
    pipelinesLogs,
    appPipelineList,
    customDeploy,
    Authorized,
    PipelineList
  },
  props: {
    applicationId: String,
    appInfo: Object,
    showAll: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      tabpanel: 'logsTab'
    };
  },
  computed: {
    ...mapState('user', ['currentUser']),
    managerArr() {
      if (Object.keys(this.appInfo).length > 0) {
        return [
          ...this.appInfo.dev_managers.map(user => user.id),
          ...this.appInfo.spdb_managers.map(user => user.id)
        ];
      } else {
        return [];
      }
    },
    isAppManager() {
      return this.managerArr.includes(this.currentUser.id);
    }
  },
  methods: {
    changeTab(val) {}
  }
};
</script>
<style lang="stylus" scoped>
.q-tab-panels {
  box-shadow: unset;
}
.tabSty {
  display: flex;
}
</style>
