<template>
  <NetApproval :typeApproval="KfRemind" :type="type" />
</template>

<script>
import NetApproval from '@/modules/Network/components/NetApproval';

export default {
  name: 'NetworkRemind',
  components: {
    NetApproval
  },
  data() {
    return {
      KfRemind: 'kf_off_batch_approval',
      type: ''
    };
  },
  created() {
    this.type = this.$route.name;
  }
};
</script>
<style lang="stylus" scoped></style>
