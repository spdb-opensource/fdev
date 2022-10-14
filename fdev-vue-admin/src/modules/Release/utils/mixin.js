export const watchRouteParams = {
  watch: {
    '$route.params.release_date': {
      handler(val) {
        this.init();
      }
    }
  }
};
