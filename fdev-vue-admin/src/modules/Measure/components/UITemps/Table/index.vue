<template>
  <fdev-table
    noExport
    class="full-width q-mt-md my-sticky-head-table"
    style="max-height:85%"
    :columns="dataSource.columns"
    :data="dataSource.data"
    noSelectCols
    :pagination="{
      page: 1,
      rowsPerPage: 0
    }"
    hide-bottom
  />
</template>

<script>
export default {
  name: 'Table',
  props: {
    dataSource: Object
  }
};
</script>

<style></style>
