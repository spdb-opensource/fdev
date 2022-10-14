<template>
  <VirtualApproval
    :typeApproval="VDIApproval"
    :typeColumns="VDIColumns"
    :type="type"
  />
</template>

<script>
import VirtualApproval from '@/modules/Network/components/VirtualApproval';
import { vdiColumns } from '@/modules/Network/utils/constants';

export default {
  name: 'VDIApproval',
  components: {
    VirtualApproval
  },
  data() {
    return {
      VDIApproval: 'vdi_approval',
      VDIColumns: vdiColumns(),
      type: ''
    };
  },
  created() {
    this.type = this.$route.name;
  }
};
</script>
<style lang="stylus" scoped></style>
