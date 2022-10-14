<template>
  <div style="height:auto">
    <div
      class="row items-center"
      style="width:500px;"
      :style="{ height: height }"
    >
      <f-ver-btn-toggle
        :options="options"
        v-model="model"
        class="items-center"
        ref="test"
      >
        <template #right>
          <div
            class="bg-cyan row q-ml-xl"
            style="width:10px;word-break:break-all"
          >
            1232141
          </div>
        </template>
      </f-ver-btn-toggle>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FVerBtnToggleDemo',
  data() {
    return {
      options: [
        { label: '且', value: 'and' },
        { label: '或', value: 'or' },
        { label: '非', value: 'not' }
      ],
      model: 'and',
      height: 'auto'
    };
  },
  mounted() {
    this.$nextTick(() => {
      this.height = this.$refs.test.$el.clientHeight + 'px';
    });
  }
};
</script>

<style scoped lang="stylus"></style>
