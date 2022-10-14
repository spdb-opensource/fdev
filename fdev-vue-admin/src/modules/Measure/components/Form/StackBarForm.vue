<template>
  <fdev-markup-table flat class="table-style">
    <tbody class="table-font-style">
      <fdev-tr class="thead-style">
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
<style lang="stylus" scoped>
.table-style
   min-height:350px
.table-font-style
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
.thead-style
  background: #F4F6FD;
</style>
