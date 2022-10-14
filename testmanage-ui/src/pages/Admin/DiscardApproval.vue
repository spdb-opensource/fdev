<template>
  <CaseApproval :typeApproval="discard" />
</template>

<script>
import CaseApproval from './compomemts/CaseApproval';

export default {
  name: 'DiscardApproval',
  components: {
    CaseApproval
  },
  data() {
    return {
      discard: 'discardApproval'
    };
  }
};
</script>
<style scoped></style>
