<template>
  <div class="q-pa-md">
    <div class="q-gutter-x-sm">
      <fdev-radio v-model="shape" val="line" label="Line" />
      <fdev-radio v-model="shape" disable val="rectangle" label="Rectangle" />
      <fdev-radio v-model="shape" val="ellipse" label="Ellipse" />
      <fdev-radio v-model="shape" val="polygon" label="Polygon" />
    </div>

    <div class="q-px-sm">
      Your selection is: <strong>{{ shape }}</strong>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FdevRadioDemo',

  data() {
    return {
      shape: 'line'
    };
  }
};
</script>

<style></style>
