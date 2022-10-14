<template>
  <NetApproval :typeApproval="KfOffApproval" :type="type" />
</template>

<script>
import NetApproval from '@/modules/Network/components/NetApproval';

export default {
  name: 'NetworkClose',
  components: {
    NetApproval
  },
  data() {
    return {
      KfOffApproval: 'kf_off_approval',
      type: ''
    };
  },
  created() {
    this.type = this.$route.name;
  }
};
</script>
<style lang="stylus" scoped></style>
