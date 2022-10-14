<template>
  <VirtualApproval
    :typeApproval="VmApproval"
    :typeColumns="VMColumns"
    :type="type"
  />
</template>

<script>
import VirtualApproval from '@/modules/Network/components/VirtualApproval';
import { vmColumns } from '@/modules/Network/utils/constants';

export default {
  name: 'VMApproval',
  components: {
    VirtualApproval
  },
  data() {
    return {
      VmApproval: 'vm_approval',
      VMColumns: vmColumns(),
      type: ''
    };
  },
  created() {
    this.type = this.$route.name;
  }
};
</script>
<style lang="stylus" scoped></style>
