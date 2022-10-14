<template>
  <fdev-select
    filled
    :value="value"
    @input="$emit('input', $event)"
    type="text"
    dense
    mask="date"
    :hide-dropdown-icon="true"
    :rules="rules"
  >
    <template v-slot:append>
      <f-image :name="iconName" class="cursor-pointer">
        <fdev-popup-proxy
          ref="dateRef"
          transition-show="scale"
          transition-hide="scale"
        >
          <f-date :value="value" @input="dateChange" :options="options" />
        </fdev-popup-proxy>
      </f-image>
    </template>
  </fdev-select>
</template>
<script>
export default {
  name: 'FDate',
  props: {
    rules: {}, //传参同quasar select 中的rules
    value: {}, //日期选择器所选值
    iconName: {
      default: 'event',
      type: String
    } //日期选择器图标name值
  },
  methods: {
    dateChange($event) {
      this.$refs['dateRef'].hide();
      this.$emit('input', $event);
    },
    options(date) {
      let item = null;
      this.$emit('setOptions', date, val => {
        item = val;
      });
      return item;
    }
  }
};
</script>
