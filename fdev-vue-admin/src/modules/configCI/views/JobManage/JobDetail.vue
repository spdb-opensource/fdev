<template>
  <div class="container">
    <f-dialog
      :value="value"
      @input="$emit('input', false)"
      :title="type === 'edit' ? '编辑任务' : '任务详情'"
    >
      <JobEdit
        :job-info="jobInfo"
        :type="type"
        @receive-job="getJobInfo"
        @close="handleDialogClose"
      />
    </f-dialog>
  </div>
</template>

<script>
import JobEdit from './JobEdit';

export default {
  name: 'JobDetail',
  components: { JobEdit },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    jobInfo: {
      type: Object,
      default: () => ({})
    },
    type: {
      type: String,
      default: 'edit'
    }
  },
  data() {
    return {};
  },
  methods: {
    getJobInfo(jobInfo) {
      this.$emit('receive-job-info', jobInfo);
      this.handleDialogClose();
    },
    handleDialogClose() {
      this.$emit('input', false);
    }
  }
};
</script>

<style scoped lang="stylus"></style>
