<template>
  <div class="row no-wrap">
    <f-sidelist
      init
      title="components"
      @clickItem="changeComponent"
      :dataSource="CompNames"
      search
    />
    <!-- <fdev-table-demo class="q-ma-lg" /> -->
    <components :is="demoName" class="q-ma-xl" />
  </div>
</template>

<script>
import { CompNames, CompDemos } from './index';
export default {
  name: 'CompDoc',
  components: { ...CompDemos },
  computed: {
    CompNames() {
      return CompNames.map(name => {
        return { label: name.replace('Demo', '') };
      });
    }
  },
  created() {},
  data() {
    return {
      demoName: ''
    };
  },
  methods: {
    changeComponent(evt) {
      let { item } = evt;
      this.demoName = item.label + 'Demo';
    }
  }
};
</script>

<style></style>
