<template>
  <f-block>
    <Loading :visible="loading">
      <div style="padding-bottom:10px">
        <f-icon
          name="list_s_f"
          class="text-primary mr12"
          :width="16"
          :height="16"
        ></f-icon>
        <span class="infoStyle">提测信息</span>
      </div>
      <fdev-table
        :data="tableData"
        :columns="columns"
        row-key="id"
        :visible-columns="visibleColumns"
        noExport
        hide-bottom
        class="my-sticky-column-table h200"
        no-select-cols
      >
        <template v-slot:body-cell-testReason="props">
          <fdev-td>
            {{ props.value ? testReason[props.value] : '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-regressionTestScope="props">
          <fdev-td class="ellipsis" :title="props.value">
            {{ props.value }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-clientVersion="props">
          <fdev-td class="ellipsis" :title="props.value">
            {{ props.value }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-testEnv="props">
          <fdev-td class="ellipsis" :title="props.value">
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
  </f-block>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import {
  setVisibleColumns,
  getVisibleColumns
} from '../../../../utils/setting.js';
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
      try {
        await this.queryTaskSitMsg({
          taskNo: this.$route.params.id
        });
        this.tableData = this.taskSitMsg;
        this.loading = false;
      } catch (error) {
        this.loading = false;
      }
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

<style lang="stylus" scoped>
.h110
  height 220px
.mr12
  margin-right 12px
.infoStyle
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
  font-weight: 600;
</style>
