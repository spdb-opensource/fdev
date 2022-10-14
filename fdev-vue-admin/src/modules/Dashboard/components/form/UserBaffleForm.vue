<template>
  <fdev-markup-table flat>
    <tbody class="table-font-style">
      <fdev-tr class="thead-style text-left">
        <fdev-td>人员</fdev-td>
        <fdev-td>
          交易量
        </fdev-td>
      </fdev-tr>
      <fdev-tr v-for="(item, index) of formData" :key="index">
        <fdev-td>{{ item.name }}</fdev-td>
        <fdev-td>{{ item.sum }}</fdev-td>
      </fdev-tr>
    </tbody>
  </fdev-markup-table>
</template>

<script>
export default {
  name: 'UserBaffleForm',
  props: {
    formData: {
      type: Array,
      required: true
    }
  }
};
</script>
<style scoped lang="stylus">
.table-font-style
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
.thead-style
  background: #f4f6fd;
</style>
