<template>
  <fdev-markup-table>
    <tbody>
      <fdev-tr>
        <fdev-td class="text-left">所属小组</fdev-td>
        <fdev-td v-for="(xAxi, index) of formData.xAxis" :key="index">
          {{ xAxi }}
        </fdev-td>
      </fdev-tr>
      <fdev-tr v-for="(serie, index) of formData.series" :key="index">
        <fdev-td>{{ serie.name }}</fdev-td>
        <fdev-td v-for="(data, dataIndex) of serie.data" :key="dataIndex">
          {{ data }}
        </fdev-td>
      </fdev-tr>
    </tbody>
  </fdev-markup-table>
</template>

<script>
export default {
  name: 'LineForm',
  props: {
    formData: {
      type: Object,
      required: true
    }
  }
};
</script>
