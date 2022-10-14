<template>
  <Loading :visible="loading">
    <fdev-table
      :data="tableData"
      :columns="columns"
      row-key="id"
      :visible-columns="visibleColumns"
      noExport
      titleIcon="list_s_f"
      class="my-sticky-column-table"
      title="提测信息"
      no-select-cols
    >
      <template v-slot:body-cell-testReason="props">
        <fdev-td>
          {{ props.value ? testReason[props.value] : '-' }}
        </fdev-td>
      </template>
      <template v-slot:body-cell-regressionTestScope="props">
        <fdev-td class="" :title="props.value">
          {{ props.value }}
        </fdev-td>
      </template>
      <template v-slot:body-cell-clientVersion="props">
        <fdev-td class="" :title="props.value">
          {{ props.value }}
        </fdev-td>
      </template>
      <template v-slot:body-cell-testEnv="props">
        <fdev-td class="" :title="props.value">
          {{ props.value }}
        </fdev-td>
      </template>
      <template v-slot:body-cell-repairDesc="props">
        <fdev-td class="ellipsis" :title="props.value">
          {{ props.value }}
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { setVisibleColumns, getVisibleColumns } from '../../utils/setting.js';
import Loading from '@/components/Loading';
export default {
  name: 'TestMsgs',
  components: { Loading },
  data() {
    return {
      loading: false,
      tableData: [],
      columns: [
        {
          name: 'testReason',
          field: 'testReason',
          label: '测试原因',
          align: 'left'
        },
        {
          name: 'regressionTestScope',
          field: 'regressionTestScope',
          label: '回归测试范围',
          align: 'left'
        },
        {
          name: 'clientVersion',
          field: 'clientVersion',
          label: '客户端版本',
          align: 'left'
        },
        {
          name: 'testEnv',
          field: 'testEnv',
          label: '测试环境',
          align: 'left'
        },
        {
          name: 'repairDesc',
          field: 'repairDesc',
          label: '功能描述',
          align: 'left'
        },
        {
          name: 'interfaceChange',
          field: 'interfaceChange',
          label: '接口变更',
          align: 'left'
        },
        {
          name: 'databaseChange',
          field: 'databaseChange',
          label: '数据库变更',
          align: 'left'
        },
        {
          name: 'otherSystemChange',
          field: 'otherSystemChange',
          label: '其他系统变更',
          align: 'left'
        },
        {
          name: 'createTime',
          field: 'createTime',
          label: '提测时间',
          align: 'left'
        }
      ],
      visibleColumns: this.visibleColumnOptions,
      testReason: {
        '1': '正常',
        '2': '缺陷',
        '3': '需求变更'
      }
    };
  },
  watch: {
    visibleColumns(val) {
      setVisibleColumns('testMsg', val);
    }
  },
  computed: {
    ...mapState('dashboard', ['taskSitMsg']),
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(0, columns.length - 1);
    }
  },
  methods: {
    ...mapActions('dashboard', ['queryTaskSitMsg']),
    async init() {
      this.loading = true;
      await this.queryTaskSitMsg({
        taskNo: this.$route.params.id
      });
      this.tableData = this.taskSitMsg;
      this.loading = false;
    }
  },
  created() {
    this.init();
  },
  mounted() {
    const tempVisibleColumns = this.visibleColumnOptions;
    this.visibleColumns = getVisibleColumns('testMsg');
    if (!this.visibleColumns || this.visibleColumns.length <= 2) {
      this.visibleColumns = tempVisibleColumns;
    }
  }
};
</script>

<style lang="stylus" scoped></style>
