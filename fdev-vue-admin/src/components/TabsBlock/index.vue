<template>
  <div>
    <div class="container bg-white">
      <div class="main">
        <div class="row justify-between items-center">
          <fdev-tabs v-model="tab" @input="$emit('change', $event)">
            <fdev-tab
              v-for="(item, index) in data"
              :key="index"
              :name="item.name"
              :label="item.label"
            />
          </fdev-tabs>
        </div>
        <fdev-separator />
      </div>
    </div>

    <f-block block>
      <div>
        <fdev-tab-panels v-model="tab">
          <slot />
        </fdev-tab-panels>
      </div>
    </f-block>
  </div>
</template>

<script>
export default {
  name: 'TabsBlock',
  props: {
    data: {
      type: Array,
      default: () => []
    },
    defaultTab: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      tab: ''
    };
  },
  created() {
    this.tab =
      this.data.length > 0
        ? this.defaultTab
          ? this.defaultTab
          : this.data[0].name
        : '';
  }
};
</script>

<style lang="stylus" scoped>
.container
  border-radius 4px
  padding 0 32px
  margin-bottom 10px
  .main
    padding 16px 0 10px 0px
</style>
