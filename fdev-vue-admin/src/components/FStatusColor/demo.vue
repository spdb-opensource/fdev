<template>
  <div class="row">
    <fdev-table
      class="q-mr-md"
      title="状态颜色列表"
      :columns="columns"
      :data="tableData"
      noExport
      no-select-cols
    >
      <template v-slot:body-cell-status="props">
        <fdev-td>
          <f-status-color
            :color="props.row.status | statusColorFilter"
          ></f-status-color>
          {{ props.row.status | statusFilter }}
        </fdev-td>
      </template>
    </fdev-table>
    <fdev-table
      noExport
      no-select-cols
      title="状态颜色列表(渐变)"
      :columns="columns"
      :data="tableData"
    >
      <template v-slot:body-cell-status="props">
        <fdev-td>
          <f-status-color
            :gradient="props.row.status | statusGradientColorFilter"
          ></f-status-color>
          {{ props.row.status | statusFilter }}
        </fdev-td>
      </template>
    </fdev-table>
  </div>
</template>

<script>
export default {
  name: 'FStatusColorDemo',
  data() {
    return {
      columns: [
        {
          name: 'name',
          label: '名称',
          field: 'name'
        },
        {
          name: 'status',
          label: '状态',
          field: 'status'
        }
      ],
      tableData: [
        {
          name: 'xxxx',
          status: 1
        },
        {
          name: 'xxxx',
          status: 2
        }
      ]
    };
  },
  filters: {
    statusFilter(val) {
      return val === 1 ? '开启' : '关闭';
    },
    statusColorFilter(val) {
      return val === 1 ? '#0F0' : '#F00';
    },
    statusGradientColorFilter(val) {
      return val === 1
        ? 'linear-gradient(270deg, rgba(74,102,219,0.50) 0%, #4A66DB 100%)'
        : 'linear-gradient(270deg, rgba(77,187,89,0.50) 0%, #4DBB59 100%)';
    }
  }
};
</script>

<style></style>
