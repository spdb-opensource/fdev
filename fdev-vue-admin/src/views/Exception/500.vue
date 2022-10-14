<template>
  <Exception type="500" desc="抱歉，服务器出错了" backText="返回首页" />
</template>

<script>
import Exception from '@/components/Exception';
export default {
  name: 'Exception500',
  components: { Exception },
  data() {
    return {};
  },
  methods: {}
};
</script>

<style></style>
