<template>
  <NetApproval :typeApproval="KfApproval" :type="type" />
</template>

<script>
import NetApproval from '@/modules/Network/components/NetApproval';

export default {
  name: 'NetworkOpen',
  components: {
    NetApproval
  },
  data() {
    return {
      KfApproval: 'kf_approval',
      type: ''
    };
  },
  created() {
    this.type = this.$route.name;
  }
};
</script>
<style lang="stylus" scoped></style>
