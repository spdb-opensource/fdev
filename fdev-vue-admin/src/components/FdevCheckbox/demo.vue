<template>
  <div>
    <div class="row q-gutter-x-lg q-mb-xl">
      <fdev-checkbox v-model="teal" label="teal" /><br />
      <fdev-checkbox v-model="orange" label="orange" /><br />
      <fdev-checkbox v-model="red" label="red" disable /><br />
      <fdev-checkbox v-model="cyan" label="cyan" disable /><br />
      <fdev-checkbox v-model="yellow" label="yellow" /><br />
    </div>

    <div>Array Model:</div>
    <div class="row q-gutter-x-lg items-center">
      <fdev-checkbox v-model="selection" label="orange" val="orange" /><br />
      <fdev-checkbox v-model="selection" label="red" disable val="red" /><br />
      <fdev-checkbox v-model="selection" label="teal" /><br />
      <fdev-checkbox v-model="selection" label="cyan" disable /><br />
      <fdev-checkbox v-model="selection" label="yellow" /><br />
    </div>
  </div>
</template>

<script>
export default {
  name: 'FdevCheckboxDemo',
  data() {
    return {
      teal: true,
      orange: false,
      red: false,
      cyan: true,
      yellow: null,
      selection: ['orange', 'red']
    };
  }
};
</script>

<style></style>
