<template>
  <CaseApproval :typeApproval="effective" />
</template>

<script>
import CaseApproval from './compomemts/CaseApproval';

export default {
  name: 'EffectiveApproval',
  components: {
    CaseApproval
  },
  data() {
    return {
      effective: 'effectiveApproval'
    };
  }
};
</script>
<style scoped></style>
