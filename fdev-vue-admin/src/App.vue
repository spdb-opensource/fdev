<template>
  <div id="app">
    <router-view />
    <f-back-to-top />
  </div>
</template>

<script>
export default {
  name: 'App',
  components: {},
  data() {
    return {};
  },
  computed: {},
  methods: {},
  async created() {
    this.$store.commit('authorized/reloadAuthorized');
    //await this.$store.dispatch('authorized/fetch');
  },
  mounted() {}
};
</script>
<style></style>
