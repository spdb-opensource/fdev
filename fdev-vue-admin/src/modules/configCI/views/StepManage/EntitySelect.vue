<template>
  <f-dialog
    right
    :value="value"
    @input="$emit('input', false)"
    title="可选实体列表"
  >
    <fdev-layout view="Lhh lpR fff">
      <fdev-page-container>
        <fdev-table
          :data="tableData"
          :columns="columns"
          noExport
          :pagination-label="setPaginationLabel"
        >
          <template v-slot:top-right>
            <f-formitem row label="实体英文名:">
              <fdev-input v-model="filterKey" @keyup.enter="filterEntity">
                <fdev-icon slot="append" name="search" @click="filterEntity" />
              </fdev-input>
            </f-formitem>
          </template>
          <template v-slot:body-cell-operate="props">
            <fdev-td :auto-width="false" style="width:120px">
              <fdev-btn flat label="选择" @click="selectEntity(props.row)" />
            </fdev-td>
          </template>
        </fdev-table>
      </fdev-page-container>
    </fdev-layout>
  </f-dialog>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { modelListColumn } from '../../utils/constants';
export default {
  name: 'EntitySelect',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    templateId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      columns: modelListColumn,
      tableData: [],
      filterKey: ''
    };
  },
  computed: {
    ...mapState('configCIForm', ['modelList'])
  },
  watch: {
    value(val) {
      if (val) {
        this.queryModelList();
      }
    },
    filterKey(val) {
      if (!val) {
        this.tableData = this.modelList.entityModelList.slice();
      }
    }
  },
  methods: {
    /*'queryEntityModel'*/
    ...mapActions('configCIForm', ['querySectionEntity']),
    // 查询实体列表
    async queryModelList() {
      await this.querySectionEntity({
        templateId: this.templateId
      });
      this.tableData = this.modelList.entityModelList.slice();
    },
    // 选择实体事件
    selectEntity(row) {
      this.$emit('receive-entity', row);
      this.$emit('input', false);
    },
    // 列表筛选
    filterEntity() {
      const key = this.filterKey.trim();
      if (key) {
        this.tableData = this.modelList.entityModelList.filter(
          item =>
            item.nameEn.indexOf(key) > -1 ||
            item.nameCn.toLowerCase().includes(key.toLowerCase())
        );
      }
    },
    handleDialogClose() {
      this.$emit('input', false);
    },
    setPaginationLabel(firstRowIndex, endRowIndex, totalRowsNumber) {
      return `${firstRowIndex}-${endRowIndex}/${totalRowsNumber}`;
    }
  }
};
</script>

<style scoped lang="stylus"></style>
