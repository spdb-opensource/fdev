<template>
  <fdev-markup-table flat>
    <tbody class="table-font-style">
      <fdev-tr class="thead-style">
        <fdev-td class="text-left">所属小组</fdev-td>
        <fdev-td v-for="(item, index) in formHeader" :key="index">{{
          item
        }}</fdev-td>
      </fdev-tr>
      <fdev-tr v-for="(item, index) in iamsFormDate" :key="index.id">
        <fdev-td>{{ item.groupName }}</fdev-td>
        <fdev-td>{{ item.totalUserSum }}</fdev-td>
        <fdev-td>
          {{
            formHeader.includes('占比') ? item.userSum : item.totalTransCount
          }}
        </fdev-td>
        <fdev-td>
          {{
            formHeader.includes('占比')
              ? item.percent + '%'
              : item.avgTransCount
          }}
        </fdev-td>
      </fdev-tr>
    </tbody>
  </fdev-markup-table>
</template>

<script>
export default {
  name: 'IamsGroupForm',
  props: ['formHeader', 'iamsFormDate']
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
