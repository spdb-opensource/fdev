<template>
  <f-block>
    <fdev-tabs class="q-mb-md" v-model="tab" align="left" @input="changeTab">
      <fdev-tab name="release" label="投产窗口" />
      <fdev-tab name="bigRelease" label="投产大窗口" />
    </fdev-tabs>

    <fdev-tab-panels v-model="tab" animated>
      <fdev-tab-panel name="release">
        <release />
      </fdev-tab-panel>

      <fdev-tab-panel name="bigRelease">
        <big-release />
      </fdev-tab-panel>
    </fdev-tab-panels>
  </f-block>
</template>

<script>
import Release from '../ReleaseWindow';
import BigRelease from '../ReleaseBigWindow';
export default {
  components: { Release, BigRelease },
  data() {
    return {
      tab: sessionStorage.getItem('tabRelease') || 'release'
    };
  },
  methods: {
    changeTab(val) {
      sessionStorage.setItem('tabRelease', val);
    }
  }
};
</script>
