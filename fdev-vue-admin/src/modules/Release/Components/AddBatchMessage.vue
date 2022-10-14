<template>
  <f-dialog
    @input="$emit('input', false)"
    :value="value"
    title="绑定应用修改"
    right
    :persistent="false"
  >
    <div>以下应用将一并修改，请确认已与联系人沟通一致。</div>
    <fdev-table
      title="应用列表"
      titleIcon="list_s_f"
      :data="data"
      :columns="columns"
      row-key="application_id"
      hide-bottom
      :pagination.sync="pagination"
    >
    </fdev-table>
    <template v-slot:btnSlot>
      <fdev-btn dialog @click="$emit('input', false)" label="确定" />
    </template>
  </f-dialog>
</template>

<script>
export default {
  props: {
    data: Array,
    value: Boolean
  },
  data() {
    return {
      pagination: {
        rowsPerPage: 0
      },
      columns: [
        {
          name: 'app_name_en',
          label: '应用',
          field: 'app_name_en'
        },
        {
          name: 'bind_people',
          label: '联系人',
          field: 'bind_people'
        }
      ]
    };
  }
};
</script>

<style lang="stylus" scoped>

.table
  width 100%
  border 1px solid #bdbdbd
  th, td
    text-align left
    padding 5px
</style>
