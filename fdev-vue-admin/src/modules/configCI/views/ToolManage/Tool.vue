<template>
  <f-block>
    <fdev-tabs v-model="tab">
      <fdev-tab
        v-for="item in tempTabs"
        :key="item.name"
        :name="item.name"
        :label="item.label"
      />
    </fdev-tabs>
    <fdev-separator />
    <fdev-tab-panels v-model="tab" class="q-mt-md">
      <fdev-tab-panel name="pipelineTemp" class="q-pa-none">
        <pipelineTemp />
      </fdev-tab-panel>
      <fdev-tab-panel name="jobTemp" class="q-pa-none">
        <Jobs />
      </fdev-tab-panel>
      <fdev-tab-panel name="runner" class="q-pa-none">
        <Runner />
      </fdev-tab-panel>
      <fdev-tab-panel name="image" class="q-pa-none">
        <EnvImage />
      </fdev-tab-panel>
    </fdev-tab-panels>
  </f-block>
</template>

<script>
import pipelineTemp from './pipelineTemp.vue';
import Jobs from './Jobs.vue';
import Runner from './Runner.vue';
import EnvImage from './EnvImage.vue';

export default {
  name: 'toolbox',
  components: { pipelineTemp, Jobs, Runner, EnvImage },
  data() {
    return {
      tempTabs: [
        {
          name: 'pipelineTemp',
          label: '流水线模板'
        },
        {
          name: 'jobTemp',
          label: '任务模板'
        },
        {
          name: 'runner',
          label: 'runner集群'
        },
        {
          name: 'image',
          label: '工作环境镜像'
        }
      ],
      tab: 'pipelineTemp'
    };
  }
};
</script>
