<template>
  <div>
    <div class="text-h5 q-mb-lg">色板</div>
    <div class="q-gutter-lg row">
      <div
        style="width:100px;height:100px"
        v-for="color in colors"
        :key="color"
        :class="`bg-${color}`"
      >
        <span
          class="text-subtitle1"
          :class="{ 'text-white': !color.includes('grey-0') }"
          >{{ color }}</span
        >
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ConfigColorDemo',
  data() {
    return {
      colors: [
        [4, 6].map(x => 'green-' + x),
        [3, 4].map(x => 'cyan-' + x),
        [4, 6, 8].map(x => 'indigo-' + x),
        [6, 7, 8, 9, 10].map(x => 'blue-' + x),
        [3].map(x => 'blue-grey-' + x),
        [0].map(x => 'black-' + x),
        [0, 1, 2, 3].map(x => 'grey-' + x),
        [0].map(x => 'indigogrey-' + x),
        [3, 4, 5, 6, 7].map(x => 'red-' + x),
        [3, 7].map(x => 'orange-' + x)
      ].reduce((acc, curr) => acc.concat(curr))
    };
  }
};
</script>

<style></style>
