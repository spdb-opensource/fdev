<template>
  <f-block>
    <app-tab :role="isManagerRole" :id="env_id" />
  </f-block>
</template>

<script>
import { mapActions } from 'vuex';
import appTab from './appTab';

export default {
  name: 'myApp',
  components: {
    appTab
  },
  data() {
    return {
      tab: JSON.parse(sessionStorage.getItem('tab')) || 'myApp',
      isManagerRole: false,
      env_id: ''
    };
  },
  methods: {
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    })
  },
  async created() {
    this.env_id = this.$route.query.env_id;
    await this.fetchCurrent();
  },
  beforeRouteEnter(to, from, next) {
    const { params } = from;
    if (Object.keys(params).length === 0) {
      sessionStorage.removeItem('tab');
      sessionStorage.removeItem('appTab');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    const { params } = to;
    if (Object.keys(params).length) {
      sessionStorage.setItem('tab', JSON.stringify(this.tab));
    }
    next();
  }
};
</script>

<style lang="stylus" scoped></style>
