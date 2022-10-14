<template>
  <f-dialog
    :title="title"
    right
    f-sc
    :value="value"
    @input="$emit('input', $event)"
  >
    <Loading :visible="loading['componentForm/queryComponentRecordHis']">
      <fdev-table
        style="width:100%"
        row-key="id"
        :data="tableData"
        :columns="columns"
        :pagination.sync="pagination"
        noExport
        no-select-cols
      >
      </fdev-table>
    </Loading>
  </f-dialog>
</template>

<script>
import Loading from '@/components/Loading';
import { modifyVersionColumns } from '../../../utils/constants';
import { mapActions, mapState } from 'vuex';

export default {
  name: 'ModifyVersionDialog',
  props: ['value', 'title', 'data'],
  components: { Loading },
  data() {
    return {
      columns: modifyVersionColumns,
      componentRecordHisaaa: [],
      pagination: {
        rowsPerPage: 5
      }
    };
  },
  watch: {
    async value(val) {
      if (val === true) {
        this.getRecordList();
      }
    }
  },
  computed: {
    ...mapState('componentForm', ['componentRecordHis']),
    ...mapState('global', ['loading']),
    // 推荐版本历史信息数据
    tableData() {
      if (this.componentRecordHis.length === 0) {
        return [];
      } else {
        return this.componentRecordHis.slice();
      }
    }
  },
  methods: {
    ...mapActions('componentForm', ['queryComponentRecordHis']),
    async getRecordList() {
      const params = {
        component_id: this.data.id
      };
      await this.queryComponentRecordHis(params);
    }
  }
};
</script>
