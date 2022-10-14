<template>
  <div>
    <f-dialog
      :value="value"
      transition-show="slide-up"
      transition-hide="slide-down"
      class="q-dialog-per"
      @input="$emit('input', $event)"
      title="sonar扫描"
    >
      <f-formitem label="应用名称" :label-col="3">
        <div>{{ appName }}</div>
      </f-formitem>
      <f-formitem label="扫描分支" :label-col="3">
        <div>{{ branchName }}</div>
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn dialog label="sonar扫描" :loading="loading" @click="submit" />
      </template>
    </f-dialog>
    <f-dialog
      v-model="sonarResOpen"
      transition-show="slide-up"
      transition-hide="slide-down"
      class="q-dialog-per bg-white"
      title="sonar扫描提示"
    >
      <div class="q-px-lg">
        {{ scanBranchSonarRes }}
      </div>
      <template v-slot:btnSlot>
        <fdev-btn flat @click="sonarResOpen = false" label="知道啦" />
      </template>
    </f-dialog>
  </div>
</template>
<script>
import { mapActions, mapState } from 'vuex';

export default {
  name: 'SonarDialog',
  data() {
    return {
      loading: false,
      sonarResOpen: false
    };
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    appName: {
      type: String,
      default: ''
    },
    branchName: {
      type: String // 任务模块会传
    },
    appId: {
      type: String
    },
    id: {
      type: String
    }
  },
  watch: {},
  computed: {
    ...mapState('appForm', ['scanBranchSonarRes'])
  },
  methods: {
    ...mapActions('appForm', ['scanningFeatureBranch']),

    async submit() {
      this.loading = true;
      // sonar手动扫描,只需要发个接口 接口还没给出来
      let params = {
        id: this.appId,
        branch_name: this.branchName
      };
      await this.scanningFeatureBranch(params);
      this.$emit('input', false);
      this.loading = false;
      this.sonarResOpen = true;
    }
  }
};
</script>

<style lang="stylus" scoped>
.dialog-height
  height 350px
  min-height 350px
  max-height 350px;
.sonar-dialog-height
  height 250px
  min-height 250px
  max-height 250px;
.btn-top
  margin-top 80px
</style>
