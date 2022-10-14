<template>
  <fdev-markup-table>
    <tbody>
      <fdev-tr>
        <fdev-td>人员</fdev-td>
        <fdev-td v-for="(item, index) in formHeader" :key="index">
          {{ item }}
        </fdev-td>
      </fdev-tr>
      <fdev-tr v-for="(id, index) of formData.xAxisId" :key="index">
        <fdev-td>{{ getUserName(id) }}</fdev-td>
        <fdev-td>
          {{
            formData[position][id].create || formData[position][id].todo || 0
          }}
        </fdev-td>
        <fdev-td>{{ formData[position][id].develop }}</fdev-td>
        <fdev-td>{{ formData[position][id].sit }}</fdev-td>
        <fdev-td>{{ formData[position][id].uat }}</fdev-td>
        <fdev-td>{{ formData[position][id].rel }}</fdev-td>
        <fdev-td>{{ formData[position][id].production }}</fdev-td>
      </fdev-tr>
    </tbody>
  </fdev-markup-table>
</template>

<script>
export default {
  name: 'UserTaskForm',
  props: {
    formData: {
      type: Object,
      required: true
    },
    formHeader: {
      type: Array,
      required: true
    },
    position: {
      type: String,
      required: true
    },
    getUserName: {
      type: Function,
      required: true
    }
  }
};
</script>
