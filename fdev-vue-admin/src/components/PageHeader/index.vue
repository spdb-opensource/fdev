<template>
  <div class="page-header">
    <fdev-breadcrumbs
      class="breadcrumbs"
      separator-color="grey-6"
      active-color="grey-6"
      gutter="xs"
    >
      <fdev-breadcrumbs-el
        class="breadcrumbs-el"
        v-for="crumb in breadCrumbList"
        :key="crumb.path"
        :to="crumb.path"
        :label="crumb.name"
        :icon="crumb.icon"
      />
    </fdev-breadcrumbs>
    <slot />
  </div>
</template>

<script>
export default {
  name: 'PageHeader',
  components: {},
  data() {
    return {
      breadCrumbList: []
    };
  },
  watch: {
    $route() {
      this.convert2BreadcrumbList(this.$route);
    }
  },
  computed: {},
  methods: {
    convert2BreadcrumbList(route) {
      const breadCrumbList = [];
      route.matched.forEach(matched => {
        if (matched.path === '') {
          breadCrumbList.push({
            path: '/',
            name: '工作台',
            icon: 'home'
          });
        } else {
          const keys = Object.keys(route.params);
          let { path } = matched;
          if (keys.length > 0) {
            keys.forEach(key => {
              const reg = new RegExp(':' + key);
              path = path.replace(reg, route.params[key]);
            });
          }
          breadCrumbList.push({
            path: path,
            ...matched.meta
          });
        }
      });
      this.breadCrumbList = breadCrumbList;
    }
  },
  created() {
    this.convert2BreadcrumbList(this.$route);
  },
  mounted() {}
};
</script>

<style lang="stylus" scoped>

.page-header
  background: white;
  padding: 16px 32px 0 32px;
  border-bottom: 1px solid #666666;
.breadcrumbs
  color: #616161;
  margin-bottom: 16px;
  .breadcrumbs-el
    &:hover
      color: #0663BE;
</style>
