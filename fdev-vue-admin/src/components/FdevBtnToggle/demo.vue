<template>
  <div class="column">
    {{ model }}
    <!-- <fdev-btn-toggle
      v-model="model"
      toggle-color="primary"
      color="white"
      text-color="black-0"
      unelevated
      :options="[
        { label: 'One', value: 'one' },
        { label: 'Two', value: 'two' },
        { label: 'Three', value: 'three' }
      ]"
    /> -->
    <fdev-btn-toggle
      v-model="model"
      :options="[
        { label: 'One', value: 'one' },
        { label: 'Two', value: 'two' },
        { label: 'Three', value: 'three' }
      ]"
    />
  </div>
</template>

<script>
export default {
  name: 'FdevBtnToggleDemo',
  data() {
    return { model: 'one' };
  }
};
</script>

<style></style>
