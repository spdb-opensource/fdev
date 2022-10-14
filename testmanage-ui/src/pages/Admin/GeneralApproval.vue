<template>
  <CaseApproval :typeApproval="general" />
</template>

<script>
import CaseApproval from './compomemts/CaseApproval';

export default {
  name: 'GeneralApproval',
  components: {
    CaseApproval
  },
  data() {
    return {
      general: 'generalApproval'
    };
  }
};
</script>
<style scoped></style>
