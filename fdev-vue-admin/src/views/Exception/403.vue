<template>
  <Exception type="403" desc="抱歉，你无权访问该页面" backText="返回首页" />
</template>

<script>
import Exception from '@/components/Exception';
export default {
  name: 'Exception403',
  components: { Exception },
  data() {
    return {};
  },
  methods: {}
};
</script>

<style></style>
