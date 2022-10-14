<template>
  <CaseApproval :typeApproval="pass" />
</template>

<script>
import CaseApproval from './compomemts/CaseApproval';

export default {
  name: 'PassApproval',
  components: {
    CaseApproval
  },
  data() {
    return {
      pass: 'passApproval'
    };
  }
};
</script>
<style scoped></style>
