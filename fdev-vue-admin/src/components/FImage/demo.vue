<template>
  <div class="row">
    <div
      v-for="icon in iconNames"
      class="column text-red q-ma-xl col-auto items-center"
      :key="icon"
    >
      {{ icon }}<f-image :name="icon" />
    </div>
  </div>
</template>

<script>
import icons from '@/assets/fImage';
export default {
  name: 'FImageDemo',
  data() {
    return {
      iconNames: Object.keys(icons)
    };
  }
};
</script>

<style></style>
