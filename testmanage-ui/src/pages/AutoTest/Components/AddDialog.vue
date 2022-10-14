<template>
  <div>
    <Dialog>
      <el-form :inline="true" label-width="40%" label-position="left">
        <el-form-item
          v-for="(formData, index) in formItems"
          :key="index"
          :label="formData.label"
          :prop="formData.prop"
        >
          <el-input />
        </el-form-item>
      </el-form>
    </Dialog>
  </div>
</template>
<script>
export default {
  data() {
    return {};
  },
  created() {},
  props: {
    formItems: {
      default: () => [],
      type: Array
    }
  }
};
</script>
