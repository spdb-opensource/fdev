<template>
  <fdev-markup-table>
    <tbody>
      <fdev-tr>
        <fdev-td class="text-left">{{ fristLiName }}</fdev-td>
        <fdev-td v-for="(stage, index) of formData.legend" :key="index">
          {{ stage }}
        </fdev-td>
      </fdev-tr>
      <fdev-tr v-for="(xAxi, index) of formData.xAxis" :key="index">
        <fdev-td>{{ xAxi }}</fdev-td>
        <fdev-td
          v-for="(serie, serieIndex) of formData.series"
          :key="serieIndex"
        >
          {{ serie.data[index] }}
        </fdev-td>
      </fdev-tr>
    </tbody>
  </fdev-markup-table>
</template>

<script>
export default {
  name: 'StackBarForm',
  props: {
    formData: {
      type: Object,
      required: true
    },
    fristLiName: {
      type: String,
      required: true
    }
  }
};
</script>
